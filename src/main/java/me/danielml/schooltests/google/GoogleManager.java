package me.danielml.schooltests.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public abstract class GoogleManager {

    protected Credential credential;

    public GoogleManager() {
        initializeService();
    }
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
