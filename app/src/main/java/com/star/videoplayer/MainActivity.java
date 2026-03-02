package com.star.videoplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.star.play.StarStandardVideoController;
import com.star.play.StarVideoPlayer;
import com.star.play.controller.StarEpisodeView;

import java.util.Arrays;
import java.util.List;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;

public class MainActivity extends AppCompatActivity {
    private StarVideoPlayer videoView;
    private String URL="https://vv.jisuzyv.com/play/negox6je/index.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView=findViewById(R.id.player);
        videoView.setPlayerFactory(ExoMediaPlayerFactory.create());  //使用ijk内核
        videoView.setUrl(URL); //设置视频地址
        /* 初始化播放器 */
        List<String> episodes = Arrays.asList("第1集", "第2集", "第3集", "第4集");
        videoView.setEpisodes(episodes, 0);           // 设置集数数据
        videoView.addDefaultControlComponent("短剧", false);
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