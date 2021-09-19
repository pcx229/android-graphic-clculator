package com.graphingcalculator.graph;

public class Range {
    public double startX, endX, startY, endY;

    public Range(double startX, double endX, double startY, double endY) {
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
    }

    public double getWidth() {
        return endX - startX;
    }

    public double getHeight() {
        return endY - startY;
    }

    @Override
    public String toString() {
        return "Range{" + "startX=" + startX + ", endX=" + endX + ", startY=" + startY + ", endY=" + endY + '}';
    }
}