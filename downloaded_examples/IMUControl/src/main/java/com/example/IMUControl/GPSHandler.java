package com.example.IMUControl;
import android.os.Bundle;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

public class GPSHandler extends Service implements LocationListener {
    private final Context mContext;

    // flag for GPS status
    boolean mIsGPSEnabled = false;

    // flag for network status
    boolean mIsNetworkEnabled = false;

    // flag for GPS status
    boolean mCanGetLocation = false;

    Location mLocation; // location
    double[] mDefault;  // default data
    double[] mLatLongAlt; // latitude and longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    // Declaring a Location Manager
    protected LocationManager mLocationManager;

    public GPSHandler(Context context) {
        this.mContext = context;
        mLatLongAlt = new double[] { 0.0, 0.0, 0.0 };
        getLocation();
    }

    public Location getLocation() {
        try {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            mIsGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            mIsNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                // no network provider is enabled
                this.mCanGetLocation = false;
                mLatLongAlt[0] = 0.0;
                mLatLongAlt[1] = 0.0;
                mLatLongAlt[2] = 0.0;
            } else {
                this.mCanGetLocation = true;
                // First get location from Network Provider
                if (mIsNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (mLocationManager != null) {
                        mLocation = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            mLatLongAlt[0] = mLocation.getLatitude();
                            mLatLongAlt[1] = mLocation.getLongitude();
                            mLatLongAlt[2] = mLocation.getAltitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (mIsGPSEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (mLocationManager != null) {
                            mLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                mLatLongAlt[0] = mLocation.getLatitude();
                                mLatLongAlt[1] = mLocation.getLongitude();
                                mLatLongAlt[2] = mLocation.getAltitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLocation;
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     * */
    public void stopUsingGPS() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(GPSHandler.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double[] getLocationData() {
        if (canGetLocation() && (mLocation != null)) {
            mLatLongAlt[0] = mLocation.getLatitude();
            mLatLongAlt[1] = mLocation.getLongitude();
            mLatLongAlt[2] = mLocation.getAltitude();
        }
        return mLatLongAlt;
    }

    /**
     * Function to check GPS/wifi enabled
     * 
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.mCanGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        mIsGPSEnabled = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
        mIsGPSEnabled = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
