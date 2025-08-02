package com.minigalaxy.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.minigalaxy.android.R;
import com.minigalaxy.android.model.Game;
import com.minigalaxy.android.ui.GameDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    
    public enum ViewType {
        GRID, LIST
    }
    
    private Context context;
    private List<Game> games;
    private List<Game> filteredGames;
    private ViewType viewType;
    private OnGameActionListener listener;
    
    public interface OnGameActionListener {
        void onGameClick(Game game);
        void onDownloadClick(Game game);
        void onInstallClick(Game game);
        void onPlayClick(Game game);
        void onPauseClick(Game game);
        void onResumeClick(Game game);
    }
    
    public GameAdapter(Context context, ViewType viewType) {
        this.context = context;
        this.viewType = viewType;
        this.games = new ArrayList<>();
        this.filteredGames = new ArrayList<>();
    }
    
    public void setOnGameActionListener(OnGameActionListener listener) {
        this.listener = listener;
    }
    
    public void setGames(List<Game> games) {
        this.games = new ArrayList<>(games);
        this.filteredGames = new ArrayList<>(games);
        notifyDataSetChanged();
    }
    
    public void addGames(List<Game> newGames) {
        int startPosition = this.games.size();
        this.games.addAll(newGames);
        this.filteredGames.addAll(newGames);
        notifyItemRangeInserted(startPosition, newGames.size());
    }
    
    public void updateGame(Game updatedGame) {
        for (int i = 0; i < filteredGames.size(); i++) {
            if (filteredGames.get(i).getId() == updatedGame.getId()) {
                filteredGames.set(i, updatedGame);
                notifyItemChanged(i);
                break;
            }
        }
        
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getId() == updatedGame.getId()) {
                games.set(i, updatedGame);
                break;
            }
        }
    }
    
    public void filter(String query) {
        filteredGames.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredGames.addAll(games);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Game game : games) {
                if (game.getName().toLowerCase().contains(lowerCaseQuery) ||
                    (game.getDeveloper() != null && game.getDeveloper().toLowerCase().contains(lowerCaseQuery)) ||
                    (game.getGenre() != null && game.getGenre().toLowerCase().contains(lowerCaseQuery))) {
                    filteredGames.add(game);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        return viewType == ViewType.GRID ? 0 : 1;
    }
    
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        
        if (viewType == 0) { // Grid
            view = inflater.inflate(R.layout.item_game_grid, parent, false);
        } else { // List
            view = inflater.inflate(R.layout.item_game_list, parent, false);
        }
        
        return new GameViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = filteredGames.get(position);
        holder.bind(game);
    }
    
    @Override
    public int getItemCount() {
        return filteredGames.size();
    }
    
    public class GameViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewCover;
        private TextView textViewTitle;
        private TextView textViewDeveloper;
        private TextView textViewSize;
        private TextView textViewStatusBadge;
        private Button buttonAction;
        private ProgressBar progressBar;
        private TextView textViewProgress;
        
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewCover = itemView.findViewById(R.id.imageViewCover);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDeveloper = itemView.findViewById(R.id.textViewDeveloper);
            textViewSize = itemView.findViewById(R.id.textViewSize);
            textViewStatusBadge = itemView.findViewById(R.id.textViewStatusBadge);
            buttonAction = itemView.findViewById(R.id.buttonAction);
            progressBar = itemView.findViewById(R.id.progressBar);
            textViewProgress = itemView.findViewById(R.id.textViewProgress);
        }
        
        public void bind(Game game) {
            // Set basic info
            textViewTitle.setText(game.getName());
            
            if (textViewDeveloper != null) {
                textViewDeveloper.setText(game.getDeveloper());
                textViewDeveloper.setVisibility(
                    game.getDeveloper() != null && !game.getDeveloper().isEmpty() ? 
                    View.VISIBLE : View.GONE);
            }
            
            if (textViewSize != null) {
                textViewSize.setText(game.getFormattedSize());
            }
            
            // Load cover image
            if (imageViewCover != null) {
                String imageUrl = game.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_game)
                        .error(R.drawable.placeholder_game)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewCover);
                } else {
                    imageViewCover.setImageResource(R.drawable.placeholder_game);
                }
            }
            
            // Set status badge
            updateStatusBadge(game);
            
            // Set action button
            updateActionButton(game);
            
            // Set progress
            updateProgress(game);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGameClick(game);
                } else {
                    // Default action - open details
                    Intent intent = new Intent(context, GameDetailsActivity.class);
                    intent.putExtra(GameDetailsActivity.EXTRA_GAME_ID, game.getId());
                    context.startActivity(intent);
                }
            });
        }
        
        private void updateStatusBadge(Game game) {
            if (textViewStatusBadge == null) return;
            
            switch (game.getDownloadState()) {
                case NOT_DOWNLOADED:
                    textViewStatusBadge.setVisibility(View.GONE);
                    break;
                    
                case DOWNLOADING:
                    textViewStatusBadge.setVisibility(View.VISIBLE);
                    textViewStatusBadge.setText("Baixando");
                    textViewStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.orange)));
                    break;
                    
                case PAUSED:
                    textViewStatusBadge.setVisibility(View.VISIBLE);
                    textViewStatusBadge.setText("Pausado");
                    textViewStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.grey)));
                    break;
                    
                case DOWNLOADED:
                    textViewStatusBadge.setVisibility(View.VISIBLE);
                    textViewStatusBadge.setText("Baixado");
                    textViewStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.green)));
                    break;
                    
                case INSTALLED:
                    textViewStatusBadge.setVisibility(View.VISIBLE);
                    textViewStatusBadge.setText("Instalado");
                    textViewStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.blue)));
                    break;
                    
                case ERROR:
                    textViewStatusBadge.setVisibility(View.VISIBLE);
                    textViewStatusBadge.setText("Erro");
                    textViewStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.red)));
                    break;
            }
        }
        
        private void updateActionButton(Game game) {
            if (buttonAction == null) return;
            
            switch (game.getDownloadState()) {
                case NOT_DOWNLOADED:
                    buttonAction.setText("Download");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onDownloadClick(game);
                    });
                    break;
                    
                case DOWNLOADING:
                    buttonAction.setText("Pausar");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.orange)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onPauseClick(game);
                    });
                    break;
                    
                case PAUSED:
                    buttonAction.setText("Continuar");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.gog_purple)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onResumeClick(game);
                    });
                    break;
                    
                case DOWNLOADED:
                    buttonAction.setText("Instalar");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.green)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onInstallClick(game);
                    });
                    break;
                    
                case INSTALLED:
                    buttonAction.setText("Jogar");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.blue)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onPlayClick(game);
                    });
                    break;
                    
                case ERROR:
                    buttonAction.setText("Tentar Novamente");
                    buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.red)));
                    buttonAction.setOnClickListener(v -> {
                        if (listener != null) listener.onDownloadClick(game);
                    });
                    break;
            }
        }
        
        private void updateProgress(Game game) {
            boolean showProgress = game.getDownloadState() == Game.DownloadState.DOWNLOADING;
            
            if (progressBar != null) {
                progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
                if (showProgress) {
                    progressBar.setProgress((int) game.getDownloadProgress());
                }
            }
            
            if (textViewProgress != null) {
                textViewProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
                if (showProgress) {
                    textViewProgress.setText(String.format("%.1f%%", game.getDownloadProgress()));
                }
            }
        }
    }
}