package com.minigalaxy.android.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.minigalaxy.android.R;
import com.minigalaxy.android.model.DLC;

import java.util.ArrayList;
import java.util.List;

public class DLCAdapter extends RecyclerView.Adapter<DLCAdapter.DLCViewHolder> {
    
    private Context context;
    private List<DLC> dlcList;
    private OnDLCActionListener listener;
    
    public interface OnDLCActionListener {
        void onDLCClick(DLC dlc);
        void onDownloadClick(DLC dlc);
        void onInstallClick(DLC dlc);
        void onPauseClick(DLC dlc);
        void onResumeClick(DLC dlc);
    }
    
    public DLCAdapter(Context context) {
        this.context = context;
        this.dlcList = new ArrayList<>();
    }
    
    public void setOnDLCActionListener(OnDLCActionListener listener) {
        this.listener = listener;
    }
    
    public void setDLCList(List<DLC> dlcList) {
        this.dlcList = new ArrayList<>(dlcList);
        notifyDataSetChanged();
    }
    
    public void updateDLC(DLC updatedDLC) {
        for (int i = 0; i < dlcList.size(); i++) {
            if (dlcList.get(i).getId().equals(updatedDLC.getId())) {
                dlcList.set(i, updatedDLC);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    @NonNull
    @Override
    public DLCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dlc, parent, false);
        return new DLCViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DLCViewHolder holder, int position) {
        DLC dlc = dlcList.get(position);
        holder.bind(dlc);
    }
    
    @Override
    public int getItemCount() {
        return dlcList.size();
    }
    
    public class DLCViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewDLCIcon;
        private TextView textViewDLCName;
        private TextView textViewDLCSize;
        private Button buttonDLCAction;
        private ProgressBar progressBarDLC;
        
        public DLCViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewDLCIcon = itemView.findViewById(R.id.imageViewDLCIcon);
            textViewDLCName = itemView.findViewById(R.id.textViewDLCName);
            textViewDLCSize = itemView.findViewById(R.id.textViewDLCSize);
            buttonDLCAction = itemView.findViewById(R.id.buttonDLCAction);
            progressBarDLC = itemView.findViewById(R.id.progressBarDLC);
        }
        
        public void bind(DLC dlc) {
            // Set basic info
            textViewDLCName.setText(dlc.getName());
            textViewDLCSize.setText(dlc.getFormattedSize());
            
            // Load DLC icon
            if (dlc.getImageUrl() != null && !dlc.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(dlc.getImageUrl())
                    .placeholder(R.drawable.placeholder_game)
                    .error(R.drawable.placeholder_game)
                    .into(imageViewDLCIcon);
            } else {
                imageViewDLCIcon.setImageResource(R.drawable.placeholder_game);
            }
            
            // Update action button
            updateActionButton(dlc);
            
            // Update progress
            updateProgress(dlc);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDLCClick(dlc);
                }
            });
        }
        
        private void updateActionButton(DLC dlc) {
            switch (dlc.getDownloadState()) {
                case NOT_DOWNLOADED:
                    buttonDLCAction.setText("Download");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonDLCAction.setOnClickListener(v -> {
                        if (listener != null) listener.onDownloadClick(dlc);
                    });
                    buttonDLCAction.setEnabled(true);
                    break;
                    
                case DOWNLOADING:
                    buttonDLCAction.setText("Pausar");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.orange)));
                    buttonDLCAction.setOnClickListener(v -> {
                        if (listener != null) listener.onPauseClick(dlc);
                    });
                    buttonDLCAction.setEnabled(true);
                    break;
                    
                case PAUSED:
                    buttonDLCAction.setText("Continuar");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonDLCAction.setOnClickListener(v -> {
                        if (listener != null) listener.onResumeClick(dlc);
                    });
                    buttonDLCAction.setEnabled(true);
                    break;
                    
                case DOWNLOADED:
                    buttonDLCAction.setText("Instalar");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.green)));
                    buttonDLCAction.setOnClickListener(v -> {
                        if (listener != null) listener.onInstallClick(dlc);
                    });
                    buttonDLCAction.setEnabled(true);
                    break;
                    
                case INSTALLED:
                    buttonDLCAction.setText("Instalado");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.blue)));
                    buttonDLCAction.setOnClickListener(null);
                    buttonDLCAction.setEnabled(false);
                    break;
                    
                case ERROR:
                    buttonDLCAction.setText("Tentar Novamente");
                    buttonDLCAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.red)));
                    buttonDLCAction.setOnClickListener(v -> {
                        if (listener != null) listener.onDownloadClick(dlc);
                    });
                    buttonDLCAction.setEnabled(true);
                    break;
            }
        }
        
        private void updateProgress(DLC dlc) {
            boolean showProgress = dlc.getDownloadState() == DLC.DownloadState.DOWNLOADING;
            
            progressBarDLC.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            
            if (showProgress) {
                progressBarDLC.setProgress((int) dlc.getDownloadProgress());
            }
        }
    }
}