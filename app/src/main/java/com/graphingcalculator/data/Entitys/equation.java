package com.graphingcalculator.data.Entitys;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

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
        return "y = " + body;
    }
}

