package org.zuzuk.providers.base;

import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.utils.Lc;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that needs initialization before it is available to provide items
 */
public abstract class LoadingItemsProvider<TItem> extends ItemsProvider<TItem> {

    private boolean isInitialized = false;
    private boolean isInitializing = false;

    /* Returns available loaded count of items */
    public abstract int getAvailableCount();

    /* Returns available loaded item by position (position related to AvailableCount) */
    public abstract TItem getAvailableItem(int position);

    /* Starts provider initialization at specific position */
    public void initialize(int initializationPosition, RequestAndTaskExecutor executor) {
        if (executor == null) {
            Lc.fatalException(new Throwable("Initialization is allowed only inside load()"));
            return;
        }
        initializeInternal(initializationPosition, executor);
    }

    /* Internal provider initialization logic */
    protected abstract void initializeInternal(int initializationPosition, RequestAndTaskExecutor executor);

}
