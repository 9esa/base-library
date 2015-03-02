package org.zuzuk.providers;

import org.zuzuk.providers.base.DataProvider;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Provider that stores an object
 */
public class ObjectProvider<TObject> extends DataProvider {
    private TObject object;

    /* Returns stored data object */
    public TObject get() {
        return object;
    }

    /* Sets data object */
    public void set(TObject object) {
        this.object = object;
        onDataSetChanged();
    }

    @Override
    protected void resetInternal() {
        object = null;
    }
}
