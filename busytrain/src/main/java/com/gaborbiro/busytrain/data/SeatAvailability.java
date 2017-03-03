package com.gaborbiro.busytrain.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ParcelablePlease
public class SeatAvailability implements Parcelable {

    private static SimpleDateFormat FORMAT_DATE_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static DecimalFormat FORMAT_2_DECIMALS = new DecimalFormat("#.0000");

    private static int ID_COUNTER = 0;

    public int ID;
    public long timestamp;
    public boolean available;
    public double longitude;
    public double latitude;
    public String name;

    private SeatAvailability() {
    }

    public SeatAvailability(long timestamp, boolean available) {
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
        SeatAvailabilityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SeatAvailability> CREATOR = new Creator<SeatAvailability>() {
        public SeatAvailability createFromParcel(Parcel source) {
            SeatAvailability target = new SeatAvailability();
            SeatAvailabilityParcelablePlease.readFromParcel(target, source);
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