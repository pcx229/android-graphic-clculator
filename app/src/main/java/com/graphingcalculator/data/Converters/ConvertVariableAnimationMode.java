package com.graphingcalculator.data.Converters;

import androidx.room.TypeConverter;

import com.graphingcalculator.data.Entitys.variable;

public class ConvertVariableAnimationMode {
    @TypeConverter
    public static variable.ANIMATION_MODE fromString(String name) {
        return variable.ANIMATION_MODE.valueOf(name);
    }

    @TypeConverter
    public static String fromVariableAnimationMode(variable.ANIMATION_MODE mode) {
        return mode.name();
    }
}
