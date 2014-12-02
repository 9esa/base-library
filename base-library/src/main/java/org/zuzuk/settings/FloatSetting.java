package org.zuzuk.settings;

import java.nio.ByteBuffer;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents float setting
 */
public class FloatSetting extends Setting<Float> {

    public FloatSetting(String name) {
        super(name);
    }

    public FloatSetting(String name, Float defaultValue) {
        super(name, defaultValue);
    }

    public FloatSetting(String name, ValueValidator<Float> valueValidator) {
        super(name, valueValidator);
    }

    public FloatSetting(String name, Float defaultValue, ValueValidator<Float> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected Float fromBytes(byte[] data) {
        return data != null ? ByteBuffer.wrap(data).getFloat() : null;
    }

    @Override
    protected byte[] toBytes(Float value) {
        return value != null
                ? ByteBuffer.allocate(Float.SIZE / 8).putFloat(value).array()
                : null;
    }
}