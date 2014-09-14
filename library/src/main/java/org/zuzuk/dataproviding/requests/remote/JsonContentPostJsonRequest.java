package org.zuzuk.dataproviding.requests.remote;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.json.JsonHttpContent;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Post request that includes JSON data as content and returns data in JSON format
 */
public abstract class JsonContentPostJsonRequest<T, ContentType> extends PostJsonRequest<T> {

    @Override
    protected HttpContent getContent() {
        return new JsonHttpContent(DefaultJsonFactory, getContentObject());
    }

    protected JsonContentPostJsonRequest(Class<T> responseResultType) {
        super(responseResultType);
    }

    @Override
    protected void setupHeaders(HttpHeaders headers) {
        super.setupHeaders(headers);
        headers.setContentType("application/json");
    }

    /* Returns content object to store in JSON format */
    protected abstract ContentType getContentObject();
}
