package com.example.finalapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TasksDAO {


    //add a task to the database
    @Insert
    void insert(Task task);

    //Observe for changes to the tasks in the database
    @Query("SELECT * FROM task")
    LiveData<List<Task>> observeAll();

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM task WHERE uid = :id LIMIT 1")
    Task getTaskById(int id);


}

