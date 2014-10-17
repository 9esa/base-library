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
public abstract class ItemsProvider<TItem extends Serializable> implements Serializable {
    private List<WeakReference<DataSetChangedListener>> dataSetChangedListenersReferences = new ArrayList<>();

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
        dataSetChangedListenersReferences.add(new WeakReference<>(dataSetChangedListener));
    }

    /* Removes data set changing listener */
    public void removeOnDataSetChangedListener(DataSetChangedListener dataSetChangedListener) {
        for (int i = dataSetChangedListenersReferences.size(); i >= 0; i--) {
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
        for (int i = dataSetChangedListenersReferences.size(); i >= 0; i--) {
            DataSetChangedListener listener = dataSetChangedListenersReferences.get(i).get();
            if (listener != null) {
                listener.onDataSetChanged();
            } else {
                dataSetChangedListenersReferences.remove(i);
            }
        }
    }

    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
        dataSetChangedListenersReferences = new ArrayList<>();
    }
}
