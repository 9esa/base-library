package org.zuzuk.ui.views.imageloading;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import org.zuzuk.utils.Lc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Class that recycles bitmaps if there is no other objects using them
 */
public enum BitmapsGlobalRecycler {
    Instance;

    private final static HashMap<String, BitmapReferencesEntry> BitmapReferences = new HashMap<>();
    private final Object lock = new Object();

    /* Adds reference to groups of bitmap */
    public void addReference(@NonNull String signature, Bitmap bitmap) {
        synchronized (lock) {
            BitmapReferencesEntry entry = BitmapReferences.get(signature);
            if (entry == null) {
                entry = new BitmapReferencesEntry();
                BitmapReferences.put(signature, entry);
            }
            if (bitmap != null && !entry.bitmapsToRecycle.contains(bitmap)) {
                entry.bitmapsToRecycle.add(bitmap);
            }
            entry.referencesCount++;
        }
    }

    /**
     * Removes reference from group of bitmaps and recycles all bitmaps of group if references count is 0.
     * Passed bitmap can be null
     */
    public void removeReference(@NonNull String signature) {
        synchronized (lock) {
            BitmapReferencesEntry entry = BitmapReferences.get(signature);
            if (entry == null) {
                Lc.fatalException(new IllegalStateException("Signature = '" + signature + "' not found in BitmapReferences for recycle"));
                return;
            }

            entry.referencesCount--;

            if (entry.referencesCount == 0) {
                for (Bitmap bitmapToRecycle : entry.bitmapsToRecycle) {
                    if (!bitmapToRecycle.isRecycled()) {
                        bitmapToRecycle.recycle();
                    }
                }
                BitmapReferences.remove(signature);
            }
        }
    }

    private class BitmapReferencesEntry {
        private int referencesCount;
        private ArrayList<Bitmap> bitmapsToRecycle = new ArrayList<>();
    }
}
