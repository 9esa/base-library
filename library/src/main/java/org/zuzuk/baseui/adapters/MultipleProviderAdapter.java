package org.zuzuk.baseui.adapters;

import java.util.ArrayList;
import java.util.List;

import org.zuzuk.dataproviding.providers.base.DataSetChangedListener;
import org.zuzuk.dataproviding.providers.base.ItemsProvider;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Adapter that includes multiple different providers inside
 */
public abstract class MultipleProviderAdapter extends BaseAdapter<Object> implements DataSetChangedListener {
    private List<ItemsProvider> providers = new ArrayList<>();
    private Integer totalCount = null;

    /* Returns list of included data providers */
    protected List<ItemsProvider> getProviders() {
        return providers;
    }

    @Override
    public int getViewTypeCount() {
        return providers.isEmpty() ? 1 : providers.size();
    }

    /* Adds data provider */
    public void addProvider(ItemsProvider provider) {
        providers.add(provider);
        provider.addOnDataSetChangedListener(this);
        onDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int offset = 0;
        int type = 0;
        for (ItemsProvider provider : providers) {
            if (position - offset < provider.getTotalCount()) {
                return type;
            }
            offset += provider.getTotalCount();
            type++;
        }
        throw new RuntimeException("Wrong position: " + position);
    }

    @Override
    public Object get(int position) {
        int offset = 0;
        for (ItemsProvider provider : providers) {
            if (position - offset < provider.getTotalCount()) {
                return provider.getItem(position - offset);
            }
            offset += provider.getTotalCount();
        }
        throw new RuntimeException("Wrong position: " + position);
    }

    @Override
    public int getCount() {
        if (providers.isEmpty()) {
            return 0;
        }

        if (totalCount == null) {
            totalCount = 0;
            for (ItemsProvider provider : providers) {
                totalCount += provider.getTotalCount();
            }
        }
        return totalCount;
    }

    @Override
    public void onDataSetChanged() {
        totalCount = null;
        notifyDataSetChanged();
    }
}
