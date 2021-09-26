package com.graphingcalculator.graph;

import android.graphics.Color;

public class Equation {
    private String equation;
    private int color;

    public Equation(String equation) {
        this.equation = equation;
        this.color = getRandomColor().toArgb();
    }

    public Equation(String equation, Color color) {
        this.equation = equation;
        this.color = color.toArgb();
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public int getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color.toArgb();
    }

    private static Color getRandomColor() {
        return Color.valueOf((int)(Math.random() * 0x1000000));
    }
}
