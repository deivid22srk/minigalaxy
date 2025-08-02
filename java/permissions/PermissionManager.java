package com.minigalaxy.android.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    
    // Permission request codes
    public static final int REQUEST_STORAGE_PERMISSION = 1001;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1002;
    public static final int REQUEST_ALL_PERMISSIONS = 1003;
    
    // Required permissions
    private static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private static final String[] NOTIFICATION_PERMISSIONS = {
        Manifest.permission.POST_NOTIFICATIONS // API 33+
    };
    
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied(String[] deniedPermissions);
    }
    
    /**
     * Check if storage permissions are granted
     */
    public static boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above, use scoped storage
            // No need for WRITE_EXTERNAL_STORAGE permission
            return true;
        } else {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Check if notification permissions are granted (API 33+)
     */
    public static boolean hasNotificationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true; // No permission needed for older versions
    }
    
    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Context context) {
        return hasStoragePermissions(context) && hasNotificationPermissions(context);
    }
    
    /**
     * Request storage permissions
     */
    public static void requestStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            List<String> permissionsToRequest = new ArrayList<>();
            
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(activity,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_STORAGE_PERMISSION);
            }
        }
    }
    
    /**
     * Request notification permissions (API 33+)
     */
    public static void requestNotificationPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    NOTIFICATION_PERMISSIONS,
                    REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
    
    /**
     * Request all required permissions
     */
    public static void requestAllPermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check storage permissions
        if (!hasStoragePermissions(activity) && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        // Check notification permissions
        if (!hasNotificationPermissions(activity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                permissionsToRequest.toArray(new String[0]),
                REQUEST_ALL_PERMISSIONS);
        }
    }
    
    /**
     * Handle permission request results
     */
    public static void handlePermissionResult(int requestCode, String[] permissions, 
                                            int[] grantResults, PermissionCallback callback) {
        
        boolean allGranted = true;
        List<String> deniedPermissions = new ArrayList<>();
        
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                deniedPermissions.add(permissions[i]);
            }
        }
        
        if (allGranted) {
            callback.onPermissionGranted();
        } else {
            callback.onPermissionDenied(deniedPermissions.toArray(new String[0]));
        }
    }
    
    /**
     * Check if we should show permission rationale
     */
    public static boolean shouldShowPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    /**
     * Get user-friendly permission names for display
     */
    public static String getPermissionDisplayName(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Leitura de Armazenamento";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Escrita de Armazenamento";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notificações";
            default:
                return permission;
        }
    }
    
    /**
     * Get permission explanation for user
     */
    public static String getPermissionExplanation(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Necessário para ler arquivos de jogos instalados";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Necessário para baixar e instalar jogos";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Para mostrar notificações de progresso de download";
            default:
                return "Esta permissão é necessária para o funcionamento do aplicativo";
        }
    }
}