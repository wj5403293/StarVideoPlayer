package com.star.play.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * StarTitleView
 *
 * 顶部标题控制组件（用于 DKPlayer）
 *
 * 功能：
 * 1. 显示视频标题
 * 2. 显示系统时间
 * 3. 提供：
 *    - 返回按钮（退出全屏）
 *    - 小窗按钮（PIP）
 *    - 投屏按钮
 *    - 设置按钮
 *
 * 仅在 全屏状态 下显示
 */
public class StarTitleView extends FrameLayout implements IControlComponent {

    /** DK 控制器包装类 */
    private ControlWrapper mControlWrapper;

    /** 标题文本 */
    public static TextView mTitleView;

    /** 系统时间文本 */
    public static TextView mSysTimeView;

    /** 返回按钮（退出全屏） */
    public static ImageView mBackView;

    /** 小窗按钮 */
    public static ImageView mPipView;

    /** 投屏按钮 */
    public static ImageView mScreenView;

    /** 设置按钮 */
    public static ImageView mSettingsView;

    /** 标题栏容器（用于刘海屏适配） */
    public static LinearLayout mTitleContainer;


    //================ 构造方法 =================//

    public StarTitleView(@NonNull Context context) {
        super(context);
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StarTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    //================ 初始化布局 =================//

    {
        // 默认隐藏
        setVisibility(GONE);

        // 加载布局
        LayoutInflater.from(getContext()).inflate(R.layout.star_title_view,this,true);

        // 获取View
        mTitleContainer = findViewById(R.id.title_container);

        // 返回按钮
        mBackView = findViewById(R.id.back);
        mBackView.setOnClickListener(view -> {
            Activity activity = PlayerUtils.scanForActivity(getContext());

            // 仅在全屏时才执行退出
            if (activity != null && mControlWrapper.isFullScreen()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mControlWrapper.stopFullScreen();
            }
        });

        mTitleView = findViewById(R.id.title);
        mSysTimeView = findViewById(R.id.sys_time);

        // 小窗按钮
        mPipView = findViewById(R.id.pip);
        mPipView.setOnClickListener(view -> {
            if (onPipClickListener != null)
                onPipClickListener.onPipClick(view);
        });

        // 投屏按钮
        mScreenView = findViewById(R.id.screen);
        mScreenView.setOnClickListener(view -> {
            if (onScreenClickListener != null)
                onScreenClickListener.onScreenClick(view);
        });

        // 设置按钮
        mSettingsView = findViewById(R.id.settings);
        mSettingsView.setOnClickListener(view -> {
            if (onSettingsClickListener != null)
                onSettingsClickListener.onSettingsClick(view);
        });
    }


    //================ 外部点击事件接口 =================//

    /**
     * 小窗点击事件
     */
    public interface OnPipClickListener {
        void onPipClick(View view);
    }
    private static OnPipClickListener onPipClickListener;
    public static void setOnPipClickListener(OnPipClickListener listener) {
        onPipClickListener = listener;
    }

    /**
     * 投屏点击事件
     */
    public interface OnScreenClickListener {
        void onScreenClick(View view);
    }
    private static OnScreenClickListener onScreenClickListener;
    public static void setOnScreenClickListener(OnScreenClickListener listener) {
        onScreenClickListener = listener;
    }

    /**
     * 设置点击事件
     */
    public interface OnSettingsClickListener {
        void onSettingsClick(View view);
    }
    private static OnSettingsClickListener onSettingsClickListener;
    public static void setOnSettingsClickListener(OnSettingsClickListener listener) {
        onSettingsClickListener = listener;
    }

    /** 清空所有静态回调，防止泄漏（在 Activity#onDestroy 调用） */
    public static void clearAllListeners() {
        onScreenClickListener = null;
        onSettingsClickListener        = null;
        onPipClickListener         = null;
    }


    //================ 对外方法 =================//

    /** 设置标题 */
    public void setTitle(String title) {
        mTitleView.setText(title);
    }


    //================ IControlComponent 实现 =================//

    /** 绑定控制器 */
    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    /** 返回View */
    @Nullable
    @Override
    public View getView() {
        return this;
    }


    /**
     * 控制器显示/隐藏时触发
     */
    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

        // 仅全屏状态才显示标题栏
        if (!mControlWrapper.isFullScreen()) return;

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


    /**
     * 播放状态变化
     */
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


    /**
     * 播放器状态变化（全屏 / 普通）
     */
    @Override
    public void onPlayerStateChanged(int playerState) {

        if (playerState == VideoView.PLAYER_FULL_SCREEN) {

            if (mControlWrapper.isShowing() && !mControlWrapper.isLocked()) {
                setVisibility(VISIBLE);
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
            }

            // 标题跑马灯
            mTitleView.setSelected(true);

        } else {

            setVisibility(GONE);
            mTitleView.setSelected(false);
        }

        // 刘海屏适配
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


    /**
     * 锁定状态变化
     */
    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
        }
    }
}