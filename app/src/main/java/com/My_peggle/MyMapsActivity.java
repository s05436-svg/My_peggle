package com.My_peggle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyMapsActivity extends AppCompatActivity {

    private ListView lvMyMaps;
    private TextView tvNoMaps;
    private List<Map<String, Object>> levelsList;
    private List<String> levelNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_maps);

        lvMyMaps = findViewById(R.id.lvMyMaps);
        tvNoMaps = findViewById(R.id.tvNoMaps);

        fetchUserLevels();

        lvMyMaps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> selectedLevel = levelsList.get(position);
                List<Map<String, Object>> coordinates = (List<Map<String, Object>>) selectedLevel.get("coordinates");
                
                Intent intent = new Intent(MyMapsActivity.this, GameActivity.class);
                intent.putExtra("CUSTOM_LEVEL", (Serializable) coordinates);
                startActivity(intent);
            }
        });
    }

    private void fetchUserLevels() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("aiGeneratedLevels")) {
                        levelsList = (List<Map<String, Object>>) documentSnapshot.get("aiGeneratedLevels");
                        if (levelsList != null && !levelsList.isEmpty()) {
                            levelNames = new ArrayList<>();
                            for (Map<String, Object> level : levelsList) {
                                String name = (String) level.get("name");
                                levelNames.add(name != null ? name : "Unnamed Level");
                            }
                            
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, levelNames);
                            lvMyMaps.setAdapter(adapter);
                            tvNoMaps.setVisibility(View.GONE);
                        } else {
                            tvNoMaps.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvNoMaps.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load maps", Toast.LENGTH_SHORT).show();
                });
    }
}
