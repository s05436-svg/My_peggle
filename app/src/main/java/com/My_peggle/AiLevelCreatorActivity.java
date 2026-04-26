package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.My_peggle.utils.GeminiManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiLevelCreatorActivity extends AppCompatActivity {

    private EditText etAiPrompt;
    private Button btnGenerateLevel, btnMyMaps;
    private ProgressBar pbGenerating;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_level_creator);

        etAiPrompt = findViewById(R.id.etAiPrompt);
        btnGenerateLevel = findViewById(R.id.btnGenerateLevel);
        btnMyMaps = findViewById(R.id.btnMyMaps);
        pbGenerating = findViewById(R.id.pbGenerating);
        tvStatus = findViewById(R.id.tvStatus);

        btnGenerateLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateLevel();
            }
        });

        btnMyMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AiLevelCreatorActivity.this, MyMapsActivity.class));
            }
        });
    }

    private void generateLevel() {
        String userPrompt = etAiPrompt.getText().toString().trim();
        if (userPrompt.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CALCULATION BASED ON GAME LOGIC (Ball.java) ---
        // In landscape mode, the game area is centered.
        // Screen width is usually around 2000-2400, height around 1000.
        // Logic from Ball.java: 
        // gameAreaWidth = screenHeight;
        // gameLeft = (screenWidth - screenHeight) / 2;
        // gameRight = gameLeft + screenHeight;
        //
        // Assuming standard landscape (e.g., 2200x1000):
        // gameAreaWidth = 1000.
        // gameLeft = (2200 - 1000) / 2 = 600.
        // gameRight = 1600.
        //
        // To be safe, we'll tell the AI that the coordinates are RELATIVE to a square area,
        // but it's better to provide the exact absolute boundaries.
        // --------------------------------------------------

        String systemPrompt = "You are a level designer for a Peggle-like game. " +
                "The game is in landscape mode. The 'playing field' is a square in the center of the screen. " +
                "Assume the total screen width is 2200 and height is 1000. " +
                "The square playing field is between X=650 and X=1550, and Y=0 and Y=1000. " +
                "IMPORTANT CONSTRAINTS FOR COORDINATES: " +
                "- X must be between 700 and 1500 (to stay inside the side walls). " +
                "- Y must be between 250 and 850 (to stay below the cannon and above the hole). " +
                "- Pegs must be at least 60 units apart. " +
                "Return the response ONLY as a JSON array of objects, where each object has 'x' (double) and 'y' (double) fields. " +
                "Example format: [{\"x\": 800.0, \"y\": 300.0}, {\"x\": 1400.0, \"y\": 550.0}] " +
                "User request: " + userPrompt;

        setLoading(true);
        tvStatus.setText("Generating level with Gemini...");

        GeminiManager.getInstance().sendText(systemPrompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                String cleanResult = result.replace("```json", "").replace("```", "").trim();
                try {
                    JSONArray jsonArray = new JSONArray(cleanResult);
                    List<Map<String, Object>> coordinates = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Map<String, Object> point = new HashMap<>();
                        point.put("x", obj.getDouble("x"));
                        point.put("y", obj.getDouble("y"));
                        coordinates.add(point);
                    }
                    saveLevelToFirestore(userPrompt, coordinates);
                } catch (JSONException e) {
                    setLoading(false);
                    tvStatus.setText("Error parsing AI response.");
                    Log.e("AiLevelCreator", "JSON Error: " + cleanResult, e);
                }
            }

            @Override
            public void onError(Throwable error) {
                setLoading(false);
                tvStatus.setText("Error: " + error.getMessage());
                Log.e("AiLevelCreator", "Gemini error", error);
            }
        });
    }

    private void saveLevelToFirestore(String prompt, List<Map<String, Object>> coordinates) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        tvStatus.setText("Saving level to Firestore...");

        Map<String, Object> newLevel = new HashMap<>();
        newLevel.put("name", prompt.length() > 20 ? prompt.substring(0, 20) + "..." : prompt);
        newLevel.put("coordinates", coordinates);
        newLevel.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("aiGeneratedLevels", FieldValue.arrayUnion(newLevel))
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    tvStatus.setText("Level saved successfully!");
                    Toast.makeText(AiLevelCreatorActivity.this, "Level added to 'My Maps'!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Map<String, Object> initialData = new HashMap<>();
                    List<Map<String, Object>> levelsList = new ArrayList<>();
                    levelsList.add(newLevel);
                    initialData.put("aiGeneratedLevels", levelsList);
                    
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(initialData, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> {
                                setLoading(false);
                                tvStatus.setText("Level saved successfully!");
                            })
                            .addOnFailureListener(e2 -> {
                                setLoading(false);
                                tvStatus.setText("Failed to save level.");
                            });
                });
    }

    private void setLoading(boolean isLoading) {
        pbGenerating.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGenerateLevel.setEnabled(!isLoading);
        etAiPrompt.setEnabled(!isLoading);
    }
}
