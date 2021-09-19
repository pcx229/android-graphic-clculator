package com.graphingcalculator.graph;

import java.math.BigDecimal;

public class Variable {
    private String name;
    private BigDecimal value;

    public Variable(String name) {
        this.name = name;
        this.value = null;
    }

    public Variable(String name, double value) {
        this.name = name;
        this.value = BigDecimal.valueOf(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
