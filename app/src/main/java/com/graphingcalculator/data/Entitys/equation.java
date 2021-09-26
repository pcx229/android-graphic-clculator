package com.graphingcalculator.data.Entitys;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Objects;

@Entity(tableName = "equations")

public class equation extends expression {

    @ColumnInfo(name = "text")
    @NonNull
    private String body;

    @ColumnInfo(name = "color")
    @NonNull
    private Color color;

    @ColumnInfo(name = "visible")
    @NonNull
    private boolean visible;

    public equation(long id, String body, Color color, boolean visible, long index) {
        super(id, index);
        this.body = body;
        this.color = color;
        this.visible = visible;
    }

    @Ignore
    public equation(String body, Color color) {
        super();
        this.body = body;
        this.color = color;
        this.visible = true;
    }

    public String getBody() {
        return body;
    }

    public Color getColor() {
        return color;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String getExpression() {
        return body;
    }

    public void update(expression exp) {
        equation eq = (equation) exp;
        body = eq.body;
        color = eq.color;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof equation)) return false;
        if (!super.equals(o)) return false;
        equation equation = (equation) o;
        return visible == equation.visible &&
                body.equals(equation.body) &&
                color.equals(equation.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), body, color, visible);
    }
}

