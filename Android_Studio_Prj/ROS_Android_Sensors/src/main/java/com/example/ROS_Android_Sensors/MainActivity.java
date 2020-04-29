/* Copyright 2018 The MathWorks, Inc. */
package com.example.ROS_Android_Sensors;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    RadioButton datalogger;
    RadioButton master;
    RadioButton client;
    EditText masterURI;
    EditText masterPort;
    Button connectButton;
    RelativeLayout relaLay;
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        RadioGroup rg = (RadioGroup)findViewById(R.id.radiogroup);
        rg.check(R.id.rosclient);
        datalogger = (RadioButton)findViewById(R.id.datalogger);
        master = (RadioButton)findViewById(R.id.rosmaster);
        client = (RadioButton)findViewById(R.id.rosclient);
        connectButton = (Button) findViewById(R.id.createorconnect);
        connectButton.setEnabled(false);
        masterURI = (EditText) findViewById(R.id.MasterUri);
        masterPort = (EditText) findViewById(R.id.MasterPort);
        relaLay = (RelativeLayout) findViewById(R.id.mastercont);

        masterURI.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String uri = s.toString();
                if((TextUtils.isEmpty(uri)) || !IP_ADDRESS.matcher(uri).matches()) {
                    masterURI.setError("Please enter valid IP address");
                    connectButton.setEnabled(false);
                }
                else {
                    masterURI.setError(null);
                    connectButton.setEnabled(checkPortValidOrNot(masterPort.getText().toString()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        masterPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String uri = s.toString();
                if((TextUtils.isEmpty(uri) || !checkPortValidOrNot(uri))) {
                    if(!TextUtils.isEmpty(uri))
                        masterPort.setError("Please enter valid port number");
                    connectButton.setEnabled(false);
                }
                else {
                    masterPort.setError(null);
                    connectButton.setEnabled(IP_ADDRESS.matcher(masterURI.getText().toString()).matches());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        datalogger.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        relaLay.setVisibility(View.GONE);
                        connectButton.setText("Log Data");
                        connectButton.setEnabled(true);
                    }
                }
        );

        master.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    relaLay.setVisibility(View.GONE);
                    connectButton.setText("Create ROS Master");
                    connectButton.setEnabled(true);
                }
            }
        );
        client.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        relaLay.setVisibility(View.VISIBLE);
                        connectButton.setText("Connect to a Remote ROS Master");
                    }
                }
        );

        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean master = true;
                String masterIpAddress = "";
                String masterPortNo = "";
                String masterU = "";
                if (client.isChecked()) {
                    master = false;
                    masterIpAddress = masterURI.getText().toString();
                    masterPortNo = masterPort.getText().toString();
                    masterU = "http://" + masterIpAddress + ":" + masterPortNo + "/";
                }
                Intent mIntent = new Intent();
                mIntent.setAction("com.android.rosupport.START_MODEL_IMU");
                mIntent.putExtra("master", master);
                mIntent.putExtra("masterURI", masterU);
                mIntent.putExtra("datalogger", datalogger.isChecked());
                MainActivity.this.startActivity(mIntent);
                finish();
            }
        });
    }

    boolean checkPortValidOrNot(String port) {
        int result = Integer.parseInt(port);
        return ((result >1023) && (result< 65536));
    }

}
