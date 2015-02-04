package org.zuzuk.utils.serialization;

public interface Serializer {

    public <TObject> byte[] serialize(TObject object);

    public <TObject> TObject deserialize(byte[] byteArray);

}
