
package de.dws.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import unima.dws.gg.util.SameAs;
import de.dws.helper.util.Constants;
import de.dws.helper.util.Utilities;
import de.dws.mapper.dbConnectivity.DBWrapper;
import de.dws.reasoner.GenericConverter;

public class Allgn {

    // sameAs statements
    private List<SameAs> sameAss = new ArrayList<SameAs>();

    public Allgn(String file, boolean isGold, int topk) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        // init DB
        DBWrapper.init(Constants.GET_WIKI_LINKS_APRIORI_SQL);

        SameAs sameAs = null;
        String line;
        String arr[];
        List<String> uriVsProbabilities = null;
        String inst = null;
        
        while ((line = reader.readLine()) != null) {
            try {
                arr = line.split("\t");
                // create the gold alignment
                if (isGold) {
                    inst = generateUniqueURI(Utilities.cleanse(arr[0].trim()));
                    //inst = arr[0].trim();
                    sameAs = new SameAs(inst, arr[1].trim());

                    if (!arr[1].trim().equals("?"))
                        sameAss.add(sameAs);
                    else{
                        //System.out.println(arr[0] + " \t " + arr[1]);
                    }

                }
                else { // create the baseline alignment

                    inst = generateUniqueURI(Utilities.cleanse(arr[0].trim()));
                    //inst = arr[0].trim();

                    
                    uriVsProbabilities = DBWrapper.fetchTopKLinksWikiPrepProb(Utilities
                            .cleanse(arr[0].trim())
                            .replaceAll("\\_+", " "), topk);

                    if (uriVsProbabilities.size() > 0) {
                        for (String val : uriVsProbabilities) { 
                            
                            sameAs = new SameAs(inst,
                                    Constants.DBPEDIA_INSTANCE_NS
                                            + Utilities.utf8ToCharacter(val.split("\t")[0]).trim());
                            sameAss.add(sameAs);
                            
//                            System.out.println(arr[0].trim() + "\t" + inst + "\t" + arr[1].trim() + "\t" + Constants.DBPEDIA_INSTANCE_NS
//                                    + Utilities.utf8ToCharacter(val.split("\t")[0]).trim() );
                        }
                    }else{
//                        System.out.println(arr[0].trim() + "\t" + inst + "\t" + arr[1].trim() + "\t" + "?");
                    }
                    
                    
                    

//                     if (topk == 1) { // this is the default Top-1 case
//                    
//                     sameAs = new SameAs(arr[0].trim(), arr[2].trim());
//                    
//                     if (!arr[2].trim().equals("?"))
//                         sameAss.add(sameAs);
//                     }
                    // else {

                    // uriVsProbabilities =
                    // DBWrapper.fetchTopKLinksWikiPrepProb(Utilities
                    // .cleanse(arr[0].trim())
                    // .replaceAll("\\_+", " "), topk);
                    //
                    // if (uriVsProbabilities.size() > 0) {
                    // for (String val : uriVsProbabilities) {
                    //
                    // sameAs = new SameAs(arr[0].trim(),
                    // Constants.DBPEDIA_INSTANCE_NS
                    // + Utilities.utf8ToCharacter(val).trim());
                    //
                    // sameAss.add(sameAs);
                    // }
                    // }
                    // }
                    // // create a file for top 2 matches
                    // if (topk == 2) {
                    //
                    // sameAs = new SameAs(arr[0].trim(), arr[2].trim());
                    // if (!arr[2].trim().equals("?"))
                    // sameAss.add(sameAs);
                    // }
                }

            } catch (Exception e) {

            }
        }       
    }
    
    

    public Allgn() {
        // TODO Auto-generated constructor stub
    }

    public int size()
    {
        // while running this alignment, you always try to align for one axiom
        // type, so either of them should be
        // filled.
        return this.sameAss.size();
    }

    /**
     * Returns the intersection between this and that alignment.
     * 
     * @param that
     * @return
     */
    public Allgn getSetIntersection(Allgn that)
    {
        Allgn inter = new Allgn();
        for (SameAs sameAs : this.sameAss) {
            if (that.contains(sameAs)) {
                inter.add(sameAs);
            }
        }

        return inter;
    }

    private boolean contains(SameAs sameAs)
    {
        if (this.sameAss.contains(sameAs)) {
            return true;
        }
        return false;
    }

    private void add(SameAs sameAs)
    {
        this.sameAss.add(sameAs);
    }

    
    private static String generateUniqueURI(String nellInst) {
        
        // check if this URI is already there
        if (GenericConverter.MAP_COUNTER.containsKey(nellInst)) {
            long value = GenericConverter.MAP_COUNTER.get(nellInst);
            GenericConverter.MAP_COUNTER.put(nellInst, value + 1);

            // create an unique URI because same entity already has been
            // encountered before
            nellInst = nellInst + Constants.POST_FIX + String.valueOf(value + 1);

        } else {
            GenericConverter.MAP_COUNTER.put(nellInst, 1L);
        }
        return nellInst;
    }
}
