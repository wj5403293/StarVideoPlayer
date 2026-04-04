package com.star.dlna.cast.control;

import android.util.Log;

import com.star.dlna.cast.model.CastDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DLNA 控制器
 * 实现 AVTransport 和 RenderingControl 服务
 */
public class DLNAController {

    private static final String TAG = "DLNAController";
    private static final long SERVICE_PARSE_TIMEOUT_MS = 5000L;

    private final Object serviceLock = new Object();

    private CastDevice device;
    private String avTransportUrl;
    private String renderingControlUrl;
    private boolean isParsingServices;
    private int currentVolume = 50;
    private long currentPosition = 0;
    private long duration = 0;

    public interface Callback {
        void onSuccess();
        void onError(String error);
    }

    public void setDevice(CastDevice device) {
        synchronized (serviceLock) {
            this.device = device;
            avTransportUrl = null;
            renderingControlUrl = null;
            isParsingServices = device != null;
        }

        if (device != null) {
            new Thread(() -> parseDeviceServices(device)).start();
        }
    }

    public void disconnect() {
        synchronized (serviceLock) {
            device = null;
            avTransportUrl = null;
            renderingControlUrl = null;
            isParsingServices = false;
            serviceLock.notifyAll();
        }
    }

    /**
     * 解析设备服务 URL
     */
    private void parseDeviceServices(CastDevice targetDevice) {
        HttpURLConnection connection = null;
        try {
            URL descriptionUrl = new URL(targetDevice.getDescriptionUrl());
            connection = (HttpURLConnection) descriptionUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            String response = readStream(inputStream);
            inputStream.close();

            String parsedAvTransportUrl = findControlUrl(
                    response,
                    "AVTransport",
                    descriptionUrl
            );
            String parsedRenderingControlUrl = findControlUrl(
                    response,
                    "RenderingControl",
                    descriptionUrl
            );

            synchronized (serviceLock) {
                if (!isSameDevice(targetDevice, device)) {
                    return;
                }
                avTransportUrl = parsedAvTransportUrl;
                renderingControlUrl = parsedRenderingControlUrl;
                isParsingServices = false;
                serviceLock.notifyAll();
            }

            Log.d(TAG, "AVTransport URL: " + parsedAvTransportUrl);
            Log.d(TAG, "RenderingControl URL: " + parsedRenderingControlUrl);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing device services", e);
            synchronized (serviceLock) {
                if (isSameDevice(targetDevice, device)) {
                    isParsingServices = false;
                    serviceLock.notifyAll();
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 设置媒体 URI
     */
    public void setAVTransportURI(String didlMetadata, Callback callback) {
        String targetUrl = requireServiceUrl(true, callback);
        if (targetUrl == null) {
            return;
        }

        String soapAction = "\"urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:SetAVTransportURI xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<CurrentURI>" + escapeXml(extractUrlFromDidl(didlMetadata)) + "</CurrentURI>" +
                "<CurrentURIMetaData>" + escapeXml(didlMetadata) + "</CurrentURIMetaData>" +
                "</u:SetAVTransportURI>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 播放
     */
    public void play(Callback callback) {
        String targetUrl = requireServiceUrl(true, callback);
        if (targetUrl == null) {
            return;
        }

        String soapAction = "\"urn:schemas-upnp-org:service:AVTransport:1#Play\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:Play xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<Speed>1</Speed>" +
                "</u:Play>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 暂停
     */
    public void pause(Callback callback) {
        String targetUrl = requireServiceUrl(true, callback);
        if (targetUrl == null) {
            return;
        }

        String soapAction = "\"urn:schemas-upnp-org:service:AVTransport:1#Pause\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:Pause xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "</u:Pause>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 停止
     */
    public void stop(Callback callback) {
        String targetUrl = requireServiceUrl(true, callback);
        if (targetUrl == null) {
            return;
        }

        String soapAction = "\"urn:schemas-upnp-org:service:AVTransport:1#Stop\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:Stop xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "</u:Stop>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 跳转进度
     */
    public void seek(long positionMs, Callback callback) {
        String targetUrl = requireServiceUrl(true, callback);
        if (targetUrl == null) {
            return;
        }

        String target = formatTime(positionMs);

        String soapAction = "\"urn:schemas-upnp-org:service:AVTransport:1#Seek\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:Seek xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<Unit>REL_TIME</Unit>" +
                "<Target>" + target + "</Target>" +
                "</u:Seek>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 设置音量
     */
    public void setVolume(int volume, Callback callback) {
        String targetUrl = requireServiceUrl(false, callback);
        if (targetUrl == null) {
            return;
        }

        currentVolume = Math.max(0, Math.min(100, volume));

        String soapAction = "\"urn:schemas-upnp-org:service:RenderingControl:1#SetVolume\"";
        String soapBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:SetVolume xmlns:u=\"urn:schemas-upnp-org:service:RenderingControl:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<Channel>Master</Channel>" +
                "<DesiredVolume>" + currentVolume + "</DesiredVolume>" +
                "</u:SetVolume>" +
                "</s:Body>" +
                "</s:Envelope>";

        sendSoapRequest(targetUrl, soapAction, soapBody, callback);
    }

    /**
     * 获取播放位置
     */
    public long getPosition() {
        return currentPosition;
    }

    /**
     * 获取总时长
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 发送 SOAP 请求
     */
    private void sendSoapRequest(String url, String soapAction, String soapBody, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL requestUrl = new URL(url);
                connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction", soapAction);
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(soapBody.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    InputStream errorStream = connection.getErrorStream();
                    String error = errorStream != null ? readStream(errorStream) : "HTTP " + responseCode;
                    Log.e(TAG, "SOAP error: " + error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sending SOAP request", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private String requireServiceUrl(boolean avTransport, Callback callback) {
        synchronized (serviceLock) {
            waitForServiceParsingLocked();

            String serviceUrl = avTransport ? avTransportUrl : renderingControlUrl;
            if (serviceUrl != null) {
                return serviceUrl;
            }
        }

        if (callback != null) {
            callback.onError(avTransport
                    ? "AVTransport service not available"
                    : "RenderingControl service not available");
        }
        return null;
    }

    private void waitForServiceParsingLocked() {
        long deadline = System.currentTimeMillis() + SERVICE_PARSE_TIMEOUT_MS;
        while (isParsingServices) {
            long waitTime = deadline - System.currentTimeMillis();
            if (waitTime <= 0) {
                isParsingServices = false;
                break;
            }
            try {
                serviceLock.wait(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isParsingServices = false;
                break;
            }
        }
    }

    private String findControlUrl(String response, String serviceName, URL descriptionUrl) {
        Pattern pattern = Pattern.compile(
                "<serviceType>urn:schemas-upnp-org:service:" + serviceName + ":\\d+</serviceType>.*?<controlURL>(.+?)</controlURL>",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(response);
        if (!matcher.find()) {
            return null;
        }

        String controlPath = matcher.group(1).trim();
        return resolveControlUrl(descriptionUrl, controlPath);
    }

    private String resolveControlUrl(URL descriptionUrl, String controlPath) {
        try {
            return new URL(descriptionUrl, controlPath).toString();
        } catch (Exception e) {
            Log.w(TAG, "Failed to resolve control URL: " + controlPath, e);
            return controlPath;
        }
    }

    private boolean isSameDevice(CastDevice first, CastDevice second) {
        if (first == null || second == null) {
            return false;
        }
        String firstUdn = first.getUdn();
        String secondUdn = second.getUdn();
        if (firstUdn != null && secondUdn != null) {
            return firstUdn.equals(secondUdn);
        }
        return String.valueOf(first.getDescriptionUrl()).equals(String.valueOf(second.getDescriptionUrl()));
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&') {
                int next = i + 1;
                if (next < text.length()) {
                    char nextChar = text.charAt(next);
                    if (nextChar == '#' || Character.isLetter(nextChar)) {
                        sb.append(c);
                    } else {
                        sb.append("&amp;");
                    }
                } else {
                    sb.append("&amp;");
                }
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String extractUrlFromDidl(String didl) {
        Pattern pattern = Pattern.compile("<res[^>]*>(.+?)</res>");
        Matcher matcher = pattern.matcher(didl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String formatTime(long ms) {
        long hours = ms / 3600000;
        long minutes = (ms % 3600000) / 60000;
        long seconds = (ms % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
