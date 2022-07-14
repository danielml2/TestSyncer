package me.danielml.schooltests;

import me.danielml.schooltests.objects.Grade;
import me.danielml.schooltests.objects.Subject;
import me.danielml.schooltests.objects.Test;
import me.danielml.schooltests.objects.Test.TestType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TestManager {

    private final String[] filterWords = {"חשיפה","עבודה","פעילות","טקס","הקאתון", "סיור", "יום"};

    public List<Test> getTests(File excelFile, Grade grade, boolean includePast) throws IOException {
        List<Test> tests = new ArrayList<>();

        FileInputStream stream = new FileInputStream(excelFile);
        Workbook workbook = new XSSFWorkbook(stream);

        Sheet sheet = workbook.getSheet(grade.getSheetIdentifier());

        Iterator<Row> rowIterator = sheet.rowIterator();
        List<Test> rowTests = new ArrayList<>();


        rowIterator.forEachRemaining(row -> {
            Iterator<Cell> iterator = row.cellIterator();

            Iterable<Cell> iterable = () -> iterator;
            Stream<Cell> cells = StreamSupport.stream(iterable.spliterator(), false);

            cells
                  .filter(cell ->
                          cell.getColumnIndex() >= 2 && cell.getCellType() == CellType.STRING && row.getCell(0).getCellType() == CellType.NUMERIC)
                  .filter(cell ->
                          Subject.from(cell.getStringCellValue()) != Subject.OTHER && Arrays.stream(filterWords).noneMatch(cell.getStringCellValue()::contains))
                  .filter(cell -> {
                        TestType type = TestType.from(cell.getRichStringCellValue().getString());
                        return cell.getColumnIndex() < 10 || type == TestType.SECOND_DATE;
                  })
                  .forEach(cell -> {
                       String value = cell.getStringCellValue();

                       Subject subject = Subject.from(value);
                       TestType type = TestType.from(value) == TestType.NONE ? TestType.TEST : TestType.from(value);
                       Date dueDate = DateUtil.getJavaDate(row.getCell(0).getNumericCellValue());

                      int classNum = cell.getColumnIndex() - 1;
                      if(grade.getGradeNum() >= 10 && (classNum > grade.getMaxClassNum() || classNum == 1) || value.contains("שכבתי"))
                         classNum = -1;


                      Optional<Test> sameSubject = rowTests.stream()
                              .map(Optional::ofNullable)
                              .filter(element -> element.isPresent() && element.get().getSubject().equals(subject))
                              .findFirst()
                              .orElseGet(Optional::empty);

                       if(!sameSubject.isPresent())
                       {
                           Test newTest = new Test(subject,dueDate.getTime(), type,grade.getGradeNum(), new Integer[]{classNum});
                           tests.add(newTest);
                           rowTests.add(newTest);
                       }
                       else
                           sameSubject.get().addClassNum(classNum);

                       System.out.println("(Grade " + grade.getGradeNum() + ") [Row: " + row.getRowNum() + ", COL: " + cell.getColumnIndex() + "] Detected test: "
                               + subject.name() + " on " + dueDate + " for " + classNum);
                       System.out.println("From: " + value);
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
