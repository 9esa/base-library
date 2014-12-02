package org.zuzuk.settings;

import android.content.Context;
import android.content.Intent;

import org.zuzuk.database.BaseOrmLiteHelper;
import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

import java.io.File;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Base class that represents setting. Settings are storing into database
 */
public abstract class Setting<T> {
    private final String name;
    private final byte[] defaultValueBytes;
    private final ValueValidator<T> valueValidator;
    private byte[] cachedValueBytes;
    protected final Object valueLocker = new Object();

    /* Raises when value changes */
    public void raiseOnSettingChanged(Context context) {
        context.sendBroadcast(new Intent(getName()));
        Lc.d("Setting " + name + " changed to " + valueToString(get(context)));
    }

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    /* Returns value bytes of setting */
    protected byte[] getValueBytes(Context context) {
        SettingsDatabaseHelper database = SettingsDatabaseHelper.getInstance(context);
        SettingDatabaseModel settingModel = database.getDbTable(SettingDatabaseModel.class).queryForId(name);
        return settingModel != null ? settingModel.getData() : null;
    }

    /* Returns value of setting */
    public T get(Context context) {
        synchronized (valueLocker) {
            if (cachedValueBytes == null) {
                byte[] bytes = getValueBytes(context);
                cachedValueBytes = bytes != null ? bytes : new byte[0];
            }
            return cachedValueBytes.length > 0
                    ? fromBytes(cachedValueBytes)
                    : (defaultValueBytes != null ? fromBytes(defaultValueBytes) : null);
        }
    }

    /* Sets value of setting. Returns false if it is not changed */
    public boolean set(Context context, T value) {
        synchronized (valueLocker) {
            if (Utils.objectsEquals(value, get(context))) {
                return true;
            }

            if (valueValidator != null && !valueValidator.isValid(value)) {
                Lc.e("Setting " + getName() + " tried to set with invalid value: "
                        + (value != null ? value : "null"));
                return false;
            }

            SettingsDatabaseHelper database = SettingsDatabaseHelper.getInstance(context);
            if (value == null) {
                database.getDbTable(SettingDatabaseModel.class).deleteById(name);
            } else {
                SettingDatabaseModel settingsModel = new SettingDatabaseModel(name, toBytes(value));
                database.getDbTable(SettingDatabaseModel.class).createOrUpdate(settingsModel);
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
        this.defaultValueBytes = defaultValue != null ? toBytes(defaultValue) : null;
        this.valueValidator = null;
    }

    public Setting(String name, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValueBytes = null;
        this.valueValidator = valueValidator;
    }

    public Setting(String name, T defaultValue, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValueBytes = defaultValue != null ? toBytes(defaultValue) : null;
        this.valueValidator = valueValidator;
    }

    private String valueToString(T value) {
        return value != null ? value.toString() : "null";
    }

    protected abstract T fromBytes(byte[] data);

    protected abstract byte[] toBytes(T value);

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
