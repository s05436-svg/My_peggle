package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.My_peggle.utils.RegistrationManager;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.etUsername);
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButtonClick();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Goes back to LoginActivity
            }
        });
    }

    private void registerButtonClick() {
        Log.d(TAG, "Register button clicked");

        // We use the text from usernameEditText because RegistrationManager 
        // will convert it to a valid email format internally (username@peggle.com)
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistrationManager registrationManager = new RegistrationManager(RegisterActivity.this);
        registrationManager.startRegistration(
                username,
                password,
                new RegistrationManager.OnResultCallback(){
                    @Override
                    public void onResult(boolean success, String message) {
                        if (success) {
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
