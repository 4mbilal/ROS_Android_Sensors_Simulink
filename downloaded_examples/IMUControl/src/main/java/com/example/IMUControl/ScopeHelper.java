package com.example.IMUControl;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Copyright 2016-2017 The MathWorks, Inc.
 */
public class ScopeHelper {
    private static final String TAG = "ScopeHelper";
    private static ScopeHelper ourInstance = new ScopeHelper();

    private ScopeFactory scopeFactory = new ScopeFactory();
    private Hashtable<Integer, SimScope> scopes = new Hashtable<>();
    private Hashtable<Integer, Integer> needToRegister = new Hashtable<>();

    private ScopeHelper() {
    }

    public static ScopeHelper getInstance() {
        return ourInstance;
    }

    public void setTitle(int id, String title) {
        SimScope simScope = this.scopes.get(id);
        if (simScope == null) {
            Log.e(TAG, "setTitle: simScope is null.");
            return;
        }
        simScope.setTitle(title);
    }

    public void putAChartInLayout(int index, FrameLayout layout, Context context) {
        SimScope simScope = this.scopes.get(index);
        if (simScope == null) {
            Log.e(TAG, "putAChartInLayout: simScope is null.");
            needToRegister.put(index, index);
            return;
        }
        if (null == layout) {
            Log.e(TAG, "putAChartInLayout: layout is null.");
            return;
        }
        simScope.setChartSettings(layout, context);
    }

    public boolean checkIfScopeRegistered(int scopeID) {
        return null == needToRegister.get(scopeID);
    }

    public void initScope(int scopeID, int numInputPorts, byte[] attribute, float[] sampleTimes) {

        InputStream is = new ByteArrayInputStream(attribute);
        ScopeAttribute scopeAttribute = null;
        try {
            scopeAttribute = ScopeAttributeParser.parse(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimScope simScope = scopeFactory.getScope(scopeAttribute);

        simScope.init(scopeAttribute, sampleTimes);

        scopes.put(scopeID, simScope);
    }

    public void cachePlotData(int scopeID, int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        SimScope simScope = scopes.get(scopeID);
        if (simScope == null) {
            Log.e(TAG, "cachePlotData: simScope is null");
            return;
        }
        simScope.cacheData(portIdx, data, sigNumDims, sigDims);
    }

    public void plotData(int scopeID) {
        SimScope simScope = scopes.get(scopeID);
        if (simScope == null) {
            Log.e(TAG, "plotData: simScope is null");
            return;
        }
        simScope.plotData();
    }
}
