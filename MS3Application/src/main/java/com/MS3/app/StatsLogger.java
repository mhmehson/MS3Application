package com.MS3.app;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class StatsLogger {
   /**
    * Logs processing stats to a file and console
    *
    * @param totalRecordsProcessed the total number of records that were processed
    *                              from CSV
    * @param validRecordsCount     the number of records that were valid
    * @param invalidRecordsCount   the number of records that were invalid
    * @param inputFileName         the name of the input file
    * 
    */
   public static void logStats(int totalRecordsProcessed, int validRecordsCount, int invalidRecordsCount,
         String inputFileName) {

      FileHandler fh = null;
      File directory = new File("logs");
      String fileName = "./logs/" + inputFileName + ".log";

      Logger logger = Logger.getLogger("Stats Logger");
      logger.setUseParentHandlers(false); // want to disable console output

      if (!directory.exists()) {
         directory.mkdir();
      }
      try {
         // Configer logger with a file handler
         fh = new FileHandler(fileName, true);
         logger.addHandler(fh);
         SimpleFormatter formatter = new SimpleFormatter();
         fh.setFormatter(formatter);

         // log message
         logger.info("\n# records received: " + totalRecordsProcessed + "\n# records successful: " + validRecordsCount
               + "\n# records failed: " + invalidRecordsCount);
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}