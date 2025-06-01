package com.example.healingapp.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.data.models.workout.RunningSession;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListWorkoutAdapter extends RecyclerView.Adapter<ListWorkoutAdapter.RunningSessionViewHolder> {
    private List<RunningSession> runningSessions = new ArrayList<>();

    @NonNull
    @Override
    public RunningSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_workout, parent, false); // Assuming your item layout is named item_running_session.xml
        return new RunningSessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RunningSessionViewHolder holder, int position) {
        RunningSession currentSession = runningSessions.get(position);
        holder.bind(currentSession);
    }

    @Override
    public int getItemCount() {
        return runningSessions.size();
    }

    public void setRunningSessions(List<RunningSession> runningSessions) {
        this.runningSessions = runningSessions;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    static class RunningSessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCalo;
        private final TextView tvDayOfWeek;
        private final TextView textView16; // This is "Running" text
        private final ShapeableImageView avatarImage; // Your image view

        public RunningSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCalo = itemView.findViewById(R.id.tvCalo);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            textView16 = itemView.findViewById(R.id.textView16);
            avatarImage = itemView.findViewById(R.id.avatarImage);
        }

        public void bind(RunningSession session) {
            // Set calories
            tvCalo.setText(String.format(Locale.getDefault(), "%.0f kcal", (float) session.getCalories())); // Assuming calories is a float and you want to display it as kcal

            // Set the "Running" text
            textView16.setText("Running"); // This is static as per your layout

            // Set day of week from timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault()); // EEEE for full day name
            String dayOfWeek = sdf.format(new Date(session.getTimestamp()));
            tvDayOfWeek.setText(dayOfWeek);

            // Set the image (you can keep it static or change based on type if you have more)
            avatarImage.setImageResource(R.drawable.running); // Assuming you have a 'running.xml' drawable
        }
    }
}