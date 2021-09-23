package com.graphingcalculator.data.Entitys;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "variables")

public class variable extends expression {

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "value")
    @NonNull
    private double value;

    @ColumnInfo(name = "range_start")
    @NonNull
    private double rangeStart;

    @ColumnInfo(name = "range_end")
    @NonNull
    private double rangeEnd;

    @ColumnInfo(name = "animate_step")
    @NonNull
    private double animationStep;

    public enum ANIMATION_MODE { NONE, REPEAT, BACK_AND_FORTH, STOP_AT_BOUNDARIES }

    @ColumnInfo(name = "animate_mode")
    @NonNull
    private ANIMATION_MODE animationMode;

    public variable(long id, String name, double value, double rangeStart, double rangeEnd, double animationStep, ANIMATION_MODE animationMode, long index) {
        super(id, index);
        this.name = name;
        this.value = value;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.animationStep = animationStep;
        this.animationMode = animationMode;
    }

    @Ignore
    public variable(String name, double value, double rangeStart, double rangeEnd) {
        super();
        this.name = name;
        this.value = value;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.animationStep = 0;
        this.animationMode = ANIMATION_MODE.NONE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(double rangeStart) {
        this.rangeStart = rangeStart;
    }

    public double getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(double rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public double getProgress() {
        return (value - rangeStart) / (rangeEnd - rangeStart);
    }

    public double getAnimationStep() {
        return animationStep;
    }

    @NonNull
    public ANIMATION_MODE getAnimationMode() {
        return animationMode;
    }

    public void setAnimation(double step, @NonNull ANIMATION_MODE mode) {
        this.animationStep = step;
        this.animationMode = mode;
    }

    public void disableAnimation() {
        this.animationStep = 0;
        this.animationMode = ANIMATION_MODE.NONE;
    }

    public boolean isAnimated() {
        return animationMode != ANIMATION_MODE.NONE;
    }

    @Override
    public String getExpression() {
        return name + " = " + value;
    }
}

