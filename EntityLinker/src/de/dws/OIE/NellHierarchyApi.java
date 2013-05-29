/**
 * 
 */

package de.dws.OIE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.dws.mapper.dbConnectivity.DBWrapper;

/**
 * @author Arnab Dutta
 */
public class NellHierarchyApi {

    // define Logger
    static Logger logger = Logger.getLogger(NellHierarchyApi.class.getName());

    public static final String GET_ALL_SUPER_CLASSES = "select object from nellCategoriesRelations where predicate = 'generalizations' and subject =?";
    public static final String GET_ALL_SUB_CLASSES = "select subject from nellCategoriesRelations where predicate = 'generalizations' and object =?";

    public static final String GET_INV_REL = "select object from nellCategoriesRelations where predicate = 'inverse' and subject =?";

    public static final String GET_DISJ_CLASSES = "select object from nellCategoriesRelations where subject =? and predicate = 'mutexpredicates'";

    public static Map<String, List<String>> subHierarchy = new HashMap<String, List<String>>();

    /**
     * @param args
     */
    public static void main(String[] args) {

        PropertyConfigurator
                .configure("resources/log4j.properties");

        initDB();

        getGeneralizedClasses("farmlocatedinstate");

        getSpecializedClasses("farmlocatedinstate");

        getInverseProp("aquariumincity");

        getDisjointClasses("buildinglocatedincity");

        isDisjoint("", "");

        DBWrapper.shutDown();
    }

    private static void isDisjoint(String arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * get a list of disjoint classes
     * 
     * @param arg class/predicate name
     * @return list of classes/predicates disjoint wth given one
     */
    private static List<String> getDisjointClasses(String arg) {
        List<String> disJClasses = new ArrayList<String>();

        disJClasses = DBWrapper.getDisjClasses(arg);
        System.out.println(disJClasses);

        return disJClasses;

    }

    /**
     * inverse of predicate
     * 
     * @param arg predicate
     * @return
     */
    private static String getInverseProp(String arg) {
        return DBWrapper.getInverseRel(arg);
    }

    /**
     * get subsumption concepts
     * 
     * @param arg
     * @return
     */
    private static List<String> getSpecializedClasses(String arg) {

        List<String> subClasses = null;
        subClasses = DBWrapper.getSub(arg);

        if (subClasses.size() >= 1) {
            subHierarchy.put(arg, subClasses);
            for (String subClass : subClasses) {
                subHierarchy.put(subClass, getSpecializedClasses(subClass));
            }
        }
        return subClasses;
    }

    /**
     * get more generalized concepts
     * 
     * @param arg
     */
    private static void getGeneralizedClasses(String arg) {

        while (!arg.equals("all")) {
            arg = DBWrapper.getSuper(arg);
            if (arg != null)
                System.out.println(arg);
            else
                break;
        }
    }

    /**
     * initialize DB connections statements
     */
    private static void initDB() {
        DBWrapper.initNellReasoner();
    }

}
