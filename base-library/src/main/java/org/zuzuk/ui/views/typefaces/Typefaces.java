package org.zuzuk.ui.views.typefaces;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Typefaces manager
 */
public class Typefaces {
    private final static HashMap<String, Typeface> typefaces = new HashMap<>();

    /* Returns typefaces by name from assets 'fonts' folder */
    public static Typeface getByName(Context context, String name) {
        Typeface result = typefaces.get(name);
        if (result == null) {
            AssetManager assetManager = context.getAssets();

            try {
                List<String> fonts = Arrays.asList(assetManager.list("fonts"));
                if (fonts.contains(name + ".ttf")) {
                    result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".ttf");
                } else if (fonts.contains(name + ".otf")) {
                    result = Typeface.createFromAsset(assetManager, "fonts/" + name + ".otf");
                } else
                    throw new IllegalStateException("Can't find .otf or .ttf file in folder 'fonts' with name: " + name);
            } catch (IOException e) {
                throw new IllegalStateException("Typefaces files should be in folder named 'fonts'");
            }
            typefaces.put(name, result);
        }
        return result;
    }
}