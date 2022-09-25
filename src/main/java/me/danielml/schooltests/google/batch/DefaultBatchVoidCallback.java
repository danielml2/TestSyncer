package me.danielml.schooltests.google.batch;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;

public class DefaultBatchVoidCallback extends JsonBatchCallback<Void> {

    @Override
    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders)  {

    }

    @Override
    public void onSuccess(Void unused, HttpHeaders httpHeaders)  {

    }
}
