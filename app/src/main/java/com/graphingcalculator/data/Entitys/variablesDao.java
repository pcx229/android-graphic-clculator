package com.graphingcalculator.data.Entitys;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface variablesDao {

    @Query("SELECT * FROM variables ORDER BY arrangement")
    List<variable> getAll();

    @Query("SELECT * FROM variables")
    LiveData<List<variable>> getAllLiveData();

    @Insert
    Long insert(variable v);

    @Insert
    List<Long> insertAll(variable... v);

    @Update
    void update(variable v);

    @Update
    void updateAll(variable... v);

    @Update
    void updateAll(List<variable> v);

    @Query("DELETE FROM variables WHERE id IN (:ids)")
    void delete(List<Long> ids);

    @Query("DELETE FROM variables WHERE id = :id")
    void delete(long id);

    @Delete
    void delete(variable v);
}
