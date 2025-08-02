package com.minigalaxy.android.model;

/**
 * Model class representing a download task
 */
public class Download {
    
    public enum DownloadType {
        GAME,
        DLC,
        UPDATE,
        PATCH,
        THUMBNAIL,
        ICON,
        OTHER
    }
    
    public enum DownloadStatus {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    private String id;
    private String url;
    private String filename;
    private String destinationPath;
    private long totalSize;
    private long downloadedSize;
    private DownloadType type;
    private DownloadStatus status;
    private String errorMessage;
    private long startTime;
    private long endTime;
    private int priority;
    private String md5Hash;
    private Game relatedGame;
    private DLC relatedDLC;
    private double downloadSpeed; // bytes per second
    private long remainingTime; // seconds
    
    public Download() {
        this.status = DownloadStatus.QUEUED;
        this.priority = 1;
        this.startTime = System.currentTimeMillis();
    }
    
    public Download(String id, String url, String filename, String destinationPath, 
                   long totalSize, DownloadType type) {
        this();
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.destinationPath = destinationPath;
        this.totalSize = totalSize;
        this.type = type;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getDestinationPath() {
        return destinationPath;
    }
    
    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    
    public long getDownloadedSize() {
        return downloadedSize;
    }
    
    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
    
    public DownloadType getType() {
        return type;
    }
    
    public void setType(DownloadType type) {
        this.type = type;
    }
    
    public DownloadStatus getStatus() {
        return status;
    }
    
    public void setStatus(DownloadStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getMd5Hash() {
        return md5Hash;
    }
    
    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }
    
    public Game getRelatedGame() {
        return relatedGame;
    }
    
    public void setRelatedGame(Game relatedGame) {
        this.relatedGame = relatedGame;
    }
    
    public DLC getRelatedDLC() {
        return relatedDLC;
    }
    
    public void setRelatedDLC(DLC relatedDLC) {
        this.relatedDLC = relatedDLC;
    }
    
    public double getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
    
    public long getRemainingTime() {
        return remainingTime;
    }
    
    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    // Utility methods
    public int getProgressPercentage() {
        if (totalSize <= 0) return 0;
        return (int) ((downloadedSize * 100) / totalSize);
    }
    
    public String getFormattedTotalSize() {
        return formatFileSize(totalSize);
    }
    
    public String getFormattedDownloadedSize() {
        return formatFileSize(downloadedSize);
    }
    
    public String getFormattedDownloadSpeed() {
        return formatFileSize((long) downloadSpeed) + "/s";
    }
    
    public String getFormattedRemainingTime() {
        return formatTime(remainingTime);
    }
    
    public long getElapsedTime() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    public String getFormattedElapsedTime() {
        return formatTime(getElapsedTime() / 1000);
    }
    
    public boolean isCompleted() {
        return status == DownloadStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == DownloadStatus.FAILED;
    }
    
    public boolean isInProgress() {
        return status == DownloadStatus.DOWNLOADING;
    }
    
    public boolean isPaused() {
        return status == DownloadStatus.PAUSED;
    }
    
    public boolean isCancelled() {
        return status == DownloadStatus.CANCELLED;
    }
    
    public boolean canResume() {
        return (status == DownloadStatus.PAUSED || status == DownloadStatus.FAILED) 
               && downloadedSize > 0;
    }
    
    public boolean canPause() {
        return status == DownloadStatus.DOWNLOADING;
    }
    
    public boolean canCancel() {
        return status == DownloadStatus.DOWNLOADING || 
               status == DownloadStatus.PAUSED || 
               status == DownloadStatus.QUEUED;
    }
    
    public void updateProgress(long downloadedBytes, double speedBps) {
        this.downloadedSize = downloadedBytes;
        this.downloadSpeed = speedBps;
        
        if (speedBps > 0 && totalSize > downloadedBytes) {
            this.remainingTime = (long) ((totalSize - downloadedBytes) / speedBps);
        } else {
            this.remainingTime = 0;
        }
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
    
    private static String formatTime(long seconds) {
        if (seconds <= 0) return "Unknown";
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm", minutes);
        } else {
            return String.format("%ds", secs);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Download download = (Download) obj;
        return id != null ? id.equals(download.id) : download.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Download{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", status=" + status +
                ", progress=" + getProgressPercentage() + "%" +
                ", type=" + type +
                '}';
    }
}