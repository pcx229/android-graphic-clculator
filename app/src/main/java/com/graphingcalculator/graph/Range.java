package com.graphingcalculator.graph;

import java.util.Objects;

public class Range {
    public double startX, endX, startY, endY;

    public Range() {

    }

    public Range(Range copy) {
        set(copy.startX, copy.endX, copy.startY, copy.endY);
    }

    public Range(double startX, double endX, double startY, double endY) {
        set(startX, endX, startY, endY);
    }

    public void set(double startX, double endX, double startY, double endY) {
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

    public void setWidth(double width) {
        startX = getCenterX() - width/2;
        endX = startX + width;
    }

    public void setHeight(double height) {
        startY = getCenterY() - height/2;
        endY = startY + height;
    }

    public void setDimensions(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public double getCenterX() {
        return startX + getWidth()/2;
    }

    public double getCenterY() {
        return startY + getHeight()/2;
    }

    @Override
    public String toString() {
        return "Range{" + "startX=" + startX + ", endX=" + endX + ", startY=" + startY + ", endY=" + endY + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return Double.compare(range.startX, startX) == 0 &&
                Double.compare(range.endX, endX) == 0 &&
                Double.compare(range.startY, startY) == 0 &&
                Double.compare(range.endY, endY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startX, endX, startY, endY);
    }
}