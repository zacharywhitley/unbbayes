package unbbayes.datamining.classifiers.neuralnetwork;

public class Tanh implements ActivationFunction{

  private double a;
  private double b;
  private double ba;

  public Tanh(double a, double b) {
    this.a = a;
    this.b = b;
    this.ba = b / a;
  }

  public double functionValue(double v){
    return (1 - Math.exp(-2 * v))/(1 + Math.exp(-2 * v));
  }

  public double sigmaOutput(double d, double o){
    return ba * (d - o) * (a - o) * (a + o);
  }

  public double sigmaHidden(double y, double sum){
    return ba * (a - y) * (a + y) * sum;
  }
}
