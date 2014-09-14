package org.zuzuk.dataproviding.requests.remote;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

import java.io.IOException;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Post request that returns data in JSON format
 */
public abstract class PostJsonRequest<T> extends JsonRequest<T> {

    /* Returns HttpContent */
    protected abstract HttpContent getContent();

    protected PostJsonRequest(Class<T> responseResultType) {
        super(responseResultType);
    }

    @Override
    protected HttpRequest buildRequest(HttpRequestFactory factory, GenericUrl url) throws IOException {
        return factory.buildPostRequest(url, getContent());
    }
}
