package com.graphingcalculator.graph;

import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Function {
    private String name;
    private List<String> params;
    private String body;
    private AbstractFunction call;

    public Function(String name) {
        this.name = name;
        this.params = new ArrayList<>();
    }

    public Function(String name, List<String> params, String body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    private void update() {
        call = new AbstractFunction(name, params.size()) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() != params.size()) {
                    throw new Expression.ExpressionException(name + " requires " + params.size() +  " parameters");
                }
                Expression ex = new Expression(body);
                Iterator<String> i = params.iterator();
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

    public List<String> getParams() {
        return params;
    }

    public void addParam(String name) {
        params.add(name);
        update();
    }

    public void removeParam(String name) {
        params.remove(name);
        update();
    }
}
