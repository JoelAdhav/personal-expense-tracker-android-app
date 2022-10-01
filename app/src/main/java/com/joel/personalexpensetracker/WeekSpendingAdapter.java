package com.joel.personalexpensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeekSpendingAdapter extends RecyclerView.Adapter<WeekSpendingAdapter.ViewHolder> {

    private Context mContext;
    private List<Data> myDataList;

    public WeekSpendingAdapter(Context mContext, List<Data> myDataList) {
        this.mContext = mContext;
        this.myDataList = myDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.retrieve_layout, parent, false);
        return new WeekSpendingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Data data = myDataList.get(position);

        holder.item.setText("Item : "+ data.getItem());
        holder.date.setText("Date : "+ data.getDate());
        holder.note.setText("Note : "+ data.getNotes());
        holder.amount.setText("Amount : "+ data.getAmount());

        switch (data.getItem()){
            case "Transport":
                holder.imageView.setImageResource(R.drawable.transport);
                break;
            case "Education":
                holder.imageView.setImageResource(R.drawable.education);
                break;
            case "House":
                holder.imageView.setImageResource(R.drawable.house);
                break;
            case "Food":
                holder.imageView.setImageResource(R.drawable.food);
                break;
            case "Health":
                holder.imageView.setImageResource(R.drawable.health);
                break;
            case "Apparel":
                holder.imageView.setImageResource(R.drawable.apparel);
                break;
            case "Personal":
                holder.imageView.setImageResource(R.drawable.personal);
                break;
            case "Other":
                holder.imageView.setImageResource(R.drawable.other);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView date, note, amount, item;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            note = itemView.findViewById(R.id.note);
            amount = itemView.findViewById(R.id.amount);
            item = itemView.findViewById(R.id.item);
            imageView = itemView.findViewById(R.id.custom);

        }
    }
}
