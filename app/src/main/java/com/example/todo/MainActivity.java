package com.example.todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {
    private ArrayList<Directory> directories;
    private DirectoryAdapter directoryAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        String firebaseURL = "https://todo-61e76-default-rtdb.europe-west1.firebasedatabase.app";
        databaseRef = FirebaseDatabase.getInstance(firebaseURL).getReference("users");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            currentUserId = "anonymous";
        }

        directories = new ArrayList<>();
        initializeViews();
        fetchDirectories();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.directoriesRecyclerView);
        Button addDirectoryButton = findViewById(R.id.addDirectoryButton);

        // Przekazanie currentUserId do adaptera
        directoryAdapter = new DirectoryAdapter(directories, this, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(directoryAdapter);

        addDirectoryButton.setOnClickListener(v -> showAddDirectoryDialog());
    }

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

    private void deleteDirectoryFromFirebase(String directoryId) {
        databaseRef.child(currentUserId).child("directories").child(directoryId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Directory deleted!", Toast.LENGTH_SHORT).show();
                    fetchDirectories();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete directory.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void fetchDirectories() {
        databaseRef.child(currentUserId).child("directories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                directories.clear();
                for (DataSnapshot directorySnapshot : snapshot.getChildren()) {
                    String id = directorySnapshot.child("id").getValue(String.class);
                    String name = directorySnapshot.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        directories.add(new Directory(id, name));
                    }
                }
                directoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load directories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("directories", directories);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}