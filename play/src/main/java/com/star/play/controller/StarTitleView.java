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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * StarTitleView
 * 顶部标题控制组件（用于 DKPlayer）
 * 功能：
 * 1. 显示视频标题
 * 2. 显示系统时间
 * 3. 提供：
 *    - 返回按钮（退出全屏）
 *    - 小窗按钮（PIP）
 *    - 投屏按钮
 *    - 设置按钮
 * 仅在 全屏状态 下显示
 */
public class StarTitleView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    private TextView mTitleView;
    private TextView mSysTimeView;
    private ImageView mBackView;
    private ImageView mPipView;
    private ImageView mScreenView;
    private ImageView mSettingsView;
    private LinearLayout mTitleContainer;

    private OnPipClickListener mOnPipClickListener;
    private OnScreenClickListener mOnScreenClickListener;
    private OnSettingsClickListener mOnSettingsClickListener;

    public StarTitleView(@NonNull Context context) {
        super(context);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_title_view, this, true);

        mTitleContainer = findViewById(R.id.title_container);

        mBackView = findViewById(R.id.back);
        mBackView.setOnClickListener(view -> {
            if (mControlWrapper == null) return;
            Activity activity = PlayerUtils.scanForActivity(getContext());
            if (activity != null && mControlWrapper.isFullScreen()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mControlWrapper.stopFullScreen();
            }
        });

        mTitleView = findViewById(R.id.title);
        mSysTimeView = findViewById(R.id.sys_time);

        mPipView = findViewById(R.id.pip);
        mPipView.setOnClickListener(view -> {
            if (mOnPipClickListener != null)
                mOnPipClickListener.onPipClick(view);
        });

        mScreenView = findViewById(R.id.screen);
        mScreenView.setOnClickListener(view -> {
            if (mOnScreenClickListener != null)
                mOnScreenClickListener.onScreenClick(view);
        });

        mSettingsView = findViewById(R.id.settings);
        mSettingsView.setOnClickListener(view -> {
            if (mOnSettingsClickListener != null)
                mOnSettingsClickListener.onSettingsClick(view);
        });
    }

    public interface OnPipClickListener {
        void onPipClick(View view);
    }

    public interface OnScreenClickListener {
        void onScreenClick(View view);
    }

    public interface OnSettingsClickListener {
        void onSettingsClick(View view);
    }

    public void setOnPipClickListener(OnPipClickListener listener) {
        mOnPipClickListener = listener;
    }

    public void setOnScreenClickListener(OnScreenClickListener listener) {
        mOnScreenClickListener = listener;
    }

    public void setOnSettingsClickListener(OnSettingsClickListener listener) {
        mOnSettingsClickListener = listener;
    }

    public void setTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
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
        if (mControlWrapper == null || !mControlWrapper.isFullScreen()) return;

        if (isVisible) {
            if (getVisibility() == GONE) {
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
                setVisibility(VISIBLE);
                if (anim != null) startAnimation(anim);
            }
        } else {
            if (getVisibility() == VISIBLE) {
                setVisibility(GONE);
                if (anim != null) startAnimation(anim);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (mControlWrapper == null) return;

        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            if (mControlWrapper.isShowing() && !mControlWrapper.isLocked()) {
                setVisibility(VISIBLE);
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
            }
            mTitleView.setSelected(true);
        } else {
            setVisibility(GONE);
            mTitleView.setSelected(false);
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.hasCutout()) {
            int orientation = activity.getRequestedOrientation();
            int cutoutHeight = mControlWrapper.getCutoutHeight();

            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                mTitleContainer.setPadding(0, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mTitleContainer.setPadding(cutoutHeight, 0, 0, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                mTitleContainer.setPadding(0, 0, cutoutHeight, 0);
            }
        }
    }

    @Override
    public void setProgress(int duration, int position) {}

    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            setVisibility(GONE);
        } else {
            if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
                setVisibility(VISIBLE);
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
            }
        }
    }
}
