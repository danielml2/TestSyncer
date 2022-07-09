package me.danielml.schooltests.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import me.danielml.schooltests.objects.Subject;
import me.danielml.schooltests.objects.Test;

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
            service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),credential).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void removeDates(List<Test> currentEvents, String calendarID) throws IOException {
        Events events = service.events().list(calendarID).execute();


        for (Event event : events.getItems()) {
            Date date = new Date(event.getStart().getDate().getValue());
            String dateFormat = eventDateFormat.format(date);
            String title = event.getSummary();

            if (currentEvents.stream().noneMatch(test -> eventDateFormat.format(test.getDueDate()).equals(dateFormat) && test.getSubject().equals(Subject.from(title)) &&
                    test.getType().equals(Test.TestType.from(title)))) {
                System.out.println("Removed date at: " + dateFormat);
                service.events().delete(calendarID, event.getId()).execute();
            }
        }
    }


    public void updateTestEvents(List<Test> tests, String calendarID) throws IOException {
        Events events = service.events().list(calendarID).execute();

        for (Test test : tests) {

           boolean isEventPresent = events.getItems().stream()
                                    .anyMatch(event -> eventDateFormat.format(test.getDueDate()).equals(event.getStart().getDate().toString()));
           if(!isEventPresent) {

               Date date = test.asDate();
               Date endDate = new Date(date.getTime() + 86400000L);

               Event event = new Event()
                       .setStart(new EventDateTime().setDate(new DateTime(eventDateFormat.format(date))))
                       .setEnd(new EventDateTime().setDate(new DateTime(eventDateFormat.format(endDate))))
                       .setDescription("כיתות: "+ Arrays.toString(test.getClassNums().toArray()).replace("[","").replace("]","").replace("-1","שכבתי"))
                       .setSummary(test.getType().getName() + " " + test.getSubject().getDefaultName());

                service.events().insert(calendarID,event).execute();
                System.out.println(eventDateFormat.format(date));
                System.out.println("Inserted event " + eventDateFormat.format(test.getDueDate()));
                delayForAPI();
           }

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
