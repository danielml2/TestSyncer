package me.danielml.schooltests.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class GoogleManager {

    public Credential credential;

    protected final void authorize() throws IOException, GeneralSecurityException {
        if(credential != null)
            return;

        InputStream in = DriveManager.class.getResourceAsStream("/credentials.json");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),new InputStreamReader(in));

        List<String> scopes = new ArrayList<>();
        scopes.add(DriveScopes.DRIVE_READONLY);
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(CalendarScopes.CALENDAR_EVENTS);


        String refreshToken = "";
        try {
            refreshToken = getRefreshToken();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setClientSecrets(clientSecrets)
                .setJsonFactory(JacksonFactory.getDefaultInstance()).build().setAccessToken(getNewToken(refreshToken,clientSecrets.getDetails().getClientId(),clientSecrets.getDetails().getClientSecret()));
    }

    private void saveRefreshToken(String refreshToken) throws IOException {
        JSONObject object = new JSONObject();
        object.put("refresh_token",refreshToken);

        File file = new File("tokens/refresh.json");
        if(!file.exists()) file.createNewFile();

        PrintWriter writer = new PrintWriter(file);
        writer.write(object.toJSONString());
        writer.flush();
        writer.close();
    }

    private String getRefreshToken() throws IOException, ParseException {
        JSONObject object = (JSONObject) new JSONParser().parse(new FileReader("tokens/refresh.json"));
        return String.valueOf(object.get("refresh_token"));
    }

    private String getNewToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        ArrayList<String> scopes = new ArrayList<>();

        scopes.add(DriveScopes.DRIVE_READONLY);
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(CalendarScopes.CALENDAR_EVENTS);

        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                refreshToken, clientId, clientSecret).setScopes(scopes).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }

    protected final void resetCredientals() {
        try {
            if(credential != null)
                return;

            InputStream in = DriveManager.class.getResourceAsStream("/credentials.json");

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),new InputStreamReader(in));

            List<String> scopes = new ArrayList<>();
            scopes.add(DriveScopes.DRIVE_READONLY);
            scopes.add(CalendarScopes.CALENDAR);
            scopes.add(CalendarScopes.CALENDAR_EVENTS);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),clientSecrets,scopes)
                    .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                    .setAccessType("offline").build();

            AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow,new LocalServerReceiver());
            credential = app.authorize("user");
            saveRefreshToken(credential.getRefreshToken());
        } catch (IOException | GeneralSecurityException exception) {
            exception.printStackTrace();
        }
    }
    public abstract void initializeService();

    /*
     if(credential != null)
            return;

           InputStream in = DriveManager.class.getResourceAsStream("/credentials.json");

           GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),new InputStreamReader(in));

           List<String> scopes = new ArrayList<>();
           scopes.add(DriveScopes.DRIVE_READONLY);
           scopes.add(CalendarScopes.CALENDAR);
           scopes.add(CalendarScopes.CALENDAR_EVENTS);

           GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                   JacksonFactory.getDefaultInstance(),clientSecrets,scopes)
                   .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                   .setAccessType("offline").build();


           credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
     */

}
