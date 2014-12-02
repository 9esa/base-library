package org.zuzuk.settings;

import java.nio.ByteBuffer;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents integer setting
 */
public class IntSetting extends Setting<Integer> {

    public IntSetting(String name) {
        super(name);
    }

    public IntSetting(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    public IntSetting(String name, ValueValidator<Integer> valueValidator) {
        super(name, valueValidator);
    }

    public IntSetting(String name, Integer defaultValue, ValueValidator<Integer> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected Integer fromBytes(byte[] data) {
        return data != null ? ByteBuffer.wrap(data).getInt() : null;
    }

    @Override
    protected byte[] toBytes(Integer value) {
        return value != null
                ? ByteBuffer.allocate(Integer.SIZE / 8).putInt(value).array()
                : null;
    }
}
