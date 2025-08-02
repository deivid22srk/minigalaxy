package com.minigalaxy.android.model;

/**
 * Model class representing file information for downloads
 */
public class FileInfo {
    
    private String filename;
    private String url;
    private long size;
    private String md5;
    private String language;
    private String version;
    private String platform;
    private String type; // installer, patch, dlc, etc.
    
    public FileInfo() {
    }
    
    public FileInfo(String filename, String url, long size, String md5) {
        this.filename = filename;
        this.url = url;
        this.size = size;
        this.md5 = md5;
    }
    
    public FileInfo(String filename, String url, long size, String md5, 
                   String language, String version, String platform, String type) {
        this.filename = filename;
        this.url = url;
        this.size = size;
        this.md5 = md5;
        this.language = language;
        this.version = version;
        this.platform = platform;
        this.type = type;
    }
    
    // Getters and Setters
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    // Utility methods
    public String getFormattedSize() {
        return formatFileSize(size);
    }
    
    public String getFileExtension() {
        if (filename == null) return "";
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    public boolean isInstaller() {
        return "installer".equalsIgnoreCase(type);
    }
    
    public boolean isPatch() {
        return "patch".equalsIgnoreCase(type);
    }
    
    public boolean isDLC() {
        return "dlc".equalsIgnoreCase(type);
    }
    
    public boolean isLinuxCompatible() {
        return platform == null || 
               platform.toLowerCase().contains("linux") || 
               platform.toLowerCase().contains("all");
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
        
        FileInfo fileInfo = (FileInfo) obj;
        return size == fileInfo.size &&
               (filename != null ? filename.equals(fileInfo.filename) : fileInfo.filename == null) &&
               (url != null ? url.equals(fileInfo.url) : fileInfo.url == null) &&
               (md5 != null ? md5.equals(fileInfo.md5) : fileInfo.md5 == null);
    }
    
    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (md5 != null ? md5.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "FileInfo{" +
                "filename='" + filename + '\'' +
                ", size=" + formatFileSize(size) +
                ", platform='" + platform + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}