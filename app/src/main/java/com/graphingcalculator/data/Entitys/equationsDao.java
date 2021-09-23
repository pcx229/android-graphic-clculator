package com.graphingcalculator.data.Entitys;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface equationsDao {

    @Query("SELECT * FROM equations ORDER BY arrangement")
    List<equation> getAll();

    @Query("SELECT * FROM equations")
    LiveData<List<equation>> getAllLiveData();

    @Insert
    Long insert(equation e);

    @Insert
    List<Long> insertAll(equation... e);

    @Update
    void update(equation e);

    @Update
    void updateAll(equation... e);

    @Update
    void updateAll(List<equation> e);

    @Query("DELETE FROM equations WHERE id IN (:ids)")
    void delete(List<Long> ids);

    @Query("DELETE FROM equations WHERE id = :id")
    void delete(long id);

    @Delete
    void delete(equation e);
}
