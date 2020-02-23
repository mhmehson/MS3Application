package com.MS3.app;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

   private final String inputFileName;
   private Connection dbConnection;

   public DatabaseManager(String inputFileName) {
      this.inputFileName = inputFileName;
   }

   /**
    * Initializes a SQLite database and return a connection to the db
    *
    * @return the connection to the database
    */
   public Connection initializeDatabase() {
      File directory = new File("database");
      String url = "jdbc:sqlite:database/" + this.inputFileName + ".db";

      if (!directory.exists()) {
         directory.mkdir();
      }
      this.dbConnection = null;
      try {
         this.dbConnection = DriverManager.getConnection(url);
      } catch (SQLException sqle) {
         sqle.printStackTrace();
      }
      createDbTable();

      return this.dbConnection;
   }

   /**
    * Creates a purchase table in the database
    *
    */
   private void createDbTable() {

      Statement statement = null;

      // SQL query statements
      String dropTable = "DROP TABLE IF EXISTS purchase";
      String createTable = "CREATE TABLE purchase (A test, B text, C text, "
            + "D text, E text, F text, G text, H text, I text, J text)";

      try {
         // create statement object
         statement = this.dbConnection.createStatement();
         // drop table if exists and create table
         statement.executeUpdate(dropTable);
         statement.executeUpdate(createTable);
         statement.close();
      } catch (SQLException sqle) {
         sqle.printStackTrace();
      }
   }

   /**
    * Closes the connection to the database
    */
   public void closeDbConnection() {
      try {
         this.dbConnection.close();
      } catch (SQLException sqle) {
         sqle.printStackTrace();
      }
   }

   /**
    * Getter for retreiving name of the database managed by this DatabaseManager
    *
    * @return the name of the database
    */
   public String getDbName() {
      return this.inputFileName;
   }

   /**
    * Getter for getting connection to the database manageded by this
    * DatabaseManager
    *
    * @return the connection to the database
    */
   public Connection getDbConnection() {
      return this.dbConnection;
   }
}