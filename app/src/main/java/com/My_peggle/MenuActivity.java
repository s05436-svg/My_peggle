package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        String username = getIntent().getStringExtra("USERNAME");
        TextView welcomeText = findViewById(R.id.welcomeText);
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Welcome, " + username + "!");
        }

        final Button btnStartGame = findViewById(R.id.btnStartGame);
        final Button btnLogout = findViewById(R.id.btnLogout);

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        // Use a GlobalLayoutListener to ensure the view is laid out before starting animations
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startMenuAnimations(btnStartGame, btnLogout);
            }
        });
    }

    private void startMenuAnimations(final View btnStart, final View btnLogout) {
        float screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 1. Setup Start Game Button
        btnStart.setTranslationY(screenHeight); // Start from off-screen bottom
        btnStart.setVisibility(View.VISIBLE);
        btnStart.animate()
                .translationY(0) // Move to its original XML position (center-right)
                .setDuration(1000)
                .setStartDelay(0)
                .start();

        // 2. Setup Logout Button
        btnLogout.setTranslationY(screenHeight); // Start from off-screen bottom
        btnLogout.setVisibility(View.VISIBLE);
        btnLogout.animate()
                .translationY(0) // Move to its original XML position (bottom-right)
                .setDuration(1000)
                .setStartDelay(500) // Delay by half a second
                .start();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
