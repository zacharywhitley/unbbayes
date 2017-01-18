/**
 * 
 */
package utils;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

/**
 * Extension of {@link ChiSquareTest} with support for zero counts.
 * @author Shou Matsumoto
 */
public class ChiSqureTestWithZero extends ChiSquareTest {
	
	public ChiSqureTestWithZero() {
		super();
	}
	
	/**
	 * This is the same of {@link ChiSquareTest#chiSquare(long[][])}, but
	 * zeros in the matrix will be properly handled.
	 * @see org.apache.commons.math3.stat.inference.ChiSquareTest#chiSquare(long[][])
	 */
	public double chiSquare(final long[][] counts)
		        throws NullArgumentException, NotPositiveException,
		        DimensionMismatchException {

        if (counts.length < 2) {
            throw new DimensionMismatchException(counts.length, 2);
        }

        if (counts[0].length < 2) {
            throw new DimensionMismatchException(counts[0].length, 2);
        }

        MathArrays.checkRectangular(counts);
        MathArrays.checkNonNegative(counts);
    
        int nRows = counts.length;
        int nCols = counts[0].length;

        // compute row, column and total sums
        double[] rowSum = new double[nRows];
        double[] colSum = new double[nCols];
        double total = 0.0d;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                rowSum[row] += counts[row][col];
                colSum[col] += counts[row][col];
                total += counts[row][col];
            }
        }

        // compute expected counts and chi-square
        double sumSq = 0.0d;
        double expected = 0.0d;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                expected = (rowSum[row] * colSum[col]) / total;
                if (expected <= 0) {
                	// when expected is zero, counts[row][col] must also be zero (because we don't have negative counts)
                	// in such case, we can ignore this factor, because (counts[row][col] - expected) = 0. 
                	// If we don't explicitly ignore this factor, it will become NaN (and the entire sum will become NaN), because of division by 0
                	continue;	
                }
                sumSq += ((counts[row][col] - expected) *
                        (counts[row][col] - expected)) / expected;
            }
        }
        return sumSq;

   }
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.commons.math3.stat.inference.ChiSquareTest#chiSquare(double[], long[])
	 */
    public double chiSquare(final double[] expected, final long[] observed)
        throws NotPositiveException, NotStrictlyPositiveException,
        DimensionMismatchException {

        if (expected.length < 2) {
            throw new DimensionMismatchException(expected.length, 2);
        }
        if (expected.length != observed.length) {
            throw new DimensionMismatchException(expected.length, observed.length);
        }
        checkNonNegative(expected);
        MathArrays.checkNonNegative(observed);

        double sumExpected = 0d;
        double sumObserved = 0d;
        for (int i = 0; i < observed.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        double ratio = 1.0d;
        boolean rescale = false;
        if (FastMath.abs(sumExpected - sumObserved) > 10E-6) {
            ratio = sumObserved / sumExpected;
            rescale = true;
        }
        double sumSq = 0.0d;
        for (int i = 0; i < observed.length; i++) {
            if (rescale) {
                final double dev = observed[i] - ratio * expected[i];
                if (dev == 0d) {
                	continue;
                }
                sumSq += dev * dev / (ratio * expected[i]);
            } else {
                final double dev = observed[i] - expected[i];
                if (dev == 0d) {
                	continue;
                }
                sumSq += dev * dev / expected[i];
            }
        }
        return sumSq;

    }

    /**
     * Check that all entries of the input array are >= 0.
     *
     * @param in Array to be tested
     * @throws NotPositiveException if any array entries are less than 0.
     */
	public void checkNonNegative(double[] in) throws NotPositiveException {
	    for (int i = 0; i < in.length; i++) {
	    	if (in[i] < 0) {
	    		throw new NotPositiveException(in[i]);
	    	}
	    }
	}

}
