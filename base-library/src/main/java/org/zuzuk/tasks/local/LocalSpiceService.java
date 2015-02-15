package org.zuzuk.tasks.local;

import android.content.Context;

import com.octo.android.robospice.networkstate.NetworkStateChecker;

import org.zuzuk.tasks.remote.cache.ORMLiteDatabaseCacheService;

/**
 * Created by Gavriil Sitnikov on 09/14.
 * RoboSpice service for executing local tasks
 */
public class LocalSpiceService extends ORMLiteDatabaseCacheService {

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {
            @Override
            public boolean isNetworkAvailable(Context context) {
                return true;
            }

            @Override
            public void checkPermissions(Context context) {

            }
        };
    }

}
