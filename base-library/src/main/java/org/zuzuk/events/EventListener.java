package org.zuzuk.events;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Gavriil Sitnikov on 18/11/2014.
 * Basic event listener
 */
public interface EventListener {

    void onEvent(Context context, String eventName, Intent intent);
}
