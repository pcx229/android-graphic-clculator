package com.graphingcalculator.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.graphingcalculator.data.Converters.ConvertColor;
import com.graphingcalculator.data.Converters.ConvertListStrings;
import com.graphingcalculator.data.Converters.ConvertVariableAnimationMode;
import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.equationsDao;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.function;
import com.graphingcalculator.data.Entitys.functionsDao;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.data.Entitys.variablesDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {expression.class, equation.class, variable.class, function.class}, version = 7, exportSchema = false)
@TypeConverters({ ConvertVariableAnimationMode.class, ConvertColor.class, ConvertListStrings.class })
abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "app_db";
    private static AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * database need to be initialized first before calling this function, see buildInstance()
     * @return an instance to database connection
     */
    public static synchronized AppDatabase getInstance() {
        if(instance == null) {
            throw new RuntimeException("database was not initialized yet");
        }
        return instance;
    }

    /**
     * this function should be called once, then any call to getInstance() without parameters
     * can be made to get the active connection
     * @param context app context
     * @return an instance to database connection
     */
    public static synchronized AppDatabase buildInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    /**
     * close database connection instance
     */
    public static synchronized void closeInstance() {
        instance.close();
        instance = null;
    }

    public abstract equationsDao equationsDao();
    public abstract variablesDao variablesDao();
    public abstract functionsDao functionsDao();
}
