package com.star.play;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StarShortDramaPlayer extends StarVideoPlayer {

    public StarShortDramaPlayer(@NonNull Context context) {
        this(context, null);
    }

    public StarShortDramaPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarShortDramaPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        hideShortDramaUI();
    }

    private void hideShortDramaUI() {
        setSelectButtonVisibility(GONE);
        setPreviousButtonVisibility(GONE);
        setNextButtonVisibility(GONE);
        setSpeedButtonVisibility(VISIBLE);
    }
}
