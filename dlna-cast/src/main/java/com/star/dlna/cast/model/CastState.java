package com.star.dlna.cast.model;

/**
 * 投屏状态
 */
public enum CastState {
    IDLE,           // 空闲
    CONNECTING,     // 连接中
    PREPARING,      // 准备中
    PLAYING,        // 播放中
    PAUSED,         // 已暂停
    BUFFERING,      // 缓冲中
    ERROR,          // 错误
    COMPLETED       // 播放完成
}
