package me.danielml.schooltests.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Subject;
import me.danielml.schooltests.objects.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class CalendarManager extends GoogleManager {

    private Calendar service;
    private final SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CalendarManager() {
        initializeService();
    }


    @Override
    public void initializeService() {
        if(service != null)
            return;

        try {
            authorize();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        try {
            service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),credential).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void updateTestEvents(List<Test> additions, List<Test> removals, Grade grade) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = (ObjectNode) mapper.readTree(new File("calendarID_" + grade.getGradeNum()));
        removeDates(removals, jsonNode, grade.getCalendarId());
        addEvents(additions, jsonNode, grade.getCalendarId());
        mapper.writeValue(new File("data/calendarID_" + additions.get(0).getGradeNum() + ".json"), jsonNode);
    }

    private void removeDates(List<Test> removals, ObjectNode jsonNode, String calendarId) throws IOException {
        for(Test test : removals) {
            service.events().delete(calendarId, jsonNode.get(test.getDateFormatted()).asText()).execute();
            jsonNode.remove(test.getDateFormatted());
        }
    }
    
    private void addEvents(List<Test> additions, ObjectNode jsonNode, String calendarId) throws IOException {
        for (Test test : additions) {
            Date date = test.asDate();
            Date endDate = new Date(date.getTime() + 86400000L);

            Event event = new Event()
                    .setStart(new EventDateTime().setDate(new DateTime(eventDateFormat.format(date))))
                    .setEnd(new EventDateTime().setDate(new DateTime(eventDateFormat.format(endDate))))
                    .setDescription("כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"))
                    .setSummary(test.getType().getName() + " " + test.getSubject().getDefaultName());

            var addedEvent = service.events().insert(calendarId,event).execute();
            jsonNode.put(test.getDateFormatted(), addedEvent.getId());

            System.out.println("Inserted event " + test.getDateFormatted());
            delayForAPI();
        }
    }

    public void clear(String calendarID) throws IOException {
        Events events = service.events().list(calendarID).execute();
        events.getItems().forEach(event -> {
            try {
                service.events().delete(calendarID,event.getId()).execute();
                Thread.sleep(250);
            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void delayForAPI() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
