package com.example.todo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.loginEmail);
        passwordInput = findViewById(R.id.loginPassword);

        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordButton);
        forgotPasswordText.setOnClickListener(v -> resetPassword());

        TextView registerText = findViewById(R.id.registerButton);
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String pass = passwordInput.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Wprowadź email i hasło", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Logowanie nieudane:\n" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void resetPassword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resetowanie hasła");

        final EditText emailInputField = new EditText(this);
        emailInputField.setHint("Podaj email");
        builder.setView(emailInputField);

        builder.setPositiveButton("Wyślij", (dialog, which) -> {
            String email = emailInputField.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Email nie może być pusty", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Jeżeli użytkownik istnieje mail do resetu hasła został wysłany.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Błąd: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
