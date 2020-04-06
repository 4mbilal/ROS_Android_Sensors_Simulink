/* Copyright 2018 The MathWorks, Inc. */
package com.example.ROS_Android_Sensors;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.ros.RosCore;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeListener;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

public class MWNodeMainExecutorService extends Service implements NodeMainExecutor {

  private static final String TAG = "MWNodeMainExecutorService";

  private static final int ONGOING_NOTIFICATION = 1;

  public static final String ACTION_START = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE";
  public static final String ACTION_SHUTDOWN = "org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
  public static final String EXTRA_NOTIFICATION_TITLE = "org.ros.android.EXTRA_NOTIFICATION_TITLE";
  public static final String EXTRA_NOTIFICATION_TICKER = "org.ros.android.EXTRA_NOTIFICATION_TICKER";

  private final NodeMainExecutor nodeMainExecutor;
  private final IBinder binder;
  private final ListenerGroup<MWNodeMainExecutorServiceListener> listeners;

  private Handler handler;
  private WakeLock wakeLock;
  private WifiLock wifiLock;
  private RosCore rosCore;
  private URI masterUri;
  private String rosHostname;

  /**
   * Class for clients to access. Because we know this service always runs in
   * the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public MWNodeMainExecutorService getService() {
      return MWNodeMainExecutorService.this;
    }
  }

  public MWNodeMainExecutorService() {
    super();
    rosHostname = null;
    nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
    binder = new LocalBinder();
    listeners =
        new ListenerGroup<MWNodeMainExecutorServiceListener>(
            nodeMainExecutor.getScheduledExecutorService());
  }

  @Override
  public void onCreate() {
    handler = new Handler();
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    wakeLock.acquire();
    int wifiLockType = WifiManager.WIFI_MODE_FULL;
    try {
      wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
    } catch (Exception e) {
      // We must be running on a pre-Honeycomb device.
      Log.w(TAG, "Unable to acquire high performance wifi lock.");
    }
    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    wifiLock = wifiManager.createWifiLock(wifiLockType, TAG);
    wifiLock.acquire();
  }

  @Override
  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration,
      Collection<NodeListener> nodeListeneners) {
    nodeMainExecutor.execute(nodeMain, nodeConfiguration, nodeListeneners);
  }

  @Override
  public void execute(NodeMain nodeMain, NodeConfiguration nodeConfiguration) {
    execute(nodeMain, nodeConfiguration, null);
  }

  @Override
  public ScheduledExecutorService getScheduledExecutorService() {
    return nodeMainExecutor.getScheduledExecutorService();
  }

  @Override
  public void shutdownNodeMain(NodeMain nodeMain) {
    nodeMainExecutor.shutdownNodeMain(nodeMain);
  }

  @Override
  public void shutdown() {
    forceShutdown();
  }

  public void forceShutdown() {
    signalOnShutdown();
    stopForeground(true);
    stopSelf();
  }

  public void addListener(MWNodeMainExecutorServiceListener listener) {
    listeners.add(listener);
  }

  public void removeListener(MWNodeMainExecutorServiceListener listener)
  {
    listeners.remove(listener);
  }

  private void signalOnShutdown() {
    listeners.signal(new SignalRunnable<MWNodeMainExecutorServiceListener>() {
      @Override
      public void run(MWNodeMainExecutorServiceListener nodeMainExecutorServiceListener) {
        nodeMainExecutorServiceListener.onShutdown(MWNodeMainExecutorService.this);
      }
    });
  }

  @Override
  public void onDestroy() {
    toast("Shutting down...");
    nodeMainExecutor.shutdown();
    if (rosCore != null) {
      rosCore.shutdown();
    }
    if (wakeLock.isHeld()) {
      wakeLock.release();
    }
    if (wifiLock.isHeld()) {
      wifiLock.release();
    }
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent.getAction() == null) {
      return START_NOT_STICKY;
    }
    if (intent.getAction().equals(ACTION_START)) {
      Preconditions.checkArgument(intent.hasExtra(EXTRA_NOTIFICATION_TICKER));
      Preconditions.checkArgument(intent.hasExtra(EXTRA_NOTIFICATION_TITLE));
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      Intent notificationIntent = new Intent(this, MWNodeMainExecutorService.class);
      notificationIntent.setAction(MWNodeMainExecutorService.ACTION_SHUTDOWN);
      PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
      Notification notification = builder.setContentIntent(pendingIntent)
              .setSmallIcon(org.ros.android.android_10.R.mipmap.icon)
              .setTicker(intent.getStringExtra(EXTRA_NOTIFICATION_TICKER))
              .setWhen(System.currentTimeMillis())
              .setContentTitle(intent.getStringExtra(EXTRA_NOTIFICATION_TITLE))
              .setAutoCancel(true)
              .setContentText("Tap to shutdown.")
              .build();
      startForeground(ONGOING_NOTIFICATION, notification);
    }
    if (intent.getAction().equals(ACTION_SHUTDOWN)) {
      shutdown();
    }
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public URI getMasterUri() {
    return masterUri;
  }

  public void setMasterUri(URI uri) {
    masterUri = uri;
  }

  public void setRosHostname(String hostname) {
    rosHostname = hostname;
  }

  public String getRosHostname() {
    return rosHostname;
  }
  /**
   * This version of startMaster can only create private masters.
   *
   * @deprecated use {@link public void startMaster(Boolean isPrivate)} instead.
   */
  @Deprecated
  public void startMaster() {
    startMaster(true);
  }

  /**
   * Starts a new ros master in an AsyncTask.
   * @param isPrivate
   */
  public void startMaster(boolean isPrivate) {
    AsyncTask<Boolean, Void, URI> task = new AsyncTask<Boolean, Void, URI>() {
      @Override
      protected URI doInBackground(Boolean[] params) {
        MWNodeMainExecutorService.this.startMasterBlocking(params[0]);
        return MWNodeMainExecutorService.this.getMasterUri();
      }
    };
    task.execute(isPrivate);
    try {
      task.get();
    } catch (InterruptedException e) {
      throw new RosRuntimeException(e);
    } catch (ExecutionException e) {
      throw new RosRuntimeException(e);
    }
  }

  /**
   * Private blocking method to start a Ros Master.
   * @param isPrivate
   */
  private void startMasterBlocking(boolean isPrivate) {
    if (isPrivate) {
      rosCore = RosCore.newPrivate();
    } else if (rosHostname != null) {
      rosCore = RosCore.newPublic(rosHostname, 11311);
    } else {
      rosCore = RosCore.newPublic(11311);
    }
    rosCore.start();
    try {
      rosCore.awaitStart();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
    masterUri = rosCore.getUri();
  }

  public void toast(final String text) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MWNodeMainExecutorService.this, text, Toast.LENGTH_SHORT).show();
      }
    });
  }
}
