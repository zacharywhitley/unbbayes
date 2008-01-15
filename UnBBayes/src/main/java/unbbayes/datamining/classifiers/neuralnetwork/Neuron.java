package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.*;

/**
 *  Abstract class that defines the methods that a neuron must have.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public abstract class Neuron implements Serializable{

  /**
   * Array that contains the wights associated to each input of the neuron
   * and the weight associated to the bias value
   */
  float[] weights;

  /**
   * Array that contains the delta W of each input and the bias value
   * calculated for the previous instance.
   */
  transient float[] deltaW;

  /**
   * The activaiton function been used.
   */
  ActivationFunction activationFunction;

  /**
   * Method that returns the output value of the neuron.
   *
   * @return The actual output value.
   */
  public abstract float outputValue();

  /**
   * Method that return the erro term of the neuron.
   *
   * @return The calculated error term.
   */
  public abstract float getErrorTerm();

  /**
   * Method use to set the initial weights of the network connections.
   */
  void startWeights(){
    double weight;
    for(int i=0; i<weights.length; i++){
      weight = Math.random();
      weights[i] = (float)(weight - 0.5);
    }
  }
}