# 测试与构建说明

## 配置 API Key

在用户 Gradle 属性文件中配置，或设置环境变量：

```properties
EXCHANGE_RATE_API_KEY=你的_API_Key
```

API Key 不应提交到 Git 仓库。

## 单元测试

在 Android Studio 的 Gradle 面板中运行：

```text
app > Tasks > verification > test
```

当前已覆盖：

- 基础货币到目标货币的换算
- 目标货币到基础货币的换算
- 空值、非法字符和负数输入
- 缺少汇率时的安全处理
- 币种代码和中文名称搜索

## 手工测试清单

1. 首次启动且 API Key、网络均正常：应获取并保存汇率。
2. 首次启动无网络：应显示无本地数据提示，并允许重试。
3. 当天再次启动：应使用 Room 数据，不重复请求。
4. 修改设备日期到下一天：应再次触发自动更新。
5. 更新失败：应保留旧汇率，不清空数据库。
6. 点击手动更新：应显示加载状态和结果。
7. 输入金额、切换币种、交换币种：结果应立即更新。
8. 输入负数或字母：输入框不接受非法金额。
9. 切换系统深色模式：页面应正常显示。

## 构建 APK

在 Android Studio 中选择 `app` 配置并运行，可生成 Debug APK。

Debug APK 通常位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

生成 Release APK 前，需要在 Android Studio 中配置签名密钥。签名文件和密码不得提交到仓库。
