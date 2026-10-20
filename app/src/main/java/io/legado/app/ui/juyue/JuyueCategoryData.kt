package io.legado.app.ui.juyue

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSource
import io.legado.app.model.analyzeRule.AnalyzeUrl
import org.json.JSONArray

data class JuyueCategory(
    val title: String,
    val url: String?,
)

/**
 * 解析书源 exploreUrl：支持 &lt;js&gt; 脚本、@js: 脚本、JSON 数组，
 * 以及 "标题::地址" 换行文本。与 iOS BookExploreService 的规则保持一致。
 */
fun parseJuyueCategories(source: BookSource): List<JuyueCategory> {
    val raw = source.exploreUrl?.trim().orEmpty()
    if (raw.isEmpty()) return emptyList()
    var text = raw
    var isScript = false
    // 走 AnalyzeUrl 的脚本环境，可绑定 source / java / page，结果就是分类 JSON。
    runCatching {
        when {
            raw.contains("<js>") || raw.startsWith("@js:") -> {
                isScript = true
                text = AnalyzeUrl(
                    mUrl = raw,
                    page = 1,
                    baseUrl = source.bookSourceUrl,
                    source = source,
                ).url.ifBlank { raw }
            }
        }
    }.onFailure { Log.d("JuyueCat", "exploreUrl eval failed: ${it.message}") }
    Log.d("JuyueCat", "parsed text: ${text.take(300)}")
    // 脚本执行失败时，原文只是 JS 代码，不能再按换行拆成分类。
    if (isScript && (text.startsWith("@js:") || text.contains("<js>"))) return emptyList()
    parseCategoryText(text)?.let {
        Log.d("JuyueCat", "categories: ${it.size}")
        if (it.isNotEmpty()) return it
    }
    return emptyList()
}

private fun parseCategoryText(text: String): List<JuyueCategory>? {
    val trimmed = text.trim()
    if (trimmed.startsWith("[")) {
        runCatching {
            val array = JSONArray(trimmed)
            val list = ArrayList<JuyueCategory>(array.length())
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val title = item.optString("title").ifBlank { item.optString("name") }
                if (title.isBlank()) continue
                val url = item.optString("url").ifBlank { null }
                list.add(JuyueCategory(title, url))
            }
            if (list.isNotEmpty()) return list
        }
    }
    val list = ArrayList<JuyueCategory>()
    trimmed.lines().forEach { line ->
        val value = line.trim()
        if (value.isEmpty()) return@forEach
        if (value.contains("::")) {
            val parts = value.split("::", limit = 2)
            list.add(JuyueCategory(parts[0].trim(), parts.getOrNull(1)?.trim()?.ifBlank { null }))
        } else {
            list.add(JuyueCategory(value, null))
        }
    }
    return list
}

/** 导入内置书源，返回新增条数。 */
suspend fun importBundledBookSources(context: Context): Int {
    val json = context.assets.open("defaultData/bookSources.json")
        .bufferedReader()
        .use { it.readText() }
    val sources = Gson().fromJson(json, Array<BookSource>::class.java) ?: return 0
    if (sources.isEmpty()) return 0
    val existing = appDb.bookSourceDao.all.map { it.bookSourceUrl }.toSet()
    val fresh = sources.filter { it.bookSourceUrl !in existing }
    if (fresh.isNotEmpty()) {
        appDb.bookSourceDao.insert(*fresh.toTypedArray())
    }
    return fresh.size
}
