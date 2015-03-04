package org.zuzuk.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Vladimir Kozhevnikov on 12/23/14.
 * Contains base methods for the recycler view adapter.
 */
public abstract class ItemsRecyclerAdapter<TItem, TViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<TViewHolder> {
    private OnItemClickListener<TItem> onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener<TItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ItemsRecyclerAdapter() {
        setHasStableIds(true);
    }

    protected boolean isItemEnabled(TItem item, int position) {
        return true;
    }

    @Override
    public void onBindViewHolder(TViewHolder holder, final int position) {
        holder.itemView.setEnabled(isItemEnabled(getItem(position), position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(getItem(position), position);
                }
            }
        });
    }

    protected abstract TItem getItem(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnItemClickListener<TItem> {
        void onClick(TItem item, int position);
    }
}
