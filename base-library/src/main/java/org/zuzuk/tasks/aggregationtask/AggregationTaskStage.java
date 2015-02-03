package org.zuzuk.tasks.aggregationtask;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * State of AggregationTask
 */
public enum AggregationTaskStage {
    // task started
    PRE_LOADING,
    // task locally loading data from cache (even if cache is dirty)
    LOADING_LOCALLY,
    // task remotely (or even locally) loading data from source
    LOADING_REMOTELY,
    // task loaded
    LOADED
}
