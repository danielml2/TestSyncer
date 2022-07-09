package me.danielml.schooltests.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danielml.schooltests.objects.Test;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JSONManager {

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
}
