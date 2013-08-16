
package de.dws.test;

import java.text.DecimalFormat;

public class Score {

    private Allgn alignment;
    private Allgn goldStandard;
    private Allgn intersection;

    /**
     * Constructs a score which is used to compute precision, recall, f-measure
     * (and whatever else is required).
     * 
     * @param alignment The input alignment
     * @param goldStandard The gold standard alignment.
     */
    public Score(Allgn alignment, Allgn goldStandard) {
        this.alignment = alignment;
        this.goldStandard = goldStandard;
        this.intersection = alignment.getSetIntersection(goldStandard);
    }

    /**
     * Computes the precision.
     * 
     * @return The precision of this score.
     */
    public double getPrecision() {
        return (double) this.intersection.size() / (double) this.alignment.size();
    }

    /**
     * Computes recall.
     * 
     * @return The recall of this score.
     */
    public double getRecall() {
        return (double) this.intersection.size() / (double) this.goldStandard.size();
    }

    /**
     * Computes F1-measure.
     * 
     * @return The f1-measure of this score.
     */
    public double getF() {
        double p = this.getPrecision();
        double r = this.getRecall();
        return (2.0 * p * r) / (p + r);
    }

    /**
     * Returns a string representation of all computed measures
     * 
     * @return The f1-measure of this score.
     */
    public String toString() {
        String rep = "";
        rep += toDecimalFormat(this.getPrecision()) + "\t"
                + toDecimalFormat(this.getRecall()) + "\t" + toDecimalFormat(this.getF());

        RunStats.global_tp = RunStats.global_tp + this.intersection.size();
        RunStats.global_blSize = RunStats.global_blSize + this.alignment.size();
        RunStats.global_gsSize = RunStats.global_gsSize + this.goldStandard.size();
               
        //System.out.println("tp = " + this.intersection.size() + "\t baseSize =" + this.alignment.size() + "\t gold = " + this.goldStandard.size()) ;
        return rep;
    }

    private static String toDecimalFormat(double value) {
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(value).replace(',', '.');
    }

}
