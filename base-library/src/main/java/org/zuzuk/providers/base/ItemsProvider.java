package org.zuzuk.providers.base;


/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base provider that stores some items data inside
 */
public abstract class ItemsProvider<TItem> extends DataProvider {

    /* Returns total count of items */
    public abstract int getTotalCount();

    /* Returns item by position */
    public abstract TItem getItem(int position);

    /* Returns is provider empty */
    public boolean isEmpty() {
        return getTotalCount() == 0;
    }
}
