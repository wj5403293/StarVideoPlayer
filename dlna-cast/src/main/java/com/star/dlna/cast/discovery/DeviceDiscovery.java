package com.star.dlna.cast.discovery;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.star.dlna.cast.model.CastDevice;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * DLNA 设备发现
 * 使用 SSDP 协议搜索局域网内的 DLNA 设备
 */
public class DeviceDiscovery {

    private static final String TAG = "DeviceDiscovery";
    private static final String SSDP_ADDRESS = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final String M_SEARCH_MESSAGE =
            "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n" +
                    "\r\n";

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    private MulticastSocket receiveSocket;
    private volatile boolean isRunning = false;
    private Callback callback;
    private final Set<String> foundDevices = new HashSet<>();
    private Context context;
    private WifiManager.MulticastLock multicastLock;

    public interface Callback {
        void onDeviceFound(CastDevice device);
        void onDeviceLost(CastDevice device);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start(long timeout) {
        if (isRunning) {
            return;
        }
        isRunning = true;
        foundDevices.clear();

        acquireMulticastLock();

        executorService = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        initReceiveSocket();
        if (receiveSocket == null) {
            Log.e(TAG, "Failed to initialize multicast socket");
            stop();
            return;
        }

        executorService.execute(this::receiveResponses);
        scheduledExecutor.scheduleAtFixedRate(this::sendSearchRequest, 0, 3, TimeUnit.SECONDS);

        if (timeout > 0) {
            scheduledExecutor.schedule(this::stop, timeout, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        isRunning = false;

        releaseMulticastLock();

        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            scheduledExecutor = null;
        }

        if (receiveSocket != null && !receiveSocket.isClosed()) {
            receiveSocket.close();
            receiveSocket = null;
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private void initReceiveSocket() {
        try {
            receiveSocket = new MulticastSocket(SSDP_PORT);
            receiveSocket.setReuseAddress(true);
            receiveSocket.setSoTimeout(1000);

            InetAddress multicastAddress = InetAddress.getByName(SSDP_ADDRESS);
            for (NetworkInterface networkInterface : getEligibleInterfaces()) {
                try {
                    receiveSocket.joinGroup(
                            new InetSocketAddress(multicastAddress, SSDP_PORT),
                            networkInterface
                    );
                    Log.d(TAG, "Joined multicast group on " + networkInterface.getName());
                } catch (Exception e) {
                    Log.w(TAG, "Failed to join multicast on " + networkInterface.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing receive socket", e);
            if (receiveSocket != null && !receiveSocket.isClosed()) {
                receiveSocket.close();
            }
            receiveSocket = null;
        }
    }

    private void sendSearchRequest() {
        try {
            if (receiveSocket == null || receiveSocket.isClosed()) {
                return;
            }

            byte[] messageBytes = M_SEARCH_MESSAGE.getBytes();
            InetAddress multicastAddress = InetAddress.getByName(SSDP_ADDRESS);

            for (NetworkInterface networkInterface : getEligibleInterfaces()) {
                try {
                    receiveSocket.setNetworkInterface(networkInterface);
                    receiveSocket.send(new DatagramPacket(
                            messageBytes,
                            messageBytes.length,
                            multicastAddress,
                            SSDP_PORT
                    ));
                    Log.d(TAG, "Sent SSDP search via " + networkInterface.getName());
                } catch (Exception e) {
                    Log.w(TAG, "Failed to send via " + networkInterface.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending search request", e);
        }
    }

    private void receiveResponses() {
        try {
            byte[] buffer = new byte[4096];

            while (isRunning) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    receiveSocket.receive(packet);

                    String response = new String(packet.getData(), 0, packet.getLength());
                    parseResponse(response, packet.getAddress().getHostAddress());
                } catch (java.net.SocketTimeoutException ignored) {
                }
            }
        } catch (Exception e) {
            if (isRunning) {
                Log.e(TAG, "Error receiving responses", e);
            }
        }
    }

    private void parseResponse(String response, String ipAddress) {
        Log.d(TAG, "Received response from " + ipAddress + ":\n" + response);

        if (response.startsWith("M-SEARCH")) {
            Log.d(TAG, "Ignoring our own M-SEARCH request");
            return;
        }

        boolean isHttp200 = response.contains("HTTP/1.1 200 OK") || response.contains("HTTP/1.0 200 OK");
        boolean isNotify = response.startsWith("NOTIFY");
        if (!isHttp200 && !isNotify) {
            Log.d(TAG, "Not a valid SSDP response");
            return;
        }

        if (!response.contains("MediaRenderer") && !response.contains("AVTransport") &&
                !response.contains("RenderingControl") && !response.contains("ConnectionManager")) {
            Log.d(TAG, "Not a MediaRenderer or related service");
            return;
        }

        Pattern locationPattern = Pattern.compile("LOCATION:\\s*(.+?)(?:\\r\\n|\\n|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = locationPattern.matcher(response);

        if (matcher.find()) {
            String location = matcher.group(1).trim().replaceAll("[`\\s]", "");
            Log.d(TAG, "Found LOCATION: " + location);

            if (!foundDevices.add(location)) {
                return;
            }

            if (executorService != null) {
                executorService.execute(() -> fetchDeviceDetails(location, ipAddress));
            }
        }
    }

    private void fetchDeviceDetails(String descriptionUrl, String ipAddress) {
        try {
            URL url = new URL(descriptionUrl);
            InputStream inputStream = url.openStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            CastDevice device = new CastDevice();
            device.setDescriptionUrl(descriptionUrl);
            device.setIpAddress(ipAddress);
            device.setPort(url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
            device.setLastSeen(System.currentTimeMillis());

            Element root = doc.getDocumentElement();
            device.setUdn(getTextContent(root, "UDN"));
            device.setId(device.getUdn() != null ? device.getUdn() : descriptionUrl);
            device.setName(getTextContent(root, "friendlyName"));
            device.setManufacturer(getTextContent(root, "manufacturer"));
            device.setModelName(getTextContent(root, "modelName"));
            device.setModelNumber(getTextContent(root, "modelNumber"));

            if (device.getName() == null || device.getName().isEmpty()) {
                device.setName(device.getModelName() != null ? device.getModelName() : ipAddress);
            }

            inputStream.close();

            Log.d(TAG, "Found device: " + device.getName() + " at " + ipAddress);
            if (callback != null) {
                callback.onDeviceFound(device);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching device details from " + descriptionUrl, e);
        }
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private void acquireMulticastLock() {
        try {
            if (context != null && multicastLock == null) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    multicastLock = wifiManager.createMulticastLock("DLNACastDiscovery");
                    multicastLock.setReferenceCounted(true);
                    multicastLock.acquire();
                    Log.d(TAG, "MulticastLock acquired");
                }
            } else if (multicastLock != null) {
                multicastLock.acquire();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to acquire MulticastLock", e);
        }
    }

    private void releaseMulticastLock() {
        try {
            if (multicastLock != null && multicastLock.isHeld()) {
                multicastLock.release();
                Log.d(TAG, "MulticastLock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to release MulticastLock", e);
        }
    }

    private List<NetworkInterface> getEligibleInterfaces() throws Exception {
        List<NetworkInterface> interfaces = new ArrayList<>();
        Enumeration<NetworkInterface> candidates = NetworkInterface.getNetworkInterfaces();

        while (candidates.hasMoreElements()) {
            NetworkInterface networkInterface = candidates.nextElement();
            if (!networkInterface.isUp() ||
                    networkInterface.isLoopback() ||
                    networkInterface.isVirtual() ||
                    !networkInterface.supportsMulticast()) {
                continue;
            }

            boolean hasIpv4 = false;
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    hasIpv4 = true;
                    break;
                }
            }

            if (hasIpv4) {
                interfaces.add(networkInterface);
            }
        }

        return interfaces;
    }
}
