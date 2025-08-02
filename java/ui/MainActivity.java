package com.minigalaxy.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.minigalaxy.android.R;
import com.minigalaxy.android.auth.AuthenticationManager;
import com.minigalaxy.android.config.Config;
import com.minigalaxy.android.download.DownloadManager;
import com.minigalaxy.android.model.Download;
import com.minigalaxy.android.model.Game;
import com.minigalaxy.android.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements 
        GameRepository.GameRepositoryListener,
        DownloadManager.DownloadManagerListener {
    
    private RecyclerView gamesRecyclerView;
    private TextInputEditText searchEditText;
    private CircularProgressIndicator loadingProgress;
    private View emptyStateLayout;
    private FloatingActionButton fabRefresh;
    
    private Config config;
    private AuthenticationManager authManager;
    private GameRepository gameRepository;
    private DownloadManager downloadManager;
    
    private List<Game> allGames = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        setupUI();
        checkAuthentication();
    }
    
    private void initializeComponents() {
        config = new Config(this);
        authManager = new AuthenticationManager(this, config);
        gameRepository = new GameRepository(this, config);
        downloadManager = new DownloadManager(this, config);
        
        gameRepository.addListener(this);
        downloadManager.addListener(this);
    }
    
    private void setupUI() {
        gamesRecyclerView = findViewById(R.id.games_recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        loadingProgress = findViewById(R.id.loading_progress);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        fabRefresh = findViewById(R.id.fab_refresh);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        
        gamesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        fabRefresh.setOnClickListener(v -> refreshLibrary());
    }
    
    private void checkAuthentication() {
        if (!authManager.isAuthenticated()) {
            startLoginActivity();
        } else {
            loadLibrary();
        }
    }
    
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void loadLibrary() {
        gameRepository.syncGames(false);
    }
    
    private void refreshLibrary() {
        gameRepository.syncGames(true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            authManager.logout();
            startLoginActivity();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // GameRepository.GameRepositoryListener implementation
    @Override
    public void onLoadingStarted() {
        runOnUiThread(() -> {
            loadingProgress.setVisibility(View.VISIBLE);
            fabRefresh.hide();
        });
    }
    
    @Override
    public void onGamesLoaded(List<Game> games) {
        runOnUiThread(() -> {
            loadingProgress.setVisibility(View.GONE);
            fabRefresh.show();
            
            allGames.clear();
            allGames.addAll(games);
            
            // Update UI
            if (games.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                gamesRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                gamesRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onGameUpdated(Game game) {
        runOnUiThread(() -> {
            // Update game in list
            for (int i = 0; i < allGames.size(); i++) {
                if (allGames.get(i).getId() == game.getId()) {
                    allGames.set(i, game);
                    break;
                }
            }
        });
    }
    
    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            loadingProgress.setVisibility(View.GONE);
            fabRefresh.show();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }
    
    // DownloadManager.DownloadManagerListener implementation
    @Override
    public void onDownloadAdded(Download download) {
        // Update downloads UI
    }
    
    @Override
    public void onDownloadStarted(Download download) {
        // Update downloads UI
    }
    
    @Override
    public void onDownloadProgress(Download download) {
        // Update progress
    }
    
    @Override
    public void onDownloadCompleted(Download download) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Download completed: " + download.getFilename(), 
                    Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onDownloadFailed(Download download, String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Download failed: " + download.getFilename(), 
                    Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (gameRepository != null) {
            gameRepository.removeListener(this);
            gameRepository.cleanup();
        }
        
        if (downloadManager != null) {
            downloadManager.removeListener(this);
            downloadManager.cleanup();
        }
        
        if (authManager != null) {
            authManager.cleanup();
        }
    }
}