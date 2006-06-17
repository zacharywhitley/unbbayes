package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.Serializable;

import unbbayes.datamining.datamanipulation.Utils;

/**
 *  Class that implements the logistic (sigmoid) activation function
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 *  @see ActivationFunction
 */
public class Sigmoid implements ActivationFunction, Serializable{

  /**Constant that defines the minimum steep of the activation function*/
  public static final double MIN_STEEP = 0.0;

  /**Constant that defines the default steep of the activation function*/
  public static final double DEF_STEEP = 1.0;

  /**Constant determining the steepness of the sigmoid function*/
  private double steep;

  /**The default constructor of the sigmod activation function*/
  public Sigmoid(){
    steep = DEF_STEEP;
  }

  /**
   * Constructor of the sigmoidal activation function with a defined steep.
   *
   * @param steep The steepness of the sigmoid function.
   */
  public Sigmoid(double steep) {
    this.steep = steep;
  }

  /**
   * Method used to set or change the steepness of the function.
   *
   * @param steep The new steepness of the sigmoid function.
   */
  public void setSteep(float steep) {
    if (steep > MIN_STEEP){
      this.steep = steep;
    }
  }

  /**
   * Method used to get the actual value of the sigmoid function steep.
   *
   * @return The actual sigmoid function steep.
   */
  public double getSteep(){
    return steep;
  }

  /**
   * Calculate the output of the sigmoidal activation function for the
   * given value.
   *
   * @param v the summation of the inputs multiplied by it's weights
   * @return the ouput of the sigomid funciton
   */
  public double functionValue(double v){
    return 1 / (1 + Math.exp(- steep * v));
  }

  /**
   * Method that calculate the error term of an output neuron using the
   * sigmoidal activation function.
   *
   * @param desiredOutput the expected (desired) output
   * @param actualOutput the actual output
   * @return the value of the error term.
   */
  public double outputErrorTerm(double desiredOutput, double actualOutput){
    return steep * (desiredOutput - actualOutput) * actualOutput * (1 - actualOutput);
  }

  /**
   * Method that calculate the error term of a hidden neuron using the
   * sigmoidal activation function.
   *
   * @param y The actual output of the hidden neuron
   * @param sum The sum of the error terms of all neuron of the next layer
   * @return the value of the error term.
   */
  public double hiddenErrorTerm(double y, double sum){
    return steep * y * (1 - y) * sum;
  }

  /**
  * Method that normalizes a number to the activation function range
  *
  * @param data The number to be normalized to the activation function interval.
  * @param dataHighestValue The maximum value that the original data may assume.
  * @param dataLowestValue The minimum value that the original data may assume.
  * @return The normalized data.
  */
  public float normalizeToFunctionInterval(float data, float dataHighestValue, float dataLowestValue){
    return Utils.normalize(data, dataHighestValue, dataLowestValue, 1, 0);
  }
}
