package com.example.todo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

public class DetailsActivity extends BaseActivity {
    private EditText inputTodo;
    private ImageView todoImageView;
    private String todoID, directoryID, currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference todoRef;
    private Bitmap capturedImage = null;
    private TextView pdfNameText;
    private Button openPdfButton;
    private Button shareBluetoothButton;
    private String pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        inputTodo = findViewById(R.id.inputTodo);
        todoImageView = findViewById(R.id.todoImage);
        pdfNameText = findViewById(R.id.pdfNameText);
        openPdfButton = findViewById(R.id.openPdfButton);
        Button editButton = findViewById(R.id.editButton);
        shareBluetoothButton = findViewById(R.id.shareBluetoothButton);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "anonymous";

        Intent intent = getIntent();
        todoID = intent.getStringExtra("todoId");
        directoryID = intent.getStringExtra("directoryId");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String firebaseURL = "https://todo-61e76-default-rtdb.europe-west1.firebasedatabase.app";
        todoRef = FirebaseDatabase.getInstance(firebaseURL)
                .getReference("users")
                .child(currentUserId)
                .child("directories")
                .child(directoryID)
                .child("todos")
                .child(todoID);

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, AddTodosActivity.class);
            editIntent.putExtra("todoId", todoID);
            editIntent.putExtra("directoryId", directoryID);
            startActivityForResult(editIntent, 100);
        });

        openPdfButton.setOnClickListener(v -> {
            if (pdfUrl != null) {
                try {
                    File pdfFile = new File(pdfUrl);
                    if (pdfFile.exists()) {
                        Uri contentUri = FileProvider.getUriForFile(this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                pdfFile);

                        Intent intentPDF = new Intent(Intent.ACTION_VIEW);
                        intentPDF.setDataAndType(contentUri, "application/pdf");
                        intentPDF.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        try {
                            startActivity(intentPDF);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Nie znaleziono aplikacji do otwarcia PDF", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Plik PDF nie istnieje", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Błąd podczas otwierania PDF: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Brak załączonego pliku PDF", Toast.LENGTH_SHORT).show();
            }
        });

        shareBluetoothButton.setOnClickListener(v -> shareNoteViaBluetooth());
    }

    private void loadTodoDetails() {
        todoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Intent intent = new Intent(DetailsActivity.this, TodosActivity.class);
                    intent.putExtra("directoryId", directoryID);
                    startActivity(intent);
                    finish();
                    return;
                }

                String content = snapshot.child("content").getValue(String.class);
                String encodedImage = snapshot.child("image").getValue(String.class);
                String fileName = snapshot.child("name").getValue(String.class);
                String filePath = snapshot.child("path").getValue(String.class);

                inputTodo.setText(content);

                if (encodedImage != null && !encodedImage.isEmpty()) {
                    capturedImage = decodeImage(encodedImage);
                    todoImageView.setImageBitmap(capturedImage);
                    todoImageView.setVisibility(View.VISIBLE);
                } else {
                    todoImageView.setVisibility(View.GONE);
                }

                if (fileName != null && !fileName.isEmpty() && filePath != null && !filePath.isEmpty()) {
                    pdfNameText.setText(fileName);
                    openPdfButton.setVisibility(View.VISIBLE);
                    pdfUrl = filePath;
                } else {
                    pdfNameText.setText(R.string.no_pdf_attached);
                    openPdfButton.setVisibility(View.GONE);
                    pdfUrl = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsActivity.this, "Nie udało się załadować szczegółów notatki", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DetailsActivity.this, TodosActivity.class);
                intent.putExtra("directoryId", directoryID);
                startActivity(intent);
                finish();
            }
        });
    }

    private void shareNoteViaBluetooth() {
        String noteContent = inputTodo.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, noteContent);

        try {
            startActivity(Intent.createChooser(intent, "Udostępnij notatkę"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nie znaleziono aplikacji do udostępniania", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap decodeImage(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Intent intent = new Intent(this, TodosActivity.class);
            intent.putExtra("directoryId", directoryID);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodoDetails();
    }
}