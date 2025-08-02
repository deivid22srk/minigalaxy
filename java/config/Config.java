package com.minigalaxy.android.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * Configuration manager using SharedPreferences
 * Equivalent to the Python config.py but for Android
 */
public class Config {
    
    private static final String PREFS_NAME = "minigalaxy_config";
    
    // Preference keys
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_LANG = "lang";
    private static final String KEY_VIEW = "view";
    private static final String KEY_INSTALL_DIR = "install_dir";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_KEEP_INSTALLERS = "keep_installers";
    private static final String KEY_KEEP_WINDOW_MAXIMIZED = "keep_window_maximized";
    private static final String KEY_USE_DARK_THEME = "use_dark_theme";
    private static final String KEY_INSTALLED_FILTER = "installed_filter";
    private static final String KEY_DOWNLOAD_THREADS = "download_threads";
    private static final String KEY_CONCURRENT_DOWNLOADS = "concurrent_downloads";
    private static final String KEY_AUTO_INSTALL = "auto_install";
    private static final String KEY_SHOW_FPS = "show_fps";
    private static final String KEY_USE_SYSTEM_DOSBOX = "use_system_dosbox";
    private static final String KEY_USE_SYSTEM_SCUMMVM = "use_system_scummvm";
    private static final String KEY_CREATE_SHORTCUTS = "create_shortcuts";
    private static final String KEY_SHOW_WINE_PREFIX = "show_wine_prefix";
    private static final String KEY_DOWNLOAD_NOTIFICATIONS = "download_notifications";
    private static final String KEY_UPDATE_NOTIFICATIONS = "update_notifications";
    private static final String KEY_AUTO_DOWNLOAD_UPDATES = "auto_download_updates";
    private static final String KEY_DOWNLOAD_SPEED_LIMIT = "download_speed_limit";
    
    // Default values
    private static final String DEFAULT_LOCALE = "";
    private static final String DEFAULT_LANG = "en";
    private static final String DEFAULT_VIEW = "grid";
    private static final boolean DEFAULT_KEEP_INSTALLERS = false;
    private static final boolean DEFAULT_KEEP_WINDOW_MAXIMIZED = false;
    private static final boolean DEFAULT_USE_DARK_THEME = false;
    private static final boolean DEFAULT_INSTALLED_FILTER = false;
    private static final int DEFAULT_DOWNLOAD_THREADS = 4;
    private static final int DEFAULT_CONCURRENT_DOWNLOADS = 4;
    private static final boolean DEFAULT_AUTO_INSTALL = false;
    private static final boolean DEFAULT_SHOW_FPS = false;
    private static final boolean DEFAULT_USE_SYSTEM_DOSBOX = false;
    private static final boolean DEFAULT_USE_SYSTEM_SCUMMVM = false;
    private static final boolean DEFAULT_CREATE_SHORTCUTS = true;
    private static final boolean DEFAULT_SHOW_WINE_PREFIX = false;
    private static final boolean DEFAULT_DOWNLOAD_NOTIFICATIONS = true;
    private static final boolean DEFAULT_UPDATE_NOTIFICATIONS = true;
    private static final boolean DEFAULT_AUTO_DOWNLOAD_UPDATES = false;
    private static final int DEFAULT_DOWNLOAD_SPEED_LIMIT = 0; // 0 for unlimited
    
    private static Config instance;
    private final SharedPreferences prefs;
    private final Context context;
    
    private Config(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized Config getInstance(Context context) {
        if (instance == null) {
            instance = new Config(context);
        }
        return instance;
    }
    
    // Locale
    public String getLocale() {
        return prefs.getString(KEY_LOCALE, DEFAULT_LOCALE);
    }
    
    public void setLocale(String locale) {
        prefs.edit().putString(KEY_LOCALE, locale).apply();
    }
    
    // Language
    public String getLang() {
        return prefs.getString(KEY_LANG, DEFAULT_LANG);
    }
    
    public void setLang(String lang) {
        prefs.edit().putString(KEY_LANG, lang).apply();
    }
    
    // View type (grid/list)
    public String getView() {
        return prefs.getString(KEY_VIEW, DEFAULT_VIEW);
    }
    
    public void setView(String view) {
        prefs.edit().putString(KEY_VIEW, view).apply();
    }
    
    // Install directory
    public String getInstallDir() {
        String defaultDir = getDefaultInstallDir();
        return prefs.getString(KEY_INSTALL_DIR, defaultDir);
    }
    
    public void setInstallDir(String installDir) {
        prefs.edit().putString(KEY_INSTALL_DIR, installDir).apply();
    }
    
    private String getDefaultInstallDir() {
        File externalStorage = Environment.getExternalStorageDirectory();
        return new File(externalStorage, "Minigalaxy/Games").getAbsolutePath();
    }
    
    // Username
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
    
    public void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }
    
    // Refresh token
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, "");
    }
    
    public void setRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }
    
    // Keep installers
    public boolean getKeepInstallers() {
        return prefs.getBoolean(KEY_KEEP_INSTALLERS, DEFAULT_KEEP_INSTALLERS);
    }
    
    public void setKeepInstallers(boolean keepInstallers) {
        prefs.edit().putBoolean(KEY_KEEP_INSTALLERS, keepInstallers).apply();
    }
    
    // Keep window maximized (for future use)
    public boolean getKeepWindowMaximized() {
        return prefs.getBoolean(KEY_KEEP_WINDOW_MAXIMIZED, DEFAULT_KEEP_WINDOW_MAXIMIZED);
    }
    
    public void setKeepWindowMaximized(boolean keepWindowMaximized) {
        prefs.edit().putBoolean(KEY_KEEP_WINDOW_MAXIMIZED, keepWindowMaximized).apply();
    }
    
    // Dark theme
    public boolean getUseDarkTheme() {
        return prefs.getBoolean(KEY_USE_DARK_THEME, DEFAULT_USE_DARK_THEME);
    }
    
    public void setUseDarkTheme(boolean useDarkTheme) {
        prefs.edit().putBoolean(KEY_USE_DARK_THEME, useDarkTheme).apply();
    }
    
    // Installed filter
    public boolean getInstalledFilter() {
        return prefs.getBoolean(KEY_INSTALLED_FILTER, DEFAULT_INSTALLED_FILTER);
    }
    
    public void setInstalledFilter(boolean installedFilter) {
        prefs.edit().putBoolean(KEY_INSTALLED_FILTER, installedFilter).apply();
    }
    
    // Download threads
    public int getDownloadThreads() {
        return prefs.getInt(KEY_DOWNLOAD_THREADS, DEFAULT_DOWNLOAD_THREADS);
    }
    
    public void setDownloadThreads(int downloadThreads) {
        prefs.edit().putInt(KEY_DOWNLOAD_THREADS, downloadThreads).apply();
    }
    
    // Concurrent downloads
    public int getConcurrentDownloads() {
        return prefs.getInt(KEY_CONCURRENT_DOWNLOADS, DEFAULT_CONCURRENT_DOWNLOADS);
    }
    
    public void setConcurrentDownloads(int concurrentDownloads) {
        prefs.edit().putInt(KEY_CONCURRENT_DOWNLOADS, concurrentDownloads).apply();
    }
    
    // Auto install
    public boolean getAutoInstall() {
        return prefs.getBoolean(KEY_AUTO_INSTALL, DEFAULT_AUTO_INSTALL);
    }
    
    public void setAutoInstall(boolean autoInstall) {
        prefs.edit().putBoolean(KEY_AUTO_INSTALL, autoInstall).apply();
    }
    
    // Show FPS
    public boolean getShowFPS() {
        return prefs.getBoolean(KEY_SHOW_FPS, DEFAULT_SHOW_FPS);
    }
    
    public void setShowFPS(boolean showFPS) {
        prefs.edit().putBoolean(KEY_SHOW_FPS, showFPS).apply();
    }
    
    // Use system DOSBox
    public boolean getUseSystemDosbox() {
        return prefs.getBoolean(KEY_USE_SYSTEM_DOSBOX, DEFAULT_USE_SYSTEM_DOSBOX);
    }
    
    public void setUseSystemDosbox(boolean useSystemDosbox) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_DOSBOX, useSystemDosbox).apply();
    }
    
    // Use system ScummVM
    public boolean getUseSystemScummvm() {
        return prefs.getBoolean(KEY_USE_SYSTEM_SCUMMVM, DEFAULT_USE_SYSTEM_SCUMMVM);
    }
    
    public void setUseSystemScummvm(boolean useSystemScummvm) {
        prefs.edit().putBoolean(KEY_USE_SYSTEM_SCUMMVM, useSystemScummvm).apply();
    }
    
    // Create shortcuts
    public boolean getCreateShortcuts() {
        return prefs.getBoolean(KEY_CREATE_SHORTCUTS, DEFAULT_CREATE_SHORTCUTS);
    }
    
    public void setCreateShortcuts(boolean createShortcuts) {
        prefs.edit().putBoolean(KEY_CREATE_SHORTCUTS, createShortcuts).apply();
    }
    
    // Show wine prefix
    public boolean getShowWinePrefix() {
        return prefs.getBoolean(KEY_SHOW_WINE_PREFIX, DEFAULT_SHOW_WINE_PREFIX);
    }
    
    public void setShowWinePrefix(boolean showWinePrefix) {
        prefs.edit().putBoolean(KEY_SHOW_WINE_PREFIX, showWinePrefix).apply();
    }

    // Download Notifications
    public boolean getDownloadNotifications() {
        return prefs.getBoolean(KEY_DOWNLOAD_NOTIFICATIONS, DEFAULT_DOWNLOAD_NOTIFICATIONS);
    }

    public void setDownloadNotifications(boolean enabled) {
        prefs.edit().putBoolean(KEY_DOWNLOAD_NOTIFICATIONS, enabled).apply();
    }

    // Update Notifications
    public boolean getUpdateNotifications() {
        return prefs.getBoolean(KEY_UPDATE_NOTIFICATIONS, DEFAULT_UPDATE_NOTIFICATIONS);
    }

    public void setUpdateNotifications(boolean enabled) {
        prefs.edit().putBoolean(KEY_UPDATE_NOTIFICATIONS, enabled).apply();
    }

    // Auto Download Updates
    public boolean getAutoDownloadUpdates() {
        return prefs.getBoolean(KEY_AUTO_DOWNLOAD_UPDATES, DEFAULT_AUTO_DOWNLOAD_UPDATES);
    }

    public void setAutoDownloadUpdates(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_DOWNLOAD_UPDATES, enabled).apply();
    }

    // Download Speed Limit
    public int getDownloadSpeedLimit() {
        return prefs.getInt(KEY_DOWNLOAD_SPEED_LIMIT, DEFAULT_DOWNLOAD_SPEED_LIMIT);
    }

    public void setDownloadSpeedLimit(int limitInKb) {
        prefs.edit().putInt(KEY_DOWNLOAD_SPEED_LIMIT, limitInKb).apply();
    }
    
    // Utility methods
    public boolean isLoggedIn() {
        return !getRefreshToken().isEmpty();
    }
    
    public void clearAuthData() {
        prefs.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }
    
    public String getCacheDir() {
        return context.getCacheDir().getAbsolutePath();
    }
    
    public String getIconCacheDir() {
        File iconDir = new File(context.getCacheDir(), "icons");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        return iconDir.getAbsolutePath();
    }
    
    public String getThumbnailCacheDir() {
        File thumbnailDir = new File(context.getCacheDir(), "thumbnails");
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs();
        }
        return thumbnailDir.getAbsolutePath();
    }
    
    public String getDownloadCacheDir() {
        File downloadDir = new File(context.getCacheDir(), "downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir.getAbsolutePath();
    }
    
    // Reset to defaults
    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }
    
    @Override
    public String toString() {
        return "Config{" +
                "locale='" + getLocale() + '\'' +
                ", lang='" + getLang() + '\'' +
                ", view='" + getView() + '\'' +
                ", installDir='" + getInstallDir() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", isLoggedIn=" + isLoggedIn() +
                '}';
    }
}