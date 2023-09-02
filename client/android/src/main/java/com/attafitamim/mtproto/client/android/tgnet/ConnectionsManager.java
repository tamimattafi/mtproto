package com.attafitamim.mtproto.client.android.tgnet;

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

import com.attafitamim.mtproto.client.android.core.BuildVars;
import com.attafitamim.mtproto.client.android.core.StatsController;
import com.attafitamim.mtproto.client.android.core.Utilities;
import com.attafitamim.mtproto.client.api.bodies.RequestError;
import com.attafitamim.mtproto.client.api.connection.ConnectionFlags;
import com.attafitamim.mtproto.client.api.connection.IConnectionManager;
import com.attafitamim.mtproto.client.api.connection.IQuickAckDelegate;
import com.attafitamim.mtproto.client.api.connection.IRequestDelegate;
import com.attafitamim.mtproto.client.api.connection.IWriteToSocketDelegate;
import com.attafitamim.mtproto.client.api.connection.RequestTimeDelegate;
import com.attafitamim.mtproto.client.api.events.IEventListener;
import com.attafitamim.mtproto.client.api.handlers.IUpdateHandler;
import com.attafitamim.mtproto.core.types.TLMethod;
import com.attafitamim.mtproto.client.android.core.UserConfig;
import com.attafitamim.mtproto.core.types.TLObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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


public class ConnectionsManager implements IConnectionManager {

    private static long lastDnsRequestTime;
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

    @NotNull
    public static IUpdateHandler updateHandler;

    @Nullable
    private static IEventListener eventListener;

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
            boolean enablePushConnection,
            @NotNull IUpdateHandler updateHandler,
            @Nullable IEventListener eventListener
    ) {
        this.eventListener = eventListener;
        this.updateHandler = updateHandler;

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

    @Override
    public <T extends TLObject> int sendRequest(@NotNull TLMethod<T> method, @NotNull IRequestDelegate<T> onComplete, int flags, int connectionType) throws Exception {
        return sendRequest(method, onComplete, null, null, flags, ConnectionFlags.DEFAULT_DATACENTER_ID, connectionType, false);
    }

    @Override
    public <T extends TLObject> int sendRequest(@NotNull TLMethod<T> method, @NotNull IRequestDelegate<T> onComplete, @Nullable IQuickAckDelegate onQuickAck, int flags) throws Exception {
        return sendRequest(method, onComplete, onQuickAck, null, flags, ConnectionFlags.DEFAULT_DATACENTER_ID, ConnectionFlags.ConnectionTypeGeneric, false);
    }

    @Override
    public <T extends TLObject> int sendRequest(@NotNull TLMethod<T> method, @NotNull IRequestDelegate<T> onComplete, @Nullable IQuickAckDelegate onQuickAck, @Nullable IWriteToSocketDelegate onWriteToSocket, int flags, int datacenterId, int connectionType, boolean immediate) throws Exception {
        final int requestToken = lastRequestToken.getAndIncrement();

        logRequest(requestToken, method);

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
                        logResponse(requestToken, method, resp);
                    } catch (Exception e) {
                        error = new RequestError(-1, e.getMessage());
                        logError(requestToken, method, error);
                    }
                } else if (errorText != null) {
                    error = new RequestError(errorCode, errorText);
                    logError(requestToken, method, error);
                }

                onComplete.run(resp, error);
            }
        }, onQuickAck, onWriteToSocket, flags, datacenterId, connectionType, immediate, requestToken);

        return requestToken;
    }

    @Override
    public void onAuthSuccess(int userId) {
        this.setUserId(userId);
        this.updateDcSettings();
    }

    @Override
    public void cancelRequest(int token, boolean notifyServer) {
        native_cancelRequest(currentAccount, token, notifyServer);
    }

    @Override
    public void cleanup(boolean resetKeys) {
        native_cleanUp(currentAccount, resetKeys);
    }

    @Override
    public void cancelRequestsForGuid(int guid) {
        native_cancelRequestsForGuid(currentAccount, guid);
    }

    @Override
    public void bindRequestToGuid(int requestToken, int guid) {
        native_bindRequestToGuid(currentAccount, requestToken, guid);
    }

    @Override
    public void applyDatacenterAddress(int datacenterId, @Nullable String ipAddress, int port) {
        native_applyDatacenterAddress(currentAccount, datacenterId, ipAddress, port);
    }

    @Override
    public int getConnectionState() {
        if (connectionState == ConnectionFlags.ConnectionStateConnected && isUpdating) {
            return ConnectionFlags.ConnectionStateUpdating;
        }
        return connectionState;
    }

    @Override
    public void setUserId(int id) {
        native_setUserId(currentAccount, id);
    }

    @Override
    public void setPushConnectionEnabled(boolean value) {
        native_setPushConnectionEnabled(currentAccount, value);
    }

    @Override
    public void switchBackend() {
        native_switchBackend(currentAccount);
    }

    @Override
    public long getCurrentTimeMillis() {
        return native_getCurrentTimeMillis(currentAccount);
    }

    @Override
    public int getCurrentTime() {
        return native_getCurrentTime(currentAccount);
    }

    @Override
    public int getTimeDifference() {
        return native_getTimeDifference(currentAccount);
    }

    @Override
    public void updateDcSettings() {
        native_updateDcSettings(currentAccount);
    }

    @Override
    public long checkProxy(@Nullable String address, int port, @Nullable String username, @Nullable String password, @Nullable String secret, @Nullable RequestTimeDelegate requestTimeDelegate) {
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

    @Override
    public void setAppPaused(boolean appPaused) {
        if (this.isAppPaused == appPaused) return;
        this.isAppPaused = appPaused;

        if (appPaused) native_pauseNetwork(currentAccount);
        else native_resumeNetwork(currentAccount, false);
    }

    private static void logRequest(int requestToken, TLMethod<?> request) {
        if (eventListener != null) eventListener.onRequest(requestToken, request);
    }

    private void checkConnection(Context context) {
        native_setUseIpv6(currentAccount, useIpv6Address());
        native_setNetworkAvailable(currentAccount, isNetworkOnline(context), getCurrentNetworkType(context), isConnectionSlow(context));
    }

    private static void logResponse(int requestToken, TLMethod<?> request, TLObject response) {
        if (eventListener != null) eventListener.onResponse(requestToken, 0, request, response);
    }

    private static void logError(int requestToken, TLMethod<?> request, RequestError error) {
        if (eventListener != null) eventListener.onError(requestToken, 0, request, error);
    }

    private static void logUpdate(TLObject update) {
        if (eventListener != null) eventListener.onUpdate(update);
    }

    public static void setLangCode(String langCode) {
        langCode = langCode.replace('_', '-').toLowerCase();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            native_setLangCode(a, langCode);
        }
    }

    public static void onUnparsedMessageReceived(long address, final int currentAccount) {
        try {
            NativeByteBuffer buff = NativeByteBuffer.wrap(address);
            buff.reused = true;
            TLObject update = updateHandler.parseUpdate(buff);
            logUpdate(update);
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
        if (eventListener != null) eventListener.onSessionCreated();
    }

    public static void onConnectionStateChanged(final int state, final int currentAccount) {
        if (eventListener != null) eventListener.onConnectionStateChanged(state);
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
    public static native void native_sendRequest(int currentAccount, long object, RequestDelegateInternal onComplete, IQuickAckDelegate onQuickAck, IWriteToSocketDelegate onWriteToSocket, int flags, int datacenterId, int connetionType, boolean immediate, int requestToken);
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
