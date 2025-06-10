package com.projeku.finalproject.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.projeku.finalproject.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    // Mengambil semua tugas yang belum selesai, diurutkan berdasarkan prioritas
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC")
    LiveData<List<Task>> getUncompletedTasks();

    // Mengambil semua tugas yang sudah selesai
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY id DESC")
    LiveData<List<Task>> getCompletedTasks();
}
