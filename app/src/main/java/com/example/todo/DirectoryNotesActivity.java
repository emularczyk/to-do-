package com.example.todo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DirectoryNotesActivity extends AppCompatActivity {
    private ArrayList<String> notes;
    private TodoAdapter notesAdapter;
    private int directoryPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_notes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("directoryName"));
        }

        directoryPosition = getIntent().getIntExtra("directoryPosition", -1);
        notes = getIntent().getStringArrayListExtra("notes");
        if (notes == null) notes = new ArrayList<>();

        EditText inputNote = findViewById(R.id.inputNote);
        Button addNoteButton = findViewById(R.id.addNoteButton);
        RecyclerView recyclerView = findViewById(R.id.notesRecyclerView);

        notesAdapter = new TodoAdapter(notes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notesAdapter);

        addNoteButton.setOnClickListener(v -> {
            String noteText = inputNote.getText().toString().trim();
            if (!noteText.isEmpty()) {
                notes.add(noteText);
                notesAdapter.notifyItemInserted(notes.size() - 1);
                inputNote.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("directoryPosition", directoryPosition);
        resultIntent.putStringArrayListExtra("notes", notes);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}