package org.zuzuk.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.zuzuk.database.BaseOrmLiteHelper;

import java.io.File;

public class SettingsDatabaseHelper extends BaseOrmLiteHelper {

    private final static String SETTINGS_DATABASE_NAME = "inner_settings";

    private final static int DEFAULT_SETTINGS_VERSION = 1;

    private final static String SETTINGS_VERSION_MANIFEST_KEY = "org.zuzuk.settings.version";

    private static SettingsDatabaseHelper instance;

    public synchronized static SettingsDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsDatabaseHelper(context);
        }
        return instance;
    }

    private SettingsDatabaseHelper(Context context) {
        super(context, context.getFilesDir() + File.separator + SETTINGS_DATABASE_NAME, null, getSettingsVersion(context));
    }

    private static int getSettingsVersion(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            return metaData.getInt(SETTINGS_VERSION_MANIFEST_KEY, DEFAULT_SETTINGS_VERSION);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class[] getTables() {
        return new Class[]{ SettingDatabaseModel.class };
    }

}
