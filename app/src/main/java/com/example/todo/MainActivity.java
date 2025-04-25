package com.example.todo;

import static com.example.todo.DirectoryAdapter.DIRECTORY_NOTES_REQUEST;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Directory> directories;
    private TodoAdapter todoAdapter;
    private SharedPreferences sharedPreferences;

    DatabaseReference databaseRef;

    private TextView userText; // Moved here
    private Button loginButton; // Moved here
    private static final String DIRECTORIES_KEY = "directories";
    private static final int DIRECTORY_MANAGEMENT_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Directory currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Call setContentView first!
        setupCameraButton();

        // Initialize views after setContentView
//        userText = findViewById(R.id.userText); // Initialize after setContentView
//        loginButton = findViewById(R.id.loginButton); // Initialize after setContentView

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        directories = new ArrayList<>();
        if (savedInstanceState != null) {
            directories = savedInstanceState.getParcelableArrayList(DIRECTORIES_KEY);
        }
        if (directories == null || directories.isEmpty()) {
            directories = new ArrayList<>();
            directories.add(new Directory(getString(R.string.main_directory)));
        }

        if (!directories.isEmpty()) {
            currentDirectory = directories.get(0);
        }

        initializeViews();

        // Get a reference to the database
//        String firebaseURL = "https://to-do-plus-plus-3bb3e-default-rtdb.europe-west1.firebasedatabase.app";
//        databaseRef = FirebaseDatabase.getInstance(firebaseURL).getReference("users");

        //-----------------------------------------------------------------------------------------
        // Send test data
        // Create a map of data
//        Map<String, Object> user = new HashMap<>();
//        user.put("username", "testuser");
//        user.put("email", "test@example.com");
//
//        // Save data under 'users/user1'
//        databaseRef.child("user1").setValue(user)
//                .addOnSuccessListener(aVoid -> {
//                    // Data saved successfully
//                    System.out.println("Data saved!");
//                })
//                .addOnFailureListener(e -> {
//                    System.out.println("Data not saved!");
//                    // Failed to save data
//                    e.printStackTrace();
//                });
        //-----------------------------------------------------------------------------------------
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

        // Check if the user is logged in
//        String loggedUser = sharedPreferences.getString("logged_user", null);
//        if (loggedUser != null) {
//            userText.setText("Witaj, " + loggedUser);
//            loginButton.setText("Wyloguj");
//        } else {
//            userText.setText("Nie zalogowano");
//            loginButton.setText("Zaloguj się");
//        }
//
//        loginButton.setOnClickListener(v -> {
//            if (sharedPreferences.getString("logged_user", null) != null) {
//                // Log out
//                sharedPreferences.edit().remove("logged_user").apply();
//                recreate(); // Refresh the activity
//            } else {
//                // Go to login screen
//                finish(); // Close current activity to prevent duplicates
//            }
//        });
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
}