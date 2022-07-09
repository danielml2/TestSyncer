package me.danielml.schooltests;

import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Subject;
import me.danielml.schooltests.objects.Test;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestManager {

    public List<Test> getTests(File excelFile, Grade grade, boolean includePast) throws IOException {
        List<Test> tests = new ArrayList<>();

        FileInputStream stream = new FileInputStream(excelFile);
        Workbook workbook = new XSSFWorkbook(stream);

        Sheet sheet = workbook.getSheet(grade.getSheetIdentifier());

        Iterator<Row> rowIterator = sheet.rowIterator();
        List<Test> rowTests = new ArrayList<>();


        rowIterator.forEachRemaining(row -> {
            Iterator<Cell> iterator = row.cellIterator();

            iterator.forEachRemaining(cell -> {

                if(cell.getColumnIndex() < 2 || cell.getCellType() != CellType.STRING || row.getCell(0).getCellType() != CellType.NUMERIC)
                    return;


                String value = cell.getRichStringCellValue().getString();
                if(Subject.from(value) != Subject.OTHER && !value.contains("האקתון") && !value.contains("סיור") && !value.contains("יום")) {

                    Date dueDate = DateUtil.getJavaDate(row.getCell(0).getNumericCellValue());

                    int classNum = cell.getColumnIndex() - 1;
                    if(grade.getGradeNum() >= 10 && (classNum > grade.getMaxClassNum() || classNum == 1))
                        classNum = -1;

                    if(!includePast && dueDate.after(new Date()))
                        return;

                    Subject subject = Subject.from(value);
                    Test.TestType type = Test.TestType.from(value);

                    if(type != Test.TestType.SECOND_DATE && cell.getColumnIndex() == 10)
                        return;

                    if(type == Test.TestType.NONE && subject != Subject.OTHER)
                        type = Test.TestType.TEST;

                    Optional<Test> sameSubject = Optional.empty();
                    try {
                        if (rowTests.size() > 0)
                            sameSubject = rowTests.stream()
                                    .filter(test -> test.getSubject().equals(subject))
                                    .findFirst();
                    }catch (NullPointerException exception) { sameSubject = Optional.empty();}

                    if(!sameSubject.isPresent())
                    {
                        Test newTest = new Test(subject,dueDate.getTime(), type,grade.getGradeNum(), new Integer[]{classNum});
                        System.out.println(subject.name() + ": " + dueDate + " at COLUMN: " + cell.getColumnIndex());
                        tests.add(newTest);
                        rowTests.add(newTest);
                    }
                    else
                        sameSubject.get().addClassNum(classNum);
                }
            });
            rowTests.clear();
        });

        return tests;
    }

    public List<Test> getAdditions(List<Test> oldest, List<Test> newest) {
        return newest.stream()
                .filter(test -> !oldest.contains(test))
                .collect(Collectors.toList());
    }

    public List<Test> getRemovals(List<Test> oldest, List<Test> newest) {
        return oldest.stream()
                .filter(test -> !newest.contains(test))
                .collect(Collectors.toList());
    }

}
