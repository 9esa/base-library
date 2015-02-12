package org.zuzuk.ui.adapters;

import android.support.v7.widget.RecyclerView;

import org.zuzuk.providers.base.DataSetChangedListener;
import org.zuzuk.providers.base.ItemsProvider;

import java.io.Serializable;

/**
 * Created by Vladimir Kozhevnikov on 12/23/14.
 * Adapter based on provider. All logic should be implemented inside the provider.
 */
public abstract class ProviderRecyclerAdapter<TItem extends Serializable, TProvider extends ItemsProvider<TItem>, TViewHolder extends RecyclerView.ViewHolder>
        extends ItemsRecyclerAdapter<TItem, TViewHolder>
        implements DataSetChangedListener {
    private TProvider provider;

    /* Returns current data provider */
    public TProvider getProvider() {
        return provider;
    }

    /* Sets current data provider */
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
    public TItem getItem(int position) {
        return provider.getItem(position);
    }

    @Override
    public int getItemCount() {
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
