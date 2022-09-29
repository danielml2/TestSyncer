## TestSyncer
[![wakatime](https://wakatime.com/badge/user/e02dbcc1-d0c7-4388-997f-0f0b3d73eaac/project/4611e0e6-63f8-4d1e-9dfe-b63c964bbed3.svg)](https://wakatime.com/badge/user/e02dbcc1-d0c7-4388-997f-0f0b3d73eaac/project/4611e0e6-63f8-4d1e-9dfe-b63c964bbed3)
### What is this project?
This, as the name of the repository says, is a java program which syncs up tests to a firebase database, using a test schedule which is an excel file downloaded from google drive, and converting that to test objects on the database.

This is combined of two apps, one app is a this, the "Java Server Side", 
which takes all of the tests from the excel file of the test schedule and exports them for each grade, 
onto a firebase database, and essentially syncs it up with any changes that happen to the test schedule & while not mentioned on the second app's page, also syncs up the tests to Google Calendars for each grade. And the second app, the ["Front-end"](https://github.com/danielml2/schooltestsite) being a website where it's all displayed in a nice organized way for people to use.


And this is the "TestSyncer" which converts the excel document to the tests on the database.

### Why do this?
Honestly, just for fun & it's good practice for java development, using a database and web development, and also gives something back for people to use in the school and all that.

however, this is 100% out of my own free time and other than that i don't get much from it, so I'm not obligated to keep this up 100% of the time, total time spent on this website project alone - can be seen on the wakatime badge.

### Technical details

Each test is built from 5 key properties:

`dueDate`: the time the test is set on for, as system time (e.g. ms since Sep 1st 1970)

`subject`: name of the subject, obviously

`type`: Type of the test (e.g. Bagrot, Matconet, etc)

`classNums`: An array of numbers representing which classes have that test, since usually on middle school they have class specific test, if it's the entire grade it would be a one element array with -1

`gradeNum`: Grade number, (e.g. Grade 7,8,9)

and 2 extra mostly debug values:

`manuallyCreated`: whether the test was manually created by me and not by the syncer (Usually manually adding is only when there's wrong detection of a test, or is a situation the syncer doesn't know how to handle)

`creationText`: Shows to the users what text was in the spreadsheet cell that the syncer detected it from and added to the database

The whole class definition can be found [here](src/main/java/me/danielml/schooltests/objects/Test.java)

The way the syncer detects a test, is by checking if it has a [valid test type](src/main/java/me/danielml/schooltests/objects/Test.java), and a [valid subject name](src/main/java/me/danielml/schooltests/objects/Subject.java) in the cell string.

It also checks before all the merged region, where they're usually used for tests that are for the whole grade. 

For the normal cells and the merged cells, I also filter them out if the cell string has 
[specific keywords](src/main/java/me/danielml/schooltests/TestManager.java) that usually mean they are not meant to be tests on the schedule, but rather some other event related to it.

After it's loaded all the tests data from the excel file, it compares it to what's already on the database, and removes or adds the changes accordingly
