package org.zuzuk.tasks.aggregationtask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 03/02/2015.
 * Current status of data on some state
 */
public class AggregationTaskStageState {

    /* Create initial pre-loading stage state */
    public static AggregationTaskStageState createPreLoadingStageState() {
        return new AggregationTaskStageState(AggregationTaskStage.PRE_LOADING, null);
    }

    private final AggregationTaskStage taskStage;
    private Boolean isLoaded = null;
    private Boolean isLoadingNeeded = null;
    private final AggregationTaskStageState previousStageState;
    private final List<Exception> exceptions = new ArrayList<>(0);
    private final List<AggregationTaskStageListener> taskStageListeners = new ArrayList<>();
    Boolean isTaskWrapped = null;

    /* Returns stage */
    public AggregationTaskStage getTaskStage() {
        return taskStage;
    }

    /* Returns if after stage data is loaded and could be used */
    public Boolean isLoaded() {
        return isLoaded;
    }

    void setIsLoaded(Boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    /* Returns if after stage it still need additional loading (on other stages) */
    public Boolean isLoadingNeeded() {
        return isLoadingNeeded;
    }

    void setIsLoadingNeeded(Boolean isLoadingNeeded) {
        this.isLoadingNeeded = isLoadingNeeded;
    }

    /* Returns if there was exceptions during loading */
    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    /* Returns exceptions occurs on stage */
    public List<Exception> getExceptions() {
        return exceptions;
    }

    /* Returns previous stage */
    public AggregationTaskStageState getPreviousStageState() {
        return previousStageState;
    }

    AggregationTaskStageState(AggregationTaskStage taskStage,
                              AggregationTaskStageState previousStageState) {
        this.taskStage = taskStage;
        this.previousStageState = previousStageState;
    }

    /* Finds state of specific stage in previous stages */
    public AggregationTaskStageState findByStage(AggregationTaskStage stage) {
        if (taskStage == stage) {
            return this;
        }
        return previousStageState != null ? previousStageState.findByStage(stage) : null;
    }

    /* Some task have failed so we collect exception */
    void addFail(Exception ex) {
        exceptions.add(ex);
    }

    /* Adds stage listener */
    public void addListener(AggregationTaskStageListener taskStageListener){
        taskStageListeners.add(taskStageListener);
    }

    /* Removes stage listener */
    public void removeListener(AggregationTaskStageListener taskStageListener){
        taskStageListeners.remove(taskStageListener);
    }

    void notifyListenerAboutLoadingStart() {
        for (AggregationTaskStageListener taskStageListener : taskStageListeners) {
            taskStageListener.onLoadingStarted(this);
        }
    }

    void notifyListenerAboutLoadSuccess() {
        for (AggregationTaskStageListener taskStageListener : taskStageListeners) {
            taskStageListener.onLoaded(this);
        }
    }

    void notifyListenerAboutLoadFailure() {
        for (AggregationTaskStageListener taskStageListener : taskStageListeners) {
            taskStageListener.onFailed(this);
        }
    }

    public Boolean isTaskWrapped() {
        return isTaskWrapped;
    }

    public void setIsTaskWrapped(Boolean isTaskWrapped) {
        this.isTaskWrapped = isTaskWrapped;
    }

}
