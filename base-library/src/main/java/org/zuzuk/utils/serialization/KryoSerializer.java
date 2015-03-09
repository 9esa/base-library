package org.zuzuk.utils.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Gavriil Sitnikov on 09/03/2015.
 * Kryo serialization logic
 */
public enum KryoSerializer implements Serializer {
    Instance;

    private final Kryo kryo = new Kryo() {{
        setAutoReset(true);
    }};

    @Override
    public synchronized <TObject> byte[] serialize(TObject tObject) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Output output = new Output(byteStream);
        kryo.writeClassAndObject(output, tObject);
        output.close();
        return byteStream.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <TObject> TObject deserialize(byte[] byteArray) throws Exception {
        Input input = new Input(new ByteArrayInputStream(byteArray));
        TObject result = (TObject) kryo.readClassAndObject(input);
        input.close();
        return result;
    }
}
