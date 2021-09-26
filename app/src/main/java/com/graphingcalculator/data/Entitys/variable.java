package com.graphingcalculator.data.Entitys;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Objects;

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

    public enum ANIMATION_MODE { REPEAT, BACK_AND_FORTH, STOP_AT_BOUNDARIES }

    @ColumnInfo(name = "animate_mode")
    @NonNull
    private ANIMATION_MODE animationMode;

    @ColumnInfo(name = "animate")
    @NonNull
    private boolean animated;

    public variable(long id, String name, double value, double rangeStart, double rangeEnd, double animationStep, boolean animated, ANIMATION_MODE animationMode, long index) {
        super(id, index);
        this.name = name;
        this.value = value;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.animationStep = animationStep;
        this.animationMode = animationMode;
        this.animated = animated;
    }

    @Ignore
    public variable(String name, double value, double rangeStart, double rangeEnd) {
        super();
        this.name = name;
        this.value = value;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.animationStep = 0;
        this.animationMode = ANIMATION_MODE.BACK_AND_FORTH;
        this.animated = false;
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
        if(value < rangeStart) {
            rangeStart = value;
        }
        if(value > rangeEnd) {
            rangeEnd = value;
        }
    }

    public double getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(double start) {
        rangeStart = start;
        if(value < rangeStart) {
            value = rangeStart;
        }
    }

    public double getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(double end) {
        rangeEnd = end;
        if(value > rangeEnd) {
            value = rangeEnd;
        }
    }

    public void setRange(double start, double end) {
        setRangeStart(start);
        setRangeEnd(end);
    }

    public void setValueProgress(double progress) {
        this.value = rangeStart + progress*(rangeEnd-rangeStart);
    }

    public double getValueProgress() {
        return (value - rangeStart) / (rangeEnd - rangeStart);
    }

    public double getAnimationStep() {
        return animationStep;
    }

    @NonNull
    public ANIMATION_MODE getAnimationMode() {
        return animationMode;
    }

    public void setAnimation(boolean active, double step, @NonNull ANIMATION_MODE mode) {
        this.animationStep = step;
        this.animationMode = mode;
        this.animated = active;
    }

    public void disableAnimation() {
        this.animationStep = 0;
        this.animated = false;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animate) {
        this.animated = animated;
        animationStep = Math.abs(animationStep);
    }

    public void stepAnimation() {
        if(animated) {
            value += animationStep;
            switch(animationMode) {
                case BACK_AND_FORTH:
                    if(value > rangeEnd) {
                        value = rangeEnd;
                        animationStep = -animationStep;
                    } else if(value < rangeStart) {
                        value = rangeStart;
                        animationStep = -animationStep;
                    }
                    break;
                case REPEAT:
                    if(value > rangeEnd) {
                        value = rangeStart;
                    }
                    break;
                case STOP_AT_BOUNDARIES:
                    if(value > rangeEnd) {
                        value = rangeEnd;
                    }
                    break;
            }
        }
    }

    @Override
    public String getExpression() {
        return String.format("%s = %.5f", name, value);
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof variable)) return false;
        if (!super.equals(o)) return false;
        variable variable = (variable) o;
        return Double.compare(variable.value, value) == 0 &&
                Double.compare(variable.rangeStart, rangeStart) == 0 &&
                Double.compare(variable.rangeEnd, rangeEnd) == 0 &&
                Double.compare(variable.animationStep, animationStep) == 0 &&
                name.equals(variable.name) &&
                animationMode == variable.animationMode &&
                animated == variable.animated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, value, rangeStart, rangeEnd, animationStep, animationMode, animated);
    }
}

