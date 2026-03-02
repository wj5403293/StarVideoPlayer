package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarBottomView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    // 控件引用（指向当前可见布局中的控件）
    private TextView mCurrTimeView;
    private TextView mTotalTimeView;
    private MaterialButton mSkipPreviousView;
    private MaterialButton mPlayView;
    private MaterialButton mSkipNextView;
    private MaterialButton mSpeedView;
    private MaterialButton mSelectedWritingsView;
    private MaterialButton mFullscreenView;
    private Slider mSeekBar;
    private ProgressBar mBottomProgress;

    // 静态控件引用（供外部直接访问）
    public static MaterialButton xuanji;
    public static MaterialButton speed;
    public static MaterialButton zplay;
    public static MaterialButton yplay;

    private boolean mIsDragging;
    private boolean mIsShowBottomProgress = true;

    // 静态回调接口
    private static OnSelectClickListener sOnSelectClickListener;
    private static OnSpeedClickListener sOnSpeedClickListener;
    private static OnUpSetClickListener sOnUpSetClickListener;
    private static OnDownSetClickListener sOnDownSetClickListener;
    private static OnProgressListener sOnProgressListener;

    // 当前显示的布局根视图（用于绑定控件）
    private View mCurrentLayout;

    public StarBottomView(@NonNull Context context) {
        this(context, null);
    }

    public StarBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        // 填充主布局，其中包含两个 include
        LayoutInflater.from(getContext()).inflate(R.layout.star_bottom_view, this, true);

        // 底部细进度条始终存在
        mBottomProgress = findViewById(R.id.bottom_progress);

        // 默认显示竖屏布局
        showNormalLayout();
    }

    /**
     * 切换到竖屏布局并绑定控件
     */
    private void showNormalLayout() {
        View normalLayout = findViewById(R.id.layout_normal);
        View fullscreenLayout = findViewById(R.id.layout_fullscreen);
        if (normalLayout == null || fullscreenLayout == null) return;

        normalLayout.setVisibility(VISIBLE);
        fullscreenLayout.setVisibility(GONE);
        mCurrentLayout = normalLayout;

        bindViews(mCurrentLayout);
        // 设置全屏按钮图标为“进入全屏”
        mFullscreenView.setIconResource(R.drawable.fullscreen);
    }

    /**
     * 切换到全屏布局并绑定控件
     */
    private void showFullscreenLayout() {
        View normalLayout = findViewById(R.id.layout_normal);
        View fullscreenLayout = findViewById(R.id.layout_fullscreen);
        if (normalLayout == null || fullscreenLayout == null) return;

        normalLayout.setVisibility(GONE);
        fullscreenLayout.setVisibility(VISIBLE);
        mCurrentLayout = fullscreenLayout;

        bindViews(mCurrentLayout);
        // 设置全屏按钮图标为“退出全屏”
        mFullscreenView.setIconResource(R.drawable.fullscreen_exit);
    }

    /**
     * 从指定布局根视图绑定所有控件，并重新设置监听器
     */
    private void bindViews(View root) {
        // 绑定控件
        mCurrTimeView = root.findViewById(R.id.curr_time);
        mTotalTimeView = root.findViewById(R.id.total_time);
        mSeekBar = root.findViewById(R.id.seekBar);
        mSkipPreviousView = root.findViewById(R.id.skip_previous);
        mPlayView = root.findViewById(R.id.play);
        mSkipNextView = root.findViewById(R.id.skip_next);
        mSpeedView = root.findViewById(R.id.speed);
        mSelectedWritingsView = root.findViewById(R.id.selected_writings);
        mFullscreenView = root.findViewById(R.id.fullscreen);

        // 更新静态控件引用（注意：这些静态变量会被多个实例共享，但通常只有一个播放器）
        xuanji = mSelectedWritingsView;
        speed = mSpeedView;
        zplay = mSkipPreviousView;
        yplay = mSkipNextView;

        // 重新设置 Slider 范围（防止因视图重建而丢失）
        mSeekBar.setValueFrom(0f);
        mSeekBar.setValueTo(1000f);

        // 重新设置点击监听器
        setupClickListeners();

        // 重新设置进度条监听器
        setupSeekBarListener();

        // 如果已有播放状态，需要恢复播放按钮图标（例如根据 mControlWrapper 的状态）
        if (mControlWrapper != null) {
            updatePlayButtonIcon();
        }
    }

    /**
     * 根据当前播放状态更新播放按钮图标
     */
    private void updatePlayButtonIcon() {
        if (mControlWrapper == null || mPlayView == null) return;
        if (mControlWrapper.isPlaying()) {
            mPlayView.setIconResource(R.drawable.pause);
        } else {
            mPlayView.setIconResource(R.drawable.play_arrow);
        }
    }

    private void setupClickListeners() {
        mPlayView.setOnClickListener(v -> {
            if (mControlWrapper != null) mControlWrapper.togglePlay();
        });

        mSkipPreviousView.setOnClickListener(v -> {
            if (sOnUpSetClickListener != null) sOnUpSetClickListener.onClick(v);
        });

        mSkipNextView.setOnClickListener(v -> {
            if (sOnDownSetClickListener != null) sOnDownSetClickListener.onClick(v);
        });

        mSpeedView.setOnClickListener(v -> {
            if (sOnSpeedClickListener != null) sOnSpeedClickListener.onClick(v);
        });

        mSelectedWritingsView.setOnClickListener(v -> {
            if (sOnSelectClickListener != null) sOnSelectClickListener.onClick(v);
        });

        mFullscreenView.setOnClickListener(v -> toggleFullScreen());
    }

    private void setupSeekBarListener() {
        mSeekBar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                mIsDragging = true;
                if (mControlWrapper != null) {
                    mControlWrapper.stopProgress();
                    mControlWrapper.stopFadeOut();
                }
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (mControlWrapper != null) {
                    long duration = mControlWrapper.getDuration();
                    long seekPos = (long) (duration * slider.getValue() / 1000f);
                    mControlWrapper.seekTo(seekPos);
                    mIsDragging = false;
                    mControlWrapper.startProgress();
                    mControlWrapper.startFadeOut();
                }
            }
        });

        mSeekBar.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && mControlWrapper != null) {
                long duration = mControlWrapper.getDuration();
                long seekPos = (long) (duration * value / 1000f);
                mCurrTimeView.setText(PlayerUtils.stringForTime((int) seekPos));
            }
        });
    }

    private void toggleFullScreen() {
        if (mControlWrapper == null) return;
        Activity activity = PlayerUtils.scanForActivity(getContext());
        mControlWrapper.toggleFullScreen(activity);
    }

    // 对外暴露静态接口
    public interface OnSelectClickListener {
        void onClick(View view);
    }
    public static void setOnSelectClickListener(OnSelectClickListener listener) {
        sOnSelectClickListener = listener;
    }

    public interface OnSpeedClickListener {
        void onClick(View view);
    }
    public static void setOnSpeedClickListener(OnSpeedClickListener listener) {
        sOnSpeedClickListener = listener;
    }

    public interface OnUpSetClickListener {
        void onClick(View view);
    }
    public static void setOnUpSetClickListener(OnUpSetClickListener listener) {
        sOnUpSetClickListener = listener;
    }

    public interface OnDownSetClickListener {
        void onClick(View view);
    }
    public static void setOnDownSetClickListener(OnDownSetClickListener listener) {
        sOnDownSetClickListener = listener;
    }

    public interface OnProgressListener {
        void onProgress();
    }
    public static void setOnProgressListener(OnProgressListener listener) {
        sOnProgressListener = listener;
    }

    public void showBottomProgress(boolean isShow) {
        mIsShowBottomProgress = isShow;
        if (!isShow) {
            mBottomProgress.setVisibility(GONE);
        }
    }

    //------------------------ IControlComponent 接口实现 ------------------------
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
        if (isVisible) {
            mCurrentLayout.setVisibility(VISIBLE);
            if (anim != null) {
                mCurrentLayout.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(GONE);
            }
        } else {
            mCurrentLayout.setVisibility(GONE);
            if (anim != null) {
                mCurrentLayout.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(300);
                mBottomProgress.startAnimation(fadeIn);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                mBottomProgress.setProgress(0);
                mBottomProgress.setSecondaryProgress(0);
                mSeekBar.setValue(0);
                break;
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
                setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
                mPlayView.setIconResource(R.drawable.pause);
                if (mIsShowBottomProgress) {
                    if (mControlWrapper != null && mControlWrapper.isShowing()) {
                        mBottomProgress.setVisibility(GONE);
                        mCurrentLayout.setVisibility(VISIBLE);
                    } else {
                        mBottomProgress.setVisibility(VISIBLE);
                        mCurrentLayout.setVisibility(VISIBLE);
                    }
                } else {
                    mCurrentLayout.setVisibility(GONE);
                }
                setVisibility(VISIBLE);
                mControlWrapper.startProgress();
                break;
            case VideoView.STATE_PAUSED:
                mPlayView.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_BUFFERING:
                mPlayView.setIconResource(mControlWrapper != null && mControlWrapper.isPlaying() ?
                        R.drawable.pause : R.drawable.play_arrow);
                mControlWrapper.stopProgress();
                break;
            case VideoView.STATE_BUFFERED:
                mPlayView.setIconResource(mControlWrapper != null && mControlWrapper.isPlaying() ?
                        R.drawable.pause : R.drawable.play_arrow);
                mControlWrapper.startProgress();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            showFullscreenLayout();
        } else {
            showNormalLayout();
        }
    }

    @Override
    public void setProgress(int duration, int position) {
        if (mIsDragging) return;

        if (sOnProgressListener != null) {
            sOnProgressListener.onProgress();
        }

        if (duration > 0) {
            float progress = (float) position / duration * 1000;
            mSeekBar.setValue(progress);
            mBottomProgress.setProgress((int) progress);

            int bufferedPercent = mControlWrapper.getBufferedPercentage();
            if (bufferedPercent >= 95) {
                mBottomProgress.setSecondaryProgress(mBottomProgress.getMax());
            } else {
                mBottomProgress.setSecondaryProgress(bufferedPercent * 10);
            }
        } else {
            mSeekBar.setValue(0);
            mBottomProgress.setProgress(0);
            mBottomProgress.setSecondaryProgress(0);
        }

        mTotalTimeView.setText(PlayerUtils.stringForTime(duration));
        mCurrTimeView.setText(PlayerUtils.stringForTime(position));
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        onVisibilityChanged(!isLocked, null);
    }
}