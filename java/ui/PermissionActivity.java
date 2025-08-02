package com.minigalaxy.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minigalaxy.android.R;
import com.minigalaxy.android.permissions.PermissionManager;
import com.minigalaxy.android.ui.adapter.PermissionAdapter;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity implements PermissionManager.PermissionCallback {
    
    private TextView textViewTitle;
    private TextView textViewDescription;
    private RecyclerView recyclerViewPermissions;
    private Button buttonGrant;
    private Button buttonSettings;
    private Button buttonSkip;
    
    private PermissionAdapter permissionAdapter;
    private List<PermissionAdapter.PermissionItem> permissionItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        
        initializeViews();
        setupPermissionsList();
        checkPermissions();
    }
    
    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        recyclerViewPermissions = findViewById(R.id.recyclerViewPermissions);
        buttonGrant = findViewById(R.id.buttonGrant);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonSkip = findViewById(R.id.buttonSkip);
        
        recyclerViewPermissions.setLayoutManager(new LinearLayoutManager(this));
        
        buttonGrant.setOnClickListener(v -> requestPermissions());
        buttonSettings.setOnClickListener(v -> openAppSettings());
        buttonSkip.setOnClickListener(v -> continueToApp());
    }
    
    private void setupPermissionsList() {
        permissionItems = new ArrayList<>();
        permissionAdapter = new PermissionAdapter(this, permissionItems);
        recyclerViewPermissions.setAdapter(permissionAdapter);
    }
    
    private void checkPermissions() {
        permissionItems.clear();
        
        // Check storage permissions
        boolean hasStorage = PermissionManager.hasStoragePermissions(this);
        permissionItems.add(new PermissionAdapter.PermissionItem(
            "Armazenamento",
            "Para baixar e gerenciar jogos",
            R.drawable.ic_storage,
            hasStorage
        ));
        
        // Check notification permissions
        boolean hasNotifications = PermissionManager.hasNotificationPermissions(this);
        permissionItems.add(new PermissionAdapter.PermissionItem(
            "Notificações",
            "Para mostrar progresso de downloads",
            R.drawable.ic_notifications,
            hasNotifications
        ));
        
        permissionAdapter.notifyDataSetChanged();
        
        // Update UI based on permissions status
        boolean allGranted = hasStorage && hasNotifications;
        
        if (allGranted) {
            textViewTitle.setText("Permissões Concedidas");
            textViewDescription.setText("Todas as permissões necessárias foram concedidas. Você pode começar a usar o aplicativo.");
            buttonGrant.setText("Continuar");
            buttonGrant.setOnClickListener(v -> continueToApp());
            buttonSettings.setVisibility(View.GONE);
            buttonSkip.setVisibility(View.GONE);
        } else {
            textViewTitle.setText("Permissões Necessárias");
            textViewDescription.setText("Para funcionar corretamente, o aplicativo precisa das seguintes permissões:");
            buttonGrant.setText("Conceder Permissões");
            buttonSettings.setVisibility(View.VISIBLE);
            buttonSkip.setVisibility(View.VISIBLE);
        }
    }
    
    private void requestPermissions() {
        PermissionManager.requestAllPermissions(this);
    }
    
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    
    private void continueToApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        PermissionManager.handlePermissionResult(requestCode, permissions, grantResults, this);
    }
    
    @Override
    public void onPermissionGranted() {
        checkPermissions(); // Refresh the UI
    }
    
    @Override
    public void onPermissionDenied(String[] deniedPermissions) {
        checkPermissions(); // Refresh the UI
        
        // Show explanation for denied permissions
        StringBuilder message = new StringBuilder("Permissões negadas:\n");
        for (String permission : deniedPermissions) {
            message.append("• ").append(PermissionManager.getPermissionDisplayName(permission)).append("\n");
        }
        message.append("\nVocê pode concedê-las nas configurações do aplicativo.");
        
        textViewDescription.setText(message.toString());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions(); // Refresh when returning from settings
    }
}