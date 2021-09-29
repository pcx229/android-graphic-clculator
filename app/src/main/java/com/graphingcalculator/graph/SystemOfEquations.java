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

        private Range range;

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

            calcWidth = (int) (screenWidth / (screenDensity *3));
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
                for(double x=range.startX, lastX=Double.NaN, y=0, lastY=0; x < range.endX ; lastX=x, x+=stepX, lastY=y) {
                    exeq.expression.setVariable("x", x);
                    try {
                        y = exeq.expression.evaluate();
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

        private byte findKernel(int i, int j) {
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
            return kernel;
        }

        private void drawWithKernel(boolean fillShape, Paint paint, Canvas canvas) {
            for(int i = 0; i < calcHeight - 1; i++) {
                for (int j = 0; j < calcWidth - 1; j++) {
                    byte kernel = findKernel(i, j);
                    switch(kernel) {
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
                    }
                }
            }
            if(fillShape) {
                for(int i = 0; i < calcHeight - 1; i++) {
                    for (int j = 0; j < calcWidth - 1; j++) {
                        if (xy[i][j].density >= 0 && xy[i][j+1].density >= 0) {
                            xy[i][j].lineTo(xy[i][j+1], paint, canvas);
                        }
                        if (xy[i][j].density >= 0 && xy[i+1][j].density >= 0) {
                            xy[i][j].lineTo(xy[i+1][j], paint, canvas);
                        }
                    }
                }
            }
        }

        private void findZeroLine(int i, int j, int depth) {
            Point child = null;
            for(int m=0, ii=i-1, jj=j-1;m<3*3;m++, ii=i-1+m/3, jj=j-1+m%3) {
                if(!(ii == i && jj == j) &&
                        ii >= 0 && jj >= 0 &&
                        ii < calcHeight && jj < calcWidth &&
                        !xy[ii][jj].isMapped) {
                    if((xy[i][j].density < 0 && xy[ii][jj].density >= 0) ||
                            (xy[i][j].density >= 0 && xy[ii][jj].density < 0)) {
                        child = xy[ii][jj];
                        break;
                    }
                }
            }
            if(child == null) {
                return;
            }
            xy[i][j].intermediateToDensityZero(child);
            xy[i][j].mapToScreen();
            if(depth != 0) {
                pathXY.lineTo((float)xy[i][j].screenX, (float)xy[i][j].screenY);
            }
            for(int m=0, ii=i-1, jj=j-1;m<3*3;m++, ii=i-1+m/3, jj=j-1+m%3) {
                if(!(ii == i && jj == j) &&
                        ii >= 0 && jj >= 0 &&
                        ii < calcHeight && jj < calcWidth &&
                        !xy[ii][jj].isMapped) {
                    if((xy[i][j].density < 0 && xy[ii][jj].density < 0) ||
                            (xy[i][j].density >= 0 && xy[ii][jj].density >= 0)) {
                        pathXY.moveTo((float)xy[i][j].screenX, (float)xy[i][j].screenY);
                        findZeroLine(ii, jj, depth+1);
                    }
                }
            }
        }

        private boolean differentSign(Point a, Point b) {
            return (a.density < 0 && b.density >=0) || (a.density >= 0 && b.density < 0);
        }

        private void drawWithSearch(boolean fillShape, Paint paint, Canvas canvas) {
            pathXY.reset();
            for(int i = 0; i < calcHeight - 1; i++) {
                for (int j = 0; j < calcWidth - 1; j++) {
                    if(differentSign(xy[i][j], xy[i][j+1]) || differentSign(xy[i][j], xy[i+1][j]) || differentSign(xy[i][j], xy[i+1][j+1])) {
                        findZeroLine(i, j, 0);
                    }
                }
            }
            canvas.drawPath(pathXY, paint);
            if(fillShape) {
                for(int i = 0; i < calcHeight - 1; i++) {
                    for (int j = 0; j < calcWidth - 1; j++) {
                        if (xy[i][j].density >= 0 && xy[i][j+1].density >= 0) {
                            xy[i][j].lineTo(xy[i][j+1], paint, canvas);
                        }
                        if (xy[i][j].density >= 0 && xy[i+1][j].density >= 0) {
                            xy[i][j].lineTo(xy[i+1][j], paint, canvas);
                        }
                    }
                }
            }
        }

        public void render(Paint paint, Canvas canvas) {
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
                        }
                        xy[i][j].set(itrX, itrY, value);
                    }
                }
                // draw
                boolean fillShape = false;
                if(Pattern.compile(">=|<=|<|>").matcher(exeq.equation.getEquation()).find()) {
                    fillShape = true;
                }
                drawWithKernel(fillShape, paint, canvas);
            }
        }
    }

    public Renderer render(int width, int height, float density){
        if(renderer == null || !renderer.isScreenParams(width, height, density)) {
            renderer = new Renderer(width, height, density);
        }
        return renderer;
    }
}
