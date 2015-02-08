package org.zuzuk.providers.base;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that needs initialization before it is available to provide items
 */
public abstract class LoadingItemsProvider<TItem> extends ItemsProvider<TItem>
        implements InitializationListener {

    private boolean isInitialized = false;
    private boolean isInitializing = false;

    /* Returns available loaded count of items */
    public abstract int getAvailableCount();

    /* Returns available loaded item by position (position related to AvailableCount) */
    public abstract TItem getAvailableItem(int position);

    /* Returns is provider initialized */
    public boolean isInitialized() {
        return isInitialized;
    }

    /* Starts provider initialization */
    public void initialize(int initializationPosition) {
        initialize(initializationPosition, null);
    }

    /* Starts provider initialization at specific position */
    public void initialize(int initializationPosition, AggregationTaskStageState stageState) {
        if (isInitialized) {
            reset();
        }

        if (!isInitializing) {
            isInitializing = true;
            initializeInternal(initializationPosition, stageState);
        }
    }

    /* Internal provider initialization logic */
    protected abstract void initializeInternal(int initializationPosition, AggregationTaskStageState stageState);

    /* Raises when provider initialized. Use it in child classes */
    @Override
    public void onInitialized() {
        isInitialized = true;
        isInitializing = false;
        onDataSetChanged();
    }

    /* Raises when provider initialization failed. Use it in child classes */
    @Override
    public void onInitializationFailed(List<Exception> exceptions) {
        isInitializing = false;
    }
}
