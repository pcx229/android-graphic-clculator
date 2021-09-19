package com.graphingcalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MathGraphEx extends View {

    public static final int MAX_FUNCTION_DRAW_POINTS = 100;
    public static final Range START_RANGE = new Range(-5, 5);

    public static class Range {
        public double startX, endX, startY, endY;

        public Range(double startX, double endX, double startY, double endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
        }

        public Range(double start, double end) {
            this.startX = start;
            this.endX = end;
            this.startY = start;
            this.endY = end;
        }

        public Range copy() {
            return new Range(startX, endX, startY, endY);
        }

        public enum FIT {X_FIT_Y, Y_FIT_X, BOTH};

        public Range fit(int width, int height, FIT fit) {
            double startX = this.startX,
                    endX = this.endX,
                    startY = this.startY,
                    endY = this.endY;
            double dy = (double)width/height,
                    dx = (double)height/width;
            if(fit == FIT.BOTH) {
                if(width > height) {
                    fit = FIT.Y_FIT_X;
                } else {
                    fit = FIT.X_FIT_Y;
                }
            }
            switch (fit) {
                case X_FIT_Y:
                    startY = startX * dx;
                    endY = endX * dx;
                    break;
                case Y_FIT_X:
                    startX = startY * dy;
                    endX = endY * dy;
                    break;
            }
            return new Range(startX, endX, startY, endY);
        }

        public Range move(double x, double y) {
            return new Range(startX-x, endX-x, startY+y, endY+y);
        }

        public Range scale(int width, int height, float focusX, float focusY, double scale) {
            double currentW = endX - startX,
                    currentH = endY - startY;
            double newW = currentW / scale,
                    newH = currentH / scale;
            double currentX = startX + focusX/width * currentW,
                    currentY = startY + (height-focusY)/height * currentH;
            double newOnScreenX = (currentX - startX) / newW * width,
                    newOnScreenY = (currentY - startY) / newH * height;
            double diffX = (newOnScreenX - focusX)/width * newW,
                    diffY = (newOnScreenY - (height-focusY))/height * newH;
            return new Range(startX+diffX, startX+newW+diffX, startY+diffY, startY+newH+diffY);
        }

        @Override
        public String toString() {
            return "Range[" + "startX=" + startX + ", endX=" + endX + ", startY=" + startY + ", endY=" + endY + "]";
        }
    }

    private Range range;

    public static class Equation {
        private Expression equation;
        private Color color;

        public Equation(Expression equation, Color color) {
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

        public BigDecimal calculate(BigDecimal x) {
            equation.setVariable("x", x);
            return equation.eval();
        }

        private static class Point {
            public double x, y;
            public Point(double x, double y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public String toString() {
                return "Point[" + "x=" + x + ", y=" + y + "]";
            }
        }

        public List<Point> calculateRange(Range range, int n) {
            List<Point> points = new ArrayList<Point>();
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

    private List<Equation> equations;

    private int height, width;

    public MathGraphEx(Context context, AttributeSet attrs) {
        super(context, attrs);

        addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            range = START_RANGE.fit(getWidth(), getHeight(), Range.FIT.BOTH);
            height = getHeight();
            width = getWidth();
            updateRangeDelta();
        });
        equations = new ArrayList<Equation>();
        equations.add(new Equation(new Expression("x^2"), Color.valueOf(Color.RED)));
        equations.add(new Equation(new Expression("sinr(x)*2.4"), Color.valueOf(Color.BLUE)));

        scaleDetector = new ScaleGestureDetector(context, new GraphScaleListener());
        dragDetector = new GraphDragListener();
    }

    private double deltaX, deltaY;

    private void updateRangeDelta() {
        deltaX = width / (range.endX - range.startX);
        deltaY = height / (range.endY - range.startY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if(!scaleDetector.isInProgress()) {
            dragDetector.onTouch(this, event);
        }
        return true;
    }

    private GraphDragListener dragDetector;

    private class GraphDragListener implements OnTouchListener {
        private float mLastTouchX, mLastTouchY, mPosX, mPosY;
        private boolean isDown = false;
        private Range savedRange;

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    isDown = true;
                    savedRange = range.copy();;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(isDown) {
                        mPosX = event.getX() - mLastTouchX;
                        mPosY = event.getY() - mLastTouchY;
                        range = savedRange.move(mPosX/deltaX, mPosY/deltaY);
                        invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_POINTER_UP:
                    isDown = false;
                    break;
            }
            return true;
        }
    }

    private ScaleGestureDetector scaleDetector;

    private class GraphScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector event) {
            range = range.scale(width, height, event.getFocusX(), event.getFocusY(), event.getScaleFactor());
            updateRangeDelta();
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector event) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector event) {

        }
    }

    public void setRange(Range r) {
        range = r;
    }

    public Range getRange() {
        return range;
    }

    public void addEquation(Equation eq) {
        equations.add(eq);
    }

    public void removeEquation(Equation eq) {
        equations.remove(eq);
    }

    public List<Equation> getEquations() {
        return equations;
    }

    private int basePixelPerNumber = 125;
    private Paint paint = new Paint();

    private Point convert(double x, double y) {
        x = (x-range.startX)*deltaX;
        y = height - (y-range.startY)*deltaY;
        return new Point((int)x, (int)y);
    }

    protected void onDraw(Canvas canvas) {

        // draw grid
        paint.reset();
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(3.f);
        paint.setStyle(Paint.Style.STROKE);
        double pixelPerNumberX = width / (range.endX - range.startX),
                pixelPerNumberY = height / (range.endY - range.startY);
        double pixelPerNumberXRatio = (range.endX - range.startX)/(width/basePixelPerNumber),
                pixelPerNumberYRatio = (range.endY - range.startY)/(height/basePixelPerNumber);
        double gridStepX = Math.pow(2, Math.round(Math.log(pixelPerNumberXRatio) / Math.log(2))),
                gridStepY = Math.pow(2, Math.round(Math.log(pixelPerNumberYRatio) / Math.log(2)));
        double gridStepI;
        gridStepI = Math.ceil(range.startX / gridStepX) * gridStepX;
        while(gridStepI < range.endX) {
            Point top = convert(gridStepI, range.endY),
                    bottom = convert(gridStepI, range.startY);
            canvas.drawLine(top.x, top.y, bottom.x, bottom.y, paint);
            gridStepI += gridStepX;
        }
        gridStepI = Math.ceil(range.startY / gridStepY) * gridStepY;
        while(gridStepI < range.endY) {
            Point left = convert(range.startX, gridStepI),
                    right = convert(range.endX, gridStepI);
            canvas.drawLine(left.x, left.y, right.x, right.y, paint);
            gridStepI += gridStepY;
        }

        // draw bases
        paint.reset();
        paint.setStrokeWidth(3.f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        if(range.startX <= 0 && range.endX >= 0) {
            Point top = convert(0, range.endY),
                    bottom = convert(0, range.startY);
            canvas.drawLine(top.x, top.y, bottom.x, bottom.y, paint);
            // arrows
            paint.setStrokeWidth(5.f);
            canvas.drawLines(new float[]{ top.x-15, top.y+20, top.x, top.y+2, top.x, top.y+2, top.x+15, top.y+20 }, paint);
        }
        paint.setStrokeWidth(3.f);
        if(range.startY <= 0 && range.endY >= 0) {
            Point left = convert(range.startX, 0),
                    right = convert(range.endX, 0);
            canvas.drawLine(left.x, left.y, right.x, right.y, paint);
            // arrows
            paint.setStrokeWidth(5.f);
            canvas.drawLines(new float[]{ right.x-20, right.y+15, right.x-2, right.y, right.x-2, right.y, right.x-20, right.y-15 }, paint);
        }

        // draw equations
        paint.reset();
        paint.setStrokeWidth(10.f);
        paint.setStyle(Paint.Style.STROKE);
        for(Equation eq : equations) {
            paint.setColor(eq.getColor().toArgb());
            Path path = null;
            for(Equation.Point p : eq.calculateRange(range, MAX_FUNCTION_DRAW_POINTS)) {
                Point converted = convert(p.x, p.y);
                if(path == null) {
                    path = new Path();
                    path.moveTo(converted.x, converted.y);
                } else {
                    path.lineTo(converted.x, converted.y);
                    path.moveTo(converted.x, converted.y);
                }
            }
            path.close();
            canvas.drawPath(path, paint);
        }

        // numbers
        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        Rect numberTextSize = new Rect();
        List<Double> numbersLabelsX = new ArrayList<Double>();
        gridStepI = Math.ceil(range.startX / gridStepX) * gridStepX;
        while(gridStepI < range.endX) {
            numbersLabelsX.add(gridStepI);
            gridStepI += gridStepX;
        }
        for(double x : numbersLabelsX) {
            Point converted = convert(x, 0);
            if(converted.y > height) {
                converted.y = height;
            } else if(converted.y < 0) {
                converted.y = 0;
            }
            paint.setStrokeWidth(5.f);
            canvas.drawLine(converted.x, converted.y+10, converted.x, converted.y-10, paint);
            paint.setStrokeWidth(3.f);
            paint.setTextSize(35.f);
            String numberText = Double.toString((int)(x*100)/100.0);
            paint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
            canvas.drawText(numberText, (float) (converted.x-numberTextSize.width()/2.0), (float) (converted.y+((converted.y <= height/2.0) ? numberTextSize.height()+35.0 : -35.0)), paint);
        }
        List<Double> numbersLabelsY = new ArrayList<Double>();
        gridStepI = Math.ceil(range.startY / gridStepY) * gridStepY;
        while(gridStepI < range.endY) {
            if(gridStepI != 0) {
                numbersLabelsY.add(gridStepI);
            }
            gridStepI += gridStepY;
        }
        for(double y : numbersLabelsY) {
            Point converted = convert(0, y);
            if(converted.x > width) {
                converted.x = width;
            } else if(converted.x < 0) {
                converted.x = 0;
            }
            paint.setStrokeWidth(5.f);
            canvas.drawLine(converted.x+10, converted.y, converted.x-10, converted.y, paint);
            paint.setStrokeWidth(3.f);
            paint.setTextSize(35.f);
            String numberText = Double.toString((int)(y*100)/100.0);
            paint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
            canvas.drawText(numberText, (float) (converted.x+((converted.x <= width/2.0) ? 35.0 : -numberTextSize.width()-35.0)), (float) (converted.y+numberTextSize.height()/2.0), paint);
        }
    }
}