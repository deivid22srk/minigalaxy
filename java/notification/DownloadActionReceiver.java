package com.minigalaxy.android.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.minigalaxy.android.download.DownloadManager;

public class DownloadActionReceiver extends BroadcastReceiver {
    public static final String ACTION_PAUSE = "com.minigalaxy.android.ACTION_PAUSE_DOWNLOAD";
    public static final String ACTION_RESUME = "com.minigalaxy.android.ACTION_RESUME_DOWNLOAD";
    public static final String ACTION_CANCEL = "com.minigalaxy.android.ACTION_CANCEL_DOWNLOAD";
    public static final String ACTION_RETRY = "com.minigalaxy.android.ACTION_RETRY_DOWNLOAD";
    
    public static final String EXTRA_DOWNLOAD_ID = "download_id";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID);
        
        if (downloadId == null || action == null) {
            return;
        }
        
        DownloadManager downloadManager = DownloadManager.getInstance(context);
        
        switch (action) {
            case ACTION_PAUSE:
                downloadManager.pauseDownload(downloadId);
                break;
                
            case ACTION_RESUME:
                downloadManager.resumeDownload(downloadId);
                break;
                
            case ACTION_CANCEL:
                downloadManager.cancelDownload(downloadId);
                NotificationManager.getInstance(context).cancelDownloadNotification(downloadId);
                break;
                
            case ACTION_RETRY:
                downloadManager.retryDownload(downloadId);
                break;
        }
    }
}