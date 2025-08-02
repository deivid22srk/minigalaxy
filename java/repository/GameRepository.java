package com.minigalaxy.android.repository;

import android.content.Context;
import android.util.Log;

import com.minigalaxy.android.api.ApiResponse;
import com.minigalaxy.android.api.GogApi;
import com.minigalaxy.android.config.Config;
import com.minigalaxy.android.model.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing game data
 */
public class GameRepository {
    
    private static final String TAG = "GameRepository";
    
    private final GogApi gogApi;
    private final Config config;
    private final ExecutorService executorService;
    
    // In-memory cache
    private final Map<Long, Game> gameCache = new HashMap<>();
    private final List<Game> allGames = new ArrayList<>();
    private long lastSyncTime = 0;
    
    // Listeners
    private final List<GameRepositoryListener> listeners = new ArrayList<>();
    private static GameRepository instance;
    
    private GameRepository(Context context) {
        this.config = Config.getInstance(context);
        this.gogApi = new GogApi(config);
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public static synchronized GameRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GameRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Sync games from GOG API
     */
    public void syncGames(boolean forceRefresh) {
        // Check if we need to refresh
        long currentTime = System.currentTimeMillis();
        if (!forceRefresh && 
            (currentTime - lastSyncTime) < config.getConcurrentDownloads() * 60 * 1000) {
            // Return cached data if recent enough
            notifyGamesLoaded(new ArrayList<>(allGames));
            return;
        }
        
        executorService.execute(() -> {
            try {
                notifyLoadingStarted();
                
                ApiResponse<List<Game>> response = gogApi.getUserGames();
                
                if (response.isSuccess()) {
                    List<Game> games = response.getData();
                    
                    synchronized (this) {
                        allGames.clear();
                        gameCache.clear();
                        
                        for (Game game : games) {
                            allGames.add(game);
                            gameCache.put(game.getId(), game);
                        }
                        
                        lastSyncTime = currentTime;
                    }
                    
                    notifyGamesLoaded(games);
                } else {
                    notifyError(response.getErrorMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing games", e);
                notifyError("Failed to sync games: " + e.getMessage());
            }
        });
    }

    /**
     * Get download links for a game
     */
    public void getDownloadLinks(long gameId, DownloadLinksCallback callback) {
        executorService.execute(() -> {
            try {
                // This is a placeholder implementation
                // In a real app, this would make an API call to get the download links
                Game game = getGameById(gameId);
                if (game != null) {
                    // Simulate a successful response with the game's existing URL
                    callback.onSuccess(game);
                } else {
                    callback.onError("Game not found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting download links", e);
                callback.onError("Failed to get download links: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get all games (cached)
     */
    public List<Game> getAllGames() {
        synchronized (this) {
            return new ArrayList<>(allGames);
        }
    }
    
    /**
     * Get installed games
     */
    public List<Game> getInstalledGames() {
        synchronized (this) {
            List<Game> installedGames = new ArrayList<>();
            for (Game game : allGames) {
                if (game.isInstalled()) {
                    installedGames.add(game);
                }
            }
            return installedGames;
        }
    }
    
    /**
     * Get games by category
     */
    public List<Game> getGamesByCategory(String category) {
        synchronized (this) {
            List<Game> categoryGames = new ArrayList<>();
            for (Game game : allGames) {
                if (category == null || category.equals(game.getCategory())) {
                    categoryGames.add(game);
                }
            }
            return categoryGames;
        }
    }
    
    /**
     * Search games by name
     */
    public List<Game> searchGames(String query) {
        synchronized (this) {
            if (query == null || query.trim().isEmpty()) {
                return new ArrayList<>(allGames);
            }
            
            String lowerQuery = query.toLowerCase();
            List<Game> searchResults = new ArrayList<>();
            
            for (Game game : allGames) {
                if (game.getName().toLowerCase().contains(lowerQuery)) {
                    searchResults.add(game);
                }
            }
            
            return searchResults;
        }
    }
    
    /**
     * Get game by ID
     */
    public Game getGameById(long gameId) {
        synchronized (this) {
            return gameCache.get(gameId);
        }
    }
    
    /**
     * Get detailed game information
     */
    public void getGameDetails(long gameId, GameDetailsCallback callback) {
        executorService.execute(() -> {
            try {
                ApiResponse<Game> response = gogApi.getGameDetails(gameId);
                
                if (response.isSuccess()) {
                    Game detailedGame = response.getData();
                    
                    // Update cache
                    synchronized (this) {
                        gameCache.put(gameId, detailedGame);
                        
                        // Update in allGames list
                        for (int i = 0; i < allGames.size(); i++) {
                            if (allGames.get(i).getId() == gameId) {
                                allGames.set(i, detailedGame);
                                break;
                            }
                        }
                    }
                    
                    callback.onSuccess(detailedGame);
                } else {
                    callback.onError(response.getErrorMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting game details", e);
                callback.onError("Failed to get game details: " + e.getMessage());
            }
        });
    }
    
    /**
     * Update game status (installed, downloading, etc.)
     */
    public void updateGameStatus(long gameId, Game.DownloadState state, boolean installed) {
        synchronized (this) {
            Game game = gameCache.get(gameId);
            if (game != null) {
                game.setDownloadState(state);
                game.setInstalled(installed);
                notifyGameUpdated(game);
            }
        }
    }
    
    /**
     * Update game download progress
     */
    public void updateGameProgress(long gameId, int progress, String speed, String eta) {
        synchronized (this) {
            Game game = gameCache.get(gameId);
            if (game != null) {
                game.setDownloadProgress(progress);
                game.setDownloadSpeed(speed);
                game.setDownloadEta(eta);
                notifyGameUpdated(game);
            }
        }
    }
    
    /**
     * Check if API is available
     */
    public void checkApiAvailability(ApiAvailabilityCallback callback) {
        executorService.execute(() -> {
            boolean available = gogApi.isNetworkAvailable();
            callback.onResult(available);
        });
    }
    
    /**
     * Add repository listener
     */
    public void addListener(GameRepositoryListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove repository listener
     */
    public void removeListener(GameRepositoryListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    // Notification methods
    private void notifyLoadingStarted() {
        synchronized (listeners) {
            for (GameRepositoryListener listener : listeners) {
                listener.onLoadingStarted();
            }
        }
    }
    
    private void notifyGamesLoaded(List<Game> games) {
        synchronized (listeners) {
            for (GameRepositoryListener listener : listeners) {
                listener.onGamesLoaded(games);
            }
        }
    }
    
    private void notifyGameUpdated(Game game) {
        synchronized (listeners) {
            for (GameRepositoryListener listener : listeners) {
                listener.onGameUpdated(game);
            }
        }
    }
    
    private void notifyError(String errorMessage) {
        synchronized (listeners) {
            for (GameRepositoryListener listener : listeners) {
                listener.onError(errorMessage);
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        executorService.shutdown();
        synchronized (listeners) {
            listeners.clear();
        }
    }
    
    // Callback interfaces
    public interface GameRepositoryListener {
        void onLoadingStarted();
        void onGamesLoaded(List<Game> games);
        void onGameUpdated(Game game);
        void onError(String errorMessage);
    }
    
    public interface GameDetailsCallback {
        void onSuccess(Game game);
        void onError(String errorMessage);
    }
    
    public interface ApiAvailabilityCallback {
        void onResult(boolean available);
    }

    public interface DownloadLinksCallback {
        void onSuccess(Game game);
        void onError(String errorMessage);
    }
}