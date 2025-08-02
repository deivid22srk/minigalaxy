package com.minigalaxy.android.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.minigalaxy.android.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        setupToolbar();
        setupViews();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about);
        }
    }
    
    private void setupViews() {
        TextView textViewVersion = findViewById(R.id.textViewVersion);
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        MaterialCardView cardOriginalProject = findViewById(R.id.cardOriginalProject);
        MaterialCardView cardGitHub = findViewById(R.id.cardGitHub);
        MaterialCardView cardLicense = findViewById(R.id.cardLicense);
        
        // Set version
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textViewVersion.setText(getString(R.string.version_format, packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            textViewVersion.setText(R.string.version_unknown);
        }
        
        // Set description
        textViewDescription.setText(R.string.app_description);
        
        // Setup click listeners
        cardOriginalProject.setOnClickListener(v -> openUrl("https://github.com/sharkwouter/minigalaxy"));
        cardGitHub.setOnClickListener(v -> openUrl("https://github.com/yourusername/minigalaxy-android"));
        cardLicense.setOnClickListener(v -> openUrl("https://www.gnu.org/licenses/gpl-3.0.html"));
    }
    
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}