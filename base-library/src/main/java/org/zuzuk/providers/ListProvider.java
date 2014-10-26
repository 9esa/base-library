package org.zuzuk.providers;

import org.zuzuk.providers.base.ItemsProvider;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on simple list
 */
public class ListProvider<TItem extends Serializable> extends ItemsProvider<TItem> {
    private List<TItem> items;

    @Override
    public int getTotalCount() {
        return isInitialized() ? items.size() : 0;
    }

    /* Returns if provider is initialized */
    public boolean isInitialized() {
        return items != null;
    }

    @Override
    public TItem getItem(int position) {
        return items.get(position);
    }

    /* Sets items as source and initializes provider if it wasn't */
    public void setItems(List<TItem> items) {
        this.items = items;
        onDataSetChanged();
    }

    /* Resets provider and made it not initialized */
    public void reset(){
        items = null;
        onDataSetChanged();
    }
}
