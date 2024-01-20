package com.example.greeting_screen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greeting_screen.model.HomeInformationEntry;

import java.util.List;

public class HomeInformationAdapter extends RecyclerView.Adapter<HomeInformationAdapter.ViewHolder> {

    private List<HomeInformationEntry> entries;

    // Constructor
    public HomeInformationAdapter(List<HomeInformationEntry> entries) {
        this.entries = entries;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView; // Adjust as needed
        TextView descriptionTextView;
        TextView informationTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView); // Replace with the actual ID from your item layout
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            informationTimeTextView = itemView.findViewById((R.id.informationTimeTextView));
        }
    }

    // Inflate the item layout and create the ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_information_entry_layout, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeInformationEntry entry = entries.get(position);
        holder.titleTextView.setText(entry.getTitle()); // Adjust as needed
        holder.descriptionTextView.setText(entry.getDescription());
        holder.informationTimeTextView.setText((entry.getInformation_time()));
    }

    // Return the number of items in the list
    @Override
    public int getItemCount() {
        return entries.size();
    }

    // Update the data and notify the adapter
    public void setData(List<HomeInformationEntry> newData) {
        entries.clear();
        entries.addAll(newData);
        notifyDataSetChanged();
    }
}