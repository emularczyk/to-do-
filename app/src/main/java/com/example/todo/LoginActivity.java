//package com.example.todo;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class LoginActivity extends AppCompatActivity {
//    EditText etEmail, etPassword;
//    Button btnLogin;
//    //DatabaseHelper db;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_login);
////
////        etEmail = findViewById(R.id.etEmail);
////        etPassword = findViewById(R.id.etPassword);
////        btnLogin = findViewById(R.id.btnLogin);
////        //db = new DatabaseHelper(this);
//
//        btnLogin.setOnClickListener(v -> {
//            String email = etEmail.getText().toString();
//            String password = etPassword.getText().toString();
//
////            if (db.checkUser(email, password)) {
////                // Zapisz użytkownika do SharedPreferences
////                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
////                prefs.edit().putString("logged_user", email).apply();
////
////                // Powrót do MainActivity
////                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
////                startActivity(intent);
////                finish();
////            } else {
////                Toast.makeText(this, "Nieprawidłowe dane", Toast.LENGTH_SHORT).show();
////            }
//        });
//    }
//}
//
