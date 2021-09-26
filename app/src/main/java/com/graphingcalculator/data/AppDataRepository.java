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
import java.util.concurrent.atomic.AtomicInteger;

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
        List<expression> expressions = new ArrayList<>();
        expressions.addAll(equationsDao.getAll());
        expressions.addAll(variablesDao.getAll());
        expressions.addAll(functionsDao.getAll());
        expressions.sort((a, b) -> (int) (b.getIndex() - a.getIndex()));
        return expressions;
    }

    public LiveData<List<expression>> getExpressionsUpdates() {
        MediatorLiveData<List<expression>[]> live = new MediatorLiveData<>();
        ArrayList[] all = new ArrayList[] {
                new ArrayList<expression>(),
                new ArrayList<expression>(),
                new ArrayList<expression>()
        };
        AtomicInteger first = new AtomicInteger(0);
        live.addSource(equationsDao.getAllLiveData(), equations -> {
            all[0].clear();
            all[0].addAll(equations);
            first.addAndGet(1);
            if(first.get() > 3) {
                live.setValue(all);
            }
        });
        live.addSource(variablesDao.getAllLiveData(), variables -> {
            all[1].clear();
            all[1].addAll(variables);
            first.addAndGet(1);
            if(first.get() > 3) {
                live.setValue(all);
            }
        });
        live.addSource(functionsDao.getAllLiveData(), functions -> {
            all[2].clear();
            all[2].addAll(functions);
            first.addAndGet(1);
            if(first.get() > 3) {
                live.setValue(all);
            }
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
        Long max = 0L, a, b, c;
        a = equationsDao.getMaxIndex();
        b = variablesDao.getMaxIndex();
        c = functionsDao.getMaxIndex();
        return Math.max(Math.max(a, b), c) + 1;
    }

    public void insertExpression(expression exp) {
        if(exp instanceof equation) {
            equationsDao.insert((equation)exp);
        } else if(exp instanceof variable) {
            variablesDao.insert((variable)exp);
        } else if(exp instanceof function) {
            functionsDao.insert((function)exp);
        }
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

    public void deleteExpression(expression exp) {
        if(exp instanceof equation) {
            equationsDao.delete((equation)exp);
        } else if(exp instanceof variable) {
            variablesDao.delete((variable)exp);
        } else if(exp instanceof function) {
            functionsDao.delete((function)exp);
        }
    }

    public void updateVariables(List<expression> exps) {
        for(expression exp : exps) {
            if(exp instanceof variable) {
                variablesDao.update((variable)exp);
            }
        }
    }

    public void changeExpression(expression expOld, expression expNew) {
        deleteExpression(expOld);
        expNew.setIndex(expOld.getIndex());
        insertExpression(expNew);
    }

    public void addExpression(expression exp) {
        exp.setIndex(getNextExpressionIndex());
        insertExpression(exp);
    }

    public void replaceExpressions(List<expression> expressions) {
        equationsDao.deleteAll();
        variablesDao.deleteAll();
        functionsDao.deleteAll();
        int index = expressions.size();
        for(expression exp : expressions) {
            exp.setIndex(index--);
        }
        for(expression exp : expressions) {
            insertExpression(exp);
        }
    }
}
