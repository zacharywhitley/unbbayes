package unbbayes.datamining.classifiers.neuralnetwork;

public abstract class Neuron {

  float[] weights;
  ActivationFunction activationFunction;

  public abstract float outputValue();

  public abstract void updateWeights();

  public abstract float getSigma();

}