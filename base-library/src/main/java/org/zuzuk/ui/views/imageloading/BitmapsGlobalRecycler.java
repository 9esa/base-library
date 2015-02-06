package org.zuzuk.ui.views.imageloading;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.zuzuk.utils.Lc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Class that recycles bitmaps if there is no other objects using them.
 * NOTE: it is enable ONLY for 2.3 devices because it is make memory cache unable to use
 */
public enum BitmapsGlobalRecycler {
    Instance;

    private final static HashMap<String, BitmapReferencesEntry> BitmapReferences = new HashMap<>();
    private final Object lock = new Object();

    /* Adds reference to groups of bitmap */
    public void addReference(@NonNull String signature, Bitmap bitmap, ImageLoader imageLoader) {
        // enable it only for pre-HONEYCOMB devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        synchronized (lock) {
            BitmapReferencesEntry entry = BitmapReferences.get(signature);
            if (entry == null) {
                entry = new BitmapReferencesEntry();
                BitmapReferences.put(signature, entry);
            }
            if (bitmap != null && !entry.contains(bitmap)) {
                entry.bitmapsToRecycle.add(new Pair<>(imageLoader, bitmap));
            }
            entry.referencesCount++;
        }
    }

    /**
     * Removes reference from group of bitmaps and recycles all bitmaps of group if references count is 0.
     * Passed bitmap can be null
     */
    public void removeReference(@NonNull String signature) {
        // enable it only for pre-HONEYCOMB devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        synchronized (lock) {
            BitmapReferencesEntry entry = BitmapReferences.get(signature);
            if (entry == null) {
                Lc.fatalException(new IllegalStateException("Signature = '" + signature + "' not found in BitmapReferences for recycle"));
                return;
            }

            entry.referencesCount--;

            if (entry.referencesCount == 0) {
                for (Pair<ImageLoader, Bitmap> bitmapToRecycle : entry.bitmapsToRecycle) {
                    if (!bitmapToRecycle.second.isRecycled()) {
                        bitmapToRecycle.second.recycle();
                    }

                    MemoryCache memoryCache = bitmapToRecycle.first.getMemoryCache();
                    if (memoryCache != null && memoryCache.get(signature) == bitmapToRecycle.second) {
                        memoryCache.remove(signature);
                    }
                }

                BitmapReferences.remove(signature);
            }
        }
    }

    private class BitmapReferencesEntry {
        private int referencesCount;
        private ArrayList<Pair<ImageLoader, Bitmap>> bitmapsToRecycle = new ArrayList<>();

        private boolean contains(Bitmap bitmap) {
            for (Pair<ImageLoader, Bitmap> entry : bitmapsToRecycle) {
                if (entry.second == bitmap) {
                    return true;
                }
            }
            return false;
        }
    }
}
