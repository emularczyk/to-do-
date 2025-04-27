package com.example.todo;

import static com.example.todo.DirectoryAdapter.DIRECTORY_NOTES_REQUEST;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTodosActivity extends AppCompatActivity {

    private ArrayList<Directory> directories;
    private TodoAdapter todoAdapter;
    private SharedPreferences sharedPreferences;

    private static final String DIRECTORIES_KEY = "directories";
    private static final int DIRECTORY_MANAGEMENT_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Directory currentDirectory;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private DatabaseReference todosRef;
    private String directoryId;
    private String directoryName;
    private String currentUserId;
    private ArrayList<String> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        directoryId = intent.getStringExtra("directoryId");  // Directory ID
        directoryName = intent.getStringExtra("directoryName");  // Directory Name (if needed)

        String firebaseURL = "https://to-do-plus-plus-3bb3e-default-rtdb.europe-west1.firebasedatabase.app";
        databaseRef = FirebaseDatabase.getInstance(firebaseURL).getReference("users");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_todo); // Call setContentView first!
        setupCameraButton();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            currentUserId = "anonymous";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Initialize the todosRef
        todosRef = databaseRef.child(currentUserId).child("directories").child(directoryId).child("todos");

        // Initialize RecyclerView and Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // Set layout manager
        todoAdapter = new TodoAdapter(allNotes, this);  // Initialize adapter with empty list
        recyclerView.setAdapter(todoAdapter);  // Set adapter to RecyclerView

        // Load todos from Firebase
        loadTodosFromFirebase();

        initializeViews();
    }

    // Method to load todos from Firebase
    private void loadTodosFromFirebase() {
        todosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allNotes.clear();  // Clear the existing list

                // Iterate through the todos in Firebase and add them to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String todoText = snapshot.child("text").getValue(String.class);
                    if (todoText != null) {
                        allNotes.add(todoText);  // Add the todo text to the list
                    }
                }

                // Notify the adapter to update the RecyclerView
                todoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AddTodosActivity", "Failed to load todos: " + databaseError.getMessage());
                Toast.makeText(AddTodosActivity.this, "Failed to load todos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        EditText inputTodo = findViewById(R.id.inputTodo);
        Button addButton = findViewById(R.id.addButton);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch themeToggleBtn = findViewById(R.id.themeToggleBtn);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        themeToggleBtn.setText(isNightMode ? "Light Mode" : "Dark Mode");
        themeToggleBtn.setChecked(isNightMode);
        themeToggleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("night_mode", isChecked);
                    editor.apply();
                themeToggleBtn.setText(isChecked ? "Light Mode" : "Dark Mode");

        AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        recreate();
    });

        addButton.setOnClickListener(v -> {
            String todoText = inputTodo.getText().toString().trim();
            if (!todoText.isEmpty()) {
                saveTodoToFirebase(todoText);
                inputTodo.setText("");  // Clear input field after adding
            }
        });
    }

    private void saveTodoToFirebase(String todoText) {
        // Generate unique ID for the new todo
        String todoId = databaseRef.push().getKey(); // Firebase automatically generates a unique key

        if (todoId != null) {
            // Create a map for the new todo
            Map<String, Object> todoData = new HashMap<>();
            todoData.put("text", todoText);

            // Save the todo under the correct directory
            databaseRef.child(currentUserId)
                    .child("directories")
                    .child(directoryId)
                    .child("todos")
                    .child(todoId)
                    .setValue(todoData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Todo added successfully!", Toast.LENGTH_SHORT).show();
                        // Optionally, refresh UI here (fetch todos again if needed)
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add todo.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case DIRECTORY_NOTES_REQUEST:
                    int position = data.getIntExtra("directoryPosition", -1);
                    ArrayList<String> updatedNotes = data.getStringArrayListExtra("notes");
                    if (position != -1 && updatedNotes != null) {
                        directories.get(position).getNotes().clear();
                        directories.get(position).getNotes().addAll(updatedNotes);
                        updateNotesList();
                    }
                    break;

                case DIRECTORY_MANAGEMENT_REQUEST:
                    ArrayList<Directory> updatedDirectories = data.getParcelableArrayListExtra("directories");
                    if (updatedDirectories != null) {
                        directories.clear();
                        directories.addAll(updatedDirectories);
                        updateNotesList();
                    }
                    break;
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    saveImageToGallery(imageBitmap);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DIRECTORIES_KEY, directories);
    }

    private void setupCameraButton() {
        FloatingActionButton cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, getString(R.string.camera_no_permission),
                        Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.camera_not_found), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                OutputStream out = getContentResolver().openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                if (out != null) {
                    out.close();
                }

                //TODO: Save photo to note logic
                if (currentDirectory != null) {
                    currentDirectory.addNote("Photo: " + imageUri);
                    //directoryAdapter.notifyDataSetChanged();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("directories", directories);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}