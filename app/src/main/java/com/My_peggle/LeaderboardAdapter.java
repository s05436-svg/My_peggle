package com.My_peggle;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<UserScore> userScores;
    private String currentUserId;

    public LeaderboardAdapter(List<UserScore> userScores) {
        this.userScores = userScores;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserScore userScore = userScores.get(position);
        holder.tvRankPos.setText(String.valueOf(position + 1));
        holder.tvUsername.setText(userScore.getUsername());
        holder.tvLevel.setText(String.valueOf(userScore.getLevel()));
        holder.tvPoints.setText(String.valueOf(userScore.getRank()));

        if (userScore.getUid().equals(currentUserId)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#80FFD700")); // Gold highlight for current user
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#40FFFFFF")); // Default semi-transparent
        }
    }

    @Override
    public int getItemCount() {
        return userScores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRankPos, tvUsername, tvLevel, tvPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRankPos = itemView.findViewById(R.id.tvRankPos);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }
    }

    public static class UserScore {
        private String uid;
        private String username;
        private long rank;
        private long level;

        public UserScore(String uid, String username, long rank, long level) {
            this.uid = uid;
            this.username = username;
            this.rank = rank;
            this.level = level;
        }

        public String getUid() { return uid; }
        public String getUsername() { return username; }
        public long getRank() { return rank; }
        public long getLevel() { return level; }
    }
}
