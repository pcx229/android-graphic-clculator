package com.graphingcalculator;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.graphingcalculator.graph.Range;

public class Settings {

    private Range range;
    public static final Range DEFAULT_RANGE = new Range(-10, 10, Double.NaN, Double.NaN);
    private boolean showAxis, showGrid, ratioLock;
    public static final boolean DEFAULT_SHOW_AXIS = true,
            DEFAULT_SHOW_GRID = true,
            DEFAULT_RATIO_LOCK = false;
    private float graphWidth, graphHeight;

    private Settings(Range range, boolean showAxis, boolean showGrid, boolean ratioLock) {
        this.range = range;
        this.showAxis = showAxis;
        this.showGrid = showGrid;
        this.ratioLock = ratioLock;
    }

    public static Settings load(Application context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // range
        Range range;
        if(pref.contains("range")) {
            float startX, endX, startY, endY;
            startX = pref.getFloat("range-startX", 0);
            endX = pref.getFloat("range-endX", 0);
            startY = pref.getFloat("range-startY", 0);
            endY = pref.getFloat("range-endY", 0);
            range = new Range(startX, endX, startY, endY);
        } else {
            range = new Range(DEFAULT_RANGE);
        }

        // show axis
        boolean showAxis = pref.getBoolean("showAxis", DEFAULT_SHOW_AXIS);

        // show grid
        boolean showGrid = pref.getBoolean("showGrid", DEFAULT_SHOW_GRID);

        // ratio lock
        boolean ratioLock = pref.getBoolean("ratioLock", DEFAULT_RATIO_LOCK);

        return new Settings(range, showAxis, showGrid, ratioLock);
    }

    public void save(Application context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        // range
        editor.putBoolean("range", true);
        editor.putFloat("range-startX", (float) range.startX);
        editor.putFloat("range-endX", (float) range.endX);
        editor.putFloat("range-startY", (float) range.startY);
        editor.putFloat("range-endY", (float) range.endY);

        // show axis
        editor.putBoolean("showAxis", showAxis);

        // show grid
        editor.putBoolean("showGrid", showGrid);

        // ratio lock
        editor.putBoolean("ratioLock", ratioLock);

        editor.commit();
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public boolean isShowAxis() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isRatioLock() {
        return ratioLock;
    }

    public void setRatioLock(boolean ratioLock) {
        this.ratioLock = ratioLock;
    }

    public void setGraphDimensions(float width, float height) {
        this.graphWidth = width;
        this.graphHeight = height;
    }

    public float getGraphHeight() {
        return graphHeight;
    }

    public float getGraphWidth() {
        return graphWidth;
    }
}
