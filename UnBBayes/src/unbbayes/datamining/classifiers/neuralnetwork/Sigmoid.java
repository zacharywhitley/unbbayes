package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

public class Sigmoid implements ActivationFunction, Serializable{

  public static final double MIN_STEEP = 0.0;
  public static final double DEF_STEEP = 1.0;

  private double steep;  //constant (determining the steepness of the sigmoid)

  public Sigmoid(){
    steep = DEF_STEEP;
  }

  public Sigmoid(double steep) {
    this.steep = steep;
  }

  public void setSteep(float steep) {
    if (steep > MIN_STEEP){
      this.steep = steep;
    }
  }

  public double getSteep(){
    return steep;
  }

  /**
   *
   * @param v the summation of the inputs multiplied by it's weights
   * @return the ouput of the sigomid funciton
   */
  public double functionValue(double v){
    return 1 / (1 + Math.exp(- steep * v));
  }

  /**
   *
   * @param d the desired (expected) output
   * @param o the actual output
   * @return the value of sigma
   */
  public double outputErrorTerm(double desiredOutput, double actualOutput){   //sigma
    return steep * (desiredOutput - actualOutput) * actualOutput * (1 - actualOutput);
  }

  public double hiddenErrorTerm(double y, double sum){   //sigma
    return steep * y * (1 - y) * sum;
  }

}
