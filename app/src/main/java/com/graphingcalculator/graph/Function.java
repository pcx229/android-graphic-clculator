package com.graphingcalculator.graph;

import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Function {
    private String name;
    private List<String> arguments;
    private String body;
    private AbstractFunction call;

    public Function(String name) {
        this.name = name;
        this.arguments = new ArrayList<>();
    }

    public Function(String name, List<String> arguments, String body) {
        this.name = name;
        this.arguments = arguments;
        this.body = body;
        update();
    }

    private void update() {
        call = new AbstractFunction(name, arguments.size()) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() != arguments.size()) {
                    throw new Expression.ExpressionException(name + " requires " + arguments.size() +  " parameters");
                }
                Expression ex = new Expression(body);
                Iterator<String> i = arguments.iterator();
                Iterator<BigDecimal> j = parameters.iterator();
                while(i.hasNext()) {
                    String name = (String) i.next();
                    BigDecimal value = (BigDecimal) j.next();
                    ex.setVariable(name, value);
                }
                return ex.eval();
            }
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        update();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        update();
    }

    public AbstractFunction getCall() {
        return call;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void addParam(String name) {
        arguments.add(name);
        update();
    }

    public void removeParam(String name) {
        arguments.remove(name);
        update();
    }
}
