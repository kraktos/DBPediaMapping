
package de.dws.standards;

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

import de.dws.mapper.dbConnectivity.DBConnection;
import de.dws.mapper.helper.util.Constants;
import de.dws.mapper.helper.util.FileUtil;
import de.dws.mapper.helper.util.Utilities;

/**
 * This class builds an index over the DBPedia data.
 * 
 * @author Arnab Dutta
 */
public class NELLIndexBuilder
{
    public static Logger logger = Logger.getLogger(NELLIndexBuilder.class.getName());

    // DB connection instance, one per servlet
    static Connection connection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    public static void main(String[] args) throws Exception {
        indexer();
    }

    /**
     * Creates index over the DBPedia data located in the directory mentioned in
     * the {@link Constants}
     * 
     * @throws Exception
     */
    public static void indexer() throws Exception
    {

        IndexWriter writer = null;
        Analyzer analyzer = null;
        File indexFilePath = null;

        final File docDir = new File(Constants.NELL_DATA_PATH);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath()
                    + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        logger.info("Started indexing");

        // get a reference to index directory file
        indexFilePath = new File(Constants.NELL_ENT_INDEX_DIR);

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
     * Indexes the given file using the given writer, or if a directory is
     * given, recurses over files and directories found under the given
     * directory. NOTE: This method indexes one document per input file. This is
     * slow. For good throughput, put multiple documents into your input
     * file(s). An example of this is in the benchmark module, which can create
     * "line doc" files, one document per line, using the <a href=
     * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     * 
     * @param writer Writer to the index where the given file/dir info will be
     *            stored
     * @param file The file to index, or the directory to recurse into to find
     *            files to index
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
                 * Lucene indexes on particular fields. We create two fields for
                 * the URI and one for the labels
                 */

                Field subjField = null;
                Field predField = null;
                Field objField = null;

                try {
                    fstream = new FileInputStream(file);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    // create document object
                    Document document = null;
                    String[] array = null;

                    String subject = null;
                    String predicate = null;
                    String object = null;

                    // read comma separated file line by line
                    while ((strLine = br.readLine()) != null) {

                        // break comma separated line using ","
                        array = strLine.split(Constants.NELL_IE_DELIMIT);

                        // add the label, store it for display purpose
                        subject = (array[1] != null) ? array[1] : "";

                        // store the subject field
                        subjField = new StringField("subjField", subject.trim().toLowerCase(),
                                Field.Store.YES);

                        // add the label, store it for display purpose
                        predicate = (array[2] != null) ? array[2] : "";

                        // store the subject field
                        predField = new StringField("predField", predicate.trim().toLowerCase(),
                                Field.Store.YES);

                        // add the label, store it for display purpose
                        object = (array[3] != null) ? ((array[3].length() == 0) ? array[4]
                                : array[3]) : "";

                        // store the subject field
                        objField = new StringField("objField", object.trim().toLowerCase(),
                                Field.Store.YES);

                        // add to document
                        document = new Document();
                        document.add(subjField);
                        document.add(predField);
                        document.add(objField);

                        // add the document finally into the
                        // writer
                        writer.addDocument(document);

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

    private static int calculateFrequency(String label, String uri, String labelFirst,
            String labelSecond,
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

            logger.info(uri); // labelFirst + " " + labelSecond + " " + uriFirst
                              // + " " + uriSecond);
            dbResults = computeWikiStats(labelFirst, dbResults, pstmt);
            dbResults = computeWikiStats(labelSecond, dbResults, pstmt);
            dbResults = computeWikiStats(uriFirst, dbResults, pstmt);
            dbResults = computeWikiStats(uriSecond, dbResults, pstmt);

            for (String result : dbResults) {
                int score = StringUtils.getLevenshteinDistance(result.toLowerCase(),
                        uri.toLowerCase());
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

    private static List<String> computeWikiStats(String userQuery, List<String> dbResults,
            PreparedStatement pstmt)
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
