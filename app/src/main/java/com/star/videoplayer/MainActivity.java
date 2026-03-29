package com.star.videoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.player);

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
        videoView.setTitleButtonsVisibility(View.GONE, View.GONE, View.GONE, View.GONE);
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
        videoView.setOnScreenClickListener(new StarVideoPlayer.OnScreenClickListener() {
            @Override
            public void onClick(View view) {
                //投屏
                /*new Screen().setStaerActivity(MainActivity.this)
                        .setName("斗破苍穹")
                        .setUrl("https://s.xlzys.com/play/9avDmPgd/index.m3u8")
                        .setImageUrl("http://i0.hdslb.com/bfs/article/96fa4320db5115711c8c30afaff936910595d336.png")
                        .show();*/
            }
        });

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
}