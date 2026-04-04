package com.star.dlna.cast.api;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.star.dlna.cast.control.DLNAController;
import com.star.dlna.cast.discovery.DeviceDiscovery;
import com.star.dlna.cast.model.CastDevice;
import com.star.dlna.cast.model.CastState;
import com.star.dlna.cast.model.MediaInfo;
import com.star.dlna.cast.server.CastHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DLNACastManager 实现类
 */
class DLNACastManagerImpl implements DLNACastManager {

    private static volatile DLNACastManagerImpl instance;
    private final Object lock = new Object();

    private Context applicationContext;
    private DeviceDiscovery deviceDiscovery;
    private DLNAController dlnaController;
    private CastHttpService httpService;
    private ExecutorService executorService;
    private Handler mainHandler;

    private final MutableLiveData<List<CastDevice>> devicesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<CastState> castStateLiveData = new MutableLiveData<>(CastState.IDLE);
    private CastDevice selectedDevice;
    private MediaInfo currentMediaInfo;

    private DLNACastManagerImpl() {
    }

    static DLNACastManagerImpl getInstance() {
        if (instance == null) {
            synchronized (DLNACastManagerImpl.class) {
                if (instance == null) {
                    instance = new DLNACastManagerImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void init(@NonNull Context context) {
        synchronized (lock) {
            if (applicationContext != null) {
                return;
            }
            applicationContext = context.getApplicationContext();
            executorService = Executors.newCachedThreadPool();
            mainHandler = new Handler(Looper.getMainLooper());

            deviceDiscovery = new DeviceDiscovery();
            deviceDiscovery.init(applicationContext);
            deviceDiscovery.setCallback(new DeviceDiscovery.Callback() {
                @Override
                public void onDeviceFound(CastDevice device) {
                    addDevice(device);
                }

                @Override
                public void onDeviceLost(CastDevice device) {
                    removeDevice(device);
                }
            });

            dlnaController = new DLNAController();
            httpService = new CastHttpService();
        }
    }

    @Override
    public void release() {
        synchronized (lock) {
            stopDiscovery();
            if (dlnaController != null) {
                dlnaController.disconnect();
            }
            if (httpService != null) {
                httpService.stop();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
            applicationContext = null;
        }
    }

    // ==================== 设备发现 ====================

    @Override
    public void startDiscovery(long timeout) {
        if (deviceDiscovery != null) {
            deviceDiscovery.start(timeout);
        }
    }

    @Override
    public void stopDiscovery() {
        if (deviceDiscovery != null) {
            deviceDiscovery.stop();
        }
    }

    @Override
    public LiveData<List<CastDevice>> getDevices() {
        return devicesLiveData;
    }

    @Override
    public CastDevice getSelectedDevice() {
        return selectedDevice;
    }

    @Override
    public void selectDevice(@Nullable CastDevice device) {
        this.selectedDevice = device;
        if (dlnaController != null) {
            dlnaController.setDevice(device);
        }
    }

    // ==================== 投屏控制 ====================

    @Override
    public void cast(@NonNull MediaInfo mediaInfo, @Nullable CastCallback callback) {
        if (selectedDevice == null) {
            if (callback != null) {
                runOnMainThread(() -> callback.onError(-1, "No device selected"));
            }
            return;
        }

        currentMediaInfo = mediaInfo;
        updateCastState(CastState.PREPARING);

        executorService.execute(() -> {
            try {
                // 判断是否网络 URL
                String videoUrl;
                if (isNetworkUrl(mediaInfo.getUri().toString())) {
                    // 网络 URL 直接使用
                    videoUrl = mediaInfo.getUri().toString();
                    Log.d(TAG, "Using network URL directly: " + videoUrl);
                } else {
                    // 本地文件启动 HTTP 服务器
                    videoUrl = httpService.startServing(mediaInfo);
                    Log.d(TAG, "Using local server: " + videoUrl);
                }

                // 设置视频 URI 并播放
                dlnaController.setAVTransportURI(videoInfoToDidl(videoUrl), new DLNAController.Callback() {
                    @Override
                    public void onSuccess() {
                        dlnaController.play(new DLNAController.Callback() {
                            @Override
                            public void onSuccess() {
                                updateCastState(CastState.PLAYING);
                                if (callback != null) {
                                    runOnMainThread(callback::onSuccess);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                updateCastState(CastState.ERROR);
                                if (callback != null) {
                                    runOnMainThread(() -> callback.onError(-2, error));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        updateCastState(CastState.ERROR);
                        if (callback != null) {
                            runOnMainThread(() -> callback.onError(-3, error));
                        }
                    }
                });
            } catch (Exception e) {
                updateCastState(CastState.ERROR);
                if (callback != null) {
                    runOnMainThread(() -> callback.onError(-4, e.getMessage()));
                }
            }
        });
    }

    private boolean isNetworkUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    @Override
    public void play(@Nullable CastCallback callback) {
        if (dlnaController != null) {
            dlnaController.play(wrapCallback(callback));
        }
    }

    @Override
    public void pause(@Nullable CastCallback callback) {
        if (dlnaController != null) {
            dlnaController.pause(wrapCallback(callback));
        }
    }

    @Override
    public void stop(@Nullable CastCallback callback) {
        if (dlnaController != null) {
            dlnaController.stop(wrapCallback(callback));
        }
    }

    @Override
    public void seek(long position, @Nullable CastCallback callback) {
        if (dlnaController != null) {
            dlnaController.seek(position, wrapCallback(callback));
        }
    }

    @Override
    public void setVolume(int volume, @Nullable CastCallback callback) {
        if (dlnaController != null) {
            dlnaController.setVolume(volume, wrapCallback(callback));
        }
    }

    @Override
    public LiveData<CastState> getCastState() {
        return castStateLiveData;
    }

    @Override
    public long getCurrentPosition() {
        if (dlnaController != null) {
            return dlnaController.getPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (dlnaController != null) {
            return dlnaController.getDuration();
        }
        return 0;
    }

    // ==================== 私有方法 ====================

    private void addDevice(CastDevice device) {
        List<CastDevice> currentList = devicesLiveData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        // 检查是否已存在
        boolean exists = false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getUdn().equals(device.getUdn())) {
                currentList.set(i, device);
                exists = true;
                break;
            }
        }

        if (!exists) {
            currentList.add(device);
        }

        devicesLiveData.postValue(currentList);
    }

    private void removeDevice(CastDevice device) {
        List<CastDevice> currentList = devicesLiveData.getValue();
        if (currentList != null) {
            currentList.removeIf(d -> d.getUdn().equals(device.getUdn()));
            devicesLiveData.postValue(currentList);
        }
    }

    private void updateCastState(CastState state) {
        castStateLiveData.postValue(state);
    }

    private void runOnMainThread(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }

    private DLNAController.Callback wrapCallback(@Nullable CastCallback callback) {
        return new DLNAController.Callback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    runOnMainThread(callback::onSuccess);
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    runOnMainThread(() -> callback.onError(-1, error));
                }
            }
        };
    }

    private String videoInfoToDidl(String videoUrl) {
        // 生成 DIDL-Lite 格式的元数据
        String title = currentMediaInfo != null ? currentMediaInfo.getTitle() : "Video";
        String mimeType = currentMediaInfo != null ? currentMediaInfo.getMimeType() : "video/mp4";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
                "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">" +
                "<item id=\"0\" parentID=\"-1\" restricted=\"true\">" +
                "<dc:title>" + escapeXml(title) + "</dc:title>" +
                "<upnp:class>object.item.videoItem</upnp:class>" +
                "<res protocolInfo=\"http-get:*:" + mimeType + ":*\">" + videoUrl + "</res>" +
                "</item></DIDL-Lite>";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
