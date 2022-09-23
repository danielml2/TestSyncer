package me.danielml.schooltests.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public abstract class GoogleManager {

    protected Credential credential;

    protected final void authorize() throws IOException, GeneralSecurityException {
        if(credential != null)
            return;

        InputStream in = DriveManager.class.getResourceAsStream("/credentials.json");

        ArrayList<String> scopes = new ArrayList<>();

        scopes.add(DriveScopes.DRIVE_READONLY);
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(CalendarScopes.CALENDAR_EVENTS);

        credential = GoogleCredential.fromStream(in).createScoped(scopes);
    }
    public abstract void initializeService();

}
