package org.zuzuk.tasks.remote.cache;

import android.content.Context;

import org.zuzuk.database.BaseOrmLiteHelper;

import java.io.File;

public class ORMLiteDatabaseCacheDbHelper extends BaseOrmLiteHelper {

    private final static String CACHE_DATABASE_NAME = "cache_db";

    private final static int CACHE_DATABASE_VERSION = 1;

    private static ORMLiteDatabaseCacheDbHelper instance;

    public synchronized static ORMLiteDatabaseCacheDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ORMLiteDatabaseCacheDbHelper(context);
        }
        return instance;
    }

    public ORMLiteDatabaseCacheDbHelper(Context context) {
        super(context, context.getFilesDir() + File.separator + CACHE_DATABASE_NAME, null, CACHE_DATABASE_VERSION);
    }

    @Override
    protected Class[] getTables() {
        return new Class[]{ ORMLiteDatabaseCacheDbEntry.class };
    }

}