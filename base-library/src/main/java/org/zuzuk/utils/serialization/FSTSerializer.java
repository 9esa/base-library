package org.zuzuk.utils.serialization;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public enum FSTSerializer implements Serializer {

    Instance;

    private static FSTConfiguration fstConfiguration = FSTConfiguration.createDefaultConfiguration().setForceSerializable(true);

    @Override
    public <TObject> byte[] serialize(TObject object) {
        try {
            FSTObjectOutput fstObjectOutput = new FSTObjectOutput(fstConfiguration);
            fstObjectOutput.writeObject(object);
            byte[] bytes = fstObjectOutput.getCopyOfWrittenBuffer();
            fstObjectOutput.close();
            return bytes;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TObject> TObject deserialize(byte[] byteArray) {
        try {
            FSTObjectInput fstObjectInput = new FSTObjectInput(fstConfiguration);
            fstObjectInput.resetForReuseUseArray(byteArray);
            TObject object = (TObject) fstObjectInput.readObject();
            fstObjectInput.close();
            return object;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
