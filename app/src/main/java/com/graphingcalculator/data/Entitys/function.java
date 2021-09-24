package com.graphingcalculator.data.Entitys;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "functions")

public class function extends expression {

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "arguments")
    @NonNull
    private List<String> arguments;

    @ColumnInfo(name = "body")
    @NonNull
    private String body;

    public function(long id, String name, List<String> arguments, String body, long index) {
        super(id, index);
        this.name = name;
        this.arguments = arguments;
        this.body = body;
    }

    @Ignore
    public function(String name, List<String> arguments, String body) {
        super();
        this.name = name;
        this.arguments = arguments;
        this.body = body;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(@NonNull List<String> arguments) {
        this.arguments = arguments;
    }

    @NonNull
    public String getBody() {
        return body;
    }

    public void setBody(@NonNull String body) {
        this.body = body;
    }

    @Override
    public String getExpression() {
        return name + "(" + String.join(",", arguments) + ") = " + body;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        function cloned = (function)super.clone();
        cloned.setArguments(new ArrayList(arguments));
        return cloned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof function)) return false;
        if (!super.equals(o)) return false;
        function function = (function) o;
        return name.equals(function.name) &&
                arguments.equals(function.arguments) &&
                body.equals(function.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, arguments, body);
    }
}

