package org.zuzuk.tasks.remote.mycache;

import android.content.Context;

import org.zuzuk.database.BaseOrmLiteHelper;

import java.io.File;

public class MyCacheDbHelper extends BaseOrmLiteHelper {
    private final static String SETTINGS_DATABASE_NAME = "cache";
    private final static int DEFAULT_SETTINGS_VERSION = 1;

    private static MyCacheDbHelper instance;

    public synchronized static MyCacheDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MyCacheDbHelper(context);
        }
        return instance;
    }

    public MyCacheDbHelper(Context context) {
        super(context, context.getFilesDir() + File.separator + SETTINGS_DATABASE_NAME, null, DEFAULT_SETTINGS_VERSION);
    }

    @Override
    protected Class[] getTables() {
        return new Class[]{MyCacheDbEntry.class};
    }

}