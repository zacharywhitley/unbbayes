package unbbayes.datamining.classifiers.neuralnetwork;

public interface ActivationFunction {


  public double functionValue(double v);

  public double sigmaOutput(double d, double o);

  public double sigmaHidden(double y, double sum);
}
