package com.graphingcalculator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

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
        Expression ex = new ExpressionBuilder("sin(x)+x^2+cos(x)+3.4^5")
                .variables("x")
                .build();
        double result = 0;
        double start, end;
        start = System.currentTimeMillis();
        for(int i=3;i<500;i++) {
            for(int j=3;j<500;j++) {
                ex.setVariable("x", i);
                result = ex.evaluate();
            }
        }
        end = System.currentTimeMillis();
        System.out.println("time - " + (end-start));
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}