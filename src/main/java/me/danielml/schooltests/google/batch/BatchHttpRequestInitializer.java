package me.danielml.schooltests.google.batch;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

public class BatchHttpRequestInitializer implements HttpRequestInitializer {
    @Override
    public void initialize(HttpRequest httpRequest) {
        httpRequest.setReadTimeout(5 * 60000);
        httpRequest.setConnectTimeout(5 * 60000);
        httpRequest.setNumberOfRetries(5);
    }

}
