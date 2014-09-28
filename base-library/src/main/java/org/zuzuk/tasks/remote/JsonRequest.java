package org.zuzuk.tasks.remote;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ObjectParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.apache.commons.io.IOUtils;
import org.zuzuk.utils.Ln;
import org.zuzuk.tasks.remote.base.HttpRequest;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Request that returns data in JSON format
 */
public abstract class JsonRequest<T> extends HttpRequest<T> {
    protected final static JsonFactory DefaultJsonFactory = new JacksonFactory();

    @Override
    protected ObjectParser getParser() {
        return DefaultJsonFactory.createJsonObjectParser();
    }

    protected JsonRequest(Class<T> responseResultType) {
        super(responseResultType);
    }
}
