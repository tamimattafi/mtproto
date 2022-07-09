package com.attafitamim.mtproto.client.tgnet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.attafitamim.mtproto.core.types.TLMethod;
import com.attafitamim.mtproto.client.core.BuildVars;
import com.attafitamim.mtproto.client.core.StatsController;
import com.attafitamim.mtproto.client.core.UserConfig;
import com.attafitamim.mtproto.client.core.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionsManager {

    public final static int ConnectionTypeGeneric = 1;
    public final static int ConnectionTypeDownload = 2;
    public final static int ConnectionTypeUpload = 4;
    public final static int ConnectionTypePush = 8;
    public final static int ConnectionTypeDownload2 = ConnectionTypeDownload | (1 << 16);

    public final static int FileTypePhoto = 0x01000000;
    public final static int FileTypeVideo = 0x02000000;
    public final static int FileTypeAudio = 0x03000000;
    public final static int FileTypeFile = 0x04000000;

    public final static int RequestFlagEnableUnauthorized = 1;
    public final static int RequestFlagFailOnServerErrors = 2;
    public final static int RequestFlagCanCompress = 4;
    public final static int RequestFlagWithoutLogin = 8;
    public final static int RequestFlagTryDifferentDc = 16;
    public final static int RequestFlagForceDownload = 32;
    public final static int RequestFlagInvokeAfter = 64;
    public final static int RequestFlagNeedQuickAck = 128;

    public final static int ConnectionStateConnecting = 1;
    public final static int ConnectionStateWaitingForNetwork = 2;
    public final static int ConnectionStateConnected = 3;
    public final static int ConnectionStateConnectingToProxy = 4;
    public final static int ConnectionStateUpdating = 5;

    private static long lastDnsRequestTime;

    public final static int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;

    private long lastPauseTime = System.currentTimeMillis();
    private boolean isAppPaused = true;
    private boolean isUpdating;
    private int connectionState;
    private AtomicInteger lastRequestToken = new AtomicInteger(1);
    private int appResumeCount;

    private static AsyncTask currentTask;

    private static class ResolvedDomain {

        public String address;
        long ttl;

        public ResolvedDomain(String a, long t) {
            address = a;
            ttl = t;
        }
    }

    private static ThreadLocal<HashMap<String, ResolvedDomain>> dnsCache = new ThreadLocal<HashMap<String, ResolvedDomain>>() {
        @Override
        protected HashMap<String, ResolvedDomain> initialValue() {
            return new HashMap<>();
        }
    };

    private static int lastClassGuid = 1;

    private int currentAccount;

    public static UpdateHandler updateHandler;

    public ConnectionsManager(
            int userId,
            BackendInfo backendInfo,
            File config,
            String deviceId,
            Context context,
            int appVersionCode,
            String appVersionName,
            int layer,
            int appId,
            String logPath,
            boolean enablePushConnection
    ) {
        currentAccount = UserConfig.selectedAccount;
        connectionState = native_getConnectionState(currentAccount);
        String deviceModel;
        String systemLangCode;
        String langCode;
        String appVersion;
        String systemVersion;
        if (currentAccount != 0) {
            config = new File(config, "account" + currentAccount);
            config.mkdirs();
        }
        String configPath = config.toString();
        try {
            systemLangCode = Locale.getDefault().getLanguage();
            langCode = Locale.getDefault().getISO3Language();
            deviceModel = Build.MANUFACTURER + Build.MODEL;
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        } catch (Exception e) {
            systemLangCode = "en";
            langCode = "";
            deviceModel = "Android unknown";
            appVersion = "App version unknown";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        }
        if (systemLangCode.trim().length() == 0) {
            systemLangCode = "en";
        }
        if (deviceModel.trim().length() == 0) {
            deviceModel = "Android unknown";
        }
        if (appVersion.trim().length() == 0) {
            appVersion = "App version unknown";
        }
        if (systemVersion.trim().length() == 0) {
            systemVersion = "SDK Unknown";
        }
        UserConfig.getInstance(currentAccount).loadConfig(context);

        init(
                appVersionCode,
                backendInfo,
                layer,
                appId,
                deviceModel,
                systemVersion,
                appVersionName,
                langCode,
                systemLangCode,
                configPath,
                logPath,
                deviceId,
                userId,
                enablePushConnection,
                context
        );
    }

    public void onAuthSuccess(int userId) {
        this.setUserId(userId);
        this.updateDcSettings();
    }

    public long getCurrentTimeMillis() {
        return native_getCurrentTimeMillis(currentAccount);
    }

    public int getCurrentTime() {
        return native_getCurrentTime(currentAccount);
    }

    public int getTimeDifference() {
        return native_getTimeDifference(currentAccount);
    }

    public <T> int sendRequest(TLMethod<T> method, RequestDelegate<T> completionBlock, int flags, int connetionType) throws Exception {
        return sendRequest(method, completionBlock, null, null, flags, DEFAULT_DATACENTER_ID, connetionType, false);
    }

    public <T> int sendRequest(TLMethod<T> method, RequestDelegate<T> completionBlock, QuickAckDelegate quickAckBlock, int flags) throws Exception {
        return sendRequest(method, completionBlock, quickAckBlock, null, flags, DEFAULT_DATACENTER_ID, ConnectionTypeGeneric, false);
    }

    public <T> int sendRequest(TLMethod<T> method, final RequestDelegate<T> onComplete, final QuickAckDelegate onQuickAck, final WriteToSocketDelegate onWriteToSocket, final int flags, final int datacenterId, final int connetionType, final boolean immediate) throws Exception {
        final int requestToken = lastRequestToken.getAndIncrement();
        NativeByteBuffer buffer = null;

        NativeByteBuffer bufferSizeCalculator = new NativeByteBuffer(true);
        bufferSizeCalculator.rewind();
        method.serialize(bufferSizeCalculator);
        int bufferSize = bufferSizeCalculator.length();
        buffer = new NativeByteBuffer(bufferSize);
        method.serialize(buffer);

        native_sendRequest(currentAccount, buffer.address, (response, errorCode, errorText, networkType) -> {
            synchronized (this) {
                T resp = null;
                RequestError error = null;


                if (response != 0) {
                    NativeByteBuffer buff = NativeByteBuffer.wrap(response);
                    buff.reused = true;
                    try {
                        resp = method.parse(buff);
                    } catch (Exception e) {
                        error = new RequestError(-1, e.getMessage());
                    }
                } else if (errorText != null) {
                    error = new RequestError(errorCode, errorText);
                }

                onComplete.run(resp, error);
            }
        }, onQuickAck, onWriteToSocket, flags, datacenterId, connetionType, immediate, requestToken);

        return requestToken;
    }

    public void cancelRequest(int token, boolean notifyServer) {
        native_cancelRequest(currentAccount, token, notifyServer);
    }

    public void cleanup(boolean resetKeys) {
        native_cleanUp(currentAccount, resetKeys);
    }

    public void cancelRequestsForGuid(int guid) {
        native_cancelRequestsForGuid(currentAccount, guid);
    }

    public void bindRequestToGuid(int requestToken, int guid) {
        native_bindRequestToGuid(currentAccount, requestToken, guid);
    }

    public void applyDatacenterAddress(int datacenterId, String ipAddress, int port) {
        native_applyDatacenterAddress(currentAccount, datacenterId, ipAddress, port);
    }

    public int getConnectionState() {
        if (connectionState == ConnectionStateConnected && isUpdating) {
            return ConnectionStateUpdating;
        }
        return connectionState;
    }

    public void setUserId(int id) {
        native_setUserId(currentAccount, id);
    }

    private void checkConnection(Context context) {
        native_setUseIpv6(currentAccount, useIpv6Address());
        native_setNetworkAvailable(currentAccount, isNetworkOnline(context), getCurrentNetworkType(context), isConnectionSlow(context));
    }

    public void setPushConnectionEnabled(boolean value) {
        native_setPushConnectionEnabled(currentAccount, value);
    }

    public void switchBackend() {
        native_switchBackend(currentAccount);
    }

    public void init(
            int version,
            BackendInfo backendInfo,
            int layer,
            int apiId,
            String deviceModel,
            String systemVersion,
            String appVersion,
            String langCode,
            String systemLangCode,
            String configPath,
            String logPath,
            String deviceId,
            int userId,
            boolean enablePushConnection,
            Context context
    ) {
        SharedPreferences preferences = context.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String proxyAddress = preferences.getString("proxy_ip", "");
        String proxyUsername = preferences.getString("proxy_user", "");
        String proxyPassword = preferences.getString("proxy_pass", "");
        String proxySecret = preferences.getString("proxy_secret", "");
        int proxyPort = preferences.getInt("proxy_port", 1080);
        if (preferences.getBoolean("proxy_enabled", false) && !TextUtils.isEmpty(proxyAddress)) {
            native_setProxySettings(currentAccount, proxyAddress, proxyPort, proxyUsername, proxyPassword, proxySecret);
        }

        native_init(
                currentAccount,
                backendInfo.getIpAddress(),
                backendInfo.getPort(),
                backendInfo.getPublicKey(),
                version,
                layer,
                apiId,
                deviceModel,
                systemVersion,
                appVersion,
                langCode,
                systemLangCode,
                configPath,
                logPath,
                deviceId,
                userId,
                enablePushConnection,
                isNetworkOnline(context),
                getCurrentNetworkType(context)
        );

        checkConnection(context);
        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkConnection(context);
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkStateReceiver, filter);
    }

    public static void setLangCode(String langCode) {
        langCode = langCode.replace('_', '-').toLowerCase();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            native_setLangCode(a, langCode);
        }
    }

    public void updateDcSettings() {
        native_updateDcSettings(currentAccount);
    }

    public long checkProxy(String address, int port, String username, String password, String secret, RequestTimeDelegate requestTimeDelegate) {
        if (TextUtils.isEmpty(address)) {
            return 0;
        }
        if (address == null) {
            address = "";
        }
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        if (secret == null) {
            secret = "";
        }
        return native_checkProxy(currentAccount, address, port, username, password, secret, requestTimeDelegate);
    }

    public void setAppPaused(final boolean isAppPaused) {
        if (this.isAppPaused == isAppPaused) return;
        this.isAppPaused = isAppPaused;

        if (isAppPaused) native_pauseNetwork(currentAccount);
        else native_resumeNetwork(currentAccount, false);
    }

    public static void onUnparsedMessageReceived(long address, final int currentAccount) {
        try {
            NativeByteBuffer buff = NativeByteBuffer.wrap(address);
            buff.reused = true;
            updateHandler.onNewUpdate(buff);
        } catch (Exception e) {
        }
    }

    public static void sendLogToServer(String name, String cryptoText, String text){
        Log.d("!!!!!", name + " " + text);
//        App.getApp().getLogApi().sendLog("123", new LogModel(name, cryptoText, text)).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.d("SendLogServer!!!!!!", String.valueOf(response.code()));
//            }
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) { }
//        });
    }

    public static void onUpdate(final int currentAccount) {

    }

    public static void onSessionCreated(final int currentAccount) {

    }

    public static void onConnectionStateChanged(final int state, final int currentAccount) {

    }

    public static void onLogout(final int currentAccount) {

    }

    public static int getCurrentNetworkType(Context context) {
        if (isConnectedOrConnectingToWiFi(context)) {
            return StatsController.TYPE_WIFI;
        } else if (isRoaming(context)) {
            return StatsController.TYPE_ROAMING;
        } else {
            return StatsController.TYPE_MOBILE;
        }
    }

    public static int getInitFlags() {
        return 0;
    }

    public static void onBytesSent(int amount, int networkType, final int currentAccount) {

    }

    public static void onRequestNewServerIpAndPort(final int second, final int currentAccount) {

    }

    public static void onProxyError() {

    }

    public static String getHostByName(String domain, final int currentAccount) {
        HashMap<String, ResolvedDomain> cache = dnsCache.get();
        ResolvedDomain resolvedDomain = cache.get(domain);
        if (resolvedDomain != null && SystemClock.elapsedRealtime() - resolvedDomain.ttl < 5 * 60 * 1000) {
            return resolvedDomain.address;
        }

        ByteArrayOutputStream outbuf = null;
        InputStream httpConnectionStream = null;
        try {
            URL downloadUrl = new URL("https://www.google.com/resolve?name=" + domain + "&type=A");
            URLConnection httpConnection = downloadUrl.openConnection();
            httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
            httpConnection.addRequestProperty("Host", "dns.google.com");
            httpConnection.setConnectTimeout(1000);
            httpConnection.setReadTimeout(2000);
            httpConnection.connect();
            httpConnectionStream = httpConnection.getInputStream();

            outbuf = new ByteArrayOutputStream();

            byte[] data = new byte[1024 * 32];
            while (true) {
                int read = httpConnectionStream.read(data);
                if (read > 0) {
                    outbuf.write(data, 0, read);
                } else if (read == -1) {
                    break;
                } else {
                    break;
                }
            }

            JSONObject jsonObject = new JSONObject(new String(outbuf.toByteArray()));
            JSONArray array = jsonObject.getJSONArray("Answer");
            int len = array.length();
            if (len > 0) {
                String ip = array.getJSONObject(Utilities.random.nextInt(array.length())).getString("data");
                ResolvedDomain newResolvedDomain = new ResolvedDomain(ip, SystemClock.elapsedRealtime());
                cache.put(domain, newResolvedDomain);
                return ip;
            }
        } catch (Throwable e) {
        } finally {
            try {
                if (httpConnectionStream != null) {
                    httpConnectionStream.close();
                }
            } catch (Throwable e) {
            }
            try {
                if (outbuf != null) {
                    outbuf.close();
                }
            } catch (Exception ignore) {

            }
        }
        return "";
    }

    public static void onBytesReceived(int amount, int networkType, final int currentAccount) {

    }

    public static void onUpdateConfig(long address, final int currentAccount) {

    }

    public static void onInternalPushReceived(final int currentAccount) {
    }

    public static void setProxySettings(boolean enabled, String address, int port, String username, String password, String secret) {
        if (address == null) {
            address = "";
        }
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        if (secret == null) {
            secret = "";
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (enabled && !TextUtils.isEmpty(address)) {
                native_setProxySettings(a, address, port, username, password, secret);
            } else {
                native_setProxySettings(a, "", 1080, "", "", "");
            }

        }
    }

    public static native void native_switchBackend(int currentAccount);
    public static native void native_pauseNetwork(int currentAccount);
    public static native void native_setUseIpv6(int currentAccount, boolean value);
    public static native void native_updateDcSettings(int currentAccount);
    public static native void native_setNetworkAvailable(int currentAccount, boolean value, int networkType, boolean slow);
    public static native void native_resumeNetwork(int currentAccount, boolean partial);
    public static native long native_getCurrentTimeMillis(int currentAccount);
    public static native int native_getCurrentTime(int currentAccount);
    public static native int native_getTimeDifference(int currentAccount);
    public static native void native_sendRequest(int currentAccount, long object, RequestDelegateInternal onComplete, QuickAckDelegate onQuickAck, WriteToSocketDelegate onWriteToSocket, int flags, int datacenterId, int connetionType, boolean immediate, int requestToken);
    public static native void native_cancelRequest(int currentAccount, int token, boolean notifyServer);
    public static native void native_cleanUp(int currentAccount, boolean resetKeys);
    public static native void native_cancelRequestsForGuid(int currentAccount, int guid);
    public static native void native_bindRequestToGuid(int currentAccount, int requestToken, int guid);
    public static native void native_applyDatacenterAddress(int currentAccount, int datacenterId, String ipAddress, int port);
    public static native int native_getConnectionState(int currentAccount);
    public static native void native_setUserId(int currentAccount, int id);
    public static native void native_init(int currentAccount, String backendIP, int backendPort, String backendPublicKey, int version, int layer, int apiId, String deviceModel, String systemVersion, String appVersion, String langCode, String systemLangCode, String configPath, String logPath, String deviceId, int userId, boolean enablePushConnection, boolean hasNetwork, int networkType);
    public static native void native_setProxySettings(int currentAccount, String address, int port, String username, String password, String secret);
    public static native void native_setLangCode(int currentAccount, String langCode);
    public static native void native_setJava(boolean useJavaByteBuffers);
    public static native void native_setPushConnectionEnabled(int currentAccount, boolean value);
    public static native void native_applyDnsConfig(int currentAccount, long address, String phone);
    public static native long native_checkProxy(int currentAccount, String address, int port, String username, String password, String secret, RequestTimeDelegate requestTimeDelegate);

    public static int generateClassGuid() {
        return lastClassGuid++;
    }


    public static boolean isRoaming(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null) {
                return netInfo.isRoaming();
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isConnectedOrConnectingToWiFi(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo.State state = netInfo.getState();
            if (netInfo != null && (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.SUSPENDED)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isConnectedToWiFi(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @SuppressLint("NewApi")
    protected static boolean useIpv6Address() {
        if (BuildVars.LOGS_ENABLED) {
            try {
                NetworkInterface networkInterface;
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = networkInterfaces.nextElement();
                    if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.getInterfaceAddresses().isEmpty()) {
                        continue;
                    }

                    List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                    for (int a = 0; a < interfaceAddresses.size(); a++) {
                        InterfaceAddress address = interfaceAddresses.get(a);
                        InetAddress inetAddress = address.getAddress();

                        if (inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) {
                            continue;
                        }
                    }
                }
            } catch (Throwable e) {
            }
        }
        try {
            NetworkInterface networkInterface;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            boolean hasIpv4 = false;
            boolean hasIpv6 = false;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (int a = 0; a < interfaceAddresses.size(); a++) {
                    InterfaceAddress address = interfaceAddresses.get(a);
                    InetAddress inetAddress = address.getAddress();
                    if (inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) {
                        continue;
                    }
                    if (inetAddress instanceof Inet6Address) {
                        hasIpv6 = true;
                    } else if (inetAddress instanceof Inet4Address) {
                        String addrr = inetAddress.getHostAddress();
                        if (!addrr.startsWith("192.0.0.")) {
                            hasIpv4 = true;
                        }
                    }
                }
            }
            if (!hasIpv4 && hasIpv6) {
                return true;
            }
        } catch (Throwable e) {
        }

        return false;
    }

    
    public static boolean isConnectionSlow(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (netInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return true;
                }
            }
        } catch (Throwable ignore) {

        }
        return false;
    }

    
    public static boolean isNetworkOnline(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

}
