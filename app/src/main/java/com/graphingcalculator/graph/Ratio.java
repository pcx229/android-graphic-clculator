package com.graphingcalculator.graph;

public class Ratio {
    private double xRatio, yRatio;
    private float width, height;
    private double xRangeWidth, yRangeHeight;

    public Ratio(float width, float height, double xRangeWidth, double yRangeHeight) {
        this.width = width;
        this.height = height;
        this.xRangeWidth = xRangeWidth;
        this.yRangeHeight = yRangeHeight;
        calculateByScreenSize();
    }

    private void calculateByScreenSize() {
        double wRatio = width / xRangeWidth,
                hRatio = height / yRangeHeight;
        if(hRatio < wRatio) {
            xRatio = 1;
            yRatio = wRatio / hRatio;
        } else {
            yRatio = 1;
            xRatio = hRatio / wRatio;
        }
    }

    public void setXRatio(double ratio) {
        xRangeWidth *= ratio / xRatio;
        calculateByScreenSize();
    }

    public void setYRatio(double ratio) {
        yRangeHeight *= ratio / yRatio;
        calculateByScreenSize();
    }

    public double getXRatio() {
        return xRatio;
    }

    public double getYRatio() {
        return yRatio;
    }

    public double getXRangeWidth() {
        return xRangeWidth;
    }

    public double getYRangeHeight() {
        return yRangeHeight;
    }

    public void setXRangeWidth(double xRangeWidth, boolean lockRatio) {
        if(lockRatio) {
            yRangeHeight *= xRangeWidth / this.xRangeWidth;
        }
        this.xRangeWidth = xRangeWidth;
        if(!lockRatio) {
            calculateByScreenSize();
        }
    }

    public void setYRangeHeight(double yRangeHeight, boolean lockRatio) {
        if(lockRatio) {
            xRangeWidth *= yRangeHeight / this.yRangeHeight;
        }
        this.yRangeHeight = yRangeHeight;
        if(!lockRatio) {
            calculateByScreenSize();
        }
    }

    @Override
    public String toString() {
        return "Ratio{" +
                "xRatio=" + xRatio +
                ", yRatio=" + yRatio +
                ", width=" + width +
                ", height=" + height +
                ", xRangeWidth=" + xRangeWidth +
                ", yRangeHeight=" + yRangeHeight +
                '}';
    }
}
