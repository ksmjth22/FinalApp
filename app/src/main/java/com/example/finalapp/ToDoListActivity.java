package com.example.finalapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    TasksDB db;
    TaskListAdapter taskListAdapter;
    private List<Task> allTasks = new ArrayList<>(); // ✅ Store all tasks for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_to_do_list);
        db = TasksDB.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskListAdapter = new TaskListAdapter(this, db);
        recyclerView.setAdapter(taskListAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Observe for changes in the list of all tasks in the database
        LiveData<List<Task>> tasks = db.tasksDAO().observeAll();

        // ✅ Handle any changes in the observer
        tasks.observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                if (taskListAdapter != null) {
                    allTasks = tasks; // Save full list
                    taskListAdapter.setTaskList(db, tasks);
                } else {
                    Log.e("ToDoListActivity", "Adapter is null!");
                }
            }
        });

        // ✅ Set up filter buttons (you can remove this part if no filters are needed)
        Button filterAll = findViewById(R.id.filterAll);
        Button filterShared = findViewById(R.id.filterShared);
        Button filterPersonal = findViewById(R.id.filterPersonal);

        // Example of filtering logic, modify it based on your needs
        // Remove filtering by shared, and adjust accordingly
        filterAll.setOnClickListener(v -> {
            taskListAdapter.setTaskList(db, allTasks); // Show all tasks
        });

        // You can remove or adjust the shared filtering logic based on any other property you have
        filterShared.setOnClickListener(v -> {
            // Example logic for a different filter, assuming you have another field for filtering
            // List<Task> filteredTasks = ...;
            // taskListAdapter.setTaskList(db, filteredTasks);
        });

        filterPersonal.setOnClickListener(v -> {
            // Example logic for filtering personal tasks
            // List<Task> filteredPersonalTasks = ...;
            // taskListAdapter.setTaskList(db, filteredPersonalTasks);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ToDoApp", "onResume");
    }

    public void addTask(View view) {
        Log.d("ToDoApp", "addTask");
        Intent taskIntent = new Intent(this, ToDo.class);
        startActivity(taskIntent);
    }
}
