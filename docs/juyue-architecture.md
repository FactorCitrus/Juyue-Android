# 橘阅 Android 重构架构（Compose 主框架）

状态：第一版基线。目标是与 iOS 版（`app/Legado-iOS`）界面与功能一致。
旧 XML 页面只保留为数据层/引擎调用方，不进入新主流程。

## 1. 边界

- 新入口：`io.legado.app.ui.juyue.JuyueMainActivity`（已设为 launcher）。
- 复用层：Room（`io.legado.app.data`）、书源引擎（`model.webBook`、`model.analyzeRule`）、
  Rhino JS、下载、TTS、WebDAV（`help.AppWebDav`）、RSS。
- 不改造：`ui/main` 等 XML Fragment 与 Activity 原样保留，供内部功能深链调用。
- 视觉基线：暖纸 `#D9D4C3`、纸面 `#E7E1D1`、凸起 `#F1ECDE`、墨 `#171715`、
  次级墨 `#6F6A5E`、分隔 `#9A9485`、氧化红 `#B33A2B`；圆角 2dp、边框 0.5dp、
  28dp 网格、悬浮胶囊底栏。

## 2. 目录

```
app/src/main/java/io/legado/app/ui/juyue/
  JuyueMainActivity.kt        # 唯一 Activity：edge-to-edge + setContent
  shell/
    JuyueApp.kt               # 隐私协议门 -> 主壳
    JuyueDestinations.kt      # 路由密封类 + 返回栈
    JuyueBottomBar.kt         # 悬浮胶囊底栏（书架/分类/设置）
    JuyueBackdrop.kt          # 暖纸底 + 28dp 网格
  theme/
    JuyueTheme.kt             # Palette / Typography / MaterialTheme 映射
    JuyueTokens.kt            # 间距、圆角、边框、字号常量
  components/
    JuyuePanel.kt             # 2dp 方角面板、0.5dp 边框、左侧红条
    JuyueChip.kt              # 分组/书源胶囊
    JuyueRule.kt              # 红短线 + 细线 + 方块分隔记号
    JuyueEmptyState.kt
  bookshelf/
    BookshelfScreen.kt        # 标题锁定 + 档案库面板 + 分组栏 + 网格
    BookshelfCard.kt          # 封面、书名、作者、进度、角标
  category/
    CategoryScreen.kt         # 书源胶囊 + 分类栏 + 书籍流
    CategoryBookRow.kt
  detail/
    BookDetailScreen.kt       # 封面/元信息/简介/目录入口/阅读入口
  settings/
    SettingsScreen.kt         # 5 模块宫格：01 阅读 02 外观 03 内容源 04 数据 05 系统
    SettingsBoard.kt          # 模块展开页
    SystemPages.kt            # 本地密码、隐私、备份、书源更新、诊断日志
  reader/
    ReaderHost.kt             # 第一版桥接旧阅读器 Activity；后续替换为 Compose
  util/
    JuyueFormatters.kt
```

## 3. 导航

- 单一 `rememberSaveable` 返回栈 `List<JuyueRoute>`，`BackHandler` 出栈。
- 底栏三入口固定，进详情/设置子页时底栏隐藏。
- 阅读器第一版通过 `Intent` 调起既有阅读 Activity，保持引擎能力；第二阶段用 Compose
  阅读器替换，数据仍走 `ReadBookConfig` / `BookContent`。

## 4. 数据流

- 书架：`appDb.bookDao.flowByGroup(BookGroup.IdAll)` + `bookGroupDao.flowSelect()`。
- 分类：`appDb.bookSourceDao` 取启用的书源，`WebBook` 拉分类与列表，分页走 `SearchBook`。
- 详情：`Book` 实体 + `appDb.bookChapterDao`，阅读入口写 `durChapterIndex`。
- 设置：`LocalConfig`、`ReadBookConfig`、`AppWebDav`、`appDb.bookSourceDao` 直连。

## 5. 阶段计划

| 阶段 | 内容 | 验收 |
| --- | --- | --- |
| P0 | 架构文档、目录骨架、构建基线 | `assembleDebug` 通过 |
| P1 | 壳层：隐私门、网格纸底、悬浮胶囊底栏、三入口 | 安装 + 三页截图 |
| P2 | 书架：标题锁定、档案库面板、真实分组、书籍网格、空态 | 截图 + 分组切换 |
| P3 | 分类：书源切换、分类栏、书籍流、分页 | 已完成：本地测试源端到端验证 |
| P4 | 详情：封面、元信息、简介、目录、开始阅读 | 截图 + 阅读器跳转 |
| P5 | 设置：五模块宫格 + 子页（密码/隐私/备份/书源更新/诊断） | 截图 + 每页可达 |
| P6 | 阅读器 Compose 化 + RSS/WebDAV/TTS 入口 | 截图 + 功能回归 |

每阶段必须：`assembleDebug` → `adb install -r` → 启动截图 → 对照 iOS 修正后再进入下一阶段。
