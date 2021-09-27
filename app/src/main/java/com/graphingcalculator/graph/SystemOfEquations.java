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

        private int width, height;
        private float density;

        private Map<Equation, Expression> equationsExpressions;

        public Renderer(int width, int height, float density) {
            this.width = width;
            this.height = height;
            this.density = density;
            equationsExpressions = new HashMap<>();
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

            int calcWidth = (int) (width / (density*3)),
                    calcHeight = (int) (height / (density*3));
            dense = new float[calcHeight][calcWidth];
            kernel = new byte[calcHeight][calcWidth];
            xy = new PointD[calcHeight][calcWidth];
            for(int i=0;i<calcHeight;i++) {
                for(int j=0;j<calcWidth;j++) {
                    xy[i][j] = new PointD();
                }
            }
        }

        public boolean isSame(int width, int height, float density) {
            return this.width == width && this.height == height && this.density == density;
        }

        private Path path = new Path();

        public void renderX(Range range, Paint paint, Canvas canvas) {
            double stepX = range.getWidth()/((width / (density*3))-1);
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
                path.reset();
                for(double x=range.startX, lastX=Double.NaN, y=0, lastY=0; x < range.endX ; lastX=x, x+=stepX, lastY=y) {
                    expression.setVariable("x", x);
                    try {
                        y = expression.evaluate();
                        float screenX = (float)(width*((x-range.startX)/range.getWidth())),
                                screenY = (float)(height*((range.getHeight()-(y-range.startY))/range.getHeight()));
                        if(!Double.isNaN(lastX)) {
                            path.lineTo(screenX, screenY);
                            path.moveTo(screenX, screenY);
                        } else {
                            path.moveTo(screenX, screenY);
                        }
                    } catch(Exception e) {}
                }
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        private class PointD {
            double x, y;
            void set(PointD p) {
                x = p.x;
                y = p.y;
            }
        }

        private PointD[][] xy;
        private float[][] dense;
        private byte[][] kernel;
        private PointD temp1 = new PointD(),
                temp2 = new PointD();

        private void mapPointToScreen(PointD p, Range range) {
            p.x = width*((p.x-range.startX)/range.getWidth());
            p.y = height*((range.getHeight()-(p.y-range.startY))/range.getHeight());
        }

        // calculation intermediate point in a line along the distance
        private void intermediate(PointD p1, float d1, PointD p2, float d2, PointD result) {
            double theta = Math.atan2((p2.y - p1.y), (p2.x - p1.x));
            double distance = Math.sqrt(Math.pow(p1.x-p2.x, 2) + Math.pow(p1.y-p2.y, 2));
            double progress = Math.abs(d1)/(Math.abs(d1)+Math.abs(d2));
            result.x = p1.x + (distance*progress)*Math.cos(theta);
            result.y = p1.y + (distance*progress)*Math.sin(theta);
        }

        private void line(PointD p11, float d11, PointD p12, float d12, PointD p21, float d21, PointD p22, float d22, Range range, Paint paint, Canvas canvas) {
            intermediate(p11, d11, p12, d12, temp1);
            mapPointToScreen(temp1, range);
            intermediate(p21, d21, p22, d22, temp2);
            mapPointToScreen(temp2, range);
            canvas.drawLine((float) (temp1.x), (float) (temp1.y), (float) (temp2.x), (float) (temp2.y), paint);
        }
        private void rect(PointD tl, PointD br, Range range, Paint paint, Canvas canvas) {
            temp1.set(tl);
            mapPointToScreen(temp1, range);
            temp2.set(br);
            mapPointToScreen(temp2, range);
            canvas.drawRect((float) (temp1.x), (float) (temp1.y), (float) (temp2.x), (float) (temp2.y), paint);
        }

        public void render(Range range, Paint paint, Canvas canvas) {
//            System.out.println("size " + dense[0].length + "-" + dense.length);
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            double stepX = range.getWidth()/(dense[0].length-1),
                    stepY = range.getHeight()/(dense.length-1);
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
                            dense[i][j] = (float) value;
                            xy[i][j].x = x;
                            xy[i][j].y = y;
                            j++;
                        }
                        i++;
                    }
                }
                // edge detection - edges kernel type
                for(int i = 0; i < dense.length - 2; i++) {
                    for(int j = 0; j < dense[0].length - 2; j++) {
                        kernel[i][j] = 0x0;
                        if(dense[i][j] >= 0) {
                            kernel[i][j] |= 0x1;
                        }
                        if(dense[i][j+1] >= 0) {
                            kernel[i][j] |= 0x2;
                        }
                        if(dense[i+1][j] >= 0) {
                            kernel[i][j] |= 0x4;
                        }
                        if(dense[i+1][j+1] >= 0) {
                            kernel[i][j] |= 0x8;
                        }
                        if(dense[i][j] == 0 && dense[i][j+1] == 0 && dense[i+1][j] == 0 && dense[i+1][j+1] == 0) {
                            kernel[i][j] = 0x16;
                        }
                    }
                }
                // lines between edges and fill areas
                for(int i = 0; i < kernel.length - 2; i++) {
                    for (int j = 0; j < kernel[0].length - 2; j++) {
                        switch(kernel[i][j]) {
                            case (0x1 | 0X2):
                            case (0x4 | 0X8):
                                line(xy[i][j], dense[i][j], xy[i+1][j], dense[i+1][j], xy[i][j+1], dense[i][j+1], xy[i+1][j+1], dense[i+1][j+1], range, paint, canvas);
                                break;
                            case (0x2 | 0X8):
                            case (0x1 | 0X4):
                            case (0x1 | 0X8):
                            case (0x2 | 0X4):
                                line(xy[i][j], dense[i][j], xy[i][j+1], dense[i][j+1], xy[i+1][j], dense[i+1][j], xy[i+1][j+1], dense[i+1][j+1], range, paint, canvas);
                                break;
                            case (0x1 | 0x2 | 0x4):
                                line(xy[i+1][j], dense[i+1][j], xy[i+1][j+1], dense[i+1][j+1], xy[i][j+1], dense[i][j+1], xy[i+1][j+1], dense[i+1][j+1], range, paint, canvas);
                                break;
                            case (0x1 | 0x2 | 0x8):
                                line(xy[i][j], dense[i][j], xy[i+1][j], dense[i+1][j], xy[i+1][j+1], dense[i+1][j+1], xy[i+1][j], dense[i+1][j], range, paint, canvas);
                                break;
                            case (0x2 | 0x4 | 0x8):
                                line(xy[i+1][j], dense[i+1][j], xy[i][j], dense[i][j], xy[i][j+1], dense[i][j+1], xy[i][j], dense[i][j], range, paint, canvas);
                                break;
                            case (0x1 | 0x4 | 0x8):
                                line(xy[i][j], dense[i][j], xy[i][j+1], dense[i][j+1], xy[i+1][j+1], dense[i+1][j+1], xy[i][j+1], dense[i][j+1], range, paint, canvas);
                                break;
                            case (0x16):
                                rect(xy[i][j], xy[i+1][j+1], range, paint, canvas);
                                break;
                        }
                    }
                }
            }
            endTime = System.currentTimeMillis();
            System.out.println("time " + (endTime - startTime) + " milliseconds");
        }
    }

    public Renderer render(int width, int height, float density){
        if(renderer == null || !renderer.isSame(width, height, density)) {
            renderer = new Renderer(width, height, density);
        }
        return renderer;
    }
}
