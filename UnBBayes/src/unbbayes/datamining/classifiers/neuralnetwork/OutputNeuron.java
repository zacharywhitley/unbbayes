package unbbayes.datamining.classifiers.neuralnetwork;

import java.util.*;

public class OutputNeuron extends Neuron{

  private float outputValue;
  private float errorTerm;    //sigma

  public OutputNeuron(ActivationFunction activationFunction, int numberOfInputs) {
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];
    deltaW = new float[numberOfInputs + 1];
    startWeights();
    Arrays.fill(deltaW, 0);
  }

  public float outputValue(){
    return outputValue;
  }

  public void updateWeights(float learningRate, float momentum, HiddenNeuron[] hiddenLayer){
    deltaW[0] = (momentum * deltaW[0]) + (learningRate * errorTerm);
    weights[0] = weights[0] + deltaW[0];  //bias

    for(int i=1; i<weights.length; i++){
      deltaW[i] = (momentum * deltaW[i]) + (learningRate * errorTerm * hiddenLayer[i-1].outputValue());
      weights[i] = weights[i] + deltaW[i];
    }
  }

  public float getErrorTerm(){   //sigma
    return errorTerm;
  }

  public float getWeight(int weightIndex){
    return weights[weightIndex + 1];   //mais um para considerar o bias
  }

  public float calculateOutputValue(HiddenNeuron[] inputs, int expectedOutput){
    float instantaneousError;
    float net = weights[0];  //bias value
    for(int i=0; i<inputs.length; i++){
      net = net + (inputs[i].outputValue() * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
    instantaneousError = expectedOutput - outputValue;
    errorTerm = (float)activationFunction.outputErrorTerm(expectedOutput, outputValue);  //calculo de sigma
    return instantaneousError;
  }
}


