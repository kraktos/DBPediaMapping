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

import de.unimannheim.informatik.dws.pipeline.en.EnglishSentenceSplitter;

/**
 * Crawl a wikipedia page and retrieves texts, links out of it
 * 
 * @author Arnab Dutta
 */
public class WikiCrawler {

    // define Logger
    static Logger logger = Logger.getLogger(WikiCrawler.class.getName());

    // page title
    private String wikiTitle;

    // DOcument instance of JSoup
    static Document doc = null;

    // Elements instance for the paragraphs
    static Elements paragraphs = null;

    // Elements instance for the links
    static Elements links = null;

    // Elements instance of Jsoup for the links
    static Elements listItems = null;

    /**
     * @param wikiTitle
     */
    public WikiCrawler(String wikiTitle) {
        this.wikiTitle = wikiTitle;

        initCrawler();
    }

    private void initCrawler() {

        try {
            doc = Jsoup.connect(this.wikiTitle).get();
            paragraphs = doc.select(".mw-content-ltr p");
            links = doc.select("a[href]");

            // not using for now
            listItems = doc.select("ul li");

        } catch (IOException e) {
            logger.error("Problem initiating wiki page crawler for " + this.wikiTitle);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        // check input parameters
        if (args.length < 1) {
            logger.info("USAGE: java -jar crawl.jar <uri of the page>");
        } else {
            logger.info("Started processing " + args[0]);

            logger.info(new WikiCrawler(args[0]).getWikiText());

            // logger.info(new WikiCrawler(args[0]).getLinks());

        }
    }

    /**
     * retrieves text from a given wikipedia url
     * 
     * @param wikiPageURI path of wikipedia
     * @return
     */
    public String getWikiText() {

        StringBuilder builder = new StringBuilder();

        // iterate the paragraphs to retrieve the texts
        for (Element element : paragraphs) {
            builder.append(element.text() + "\n");
        }
        return builder.toString();
    }

    /**
     * returns all the links with the list items/bullet points texts in a page
     * 
     * @return String delimited by newline
     */
    public String getListItems() {
        StringBuilder builder = new StringBuilder();

        for (Element listItem : listItems) {
            builder.append(listItem.text() + "\n");
        }
        return builder.toString();
    }

    /**
     * returns all the links with the anchor texts in a page
     * 
     * @return String delimited by newline
     */
    public String getLinks() {
        StringBuilder builder = new StringBuilder();

        for (Element link : links) {
            builder.append(link.attr("abs:href") + "  " + trim(link.text(),
                    35) + "\n");
        }
        return builder.toString();
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }

}
