package com.minigalaxy.android.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.minigalaxy.android.R;
import com.minigalaxy.android.api.GameRepository;
import com.minigalaxy.android.download.DownloadManager;
import com.minigalaxy.android.model.Download;
import com.minigalaxy.android.model.Game;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_GAME_ID = "game_id";
    
    private ExecutorService executorService;
    private GameRepository gameRepository;
    private DownloadManager downloadManager;
    
    private Game currentGame;
    
    // UI Components
    private ImageView imageViewCover;
    private TextView textViewTitle;
    private TextView textViewDeveloper;
    private TextView textViewPublisher;
    private TextView textViewGenre;
    private TextView textViewReleaseDate;
    private TextView textViewSize;
    private TextView textViewDescription;
    private Button buttonAction;
    private ProgressBar progressBarDownload;
    private TextView textViewProgress;
    private RecyclerView recyclerViewDLC;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);
        
        initializeViews();
        initializeServices();
        
        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (gameId != null) {
            loadGameDetails(gameId);
        } else {
            finish();
        }
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        imageViewCover = findViewById(R.id.imageViewCover);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDeveloper = findViewById(R.id.textViewDeveloper);
        textViewPublisher = findViewById(R.id.textViewPublisher);
        textViewGenre = findViewById(R.id.textViewGenre);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewSize = findViewById(R.id.textViewSize);
        textViewDescription = findViewById(R.id.textViewDescription);
        buttonAction = findViewById(R.id.buttonAction);
        progressBarDownload = findViewById(R.id.progressBarDownload);
        textViewProgress = findViewById(R.id.textViewProgress);
        recyclerViewDLC = findViewById(R.id.recyclerViewDLC);
        
        recyclerViewDLC.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void initializeServices() {
        executorService = Executors.newFixedThreadPool(2);
        gameRepository = GameRepository.getInstance(this);
        downloadManager = DownloadManager.getInstance(this);
    }
    
    private void loadGameDetails(String gameId) {
        executorService.execute(() -> {
            currentGame = gameRepository.getGameById(gameId);
            if (currentGame != null) {
                runOnUiThread(this::updateUI);
                
                // Fetch detailed info from API
                gameRepository.getGameDetails(gameId, new GameRepository.GameDetailsCallback() {
                    @Override
                    public void onSuccess(Game game) {
                        currentGame = game;
                        runOnUiThread(() -> updateUI());
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(GameDetailsActivity.this, 
                                "Erro ao carregar detalhes: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Jogo não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void updateUI() {
        if (currentGame == null) return;
        
        // Load cover image
        if (currentGame.getImageUrl() != null && !currentGame.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(currentGame.getImageUrl())
                .placeholder(R.drawable.placeholder_game)
                .error(R.drawable.placeholder_game)
                .into(imageViewCover);
        }
        
        // Set text content
        textViewTitle.setText(currentGame.getName());
        textViewDeveloper.setText(currentGame.getDeveloper() != null ? 
            "Desenvolvedor: " + currentGame.getDeveloper() : "");
        textViewPublisher.setText(currentGame.getPublisher() != null ? 
            "Publicadora: " + currentGame.getPublisher() : "");
        textViewGenre.setText(currentGame.getGenre() != null ? 
            "Gênero: " + currentGame.getGenre() : "");
        textViewReleaseDate.setText(currentGame.getReleaseDate() != null ? 
            "Lançamento: " + currentGame.getReleaseDate() : "");
        textViewSize.setText(currentGame.getFormattedSize());
        textViewDescription.setText(currentGame.getDescription() != null ? 
            currentGame.getDescription() : "Descrição não disponível");
        
        updateActionButton();
        updateDownloadProgress();
    }
    
    private void updateActionButton() {
        if (currentGame == null) return;
        
        switch (currentGame.getDownloadState()) {
            case NOT_DOWNLOADED:
                buttonAction.setText("Download");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.gog_purple)));
                buttonAction.setOnClickListener(v -> startDownload());
                buttonAction.setEnabled(true);
                break;
                
            case DOWNLOADING:
                buttonAction.setText("Pausar");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.orange)));
                buttonAction.setOnClickListener(v -> pauseDownload());
                buttonAction.setEnabled(true);
                break;
                
            case PAUSED:
                buttonAction.setText("Continuar");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.gog_purple)));
                buttonAction.setOnClickListener(v -> resumeDownload());
                buttonAction.setEnabled(true);
                break;
                
            case DOWNLOADED:
                buttonAction.setText("Instalar");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.green)));
                buttonAction.setOnClickListener(v -> installGame());
                buttonAction.setEnabled(true);
                break;
                
            case INSTALLED:
                buttonAction.setText("Jogar");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.blue)));
                buttonAction.setOnClickListener(v -> launchGame());
                buttonAction.setEnabled(true);
                break;
                
            case ERROR:
                buttonAction.setText("Tentar Novamente");
                buttonAction.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.red)));
                buttonAction.setOnClickListener(v -> retryDownload());
                buttonAction.setEnabled(true);
                break;
        }
    }
    
    private void updateDownloadProgress() {
        if (currentGame == null) return;
        
        boolean isDownloading = currentGame.getDownloadState() == Game.DownloadState.DOWNLOADING;
        progressBarDownload.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
        textViewProgress.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
        
        if (isDownloading) {
            Download download = downloadManager.getDownloadForGame(currentGame.getId());
            if (download != null) {
                progressBarDownload.setProgress((int) download.getProgress());
                textViewProgress.setText(String.format("%.1f%% - %s/s", 
                    download.getProgress(), download.getFormattedSpeed()));
            }
        }
    }
    
    private void startDownload() {
        if (currentGame == null) return;
        
        executorService.execute(() -> {
            gameRepository.getDownloadLinks(currentGame.getId(), new GameRepository.DownloadLinksCallback() {
                @Override
                public void onSuccess(Game game) {
                    downloadManager.addDownload(game);
                    currentGame.setDownloadState(Game.DownloadState.DOWNLOADING);
                    runOnUiThread(() -> updateActionButton());
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(GameDetailsActivity.this, 
                            "Erro ao iniciar download: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
    
    private void pauseDownload() {
        if (currentGame != null) {
            downloadManager.pauseDownload(currentGame.getId());
            currentGame.setDownloadState(Game.DownloadState.PAUSED);
            updateActionButton();
        }
    }
    
    private void resumeDownload() {
        if (currentGame != null) {
            downloadManager.resumeDownload(currentGame.getId());
            currentGame.setDownloadState(Game.DownloadState.DOWNLOADING);
            updateActionButton();
        }
    }
    
    private void retryDownload() {
        if (currentGame != null) {
            downloadManager.retryDownload(currentGame.getId());
            currentGame.setDownloadState(Game.DownloadState.DOWNLOADING);
            updateActionButton();
        }
    }
    
    private void installGame() {
        Toast.makeText(this, "Instalação ainda não implementada", Toast.LENGTH_SHORT).show();
    }
    
    private void launchGame() {
        Toast.makeText(this, "Lançamento de jogos ainda não implementado", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_details_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_uninstall) {
            uninstallGame();
            return true;
        } else if (id == R.id.action_remove_from_library) {
            removeFromLibrary();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void uninstallGame() {
        Toast.makeText(this, "Desinstalação ainda não implementada", Toast.LENGTH_SHORT).show();
    }
    
    private void removeFromLibrary() {
        Toast.makeText(this, "Remoção da biblioteca ainda não implementada", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}