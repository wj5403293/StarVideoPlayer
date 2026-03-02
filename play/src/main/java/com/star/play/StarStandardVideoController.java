package com.star.play;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.star.play.R;
import com.star.play.controller.StarBottomView;
import com.star.play.controller.StarCompleteView;
import com.star.play.controller.StarErrorView;
import com.star.play.controller.StarGestureView;
import com.star.play.controller.StarLiveControlView;
import com.star.play.controller.StarPrepareView;
import com.star.play.controller.StarTitleView;

import xyz.doikki.videoplayer.controller.GestureVideoController;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 标准视频控制器，整合所有控制组件
 * 支持点播/直播，包含锁屏、加载、长按倍速等功能
 */
public class StarStandardVideoController extends GestureVideoController implements View.OnClickListener {

    private MaterialButton mLockButton;
    private CircularProgressIndicator mLoadingIndicator;
    private TextView mTcpSpeedView;
    private LinearLayout mSpeedLayout;
    private TextView mSpeedTextView;

    private boolean mIsBuffering;

    private OnSpeedListener mOnSpeedListener;
    private OnCancelSpeedListener mOnCancelSpeedListener;

    public interface OnSpeedListener {
        void onSpeed();
    }

    public interface OnCancelSpeedListener {
        void onCancelSpeed();
    }

    public void setOnSpeedListener(OnSpeedListener listener) {
        mOnSpeedListener = listener;
    }

    public void setOnCancelSpeedListener(OnCancelSpeedListener listener) {
        mOnCancelSpeedListener = listener;
    }

    public StarStandardVideoController(@NonNull Context context) {
        this(context, null);
    }

    public StarStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarStandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.star_standard_video_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        mLockButton = findViewById(R.id.lock);
        mLoadingIndicator = findViewById(R.id.loading);
        mTcpSpeedView = findViewById(R.id.TcpSpeed);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mSpeedTextView = findViewById(R.id.speed_text);

        mLockButton.setOnClickListener(this);
    }

    /**
     * 快速添加默认控制组件
     * @param title 视频标题
     * @param isLive 是否为直播
     */
    public void addDefaultControlComponent(String title, boolean isLive) {
        StarCompleteView completeView = new StarCompleteView(getContext());
        StarErrorView errorView = new StarErrorView(getContext());
        StarPrepareView prepareView = new StarPrepareView(getContext());
        prepareView.setClickStart();
        StarTitleView titleView = new StarTitleView(getContext());
        titleView.setTitle(title);
        addControlComponent(completeView, errorView, prepareView, titleView);

        addControlComponent(new StarGestureView(getContext()));

        if (isLive) {
            addControlComponent(new StarLiveControlView(getContext()));
        } else {
            addControlComponent(new StarBottomView(getContext()));
        }

        setCanChangePosition(!isLive);
    }

    public void setTcpSpeed(String speed) {
        if (mTcpSpeedView != null) {
            mTcpSpeedView.setText(speed);
        }
    }

    public void setSpeedLayoutVisibility(int visibility) {
        if (mSpeedLayout != null) {
            mSpeedLayout.setVisibility(visibility);
        }
    }

    public void setSpeedText(String text) {
        if (mSpeedTextView != null) {
            mSpeedTextView.setText(text);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.lock) {
            mControlWrapper.toggleLockState();
        }
    }

    @Override
    protected void onLockStateChanged(boolean isLocked) {
        super.onLockStateChanged(isLocked);
        if (isLocked) {
            mLockButton.setIconResource(R.drawable.lock);
        } else {
            mLockButton.setIconResource(R.drawable.lock_open);
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        if (mOnSpeedListener != null) {
            mOnSpeedListener.onSpeed();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mOnCancelSpeedListener != null) {
                    mOnCancelSpeedListener.onCancelSpeed();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        super.onVisibilityChanged(isVisible, anim);
        if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
            if (isVisible) {
                if (mLockButton.getVisibility() == GONE) {
                    mLockButton.setVisibility(VISIBLE);
                    if (anim != null) {
                        mLockButton.startAnimation(anim);
                    }
                }
            } else {
                if (mLockButton.getVisibility() == VISIBLE) {
                    mLockButton.setVisibility(GONE);
                    if (anim != null) {
                        mLockButton.startAnimation(anim);
                    }
                }
            }
        }
    }

    @Override
    protected void onPlayerStateChanged(int playerState) {
        super.onPlayerStateChanged(playerState);
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                mLockButton.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                mLockButton.setVisibility(isShowing() ? VISIBLE : GONE);
                break;
        }

        if (mActivity != null && hasCutout()) {
            int orientation = mActivity.getRequestedOrientation();
            int dp24 = PlayerUtils.dp2px(getContext(), 24);
            int cutoutHeight = getCutoutHeight();
            LayoutParams lp = (LayoutParams) mLockButton.getLayoutParams();
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                lp.setMargins(dp24, 0, dp24, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                lp.setMargins(dp24 + cutoutHeight, 0, dp24, 0);
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                lp.setMargins(dp24, 0, dp24 + cutoutHeight, 0);
            }
            mLockButton.setLayoutParams(lp);
        }
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        switch (playState) {
            case VideoView.STATE_IDLE:
                mLockButton.setSelected(false);
                mLoadingIndicator.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERED:
                if (playState == VideoView.STATE_BUFFERED) {
                    mIsBuffering = false;
                }
                if (!mIsBuffering) {
                    mLoadingIndicator.setVisibility(GONE);
                }
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                mLoadingIndicator.setVisibility(VISIBLE);
                if (playState == VideoView.STATE_BUFFERING) {
                    mIsBuffering = true;
                }
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mLoadingIndicator.setVisibility(GONE);
                mLockButton.setVisibility(GONE);
                mLockButton.setSelected(false);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isLocked()) {
            show();
            Toast.makeText(getContext(), "控制器已锁定", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
            return stopFullScreen();
        }
        return super.onBackPressed();
    }
}
