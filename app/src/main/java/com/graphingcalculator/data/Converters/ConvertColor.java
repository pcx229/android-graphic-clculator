package com.graphingcalculator.data.Converters;

import android.graphics.Color;

import androidx.room.TypeConverter;

public class ConvertColor {

    // Date

    @TypeConverter
    public static Color fromNumber(Integer value) {
        if(value == null) {
            return null;
        }
        return Color.valueOf(value);
    }

    @TypeConverter
    public static Integer colorToNumber(Color color) {
        if(color == null) {
            return null;
        }
        return color.toArgb();
    }

}