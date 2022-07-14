package me.danielml.schooltests.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.NonNull;
import me.danielml.schooltests.objects.Change;
import me.danielml.schooltests.objects.ChangeType;
import me.danielml.schooltests.objects.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class FirebaseManager extends GoogleManager{

    private FirebaseApp app;
    private FirebaseDatabase db;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

    public FirebaseManager() {
        initializeService();
    }

    @Override
    public void initializeService() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(FirebaseManager.class.getResourceAsStream("/firebase.json")))
                    .setDatabaseUrl("https://schooltests-419f5-default-rtdb.europe-west1.firebasedatabase.app")
                    .build();

            app = FirebaseApp.initializeApp(options);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        db = FirebaseDatabase.getInstance(app);
    }

    public void addTest(Test test) {
        setValue("/tests/grade" + test.getGradeNum() + "/" + formatTestDBName(test), test);
        Change change = new Change(test, ChangeType.ADD, new Date());
        setValue("/changes/grade" + test.getGradeNum() + "/" + formatChangeDBName(change), change);
    }

    public void removeTest(Test test) {
        setValue("/tests/grade" + test.getGradeNum() + "/" + formatTestDBName(test),null);
        Change change = new Change(test, ChangeType.REMOVE, new Date());
        setValue("/changes/grade" + test.getGradeNum() + "/" + formatChangeDBName(change), change);
    }

    public void setValue(String refString, Object data) {
        CountDownLatch latch = new CountDownLatch(1);
        DatabaseReference ref = db.getReference(refString);

        ref.setValue(data, (databaseError, databaseReference) -> {
            System.out.println("Updated " + refString + " to " + data);
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private String formatTestDBName(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate());
    }

    private String formatChangeDBName(Change change) {
        return "change_" + change.getTest().getSubject().name().toLowerCase() + "_" + change.getType().name().toLowerCase() + "_" + dateFormat.format(new Date(change.getChangeDate()));
    }
}


