package org.zuzuk.dataproviding.requests.remote;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ObjectParser;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.apache.commons.io.IOUtils;
import org.zuzuk.utils.Ln;
import org.zuzuk.dataproviding.requests.remote.base.HttpRequest;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Request that returns data in JSON format
 */
public abstract class JsonRequest<T> extends HttpRequest<T> {
    protected final static JsonFactory DefaultJsonFactory = new JacksonFactory();

    private final Class<T> responseResultType;

    @Override
    public long getCacheExpiryDuration() {
        return DurationInMillis.ALWAYS_EXPIRED;
    }

    @Override
    protected ObjectParser getParser() {
        return DefaultJsonFactory.createJsonObjectParser();
    }

    /* Returns base url without parameters */
    protected abstract String getUrl();

    protected JsonRequest(Class<T> responseResultType) {
        super(responseResultType);
        this.responseResultType = responseResultType;
    }

    @Override
    public T execute() throws Exception {
        com.google.api.client.http.HttpRequest request = buildRequest();

        Ln.v("REQUESTED: " + request.getUrl().toString());

        if (getUrl().contains("json")) {
            T response = request.execute().parseAs(responseResultType);
            handleResponse(response);
            return response;
        } else {
            //TODO: delete that after full json implementation
            StringWriter writer = new StringWriter();
            IOUtils.copy(request.execute().getContent(), writer, "UTF-8");
            String json = writer.toString();
            json = json.substring(json.indexOf("(") + 1, json.lastIndexOf(")"));
            T response = DefaultJsonFactory.createJsonObjectParser().parseAndClose(new StringReader(json), responseResultType);
            handleResponse(response);
            return response;
        }
    }
}
