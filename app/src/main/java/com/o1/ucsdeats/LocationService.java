package com.o1.ucsdeats;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Peter on 4/26/2018.
 */

public class LocationService implements LocationListener {

    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;//1000 * 60 * 1; // 1 minute

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;
    public boolean isGPSEnabled;
    public boolean isNetworkEnabled;
    public boolean locationServiceAvailable;
    public boolean isPassiveEnabled;

    /**
     * Singleton implementation
     *
     * @return
     */
    public static LocationService getLocationManager(Context context) {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    public LocationService(Context context) {
        initLocationService(context);
    }


    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationService(Context context) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            this.isPassiveEnabled = locationManager.isProviderEnabled(locationManager.PASSIVE_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }
            else
            {
                this.locationServiceAvailable = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }else{
                            locationManager.requestLocationUpdates(locationManager.PASSIVE_PROVIDER,
                                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
                        }
                        Toast.makeText(context, "NET: "+longitude+"||"+latitude, Toast.LENGTH_SHORT).show();
                    }
                }//end if

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }else{
                            locationManager.requestLocationUpdates(locationManager.PASSIVE_PROVIDER,
                                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
                        }
                        Toast.makeText(context, "GPS: "+longitude+"||"+latitude, Toast.LENGTH_SHORT).show();
                    }
                }

                if(isPassiveEnabled){
                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if(location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }else{
                            locationManager.requestLocationUpdates(locationManager.PASSIVE_PROVIDER,
                                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
                        }
                        Toast.makeText(context, "GPS: "+longitude+"||"+latitude, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        } catch (Exception ex) {
            System.out.println("There is an exception");
            ex.printStackTrace();
        }
    }

    public Location getLocation() {
        return location;
    }


    @Override
    public void onLocationChanged(Location location) {
        // do stuff here with location object
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
