package com.graphingcalculator.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SystemOfEquations {

    private List<Equation> equations;
    private Map<String, Double> variables;
    private Map<String, Function> functions;
    private List<Operator> operators;

    private Renderer renderer;

    public SystemOfEquations() {
        equations = new ArrayList<>();
        variables = new HashMap<>();
        functions = new HashMap<>();
        operators = new ArrayList<>();
        operators.add(new Operator("=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                return values[1] - values[0];
            }
        });
        operators.add(new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                return values[0] - values[1];
            }
        });
        operators.add(new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                return values[0] - values[1];
            }
        });
        operators.add(new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                return values[1] - values[0];
            }
        });
        operators.add(new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                return values[1] - values[0];
            }
        });
        operators.add(new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
            @Override
            public double apply(double... args) {
                final int arg = (int) args[0];
                if ((double) arg != args[0]) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }
                if (arg < 0) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }
                double result = 1;
                for (int i = 1; i <= arg; i++) {
                    result *= i;
                }
                return result;
            }
        });
    }

    public Map<String, Double> getVariables() {
        return variables;
    }

    public void addVariable(String name, double value) {
        variables.put(name, value);
    }

    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    public void deleteVariable(String name) {
        variables.remove(name);
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public void addFunction(String name, List<String> arguments, String body) {
        functions.put(name, parseFunction(name, arguments, body));
    }

    public static Function parseFunction(String name, List<String> arguments, String body) {
        return new Function(name, arguments.size()) {
            Expression exp = new ExpressionBuilder(body).variables(arguments.toArray(new String[arguments.size()])).build();
            @Override
            public double apply(double... args) {
                int i=0;
                for(String arg : arguments) {
                    exp.setVariable(arg, args[i++]);
                }
                return exp.evaluate();
            }
        };
    }

    public void deleteFunction(String name) {
        functions.remove(name);
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    public List<Equation> getEquations() {
        return equations;
    }

    public void addEquation(Equation eq) {
        equations.add(eq);
    }

    public void deleteEquation(Equation eq) {
        equations.remove(eq);
    }

    public boolean hasEquation(Equation eq) {
        return equations.contains(eq);
    }

    class Renderer {

        private int screenWidth, screenHeight;
        private float screenDensity;

        private class EquationsExpressions {
            public Equation equation;
            public Expression expression;

            public EquationsExpressions(Equation equation, Expression expression) {
                this.equation = equation;
                this.expression = expression;
            }
        }
        private List<EquationsExpressions> equationsExpressions;

        private Path pathX;

        private int calcWidth, calcHeight;
        private Point[][] xy;
        private Path pathXY;

        public Renderer(int screenWidth, int screenHeight, float screenDensity) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.screenDensity = screenDensity;
            equationsExpressions = new ArrayList<>();
            pathX = new Path();
            pathXY = new Path();
            List<Function> functionsList = new ArrayList<>(functions.values());
            Set<String> variablesSet = new HashSet<>(variables.keySet());
            variablesSet.add("y");
            variablesSet.add("x");
            for(Equation equation : equations) {
                try {
                    Expression expression = new ExpressionBuilder(equation.getEquation())
                            .variables(variablesSet)
                            .functions(functionsList)
                            .operator(operators)
                            .build();
                    equationsExpressions.add(new EquationsExpressions(equation, expression));
                } catch(Exception e) {}
            }

            calcWidth = (int) (screenWidth / (screenDensity*2.5));
            calcHeight = (int) (screenHeight / (screenDensity*2.5));
            xy = new Point[calcHeight][calcWidth];
            for(int i=0;i<calcHeight;i++) {
                for(int j=0;j<calcWidth;j++) {
                    xy[i][j] = new Point();
                }
            }
        }

        public boolean isScreenParams(int width, int height, float density) {
            return this.screenWidth == width && this.screenHeight == height && this.screenDensity == density;
        }

        public void renderX(Paint paint, Canvas canvas, Range range) {
            double stepX = range.getWidth()/((screenWidth / (screenDensity *3))-1);
            // general variables
            for(EquationsExpressions exeq : equationsExpressions) {
                for(Map.Entry<String, Double> var : variables.entrySet()) {
                    exeq.expression.setVariable(var.getKey(), var.getValue());
                }
            }
            // x variable
            Expression expression;
            Equation equation;
            for(EquationsExpressions exeq : equationsExpressions) {
                paint.setColor(exeq.equation.getColor());
                pathX.reset();
                for(double x=range.startX, lastX=Double.NaN, y=0; x < range.endX ; lastX=x, x+=stepX) {
                    exeq.expression.setVariable("x", x);
                    try {
                        y = exeq.expression.evaluate();
                    } catch(Exception e) {
                        y = Double.NEGATIVE_INFINITY;
                    }
                    float screenX = (float)(screenWidth *((x-range.startX)/range.getWidth())),
                            screenY = (float)(screenHeight *((range.getHeight()-(y-range.startY))/range.getHeight()));
                    if(!Double.isNaN(lastX)) {
                        pathX.lineTo(screenX, screenY);
                        pathX.moveTo(screenX, screenY);
                    } else {
                        pathX.moveTo(screenX, screenY);
                    }
                }
                pathX.close();
                canvas.drawPath(pathX, paint);
            }
        }

        private class Point {
            public double x, y, density;
            public byte sign;

            public double screenX, screenY;
            public boolean isMapped;

            public double zeroX, zeroY;
            public boolean isZeroed;

            public Range range;

            public void set(double x, double y, double density, Range range) {
                this.x = x;
                this.y = y;
                this.density = density;
                this.range = range;
                isMapped = false;
                isZeroed = false;
                sign = 0x0;
            }

            void set(Point p) {
                x = p.x;
                y = p.y;
                density = p.density;
                screenX = p.screenX;
                screenY = p.screenY;
                isMapped = p.isMapped;
                zeroX = p.zeroX;
                zeroY = p.zeroY;
                isZeroed = p.isZeroed;
                range = p.range;
            }

            public void mapToScreen() {
                if(isZeroed) {
                    screenX = screenWidth*((zeroX-range.startX)/range.getWidth());
                    screenY = screenHeight*((range.getHeight()-(zeroY-range.startY))/range.getHeight());
                } else {
                    screenX = screenWidth*((x-range.startX)/range.getWidth());
                    screenY = screenHeight*((range.getHeight()-(y-range.startY))/range.getHeight());
                }
                isMapped = true;
            }

            public void intermediateToDensityZero(Point to) {
                double theta = Math.atan2((to.y - y), (to.x - x));
                double distance = Math.sqrt(Math.pow(x-to.x, 2) + Math.pow(y-to.y, 2));
                double progress = Math.abs(density)/(Math.abs(density)+Math.abs(to.density));
                zeroX = x + (distance*progress)*Math.cos(theta);
                zeroY = y + (distance*progress)*Math.sin(theta);
                isMapped = false;
                isZeroed = true;
            }

            public double densityDistance(Point to) {
                return Math.abs(density-to.density);
            }

            public void lineTo(Point to) {
                if(!isMapped) {
                    mapToScreen();
                }
                if(!to.isMapped) {
                    to.mapToScreen();
                }
                pathXY.moveTo((float) (screenX), (float) (screenY));
                pathXY.lineTo((float) (to.screenX), (float) (to.screenY));
            }

            public void drawPoint(int color, Paint paint, Canvas canvas) {
                if(!isMapped) {
                    mapToScreen();
                }
                paint.setColor(color);
                canvas.drawPoint((float) (screenX), (float) (screenY), paint);
            }
        }

        private void drawDeptPositiveNegativeHeatMap(Paint paint, Canvas canvas) {
            double pMin = Double.POSITIVE_INFINITY,
                    pMax = Double.NEGATIVE_INFINITY,
                    nMin = Double.POSITIVE_INFINITY,
                    nMax = Double.NEGATIVE_INFINITY;
            for(int i = 0; i < calcHeight; i++) {
                for (int j = 0; j < calcWidth; j++) {
                    if(xy[i][j].density >= 0) {
                        if(pMin > xy[i][j].density) {
                            pMin = xy[i][j].density;
                        }
                        if(pMax < xy[i][j].density) {
                            pMax = xy[i][j].density;
                        }
                    } else {
                        if(nMin > xy[i][j].density) {
                            nMin = xy[i][j].density;
                        }
                        if(nMax < xy[i][j].density) {
                            nMax = xy[i][j].density;
                        }
                    }
                }
            }
            int color;
            for(int i = 0; i < calcHeight; i++) {
                for (int j = 0; j < calcWidth; j++) {
                    if(xy[i][j].density >= 0) {
                        color = Color.rgb(0, (int) ((xy[i][j].density-pMin)/(pMax-pMin)*255), 0);
                    } else {
                        color = Color.rgb((int) ((xy[i][j].density-nMin)/(nMax-nMin)*255), 0, 0);
                    }
                    xy[i][j].drawPoint(color, paint, canvas);
                }
            }
        }

        private void drawDeptHeatMap(Paint paint, Canvas canvas) {
            double min = Double.POSITIVE_INFINITY,
                    max = Double.NEGATIVE_INFINITY;
            for(int i = 0; i < calcHeight; i++) {
                for (int j = 0; j < calcWidth; j++) {
                    if(min > xy[i][j].density) {
                        min = xy[i][j].density;
                    }
                    if(max < xy[i][j].density) {
                        max = xy[i][j].density;
                    }
                }
            }
            int color;
            for(int i = 0; i < calcHeight; i++) {
                for (int j = 0; j < calcWidth; j++) {
                    color = Color.rgb(0, 0, (int) ((xy[i][j].density-min)/(max-min)*255));
                    xy[i][j].drawPoint(color, paint, canvas);
                }
            }
        }

        private void drawFlatHeatMap(Paint paint, Canvas canvas) {
            int color;
            for(int i = 0; i < calcHeight; i++) {
                for (int j = 0; j < calcWidth; j++) {
                    if(xy[i][j].density >= 0) {
                        color = Color.rgb(0, 255, 0);
                    } else {
                        color = Color.rgb(255, 0, 0);
                    }
                    xy[i][j].drawPoint(color, paint, canvas);
                }
            }
        }

        private boolean sameSign(double a, double b) {
            return (a >= 0 && b >= 0) || (a < 0 && b < 0);
        }

        private byte signKernel(int i, int j) {
            byte kernel = 0x0;
            if(xy[i][j].density >= 0) {
                kernel |= 0x1;
            }
            if(xy[i][j+1].density >= 0) {
                kernel |= 0x2;
            }
            if(xy[i+1][j].density >= 0) {
                kernel |= 0x4;
            }
            if(xy[i+1][j+1].density >= 0) {
                kernel |= 0x8;
            }
            if((!sameSign(xy[i][j].density, xy[i][j+1].density) && Math.abs(xy[i][j].density - xy[i][j+1].density) > Math.abs(xy[i][j].density - xy[i][j+2].density)) ||
                    (!sameSign(xy[i][j].density, xy[i+1][j].density) && Math.abs(xy[i][j].density - xy[i+1][j].density) > Math.abs(xy[i][j].density - xy[i+2][j].density)) ||
                    (!sameSign(xy[i][j].density, xy[i+1][j+1].density) && Math.abs(xy[i][j].density - xy[i+1][j+1].density) > Math.abs(xy[i][j].density - xy[i+2][j+2].density))) {
                kernel = 0x0;
            }
            return kernel;
        }

        private void applySignKernel() {
            for(int i = 0; i < calcHeight - 2; i++) {
                for (int j = 0; j < calcWidth - 2; j++) {
                    xy[i][j].sign = signKernel(i, j);
                }
            }
        }

        private void drawWithKernel(boolean fillShape) {
            applySignKernel();
            for(int i = 0; i < calcHeight - 2; i++) {
                for (int j = 0; j < calcWidth - 2; j++) {
                    switch(xy[i][j].sign) {
                        case (0x1 | 0X2):
                        case (0x4 | 0X8):
                            xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                            xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i][j].lineTo(xy[i][j+1]);
                            break;
                        case (0x2 | 0X8):
                        case (0x1 | 0X4):
                        case (0x1 | 0X8):
                        case (0x2 | 0X4):
                            xy[i][j].intermediateToDensityZero(xy[i][j+1]);
                            xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i][j].lineTo(xy[i+1][j]);
                            break;
                        case (0x1 | 0x2 | 0x4):
                        case (0x8):
                            xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i+1][j].lineTo(xy[i][j+1]);
                            break;
                        case (0x1 | 0x2 | 0x8):
                        case (0x4):
                            xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                            xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i][j].lineTo(xy[i+1][j]);
                            break;
                        case (0x2 | 0x4 | 0x8):
                        case (0x1):
                            xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                            xy[i][j+1].intermediateToDensityZero(xy[i][j]);
                            xy[i][j].lineTo(xy[i][j+1]);
                            break;
                        case (0x1 | 0x4 | 0x8):
                        case (0x2):
                            xy[i][j].intermediateToDensityZero(xy[i][j+1]);
                            xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                            xy[i][j].lineTo(xy[i][j+1]);
                            break;
                    }
                }
            }
            if(fillShape) {
                for(int i = 0; i < calcHeight - 1; i++) {
                    for (int j = 0; j < calcWidth - 1; j++) {
                        if (xy[i][j].density >= 0 && xy[i][j+1].density >= 0) {
                            xy[i][j].lineTo(xy[i][j+1]);
                        }
                        if (xy[i][j].density >= 0 && xy[i+1][j].density >= 0) {
                            xy[i][j].lineTo(xy[i+1][j]);
                        }
                    }
                }
            }
        }

        public void renderXY(Paint paint, Canvas canvas, Range range) {
            double stepX = range.getWidth()/(calcWidth-1),
                    stepY = range.getHeight()/(calcHeight-1);
            // general variables
            for(EquationsExpressions exeq : equationsExpressions) {
                for(Map.Entry<String, Double> var : variables.entrySet()) {
                    exeq.expression.setVariable(var.getKey(), var.getValue());
                }
            }
            // x, y variables
            Expression expression;
            Equation equation;
            for(EquationsExpressions exeq : equationsExpressions) {
                paint.setColor(exeq.equation.getColor());
                // density
                double value = 0;
                double itrY = range.startY;
                for(int i = 0; i < calcHeight; i++, itrY += stepY) {
                    exeq.expression.setVariable("y", itrY);
                    double itrX = range.startX;
                    for (int j = 0; j < calcWidth; j++, itrX += stepX) {
                        exeq.expression.setVariable("x", itrX);
                        try {
                            value = exeq.expression.evaluate();
                        } catch (Exception e) {
                            value = 0;
                        }
                        xy[i][j].set(itrX, itrY, value, range);
                    }
                }
                // draw
                boolean fillShape = false;
                if(Pattern.compile(">=|<=|<|>").matcher(exeq.equation.getEquation()).find()) {
                    fillShape = true;
                }
                pathXY.reset();
                drawWithKernel(fillShape);
                canvas.drawPath(pathXY, paint);
            }
        }
    }

    public Renderer renderer(int width, int height, float density){
        if(renderer == null || !renderer.isScreenParams(width, height, density)) {
            renderer = new Renderer(width, height, density);
        }
        return renderer;
    }
}
