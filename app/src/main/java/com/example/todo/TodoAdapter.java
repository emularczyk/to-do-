package com.example.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private final ArrayList<String> todoList;
    private final Context context;

    public TodoAdapter(ArrayList<String> todoList, Context context) {
        this.todoList = todoList;
        this.context = context;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        holder.todoText.setText(todoList.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            showEditDialog(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    private void showEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.edit_note);

        final EditText input = new EditText(context);
        input.setText(todoList.get(position));
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                todoList.set(position, newText);
                notifyItemChanged(position);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView todoText;

        TodoViewHolder(View itemView) {
            super(itemView);
            todoText = itemView.findViewById(R.id.todoText);
        }
    }
}