package com.star.dlna.cast.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.star.dlna.cast.model.CastDevice;
import com.star.dlna.cast.model.CastState;
import com.star.dlna.cast.model.MediaInfo;

import java.util.List;

/**
 * DLNA 投屏管理器接口
 * 对外提供的核心 API
 */
public interface DLNACastManager {

    /**
     * 获取单例实例
     */
    static DLNACastManager getInstance() {
        return DLNACastManagerImpl.getInstance();
    }

    /**
     * 初始化
     * @param context 应用上下文
     */
    void init(@NonNull Context context);

    /**
     * 释放资源
     */
    void release();

    // ==================== 设备发现 ====================

    /**
     * 开始搜索设备
     * @param timeout 超时时间（毫秒）
     */
    void startDiscovery(long timeout);

    /**
     * 停止搜索
     */
    void stopDiscovery();

    /**
     * 获取设备列表 LiveData
     */
    LiveData<List<CastDevice>> getDevices();

    /**
     * 获取当前选中的设备
     */
    @Nullable
    CastDevice getSelectedDevice();

    /**
     * 选择设备
     * @param device 设备，null 表示取消选择
     */
    void selectDevice(@Nullable CastDevice device);

    // ==================== 投屏控制 ====================

    /**
     * 开始投屏
     * @param mediaInfo 媒体信息
     * @param callback 回调
     */
    void cast(@NonNull MediaInfo mediaInfo, @Nullable CastCallback callback);

    /**
     * 播放
     */
    void play(@Nullable CastCallback callback);

    /**
     * 暂停
     */
    void pause(@Nullable CastCallback callback);

    /**
     * 停止
     */
    void stop(@Nullable CastCallback callback);

    /**
     * 跳转进度
     * @param position 位置（毫秒）
     */
    void seek(long position, @Nullable CastCallback callback);

    /**
     * 设置音量
     * @param volume 音量 0-100
     */
    void setVolume(int volume, @Nullable CastCallback callback);

    /**
     * 获取当前播放状态
     */
    LiveData<CastState> getCastState();

    /**
     * 获取当前播放位置（毫秒）
     */
    long getCurrentPosition();

    /**
     * 获取总时长（毫秒）
     */
    long getDuration();

    // ==================== 回调接口 ====================

    interface CastCallback {
        void onSuccess();
        void onError(int code, String message);
    }
}
