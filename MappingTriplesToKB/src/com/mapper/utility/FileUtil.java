package com.mapper.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.csvreader.CsvWriter;
import com.mapper.dataObjects.PredicatesDAO;
import com.mapper.dbConnectivity.DBClient;

/**
 * Class to peform IO operations
 * 
 * @author Arnab Dutta
 */
public class FileUtil
{

    static Logger logger = Logger.getLogger(FileUtil.class.getName());

    /**
     * @param strTokens array of Strings, the values to be written in each row
     * @param csvOutput the reference to the output CSV file
     */
    public static void writeToCSV(final String[] strTokens, final CsvWriter csvOutput)
    {

        try {

            // write out the records
            csvOutput.write(strTokens[0]);// subject
            csvOutput.write(strTokens[1]);// predicate
            csvOutput.write(strTokens[2]);// object
            csvOutput.write(strTokens[4]);// truth value: Required later for
                                          // aposteriori probability
                                          // calculation
            csvOutput.endRecord();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a piece of text to a writer object
     * 
     * @param out writer object to write the output
     * @param text the text to write
     */
    public static void writeToFlatFile(BufferedWriter out, String text)
    {
        try {
            // Create file
            out.write(text);

        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * @param userQuery
     * @param file
     * @param outProperty
     */
    public static void extractMatchingTuples(String userQuery, File file, BufferedWriter outProperty)
    {
        Scanner input;

        try {

            input = new Scanner(file);
            while (input.hasNext()) {
                String word = input.next();
                if (word.toLowerCase().contains(userQuery.toLowerCase())) {
                    // Take these subset of data and match them against DBPedia
                    // store them somehow and match the predicates
                    // writeToFlatFile(outProperty,
                    // Utilities.extractPredicatesFromTuples(word) + "\n");

                    writeToFlatFile(outProperty, word + "\n");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readIEFile(String ieOutputCsvFilePath) throws IOException
    {
        String strLine = null;
        String[] stArr = null;

        // create BufferedReader to read csv file
        BufferedReader br = new BufferedReader(new FileReader(ieOutputCsvFilePath));

        // int count = 0;
        // Fact fact = null;
        // read each facts
        while ((strLine = br.readLine()) != null) {

            // break comma separated line using one or more tabs
            stArr = strLine.split(Constants.DELIMIT_IE_FILE);

            // feed these cleansed ;iterals to the search engine to find best matches
            DBClient.findMatches(stArr[0], stArr[1], stArr[2]);

            stArr[0] = null;
            stArr[1] = null;
            stArr[2] = null;

            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

    }

    /**
     * method to rename a bunch of files in a directory
     * 
     * @param docDir Location of directory where the files are to be renamed
     */
    public static void renameFiles(File docDir)
    {

        String[] dir = docDir.list();
        java.util.Arrays.sort(dir);
        File arrFiles[] = docDir.listFiles();
        int numOfFilesInDir = dir.length;

        for (int fileCounter = 0; fileCounter < numOfFilesInDir; fileCounter++) {
            String oldFileName = arrFiles[fileCounter].getName();

            if (!oldFileName.contains(".csv")) {
                int j = oldFileName.indexOf(" ");
                oldFileName = (j == -1) ? oldFileName : oldFileName.substring(0, j);
                String newname = oldFileName + "_" + System.currentTimeMillis() + ".csv";
                File newFileName = new File(arrFiles[fileCounter].getParentFile(), newname);
                arrFiles[fileCounter].renameTo(newFileName);
            }
        }
    }

    /**
     * Clear any files in a directory
     * 
     * @param file directory path to be cleared
     * @throws InterruptedException
     */
    public static void emptyIndexDir(File file) throws InterruptedException
    {
        File[] finlist = file.listFiles();
        for (int n = 0; n < finlist.length; n++) {
            if (finlist[n].isFile()) {
                System.gc();
                Thread.sleep(20);
                finlist[n].delete();
            }
        }
    }
    
    /**
     * flushes to the output writer object
     * 
     * @param bufferedWriter
     * @param iePredicate
     * @param predDaoArr
     * @throws IOException
     */
    public static void dumpToFile(BufferedWriter bufferedWriter, String iePredicate, PredicatesDAO[] predDaoArr)
        throws IOException
    {
        try {
            bufferedWriter.write(iePredicate + "->" + predDaoArr[0].toString() + "," + predDaoArr[1].toString() + "\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            bufferedWriter.write(iePredicate + "->" + predDaoArr[0].toString() + "\n");
        }
    }
}
