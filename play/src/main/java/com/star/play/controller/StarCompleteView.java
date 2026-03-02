package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 播放完成界面
 * 当视频播放完毕时显示，提供重新播放和退出全屏功能
 * 参考 CompleteView 实现
 */
public class StarCompleteView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;          // 控制器包装类

    private MaterialButton mReplayView;               // 重新播放按钮
    private ImageView mStopFullscreenView;            // 退出全屏按钮（返回箭头）

    public StarCompleteView(@NonNull Context context) {
        this(context, null);
    }

    public StarCompleteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarCompleteView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化视图和点击事件
     */
    private void init() {
        // 初始隐藏，由播放状态控制显示
        setVisibility(GONE);

        // 填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.star_complete_view, this, true);

        // 绑定控件
        mReplayView = findViewById(R.id.replay);
        mStopFullscreenView = findViewById(R.id.stop_fullscreen);

        // 设置重新播放点击事件
        mReplayView.setOnClickListener(v -> {
            if (mControlWrapper != null) {
                mControlWrapper.replay(true); // 重新播放，true表示清除播放状态
            }
        });

        // 设置退出全屏点击事件
        mStopFullscreenView.setOnClickListener(v -> {
            if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
                Activity activity = PlayerUtils.scanForActivity(getContext());
                if (activity != null && !activity.isFinishing()) {
                    // 强制设置为竖屏（根据实际需求调整）
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mControlWrapper.stopFullScreen(); // 退出全屏模式
                }
            }
        });

        // 设置可点击，防止点击事件穿透
        setClickable(true);
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
        // 当播放完成时显示本视图
        if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
            setVisibility(VISIBLE);
            // 根据全屏状态显示/隐藏退出全屏按钮
            if (mControlWrapper != null) {
                mStopFullscreenView.setVisibility(mControlWrapper.isFullScreen() ? VISIBLE : GONE);
            }
            // 将本视图置于顶层，避免被其他控件遮挡
            bringToFront();
        } else {
            setVisibility(GONE);
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        // 根据全屏状态更新退出全屏按钮的可见性
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            mStopFullscreenView.setVisibility(VISIBLE);
        } else if (playerState == VideoView.PLAYER_NORMAL) {
            mStopFullscreenView.setVisibility(GONE);
        }

        // 刘海屏适配：根据屏幕方向和刘海高度调整退出按钮的左边距
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper != null && mControlWrapper.hasCutout()) {
            int orientation = activity.getRequestedOrientation();
            int cutoutHeight = mControlWrapper.getCutoutHeight();
            LayoutParams params = (LayoutParams) mStopFullscreenView.getLayoutParams();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                params.setMargins(0, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                // 左横屏时，左边距增加刘海高度
                params.setMargins(cutoutHeight, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                // 右横屏时，右边距增加刘海高度（但本控件是左边距，这里保持原样或可根据需求调整）
                params.setMargins(0, 0, 0, 0);
            }
            mStopFullscreenView.setLayoutParams(params);
        }
    }

    @Override
    public void setProgress(int duration, int position) {
        // 不需要处理进度
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        // 锁屏状态变化，本组件无需处理
    }
}