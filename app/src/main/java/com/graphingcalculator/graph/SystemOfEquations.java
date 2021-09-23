package com.graphingcalculator.graph;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean hasVariable(String name) {
        for(Variable v : getVariables()) {
            if(v.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFunction(String name) {
        for(Function f : getFunctions()) {
            if(f.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Map<Equation, Point[]> calculateRange(Range range, int n) {
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
        return result;
    }
}
