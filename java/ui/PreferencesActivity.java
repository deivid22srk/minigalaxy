package com.minigalaxy.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.minigalaxy.android.R;
import com.minigalaxy.android.config.Config;

import java.io.File;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.preferences);
        }
        
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new PreferencesFragment())
                .commit();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat {
        private Config config;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            
            config = Config.getInstance(requireContext());
            setupPreferences();
        }

        private void setupPreferences() {
            setupLanguagePreference();
            setupViewPreference();
            setupInstallDirPreference();
            setupDownloadPreferences();
            setupNotificationPreferences();
            setupAdvancedPreferences();
        }

        private void setupLanguagePreference() {
            ListPreference langPreference = findPreference("pref_language");
            if (langPreference != null) {
                langPreference.setValue(config.getLang());
                langPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setLang((String) newValue);
                    // Restart activity to apply language change
                    requireActivity().recreate();
                    return true;
                });
            }
        }

        private void setupViewPreference() {
            ListPreference viewPreference = findPreference("pref_view_type");
            if (viewPreference != null) {
                viewPreference.setValue(config.getView());
                viewPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setView((String) newValue);
                    Toast.makeText(getContext(), 
                        "Reinicie o aplicativo para aplicar a mudança", 
                        Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }

        private void setupInstallDirPreference() {
            EditTextPreference installDirPreference = findPreference("pref_install_dir");
            if (installDirPreference != null) {
                String currentDir = config.getInstallDir();
                installDirPreference.setText(currentDir);
                installDirPreference.setSummary(currentDir);
                
                installDirPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    String newDir = (String) newValue;
                    File dir = new File(newDir);
                    
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            Toast.makeText(getContext(), 
                                "Não foi possível criar o diretório", 
                                Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    
                    if (!dir.canWrite()) {
                        Toast.makeText(getContext(), 
                            "Sem permissão de escrita no diretório", 
                            Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    config.setInstallDir(newDir);
                    installDirPreference.setSummary(newDir);
                    return true;
                });
            }
        }

        private void setupDownloadPreferences() {
            // Keep installers preference
            SwitchPreferenceCompat keepInstallersPreference = findPreference("pref_keep_installers");
            if (keepInstallersPreference != null) {
                keepInstallersPreference.setChecked(config.getKeepInstallers());
                keepInstallersPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setKeepInstallers((Boolean) newValue);
                    return true;
                });
            }

            // Auto download updates
            SwitchPreferenceCompat autoUpdatePreference = findPreference("pref_auto_download_updates");
            if (autoUpdatePreference != null) {
                autoUpdatePreference.setChecked(config.getAutoDownloadUpdates());
                autoUpdatePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setAutoDownloadUpdates((Boolean) newValue);
                    return true;
                });
            }

            // Concurrent downloads
            ListPreference concurrentDownloadsPreference = findPreference("pref_concurrent_downloads");
            if (concurrentDownloadsPreference != null) {
                concurrentDownloadsPreference.setValue(String.valueOf(config.getConcurrentDownloads()));
                concurrentDownloadsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    int value = Integer.parseInt((String) newValue);
                    config.setConcurrentDownloads(value);
                    return true;
                });
            }

            // Download speed limit
            EditTextPreference speedLimitPreference = findPreference("pref_download_speed_limit");
            if (speedLimitPreference != null) {
                int currentLimit = config.getDownloadSpeedLimit();
                speedLimitPreference.setText(currentLimit > 0 ? String.valueOf(currentLimit) : "");
                speedLimitPreference.setSummary(currentLimit > 0 ? 
                    currentLimit + " KB/s" : "Sem limite");
                
                speedLimitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    String value = (String) newValue;
                    int limit = 0;
                    
                    if (!value.isEmpty()) {
                        try {
                            limit = Integer.parseInt(value);
                            if (limit < 0) limit = 0;
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), 
                                "Valor inválido", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    
                    config.setDownloadSpeedLimit(limit);
                    speedLimitPreference.setSummary(limit > 0 ? 
                        limit + " KB/s" : "Sem limite");
                    return true;
                });
            }
        }

        private void setupNotificationPreferences() {
            SwitchPreferenceCompat downloadNotificationsPreference = findPreference("pref_download_notifications");
            if (downloadNotificationsPreference != null) {
                downloadNotificationsPreference.setChecked(config.getDownloadNotifications());
                downloadNotificationsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setDownloadNotifications((Boolean) newValue);
                    return true;
                });
            }

            SwitchPreferenceCompat updateNotificationsPreference = findPreference("pref_update_notifications");
            if (updateNotificationsPreference != null) {
                updateNotificationsPreference.setChecked(config.getUpdateNotifications());
                updateNotificationsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    config.setUpdateNotifications((Boolean) newValue);
                    return true;
                });
            }
        }

        private void setupAdvancedPreferences() {
            // Clear cache preference
            Preference clearCachePreference = findPreference("pref_clear_cache");
            if (clearCachePreference != null) {
                clearCachePreference.setOnPreferenceClickListener(preference -> {
                    clearCache();
                    return true;
                });
            }

            // Reset preferences
            Preference resetPreference = findPreference("pref_reset_settings");
            if (resetPreference != null) {
                resetPreference.setOnPreferenceClickListener(preference -> {
                    resetSettings();
                    return true;
                });
            }

            // About preference
            Preference aboutPreference = findPreference("pref_about");
            if (aboutPreference != null) {
                aboutPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                    return true;
                });
            }
        }

        private void clearCache() {
            try {
                File cacheDir = requireContext().getCacheDir();
                deleteDirectory(cacheDir);
                
                // Clear image cache
                File imageCache = new File(requireContext().getFilesDir(), "image_cache");
                deleteDirectory(imageCache);
                
                Toast.makeText(getContext(), 
                    "Cache limpo com sucesso", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), 
                    "Erro ao limpar cache: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }

        private void resetSettings() {
            config.resetToDefaults();
            
            // Refresh all preferences to show default values
            getPreferenceScreen().removeAll();
            onCreatePreferences(null, null);
            
            Toast.makeText(getContext(), 
                "Configurações resetadas para o padrão", 
                Toast.LENGTH_SHORT).show();
        }

        private boolean deleteDirectory(File directory) {
            if (directory != null && directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteDirectory(file);
                        } else {
                            file.delete();
                        }
                    }
                }
                return directory.delete();
            }
            return false;
        }
    }
}