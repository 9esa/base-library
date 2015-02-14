package org.zuzuk.ui.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

import org.zuzuk.events.EventListener;
import org.zuzuk.events.EventListenerHelper;

public abstract class BaseService extends Service implements EventListener {

    private EventListenerHelper eventListenerHelper;

    private ServiceBinder serviceBinder;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();
        serviceBinder = new ServiceBinder(this);
        eventListenerHelper = new EventListenerHelper(this);
        eventListenerHelper.onCreate(this);
        eventListenerHelper.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        eventListenerHelper.onPause();
        eventListenerHelper.onDestroy();
        eventListenerHelper = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onEvent(Context context, @NonNull String eventName, Intent intent) {
    }

}
