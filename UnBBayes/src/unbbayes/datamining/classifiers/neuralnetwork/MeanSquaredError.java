package unbbayes.datamining.classifiers.neuralnetwork;

/**
 * Interface that defines a method that is used by the neural network main
 * class (NeuralNetwork) to output the values of the mean squared error an
 * it's epoch
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public interface MeanSquaredError {

  /**
   * Method that receives the mean squared error and it's epoch
   *
   * @param epoch An specific epoch
   * @param meanSquaredError The mean squared error associated to the epoch
   */
  public void setMeanSquaredError(int epoch, double meanSquaredError);
}