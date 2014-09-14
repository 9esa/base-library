package org.zuzuk.dataproviding.requests.base;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base interface for long (prefer async) tasks like loading local data or remote request
 */
public interface Task<T> {

    /* Executes task */
    public T execute() throws Exception;
}
