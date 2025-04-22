package com.example.todo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends ComponentActivity {
    private ArrayList<String> todoList;
    private TodoAdapter todoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        EditText inputTodo = findViewById(R.id.inputTodo);
        Button addButton = findViewById(R.id.addButton);

        todoAdapter = new TodoAdapter(todoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(todoAdapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoText = inputTodo.getText().toString().trim();
                Log.d("MainActivity", "Add button clicked");
                if (!todoText.isEmpty()) {
                    todoList.add(todoText);
                    todoAdapter.notifyItemInserted(todoList.size() - 1);
                    inputTodo.setText("");
                }
            }
        });


        inputTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoText = inputTodo.getText().toString().trim();
                // Log the input value
                Log.d("TODO_APP", "Set " + todoText);

            }
        });
    }
}