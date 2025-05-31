package com.example.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private final ArrayList<Todo> todoList;
    private final Context context;
    private final DatabaseReference databaseRef;
    private final String currentUserId;
    private final String directoryId;

    public TodoAdapter(ArrayList<Todo> todoList, Context context, String directoryId, String currentUserId) {
        this.todoList = todoList;
        this.context = context;
        this.directoryId = directoryId;
        this.currentUserId = currentUserId;
        this.databaseRef = FirebaseDatabase.getInstance("https://todo-61e76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        if (todo.getType().equals("text")) {
            holder.todoText.setVisibility(View.VISIBLE);
            holder.todoImage.setVisibility(View.GONE);
            holder.todoText.setText(todo.getContent());
        } else {
            holder.todoText.setVisibility(View.GONE);
            holder.todoImage.setVisibility(View.VISIBLE);

            // Konwertuj Base64 na bitmap
            try {
                String base64Image = todo.getContent().split(",")[1];
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.todoImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("TodoAdapter", "Error loading image: " + e.getMessage());
                holder.todoImage.setImageResource(R.drawable.ic_error_foreground);
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (todo.getType().equals("text")) {
                showEditDialog(position, todo);
            } else {
                showDeleteDialog(position, todo);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    private void showEditDialog(int position, Todo todo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.edit_note);

        final EditText input = new EditText(context);
        input.setText(todo.getContent());
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                // Najpierw aktualizuj obiekt lokalnie
                todo.setContent(newText);

                // Przygotuj pełną ścieżkę do notatki w Firebase
                DatabaseReference todoRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUserId)
                        .child("directories")
                        .child(directoryId)
                        .child("todos")
                        .child(todo.getId());

                // Zapisz cały obiekt do Firebase
                todoRef.setValue(todo)
                        .addOnSuccessListener(aVoid -> {
                            // Po udanym zapisie zaktualizuj widok
                            todoList.set(position, todo);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Note updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // W przypadku błędu, przywróć starą wartość
                            Todo oldTodo = todoList.get(position);
                            todo.setContent(oldTodo.getContent());
                            Toast.makeText(context, "Failed to update note", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteDialog(int position, Todo todo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_note);
        builder.setMessage(R.string.delete_note_confirmation);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            deleteTodoFromFirebase(todo, position);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void updateTodoInFirebase(Todo todo) {
        DatabaseReference todoRef = databaseRef.child(currentUserId)
                .child("directories")
                .child(directoryId)
                .child("todos")
                .child(todo.getId());

        todoRef.setValue(todo)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TodoAdapter", "Note updated successfully");
                    Toast.makeText(context, "Note updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("TodoAdapter", "Failed to update note: " + e.getMessage());
                    Toast.makeText(context, "Failed to update note", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteTodoFromFirebase(Todo todo, int position) {
        deleteFromDatabase(todo, position);
    }

    private void deleteFromDatabase(Todo todo, int position) {
        databaseRef.child(currentUserId)
                .child("directories")
                .child(directoryId)
                .child("todos")
                .child(todo.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    todoList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show());
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView todoText;
        ImageView todoImage;

        TodoViewHolder(View itemView) {
            super(itemView);
            todoText = itemView.findViewById(R.id.todoText);
            todoImage = itemView.findViewById(R.id.todoImage);
        }
    }
}