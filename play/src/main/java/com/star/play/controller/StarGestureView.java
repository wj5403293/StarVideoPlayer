package com.star.play.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IGestureComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class StarGestureView extends FrameLayout implements IGestureComponent {

    private ControlWrapper mControlWrapper;

    private MaterialButton mIconView;
    private LinearProgressIndicator mProgressIndicator;
    private TextView mPercentTextView;
    private LinearLayout mCenterContainer;

    public StarGestureView(@NonNull Context context) {
        this(context, null);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_gesture_view, this, true);

        mIconView = findViewById(R.id.iv_icon);
        mProgressIndicator = findViewById(R.id.pro_percent);
        mPercentTextView = findViewById(R.id.tv_percent);
        mCenterContainer = findViewById(R.id.center_container);
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void onStartSlide() {
        if (mControlWrapper != null) {
            mControlWrapper.hide();
        }
        if (mCenterContainer != null) {
            mCenterContainer.setVisibility(VISIBLE);
            mCenterContainer.setAlpha(1f);
        }
    }

    @Override
    public void onStopSlide() {
        if (mCenterContainer == null) return;
        mCenterContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mCenterContainer != null) {
                            mCenterContainer.setVisibility(GONE);
                        }
                    }
                })
                .start();
    }

    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {
        if (mProgressIndicator != null) mProgressIndicator.setVisibility(GONE);
        if (mIconView != null) {
            if (slidePosition > currentPosition) {
                mIconView.setIconResource(R.drawable.fast_forward);
            } else {
                mIconView.setIconResource(R.drawable.fast_rewind);
            }
        }
        if (mPercentTextView != null) {
            mPercentTextView.setText(String.format("%s/%s",
                    PlayerUtils.stringForTime(slidePosition),
                    PlayerUtils.stringForTime(duration)));
        }
    }

    @Override
    public void onBrightnessChange(int percent) {
        if (mProgressIndicator != null) {
            mProgressIndicator.setVisibility(VISIBLE);
            mProgressIndicator.setProgress(percent);
        }
        if (mIconView != null) {
            mIconView.setIconResource(R.drawable.brightness);
        }
        if (mPercentTextView != null) {
            mPercentTextView.setText(percent + "%");
        }
    }

    @Override
    public void onVolumeChange(int percent) {
        if (mProgressIndicator != null) {
            mProgressIndicator.setVisibility(VISIBLE);
            mProgressIndicator.setProgress(percent);
        }
        if (mIconView != null) {
            if (percent <= 0) {
                mIconView.setIconResource(R.drawable.volume_off);
            } else {
                mIconView.setIconResource(R.drawable.volume_up);
            }
        }
        if (mPercentTextView != null) {
            mPercentTextView.setText(percent + "%");
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        if (playState == VideoView.STATE_IDLE
                || playState == VideoView.STATE_START_ABORT
                || playState == VideoView.STATE_PREPARING
                || playState == VideoView.STATE_PREPARED
                || playState == VideoView.STATE_ERROR
                || playState == VideoView.STATE_PLAYBACK_COMPLETED) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void setProgress(int duration, int position) {
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
    }
}
