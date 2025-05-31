package com.example.todo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends BaseActivity {
    private DirectoryAdapter directoryAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String currentUserId;
    private ArrayList<Directory> directoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        directoryList = new ArrayList<>();
        directoryAdapter = new DirectoryAdapter(directoryList, this);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        String firebaseURL = "https://todo-61e76-default-rtdb.europe-west1.firebasedatabase.app";
        databaseRef = FirebaseDatabase.getInstance(firebaseURL).getReference("users");
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            currentUserId = "anonymous";
        }
        fetchDirectories();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.directoriesRecyclerView);
        Button addDirectoryButton = findViewById(R.id.addDirectoryButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(directoryAdapter);

        addDirectoryButton.setOnClickListener(v -> showAddDirectoryDialog());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAddDirectoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_directory);

        final EditText input = new EditText(this);
        input.setHint(R.string.directory_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String directoryName = input.getText().toString().trim();
            if (!directoryName.isEmpty()) {
                saveDirectoryToFirebase(directoryName);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveDirectoryToFirebase(String directoryName) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> directoryData = new HashMap<>();
        directoryData.put("id", id);
        directoryData.put("name", directoryName);

        databaseRef.child(currentUserId).child("directories").child(id).setValue(directoryData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Directory added!", Toast.LENGTH_SHORT).show();
                    fetchDirectories();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add directory.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDirectories();
    }

    private void fetchDirectories() {
        if (databaseRef != null && currentUserId != null) {
            databaseRef.child(currentUserId).child("directories").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    directoryList.clear();
                    for (DataSnapshot directorySnapshot : snapshot.getChildren()) {
                        String id = directorySnapshot.child("id").getValue(String.class);
                        String name = directorySnapshot.child("name").getValue(String.class);

                        if (id != null && name != null) {
                            Directory directory = new Directory(id, name);

                            databaseRef.child(currentUserId).child("directories").child(id).child("todos")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot todosSnapshot) {
                                            for (DataSnapshot todoSnapshot : todosSnapshot.getChildren()) {
                                                directory.addNote(todoSnapshot.getKey());
                                            }
                                            directoryAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(MainActivity.this, "Failed to load notes count.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            directoryList.add(directory);
                        }
                    }
                    directoryAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load directories.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Database reference or user ID is null.", Toast.LENGTH_SHORT).show();
        }
    }
}