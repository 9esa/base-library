package org.zuzuk.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.zuzuk.database.BaseOrmLiteHelper;
import org.zuzuk.utils.Lc;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Base class that represents setting. Settings are storing into database
 */
public abstract class Setting<T> {
    private final static byte[] EMPTY_VALUE = new byte[0];
    private final static String RESET_BROADCAST_EVENT = "RESET_BROADCAST_EVENT";

    private final String name;
    private final byte[] defaultValueBytes;
    private final ValueValidator<T> valueValidator;
    private byte[] cachedValueBytes;

    protected final Object valueLocker = new Object();
    private boolean isResetReceiverRegistered = false;
    private final BroadcastReceiver resetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCacheValueBytes(context);
            onSettingChanged(context);
        }
    };

    /* Raises when value changes */
    public void raiseOnSettingChanged(Context context) {
        context.sendBroadcast(new Intent(getName() + "/" + RESET_BROADCAST_EVENT));
    }

    /* Raised after setting changed */
    protected void onSettingChanged(Context context) {
        context.sendBroadcast(new Intent(getName()));
        Lc.d("Setting " + name + " changed to " + valueToString(get(context)));
    }

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    private void updateCacheValueBytes(Context context) {
        SettingsDatabaseHelper database = SettingsDatabaseHelper.getInstance(context);
        SettingDatabaseModel settingModel = database.getDbTable(SettingDatabaseModel.class).queryForId(name);
        cachedValueBytes = settingModel != null
                ? settingModel.getData()
                : (defaultValueBytes != null ? defaultValueBytes : EMPTY_VALUE);

        if (!isResetReceiverRegistered) {
            context.getApplicationContext().registerReceiver(resetReceiver, new IntentFilter(getName() + "/" + RESET_BROADCAST_EVENT));
            isResetReceiverRegistered = true;
        }
    }

    /* Returns value of setting */
    public T get(Context context) {
        synchronized (valueLocker) {
            if (cachedValueBytes == null) {
                updateCacheValueBytes(context);
            }
            return cachedValueBytes != EMPTY_VALUE
                    ? fromBytes(cachedValueBytes)
                    : null;
        }
    }

    /* Sets value of setting. Returns false if it is not changed */
    public boolean set(Context context, T value) {
        synchronized (valueLocker) {
            byte[] valueBytes;
            try {
                valueBytes = value != null ? toBytes(value) : EMPTY_VALUE;
            } catch (Exception e) {
                Lc.e("Setting " + getName() + " cannot be serialized: " + value.toString() + '\n' + e.getMessage());
                return false;
            }
            if (cachedValueBytes == null) {
                updateCacheValueBytes(context);
            }

            if (Arrays.equals(cachedValueBytes, valueBytes)) {
                return false;
            }

            if (valueValidator != null && !valueValidator.isValid(value)) {
                Lc.e("Setting " + getName() + " tried to set with invalid value: "
                        + (value != null ? value : "null"));
                return false;
            }

            SettingsDatabaseHelper database = SettingsDatabaseHelper.getInstance(context);
            if (value == null) {
                database.getDbTable(SettingDatabaseModel.class).deleteById(name);
                cachedValueBytes = defaultValueBytes != null ? defaultValueBytes : EMPTY_VALUE;
            } else {
                SettingDatabaseModel settingsModel = new SettingDatabaseModel(name, valueBytes);
                database.getDbTable(SettingDatabaseModel.class).createOrUpdate(settingsModel);
                cachedValueBytes = valueBytes;
            }
            raiseOnSettingChanged(context);
            return true;
        }
    }

    public Setting(String name) {
        this.name = name;
        this.defaultValueBytes = null;
        this.valueValidator = null;
    }

    public Setting(String name, T defaultValue) {
        this.name = name;
        try {
            this.defaultValueBytes = defaultValue != null ? toBytes(defaultValue) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.valueValidator = null;
    }

    public Setting(String name, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValueBytes = null;
        this.valueValidator = valueValidator;
    }

    public Setting(String name, T defaultValue, ValueValidator<T> valueValidator) {
        this.name = name;
        try {
            this.defaultValueBytes = defaultValue != null ? toBytes(defaultValue) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.valueValidator = valueValidator;
    }

    private String valueToString(T value) {
        return value != null ? value.toString() : "null";
    }

    protected abstract T fromBytes(byte[] data);

    protected abstract byte[] toBytes(T value) throws Exception;

    private static class SettingsDatabaseHelper extends BaseOrmLiteHelper {
        private final static String SETTINGS_DATABASE_NAME = "inner_settings";
        private final static int SETTINGS_VERSION = 1;

        private static SettingsDatabaseHelper instance;

        private synchronized static SettingsDatabaseHelper getInstance(Context context) {
            if (instance == null) {
                instance = new SettingsDatabaseHelper(context);
            }
            return instance;
        }

        public SettingsDatabaseHelper(Context context) {
            super(context, context.getFilesDir() + File.separator + SETTINGS_DATABASE_NAME, null, SETTINGS_VERSION);
        }

        @Override
        protected Class[] getTables() {
            return new Class[]{SettingDatabaseModel.class};
        }
    }
}
