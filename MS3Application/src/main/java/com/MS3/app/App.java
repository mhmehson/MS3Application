package com.MS3.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class App {

    private static String inputFileName = "ms3Interview";
    private static int numberOfElements = 10;

    public static void main(String[] args) {
        Connection connection = getDbConnection();
        createTable(connection);
        processCSV(connection);
        closeDbConnection(connection);
    }

    public static void createTable(Connection connection) {

        Statement statement = null;

        // SQL query statements
        String dropTable = "DROP TABLE IF EXISTS transactions";
        String createTable = "CREATE TABLE transactions (A test, B text, C text, D text, E text, F text, G text, H text, I text, J text)";

        try {
            // create statement object
            statement = connection.createStatement();

            // drop table if exists and create table
            statement.executeUpdate(dropTable);
            statement.executeUpdate(createTable);
            statement.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private static void processCSV(Connection connection) {
        Reader reader = null;
        CSVReader csvReader = null;
        PreparedStatement preparedStatement = null;
        List<String[]> invalidRecords = new ArrayList<>();
        String insertIntoTable = "INSERT INTO transactions(A,B,C,D,E,F,G,H,I,J) VALUES(?,?,?,?,?,?,?,?,?,?)";

        int totalRecordsProcessded = 0;
        int validRecordsCount = 0;
        int invalidRecordsCount = 0;

        try {

            // create PreparedStatement - insert SQL statement is parameterized
            preparedStatement = connection.prepareStatement(insertIntoTable);

            // Instantiate CSVReader - use builder to skip header line of CSV
            reader = new BufferedReader(new FileReader("resources/" + inputFileName + ".csv"));
            csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

            // set batch size for insert statements; count tracks total number
            int batchSize = 2000;
            int count = 0;

            // parse csv line by line, adding valid records to batch for insertion and invalid ones to list
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                boolean isValidRecord = true;
                if (line.length != numberOfElements) { // if number of columns exceeds 10, not a valid record
                    isValidRecord = false;
                } else {
                    int index = 1;
                    for (String string : line) {
                        if (string.isEmpty()) { // number of columns is 10, but 1 or more empty, not a valid record
                            isValidRecord = false;
                            break;
                        } else {
                            preparedStatement.setString(index++, string);
                        }
                    }
                }
                // if the record was valid add prepared statement to batch, else add line to list of invalid records
                if (isValidRecord) {
                    preparedStatement.addBatch();
                    validRecordsCount++;
                    count++;
                } else {
                    invalidRecords.add(line);
                    invalidRecordsCount++;
                }
                // if batch of commands meets size requirment, submit batch to db for execution
                if (count == batchSize) {
                    preparedStatement.executeBatch();
                    count = 0;
                }
                totalRecordsProcessded++;
            }
            // last batch of commnds may not meet min requirement, but still needs to be executed
            preparedStatement.executeBatch();
            reader.close();
            csvReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        writeInvalidRecordsToCsv(invalidRecords);
        logStats(totalRecordsProcessded, validRecordsCount, invalidRecordsCount);
    }

    private static void writeInvalidRecordsToCsv(List<String[]> invalidRecords) {
        try {

            File directory = new File("output");
            if (!directory.exists()) {
                directory.mkdir();
            }
            Writer writer = Files.newBufferedWriter(Paths.get("output/" + inputFileName + "-bad.csv"));
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            csvWriter.writeAll(invalidRecords);
            csvWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static Connection getDbConnection() {
      Connection connection = null;
      try {
          connection = DriverManager.getConnection("jdbc:sqlite:database/" + inputFileName + ".db");
      } catch (SQLException sqle) {
          sqle.printStackTrace();
      }
      return connection;
    }

    private static void closeDbConnection(Connection connection) {
      try {
          connection.close();
      } catch (SQLException sqle) {
          sqle.printStackTrace();
      }
    }
    
    private static void logStats(int totalRecordsProcessded, int validRecordsCount, int invalidRecordsCount){
      Logger logger = Logger.getLogger("MyLog");  
      FileHandler fh;  
  
      try {  
  
          // This block configure the logger with handler and formatter  
          fh = new FileHandler("./logs/" + inputFileName + ".log", true);  
          logger.addHandler(fh);
          SimpleFormatter formatter = new SimpleFormatter();  
          fh.setFormatter(formatter);  
  
          // the following statement is used to log any messages  
          logger.info(
            "\n# records received: " + totalRecordsProcessded +
            "\n# records successful: " + validRecordsCount +
            "\n# records failed: " + invalidRecordsCount
            );  

      } catch (SecurityException e) {  
          e.printStackTrace();  
      } catch (IOException e) {  
          e.printStackTrace();  
      }  
    }
}
