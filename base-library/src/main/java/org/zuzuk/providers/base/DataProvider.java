package org.zuzuk.providers.base;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Basic data provider
 */
public abstract class DataProvider implements Serializable {
    private List<WeakReference<DataSetChangedListener>> dataSetChangedListenersReferences = new ArrayList<>();

    /* Returns if provider is initialized */
    public abstract boolean isInitialized();

    /* Adds data set changing listener */
    public void addOnDataSetChangedListener(DataSetChangedListener dataSetChangedListener) {
        dataSetChangedListenersReferences.add(new WeakReference<>(dataSetChangedListener));
    }

    /* Removes data set changing listener */
    public void removeOnDataSetChangedListener(DataSetChangedListener dataSetChangedListener) {
        for (int i = dataSetChangedListenersReferences.size() - 1; i >= 0; i--) {
            DataSetChangedListener listener = dataSetChangedListenersReferences.get(i).get();
            if (listener != null && listener == dataSetChangedListener) {
                dataSetChangedListenersReferences.remove(i);
                return;
            } else {
                dataSetChangedListenersReferences.remove(i);
            }
        }
    }

    /* Fires data set changing events in all listeners */
    public void onDataSetChanged() {
        for (int i = dataSetChangedListenersReferences.size() - 1; i >= 0; i--) {
            DataSetChangedListener listener = dataSetChangedListenersReferences.get(i).get();
            if (listener != null) {
                listener.onDataSetChanged();
            } else {
                dataSetChangedListenersReferences.remove(i);
            }
        }
    }

    /* Resets provider and made it not initialized */
    public void reset() {
        resetInternal();
        onDataSetChanged();
    }

    /* Resets provider and made it not initialized */
    protected abstract void resetInternal();

    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
        dataSetChangedListenersReferences = new ArrayList<>();
    }
}
