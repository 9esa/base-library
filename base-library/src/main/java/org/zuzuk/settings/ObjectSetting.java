package org.zuzuk.settings;

import org.zuzuk.utils.Lc;

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

    @Override
    protected byte[] toBytes(T value) throws IOException {
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
}
