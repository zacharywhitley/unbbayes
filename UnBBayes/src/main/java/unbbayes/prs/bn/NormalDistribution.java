package unbbayes.prs.bn;

import edu.csusb.danby.math.ProbMath;

public class NormalDistribution {
	
	private double mean;
	private double variance;
	
	public NormalDistribution() {
		this.mean = 0;
		this.variance = 1;
	}
	
	public NormalDistribution(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
	}

	/**
	 * Calculate x using the standard normal distribution from the P(Z <= z) 
	 * where z = (x - mean)/(standard variance) and P(Z <= z) = P(X <= x).
	 * 
	 * @param probability P(X <= x).
	 * @return The CDF upper bound (x).
	 */
	public double getCDFUpperBound(double probability) {
		double z = ProbMath.inverseNormal(probability);
		double x = Math.sqrt(variance) * z + mean;
		return x;
	}
	
	/**
	 * Calculate P(X <= x) using the standard normal distribution P(Z <= z) 
	 * where z = (x - mean)/(standard variance) and P(Z <= z) = P(X <= x).
	 * @param x The CDF upper bound.
	 * @return P(X <= x).
	 */
	public double getCDF(double x) {
		double z = (x - mean) / Math.sqrt(variance);
		double probability = ProbMath.normalCdf(z);
		return probability;
	}
	
	/**
	 * Calculate PDF from an interval from CDF. 
	 * P(ini <= X <= end) = P(X <= end) - P(X <= ini).
	 * @param ini The interval initial value.
	 * @param end The interval end value.
	 * @return The PDF for the interval from initial to end value.
	 */
	public double getPDF(double ini, double end) {
		return getCDF(end) - getCDF(ini);
	}
	
	/**
	 * Get the normal distribution mean.
	 * @return The normal distribution mean.
	 */
	public double getMean() {
		return mean;
	}
	
	/**
	 * Set the normal distribution mean.
	 * @param mean The normal distribution mean.
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}
	
	/**
	 * Get the normal distribution variance.
	 * @return The normal distribution variance.
	 */
	public double getVariance() {
		return variance;
	}
	
	/**
	 * Set the normal distribution variance.
	 * @param variance The normal distribution variance.
	 */
	public void setVariance(double variance) {
		this.variance = variance;
	}
	
	public static void main(String[] args) {
		NormalDistribution nd = new NormalDistribution(700, 300);
		
		nd.getCDFUpperBound(.6);
	}
}
