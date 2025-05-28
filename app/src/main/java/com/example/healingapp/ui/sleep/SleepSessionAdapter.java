package com.example.healingapp.ui.sleep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.data.models.sleep.SleepSession;

public class SleepSessionAdapter extends ListAdapter<SleepSession, SleepSessionAdapter.SleepSessionViewHolder> {
    public SleepSessionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SleepSession> DIFF_CALLBACK = new DiffUtil.ItemCallback<SleepSession>() {
        @Override
        public boolean areItemsTheSame(@NonNull SleepSession oldItem, @NonNull SleepSession newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SleepSession oldItem, @NonNull SleepSession newItem) {
            return oldItem.getStartTimeMillis() == newItem.getStartTimeMillis() &&
                    oldItem.getEndTimeMillis() == newItem.getEndTimeMillis() &&
                    oldItem.getDurationMillis() == newItem.getDurationMillis() &&
                    oldItem.getCreationDateMillis() == newItem.getCreationDateMillis();
        }
    };

    @NonNull
    @Override
    public SleepSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_sleep_session, parent, false);
        return new SleepSessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepSessionViewHolder holder, int position) {
        SleepSession currentSession = getItem(position);
        holder.textViewStartTime.setText("Bắt đầu: " + currentSession.getFormattedStartTime());
        holder.textViewEndTime.setText("Kết thúc: " + currentSession.getFormattedEndTime());
        holder.textViewDuration.setText("Thời gian ngủ: " + currentSession.getFormattedDuration());
        holder.textViewCreationDate.setText("Tạo lúc: " + currentSession.getFormattedCreationDate()); // GÁN DỮ LIỆU
    }

    static class SleepSessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewStartTime;
        private final TextView textViewEndTime;
        private final TextView textViewDuration;
        private final TextView textViewCreationDate; // THÊM THAM CHIẾU

        public SleepSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.textViewStartTime);
            textViewEndTime = itemView.findViewById(R.id.textViewEndTime);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            textViewCreationDate = itemView.findViewById(R.id.textViewCreationDate); // LẤY THAM CHIẾU
        }
    }
}
