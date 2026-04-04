package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuActivity extends AppCompatActivity {

    private TextView welcomeText, rankText, levelText;
    private Button btnStartGame;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        welcomeText = findViewById(R.id.welcomeText);
        rankText = findViewById(R.id.rankText);
        levelText = findViewById(R.id.levelText);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnLogout = findViewById(R.id.btnLogout);

        String username = getIntent().getStringExtra("USERNAME");
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Welcome, " + username + "!");
        }

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

        // Initialize buttons as invisible for animation
        btnStartGame.setVisibility(View.INVISIBLE);
        btnLogout.setVisibility(View.INVISIBLE);

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startMenuAnimations();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats whenever we return to this screen
        fetchUserStats();
    }

    private void fetchUserStats() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                long rank = 0;
                                long level = 0;
                                
                                if (task.getResult().contains("rank")) {
                                    rank = task.getResult().getLong("rank");
                                }
                                if (task.getResult().contains("level")) {
                                    level = task.getResult().getLong("level");
                                }
                                
                                rankText.setText("Rank: " + rank);
                                levelText.setText("Level: " + level);
                                
                                String username = task.getResult().getString("username");
                                if (username != null) {
                                    welcomeText.setText("Welcome, " + username + "!");
                                }
                            }
                        }
                    });
        }
    }

    private void startMenuAnimations() {
        float screenHeight = getResources().getDisplayMetrics().heightPixels;

        btnStartGame.setTranslationY(screenHeight);
        btnStartGame.setVisibility(View.VISIBLE);
        btnStartGame.animate()
                .translationY(0)
                .setDuration(1000)
                .setStartDelay(0)
                .start();

        btnLogout.setTranslationY(screenHeight);
        btnLogout.setVisibility(View.VISIBLE);
        btnLogout.animate()
                .translationY(0)
                .setDuration(1000)
                .setStartDelay(500)
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
