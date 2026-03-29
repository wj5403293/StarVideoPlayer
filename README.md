# StarVideoPlayer
[![](https://jitpack.io/v/1240444767/StarVideoPlayer.svg)](https://jitpack.io/#1240444767/StarVideoPlayer)
![GitHub Repo stars](https://img.shields.io/github/stars/1240444767/StarVideoPlayer)

一个功能强大的 Android 视频播放器库，专为短剧/视频应用设计，基于 DKPlayer 开发。

## 功能特性

- **播放内核** - 支持自定义播放内核（IJKPlayer、ExoPlayer 等），用户自行选择引入
- **选集功能** - 剧集列表展示与快速切换，支持自定义适配器
- **倍速播放** - 点击倍速按钮弹出菜单选择（0.5x ~ 3.0x）
- **长按倍速** - 长按屏幕快速播放（默认3倍速，可自定义1.0x~10.0x）
- **双击暂停/播放** - 双击屏幕切换播放/暂停状态
- **定时关闭** - 支持30分钟、60分钟定时关闭
- **跳过片头/片尾** - 自动跳过指定时间段
- **画面比例调整** - 支持16:9、4:3、默认、填充、缩放、裁剪等模式
- **静音控制** - 一键静音/取消静音
- **手势控制** - 滑动调节亮度、音量、进度
- **全屏/小窗模式** - 支持全屏播放和画中画
- **投屏功能接口** - 预留投屏回调接口
- **刘海屏适配** - 完美适配刘海屏设备
- **锁屏功能** - 全屏模式下可锁定控制栏
- **隐藏进度条** - 可选择隐藏底部进度条
- **自动旋转** - 支持根据设备方向自动切换横竖屏
- **竖屏全屏** - 支持竖屏全屏模式，适合短剧场景
- **按钮可见性控制** - 可控制底部和顶部各按钮的显示/隐藏，支持区分全屏/非全屏状态
- **短剧播放器** - 提供精简版播放器 `StarShortDramaPlayer`，隐藏选集/上下集按钮

## 引入方式

### Step 1. 添加 JitPack 仓库

在项目根目录的 `settings.gradle` 或 `build.gradle` 中添加：

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. 添加依赖

```groovy
dependencies {
    implementation 'xyz.doikki.android.dkplayer:dkplayer-java:3.3.7'
    // 根据需要选择播放内核（二选一或都引入）
    implementation 'xyz.doikki.android.dkplayer:player-exo:3.3.7'   // ExoPlayer
    // implementation 'xyz.doikki.android.dkplayer:player-ijk:3.3.7'  // IJKPlayer
    implementation 'com.github.1240444767:StarVideoPlayer:1.3.0'
}
```

## 使用方法

### 1. 添加权限

在 `AndroidManifest.xml` 中添加必要权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 2. 布局文件

#### 标准播放器（带选集功能）

```xml
<com.star.play.StarVideoPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

#### 短剧播放器（精简版，无选集/上下集按钮）

```xml
<com.star.play.StarShortDramaPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 3. 基础使用

#### StarVideoPlayer 使用

```java
public class MainActivity extends AppCompatActivity {
    private StarVideoPlayer videoView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        videoView = findViewById(R.id.player);
        
        // 必须设置播放内核（根据你引入的依赖选择）
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create());  // ExoPlayer
        // videoView.setPlayerFactory(IjkPlayerFactory.create());    // IJKPlayer
        
        // 设置视频地址
        videoView.setUrl("https://example.com/video.mp4");
        
        // 添加控制器
        videoView.addDefaultControlComponent("视频标题", false);
        
        // 开始播放
        videoView.start();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }
    
    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
```

#### StarShortDramaPlayer 使用（短剧专用）

`StarShortDramaPlayer` 继承自 `StarVideoPlayer`，自动隐藏了选集、上一集、下一集按钮。

```java
public class ShortDramaActivity extends AppCompatActivity {
    private StarShortDramaPlayer videoView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_drama);
        
        videoView = findViewById(R.id.player);
        
        // 必须设置播放内核
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create());
        videoView.setUrl("https://example.com/video.m3u8");
        videoView.addDefaultControlComponent("短剧标题", false);
        videoView.start();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }
    
    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
```

### 4. 选集功能

#### 方式一：使用默认适配器

```java
// 设置剧集列表
List<String> episodes = Arrays.asList("第1集", "第2集", "第3集", "第4集");
videoView.setEpisodes(episodes, 0);  // 第二个参数为当前选中索引

// 设置选集监听器
videoView.setOnEpisodeSelectListener((index, title) -> {
    // 切换视频源
    videoView.setUrl(getEpisodeUrl(index));
    videoView.start();
});
```

#### 方式二：使用自定义适配器

```java
// 创建自定义适配器
MyEpisodeAdapter adapter = new MyEpisodeAdapter(episodeList);
videoView.setEpisodeAdapter(adapter);

// 如果需要获取 RecyclerView 进行更多操作
RecyclerView recyclerView = videoView.getEpisodeRecyclerView();
```

#### 上一集/下一集

```java
// 上一集
videoView.setOnUpSetClickListener(view -> {
    // 处理上一集逻辑
});

// 下一集
videoView.setOnDownSetClickListener(view -> {
    // 处理下一集逻辑
});
```

### 5. 其他功能

```java
// 小窗播放
videoView.setOnWindowClickListener(view -> {
    // 启动小窗播放
});

// 投屏功能
videoView.setOnScreenClickListener(view -> {
    // 启动投屏
});

// 设置跳过片头片尾（秒）
videoView.setSkipStartTime(30);   // 跳过片头30秒
videoView.setSkipEndTime(60);     // 跳过片尾60秒

// 设置长按倍速
videoView.setLongPressSpeed(3.0f);  // 长按时3倍速播放

// 设置播放速度
videoView.setPlaybackSpeed(1.5f);   // 1.5倍速播放

// 设置静音
videoView.setMuted(true);

// 设置画面比例
videoView.setScreenScale(VideoView.SCREEN_SCALE_16_9);

// 隐藏底部进度条
videoView.setHideProgress(true);

// 开启自动旋转
videoView.setAutoRotate(true);

// 设置播放内核
videoView.setPlayerKernel("ExoPlayer");  // 或 "IJKPlayer"

// 监听播放内核切换
videoView.setOnPlayerKernelChangeListener(kernel -> {
    // 内核切换，需要重新设置播放源并开始播放
    videoView.setPlayerFactory(
        "ExoPlayer".equals(kernel) 
            ? ExoMediaPlayerFactory.create() 
            : IjkPlayerFactory.create()
    );
    videoView.setUrl(currentUrl);
    videoView.start();
});

// 显示竖屏全屏按钮
videoView.setFullscreenPortraitButtonVisibility(View.VISIBLE);

// 设置竖屏全屏点击监听（可选，默认已有竖屏全屏功能）
videoView.setOnFullscreenPortraitClickListener(view -> {
    // 额外操作，如记录日志等
});
```

### 6. 按钮可见性控制

#### 全局设置（同时影响全屏和非全屏）

```java
// 隐藏底部按钮（4个参数：选集、倍速、上一集、下一集）
videoView.setVisibilityBottom(View.GONE, View.GONE, View.GONE, View.GONE);

// 隐藏底部按钮（6个参数：选集、倍速、上一集、下一集、全屏、竖屏全屏）
videoView.setVisibilityBottom(
    View.GONE, View.GONE, View.GONE, View.GONE,  // 选集、倍速、上一集、下一集
    View.GONE, View.VISIBLE                       // 全屏、竖屏全屏
);

// 单独控制底部按钮（全局）
videoView.setSelectButtonVisibility(View.GONE);      // 选集按钮
videoView.setSpeedButtonVisibility(View.GONE);       // 倍速按钮
videoView.setPreviousButtonVisibility(View.GONE);    // 上一集按钮
videoView.setNextButtonVisibility(View.GONE);        // 下一集按钮
videoView.setFullscreenButtonVisibility(View.GONE);  // 全屏按钮
videoView.setFullscreenPortraitButtonVisibility(View.VISIBLE);  // 竖屏全屏按钮

// 隐藏顶部按钮（返回、小窗、投屏、设置）
videoView.setTitleButtonsVisibility(View.GONE, View.GONE, View.GONE, View.GONE);

// 单独控制顶部按钮
videoView.setBackButtonVisibility(View.GONE);      // 返回按钮
videoView.setPipButtonVisibility(View.GONE);       // 小窗按钮
videoView.setScreenButtonVisibility(View.GONE);    // 投屏按钮
videoView.setSettingsButtonVisibility(View.GONE);  // 设置按钮
videoView.setSysTimeVisibility(View.GONE);         // 系统时间
```

#### 区分全屏/非全屏设置

```java
// 设置非全屏状态下的底部按钮可见性（4个参数）
videoView.setVisibilityBottomNormal(View.GONE, View.GONE, View.GONE, View.GONE);

// 设置非全屏状态下的底部按钮可见性（6个参数）
videoView.setVisibilityBottomNormal(
    View.GONE, View.GONE, View.GONE, View.GONE,  // 选集、倍速、上一集、下一集
    View.VISIBLE, View.GONE                       // 全屏、竖屏全屏
);

// 设置全屏状态下的底部按钮可见性（4个参数）
videoView.setVisibilityBottomFullscreen(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);

// 设置全屏状态下的底部按钮可见性（6个参数）
videoView.setVisibilityBottomFullscreen(
    View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE,  // 选集、倍速、上一集、下一集
    View.GONE, View.GONE                                       // 全屏、竖屏全屏
);

// 一次性设置全屏和非全屏的按钮可见性（12个参数）
videoView.setVisibilityBottomAll(
    // 非全屏状态
    View.GONE, View.GONE, View.GONE, View.GONE,  // 选集、倍速、上一集、下一集
    View.VISIBLE, View.GONE,                      // 全屏、竖屏全屏
    // 全屏状态
    View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE,  // 选集、倍速、上一集、下一集
    View.GONE, View.GONE                          // 全屏、竖屏全屏
);

// 单独控制各按钮在非全屏状态下的可见性
videoView.setSelectButtonVisibilityNormal(View.GONE);
videoView.setSpeedButtonVisibilityNormal(View.GONE);
videoView.setPreviousButtonVisibilityNormal(View.GONE);
videoView.setNextButtonVisibilityNormal(View.GONE);
videoView.setFullscreenButtonVisibilityNormal(View.VISIBLE);
videoView.setFullscreenPortraitButtonVisibilityNormal(View.GONE);

// 单独控制各按钮在全屏状态下的可见性
videoView.setSelectButtonVisibilityFullscreen(View.VISIBLE);
videoView.setSpeedButtonVisibilityFullscreen(View.VISIBLE);
videoView.setPreviousButtonVisibilityFullscreen(View.VISIBLE);
videoView.setNextButtonVisibilityFullscreen(View.VISIBLE);
videoView.setFullscreenButtonVisibilityFullscreen(View.GONE);
videoView.setFullscreenPortraitButtonVisibilityFullscreen(View.GONE);
```

## API 说明

### StarVideoPlayer 主要方法

| 方法 | 说明 |
|------|------|
| `setUrl(String url)` | 设置视频地址 |
| `setPlayerFactory(PlayerFactory factory)` | 设置播放内核 |
| `setTitle(String title)` | 设置视频标题 |
| `setEpisodes(List<String> episodes, int currentIndex)` | 设置剧集列表（使用默认适配器） |
| `setEpisodeAdapter(RecyclerView.Adapter<?> adapter)` | 设置自定义选集适配器 |
| `getEpisodeAdapter()` | 获取当前选集适配器 |
| `getEpisodeRecyclerView()` | 获取选集 RecyclerView |
| `setSkipStartTime(int seconds)` | 设置跳过片头时间 |
| `setSkipEndTime(int seconds)` | 设置跳过片尾时间 |
| `setLongPressSpeed(float speed)` | 设置长按倍速 |
| `setPlaybackSpeed(float speed)` | 设置播放速度 |
| `setMuted(boolean mute)` | 设置静音 |
| `setScreenScale(int scaleType)` | 设置画面比例 |
| `setHideProgress(boolean hide)` | 设置是否隐藏底部进度条 |
| `setAutoRotate(boolean autoRotate)` | 设置是否开启自动旋转 |
| `setVisibilityBottom(select, speed, previous, next)` | 设置底部按钮可见性（4参数，全局） |
| `setVisibilityBottom(select, speed, previous, next, fullscreen, fullscreenPortrait)` | 设置底部按钮可见性（6参数，全局） |
| `setVisibilityBottomNormal(select, speed, previous, next)` | 设置非全屏状态下底部按钮可见性（4参数） |
| `setVisibilityBottomNormal(select, speed, previous, next, fullscreen, fullscreenPortrait)` | 设置非全屏状态下底部按钮可见性（6参数） |
| `setVisibilityBottomFullscreen(select, speed, previous, next)` | 设置全屏状态下底部按钮可见性（4参数） |
| `setVisibilityBottomFullscreen(select, speed, previous, next, fullscreen, fullscreenPortrait)` | 设置全屏状态下底部按钮可见性（6参数） |
| `setVisibilityBottomAll(...)` | 一次性设置全屏和非全屏按钮可见性（12参数） |
| `setSelectButtonVisibility(visibility)` | 设置选集按钮可见性（全局） |
| `setSelectButtonVisibilityNormal(visibility)` | 设置非全屏状态下选集按钮可见性 |
| `setSelectButtonVisibilityFullscreen(visibility)` | 设置全屏状态下选集按钮可见性 |
| `setSpeedButtonVisibility(visibility)` | 设置倍速按钮可见性（全局） |
| `setSpeedButtonVisibilityNormal(visibility)` | 设置非全屏状态下倍速按钮可见性 |
| `setSpeedButtonVisibilityFullscreen(visibility)` | 设置全屏状态下倍速按钮可见性 |
| `setPreviousButtonVisibility(visibility)` | 设置上一集按钮可见性（全局） |
| `setPreviousButtonVisibilityNormal(visibility)` | 设置非全屏状态下上一集按钮可见性 |
| `setPreviousButtonVisibilityFullscreen(visibility)` | 设置全屏状态下上一集按钮可见性 |
| `setNextButtonVisibility(visibility)` | 设置下一集按钮可见性（全局） |
| `setNextButtonVisibilityNormal(visibility)` | 设置非全屏状态下下一集按钮可见性 |
| `setNextButtonVisibilityFullscreen(visibility)` | 设置全屏状态下下一集按钮可见性 |
| `setFullscreenButtonVisibility(visibility)` | 设置全屏按钮可见性（全局） |
| `setFullscreenButtonVisibilityNormal(visibility)` | 设置非全屏状态下全屏按钮可见性 |
| `setFullscreenButtonVisibilityFullscreen(visibility)` | 设置全屏状态下全屏按钮可见性 |
| `setFullscreenPortraitButtonVisibility(visibility)` | 设置竖屏全屏按钮可见性（全局） |
| `setFullscreenPortraitButtonVisibilityNormal(visibility)` | 设置非全屏状态下竖屏全屏按钮可见性 |
| `setFullscreenPortraitButtonVisibilityFullscreen(visibility)` | 设置全屏状态下竖屏全屏按钮可见性 |
| `setTitleButtonsVisibility(back, pip, screen, settings)` | 设置顶部按钮可见性 |
| `setBackButtonVisibility(visibility)` | 设置返回按钮可见性 |
| `setPipButtonVisibility(visibility)` | 设置小窗按钮可见性 |
| `setScreenButtonVisibility(visibility)` | 设置投屏按钮可见性 |
| `setSettingsButtonVisibility(visibility)` | 设置设置按钮可见性 |
| `setSysTimeVisibility(visibility)` | 设置系统时间可见性 |
| `addDefaultControlComponent(String title, boolean isLive)` | 添加默认控制器 |
| `start()` | 开始播放 |
| `pause()` | 暂停播放 |
| `resume()` | 恢复播放 |
| `release()` | 释放资源 |
| `onBackPressed()` | 处理返回键（返回true表示已处理） |

### StarShortDramaPlayer 主要方法

`StarShortDramaPlayer` 继承自 `StarVideoPlayer`，拥有父类所有方法，隐藏了选集、上一集、下一集按钮。

### 监听器设置

| 方法 | 说明 |
|------|------|
| `setOnWindowClickListener(OnWindowClickListener listener)` | 小窗点击监听 |
| `setOnScreenClickListener(OnScreenClickListener listener)` | 投屏点击监听 |
| `setOnSelectClickListener(OnSelectClickListener listener)` | 选集按钮点击监听 |
| `setOnUpSetClickListener(OnUpSetClickListener listener)` | 上一集点击监听 |
| `setOnDownSetClickListener(OnDownSetClickListener listener)` | 下一集点击监听 |
| `setOnEpisodeSelectListener(OnEpisodeSelectListener listener)` | 选集选中监听 |
| `setOnFullscreenPortraitClickListener(OnFullscreenPortraitClickListener listener)` | 竖屏全屏点击监听 |

### 画面比例常量

| 常量 | 说明 |
|------|------|
| `SCREEN_SCALE_DEFAULT` | 默认比例 |
| `SCREEN_SCALE_16_9` | 16:9 比例 |
| `SCREEN_SCALE_4_3` | 4:3 比例 |
| `SCREEN_SCALE_FIT_PARENT` | 填充父容器 |
| `SCREEN_SCALE_ORIGINAL` | 原始尺寸 |
| `SCREEN_SCALE_CENTER_CROP` | 居中裁剪 |

## 混淆配置

```pro
-keep class com.star.play.** { *; }
-dontwarn com.star.play.**
```

## 依赖说明

本库基于 [DKPlayer](https://github.com/Doikki/DKPlayer) 开发，感谢原作者的贡献。

## 更新日志
### v1.8.0 (2025-03-29)

#### 优化改进
- 移除设置面板中的播放内核切换功能，改为用户自行设置
  - 用户需要根据引入的依赖自行调用 `setPlayerFactory()` 设置播放内核
  - 避免因未引入某个播放内核导致切换时崩溃的问题
  - 设置面板不再显示播放内核选择选项

---

### v1.7.0 (2025-03-09)

#### 新增功能
- ✨ 底部按钮可见性支持区分全屏/非全屏状态
  - 新增 `setVisibilityBottomNormal()` 设置非全屏状态下的按钮可见性
  - 新增 `setVisibilityBottomFullscreen()` 设置全屏状态下的按钮可见性
  - 新增 `setVisibilityBottomAll()` 一次性设置全屏和非全屏的按钮可见性（12参数）
  - 新增各按钮的 `setXxxButtonVisibilityNormal()` 和 `setXxxButtonVisibilityFullscreen()` 方法
  - 原有的 `setVisibilityBottom()` 和 `setXxxButtonVisibility()` 方法仍保留，同时影响全屏和非全屏
- ✨ 倍速按钮点击弹出速度选择菜单
  - 点击倍速按钮现在会弹出 PopupMenu 选择播放速度
  - 支持 0.5x、0.75x、1.0x、1.25x、1.5x、2.0x、2.5x、3.0x 八档速度
  - 菜单中当前速度选项显示为选中状态（带勾选标记）
  - 选择速度后立即应用到当前视频播放

#### 优化改进
- 移除设置面板中的播放内核切换功能，改为用户自行设置
  - 用户需要根据引入的依赖自行调用 `setPlayerFactory()` 设置播放内核
  - 避免因未引入某个播放内核导致切换时崩溃的问题
  - 设置面板不再显示播放内核选择选项

---

### v1.6.0 (2025-03-07)

#### 修复问题
- 🐛 修复 Slider 进度值超出范围导致崩溃的问题
  - 当视频进度超过时长时，progress 值可能超过 1000，导致 `IllegalStateException`
  - 添加 `Math.max(0, Math.min(1000, progress))` 范围限制

---

### v1.4.0 (2025-03-07)

#### 新增功能
- 新增 StarShortDramaPlayer 短剧专用播放器
- 继承自 StarVideoPlayer
- 新增： 双击暂停/播放 - 双击屏幕切换播放/暂停状态
- 新增： 短剧播放器 - 提供精简版播放器 StarShortDramaPlayer ，隐藏选集/上下集按钮，默认使用 ExoPlayer 内核
- 新增双击暂停/播放功能
- 自动隐藏选集、上一集、下一集按钮
- 默认使用 ExoPlayer 内核，无需手动设置
---

### v1.3.0 (2025-03-06)

#### 新增功能
- 竖屏全屏模式
- 新增竖屏全屏按钮，点击自动进入竖屏全屏
- 新增 setFullscreenPortraitButtonVisibility（） - 控制竖屏全屏按钮可见性
- 新增 setOnFullscreenPortraitClickListener（） - 竖屏全点击监听
- 按钮可见性控制
- 支持批量控制底部按钮可见性
- setVisibilityBottom（select， speed， previous， next） - 4参数版本
- setVisibilityBottom（select， speed， previous， next， fullscreen， fullscreenPortrait） - 6参数版本
- 支持单独控制底部各按钮可见性
- setSelectButtonVisibility（） - 选集按钮
- setSpeedButtonVisibility（） - 倍速按钮
- setPreviousButtonVisibility（） - 上一集按钮
- setNextButtonVisibility（） - 下一集按钮
- setFullscreenButtonVisibility（） - 全屏按钮
- setFullscreenPortraitButtonVisibility（） - 竖屏全屏按钮
- 支持批量控制顶部按钮可见性
- setTitleButtonsVisibility（back， pip， screen， settings）
- 支持单独控制顶部各按钮可见性
- setBackButtonVisibility（） - 返回按钮
- setPipButtonVisibility（） - 小窗按钮
- setScreenButtonVisibility（） - 投屏按钮
- setSettingsButtonVisibility（） - 设置按钮
- setSysTimeVisibility（） - 系统时间

#### 优化改进
- 修复全屏切换后按钮可见性状态丢失的问题
- 按钮可见性状态现在会在竖屏/全屏布局切换时保持一致

---

### v1.2.0 (2025-03-05)

### 新增功能
- 选集功能支持自定义适配器
- 设置面板新增播放内核选择（ExoPlayer / IJKPlayer）
- 设置面板新增隐藏底部进度条开关
- 设置面板新增自动旋转开关（根据视频宽高比自动切换横竖屏）
- 全屏模式下播放按钮组居中显示在屏幕中央
- 底部控制栏显示上一个按钮

### 修复问题
- 修复锁定按钮点击无效的问题
- 修复全屏时顶部标题栏不显示的问题
- 修复全屏切换内核时控制栏显示异常的问题
- 修复加载指示器不显示的问题
- 修复加载时播放按钮图标不变化的问题
- 修复多处空指针风险（StarBottomView、StarTitleView、StarSettingsView、StarGestureView）
- 修复选集列表数组越界风险
- 修复进度条单位不一致导致显示不准确的问题

### 优化改进
- 优化全屏状态下的控制栏显示逻辑
- 优化加载状态下的 UI 反馈
- 优化代码健壮性，添加空指针检查

---

### v1.1.0 (2025-03-04)

#### 新增功能
- 需要引用dkplayer库
- 修复打包时版本过高
- 修复权限冲突

---

### v1.0.0 (2025-03-03)

#### 首次发布
- 基于 DKPlayer 封装的视频播放器
- 支持 IJKPlayer 和 ExoPlayer 双内核
- 支持倍速播放、长按倍速
- 支持手势控制（亮度、音量、进度）
- 支持全屏/小窗模式
- 支持锁屏功能
- 支持刘海屏适配

## License

Apache License 2.0
