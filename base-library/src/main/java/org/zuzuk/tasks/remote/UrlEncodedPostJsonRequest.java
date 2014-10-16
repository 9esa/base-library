package org.zuzuk.tasks.remote;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.UrlEncodedParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Post request that includes url encoded data as content and returns data in JSON format
 */
public abstract class UrlEncodedPostJsonRequest<T> extends PostJsonRequest<T> {

    @Override
    protected HttpContent getContent() {
        Map<String, Object> urlEncodedParameters = new HashMap<>();
        setupUrlEncodedParameters(urlEncodedParameters);
        return new UrlEncodedContent(urlEncodedParameters);
    }

    protected UrlEncodedPostJsonRequest(Class<T> responseResultType) {
        super(responseResultType);
    }

    /* Setup url encoded parameters */
    protected abstract void setupUrlEncodedParameters(Map<String, Object> urlEncodedParameters);
}
