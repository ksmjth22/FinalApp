package com.example.finalapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ToDo extends AppCompatActivity {

    Uri imageURI;
    ActivityResultLauncher<Intent> launchCameraActivity;
    private boolean isEditMode = false;
    private int taskIdToEdit = -1;
    private Task editingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task");

            Drawable upArrow = AppCompatResources.getDrawable(this, android.R.drawable.ic_media_previous);
            if (upArrow != null) {
                upArrow.setTint(Color.parseColor("#FFA500")); // Orange
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        }

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("editMode", false);
        taskIdToEdit = intent.getIntExtra("taskId", -1);

        if (isEditMode && taskIdToEdit != -1) {
            TasksDB db = TasksDB.getInstance(this);
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                editingTask = db.tasksDAO().getTaskById(taskIdToEdit);
                if (editingTask != null) {
                    runOnUiThread(() -> {
                        ((EditText) findViewById(R.id.taskTitleView)).setText(editingTask.title);
                        ((EditText) findViewById(R.id.taskDescriptionView)).setText(editingTask.description);
                        ((EditText) findViewById(R.id.taskDueDateView)).setText(editingTask.duedate);
                        ((CheckBox) findViewById(R.id.sharedCheckBox)).setChecked(editingTask.shared);
                        if (editingTask.imageURI != null) {
                            imageURI = Uri.parse(editingTask.imageURI);
                            ((ImageView) findViewById(R.id.taskImageView)).setImageURI(imageURI);
                        }
                    });
                }
            });
        }

        launchCameraActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == RESULT_OK && imageURI != null) {
                        ((ImageView) findViewById(R.id.taskImageView)).setImageURI(imageURI);
                        Log.d("ToDoApp", "Picture stored at: " + imageURI);
                    }
                });

        Button saveButton = findViewById(R.id.saveTaskButton);
        saveButton.setOnClickListener(view -> {
            Log.d("ToDoActivity", "onSaveClick");

            EditText titleView = findViewById(R.id.taskTitleView);
            EditText descView = findViewById(R.id.taskDescriptionView);
            EditText dateView = findViewById(R.id.taskDueDateView);
            EditText timeView = findViewById(R.id.taskDueTimeView);
            CheckBox sharedBox = findViewById(R.id.sharedCheckBox);

            String title = titleView.getText().toString();
            String desc = descView.getText().toString();
            String date = dateView.getText().toString();
            String time = timeView.getText().toString();
            boolean isShared = sharedBox.isChecked();

            TasksDB db = TasksDB.getInstance(view.getContext());

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (isEditMode && editingTask != null) {
                    editingTask.title = title;
                    editingTask.description = desc;
                    editingTask.duedate = date + " " + time;
                    editingTask.imageURI = (imageURI != null) ? imageURI.toString() : null;
                    editingTask.shared = isShared;
                    db.tasksDAO().updateTask(editingTask);
                } else {
                    Task newTask = new Task();
                    newTask.title = title;
                    newTask.description = desc;
                    newTask.duedate = date + " " + time;
                    newTask.imageURI = (imageURI != null) ? imageURI.toString() : null;
                    newTask.done = false;
                    newTask.shared = isShared;
                    db.tasksDAO().insert(newTask);
                }
            });

            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onClick(View view) {
        DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, dayOfMonth) -> {
            EditText dateView = findViewById(R.id.taskDueDateView);
            dateView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        };
        DatePickerDialog dialog = new DatePickerDialog(this, listener, 2020, 1, 1);
        dialog.show();
    }

    public void onTimeClick(View view) {
        TimePickerDialog.OnTimeSetListener listener = (timePicker, hourOfDay, minute) -> {
            EditText timeView = findViewById(R.id.taskDueTimeView);
            timeView.setText(hourOfDay + ":" + minute);
        };
        TimePickerDialog dialog = new TimePickerDialog(this, listener, 0, 0, true);
        dialog.show();
    }

    public void onCameraClick(View view) {
        String filename = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(getFilesDir(), filename);
        imageURI = FileProvider.getUriForFile(this, "com.example.finalapp.fileprovider", imageFile);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        launchCameraActivity.launch(takePictureIntent);
    }
}
