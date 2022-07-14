package me.danielml.schooltests.google;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class DriveManager extends GoogleManager {

    private Drive service;

    public DriveManager() {
        initializeService();
    }

    public File downloadFile(String fileId) throws IOException {

        File file = new File("table.xlsx");
        if(!file.exists()) file.createNewFile();


        OutputStream stream = new FileOutputStream(file);
        try {
            service.files().get(fileId).executeMediaAndDownloadTo(stream);
        }catch (TokenResponseException exception) {
            resetCredentials();
            downloadFile(fileId);
        }
        return file;
    }

    @Override
    public void initializeService(){
        if(service != null)
            return;

        try {
            authorize();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        try {
            service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),credential).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}
