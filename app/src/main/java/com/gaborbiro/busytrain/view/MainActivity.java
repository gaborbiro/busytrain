package com.gaborbiro.busytrain.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gaborbiro.busytrain.R;
import com.gaborbiro.busytrain.data.SeatAvailabilityRegistry;
import com.gaborbiro.busytrain.location.LocationClient;
import com.gaborbiro.busytrain.location.LocationListener;
import com.gaborbiro.busytrain.util.PrefsUtil;
import com.gaborbiro.filepicker.FileDialogActivity;
import com.gaborbiro.filepicker.PermissionVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final long LOCATION_EXPIRY = 5000;
    private static final long PLACE_EXPIRY = 15000;
    private static final int REQUEST_CODE_PERMISSION_FINE_LOCATION = 1;
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXT_STORAGE = 2;

    private static final int REQUEST_CODE_SELECT_FILE_FOR_IMPORT = 3;
    private static final int REQUEST_CODE_SELECT_FILE_FOR_EXPORT = 4;

    private SeatAvailabilityRegistry registry = new SeatAvailabilityRegistry();
    private LocationClient locationClient;

    private PermissionVerifier permissionVerifier;

    private ListView list;

    // UI Setup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registry.registerListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                refreshUI();
            }
        });
        locationClient = LocationClient.getInstance(this);

        FloatingActionButton noSeatBtn = (FloatingActionButton) findViewById(R.id.no_seat_button);
        noSeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerSeatAvailability(false);
            }
        });
        noSeatBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_sad)));
        noSeatBtn.setRippleColor(getResources().getColor(R.color.fab_sad));

        FloatingActionButton yesSeatBtn = (FloatingActionButton) findViewById(R.id.yes_seat_button);
        yesSeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerSeatAvailability(true);
            }
        });
        yesSeatBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_happy)));
        yesSeatBtn.setRippleColor(getResources().getColor(R.color.fab_happy));

        list = (ListView) findViewById(android.R.id.list);
        refreshUI();
    }

    private void refreshUI() {
        list.setAdapter(getAdapter(registry.getAll()));
    }

    private ListAdapter getAdapter(SeatAvailabilityRegistry.SeatAvailability[] availabilities) {
        List<Map<String, Object>> data = new ArrayList<>();
        String KEY_TEXT = "KEY_TEXT";

        for (SeatAvailabilityRegistry.SeatAvailability availability : availabilities) {
            Map<String, Object> item = new HashMap<>();
            item.put(KEY_TEXT, availability.toString());
            data.add(item);
        }

        String[] from = new String[]{KEY_TEXT};
        int[] to = new int[]{android.R.id.text1};

        return new SimpleAdapter(this, data, R.layout.seat_availability_item, from, to) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                view.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final SeatAvailabilityRegistry.SeatAvailability availability = registry.getAll()[position];
                        registry.remove(availability);
                        Snackbar.make(list, "1 item deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        registry.add(availability);
                                    }
                                }).show();

                    }
                });
                return view;
            }
        };
    }

    // End UI Setup

    // Business Logic

    private void registerSeatAvailability(final boolean seatWasAvailable) {
        permissionVerifier = new PermissionVerifier(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});

        if (permissionVerifier.verifyPermissions(true, REQUEST_CODE_PERMISSION_FINE_LOCATION)) {
            final SeatAvailabilityRegistry.SeatAvailability availability = registry.create(seatWasAvailable);
            registry.add(availability);
            Location lastKnownLocation = locationClient.getLastLocation();

            if (lastKnownLocation != null && lastKnownLocation.getTime() > System.currentTimeMillis() - LOCATION_EXPIRY) {
                availability.longitude = lastKnownLocation.getLongitude();
                availability.latitude = lastKnownLocation.getLatitude();
            } else {
                locationClient.requestLocationUpdates(0, new LocationListener() {

                    long startTime = System.currentTimeMillis();
                    SeatAvailabilityRegistry.SeatAvailability pendingAvailability = availability;

                    @Override
                    public void onLocationChanged(Location location) {
                        long endTime = System.currentTimeMillis();

                        if (endTime < startTime + LOCATION_EXPIRY) {
                            locationClient.removeLocationUpdates(this);
                            pendingAvailability.longitude = location.getLongitude();
                            pendingAvailability.latitude = location.getLatitude();
                            registry.update(pendingAvailability);
                            pendingAvailability = null;
                        }
                    }
                });
            }
            locationClient.getNearbyPlaces(new LocationClient.NearbyPlacesListener() {

                long startTime = System.currentTimeMillis();
                SeatAvailabilityRegistry.SeatAvailability pendingAvailability = availability;

                @Override
                public void onNearbyPlaceAvailable(String name) {
                    long endTime = System.currentTimeMillis();

                    if (endTime < startTime + PLACE_EXPIRY) {
                        pendingAvailability.name = name;
                        registry.update(pendingAvailability);
                        pendingAvailability = null;
                    }
                }
            });
        }
    }

    // End Business Logic

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_clear:
                registry.clear();
                break;
            case R.id.action_import:
                import_();
                break;
            case R.id.action_export:
                permissionVerifier = new PermissionVerifier(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                if (permissionVerifier.verifyPermissions(true,
                        REQUEST_CODE_PERMISSION_WRITE_EXT_STORAGE)) {
                    export();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // End Menu

    // Import/Export

    private static final String BACKUP_FOLDER = "/BusyTrain";

    private void import_() {
        Intent i = new Intent(this, FileDialogActivity.class);
        i.putExtra(FileDialogActivity.EXTRA_START_PATH,
                Environment.getExternalStorageDirectory()
                        .getPath() + BACKUP_FOLDER);
        i.putExtra(FileDialogActivity.EXTRA_SELECTION_MODE,
                FileDialogActivity.SELECTION_MODE_OPEN);
        startActivityForResult(i, REQUEST_CODE_SELECT_FILE_FOR_IMPORT);
    }

    private void export() {
        File sd = Environment.getExternalStorageDirectory();
        File targetFolder = new File(sd + BACKUP_FOLDER);

        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        Intent i = new Intent(this, FileDialogActivity.class);
        i.putExtra(FileDialogActivity.EXTRA_START_PATH,
                Environment.getExternalStorageDirectory()
                        .getPath() + BACKUP_FOLDER);
        i.putExtra(FileDialogActivity.EXTRA_SELECTION_MODE,
                FileDialogActivity.SELECTION_MODE_CREATE);
        startActivityForResult(i, REQUEST_CODE_SELECT_FILE_FOR_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_FILE_FOR_IMPORT:
                    if (resultCode == RESULT_OK) {
                        String path =
                                data.getStringExtra(FileDialogActivity.EXTRA_RESULT_PATH);
                        try {
                            if (PrefsUtil.import_(new FileInputStream(path))) {
                                Toast.makeText(this, "Merge success", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Merge failed. Check logs.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (FileNotFoundException e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case REQUEST_CODE_SELECT_FILE_FOR_EXPORT:
                    String path =
                            data.getStringExtra(FileDialogActivity.EXTRA_RESULT_PATH);
                    try {
                        if (PrefsUtil.export(new FileOutputStream(path))) {
                            Toast.makeText(this, "Export success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Export failed. Check logs.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // End Import/Export

    // Permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "You cannot use this app without GPS!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_PERMISSION_WRITE_EXT_STORAGE:
                if (permissionVerifier.onRequestPermissionsResult(requestCode, permissions,
                        grantResults)) {
                    export();
                } else {
                    Toast.makeText(this, "You cannot export the database without this permission!", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    // End Permissions
}
