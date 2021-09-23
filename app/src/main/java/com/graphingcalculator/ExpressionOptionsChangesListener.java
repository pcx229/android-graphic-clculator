package com.graphingcalculator;

import android.graphics.Color;

import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.variable;

public interface ExpressionOptionsChangesListener {

    void changeEquationColor(expression exp, Color color);

    void removeExpression(expression exp);

    void changeEquationVisibility(expression exp, boolean visible);

    void changeExpression(expression exp, String toString);

    void changeVariableRange(expression exp, double start, double end);

    void changeVariableValue(expression exp, double x);

    void changeAnimateVariableStatus(expression exp, boolean isAnimated, double step, variable.ANIMATION_MODE mode);
}
