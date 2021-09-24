package com.graphingcalculator.data;

import android.animation.Animator;
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

    private AppDataRepository appDataRepository;

    private Animator variablesAnimator;
    private Settings settings;

    private LiveData<List<expression>> expressionsUpdates;
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

    private SystemOfEquations parseSystemOfEquations(List<expression> sys) {
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
        expressionsUpdates = appDataRepository.getExpressionsUpdates();
        equationsUpdates = Transformations.map(expressionsUpdates, this::parseSystemOfEquations);
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

    public void addExpression(expression exp) {
        AsyncTask.execute(() -> {
            appDataRepository.addExpression(exp);
        });
    }

    public void updateExpression(expression exp) {
        AsyncTask.execute(() -> {
            appDataRepository.updateExpression(exp);
        });
    }

    public void changeExpression(expression expOld, expression expNew) {
        AsyncTask.execute(() -> {
            appDataRepository.changeExpression(expOld, expNew);
        });
    }

    public void removeExpression(expression exp) {
        AsyncTask.execute(() -> {
            appDataRepository.removeExpression(exp);
        });
    }

    public LiveData<List<expression>> getExpressionsUpdates() {
        return expressionsUpdates;
    }

    public LiveData<SystemOfEquations> getEquationsUpdates() {
        return equationsUpdates;
    }
}