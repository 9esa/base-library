package org.zuzuk.dataproviding.providers.base;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base provider that stores some items data inside
 */
public abstract class ItemsProvider<TItem> {
    private final List<DataSetChangedListener> dataSetChangedListeners = new ArrayList<>();

    /* Returns total count of items */
    public abstract int getTotalCount();

    /* Returns item by position */
    public abstract TItem getItem(int position);

    /* Returns is provider empty */
    public boolean isEmpty() {
        return getTotalCount() == 0;
    }

    /* Adds data set changing listener */
    public void addOnDataSetChangedListener(DataSetChangedListener dataSetChangedListener) {
        if (dataSetChangedListeners.contains(dataSetChangedListener)) {
            throw new RuntimeException("DataSetChangedListener added");
        }
        dataSetChangedListeners.add(dataSetChangedListener);
    }

    /* Removes data set changing listener */
    public void removeOnDataSetChangedListener(DataSetChangedListener dataSetChangedListener) {
        if (!dataSetChangedListeners.contains(dataSetChangedListener)) {
            throw new RuntimeException("DataSetChangedListener not added");
        }
        dataSetChangedListeners.remove(dataSetChangedListener);
    }

    /* Fires data set changing events in all listeners */
    public void onDataSetChanged() {
        for (DataSetChangedListener dataSetChangedListener : dataSetChangedListeners) {
            dataSetChangedListener.onDataSetChanged();
        }
    }
}
