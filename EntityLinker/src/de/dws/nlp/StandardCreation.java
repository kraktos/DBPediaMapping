/**
 * 
 */

package de.dws.nlp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.mapper.helper.util.Constants;

/**
 * @author Arnab Dutta
 */
public class StandardCreation {

    // define Logger
    static Logger logger = Logger.getLogger(StandardCreation.class.getName());

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // check input parameters
        if (args.length < 1) {
            logger.error("Please provide the path for the input file.");
        } else {

            processFile(args[0]);
        }

    }

    /**
     * Takes as input an Wikipedia infobox extracted data
     * 
     * @param filePath
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void processFile(String filePath) throws FileNotFoundException, IOException {
        String[] arr = null;
        boolean flag = false;

        BufferedReader tupleReader = new BufferedReader(new FileReader(filePath));

        if (tupleReader != null) {
            String tupleFromIE;

            // create a DB routine to fetch the surface forms of the arguments
            DBWrapper.init(Constants.GET_WIKI_SURFACE_FORMS_SQL);

            long cntr = 0;
            // read a random triple at a time
            while ((tupleFromIE = tupleReader.readLine()) != null) {
                arr = tupleFromIE.split("\\s");
                flag = checkIfValidTriple(arr[0], arr[1], arr[2]);
                if (flag) {

                    // use this high quality ground fact to generate possible
                    // facts
                    List<String> results = findSurfaceForms(stripHeaders(arr[0]),
                            stripHeaders(arr[1]),
                            stripHeaders(arr[2]));

                    if (results.size() == 0)
                    {
                        logger.info(tupleFromIE);
                        logger.info(results);
                        cntr++;
                    }

                }
            } // end of while

            logger.info("Un mapped = " + cntr + " entities");
        }
    }

    /**
     * remove the header information. We are interested only in the concept name
     * 
     * @param arg full URI of the concept
     * @return stripped concept name
     */
    private static String stripHeaders(String arg) {
        arg = arg.replace("<http://dbpedia.org/resource/", "");
        arg = arg.replace("<http://dbpedia.org/ontology/", "");
        arg = arg.replace(">", "");
        arg = arg.replace("%", "");

        return arg;
    }

    /**
     * Figure out what are the different ways of referring this subject or
     * object. Basically, fetch from wikipedia data dumps which anchors are
     * leading to the page
     * 
     * @param arg1
     * @param rel
     * @param arg2
     * @return
     */
    private static List<String> findSurfaceForms(String arg1, String rel, String arg2) {

        return DBWrapper.fetchSurfaceForms(arg1);

    }

    /**
     * Skipping any datatype values. Only entities i.e something with
     * "http://dbpedia.org/" header
     * 
     * @param tupleFromIE
     * @param arr2
     * @param arr
     * @return
     */
    private static boolean checkIfValidTriple(String arg1, String rel, String arg2) {
        if (arg1.contains(Constants.DBPEDIA_HEADER) && rel.contains(Constants.ONTOLOGY_NAMESPACE) &&
                arg2.contains(Constants.DBPEDIA_HEADER))
            return true;
        return false;
    }

}
