package me.danielml.schooltests.objects;

import java.util.Date;

public class Change {

    private final Test test;
    private final long changeDate;
    private final ChangeType type;


    public Change(Test test, ChangeType type, Date changeDate) {
        this.test = test;
        this.type = type;
        this.changeDate = changeDate.getTime();
    }

    public Test getTest() {
        return test;
    }

    public long getChangeDate() {
        return changeDate;
    }

    public ChangeType getType() {
        return type;
    }
}


