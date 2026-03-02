package com.star.play;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarEpisodeView;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarLiveControlView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarSettingsView;
import com.star.play.controller.StarTitleView;

import java.util.Locale;

import xyz.doikki.videoplayer.player.VideoView;

public class StarVideoPlayer extends VideoView {

    private static final String PREFS_NAME = "star_video_prefs";
    private static final String KEY_LONG_PRESS_SPEED = "long_press_speed";
    private static final String KEY_LONG_PRESS_SPEED_TEXT = "long_press_speed_text";
    private static final String KEY_MUTE = "mute";
    private static final String KEY_SKIP_START_PROGRESS = "skip_start_progress";
    private static final String KEY_SKIP_END_PROGRESS = "skip_end_progress";
    private static final String KEY_AUTO_NEXT = "auto_next";

    private static final String TIMING_OFF = "不启用";
    private static final String TIMING_AFTER_CURRENT = "播完当前";
    private static final String TIMING_30_MIN = "30分钟";
    private static final String TIMING_60_MIN = "60分钟";

    private SharedPreferences mPrefs;

    private float mCurrentSpeed = 1.0f;
    private String mCurrentSpeedText = "1.0x";
    private float mLongPressSpeed = 3.0f;
    private String mLongPressSpeedText = "3.0x";

    private String mTimingText = TIMING_OFF;
    private CountDownTimer mCountDownTimer;

    private int mThemeColor;

    private StarStandardVideoController mController;
    private StarBottomView mBottomView;
    private StarEpisodeView mEpisodeView;
    private StarSettingsView mSettingsView;
    private StarTitleView mTitleView;
    private int mCurrentEpisodeIndex = 0;

    private int mScreenScaleType = SCREEN_SCALE_DEFAULT;

    public interface OnWindowClickListener {
        void onClick(android.view.View view);
    }

    public interface OnScreenClickListener {
        void onClick(android.view.View view);
    }

    public interface OnSelectClickListener {
        void onClick(android.view.View view);
    }

    public interface OnUpSetClickListener {
        void onClick(android.view.View view);
    }

    public interface OnDownSetClickListener {
        void onClick(android.view.View view);
    }

    private OnWindowClickListener mOnWindowClickListener;
    private OnScreenClickListener mOnScreenClickListener;
    private OnSelectClickListener mOnSelectClickListener;
    private OnUpSetClickListener mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;

    public void setOnWindowClickListener(OnWindowClickListener l) {
        mOnWindowClickListener = l;
    }

    public void setOnScreenClickListener(OnScreenClickListener l) {
        mOnScreenClickListener = l;
    }

    public void setOnSelectClickListener(OnSelectClickListener l) {
        mOnSelectClickListener = l;
    }

    public void setOnUpSetClickListener(OnUpSetClickListener l) {
        mOnUpSetClickListener = l;
    }

    public void setOnDownSetClickListener(OnDownSetClickListener l) {
        mOnDownSetClickListener = l;
    }

    public StarVideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public StarVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPrefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSettings();
        setupController();
        setupControllerCallbacks();
        syncSettingsView();

        addOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {
            }

            @Override
            public void onPlayStateChanged(int playState) {
                handlePlayState(playState);
            }
        });
    }

    private void loadSettings() {
        mLongPressSpeed = mPrefs.getFloat(KEY_LONG_PRESS_SPEED, 3.0f);
        mLongPressSpeedText = mPrefs.getString(KEY_LONG_PRESS_SPEED_TEXT, "3.0x");
        setMute(mPrefs.getBoolean(KEY_MUTE, false));
    }

    private void setupController() {
        mController = new StarStandardVideoController(getContext());
        mBottomView = new StarBottomView(getContext());
        mEpisodeView = new StarEpisodeView(getContext());
        mSettingsView = new StarSettingsView(getContext());
        mTitleView = new StarTitleView(getContext());

        mController.addControlComponent(
                new StarCompleteView(getContext()),
                new StarErrorView(getContext()),
                buildPrepareView(),
                mTitleView
        );
        mController.addControlComponent(new StarGestureView(getContext()));
        mController.addControlComponent(mEpisodeView);
        mController.addControlComponent(mSettingsView);
        mController.addControlComponent(mBottomView);
        mController.setCanChangePosition(true);

        setVideoController(mController);
    }

    private StarPrepareView buildPrepareView() {
        StarPrepareView v = new StarPrepareView(getContext());
        v.setClickStart();
        return v;
    }

    private void setupControllerCallbacks() {
        mBottomView.setOnSpeedClickListener(v -> mSettingsView.show());

        mBottomView.setOnUpSetClickListener(v -> {
            if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v);
        });

        mBottomView.setOnDownSetClickListener(v -> {
            if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v);
        });

        mBottomView.setOnSelectClickListener(v -> mEpisodeView.show());

        mEpisodeView.setOnEpisodeSelectListener((index, title) -> {
            mCurrentEpisodeIndex = index;
            if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(null);
        });

        mTitleView.setOnPipClickListener(v -> {
            if (mOnWindowClickListener != null) mOnWindowClickListener.onClick(v);
        });

        mTitleView.setOnScreenClickListener(v -> {
            if (mOnScreenClickListener != null) mOnScreenClickListener.onClick(v);
        });

        mTitleView.setOnSettingsClickListener(v -> mSettingsView.show());

        mSettingsView.setOnScaleChangeListener((scaleType, scaleText) -> {
            setScreenScaleType(scaleType);
            mScreenScaleType = scaleType;
        });

        mSettingsView.setOnMuteChangeListener(isMute -> {
            setMute(isMute);
            mPrefs.edit().putBoolean(KEY_MUTE, isMute).apply();
        });

        mSettingsView.setOnTimingOptionSelectedListener(option -> {
            applyTiming(option);
        });

        mSettingsView.setOnLongPressSpeedChangeListener(speed -> {
            mLongPressSpeed = speed;
            mLongPressSpeedText = String.format(Locale.US, "%.1fX", speed);
            mPrefs.edit()
                    .putFloat(KEY_LONG_PRESS_SPEED, speed)
                    .putString(KEY_LONG_PRESS_SPEED_TEXT, mLongPressSpeedText)
                    .apply();
        });

        mSettingsView.setOnSkipStartChangeListener((progress, timeText) -> {
            mPrefs.edit()
                    .putInt(KEY_SKIP_START_PROGRESS, progress)
                    .apply();
            if (getCurrentPosition() < progress * 1000L) {
                seekTo(progress * 1000L);
            }
        });

        mSettingsView.setOnSkipEndChangeListener((progress, timeText) -> {
            mPrefs.edit()
                    .putInt(KEY_SKIP_END_PROGRESS, progress)
                    .apply();
        });

        mController.setOnSpeedListener(() -> {
            setSpeed(mLongPressSpeed);
            mController.setSpeedLayoutVisibility(android.view.View.VISIBLE);
            boolean same = Math.abs(mLongPressSpeed - mCurrentSpeed) < 0.01f;
            mController.setSpeedText(same
                    ? "已经是 " + mLongPressSpeedText + " 倍速"
                    : mLongPressSpeedText + " 倍速中");
        });

        mController.setOnCancelSpeedListener(() -> {
            setSpeed(mCurrentSpeed);
            mController.setSpeedLayoutVisibility(android.view.View.GONE);
        });

        mBottomView.setOnProgressListener(() -> {
            int skipEndProgress = mPrefs.getInt(KEY_SKIP_END_PROGRESS, 0);
            long skipEndMs = skipEndProgress * 1000L;
            if (skipEndMs > 0 && (getDuration() - getCurrentPosition()) <= skipEndMs) {
                seekTo(getDuration());
            }
        });
    }

    private void syncSettingsView() {
        mSettingsView.setScaleType(mScreenScaleType);
        mSettingsView.setMuteChecked(isMute());
        mSettingsView.setTimingText(mTimingText);
        mSettingsView.setLongPressSpeed(mLongPressSpeed);

        int startProgress = mPrefs.getInt(KEY_SKIP_START_PROGRESS, 0);
        String startText = formatSkipTime(startProgress);
        mSettingsView.setSkipStartTime(startText, startProgress);

        int endProgress = mPrefs.getInt(KEY_SKIP_END_PROGRESS, 0);
        String endText = formatSkipTime(endProgress);
        mSettingsView.setSkipEndTime(endText, endProgress);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTimer();
    }

    private void handlePlayState(int playState) {
        if (playState == STATE_PREPARING) {
            int startProgress = mPrefs.getInt(KEY_SKIP_START_PROGRESS, 0);
            if (startProgress > 0) {
                seekTo(startProgress * 1000L);
            }
        } else if (playState == STATE_PLAYBACK_COMPLETED) {
            if (TIMING_AFTER_CURRENT.equals(mTimingText)) {
                android.app.Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
            }
            if (mPrefs.getBoolean(KEY_AUTO_NEXT, true) && mOnDownSetClickListener != null) {
                mOnDownSetClickListener.onClick(null);
            }
        }
    }

    private void applyTiming(String option) {
        cancelTimer();
        mTimingText = option;
        mSettingsView.setTimingText(option);

        long millis = 0;
        if (TIMING_30_MIN.equals(option)) millis = 30 * 60_000L;
        else if (TIMING_60_MIN.equals(option)) millis = 60 * 60_000L;

        if (millis > 0) {
            mCountDownTimer = new CountDownTimer(millis, 1_000) {
                @Override
                public void onTick(long ms) {
                }

                @Override
                public void onFinish() {
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }.start();
        }
    }

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    public void setEpisodes(java.util.List<String> episodes, int currentIndex) {
        mEpisodeView.setEpisodes(episodes, currentIndex);
        mCurrentEpisodeIndex = currentIndex;
    }

    public void showEpisodePanel() {
        mEpisodeView.show();
    }

    public void hideEpisodePanel() {
        mEpisodeView.hide();
    }

    public boolean isEpisodePanelShowing() {
        return mEpisodeView.isEpisodeShowing();
    }

    public void showSettingsPanel() {
        mSettingsView.show();
    }

    public void hideSettingsPanel() {
        mSettingsView.hide();
    }

    public boolean isSettingsPanelShowing() {
        return mSettingsView.isSettingsShowing();
    }

    public void setPlaybackSpeed(float speed) {
        mCurrentSpeed = speed;
        mCurrentSpeedText = String.format(Locale.US, "%.1fX", speed);
        setSpeed(speed);
    }

    public float getPlaybackSpeed() {
        return mCurrentSpeed;
    }

    public void setLongPressSpeed(float speed) {
        mLongPressSpeed = speed;
        mLongPressSpeedText = String.format(Locale.US, "%.1fX", speed);
        mSettingsView.setLongPressSpeed(speed);
        mPrefs.edit()
                .putFloat(KEY_LONG_PRESS_SPEED, speed)
                .putString(KEY_LONG_PRESS_SPEED_TEXT, mLongPressSpeedText)
                .apply();
    }

    public float getLongPressSpeed() {
        return mLongPressSpeed;
    }

    public void setMuted(boolean mute) {
        setMute(mute);
        mSettingsView.setMuteChecked(mute);
        mPrefs.edit().putBoolean(KEY_MUTE, mute).apply();
    }

    public boolean isMuted() {
        return isMute();
    }

    public void setScreenScale(int scaleType) {
        mScreenScaleType = scaleType;
        setScreenScaleType(scaleType);
        mSettingsView.setScaleType(scaleType);
    }

    public int getScreenScale() {
        return mScreenScaleType;
    }

    public void setSkipStartTime(int seconds) {
        if (seconds < 0) seconds = 0;
        String timeText = formatSkipTime(seconds);
        mSettingsView.setSkipStartTime(timeText, seconds);
        mPrefs.edit()
                .putInt(KEY_SKIP_START_PROGRESS, seconds)
                .apply();
    }

    public int getSkipStartTime() {
        return mPrefs.getInt(KEY_SKIP_START_PROGRESS, 0);
    }

    public void setSkipEndTime(int seconds) {
        if (seconds < 0) seconds = 0;
        String timeText = formatSkipTime(seconds);
        mSettingsView.setSkipEndTime(timeText, seconds);
        mPrefs.edit()
                .putInt(KEY_SKIP_END_PROGRESS, seconds)
                .apply();
    }

    public int getSkipEndTime() {
        return mPrefs.getInt(KEY_SKIP_END_PROGRESS, 0);
    }

    public void setTimingOption(String option) {
        applyTiming(option);
    }

    public String getTimingOption() {
        return mTimingText;
    }

    public void setAutoNext(boolean autoNext) {
        mPrefs.edit().putBoolean(KEY_AUTO_NEXT, autoNext).apply();
    }

    public boolean isAutoNext() {
        return mPrefs.getBoolean(KEY_AUTO_NEXT, true);
    }

    public int getCurrentEpisodeIndex() {
        return mCurrentEpisodeIndex;
    }

    public void setCurrentEpisodeIndex(int index) {
        mCurrentEpisodeIndex = index;
        mEpisodeView.setCurrentIndex(index);
    }

    public void addDefaultControlComponent(String title, boolean isLive) {
        mTitleView.setTitle(title);
    }

    public void setOnEpisodeSelectListener(StarEpisodeView.OnEpisodeSelectListener listener) {
        mEpisodeView.setOnEpisodeSelectListener(listener);
    }

    private String formatSkipTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }
}
