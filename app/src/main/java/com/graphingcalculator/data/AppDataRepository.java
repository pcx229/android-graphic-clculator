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
import java.util.Random;

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

    public List<expression> getExpressions() {
        List<expression> exps = new ArrayList<>();
        exps.addAll(equationsDao.getAll());
        exps.addAll(variablesDao.getAll());
        exps.addAll(functionsDao.getAll());
        exps.sort((a, b) -> (int) (b.getIndex() - a.getIndex()));
        return exps;
    }

    public LiveData<List<expression>> getExpressionsUpdates() {
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
            list.sort((a, b) -> (int) (b.getIndex() - a.getIndex()));
            return list;
        });
    }

    private long getNextExpressionIndex() {
        Long max = 0L, next;
        next = equationsDao.getMaxIndex();
        if(next != null && next > max) {
            max = next;
        }
        next = variablesDao.getMaxIndex();
        if(next != null && next > max) {
            max = next;
        }
        next = functionsDao.getMaxIndex();
        if(next != null && next > max) {
            max = next;
        }
        return max + 1;
    }

    public void addExpression(expression exp, long index) {
        long id = 0;
        exp.setId(new Random().nextInt());
        if(index == -1) {
            exp.setIndex(getNextExpressionIndex());
        }
        if(exp instanceof equation) {
            id = equationsDao.insert((equation)exp);
        } else if(exp instanceof variable) {
            id = variablesDao.insert((variable)exp);
        } else if(exp instanceof function) {
            id = functionsDao.insert((function)exp);
        }
        exp.setId(id);
    }

    public void addExpression(expression exp) {
        addExpression(exp, -1);
    }

    public void updateExpression(expression exp) {
        if(exp instanceof equation) {
            equationsDao.update((equation)exp);
        } else if(exp instanceof variable) {
            variablesDao.update((variable)exp);
        } else if(exp instanceof function) {
            functionsDao.update((function)exp);
        }
    }

    public void removeExpression(expression exp) {
        if(exp instanceof equation) {
            equationsDao.delete((equation)exp);
        } else if(exp instanceof variable) {
            variablesDao.delete((variable)exp);
        } else if(exp instanceof function) {
            functionsDao.delete((function)exp);
        }
    }

    public void changeExpression(expression expOld, expression expNew) {
        removeExpression(expOld);
        addExpression(expNew, expOld.getIndex());
    }
}
