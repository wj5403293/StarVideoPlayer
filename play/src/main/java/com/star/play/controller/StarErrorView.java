package com.star.play.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * 播放错误提示界面
 * 当视频播放出错时显示，包含错误信息和重试按钮
 */
public class StarErrorView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;      // 控制器包装类

    private TextView mMessageView;                // 错误信息文本
    private MaterialButton mRetryButton;          // 重试按钮

    // 触摸事件相关变量，用于防止父容器拦截滑动事件
    private float mDownX;
    private float mDownY;

    public StarErrorView(@NonNull Context context) {
        this(context, null);
    }

    public StarErrorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarErrorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() {
        // 初始隐藏，由播放状态控制显示
        setVisibility(GONE);

        // 填充布局
        LayoutInflater.from(getContext()).inflate(R.layout.star_error_view, this, true);

        // 绑定控件
        mMessageView = findViewById(R.id.message);
        mRetryButton = findViewById(R.id.status); // 布局中按钮 id 为 status

        // 设置重试点击事件
        mRetryButton.setOnClickListener(v -> {
            if (mControlWrapper == null) return;
            // 隐藏当前视图
            setVisibility(GONE);
            // 调用控制器的 replay 方法重新播放，参数 false 表示不重置播放状态（根据 DKPlayer 定义）
            mControlWrapper.replay(false);
        });

        // 设置可点击，防止点击事件穿透到下层
        setClickable(true);
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
        // 不需要额外处理
    }

    @Override
    public void onPlayStateChanged(int playState) {
        // 当播放状态为出错时显示本视图
        if (playState == VideoView.STATE_ERROR) {
            // 将视图置于顶层，避免被其他控件遮挡
            bringToFront();
            setVisibility(VISIBLE);
        } else if (playState == VideoView.STATE_IDLE) {
            // 当播放器空闲时隐藏错误视图
            setVisibility(GONE);
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
        // 不需要处理
    }

    @Override
    public void setProgress(int duration, int position) {
        // 不需要处理进度
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        // 不需要处理锁屏
    }

    /**
     * 重写触摸事件分发，防止父容器拦截滑动事件
     * 参考原 ErrorView 实现，确保错误视图内的触摸事件正常处理
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                // 请求父容器不要拦截后续触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getX() - mDownX);
                float deltaY = Math.abs(ev.getY() - mDownY);
                int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (deltaX > touchSlop || deltaY > touchSlop) {
                    // 当滑动距离超过阈值时，允许父容器拦截（通常是视频控制器需要滑动）
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                // 可以恢复，但通常不需要额外操作
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}