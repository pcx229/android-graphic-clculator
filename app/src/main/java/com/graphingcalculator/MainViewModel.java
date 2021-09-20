package com.graphingcalculator;

import android.app.Application;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.graphingcalculator.graph.Equation;
import com.graphingcalculator.graph.Range;
import com.graphingcalculator.graph.SystemOfEquations;

public class MainViewModel extends AndroidViewModel {
    private SystemOfEquations equations;
    private Settings settings;

    private MutableLiveData<SystemOfEquations> equationsUpdates = new MutableLiveData<>();
    private MutableLiveData<Settings> settingsUpdates = new MutableLiveData<>();

    public interface OnResetRangeListener {
        void onReset(Range range);
    }
    private OnResetRangeListener resetRangeUpdates;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        // load settings
        settings = Settings.load(getApplication());
        settingsUpdates.postValue(settings);

        // equations
        equations = new SystemOfEquations();
        AsyncTask.execute(() -> {
            equations.addEquation(new Equation("x^2", Color.valueOf(Color.BLUE)));
            equations.addEquation(new Equation("sinr(x)*2.4", Color.valueOf(Color.GREEN)));

            equationsUpdates.postValue(equations);
        });
    }

    public void saveSettings() {
        settings.save(getApplication());
    }

    public MutableLiveData<Settings> getSettingsUpdates() {
        return settingsUpdates;
    }

    public void setOnResetRangeListener(OnResetRangeListener listener) {
        resetRangeUpdates = listener;
    }

    public void resetRange() {
        if(resetRangeUpdates != null) {
            settings.setRange(new Range(Settings.DEFAULT_RANGE));
            resetRangeUpdates.onReset(settings.getRange());
            settingsUpdates.postValue(settings);
        }
    }

    public void setRange(Range range) {
        settings.setRange(range);
        settingsUpdates.postValue(settings);
    }

    public void setGraphSize(float width, float height) {
        settings.setGraphDimensions(width, height);
        settingsUpdates.postValue(settings);
    }

    public void setShowAxis(boolean showAxis) {
        settings.setShowAxis(showAxis);
        settingsUpdates.postValue(settings);
    }

    public void setShowGrid(boolean showGrid) {
        settings.setShowGrid(showGrid);
        settingsUpdates.postValue(settings);
    }

    public void setRatioLock(boolean ratioLock) {
        settings.setRatioLock(ratioLock);
        settingsUpdates.postValue(settings);
    }

    public LiveData<SystemOfEquations> getEquationsUpdates() {
        return equationsUpdates;
    }
}