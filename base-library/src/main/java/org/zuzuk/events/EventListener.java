package org.zuzuk.events;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 18/11/2014.
 * Basic event listener
 */
public interface EventListener {

    void onEvent(Context context,@NonNull String eventName, Intent intent);
}
