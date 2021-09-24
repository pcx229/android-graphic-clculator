package com.graphingcalculator.data.Entitys;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface functionsDao {

    @Query("SELECT * FROM functions ORDER BY arrangement")
    List<function> getAll();

    @Query("SELECT * FROM functions")
    LiveData<List<function>> getAllLiveData();

    @Query("SELECT Max(arrangement) FROM functions")
    Long getMaxIndex();

    @Insert
    Long insert(function f);

    @Insert
    List<Long> insertAll(function... f);

    @Update
    void update(function f);

    @Update
    void updateAll(function... f);

    @Update
    void updateAll(List<function> f);

    @Query("DELETE FROM functions WHERE id IN (:ids)")
    void delete(List<Long> ids);

    @Query("DELETE FROM functions WHERE id = :id")
    void delete(long id);

    @Delete
    void delete(function f);
}
