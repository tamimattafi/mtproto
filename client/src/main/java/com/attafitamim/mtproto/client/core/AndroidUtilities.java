/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package com.attafitamim.mtproto.client.core;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class AndroidUtilities {

    public static float density = 1;
    public static Point displaySize = new Point();
    public static int roundMessageSize;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int leftBaseline;
    public static boolean usingHardwareInput;


    private static Boolean isTablet = null;

    public static Pattern WEB_URL = null;

    static {
        try {
            final String GOOD_IRI_CHAR = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
            final Pattern IP_ADDRESS = Pattern.compile(
                    "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                            + "|[1-9][0-9]|[0-9]))");
            final String IRI = "[" + GOOD_IRI_CHAR + "]([" + GOOD_IRI_CHAR + "\\-]{0,61}[" + GOOD_IRI_CHAR + "]){0,1}";
            final String GOOD_GTLD_CHAR = "a-zA-Z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
            final String GTLD = "[" + GOOD_GTLD_CHAR + "]{2,63}";
            final String HOST_NAME = "(" + IRI + "\\.)+" + GTLD;
            final Pattern DOMAIN_NAME = Pattern.compile("(" + HOST_NAME + "|" + IP_ADDRESS + ")");
            WEB_URL = Pattern.compile(
                    "((?:(http|https|Http|Https):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                            + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                            + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                            + "(?:" + DOMAIN_NAME + ")"
                            + "(?:\\:\\d{1,5})?)" // plus option port number
                            + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
                            + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                            + "(?:\\b|$)");
        } catch (Exception ignored) {
        }
    }

    public static void init(Context context) {
        leftBaseline = 72;
        checkDisplaySize(context, null);
    }

    public static byte[] getStringBytes(String src) {
        try {
            return src.getBytes("UTF-8");
        } catch (Exception ignore) {

        }
        return new byte[0];
    }


    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static int compare(int lhs, int rhs) {
        if (lhs == rhs) {
            return 0;
        } else if (lhs > rhs) {
            return 1;
        }
        return -1;
    }

    public static void checkDisplaySize(Context context, Configuration newConfiguration) {
        try {
            density = context.getResources().getDisplayMetrics().density;
            Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
            if (roundMessageSize == 0) {
                roundMessageSize = (int) (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.6f);
            }
        } catch (Exception ignored) {
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).removeCallbacks(runnable);
    }

    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 700;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    private static ContentObserver callLogContentObserver;
    private static Runnable unregisterRunnable;
    private static boolean hasCallPermissions = Build.VERSION.SDK_INT >= 23;

    private static void registerLoginContentObserver(boolean shouldRegister, final String number, Context context) {
        if (shouldRegister) {
            if (callLogContentObserver != null) {
                return;
            }
            context.getContentResolver().registerContentObserver(
                    CallLog.Calls.CONTENT_URI,
                    true,
                    callLogContentObserver = new ContentObserver(new Handler()) {
                        @Override
                        public boolean deliverSelfNotifications() {
                            return true;
                        }

                        @Override
                        public void onChange(boolean selfChange) {
                            registerLoginContentObserver(false, number, context);
                            removeLoginPhoneCall(number, false, context);
                        }
                    });
            runOnUIThread(unregisterRunnable = () -> {
                unregisterRunnable = null;
                registerLoginContentObserver(false, number, context);
            }, 10000);
        } else {
            if (callLogContentObserver == null) {
                return;
            }
            if (unregisterRunnable != null) {
                cancelRunOnUIThread(unregisterRunnable);
                unregisterRunnable = null;
            }
            try {
                context.getContentResolver().unregisterContentObserver(callLogContentObserver);
            } catch (Exception ignore) {

            } finally {
                callLogContentObserver = null;
            }
        }
    }

    
    public static void removeLoginPhoneCall(String number, boolean first, Context context) {
        if (!hasCallPermissions) {
            return;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls._ID, CallLog.Calls.NUMBER},
                    CallLog.Calls.TYPE + " IN (" + CallLog.Calls.MISSED_TYPE + "," + CallLog.Calls.INCOMING_TYPE + "," + CallLog.Calls.REJECTED_TYPE + ")",
                    null,
                    "date DESC LIMIT 5");
            boolean removed = false;
            while (cursor.moveToNext()) {
                String phone = cursor.getString(1);
                if (phone.contains(number) || number.contains(phone)) {
                    removed = true;
                    context.getContentResolver().delete(
                            CallLog.Calls.CONTENT_URI,
                            CallLog.Calls._ID + " = ? ",
                            new String[]{String.valueOf(cursor.getInt(0))});
                    break;
                }
            }
            if (!removed && first) {
                registerLoginContentObserver(true, number, context);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static final int FLAG_TAG_BR = 1;
    public static final int FLAG_TAG_BOLD = 2;
    public static final int FLAG_TAG_COLOR = 4;
    public static final int FLAG_TAG_URL = 8;
    public static final int FLAG_TAG_ALL = FLAG_TAG_BR | FLAG_TAG_BOLD | FLAG_TAG_URL;

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream source = null;
        FileOutputStream destination = null;
        try {
            source = new FileInputStream(sourceFile);
            destination = new FileOutputStream(destFile);
            destination.getChannel().transferFrom(source.getChannel(), 0, source.getChannel().size());
        } catch (Exception e) {
            return false;
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return true;
    }

}
