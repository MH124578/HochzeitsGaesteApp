package com.example.hgapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import com.example.hgapp.MainActivity.ImageData;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private final List<ImageData> images;
    private final Activity activity;

    public ImagesAdapter(Activity activity, List<ImageData> images) {
        this.activity = activity;
        this.images = images;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageData imageData = images.get(position);
        Glide.with(activity)
                .load(imageData.getImageUrl())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imageView);
        holder.textView.setText(imageData.getText()); // Text setzen
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView; // TextView-Variable hinzufügen

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageViewItem);
            textView = view.findViewById(R.id.textViewItem); // TextView initialisieren
        }
    }
}