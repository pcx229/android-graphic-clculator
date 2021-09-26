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
        functions.put(name, new Function(name, arguments.size()) {
            Expression exp = new ExpressionBuilder(body).variables(arguments.toArray(new String[arguments.size()])).build();
            @Override
            public double apply(double... args) {
                int i=0;
                for(String arg : arguments) {
                    exp.setVariable(arg, args[i++]);
                }
                return exp.evaluate();
            }
        });
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

        private double[][] dense, xs, ys;

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
            dense = new double[calcHeight][calcWidth];
            xs = new double[calcHeight][calcWidth];
            ys = new double[calcHeight][calcWidth];
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

        public void render(Range range, Paint paint, Canvas canvas) {
            System.out.println("size " + dense[0].length + "-" + dense.length);
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
            double value;
            for(Map.Entry<Equation, Expression> exeq : equationsExpressions.entrySet()) {
                expression = exeq.getValue();
                equation = exeq.getKey();
                paint.setColor(equation.getColor());
                for (double y=range.startY, i=0; y < range.endY; y+=stepY, i++) {
                    expression.setVariable("y", y);
                    for(double x=range.startX, j=0; x < range.endX ; x+=stepX, j++) {
                        expression.setVariable("x", x);
                        try {
                            value = expression.evaluate();
                            dense[(int) i][(int) j] = value;
                            float screenX = (float)(width*((x-range.startX)/range.getWidth())),
                                    screenY = (float)(height*((range.getHeight()-(y-range.startY))/range.getHeight()));
                            xs[(int) i][(int) j] = screenX;
                            ys[(int) i][(int) j] = screenY;
                            if(value == 0) {
                                canvas.drawPoint(screenX, screenY, paint);
                            }
                        } catch(Exception e) {}
                    }
                }
                // edge detection
                for(int i=0;i<dense.length-1;i++) {
                    for(int j=0;j<dense[0].length-1;j++) {
                        if(dense[i][j] >= 0 && dense[i][j+1] >= 0 && dense[i+1][j] < 0 && dense[i+1][j+1] < 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i][j+1], (float)ys[i][j+1], paint);
                        }
                        else if(dense[i][j] < 0 && dense[i][j+1] >= 0 && dense[i+1][j] < 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i][j+1], (float)ys[i][j+1], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                        else if(dense[i][j] >= 0 && dense[i][j+1] < 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] < 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i+1][j], (float)ys[i+1][j], paint);
                        }
                        else if(dense[i][j] < 0 && dense[i][j+1] < 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i+1][j], (float)ys[i+1][j], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                        else if(dense[i][j] >= 0 && dense[i][j+1] < 0 && dense[i+1][j] < 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                        else if(dense[i][j] < 0 && dense[i][j+1] >= 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] < 0) {
                            canvas.drawLine((float)xs[i+1][j], (float)ys[i+1][j], (float)xs[i][j+1], (float)ys[i][j+1], paint);
                        }

                        else if(dense[i][j] >= 0 && dense[i][j+1] >= 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] < 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i][j+1], (float)ys[i][j+1], paint);
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i+1][j], (float)ys[i+1][j], paint);
                        }
                        else if(dense[i][j] >= 0 && dense[i][j+1] >= 0 && dense[i+1][j] < 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i][j+1], (float)ys[i][j+1], paint);
                            canvas.drawLine((float)xs[i][j+1], (float)ys[i][j+1], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                        else if(dense[i][j] >= 0 && dense[i][j+1] < 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i][j], (float)ys[i][j], (float)xs[i+1][j], (float)ys[i+1][j], paint);
                            canvas.drawLine((float)xs[i+1][j], (float)ys[i+1][j], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                        else if(dense[i][j] < 0 && dense[i][j+1] >= 0 && dense[i+1][j] >= 0 && dense[i+1][j+1] >= 0) {
                            canvas.drawLine((float)xs[i][j+1], (float)ys[i][j+1], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                            canvas.drawLine((float)xs[i+1][j], (float)ys[i+1][j], (float)xs[i+1][j+1], (float)ys[i+1][j+1], paint);
                        }
                    }
                }
            }
        }
    }

    public Renderer render(int width, int height, float density){
        if(renderer == null || !renderer.isSame(width, height, density)) {
            renderer = new Renderer(width, height, density);
        }
        return renderer;
    }
}
