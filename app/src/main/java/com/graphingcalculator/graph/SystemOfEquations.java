package com.graphingcalculator.graph;

import android.graphics.Canvas;
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
                if (values[0] >= values[1]) {
                    return 0d;
                } else {
                    return values[1] - values[0];
                }
            }
        });
        operators.add(new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] > values[1]) {
                    return 0d;
                } else {
                    return values[1] - values[0];
                }
            }
        });
        operators.add(new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] <= values[1]) {
                    return 0d;
                } else {
                    return values[0] - values[1];
                }
            }
        });
        operators.add(new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] < values[1]) {
                    return 0d;
                } else {
                    return values[0] - values[1];
                }
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

        private Map<Equation, Expression> equationsExpressions;

        private Range range;

        private Path pathX;

        private Point[][] xy;
        private Path pathXY;

        public Renderer(int screenWidth, int screenHeight, float screenDensity) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.screenDensity = screenDensity;
            equationsExpressions = new HashMap<>();
            pathX = new Path();
            pathXY = new Path();
            List<Function> functionsList = new ArrayList<>(functions.values());
            Set<String> variablesSet = new HashSet<>(variables.keySet());
            variablesSet.add("y");
            variablesSet.add("x");
            for(Equation equation : equations) {
                Expression expression = new ExpressionBuilder(equation.getEquation())
                        .variables(variablesSet)
                        .functions(functionsList)
                        .operator(operators)
                        .build();
                equationsExpressions.put(equation, expression);
            }

            int calcWidth = (int) (screenWidth / (screenDensity *3)),
                    calcHeight = (int) (screenHeight / (screenDensity *3));
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

        public void setRange(Range range) {
            this.range = range;
        }

        public void renderX(Paint paint, Canvas canvas) {
            double stepX = range.getWidth()/((screenWidth / (screenDensity *3))-1);
            // general variables
            for(Map.Entry<Equation, Expression> i : equationsExpressions.entrySet()) {
                Expression expression = i.getValue();
                for(Map.Entry<String, Double> var : variables.entrySet()) {
                    expression.setVariable(var.getKey(), var.getValue());
                }
            }
            // x variable
            Expression expression;
            Equation equation;
            for(Map.Entry<Equation, Expression> exeq : equationsExpressions.entrySet()) {
                expression = exeq.getValue();
                equation = exeq.getKey();
                paint.setColor(equation.getColor());
                pathX.reset();
                for(double x=range.startX, lastX=Double.NaN, y=0, lastY=0; x < range.endX ; lastX=x, x+=stepX, lastY=y) {
                    expression.setVariable("x", x);
                    try {
                        y = expression.evaluate();
                        float screenX = (float)(screenWidth *((x-range.startX)/range.getWidth())),
                                screenY = (float)(screenHeight *((range.getHeight()-(y-range.startY))/range.getHeight()));
                        if(!Double.isNaN(lastX)) {
                            pathX.lineTo(screenX, screenY);
                            pathX.moveTo(screenX, screenY);
                        } else {
                            pathX.moveTo(screenX, screenY);
                        }
                    } catch(Exception e) {}
                }
                pathX.close();
                canvas.drawPath(pathX, paint);
            }
        }

        private class Point {
            public double x, y, density;

            public double screenX, screenY;
            public boolean isMapped;

            public double zeroX, zeroY;
            public boolean isZeroed;

            public void set(double x, double y, double density) {
                this.x = x;
                this.y = y;
                this.density = density;
                isMapped = false;
                isZeroed = false;
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

            public boolean isNegative() {
                return density < 0;
            }

            public boolean isPositive() {
                return density > 0;
            }

            public void lineTo(Point to, Paint paint, Canvas canvas) {
                if(!isMapped) {
                    mapToScreen();
                }
                if(!to.isMapped) {
                    to.mapToScreen();
                }
                canvas.drawLine((float) (screenX), (float) (screenY), (float) (to.screenX), (float) (to.screenY), paint);
            }

            public void rect(Point bottomRight, Paint paint, Canvas canvas) {
                if(!isMapped) {
                    mapToScreen();
                }
                if(!bottomRight.isMapped) {
                    bottomRight.mapToScreen();
                }
                canvas.drawRect((float) (screenX), (float) (screenY), (float) (bottomRight.screenX), (float) (bottomRight.screenY), paint);
            }

            public void drawPoint(Paint paint, Canvas canvas) {
                if(!isMapped) {
                    mapToScreen();
                }
                canvas.drawPoint((float) (screenX), (float) (screenY), paint);
            }
        }

        private byte point2Kernel(int i, int j) {
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
            if(xy[i][j].density == 0 && xy[i][j+1].density == 0 && xy[i+1][j].density == 0 && xy[i+1][j+1].density == 0) {
                kernel = 0x16;
            }
            return kernel;
        }

        // walk along the zero line until reaching the edges, assess the shape type.
        private void findZeroLine(int i, int j) {
            // find child with a change of sign then me value
            Point child = null;
            for(int m=0, ii=i-1, jj=j-1;m<9;m++, ii=i-1+m/3, jj=j-1+m%3) {
                if(ii != i && jj != j && ii > 0 && jj > 0) {
                    if(xy[i][j].density >= 0 && xy[ii][jj].isNegative() ||
                            xy[i][j].density <= 0 && xy[ii][jj].isPositive()) {
                        child = xy[ii][jj];
                        break;
                    }
                }
            }
            // is my value 0 and im on the edge?
            if(xy[i][j].density == 0 && i == 0 || i == xy.length-1 || j == 0 || j == xy[0].length-1) {

            }
            if(child == null) {
                return;
            }
            // put path
            xy[i][j].intermediateToDensityZero(child);
            xy[i][j].mapToScreen();
            if(!pathXY.isEmpty()) {
                pathXY.lineTo((float)xy[i][j].x, (float)xy[i][j].y);
            }
            pathXY.moveTo((float)xy[i][j].x, (float)xy[i][j].y);
            // check neighbors
            for(int m=0, ii=i-1, jj=j-1;m<9;m++, ii=i-1+m/3, jj=j-1+m%3) {
                if(ii != i && jj != j && ii > 0 && jj > 0) {
                    pathXY.moveTo((float)xy[i][j].x, (float)xy[i][j].y);
                    findZeroLine(ii, jj);
                }
            }
        }

        public void render(Paint paint, Canvas canvas) {
//            System.out.println("size " + dense[0].length + "-" + dense.length);
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            double stepX = range.getWidth()/(xy[0].length-1),
                    stepY = range.getHeight()/(xy.length-1);
            // general variables
            for(Map.Entry<Equation, Expression> i : equationsExpressions.entrySet()) {
                Expression expression = i.getValue();
                for(Map.Entry<String, Double> var : variables.entrySet()) {
                    expression.setVariable(var.getKey(), var.getValue());
                }
            }
            // x, y variables
            Expression expression;
            Equation equation;
            for(Map.Entry<Equation, Expression> exeq : equationsExpressions.entrySet()) {
                expression = exeq.getValue();
                equation = exeq.getKey();
                paint.setColor(equation.getColor());
                // density
                {
                    int i=0, j;
                    double value = 0;
                    for (double y=range.startY; y < range.endY; y+=stepY) {
                        expression.setVariable("y", y);
                        j=0;
                        for(double x=range.startX; x < range.endX ; x+=stepX) {
                            expression.setVariable("x", x);
                            try { value = expression.evaluate(); } catch(Exception e) {}
                            xy[i][j].set(x, y, value);
                            j++;
                        }
                        i++;
                    }
                }
                for(int i = 0; i < xy.length - 2; i++) {
                    for (int j = 0; j < xy[0].length - 2; j++) {
                        switch(point2Kernel(i, j)) {
                            case (0x1 | 0X2):
                            case (0x4 | 0X8):
                                xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                                xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i][j].lineTo(xy[i][j+1], paint, canvas);
                                break;
                            case (0x2 | 0X8):
                            case (0x1 | 0X4):
                            case (0x1 | 0X8):
                            case (0x2 | 0X4):
                                xy[i][j].intermediateToDensityZero(xy[i][j+1]);
                                xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i][j].lineTo(xy[i+1][j], paint, canvas);
                                break;
                            case (0x1 | 0x2 | 0x4):
                            case (0x8):
                                xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i+1][j].lineTo(xy[i][j+1], paint, canvas);
                                break;
                            case (0x1 | 0x2 | 0x8):
                            case (0x4):
                                xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                                xy[i+1][j].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i][j].lineTo(xy[i+1][j], paint, canvas);
                                break;
                            case (0x2 | 0x4 | 0x8):
                            case (0x1):
                                xy[i][j].intermediateToDensityZero(xy[i+1][j]);
                                xy[i][j+1].intermediateToDensityZero(xy[i][j]);
                                xy[i][j].lineTo(xy[i][j+1], paint, canvas);
                                break;
                            case (0x1 | 0x4 | 0x8):
                            case (0x2):
                                xy[i][j].intermediateToDensityZero(xy[i][j+1]);
                                xy[i][j+1].intermediateToDensityZero(xy[i+1][j+1]);
                                xy[i][j].lineTo(xy[i][j+1], paint, canvas);
                                break;
                            case (0x16):
                                xy[i][j].drawPoint(paint, canvas);
                                xy[i][j+1].drawPoint(paint, canvas);
                                xy[i+1][j].drawPoint(paint, canvas);
                                xy[i+1][j+1].drawPoint(paint, canvas);
                                break;
                        }
                    }
                }
                /*
                for(int i = 0; i < xy.length - 1; i++) {
                    for (int j = 0; j < xy[0].length - 1; j++) {
                        paint.setColor(Color.rgb((xy[i][j].density < 0) ? (int)(Math.max(-xy[i][j].density*255, 255)) : 0,
                                (xy[i][j].density > 0) ? (int)(Math.max(xy[i][j].density*255, 255)) : 0,
                                (xy[i][j].density == 0) ? 255 : 0));
                        xy[i][j].mapToScreen();
                        canvas.drawPoint((float) (xy[i][j].screenX), (float) (xy[i][j].screenY), paint);
                    }
                }
                double min = Double.POSITIVE_INFINITY,
                        max = Double.NEGATIVE_INFINITY;
                for(int i = 0; i < xy.length - 1; i++) {
                    for (int j = 0; j < xy[0].length - 1; j++) {
                        if(xy[i][j].density < min) {
                            min = xy[i][j].density;
                        }
                        if(xy[i][j].density > max) {
                            max = xy[i][j].density;
                        }
                    }
                }
                 */
            }
            endTime = System.currentTimeMillis();
            System.out.println("time " + (endTime - startTime) + " milliseconds");
        }
    }

    public Renderer render(int width, int height, float density){
        if(renderer == null || !renderer.isScreenParams(width, height, density)) {
            renderer = new Renderer(width, height, density);
        }
        return renderer;
    }
}
