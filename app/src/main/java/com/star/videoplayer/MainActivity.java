package com.star.videoplayer;

import android.net.Uri;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.star.dlna.cast.api.DLNACastManager;
import com.star.dlna.cast.model.CastDevice;
import com.star.dlna.cast.model.MediaInfo;
import com.star.dlna.cast.ui.DeviceListDialog;
import com.star.play.StarShortDramaPlayer;
import com.star.play.StarVideoPlayer;
import com.star.play.controller.StarEpisodeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;

public class MainActivity extends AppCompatActivity {
    private StarVideoPlayer videoView;
    private  String URL = "https://vv.jisuzyv.com/play/negox6je/index.m3u8";
    private ArrayList<HashMap<String, Object>> episodeItems = new ArrayList<>();
    private final ActivityResultLauncher<String[]> castPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = true;
                for (Boolean value : result.values()) {
                    if (Boolean.FALSE.equals(value)) {
                        granted = false;
                        break;
                    }
                }
                if (granted) {
                    showDeviceDialog();
                } else {
                    Toast.makeText(this, "请先授予局域网相关权限，再搜索投屏设备", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.player);
        DLNACastManager.getInstance().init(this);
        // 设置初始播放内核
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create());

        // 设置视频地址
        videoView.setUrl(URL);

        // 设置剧集
        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", "第" + i + "集");
            episodeItems.add(map);
        }
        videoView.setEpisodeAdapter(new EpisodeAdapter(episodeItems));

        // 设置标题
        videoView.addDefaultControlComponent("短剧", false);

        // 非全屏时隐藏选集、倍速按钮，显示全屏按钮
        videoView.setVisibilityBottomNormal(
                View.GONE, View.GONE, View.GONE, View.GONE,  // 选集、倍速、上一集、下一集
                View.VISIBLE, View.GONE                       // 全屏、竖屏全屏
        );

// 全屏时显示选集、倍速按钮，隐藏全屏按钮
        videoView.setVisibilityBottomFullscreen(
                View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE,
                View.VISIBLE, View.VISIBLE
        );
        videoView.setTitleButtonsVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
        videoView.start(); //开始播放，不调用则不自动播放
        videoView.setOnUpSetClickListener(new StarVideoPlayer.OnUpSetClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "上一集", Toast.LENGTH_SHORT).show();
            }
        });
        videoView.setOnDownSetClickListener(new StarVideoPlayer.OnDownSetClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "下一集", Toast.LENGTH_SHORT).show();
            }
        });

        videoView.setOnEpisodeSelectListener(new StarEpisodeView.OnEpisodeSelectListener() {
            @Override
            public void onEpisodeSelect(int index, String title) {
                Toast.makeText(MainActivity.this, title + index, Toast.LENGTH_SHORT).show();
            }
        });

        videoView.setOnWindowClickListener(new StarVideoPlayer.OnWindowClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "小窗", Toast.LENGTH_SHORT).show();
            }
        });
        videoView.setOnScreenClickListener(view -> ensureCastPermissionsAndShowDialog());

        MaterialButton button = findViewById(R.id.go);
        button.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(this,MainActivity2.class));
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        //暂停播放
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //继续播放
        videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放播放器
        videoView.release();
    }


    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (!videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void ensureCastPermissionsAndShowDialog() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.isEmpty()) {
            showDeviceDialog();
        } else {
            castPermissionLauncher.launch(missingPermissions.toArray(new String[0]));
        }
    }

    private void showDeviceDialog() {
        DeviceListDialog dialog = new DeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(device -> {
            Toast.makeText(this, "已选择: " + device.getName(), Toast.LENGTH_SHORT).show();
            startCasting(device);
        });
        dialog.show();
    }

    private void startCasting(CastDevice device) {
        DLNACastManager.getInstance().selectDevice(device);
        MediaInfo mediaInfo = new MediaInfo.Builder()
                .setTitle("短剧")
                .setUri(Uri.parse(URL))
                .setMimeType(resolveMimeType(URL))
                .build();

        DLNACastManager.getInstance().cast(mediaInfo, new DLNACastManager.CastCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "投屏成功", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "投屏失败: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String resolveMimeType(String url) {
        if (url == null) {
            return "video/mp4";
        }

        String lowerCaseUrl = url.toLowerCase();
        if (lowerCaseUrl.contains(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        }
        if (lowerCaseUrl.contains(".mpd")) {
            return "application/dash+xml";
        }
        if (lowerCaseUrl.contains(".mkv")) {
            return "video/x-matroska";
        }
        if (lowerCaseUrl.contains(".mov")) {
            return "video/quicktime";
        }
        return "video/mp4";
    }
}
