package org.zuzuk.tasks.local;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Simple task that is using like runnable but executing asynchronously
 */
public abstract class LocalTask extends Task<Void> {

    public LocalTask() {
        super(Void.class);
    }
}
