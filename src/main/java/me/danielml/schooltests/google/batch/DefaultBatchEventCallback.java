package me.danielml.schooltests.google.batch;

import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.calendar.model.Event;


public class DefaultBatchEventCallback extends JsonBatchCallback<Event> {
    @Override
    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {

    }

    @Override
    public void onSuccess(Event event, HttpHeaders httpHeaders) {

    }

}
