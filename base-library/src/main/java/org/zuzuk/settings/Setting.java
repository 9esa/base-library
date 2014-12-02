package org.zuzuk.settings;

import android.content.Context;
import android.content.Intent;

import org.zuzuk.database.BaseOrmLiteHelper;
import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Base class that represents setting. Settings are storing into database
 */
public abstract class Setting<T> {
    private final String name;
    private final T defaultValue;
    private final ValueValidator<T> valueValidator;
    private CachedValue cachedValue;
    protected final Object valueLocker = new Object();

    /* Raises when value changes */
    public void raiseOnSettingChanged(Context context) {
        context.sendBroadcast(new Intent(getName()));
        Lc.d("Setting " + name + " changed to " + valueToString(getCachedValue().get()));
    }

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    /* Returns cached value of setting */
    private CachedValue getCachedValue() {
        return cachedValue;
    }

    /* Sets string value of setting */
    protected void setCachedValue(CachedValue cachedValue) {
        this.cachedValue = cachedValue;
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
            CachedValue cachedValue = getCachedValue();
            if (cachedValue == null) {
                byte[] valueBytes = getValueBytes(context);
                T value = valueBytes != null ? fromBytes(valueBytes) : null;

                if (value == null && defaultValue != null) {
                    value = defaultValue;
                }

                cachedValue = new CachedValue(value);
                setCachedValue(cachedValue);
            }
            return cachedValue.get();
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
                if (defaultValue != null) {
                    SettingDatabaseModel settingsModel = new SettingDatabaseModel(name, toBytes(defaultValue));
                    database.getDbTable(SettingDatabaseModel.class).update(settingsModel);
                    value = defaultValue;
                } else {
                    database.getDbTable(SettingDatabaseModel.class).deleteById(name);
                }
            } else {
                SettingDatabaseModel settingsModel = new SettingDatabaseModel(name, toBytes(defaultValue));
                database.getDbTable(SettingDatabaseModel.class).update(settingsModel);
            }
            setCachedValue(new CachedValue(value));
            raiseOnSettingChanged(context);
            return true;
        }
    }

    public Setting(String name) {
        this.name = name;
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public Setting(String name, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public Setting(String name, T defaultValue, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    private String valueToString(T value) {
        return value != null ? value.toString() : "null";
    }

    protected abstract T fromBytes(byte[] data);

    protected abstract byte[] toBytes(T value);

    /* helper class to store cached value */
    protected class CachedValue {
        private final T cachedValue;

        public T get() {
            return cachedValue;
        }

        public CachedValue(T cachedValue) {
            this.cachedValue = cachedValue;
        }
    }

    private static class SettingsDatabaseHelper extends BaseOrmLiteHelper {
        private final static String SETTINGS_DATABASE_NAME = "INNER_SETTINGS";
        private final static int SETTINGS_VERSION = 1;

        private static SettingsDatabaseHelper instance;

        private synchronized static SettingsDatabaseHelper getInstance(Context context) {
            if (instance == null) {
                instance = new SettingsDatabaseHelper(context);
            }
            return instance;
        }

        public SettingsDatabaseHelper(Context context) {
            super(context, SETTINGS_DATABASE_NAME, null, SETTINGS_VERSION);
        }

        @Override
        protected Class[] getTables() {
            return new Class[]{SettingDatabaseModel.class};
        }
    }
}
