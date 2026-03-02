package com.star.play.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * 准备播放界面
 * 包含封面图、开始播放按钮、加载指示器和移动网络提示
 */
public class StarPrepareView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    private ImageView mThumbView;                     // 封面图
    private MaterialButton mStartPlayButton;          // 开始播放按钮
    private CircularProgressIndicator mLoadingIndicator; // 加载指示器
    private FrameLayout mNetWarningLayout;            // 移动网络警告布局
    private TextView mMessageView;                    // 警告信息文本
    private MaterialButton mContinueButton;           // 继续播放按钮

    public StarPrepareView(@NonNull Context context) {
        this(context, null);
    }

    public StarPrepareView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarPrepareView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() {
        // 填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.star_prepare_view, this, true);

        // 绑定控件
        mThumbView = findViewById(R.id.thumb);
        mStartPlayButton = findViewById(R.id.start_play);
        mLoadingIndicator = findViewById(R.id.loading);
        mNetWarningLayout = findViewById(R.id.net_warning_layout);
        mMessageView = findViewById(R.id.message);
        mContinueButton = findViewById(R.id.status);

        // 设置继续播放按钮点击事件
        mContinueButton.setOnClickListener(v -> {
            // 隐藏移动网络警告
            mNetWarningLayout.setVisibility(GONE);
            // 告知播放器允许使用移动网络播放（DKPlayer 全局设置）
            VideoViewManager.instance().setPlayOnMobileNetwork(true);
            // 开始播放
            if (mControlWrapper != null) {
                mControlWrapper.start();
            }
        });

        // 默认隐藏整个视图，由播放状态控制显示
        setVisibility(GONE);
    }

    /**
     * 设置点击整个界面开始播放（通常用于封面图点击）
     */
    public void setClickStart() {
        setOnClickListener(v -> {
            if (mControlWrapper != null) {
                mControlWrapper.start();
            }
        });
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Nullable
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        // 不需要额外处理
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_PREPARING:
                // 正在准备：显示加载圈，隐藏开始按钮和警告
                bringToFront();
                setVisibility(VISIBLE);
                mStartPlayButton.setVisibility(View.GONE);
                mNetWarningLayout.setVisibility(GONE);
                mLoadingIndicator.setVisibility(View.VISIBLE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERING:
            case VideoView.STATE_BUFFERED:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                // 这些状态隐藏准备视图
                setVisibility(GONE);
                break;
            case VideoView.STATE_IDLE:
                // 空闲状态：显示封面和开始按钮，隐藏加载和警告
                setVisibility(VISIBLE);
                bringToFront();
                mLoadingIndicator.setVisibility(View.GONE);
                mNetWarningLayout.setVisibility(GONE);
                mStartPlayButton.setVisibility(View.VISIBLE);
                mThumbView.setVisibility(View.VISIBLE);
                break;
            case VideoView.STATE_START_ABORT:
                // 开始播放中止（通常是因为移动网络限制）：显示移动网络警告
                setVisibility(VISIBLE);
                mNetWarningLayout.setVisibility(VISIBLE);
                mNetWarningLayout.bringToFront();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        // 不需要处理
    }

    @Override
    public void setProgress(int duration, int position) {
        // 不需要处理
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        // 不需要处理
    }
}