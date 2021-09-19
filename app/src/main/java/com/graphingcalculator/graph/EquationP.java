package com.graphingcalculator.graph;

import android.graphics.Color;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EquationP {
    private Expression equation;
    private Color color;

    public EquationP(Expression equation, Color color) {
        this.equation = equation;
        this.color = color;
    }

    public Expression getEquation() {
        return equation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Point> calculateRange(Range range, int n) {
        List<Point> points = new ArrayList<>();
        BigDecimal start = new BigDecimal(range.startX),
                stop = new BigDecimal(range.endX),
                step = new BigDecimal((range.endX - range.startX) / (n-1));
        while(points.size() < n-1) {
            equation.setVariable("x", start);
            points.add(new Point(start.doubleValue(), equation.eval().doubleValue()));
            start = start.add(step);
        }
        equation.setVariable("x", stop);
        points.add(new Point(start.doubleValue(), equation.eval().doubleValue()));
        return points;
    }
}