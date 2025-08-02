package com.minigalaxy.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.minigalaxy.android.R;
import com.minigalaxy.android.auth.AuthenticationManager;
import com.minigalaxy.android.config.Config;

public class LoginActivity extends AppCompatActivity implements 
        AuthenticationManager.AuthenticationCallback {
    
    private static final String TAG = "LoginActivity";
    
    private WebView webViewLogin;
    private View loadingLayout;
    private View errorLayout;
    private MaterialButton loginButton;
    private MaterialButton retryButton;
    
    private Config config;
    private AuthenticationManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initializeComponents();
        setupUI();
        setupWebView();
    }
    
    private void initializeComponents() {
        config = new Config(this);
        authManager = new AuthenticationManager(this, config);
    }
    
    private void setupUI() {
        webViewLogin = findViewById(R.id.webview_login);
        loadingLayout = findViewById(R.id.loading_layout);
        errorLayout = findViewById(R.id.error_layout);
        loginButton = findViewById(R.id.login_button);
        retryButton = findViewById(R.id.retry_button);
        
        loginButton.setOnClickListener(v -> startLogin());
        retryButton.setOnClickListener(v -> startLogin());
    }
    
    private void setupWebView() {
        webViewLogin.getSettings().setJavaScriptEnabled(true);
        webViewLogin.getSettings().setDomStorageEnabled(true);
        
        webViewLogin.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                Log.d(TAG, "Page finished loading: " + url);
                
                if (AuthenticationManager.isSuccessUrl(url)) {
                    String loginCode = AuthenticationManager.extractLoginCodeFromUrl(url);
                    if (loginCode != null) {
                        handleLoginCode(loginCode);
                    } else {
                        showError("Failed to extract login code");
                    }
                } else {
                    showWebView();
                }
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, 
                                      String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView error: " + description);
                showError("Failed to load login page: " + description);
            }
        });
    }
    
    private void startLogin() {
        Log.d(TAG, "Starting GOG login");
        
        showLoading();
        
        String loginUrl = AuthenticationManager.getLoginUrl();
        webViewLogin.loadUrl(loginUrl);
    }
    
    private void handleLoginCode(String loginCode) {
        Log.d(TAG, "Received login code, authenticating...");
        
        showLoading();
        authManager.authenticateWithCode(loginCode, this);
    }
    
    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        webViewLogin.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        loginButton.setEnabled(false);
    }
    
    private void showWebView() {
        loadingLayout.setVisibility(View.GONE);
        webViewLogin.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }
    
    private void showError(String message) {
        loadingLayout.setVisibility(View.GONE);
        webViewLogin.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        loginButton.setEnabled(true);
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    // AuthenticationManager.AuthenticationCallback implementation
    @Override
    public void onAuthenticationSuccess(String username) {
        Log.d(TAG, "Authentication successful for user: " + username);
        
        runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            
            // Navigate to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    @Override
    public void onAuthenticationError(String errorMessage) {
        Log.e(TAG, "Authentication failed: " + errorMessage);
        
        runOnUiThread(() -> {
            showError(getString(R.string.login_error) + ": " + errorMessage);
        });
    }
    
    @Override
    public void onBackPressed() {
        if (webViewLogin.canGoBack() && webViewLogin.getVisibility() == View.VISIBLE) {
            webViewLogin.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (authManager != null) {
            authManager.cleanup();
        }
        
        if (webViewLogin != null) {
            webViewLogin.destroy();
        }
    }
}