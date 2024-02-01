package com.example.hgapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.PopupMenu;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Random;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private final List<ImageData> images;
    private final MainActivity activity;

    public ImagesAdapter(MainActivity activity, List<ImageData> images) {
        this.activity = activity;
        this.images = images;
    }

    public List<ImageData> getImages() {
        return images;
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
        Random random = new Random();
        float rotationAngle = random.nextFloat() * 10 - 5; // Zufälliger Winkel zwischen -5 und +5 Grad
        holder.imageView.setRotation(rotationAngle);
        holder.textView.setText(imageData.getText());
        holder.menuImageView.setOnClickListener(v -> showPopupMenu(holder.menuImageView, imageData));
        holder.checkBox.setChecked(imageData.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            imageData.setSelected(isChecked);
        });
    }

    private void showPopupMenu(View view, ImageData imageData) {
        PopupMenu popup = new PopupMenu(activity, view);
        popup.inflate(R.menu.image_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit) {
                activity.showEditDialog(imageData.getEntryId(), imageData.getText());
                return true;
            } else if (item.getItemId() == R.id.delete) {
                confirmAndDeleteImage(imageData.getEntryId());
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmAndDeleteImage(int entryId) {
        new AlertDialog.Builder(activity)
                .setTitle("Bild löschen")
                .setMessage("Sind Sie sicher, dass Sie dieses Bild löschen möchten?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> activity.deleteImage(entryId))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public ImageView menuImageView;
        public CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageViewItem);
            textView = view.findViewById(R.id.textViewItem);
            menuImageView = view.findViewById(R.id.menuImageView);
            checkBox = view.findViewById(R.id.checkboxDownload);
        }
    }
}