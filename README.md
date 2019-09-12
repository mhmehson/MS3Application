# MS3Application
This repo contains a java application. The application consumes the csv in the MS3Application/resources folder and inserts valid records into a SQLite database (MS3Application/database/ms3Interview.db). Invalid records are written to MS3Application/output/ms3Interview-bad.csv. Processing stats are logged to MS3Application/logs/ms3Interview.log. The database, output, and logs folders are automatically created at run-time if not already present

Steps to run app:
1)  Ensure the Java SDK (JDK) installed on your computer. This app uses Java SE 8. Compiler version 1.8.

2)  Ensure maven is installed on your computer. 3.6.2 is latest as of this writting
    One Mac:
      Using brew: 
        brew update
        brew install maven
      Manually:
        Download tar from http://maven.apache.org/download.cgi#
        tar xzvf apache-maven-3.6.2-bin.tar.gz
    On Windows:
      See: https://maven.apache.org/guides/getting-started/windows-prerequisites.html

3)  Clone the repo locally : git clone https://github.com/mhmehson/MS3Application.git
4)  After cloning:
a) cd/MS3Applicaiton/MS3Application
b) int pom.xml update the compiler version within <properties> to the one installed on your machine (check with javac -version):
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
</properties>
c) mvn clean install
c) mvn exec:java

Application Info:

Approach and Design Choices:

A) I used Maven as a build tool. It was suggested and having worked with it previously I was already familiar and satisfied with it. 

B) After initializing my project with Maven, I decided to read the instructions to determine if there were any obvious dependencies/libraries I would need for the project. I added the sqlite dependency. After researching csv parsers I chose opencsv as it appeared to be widely used and appeared to be easy to use as well. At this point I put off adding a logging framework as is was something I didn't need immediately and I needed to research the frameworks a bit more.
  i. At the end I decided to use the built in java.util.Logger since I was only logging 1 statement (csv processing stats) and    likely wouldn't have benefited from using a framework such as log4j2. If I were logging extensively throghout the            application, I would definetly use the framework, as it is proven to be optimized for performance. 

D) My code first sets up the database. I called the single table "order" as it looks like from the data that the records represent some sort of tranaction (I discovered i couldn't call it transaction as it is a keyword). Having worked with Sprng Boot previously, I recalled that Spring typically sets up the database for the application during initialization, so it made sense to that here. After all, you can't store records if there is no database setup already to store them into. 

E) Next I parse the csv line by line using CSVReader. I validate a record, build a PreparedStatment for inserting the particular record, and add the PreparedStatement to a batch. I used PreparedStatement (vs Statement) because they improve peformance do tho Db caching and are reusable when you have to repeat a statement(i.e. repeated inserts). Additionally PrepareStatement allow for parameters and when used in conjunction with the setString method (i.e. not using String concatenation), prevent SQL injection attacks. I decided to batch the statements, in order to reduce the number of Database round trip accesses which should greatly improve performance.

F) I should mention, that intially, my code had very few methods. I decided to break up the code as much as a possible so that each method has a more well defined task. I believe this makes the code more readable.

Assumptions:

A) Input data format does not change. There will always be 10 column headers (labeled A to J). The class variable inputFileName can be updated if the name of the file changes.
B) A record cannot contain more than 10 columns or elements and it cannot be missing any values either. Must have 10 columns and no empty/null elements. 
C) Since SQLite does not support the boolean type, columns H and I are stored as text rather then converting them to 1 and 0.
D) Data appears to represents some sort of purchase or money related transaction, so I called the table "order". I'd would confirm with the client about what the records represent to ensure the name accuretly reflects what is being stored.
E) A log history will be kept vs overwriting the log file. Stats are appended in the log file and include a date for each log event.














  
