package org.zuzuk.dataproviding.requests.base;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface to listen async request result
 */
public interface ResultListener<T> {

    /* Raises when task is successfully completed */
    public void onSuccess(T response);

    /* Raises when task is failed on execution */
    public void onFailure(Exception ex);
}