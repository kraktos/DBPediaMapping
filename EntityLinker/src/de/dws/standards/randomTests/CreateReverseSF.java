/**
 * 
 */

package de.dws.standards.randomTests;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class CreateReverseSF {

    // define Logger
    static Logger logger = Logger.getLogger(CreateReverseSF.class.getName());

    private static final String DB_NAME_SOURCE_GS = "goldStandardClean";

    // set of nell subjects and objects and dbpedia URIs they point to
    private static final String ALL_CANON_ENTITIES = "select E_SUB as a from " +
            DB_NAME_SOURCE_GS +
            " union select E_OBJ as a from " +
            DB_NAME_SOURCE_GS;

    // a DBPedia URI, Nell concept pair with a count of their occurrence
    // frequency
    private static final List<String> LIST_SURFACE_FORMS = new ArrayList<String>();

    private static final String GET_WIKI_TITLES_SQL = "select  t.title, count(*) as cnt from link_anchors l, title_2_id t where l.anchor=? and l.target=t.id group by t.title order by cnt desc limit ?";

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator
                .configure("resources/log4j.properties");

        // load the pairs of nell concepts and DBP uris
        loadCanonicalForm();

        fetchTopKURI();
    }

    private static void fetchTopKURI() {
        List<String> uris = null;

        DBWrapper.init(GET_WIKI_TITLES_SQL);

        for (String form : LIST_SURFACE_FORMS) {
            uris = DBWrapper.fetchTopKWikiTitles(Utilities.cleanse(form).replaceAll(
                    "_", " "), 3);
            for (String uri : uris) {                
                logger.info(form + "\t" + uri.split("~~")[0] + "\t"+ uri.split("~~")[1]);
            }
        }

    }

    /**
     * load the pairs of NELL concepts and DBP uris
     */
    private static void loadCanonicalForm() {

        DBWrapper.init(ALL_CANON_ENTITIES);
        DBWrapper.getCanonForms(LIST_SURFACE_FORMS);

        System.out.println(LIST_SURFACE_FORMS.size());
    }
}
