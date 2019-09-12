# MS3Application
This repo contains a java application that consumes a csv, parses the data, adds valid records to a SQLite database, writes invalid records to a csv, and logs stats to a log file.

## Steps to run app
1)  Ensure the Java SDK (JDK) is installed on your computer. Use Java SE 8 to avoid any issues.
2)  Ensure Maven is installed on your computer. See https://maven.apache.org/
3)  Ensure Git is installed on your computer. See https://git-scm.com/downloads
4)  Clone the repo locally: git clone https://github.com/mhmehson/MS3Application.git
5)  cd into MS3Applicaiton/MS3Application (make sure you're inside the sub folder MS3Application containing the pom.xml)
6)  In the pom.xml update the compiler version within the "properties" tag to the one installed on your machine (check with       javac -version). Is set to 1.8. Update accordingly.
7)  mvn clean install
8)  mvn exec:java

## Application Info

### Approach and Design Choices:

A) Maven is used as a build tool. It was suggested and having worked with it previously I was already familiar with its usage.

B) After initializing my project with Maven, I decided to look through the instructions to determine if there were any obvious dependencies/libraries I may need for the project. I added the sqlite dependency. After researching csv parsers I chose opencsv as it appeared to be widely used and appeared to be easy to use as well. At this point I put off adding a logging framework as is was something I didn't need immediately and I needed to research the frameworks a bit more.

C) The projector is structured so that initially there is only a "resource" folder containing the input CSV and the "src" folder. When the application runs, an "output" folder is created containing the ms3Interview-bad.csv file, a "database" folder is created containing the ms3Interview.db file, and a "logs" folder is created containing the ms3Interview.log file. 

D) The code first initializes a database. Having worked with Sprng Boot previously, I recalled that Spring Boot typically sets up the database for the application during initialization, so it made sense to do initialize the database first. After all, you can't store records if there is no database setup already to store them into. I called the single table "purchase" (see assumptions for more info). The data type for the columns is text (see Assumption C).

E) Next, the CSV is parsed line-by-line using a CSVReader. Each record is validated, a PreparedStatement is built for inserting the particular record, and the PreparedStatement is added to a batch. PreparedStatements are used (vs Statement) because they improve peformance do to database caching and are reusable when one need to repeat a statement(i.e. repeated inserts). Additionally PrepareStatement allow for parameters and when used in conjunction with the setString method (i.e. not using String concatenation), prevent SQL injection attacks. The PreparedStatements are batched, in order to reduce the number of Database round trip accesses, which should improve performance.

F) Invalid records are written to ./output/ms3Interview-bad.csv. A CSVWriter to write the records to the csv. Also note that BufferedReader and BufferedWriter are used for the CSVReader and CSVWriter. The buffered versions should be more efficient then non-buffered implementations of Reader and Writer.

F) The built in java.util.Logger to log the processing stats to ./logs/ms3Interview.log (see Assumptions F)

### Assumptions:

A) A framework such as Spring Boot may have been overkill for this project, So didn't use it.

B) Input data format does not change. There will always be 10 column headers (labeled A to J). The class variable inputFileName can be updated if the name of the file changes.

C) A record cannot contain more than 10 columns or elements and it cannot be missing any values either. Must have 10 columns and no empty/null elements. 

D) Since SQLite does not support the boolean type, columns H and I are stored as text rather than converting them to 1 and 0.

E) Data appears to represents some sort of purchase or transaction involving money, so the table is called "purchase". Should confirm with the client about what the records represent to ensure the name accurately reflects what is being stored.

F) A log history will be kept (vs overwriting the log file). Stats are appended in the log file and include a date for each log event.

G) java.util.Logger is adequate for this application. This assumes that the application is executed once in a while. If you were processing many CSVs or extensive logging is required throughout the application, then perhaps the performance benefits of a framework such as log4j2 would make it worth using in the application.














  
