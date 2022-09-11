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

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),new InputStreamReader(in));


        File refreshTokenFile = new File("tokens/refresh.json");
        if(!refreshTokenFile.exists())
            resetCredentials();


        String refreshToken = getRefreshToken();

        credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setClientSecrets(clientSecrets)
                .setJsonFactory(JacksonFactory.getDefaultInstance()).build().setAccessToken(getNewToken(refreshToken,clientSecrets.getDetails().getClientId(),clientSecrets.getDetails().getClientSecret()));
    }

    private void saveRefreshToken(String refreshToken) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();


        node.put("refresh_token", refreshToken);


        File file = new File("tokens/refresh.json");
        if(!file.exists()) file.createNewFile();

        mapper.writeValue(new File("tokens/refresh.json"), node);
    }

    private String getRefreshToken() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new File("tokens/refresh.json"));

        return node.get("refresh_token").asText();
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

    private void resetCredentials() {
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

}
