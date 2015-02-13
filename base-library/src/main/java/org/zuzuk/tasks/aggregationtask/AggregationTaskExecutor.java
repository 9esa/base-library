package org.zuzuk.tasks.aggregationtask;

public interface AggregationTaskExecutor<TRequestAndTaskExecutor extends RequestAndTaskExecutor<TRequestAndTaskExecutor>> {

    /* Executes aggregation task */
    public void executeAggregationTask(AggregationTask<TRequestAndTaskExecutor> aggregationTask);

}
