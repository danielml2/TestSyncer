package me.danielml.schooltests.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import me.danielml.schooltests.objects.Change;
import me.danielml.schooltests.objects.Subject;
import me.danielml.schooltests.objects.Test;
import me.danielml.schooltests.objects.Test.TestType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.danielml.schooltests.TestMain.DEBUG;
import static me.danielml.schooltests.TestMain.YEAR_ID;

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
            String credentialsFile = DEBUG ? "/debug_firebase.json" : "/firebase.json";
            String dbURL = DEBUG ? "https://schooltests-debug-default-rtdb.europe-west1.firebasedatabase.app/" : "https://schooltests-419f5-default-rtdb.europe-west1.firebasedatabase.app";

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(getClass().getResourceAsStream(credentialsFile)))
                    .setDatabaseUrl(dbURL)
                    .build();

            app = FirebaseApp.initializeApp(options);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        db = FirebaseDatabase.getInstance(app);
    }
    public List<Test> loadTestsFromFirebase(int gradeNum) {
        DataSnapshot testsDB = get("years/" + YEAR_ID + "/tests/grade" + gradeNum);

        return StreamSupport.stream(testsDB.getChildren().spliterator(), false)
                .map(testSnapshot -> {
                    var classNums = testSnapshot.child("classNums").getValue(new GenericTypeIndicator<ArrayList<Integer>>() {});
                    Subject subject = Subject.from(testSnapshot.child("subject").getValue(String.class));
                    TestType type = TestType.from(testSnapshot.child("type").getValue(String.class));
                    long dueDate = testSnapshot.child("dueDate").getValue(Long.class);

                    return new Test(subject,dueDate,type,gradeNum,classNums.toArray(new Integer[0]));
                })
                .collect(Collectors.toList());
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

    public DataSnapshot get(String refString) {
        DatabaseReference ref = db.getReference(refString);

        final DataSnapshot[] snapshot = {null};
        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapshot[0] = dataSnapshot;
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return snapshot[0];
    }
    public String formatTestDBName(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + dateFormat.format(test.getDueDate());
    }

    public String formatChangeDBName(Change change) {
        return "change_" + change.getTest().getSubject().name().toLowerCase() + "_" + change.getType().name().toLowerCase() + "_" + dateFormat.format(new Date(change.getChangeDate()));
    }
}


