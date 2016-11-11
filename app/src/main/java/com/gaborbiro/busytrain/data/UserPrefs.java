package com.gaborbiro.busytrain.data;

import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.gaborbiro.busytrain.util.PrefsUtil;

public class UserPrefs {

    private static final String KEY_SEAT_AVAILABILITIES = "com.gaborbiro.busytrain.data.KEY_SEAT_AVAILABILITIES";

    @NonNull
    public static SeatAvailabilityRegistry.SeatAvailability[] getSeatAvailabilities() {
        Parcelable[] parcelables = PrefsUtil.get(KEY_SEAT_AVAILABILITIES, SeatAvailabilityRegistry.SeatAvailability.class.getClassLoader());

        if (parcelables == null) {
            return new SeatAvailabilityRegistry.SeatAvailability[0];
        } else {
            SeatAvailabilityRegistry.SeatAvailability[] result = new SeatAvailabilityRegistry.SeatAvailability[parcelables.length];

            for (int i = 0; i < parcelables.length; i++) {
                result[i] = (SeatAvailabilityRegistry.SeatAvailability) parcelables[i];
            }
            return result;
        }
    }

    public static void setSeatAvailabilities(SeatAvailabilityRegistry.SeatAvailability[] seatAvailabilities) {
        if (seatAvailabilities != null) {
            PrefsUtil.put(KEY_SEAT_AVAILABILITIES, seatAvailabilities);
        } else {
            PrefsUtil.remove(KEY_SEAT_AVAILABILITIES);
        }
    }

    public static void registerSeatAvailabilitiesChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PrefsUtil.registerOnSharedPreferenceChangeListener(KEY_SEAT_AVAILABILITIES, listener);
    }
}
