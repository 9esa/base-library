package org.zuzuk.utils.serialization;

import org.nustaq.serialization.FSTConfiguration;

public enum FSTSerializer implements Serializer {

    Instance;

    private static ThreadLocal<FSTConfiguration> fstConfigurationThreadLocal = new ThreadLocal<FSTConfiguration>() {
        @Override
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    @Override
    public <TObject> byte[] serialize(TObject object) {
        return fstConfigurationThreadLocal.get().asByteArray(object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TObject> TObject deserialize(byte[] byteArray) {
        return (TObject) fstConfigurationThreadLocal.get().asObject(byteArray);
    }

}
