package com.graphingcalculator.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.function;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.graph.Equation;
import com.graphingcalculator.graph.Function;
import com.graphingcalculator.graph.Range;
import com.graphingcalculator.graph.SystemOfEquations;
import com.graphingcalculator.graph.Variable;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private Settings settings;
    private AppDataRepository appDataRepository;

    private LiveData<List<expression>> equationsEditDataUpdates;
    private LiveData<SystemOfEquations> equationsUpdates;
    private MutableLiveData<Settings> settingsUpdates = new MutableLiveData<>();

    public interface OnResetRangeListener {
        void onReset(Range range);
    }
    private OnResetRangeListener resetRangeUpdates;

    public MainViewModel(@NonNull Application application) {
        super(application);
        appDataRepository = AppDataRepository.buildInstance(application);
    }

    private SystemOfEquations parseSystemOfEquationsFromDatabase(List<expression> sys) {
        SystemOfEquations equations = new SystemOfEquations();
        for(expression i : sys) {
            if(i instanceof equation) {
                equation o = (equation) i;
                if(o.isVisible()) {
                    equations.addEquation(new Equation(o.getBody(), o.getColor()));
                }
            } else if(i instanceof variable) {
                variable o = (variable) i;
                equations.addVariable(new Variable(o.getName(), o.getValue()));
            } else if(i instanceof function) {
                function o = (function) i;
                equations.addFunction(new Function(o.getName(), o.getArguments(), o.getBody()));
            }
        }
        return equations;
    }

    public void init() {
        settings = Settings.load(getApplication());
        settingsUpdates.postValue(settings);

        equationsEditDataUpdates = appDataRepository.getSystemOfEquationsUpdates();
        equationsUpdates = Transformations.map(equationsEditDataUpdates, this::parseSystemOfEquationsFromDatabase);
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

    public void changeEquationsEditData(List<expression> sys) {
        AsyncTask.execute(() -> {
            appDataRepository.updateSystemOfEquations(sys);
        });
    }

    public LiveData<List<expression>> getEquationsEditDataUpdates() {
        return equationsEditDataUpdates;
    }

    public LiveData<SystemOfEquations> getEquationsUpdates() {
        return equationsUpdates;
    }
}