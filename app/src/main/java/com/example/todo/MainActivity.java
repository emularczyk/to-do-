package com.example.todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Directory> directories;
    private DirectoryAdapter directoryAdapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        directories = new ArrayList<>();
        if (getIntent().hasExtra("directories")) {
            ArrayList<Directory> receivedDirectories = getIntent().getParcelableArrayListExtra("directories");
            if (receivedDirectories != null) {
                directories.addAll(receivedDirectories);
            }
        }

        initializeViews();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.directoriesRecyclerView);
        Button addDirectoryButton = findViewById(R.id.addDirectoryButton);

        directoryAdapter = new DirectoryAdapter(directories, this);
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
                directories.add(new Directory(directoryName));
                directoryAdapter.notifyItemInserted(directories.size() - 1);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
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
        mAuth.signOut(); // wylogowujemy użytkownika z Firebase Authentication
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // kończymy aktualną aktywność, aby nie móc wrócić wstecz
    }
}
