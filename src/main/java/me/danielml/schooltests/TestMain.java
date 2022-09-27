package me.danielml.schooltests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danielml.schooltests.json.JSONManager;
import java.io.File;
import java.util.*;

import me.danielml.schooltests.google.*;
import me.danielml.schooltests.mongodb.MongoManager;
import me.danielml.schooltests.objects.*;

public class TestMain {

    private static final String FILE_ID = System.getenv("FILE_ID");
    public static boolean DEBUG = false;
    public static final String YEAR_ID = "2022-2023";

    public static void main(String[] args) throws Exception  {
        DEBUG = Arrays.stream(args).anyMatch(arg -> arg.contains("-usingDebug"));

        TestManager testManager = new TestManager();
        CalendarManager calendarManager = new CalendarManager();
        DriveManager manager = new DriveManager();

        File file = manager.downloadFile(FILE_ID);

        JSONManager json = new JSONManager();
        Grade[] grades = new ObjectMapper().readValue(new File("data/grades.json"), new TypeReference<>() {});
        List<Test> allAdditions = new ArrayList<>();
        List<Test> allRemovals = new ArrayList<>();

        for(Grade grade : grades) {
            System.out.println("Starting grade #" + grade.getGradeNum());
            List<Test> loadedTests = json.fromJSON("tests_" + grade.getGradeNum());

            List<Test> fromExcel = testManager.getTests(file, grade);

            List<Test> gradeAdditions = testManager.getAdditions(loadedTests, fromExcel);
            List<Test> gradeRemovals = testManager.getRemovals(loadedTests, fromExcel);

            if (DEBUG)
                json.toJSON("tests_" + grade.getGradeNum(), fromExcel);

            calendarManager.updateTestEvents(gradeAdditions, gradeRemovals, grade);

            allAdditions.addAll(gradeAdditions);
            allRemovals.addAll(gradeRemovals);
            System.out.println("Finished Grade #" + grade.getGradeNum());
        }
        MongoManager mongoDBManager = new MongoManager();

        mongoDBManager.updateTests(allAdditions, allRemovals);
        mongoDBManager.close();
    }
}
