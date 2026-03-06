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

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarTitleView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    private TextView mTitleView;
    private TextView mSysTimeView;
    private MaterialButton mBackView;
    private MaterialButton mPipView;
    private MaterialButton mScreenView;
    private MaterialButton mSettingsView;
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
        if (mBackView != null) {
            mBackView.setOnClickListener(view -> {
                if (mControlWrapper == null) return;
                Activity activity = PlayerUtils.scanForActivity(getContext());
                if (activity != null && mControlWrapper.isFullScreen()) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mControlWrapper.stopFullScreen();
                }
            });
        }

        mTitleView = findViewById(R.id.title);
        mSysTimeView = findViewById(R.id.sys_time);

        mPipView = findViewById(R.id.pip);
        if (mPipView != null) {
            mPipView.setOnClickListener(view -> {
                if (mOnPipClickListener != null)
                    mOnPipClickListener.onPipClick(view);
            });
        }

        mScreenView = findViewById(R.id.screen);
        if (mScreenView != null) {
            mScreenView.setOnClickListener(view -> {
                if (mOnScreenClickListener != null)
                    mOnScreenClickListener.onScreenClick(view);
            });
        }

        mSettingsView = findViewById(R.id.settings);
        if (mSettingsView != null) {
            mSettingsView.setOnClickListener(view -> {
                if (mOnSettingsClickListener != null)
                    mOnSettingsClickListener.onSettingsClick(view);
            });
        }
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

    public void setTitleButtonsVisibility(int backVisibility, int pipVisibility, 
                                          int screenVisibility, int settingsVisibility) {
        if (mBackView != null) {
            mBackView.setVisibility(backVisibility);
        }
        if (mPipView != null) {
            mPipView.setVisibility(pipVisibility);
        }
        if (mScreenView != null) {
            mScreenView.setVisibility(screenVisibility);
        }
        if (mSettingsView != null) {
            mSettingsView.setVisibility(settingsVisibility);
        }
    }

    public void setBackButtonVisibility(int visibility) {
        if (mBackView != null) {
            mBackView.setVisibility(visibility);
        }
    }

    public void setPipButtonVisibility(int visibility) {
        if (mPipView != null) {
            mPipView.setVisibility(visibility);
        }
    }

    public void setScreenButtonVisibility(int visibility) {
        if (mScreenView != null) {
            mScreenView.setVisibility(visibility);
        }
    }

    public void setSettingsButtonVisibility(int visibility) {
        if (mSettingsView != null) {
            mSettingsView.setVisibility(visibility);
        }
    }

    public void setSysTimeVisibility(int visibility) {
        if (mSysTimeView != null) {
            mSysTimeView.setVisibility(visibility);
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
        if (mControlWrapper.isLocked()) return;

        if (isVisible) {
            if (getVisibility() == GONE) {
                if (mSysTimeView != null) {
                    mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
                }
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
            case VideoView.STATE_ERROR:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
                if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
                    setVisibility(VISIBLE);
                } else {
                    setVisibility(GONE);
                }
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        if (mControlWrapper == null) return;

        if (playerState == VideoView.PLAYER_FULL_SCREEN) {
            setVisibility(VISIBLE);
            if (mSysTimeView != null) {
                mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
            }
            if (mTitleView != null) mTitleView.setSelected(true);
        } else {
            setVisibility(GONE);
            if (mTitleView != null) mTitleView.setSelected(false);
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity != null && mControlWrapper.hasCutout() && mTitleContainer != null) {
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
                if (mSysTimeView != null) {
                    mSysTimeView.setText(PlayerUtils.getCurrentSystemTime());
                }
            }
        }
    }
}
