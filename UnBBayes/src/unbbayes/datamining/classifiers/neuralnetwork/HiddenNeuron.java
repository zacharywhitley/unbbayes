package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.Serializable;
import java.util.Arrays;

/**
 *  Class that defines a hidden neuron.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 *  @see Neuron
 */
public class HiddenNeuron extends Neuron implements Serializable{

  /**The calculated value of the neuron output*/
  public transient float outputValue;

  /**The calculated error term of the neuron*/
  public transient float errorTerm;

  /**The momentum been used*/
  public transient float momentum;

  /**
   * The constructor of the HiddenNeuron class
   *
   * @param activationFunction The activation function to be used by this neuron.
   * @param numberOfInputs The number of inputs connected to this neuron.
   * @param momentum The momentum factor to be used.
   * @see {@link ActivationFunction}
   */
  public HiddenNeuron(ActivationFunction activationFunction, int numberOfInputs, float momentum){
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];   //+1 for the bias value
    deltaW = new float[numberOfInputs + 1];
    this.momentum = momentum;
    startWeights();
    Arrays.fill(deltaW, 0);   //initialize deltaW with 0
  }

  /**
   * Method that returns the output value calculated by the neruon.
   *
   * @return The calculated output value.
   */
  public float outputValue(){
    return outputValue;
  }

  /**
   * Method used during the trainning phase of the network used to update
   * the input connections weigths.
   *
   * @param learningRate The learning rate value
   * @param inputValues The input values of the neuron
   */
  public void updateWeights(float learningRate, float[] inputValues) {
    float learningRateXErrorTerm = learningRate * errorTerm;
    deltaW[0] = (momentum * deltaW[0]) + learningRateXErrorTerm;
    weights[0] = weights[0] + deltaW[0]; //bias

    for (int i=1; i<weights.length; i++) {
      deltaW[i] = (momentum * deltaW[i]) + (learningRateXErrorTerm * inputValues[i-1]);
      weights[i] = weights[i] + deltaW[i];
    }
  }

  /**
   * Method that returns the error term calculated by the neuron.
   *
   * @return The calculated error term.
   */
  public float getErrorTerm(){
    return errorTerm;
  }

  /**
   * Method that calculate the output value of the neuron using the inputValues
   *
   * @param inputValues An array with the inputs of the neuron.
   */
  public void calculateOutputValue(float[] inputValues){
    float net = weights[0];  //bias value
    for(int i=0; i<inputValues.length; i++){
      net = net + (inputValues[i] * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
  }

  /**
   * Method that calculate the error term of the neuron.
   *
   * @param outputLayer The ouput layer of the network
   * @param neuronIndex This hidden neuron index
   * @see {@link OutputNeuron}
   */
  public void calculateErrorTerm(OutputNeuron[] outputLayer, int neuronIndex){
    float sum = 0;
    for(int i=0; i<outputLayer.length; i++){
      sum = sum + (outputLayer[i].errorTerm * outputLayer[i].getWeight(neuronIndex));
    }
    errorTerm = (float)activationFunction.hiddenErrorTerm(outputValue, sum);
  }
}