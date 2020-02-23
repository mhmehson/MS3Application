package com.MS3.app;

import java.sql.Connection;

public class Ms3Application {

   private static final String INPUT_FILE_NAME = "ms3Interview";

   public static void main(String args[]) {

      // initialize database
      DatabaseManager databaseManager = new DatabaseManager(INPUT_FILE_NAME);
      Connection dbConnection = databaseManager.initializeDatabase();

      // process csv
      Csv2DbLoader csv2DbLoader = new Csv2DbLoader(dbConnection, INPUT_FILE_NAME);
      csv2DbLoader.processCSV();

      databaseManager.closeDbConnection();
   }
}