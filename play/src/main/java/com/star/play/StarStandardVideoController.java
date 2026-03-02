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

    private MaterialButton mLockButton;                     // 锁屏按钮
    private CircularProgressIndicator mLoadingIndicator;    // 加载指示器
    private TextView mTcpSpeedView;                         // 网速显示
    private LinearLayout mSpeedLayout;                      // 长按倍速布局
    private TextView mSpeedTextView;                         // 倍速文本

    private boolean mIsBuffering;                            // 是否正在缓冲

    // 长按倍速回调接口
    public interface OnSpeedListener {
        void onSpeed();
    }
    private static OnSpeedListener sOnSpeedListener;

    public static void setOnSpeedListener(OnSpeedListener listener) {
        sOnSpeedListener = listener;
    }

    // 取消倍速回调接口
    public interface OnCancelSpeedListener {
        void onCancelSpeed();
    }
    private static OnCancelSpeedListener sOnCancelSpeedListener;

    public static void setOnCancelSpeedListener(OnCancelSpeedListener listener) {
        sOnCancelSpeedListener = listener;
    }

    // 构造方法
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
        // 绑定控件
        mLockButton = findViewById(R.id.lock);
        mLoadingIndicator = findViewById(R.id.loading);
        mTcpSpeedView = findViewById(R.id.TcpSpeed);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mSpeedTextView = findViewById(R.id.speed_text);

        // 设置点击监听
        mLockButton.setOnClickListener(this);
    }

    /**
     * 快速添加默认控制组件
     * @param title 视频标题
     * @param isLive 是否为直播
     */
    public void addDefaultControlComponent(String title, boolean isLive) {
        // 添加完成界面
        StarCompleteView completeView = new StarCompleteView(getContext());
        // 添加错误界面
        StarErrorView errorView = new StarErrorView(getContext());
        // 添加准备界面
        StarPrepareView prepareView = new StarPrepareView(getContext());
        prepareView.setClickStart(); // 设置点击封面开始播放
        // 添加标题栏
        StarTitleView titleView = new StarTitleView(getContext());
        titleView.setTitle(title);
        // 将以上组件添加到控制器
        addControlComponent(completeView, errorView, prepareView, titleView);

        // 添加手势控制
        addControlComponent(new StarGestureView(getContext()));

        // 根据直播/点播添加底部控制栏
        if (isLive) {
            addControlComponent(new StarLiveControlView(getContext()));
        } else {
            addControlComponent(new StarBottomView(getContext()));
        }

        // 设置是否可拖动进度（点播可拖动，直播不可）
        setCanChangePosition(!isLive);
    }

    /**
     * 设置网速文本
     */
    public void setTcpSpeed(String speed) {
        if (mTcpSpeedView != null) {
            mTcpSpeedView.setText(speed);
        }
    }

    /**
     * 设置长按倍速布局可见性
     */
    public void setSpeedLayoutVisibility(int visibility) {
        if (mSpeedLayout != null) {
            mSpeedLayout.setVisibility(visibility);
        }
    }

    /**
     * 设置倍速文本
     */
    public void setSpeedText(String text) {
        if (mSpeedTextView != null) {
            mSpeedTextView.setText(text);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.lock) {
            // 切换锁屏状态
            mControlWrapper.toggleLockState();
        }
    }

    @Override
    protected void onLockStateChanged(boolean isLocked) {
        super.onLockStateChanged(isLocked);
        // 更新锁按钮图标
        if (isLocked) {
            mLockButton.setIconResource(R.drawable.lock); // 锁定图标
        } else {
            mLockButton.setIconResource(R.drawable.lock_open); // 解锁图标
        }
    }

    // 修正：将返回类型改为 void，并调用父类方法
    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e); // 调用父类处理（如果有必要）
        if (sOnSpeedListener != null) {
            sOnSpeedListener.onSpeed();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 手指松开或取消，取消倍速
                if (sOnCancelSpeedListener != null) {
                    sOnCancelSpeedListener.onCancelSpeed();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        super.onVisibilityChanged(isVisible, anim);
        // 全屏模式下，控制栏显示/隐藏时同步锁按钮的显示
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
                // 普通模式下锁按钮隐藏
                mLockButton.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                // 全屏模式下，根据控制栏显示状态决定锁按钮显示
                mLockButton.setVisibility(isShowing() ? VISIBLE : GONE);
                break;
        }

        // 刘海屏适配：根据屏幕方向调整锁按钮边距
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
        // 如果当前处于锁定状态，则显示提示并返回 true 拦截返回键
        if (isLocked()) {
            show(); // 显示控制栏
            Toast.makeText(getContext(), "控制器已锁定", Toast.LENGTH_SHORT).show();
            return true;
        }
        // 如果当前是全屏，则退出全屏
        if (mControlWrapper != null && mControlWrapper.isFullScreen()) {
            return stopFullScreen();
        }
        return super.onBackPressed();
    }
}