# DLNA Cast 模块

一个基于 DLNA/UPnP 协议的 Android 视频投屏模块，支持 Material Design 3 设计风格。

[![JitPack](https://jitpack.io/v/1240444767/StarDLNA.svg)](https://jitpack.io/#1240444767/StarDLNA)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 功能特性

- 🔍 **设备发现** - 自动搜索局域网内的 DLNA 设备（智能电视、盒子等）
- 📺 **视频投屏** - 支持本地视频文件和网络视频URL投屏到电视
- 🎮 **播放控制** - 播放、暂停、停止、进度跳转、音量调节
- 🎨 **Material Design 3** - 现代化的 UI 设计
- 🔧 **易于集成** - 简单的 API 设计，通过 JitPack 引入即可使用

## 快速开始

### 1. 添加 JitPack 仓库

在项目的 `settings.gradle` 中添加 JitPack 仓库：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. 添加模块依赖

```gradle
dependencies {
    implementation 'com.github.1240444767:StarDLNA:dlna-cast:1.0.0'
}
```

或使用 SNAPSHOT 版本（最新代码）：

```gradle
dependencies {
    implementation 'com.github.1240444767:StarDLNA:dlna-cast-SNAPSHOT'
}
```

### 3. 初始化

在 Application 或 Activity 中初始化：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DLNACastManager.getInstance().init(this);
    }
}
```

### 4. 显示设备列表

```java
DeviceListDialog dialog = new DeviceListDialog(context);
dialog.setOnDeviceSelectedListener(device -> {
    Toast.makeText(context, "已选择: " + device.getName(), Toast.LENGTH_SHORT).show();
});
dialog.show();
```

### 5. 开始投屏

```java
MediaInfo mediaInfo = new MediaInfo.Builder()
    .setTitle("我的视频")
    .setUri(Uri.fromFile(new File("/path/to/video.mp4")))
    .setMimeType("video/mp4")
    .setDuration(120000)
    .build();

DLNACastManager.getInstance().cast(mediaInfo, new DLNACastManager.CastCallback() {
    @Override
    public void onSuccess() {
        // 投屏成功
    }

    @Override
    public void onError(int code, String message) {
        // 投屏失败
    }
});
```

### 6. 播放控制

```java
DLNACastManager castManager = DLNACastManager.getInstance();

castManager.play(null);
castManager.pause(null);
castManager.stop(null);
castManager.seek(60000, null);
castManager.setVolume(80, null);
```

### 7. 显示控制面板

在布局 XML 中添加控制视图：

```xml
<com.star.dlna.cast.ui.CastControlView
    android:id="@+id/cast_control"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone" />
```

在代码中显示：

```java
CastControlView castControl = findViewById(R.id.cast_control);
castControl.show();
```

### 8. 监听投屏状态

```java
DLNACastManager.getInstance().getCastState().observe(this, state -> {
    switch (state) {
        case PLAYING:
            break;
        case PAUSED:
            break;
        case ERROR:
            break;
    }
});
```

### 9. 释放资源

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    DLNACastManager.getInstance().release();
}
```

## 完整示例

```java
public class VideoPlayerActivity extends AppCompatActivity {

    private CastControlView castControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        castControl = findViewById(R.id.cast_control);
        MaterialButton btnCast = findViewById(R.id.btn_cast);

        btnCast.setOnClickListener(v -> showDeviceList());
        DLNACastManager.getInstance().getCastState().observe(this, this::onCastStateChanged);
    }

    private void showDeviceList() {
        DeviceListDialog dialog = new DeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(device -> startCasting());
        dialog.show();
    }

    private void startCasting() {
        MediaInfo mediaInfo = new MediaInfo.Builder()
            .setTitle("测试视频")
            .setUri(Uri.fromFile(new File("/sdcard/video.mp4")))
            .setMimeType("video/mp4")
            .build();

        DLNACastManager.getInstance().cast(mediaInfo, new DLNACastManager.CastCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> castControl.show());
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(VideoPlayerActivity.this,
                        "投屏失败: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void onCastStateChanged(CastState state) {
        // 处理状态变化
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DLNACastManager.getInstance().release();
    }
}
```

## API 文档

### DLNACastManager

| 方法 | 说明 |
|------|------|
| `init(Context)` | 初始化管理器 |
| `release()` | 释放资源 |
| `startDiscovery(long)` | 开始搜索设备 |
| `stopDiscovery()` | 停止搜索 |
| `getDevices()` | 获取设备列表 (LiveData) |
| `selectDevice(CastDevice)` | 选择设备 |
| `cast(MediaInfo, Callback)` | 开始投屏 |
| `play(Callback)` | 播放 |
| `pause(Callback)` | 暂停 |
| `stop(Callback)` | 停止 |
| `seek(long, Callback)` | 跳转进度 |
| `setVolume(int, Callback)` | 设置音量 |
| `getCastState()` | 获取投屏状态 (LiveData) |

### 数据模型

#### CastDevice
- `id` - 设备 ID
- `name` - 设备名称
- `ipAddress` - IP 地址
- `manufacturer` - 制造商
- `modelName` - 型号

#### MediaInfo
- `title` - 标题
- `uri` - 视频 URI
- `mimeType` - MIME 类型
- `duration` - 时长（毫秒）

## 注意事项

1. **网络权限** - 确保已添加必要的网络权限（模块已自动声明）
2. **WiFi 环境** - 手机和电视需要在同一 WiFi 网络
3. **视频格式** - 电视需要支持的视频格式（推荐 MP4/H.264）
4. **Android 9+** - 已配置明文流量支持

## 许可证

MIT License