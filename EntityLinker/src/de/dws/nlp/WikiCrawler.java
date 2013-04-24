/**
 * 
 */

package de.dws.nlp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Arnab Dutta
 */
public class WikiCrawler {

    // define Logger
    static Logger logger = Logger.getLogger(WikiCrawler.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {

        // check input parameters
        if (args.length < 1) {
            logger.info("USAGE: java -jar runner.jar <uri of the page>");
        } else {
            logger.info("Starting processing " + args[0]);
            loadHTML(args[0]);
        }

    }

    /**
     * retrieves text from a given wikipedia url
     * 
     * @param wikiPageURI path of wikipedia
     */
    public static void loadHTML(final String wikiPageURI) {
        Document doc = null;
        Elements paragraphs = null;

        try {
            doc = Jsoup.connect(wikiPageURI).get();
            paragraphs = doc.select(".mw-content-ltr p");

            // iterate the paragraphs to retrieve the texts
            for (Element element : paragraphs) {
                logger.info(element.text());
            }
        } catch (IOException e) {
            logger.error("Exception while parsing " + wikiPageURI + "  " + e.getMessage());
        }
    }

}
