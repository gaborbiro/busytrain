package com.gaborbiro.busytrain.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gaborbiro.busytrain.util.ArrayUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultTransform;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class LocationClient {

    private static LocationClient instance;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Collection<LocationListener> listenerList;

    public interface NearbyPlacesListener {
        void onNearbyPlaceAvailable(String name);
    }

    private LocationClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(locationConnectionListener)
                .addOnConnectionFailedListener(locationConnectionFailedListener).build();
        listenerList = new LinkedBlockingQueue<>();
    }

    public static final LocationClient getInstance(Context context) {
        if (instance == null) {
            instance = new LocationClient(context);
        }
        return instance;
    }

    public Location getLastLocation() {
        if (googleApiClient.isConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        return null;
    }

    public void requestLocationUpdates(long refreshTimeMillis, LocationListener locationListener) {
        if (locationListener == null) {
            return;
        }

        if (!listenerList.contains(locationListener)) {
            listenerList.add(locationListener);
        }

        if (listenerList.size() == 1) {
            startLocationUpdates(refreshTimeMillis);
        }
    }

    public void removeLocationUpdates(LocationListener locationListener) {
        if (locationListener == null) {
            return;
        }
        listenerList.remove(locationListener);

        if (listenerList.size() == 0) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    public void getNearbyPlaces(final NearbyPlacesListener listener) {
        ArrayList<Integer> restrictToTransitStations = new ArrayList<>();
        restrictToTransitStations.add(Place.TYPE_TRANSIT_STATION);
        PlaceFilter transitStations = new PlaceFilter(restrictToTransitStations, false, null, null);
        Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, transitStations)
                .then(new ResultTransform<PlaceLikelihoodBuffer, Result>() {
                    @Nullable
                    @Override
                    public PendingResult<Result> onSuccess(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                        if (placeLikelihoods.getCount() == 0) {
                            System.out.println("No nearby transit station was found");
                        } else {
                            if (listener != null) {
                                Place mostLikelyStation = placeLikelihoods.get(0).getPlace();
                                String name = mostLikelyStation.getName().toString();
                                List<String> types = new ArrayList<>();

                                if (mostLikelyStation.getPlaceTypes().contains(Place.TYPE_BUS_STATION)) {
                                    types.add("bus");
                                }
                                if (mostLikelyStation.getPlaceTypes().contains(Place.TYPE_SUBWAY_STATION)) {
                                    types.add("subway");
                                }
                                if (mostLikelyStation.getPlaceTypes().contains(Place.TYPE_TRAIN_STATION)) {
                                    types.add("train");
                                }
                                if (!types.isEmpty()) {
                                    name += " " + Arrays.toString(types.toArray(new String[types.size()]));
                                }
                                listener.onNearbyPlaceAvailable(name);
                            }
                        }
                        placeLikelihoods.release();
                        return null;
                    }
                });
    }

    private void startLocationUpdates(long refreshTimeMillis) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(refreshTimeMillis);
        locationRequest.setFastestInterval(refreshTimeMillis);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        } else {
            googleApiClient.connect();
        }
    }

    private void startLocationUpdates() {
        if (locationRequest != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, gmsLocationListener);
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, gmsLocationListener);
        }
        locationRequest = null;
    }

    protected void onGooglePlayConnectionFailed(ConnectionResult result) {
        // This must be handled by the application as it has to start an activity and attempt to fix the problem
    }

    private GoogleApiClient.ConnectionCallbacks locationConnectionListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            // start listening to locations. If settings are wrong the app will do the checks.
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            // Disconnected on purpose
            if (listenerList.size() == 0) {
                return;
            }

            // This will either work or get a connection failure error that will be handled later
            try {
                googleApiClient.connect();
            } catch (Exception e) {
                // Don't crash due to Google issues. Sometimes connect fails with "DeadObjectException" so just catch it.
            }
        }
    };

    private GoogleApiClient.OnConnectionFailedListener locationConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            onGooglePlayConnectionFailed(connectionResult);
        }
    };

    private com.google.android.gms.location.LocationListener gmsLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            for (LocationListener listener : listenerList) {
                listener.onLocationChanged(location);
            }
        }
    };
}
