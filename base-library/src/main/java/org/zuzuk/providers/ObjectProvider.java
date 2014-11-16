package org.zuzuk.providers;

import org.zuzuk.providers.base.DataProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Provider that stores an object
 */
public class ObjectProvider<TObject extends Serializable> extends DataProvider {
    private TObject object;

    /* Returns stored data object */
    public TObject get() {
        return object;
    }

    /* Sets data object */
    public void set(TObject object) {
        this.object = object;
        onDataSetChanged();
    }

    @Override
    public boolean isInitialized() {
        return object != null;
    }

    @Override
    protected void resetInternal() {
        object = null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(isInitialized());
        if (isInitialized()) {
            out.writeObject(object);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            object = (TObject) in.readObject();
        }
    }
}
