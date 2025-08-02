package com.minigalaxy.android.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.minigalaxy.android.config.Config;
import com.minigalaxy.android.config.Constants;
import com.minigalaxy.android.model.FileInfo;
import com.minigalaxy.android.model.Game;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Main API client for GOG services
 */
public class GogApi {
    
    private static final String TAG = "GogApi";
    
    private final Config config;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String activeToken;
    private long activeTokenExpirationTime;
    
    public GogApi(Config config) {
        this.config = config;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.activeTokenExpirationTime = System.currentTimeMillis();
    }
    
    /**
     * Authenticate with login code or refresh token
     */
    public ApiResponse<AuthResult> authenticate(String loginCode, String refreshToken) {
        try {
            if (refreshToken != null && !refreshToken.isEmpty()) {
                return refreshToken(refreshToken);
            } else if (loginCode != null && !loginCode.isEmpty()) {
                return getTokenFromCode(loginCode);
            } else {
                return ApiResponse.error("No login code or refresh token provided");
            }
        } catch (Exception e) {
            Log.e(TAG, "Authentication error", e);
            return ApiResponse.error("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Get new access token using refresh token
     */
    private ApiResponse<AuthResult> refreshToken(String refreshToken) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("client_id", Constants.GOG_CLIENT_ID)
                .add("client_secret", Constants.GOG_CLIENT_SECRET)
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();
        
        Request request = new Request.Builder()
                .url(Constants.GOG_TOKEN_URL)
                .post(formBody)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                
                String accessToken = jsonResponse.get("access_token").getAsString();
                String newRefreshToken = jsonResponse.get("refresh_token").getAsString();
                int expiresIn = jsonResponse.get("expires_in").getAsInt();
                
                this.activeToken = accessToken;
                this.activeTokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L);
                
                AuthResult authResult = new AuthResult(accessToken, newRefreshToken, expiresIn);
                return ApiResponse.success(authResult);
            } else {
                return ApiResponse.error("Failed to refresh token: " + response.code());
            }
        }
    }
    
    /**
     * Get access token from login code
     */
    private ApiResponse<AuthResult> getTokenFromCode(String loginCode) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("client_id", Constants.GOG_CLIENT_ID)
                .add("client_secret", Constants.GOG_CLIENT_SECRET)
                .add("grant_type", "authorization_code")
                .add("code", loginCode)
                .add("redirect_uri", Constants.GOG_REDIRECT_URI)
                .build();
        
        Request request = new Request.Builder()
                .url(Constants.GOG_TOKEN_URL)
                .post(formBody)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                
                String accessToken = jsonResponse.get("access_token").getAsString();
                String refreshToken = jsonResponse.get("refresh_token").getAsString();
                int expiresIn = jsonResponse.get("expires_in").getAsInt();
                
                this.activeToken = accessToken;
                this.activeTokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L);
                
                AuthResult authResult = new AuthResult(accessToken, refreshToken, expiresIn);
                return ApiResponse.success(authResult);
            } else {
                return ApiResponse.error("Failed to get token: " + response.code());
            }
        }
    }
    
    /**
     * Get user's game library
     */
    public ApiResponse<List<Game>> getUserGames() {
        try {
            if (!ensureValidToken()) {
                return ApiResponse.error("Authentication required");
            }
            
            Request request = new Request.Builder()
                    .url(Constants.GOG_GAMES_URL)
                    .addHeader("Authorization", "Bearer " + activeToken)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    List<Game> games = parseGamesFromResponse(jsonResponse);
                    return ApiResponse.success(games);
                } else {
                    return ApiResponse.error("Failed to get games: " + response.code());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user games", e);
            return ApiResponse.error("Failed to get games: " + e.getMessage());
        }
    }
    
    /**
     * Get detailed game information
     */
    public ApiResponse<Game> getGameDetails(long gameId) {
        try {
            if (!ensureValidToken()) {
                return ApiResponse.error("Authentication required");
            }
            
            String url = Constants.GOG_GAME_DETAILS_URL + gameId + "?expand=downloads,description,screenshots";
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + activeToken)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    Game game = parseGameDetails(jsonResponse);
                    return ApiResponse.success(game);
                } else {
                    return ApiResponse.error("Failed to get game details: " + response.code());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting game details", e);
            return ApiResponse.error("Failed to get game details: " + e.getMessage());
        }
    }
    
    /**
     * Get download link for a game file
     */
    public ApiResponse<String> getDownloadLink(long gameId, long fileId) {
        try {
            if (!ensureValidToken()) {
                return ApiResponse.error("Authentication required");
            }
            
            String url = Constants.GOG_DOWNLOAD_URL
                    .replace("{id}", String.valueOf(gameId))
                    .replace("{type}", "installer")
                    .replace("{file_id}", String.valueOf(fileId));
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + activeToken)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    String downloadLink = jsonResponse.get("downlink").getAsString();
                    return ApiResponse.success(downloadLink);
                } else {
                    return ApiResponse.error("Failed to get download link: " + response.code());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting download link", e);
            return ApiResponse.error("Failed to get download link: " + e.getMessage());
        }
    }
    
    /**
     * Get file information for a game
     */
    public ApiResponse<List<FileInfo>> getGameFiles(long gameId, String platform, String language) {
        try {
            ApiResponse<Game> gameResponse = getGameDetails(gameId);
            if (!gameResponse.isSuccess()) {
                return ApiResponse.error(gameResponse.getErrorMessage());
            }
            
            // Parse file information from game details
            // This would need to be implemented based on GOG API response structure
            List<FileInfo> files = new ArrayList<>();
            
            return ApiResponse.success(files);
        } catch (Exception e) {
            Log.e(TAG, "Error getting game files", e);
            return ApiResponse.error("Failed to get game files: " + e.getMessage());
        }
    }
    
    /**
     * Check if current token is valid and refresh if necessary
     */
    private boolean ensureValidToken() {
        if (activeToken == null || activeToken.isEmpty()) {
            return false;
        }
        
        // Check if token is about to expire (5 minutes buffer)
        if (System.currentTimeMillis() >= (activeTokenExpirationTime - 300000)) {
            String refreshToken = config.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty()) {
                ApiResponse<AuthResult> refreshResponse = refreshToken(refreshToken);
                if (refreshResponse.isSuccess()) {
                    AuthResult result = refreshResponse.getData();
                    config.setRefreshToken(result.getRefreshToken());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Parse games from API response
     */
    private List<Game> parseGamesFromResponse(JsonObject response) {
        List<Game> games = new ArrayList<>();
        
        try {
            JsonArray products = response.getAsJsonArray("products");
            if (products != null) {
                for (JsonElement element : products) {
                    JsonObject gameObj = element.getAsJsonObject();
                    
                    long id = gameObj.get("id").getAsLong();
                    
                    // Skip ignored games
                    if (Constants.IGNORE_GAME_IDS.contains(id)) {
                        continue;
                    }
                    
                    Game game = parseGameFromJson(gameObj);
                    if (game != null) {
                        games.add(game);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing games", e);
        }
        
        return games;
    }
    
    /**
     * Parse single game from JSON
     */
    private Game parseGameFromJson(JsonObject gameObj) {
        try {
            Game game = new Game();
            
            game.setId(gameObj.get("id").getAsLong());
            game.setName(gameObj.get("title").getAsString());
            
            if (gameObj.has("url")) {
                game.setUrl(gameObj.get("url").getAsString());
            }
            
            if (gameObj.has("image")) {
                game.setImageUrl(gameObj.get("image").getAsString());
            }
            
            if (gameObj.has("category")) {
                game.setCategory(gameObj.get("category").getAsString());
            }
            
            // Set platform based on available downloads
            game.setPlatform("linux"); // Default to linux for Android client
            
            return game;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing game from JSON", e);
            return null;
        }
    }
    
    /**
     * Parse detailed game information
     */
    private Game parseGameDetails(JsonObject gameObj) {
        Game game = parseGameFromJson(gameObj);
        
        if (game != null) {
            try {
                if (gameObj.has("description")) {
                    JsonObject description = gameObj.getAsJsonObject("description");
                    if (description.has("full")) {
                        game.setDescription(description.get("full").getAsString());
                    }
                }
                
                if (gameObj.has("downloads")) {
                    JsonObject downloads = gameObj.getAsJsonObject("downloads");
                    // Parse download information
                    // This would need to be implemented based on actual GOG API structure
                }
                
                // Parse additional fields as needed
            } catch (Exception e) {
                Log.e(TAG, "Error parsing game details", e);
            }
        }
        
        return game;
    }
    
    /**
     * Check network connectivity
     */
    public boolean isNetworkAvailable() {
        try {
            Request request = new Request.Builder()
                    .url("https://www.gog.com")
                    .head()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Authentication result class
     */
    public static class AuthResult {
        private final String accessToken;
        private final String refreshToken;
        private final int expiresIn;
        
        public AuthResult(String accessToken, String refreshToken, int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public int getExpiresIn() {
            return expiresIn;
        }
    }
}