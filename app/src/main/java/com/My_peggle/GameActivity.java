package com.My_peggle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.My_peggle.ui.CustomSurfaceView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private CustomSurfaceView gameView;
    private LinearLayout gameOverLayout;
    private TextView tvFinalScore;
    private Button btnBackToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // Initialize views
        gameView = findViewById(R.id.gameView);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Reset visibility just in case
        gameOverLayout.setVisibility(View.GONE);

        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close game and return to MenuActivity
            }
        });

        // Set listener for Game Over
        gameView.setGameOverListener(new CustomSurfaceView.OnGameOverListener() {
            @Override
            public void onGameOver() {
                final int finalScore = gameView.getTotalScore();
                
                // Save score to Firestore
                saveScoreToFirestore(finalScore);

                // Update UI on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGameOverUI(finalScore);
                    }
                });
            }
        });
    }

    private void saveScoreToFirestore(int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(uid);

        // Atomically increment the rank by the game score
        userRef.update("rank", FieldValue.increment(score))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully updated rank with score: " + score))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update rank", e));
    }

    private void showGameOverUI(int score) {
        tvFinalScore.setText("Final Score: " + score);
        gameOverLayout.setVisibility(View.VISIBLE);
        gameOverLayout.bringToFront();
        
        // Simple animation
        gameOverLayout.setAlpha(0f);
        gameOverLayout.setTranslationY(-100f);
        gameOverLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start();
    }
}
