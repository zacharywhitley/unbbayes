package unbbayes.datamining.classifiers.neuralnetwork;

public class Sigmoid implements ActivationFunction{

  private double a;  //constant (determining the steepness of the sigmoid)

  public Sigmoid(double a) {
    this.a = a;
  }


  /**
   *
   * @param v the summation of the inputs multiplied by it's weights
   * @return the ouput of the sigomid funciton
   */
  public double functionValue(double v){
    return 1 / (1 + Math.exp(- a * v));
  }

  /**
   *
   * @param d the desired (expected) output
   * @param o the actual output
   * @return the value of sigma
   */
  public double sigmaOutput(double d, double o){
    return a * (d - o) * o * (1 - o);
  }

  public double sigmaHidden(double y, double sum){
    return a * y * (1 - y) * sum;
  }

}
