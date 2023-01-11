package me.danielml.schooltests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danielml.schooltests.json.JSONManager;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.danielml.schooltests.google.*;
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

        FirebaseManager dbManager = new FirebaseManager();
        JSONManager json = new JSONManager();

        int totalAdditions = 0;
        int totalRemovals = 0;
        Grade[] grades = new ObjectMapper().readValue(new File("data/grades.json"), new TypeReference<>() {});
        for(Grade grade : grades) {
            System.out.println("Starting grade #" + grade.getGradeNum());
            List<Test> loadedTests = dbManager.loadTestsFromFirebase(grade.getGradeNum());

            List<Integer> exclusions = dbManager.getRowExclusions(grade);

            List<Test> fromExcel = testManager.getTests(file, grade, exclusions);

            List<Test> gradeAdditions = testManager.getAdditions(loadedTests, fromExcel);
            List<Test> gradeRemovals = testManager.getRemovals(loadedTests, fromExcel);

            totalAdditions += gradeAdditions.size();
            totalRemovals += gradeRemovals.size();

            if(DEBUG)
                json.toJSON("tests_" + grade.getGradeNum(), fromExcel);

            calendarManager.updateTestEvents(gradeAdditions, gradeRemovals, grade);

            HashMap<String, Object> testChanges = new HashMap<>();
            HashMap<String, Object> newChangesLog = new HashMap<>();

            gradeAdditions.forEach(test -> {
                testChanges.put(dbManager.formatTestDBName(test), test);
                Change change = new Change(test, ChangeType.ADD, new Date());
                newChangesLog.put(dbManager.formatChangeDBName(change), change);
                System.out.println("[NEW EVENT]: " + test);
            });
            gradeRemovals.forEach(test -> {
                testChanges.put(dbManager.formatTestDBName(test), null);
                Change change = new Change(test, ChangeType.REMOVE, new Date());
                newChangesLog.put(dbManager.formatChangeDBName(change), change);
                System.out.println("[REMOVED EVENT]: " + test);
            });

            if(testChanges.size() > 0)
                dbManager.update("years/" + YEAR_ID + "/tests/grade" + grade.getGradeNum(), testChanges);
            if(newChangesLog.size() > 0)
                dbManager.update("years/" + YEAR_ID + "/changes/grade" + grade.getGradeNum(), newChangesLog);

            System.out.println("(Additions: " + gradeAdditions.size() + ", Removals: " + gradeRemovals.size() + ")");
            System.out.println("Finished Grade #" + grade.getGradeNum());
        }

        System.out.println("Total changes - (Additions: " + totalAdditions + ", Removals: " + totalRemovals + ")");
        dbManager.setValue("/last_update",new Date().getTime());
    }
}
