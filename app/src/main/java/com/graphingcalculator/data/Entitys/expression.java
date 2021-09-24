package com.graphingcalculator.data.Entitys;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity

public abstract class expression {

    @PrimaryKey
    private long id;

    @ColumnInfo(name = "arrangement")
    @NonNull
    private long index;

    public expression(long id, long index) {
        this.id = id;
        this.index = index;
    }

    @Ignore
    public expression() {
        this.id = 0;
        this.index = 0;
    }

    public boolean isInitialized() {
        return id != 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public abstract String getExpression();

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof expression)) return false;
        expression that = (expression) o;
        return id == that.id &&
                index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, index);
    }
}
