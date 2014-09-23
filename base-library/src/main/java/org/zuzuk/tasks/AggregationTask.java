package org.zuzuk.tasks;

/**
 * Created by Gavriil Sitnikov on 18/09/2014.
 * An object that can detect executing several Task objects and observe them as one long task.
 * All Task objects should be created in one thread (prefer UI)
 */
public abstract interface AggregationTask {

    /* Returns is task needs something to load or not */
    boolean isLoadingNeeded();

    /* Returns is task loaded all needed info */
    boolean isLoaded();

    /* Reloading data */
    void load(boolean isInBackground);

    /* Calls when loading have started */
    void onLoadingStarted(boolean isInBackground);

    /* Calls when loading have started */
    void onLoaded();

    /* Calls when loading failed with exception */
    void onFailed(Exception ex);
}
