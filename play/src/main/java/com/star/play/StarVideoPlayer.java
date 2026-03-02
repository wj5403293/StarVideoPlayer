package com.star.play;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.star.play.R;
import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarEpisodeView;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarLiveControlView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarSettingsView;
import com.star.play.controller.StarTitleView;

import java.util.List;
import java.util.Locale;

import xyz.doikki.videoplayer.player.VideoView;

public class StarVideoPlayer extends VideoView {

    // ---- SharedPreferences ----
    private SharedPreferences mPrefs;

    // ---- 播放速度 ----
    private float mCurrentSpeed = 1.0f;
    private String mCurrentSpeedText = "1.0x";
    private float mLongPressSpeed = 3.0f;
    private String mLongPressSpeedText = "3.0x";

    // ---- 定时关闭 ----
    private String mTimingText = "不启用";
    private CountDownTimer mCountDownTimer;

    // ---- 主题色 ----
    private int mThemeColor;

    // ---- 控制器 & 子组件 ----
    private StarStandardVideoController mController;
    private StarBottomView mBottomView;
    private StarEpisodeView mEpisodeView;
    private StarSettingsView mSettingsView;
    private int mCurrentEpisodeIndex = 0;

    // ---- 画面比例 ----
    private int mScreenScaleType = SCREEN_SCALE_DEFAULT;

    // ---- 缓存当前播放 URL（供外部播放器使用） ----
    private String mCurrentUrl = "";

    // ---- 外部回调接口 ----
    public interface OnWindowClickListener {
        void onClick(View view);
    }

    public interface OnScreenClickListener {
        void onClick(View view);
    }

    public interface OnSelectClickListener {
        void onClick(View view);
    }

    public interface OnUpSetClickListener {
        void onClick(View view);
    }

    public interface OnDownSetClickListener {
        void onClick(View view);
    }

    public interface OnExternalPlayerListener {
        void onUseExternal(String url);
    }

    private OnWindowClickListener mOnWindowClickListener;
    private OnScreenClickListener mOnScreenClickListener;
    private OnSelectClickListener mOnSelectClickListener;
    private OnUpSetClickListener mOnUpSetClickListener;
    private OnDownSetClickListener mOnDownSetClickListener;
    private OnExternalPlayerListener mOnExternalPlayerListener;

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

    public void setOnExternalPlayerListener(OnExternalPlayerListener l) {
        mOnExternalPlayerListener = l;
    }

    // ---- 构造器 ----
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

    // ---- 初始化 ----
    private void init(AttributeSet attrs) {
        mPrefs = getContext().getSharedPreferences("star_video_prefs", Context.MODE_PRIVATE);

        TypedArray a = getContext().obtainStyledAttributes(
                new int[]{androidx.appcompat.R.attr.colorPrimary});
        mThemeColor = a.getColor(0, Color.WHITE);
        a.recycle();

        loadSettings();
        setupController();
        setupControllerCallbacks();
        syncSettingsView(); // 将设置同步到侧滑面板

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
        mLongPressSpeed = mPrefs.getFloat("long_press_speed", 3.0f);
        mLongPressSpeedText = mPrefs.getString("long_press_speed_text", "3.0x");
        setMute(mPrefs.getBoolean("mute", false));
    }

    private void setupController() {
        mController = new StarStandardVideoController(getContext());
        mBottomView = new StarBottomView(getContext());
        mEpisodeView = new StarEpisodeView(getContext());
        mSettingsView = new StarSettingsView(getContext());

        mController.addControlComponent(
                new StarCompleteView(getContext()),
                new StarErrorView(getContext()),
                buildPrepareView(),
                buildTitleView()
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

    private StarTitleView buildTitleView() {
        return new StarTitleView(getContext());
    }

    private void setupControllerCallbacks() {

        // ---- 底部控制栏 ----
        StarBottomView.setOnSpeedClickListener(v -> mSettingsView.show()); // 显示倍速弹窗

        StarBottomView.setOnUpSetClickListener(v -> {
            if (mOnUpSetClickListener != null) mOnUpSetClickListener.onClick(v);
        });

        StarBottomView.setOnDownSetClickListener(v -> {
            if (mOnDownSetClickListener != null) mOnDownSetClickListener.onClick(v);
        });

        StarBottomView.setOnSelectClickListener(v -> mEpisodeView.show());

        // ---- 选集面板 ----
        mEpisodeView.setOnEpisodeSelectListener((index, title) -> {
            mCurrentEpisodeIndex = index;
            if (mOnSelectClickListener != null) mOnSelectClickListener.onClick(null);
        });

        // ---- 标题栏 ----
        StarTitleView.setOnPipClickListener(v -> {
            if (mOnWindowClickListener != null) mOnWindowClickListener.onClick(v);
        });

        StarTitleView.setOnScreenClickListener(v -> {
            if (mOnScreenClickListener != null) mOnScreenClickListener.onClick(v);
        });

        StarTitleView.setOnSettingsClickListener(v -> mSettingsView.show());

        // ---- 侧滑面板监听器 ----

        // 画面比例
        mSettingsView.setOnScaleChangeListener((scaleType, scaleText) -> {
            setScreenScaleType(scaleType);
            mScreenScaleType = scaleType;
            // 可选保存到Prefs
        });

        // 静音开关
        mSettingsView.setOnMuteChangeListener(isMute -> {
            setMute(isMute);
            mPrefs.edit().putBoolean("mute", isMute).apply();
        });

        // 定时关闭选项选中
        mSettingsView.setOnTimingOptionSelectedListener(option -> {
            applyTiming(option);
        });

        // 长按倍速滑块
        mSettingsView.setOnLongPressSpeedChangeListener(speed -> {
            mLongPressSpeed = speed;
            mLongPressSpeedText = String.format(Locale.US, "%.1fX", speed);
            mPrefs.edit()
                    .putFloat("long_press_speed", speed)
                    .putString("long_press_speed_text", mLongPressSpeedText)
                    .apply();
        });

        // 跳过片头滑块
        mSettingsView.setOnSkipStartChangeListener((progress, timeText) -> {
            mPrefs.edit()
                    .putInt("skip_start_progress", progress)
                    .putString("skip_start", timeText)
                    .apply();
            if (getCurrentPosition() < progress * 1000L) {
                seekTo(progress * 1000L);
            }
        });

        // 跳过片尾滑块
        mSettingsView.setOnSkipEndChangeListener((progress, timeText) -> {
            mPrefs.edit()
                    .putInt("skip_end_progress", progress)
                    .putString("skip_end", timeText)
                    .apply();
        });

        // ---- 长按倍速（控制器全局） ----
        StarStandardVideoController.setOnSpeedListener(() -> {
            setSpeed(mLongPressSpeed);
            mController.setSpeedLayoutVisibility(View.VISIBLE);
            boolean same = Math.abs(mLongPressSpeed - mCurrentSpeed) < 0.01f;
            mController.setSpeedText(same
                    ? "已经是 " + mLongPressSpeedText + " 倍速"
                    : mLongPressSpeedText + " 倍速中");
        });

        StarStandardVideoController.setOnCancelSpeedListener(() -> {
            setSpeed(mCurrentSpeed);
            mController.setSpeedLayoutVisibility(View.GONE);
        });

        // ---- 进度监听（用于跳过片尾） ----
        StarBottomView.setOnProgressListener(() -> {
            int skipEndProgress = mPrefs.getInt("skip_end_progress", 0);
            long skipEndMs = skipEndProgress * 1000L;
            if (skipEndMs > 0 && (getDuration() - getCurrentPosition()) <= skipEndMs) {
                seekTo(getDuration());
            }
        });
    }

    /**
     * 将当前设置同步到侧滑面板UI
     */
    private void syncSettingsView() {
        mSettingsView.setScaleType(mScreenScaleType);
        mSettingsView.setMuteChecked(isMute());
        mSettingsView.setTimingText(mTimingText);
        mSettingsView.setLongPressSpeed(mLongPressSpeed);

        int startProgress = mPrefs.getInt("skip_start_progress", 0);
        String startText = formatSkipTime(startProgress);
        mSettingsView.setSkipStartTime(startText, startProgress);

        int endProgress = mPrefs.getInt("skip_end_progress", 0);
        String endText = formatSkipTime(endProgress);
        mSettingsView.setSkipEndTime(endText, endProgress);
    }

    // ---- 生命周期 ----
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTimer();
        StarTitleView.clearAllListeners();
        StarStandardVideoController.setOnSpeedListener(null);
        StarStandardVideoController.setOnCancelSpeedListener(null);
    }

    // ---- 播放状态处理 ----
    private void handlePlayState(int playState) {
        if (playState == STATE_PREPARING) {
            int startProgress = mPrefs.getInt("skip_start_progress", 0);
            if (startProgress > 0) {
                seekTo(startProgress * 1000L);
            }
        } else if (playState == STATE_PLAYBACK_COMPLETED) {
            if ("播完当前".equals(mTimingText) && getActivity() != null) {
                getActivity().finish();
            }
            if (mPrefs.getBoolean("auto_next", true) && mOnDownSetClickListener != null) {
                mOnDownSetClickListener.onClick(null);
            }
        }
    }



    // ---- 定时关闭逻辑 ----
    private void applyTiming(String option) {
        cancelTimer();
        mTimingText = option;
        mSettingsView.setTimingText(option);

        long millis = 0;
        if ("30分钟".equals(option)) millis = 30 * 60_000L;
        else if ("60分钟".equals(option)) millis = 60 * 60_000L;

        if (millis > 0) {
            mCountDownTimer = new CountDownTimer(millis, 1_000) {
                @Override
                public void onTick(long ms) {
                }

                @Override
                public void onFinish() {
                    if (getActivity() != null) getActivity().finish();
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

    // ---- 对外公开方法 ----
// ==================== 对外公开方法 ====================

    /**
     * 设置剧集列表并指定当前集数
     * @param episodes 剧集标题列表
     * @param currentIndex 当前播放的索引
     */
    public void setEpisodes(List<String> episodes, int currentIndex) {
        mEpisodeView.setEpisodes(episodes, currentIndex);
        mCurrentEpisodeIndex = currentIndex;
    }


    /**
     * 显示选集面板
     */
    public void showEpisodePanel() {
        mEpisodeView.show();
    }

    /**
     * 隐藏选集面板
     */
    public void hideEpisodePanel() {
        mEpisodeView.hide();
    }

    /**
     * 判断选集面板是否正在显示
     */
    public boolean isEpisodePanelShowing() {
        return mEpisodeView.isEpisodeShowing();
    }

    /**
     * 显示设置面板
     */
    public void showSettingsPanel() {
        mSettingsView.show();
    }

    /**
     * 隐藏设置面板
     */
    public void hideSettingsPanel() {
        mSettingsView.hide();
    }

    /**
     * 判断设置面板是否正在显示
     */
    public boolean isSettingsPanelShowing() {
        return mSettingsView.isSettingsShowing();
    }

    /**
     * 设置正常播放速度（非长按倍速）
     * @param speed 速度值，如 1.0f, 2.0f 等
     */
    public void setPlaybackSpeed(float speed) {
        mCurrentSpeed = speed;
        mCurrentSpeedText = String.format(Locale.US, "%.1fX", speed);
        setSpeed(speed); // VideoView 的 setSpeed
    }

    /**
     * 获取当前正常播放速度
     */
    public float getPlaybackSpeed() {
        return mCurrentSpeed;
    }

    /**
     * 设置长按倍速值
     */
    public void setLongPressSpeed(float speed) {
        mLongPressSpeed = speed;
        mLongPressSpeedText = String.format(Locale.US, "%.1fX", speed);
        mSettingsView.setLongPressSpeed(speed);
        mPrefs.edit()
                .putFloat("long_press_speed", speed)
                .putString("long_press_speed_text", mLongPressSpeedText)
                .apply();
    }

    /**
     * 获取长按倍速值
     */
    public float getLongPressSpeed() {
        return mLongPressSpeed;
    }

    /**
     * 设置静音状态
     */
    public void setMuted(boolean mute) {
        setMute(mute);
        mSettingsView.setMuteChecked(mute);
        mPrefs.edit().putBoolean("mute", mute).apply();
    }

    /**
     * 获取静音状态
     */
    public boolean isMuted() {
        return isMute();
    }

    /**
     * 设置画面比例
     * @param scaleType 取值如 VideoView.SCREEN_SCALE_16_9 等
     */
    public void setScreenScale(int scaleType) {
        mScreenScaleType = scaleType;
        setScreenScaleType(scaleType);
        mSettingsView.setScaleType(scaleType);
    }

    /**
     * 获取当前画面比例
     */
    public int getScreenScale() {
        return mScreenScaleType;
    }

    /**
     * 设置跳过片头时间（秒）
     */
    public void setSkipStartTime(int seconds) {
        String timeText = formatSkipTime(seconds);
        mSettingsView.setSkipStartTime(timeText, seconds);
        mPrefs.edit()
                .putInt("skip_start_progress", seconds)
                .putString("skip_start", timeText)
                .apply();
    }

    /**
     * 获取跳过片头时间（秒）
     */
    public int getSkipStartTime() {
        return mPrefs.getInt("skip_start_progress", 0);
    }

    /**
     * 设置跳过片尾时间（秒）
     */
    public void setSkipEndTime(int seconds) {
        String timeText = formatSkipTime(seconds);
        mSettingsView.setSkipEndTime(timeText, seconds);
        mPrefs.edit()
                .putInt("skip_end_progress", seconds)
                .putString("skip_end", timeText)
                .apply();
    }

    /**
     * 获取跳过片尾时间（秒）
     */
    public int getSkipEndTime() {
        return mPrefs.getInt("skip_end_progress", 0);
    }

    /**
     * 设置定时关闭选项
     * @param option 可选值："不启用"、"播完当前"、"30分钟"、"60分钟"
     */
    public void setTimingOption(String option) {
        applyTiming(option);
    }

    /**
     * 获取当前定时关闭选项文字
     */
    public String getTimingOption() {
        return mTimingText;
    }

    /**
     * 设置是否自动播放下一个（播放完成时自动触发下一集点击事件）
     */
    public void setAutoNext(boolean autoNext) {
        mPrefs.edit().putBoolean("auto_next", autoNext).apply();
    }

    /**
     * 是否自动播放下一个
     */
    public boolean isAutoNext() {
        return mPrefs.getBoolean("auto_next", true);
    }

    /**
     * 获取当前剧集索引
     */
    public int getCurrentEpisodeIndex() {
        return mCurrentEpisodeIndex;
    }

    /**
     * 设置当前剧集索引（仅更新UI，不触发选集回调）
     */
    public void setCurrentEpisodeIndex(int index) {
        mCurrentEpisodeIndex = index;
        mEpisodeView.setCurrentIndex(index);
    }

    /**
     * 直接播放指定URL，并设置标题（如果标题视图支持）
     * @param title 视频标题
     * @param title 是否直播
     */
    public void addDefaultControlComponent(String title, boolean isLive) {
        StarTitleView titleView = new StarTitleView(getContext());
        titleView.setTitle(title);
    }

    /**
     * 设置选集选择监听器（替代原有的 OnSelectClickListener，提供索引和标题）
     */
    public void setOnEpisodeSelectListener(StarEpisodeView.OnEpisodeSelectListener listener) {
        mEpisodeView.setOnEpisodeSelectListener(listener);
    }




    // ---- 工具方法 ----
    private String formatSkipTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }


}