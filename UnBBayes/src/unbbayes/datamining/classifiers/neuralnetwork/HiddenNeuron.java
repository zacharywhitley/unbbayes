package unbbayes.datamining.classifiers.neuralnetwork;

public class HiddenNeuron extends Neuron{

  public HiddenNeuron(ActivationFunction activationFunction, int numberOfInputs){
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