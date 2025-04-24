package com.example.todo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Directory> directories;
    private TodoAdapter todoAdapter;
    private SharedPreferences sharedPreferences;
    private static final String DIRECTORIES_KEY = "directories";
    private static final int DIRECTORY_MANAGEMENT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        directories = new ArrayList<>();
        if (savedInstanceState != null) {
            directories = savedInstanceState.getParcelableArrayList(DIRECTORIES_KEY);
        }
        if (directories == null || directories.isEmpty()) {
            directories = new ArrayList<>();
            directories.add(new Directory("Główny"));
        }

        initializeViews();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        EditText inputTodo = findViewById(R.id.inputTodo);
        Button addButton = findViewById(R.id.addButton);
        Button manageDirButton = findViewById(R.id.manageDirButton);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch themeToggleBtn = findViewById(R.id.themeToggleBtn);

        ArrayList<String> allNotes = getAllNotes();
        todoAdapter = new TodoAdapter(allNotes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(todoAdapter);

        addButton.setOnClickListener(v -> {
            String todoText = inputTodo.getText().toString().trim();
            if (!todoText.isEmpty()) {
                directories.get(0).addNote(todoText);
                updateNotesList();
                inputTodo.setText("");
            }
        });

        manageDirButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DirectoryManagementActivity.class);
            intent.putParcelableArrayListExtra("directories", new ArrayList<>(directories));
            startActivityForResult(intent, DIRECTORY_MANAGEMENT_REQUEST);
        });

        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        themeToggleBtn.setText(isNightMode ? R.string.light_mode : R.string.dark_mode);
        themeToggleBtn.setChecked(isNightMode);

        themeToggleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();

            themeToggleBtn.setText(isChecked ? R.string.light_mode : R.string.dark_mode);

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            recreate();
        });
    }

    private ArrayList<String> getAllNotes() {
        ArrayList<String> allNotes = new ArrayList<>();
        for (Directory dir : directories) {
            allNotes.addAll(dir.getNotes());
        }
        return allNotes;
    }

    private void updateNotesList() {
        ArrayList<String> allNotes = getAllNotes();
        todoAdapter = new TodoAdapter(allNotes, this);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(todoAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_MANAGEMENT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<Directory> updatedDirectories = data.getParcelableArrayListExtra("directories");
            if (updatedDirectories != null) {
                directories = updatedDirectories;
                updateNotesList();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DIRECTORIES_KEY, directories);
    }
}