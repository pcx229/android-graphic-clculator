package com.graphingcalculator.data.Entitys;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.List;

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
}

