/* Copyright 2018 The MathWorks, Inc. */
package com.example.IMUControl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.ros.address.InetAddressFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.namespace.GraphName;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class ROSMainActivity extends AppCompatActivity {
    private NodeMainExecutorServiceConnection nodeMainExecutorServiceConnection;
    protected MWNodeMainExecutorService nodeMainExecutorService;
    //masterURI should be null for device acting as Master
   String masterURI = "http://172.18.188.153:11311/";
    /**
     * Lookup text for catching a ConnectionException when attempting to
     * connect to a master.
     */
    private static final String CONNECTION_EXCEPTION_TEXT = "ECONNREFUSED";

    /**
     * Lookup text for catching a UnknownHostException when attemping to
     * connect to a master.
     */
    private static final String UNKNOW_HOST_TEXT = "UnknownHost";
    public URI getMasterUri() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getMasterUri();
    }

    public String getRosHostname() {
        Preconditions.checkNotNull(nodeMainExecutorService);
        return nodeMainExecutorService.getRosHostname();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean master = getIntent().getBooleanExtra("master",true);
        if(master)
            masterURI =null;
        else {
            masterURI = getIntent().getStringExtra("masterURI");
            Log.d("dfg","get master uri " + masterURI);
        }
        try {
            if(masterURI != null)
            nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection(new URI(masterURI));
            else
                nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection(null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }





    }
    @Override
    protected void onStart() {
        super.onStart();
        if(masterURI != null) {
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        MasterClient masterClient = new MasterClient(new URI(masterURI));
                        masterClient.getUri(GraphName.of("android/master_chooser_activity"));
                        toast("Connected!");
                        return true;
                    } catch (URISyntaxException e) {
                        toast("Invalid URI.");
                        return false;
                    } catch (XmlRpcTimeoutException e) {
                        toast("Master unreachable!");
                        return false;
                    } catch (Exception e) {
                        String exceptionMessage = e.getMessage();
                        if(exceptionMessage.contains(CONNECTION_EXCEPTION_TEXT))
                            toast("Unable to communicate with master!");
                        else if(exceptionMessage.contains(UNKNOW_HOST_TEXT))
                            toast("Unable to resolve URI hostname!");
                        else
                            toast("Communication error!");
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                }
            }.execute();
        }
        bindNodeMainExecutorService();
    }

    protected void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ROSMainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    protected void bindNodeMainExecutorService() {
        Intent intent = new Intent(this, MWNodeMainExecutorService.class);
        intent.setAction(MWNodeMainExecutorService.ACTION_START);
        intent.putExtra(MWNodeMainExecutorService.EXTRA_NOTIFICATION_TICKER, "shutdown");
        intent.putExtra(MWNodeMainExecutorService.EXTRA_NOTIFICATION_TITLE, "rosapp");
        startService(intent);
        Preconditions.checkState(
                bindService(intent, nodeMainExecutorServiceConnection, BIND_AUTO_CREATE),
                "Failed to bind NodeMainExecutorService.");
    }
    private final class NodeMainExecutorServiceConnection implements ServiceConnection {

        private MWNodeMainExecutorServiceListener serviceListener;
        private URI customMasterUri;

        public NodeMainExecutorServiceConnection(URI customUri) {
            super();
            customMasterUri = customUri;
        }
        public URI getMasterUri() {
            Preconditions.checkNotNull(nodeMainExecutorService);
            return nodeMainExecutorService.getMasterUri();
        }

        public String getRosHostname() {
            Preconditions.checkNotNull(nodeMainExecutorService);
            return nodeMainExecutorService.getRosHostname();
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            nodeMainExecutorService = ((MWNodeMainExecutorService.LocalBinder) binder).getService();
            Log.d("sruthi","custome uri " + customMasterUri);
            if (customMasterUri != null) {


                nodeMainExecutorService.setMasterUri(customMasterUri);
                nodeMainExecutorService.setRosHostname(getDefaultHostAddress());
            }

            serviceListener = new MWNodeMainExecutorServiceListener() {
                @Override
                public void onShutdown(MWNodeMainExecutorService nodeMainExecutorService) {
                    // We may have added multiple shutdown listeners and we only want to
                    // call finish() once.
                    ROSMainActivity.this.finish();
                }
            };
            nodeMainExecutorService.addListener(serviceListener);
            if (getMasterUri() == null) {
                startMasterChooser();
            } else {
                init();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            nodeMainExecutorService.removeListener(serviceListener);
            serviceListener = null;
        }

        public MWNodeMainExecutorServiceListener getServiceListener() {
            return serviceListener;
        }

    }
    private void startMasterChooser() {
        String host;
        host = getDefaultHostAddress();

        nodeMainExecutorService.setRosHostname(host);
        nodeMainExecutorService.startMaster( false);
        // Run init() in a new thread as a convenience since it often requires network access.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ROSMainActivity.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();

    }
    @Override
    protected void onDestroy() {
        unbindService(nodeMainExecutorServiceConnection);
        nodeMainExecutorService.
                removeListener(nodeMainExecutorServiceConnection.getServiceListener());
        super.onDestroy();
    }


    protected void init() {
        // Run init() in a new thread as a convenience since it often requires
        // network access.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ROSMainActivity.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();
    }

    protected abstract void init(NodeMainExecutor nodeMainExecutor);
    private String getDefaultHostAddress() {
        return InetAddressFactory.newNonLoopback().getHostAddress();
    }


}
