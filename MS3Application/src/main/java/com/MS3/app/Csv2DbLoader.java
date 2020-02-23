package com.MS3.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class Csv2DbLoader {
   private final int NUMBER_OF_COLUMNS = 10;
   private final String inputFileName;
   private final Connection dbConnection;

   public Csv2DbLoader(Connection dbConnection, String inputFileName) {
      this.dbConnection = dbConnection;
      this.inputFileName = inputFileName;
   }

   /**
    * Parses a csv file, inserts valid records into SQLite database, and write
    * invalid records to new csv
    *
    */
   public void processCSV() {
      CSVReader csvReader = getCSVReader();
      CSVWriter csvWriter = getCSVWriter();
      PreparedStatement preparedStatement = null;
      String insertIntoTable = "INSERT INTO purchase(A,B,C,D,E,F,G,H,I,J) " + "VALUES(?,?,?,?,?,?,?,?,?,?)";

      int totalRecordsProcessed = 0;
      int validRecordsCount = 0;
      int invalidRecordsCount = 0;

      try {
         // create PreparedStatement - insert SQL statement is parameterized
         preparedStatement = dbConnection.prepareStatement(insertIntoTable);

         // variables for batching insert commands - batching is more efficient than
         // individual inserts
         int batchSizeThreshold = 1000;
         int batchSize = 0;

         // parse csv line by line, adding valid records to batch for insertion and
         // invalid ones to list
         String[] line;
         while ((line = csvReader.readNext()) != null) {
            boolean isValidRecord = true;
            if (line.length != NUMBER_OF_COLUMNS) {
               isValidRecord = false;
            } else {
               int index = 1;
               for (String string : line) {
                  if (string.isEmpty()) { // number of columns is 10, but at least 1 empty = invalid record
                     isValidRecord = false;
                     break;
                  } else {
                     preparedStatement.setString(index++, string);
                  }
               }
            }
            // if the record was valid, add insert command to batch, else write record to
            // csv
            if (isValidRecord) {
               preparedStatement.addBatch();
               validRecordsCount++;
               batchSize++;
            } else {
               csvWriter.writeNext(line);
               invalidRecordsCount++;
            }
            if (batchSize == batchSizeThreshold) {
               preparedStatement.executeBatch();
               batchSize = 0;
            }
            totalRecordsProcessed++;
         }
         // last batch of commands may not meet threshold requirement, but still needs to
         // be executed
         preparedStatement.executeBatch();
         preparedStatement.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      } catch (SQLException sqle) {
         sqle.printStackTrace();
      } finally {
      }

      closeCSVReader(csvReader);
      closeCSVWriter(csvWriter);

      this.logResults(totalRecordsProcessed, validRecordsCount, invalidRecordsCount);
   }

   /**
    * Returns a CSVReader instance for reading a CSV file
    *
    * @return a CSVReader instance
    */
   private CSVReader getCSVReader() {
      Reader reader = null;
      CSVReader csvReader = null;
      String fileName = "resources/" + this.inputFileName + ".csv";
      try {
         reader = new BufferedReader(new FileReader(fileName));
         csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
      } catch (FileNotFoundException fnfe) {
         fnfe.printStackTrace();
      }

      return csvReader;
   }

   /**
    * Closes a CSVReader instance
    *
    * @param csvReader CSVReader instance that needs to be closed
    */
   private void closeCSVReader(CSVReader csvReader) {
      try {
         csvReader.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
   }

   /**
    * Returns a CSVWriter instance for writing to CSV file
    *
    * @return a CSVWriter instance
    */
   private CSVWriter getCSVWriter() {
      File directory = new File("output");
      Writer writer = null;
      CSVWriter csvWriter = null;
      String fileName = "output/" + this.inputFileName + "-bad.csv";

      if (!directory.exists()) {
         directory.mkdir();
      }
      try {
         writer = new BufferedWriter(new FileWriter(fileName));
         csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
               CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
      return csvWriter;
   }

   /**
    * Closes a CSVWriter instance
    *
    * @param csvWriter CSVWriter instance that needs to be closed
    */
   private void closeCSVWriter(CSVWriter csvWriter) {
      try {
         csvWriter.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
   }

   /**
    * Used StatsLogger to log results to file (/logs/ms3Interview.log)
    *
    * @param totalRecordsProcessed total records read from csv
    * @param validRecordsCount     total number of valid records
    * @param invalidRecordsCount   total number of invalid records
    * 
    */
   private void logResults(int totalRecordsProcessed, int validRecordsCount, int invalidRecordsCount) {
      StatsLogger.logStats(totalRecordsProcessed, validRecordsCount, invalidRecordsCount, this.inputFileName);
   }
}
