package com.example.todo;

import java.util.HashMap;
import java.util.Map;

public class TestRepository {
    private final Map<String, Todo> todos = new HashMap<>();

    public boolean saveTodo(Todo todo) {
        todos.put(todo.getId(), todo);
        return true;
    }

    public Todo getTodo(String id) {
        return todos.get(id);
    }

    public int getTodosCount() {
        return todos.size();
    }
}