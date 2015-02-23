package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Created by Gavriil Sitnikov on 12/02/2015.
 * No retrying policy
 */
public class OneShotRetryPolicy implements RetryPolicy {

    @Override
    public int getRetryCount() {
        return 0;
    }

    @Override
    public void retry(SpiceException e) {
    }

    @Override
    public long getDelayBeforeRetry() {
        return 0;
    }
}
