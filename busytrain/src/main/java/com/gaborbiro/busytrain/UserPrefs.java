package com.gaborbiro.busytrain;

import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.gaborbiro.busytrain.data.SeatAvailability;
import com.gaborbiro.busytrain.util.PrefsUtil;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class UserPrefs {

    private static final String KEY_SEAT_AVAILABILITIES = "com.gaborbiro.busytrain.data.KEY_SEAT_AVAILABILITIES";

    @NonNull
    public static SeatAvailability[] getSeatAvailabilities() {
        Parcelable[] parcelables = PrefsUtil.get(KEY_SEAT_AVAILABILITIES, SeatAvailability.class.getClassLoader());

        if (parcelables == null) {
            return new SeatAvailability[0];
        } else {
            SeatAvailability[] result = new SeatAvailability[parcelables.length];

            for (int i = 0; i < parcelables.length; i++) {
                result[i] = (SeatAvailability) parcelables[i];
            }
            return result;
        }
    }

    public static void setSeatAvailabilities(SeatAvailability[] seatAvailabilities) {
        if (seatAvailabilities != null) {
            PrefsUtil.put(KEY_SEAT_AVAILABILITIES, seatAvailabilities);
        } else {
            PrefsUtil.remove(KEY_SEAT_AVAILABILITIES);
        }
    }

    public static boolean exportSeatAvailabilities(OutputStream out) {
        Gson gson = new Gson();
        SeatAvailability[] seatAvailabilities = getSeatAvailabilities();

        try {
            for (SeatAvailability seatAvailability : seatAvailabilities) {
                out.write(gson.toJson(seatAvailability).getBytes());
                out.write("\n".getBytes());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean importSeatAvailabilities(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Gson gson = new Gson();
        String line;
        try {
            Set<SeatAvailability> seatAvailabilities = new TreeSet<>(new Comparator<SeatAvailability>() {
                @Override
                public int compare(SeatAvailability lhs, SeatAvailability rhs) {
                    return Double.valueOf(rhs.timestamp).compareTo(Double.valueOf(lhs.timestamp));
                }
            });
            seatAvailabilities.addAll(Arrays.asList(getSeatAvailabilities()));
            while ((line = reader.readLine()) != null) {
                seatAvailabilities.add(gson.fromJson(line, SeatAvailability.class));
            }
            setSeatAvailabilities(seatAvailabilities.toArray(new SeatAvailability[seatAvailabilities.size()]));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void registerSeatAvailabilitiesChangedListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PrefsUtil.registerOnSharedPreferenceChangeListener(KEY_SEAT_AVAILABILITIES, listener);
    }
}
