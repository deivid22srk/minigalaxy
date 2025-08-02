package com.minigalaxy.android.auth;

import android.content.Context;
import android.util.Log;

import com.minigalaxy.android.api.ApiResponse;
import com.minigalaxy.android.api.GogApi;
import com.minigalaxy.android.config.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager for handling GOG authentication
 */
public class AuthenticationManager {
    
    private static final String TAG = "AuthenticationManager";
    
    private final Config config;
    private final GogApi gogApi;
    private final ExecutorService executorService;
    
    private boolean isAuthenticated = false;
    private String currentUsername = "";
    
    public AuthenticationManager(Context context, Config config) {
        this.config = config;
        this.gogApi = new GogApi(config);
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Check if already authenticated
        checkExistingAuthentication();
    }
    
    /**
     * Check if user is already authenticated
     */
    private void checkExistingAuthentication() {
        String refreshToken = config.getRefreshToken();
        String username = config.getUsername();
        
        if (refreshToken != null && !refreshToken.isEmpty() && 
            username != null && !username.isEmpty()) {
            this.isAuthenticated = true;
            this.currentUsername = username;
        }
    }
    
    /**
     * Authenticate with login code from OAuth flow
     */
    public void authenticateWithCode(String loginCode, AuthenticationCallback callback) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Authenticating with login code");
                
                ApiResponse<GogApi.AuthResult> response = gogApi.authenticate(loginCode, null);
                
                if (response.isSuccess()) {
                    GogApi.AuthResult authResult = response.getData();
                    
                    // Save authentication data
                    config.setRefreshToken(authResult.getRefreshToken());
                    
                    // Get user info to save username
                    getUserInfo(authResult.getAccessToken(), new UserInfoCallback() {
                        @Override
                        public void onSuccess(String username) {
                            config.setUsername(username);
                            isAuthenticated = true;
                            currentUsername = username;
                            
                            Log.d(TAG, "Authentication successful for user: " + username);
                            callback.onAuthenticationSuccess(username);
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            Log.e(TAG, "Failed to get user info: " + errorMessage);
                            // Still consider auth successful if we got tokens
                            isAuthenticated = true;
                            currentUsername = "Unknown";
                            config.setUsername(currentUsername);
                            callback.onAuthenticationSuccess(currentUsername);
                        }
                    });
                    
                } else {
                    Log.e(TAG, "Authentication failed: " + response.getErrorMessage());
                    callback.onAuthenticationError(response.getErrorMessage());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Authentication error", e);
                callback.onAuthenticationError("Authentication failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Refresh authentication using stored refresh token
     */
    public void refreshAuthentication(AuthenticationCallback callback) {
        executorService.execute(() -> {
            try {
                String refreshToken = config.getRefreshToken();
                
                if (refreshToken == null || refreshToken.isEmpty()) {
                    callback.onAuthenticationError("No refresh token available");
                    return;
                }
                
                Log.d(TAG, "Refreshing authentication");
                
                ApiResponse<GogApi.AuthResult> response = gogApi.authenticate(null, refreshToken);
                
                if (response.isSuccess()) {
                    GogApi.AuthResult authResult = response.getData();
                    
                    // Update stored refresh token
                    config.setRefreshToken(authResult.getRefreshToken());
                    
                    isAuthenticated = true;
                    
                    Log.d(TAG, "Authentication refresh successful");
                    callback.onAuthenticationSuccess(currentUsername);
                    
                } else {
                    Log.e(TAG, "Authentication refresh failed: " + response.getErrorMessage());
                    
                    // Clear stored authentication data if refresh failed
                    logout();
                    
                    callback.onAuthenticationError(response.getErrorMessage());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Authentication refresh error", e);
                callback.onAuthenticationError("Authentication refresh failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Check if user is currently authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated && !config.getRefreshToken().isEmpty();
    }
    
    /**
     * Get current username
     */
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    /**
     * Logout user and clear authentication data
     */
    public void logout() {
        Log.d(TAG, "Logging out user");
        
        isAuthenticated = false;
        currentUsername = "";
        
        // Clear stored authentication data
        config.clearAuthData();
    }
    
    /**
     * Validate current authentication status
     */
    public void validateAuthentication(AuthenticationCallback callback) {
        if (!isAuthenticated()) {
            callback.onAuthenticationError("Not authenticated");
            return;
        }
        
        // Try to refresh to validate
        refreshAuthentication(new AuthenticationCallback() {
            @Override
            public void onAuthenticationSuccess(String username) {
                callback.onAuthenticationSuccess(username);
            }
            
            @Override
            public void onAuthenticationError(String errorMessage) {
                // If refresh fails, try one more time
                refreshAuthentication(callback);
            }
        });
    }
    
    /**
     * Get user information from GOG API
     */
    private void getUserInfo(String accessToken, UserInfoCallback callback) {
        // This would need to be implemented with a proper GOG API endpoint
        // For now, we'll use a placeholder
        callback.onSuccess("GOG User");
    }
    
    /**
     * Get GOG login URL for OAuth flow
     */
    public static String getLoginUrl() {
        return "https://auth.gog.com/auth" +
                "?client_id=" + com.minigalaxy.android.config.Constants.GOG_CLIENT_ID +
                "&redirect_uri=" + java.net.URLEncoder.encode(com.minigalaxy.android.config.Constants.GOG_REDIRECT_URI) +
                "&response_type=code" +
                "&layout=client2";
    }
    
    /**
     * Extract login code from redirect URL
     */
    public static String extractLoginCodeFromUrl(String url) {
        if (url == null || !url.contains("code=")) {
            return null;
        }
        
        try {
            String[] parts = url.split("code=");
            if (parts.length > 1) {
                String code = parts[1];
                
                // Remove any additional parameters
                int ampersandIndex = code.indexOf('&');
                if (ampersandIndex != -1) {
                    code = code.substring(0, ampersandIndex);
                }
                
                return code;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting login code from URL", e);
        }
        
        return null;
    }
    
    /**
     * Check if URL is the success redirect URL
     */
    public static boolean isSuccessUrl(String url) {
        return url != null && url.startsWith(com.minigalaxy.android.config.Constants.GOG_LOGIN_SUCCESS_URL);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        executorService.shutdown();
    }
    
    // Callback interfaces
    public interface AuthenticationCallback {
        void onAuthenticationSuccess(String username);
        void onAuthenticationError(String errorMessage);
    }
    
    private interface UserInfoCallback {
        void onSuccess(String username);
        void onError(String errorMessage);
    }
}