package com.example.finalapp;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private List<Task> tasks;
    private TasksDB db;

    public TaskListAdapter(ToDoListActivity toDoListActivity, TasksDB db) {
        this.db = db;
        this.tasks = new ArrayList<>();
    }

    public void setTaskList(TasksDB db, List<Task> tasks) {
        this.db = db;
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Task task = tasks.get(position);
        holder.titleView.setText(task.title);
        holder.descView.setText(task.description);

        if (task.imageURI != null && !task.imageURI.isEmpty()) {
            holder.imageView.setImageURI(Uri.parse(task.imageURI));
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.doneCheckBox.setChecked(task.done);

        holder.doneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.done = isChecked;
            Executor myExecutor = Executors.newSingleThreadExecutor();
            myExecutor.execute(() -> db.tasksDAO().updateTask(task));
        });

        // ✅ Tap to edit the task
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ToDo.class);
            intent.putExtra("editMode", true);
            intent.putExtra("taskId", task.uid);
            v.getContext().startActivity(intent);
        });

        // 🗑️ Long press to delete the task
        holder.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(() -> db.tasksDAO().deleteTask(task));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return (tasks == null) ? 0 : tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView descView;
        ImageView imageView;
        CheckBox doneCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.taskListTitle);
            descView = itemView.findViewById(R.id.taskListDesc);
            imageView = itemView.findViewById(R.id.taskListImage);
            doneCheckBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
