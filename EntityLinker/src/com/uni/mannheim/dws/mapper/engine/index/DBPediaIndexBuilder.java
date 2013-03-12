package com.uni.mannheim.dws.mapper.engine.index;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.uni.mannheim.dws.mapper.dbConnectivity.DBConnection;
import com.uni.mannheim.dws.mapper.helper.util.Constants;
import com.uni.mannheim.dws.mapper.helper.util.FileUtil;
import com.uni.mannheim.dws.mapper.helper.util.Utilities;

/**
 * This class builds an index over the DBPedia data.
 * 
 * @author Arnab Dutta
 */
public class DBPediaIndexBuilder
{
    public static Logger logger = Logger.getLogger(DBPediaIndexBuilder.class.getName());

    // DB connection instance, one per servlet
    static Connection connection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    /**
     * Creates index over the DBPedia data located in the directory mentioned in the {@link Constants}
     * 
     * @throws Exception
     */
    public static void indexer() throws Exception
    {

        IndexWriter writer = null;
        Analyzer analyzer = null;
        File indexFilePath = null;

        final File docDir = new File(Constants.DBPEDIA_DATA_DIR);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath()
                + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        logger.info("Started indexing");

        // get a reference to index directory file
        indexFilePath = new File(Constants.DBPEDIA_ENT_INDEX_DIR);

        // clear any pre-existing index files
        if (Constants.EMPTY_INDICES)
            FileUtil.emptyIndexDir(indexFilePath);

        analyzer = Constants.LUCENE_ANALYZER;
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // create a directory of the Indices
        Directory indexDirectory = FSDirectory.open(indexFilePath);

        // create a Index writer
        writer = new IndexWriter(indexDirectory, iwc);

        // start timer
        long start = Utilities.startTimer();

        // start indexing iteratively all files at the location
        indexDocs(writer, docDir);

        writer.forceMerge(1);
        writer.commit();
        writer.close();

        // end timer
        Utilities.endTimer(start, "INDEXING COMPLETED IN ");
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given, recurses over files and directories
     * found under the given directory. NOTE: This method indexes one document per input file. This is slow. For good
     * throughput, put multiple documents into your input file(s). An example of this is in the benchmark module, which
     * can create "line doc" files, one document per line, using the <a href=
     * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     * 
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param file The file to index, or the directory to recurse into to find files to index
     * @throws IOException
     */
    private static void indexDocs(IndexWriter writer, File file) throws IOException
    {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {

                logger.info("Indexing " + file.getAbsolutePath());
                String strLine = null;
                FileInputStream fstream = null;

                /**
                 * Lucene indexes on particular fields. We create two fields for the URI and one for the labels
                 */

                Field uriField = null;
                Field fullUriField = null;
                Field uriFirstTextField = null;
                Field uriSecondTextField = null;

                Field labelField = null;
                Field labelSmallField = null;
                Field surName = null;
                Field firstName = null;

                //Field highFrequency = null;

                String labelFirstName = null;
                String labelLastName = null;

                String uriFirstWord = null;
                String uriSecondWord = null;

                //int isFrequent = 0;

                try {
                    fstream = new FileInputStream(file);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    // create document object
                    Document document = null;
                    String[] array = null;
                    String uri = null;
                    String uriText = null;
                    String label = null;

                    // read comma separated file line by line
                    while ((strLine = br.readLine()) != null) {

                        if (strLine.startsWith(Constants.DBPEDIA_HEADER)) {
                            // break comma separated line using ","
                            array = strLine.split(Constants.DBPEDIA_DATA_DELIMIT);

                            // add the label, store it for display purpose
                            label = (array[1] != null) ? array[1] : "";

                            // do not index Chinese or other such scripts
                            if (!Utilities.containsNonEnglish(label)) {

                                // store the label field
                                labelField = new StringField("labelField", label.trim(), Field.Store.YES);

                                // replace all the punctuation marks in the label
                                label = Pattern.compile(Constants.LABEL_FILTER).matcher(label).replaceAll("");

                                // extract the first and last names from the label
                                if (label.split(" ").length > 0) {
                                    String[] str = label.split(" ");
                                    if (str.length >= 2) {
                                        labelFirstName = str[0];
                                        labelLastName = str[str.length - 1];
                                    } else {
                                        labelFirstName = str[0];
                                        labelLastName = labelFirstName;
                                    }
                                    // after first and last names are extracted out, join the labels to form one
                                    // un-concatenated word
                                    label = Pattern.compile("[\\s]").matcher(label).replaceAll("");

                                    // add the URI, store it for display purpose
                                    uri = (array[0] != null) ? array[0] : "";
                                    uriField = new StringField("uriField", uri.trim(), Field.Store.YES);

                                    if (uri.indexOf("Michelsen") != -1) {
                                        logger.info("");
                                    }

                                    uri =
                                        Pattern.compile(Constants.URI_FILTER)
                                            .matcher(uri.substring(uri.lastIndexOf("/") + 1, uri.length()))
                                            .replaceAll("");
                                    uriText = Pattern.compile("[_]").matcher(uri).replaceAll(" ");

                                    if (uriText.split(" ").length > 0) {
                                        String[] uriArr = uriText.split(" ");
                                        if (uriArr.length >= 2) {
                                            uriFirstWord = uriArr[0];
                                            uriSecondWord = uriArr[uriArr.length - 1];
                                        } else {
                                            uriFirstWord = uriArr[0];
                                            uriSecondWord = uriFirstWord;
                                        }
                                        uriText = Pattern.compile("[\\s]").matcher(uriText).replaceAll("");

                                        /*isFrequent =
                                            calculateFrequency(label, Pattern.compile("[\\s]").matcher(uriText)
                                                .replaceAll("_"), labelFirstName.trim().toLowerCase(), labelLastName
                                                .trim().toLowerCase(), uriFirstWord.toLowerCase(),
                                                uriSecondWord.toLowerCase());*/

                                        // define all the fields to be indexed
                                        labelSmallField =
                                            new StringField("labelSmallField", label.trim().toLowerCase(),
                                                Field.Store.NO);

                                        fullUriField =
                                            new StringField("uriFullTextField", uriText.toLowerCase(), Field.Store.YES);

                                        uriFirstTextField =
                                            new StringField("uriTextField1", uriFirstWord.toLowerCase(), Field.Store.NO);
                                        uriSecondTextField =
                                            new StringField("uriTextField2", uriSecondWord.toLowerCase(),
                                                Field.Store.NO);
                                        surName =
                                            new StringField("surname", labelLastName.trim().toLowerCase(),
                                                Field.Store.NO);
                                        firstName =
                                            new StringField("firstname", labelFirstName.trim().toLowerCase(),
                                                Field.Store.NO);

                                        /*highFrequency =
                                            new StringField("isHighFreq", String.valueOf(isFrequent), Field.Store.YES);
*/
                                        /*
                                         * logger.info("\nuriTextField1 " + uriFirstWord.toLowerCase() +
                                         * "\nuriTextField2 " + uriSecondWord.toLowerCase() + "\nuriFullTextField " +
                                         * uriText.toLowerCase() + "\nfirstname  " + labelFirstName.trim().toLowerCase()
                                         * + "\nsurname " + labelLastName.trim().toLowerCase() + "\nlabelSmallField " +
                                         * label.trim().toLowerCase());
                                         */
                                        // uriText = Pattern.compile("[\\s]").matcher(uriText).replaceAll("_");

                                        // add to document
                                        document = new Document();
                                        document.add(uriField);
                                        document.add(fullUriField);
                                        document.add(labelField);
                                        document.add(labelSmallField);
                                        document.add(surName);
                                        document.add(firstName);
                                        document.add(uriFirstTextField);
                                        document.add(uriSecondTextField);
                                        //document.add(highFrequency);

                                        // add the document finally into the writer
                                        writer.addDocument(document);
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.error(ex.getMessage() + " while reading  " + strLine);

                } finally {
                    // Close the input stream
                    fstream.close();
                }
            }
        }
    }

    private static int calculateFrequency(String label, String uri, String labelFirst, String labelSecond,
        String uriFirst, String uriSecond)
    {
        List<String> dbResults = new ArrayList<String>();

        try {
            // instantiate the DB connection
            DBConnection dbConnection = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbConnection.getConnection();

            // create a statement
            pstmt = connection.prepareStatement(Constants.GET_WIKI_STAT);

            logger.info(uri); // labelFirst + " " + labelSecond + " " + uriFirst + " " + uriSecond);
            dbResults = computeWikiStats(labelFirst, dbResults, pstmt);
            dbResults = computeWikiStats(labelSecond, dbResults, pstmt);
            dbResults = computeWikiStats(uriFirst, dbResults, pstmt);
            dbResults = computeWikiStats(uriSecond, dbResults, pstmt);

            for (String result : dbResults) {
                int score = StringUtils.getLevenshteinDistance(result.toLowerCase(), uri.toLowerCase());
                if (score < 2) {
                    return score;
                }
            }
        } catch (SQLException e) {
            logger.error(" Exception while computing wiki stats " + e.getMessage());
        } finally {
            try {
                pstmt.clearParameters();
                pstmt.close();
                connection.close();

            } catch (SQLException e) {
                logger.error(" Exception while closing DB " + e.getMessage());
            }
        }
        return -1;

    }

    private static List<String> computeWikiStats(String userQuery, List<String> dbResults, PreparedStatement pstmt)
        throws SQLException
    {
        java.sql.ResultSet rs = null;
        try {
            pstmt.setString(1, userQuery);
            pstmt.setString(2, userQuery);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                String entity = rs.getString("entity");
                String count = rs.getString("cnt");
                if (!dbResults.contains(entity)) {
                    dbResults.add(entity);
                    // logger.info(entity + "  " + count);
                }

            }
        } catch (SQLException e) {
            logger.error(" Exception while computing wiki stats " + e.getMessage());
        } finally {
            rs.close();
        }

        return dbResults;
    }
}
