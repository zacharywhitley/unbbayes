package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

public class Tanh implements ActivationFunction, Serializable{

  private double a;
  private double b;
  private double ba;

  public Tanh(double a, double b) {
    this.a = a;
    this.b = b;
    this.ba = b / a;
  }

  public double functionValue(double v){
    //return (1 - Math.exp(-2 * v))/(1 + Math.exp(-2 * v));
    return a * (1 - Math.exp(-2 * b * v))/(1 + Math.exp(-2 * b * v));
  }

  public double outputErrorTerm(double d, double o){  //sigma
    return ba * (d - o) * (a - o) * (a + o);
  }

  public double hiddenErrorTerm(double y, double sum){   //sigma
    return ba * (a - y) * (a + y) * sum;
  }
}
