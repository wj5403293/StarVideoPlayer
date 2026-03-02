# StarVideoPlayer

一个功能强大的 Android 视频播放器库，专为短剧/视频应用设计，基于 DKPlayer 开发。

## 功能特性

- **双内核支持** - 支持 IJKPlayer 和 ExoPlayer 两种播放内核
- **选集功能** - 剧集列表展示与快速切换
- **倍速播放** - 支持多档倍速（0.5x、0.75x、1.0x、1.25x、1.5x、2.0x）
- **长按倍速** - 长按屏幕快速播放（默认2倍速，可自定义）
- **定时关闭** - 支持30分钟、60分钟定时关闭
- **跳过片头/片尾** - 自动跳过指定时间段
- **画面比例调整** - 支持16:9、4:3、默认、填充、缩放、裁剪等模式
- **静音控制** - 一键静音/取消静音
- **手势控制** - 滑动调节亮度、音量、进度
- **全屏/小窗模式** - 支持全屏播放和画中画
- **投屏功能接口** - 预留投屏回调接口
- **刘海屏适配** - 完美适配刘海屏设备
- **锁屏功能** - 全屏模式下可锁定控制栏

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
    implementation 'com.github.1240444767:StarVideoPlayer:1.0.0'
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

```xml
<com.star.play.StarVideoPlayer
    android:id="@+id/player"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

### 3. 基础使用

```java
public class MainActivity extends AppCompatActivity {
    private StarVideoPlayer videoView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        videoView = findViewById(R.id.player);
        
        // 设置播放内核（二选一）
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

### 4. 选集功能

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

// 上一集/下一集
videoView.setOnUpSetClickListener(view -> {
    // 处理上一集逻辑
});

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
videoView.setSkipOpening(30);   // 跳过片头30秒
videoView.setSkipEnding(60);    // 跳过片尾60秒

// 设置标题
videoView.setTitle("视频标题");
```

## API 说明

### StarVideoPlayer 主要方法

| 方法 | 说明 |
|------|------|
| `setUrl(String url)` | 设置视频地址 |
| `setPlayerFactory(PlayerFactory factory)` | 设置播放内核 |
| `setTitle(String title)` | 设置视频标题 |
| `setEpisodes(List<String> episodes, int currentIndex)` | 设置剧集列表 |
| `setSkipOpening(int seconds)` | 设置跳过片头时间 |
| `setSkipEnding(int seconds)` | 设置跳过片尾时间 |
| `addDefaultControlComponent(String title, boolean isLive)` | 添加默认控制器 |
| `start()` | 开始播放 |
| `pause()` | 暂停播放 |
| `resume()` | 恢复播放 |
| `release()` | 释放资源 |
| `onBackPressed()` | 处理返回键（返回true表示已处理） |

### 监听器设置

| 方法 | 说明 |
|------|------|
| `setOnWindowClickListener(OnWindowClickListener listener)` | 小窗点击监听 |
| `setOnScreenClickListener(OnScreenClickListener listener)` | 投屏点击监听 |
| `setOnSelectClickListener(OnSelectClickListener listener)` | 选集按钮点击监听 |
| `setOnUpSetClickListener(OnUpSetClickListener listener)` | 上一集点击监听 |
| `setOnDownSetClickListener(OnDownSetClickListener listener)` | 下一集点击监听 |
| `setOnEpisodeSelectListener(OnEpisodeSelectListener listener)` | 选集选中监听 |

## 混淆配置

```pro
-keep class com.star.play.** { *; }
-dontwarn com.star.play.**
```

## 依赖说明

本库基于 [DKPlayer](https://github.com/Doikki/DKPlayer) 开发，感谢原作者的贡献。

## License

Apache License 2.0
