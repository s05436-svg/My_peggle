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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Auto-login check
        if (auth.getCurrentUser() != null) {
            Log.i(TAG, "User already signed in, navigating to MenuActivity");
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            // We can still try to get a display name or email to pass as USERNAME
            String displayName = auth.getCurrentUser().getEmail();
            if (displayName != null && displayName.contains("@")) {
                displayName = displayName.split("@")[0];
            }
            intent.putExtra("USERNAME", displayName);
            startActivity(intent);
            finish();
        }

        emailEditText = findViewById(R.id.etUsername); // Mapping username field to emailEditText as per instructions
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
        String input = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (input.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "Empty username/email and/or password field");
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Convert username to email format if it doesn't look like one
        final String username = input;
        String email = input.contains("@") ? input : input + "@peggle.com";

        // Perform Firebase authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithEmail:success");
                            startMenuActivity(true, username);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            String errorMessage = "Authentication failed. ";
                            if (task.getException() != null) {
                                errorMessage += task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startMenuActivity(boolean sendToast, String username) {
        if(sendToast)
            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

        // Navigate to MenuActivity (referred to as FeedActivity in instructions)
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }
}
