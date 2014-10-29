package org.zuzuk.ui.services;

import android.app.Service;
import android.os.Binder;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * Basic binding to service
 */
public class CommonServiceBinder<TService extends Service> extends Binder {
    private final TService service;

    public CommonServiceBinder(TService service) {
        this.service = service;
    }

    /* Returns service that binder holds */
    public TService getService() {
        return service;
    }
}