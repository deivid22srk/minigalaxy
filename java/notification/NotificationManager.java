package com.minigalaxy.android.notification;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.minigalaxy.android.R;
import com.minigalaxy.android.config.Config;
import com.minigalaxy.android.model.Download;
import com.minigalaxy.android.ui.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager {
    private static final String CHANNEL_DOWNLOADS = "downloads";
    private static final String CHANNEL_UPDATES = "updates";
    
    private static NotificationManager instance;
    private Context context;
    private android.app.NotificationManager notificationManager;
    private Config config;
    private Map<String, Integer> downloadNotificationIds;
    private int nextNotificationId = 1000;
    
    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (android.app.NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.config = Config.getInstance(context);
        this.downloadNotificationIds = new HashMap<>();
        
        createNotificationChannels();
    }
    
    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Downloads channel
            NotificationChannel downloadsChannel = new NotificationChannel(
                CHANNEL_DOWNLOADS,
                context.getString(R.string.notification_channel_downloads),
                android.app.NotificationManager.IMPORTANCE_LOW
            );
            downloadsChannel.setDescription(context.getString(R.string.notification_channel_downloads_desc));
            downloadsChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(downloadsChannel);
            
            // Updates channel
            NotificationChannel updatesChannel = new NotificationChannel(
                CHANNEL_UPDATES,
                "Game Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            updatesChannel.setDescription("Notifications for game updates");
            notificationManager.createNotificationChannel(updatesChannel);
        }
    }
    
    public void showDownloadProgress(Download download) {
        if (!config.getDownloadNotifications()) {
            return;
        }
        
        int notificationId = getDownloadNotificationId(download.getId());
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(context.getString(R.string.notification_download_progress, download.getGameName()))
            .setContentText(String.format("%.1f%% - %s", download.getProgress(), download.getFormattedSpeed()))
            .setProgress(100, (int) download.getProgress(), false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Add pause/resume action
        if (download.getStatus() == Download.Status.DOWNLOADING) {
            Intent pauseIntent = new Intent(context, DownloadActionReceiver.class);
            pauseIntent.setAction(DownloadActionReceiver.ACTION_PAUSE);
            pauseIntent.putExtra(DownloadActionReceiver.EXTRA_DOWNLOAD_ID, download.getId());
            
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(
                context, notificationId + 1, pauseIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                    PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
            );
            
            builder.addAction(R.drawable.ic_pause, "Pausar", pausePendingIntent);
        }
        
        // Add cancel action
        Intent cancelIntent = new Intent(context, DownloadActionReceiver.class);
        cancelIntent.setAction(DownloadActionReceiver.ACTION_CANCEL);
        cancelIntent.putExtra(DownloadActionReceiver.EXTRA_DOWNLOAD_ID, download.getId());
        
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2, cancelIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        builder.addAction(R.drawable.ic_cancel, "Cancelar", cancelPendingIntent);
        
        notificationManager.notify(notificationId, builder.build());
    }
    
    public void showDownloadPaused(Download download) {
        if (!config.getDownloadNotifications()) {
            return;
        }
        
        int notificationId = getDownloadNotificationId(download.getId());
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(R.drawable.ic_pause)
            .setContentTitle(download.getGameName())
            .setContentText("Download pausado")
            .setProgress(100, (int) download.getProgress(), false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Add resume action
        Intent resumeIntent = new Intent(context, DownloadActionReceiver.class);
        resumeIntent.setAction(DownloadActionReceiver.ACTION_RESUME);
        resumeIntent.putExtra(DownloadActionReceiver.EXTRA_DOWNLOAD_ID, download.getId());
        
        PendingIntent resumePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, resumeIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        builder.addAction(R.drawable.ic_play, "Continuar", resumePendingIntent);
        
        // Add cancel action
        Intent cancelIntent = new Intent(context, DownloadActionReceiver.class);
        cancelIntent.setAction(DownloadActionReceiver.ACTION_CANCEL);
        cancelIntent.putExtra(DownloadActionReceiver.EXTRA_DOWNLOAD_ID, download.getId());
        
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2, cancelIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        builder.addAction(R.drawable.ic_cancel, "Cancelar", cancelPendingIntent);
        
        notificationManager.notify(notificationId, builder.build());
    }
    
    public void showDownloadCompleted(Download download) {
        if (!config.getDownloadNotifications()) {
            return;
        }
        
        int notificationId = getDownloadNotificationId(download.getId());
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(context.getString(R.string.notification_download_complete, download.getGameName()))
            .setContentText("Download concluído com sucesso")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        notificationManager.notify(notificationId, builder.build());
        
        // Remove from ongoing downloads tracking
        downloadNotificationIds.remove(download.getId());
    }
    
    public void showDownloadFailed(Download download, String error) {
        if (!config.getDownloadNotifications()) {
            return;
        }
        
        int notificationId = getDownloadNotificationId(download.getId());
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle(context.getString(R.string.notification_download_failed, download.getGameName()))
            .setContentText(error)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        // Add retry action
        Intent retryIntent = new Intent(context, DownloadActionReceiver.class);
        retryIntent.setAction(DownloadActionReceiver.ACTION_RETRY);
        retryIntent.putExtra(DownloadActionReceiver.EXTRA_DOWNLOAD_ID, download.getId());
        
        PendingIntent retryPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, retryIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        builder.addAction(R.drawable.ic_refresh, "Tentar Novamente", retryPendingIntent);
        
        notificationManager.notify(notificationId, builder.build());
        
        // Remove from ongoing downloads tracking
        downloadNotificationIds.remove(download.getId());
    }
    
    public void showInstallCompleted(String gameName) {
        if (!config.getUpdateNotifications()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(context.getString(R.string.notification_install_complete, gameName))
            .setContentText("Jogo instalado e pronto para jogar")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        notificationManager.notify(nextNotificationId++, builder.build());
    }
    
    public void showInstallFailed(String gameName, String error) {
        if (!config.getUpdateNotifications()) {
            return;
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle(context.getString(R.string.notification_install_failed, gameName))
            .setContentText(error)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        notificationManager.notify(nextNotificationId++, builder.build());
    }
    
    public void cancelDownloadNotification(String downloadId) {
        Integer notificationId = downloadNotificationIds.get(downloadId);
        if (notificationId != null) {
            notificationManager.cancel(notificationId);
            downloadNotificationIds.remove(downloadId);
        }
    }
    
    public void cancelAllDownloadNotifications() {
        for (Integer notificationId : downloadNotificationIds.values()) {
            notificationManager.cancel(notificationId);
        }
        downloadNotificationIds.clear();
    }
    
    private int getDownloadNotificationId(String downloadId) {
        return downloadNotificationIds.computeIfAbsent(downloadId, k -> nextNotificationId++);
    }
}