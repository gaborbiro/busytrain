package com.gaborbiro.busytrain;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class App extends Application {

    private static Context appContext;

    public App() {
        this.appContext = this;
    }

    public static Context getAppContext() {
        return appContext;
    }

    /**
     * Generate a unique id for the device. Changes with every factory reset. If the
     * device doesn't have a proper
     * android_id and deviceId, it falls back to a randomly generated id, that is
     * persisted in SharedPreferences.
     */
    public static String generateUDID() {
        String deviceId = null;
        String androidId;
        UUID deviceUuid = null;

        // androidId changes with every factory reset (which is useful in our case)
        androidId = "" + android.provider.Settings.Secure.getString(
                getAppContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                deviceUuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                // On some 2.2 devices androidId is always 9774d56d682e549c,
                // which is unsafe
                TelephonyManager tm = (TelephonyManager) getAppContext().getSystemService(
                        Context.TELEPHONY_SERVICE);

                if (tm != null) {
                    // Tablets may not have imei and/or imsi.
                    // Does not change on factory reset.
                    deviceId = tm.getDeviceId();
                }

                if (TextUtils.isEmpty(deviceId)) {
                    // worst case scenario as this id is lost when the
                    // application stops
                    deviceUuid = UUID.randomUUID();
                } else {
                    deviceUuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            // Change it back to "utf8" right now!!
        }
        return deviceUuid.toString();
    }

}
