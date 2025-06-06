package com.example.todo;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class AddTodosActivity extends BaseActivity {
    private TodoAdapter todoAdapter;
    private SharedPreferences sharedPreferences;
    private static final int DIRECTORY_MANAGEMENT_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private DatabaseReference todosRef;
    private String directoryId;
    private String directoryName;
    private String currentUserId;
    private final ArrayList<Todo> allNotes = new ArrayList<>();
    private String todoId;
    private EditText inputTodo;
    private Bitmap capturedImage = null;
    private ImageView todoImageView;

    private FloatingActionButton deleteButton;
    private static final int PICK_PDF_REQUEST = 2;
    private Uri pdfUri;
    private TextView pdfNameText;
    private String pdfFileName;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        directoryId = intent.getStringExtra("directoryId");
        todoId = intent.getStringExtra("todoId");

        String firebaseURL = "https://todo-61e76-default-rtdb.europe-west1.firebasedatabase.app";
        databaseRef = FirebaseDatabase.getInstance(firebaseURL).getReference("users");
        setContentView(R.layout.add_todo);
        initializeViews();

        setupCameraButton();
        setupDeletePictureButton();

        storageRef = FirebaseStorage.getInstance().getReference();

        pdfNameText = findViewById(R.id.pdfNameText);
        Button attachPdfButton = findViewById(R.id.attachPdfButton);

        attachPdfButton.setOnClickListener(v -> {
            Intent intentPDF = new Intent();
            intentPDF.setType("application/pdf");
            intentPDF.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intentPDF, "Select PDF"), PICK_PDF_REQUEST);
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "anonymous";

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );


        todosRef = databaseRef.child(currentUserId).child("directories").child(directoryId).child("todos");

        if (todoExist()) {
            loadTodoDetails();
//            setCurrentData();
        }
        setPictureVisibility();
    }

    private boolean todoExist() {
        return todoId != null;
    }

    private void loadTodoDetails() {
        if (todoId != null) {
            todosRef.child(todoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String content = snapshot.child("content").getValue(String.class);
                    String encodedImage = snapshot.child("image").getValue(String.class);
                    String pdfName = snapshot.child("name").getValue(String.class);
                    String pdfPath = snapshot.child("path").getValue(String.class);

                    inputTodo.setText(content);

                    if (encodedImage != null && !encodedImage.isEmpty()) {
                        capturedImage = decodeImage(encodedImage);
                        setPictureVisibility();
                    }

                    if (pdfName != null && pdfPath != null) {
                        pdfFileName = pdfName;
                        pdfNameText.setText(pdfName);
                    } else {
                        pdfNameText.setText(R.string.no_pdf_attached);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setPictureVisibility() {
        if (capturedImage == null) {
            todoImageView.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else {
            todoImageView.setImageBitmap(capturedImage);
            todoImageView.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    private void initializeViews() {
        inputTodo = findViewById(R.id.inputTodo);
        todoImageView = findViewById(R.id.todoImage);
        deleteButton = findViewById(R.id.deletePictureButton);
        Button addButton = findViewById(R.id.addButton);

        if (todoExist()) {
            addButton.setText(R.string.edit_note);
        }

        addButton.setOnClickListener(v -> {
            String todoText = inputTodo.getText().toString().trim();
            if (!todoText.isEmpty()) {
                saveTodoWithOptionalImage();
                inputTodo.setText("");
            }
            onBackPressed();
        });
    }

    private void saveTodoWithOptionalImage() {
        String newTodoId = todoExist() ? todoId : todosRef.push().getKey();

        if (newTodoId == null) {
            newTodoId = UUID.randomUUID().toString();
        }

        String todoText = inputTodo.getText().toString().trim();
        Todo todo;

        if (capturedImage == null) {
            todo = new Todo(newTodoId, todoText);
        } else {
            String encodedImage = encodeImage(capturedImage);
            todo = new Todo(newTodoId, todoText, encodedImage);
        }

        String finalNewTodoId = newTodoId;

        if (todoExist()) {
            todosRef.child(todoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    todosRef.child(finalNewTodoId).setValue(todo)
                            .addOnSuccessListener(aVoid -> {
                                if (pdfUri != null) {
                                    uploadPdfToFirebase(finalNewTodoId);
                                } else {
                                    String existingPdfName = snapshot.child("name").getValue(String.class);
                                    String existingPdfPath = snapshot.child("path").getValue(String.class);
                                    if (existingPdfName != null && existingPdfPath != null) {
                                        todosRef.child(finalNewTodoId).child("name").setValue(existingPdfName);
                                        todosRef.child(finalNewTodoId).child("path").setValue(existingPdfPath);
                                    }
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("directoryId", directoryId);
                                    resultIntent.putExtra("isUpdated", true);
                                    setResult(RESULT_OK, resultIntent);
                                    Toast.makeText(AddTodosActivity.this, getString(R.string.todo_saved_successfully), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddTodosActivity.this, getString(R.string.failed_to_load_pdf_data), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            // Nowa notatka
            todosRef.child(finalNewTodoId)
                    .setValue(todo)
                    .addOnSuccessListener(aVoid -> {
                        if (pdfUri != null) {
                            uploadPdfToFirebase(finalNewTodoId);
                        } else {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("directoryId", directoryId);
                            resultIntent.putExtra("isUpdated", true);
                            setResult(RESULT_OK, resultIntent);
                            Toast.makeText(this, getString(R.string.todo_saved_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeImage(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            pdfFileName = getFileName(pdfUri);
            pdfNameText.setText(pdfFileName);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    capturedImage = imageBitmap;
                    setPictureVisibility();
                    Toast.makeText(this, getString(R.string.image_captured), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadPdfToFirebase(String todoId) {
        if (pdfUri != null) {
            try {
                String fileName = "pdf_" + System.currentTimeMillis() + ".pdf";
                java.io.File localFile = new java.io.File(getFilesDir(), fileName);

                java.io.InputStream inputStream = getContentResolver().openInputStream(pdfUri);
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(localFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();

                todosRef.child(todoId).child("name").setValue(fileName);
                todosRef.child(todoId).child("path").setValue(localFile.getAbsolutePath());
                todosRef.child(todoId).child("timestamp").setValue(System.currentTimeMillis())
                        .addOnSuccessListener(aVoid -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("directoryId", directoryId);
                            resultIntent.putExtra("isUpdated", true);
                            setResult(RESULT_OK, resultIntent);
                            Toast.makeText(this, getString(R.string.todo_saved_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, getString(R.string.failed_to_save_pdf) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            finish();
                        });

            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.failed_to_save_pdf) + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                finish();
            }
        }
    }

    private void updateTodoWithPdfUrl(String todoId, String pdfUrl) {
        todosRef.child(todoId).child("pdfUrl").setValue(pdfUrl);
        todosRef.child(todoId).child("pdfName").setValue(pdfFileName);
    }

    private void setupCameraButton() {
        FloatingActionButton cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
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

    private void setupDeletePictureButton() {
        ImageView todoImage = findViewById(R.id.todoImage);

        deleteButton.setOnClickListener(v -> {
            capturedImage = null;
            todoImage.setImageDrawable(null);
            Toast.makeText(this, getString(R.string.image_removed), Toast.LENGTH_SHORT).show();
            setPictureVisibility();
        });
    }
}