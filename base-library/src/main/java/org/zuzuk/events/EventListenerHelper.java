package org.zuzuk.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.zuzuk.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 18/11/2014.
 * Helper to listen events during lifecycle of object
 */
public class EventListenerHelper {
    private final EventListener eventListener;
    private Context context;
    private final List<String> globalEvents = new ArrayList<>();
    private final List<String> localEvents = new ArrayList<>();
    private final List<String> globalOnResumeEvents = new ArrayList<>();
    private final List<String> localOnResumeEvents = new ArrayList<>();
    private boolean isOnCreateReceiversRegistered = false;
    private final BroadcastReceiver globalEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            eventListener.onEvent(context, intent.getAction(), intent);
        }
    };
    private final BroadcastReceiver localEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            eventListener.onEvent(context, intent.getAction(), intent);
        }
    };
    private final BroadcastReceiver globalOnResumeEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            eventListener.onEvent(context, intent.getAction(), intent);
        }
    };
    private final BroadcastReceiver localOnResumeEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            eventListener.onEvent(context, intent.getAction(), intent);
        }
    };

    public EventListenerHelper(EventListener eventListener) {
        this.eventListener = eventListener;
        fillListeningBroadcastEventsFromClass(eventListener.getClass());
    }

    public void onCreate(Context context) {
        this.context = context;
        LocalBroadcastManager.getInstance(context).registerReceiver(localEventReceiver, UIUtils.createIntentFilter(localEvents));
        context.registerReceiver(globalEventReceiver, UIUtils.createIntentFilter(globalEvents));
        isOnCreateReceiversRegistered = true;
    }

    public void onResume() {
        LocalBroadcastManager.getInstance(context).registerReceiver(localOnResumeEventReceiver, UIUtils.createIntentFilter(localOnResumeEvents));
        context.registerReceiver(globalOnResumeEventReceiver, UIUtils.createIntentFilter(globalOnResumeEvents));
    }

    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(localOnResumeEventReceiver);
        context.unregisterReceiver(globalOnResumeEventReceiver);
    }

    public void onDestroy() {
        if (isOnCreateReceiversRegistered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(localEventReceiver);
            context.unregisterReceiver(globalEventReceiver);
            isOnCreateReceiversRegistered = false;
        }
        context = null;
    }

    private void fillListeningBroadcastEventsFromClass(Class clazz) {
        BroadcastEvents events = (BroadcastEvents) clazz.getAnnotation(BroadcastEvents.class);
        if (events == null) {
            return;
        }

        for (EventAnnotation eventAnnotation : events.value()) {
            String eventName = eventAnnotation.value();
            if (!eventAnnotation.isGlobalBroadcast()) {
                if (eventAnnotation.isOnlyWhileResumed()) {
                    if (!localOnResumeEvents.contains(eventName)) {
                        localOnResumeEvents.add(eventName);
                    }
                } else {
                    if (!localEvents.contains(eventName)) {
                        localEvents.add(eventName);
                    }
                }
            } else {
                if (eventAnnotation.isOnlyWhileResumed()) {
                    if (!globalOnResumeEvents.contains(eventName)) {
                        globalOnResumeEvents.add(eventName);
                    }
                } else {
                    if (!globalEvents.contains(eventName)) {
                        globalEvents.add(eventName);
                    }
                }
            }
        }

        fillListeningBroadcastEventsFromClass(clazz.getSuperclass());
    }
}
