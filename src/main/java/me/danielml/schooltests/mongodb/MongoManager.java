package me.danielml.schooltests.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import me.danielml.schooltests.objects.Test;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.in;
import static me.danielml.schooltests.TestMain.DEBUG;
import static me.danielml.schooltests.TestMain.YEAR_ID;

public class MongoManager {

    private MongoClient client;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

    public MongoManager() {

        String dbURI = DEBUG ? "mongodb://localhost:27017" : System.getenv("MONGODB_URI");

        ConnectionString connectionString = new ConnectionString(dbURI);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();

        try {
            System.out.println("Connecting..");
            client = MongoClients.create(settings);
            System.out.println("Connected successfully!");
        } catch (Exception exception) {
            System.err.println("Database didn't connect properly: ");
            exception.printStackTrace();
            System.exit(1);
        }

    }

   public void updateTests(List<Test> additions, List<Test> removals) {
        List<Document> newDocs = additions.stream().map(this::convertTestToDocument).collect(Collectors.toList());
        List<String> objectIdsToRemove = removals.stream().map(this::getTestDBName).collect(Collectors.toList());


       MongoCollection<Document> testsCollection = client.getDatabase("tests").getCollection(YEAR_ID);

       if(additions.size() > 0) testsCollection.insertMany(newDocs);
       if(removals.size() > 0) testsCollection.deleteMany(in("_id", objectIdsToRemove));
   }

   public Document convertTestToDocument(Test test) {
        Document document = new Document();
        document.put("_id", getTestDBName(test));
        return document
                .append("subject", test.getSubject().name())
                .append("gradeNum", test.getGradeNum())
                .append("classNums", test.getClassNums())
                .append("dueDate", test.getDueDate())
                .append("type", test.getType())
                .append("creationText", test.getCreationText());
   }

   public String getTestDBName(Test test) {
        return test.getSubject().name().toLowerCase() + "_" + test.getType().name().toLowerCase() + "_" + test.getGradeNum() + "_" + dateFormat.format(test.getDueDate());
   }

   public void close() {
        client.close();
   }
}
