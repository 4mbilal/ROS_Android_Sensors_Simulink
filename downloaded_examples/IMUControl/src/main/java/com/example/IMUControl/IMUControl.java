package com.example.IMUControl;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
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

public class IMUControl extends ROSMainActivity implements SensorEventListener, OnFragmentInteractionListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private InfoFragment infoFragment = null;
    private ViewPager mCameraScopePager;

    private boolean isWidgetsLayoutHidden = false;
     private Hashtable<Integer,TextView> textViews = new Hashtable<Integer,TextView>();
     private float[] mGyroscopeData = { 0.0f, 0.0f, 0.0f };
     private float[] mAccelerometerData = { 0.0f, 0.0f, 0.0f };
     private float[] mMagnetometerData = { 0.0f, 0.0f, 0.0f };
   private final float[] mRotationMatrix = new float[9];
private final float[] mOrientationAngles = new float[3];
     private SensorManager mSensorManager;
     private GPSHandler mGPSHandler;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 300;
    private boolean isFineLocationPermissionGranted = false;
    private boolean isFineLocationPermissionRequested = false;
    private String[] scopeTitles = {"Scope"};

    private ImageButton btnZoom = null;
    PublishNode pn_imu;
    PublishNodeGPS pn_gps;

    private final CountDownTimer timerZoomButton = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long l) {
            if (btnZoom!=null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    btnZoom.setAlpha(0.5f + 0.1f*l);
                }
            }
        }

        @Override
        public void onFinish() {
            if (btnZoom != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    btnZoom.setAlpha(0.4f);
                }
            }
        }
    };
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
     }

    private void registerAndSetChartSettingsOfaScope(int i) {
        FrameLayout frameLayout = (FrameLayout) findViewById(
                getResources().getIdentifier("scopeLayout" + i, "id", getPackageName()));
        if (null == frameLayout) {
            Log.e("MainActivity", "registerAndSetChartSettingsOfaScope: frameLayout is null.");
            return;
        }
        ScopeHelper.getInstance().putAChartInLayout(i, frameLayout, this);
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
     }

    private IMUControl thisClass;
    private final Thread BgThread = new Thread() {
    @Override
    public void run() {
            String argv[] = new String[] {"MainActivity","IMUControl"};
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
         if (BgThread.isAlive())
             naOnAppStateChange(6);
         super.onDestroy();
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
            case "App":
                if (mCameraScopePager == null) {
                    registerCameraScopeLayout();
                }
                registerDataDisplays();
                break;
            case "dot1":
                registerAndSetChartSettingsOfaScope(1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentResume(String name) {
        switch (name) {
            case "App":
                break;
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

    public void registerDataDisplays() {
    // bind text views for data display block;
    for (int i = 1; i <= 2; i++) {
            TextView textView = (TextView) findViewById(
            getResources().getIdentifier("DataDisplay" + i, "id", getPackageName()));
            textViews.put(i, textView);
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
        }
    }

    // Get SensorEvent Data throws exception if the data is null
    public float[] getGyroscopeData() {
        return mGyroscopeData;
    }

    public float[] getAccelerometerData() {
        return mAccelerometerData;
    }

    public float[] getOrientationData() {
        mSensorManager.getRotationMatrix(mRotationMatrix, null,mAccelerometerData, mMagnetometerData);
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        mOrientationAngles[0] = (float)Math.toDegrees(mOrientationAngles[0]);
        mOrientationAngles[1] = (float)Math.toDegrees(mOrientationAngles[1]);
        mOrientationAngles[2] = (float)Math.toDegrees(mOrientationAngles[2]);
        return mOrientationAngles;
    }

    // Get GPS Data if GPS is enabled. Otherwise return 0,0
    public double[] getGPSData() {
        return mGPSHandler.getLocationData();
    }

    public void displayText(int id, byte[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, short[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, int[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, long[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, float[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, double[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    private void updateTextViewById(final int id, final String finalStringToDisplay) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    textViews.get(id).setText(finalStringToDisplay);
                } catch (Exception ex) {
                    Log.e("IMU.updateTextViewById", ex.getLocalizedMessage());
                }
            }
        });
    }
    public void initScope(final int scopeID, final int numInputPorts, final byte[] attribute, final float[] sampleTimes) {
        ScopeHelper scopeHelper = ScopeHelper.getInstance();
        scopeHelper.initScope(scopeID, numInputPorts, attribute, sampleTimes);
        if (!scopeHelper.checkIfScopeRegistered(scopeID)){
            Log.w("MainActivity", "Scope not registered.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registerAndSetChartSettingsOfaScope(scopeID);
                }
            });
        }
        scopeHelper.setTitle(scopeID, scopeTitles[scopeID-1]);
    }

    public void cachePlotData(int scopeID, int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        ScopeHelper.getInstance().cachePlotData(scopeID, portIdx, data, sigNumDims, sigDims);
    }

    public void plotData(int scopeID) {
        ScopeHelper.getInstance().plotData(scopeID);
    }
    private void registerCameraScopeLayout() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        CameraScopeSectionsPagerAdapter mCameraScopeAdapter = new CameraScopeSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mCameraScopePager = (ViewPager) findViewById(R.id.cameraScopeContainer);
        mCameraScopePager.setOffscreenPageLimit(1);
        mCameraScopePager.setAdapter(mCameraScopeAdapter);

        TabLayout dotsLayout = (TabLayout) findViewById(R.id.dots);
        dotsLayout.setupWithViewPager(mCameraScopePager);

        btnZoom = (ImageButton) findViewById(R.id.btnZoom);
        btnZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.INVISIBLE);
                View widgetsLayout = findViewById(R.id.auto_generated_widgets);
                if (isWidgetsLayoutHidden) {
                    widgetsLayout.setVisibility(View.VISIBLE);
                    isWidgetsLayoutHidden = false;
                    ((ImageButton)view).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.expand, null));
                } else {
                    widgetsLayout.setVisibility(View.GONE);
                    isWidgetsLayoutHidden = true;
                    ((ImageButton)view).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.shrink, null));
                }
                view.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    view.setAlpha(1.0f);
                }
                timerZoomButton.start();
            }
        });
timerZoomButton.start();
    }

    public void ROSPublish(float[] data)
    {
        if(pn_imu != null)
            pn_imu.publishData(data);
        if(pn_gps != null)
            pn_gps.publishData(data);
    }

    protected  void init(NodeMainExecutor nodeMainExecutor) {
        pn_imu = new PublishNode("/android/imu","sensor_msgs/Imu");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeConfiguration.setNodeName("android_sensors_driver_imu");
        nodeMainExecutor.execute(pn_imu, nodeConfiguration);

        pn_gps = new PublishNodeGPS("/android/gps","sensor_msgs/NavSatFix");
        NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration2.setMasterUri(getMasterUri());
        nodeConfiguration2.setNodeName("android_sensors_driver_nav_sat_fix");
        nodeMainExecutor.execute(pn_gps, nodeConfiguration2);
    };

    private native int naMain(String[] argv, IMUControl pThis);
    private native void naOnAppStateChange(int state);
    static {
        System.loadLibrary("IMUControl");
    }

}
