package org.zuzuk.ui.adapters;

import org.zuzuk.providers.base.DataSetChangedListener;
import org.zuzuk.providers.base.ItemsProvider;

import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Adapter that based on provider so all logic should be implemented inside provider
 */
public abstract class ProviderAdapter<TItem extends Serializable, TProvider extends ItemsProvider<TItem>>
        extends BaseAdapter<TItem>
        implements DataSetChangedListener {
    private TProvider provider;

    /* Returns current data provider */
    public TProvider getProvider() {
        return provider;
    }

    public void setProvider(TProvider provider) {
        if (this.provider != null) {
            this.provider.removeOnDataSetChangedListener(this);
        }
        this.provider = provider;
        if (provider != null) {
            provider.addOnDataSetChangedListener(this);
        }
        onDataSetChanged();
    }

    @Override
    public TItem get(int position) {
        return provider.getItem(position);
    }

    @Override
    public int getCount() {
        return provider == null ? 0 : provider.getTotalCount();
    }

    @Override
    public void onDataSetChanged() {
        notifyDataSetChanged();
    }

    /* Disposes adapter to remove all links from data to it and avoid memory leaks */
    public void dispose() {
        if (provider != null) {
            provider.removeOnDataSetChangedListener(this);
        }
        provider = null;
    }
}
