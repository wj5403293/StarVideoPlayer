package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
    private MaterialButton mFullscreenPortraitView;
    private Slider mSeekBar;
    private ProgressBar mBottomProgress;
    private View mPlayButtonsContainer;

    private boolean mIsDragging;
    private boolean mIsShowBottomProgress = true;
    private boolean mIsFullScreen = false;

    private int mSelectVisibility = View.VISIBLE;
    private int mSpeedVisibility = View.VISIBLE;
    private int mPreviousVisibility = View.GONE;
    private int mNextVisibility = View.GONE;
    private int mFullscreenVisibility = View.VISIBLE;
    private int mFullscreenPortraitVisibility = View.GONE;

    private OnSelectClickListener mOnSelectClickListener;
    private OnSpeedClickListener mOnSpeedClickListener;
    private OnUpSetClickListener mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;
    private OnProgressListener mOnProgressListener;
    private OnFullscreenPortraitClickListener mOnFullscreenPortraitClickListener;

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
        if (mBottomProgress != null) {
            mBottomProgress.setMax(1000);
        }
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
        if (mFullscreenView != null) {
            mFullscreenView.setIconResource(R.drawable.fullscreen);
        }
    }

    private void showFullscreenLayout() {
        View normalLayout = findViewById(R.id.layout_normal);
        View fullscreenLayout = findViewById(R.id.layout_fullscreen);
        if (normalLayout == null || fullscreenLayout == null) return;

        normalLayout.setVisibility(GONE);
        fullscreenLayout.setVisibility(VISIBLE);
        mCurrentLayout = fullscreenLayout;

        bindViews(mCurrentLayout);
        if (mFullscreenView != null) {
            mFullscreenView.setIconResource(R.drawable.fullscreen_exit);
        }
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
        mFullscreenPortraitView = root.findViewById(R.id.fullscreen_portrait);
        mPlayButtonsContainer = root.findViewById(R.id.play_buttons_container);

        if (mSeekBar != null) {
            mSeekBar.setValueFrom(0f);
            mSeekBar.setValueTo(1000f);
        }

        setupClickListeners();
        setupSeekBarListener();

        if (mControlWrapper != null) {
            updatePlayButtonIcon();
        }

        applyButtonVisibility();
    }

    private void applyButtonVisibility() {
        if (mSelectedWritingsView != null) {
            mSelectedWritingsView.setVisibility(mSelectVisibility);
        }
        if (mSpeedView != null) {
            mSpeedView.setVisibility(mSpeedVisibility);
        }
        if (mSkipPreviousView != null) {
            mSkipPreviousView.setVisibility(mPreviousVisibility);
        }
        if (mSkipNextView != null) {
            mSkipNextView.setVisibility(mNextVisibility);
        }
        if (mFullscreenView != null) {
            mFullscreenView.setVisibility(mFullscreenVisibility);
        }
        if (mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setVisibility(mFullscreenPortraitVisibility);
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
        if (mPlayView != null) {
            mPlayView.setOnClickListener(v -> {
                if (mControlWrapper != null) mControlWrapper.togglePlay();
            });
        }

        if (mSkipPreviousView != null) {
            mSkipPreviousView.setOnClickListener(v -> {
                if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v);
            });
        }

        if (mSkipNextView != null) {
            mSkipNextView.setOnClickListener(v -> {
                if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v);
            });
        }

        if (mSpeedView != null) {
            mSpeedView.setOnClickListener(v -> {
                if (mOnSpeedClickListener != null) mOnSpeedClickListener.onClick(v);
            });
        }

        if (mSelectedWritingsView != null) {
            mSelectedWritingsView.setOnClickListener(v -> {
                if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(v);
            });
        }

        if (mFullscreenView != null) {
            mFullscreenView.setOnClickListener(v -> toggleFullScreen());
        }

        if (mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setOnClickListener(v -> {
                togglePortraitFullScreen();
                if (mOnFullscreenPortraitClickListener != null) {
                    mOnFullscreenPortraitClickListener.onClick(v);
                }
            });
        }
    }

    private void setupSeekBarListener() {
        if (mSeekBar == null) return;

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
            if (fromUser && mControlWrapper != null && mCurrTimeView != null) {
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

    private void togglePortraitFullScreen() {
        if (mControlWrapper == null) return;
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mControlWrapper.startFullScreen();
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

    public interface OnFullscreenPortraitClickListener {
        void onClick(View view);
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

    public void setOnFullscreenPortraitClickListener(OnFullscreenPortraitClickListener listener) {
        mOnFullscreenPortraitClickListener = listener;
    }

    public void showBottomProgress(boolean isShow) {
        mIsShowBottomProgress = isShow;
        if (!isShow && mBottomProgress != null) {
            mBottomProgress.setVisibility(GONE);
        }
    }

    public void setShowBottomProgress(boolean isShow) {
        showBottomProgress(isShow);
    }

    public void setBottomButtonsVisibility(int selectVisibility, int speedVisibility, 
                                           int previousVisibility, int nextVisibility) {
        mSelectVisibility = selectVisibility;
        mSpeedVisibility = speedVisibility;
        mPreviousVisibility = previousVisibility;
        mNextVisibility = nextVisibility;
        applyButtonVisibility();
    }

    public void setBottomButtonsVisibility(int selectVisibility, int speedVisibility, 
                                           int previousVisibility, int nextVisibility,
                                           int fullscreenVisibility, int fullscreenPortraitVisibility) {
        mSelectVisibility = selectVisibility;
        mSpeedVisibility = speedVisibility;
        mPreviousVisibility = previousVisibility;
        mNextVisibility = nextVisibility;
        mFullscreenVisibility = fullscreenVisibility;
        mFullscreenPortraitVisibility = fullscreenPortraitVisibility;
        applyButtonVisibility();
    }

    public void setSelectButtonVisibility(int visibility) {
        mSelectVisibility = visibility;
        if (mSelectedWritingsView != null) {
            mSelectedWritingsView.setVisibility(visibility);
        }
    }

    public void setSpeedButtonVisibility(int visibility) {
        mSpeedVisibility = visibility;
        if (mSpeedView != null) {
            mSpeedView.setVisibility(visibility);
        }
    }

    public void setPreviousButtonVisibility(int visibility) {
        mPreviousVisibility = visibility;
        if (mSkipPreviousView != null) {
            mSkipPreviousView.setVisibility(visibility);
        }
    }

    public void setNextButtonVisibility(int visibility) {
        mNextVisibility = visibility;
        if (mSkipNextView != null) {
            mSkipNextView.setVisibility(visibility);
        }
    }

    public void setFullscreenButtonVisibility(int visibility) {
        mFullscreenVisibility = visibility;
        if (mFullscreenView != null) {
            mFullscreenView.setVisibility(visibility);
        }
    }

    public void setFullscreenPortraitButtonVisibility(int visibility) {
        mFullscreenPortraitVisibility = visibility;
        if (mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setVisibility(visibility);
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
        if (mCurrentLayout == null) return;

        if (isVisible) {
            mCurrentLayout.setVisibility(VISIBLE);
            if (anim != null) {
                mCurrentLayout.startAnimation(anim);
            }
            if (mIsShowBottomProgress && mBottomProgress != null) {
                mBottomProgress.setVisibility(GONE);
            }
            if (mIsFullScreen && mPlayButtonsContainer != null) {
                mPlayButtonsContainer.setVisibility(VISIBLE);
                if (anim != null) {
                    mPlayButtonsContainer.startAnimation(anim);
                }
            }
        } else {
            mCurrentLayout.setVisibility(GONE);
            if (anim != null) {
                mCurrentLayout.startAnimation(anim);
            }
            if (mIsShowBottomProgress && mBottomProgress != null) {
                mBottomProgress.setVisibility(VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(300);
                mBottomProgress.startAnimation(fadeIn);
            }
            if (mIsFullScreen && mPlayButtonsContainer != null) {
                mPlayButtonsContainer.setVisibility(GONE);
                if (anim != null) {
                    mPlayButtonsContainer.startAnimation(anim);
                }
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                if (mBottomProgress != null) {
                    mBottomProgress.setProgress(0);
                    mBottomProgress.setSecondaryProgress(0);
                }
                if (mSeekBar != null) mSeekBar.setValue(0);
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_ERROR:
                setVisibility(GONE);
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_PREPARING:
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.pause);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    if (mCurrentLayout != null) mCurrentLayout.setVisibility(VISIBLE);
                    if (mPlayButtonsContainer != null) {
                        mPlayButtonsContainer.setVisibility(VISIBLE);
                    }
                } else {
                    setVisibility(GONE);
                }
                break;
            case VideoView.STATE_PREPARED:
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.play_arrow);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    if (mCurrentLayout != null) mCurrentLayout.setVisibility(VISIBLE);
                    if (mPlayButtonsContainer != null) {
                        mPlayButtonsContainer.setVisibility(VISIBLE);
                    }
                }
                break;
            case VideoView.STATE_PLAYING:
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.pause);
                if (mIsFullScreen) {
                    setVisibility(VISIBLE);
                    if (mCurrentLayout != null) mCurrentLayout.setVisibility(VISIBLE);
                    if (mPlayButtonsContainer != null) {
                        mPlayButtonsContainer.setVisibility(VISIBLE);
                    }
                    if (mIsShowBottomProgress && mBottomProgress != null) {
                        mBottomProgress.setVisibility(GONE);
                    }
                } else {
                    if (mIsShowBottomProgress) {
                        if (mControlWrapper != null && mControlWrapper.isShowing()) {
                            if (mBottomProgress != null) mBottomProgress.setVisibility(GONE);
                            if (mCurrentLayout != null) mCurrentLayout.setVisibility(VISIBLE);
                        } else {
                            if (mBottomProgress != null) mBottomProgress.setVisibility(VISIBLE);
                            if (mCurrentLayout != null) mCurrentLayout.setVisibility(VISIBLE);
                        }
                    } else {
                        if (mCurrentLayout != null) mCurrentLayout.setVisibility(GONE);
                    }
                    setVisibility(VISIBLE);
                }
                if (mControlWrapper != null) mControlWrapper.startProgress();
                break;
            case VideoView.STATE_PAUSED:
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.play_arrow);
                break;
            case VideoView.STATE_BUFFERING:
                if (mPlayView != null) mPlayView.setIconResource(R.drawable.pause);
                if (mControlWrapper != null) mControlWrapper.stopProgress();
                break;
            case VideoView.STATE_BUFFERED:
                if (mPlayView != null) {
                    mPlayView.setIconResource(mControlWrapper != null && mControlWrapper.isPlaying() ?
                            R.drawable.pause : R.drawable.play_arrow);
                }
                if (mControlWrapper != null) mControlWrapper.startProgress();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            mIsFullScreen = true;
            showFullscreenLayout();
            if (mControlWrapper != null && mControlWrapper.isShowing() && mPlayButtonsContainer != null) {
                mPlayButtonsContainer.setVisibility(VISIBLE);
            }
        } else {
            mIsFullScreen = false;
            if (mPlayButtonsContainer != null) {
                mPlayButtonsContainer.setVisibility(GONE);
            }
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
            progress = Math.max(0, Math.min(1000, progress));
            if (mSeekBar != null) mSeekBar.setValue(progress);
            if (mBottomProgress != null) mBottomProgress.setProgress((int) progress);

            if (mControlWrapper != null) {
                int bufferedPercent = mControlWrapper.getBufferedPercentage();
                if (mBottomProgress != null) {
                    if (bufferedPercent >= 95) {
                        mBottomProgress.setSecondaryProgress(mBottomProgress.getMax());
                    } else {
                        mBottomProgress.setSecondaryProgress(bufferedPercent * 10);
                    }
                }
            }
        } else {
            if (mSeekBar != null) mSeekBar.setValue(0);
            if (mBottomProgress != null) {
                mBottomProgress.setProgress(0);
                mBottomProgress.setSecondaryProgress(0);
            }
        }

        if (mTotalTimeView != null) mTotalTimeView.setText(PlayerUtils.stringForTime(duration));
        if (mCurrTimeView != null) mCurrTimeView.setText(PlayerUtils.stringForTime(position));
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        onVisibilityChanged(!isLocked, null);
    }
}
