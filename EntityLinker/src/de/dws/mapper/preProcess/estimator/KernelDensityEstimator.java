/**
 * 
 */
package de.dws.mapper.preProcess.estimator;

import org.apache.log4j.Logger;

import xxl.core.math.statistics.nonparametric.kernels.GaussianKernel;
import xxl.core.math.statistics.nonparametric.kernels.NativeKernelDensityEstimator;

/**
 * This class is responsible for estimating the density function for a set of random variables whose underlying
 * distribution is not known. This is actually what KDE is all about. We use it here to estimate the likelihood of a
 * value lying in the given set of training examples. For instance, this class is used to estimate the probability with
 * which a particular triple is more likely than another triple. Suppose we have two triples "X married to Y" and
 * "Z married to Y". We create a distribution on the predicate (in this example) by defining the difference of the birth
 * dates of the persons occurring as arguments to the given predicate. Obviously, if a person is married to someone, the
 * age difference cannot be widely different, it should range somewhere between [-10 to +10] but not +-100. So if the
 * two arguments in a triple are having a wide difference, it is highly probable( not absolutely ) that they are error
 * facts. We can easily filer those out and re-rank the possible results with a descending order of probabilities of
 * likelihood.
 * 
 * @author Arnab Dutta
 */
public class KernelDensityEstimator
{
    /**
     * logger
     */
    public static Logger logger = Logger.getLogger(KernelDensityEstimator.class.getName());

    /**
     * kernel Density Estimator instance
     */
    private NativeKernelDensityEstimator kde;

    /**
     * maximum value within the data input array
     */
    private Double maxValue = null;

    /**
     * minimum value within the data input array
     */
    private Double minValue = null;

    /**
     * Default constructor, Initializing the kernel
     * 
     * @param dataArr array of data values
     */
    public KernelDensityEstimator(Double[] dataArr)
    {
        super();
        computeMinMax(dataArr);

        // define the size of each buckets, i.e bin size
        double bandWidth = (this.maxValue - this.minValue) / dataArr.length;

        // define a kernel, we choose a Gaussian one, There is no absolute reason for this, any different kernel
        // functions may be chosen
        this.kde = new NativeKernelDensityEstimator(new GaussianKernel(), dataArr, bandWidth);

    }

    /**
     * @return the maxValue
     */
    public Double getMaxValue()
    {
        return maxValue;
    }

    /**
     * @return the minValue
     */
    public Double getMinValue()
    {
        return minValue;
    }

    /**
     * computes the maximum and minimum value from the range of input values
     * 
     * @param dataArr takes the data array as input,
     */
    private void computeMinMax(Double[] dataArr)
    {
        double maxValue = dataArr[0];
        double minValue = dataArr[0];

        for (Double val : dataArr) {
            if (val > maxValue) {
                maxValue = val;
            }
            if (val < minValue) {
                minValue = val;
            }
        }
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    /**
     * returns the value of the estimated density at the given value as input
     * 
     * @param value input value for which we want to estimate the the probability
     * @return the probability of occurrence
     */
    public double getEstimatedDensity(double value)
    {
        return this.kde.evalKDE(value);
    }

}
