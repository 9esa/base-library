package org.zuzuk.utils.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public enum KryoSerializer implements Serializer {

    Instance;

    private static ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        public Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        }
    };

    @Override
    public <TObject> byte[] serialize(TObject object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4000);
        kryoThreadLocal.get().writeClassAndObject(new Output(byteArrayOutputStream), object);
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

    @Override
    public <TObject> TObject deserialize(byte[] byteArray) {
        TObject tObject = (TObject) kryoThreadLocal.get().readClassAndObject(new Input(byteArray));
        return tObject;
    }

}
