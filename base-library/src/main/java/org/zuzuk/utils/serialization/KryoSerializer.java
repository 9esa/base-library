package org.zuzuk.utils.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;

public enum KryoSerializer implements Serializer {

    Instance;

    private static ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        public Kryo initialValue() {
            return new Kryo();
        }
    };

    @Override
    public <TObject> byte[] serialize(TObject object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryoThreadLocal.get().writeClassAndObject(output, object);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <TObject> TObject deserialize(byte[] byteArray) {
        TObject tObject = (TObject) kryoThreadLocal.get().readClassAndObject(new Input(byteArray));
        return tObject;
    }

}
