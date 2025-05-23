package com.example.healingapp.ui.workout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.data.models.GridModelList;

import java.util.ArrayList;

public class SelectWorkoutAdapter extends RecyclerView.Adapter<SelectWorkoutAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<GridModelList> gridArrayList;
    private final LayoutInflater inflater;

    public SelectWorkoutAdapter(Context context, ArrayList<GridModelList> list) {
        this.context = context;
        this.gridArrayList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SelectWorkoutAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.grid_layout_item_select_workout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectWorkoutAdapter.ViewHolder holder, int position) {

        GridModelList list = gridArrayList.get(position);

        holder.imageView.setImageResource(list.getIcon());
        holder.textView.setText(list.getTitle());
        holder.setClickmethod(position);
    }

    @Override
    public int getItemCount() {
        return gridArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final ImageView imageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView10);
            imageView = itemView.findViewById(R.id.imageView7);
        }

        public void setClickmethod(int position) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GridModelList list = gridArrayList.get(position);
                    Toast.makeText(context, "tessssss", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
