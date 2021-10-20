package com.graphingcalculator.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.graphingcalculator.R;

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
    private float gridBlockSize;
    public final static float DEFAULT_BLOCK_SIZE_DP = 100f;
    public final static float DEFAULT_AXIS_GRID_LINE_WIDTH_DP = 3f;
    private float axisAndGridLineWidth;
    public final static float DEFAULT_FUNCTION_LINE_WIDTH_DP = 10f;
    private float functionLineWidth;
    private float fontSize;
    public final static float DEFAULT_FONT_SIZE_SP = 40f;
    private int fontColor;
    public final static int DEFAULT_FONT_COLOR = Color.BLACK;
    private Typeface fontFamily;
    private int backgroundColor;
    public final static int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final double MAX_RANGE_UNTIL_CIRCLE_HIGHLIGHT = 1000.0,
            MIN_RANGE_UNTIL_CIRCLE_HIGHLIGHT = 0.01;
    public final static int CENTER_POINT_HIGHLIGHT_CIRCLE_SIZE_DP = 3;
    private int centerPointHighlightCircleSize;
    public final static float AXIS_DIRECTION_ARROW_SIZE_DP = 30f;
    private float axisDirectionArrowSize;

    private double scaleX, scaleY, centerX, centerY;
    private Range range = new Range();
    private int width, height;
    private boolean initializedViewSize;

    public enum FIT_RANGE {X_FIT_Y, Y_FIT_X, BOTH};

    private ScaleGestureDetector scaleDetector;

    private GraphDragListener dragDetector;

    private SystemOfEquations equations;

    private OnRangeChangesListener rangeChangeListener;

    private OnGraphSizeChangesListener sizeChangeListener;

    private float density;

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
            gridBlockSize = a.getDimension(R.styleable.MathGraph_gridBlockSize, DEFAULT_BLOCK_SIZE_DP);

            backgroundColor = a.getColor(R.styleable.MathGraph_android_colorBackground, DEFAULT_BACKGROUND_COLOR);
            fontFamily = a.getFont(R.styleable.MathGraph_android_fontFamily);
            fontSize = a.getDimension(R.styleable.MathGraph_android_textSize, DEFAULT_FONT_SIZE_SP);
            fontColor = a.getColor(R.styleable.MathGraph_android_textColor, DEFAULT_FONT_COLOR);
        } finally {
            a.recycle();
        }
        axisAndGridLineWidth =  DEFAULT_AXIS_GRID_LINE_WIDTH_DP;
        functionLineWidth =  DEFAULT_FUNCTION_LINE_WIDTH_DP;
        centerPointHighlightCircleSize = CENTER_POINT_HIGHLIGHT_CIRCLE_SIZE_DP;
        axisDirectionArrowSize = AXIS_DIRECTION_ARROW_SIZE_DP;

        // listen to changes in container size
        initializedViewSize = false;

        // scroll and drag events listeners
        scaleDetector = new ScaleGestureDetector(context, new GraphScaleListener());
        dragDetector = new GraphDragListener();

        // others
        density = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        this.width = width;
        this.height = height;
        if(width == 0 && height == 0) {
            return;
        }
        if(sizeChangeListener != null) {
            sizeChangeListener.onChange(width, height);
        }
        initializedViewSize = true;
        invalidate();
    }

    private void updateRangeStartEnd() {
        double dx = (width*scaleX)/2,
                dy = (height*scaleY)/2;
        range.set(centerX-dx, centerX+dx, centerY-dy, centerY+dy);
    }

    public Range getRangeStartEnd() {
        return new Range(range);
    }

    public void setRangeByCenterPoint(float centerX, float centerY, float scaleX, float scaleY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        updateRangeStartEnd();
        invalidate();
    }

    public void setRangeByCenterPoint(float centerX, float centerY, float scale) {
        setRangeByCenterPoint(centerX, centerY, scale, scale);
    }

    public void setRangeByStartEnd(float startX, float endX, float startY, float endY) {
        if(!initializedViewSize) {
            getRootView().post(() -> {
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
            getRootView().post(() -> {
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

    public float getGridBlockSize() {
        return gridBlockSize;
    }

    public void seGridBlockSize(int gridBlockSize) {
        this.gridBlockSize = gridBlockSize;
        invalidate();
    }

    public float getFontSize() {
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
        updateRangeStartEnd();
    }

    private void move(double fromCenterX, double fromCenterY, float amountScreenX, float amountScreenY) {
        if(!isMovable) {
            return;
        }
        centerX = fromCenterX - amountScreenX * scaleX;
        centerY = fromCenterY + amountScreenY * scaleY;
        updateRangeStartEnd();
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
            rangeChangeListener.onChange(new Range(range));
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

    private final static int UPDATE_RANGE_MESSAGE = 1;
    private HandlerThread rangeRenderUpdatesHandlerThread;
    private Handler rangeRenderUpdatesHandler;
    private Bitmap rangeRenderedBitmap;
    private Canvas rangeRenderedCanvas;
    private Paint rangeRenderedPaint;
    private SystemOfEquations.Renderer rangeRenderer;
    private Range rangeRendered;
    private boolean isRangeRendererInitialized = false;

    private void initializeRangeRenderer() {
        if(isRangeRendererInitialized || equations == null) {
            return;
        }
        rangeRenderer = equations.renderer(width, height, density);
        rangeRenderedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        rangeRenderedCanvas = new Canvas(rangeRenderedBitmap);
        rangeRenderedCanvas.setDensity((int) density);
        rangeRenderedPaint = new Paint();
        rangeRenderedPaint.setStrokeWidth(functionLineWidth);
        rangeRenderedPaint.setStyle(Paint.Style.STROKE);
        isRangeRendererInitialized = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        rangeRenderUpdatesHandlerThread = new HandlerThread("RangeRenderUpdatesHandlerThread");
        rangeRenderUpdatesHandlerThread.start();
        rangeRenderUpdatesHandler = new Handler(rangeRenderUpdatesHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if(isRangeRendererInitialized && !range.equals(rangeRendered)) {
                    do {
                        removeMessages(UPDATE_RANGE_MESSAGE);
                        Range temp = new Range(range);
                        rangeRenderedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        rangeRenderer.renderXY(rangeRenderedPaint, rangeRenderedCanvas, temp);
                        rangeRendered = temp;
                    } while(hasMessages(UPDATE_RANGE_MESSAGE));
                    invalidate();
                }
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        isRangeRendererInitialized = false;
        rangeRenderUpdatesHandlerThread.quit();
    }

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint();
    private Rect numberTextSize = new Rect();
    private Point centerAxis = new Point();
    private Point gridNumberX = new Point(),
            gridNumberY = new Point();

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(!initializedViewSize) {
            return;
        }

        initializeRangeRenderer();

//        long startTime, endTime;
//        startTime = System.currentTimeMillis();

        // setup canvas
        canvas.setDensity((int) density);

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
            centerAxis.set(0, 0);
            mapScreenPointToGraph(centerAxis);
            centerAxis.x = Math.floor(centerAxis.x/gridStepX)*gridStepX;
            centerAxis.y = Math.floor(centerAxis.y/gridStepY)*gridStepY;
            mapGraphPointToScreen(centerAxis);
            for(double i=centerAxis.x; i < width; i+=gridStepXPixels) {
                canvas.drawLine((float)i, 0, (float)i, height, paint);
            }
            for(double i=centerAxis.y; i < height; i+=gridStepYPixels) {
                canvas.drawLine(0, (float)i, width, (float)i, paint);
            }
        }

        // equations
        if(range.equals(rangeRendered)) {
            canvas.drawBitmap(rangeRenderedBitmap, 0, 0, null);
        } else {
            rangeRenderUpdatesHandler.sendEmptyMessage(UPDATE_RANGE_MESSAGE);
        }

        // axis
        if(showAxis) {
            paint.reset();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(axisColor);
            centerAxis.set(0, 0);
            mapGraphPointToScreen(centerAxis);
            paint.setStrokeWidth(axisAndGridLineWidth);
            canvas.drawLine((float)centerAxis.x, 0, (float)centerAxis.x, height, paint);
            canvas.drawLine(0,(float)centerAxis.y, width, (float)centerAxis.y, paint);
            paint.setStrokeWidth(axisAndGridLineWidth*1.5f);
            canvas.drawLine((float)centerAxis.x-axisDirectionArrowSize*0.6f, axisDirectionArrowSize, (float)centerAxis.x, 2, paint);
            canvas.drawLine((float)centerAxis.x, 2, (float)centerAxis.x+axisDirectionArrowSize*0.6f, axisDirectionArrowSize, paint);
            canvas.drawLine(width-axisDirectionArrowSize, (float)centerAxis.y+axisDirectionArrowSize*0.6f, width-2, (float)centerAxis.y, paint);
            canvas.drawLine((float)width-2, (float)centerAxis.y, width-axisDirectionArrowSize, (float)centerAxis.y-axisDirectionArrowSize*0.6f, paint);
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

            if(Math.abs(range.startX) > MAX_RANGE_UNTIL_CIRCLE_HIGHLIGHT || Math.abs(range.endX) > MAX_RANGE_UNTIL_CIRCLE_HIGHLIGHT ||
                    Math.abs(range.startY) > MAX_RANGE_UNTIL_CIRCLE_HIGHLIGHT || Math.abs(range.endY) > MAX_RANGE_UNTIL_CIRCLE_HIGHLIGHT ||
                    Math.abs(gridStepX) < MIN_RANGE_UNTIL_CIRCLE_HIGHLIGHT || Math.abs(gridStepY) < MIN_RANGE_UNTIL_CIRCLE_HIGHLIGHT) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(axisColor);
                canvas.drawCircle(width/2f, height/2f, centerPointHighlightCircleSize*2f, paint);
                paint.setColor(backgroundColor);
                String pointText = String.format("(%.6f, %.6f)", centerX, centerY);
                textPaint.getTextBounds(pointText, 0, pointText.length(), numberTextSize);
                float left = width/2f-numberTextSize.width()/2f,
                        right = width/2f+numberTextSize.width()/2f,
                        top = height/2f+centerPointHighlightCircleSize*2+20,
                        bottom = height/2f+centerPointHighlightCircleSize*2+20+numberTextSize.height();
                canvas.drawRect(left-10, top, right+10, bottom+15, paint);
                canvas.drawText(pointText, left, bottom-10, textPaint);
            } else {
                double leftX = Math.floor((centerX-width/2f*scaleX)/gridStepX)*gridStepX,
                        bottomY = Math.floor((centerY-height/2f*scaleY)/gridStepY)*gridStepY;
                gridNumberX.set(leftX, 0);
                gridNumberY.set(0, bottomY);
                mapGraphPointToScreen(gridNumberX);
                mapGraphPointToScreen(gridNumberY);
                if(gridNumberX.y > height) {
                    gridNumberX.y = height;
                } else if(gridNumberX.y < 0) {
                    gridNumberX.y = 0;
                }
                if(gridNumberY.x > width) {
                    gridNumberY.x = width;
                } else if(gridNumberY.x < 0) {
                    gridNumberY.x = 0;
                }
                for(double i=gridNumberX.x, j=leftX; i < width; i+=gridStepXPixels, j+=gridStepX) {
                    canvas.drawLine((float)i, (float)gridNumberX.y+10, (float)i, (float)gridNumberX.y-10, paint);
                    double n = (int)(j*100)/100.0;
                    String numberText = (Math.abs(n - (int)n) < 1E-4) ? Integer.toString((int)n) : Double.toString(n);
                    textPaint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
                    canvas.drawText(numberText, (float) (((n == 0) ? 30 : 0) + i-numberTextSize.width()/2.0), (float) (gridNumberX.y+((gridNumberX.y <= height/2.0) ? numberTextSize.height()+35.0 : -35.0)), textPaint);
                }
                for(double i=gridNumberY.y, j=bottomY; i > 0; i-=gridStepYPixels, j+=gridStepY) {
                    canvas.drawLine((float)gridNumberY.x+10, (float)i, (float)gridNumberY.x-10, (float)i, paint);
                    double n = (int)(j*100)/100.0;
                    String numberText = (Math.abs(n - (int)n) < 1E-4) ? Integer.toString((int)n) : Double.toString(n);
                    textPaint.getTextBounds(numberText, 0, numberText.length(), numberTextSize);
                    canvas.drawText((n == 0) ? "" : numberText, (float) (gridNumberY.x+((gridNumberY.x <= width/2.0) ? 35.0 : -numberTextSize.width()-35.0)), (float) (i+numberTextSize.height()/2.0), textPaint);
                }
            }
        }

//        endTime = System.currentTimeMillis();
//        Log.d("MATH_GRAPH_DRAW_TIME", (endTime - startTime) + " milliseconds");
    }
}
