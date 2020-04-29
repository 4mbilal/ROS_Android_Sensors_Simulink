package com.example.ROS_Android_Sensors;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.ImageButton;
import android.support.v4.content.res.ResourcesCompat;
import android.os.CountDownTimer;
import android.os.Build;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class ROS_Android_Sensors extends ROSMainActivity implements SensorEventListener, OnFragmentInteractionListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private InfoFragment infoFragment = null;
     private float[] mGyroscopeData = { 0.0f, 0.0f, 0.0f };
     private float[] mAccelerometerData = { 0.0f, 0.0f, 0.0f };
     private float mTemperatureData = 0.0f;
     private float mPressureData = 0.0f;
     private float[] mMagnetometerData = { 0.0f, 0.0f, 0.0f };
   private final float[] mRotationMatrix = new float[9];
private final float[] mOrientationAngles = new float[3];
     private SensorManager mSensorManager;
     private GPSHandler mGPSHandler;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 300;
    private boolean isFineLocationPermissionGranted = false;
    private boolean isFineLocationPermissionRequested = false;

    PublishNodeIMU pn_imu;
    PublishNodeGPS pn_gps;
    PublishNodeMag pn_mag;
    PublishNodePress pn_press;
    PublishNodeTemp pn_tmp;
    PublishNodeOdom pn_odom;

    private String filename = "";////"/storage/emulated/0/highway.mp4"
    private String filepath = "Sensor_Logs";
    File myExternalFile;
    String myData = "";
    FileOutputStream fos;
    PowerManager.WakeLock wl;

     private void registerSensorManager() {
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
            SensorManager.SENSOR_DELAY_FASTEST);
     }

    private boolean checkIfAllPermissionsGranted()
    {
        return true && isFineLocationPermissionGranted;
    }
    private void requestPermission() {
        String permissionRationale = "";
        // Here, thisClass is the current activity
        //request for fine location
        if (ContextCompat.checkSelfPermission(thisClass,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted. Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisClass,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionRationale += "Access fine location, ";
            } else {
                // No explanation needed; request the permission
                if (!isFineLocationPermissionRequested) {
                    isFineLocationPermissionRequested = true;
                    ActivityCompat.requestPermissions(thisClass,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    return;
                }
            }
        } else {
            // Permission has already been granted
            isFineLocationPermissionGranted = true;
        }
        if (!permissionRationale.isEmpty())
            if (infoFragment != null) {
                infoFragment.updateModelInfo(permissionRationale + "permission not granted. Model cannot start.");
            }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Uncomment the following line to specify a custom App Title
        //toolbar.setTitle("My custom Title");
        setSupportActionBar(toolbar);

        // Create a FragmentPagerAdapter that returns individual fragments
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(SectionsPagerAdapter.getNumTabs()-1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Initiate the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mGPSHandler = new GPSHandler(this);
        thisClass = this;

        long msecs = System.currentTimeMillis();
        filename = String.format("%d_Sensor_Log.csv",msecs);
        myExternalFile = new File(getExternalFilesDir(filepath), filename);
        try {
            fos = new FileOutputStream(myExternalFile);
            //fos.write(myData.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Try to remain awake during screen locks
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ROS:Sense");
        wl.acquire();
     }

    private ROS_Android_Sensors thisClass;
    private final Thread BgThread = new Thread() {
    @Override
    public void run() {
            String argv[] = new String[] {"MainActivity","ROS_Android_Sensors"};
            naMain(argv, thisClass);
        }
    };

    public void flashMessage(final String inMessage) {
        runOnUiThread(new Runnable() {
              public void run() {
                    Toast.makeText(getBaseContext(), inMessage, Toast.LENGTH_SHORT).show();
              }
        });
    }

    protected void onDestroy() {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

         if (BgThread.isAlive())
             naOnAppStateChange(6);
         super.onDestroy();
        wl.release();//Release the wake lock
         System.exit(0); //to kill all our threads.
    }

	@Override
    public void onFragmentCreate(String name) {

    }

    @Override
    public void onFragmentStart(String name) {
        switch (name) {
            case "Info":
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentResume(String name) {
        switch (name) {
            case "Info":
                if (checkIfAllPermissionsGranted()){
                    if (!BgThread.isAlive()) {
                        BgThread.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentPause(String name) {
    }
    @Override
    protected void onResume() {
         super.onResume();
         if (BgThread.isAlive())
             naOnAppStateChange(3);
         registerSensorManager();
    }

    @Override
    protected void onPause() {
        if (BgThread.isAlive())
            naOnAppStateChange(4);
         mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof InfoFragment) {
            this.infoFragment = (InfoFragment) fragment;
            infoFragment.setFragmentInteractionListener(this);
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do related task you need to do.
                    isFineLocationPermissionGranted = true;
                    mGPSHandler = new GPSHandler(thisClass);
                } else {
                    // permission denied, boo!
                    flashMessage("Access location Permission not granted");
                }
                isFineLocationPermissionRequested = false;
                break;

            // other case lines to check for other
            // permissions this app might request.
        }
        if (!checkIfAllPermissionsGranted()) {
            requestPermission();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float [] values = event.values;
        //Comment out if you want to log the data in logcat
        //String logMessage = String.format("%d: 0'%g'", event.sensor.getType(), values[0]);
        //Log.d("Sensor Data IN:", logMessage);
        switch(event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                mGyroscopeData[0] = values[0];
                mGyroscopeData[1] = values[1];
                mGyroscopeData[2] = values[2];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData[0] = values[0];
                mAccelerometerData[1] = values[1];
                mAccelerometerData[2] = values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData[0] = values[0];
                mMagnetometerData[1] = values[1];
                mMagnetometerData[2] = values[2];
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                mTemperatureData = values[0];
                break;
            case Sensor.TYPE_PRESSURE:
                mPressureData = values[0];
                break;
        }
/*        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ROS:Sense");
        wl.acquire();*/
    }

    // Get SensorEvent Data throws exception if the data is null
    public float[] getGyroscopeData() {
        return mGyroscopeData;
    }

    public float[] getAccelerometerData() {
        return mAccelerometerData;
    }

    public float[] getMagnetometerData() {
        return mMagnetometerData;
    }

    public float[] getOrientationData() {
        mSensorManager.getRotationMatrix(mRotationMatrix, null,mAccelerometerData, mMagnetometerData);
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
//        mOrientationAngles[0] = (float)Math.toDegrees(mOrientationAngles[0]);
//        mOrientationAngles[1] = (float)Math.toDegrees(mOrientationAngles[1]);
//        mOrientationAngles[2] = (float)Math.toDegrees(mOrientationAngles[2]);
        return mOrientationAngles;
    }

    public float getTemperatureData() {
        return mTemperatureData;
    }

    public float getPressureData() {
        return mPressureData;
    }

    // Get GPS Data if GPS is enabled. Otherwise return 0,0
    public double[] getGPSData() {
        return mGPSHandler.getLocationData();
    }

    public void ROSPublishFast(float[] data)
    {
        if(datalogger) {
            try {
                long msecs = System.currentTimeMillis();
                //GPS and XYZ odometry needs to be stored with at least 6 figures after decimal
                myData = String.format("1,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.7f,%.7f,%.7f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n", msecs, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19], data[20], data[21], data[22], data[23], data[24], data[25], data[26], data[27]);
                fos.write(myData.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (pn_imu != null)
                pn_imu.publishData(data);
            if (pn_mag != null)
                pn_mag.publishData(data);
            if (pn_odom != null)
                pn_odom.publishData(data);
        }
    }
    public void ROSPublishSlow(float[] data)
    {

        if(datalogger) {
            try {
                long msecs = System.currentTimeMillis();
                myData = String.format("0,%d,%.7f,%.7f,%.4f,%.4f,%.4f\n", msecs, data[0], data[1], data[2], data[3], data[4]);
                fos.write(myData.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (pn_gps != null)
                pn_gps.publishData(data);
            if (pn_press != null)
                pn_press.publishData(data);
            if (pn_tmp != null)
                pn_tmp.publishData(data);
        }

        ROS_Android_Sensors.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            final TextView mTextView = (TextView) findViewById(R.id.InfoTab_Data);
            mTextView.setText(myData);//show the sent message on screen
        //    mTextView.invalidate();
        }
        });
    }
    protected  void init(NodeMainExecutor nodeMainExecutor) {
        pn_imu = new PublishNodeIMU("/android/imu","sensor_msgs/Imu");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeConfiguration.setNodeName("android_sensors_driver_imu");
        nodeMainExecutor.execute(pn_imu, nodeConfiguration);

        pn_gps = new PublishNodeGPS("/android/gps","sensor_msgs/NavSatFix");
        NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration2.setMasterUri(getMasterUri());
        nodeConfiguration2.setNodeName("android_sensors_driver_nav_sat_fix");
        nodeMainExecutor.execute(pn_gps, nodeConfiguration2);

        pn_mag = new PublishNodeMag("/android/mag","sensor_msgs/MagneticField");
        NodeConfiguration nodeConfiguration3 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration3.setMasterUri(getMasterUri());
        nodeConfiguration3.setNodeName("android_sensors_driver_mag");
        nodeMainExecutor.execute(pn_mag, nodeConfiguration3);

        pn_press = new PublishNodePress("/android/pressure","sensor_msgs/FluidPressure");
        NodeConfiguration nodeConfiguration4 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration4.setMasterUri(getMasterUri());
        nodeConfiguration4.setNodeName("android_sensors_driver_pressure");
        nodeMainExecutor.execute(pn_press, nodeConfiguration4);

        pn_tmp = new PublishNodeTemp("/android/temp","sensor_msgs/Temperature");
        NodeConfiguration nodeConfiguration5 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration5.setMasterUri(getMasterUri());
        nodeConfiguration5.setNodeName("android_sensors_driver_temp");
        nodeMainExecutor.execute(pn_tmp, nodeConfiguration5);

        pn_odom = new PublishNodeOdom("/android/odom","nav_msgs/Odometry");
        NodeConfiguration nodeConfiguration6 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration6.setMasterUri(getMasterUri());
        nodeConfiguration6.setNodeName("android_nav_odom");
        nodeMainExecutor.execute(pn_odom, nodeConfiguration6);

    };

    private native int naMain(String[] argv, ROS_Android_Sensors pThis);
    private native void naOnAppStateChange(int state);
    static {
        System.loadLibrary("ROS_Android_Sensors");
    }

}
