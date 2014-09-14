package org.zuzuk.providers;

import org.zuzuk.providers.base.ItemsProvider;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on simple list
 */
public class ListProvider<TItem extends Serializable> extends ItemsProvider<TItem> {
    private final List<TItem> items;

    @Override
    public int getTotalCount() {
        return items.size();
    }

    @Override
    public TItem getItem(int position) {
        return items.get(position);
    }

    public ListProvider(List<TItem> items) {
        this.items = items;
    }
}
