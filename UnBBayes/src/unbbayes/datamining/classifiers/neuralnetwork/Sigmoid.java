package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

public class Sigmoid implements ActivationFunction, Serializable{

  public static final double MIN_BETA = 0.0;
  public static final double DEF_BETA = 1.0;

  private double beta;  //constant (determining the steepness of the sigmoid)

  public Sigmoid(){
    beta = DEF_BETA;
  }

  public Sigmoid(double beta) {
    this.beta = beta;
  }

  public void setBeta(float beta) {
    if (beta > MIN_BETA)
      beta = beta;
  }

  public double getBeta(){
    return beta;
  }

  /**
   *
   * @param v the summation of the inputs multiplied by it's weights
   * @return the ouput of the sigomid funciton
   */
  public double functionValue(double v){
    return 1 / (1 + Math.exp(- beta * v));
  }

  /**
   *
   * @param d the desired (expected) output
   * @param o the actual output
   * @return the value of sigma
   */
  public double outputErrorTerm(double desiredOutput, double actualOutput){   //sigma
    return beta * (desiredOutput - actualOutput) * actualOutput * (1 - actualOutput);
  }

  public double hiddenErrorTerm(double y, double sum){   //sigma
    return beta * y * (1 - y) * sum;
  }

}
