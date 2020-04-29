package com.example.IMUControl;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * ScopeAttributeParser.java
 *
 * Utility class to parse scope attributes JSON string.
 * 
 * Copyright 2016-2017 The MathWorks, Inc.
 */
class ScopeAttributeParser {
    public static ScopeAttribute parse(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return readScopeAttribute(reader);
        } finally {
            reader.close();
        }
    }

    private static ScopeAttribute readScopeAttribute(JsonReader reader) throws IOException {
        List<Double> axesColor = null;
        String axesScaling = null;
        List<Double> axesTickColor = null;
        String blockType = null;
        List<ScopeAttributesDisplay> displays = null;
        Boolean frameBasedProcessing = null;
        List<String> inputNames = null;
        List<Double> layoutDimensions = null;
        Double timeSpan = null;
        String timeSpanOverrunMode = null;
        String plotType = null;
        Double sampleIncrement = null;
        Double xOffset = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "axesColor":
                    axesColor = readDoublesArray(reader);
                    break;
                case "axesScaling":
                    axesScaling = reader.nextString();
                    break;
                case "axesTickColor":
                    axesTickColor = readDoublesArray(reader);
                    break;
                case "blockType":
                    blockType = reader.nextString();
                    break;
                case "displays":
                    displays = readDisplaysArray(reader);
                    break;
                case "frameBasedProcessing":
                    frameBasedProcessing = reader.nextBoolean();
                    break;
                case "inputNames":
                    inputNames = readStringsArray(reader);
                    break;
                case "layoutDimensions":
                    layoutDimensions = readDoublesArray(reader);
                    break;
                case "timeSpan":
                    timeSpan = reader.nextDouble();
                    break;
                case "timeSpanOverrunMode":
                    timeSpanOverrunMode = reader.nextString();
                    break;
                case "plotType":
                    plotType = reader.nextString();
                    break;
                case "sampleIncrement":
                    sampleIncrement = reader.nextDouble();
                    break;
                case "xOffset":
                    xOffset = reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new ScopeAttribute(axesColor, axesScaling, axesTickColor, blockType, displays, frameBasedProcessing, inputNames, layoutDimensions, timeSpan, timeSpanOverrunMode, plotType, sampleIncrement, xOffset);
    }

    private static List<Double> readDoublesArray(JsonReader reader) throws IOException {
        List<Double> doubles = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            doubles.add(reader.nextDouble());
        }
        reader.endArray();
        return doubles;
    }

    private static List<List<Double>> readArrayOfDoublesArray(JsonReader reader) throws IOException {
        List<List<Double>> doubles = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            doubles.add(readDoublesArray(reader));
        }
        reader.endArray();
        return doubles;
    }

    private static List<String> readStringsArray(JsonReader reader) throws IOException {
        List<String> strings = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            switch (reader.peek()) {
                case STRING:
                    strings.add(reader.nextString());
                    break;
                case BEGIN_ARRAY:
                    strings.addAll((ArrayList) readStringsArray(reader));
                    break;
            }
        }
        reader.endArray();
        return strings;
    }

    private static List<ScopeAttributesDisplay> readDisplaysArray(JsonReader reader) throws IOException {
        List<ScopeAttributesDisplay> displays = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            displays.add(readDisplay(reader));
        }
        reader.endArray();
        return displays;
    }

    private static ScopeAttributesDisplay readDisplay(JsonReader reader) throws IOException {
        List<List<Double>> lineColors = null;
        List<String> lineStyles = null;
        List<Double> lineWidths = null;
        Boolean showGrid = null;
        Boolean showLegend = null;
        List<Double> yLimits = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "lineColors":
                    lineColors = readArrayOfDoublesArray(reader);
                    break;
                case "lineStyles":
                    lineStyles = readStringsArray(reader);
                    break;
                case "lineWidths":
                    lineWidths = readDoublesArray(reader);
                    break;
                case "showGrid":
                    showGrid = reader.nextBoolean();
                    break;
                case "showLegend":
                    showLegend = reader.nextBoolean();
                    break;
                case "yLimits":
                    yLimits = readDoublesArray(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new ScopeAttributesDisplay(lineColors, lineStyles, lineWidths, showGrid, showLegend, yLimits);
    }
}
