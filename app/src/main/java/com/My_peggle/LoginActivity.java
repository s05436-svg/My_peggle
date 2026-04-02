package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Auto-login check
        if (auth.getCurrentUser() != null) {
            Log.i(TAG, "User already signed in, fetching data...");
            fetchUserDataAndNavigate(auth.getCurrentUser().getUid());
        }

        emailEditText = findViewById(R.id.etEmail); // Fixed: ID is etEmail in XML
        passwordEditText = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        if (emailEditText == null || passwordEditText == null) {
            Log.e(TAG, "EditText fields are null. Check layout XML IDs.");
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Add default domain if not an email for convenience
        if (!email.contains("@")) {
            email = email + "@peggle.com";
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "signInWithEmail:success");
                            fetchUserDataAndNavigate(auth.getCurrentUser().getUid());
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String msg = "Authentication failed.";
                            if (task.getException() != null) msg = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserDataAndNavigate(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String username = "Player";
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            username = task.getResult().getString("username");
                        }
                        
                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
