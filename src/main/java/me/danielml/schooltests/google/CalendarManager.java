package me.danielml.schooltests.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Test;
import org.apache.poi.ss.formula.functions.Even;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
            service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),credential).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void updateTestEvents(List<Test> additions, List<Test> removals, Grade grade) throws IOException {
        removeDates(removals, grade.getCalendarId());
        addEvents(additions, grade.getCalendarId());
    }

    private void removeDates(List<Test> removals, String calendarId) throws IOException {
        String testName;
        for(Test test : removals) {
            testName = test.getSubject().getCalendarIDName() + "date" + test.getDueDate() + "grade" + test.getGradeNum();
            service.events().delete(calendarId, testName).execute();
        }
    }
    
    private void addEvents(List<Test> additions, String calendarId) throws IOException {
        for (Test test : additions) {

            Event event = createEventFor(test);

            System.out.println("Event ID: " +test.getSubject().getCalendarIDName() + "date" + test.getDueDate() + "grade" + test.getGradeNum());
            service.events().insert(calendarId, event);

            System.out.println("Inserted event at " + test.getDateFormatted());
            delayForAPI();
        }
    }

    public Event createEventFor(Test test) {
        Date date = test.asDate();
        Date endDate = new Date(date.getTime() + 86400000L);

        return new Event()
                .setStart(new EventDateTime().setDate(new DateTime(eventDateFormat.format(date))))
                .setEnd(new EventDateTime().setDate(new DateTime(eventDateFormat.format(endDate))))
                .setDescription("כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"))
                .setSummary(test.getType().getName() + " " + test.getSubject().getDefaultName())
                .setId(test.getSubject().getCalendarIDName() + "date" + test.getDueDate() + "grade" + test.getGradeNum());
    }

   @SuppressWarnings("unused") // This is a debug method.
   public void clear(String calendarID) throws IOException {
        Events events = service.events().list(calendarID).execute();
        events.getItems().forEach(event -> {
            try {
                    service.events().delete(calendarID,event.getId()).execute();
                    delayForAPI();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
    @SuppressWarnings("unused") // This is also a debug method.
    public List<Event> listEvents(String calendarID) throws IOException {
        return service.events().list(calendarID).execute().getItems();
    }

    public void delayForAPI() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unused") // Debug method
    public void addMissingEvents(String calendarID, List<Test> tests) throws IOException {
        List<Event> events = listEvents(calendarID);

        for(Test test: tests) {
            if(events.stream().noneMatch(event ->  event.getId().equals(test.getSubject().getCalendarIDName() + "date" + test.getDueDate() + "grade" + test.getGradeNum())))
            {
                service.events().insert(calendarID, createEventFor(test)).execute();
                delayForAPI();
            }
        }
    }
}
