/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package com.attafitamim.mtproto.client.android.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.attafitamim.mtproto.client.android.tgnet.SerializedData;

import java.io.File;
import java.util.Arrays;


public class UserConfig {

    public static int selectedAccount;
    public final static int MAX_ACCOUNT_COUNT = 3;

    private final Object sync = new Object();
    private boolean configLoaded;
    public boolean registeredForPush;
    public int lastSendMessageId = -210000;
    public int lastBroadcastId = -1;
    public int contactsSavedCount;
    public boolean blockedUsersLoaded;
    public int lastContactsSyncTime;
    public int lastHintsSyncTime;
    public boolean draftsLoaded;
    public boolean pinnedDialogsLoaded = true;
    public boolean unreadDialogsLoaded = true;
    public int ratingLoadTime;
    public int botRatingLoadTime;
    public boolean contactsReimported;
    public int migrateOffsetId = -1;
    public int migrateOffsetDate = -1;
    public int migrateOffsetUserId = -1;
    public int migrateOffsetChatId = -1;
    public int migrateOffsetChannelId = -1;
    public long migrateOffsetAccess = -1;
    public int totalDialogsLoadCount = 0;
    public int dialogsLoadOffsetId = 0;
    public int dialogsLoadOffsetDate = 0;
    public int dialogsLoadOffsetUserId = 0;
    public int dialogsLoadOffsetChatId = 0;
    public int dialogsLoadOffsetChannelId = 0;
    public long dialogsLoadOffsetAccess = 0;
    public boolean notificationsSettingsLoaded;
    public boolean syncContacts = true;
    public boolean suggestContacts = true;
    public boolean hasSecureData;
    public int loginTime;
    public int pendingAppUpdateBuildVersion;
    public long pendingAppUpdateInstallTime;
    public long lastUpdateCheckTime;

    public volatile byte[] savedPasswordHash;
    public volatile byte[] savedSaltedPassword;
    public volatile long savedPasswordTime;

    private int currentAccount;
    private static volatile UserConfig[] Instance = new UserConfig[UserConfig.MAX_ACCOUNT_COUNT];
    public static UserConfig getInstance(int num) {
        UserConfig localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (UserConfig.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new UserConfig(num);
                }
            }
        }
        return localInstance;
    }

    public UserConfig(int instance) {
        currentAccount = instance;
    }

    public void saveConfig(boolean withFile, Context context) {
        saveConfig(withFile, null, context);
    }

    public void saveConfig(boolean withFile, File oldFile, Context context) {
        synchronized (sync) {
            try {
                SharedPreferences preferences;
                if (currentAccount == 0) {
                    preferences = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
                } else {
                    preferences = context.getSharedPreferences("userconfig" + currentAccount, Context.MODE_PRIVATE);
                }
                SharedPreferences.Editor editor = preferences.edit();
                if (currentAccount == 0) {
                    editor.putInt("selectedAccount", selectedAccount);
                }
                editor.putBoolean("registeredForPush", registeredForPush);
                editor.putInt("lastSendMessageId", lastSendMessageId);
                editor.putInt("contactsSavedCount", contactsSavedCount);
                editor.putInt("lastBroadcastId", lastBroadcastId);
                editor.putBoolean("blockedUsersLoaded", blockedUsersLoaded);
                editor.putInt("lastContactsSyncTime", lastContactsSyncTime);
                editor.putInt("lastHintsSyncTime", lastHintsSyncTime);
                editor.putBoolean("draftsLoaded", draftsLoaded);
                editor.putBoolean("pinnedDialogsLoaded", pinnedDialogsLoaded);
                editor.putBoolean("unreadDialogsLoaded", unreadDialogsLoaded);
                editor.putInt("ratingLoadTime", ratingLoadTime);
                editor.putInt("botRatingLoadTime", botRatingLoadTime);
                editor.putBoolean("contactsReimported", contactsReimported);
                editor.putInt("loginTime", loginTime);
                editor.putBoolean("syncContacts", syncContacts);
                editor.putBoolean("suggestContacts", suggestContacts);
                editor.putBoolean("hasSecureData", hasSecureData);
                editor.putBoolean("notificationsSettingsLoaded", notificationsSettingsLoaded);

                editor.putInt("3migrateOffsetId", migrateOffsetId);
                if (migrateOffsetId != -1) {
                    editor.putInt("3migrateOffsetDate", migrateOffsetDate);
                    editor.putInt("3migrateOffsetUserId", migrateOffsetUserId);
                    editor.putInt("3migrateOffsetChatId", migrateOffsetChatId);
                    editor.putInt("3migrateOffsetChannelId", migrateOffsetChannelId);
                    editor.putLong("3migrateOffsetAccess", migrateOffsetAccess);
                }

                editor.putInt("2totalDialogsLoadCount", totalDialogsLoadCount);
                editor.putInt("2dialogsLoadOffsetId", dialogsLoadOffsetId);
                editor.putInt("2dialogsLoadOffsetDate", dialogsLoadOffsetDate);
                editor.putInt("2dialogsLoadOffsetUserId", dialogsLoadOffsetUserId);
                editor.putInt("2dialogsLoadOffsetChatId", dialogsLoadOffsetChatId);
                editor.putInt("2dialogsLoadOffsetChannelId", dialogsLoadOffsetChannelId);
                editor.putLong("2dialogsLoadOffsetAccess", dialogsLoadOffsetAccess);

                SharedConfig.saveConfig(context);


                editor.apply();
                if (oldFile != null) {
                    oldFile.delete();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void loadConfig(Context context) {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences;
            if (currentAccount == 0) {
                preferences = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
                selectedAccount = preferences.getInt("selectedAccount", 0);
            } else {
                preferences = context.getSharedPreferences("userconfig" + currentAccount, Context.MODE_PRIVATE);
            }
            registeredForPush = preferences.getBoolean("registeredForPush", false);
            lastSendMessageId = preferences.getInt("lastSendMessageId", -210000);
            contactsSavedCount = preferences.getInt("contactsSavedCount", 0);
            lastBroadcastId = preferences.getInt("lastBroadcastId", -1);
            blockedUsersLoaded = preferences.getBoolean("blockedUsersLoaded", false);
            lastContactsSyncTime = preferences.getInt("lastContactsSyncTime", 0);
            lastHintsSyncTime = preferences.getInt("lastHintsSyncTime", 0);
            draftsLoaded = preferences.getBoolean("draftsLoaded", false);
            pinnedDialogsLoaded = preferences.getBoolean("pinnedDialogsLoaded", false);
            unreadDialogsLoaded = preferences.getBoolean("unreadDialogsLoaded", false);
            contactsReimported = preferences.getBoolean("contactsReimported", false);
            ratingLoadTime = preferences.getInt("ratingLoadTime", 0);
            botRatingLoadTime = preferences.getInt("botRatingLoadTime", 0);
            loginTime = preferences.getInt("loginTime", currentAccount);
            syncContacts = preferences.getBoolean("syncContacts", true);
            suggestContacts = preferences.getBoolean("suggestContacts", true);
            hasSecureData = preferences.getBoolean("hasSecureData", false);
            notificationsSettingsLoaded = preferences.getBoolean("notificationsSettingsLoaded", false);

            migrateOffsetId = preferences.getInt("3migrateOffsetId", 0);
            if (migrateOffsetId != -1) {
                migrateOffsetDate = preferences.getInt("3migrateOffsetDate", 0);
                migrateOffsetUserId = preferences.getInt("3migrateOffsetUserId", 0);
                migrateOffsetChatId = preferences.getInt("3migrateOffsetChatId", 0);
                migrateOffsetChannelId = preferences.getInt("3migrateOffsetChannelId", 0);
                migrateOffsetAccess = preferences.getLong("3migrateOffsetAccess", 0);
            }

            dialogsLoadOffsetId = preferences.getInt("2dialogsLoadOffsetId", -1);
            totalDialogsLoadCount = preferences.getInt("2totalDialogsLoadCount", 0);
            dialogsLoadOffsetDate = preferences.getInt("2dialogsLoadOffsetDate", -1);
            dialogsLoadOffsetUserId = preferences.getInt("2dialogsLoadOffsetUserId", -1);
            dialogsLoadOffsetChatId = preferences.getInt("2dialogsLoadOffsetChatId", -1);
            dialogsLoadOffsetChannelId = preferences.getInt("2dialogsLoadOffsetChannelId", -1);
            dialogsLoadOffsetAccess = preferences.getLong("2dialogsLoadOffsetAccess", -1);

            String string = preferences.getString("tmpPassword", null);
            if (string != null) {
                byte[] bytes = Base64.decode(string, Base64.DEFAULT);
                if (bytes != null) {
                    SerializedData data = new SerializedData(bytes);
                    data.cleanup();
                }
            }

            configLoaded = true;
        }
    }

    public void resetSavedPassword() {
        savedPasswordTime = 0;
        if (savedPasswordHash != null) {
            Arrays.fill(savedPasswordHash, (byte) 0);
            savedPasswordHash = null;
        }
        if (savedSaltedPassword != null) {
            Arrays.fill(savedSaltedPassword, (byte) 0);
            savedSaltedPassword = null;
        }
    }

    public void clearConfig(Context context) {
        registeredForPush = false;
        contactsSavedCount = 0;
        lastSendMessageId = -210000;
        lastBroadcastId = -1;
        blockedUsersLoaded = false;
        notificationsSettingsLoaded = false;
        migrateOffsetId = -1;
        migrateOffsetDate = -1;
        migrateOffsetUserId = -1;
        migrateOffsetChatId = -1;
        migrateOffsetChannelId = -1;
        migrateOffsetAccess = -1;
        dialogsLoadOffsetId = 0;
        totalDialogsLoadCount = 0;
        dialogsLoadOffsetDate = 0;
        dialogsLoadOffsetUserId = 0;
        dialogsLoadOffsetChatId = 0;
        dialogsLoadOffsetChannelId = 0;
        dialogsLoadOffsetAccess = 0;
        ratingLoadTime = 0;
        botRatingLoadTime = 0;
        draftsLoaded = true;
        contactsReimported = true;
        syncContacts = true;
        suggestContacts = true;
        pinnedDialogsLoaded = false;
        unreadDialogsLoaded = true;
        hasSecureData = false;
        loginTime = 0;
        lastContactsSyncTime = 0;
        lastHintsSyncTime = 0;
        resetSavedPassword();
        saveConfig(true, context);
    }
}
