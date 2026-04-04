package com.star.dlna.cast.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.star.dlna.cast.R;
import com.star.dlna.cast.api.DLNACastManager;
import com.star.dlna.cast.model.CastState;

/**
 * 投屏控制视图 (Material Design 3)
 */
public class CastControlView extends MaterialCardView {

    private TextView tvDeviceName;
    private TextView tvStatus;
    private MaterialButton btnPlayPause;
    private MaterialButton btnStop;
    private MaterialButton btnDisconnect;
    private Slider sliderVolume;
    private SeekBar seekBarProgress;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;

    private boolean isPlaying = false;
    private DLNACastManager castManager;

    public CastControlView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CastControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CastControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        castManager = DLNACastManager.getInstance();

        LayoutInflater.from(context).inflate(R.layout.view_cast_control, this, true);

        // 设置卡片样式
        setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        setRadius(getResources().getDimension(R.dimen.card_corner_radius));

        initViews();
        setupListeners();
        observeState();
    }

    private void initViews() {
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvStatus = findViewById(R.id.tv_status);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        sliderVolume = findViewById(R.id.slider_volume);
        seekBarProgress = findViewById(R.id.seekbar_progress);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        // 设置设备名称
        if (castManager.getSelectedDevice() != null) {
            tvDeviceName.setText(castManager.getSelectedDevice().getName());
        }
    }

    private void setupListeners() {
        // 播放/暂停
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                castManager.pause(new DLNACastManager.CastCallback() {
                    @Override
                    public void onSuccess() {
                        updatePlayPauseButton(false);
                    }

                    @Override
                    public void onError(int code, String message) {
                    }
                });
            } else {
                castManager.play(new DLNACastManager.CastCallback() {
                    @Override
                    public void onSuccess() {
                        updatePlayPauseButton(true);
                    }

                    @Override
                    public void onError(int code, String message) {
                    }
                });
            }
        });

        // 停止
        btnStop.setOnClickListener(v -> {
            castManager.stop(new DLNACastManager.CastCallback() {
                @Override
                public void onSuccess() {
                    updatePlayPauseButton(false);
                }

                @Override
                public void onError(int code, String message) {
                }
            });
        });

        // 断开连接
        btnDisconnect.setOnClickListener(v -> {
            castManager.stop(null);
            castManager.selectDevice(null);
            setVisibility(GONE);
        });

        // 音量调节
        sliderVolume.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                castManager.setVolume((int) value, null);
            }
        });

        // 进度调节
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    long duration = castManager.getDuration();
                    long position = duration * progress / 100;
                    tvCurrentTime.setText(formatTime(position));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long duration = castManager.getDuration();
                long position = duration * seekBar.getProgress() / 100;
                castManager.seek(position, null);
            }
        });
    }

    private void observeState() {
        castManager.getCastState().observeForever(state -> {
            if (state == null) return;

            switch (state) {
                case IDLE:
                    tvStatus.setText(R.string.cast_status_idle);
                    updatePlayPauseButton(false);
                    break;
                case CONNECTING:
                    tvStatus.setText(R.string.cast_status_connecting);
                    break;
                case PREPARING:
                    tvStatus.setText(R.string.cast_status_preparing);
                    break;
                case PLAYING:
                    tvStatus.setText(R.string.cast_status_playing);
                    updatePlayPauseButton(true);
                    break;
                case PAUSED:
                    tvStatus.setText(R.string.cast_status_paused);
                    updatePlayPauseButton(false);
                    break;
                case BUFFERING:
                    tvStatus.setText(R.string.cast_status_buffering);
                    break;
                case ERROR:
                    tvStatus.setText(R.string.cast_status_error);
                    updatePlayPauseButton(false);
                    break;
                case COMPLETED:
                    tvStatus.setText(R.string.cast_status_completed);
                    updatePlayPauseButton(false);
                    break;
            }
        });

        // 更新进度
        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress();
                postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updatePlayPauseButton(boolean playing) {
        isPlaying = playing;
        btnPlayPause.setIconResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void updateProgress() {
        long position = castManager.getCurrentPosition();
        long duration = castManager.getDuration();

        tvCurrentTime.setText(formatTime(position));
        tvTotalTime.setText(formatTime(duration));

        if (duration > 0) {
            seekBarProgress.setProgress((int) (position * 100 / duration));
        }
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        if (castManager.getSelectedDevice() != null) {
            tvDeviceName.setText(castManager.getSelectedDevice().getName());
        }
    }

    public void hide() {
        setVisibility(GONE);
    }
}
