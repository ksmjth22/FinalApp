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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    TasksDB db;
    TaskListAdapter taskListAdapter;
    private List<Task> allTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_to_do_list);

        db = TasksDB.getInstance(this);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Use the correct adapter constructor
        taskListAdapter = new TaskListAdapter(db);
        recyclerView.setAdapter(taskListAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Observe task changes from DB
        LiveData<List<Task>> tasks = db.tasksDAO().observeAll();
        tasks.observe(this, updatedTasks -> {
            allTasks = updatedTasks;
            taskListAdapter.setTaskList(updatedTasks); // ✅ Matches your adapter method
        });

        // ✅ Filter Buttons
        Button filterAll = findViewById(R.id.filterAll);
        Button filterShared = findViewById(R.id.filterShared);
        Button filterPersonal = findViewById(R.id.filterPersonal);

        filterAll.setOnClickListener(v -> taskListAdapter.setTaskList(allTasks));

        filterShared.setOnClickListener(v -> {
            List<Task> sharedTasks = new ArrayList<>();
            for (Task t : allTasks) {
                if (t.shared) sharedTasks.add(t);
            }
            taskListAdapter.setTaskList(sharedTasks);
        });

        filterPersonal.setOnClickListener(v -> {
            List<Task> personalTasks = new ArrayList<>();
            for (Task t : allTasks) {
                if (!t.shared) personalTasks.add(t);
            }
            taskListAdapter.setTaskList(personalTasks);
        });
    }

    public void addTask(View view) {
        Intent taskIntent = new Intent(this, ToDo.class);
        startActivity(taskIntent);
    }
}
