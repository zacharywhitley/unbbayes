package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

public abstract class Neuron implements Serializable{

  float[] weights;
  float[] deltaW;
  ActivationFunction activationFunction;

  public abstract float outputValue();

  public abstract float getErrorTerm();

  void startWeights(){
    double weight;
    for(int i=0; i<weights.length; i++){
      weight = Math.random();
      weights[i] = (float)(weight - 0.5);
    }
  }
}