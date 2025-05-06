package com.example.finalapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1) // Set the version back to 1
public abstract class TasksDB extends RoomDatabase {

    public abstract TasksDAO tasksDAO();

    private static final String DB_NAME = "tasks_database_name";
    private static TasksDB db;

    // Return a database instance.
    public static TasksDB getInstance(Context context) {
        if (db == null) {
            db = buildDatabaseInstance(context);
        }
        return db;
    }

    private static TasksDB buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), TasksDB.class, DB_NAME)
                // Removed the migration since we're no longer adding the shared column
                .build();
    }


}
