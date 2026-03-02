package com.star.play.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IGestureComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 手势控制视图
 * 显示亮度、音量、进度调节的反馈
 */
public class StarGestureView extends FrameLayout implements IGestureComponent {

    private ControlWrapper mControlWrapper;

    private ImageView mIconView;
    private LinearProgressIndicator mProgressIndicator;
    private TextView mPercentTextView;
    private LinearLayout mCenterContainer;

    public StarGestureView(@NonNull Context context) {
        this(context, null);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化视图
     */
    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_gesture_view, this, true);

        mIconView = findViewById(R.id.iv_icon);
        mProgressIndicator = findViewById(R.id.pro_percent);
        mPercentTextView = findViewById(R.id.tv_percent);
        mCenterContainer = findViewById(R.id.center_container);
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        // 不需要额外处理
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        // 不需要处理
    }

    @Override
    public void onStartSlide() {
        // 开始滑动，隐藏控制栏，显示手势反馈容器
        if (mControlWrapper != null) {
            mControlWrapper.hide();
        }
        mCenterContainer.setVisibility(VISIBLE);
        mCenterContainer.setAlpha(1f);
    }

    @Override
    public void onStopSlide() {
        // 停止滑动，淡出隐藏手势反馈容器
        mCenterContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mCenterContainer.setVisibility(GONE);
                    }
                })
                .start();
    }

    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        // 进度滑动时更新图标和时间显示
        mProgressIndicator.setVisibility(GONE);
        if (slidePosition > currentPosition) {
            mIconView.setImageResource(R.drawable.fast_forward); // 快进图标
        } else {
            mIconView.setImageResource(R.drawable.fast_rewind); // 快退图标
        }
        mPercentTextView.setText(String.format("%s/%s",
                PlayerUtils.stringForTime(slidePosition),
                PlayerUtils.stringForTime(duration)));
    }

    @Override
    public void onBrightnessChange(int percent) {
        // 亮度调节时更新图标和百分比
        mProgressIndicator.setVisibility(VISIBLE);
        mIconView.setImageResource(R.drawable.brightness); // 亮度图标
        mPercentTextView.setText(percent + "%");
        mProgressIndicator.setProgress(percent);
    }

    @Override
    public void onVolumeChange(int percent) {
        // 音量调节时更新图标和百分比
        mProgressIndicator.setVisibility(VISIBLE);
        if (percent <= 0) {
            mIconView.setImageResource(R.drawable.volume_off); // 静音图标
        } else {
            mIconView.setImageResource(R.drawable.volume_up); // 音量图标
        }
        mPercentTextView.setText(percent + "%");
        mProgressIndicator.setProgress(percent);
    }

    @Override
    public void onPlayStateChanged(int playState) {
        // 根据播放状态控制手势视图的整体可见性
        if (playState == VideoView.STATE_IDLE
                || playState == VideoView.STATE_START_ABORT
                || playState == VideoView.STATE_PREPARING
                || playState == VideoView.STATE_PREPARED
                || playState == VideoView.STATE_ERROR
                || playState == VideoView.STATE_PLAYBACK_COMPLETED) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
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