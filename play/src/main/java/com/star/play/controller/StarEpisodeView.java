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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.star.play.R;

import java.util.ArrayList;
import java.util.List;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;

public class StarEpisodeView extends FrameLayout implements IControlComponent {

    private ControlWrapper mControlWrapper;

    private LinearLayout mPanelView;
    private View mDimView;
    private RecyclerView mRecyclerView;
    private MaterialButton mCloseButton;

    private EpisodeAdapter mAdapter;
    private boolean mIsShowing = false;

    public interface OnEpisodeSelectListener {
        void onEpisodeSelect(int index, String title);
    }
    private OnEpisodeSelectListener mOnEpisodeSelectListener;

    public void setOnEpisodeSelectListener(OnEpisodeSelectListener l) {
        mOnEpisodeSelectListener = l;
    }

    public StarEpisodeView(@NonNull Context context) {
        this(context, null);
    }

    public StarEpisodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarEpisodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.star_episode_view, this, true);

        mDimView      = findViewById(R.id.dim);
        mPanelView    = findViewById(R.id.panel);
        mRecyclerView = findViewById(R.id.recycler_view);
        mCloseButton  = findViewById(R.id.btn_close);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new EpisodeAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mDimView.setOnClickListener(v -> hide());
        mCloseButton.setOnClickListener(v -> hide());
        mPanelView.setOnClickListener(v -> { });
    }

    public void setEpisodes(List<String> episodes, int currentIndex) {
        mAdapter.setData(episodes, currentIndex);
    }

    public void setCurrentIndex(int index) {
        mAdapter.setCurrentIndex(index);
        mRecyclerView.scrollToPosition(Math.max(0, index));
    }

    public void show() {
        if (mIsShowing) return;
        mIsShowing = true;
        setVisibility(VISIBLE);
        bringToFront();
        TranslateAnimation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        slideIn.setDuration(250);
        mPanelView.startAnimation(slideIn);

        mDimView.setAlpha(0f);
        mDimView.animate().alpha(1f).setDuration(250).start();

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

    public boolean isEpisodeShowing() { return mIsShowing; }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsShowing;
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Nullable
    @Override
    public View getView() { return this; }

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
    }

    @Override
    public void setProgress(int duration, int position) { }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        if (isLocked && mIsShowing) hide();
    }

    private class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.VH> {

        private final List<String> mData = new ArrayList<>();
        private int mCurrentIndex = -1;

        void setData(List<String> data, int currentIndex) {
            mData.clear();
            if (data != null) {
                mData.addAll(data);
            }
            mCurrentIndex = currentIndex;
            notifyDataSetChanged();
        }

        void setCurrentIndex(int index) {
            int old = mCurrentIndex;
            mCurrentIndex = index;
            if (old >= 0 && old < mData.size())    notifyItemChanged(old);
            if (index >= 0 && index < mData.size()) notifyItemChanged(index);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_episode, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.titleView.setText(mData.get(position));
            holder.titleView.setSelected(position == mCurrentIndex);
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                setCurrentIndex(pos);
                if (mOnEpisodeSelectListener != null)
                    mOnEpisodeSelectListener.onEpisodeSelect(pos, mData.get(pos));
                hide();
            });
        }

        @Override
        public int getItemCount() { return mData.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView titleView;
            VH(@NonNull View v) {
                super(v);
                titleView = v.findViewById(R.id.episode_title);
            }
        }
    }
}
