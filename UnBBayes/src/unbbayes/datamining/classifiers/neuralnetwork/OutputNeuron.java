package unbbayes.datamining.classifiers.neuralnetwork;

public class OutputNeuron extends Neuron{

  public OutputNeuron(ActivationFunction activationFunction, int numberOfInputs) {
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs];
  }

  public float outputValue(){
    return 0;
  }

  public void updateWeights(){

  }

  public float getSigma(){
    return 0;
  }



}