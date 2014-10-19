package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Base64;
import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents object setting
 */
public class ObjectSetting<T extends Serializable> extends Setting<T> {
    private final Class<T> clazz;
    private final T defaultValue;
    private final ValueValidator<T> valueValidator;

    public ObjectSetting(Class<T> clazz, String name) {
        super(name);
        this.clazz = clazz;
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public ObjectSetting(Class<T> clazz, String name, T defaultValue) {
        super(name);
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public ObjectSetting(Class<T> clazz, String name, ValueValidator<T> valueValidator) {
        super(name);
        this.clazz = clazz;
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public ObjectSetting(Class<T> clazz, String name, T defaultValue, ValueValidator<T> valueValidator) {
        super(name);
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    private String getDataString(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.contains(getName())
                ? preferences.getString(getName(), "")
                : null;
    }

    @SuppressWarnings("unchecked")
    private T deserializeObject(String dataString) {
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(dataString.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream is = new ObjectInputStream(in);
            return (T) is.readObject();
        } catch (Exception e) {
            Lc.e("Setting " + getName() + " cannot be deserialized from: " + dataString + '\n' + e.getMessage());
            return null;
        }
    }

    private String serializeObject(T value) throws IOException {
        if (value != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(value);
            return new String(Base64.encode(out.toByteArray(), Base64.DEFAULT));
        } else {
            return null;
        }
    }

    /* Returns value of setting */
    public T get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            String dataString = getDataString(context);
            T value = dataString != null ? deserializeObject(dataString) : null;

            if (value == null && defaultValue != null) {
                value = defaultValue;
            }
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets value of setting */
    public boolean set(Context context, T value) {
        String currentDataString = getDataString(context);
        String valueDataString;
        try {
            valueDataString = serializeObject(value);
        } catch (IOException e) {
            Lc.e("Setting " + getName() + " cannot be serialized: " + value.toString() + '\n' + e.getMessage());
            return false;
        }

        if (Utils.objectsEquals(currentDataString, valueDataString)) {
            return true;
        }

        if (valueValidator != null && !valueValidator.isValid(value)) {
            Lc.e("Setting " + getName() + " tried to set with invalid value: "
                    + (valueDataString != null ? valueDataString : "null"));
            return false;
        }

        if (value == null) {
            if (defaultValue != null) {
                try {
                    getPreferences(context).edit().putString(getName(), serializeObject(defaultValue)).commit();
                } catch (Exception e) {
                    Lc.e("Setting " + getName() + " cannot be serialized: " + defaultValue.toString() + '\n' + e.getMessage());
                }
            } else {
                getPreferences(context).edit().remove(getName()).commit();
            }
        } else {
            getPreferences(context).edit().putString(getName(), valueDataString).commit();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
        return true;
    }
}
