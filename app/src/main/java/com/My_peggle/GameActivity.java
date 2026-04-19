package com.My_peggle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.My_peggle.ui.CustomSurfaceView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private CustomSurfaceView gameView;
    private LinearLayout gameOverLayout;
    private TextView tvFinalScore;
    private Button btnBackToMenu;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        db = FirebaseFirestore.getInstance();
        
        gameView = findViewById(R.id.gameView);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        gameOverLayout.setVisibility(View.GONE);

        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gameView.setGameOverListener(new CustomSurfaceView.OnGameOverListener() {
            @Override
            public void onGameOver() {
                final int finalScore = gameView.getTotalScore();
                saveScoreToFirestore(finalScore);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGameOverUI(finalScore);
                    }
                });
            }
        });

        loadUserLevelAndInitGame();
    }

    private void loadUserLevelAndInitGame() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long level = 0;
                        if (documentSnapshot.contains("level")) {
                            level = documentSnapshot.getLong("level");
                        }
                        Log.d(TAG, "Loading level: " + level);
                        fetchCoordinatesDataForLevel((int) level, new OnLevelDataLoadedListener() {
                            @Override
                            public void onSuccess(List<Map<String, Object>> coordinates) {
                                gameView.setLevelData(coordinates);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to load level coordinates", e);
                                // מציג את השגיאה המדויקת כדי שנוכל לאבחן
                                Toast.makeText(GameActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public interface OnLevelDataLoadedListener {
        void onSuccess(List<Map<String, Object>> coordinates);
        void onFailure(Exception e);
    }

    private void fetchCoordinatesDataForLevel(int levelNumber, OnLevelDataLoadedListener listener) {
        // וודא שהשם כאן תואם לשם המסמך ב-Firebase (למשל level_0)
        String documentId = "level_" + levelNumber;
        Log.d(TAG, "Fetching document: levels/" + documentId);

        db.collection("levels").document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<Map<String, Object>> coordinatesList =
                                    (List<Map<String, Object>>) document.get("coordinates");

                            if (coordinatesList != null && !coordinatesList.isEmpty()) {
                                listener.onSuccess(coordinatesList);
                            } else {
                                listener.onFailure(new Exception("Field 'coordinates' is empty or missing in " + documentId));
                            }
                        } else {
                            listener.onFailure(new Exception("Document " + documentId + " does not exist in 'levels' collection"));
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    private void saveScoreToFirestore(int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        db.collection("users").document(uid).update("rank", FieldValue.increment(score));
    }

    private void showGameOverUI(int score) {
        tvFinalScore.setText("Final Score: " + score);
        gameOverLayout.setVisibility(View.VISIBLE);
        gameOverLayout.bringToFront();
        gameOverLayout.setAlpha(0f);
        gameOverLayout.animate().alpha(1f).setDuration(600).start();
    }
}
