package com.gaborbiro.busytrain.data;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.gaborbiro.busytrain.UserPrefs;
import com.gaborbiro.busytrain.util.ArrayUtils;

import java.util.Comparator;

public class SeatAvailabilityRegistry {

    public SeatAvailability create(boolean available) {
        return new SeatAvailability(System.currentTimeMillis(), available);
    }

    public synchronized
    @NonNull
    SeatAvailability[] getAll() {
        return UserPrefs.getSeatAvailabilities();
    }

    public synchronized void remove(SeatAvailability availability) {
        UserPrefs.setSeatAvailabilities(ArrayUtils.remove(getAll(), availability, SeatAvailability.class));
    }

    public synchronized void add(SeatAvailability availability) {
        UserPrefs.setSeatAvailabilities(ArrayUtils.insert(getAll(), availability, SeatAvailability.class, new Comparator<SeatAvailability>() {
            @Override
            public int compare(SeatAvailability o1, SeatAvailability o2) {
                if (o1 == null) {
                    if (o2 == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (o2 == null) {
                        return -1;
                    } else {
                        return (int) (o1.timestamp - o2.timestamp);
                    }
                }
            }
        }));
    }

    public synchronized boolean update(SeatAvailability availability) {
        SeatAvailability[] availabilities = getAll();

        int index = -1;

        for (int i = 0; i < availabilities.length && index < 0; i++) {
            if (availabilities[i].ID == availability.ID) {
                index = i;
            }
        }

        if (index >= 0) {
            availabilities[index] = availability;
            UserPrefs.setSeatAvailabilities(availabilities);
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        UserPrefs.setSeatAvailabilities(null);
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        UserPrefs.registerSeatAvailabilitiesChangedListener(listener);
    }
}
