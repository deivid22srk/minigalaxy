package com.minigalaxy.android.model;

/**
 * Model class representing downloadable content (DLC) for a game
 */
public class DLC {
    
    private long id;
    private String name;
    private String description;
    private String imageUrl;
    private String url;
    private long fileSize;
    private boolean isInstalled;
    private boolean isDownloading;
    private String version;
    private String language;
    private Game.DownloadState downloadState;
    private int downloadProgress;
    
    public DLC() {
        this.downloadState = Game.DownloadState.NOT_DOWNLOADED;
        this.downloadProgress = 0;
    }
    
    public DLC(long id, String name, String description, String imageUrl, String url) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.url = url;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Game.DownloadState getDownloadState() {
        return downloadState;
    }
    
    public void setDownloadState(Game.DownloadState downloadState) {
        this.downloadState = downloadState;
    }
    
    public int getDownloadProgress() {
        return downloadProgress;
    }
    
    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
    
    // Utility methods
    public String getFormattedSize() {
        return formatFileSize(fileSize);
    }

    public String getFormattedFileSize() {
        return formatFileSize(fileSize);
    }
    
    public String getCachedIconPath(String cacheDir) {
        return cacheDir + "/icons/" + id + ".jpg";
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
        
        DLC dlc = (DLC) obj;
        return id == dlc.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "DLC{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isInstalled=" + isInstalled +
                ", downloadState=" + downloadState +
                '}';
    }
}