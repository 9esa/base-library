package org.zuzuk.tasks.aggregationtask;

public interface AggregationTaskExecutor<TRequestAndTaskExecutor extends RequestAndTaskExecutor> {

    /* Executes aggregation task */
    public void executeAggregationTask(AggregationTask<TRequestAndTaskExecutor> aggregationTask);
}
