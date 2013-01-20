package com.mapper.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

import com.mapper.utility.Constants;
import com.mapper.utility.FileUtil;
import com.mapper.utility.Utilities;

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
        indexFilePath = new File(Constants.DBPEDIA_INDEX_DIR);

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
                Field labelField = null;
                Field labelSmallField = null;
                Field fullContentField = null;

                try {
                    fstream = new FileInputStream(file);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    // create document object
                    Document document = null;
                    String[] array = null;
                    String uri = null;
                    String label = null;

                    // read comma separated file line by line
                    while ((strLine = br.readLine()) != null && strLine.startsWith(Constants.DBPEDIA_HEADER)) {
                        logger.debug(strLine);
                        // break comma separated line using ","
                        document = new Document();
                        array = strLine.split(Constants.DBPEDIA_DATA_DELIMIT);

                        // add the URI, store it for display purpose
                        uri = (array[0] != null) ? array[0] : "";
                        uriField = new StringField("uriField", uri.trim(), Field.Store.YES);
                        document.add(uriField);

                        // add the label, store it for display purpose
                        label = (array[1] != null) ? array[1] : "";
                        labelField = new StringField("labelField", label.trim(), Field.Store.YES);
                        document.add(labelField);

                        // add the label in small caps form, do not store it for display purpose
                        // This is a work around since Lucene is inherently case sensitive,
                        // hence search for "Tom", "TOM" or "TOm" won't match to the actual term "Tom" stored in the
                        // index
                        // list
                        labelSmallField =
                            new StringField("labelSmallField", label.trim().toLowerCase(), Field.Store.NO);
                        document.add(labelSmallField);

                        // adding the full text as well to increase recall. No need to store it
                        fullContentField = new StringField("fullContentField", label + uri, Field.Store.NO);
                        document.add(fullContentField);

                        // add the document finally into the writer
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

}
