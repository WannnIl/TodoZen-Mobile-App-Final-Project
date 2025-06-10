package com.projeku.finalproject.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.projeku.finalproject.db.AppDatabase;
import com.projeku.finalproject.db.TaskDao;
import com.projeku.finalproject.model.Task;

import java.util.List;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<Task>> uncompletedTasks;
    private LiveData<List<Task>> completedTasks;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        uncompletedTasks = taskDao.getUncompletedTasks();
        completedTasks = taskDao.getCompletedTasks();
    }

    // Metode ini akan diobservasi oleh ViewModel
    public LiveData<List<Task>> getUncompletedTasks() {
        return uncompletedTasks;
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }

    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.insert(task);
        });
    }

    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.update(task);
        });
    }

    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.delete(task);
        });
    }
}
