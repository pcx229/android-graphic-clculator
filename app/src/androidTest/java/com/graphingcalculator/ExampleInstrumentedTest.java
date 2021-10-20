package com.graphingcalculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.graphingcalculator.graph.Range;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void cc() throws InterruptedException {

        int UPDATE_RANGE = 1;
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
//        handlerThread.interrupt();
//        handlerThread.quit();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper) {
            public void handleMessage(Message msg) {
                while(hasMessages(UPDATE_RANGE)) {

                }
            }
        };
        handler.sendEmptyMessage(UPDATE_RANGE);
        handler.sendEmptyMessage(UPDATE_RANGE);
        handler.sendEmptyMessage(UPDATE_RANGE);
        handler.sendEmptyMessage(UPDATE_RANGE);
        handlerThread.join();
    }

    @Test
    public void matrix() {/*
        long start, end;
        Expression ex = new Expression("sinr(x)+x^2+cosr(x)+3.4^5");
        BigDecimal result;
        BigDecimal step = new BigDecimal(1);
        BigDecimal x = new BigDecimal(0);
        BigDecimal y = new BigDecimal(0);
        ex.setVariable("x", x);
        ex.setVariable("y", y);
        start = System.currentTimeMillis();
        for(int i=0;i<500;i++) {
            for(int j=0;j<500;j++) {
                x = x.add(step);
                result = ex.eval();
                if(i == 0 && i < 3) System.out.println(result.doubleValue());
            }
            y = y.add(step);
        }
        end = System.currentTimeMillis();
        System.out.println("time - " + (end-start));*/
    }

    @Test
    public void matrix2() {
        int width = 1080, height = 1920;
        float density = 3.5f;
        int calculatedWidth = (int)(width/density),
                calculatedHeight = (int)(height/density);
        Range range = new Range(-10, 10, -10, 10);
        System.out.println("calculated dimensions: " + calculatedWidth + "x" + calculatedHeight);

        Bitmap result = Bitmap.createBitmap(calculatedWidth, calculatedHeight, Bitmap.Config.ARGB_8888);

        int nx = result.getWidth(),
                ny = result.getHeight();
        double stepX = range.getWidth()/(nx-1),
                stepY = range.getHeight()/(ny-1);
        double step = Math.min(stepX, stepY);

        Expression ex = new ExpressionBuilder("y=x^2")
                .variables("x", "y")
                .operator(new Operator("=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

                    @Override
                    public double apply(double[] values) {
                        if (Math.abs(values[0] - values[1]) < step) {
                            return 1d;
                        } else {
                            return 0d;
                        }
                    }
                })
                .build();
        double value = 0;
        double startTime, endTime;
        startTime = System.currentTimeMillis();
        for(double x=range.startX, i=0; x < range.endX ; x+=stepX, i++) {
            for (double y = range.startY, j=0; y < range.endY; y += stepY, j++) {
                ex.setVariable("x", i);
                ex.setVariable("y", j);
                try {
                    value = ex.evaluate();
                    if(value != 0) {
                        result.setPixel((int)i, (int)j, Color.BLUE);
                    }
                } catch(Exception e) {}
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("time - " + (endTime-startTime));
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.graphingcalculator", appContext.getPackageName());
    }
}