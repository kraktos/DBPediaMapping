/**
 * 
 */

package de.dws.standards;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Timer;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * creates a surface form of DBPedia concepts. Supposedly cleaner than Wikilinks
 * data set !!
 * 
 * @author Arnab Dutta
 */
public class SurfaceFormCreator {

    // define Logger
    static Logger logger = Logger.getLogger(SurfaceFormCreator.class.getName());
    private static int cntr = 0;

    private static String MAGIC_DELIMITER = "---";
    private static String SCORE_NAME = "score#sfGivenUri";
                                                          // //
                                                          // "score#uriGivenSf";

    // global timer
    private static long timer = 0;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        processLexicalizationPairs(args[0],
                30000);

    }

    private static void processLexicalizationPairs(String filePath, int dataLimit)
            throws IOException {

        String[] arr = null;

        String[] pair = null;
        String uri = null;
        String sf = null;
        double sfGivenUri = 0D;
        
        // initiate timer
        Timer timerObj = new Timer();

        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        if (tupleReader != null) {
            String tripleFromDBPedia;

            // create a DB routine to fetch the surface forms of the arguments
            DBWrapper.init(Constants.INSERT_DB_SURFACE_FORMS_SQL);

            // read a random triple at a time
            while ((tripleFromDBPedia = tupleReader.readLine()) != null) {

                try {
                    arr = tripleFromDBPedia.split("\\s");
                    // if (validRow(arr[0], arr[1])) {
                    pair = splitter(arr[0]);
                    uri = pair[0];
                    sf = pair[1];

                    sfGivenUri = cleanse(arr[2]);
                    logger.debug(arr[0] + "  " + uri + "  " + sf + " " + sfGivenUri);

                    DBWrapper.saveSurfaceForms(
                            "http://dbpedia.org/resource/" +
                                    Utilities.utf8ToCharacter(uri),
                            Utilities.utf8ToCharacter(sf), sfGivenUri);

                    cntr++;
                    if ((cntr % Constants.BATCH_SIZE) == 0) {
                        timer = timer + timerObj.tick();
                        logger.info(cntr + " " + timer + " millsecds" + arr[0]);
                    }
                    // if (cntr > dataLimit)
                    // break;

                } catch (Exception e) {
                    logger.info("error processing " + tripleFromDBPedia);

                    continue;
                }

                // }

            }

            // write to DB residual tuples
            DBWrapper.saveResidualSFs();

            // shutdown DB
            DBWrapper.shutDown();
        }
    }

    private static double cleanse(String prob) {
        // System.out.println(prob);
        prob = prob.substring(0, prob.indexOf("^^")).replaceAll("\"", "");
        return Double.parseDouble(prob);
    }

    private static String[] splitter(String row) {
        row = row.replace("<http://dbepdia.org/spotlight/id/", "");
        row = row.replace(">", "");
        return row.split(MAGIC_DELIMITER);
    }

    private static boolean validRow(String row, String score) {
        if (row.indexOf(MAGIC_DELIMITER) != -1 &&
                score.indexOf(SCORE_NAME) != -1) {
            return true;
        }
        return false;
    }
}
