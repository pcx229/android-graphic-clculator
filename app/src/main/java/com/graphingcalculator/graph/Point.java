package com.graphingcalculator.graph;

class Point {
    public double x, y;

    public Point() {

    }

    public Point(Point copy) {
        set(copy.x, copy.y);
    }

    public Point(double x, double y) {
        set(x, y);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}
