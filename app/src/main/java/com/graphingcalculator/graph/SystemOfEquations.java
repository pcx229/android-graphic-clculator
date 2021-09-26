package com.graphingcalculator.graph;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemOfEquations {

    private List<Equation> equations;
    private Map<String, Double> variables;
    private Map<String, Function> functions;

    public SystemOfEquations() {
        equations = new ArrayList<>();
        variables = new HashMap<>();
        functions = new HashMap<>();
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
        public Renderer() {

        }
    }

    public Renderer build() {
        return new Renderer();
    }

    public Map<Equation, Point[]> calculateRange(Range range, int n) {/*
        Map<Equation, Point[]> result = new HashMap<>();
        for(Object i : stack) {
            if(i instanceof Equation) {
                Equation eq = (Equation) i;
                Point[] points = new Point[n];
                Expression ex = new Expression(eq.getEquation());
                for(Object j : stack) {
                    if (j instanceof Variable) {
                        Variable var = (Variable) j;
                        ex.setVariable(var.getName(), var.getValue());
                    } else if (j instanceof Function) {
                        Function func = (Function) j;
                        ex.addFunction(func.getCall());
                    }
                }
                BigDecimal itrX = BigDecimal.valueOf(range.startX),
                        stepX = BigDecimal.valueOf(range.getWidth()/(n-1));
                boolean error = false;
                for(int x=0; x < n ; x++, itrX = itrX.add(stepX)) {
                    ex.setVariable("x", itrX);
                    try {
                        points[x] = new Point(itrX.doubleValue(), ex.eval().doubleValue());
                    } catch(Exception e) {
                        error = true;
                        break;
                    }
                }
                if(error) {
                    continue;
                }
                result.put(eq, points);
            }
        }
        return result;*/
        return null;
    }
}
