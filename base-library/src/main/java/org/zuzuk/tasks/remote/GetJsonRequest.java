package org.zuzuk.tasks.remote;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

import org.apache.commons.io.IOUtils;
import org.zuzuk.utils.Ln;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Get request that returns data in JSON format
 */
public abstract class GetJsonRequest<T> extends JsonRequest<T> {

    protected GetJsonRequest(Class<T> responseResultType) {
        super(responseResultType);
    }

    @Override
    protected HttpRequest buildRequest(HttpRequestFactory factory, GenericUrl url) throws IOException {
        return factory.buildGetRequest(url);
    }
}
