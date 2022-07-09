package me.danielml.schooltests.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Grade {
    private final int maxClassNum;
    private final String sheetIdentifier;
    private final int gradeNum;
    private final String calendarId;

    @JsonCreator
    public Grade(@JsonProperty("maxClassNum") int maxClassNum, @JsonProperty("gradeNum") int gradeNum, @JsonProperty("sheetIdentifier") String sheetIdentifier,
                 @JsonProperty("calendarId") String calendarId) {
        this.maxClassNum = maxClassNum;
        this.sheetIdentifier = sheetIdentifier;
        this.gradeNum = gradeNum;
        this.calendarId = calendarId;
    }

    public int getMaxClassNum() {
        return maxClassNum;
    }

    public String getSheetIdentifier() {
        return sheetIdentifier;
    }

    public int getGradeNum() {
        return gradeNum;
    }

    public String getCalendarId() {
        return calendarId;
    }
}
