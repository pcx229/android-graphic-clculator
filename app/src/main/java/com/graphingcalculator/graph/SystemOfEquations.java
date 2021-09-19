package com.graphingcalculator.graph;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SystemOfEquations {

    private List<Object> stack;

    public SystemOfEquations() {
        stack = new ArrayList<>();
    }

    public void addVariable(Variable var) {
        stack.add(var);
    }

    public List<Variable> getVariables() {
        List<Variable> list = new ArrayList<>();
        for(Object i : stack) {
            if(i instanceof Variable) {
                list.add((Variable)i);
            }
        }
        return list;
    }

    public void removeVariable(Variable var) {
        stack.remove(var);
    }

    public void addFunction(Function func) {
        stack.add(func);
    }

    public void removeFunction(Function func) {
        stack.remove(func);
    }

    public List<Function> getFunctions() {
        List<Function> list = new ArrayList<>();
        for(Object i : stack) {
            if(i instanceof Function) {
                list.add((Function)i);
            }
        }
        return list;
    }

    public void addEquation(Equation eq) {
        stack.add(eq);
    }

    public void removeEquation(Equation eq) {
        stack.remove(eq);
    }

    public List<Equation> getEquation() {
        List<Equation> list = new ArrayList<>();
        for(Object i : stack) {
            if(i instanceof Equation) {
                list.add((Equation)i);
            }
        }
        return list;
    }

    private Stack<boolean[][]> matrix_cash = new Stack<>();

    public Map<Equation, boolean[][]> calculateRange(Range range, int n) {
        Map<Equation, boolean[][]> result = new HashMap<>();
        Stack<boolean[][]> new_matrix_cash = new Stack<>();
        for(Object i : stack) {
            if(i instanceof Equation) {
                boolean[][] matrix;
                if(matrix_cash.isEmpty()) {
                    matrix = new boolean[n][n];
                } else {
                    matrix = matrix_cash.pop();
                }
                new_matrix_cash.push(matrix);
                Equation eq = (Equation) i;
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
                BigDecimal itrY = BigDecimal.valueOf(range.startY),
                        stepY = BigDecimal.valueOf(range.getHeight()/(n-1));
                for(int x=0; x < n ; x++, itrX = itrX.add(stepX)) {
                    for(int y=0; y < n ; y++, itrY = itrY.add(stepY)) {
                        ex.setVariable("x", itrX);
                        ex.setVariable("y", itrY);
                        if(ex.eval().intValue() == 1) {
                            matrix[y][x] = true;
                        }
                    }
                }
                result.put(eq, matrix);
            }
        }
        matrix_cash = new_matrix_cash;
        return result;
    }
}
