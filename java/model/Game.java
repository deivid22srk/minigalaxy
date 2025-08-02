package com.minigalaxy.android.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a GOG game
 */
public class Game {
    
    private String name;
    private String url;
    private Map<String, String> md5sum;
    private long id;
    private String installDir;
    private String imageUrl;
    private String platform;
    private List<DLC> dlcs;
    private String category;
    private String description;
    private String version;
    private String developer;
    private String genre;
    private String publisher;
    private String releaseDate;
    private long fileSize;
    private long installedSize;
    private String language;
    private boolean isInstalled;
    private boolean isDownloading;
    private boolean isQueued;
    private int downloadProgress;
    private String downloadSpeed;
    private String downloadEta;
    
    // Download states
    public enum DownloadState {
        NOT_DOWNLOADED,
        DOWNLOADING,
        DOWNLOADED,
        INSTALLED,
        ERROR
    }
    
    private DownloadState downloadState;
    
    public Game() {
        this.md5sum = new HashMap<>();
        this.dlcs = new ArrayList<>();
        this.downloadState = DownloadState.NOT_DOWNLOADED;
        this.downloadProgress = 0;
    }
    
    public Game(String name, String url, Map<String, String> md5sum, long id, 
                String installDir, String imageUrl, String platform, 
                List<DLC> dlcs, String category) {
        this();
        this.name = name;
        this.url = url;
        this.md5sum = md5sum != null ? md5sum : new HashMap<>();
        this.id = id;
        this.installDir = installDir;
        this.imageUrl = imageUrl;
        this.platform = platform;
        this.dlcs = dlcs != null ? dlcs : new ArrayList<>();
        this.category = category;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, String> getMd5sum() {
        return md5sum;
    }
    
    public void setMd5sum(Map<String, String> md5sum) {
        this.md5sum = md5sum;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getInstallDir() {
        return installDir;
    }
    
    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public List<DLC> getDlcs() {
        return dlcs;
    }
    
    public void setDlcs(List<DLC> dlcs) {
        this.dlcs = dlcs;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getFormattedSize() {
        return formatFileSize(fileSize);
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public long getInstalledSize() {
        return installedSize;
    }
    
    public void setInstalledSize(long installedSize) {
        this.installedSize = installedSize;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public boolean isInstalled() {
        return isInstalled;
    }
    
    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }
    
    public boolean isDownloading() {
        return isDownloading;
    }
    
    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }
    
    public boolean isQueued() {
        return isQueued;
    }
    
    public void setQueued(boolean queued) {
        isQueued = queued;
    }
    
    public int getDownloadProgress() {
        return downloadProgress;
    }
    
    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
    
    public String getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
    
    public String getDownloadEta() {
        return downloadEta;
    }
    
    public void setDownloadEta(String downloadEta) {
        this.downloadEta = downloadEta;
    }
    
    public DownloadState getDownloadState() {
        return downloadState;
    }
    
    public void setDownloadState(DownloadState downloadState) {
        this.downloadState = downloadState;
    }
    
    // Utility methods
    public String getStrippedName() {
        return stripString(name, false);
    }
    
    public String getInstallDirectoryName() {
        return stripString(name, true);
    }
    
    public String getCachedIconPath(String cacheDir) {
        return cacheDir + "/icons/" + id + ".png";
    }
    
    public String getThumbnailPath(String cacheDir) {
        if (isInstalled && installDir != null) {
            return installDir + "/thumbnail.jpg";
        }
        return cacheDir + "/thumbnails/" + id + ".jpg";
    }
    
    public String getFormattedFileSize() {
        return formatFileSize(fileSize);
    }
    
    public String getFormattedInstalledSize() {
        return formatFileSize(installedSize);
    }
    
    // Helper methods
    private static String stripString(String input, boolean toPath) {
        if (input == null) return "";
        
        String stripped = input.trim();
        
        if (toPath) {
            // Remove characters that are not safe for file paths
            stripped = stripped.replaceAll("[^a-zA-Z0-9\\s\\-_]", "");
            stripped = stripped.replaceAll("\\s+", "_");
        }
        
        return stripped;
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }
        
        double size = bytes / Math.pow(1024, digitGroups);
        
        if (digitGroups == 0) {
            return String.format("%.0f %s", size, units[digitGroups]);
        } else {
            return String.format("%.1f %s", size, units[digitGroups]);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Game game = (Game) obj;
        return id == game.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "Game{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", platform='" + platform + '\'' +
                ", isInstalled=" + isInstalled +
                ", downloadState=" + downloadState +
                '}';
    }
}