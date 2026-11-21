# CurrencyConverter

一个使用 Kotlin 与 Jetpack Compose 构建的 Android 本地汇率换算应用。

## 当前阶段：项目初始化

- Android 原生 Kotlin + Jetpack Compose
- Material 3 UI
- 最低支持 Android 8.0（API 26）
- 目标 SDK 35
- 已声明网络访问权限，后续用于接入 Exchange Rate API
- 已接入 Retrofit、OkHttp 和 Kotlin Serialization 网络层
- 已接入 Room 数据库，支持汇率和更新记录的事务保存
- 已实现每日首次访问更新、失败回退、并发保护和手动刷新
- 已实现基于 BigDecimal 的本地换算核心和币种搜索目录
- 已完成 Compose 单页面换算界面、币种搜索、交换和更新状态展示
- API Key 从 Gradle 属性或环境变量 `EXCHANGE_RATE_API_KEY` 读取，不写入源码

## 打开与运行

1. 使用 Android Studio 打开本目录。
2. 等待 Gradle 同步完成。
3. 选择模拟器或已连接的 Android 设备。
4. 点击 Run 运行 `app` 配置。

当前项目已完成网络层、Room 数据库、每日更新、离线换算和 Compose 主界面。

### 使用 VS Code 轻量开发

项目包含 `.vscode` 配置。打开项目目录后，按提示安装 Java、Kotlin 和 Android 扩展。运行前需要安装 JDK 17、Android SDK（含 Platform 35 和 Build Tools），并设置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`；要安装到设备还需安装 ADB 并启用设备调试。

在 VS Code 的“运行任务”中可执行 `Android: Assemble Debug`、`Android: Run Unit Tests` 或 `Android: Install Debug on Device`。当前仓库没有 Gradle Wrapper 脚本，因此任务会使用系统安装的 Gradle；若未安装 Gradle，请先安装，或在 Android Studio 中生成并提交 Wrapper 文件。APK 输出在 `app/build/outputs/apk/debug/app-debug.apk`。

VS Code 可编辑 Kotlin、查看 Java/Kotlin 项目并运行 Gradle 任务；Compose Preview、布局检查和完整 Android 调试体验仍建议使用 Android Studio。

## 配置 Exchange Rate API Key

在 Android Studio 的 Gradle 用户属性文件中加入：

```properties
EXCHANGE_RATE_API_KEY=你的_API_Key
```

也可以设置同名环境变量。未配置 Key 时，网络层会明确报错，不会发起请求。

测试清单和 APK 构建说明见 [TESTING.md](TESTING.md)。
