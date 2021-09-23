package com.graphingcalculator.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.graphingcalculator.data.Entitys.equation;
import com.graphingcalculator.data.Entitys.equationsDao;
import com.graphingcalculator.data.Entitys.expression;
import com.graphingcalculator.data.Entitys.function;
import com.graphingcalculator.data.Entitys.functionsDao;
import com.graphingcalculator.data.Entitys.variable;
import com.graphingcalculator.data.Entitys.variablesDao;

import java.util.ArrayList;
import java.util.List;

class AppDataRepository {
    private static volatile AppDataRepository instance;
    private equationsDao equationsDao;
    private functionsDao functionsDao;
    private variablesDao variablesDao;

    public static AppDataRepository buildInstance(Context context) {
        if (instance == null) {
            instance = new AppDataRepository(context);
        }
        return instance;
    }

    public static AppDataRepository getInstance() {
        return instance;
    }

    public AppDataRepository(Context context) {
        AppDatabase db = AppDatabase.buildInstance(context);
        equationsDao = db.equationsDao();
        variablesDao = db.variablesDao();
        functionsDao = db.functionsDao();
    }

    public List<expression> getSystemOfEquations() {
        List<expression> sys = new ArrayList<>();
        sys.addAll(equationsDao.getAll());
        sys.addAll(variablesDao.getAll());
        sys.addAll(functionsDao.getAll());
        sys.sort((a, b) -> (int) (a.getIndex() - b.getIndex()));
        return sys;
    }

    public LiveData<List<expression>> getSystemOfEquationsUpdates() {
        MediatorLiveData<List<expression>[]> live = new MediatorLiveData<>();
        ArrayList[] all = new ArrayList[] {
                new ArrayList<expression>(),
                new ArrayList<expression>(),
                new ArrayList<expression>()
        };
        live.addSource(equationsDao.getAllLiveData(), equations -> {
            all[0].clear();
            all[0].addAll(equations);
            live.setValue(all);
        });
        live.addSource(variablesDao.getAllLiveData(), variables -> {
            all[1].clear();
            all[1].addAll(variables);
            live.setValue(all);
        });
        live.addSource(functionsDao.getAllLiveData(), functions -> {
            all[2].clear();
            all[2].addAll(functions);
            live.setValue(all);
        });
        return Transformations.map(live, _all -> {
            List<expression> list = new ArrayList<expression>();
            list.addAll(all[0]);
            list.addAll(all[1]);
            list.addAll(all[2]);
            list.sort((a, b) -> (int) (a.getIndex() - b.getIndex()));
            return list;
        });
    }

    public void updateSystemOfEquations(List<expression> sys) {
        int index = 0;
        for(expression i : sys) {
            i.setIndex(index);
            if(i.isInitialized()) {
                if(i instanceof equation) {
                    equationsDao.update((equation)i);
                } else if(i instanceof variable) {
                    variablesDao.update((variable)i);
                } else if(i instanceof function) {
                    functionsDao.update((function)i);
                }
            } else {
                long id = 0;
                if(i instanceof equation) {
                    id = equationsDao.insert((equation)i);
                } else if(i instanceof variable) {
                    id = variablesDao.insert((variable)i);
                } else if(i instanceof function) {
                    id = functionsDao.insert((function)i);
                }
                i.setId(id);
            }
            index++;
        }
    }
}
