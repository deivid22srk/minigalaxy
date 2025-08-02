package com.minigalaxy.android.download;

import android.content.Context;
import android.util.Log;

import com.minigalaxy.android.config.Config;
import com.minigalaxy.android.config.Constants;
import com.minigalaxy.android.model.Download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Manager for handling file downloads
 */
public class DownloadManager {
    
    private static final String TAG = "DownloadManager";
    
    private final Context context;
    private final Config config;
    private final OkHttpClient httpClient;
    private final ExecutorService downloadExecutor;
    
    private final PriorityBlockingQueue<QueuedDownload> downloadQueue;
    private final Map<String, Download> activeDownloads;
    private final List<DownloadManagerListener> listeners;
    private static DownloadManager instance;
    
    private DownloadManager(Context context) {
        this.context = context;
        this.config = Config.getInstance(context);
        this.httpClient = new OkHttpClient.Builder().build();
        
        int threadCount = config.getConcurrentDownloads();
        this.downloadExecutor = Executors.newFixedThreadPool(threadCount);
        
        this.downloadQueue = new PriorityBlockingQueue<>();
        this.activeDownloads = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        
        startQueueProcessor();
    }

    public static synchronized DownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void addDownload(Download download) {
        addDownload(download, 1);
    }
    
    public void addDownload(Download download, int priority) {
        if (download == null || download.getId() == null) {
            return;
        }
        
        download.setStatus(Download.DownloadStatus.QUEUED);
        downloadQueue.offer(new QueuedDownload(download, priority));
        
        notifyDownloadAdded(download);
        processQueue();
    }

    public void addDownload(Game game) {
        if (game == null) return;

        String url = game.getUrl(); // Assuming Game has a getUrl() for the download
        if (url == null || url.isEmpty()) return;

        String fileName = game.getName() + ".exe"; // Or a more appropriate extension
        String destPath = new File(config.getInstallDir(), fileName).getAbsolutePath();

        Download download = new Download(
            String.valueOf(game.getId()),
            url,
            fileName,
            destPath,
            game.getFileSize(),
            Download.DownloadType.GAME
        );
        download.setRelatedGame(game);
        addDownload(download);
    }
    
    public void pauseDownload(String downloadId) {
        Download download = activeDownloads.get(downloadId);
        if (download != null && download.canPause()) {
            download.setStatus(Download.DownloadStatus.PAUSED);
            notifyDownloadUpdated(download);
        }
    }

    public void retryDownload(String downloadId) {
        Download failedDownload = getDownloadById(downloadId);
        if (failedDownload != null && failedDownload.isFailed()) {
            // Create a new download object to avoid issues with the old one
            Download newDownload = new Download(
                failedDownload.getId(),
                failedDownload.getUrl(),
                failedDownload.getFilename(),
                failedDownload.getDestinationPath(),
                failedDownload.getTotalSize(),
                failedDownload.getType()
            );
            newDownload.setRelatedGame(failedDownload.getRelatedGame());
            newDownload.setRelatedDLC(failedDownload.getRelatedDLC());
            addDownload(newDownload);
        }
    }
    
    public void resumeDownload(String downloadId) {
        Download download = getDownloadById(downloadId);
        if (download != null && download.canResume()) {
            download.setStatus(Download.DownloadStatus.QUEUED);
            downloadQueue.offer(new QueuedDownload(download, 1));
            notifyDownloadUpdated(download);
            processQueue();
        }
    }
    
    public void cancelDownload(String downloadId) {
        Download download = activeDownloads.get(downloadId);
        if (download != null && download.canCancel()) {
            download.setStatus(Download.DownloadStatus.CANCELLED);
            activeDownloads.remove(downloadId);
            
            File partialFile = new File(download.getDestinationPath() + ".partial");
            if (partialFile.exists()) {
                partialFile.delete();
            }
            
            notifyDownloadUpdated(download);
        }
    }
    
    public Download getDownloadById(String downloadId) {
        Download activeDownload = activeDownloads.get(downloadId);
        if (activeDownload != null) {
            return activeDownload;
        }
        
        for (QueuedDownload queuedDownload : downloadQueue) {
            if (queuedDownload.download.getId().equals(downloadId)) {
                return queuedDownload.download;
            }
        }
        
        return null;
    }

    public Download getDownloadForGame(long gameId) {
        // Check active downloads first
        for (Download download : activeDownloads.values()) {
            if (download.getRelatedGame() != null && download.getRelatedGame().getId() == gameId) {
                return download;
            }
        }
        // Then check queued downloads
        for (QueuedDownload queuedDownload : downloadQueue) {
            Download download = queuedDownload.download;
            if (download.getRelatedGame() != null && download.getRelatedGame().getId() == gameId) {
                return download;
            }
        }
        return null;
    }
    
    public List<Download> getActiveDownloads() {
        return new ArrayList<>(activeDownloads.values());
    }
    
    public List<Download> getAllDownloads() {
        List<Download> allDownloads = new ArrayList<>(activeDownloads.values());
        for (QueuedDownload queuedDownload : downloadQueue) {
            allDownloads.add(queuedDownload.download);
        }
        return allDownloads;
    }
    
    private void startQueueProcessor() {
        downloadExecutor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    processQueue();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void processQueue() {
        int maxConcurrent = config.getConcurrentDownloads();
        int currentActive = activeDownloads.size();
        
        while (currentActive < maxConcurrent && !downloadQueue.isEmpty()) {
            QueuedDownload queuedDownload = downloadQueue.poll();
            if (queuedDownload != null) {
                startDownload(queuedDownload.download);
                currentActive++;
            }
        }
    }
    
    private void startDownload(Download download) {
        download.setStatus(Download.DownloadStatus.DOWNLOADING);
        download.setStartTime(System.currentTimeMillis());
        
        activeDownloads.put(download.getId(), download);
        
        downloadExecutor.execute(new DownloadTask(download));
        notifyDownloadStarted(download);
    }
    
    public void addListener(DownloadManagerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(DownloadManagerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void notifyDownloadAdded(Download download) {
        synchronized (listeners) {
            for (DownloadManagerListener listener : listeners) {
                listener.onDownloadAdded(download);
            }
        }
    }
    
    private void notifyDownloadStarted(Download download) {
        synchronized (listeners) {
            for (DownloadManagerListener listener : listeners) {
                listener.onDownloadStarted(download);
            }
        }
    }
    
    private void notifyDownloadUpdated(Download download) {
        synchronized (listeners) {
            for (DownloadManagerListener listener : listeners) {
                listener.onDownloadProgress(download);
            }
        }
    }
    
    private void notifyDownloadCompleted(Download download) {
        synchronized (listeners) {
            for (DownloadManagerListener listener : listeners) {
                listener.onDownloadCompleted(download);
            }
        }
    }
    
    private void notifyDownloadFailed(Download download, String error) {
        synchronized (listeners) {
            for (DownloadManagerListener listener : listeners) {
                listener.onDownloadFailed(download, error);
            }
        }
    }
    
    public void cleanup() {
        downloadExecutor.shutdown();
        synchronized (listeners) {
            listeners.clear();
        }
    }
    
    private static class QueuedDownload implements Comparable<QueuedDownload> {
        final Download download;
        final int priority;
        final long queueTime;
        
        QueuedDownload(Download download, int priority) {
            this.download = download;
            this.priority = priority;
            this.queueTime = System.currentTimeMillis();
        }
        
        @Override
        public int compareTo(QueuedDownload other) {
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority);
            }
            return Long.compare(this.queueTime, other.queueTime);
        }
    }
    
    private class DownloadTask implements Runnable {
        private final Download download;
        
        DownloadTask(Download download) {
            this.download = download;
        }
        
        @Override
        public void run() {
            try {
                downloadFile();
            } catch (Exception e) {
                Log.e(TAG, "Download failed: " + download.getFilename(), e);
                download.setStatus(Download.DownloadStatus.FAILED);
                download.setErrorMessage(e.getMessage());
                notifyDownloadFailed(download, e.getMessage());
            } finally {
                activeDownloads.remove(download.getId());
                processQueue();
            }
        }
        
        private void downloadFile() throws IOException {
            File destinationFile = new File(download.getDestinationPath());
            File partialFile = new File(download.getDestinationPath() + ".partial");
            
            destinationFile.getParentFile().mkdirs();
            
            Request request = new Request.Builder()
                    .url(download.getUrl())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP error: " + response.code());
                }
                
                long contentLength = response.body().contentLength();
                if (contentLength > 0) {
                    download.setTotalSize(contentLength);
                }
                
                downloadWithProgress(response.body().byteStream(), partialFile);
                
                if (partialFile.renameTo(destinationFile)) {
                    download.setStatus(Download.DownloadStatus.COMPLETED);
                    download.setEndTime(System.currentTimeMillis());
                    notifyDownloadCompleted(download);
                } else {
                    throw new IOException("Failed to move file to final location");
                }
            }
        }
        
        private void downloadWithProgress(InputStream inputStream, File partialFile) throws IOException {
            byte[] buffer = new byte[Constants.DOWNLOAD_CHUNK_SIZE];
            long totalBytesRead = 0;
            long lastUpdate = System.currentTimeMillis();
            
            try (FileOutputStream outputStream = new FileOutputStream(partialFile)) {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdate >= 1000) {
                        download.updateProgress(totalBytesRead, 0);
                        notifyDownloadUpdated(download);
                        lastUpdate = currentTime;
                    }
                }
            }
        }
    }
    
    public interface DownloadManagerListener {
        void onDownloadAdded(Download download);
        void onDownloadStarted(Download download);
        void onDownloadProgress(Download download);
        void onDownloadCompleted(Download download);
        void onDownloadFailed(Download download, String error);
    }
}