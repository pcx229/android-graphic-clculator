package com.graphingcalculator;

import android.animation.Animator;
import android.graphics.Color;

import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.variable;

public interface ExpressionOptionsChangesListener {

    void changeEquationColor(expression exp, Color color);

    void deleteExpression(expression exp);

    void changeEquationVisibility(expression exp, boolean visible);

    void changeExpression(expression exp, String text);

    void changeVariableRange(expression exp, double start, double end);

    void changeVariableValueProgress(expression exp, double progress);

    void changeVariableValueSave(expression exp);

    void changeAnimateVariableStatus(expression exp, boolean isAnimated, double step, variable.ANIMATION_MODE mode);

    void addVariablesAnimationListener(Animator.AnimatorListener listener);

    void removeVariablesAnimationListener(Animator.AnimatorListener listener);
}
