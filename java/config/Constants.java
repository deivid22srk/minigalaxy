package com.minigalaxy.android.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants class containing static values used throughout the application
 */
public class Constants {
    
    // Download constants
    public static final int DOWNLOAD_CHUNK_SIZE = 1024 * 1024; // 1 MB
    public static final long MINIMUM_RESUME_SIZE = 20 * 1024 * 1024; // 20 MB
    public static final int UI_DOWNLOAD_THREADS = 4;
    public static final int DEFAULT_DOWNLOAD_THREAD_COUNT = 4;
    
    // Supported download languages
    public static final String[][] SUPPORTED_DOWNLOAD_LANGUAGES = {
            {"br", "Brazilian Portuguese"},
            {"cn", "Chinese"},
            {"da", "Danish"},
            {"nl", "Dutch"},
            {"en", "English"},
            {"fi", "Finnish"},
            {"fr", "French"},
            {"de", "German"},
            {"hu", "Hungarian"},
            {"it", "Italian"},
            {"jp", "Japanese"},
            {"ko", "Korean"},
            {"no", "Norwegian"},
            {"pl", "Polish"},
            {"pt", "Portuguese"},
            {"ru", "Russian"},
            {"es", "Spanish"},
            {"sv", "Swedish"},
            {"tr", "Turkish"},
            {"ro", "Romanian"}
    };
    
    // Game language mapping
    public static final Map<String, String[]> GAME_LANGUAGE_IDS = new HashMap<String, String[]>() {{
        put("br", new String[]{"brazilian"});
        put("cn", new String[]{"chinese"});
        put("da", new String[]{"danish"});
        put("nl", new String[]{"dutch"});
        put("en", new String[]{"english"});
        put("fi", new String[]{"finnish"});
        put("fr", new String[]{"french"});
        put("de", new String[]{"german"});
        put("hu", new String[]{"hungarian"});
        put("it", new String[]{"italian"});
        put("jp", new String[]{"japanese"});
        put("ko", new String[]{"korean"});
        put("no", new String[]{"norwegian"});
        put("pl", new String[]{"polish"});
        put("pt", new String[]{"portuguese"});
        put("ru", new String[]{"russian"});
        put("es", new String[]{"spanish"});
        put("sv", new String[]{"swedish"});
        put("tr", new String[]{"turkish"});
        put("ro", new String[]{"romanian"});
    }};
    
    // Supported locales
    public static final String[][] SUPPORTED_LOCALES = {
            {"", "System default"},
            {"pt_BR", "Brazilian Portuguese"},
            {"cs_CZ", "Czech"},
            {"nl", "Dutch"},
            {"en_US", "English"},
            {"fi", "Finnish"},
            {"fr", "French"},
            {"de", "German"},
            {"it_IT", "Italian"},
            {"nb_NO", "Norwegian Bokmål"},
            {"nn_NO", "Norwegian Nynorsk"},
            {"pl", "Polish"},
            {"pt_PT", "Portuguese"},
            {"ru_RU", "Russian"},
            {"zh_CN", "Simplified Chinese"},
            {"es", "Spanish"},
            {"es_ES", "Spanish (Spain)"},
            {"sv_SE", "Swedish"},
            {"zh_TW", "Traditional Chinese"},
            {"tr", "Turkish"},
            {"uk", "Ukrainian"},
            {"el", "Greek"},
            {"ro", "Romanian"}
    };
    
    // View types
    public static final String[][] VIEWS = {
            {"grid", "Grid"},
            {"list", "List"}
    };
    
    // Game IDs to ignore when received by the API
    public static final List<Long> IGNORE_GAME_IDS = Arrays.asList(
            1424856371L,  // Hotline Miami 2: Wrong Number - Digital Comics
            1980301910L,  // The Witcher Goodies Collection
            2005648906L,  // Spring Sale Goodies Collection #1
            1486144755L,  // Cyberpunk 2077 Goodies Collection
            1581684020L,  // A Plague Tale Digital Goodies Pack
            1185685769L   // CDPR Goodie Pack Content
    );
    
    // Windows executables to ignore when launching
    public static final List<String> BINARY_NAMES_TO_IGNORE = Arrays.asList(
            // Standard uninstaller
            "unins000.exe",
            // Common extra binaries
            "UnityCrashHandler64.exe",
            "nglide_config.exe",
            // Diablo 2 specific
            "ipxconfig.exe",
            "BNUpdate.exe",
            "VidSize.exe",
            // FreeSpace 2 specific
            "FRED2.exe",
            "FS2.exe"
    );
    
    // API Constants
    public static final String GOG_LOGIN_SUCCESS_URL = "https://embed.gog.com/on_login_success";
    public static final String GOG_REDIRECT_URI = "https://embed.gog.com/on_login_success?origin=client";
    public static final String GOG_CLIENT_ID = "46899977096215655";
    public static final String GOG_CLIENT_SECRET = "9d85c43b1482497dbbce61f6e4aa173a433796eeae2ca8c5f6129f2dc4de46d9";
    
    // API URLs
    public static final String GOG_TOKEN_URL = "https://auth.gog.com/token";
    public static final String GOG_USER_URL = "https://embed.gog.com/user/data/games";
    public static final String GOG_GAMES_URL = "https://embed.gog.com/account/getFilteredProducts";
    public static final String GOG_GAME_DETAILS_URL = "https://api.gog.com/products/";
    public static final String GOG_DOWNLOAD_URL = "https://api.gog.com/products/{id}/downlink/{type}/{file_id}";
    
    // Notification constants
    public static final String NOTIFICATION_CHANNEL_DOWNLOADS = "downloads";
    public static final int NOTIFICATION_ID_DOWNLOAD_PROGRESS = 1000;
    public static final int NOTIFICATION_ID_DOWNLOAD_COMPLETE = 1001;
    public static final int NOTIFICATION_ID_DOWNLOAD_FAILED = 1002;
    
    // File extensions
    public static final List<String> INSTALLER_EXTENSIONS = Arrays.asList(
            "exe", "msi", "deb", "rpm", "tar.gz", "zip", "run", "sh"
    );
    
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );
    
    // Platform names
    public static final String PLATFORM_LINUX = "linux";
    public static final String PLATFORM_WINDOWS = "windows";
    public static final String PLATFORM_MAC = "mac";
    
    // Categories
    public static final String[] GAME_CATEGORIES = {
            "Action",
            "Adventure",
            "Puzzle",
            "Racing",
            "RPG",
            "Shooter",
            "Simulation",
            "Strategy",
            "Other"
    };
    
    // Error codes
    public static final int ERROR_NETWORK = 1000;
    public static final int ERROR_AUTH = 1001;
    public static final int ERROR_DOWNLOAD = 1002;
    public static final int ERROR_INSTALL = 1003;
    public static final int ERROR_PERMISSION = 1004;
    public static final int ERROR_STORAGE = 1005;
    public static final int ERROR_UNKNOWN = 9999;
    
    // Request codes
    public static final int REQUEST_CODE_LOGIN = 100;
    public static final int REQUEST_CODE_PERMISSIONS = 101;
    public static final int REQUEST_CODE_INSTALL_DIR = 102;
    
    // Bundle keys
    public static final String BUNDLE_GAME_ID = "game_id";
    public static final String BUNDLE_GAME_NAME = "game_name";
    public static final String BUNDLE_DOWNLOAD_ID = "download_id";
    
    // Shared preferences keys
    public static final String PREF_FIRST_RUN = "first_run";
    public static final String PREF_LAST_SYNC = "last_sync";
    public static final String PREF_SYNC_INTERVAL = "sync_interval";
    
    // Default values
    public static final long DEFAULT_SYNC_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}