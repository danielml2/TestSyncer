package me.danielml.schooltests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.danielml.schooltests.json.JSONManager;
import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Test;

import me.danielml.schooltests.google.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TestMain {

    private static final String FILE_ID = System.getenv("FILE_ID");

    public static void main(String[] args) throws Exception  {
        TestManager testManager = new TestManager();
        CalendarManager calendarManager = new CalendarManager();
        JSONManager json = new JSONManager();
        DriveManager manager = new DriveManager();

        File file = manager.downloadFile(FILE_ID);

        List<Test> allAdditions = new ArrayList<>();
        List<Test> allRemovals = new ArrayList<>();

        Grade[] grades = new ObjectMapper().readValue(new File("data/grades.json"), new TypeReference<>() {});

        for(Grade grade : grades) {
            System.out.println("Starting grade #" + grade.getGradeNum());
            List<Test> fromJSON = json.fromJSON("tests_" + grade.getGradeNum());
            List<Test> fromExcel = testManager.getTests(file, grade,true);


            List<Test> gradeAdditions = testManager.getAdditions(fromJSON, fromExcel);
            List<Test> gradeRemovals = testManager.getRemovals(fromJSON, fromExcel);
            allAdditions.addAll(gradeAdditions);
            allRemovals.addAll(gradeRemovals);

            json.toJSON("tests_" + grade.getGradeNum(), fromExcel);

            calendarManager.updateTestEvents(gradeAdditions, gradeRemovals, grade);
            System.out.println("Finished Grade #" + grade.getGradeNum());
        }

        allAdditions.forEach(test -> System.out.println("(Grade " + test.getGradeNum() +") Added: " + test.getSubject() + ": " + test.asDate()));

        FirebaseManager dbManager = new FirebaseManager();
        allAdditions.forEach(dbManager::addTest);
        allRemovals.forEach(dbManager::removeTest);
        dbManager.setValue("/last_update",new Date().getTime());
        displaySystemMessage("Finished updating", allRemovals.size() + " tests were removed and " + allAdditions.size() + " were added.");
    }

    public static void displaySystemMessage(String caption, String message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = Toolkit.getDefaultToolkit().getImage("images//trayicon.png");
            TrayIcon trayIcon = new TrayIcon(image, "gaming");
            tray.add(trayIcon);
            trayIcon.displayMessage(caption, message, TrayIcon.MessageType.INFO);
            tray.remove(trayIcon);
        }catch (AWTException exception) {
            exception.printStackTrace();
        }
    }


}
