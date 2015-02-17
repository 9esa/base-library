package org.zuzuk.utils.serialization;

import com.google.api.client.util.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;

public class ObjectFromJson implements Externalizable {

    private transient boolean isNull = false;

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        try {
            isNull = input.readBoolean();
            for (Field field : ClassFields.Instance.getFields(this)) {
                JsonObjectState fieldJsonObjectState = (JsonObjectState) input.readObject();
                switch (fieldJsonObjectState) {
                    case NULL:
                        field.set(this, null);
                        break;
                    case DATA_NULL:
                        field.set(this, Data.nullOf(field.getType()));
                        break;
                    case OBJECT:
                        int length = input.readInt();
                        byte[] buffer = new byte[length];
                        input.readFully(buffer);
                        field.set(this, FSTSerializer.Instance.deserialize(buffer));
                }
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        try {
            output.writeBoolean(Data.isNull(this));
            for (Field field : ClassFields.Instance.getFields(this)) {
                Object value = field.get(this);
                if (value == null) {
                    output.writeObject(JsonObjectState.NULL);
                } else if (Data.isNull(value)) {
                    output.writeObject(JsonObjectState.DATA_NULL);
                } else {
                    output.writeObject(JsonObjectState.OBJECT);
                    byte[] serialized = FSTSerializer.Instance.serialize(value);
                    output.writeInt(serialized.length);
                    output.write(serialized);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isDataNull(Object object) {
        // may be wrong for arrays
        return object != null &&
                (Data.isNull(object) ||
                        (object instanceof ObjectFromJson && ((ObjectFromJson) object).isNull) ||
                        (object.getClass().isEnum() && object == Data.nullOf(object.getClass())));
    }

    public static boolean isNull(Object object) {
        return object == null || isDataNull(object);
    }

}
