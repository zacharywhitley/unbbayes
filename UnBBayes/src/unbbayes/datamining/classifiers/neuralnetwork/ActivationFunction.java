package unbbayes.datamining.classifiers.neuralnetwork;

public interface ActivationFunction {


  public double functionValue(double v);

  public double outputErrorTerm(double d, double o);  //sigma

  public double hiddenErrorTerm(double y, double sum);  //sigma
}
