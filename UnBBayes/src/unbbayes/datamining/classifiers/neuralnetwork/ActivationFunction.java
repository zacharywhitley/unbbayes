package unbbayes.datamining.classifiers.neuralnetwork;

/**
 *  Interface that defines the methods that an activationFunction must have.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public interface ActivationFunction{

  /**
   * Calculate the outputo of the activation function for a given value.
   *
   * @param v The value to be evaluated by the activation function
   * @return The evaluated value
   */
  public double functionValue(double v);

  /**
   * Calculate the error term of an output neuron, given the expected value and
   * the actual calculated value.
   *
   * @param desired The desired value.
   * @param actualValue The actual calculated value.
   * @return The error term.
   */
  public double outputErrorTerm(double desired, double actualValue);

  /**
   * Calculate the error term of a hidden neuron, given the actual calculated
   * value and the sum of the error terms of all neuron of the next layer.
   *
   * @param y The actual output of the hidden neuron
   * @param sum The sum of the error terms of all neuron of the next layer
   * @return The error term.
   */
  public double hiddenErrorTerm(double y, double sum);

  /**
   * Method that normalizes a number to the activation function range
   *
   * @param data The number to be normalized to the activation function interval.
   * @param dataHighestValue The maximum value that the original data may assume.
   * @param dataLowestValue The minimum value that the original data may assume.
   * @return The normalized data.
   */
  public float normalizeToFunctionInterval(float data, float dataHighestValue, float dataLowestValue);
}
