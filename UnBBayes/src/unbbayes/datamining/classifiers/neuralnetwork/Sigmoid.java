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
  public double outputErrorTerm(double desiredOutput, double actualOutput){   //sigma
    return a * (desiredOutput - actualOutput) * actualOutput * (1 - actualOutput);
  }

  public double hiddenErrorTerm(double y, double sum){   //sigma
    return a * y * (1 - y) * sum;
  }

}
