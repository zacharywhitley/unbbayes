package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

public class Tanh implements ActivationFunction, Serializable{

  public static final double MIN_STEEP = 0.0;
  public static final double DEF_STEEP = 1.0;

  private double steep;

  public Tanh(){
    steep = DEF_STEEP;
  }

  public Tanh(double steep) {
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

  public double functionValue(double v){
    return (1 - Math.exp(-2 * steep * v))/(1 + Math.exp(-2 * steep * v));
  }

  public double outputErrorTerm(double d, double o){  //sigma
    return steep * (d - o) * (1 - o) * (1 + o);
  }

  public double hiddenErrorTerm(double y, double sum){   //sigma
    return steep * (1 - y) * (1 + y) * sum;
  }
}
