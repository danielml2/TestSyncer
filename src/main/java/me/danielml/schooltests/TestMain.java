package me.danielml.schooltests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danielml.schooltests.json.JSONManager;
import java.io.File;
import java.util.Date;
import java.util.List;

import me.danielml.schooltests.google.*;
import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Test;

public class TestMain {

    private static final String FILE_ID = System.getenv("FILE_ID");

    public static void main(String[] args) throws Exception  {


        TestManager testManager = new TestManager();
        CalendarManager calendarManager = new CalendarManager();
        DriveManager manager = new DriveManager();

        File file = manager.downloadFile(FILE_ID);

        FirebaseManager dbManager = new FirebaseManager();
        JSONManager json = new JSONManager();
        boolean usingJSONFiles = isUsingJSON(args);

        Grade[] grades = new ObjectMapper().readValue(new File("data/grades.json"), new TypeReference<>() {});

        for(Grade grade : grades) {
            System.out.println("Starting grade #" + grade.getGradeNum());
            List<Test> loadedTests = usingJSONFiles ? json.fromJSON("tests_" + grade.getGradeNum()) : dbManager.loadTestsFromFirebase(grade.getGradeNum());

            List<Test> fromExcel = testManager.getTests(file, grade);

            List<Test> gradeAdditions = testManager.getAdditions(loadedTests, fromExcel);
            List<Test> gradeRemovals = testManager.getRemovals(loadedTests, fromExcel);

            if(usingJSONFiles)
                json.toJSON("tests_" + grade.getGradeNum(), fromExcel);

            gradeAdditions.forEach(dbManager::addTest);
            gradeRemovals.forEach(dbManager::removeTest);
            calendarManager.updateTestEvents(gradeAdditions, gradeRemovals, grade);
            System.out.println("Finished Grade #" + grade.getGradeNum());
        }


        dbManager.setValue("/last_update",new Date().getTime());
    }

    public static boolean isUsingJSON(String[] args) {
        for (String arg : args) {
            if (arg.contains("-using-json="))
                return Boolean.parseBoolean(arg.substring(arg.indexOf("=") + 1));
        }
        return false;
    }

}
