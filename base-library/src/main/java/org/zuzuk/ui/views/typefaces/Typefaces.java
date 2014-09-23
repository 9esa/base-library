package org.zuzuk.ui.views.typefaces;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Typefaces manager
 */
public class Typefaces {
    private final static HashMap<String, Typeface> typefaces = new HashMap<>();

    private static boolean isInitialized = false;
    private static String defaultTypefaceName;

    /* Initialize typefaces directly by their asset paths */
    public static void initialize(Context context, String defaultTypefaceName, String[] paths) {
        AssetManager assetManager = context.getAssets();
        for (String path : paths) {
            String name = path.substring(path.lastIndexOf('/') + 1, path.length() - 4);
            typefaces.put(name, Typeface.createFromAsset(assetManager, path));
        }
        isInitialized = true;
        Typefaces.defaultTypefaceName = defaultTypefaceName;
    }

    public static String getDefaultTypefaceName() {
        return defaultTypefaceName;
    }

    public static Typeface getByName(String name) {
        if (!isInitialized)
            throw new RuntimeException("You should initialize Typefaces class first");

        if (name.endsWith(".otf") || name.endsWith(".ttf")) {
            return typefaces.get(name.substring(0, name.length() - 4));
        }
        return typefaces.get(name);
    }
}