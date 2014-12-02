package org.zuzuk.settings;

import android.content.Context;

import org.zuzuk.utils.Lc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents object setting
 */
public class ObjectSetting<T extends Serializable> extends Setting<T> {

    public ObjectSetting(String name) {
        super(name);
    }

    public ObjectSetting(String name, T defaultValue) {
        super(name, defaultValue);
    }

    public ObjectSetting(String name, ValueValidator<T> valueValidator) {
        super(name, valueValidator);
    }

    public ObjectSetting(String name, T defaultValue, ValueValidator<T> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected T fromBytes(byte[] data) {
        return deserializeObject(data);
    }

    @Override
    protected byte[] toBytes(T value) {
        try {
            return serializeObject(value);
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private T deserializeObject(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            ObjectInputStream is = new ObjectInputStream(in);
            return (T) is.readObject();
        } catch (Exception e) {
            Lc.e("Setting " + getName() + " cannot be deserialized: " + '\n' + e.getMessage());
            return null;
        }
    }

    private byte[] serializeObject(T value) throws IOException {
        if (value != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(value);
            byte[] result = out.toByteArray();
            out.close();
            return result;
        } else {
            return null;
        }
    }

    @Override
    public boolean set(Context context, T value) {
        byte[] currentValueBytes = getValueBytes(context);
        byte[] valueBytes;
        try {
            valueBytes = serializeObject(value);
        } catch (IOException e) {
            Lc.e("Setting " + getName() + " cannot be serialized: " + value.toString() + '\n' + e.getMessage());
            return false;
        }

        return Arrays.equals(currentValueBytes, valueBytes) || super.set(context, value);
    }
}
