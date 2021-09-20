package com.graphingcalculator.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.graphingcalculator.R;

import java.util.Map;

/**
 * default implementation:
 *
 *     <com.graphingcalculator.graph.MathGraph
 *         custom:scalable="true"
 *         custom:movable="true"
 *         custom:showNumbers="true"
 *         custom:showAxis="true"
 *         custom:axisColor="Color.LTGRAY"
 *         custom:showGrid="true"
 *         custom:gridColor="Color.BLACK"
 *         custom:gridBlockSize="10dp"
 *         custom:dataPointPerEquation="100"
 *         android:colorBackground="Color.WHITE"
 *         android:fontFamily="null"
 *         android:textSize="5dp"
 *         android:textColor="Colors.black"
 *         android:padding="0sp"
 *         />
 *
 *      require to call setRange after view loaded.
 */

public class MathGraph extends View {

    private boolean isScalable, isMovable, showNumbers, showAxis, showGrid;
    private int axisColor;
    public final static int DEFAULT_AXIS_COLOR = Color.BLACK;
    private int gridColor;
    public final static int DEFAULT_GRID_COLOR = Color.LTGRAY;
    private int gridBlockSize;
    public final static float DEFAULT_BLOCK_SIZE_DP = 50f;
    private int axisAndGridLineWidth;
    public final static float DEFAULT_AXIS_GRID_LINE_WIDTH_DP = 2f;
    private int functionLineWidth;
    public final static float DEFAULT_FUNCTION_LINE_WIDTH_DP = 5f;
    private int fontSize;
    public final static float DEFAULT_FONT_SIZE_SP = 20f;
    private int fontColor;
    public final static int DEFAULT_FONT_COLOR = Color.BLACK;
    private Typeface fontFamily;
    private int backgroundColor;
    public final static int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private int dataPointPerEquation;
    public final static int DEFAULT_DATA_POINTS_PER_EQUATION = 100;

    private double scaleX, scaleY, centerX, centerY;
    private int width, height;
    private boolean initializedViewSize;

    public enum FIT_RANGE {X_FIT_Y, Y_FIT_X, BOTH};

    private ScaleGestureDetector scaleDetector;

    private GraphDragListener dragDetector;

    private SystemOfEquations equations;

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();

    private OnRangeChangesListener rangeChangeListener;

    private OnGraphSizeChangesListener sizeChangeListener;

    public MathGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        // arguments
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MathGraph, 0, 0);
        try {
            isScalable = a.getBoolean(R.styleable.MathGraph_scalable, true);
            isMovable = a.getBoolean(R.styleable.MathGraph_movable, true);
            showNumbers = a.getBoolean(R.styleable.MathGraph_showNumbers, true);
            showAxis = a.getBoolean(R.styleable.MathGraph_showAxis, true);
            axisColor = a.getColor(R.styleable.MathGraph_axisColor, DEFAULT_AXIS_COLOR);
            showGrid = a.getBoolean(R.styleable.MathGraph_showGrid, true);
            gridColor = a.getColor(R.styleable.MathGraph_gridColor, DEFAULT_GRID_COLOR);
            gridBlockSize = a.getDimensionPixelSize(R.styleable.MathGraph_gridBlockSize, (int) (getResources().getDisplayMetrics().density *  DEFAULT_BLOCK_SIZE_DP));
            dataPointPerEquation = a.getInteger(R.styleable.MathGraph_dataPointPerEquation, DEFAULT_DATA_POINTS_PER_EQUATION);

            backgroundColor = a.getColor(R.styleable.MathGraph_android_textColor, DEFAULT_BACKGROUND_COLOR);
            fontFamily = a.getFont(R.styleable.MathGraph_android_fontFamily);
            fontSize = a.getDimensionPixelSize(R.styleable.MathGraph_android_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_FONT_SIZE_SP, getResources().getDisplayMetrics()));
            fontColor = a.getColor(R.styleable.MathGraph_android_textColor, DEFAULT_FONT_COLOR);
        } finally {
            a.recycle();
        }
        axisAndGridLineWidth =  (int) (getResources().getDisplayMetrics().density *  DEFAULT_AXIS_GRID_LINE_WIDTH_DP);
        functionLineWidth =  (int) (getResources().getDisplayMetrics().density *  DEFAULT_FUNCTION_LINE_WIDTH_DP);

        // listen to changes in container size
        initializedViewSize = false;
        addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            width = getWidth();
            height = getHeight();
            if(sizeChangeListener != null) {
                sizeChangeListener.onChange(width, height);
            }
            initializedViewSize = true;
            invalidate();
        });

        // scroll and drag events listeners
        scaleDetector = new ScaleGestureDetector(context, new GraphScaleListener());
        dragDetector = new GraphDragListener();
    }

    public void setRangeByCenterPoint(float centerX, float centerY, float scaleX, float scaleY) {
        if(!initializedViewSize) {
            post(() -> {
                setRangeByCenterPoint(centerX, centerY, scaleX, scaleY);
            });
            return;
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        invalidate();
    }

    public void setRangeByCenterPoint(float centerX, float centerY, float scale) {
        setRangeByCenterPoint(centerX, centerY, scale, scale);
    }

    public void setRangeByStartEnd(float startX, float endX, float startY, float endY) {
        if(!initializedViewSize) {
            post(() -> {
                setRangeByStartEnd(startX, endX, startY, endY);
            });
            return;
        }
        float numbersXRange = endX-startX,
                numbersYRange = endY-startY;
        float centerXRange = startX+numbersXRange/2,
                centerYRange = startY+numbersYRange/2;
        float scaleXRange = numbersXRange/width,
                scaleYRange = numbersYRange/height;
        setRangeByCenterPoint(centerXRange, centerYRange, scaleXRange, scaleYRange);
    }

    public void setRangeByStartEnd(float start, float end, FIT_RANGE fit) {
        if(!initializedViewSize) {
            FIT_RANGE finalFit = fit;
            post(() -> {
                setRangeByStartEnd(start, end, finalFit);
            });
            return;
        }
        float startX = start,
                endX = end,
                startY = start,
                endY = end;
        float dy = (float)width/height,
                dx = (float)height/width;
        if(fit == FIT_RANGE.BOTH) {
            if(width > height) {
                fit = FIT_RANGE.Y_FIT_X;
            } else {
                fit = FIT_RANGE.X_FIT_Y;
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
        setRangeByStartEnd(startX, endX, startY, endY);
    }

    public Range getRangeStartEnd() {
        double numbersXRange = width*scaleX,
                numbersYRange = height*scaleY;
        double startXRange = centerX-(numbersXRange/2),
                startYRange = centerY-(numbersYRange/2);
        return new Range(startXRange, startXRange+numbersXRange, startYRange, startYRange+numbersYRange);
    }

    public boolean isScalable() {
        return isScalable;
    }

    public void setScalable(boolean scalable) {
        isScalable = scalable;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void setMovable(boolean movable) {
        isMovable = movable;
    }

    public boolean isShowingNumbers() {
        return showNumbers;
    }

    public void setShowingNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
        invalidate();
    }

    public boolean isShowingAxis() {
        return showAxis;
    }

    public void setShowingAxis(boolean showAxis) {
        this.showAxis = showAxis;
        invalidate();
    }

    public boolean isShowingGrid() {
        return showGrid;
    }

    public void setShowingGrid(boolean showGrid) {
        this.showGrid = showGrid;
        invalidate();
    }

    public int getAxisColor() {
        return axisColor;
    }

    public void setAxisColor(int axisColor) {
        this.axisColor = axisColor;
        invalidate();
    }

    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(int gridColor) {
        this.gridColor = gridColor;
        invalidate();
    }

    public int getGridBlockSize() {
        return gridBlockSize;
    }

    public void seGridBlockSize(int gridBlockSize) {
        this.gridBlockSize = gridBlockSize;
        invalidate();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        invalidate();
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        invalidate();
    }

    public Typeface getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(Typeface fontFamily) {
        this.fontFamily = fontFamily;
        invalidate();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public int getDataPointPerEquation() {
        return dataPointPerEquation;
    }

    public void setDataPointPerEquation(int dataPointPerEquation) {
        this.dataPointPerEquation = dataPointPerEquation;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if(!scaleDetector.isInProgress()) {
            dragDetector.onTouch(this, event);
        }
        return true;
    }

    private class GraphDragListener implements OnTouchListener {
        private float mLastTouchX, mLastTouchY, mPosX, mPosY;
        private boolean isDown = false;
        private double savedCenterX, savedCenterY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    isDown = true;
                    savedCenterX = centerX;
                    savedCenterY = centerY;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(isDown) {
                        mPosX = event.getX() - mLastTouchX;
                        mPosY = event.getY() - mLastTouchY;
                        move(savedCenterX, savedCenterY, mPosX, mPosY);
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

    private class GraphScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector event) {
            scale(event.getFocusX(), event.getFocusY(), event.getScaleFactor(), event.getScaleFactor());
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

    public void setEquations(SystemOfEquations equations) {
        this.equations = equations;
        invalidate();
    }

    public SystemOfEquations getEquations() {
        return equations;
    }

    private void scale(float focusScreenX, float focusScreenY, float scaleFactorX, float scaleFactorY) {
        if(!isScalable) {
            return;
        }
        scaleX /= scaleFactorX;
        scaleY /= scaleFactorY;
        if(isMovable) {
            centerX += (focusScreenX - width/2f) * scaleX * (1/scaleFactorX-1);
            centerY += (focusScreenY - height/2f) * scaleY * (1/scaleFactorY-1);
        }
    }

    private void move(double fromCenterX, double fromCenterY, float amountScreenX, float amountScreenY) {
        if(!isMovable) {
            return;
        }
        centerX = fromCenterX - amountScreenX * scaleX;
        centerY = fromCenterY + amountScreenY * scaleY;
    }

    public interface OnRangeChangesListener {
        void onChange(Range range);
    }

    public void setOnRangeChangesListener(OnRangeChangesListener listener) {
        rangeChangeListener = listener;
        if(initializedViewSize) {
            listener.onChange(getRangeStartEnd());
        }
    }

    public interface OnGraphSizeChangesListener {
        void onChange(float width, float height);
    }

    public void setOnGraphSizeChangesListener(OnGraphSizeChangesListener listener) {
        sizeChangeListener = listener;
        if(initializedViewSize) {
            listener.onChange(width, height);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(rangeChangeListener != null) {
            rangeChangeListener.onChange(getRangeStartEnd());
        }
    }

    private Point mapGraphPointToScreen(Point point) {
        point.x = - (centerX - point.x) / scaleX + width/2f;
        point.y = (centerY - point.y) / scaleY + height/2f;
        return point;
    }

    private Point mapScreenPointToGraph(Point point) {
        point.x = centerX + (point.x - width/2f) * scaleX;
        point.y = centerY - (point.y - height/2f) * scaleY;
        return point;
    }

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(!initializedViewSize) {
            return;
        }

        // paint background
        paint.reset();
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, width, height, paint);

        // grid
        double gridStepX = Math.pow(2, Math.ceil(Math.log(scaleX*gridBlockSize) / Math.log(2))),
                gridStepY = Math.pow(2, Math.ceil(Math.log(scaleY*gridBlockSize) / Math.log(2)));
        double gridStepXPixels = gridStepX / scaleX,
                gridStepYPixels = gridStepY / scaleY;

        // grid lines
        if(showGrid) {
            paint.reset();
            paint.setColor(gridColor);
            paint.setStrokeWidth(axisAndGridLineWidth);
            paint.setStyle(Paint.Style.STROKE);
            Point g = new Point(0, 0);
            mapScreenPointToGraph(g);
            g.x = Math.floor(g.x/gridStepX)*gridStepX;
            g.y = Math.floor(g.y/gridStepY)*gridStepY;
            mapGraphPointToScreen(g);
            for(double i=g.x; i < width; i+=gridStepXPixels) {
                canvas.drawLine((float)i, 0, (float)i, height, paint);
            }
            for(double i=g.y; i < height; i+=gridStepYPixels) {
                canvas.drawLine(0, (float)i, width, (float)i, paint);
            }
        }

        // equations
        paint.reset();
        paint.setStrokeWidth(functionLineWidth);
        paint.setStyle(Paint.Style.STROKE);
        if(equations != null) {
            Range range = getRangeStartEnd();
            for(Map.Entry<Equation, Point[]> entry : equations.calculateRange(range, dataPointPerEquation).entrySet()) {
                Equation eq = entry.getKey();
                Point[] points = entry.getValue();
                paint.setColor(eq.getColor().toArgb());
                Path path = null;
                for(Point p : points) {
                    Point converted = new Point(p.x, p.y);
                    mapGraphPointToScreen(converted);
                    if(path == null) {
                        path = new Path();
                        path.moveTo((float)converted.x, (float)converted.y);
                    } else {
                        path.lineTo((float)converted.x, (float)converted.y);
                        path.moveTo((float)converted.x, (float)converted.y);
                    }
                }
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        // axis
        if(showAxis) {
            paint.reset();
            paint.setStrokeWidth(axisAndGridLineWidth);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(axisColor);
            Point axis = new Point(0, 0);
            mapGraphPointToScreen(axis);
            canvas.drawLine((float)axis.x, 0, (float)axis.x, height, paint);
            float[] arrowTop = new float[]{ (float)axis.x-15, 0+20, (float)axis.x, 0+2, (float)axis.x, 0+2, (float)axis.x+15, 0+20 };
            paint.setStrokeWidth(axisAndGridLineWidth*1.5f);
            canvas.drawLines(arrowTop, paint);
            paint.setStrokeWidth(axisAndGridLineWidth);
            canvas.drawLine(0,(float)axis.y, width, (float)axis.y, paint);
            float[] arrowBottom = new float[]{ width-20, (float)axis.y+15, width-2, (float)axis.y, width-2, (float)axis.y, width-20, (float)axis.y-15 };
            paint.setStrokeWidth(axisAndGridLineWidth*1.5f);
            canvas.drawLines(arrowBottom, paint);
        }

        // grid numbers
        if(showAxis && showNumbers) {
            paint.reset();
            paint.setStrokeWidth(axisAndGridLineWidth);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(axisColor);
            textPaint.setFakeBoldText(true);
            textPaint.setTypeface(fontFamily);
            textPaint.setTextSize(fontSize);
            textPaint.setColor(fontColor);
            textPaint.setAntiAlias(true);
            double leftX = Math.floor((centerX-width/2f*scaleX)/gridStepX)*gridStepX,
                    bottomY = Math.floor((centerY-height/2f*scaleY)/gridStepY)*gridStepY;
            Point x = new Point(leftX, 0),
                    y = new Point(0, bottomY);
            mapGraphPointToScreen(x);
            mapGraphPointToScreen(y);
            if(x.y > height) {
                x.y = height;
            } else if(x.y < 0) {
                x.y = 0;
            }
            if(y.x > width) {
                y.x = width;
            } else if(y.x < 0) {
                y.x = 0;
            }
            Rect numberTextSize = new Rect();
            for(double i=x.x, j=leftX; i < width; i+=gridStepXPixels, j+=gridStepX) {
                canvas.drawLine((float)i, (float)x.y+10, (float)i, (float)x.y-10, paint);
                double n = (int)(j*100)/100.0;
                String numberText = (Math.abs(n - (int)n) < 1E-4) ? Integer.toString((int)n) : Double.toString(n);
                textPaint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
                canvas.drawText(numberText, (float) (((n == 0) ? 30 : 0) + i-numberTextSize.width()/2.0), (float) (x.y+((x.y <= height/2.0) ? numberTextSize.height()+35.0 : -35.0)), textPaint);
            }
            for(double i=y.y, j=bottomY; i > 0; i-=gridStepYPixels, j+=gridStepY) {
                canvas.drawLine((float)y.x+10, (float)i, (float)y.x-10, (float)i, paint);
                double n = (int)(j*100)/100.0;
                String numberText = (Math.abs(n - (int)n) < 1E-4) ? Integer.toString((int)n) : Double.toString(n);
                textPaint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
                canvas.drawText((n == 0) ? "" : numberText, (float) (y.x+((y.x <= width/2.0) ? 35.0 : -numberTextSize.width()-35.0)), (float) (i+numberTextSize.height()/2.0), textPaint);
            }
        }

    }
}
