# NarutoOverlay

[English](README-EN.md) | **简体中文**

> _这是一个信息显示模组，倾注了我对漩涡鸣人深沉的爱。_
>
> _——世界上最幸福的小猪_

NarutoOverlay 是一个面向 **Minecraft 1.20.1 Forge** 的客户端 HUD 信息模组。它默认只显示瞬时帧率、半透明背板和文字流光，其他信息（最低/平均/1% 低帧、GC、内存）都可以在游戏内配置界面按需打开。

配置界面基于 [ModernUI](https://www.curseforge.com/minecraft/mc-mods/modern-ui)，强烈建议安装；没有 ModernUI 时 HUD 仍可使用，但无法打开可视化配置界面。

---

## 目录

- [特点](#特点)
- [安装](#安装)
- [快速上手](#快速上手)
- [操作方式](#操作方式)
- [配置界面](#配置界面)
- [背景与轮播](#背景与轮播)
- [文字流光](#文字流光)
- [高级配置](#高级配置)
- [性能建议](#性能建议)
- [常见问题](#常见问题)
- [许可与致谢](#许可与致谢)

---

## 特点

### HUD 卡片

| 卡片 | 默认状态 | 字段 |
|---|---|---|
| **FPS** | 开启 | 瞬时帧率（默认开启）、最低帧率、平均帧率、1% 低帧 |
| **GC** | 关闭 | 回收器名称、上一次 GC 耗时、距离上一次 GC 的时间 |
| **RAM** | 关闭 | 已用/最大内存、占用百分比（默认关闭）、最大内存中的已分配内存 |

- 每张卡片都可以单独开关、调整位置、选择显示哪些字段，以及是否显示字段的“标签”和“单位”。
- FPS 低于 60 显示黄色，低于 30 显示红色；RAM 占用达到 75% 显示黄色，达到 90% 显示红色。
- 卡片可以设为等宽，方便多张卡片上下对齐。
- 支持文字阴影、半透明背板、圆角边框、自定义边框颜色。

### 主题

- **NARUTO**：从九尾查克拉模式取色的暖黑、火焰橙、金色、查克拉黄与红色点缀。
- **CLASSIC**：纯白文字配半透明灰色背板。
- 主题只影响配色；文字流光只在 NARUTO 主题下可用。

### 文字流光

- 金色查克拉高光扫过 HUD 与配置界面的文字，NARUTO 主题下可开启。
- 支持按类别开关：浮层文字、配置文字、控件与装饰（开关、滑条、Logo 圆、ESC 提示边框）。
- 可调涟漪速度、密度、颜色强度，以及 HUD / 配置界面的更新率。
- HUD 使用缓存重着色，避免逐帧为每个字形创建 `Style`/`TextColor`。

### 配置界面背景

- 支持单张背景图，也支持整个文件夹的图片轮播与交叉淡入淡出。
- 启动时自动把内置的 `bg1.jpg`、`bg2.jpg`、`logo.jpg` 解压到 `<游戏目录>/narutooverlay` 并作为默认轮播。
- 支持选择文件夹、选择单图、裁剪单图、调节轮播间隔、淡入时长和背景暗度。

---

## 安装

1. 安装 Minecraft 1.20.1 与对应版本的 Forge。
2. 将 `narutooverlay-1.20.1-1.0.0.jar` 放入 `.minecraft/mods`。
3. 强烈建议同时安装 **ModernUI 3.12.0+**，否则只能使用 HUD，无法打开配置界面。
4. 启动游戏。

首次启动会在游戏目录下创建 `narutooverlay` 文件夹并解压默认背景图。

---

## 快速上手

- 默认只显示左上角的 **瞬时帧率** 卡片。
- 打开聊天栏（默认 `T`），**左键拖动** 任意卡片可以移动位置。
- 打开聊天栏，**右键点击** 任意卡片可以打开配置界面。
- 也可以在 **Mods → NarutoOverlay → Config** 中打开配置界面。
- 在配置界面按 `ESC` 保存并关闭。

如果卡片被拖到屏幕外，可以在 **控制 → 按键绑定** 中给“Reset Text Positions”绑定一个键，然后按该键恢复所有卡片默认位置。

---

## 操作方式

| 操作 | 方式 |
|---|---|
| 移动卡片 | 打开聊天栏，左键按住卡片拖动 |
| 打开配置界面 | 打开聊天栏，右键点击卡片；或 Mods 列表中的 Config |
| 保存并关闭配置 | `ESC` |
| 恢复所有卡片位置 | 绑定 “Reset Text Positions” 按键后按下 |
| 恢复单张卡片 | Cards 页面中的 “Reset this card” |
| 恢复全部卡片 | Cards 页面底部的 “Reset all settings”（只重置卡片，不会重置外观/背景） |

---

## 配置界面

配置界面分为三页：

### Cards（卡片）

- 每张卡片的开关。
- X / Y 坐标、左上/右上/左下/右下快速定位。
- 字段列表：单独开关每个字段，切换是否显示“标签”和“单位”。
- 单张卡片恢复默认、全部卡片恢复默认。

### Appearance（外观）

- **主题**：NARUTO / CLASSIC。
- **显示**：文字阴影、等宽卡片、绘制背板、文字流光总开关。
- **流光参数**：类别开关、速度、密度、颜色强度、HUD 更新率、配置更新率、缓存重着色。
- **背板边框**：边框宽度、自定义颜色（RGBA）。
- **性能**：FPS / GC / RAM 的更新间隔，FPS 统计窗口。

### About（关于）

- 模组版本、运行环境、配置文件路径。
- 操作提示。
- 背景设置：选择文件夹、清除文件夹、选择单图、图片数量、轮播间隔、淡入时长、背景暗度。

---

## 背景与轮播

### 默认行为

启动时会把 jar 内的三张图解压到：

```text
<游戏目录>/narutooverlay/
├── bg1.jpg
├── bg2.jpg
└── logo.jpg
```

默认配置：

```toml
[ConfigScreen]
backgroundFolder = "narutooverlay"
backgroundOrder = ["bg1.jpg", "bg2.jpg", "logo.jpg"]
backgroundImage = "narutooverlay/bg1.jpg"
```

- 已存在的文件**不会覆盖**，可以直接替换成自己的图片。
- 文件夹里的所有图片都会参与轮播；`backgroundOrder` 中列出的文件排在最前，其余按文件名排序跟在后面。
- 轮播图片自动居中裁剪铺满窗口；单图模式才使用 `Crop` 区域。
- 如果 `backgroundFolder` 为空，则回退到单张 `backgroundImage`。

> 如果只想轮播内置的三张图，把 `narutooverlay` 文件夹里其他图片移走即可。

### 单图模式

- 在 About 页选择一张图片，会复制为 `<游戏目录>/narutooverlay-config-bg.<扩展名>`。
- 可以用裁剪界面选择可见区域；裁剪只记录在配置里，不会改写原图。
- 清空文件夹后就会使用这张单图。

### 支持的图片格式

`png`、`jpg`、`jpeg`、`bmp`、`gif`。文件夹只扫描第一层，不递归子文件夹。

---

## 文字流光

流光只在 **NARUTO** 主题下生效，CLASSIC 下不可用。

### 类别

| 开关 | 控制内容 |
|---|---|
| 浮层文字 | 游戏内 HUD 卡片文字 |
| 配置文字 | 配置界面的标题、标签、数值、按钮文字 |
| 控件与装饰 | 开关、滑条、Logo 圆、ESC 提示边框 |

总开关“文字流光”关闭后，三个子类别会一起停止并恢复基础颜色。

### 参数

| 参数 | 范围 | 默认 | 说明 |
|---|---|---|---|
| 涟漪速度 | 0.2–3.0 | 2.5 | 色带漂移快慢 |
| 涟漪密度 | 0.2–3.0 | 2.5 | 空间频率，越大波纹越密 |
| 颜色强度 | 0–100% | 100% | 0 保持基础色，数值越大越接近完整涟漪色 |
| 浮层更新率 | 5–240 Hz | 30 | HUD 颜色重算频率，越低越省 CPU/GC |
| 配置更新率 | 5–120 Hz | 30 | 配置界面颜色更新频率 |
| 缓存重新着色 | 开/关 | 开 | 关闭后回退到逐帧着色，仅建议用于对比 |

HUD 的缓存会在每次更新时生成一份已着色的 `FormattedCharSequence` 快照，缓存命中帧不再做逐字 `sin`、`Style`、`TextColor` 分配。

---

## 高级配置

配置文件：

```text
<游戏目录>/config/narutooverlay-client.toml
```

常用键：

| 键 | 说明 |
|---|---|
| `Quality.theme` | `NARUTO` / `CLASSIC` |
| `Quality.textShimmer` | 文字流光总开关 |
| `Quality.hudTextShimmer` | HUD 文字流光 |
| `Quality.configTextShimmer` | 配置界面文字流光 |
| `Quality.controlShimmer` | 开关 / 滑条 / Logo / ESC 边框流光 |
| `Quality.textShimmerSpeed` | 涟漪速度 |
| `Quality.textShimmerScale` | 涟漪密度 |
| `Quality.textShimmerStrength` | 颜色强度 |
| `Quality.textShimmerFps` | HUD 更新率 |
| `Quality.configShimmerFps` | 配置界面更新率 |
| `Quality.textShimmerCache` | HUD 缓存重着色 |
| `Quality.textOffsetY` | HUD 文字在背板内的垂直偏移（半像素级） |
| `ConfigScreen.backgroundFolder` | 轮播文件夹，相对游戏目录 |
| `ConfigScreen.backgroundOrder` | 轮播顺序，未列出的图片按文件名排在后面 |
| `ConfigScreen.backgroundImage` | 文件夹为空时的单图兜底 |
| `ConfigScreen.backgroundInterval` | 每张图停留秒数（2–300） |
| `ConfigScreen.backgroundFade` | 交叉淡入秒数（0–3） |
| `ConfigScreen.backgroundDim` | 背景暗度（0–0.95） |

绝大多数选项都可以在游戏内直接调，不需要手改 TOML。

---

## 性能建议

- 默认 HUD 只显示瞬时帧率，开销很小。
- 如果同时打开很多卡片/字段，又把“浮层更新率”拉到 240 Hz，会明显增加每帧的字体绘制和 GC 压力。
- 高刷新率或 CPU 瓶颈时，建议：
  - 把 **浮层更新率** 降到 20–30 Hz；
  - 只保留“浮层文字”，关闭“配置文字/控件与装饰”；
  - 保持 **缓存重新着色** 开启；
  - 关闭不需要的卡片和字段；
  - 适当增大 FPS/GC/RAM 的更新间隔。

HUD 的流光只在游戏内 HUD 渲染时运行；配置界面的流光只在设置界面打开时运行，不会在后台常驻。

---

## 常见问题

**Q：配置界面打不开 / 没有 Config 按钮？**  
A：安装 ModernUI 3.12.0+。HUD 本身不依赖 ModernUI，但配置界面依赖它。

**Q：文字流光没有效果？**  
A：确认主题是 NARUTO、总开关和对应类别开关已打开、颜色强度大于 0；CLASSIC 主题下流光不可用。

**Q：背景没有轮播？**  
A：确认 `backgroundFolder` 指向的文件夹存在，且图片直接放在第一层；检查 `backgroundOrder` 中的文件名是否与实际文件一致。配置界面 About 页可以看到图片数量。

**Q：为什么轮播里出现了我没放进去的图片？**  
A：文件夹中的所有图片都会参与轮播，`backgroundOrder` 只决定顺序，不会排除未列出的图片。把不需要的图片移走即可。

**Q：Logo 被裁切了？**  
A：轮播中的图片统一自动居中裁剪铺满。想让 Logo 完整显示，可以改用单图模式 + 裁剪，或换一张比例更合适的图片。

**Q：卡片拖出屏幕了怎么办？**  
A：在控制里给 “Reset Text Positions” 绑定按键，按一下恢复全部位置；或在 Cards 页使用 “Reset all settings”（只重置卡片）。

**Q：怎么彻底恢复默认？**  
A：删除 `config/narutooverlay-client.toml`，重启游戏；`narutooverlay` 背景文件夹不会被删除。

**Q：这个模组需要装在服务器上吗？**  
A：不需要。它是客户端 HUD 模组，服务器无需安装。

---

## 许可与致谢

- 作者：**Kasualix**, **DeepSeek-V4.1-Flash-1017**
- 许可：[LGPL-3.0](https://github.com/MCTeamPotato/NarutoOverlay?tab=LGPL-3.0-1-ov-file)
- 配置界面与文本渲染使用了 [ModernUI](https://www.curseforge.com/minecraft/mc-mods/modern-ui)，感谢 BloCamLimb 与其贡献者。
- 感谢 Minecraft、Forge，以及 Naruto。

> _「流光破暗，帧定乾坤」_