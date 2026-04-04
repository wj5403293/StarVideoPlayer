package com.star.dlna.cast.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.star.dlna.cast.model.MediaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地 HTTP 服务器
 * 用于向 DLNA 设备提供视频流
 */
public class CastHttpService {

    private static final String TAG = "CastHttpService";
    private static final int SERVER_PORT = 8765;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    private MediaInfo currentMediaInfo;
    private String serverUrl;

    /**
     * 启动服务并提供视频
     */
    public String startServing(MediaInfo mediaInfo) {
        this.currentMediaInfo = mediaInfo;

        if (!isRunning) {
            startServer();
        }

        // 生成视频 URL
        String fileName = getFileNameFromUri(mediaInfo.getUri());
        serverUrl = "http://" + getLocalIpAddress() + ":" + SERVER_PORT + "/" + fileName;

        return serverUrl;
    }

    /**
     * 停止服务
     */
    public void stop() {
        isRunning = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
            serverSocket = null;
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }

        currentMediaInfo = null;
    }

    /**
     * 启动 HTTP 服务器
     */
    private void startServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            isRunning = true;

            executorService = Executors.newCachedThreadPool();

            // 启动服务器线程
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.execute(() -> handleRequest(clientSocket));
                    } catch (IOException e) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting connection", e);
                        }
                    }
                }
            }).start();

            Log.d(TAG, "HTTP Server started on port " + SERVER_PORT);

        } catch (IOException e) {
            Log.e(TAG, "Error starting HTTP server", e);
        }
    }

    /**
     * 处理 HTTP 请求
     */
    private void handleRequest(Socket clientSocket) {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            // 读取请求头
            String request = readRequest(inputStream);
            Log.d(TAG, "Request: " + request.split("\\r\\n")[0]);

            // 解析请求
            Map<String, String> headers = parseHeaders(request);
            String range = headers.get("Range");

            if (currentMediaInfo == null || currentMediaInfo.getUri() == null) {
                send404(outputStream);
                return;
            }

            // 获取视频文件
            File videoFile = getFileFromUri(currentMediaInfo.getUri());
            if (videoFile == null || !videoFile.exists()) {
                send404(outputStream);
                return;
            }

            long fileLength = videoFile.length();
            long start = 0;
            long end = fileLength - 1;

            // 处理 Range 请求
            boolean isPartial = false;
            if (range != null && range.startsWith("bytes=")) {
                String[] ranges = range.substring(6).split("-");
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                isPartial = true;
            }

            // 发送响应头
            String contentType = currentMediaInfo.getMimeType() != null ?
                    currentMediaInfo.getMimeType() : "video/mp4";

            StringBuilder response = new StringBuilder();
            if (isPartial) {
                response.append("HTTP/1.1 206 Partial Content\r\n");
                response.append("Content-Range: bytes ").append(start).append("-").append(end)
                        .append("/").append(fileLength).append("\r\n");
            } else {
                response.append("HTTP/1.1 200 OK\r\n");
            }

            response.append("Content-Type: ").append(contentType).append("\r\n");
            response.append("Content-Length: ").append(end - start + 1).append("\r\n");
            response.append("Accept-Ranges: bytes\r\n");
            response.append("Connection: close\r\n");
            response.append("\r\n");

            outputStream.write(response.toString().getBytes());

            // 发送文件内容
            FileInputStream fileInputStream = new FileInputStream(videoFile);
            fileInputStream.skip(start);

            byte[] buffer = new byte[8192];
            long bytesToSend = end - start + 1;
            int bytesRead;

            while (bytesToSend > 0 && (bytesRead = fileInputStream.read(buffer, 0,
                    (int) Math.min(buffer.length, bytesToSend))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesToSend -= bytesRead;
            }

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            clientSocket.close();

            Log.d(TAG, "Video streaming completed");

        } catch (Exception e) {
            Log.e(TAG, "Error handling request", e);
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 读取 HTTP 请求
     */
    private String readRequest(InputStream inputStream) throws IOException {
        StringBuilder request = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            request.append(new String(buffer, 0, bytesRead));
            if (request.toString().contains("\r\n\r\n")) {
                break;
            }
        }

        return request.toString();
    }

    /**
     * 解析请求头
     */
    private Map<String, String> parseHeaders(String request) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = request.split("\r\n");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) break;

            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }

        return headers;
    }

    /**
     * 发送 404 响应
     */
    private void send404(OutputStream outputStream) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n\r\n";
        outputStream.write(response.getBytes());
        outputStream.flush();
    }

    /**
     * 从 URI 获取文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0) {
                return path.substring(lastSlash + 1);
            }
        }
        return "video.mp4";
    }

    /**
     * 从 URI 获取文件
     */
    private File getFileFromUri(Uri uri) {
        if (uri == null) return null;

        String path = uri.getPath();
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    /**
     * 获取本机 IP 地址
     */
    private String getLocalIpAddress() {
        try {
            java.net.NetworkInterface wlanInterface = java.net.NetworkInterface.getByName("wlan0");
            if (wlanInterface == null) {
                wlanInterface = java.net.NetworkInterface.getByName("eth0");
            }

            if (wlanInterface != null) {
                java.util.Enumeration<java.net.InetAddress> addresses = wlanInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') < 0) {
                        return addr.getHostAddress();
                    }
                }
            }

            // 备用方案
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address", e);
            return "127.0.0.1";
        }
    }
}
