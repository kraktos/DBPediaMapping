package com.mapper.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mapper.utility.Constants;
import com.mapper.utility.FileUtil;

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
        StandardAnalyzer analyzer = null;
        File indexFilePath = null;

        final File docDir = new File(Constants.DBPEDIA_DATA_DIR);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath()
                + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        logger.info("Start indexing");

        // get a reference to index directory file
        indexFilePath = new File(Constants.DBPEDIA_INDEX_DIR);

        // clear any pre-existing index files
        if(Constants.EMPTY_INDICES)
            FileUtil.emptyIndexDir(indexFilePath);

        analyzer = new StandardAnalyzer(Version.LUCENE_36);
        Directory dir = FSDirectory.open(indexFilePath);

        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        writer = new IndexWriter(dir, iwc);

        // start timer
        long start = System.currentTimeMillis();

        // rename all files
        FileUtil.renameFiles(docDir);

        // start indexing iteratively all files at the location
        indexDocs(writer, docDir);

        writer.forceMerge(1);
        writer.close();

        // end timer
        long end = System.currentTimeMillis();
        long millis = end - start;
        
        String format = String.format(
            "%02d hh: %02d mm: %02d ss",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        
        logger.info("\nINDEXING COMPLETED IN " + format);

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

                Field uriField = null;
                Field labelField = null;
                Field labelCapsField = null;

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
                    while ((strLine = br.readLine()) != null) {
                        // break comma separated line using ","
                        document = new Document();
                        array = strLine.split(",");

                        // display csv values
                        uri = (array[0] != null) ? array[0] : "";
                        uriField = new Field("uriField", uri.trim(), Field.Store.YES, Field.Index.NOT_ANALYZED);
                        document.add(uriField);

                        label = (array[1] != null) ? array[1] : "";
                        labelField = new Field("labelField", label, Field.Store.YES, Field.Index.NOT_ANALYZED);
                        document.add(labelField);

                        label = label.trim().replaceAll(" ", "_").toUpperCase();
                        labelCapsField = new Field("labelCapsField", label, Field.Store.YES, Field.Index.NOT_ANALYZED);
                        document.add(labelCapsField);

                        writer.addDocument(document);
                    }

                } finally {
                    // Close the input stream
                    fstream.close();

                }
            }
        }
    }

}
