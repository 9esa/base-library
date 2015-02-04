package org.zuzuk.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.zuzuk.utils.Lc;
import org.zuzuk.utils.serialization.FSTSerializer;
import org.zuzuk.utils.serialization.Serializer;

import java.util.Arrays;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Base class that represents setting. Settings are storing into database
 */
public class Setting<T> {
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
    private Serializer serializer = FSTSerializer.Instance;

    public Setting(String name) {
        this.name = name;
        this.defaultValueBytes = null;
        this.valueValidator = null;
    }

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValueBytes = getBytesOrNull(defaultValue, null);
        this.valueValidator = null;
    }

    public Setting(String name, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValueBytes = null;
        this.valueValidator = valueValidator;
    }

    public Setting(String name, T defaultValue, ValueValidator<T> valueValidator) {
        this.name = name;
        this.defaultValueBytes = getBytesOrNull(defaultValue, null);
        this.valueValidator = valueValidator;
    }

    /* Raises when value changes */
    public void raiseOnSettingChanged(Context context) {
        context.sendBroadcast(new Intent(getName() + "/" + RESET_BROADCAST_EVENT));
    }

    /* Raised after setting changed */
    protected void onSettingChanged(Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(getName()));

        if (Lc.getLogLevel() <= Log.DEBUG) {
            // valueToString() is hard operation
            Lc.d("Setting " + name + " changed to " + valueToString(get(context)));
        }
    }

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    private void updateCacheValueBytes(Context context) {
        SettingDatabaseModel settingModel = getSettingsDbTable(context).queryForId(name);
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
                    ? serializer.<T>deserialize(cachedValueBytes)
                    : null;
        }
    }

    /* Sets value of setting. Returns false if it is not changed */
    public boolean set(Context context, T value) {
        synchronized (valueLocker) {
            byte[] valueBytes = getBytesOrNull(value, EMPTY_VALUE);

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

            if (value == null) {
                getSettingsDbTable(context).deleteById(name);
                cachedValueBytes = defaultValueBytes != null ? defaultValueBytes : EMPTY_VALUE;
            } else {
                SettingDatabaseModel settingsModel = new SettingDatabaseModel(name, valueBytes);
                getSettingsDbTable(context).createOrUpdate(settingsModel);
                cachedValueBytes = valueBytes;
            }
            raiseOnSettingChanged(context);
            return true;
        }
    }

    private RuntimeExceptionDao<SettingDatabaseModel, Object> getSettingsDbTable(Context context) {
        return SettingsDatabaseHelper.getInstance(context).getDbTable(SettingDatabaseModel.class);
    }

    private byte[] getBytesOrNull(T value, byte[] nullValue) {
        return value != null ? serializer.serialize(value) : null;
    }

    private String valueToString(T value) {
        return value != null ? value.toString() : "null";
    }

    protected void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

}
