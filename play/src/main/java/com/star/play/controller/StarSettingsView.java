package com.star.play.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.star.play.R;

import java.util.Locale;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;

public class StarSettingsView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    // 视图
    private View mDimView;
    private LinearLayout mPanelView;
    private MaterialButton mCloseButton;

    // 画面比例
    private ChipGroup mChipGroupScale;

    // 播放内核
    private ChipGroup mChipGroupPlayer;

    // 静音开关
    private MaterialSwitch mMuteSwitch;

    // 隐藏进度条
    private MaterialSwitch mHideProgressSwitch;

    // 自动旋转
    private MaterialSwitch mAutoRotateSwitch;

    // 定时关闭
    private MaterialButton mBtnTiming;

    // 长按倍速
    private TextView mTvLongPressSpeed;
    private Slider mSliderLongPressSpeed;

    // 跳过片头
    private TextView mTvSkipStart;
    private Slider mSliderSkipStart;

    // 跳过片尾
    private TextView mTvSkipEnd;
    private Slider mSliderSkipEnd;

    // 状态
    private boolean mIsShowing = false;

    // ---------- 回调接口 ----------
    public interface OnScaleChangeListener {
        void onScaleChanged(int scaleType, String scaleText);
    }

    public interface OnMuteChangeListener {
        void onMuteChanged(boolean isMute);
    }

    public interface OnPlayerKernelChangeListener {
        void onPlayerKernelChanged(String kernel);
    }

    public interface OnHideProgressChangeListener {
        void onHideProgressChanged(boolean isHide);
    }

    public interface OnAutoRotateChangeListener {
        void onAutoRotateChanged(boolean isAutoRotate);
    }

    // 定时关闭选项选中监听器
    public interface OnTimingOptionSelectedListener {
        void onTimingOptionSelected(String option); // option: "不启用", "播完当前", "30分钟", "60分钟"
    }

    public interface OnLongPressSpeedChangeListener {
        void onLongPressSpeedChanged(float speed);
    }

    public interface OnSkipStartChangeListener {
        void onSkipStartChanged(int progress, String timeText);
    }

    public interface OnSkipEndChangeListener {
        void onSkipEndChanged(int progress, String timeText);
    }

    private OnScaleChangeListener mOnScaleChangeListener;
    private OnMuteChangeListener mOnMuteChangeListener;
    private OnPlayerKernelChangeListener mOnPlayerKernelChangeListener;
    private OnHideProgressChangeListener mOnHideProgressChangeListener;
    private OnAutoRotateChangeListener mOnAutoRotateChangeListener;
    private OnTimingOptionSelectedListener mOnTimingOptionSelectedListener;
    private OnLongPressSpeedChangeListener mOnLongPressSpeedChangeListener;
    private OnSkipStartChangeListener mOnSkipStartChangeListener;
    private OnSkipEndChangeListener mOnSkipEndChangeListener;

    // 设置监听器的方法
    public void setOnScaleChangeListener(OnScaleChangeListener l) {
        mOnScaleChangeListener = l;
    }

    public void setOnMuteChangeListener(OnMuteChangeListener l) {
        mOnMuteChangeListener = l;
    }

    public void setOnPlayerKernelChangeListener(OnPlayerKernelChangeListener l) {
        mOnPlayerKernelChangeListener = l;
    }

    public void setOnHideProgressChangeListener(OnHideProgressChangeListener l) {
        mOnHideProgressChangeListener = l;
    }

    public void setOnAutoRotateChangeListener(OnAutoRotateChangeListener l) {
        mOnAutoRotateChangeListener = l;
    }

    public void setOnTimingOptionSelectedListener(OnTimingOptionSelectedListener l) {
        mOnTimingOptionSelectedListener = l;
    }

    public void setOnLongPressSpeedChangeListener(OnLongPressSpeedChangeListener l) {
        mOnLongPressSpeedChangeListener = l;
    }

    public void setOnSkipStartChangeListener(OnSkipStartChangeListener l) {
        mOnSkipStartChangeListener = l;
    }

    public void setOnSkipEndChangeListener(OnSkipEndChangeListener l) {
        mOnSkipEndChangeListener = l;
    }

    // 构造器
    public StarSettingsView(@NonNull Context context) {
        this(context, null);
    }

    public StarSettingsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarSettingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_settings_view, this, true);

        // 基础视图
        mDimView = findViewById(R.id.dim);
        mPanelView = findViewById(R.id.panel);
        mCloseButton = findViewById(R.id.btn_close);

        // 画面比例 ChipGroup
        mChipGroupScale = findViewById(R.id.chipGroup);
        // 播放内核 ChipGroup
        mChipGroupPlayer = findViewById(R.id.chipGroupPlayer);
        // 静音开关
        mMuteSwitch = findViewById(R.id.switch_mute);
        // 隐藏进度条
        mHideProgressSwitch = findViewById(R.id.switch_hide_progress);
        // 自动旋转
        mAutoRotateSwitch = findViewById(R.id.switch_auto_rotate);
        // 定时关闭按钮
        mBtnTiming = findViewById(R.id.btn_timing);
        // 长按倍速
        mTvLongPressSpeed = findViewById(R.id.tv_long_press_speed);
        mSliderLongPressSpeed = findViewById(R.id.slider_long_press_speed);
        // 跳过片头
        mTvSkipStart = findViewById(R.id.tv_skip_start);
        mSliderSkipStart = findViewById(R.id.slider_skip_start);
        // 跳过片尾
        mTvSkipEnd = findViewById(R.id.tv_skip_end);
        mSliderSkipEnd = findViewById(R.id.slider_skip_end);

        // ---------- 设置滑块范围（必须在设置值之前） ----------
        // 长按倍速：1.0 ~ 10.0，步长 0.5
        mSliderLongPressSpeed.setValueFrom(1.0f);
        mSliderLongPressSpeed.setValueTo(10.0f);
        mSliderLongPressSpeed.setStepSize(0.5f);

        // 跳过片头/片尾：0 ~ 300 秒（5分钟），步长 1 秒
        mSliderSkipStart.setValueFrom(0f);
        mSliderSkipStart.setValueTo(300f);
        mSliderSkipStart.setStepSize(1f);

        mSliderSkipEnd.setValueFrom(0f);
        mSliderSkipEnd.setValueTo(300f);
        mSliderSkipEnd.setStepSize(1f);

        // 设置监听器
        setupListeners();

        // 蒙层和关闭按钮
        mDimView.setOnClickListener(v -> hide());
        mCloseButton.setOnClickListener(v -> hide());
        mPanelView.setOnClickListener(v -> { }); // 消费点击，不穿透
    }

    private void setupListeners() {
        // 画面比例选择
        mChipGroupScale.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            Chip chip = findViewById(id);
            if (chip == null) return;
            String text = chip.getText().toString();
            int scaleType = mapScaleTextToType(text);
            if (mOnScaleChangeListener != null) {
                mOnScaleChangeListener.onScaleChanged(scaleType, text);
            }
        });

        // 播放内核选择
        mChipGroupPlayer.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            Chip chip = findViewById(id);
            if (chip == null) return;
            String kernel = chip.getText().toString();
            if (mOnPlayerKernelChangeListener != null) {
                mOnPlayerKernelChangeListener.onPlayerKernelChanged(kernel);
            }
        });

        // 静音开关
        mMuteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mOnMuteChangeListener != null) {
                mOnMuteChangeListener.onMuteChanged(isChecked);
            }
        });

        // 隐藏进度条开关
        mHideProgressSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mOnHideProgressChangeListener != null) {
                mOnHideProgressChangeListener.onHideProgressChanged(isChecked);
            }
        });

        // 自动旋转开关
        mAutoRotateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mOnAutoRotateChangeListener != null) {
                mOnAutoRotateChangeListener.onAutoRotateChanged(isChecked);
            }
        });

        // 定时关闭按钮点击 -> 显示 PopupMenu
        mBtnTiming.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.popup_menu_time, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String option = item.getTitle().toString(); // 获取选项文字
                // 更新按钮文字
                mBtnTiming.setText(option);
                // 回调给 StarVideoPlayer
                if (mOnTimingOptionSelectedListener != null) {
                    mOnTimingOptionSelectedListener.onTimingOptionSelected(option);
                }
                return true;
            });
            popup.show();
        });

        // 长按倍速滑块（仅停止拖动时回调，避免频繁保存）
        mSliderLongPressSpeed.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float value = slider.getValue();
                if (mOnLongPressSpeedChangeListener != null) {
                    mOnLongPressSpeedChangeListener.onLongPressSpeedChanged(value);
                }
                updateLongPressSpeedText(value);
            }
        });

        // 跳过片头滑块
        mSliderSkipStart.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int progress = (int) slider.getValue();
                String timeText = formatSkipTime(progress);
                if (mOnSkipStartChangeListener != null) {
                    mOnSkipStartChangeListener.onSkipStartChanged(progress, timeText);
                }
                mTvSkipStart.setText(timeText);
            }
        });

        // 跳过片尾滑块
        mSliderSkipEnd.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int progress = (int) slider.getValue();
                String timeText = formatSkipTime(progress);
                if (mOnSkipEndChangeListener != null) {
                    mOnSkipEndChangeListener.onSkipEndChanged(progress, timeText);
                }
                mTvSkipEnd.setText(timeText);
            }
        });
    }

    // ---------- 公共方法：更新UI ----------
    public void setScaleType(int scaleType) {
        String text = getScaleText(scaleType);
        for (int i = 0; i < mChipGroupScale.getChildCount(); i++) {
            View child = mChipGroupScale.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equals(text)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    public void setPlayerKernel(String kernel) {
        for (int i = 0; i < mChipGroupPlayer.getChildCount(); i++) {
            View child = mChipGroupPlayer.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equals(kernel)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    public void setMuteChecked(boolean isMute) {
        mMuteSwitch.setChecked(isMute);
    }

    public void setHideProgressChecked(boolean isHide) {
        mHideProgressSwitch.setChecked(isHide);
    }

    public void setAutoRotateChecked(boolean isAutoRotate) {
        mAutoRotateSwitch.setChecked(isAutoRotate);
    }

    public void setTimingText(String text) {
        mBtnTiming.setText(text);
    }

    public void setLongPressSpeed(float speed) {
        // 确保值在范围内
        speed = Math.max(mSliderLongPressSpeed.getValueFrom(),
                Math.min(mSliderLongPressSpeed.getValueTo(), speed));
        mSliderLongPressSpeed.setValue(speed);
        updateLongPressSpeedText(speed);
    }

    private void updateLongPressSpeedText(float speed) {
        String text = String.format(Locale.US, "%.1fX", speed);
        mTvLongPressSpeed.setText(text);
    }

    public void setSkipStartTime(String timeText, int progress) {
        mTvSkipStart.setText(timeText);
        // 确保值在范围内
        progress = (int) Math.max(mSliderSkipStart.getValueFrom(),
                Math.min(mSliderSkipStart.getValueTo(), progress));
        mSliderSkipStart.setValue(progress);
    }

    public void setSkipEndTime(String timeText, int progress) {
        mTvSkipEnd.setText(timeText);
        progress = (int) Math.max(mSliderSkipEnd.getValueFrom(),
                Math.min(mSliderSkipEnd.getValueTo(), progress));
        mSliderSkipEnd.setValue(progress);
    }

    // ---------- 辅助方法 ----------
    private int mapScaleTextToType(String text) {
        switch (text) {
            case "16 : 9": return VideoView.SCREEN_SCALE_16_9;
            case "4 : 3":  return VideoView.SCREEN_SCALE_4_3;
            case "默 认":   return VideoView.SCREEN_SCALE_DEFAULT;
            case "填 充":   return VideoView.SCREEN_SCALE_MATCH_PARENT;
            case "缩 放":   return VideoView.SCREEN_SCALE_ORIGINAL;
            case "裁 剪":   return VideoView.SCREEN_SCALE_CENTER_CROP;
            default:        return VideoView.SCREEN_SCALE_DEFAULT;
        }
    }

    private String getScaleText(int type) {
        switch (type) {
            case VideoView.SCREEN_SCALE_16_9:         return "16 : 9";
            case VideoView.SCREEN_SCALE_4_3:          return "4 : 3";
            case VideoView.SCREEN_SCALE_MATCH_PARENT: return "填 充";
            case VideoView.SCREEN_SCALE_ORIGINAL:     return "缩 放";
            case VideoView.SCREEN_SCALE_CENTER_CROP:  return "裁 剪";
            default:                                   return "默 认";
        }
    }

    private String formatSkipTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }

    // ---------- 显示/隐藏动画 ----------
    public void show() {
        if (mIsShowing) return;
        mIsShowing = true;
        setVisibility(VISIBLE);
        bringToFront();

        TranslateAnimation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        slideIn.setDuration(260);
        mPanelView.startAnimation(slideIn);

        mDimView.setAlpha(0f);
        mDimView.animate().alpha(1f).setDuration(260).start();

        if (mControlWrapper != null) mControlWrapper.hide();
    }

    public void hide() {
        if (!mIsShowing) return;
        mIsShowing = false;

        TranslateAnimation slideOut = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        slideOut.setDuration(200);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation a) { }
            @Override public void onAnimationRepeat(Animation a) { }
            @Override public void onAnimationEnd(Animation a) { setVisibility(GONE); }
        });
        mPanelView.startAnimation(slideOut);
        mDimView.animate().alpha(0f).setDuration(200).start();
    }

    public boolean isSettingsShowing() { return mIsShowing; }

    // ---------- 触摸拦截 ----------
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false; // 必须为 false，让子控件处理事件
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsShowing; // 当面板显示时，消费未被子视图处理的触摸事件
    }

    // ---------- IControlComponent 实现 ----------
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
    public void onVisibilityChanged(boolean isVisible, Animation anim) { }

    @Override
    public void onPlayStateChanged(int playState) {
        if (mIsShowing &&
                (playState == VideoView.STATE_IDLE || playState == VideoView.STATE_ERROR)) {
            hide();
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        // 横竖屏切换时可根据需要关闭面板，此处暂不处理
    }

    @Override
    public void setProgress(int duration, int position) { }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked && mIsShowing) hide();
    }
}
