package com.minigalaxy.android.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.minigalaxy.android.R;
import com.minigalaxy.android.model.Download;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
    
    private Context context;
    private List<Download> downloads;
    private OnDownloadActionListener listener;
    
    public interface OnDownloadActionListener {
        void onPauseClick(Download download);
        void onResumeClick(Download download);
        void onCancelClick(Download download);
        void onRetryClick(Download download);
    }
    
    public DownloadAdapter(Context context) {
        this.context = context;
        this.downloads = new ArrayList<>();
    }
    
    public void setOnDownloadActionListener(OnDownloadActionListener listener) {
        this.listener = listener;
    }
    
    public void setDownloads(List<Download> downloads) {
        this.downloads = new ArrayList<>(downloads);
        notifyDataSetChanged();
    }
    
    public void addDownload(Download download) {
        this.downloads.add(0, download); // Add at top
        notifyItemInserted(0);
    }
    
    public void updateDownload(Download updatedDownload) {
        for (int i = 0; i < downloads.size(); i++) {
            if (downloads.get(i).getId().equals(updatedDownload.getId())) {
                downloads.set(i, updatedDownload);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void removeDownload(String downloadId) {
        for (int i = 0; i < downloads.size(); i++) {
            if (downloads.get(i).getId().equals(downloadId)) {
                downloads.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    
    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        Download download = downloads.get(position);
        holder.bind(download);
    }
    
    @Override
    public int getItemCount() {
        return downloads.size();
    }
    
    public class DownloadViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewGameIcon;
        private TextView textViewGameName;
        private TextView textViewFileName;
        private TextView textViewStatus;
        private ProgressBar progressBar;
        private TextView textViewProgress;
        private TextView textViewSpeed;
        private ImageButton buttonPauseResume;
        private ImageButton buttonCancel;
        
        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewGameIcon = itemView.findViewById(R.id.imageViewGameIcon);
            textViewGameName = itemView.findViewById(R.id.textViewGameName);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
            textViewProgress = itemView.findViewById(R.id.textViewProgress);
            textViewSpeed = itemView.findViewById(R.id.textViewSpeed);
            buttonPauseResume = itemView.findViewById(R.id.buttonPauseResume);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);
        }
        
        public void bind(Download download) {
            // Set basic info
            textViewGameName.setText(download.getGameName());
	textViewFileName.setText(download.getFilename());
            
            // Load game icon
            if (download.getGameImageUrl() != null && !download.getGameImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(download.getGameImageUrl())
                    .placeholder(R.drawable.placeholder_game)
                    .error(R.drawable.placeholder_game)
                    .into(imageViewGameIcon);
            } else {
                imageViewGameIcon.setImageResource(R.drawable.placeholder_game);
            }
            
            // Update status and progress
            updateStatus(download);
            updateProgress(download);
            updateButtons(download);
        }
        
        private void updateStatus(Download download) {
            switch (download.getStatus()) {
                case QUEUED:
                    textViewStatus.setText("Na fila");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.grey));
                    break;
                    
                case DOWNLOADING:
                    textViewStatus.setText("Baixando");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.gog_purple));
                    break;
                    
                case PAUSED:
                    textViewStatus.setText("Pausado");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    break;
                    
                case COMPLETED:
                    textViewStatus.setText("Concluído");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
                    break;
                    
                case FAILED:
                    textViewStatus.setText("Erro");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                    break;
                    
                case CANCELLED:
                    textViewStatus.setText("Cancelado");
                    textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.grey));
                    break;
            }
        }
        
        private void updateProgress(Download download) {
            boolean showProgress = download.getStatus() == Download.DownloadStatus.DOWNLOADING ||
                                 download.getStatus() == Download.DownloadStatus.PAUSED;
            
            progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            textViewProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            textViewSpeed.setVisibility(download.getStatus() == Download.DownloadStatus.DOWNLOADING ?
                View.VISIBLE : View.GONE);
            
            if (showProgress) {
                progressBar.setProgress((int) download.getProgress());
                textViewProgress.setText(String.format("%.1f%% (%s/%s)", 
                    download.getProgress(),
	download.getFormattedDownloadedSize(),
                    download.getFormattedTotalSize()));
            }
            
            if (download.getStatus() == Download.DownloadStatus.DOWNLOADING) {
                textViewSpeed.setText(download.getFormattedSpeed());
            }
        }
        
        private void updateButtons(Download download) {
            switch (download.getStatus()) {
                case QUEUED:
                    buttonPauseResume.setImageResource(R.drawable.ic_cancel);
                    buttonPauseResume.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.red)));
                    buttonPauseResume.setOnClickListener(v -> {
                        if (listener != null) listener.onCancelClick(download);
                    });
                    buttonCancel.setVisibility(View.GONE);
                    break;
                    
                case DOWNLOADING:
                    buttonPauseResume.setImageResource(R.drawable.ic_pause);
                    buttonPauseResume.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.orange)));
                    buttonPauseResume.setOnClickListener(v -> {
                        if (listener != null) listener.onPauseClick(download);
                    });
                    buttonCancel.setVisibility(View.VISIBLE);
                    buttonCancel.setOnClickListener(v -> {
                        if (listener != null) listener.onCancelClick(download);
                    });
                    break;
                    
                case PAUSED:
                    buttonPauseResume.setImageResource(R.drawable.ic_play);
                    buttonPauseResume.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonPauseResume.setOnClickListener(v -> {
                        if (listener != null) listener.onResumeClick(download);
                    });
                    buttonCancel.setVisibility(View.VISIBLE);
                    buttonCancel.setOnClickListener(v -> {
                        if (listener != null) listener.onCancelClick(download);
                    });
                    break;
                    
                case FAILED:
                    buttonPauseResume.setImageResource(R.drawable.ic_refresh);
                    buttonPauseResume.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonPauseResume.setOnClickListener(v -> {
                        if (listener != null) listener.onRetryClick(download);
                    });
                    buttonCancel.setVisibility(View.VISIBLE);
                    buttonCancel.setOnClickListener(v -> {
                        if (listener != null) listener.onCancelClick(download);
                    });
                    break;
                    
                case COMPLETED:
                case CANCELLED:
                    buttonPauseResume.setVisibility(View.GONE);
                    buttonCancel.setVisibility(View.GONE);
                    break;
            }
        }
    }
}