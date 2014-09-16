package org.zuzuk.providers.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that needs initialization before it is available to provide items
 */
public abstract class LoadingItemsProvider<TItem extends Serializable> extends ItemsProvider<TItem> implements InitializationListener {
    private InitializationListener initializationListener;
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
    public void initialize(InitializationListener initializationListener) {
        initialize(initializationListener, 0);
    }

    /* Starts provider initialization at specific position */
    public synchronized void initialize(InitializationListener initializationListener, int initializationPosition) {
        this.initializationListener = initializationListener;

        if (isInitialized) {
            initializationListener.onInitialized();
        } else if (!isInitializing) {
            isInitializing = true;
            initialize(initializationPosition);
        }
    }

    /* Internal provider initialization logic */
    protected abstract void initialize(int initializationPosition);

    /* Raises when provider initialized. Use it in child classes */
    @Override
    public void onInitialized() {
        isInitialized = true;
        isInitializing = false;
        initializationListener.onInitialized();
        initializationListener = null;
    }

    /* Raises when provider initialization failed. Use it in child classes */
    @Override
    public void onInitializationFailed(Exception ex) {
        isInitializing = false;
        initializationListener.onInitializationFailed(ex);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(isInitialized);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        isInitialized = in.readBoolean();
    }
}
