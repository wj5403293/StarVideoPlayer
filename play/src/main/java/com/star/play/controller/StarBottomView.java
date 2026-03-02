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

    private boolean mIsDragging;
    private boolean mIsShowBottomProgress = true;

    private OnSelectClickListener mOnSelectClickListener;
    private OnSpeedClickListener mOnSpeedClickListener;
    private OnUpSetClickListener mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;
    private OnProgressListener mOnProgressListener;

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
        LayoutInflater.from(getContext()).inflate(R.layout.star_bottom_view, this, true);
        mBottomProgress = findViewById(R.id.bottom_progress);
        showNormalLayout();
    }

    private void showNormalLayout() {
        View normalLayout = findViewById(R.id.layout_normal);
        View fullscreenLayout = findViewById(R.id.layout_fullscreen);
        if (normalLayout == null || fullscreenLayout == null) return;

        normalLayout.setVisibility(VISIBLE);
        fullscreenLayout.setVisibility(GONE);
        mCurrentLayout = normalLayout;

        bindViews(mCurrentLayout);
        mFullscreenView.setIconResource(R.drawable.fullscreen);
    }

    private void showFullscreenLayout() {
        View normalLayout = findViewById(R.id.layout_normal);
        View fullscreenLayout = findViewById(R.id.layout_fullscreen);
        if (normalLayout == null || fullscreenLayout == null) return;

        normalLayout.setVisibility(GONE);
        fullscreenLayout.setVisibility(VISIBLE);
        mCurrentLayout = fullscreenLayout;

        bindViews(mCurrentLayout);
        mFullscreenView.setIconResource(R.drawable.fullscreen_exit);
    }

    private void bindViews(View root) {
        mCurrTimeView = root.findViewById(R.id.curr_time);
        mTotalTimeView = root.findViewById(R.id.total_time);
        mSeekBar = root.findViewById(R.id.seekBar);
        mSkipPreviousView = root.findViewById(R.id.skip_previous);
        mPlayView = root.findViewById(R.id.play);
        mSkipNextView = root.findViewById(R.id.skip_next);
        mSpeedView = root.findViewById(R.id.speed);
        mSelectedWritingsView = root.findViewById(R.id.selected_writings);
        mFullscreenView = root.findViewById(R.id.fullscreen);

        mSeekBar.setValueFrom(0f);
        mSeekBar.setValueTo(1000f);

        setupClickListeners();
        setupSeekBarListener();

        if (mControlWrapper != null) {
            updatePlayButtonIcon();
        }
    }

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
            if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v);
        });

        mSkipNextView.setOnClickListener(v -> {
            if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v);
        });

        mSpeedView.setOnClickListener(v -> {
            if (mOnSpeedClickListener != null) mOnSpeedClickListener.onClick(v);
        });

        mSelectedWritingsView.setOnClickListener(v -> {
            if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(v);
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

    public interface OnSelectClickListener {
        void onClick(View view);
    }

    public interface OnSpeedClickListener {
        void onClick(View view);
    }

    public interface OnUpSetClickListener {
        void onClick(View view);
    }

    public interface OnDownSetClickListener {
        void onClick(View view);
    }

    public interface OnProgressListener {
        void onProgress();
    }

    public void setOnSelectClickListener(OnSelectClickListener listener) {
        mOnSelectClickListener = listener;
    }

    public void setOnSpeedClickListener(OnSpeedClickListener listener) {
        mOnSpeedClickListener = listener;
    }

    public void setOnUpSetClickListener(OnUpSetClickListener listener) {
        mOnUpSetClickListener = listener;
    }

    public void setOnDownSetClickListener(OnDownSetClickListener listener) {
        mOnDownSetClickListener = listener;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListener = listener;
    }

    public void showBottomProgress(boolean isShow) {
        mIsShowBottomProgress = isShow;
        if (!isShow) {
            mBottomProgress.setVisibility(GONE);
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

        if (mOnProgressListener != null) {
            mOnProgressListener.onProgress();
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
