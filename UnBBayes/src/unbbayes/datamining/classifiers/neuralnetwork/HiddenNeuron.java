package unbbayes.datamining.classifiers.neuralnetwork;

import java.util.*;
import java.io.*;

public class HiddenNeuron extends Neuron implements Serializable{

  private float outputValue;
  private float errorTerm;   //sigma

  public HiddenNeuron(ActivationFunction activationFunction, int numberOfInputs){
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];
    deltaW = new float[numberOfInputs + 1];
    startWeights();
    Arrays.fill(deltaW, 0);   //inicializa delta w com 0
  }

  public float outputValue(){
    return outputValue;
  }

  public void updateWeights(float learningRate, float momentum, float[] inputLayer) {
    deltaW[0] = (momentum * deltaW[0]) + (learningRate * errorTerm);
    weights[0] = weights[0] + deltaW[0]; //bias

    for (int i=1; i<weights.length; i++) {
      deltaW[i] = (momentum * deltaW[i]) + (learningRate * errorTerm * inputLayer[i-1]);
      weights[i] = weights[i] + deltaW[i];
    }
  }

  public float getErrorTerm(){
    return errorTerm;
  }

  public void calculateOutputValue(float[] inputValues){
    float net = weights[0];  //bias value
    for(int i=0; i<inputValues.length; i++){
      net = net + (inputValues[i] * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
  }

  public void calculateErrorTerm(OutputNeuron[] outputLayer, int neuronIndex){
    float sum = 0;
    for(int i=0; i<outputLayer.length; i++){
      sum = sum + (outputLayer[i].getErrorTerm() * outputLayer[i].getWeight(neuronIndex));
    }
    errorTerm = (float)activationFunction.hiddenErrorTerm(outputValue, sum);
  }
}