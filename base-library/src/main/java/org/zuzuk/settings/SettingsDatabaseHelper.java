package org.zuzuk.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.j256.ormlite.support.ConnectionSource;

import org.zuzuk.database.BaseOrmLiteHelper;

import java.io.File;

public class SettingsDatabaseHelper extends BaseOrmLiteHelper {
    private final static String SETTINGS_DATABASE_NAME = "inner_settings";
    private final static int DEFAULT_SETTINGS_VERSION = 1;
    private final static String SETTINGS_VERSION_MANIFEST_KEY = "org.zuzuk.settings.version";

    private static SettingsDatabaseHelper instance;
    private static MigrateProcessor migrateProcessor;

    public synchronized static SettingsDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsDatabaseHelper(context);
        }
        return instance;
    }

    public static void setMigrateProcessor(MigrateProcessor migrateProcessor) {
        SettingsDatabaseHelper.migrateProcessor = migrateProcessor;
    }

    @Override
    protected Class[] getTables() {
        return new Class[]{SettingDatabaseModel.class};
    }

    private SettingsDatabaseHelper(Context context) {
        super(context, context.getFilesDir() + File.separator + SETTINGS_DATABASE_NAME, null, getSettingsVersion(context));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (migrateProcessor != null) {
            migrateProcessor.migrate(this, oldVersion, newVersion);
        }
        super.onUpgrade(sqLiteDatabase, connectionSource, oldVersion, newVersion);
    }

    private static int getSettingsVersion(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            return metaData != null
                    ? metaData.getInt(SETTINGS_VERSION_MANIFEST_KEY, DEFAULT_SETTINGS_VERSION)
                    : DEFAULT_SETTINGS_VERSION;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public byte[] getSettingRawData(String settingName) {
        SettingDatabaseModel setting = getDbTable(SettingDatabaseModel.class).queryForId(settingName);
        return setting == null ? null : setting.getData();
    }

    public interface MigrateProcessor {

        void migrate(SettingsDatabaseHelper settingsDatabase, int oldVersion, int newVersion);
    }
}
