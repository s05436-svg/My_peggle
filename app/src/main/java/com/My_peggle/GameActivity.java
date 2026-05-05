package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.My_peggle.ui.CustomSurfaceView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private CustomSurfaceView gameView;
    private LinearLayout gameOverLayout;
    private LinearLayout pauseMenuLayout;
    private LinearLayout leaderboardOverlay;
    private TextView tvFinalScore;
    private Button btnBackToMenu;
    private Button btnQuit;
    private Button btnPause;
    private Button btnResume;
    private Button btnLeaderboard;
    private Button btnPauseQuit;
    private Button btnBackFromLeaderboard;
    
    private RecyclerView rvGameLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardAdapter.UserScore> leaderboardList;
    private ProgressBar pbGameLeaderboard;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        db = FirebaseFirestore.getInstance();
        
        gameView = findViewById(R.id.gameView);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        pauseMenuLayout = findViewById(R.id.pauseMenuLayout);
        leaderboardOverlay = findViewById(R.id.leaderboardOverlay);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnQuit = findViewById(R.id.btnQuit);
        btnPause = findViewById(R.id.btnPause);
        btnResume = findViewById(R.id.btnResume);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnPauseQuit = findViewById(R.id.btnPauseQuit);
        btnBackFromLeaderboard = findViewById(R.id.btnBackFromLeaderboard);

        rvGameLeaderboard = findViewById(R.id.rvGameLeaderboard);
        pbGameLeaderboard = findViewById(R.id.pbGameLeaderboard);

        leaderboardList = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(leaderboardList);
        rvGameLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvGameLeaderboard.setAdapter(leaderboardAdapter);

        gameOverLayout.setVisibility(View.GONE);
        pauseMenuLayout.setVisibility(View.GONE);
        leaderboardOverlay.setVisibility(View.GONE);

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseGame();
            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeGame();
            }
        });

        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaderboardOverlay();
            }
        });

        btnBackFromLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLeaderboardOverlay();
            }
        });

        View.OnClickListener quitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        };

        btnQuit.setOnClickListener(quitListener);
        btnPauseQuit.setOnClickListener(quitListener);
        btnBackToMenu.setOnClickListener(quitListener);

        gameView.setGameOverListener(new CustomSurfaceView.OnGameOverListener() {
            @Override
            public void onGameOver(boolean isWin) {
                final int finalScore = gameView.getTotalScore();
                saveScoreToFirestore(finalScore, isWin);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnQuit.setVisibility(View.GONE);
                        btnPause.setVisibility(View.GONE);
                        showGameOverUI(finalScore, isWin);
                    }
                });
            }
        });

        // Check if a custom level was passed
        int customLevel = getIntent().getIntExtra("CUSTOM_LEVEL", -1);

        if(customLevel != -1)
        {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;

            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("aiGeneratedLevels")) {
                            List<Map<String, Object>> levelsList = (List<Map<String, Object>>) documentSnapshot.get("aiGeneratedLevels");
                            if (levelsList != null && !levelsList.isEmpty()) {
                                Map<String, Object> selectedLevel = levelsList.get(customLevel);
                                List<Map<String, Object>> customCoordinates = (List<Map<String, Object>>) selectedLevel.get("coordinates");

                                if (customCoordinates != null) {
                                    gameView.setLevelData(customCoordinates);
                                } else {
                                    loadUserLevelAndInitGame();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load maps", Toast.LENGTH_SHORT).show();
                    });
        }
        else
        {
            loadUserLevelAndInitGame();
        }
    }

    private void pauseGame() {
        gameView.setPaused(true);
        pauseMenuLayout.setVisibility(View.VISIBLE);
        pauseMenuLayout.bringToFront();
    }

    private void resumeGame() {
        gameView.setPaused(false);
        pauseMenuLayout.setVisibility(View.GONE);
    }

    private void showLeaderboardOverlay() {
        pauseMenuLayout.setVisibility(View.GONE);
        leaderboardOverlay.setVisibility(View.VISIBLE);
        leaderboardOverlay.bringToFront();
        fetchLeaderboard();
    }

    private void hideLeaderboardOverlay() {
        leaderboardOverlay.setVisibility(View.GONE);
        pauseMenuLayout.setVisibility(View.VISIBLE);
    }

    private void fetchLeaderboard() {
        pbGameLeaderboard.setVisibility(View.VISIBLE);
        db.collection("users")
                .orderBy("rank", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        pbGameLeaderboard.setVisibility(View.GONE);
                        if (task.isSuccessful() && task.getResult() != null) {
                            leaderboardList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uid = document.getId();
                                String username = document.getString("username");
                                if (username == null) username = "Unknown";
                                long rank = 0;
                                if (document.contains("rank")) {
                                    rank = document.getLong("rank");
                                }
                                long level = 0;
                                if (document.contains("level")) {
                                    level = document.getLong("level");
                                }
                                leaderboardList.add(new LeaderboardAdapter.UserScore(uid, username, rank, level));
                            }
                            leaderboardAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error fetching leaderboard", task.getException());
                            Toast.makeText(GameActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (leaderboardOverlay.getVisibility() == View.VISIBLE) {
            hideLeaderboardOverlay();
        } else if (pauseMenuLayout.getVisibility() == View.VISIBLE) {
            resumeGame();
        } else if (gameOverLayout.getVisibility() != View.VISIBLE) {
            pauseGame();
        } else {
            super.onBackPressed();
        }
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

    private void saveScoreToFirestore(int score, boolean isWin) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.update("rank", FieldValue.increment(score));
        
        if (isWin) {
            userRef.update("level", FieldValue.increment(1));
        }
    }

    private void showGameOverUI(int score, boolean isWin) {
        String status = isWin ? "YOU WIN!" : "GAME OVER";
        tvFinalScore.setText(status + "\nFinal Score: " + score);
        gameOverLayout.setVisibility(View.VISIBLE);
        gameOverLayout.bringToFront();
        gameOverLayout.setAlpha(0f);
        gameOverLayout.animate().alpha(1f).setDuration(600).start();
    }
}
