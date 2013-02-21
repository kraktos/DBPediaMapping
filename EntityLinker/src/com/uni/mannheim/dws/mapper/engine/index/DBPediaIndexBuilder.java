package com.uni.mannheim.dws.mapper.engine.index;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

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

    /**
     * Creates index over the DBPedia data located in the directory mentioned in the Constants.java
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
                String strLine = "";
                FileInputStream fstream = null;

                /**
                 * Lucene indexes on particular fields. We crerate two fields for the URI and one for the labels
                 */

                Field uriField = null;
                Field fullUriField = null;
                Field uriTextField1 = null;
                Field uriTextField2 = null;

                Field labelField = null;
                Field labelSmallField = null;
                Field surName = null;
                Field firstName = null;

                String name1 = null;
                String name2 = null;

                String uriFirstWord = null;
                String uriSecondWord = null;

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

                            if (!Utilities.containsNonEnglish(label)) { // do not index Chinese or other such scripts

                                labelField = new StringField("labelField", label.trim(), Field.Store.YES);

                                // index the small cap-ed form of labels

                                // adding the full text as well to increase recall. No need to store it
                                // fullContentField = new StringField("fullContentField", label + uriText,
                                // Field.Store.NO);

                                /*
                                 * if (label.toLowerCase().indexOf("tom cruise") != -1) logger.info(label);
                                 */
                                label = Pattern.compile(Constants.LABEL_FILTER).matcher(label).replaceAll("");

                                if (label.split(" ").length > 0) {
                                    String[] str = label.split(" ");
                                    if (str.length >= 2) {
                                        name1 = str[0];
                                        name2 = str[str.length - 1];
                                    } else {
                                        name1 = str[0];
                                        name2 = name1;
                                    }
                                    // after first and last names are extracted out, join the labels to form one
                                    // un-concatenated word
                                    label = Pattern.compile("[\\s]").matcher(label).replaceAll("");

                                    // add the URI, store it for display purpose
                                    uri = (array[0] != null) ? array[0] : "";
                                    uriField = new StringField("uriField", uri.trim(), Field.Store.YES);

                                    uri =
                                        Pattern.compile(Constants.URI_FILTER)
                                            .matcher(uri.substring(uri.lastIndexOf("/") + 1, uri.length()))
                                            .replaceAll("");
                                    uriText = Pattern.compile("[_]").matcher(uri).replaceAll(" ");

                                    /*
                                     * if (label.toLowerCase().indexOf("born") != -1) {
                                     * logger.info(label.trim().toLowerCase() + "  " + uriText.toLowerCase()); }
                                     */

                                    if (uriText.split(" ").length > 0) {
                                        String[] uriArr = uriText.split(" ");
                                        if (uriArr.length >= 2) {
                                            uriFirstWord = uriArr[0];
                                            uriSecondWord = uriArr[uriArr.length - 1];
                                        } else {
                                            uriFirstWord = uriArr[0];
                                            uriSecondWord = uriFirstWord;
                                        }

                                        labelSmallField =
                                            new StringField("labelSmallField", label.trim().toLowerCase(),
                                                Field.Store.NO);

                                        fullUriField =
                                            new StringField("uriFullTextField", uriText.toLowerCase(), Field.Store.YES);

                                        uriTextField1 =
                                            new StringField("uriTextField1", uriFirstWord.toLowerCase(), Field.Store.NO);
                                        uriTextField2 =
                                            new StringField("uriTextField2", uriSecondWord.toLowerCase(), Field.Store.NO);
                                        surName =
                                            new StringField("surname", name2.trim().toLowerCase(), Field.Store.NO);
                                        firstName =
                                            new StringField("firstname", name1.trim().toLowerCase(), Field.Store.NO);

                                        // add to document
                                        document = new Document();
                                        document.add(uriField);
                                        document.add(fullUriField);
                                        document.add(labelField);
                                        document.add(labelSmallField);
                                        document.add(surName);
                                        document.add(firstName);
                                        document.add(uriTextField1);
                                        document.add(uriTextField2);

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

}
