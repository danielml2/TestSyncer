package me.danielml.schooltests.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.calendar.model.Events;
import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Test;
import org.apache.poi.ss.formula.functions.Even;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JSONManager {

    private final SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void toJSON(String fileName, List<Test> tests) throws IOException {
        File file = new File("data/" + fileName + ".json");
        if(!file.exists()) file.createNewFile();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, tests);
    }

    public List<Test> fromJSON(String fileName)  {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("data/" + fileName + ".json"), new TypeReference<List<Test>>() {
            });
        } catch (IOException exception) {
            System.out.println(fileName + " had an error while loading: " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    public void loadTestsToJSON(List<Test> tests, Events calendarEvents, Grade grade) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode calendarNode = mapper.createObjectNode();

        tests.forEach(test -> {
            calendarEvents.getItems()
                    .forEach(event -> {
                        if (eventDateFormat.format(test.getDueDate()).equals(event.getStart().getDate().toString()))
                            calendarNode.put(eventDateFormat.format(test.getDueDate()), event.getId());
                    });
        });

        mapper.writeValue(new File("data/calendarID_" + grade.getGradeNum() + ".json"), calendarNode);
    }

}
