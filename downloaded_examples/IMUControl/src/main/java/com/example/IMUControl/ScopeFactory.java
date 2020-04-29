package com.example.IMUControl;

/**
 * Copyright 2017 The MathWorks, Inc.
 */

class ScopeFactory {
    SimScope getScope(ScopeAttribute scopeAttribute) {
        if (scopeAttribute == null) {
            return null;
        }
        if (scopeAttribute.getBlockType().equalsIgnoreCase("Scope")) {
            return new TimeScope();
        }
        if (scopeAttribute.getBlockType().equalsIgnoreCase("ArrayPlot")) {
            if (scopeAttribute.getPlotType().equalsIgnoreCase("Line")) {
                return new LineArrayPlot();
            }
            if (scopeAttribute.getPlotType().equalsIgnoreCase("Stem")) {
                return new StemArrayPlot();
            }
            if (scopeAttribute.getPlotType().equalsIgnoreCase("Stairs")) {
                return new LineArrayPlot();
            }
        }
        return null;
    }
}
