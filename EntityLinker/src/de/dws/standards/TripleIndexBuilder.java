
package de.dws.standards;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;

/**
 * This class builds an index over any dataset, having S,P,O format of data.
 * 
 * @author Arnab Dutta
 */
public class TripleIndexBuilder
{
    public static Logger logger = Logger.getLogger(TripleIndexBuilder.class.getName());

    // DB connection instance, one per servlet
    static Connection connection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    /**
     * identifier for the type of data set
     * 
     * @author Arnab Dutta
     */
    public enum DATA_SET {
        DBPEDIA, NELL, REVERB;
    }

    public static void main(String[] args) throws Exception {

        PropertyConfigurator
                .configure("/home/arnab/Workspaces/SchemaMapping/EntityLinker/log4j.properties");

        if (args.length != 3) {
            logger.info("USAGE: java -jar <jar file> <path of Data file> <path of index directory> <identifier> \n"
                    +
                    "0: DBPedia dataset\n" +
                    "1: NELL dataset\n" +
                    "2: ReVerb dataset");

        }

        if (args.length == 3) {
            indexer(args[0],
                    args[1], args[2]);
        } else {
            // indexer(Constants.NELL_DATA_PATH, Constants.NELL_ENT_INDEX_DIR);
        }
    }

    /**
     * Creates index over the DBPedia data located in the directory mentioned in
     * the {@link Constants}
     * 
     * @param indexPath
     * @param dataPath
     * @param identifier
     * @throws Exception
     */
    public static void indexer(String dataPath, String indexPath, String identifier)
            throws Exception
    {

        IndexWriter writer = null;
        Analyzer analyzer = null;
        File indexFilePath = null;

        final File docDir = new File(dataPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath()
                    + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        logger.info("Started indexing");

        // get a reference to index directory file
        indexFilePath = new File(indexPath);

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
        indexDocs(writer, docDir, identifier);

        writer.forceMerge(1);
        writer.commit();
        writer.close();

        // end timer
        Utilities.endTimer(start, "INDEXING COMPLETED IN ");
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
     * @param identifier
     * @throws IOException
     */
    private static void indexDocs(IndexWriter writer, File file, String identifier)
            throws IOException
    {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]), identifier);
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
                Field tripleField = null;

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

                        if (!strLine.startsWith("#")) {
                            if (strLine.indexOf(Constants.DBPEDIA_HEADER) == -1) { // NELL/ReVerb
                                                                                   // TRIPLES

                                if (identifier.equals("1")) { // specific
                                                              // processing
                                                              // related to NELL
                                    // break comma separated line using ","
                                    array = strLine.split(Constants.NELL_IE_DELIMIT);
                                    // add the label, store it for display
                                    // purpose
                                    subject = (array[0] != null) ? array[0] : "";
                                    // add the label, store it for display
                                    // purpose
                                    predicate = (array[1] != null) ? array[1] : "";
                                    // add the label, store it for display
                                    // purpose
                                    object = (array[2] != null) ? array[2] : "";

                                    // store the subject field
                                    subjField = new StringField("subjField",
                                            Utilities.cleanse(subject
                                                    .trim()),
                                            Field.Store.NO);
                                    // store the subject field
                                    predField = new StringField("predField",
                                            Utilities.cleanse(predicate.trim()),
                                            Field.Store.NO);
                                    // store the subject field
                                    objField = new StringField("objField", Utilities.cleanse(object
                                            .trim()),
                                            Field.Store.NO);

                                    // store the entire triple as a string
                                    tripleField = new StringField("tripleField",
                                            replaceTags(strLine),
                                            Field.Store.YES);

                                } else if (identifier.equals("2")) { // specific
                                                                     // processing
                                                                     // related
                                                                     // to
                                                                     // ReVerb
                                    logger.info(strLine);
                                    // break semicolon separated line 
                                    array = strLine.split(Constants.REVERB_IE_DELIMIT);
                                    // add the label, store it for display
                                    // purpose
                                    subject = (array[0] != null) ? array[0] : "";
                                    // add the label, store it for display
                                    // purpose
                                    predicate = (array[1] != null) ? array[1] : "";
                                    // add the label, store it for display
                                    // purpose
                                    object = (array[2] != null) ? array[2] : "";

                                    // store the subject field
                                    subjField = new StringField("subjField",
                                            Utilities.removeStopWords(subject
                                                    .trim().toLowerCase()).replaceAll(" 's", "'s").replaceAll(" ", "_"),
                                            Field.Store.NO);
                                    // store the subject field
                                    predField = new StringField("predField",
                                            predicate.trim().toLowerCase().replaceAll(" ", "_"),
                                            Field.Store.NO);
                                    // store the subject field
                                    objField = new StringField("objField",
                                            Utilities.removeStopWords(object
                                                    .trim().toLowerCase()).replaceAll(" 's", "'s").replaceAll(" ", "_"),
                                            Field.Store.NO);

                                    tripleField = new StringField("tripleField",
                                            strLine,
                                            Field.Store.YES);
                                }

                            } else if (strLine.indexOf(Constants.DBPEDIA_HEADER) != -1) { // DBPedia
                                                                                          // TRIPLES

                                // break comma separated line using ","
                                array = strLine.split("\\s");

                                boolean flag = checkIfValidTriple(array[0], array[1], array[2]);
                                if (flag) {

                                    subject = (array[0] != null) ? stripHeaders(array[0]) : "";
                                    predicate = (array[1] != null) ? stripHeaders(array[1]) : "";
                                    object = (array[2] != null) ? stripHeaders(array[2]) : "";

                                    logger.info(subject + ", " + predicate + ", " + object);

                                    // store the subject field
                                    subjField = new StringField("subjField", subject.trim()
                                            .toLowerCase(),
                                            Field.Store.NO);

                                    // store the subject field
                                    predField = new StringField("predField",
                                            predicate.trim().toLowerCase(),
                                            Field.Store.NO);

                                    // store the subject field
                                    objField = new StringField("objField", object.trim()
                                            .toLowerCase(),
                                            Field.Store.NO);

                                    // store the entire triple as a string
                                    tripleField = new StringField("tripleField",
                                            replaceDBPTags(strLine),
                                            Field.Store.YES);

                                }
                            }

                            // add to document
                            document = new Document();
                            document.add(subjField);
                            document.add(predField);
                            document.add(objField);
                            document.add(tripleField);

                            // add the document finally into the
                            // writer
                            writer.addDocument(document);

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

    private static String replaceTags(String arg) {
        arg = arg.replace(">", "");
        arg = arg.replace("<", "");
        arg = arg.replace(" ", ",");
        return arg;
    }

    private static String replaceDBPTags(String arg) {
        arg = arg.replace(">", "");
        arg = arg.replace("<", "");
        arg = arg.replace(" ", Constants.DBPEDIA_DATA_DELIMIT);
        return arg;
    }

    private static boolean checkIfValidTriple(String arg1, String rel, String arg2) {
        if (arg1.contains(Constants.DBPEDIA_HEADER) && rel.contains(Constants.ONTOLOGY_NAMESPACE) &&
                arg2.contains(Constants.DBPEDIA_HEADER))
            return true;
        return false;
    }

}
