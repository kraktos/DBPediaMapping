
package de.dws.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.dws.reasoner.GenericConverter;

public class RunStats
{

    public static long global_tp = 0;    
    public static long global_blSize = 0;
    public static long global_gsSize = 0;
    
    
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {

         String[] name = {
         "actorstarredinmovie", "agentcollaborateswithagent",
         "animalistypeofanimal"
         , "athleteledsportsteam", "bankbankincountry", "citylocatedinstate",
         "bookwriter",
         "companyasloknownas",
         "personleadsorganization", "teamplaysagainstteam",
         "weaponmadeincountry",
         "lakeinstate"
         };

//        String[] name = {
//                "lakeinstate",
//        };

        String file = null;
        Allgn allGold;
        Allgn allBase;
        Score baselineScore;

        for (String s : name) {
            file = "/home/arnab/Dropbox/PHD_WORK/data/" + s + ".csv";

            // get the

            // create the two afiles to be aligned
            allGold = new Allgn(file, true, 0);
            
            GenericConverter.MAP_COUNTER.clear();
            
            allBase = new Allgn(file, false, 1);
            // calculate the score of the baselines (both Precison and recall)
            baselineScore = new Score(allBase ,allGold);

            //System.out.println(s + "\t" + (200-allGold.size())/(double)2);

            System.out.println(s + "\t" + baselineScore);
            
        }

        //System.out.println("Micro Precision = " + global_tp/(double)global_blSize + " \t" + global_tp + "\t" + global_blSize);
        //System.out.println("Micro Recall = " + global_tp/(double)global_gsSize + "\t" + global_tp + "\t" + global_gsSize);
        
    }

    private static void computePrecisionAndRecall(String file, boolean allMode) throws IOException
    {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        String[] elem = null;
        String gsvalue = null;
        String blvalue = null;

        int intersectionCount = 0;
        int gsSize = 0;

        int blSize = 0;

        int unmappedGS = 0;

        while ((line = reader.readLine()) != null) {

            try {
                elem = line.split("\t");
                gsvalue = elem[1];
                blvalue = elem[2];

                if (gsvalue.trim().equals("?")) {
                    unmappedGS++;
                }

                if (gsvalue.trim().equals(blvalue.trim()))
                    intersectionCount++;
                gsSize = 200;
                blSize = 200;
            } catch (Exception e) {
                e.getMessage();
            }
        }

        System.out.println("unmapped = " + unmappedGS / (double) 200);
        System.out.println("Precision All =  " + intersectionCount / (double) blSize);
        System.out.println("Recall  All  =  " + "\t" + intersectionCount / (double) gsSize);

        intersectionCount = 0;
        gsSize = 0;
        blSize = 0;

        reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {

            try {
                elem = line.split("\t");
                gsvalue = elem[1];
                blvalue = elem[2];

                if (!blvalue.trim().equals("?")) { // ignore the "?" marked
                                                   // stuffs
                    if (gsvalue.trim().equals(blvalue.trim())) {
                        intersectionCount++;
                        blSize++;
                    }
                } else { // the "?" cases in baseline
                    if (!gsvalue.trim().equals("?")) {
                        blSize++;
                    }
                }
                if (!gsvalue.trim().equals("?"))
                    gsSize++;

            } catch (Exception e) {
                e.getMessage();
            }
        }

        System.out.println("Precision =  " + intersectionCount / (double) blSize);
        System.out.println("Recall    =  " + "\t" + intersectionCount / (double) gsSize);

    }

}
