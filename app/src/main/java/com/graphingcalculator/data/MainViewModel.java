package com.graphingcalculator.data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Application;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.function;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.graph.Equation;
import com.graphingcalculator.graph.Range;
import com.graphingcalculator.graph.SystemOfEquations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MainViewModel extends AndroidViewModel {

    private AppDataRepository appDataRepository;

    private Settings settings;
    private List<expression> expressions;
    private SystemOfEquations equations;

    private ValueAnimator variablesAnimator;

    private MutableLiveData<List<expression>> expressionsUpdates = new MutableLiveData<>();
    private MutableLiveData<SystemOfEquations> equationsUpdates = new MutableLiveData<>();
    private MutableLiveData<Settings> settingsUpdates = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        appDataRepository = AppDataRepository.buildInstance(application);

        settings = Settings.load(getApplication());
        settingsUpdates.postValue(settings);

        AsyncTask.execute(() -> {
            expressions = appDataRepository.getExpressions();
            equations = parseSystemOfEquations(expressions);
            expressionsUpdates.postValue(expressions);
            equationsUpdates.postValue(equations);
        });

        variablesAnimator = ValueAnimator.ofFloat(0, 1);
        variablesAnimator.setDuration(100);
        variablesAnimator.setRepeatMode(ValueAnimator.RESTART);
        variablesAnimator.setRepeatCount(ValueAnimator.INFINITE);
        variablesAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                if(expressions != null) {
                    boolean changes = false;
                    for(expression exp : expressions) {
                        if(exp instanceof variable) {
                            variable o = (variable) exp;
                            if(o.isAnimated()) {
                                o.stepAnimation();
                                equations.setVariable(o.getName(), o.getValue());
                                changes = true;
                            }
                        }
                    }
                    if(changes) {
                        expressionsUpdates.postValue(expressions);
                        equationsUpdates.postValue(equations);
                    }
                }
            }
        });
        variablesAnimator.start();
    }

    // settings

    public void saveSettings() {
        settings.save(getApplication());
    }

    public MutableLiveData<Settings> getSettingsUpdates() {
        return settingsUpdates;
    }

    public Range resetRange() {
        Range range = new Range(Settings.DEFAULT_RANGE);
        settings.setRange(range);
        settingsUpdates.postValue(settings);
        return range;
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

    // expressions

    private void updateExpressions() {
        equations = parseSystemOfEquations(expressions);
        expressionsUpdates.postValue(expressions);
        equationsUpdates.postValue(equations);
    }

    public void saveExpressions() {
        AsyncTask.execute(() -> {
            appDataRepository.replaceExpressions(expressions);
        });
    }

    public int positionOfExpression(expression exp) {
        return expressions.indexOf(exp);
    }

    public void changeEquationColor(expression exp, Color color) {
        ((equation)exp).setColor(color);
        updateExpressions();
    }

    public void deleteExpression(expression exp) {
        expressions.remove(exp);
        updateExpressions();
    }

    public void changeEquationVisibility(expression exp, boolean visible) {
        ((equation)exp).setVisible(visible);
        updateExpressions();
    }

    public expression changeExpression(expression expOld, String text) {
        expression expNew = parseExpression(expOld, text);
        if(expNew != expOld) {
            int index = expressions.indexOf(expOld);
            expressions.set(index, expNew);
        }
        updateExpressions();
        return expNew;
    }

    public void changeVariableRange(expression exp, double start, double end) {
        ((variable)exp).setRange(start, end);
        updateExpressions();
    }

    public void changeVariableValueProgress(expression exp, double progress) {
        ((variable)exp).setValueProgress(progress);
    }

    public void changeVariableValueSave() {
        updateExpressions();
    }

    public void changeAnimateVariableStatus(expression exp, boolean isAnimated, double step, variable.ANIMATION_MODE mode) {
        ((variable)exp).setAnimation(isAnimated, step, mode);
        updateExpressions();
    }

    public void addVariablesAnimationListener(Animator.AnimatorListener listener) {
        variablesAnimator.addListener(listener);
    }

    public void removeVariablesAnimationListener(Animator.AnimatorListener listener) {
        variablesAnimator.removeListener(listener);
    }

    public void moveExpressionUp(expression a) {
        int index = expressions.indexOf(a);
        if(index > 0) {
            expressions.remove(index);
            expression b = expressions.remove(index-1);
            expressions.add(index-1, a);
            expressions.add(index, b);
            long temp = a.getIndex();
            a.setIndex(b.getIndex());
            b.setIndex(temp);
            updateExpressions();
        }
    }

    public void moveExpressionDown(expression a) {
        int index = expressions.indexOf(a);
        if(index < expressions.size()-1) {
            expressions.remove(index);
            expression b = expressions.remove(index);
            expressions.add(index, a);
            expressions.add(index, b);
            long temp = a.getIndex();
            a.setIndex(b.getIndex());
            b.setIndex(temp);
            updateExpressions();
        }
    }

    public expression addExpression(String text) {
        expression exp = parseExpression(null, text);
        expressions.add(0, exp);
        updateExpressions();
        return exp;
    }

    public LiveData<List<expression>> getExpressionsUpdates() {
        return expressionsUpdates;
    }

    public LiveData<SystemOfEquations> getEquationsUpdates() {
        return equationsUpdates;
    }

    // other

    private long getExpressionNextIndex() {
        return (expressions == null || expressions.size() == 0) ? 0 : expressions.get(0).getIndex()+1;
    }

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public boolean expressionsHaveTheSameType(expression e1, expression e2) {
        if(e1 instanceof equation && e2 instanceof equation) {
            return true;
        }
        if(e1 instanceof variable && e2 instanceof variable) {
            return true;
        }
        if(e1 instanceof function && e2 instanceof function) {
            return true;
        }
        return false;
    }

    public expression parseExpression(expression last, String pattern) {

        if(pattern.trim().equals("")) {
            throw new PatternSyntaxException("expression is empty", pattern , 0);
        }

        // function
        Pattern functionPattern = Pattern.compile("^[ ]*([a-zA-Z][a-zA-Z0-9]*)\\(((?:[ ]*[a-zA-Z][a-zA-Z0-9]*[ ]*,)*(?:[ ]*[a-zA-Z][a-zA-Z0-9]*[ ]*)?)\\)[ ]*=[ ]*(.+)[ ]*$");
        Matcher functionMatcher = functionPattern.matcher(pattern);
        if(functionMatcher.matches()) {
            String name = functionMatcher.group(1);
            String argumentsString = functionMatcher.group(2);
            List<String> arguments = new ArrayList<String>();
            for(String arg : argumentsString.split(",")) {
                arg = arg.trim();
                if(!arg.isEmpty()) {
                    arguments.add(arg);
                }
            }
            String body = functionMatcher.group(3);
            if(last instanceof function) {
                ((function) last).setBody(body);
                ((function) last).setName(name);
                ((function) last).setArguments(arguments);
                return last;
            } else {
                if(equations.hasFunction(name)) {
                    throw new IllegalArgumentException("function name already exist");
                }
                if(SystemOfEquations.parseFunction(name, arguments, body) == null) {
                    throw new IllegalArgumentException("invalid function");
                }
                function o = new function(name, arguments, body);
                if(last != null) {
                    o.setIndex(last.getIndex());
                } else {
                    o.setIndex(getExpressionNextIndex());
                }
                return o;
            }
        }

        // variable
        Pattern variablePattern = Pattern.compile("^[ ]*([a-zA-Z][a-zA-Z0-9]*)[ ]*=[ ]*([-]?\\d+(\\.\\d+)?)[ ]*$");
        Matcher variableMatcher = variablePattern.matcher(pattern);
        if(variableMatcher.matches()) {
            String name = variableMatcher.group(1);
            double value = Double.parseDouble(variableMatcher.group(2));
            if(last instanceof variable) {
                ((variable) last).setName(name);
                ((variable) last).setValue(value);
                return last;
            } else {
                double startValue = value - 10.0,
                        endValue = value + 10.0;
                if (equations.hasVariable(name)) {
                    throw new IllegalArgumentException("variable name already exist");
                }
                variable o = new variable(name, value, startValue, endValue);
                if(last != null) {
                    o.setIndex(last.getIndex());
                } else {
                    o.setIndex(getExpressionNextIndex());
                }
                return o;
            }
        }

        // equation
        Pattern equationPattern = Pattern.compile("(>=|<=|=|>|<)");
        Matcher equationMatcher = equationPattern.matcher(pattern);
        if(equationMatcher.find()) {
            String type = equationMatcher.group(1);
            String[] parts = pattern.split(type);
            if(parts.length == 2) {
                String text = parts[0].trim() + " " + type + " " + parts[1].trim();
                if(last instanceof equation) {
                    ((equation) last).setBody(text);
                    return last;
                } else {
                    Color color = Color.valueOf(getRandomColor());
                    equation o = new equation(text, color);
                    if(last != null) {
                        o.setIndex(last.getIndex());
                    } else {
                        o.setIndex(getExpressionNextIndex());
                    }
                    return o;
                }
            }
        }

        throw new PatternSyntaxException("syntax error", pattern , 0);
    }

    private SystemOfEquations parseSystemOfEquations(List<expression> sys) {
        SystemOfEquations equations = new SystemOfEquations();
        // variables
        for(expression i : sys) {
            if(i instanceof variable) {
                variable o = (variable) i;
                equations.addVariable(o.getName(), o.getValue());
            }
        }
        // functions
        for(expression i : sys) {
            if(i instanceof function) {
                function o = (function) i;
                equations.addFunction(o.getName(), o.getArguments(), o.getBody());
            }
        }
        // equations
        for(expression i : sys) {
            if(i instanceof equation) {
                equation o = (equation) i;
                if(o.isVisible()) {
                    equations.addEquation(new Equation(o.getBody(), o.getColor()));
                }
            }
        }
        return equations;
    }
}