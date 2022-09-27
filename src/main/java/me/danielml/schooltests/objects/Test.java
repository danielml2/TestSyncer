package me.danielml.schooltests.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Test {

    private final Subject subject;
    private final int gradeNum;
    private final List<Integer> classNums;
    private final long dueDate;
    private final TestType type;

    @JsonIgnore
    private String creationText;

    @JsonIgnore
    private boolean manuallyCreated;

    @JsonCreator
    public Test(@JsonProperty("subject") Subject subject, @JsonProperty("date") long dueDate, @JsonProperty("type") TestType type, @JsonProperty("gradeNum") int gradeNum, @JsonProperty("classNum") Integer[] classNums) {
        this.subject = subject;
        this.dueDate = dueDate;
        this.type = type;
        this.gradeNum = gradeNum;
        this.creationText = "NONE";
        this.manuallyCreated = false;
        this.classNums = new ArrayList<>(Arrays.asList(classNums));
    }

    public void addClassNum(int classNum) {
        this.classNums.add(classNum);
    }

    @JsonProperty("subject")
    public Subject getSubject() {
        return subject;
    }

    @JsonProperty("date")
    public long getDueDate() {
        return dueDate;
    }

    @JsonIgnore
    public Date asDate() { return new Date(dueDate); }

    @JsonIgnore
    public String getDateFormatted() {
        return new SimpleDateFormat("yyyy-MM-dd").format(asDate());
    }

    @JsonProperty("type")
    public TestType getType() {
        return type;
    }

    @JsonProperty("classNum")
    public List<Integer> getClassNums() {
        return classNums;
    }

    @JsonProperty("gradeNum")
    public int getGradeNum() {
        return gradeNum;
    }

    public void setCreationText(String creationText) {
        this.creationText = creationText;
    }

    public String getCreationText() {
        return creationText;
    }

    public boolean isManuallyCreated() {
        return manuallyCreated;
    }

    public void setManuallyCreated(boolean manuallyCreated) {
        this.manuallyCreated = manuallyCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Test test = (Test) o;
        return subject == test.subject && dueDate == test.dueDate && type == test.type;
    }

    @Override
    public String toString() {
        return "(Grade " + gradeNum + "): " + subject + " " + type + " at " + getDateFormatted() + " for " + classNums;
    }

    public enum TestType {
        BAGROT("בגרות"),MATCONET("מתכונת"),TEST("מבחן"),QUIZ("בוחן"),SECOND_DATE("מועד ב"),NONE("");

        private final String name;

        TestType(String type) {
            this.name = type;
        }

        public static TestType from(String other) {
            if(other.contains("שכבתי"))
                return TEST;

            for(TestType type : values()) {
                if(type.name().contains(other))
                    return type;
                if(other.contains(type.name))
                    return type;
            }

            return NONE;
        }

        @JsonValue
        public String getEnumName() {
            return this.name();
        }

        public String getName() {
            return name;
        }
    }

}


