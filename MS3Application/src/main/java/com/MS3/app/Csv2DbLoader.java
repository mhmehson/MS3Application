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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class Csv2DbLoader {

    private static String inputFileName = "ms3Interview";
    private static int numberOfColumns = 10;

    public static void main(String[] args) {
        Connection connection = getDbConnection();
        createDbTable(connection);
        processCSV(connection);
        closeDbConnection(connection);
    }

    /**
     * Creates a table in the SQLite database for storing impending records
     *
     * @param connection the connection to the SQLite DB
     */
    public static void createDbTable(Connection connection) {

        Statement statement = null;

        // SQL query statements
        String dropTable = "DROP TABLE IF EXISTS transactions";
        String createTable = "CREATE TABLE transactions (A test, B text, C text, " +
                "D text, E text, F text, G text, H text, I text, J text)";

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

    /**
     * Parses a csv file, inserts valid records into SQLite database, and write invalid records to new csv
     *
     * @param connection the connection to the SQLite DB
     */
    private static void processCSV(Connection connection) {
        CSVReader csvReader = getCSVReader();
        CSVWriter csvWriter = getCSVWriter();
        PreparedStatement preparedStatement = null;
        String insertIntoTable = "INSERT INTO transactions(A,B,C,D,E,F,G,H,I,J) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";

        int totalRecordsProcessded = 0;
        int validRecordsCount = 0;
        int invalidRecordsCount = 0;

        try {
            // create PreparedStatement - insert SQL statement is parameterized
            preparedStatement = connection.prepareStatement(insertIntoTable);

            // batchSize is the batch size threshold, count tracks current batch size
            int batchSize = 1000;
            int count = 0;

            // parse csv line by line, adding valid records to batch for insertion and invalid ones to list
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                boolean isValidRecord = true;
                if (line.length != numberOfColumns) {
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
                // if the record was valid, add insert command to batch, else write record to csv
                if (isValidRecord) {
                    preparedStatement.addBatch();
                    validRecordsCount++;
                    count++;
                } else {
                    csvWriter.writeNext(line);
                    invalidRecordsCount++;
                }
                // if batch of commands meets size requirment, submit batch to db for execution
                if (count == batchSize) {
                    preparedStatement.executeBatch();
                    count = 0;
                }
                totalRecordsProcessded++;
            }
            // last batch of commands may not meet min requirement, but still needs to be executed
            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
        }
        logStats(totalRecordsProcessded, validRecordsCount, invalidRecordsCount);
        closeCSVReader(csvReader);
        closeCSVWriter(csvWriter);
    }

    /**
     * Returns the connection to a SQLite database
     *
     * @return the connection to the SQLite DB
     */
    private static Connection getDbConnection() {

        File directory = new File("database");
        if (!directory.exists()) {
            directory.mkdir();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database/" + inputFileName + ".db");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return connection;
    }

    /**
     * Closed the connection to a database
     *
     * @param connection the connection to the SQLite DB
     */
    private static void closeDbConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Return an instance of CSVReader for reading a CSV file
     *
     * @return a CSVReader instance
     */
    private static CSVReader getCSVReader() {
        Reader reader = null;
        CSVReader csvReader = null;
        try {
            reader = new BufferedReader(new FileReader("resources/" + inputFileName + ".csv"));
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
    private static void closeCSVReader(CSVReader csvReader) {
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
    private static CSVWriter getCSVWriter() {
        File directory = new File("output");
        if (!directory.exists()) {
            directory.mkdir();
        }
        Writer writer = null;
        CSVWriter csvWriter = null;
        try {
            writer = new BufferedWriter(new FileWriter("output/" + inputFileName + "-bad.csv"));
            csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
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
    private static void closeCSVWriter(CSVWriter csvWriter) {
        try {
            csvWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Logs processing stats to a file and console
     *
     * @param totalRecordsProcessed the total number of records that were processed from CSV
     * @param validRecordsCount     the number of records that were valid
     * @param invalidRecordsCount   the number of records that were invalid
     */
    private static void logStats(int totalRecordsProcessded, int validRecordsCount, int invalidRecordsCount) {

        File directory = new File("logs");
        if (!directory.exists()) {
            directory.mkdir();
        }
        Logger logger = Logger.getLogger("Stats_Logger");
        FileHandler fh;

        try {

            // Configer logger with a file handler
            fh = new FileHandler("./logs/" + inputFileName + ".log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // log message
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
