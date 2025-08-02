package com.minigalaxy.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.minigalaxy.android.R;

import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {
    
    private Context context;
    private List<PermissionItem> permissions;
    
    public static class PermissionItem {
        private String name;
        private String description;
        private int iconRes;
        private boolean isGranted;
        
        public PermissionItem(String name, String description, int iconRes, boolean isGranted) {
            this.name = name;
            this.description = description;
            this.iconRes = iconRes;
            this.isGranted = isGranted;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getIconRes() { return iconRes; }
        public boolean isGranted() { return isGranted; }
        
        // Setters
        public void setGranted(boolean granted) { this.isGranted = granted; }
    }
    
    public PermissionAdapter(Context context, List<PermissionItem> permissions) {
        this.context = context;
        this.permissions = permissions;
    }
    
    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_permission, parent, false);
        return new PermissionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        PermissionItem permission = permissions.get(position);
        holder.bind(permission);
    }
    
    @Override
    public int getItemCount() {
        return permissions.size();
    }
    
    public class PermissionViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewIcon;
        private ImageView imageViewStatus;
        private TextView textViewName;
        private TextView textViewDescription;
        
        public PermissionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
        
        public void bind(PermissionItem permission) {
            textViewName.setText(permission.getName());
            textViewDescription.setText(permission.getDescription());
            imageViewIcon.setImageResource(permission.getIconRes());
            
            if (permission.isGranted()) {
                imageViewStatus.setImageResource(R.drawable.ic_check);
                imageViewStatus.setColorFilter(ContextCompat.getColor(context, R.color.green));
                textViewName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                textViewDescription.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                imageViewStatus.setImageResource(R.drawable.ic_error);
                imageViewStatus.setColorFilter(ContextCompat.getColor(context, R.color.red));
                textViewName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                textViewDescription.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }
    }
}