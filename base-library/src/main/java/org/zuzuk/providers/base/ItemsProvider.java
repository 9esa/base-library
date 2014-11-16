package org.zuzuk.providers.base;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base provider that stores some items data inside
 */
public abstract class ItemsProvider<TItem extends Serializable> extends DataProvider {

    /* Returns total count of items */
    public abstract int getTotalCount();

    /* Returns item by position */
    public abstract TItem getItem(int position);

    /* Returns is provider empty */
    public boolean isEmpty() {
        return getTotalCount() == 0;
    }
}
