package com.gaborbiro.busytrain.data;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gaborbiro.busytrain.UserPrefs;
import com.gaborbiro.busytrain.util.ArrayUtils;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class SeatAvailabilityRegistry {

    private static SimpleDateFormat FORMAT_DATE_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static DecimalFormat FORMAT_2_DECIMALS = new DecimalFormat("#.0000");

    @ParcelablePlease
    public static class SeatAvailability implements Parcelable {

        private static int ID_COUNTER = 0;

        public int ID;
        public long timestamp;
        public boolean available;
        public double longitude;
        public double latitude;
        public String name;

        private SeatAvailability() {
        }

        private SeatAvailability(long timestamp, boolean available) {
            this.ID = ID_COUNTER++;
            this.timestamp = timestamp;
            this.available = available;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            SeatAvailabilityRegistry$SeatAvailabilityParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<SeatAvailability> CREATOR = new Creator<SeatAvailability>() {
            public SeatAvailability createFromParcel(Parcel source) {
                SeatAvailability target = new SeatAvailability();
                SeatAvailabilityRegistry$SeatAvailabilityParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public SeatAvailability[] newArray(int size) {
                return new SeatAvailability[size];
            }
        };

        @Override
        public String toString() {
            return "Seat was " + (available ? "available" : "NOT available") + "\n" + FORMAT_DATE_TIME.format(new Date(timestamp)) +
                    (TextUtils.isEmpty(name) && longitude != 0 ? "\n" + FORMAT_2_DECIMALS.format(longitude) + "/" + FORMAT_2_DECIMALS.format(latitude) : "") +
                    (!TextUtils.isEmpty(name) ? "\n" + name : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SeatAvailability that = (SeatAvailability) o;

            if (timestamp != that.timestamp) return false;
            if (available != that.available) return false;
            if (Double.compare(that.longitude, longitude) != 0) return false;
            return Double.compare(that.latitude, latitude) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (int) (timestamp ^ (timestamp >>> 32));
            result = 31 * result + (available ? 1 : 0);
            temp = Double.doubleToLongBits(longitude);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(latitude);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

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
