package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.zuzuk.utils.Ln;
import org.zuzuk.utils.Utils;

import java.io.IOException;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents object setting
 */
public class ObjectSetting<T> extends Setting<T> {
    private final static JsonFactory DefaultJsonFactory = new JacksonFactory();

    private final Class<T> clazz;
    private final ValueChecker<T> valueChecker;

    public ObjectSetting(Class<T> clazz, String name) {
        this(clazz, name, null);
    }

    public ObjectSetting(Class<T> clazz, String name, ValueChecker<T> valueChecker) {
        super(name);
        this.clazz = clazz;
        this.valueChecker = valueChecker;
    }

    private String getJsonString(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.contains(getName())
                ? preferences.getString(getName(), "")
                : null;
    }

    /* Returns value of setting */
    public T get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            String jsonString = getJsonString(context);
            T value;
            try {
                value = jsonString != null
                        ? DefaultJsonFactory.createJsonParser(jsonString).parseAndClose(clazz)
                        : null;
            } catch (IOException e) {
                Ln.e("Setting " + getName() + " have invalid JSON: " + jsonString);
                value = null;
            }
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets value of setting */
    public boolean set(Context context, T value) {
        String currentJsonString = getJsonString(context);
        String valueJsonString;
        try {
            valueJsonString = value != null
                    ? DefaultJsonFactory.toPrettyString(value)
                    : null;
        } catch (IOException e) {
            Ln.e("Setting " + getName() + " can't parse to JSON: " + value.toString());
            return false;
        }

        if (Utils.objectsEquals(currentJsonString, valueJsonString)) {
            return true;
        }

        if (!valueChecker.isValid(value)) {
            Ln.e("Setting " + getName() + " tried to set with invalid value: "
                    + (valueJsonString != null ? valueJsonString : "null"));
            return false;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putString(getName(), valueJsonString).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
        return true;
    }
}
