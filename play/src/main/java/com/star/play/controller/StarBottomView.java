package com.star.play.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarBottomView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    private TextView mCurrTimeView;
    private TextView mTotalTimeView;
    private TextView mTimeIndicatorView;
    private MaterialButton mSkipPreviousView;
    private MaterialButton mPlayView;
    private MaterialButton mSkipNextView;
    private MaterialButton mSpeedView;
    private MaterialButton mSelectedWritingsView;
    private MaterialButton mFullscreenView;
    private MaterialButton mFullscreenPortraitView;
    private SeekBar mSeekBar;
    private ProgressBar mBottomProgress;
    private View mPlayButtonsContainer;

    private boolean mIsDragging;
    private boolean mIsShowBottomProgress = true;
    private boolean mIsFullScreen = false;
    private float mCurrentSpeed = 1.0f;

    private int mSelectVisibility = View.GONE;
    private int mSpeedVisibility = View.GONE;
    private int mPreviousVisibility = View.GONE;
    private int mNextVisibility = View.GONE;
    private int mFullscreenVisibility = View.VISIBLE;
    private int mFullscreenPortraitVisibility = View.GONE;

    private int mSelectVisibilityNormal = View.GONE;
    private int mSpeedVisibilityNormal = View.GONE;
    private int mPreviousVisibilityNormal = View.GONE;
    private int mNextVisibilityNormal = View.GONE;
    private int mFullscreenVisibilityNormal = View.VISIBLE;
    private int mFullscreenPortraitVisibilityNormal = View.GONE;

    private int mSelectVisibilityFullscreen = View.GONE;
    private int mSpeedVisibilityFullscreen = View.GONE;
    private int mPreviousVisibilityFullscreen = View.GONE;
    private int mNextVisibilityFullscreen = View.GONE;
    private int mFullscreenVisibilityFullscreen = View.VISIBLE;
    private int mFullscreenPortraitVisibilityFullscreen = View.GONE;

    private OnSelectClickListener mOnSelectClickListener;
    private OnSpeedClickListener mOnSpeedClickListener;
    private OnSpeedOptionSelectedListener mOnSpeedOptionSelectedListener;
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
        mTimeIndicatorView = root.findViewById(R.id.time_indicator);
        mSeekBar = root.findViewById(R.id.seekBar);
        mSkipPreviousView = root.findViewById(R.id.skip_previous);
        mPlayView = root.findViewById(R.id.play);
        mSkipNextView = root.findViewById(R.id.skip_next);
        mSpeedView = root.findViewById(R.id.speed);
        mSelectedWritingsView = root.findViewById(R.id.selected_writings);
        mFullscreenView = root.findViewById(R.id.fullscreen);
        mFullscreenPortraitView = root.findViewById(R.id.fullscreen_portrait);
        mPlayButtonsContainer = root.findViewById(R.id.play_buttons_container);

        setupClickListeners();
        setupSeekBarListener();

        if (mControlWrapper != null) {
            updatePlayButtonIcon();
        }

        applyButtonVisibility();
    }

    private void applyButtonVisibility() {
        int selectVisibility = mIsFullScreen ? mSelectVisibilityFullscreen : mSelectVisibilityNormal;
        int speedVisibility = mIsFullScreen ? mSpeedVisibilityFullscreen : mSpeedVisibilityNormal;
        int previousVisibility = mIsFullScreen ? mPreviousVisibilityFullscreen : mPreviousVisibilityNormal;
        int nextVisibility = mIsFullScreen ? mNextVisibilityFullscreen : mNextVisibilityNormal;
        int fullscreenVisibility = mIsFullScreen ? mFullscreenVisibilityFullscreen : mFullscreenVisibilityNormal;
        int fullscreenPortraitVisibility = mIsFullScreen ? mFullscreenPortraitVisibilityFullscreen : mFullscreenPortraitVisibilityNormal;

        if (mSelectedWritingsView != null) {
            mSelectedWritingsView.setVisibility(selectVisibility);
        }
        if (mSpeedView != null) {
            mSpeedView.setVisibility(speedVisibility);
        }
        if (mSkipPreviousView != null) {
            mSkipPreviousView.setVisibility(previousVisibility);
        }
        if (mSkipNextView != null) {
            mSkipNextView.setVisibility(nextVisibility);
        }
        if (mFullscreenView != null) {
            mFullscreenView.setVisibility(fullscreenVisibility);
        }
        if (mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setVisibility(fullscreenPortraitVisibility);
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
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.getMenuInflater().inflate(R.menu.popup_menu_speed, popup.getMenu());
                Menu menu = popup.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    String speedText = item.getTitle().toString();
                    float speed = parseSpeedText(speedText);
                    if (Math.abs(speed - mCurrentSpeed) < 0.01f) {
                        item.setChecked(true);
                        break;
                    }
                }
                popup.setOnMenuItemClickListener(item -> {
                    String speedText = item.getTitle().toString();
                    float speed = parseSpeedText(speedText);
                    mCurrentSpeed = speed;
                    if (mOnSpeedOptionSelectedListener != null) {
                        mOnSpeedOptionSelectedListener.onSpeedOptionSelected(speed, speedText);
                    }
                    return true;
                });
                popup.show();
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

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mControlWrapper != null && mCurrTimeView != null) {
                    long duration = mControlWrapper.getDuration();
                    long seekPos = (long) (duration * progress / 1000f);
                    mCurrTimeView.setText(PlayerUtils.stringForTime((int) seekPos));
                    if (mTimeIndicatorView != null) {
                        mTimeIndicatorView.setText(PlayerUtils.stringForTime((int) seekPos));
                        updateTimeIndicatorPosition(seekBar);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDragging = true;
                if (mControlWrapper != null) {
                    mControlWrapper.stopProgress();
                    mControlWrapper.stopFadeOut();
                }
                if (mTimeIndicatorView != null) {
                    mTimeIndicatorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mControlWrapper != null) {
                    long duration = mControlWrapper.getDuration();
                    long seekPos = (long) (duration * seekBar.getProgress() / 1000f);
                    mControlWrapper.seekTo(seekPos);
                    mIsDragging = false;
                    mControlWrapper.startProgress();
                    mControlWrapper.startFadeOut();
                }
                if (mTimeIndicatorView != null) {
                    mTimeIndicatorView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void toggleFullScreen() {
        if (mControlWrapper == null) return;
        Activity activity = PlayerUtils.scanForActivity(getContext());
        mControlWrapper.toggleFullScreen(activity);
    }

    private void updateTimeIndicatorPosition(SeekBar seekBar) {
        if (mTimeIndicatorView == null || seekBar == null) return;

        int seekBarWidth = seekBar.getWidth();
        if (seekBarWidth == 0) return;

        float progress = seekBar.getProgress() / (float) seekBar.getMax();

        int[] seekBarLocation = new int[2];
        seekBar.getLocationOnScreen(seekBarLocation);

        int[] thisLocation = new int[2];
        getLocationOnScreen(thisLocation);

        float thumbScreenX = seekBarLocation[0] + progress * seekBarWidth;
        float indicatorX = thumbScreenX - thisLocation[0];

        int indicatorWidth = mTimeIndicatorView.getWidth();
        int indicatorHeight = mTimeIndicatorView.getHeight();

        int indicatorY = seekBarLocation[1] - thisLocation[1] - indicatorHeight - 10;

        mTimeIndicatorView.setX(indicatorX - indicatorWidth / 2);
        mTimeIndicatorView.setY(indicatorY);
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

    public interface OnSpeedOptionSelectedListener {
        void onSpeedOptionSelected(float speed, String speedText);
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

    public void setOnSpeedOptionSelectedListener(OnSpeedOptionSelectedListener listener) {
        mOnSpeedOptionSelectedListener = listener;
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

    private float parseSpeedText(String speedText) {
        try {
            String numStr = speedText.replace("x", "").replace("X", "");
            return Float.parseFloat(numStr);
        } catch (NumberFormatException e) {
            return 1.0f;
        }
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
        setBottomButtonsVisibilityNormal(selectVisibility, speedVisibility, previousVisibility, nextVisibility);
        setBottomButtonsVisibilityFullscreen(selectVisibility, speedVisibility, previousVisibility, nextVisibility);
        applyButtonVisibility();
    }

    public void setBottomButtonsVisibility(int selectVisibility, int speedVisibility, 
                                           int previousVisibility, int nextVisibility,
                                           int fullscreenVisibility, int fullscreenPortraitVisibility) {
        setBottomButtonsVisibilityNormal(selectVisibility, speedVisibility, previousVisibility, 
                nextVisibility, fullscreenVisibility, fullscreenPortraitVisibility);
        setBottomButtonsVisibilityFullscreen(selectVisibility, speedVisibility, previousVisibility, 
                nextVisibility, fullscreenVisibility, fullscreenPortraitVisibility);
        applyButtonVisibility();
    }

    public void setBottomButtonsVisibilityNormal(int selectVisibility, int speedVisibility, 
                                                 int previousVisibility, int nextVisibility) {
        mSelectVisibilityNormal = selectVisibility;
        mSpeedVisibilityNormal = speedVisibility;
        mPreviousVisibilityNormal = previousVisibility;
        mNextVisibilityNormal = nextVisibility;
        if (!mIsFullScreen) {
            applyButtonVisibility();
        }
    }

    public void setBottomButtonsVisibilityNormal(int selectVisibility, int speedVisibility, 
                                                 int previousVisibility, int nextVisibility,
                                                 int fullscreenVisibility, int fullscreenPortraitVisibility) {
        mSelectVisibilityNormal = selectVisibility;
        mSpeedVisibilityNormal = speedVisibility;
        mPreviousVisibilityNormal = previousVisibility;
        mNextVisibilityNormal = nextVisibility;
        mFullscreenVisibilityNormal = fullscreenVisibility;
        mFullscreenPortraitVisibilityNormal = fullscreenPortraitVisibility;
        if (!mIsFullScreen) {
            applyButtonVisibility();
        }
    }

    public void setBottomButtonsVisibilityFullscreen(int selectVisibility, int speedVisibility, 
                                                     int previousVisibility, int nextVisibility) {
        mSelectVisibilityFullscreen = selectVisibility;
        mSpeedVisibilityFullscreen = speedVisibility;
        mPreviousVisibilityFullscreen = previousVisibility;
        mNextVisibilityFullscreen = nextVisibility;
        if (mIsFullScreen) {
            applyButtonVisibility();
        }
    }

    public void setBottomButtonsVisibilityFullscreen(int selectVisibility, int speedVisibility, 
                                                     int previousVisibility, int nextVisibility,
                                                     int fullscreenVisibility, int fullscreenPortraitVisibility) {
        mSelectVisibilityFullscreen = selectVisibility;
        mSpeedVisibilityFullscreen = speedVisibility;
        mPreviousVisibilityFullscreen = previousVisibility;
        mNextVisibilityFullscreen = nextVisibility;
        mFullscreenVisibilityFullscreen = fullscreenVisibility;
        mFullscreenPortraitVisibilityFullscreen = fullscreenPortraitVisibility;
        if (mIsFullScreen) {
            applyButtonVisibility();
        }
    }

    public void setBottomButtonsVisibilityAll(int selectNormal, int speedNormal, int previousNormal, int nextNormal,
                                              int fullscreenNormal, int fullscreenPortraitNormal,
                                              int selectFullscreen, int speedFullscreen, int previousFullscreen, int nextFullscreen,
                                              int fullscreenFullscreen, int fullscreenPortraitFullscreen) {
        mSelectVisibilityNormal = selectNormal;
        mSpeedVisibilityNormal = speedNormal;
        mPreviousVisibilityNormal = previousNormal;
        mNextVisibilityNormal = nextNormal;
        mFullscreenVisibilityNormal = fullscreenNormal;
        mFullscreenPortraitVisibilityNormal = fullscreenPortraitNormal;

        mSelectVisibilityFullscreen = selectFullscreen;
        mSpeedVisibilityFullscreen = speedFullscreen;
        mPreviousVisibilityFullscreen = previousFullscreen;
        mNextVisibilityFullscreen = nextFullscreen;
        mFullscreenVisibilityFullscreen = fullscreenFullscreen;
        mFullscreenPortraitVisibilityFullscreen = fullscreenPortraitFullscreen;
        applyButtonVisibility();
    }

    public void setSelectButtonVisibility(int visibility) {
        setSelectButtonVisibilityNormal(visibility);
        setSelectButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setSelectButtonVisibilityNormal(int visibility) {
        mSelectVisibilityNormal = visibility;
        if (!mIsFullScreen && mSelectedWritingsView != null) {
            mSelectedWritingsView.setVisibility(visibility);
        }
    }

    public void setSelectButtonVisibilityFullscreen(int visibility) {
        mSelectVisibilityFullscreen = visibility;
        if (mIsFullScreen && mSelectedWritingsView != null) {
            mSelectedWritingsView.setVisibility(visibility);
        }
    }

    public void setSpeedButtonVisibility(int visibility) {
        setSpeedButtonVisibilityNormal(visibility);
        setSpeedButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setSpeedButtonVisibilityNormal(int visibility) {
        mSpeedVisibilityNormal = visibility;
        if (!mIsFullScreen && mSpeedView != null) {
            mSpeedView.setVisibility(visibility);
        }
    }

    public void setSpeedButtonVisibilityFullscreen(int visibility) {
        mSpeedVisibilityFullscreen = visibility;
        if (mIsFullScreen && mSpeedView != null) {
            mSpeedView.setVisibility(visibility);
        }
    }

    public void setSpeedButtonText(String text) {
        if (mSpeedView != null) {
            mSpeedView.setText(text);
        }
    }

    public void setCurrentSpeed(float speed) {
        mCurrentSpeed = speed;
    }

    public void setTimeTextColor(int color) {
        if (mCurrTimeView != null) {
            mCurrTimeView.setTextColor(color);
        }
        if (mTotalTimeView != null) {
            mTotalTimeView.setTextColor(color);
        }
    }

    public void setButtonIconTint(int color) {
        if (mSkipPreviousView != null) {
            mSkipPreviousView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mPlayView != null) {
            mPlayView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mSkipNextView != null) {
            mSkipNextView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mSpeedView != null) {
            mSpeedView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mSelectedWritingsView != null) {
            mSelectedWritingsView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mFullscreenView != null) {
            mFullscreenView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
        if (mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setIconTint(android.content.res.ColorStateList.valueOf(color));
        }
    }

    public void setBottomContainerBackground(int color) {
        if (mCurrentLayout != null) {
            mCurrentLayout.setBackgroundColor(color);
        }
    }

    public void setPreviousButtonVisibility(int visibility) {
        setPreviousButtonVisibilityNormal(visibility);
        setPreviousButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setPreviousButtonVisibilityNormal(int visibility) {
        mPreviousVisibilityNormal = visibility;
        if (!mIsFullScreen && mSkipPreviousView != null) {
            mSkipPreviousView.setVisibility(visibility);
        }
    }

    public void setPreviousButtonVisibilityFullscreen(int visibility) {
        mPreviousVisibilityFullscreen = visibility;
        if (mIsFullScreen && mSkipPreviousView != null) {
            mSkipPreviousView.setVisibility(visibility);
        }
    }

    public void setNextButtonVisibility(int visibility) {
        setNextButtonVisibilityNormal(visibility);
        setNextButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setNextButtonVisibilityNormal(int visibility) {
        mNextVisibilityNormal = visibility;
        if (!mIsFullScreen && mSkipNextView != null) {
            mSkipNextView.setVisibility(visibility);
        }
    }

    public void setNextButtonVisibilityFullscreen(int visibility) {
        mNextVisibilityFullscreen = visibility;
        if (mIsFullScreen && mSkipNextView != null) {
            mSkipNextView.setVisibility(visibility);
        }
    }

    public void setFullscreenButtonVisibility(int visibility) {
        setFullscreenButtonVisibilityNormal(visibility);
        setFullscreenButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setFullscreenButtonVisibilityNormal(int visibility) {
        mFullscreenVisibilityNormal = visibility;
        if (!mIsFullScreen && mFullscreenView != null) {
            mFullscreenView.setVisibility(visibility);
        }
    }

    public void setFullscreenButtonVisibilityFullscreen(int visibility) {
        mFullscreenVisibilityFullscreen = visibility;
        if (mIsFullScreen && mFullscreenView != null) {
            mFullscreenView.setVisibility(visibility);
        }
    }

    public void setFullscreenPortraitButtonVisibility(int visibility) {
        setFullscreenPortraitButtonVisibilityNormal(visibility);
        setFullscreenPortraitButtonVisibilityFullscreen(visibility);
        applyButtonVisibility();
    }

    public void setFullscreenPortraitButtonVisibilityNormal(int visibility) {
        mFullscreenPortraitVisibilityNormal = visibility;
        if (!mIsFullScreen && mFullscreenPortraitView != null) {
            mFullscreenPortraitView.setVisibility(visibility);
        }
    }

    public void setFullscreenPortraitButtonVisibilityFullscreen(int visibility) {
        mFullscreenPortraitVisibilityFullscreen = visibility;
        if (mIsFullScreen && mFullscreenPortraitView != null) {
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
                if (mSeekBar != null) mSeekBar.setProgress(0);
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
            if (mSeekBar != null) mSeekBar.setProgress((int) progress);
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
            if (mSeekBar != null) mSeekBar.setProgress(0);
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
