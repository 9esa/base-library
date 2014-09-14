package org.zuzuk.dataproviding.requests.base;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Class that reflects manually controlling of complex task.
 * So it looks weird but there is no simple option so developer should manually call fireOnSuccess or
 * fireOnFailure to notify that complex request (like if there is a chain of request
 * or some parallel requests) ends
 */
public class TaskResultController {
    private OnCompletionListener onCompletionListener;

    /* Sets listener that waits for task completion */
    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    /* Fires successfully complex task completion */
    public void fireOnSuccess() {
        onCompletionListener.onCompleted();
    }

    /* Fires complex task failure */
    public void fireOnFailure(Exception ex) {
        onCompletionListener.onCompleted();
    }

    public interface OnCompletionListener {

        /* Raises when task is completed with any result */
        public void onCompleted();
    }
}
