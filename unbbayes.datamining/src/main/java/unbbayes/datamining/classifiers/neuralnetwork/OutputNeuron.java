/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.Serializable;
import java.util.Arrays;

/**
 *  Class that defines an output neuron.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 *  @see Neuron
 */
public class OutputNeuron extends Neuron implements Serializable{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  /**The calculated value of the neuron output*/
  public transient float outputValue;

  /**The calculated error term of the neuron*/
  public transient float errorTerm;

  /**The momentum factor*/
  public transient float momentum;

  /**
   * The constructor of the OutputNeuron class
   *
   * @param activationFunction The activation function to be used by this neuron.
   * @param numberOfInputs The number of inputs connected to this neuron.
   * @param momentum The momentum factor.
   * @see {@link ActivationFunction}
   */
  public OutputNeuron(ActivationFunction activationFunction, int numberOfInputs, float momentum) {
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];
    deltaW = new float[numberOfInputs + 1];
    this.momentum = momentum;
    startWeights();
    Arrays.fill(deltaW, 0);
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
   * @param hiddenLayer The network hidden layer
   * @see {@link HiddenNeuron}
   */
  public void updateWeights(float learningRate, float[] inputValues){
    float learningRateXErrorTerm = learningRate * errorTerm;
    deltaW[0] = (momentum * deltaW[0]) + learningRateXErrorTerm;
    weights[0] = weights[0] + deltaW[0];  //bias

    for(int i=1; i<weights.length; i++){
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
   * Method that returns the weight associated to an specific input.
   *
   * @param weightIndex The index of the desired weight.
   * @return The desired weight.
   */
  public float getWeight(int weightIndex){
    return weights[weightIndex + 1];   //mais um para considerar o bias
  }

  /**
   * Method that calculate the output value of the neuron using the output
   * values of the hidden neuron.
   *
   * @param inputValues An array with the inputs of the neuron.
   */
  public float calculateOutputValue(float[] inputValues){
    float net = weights[0];  //bias value
    for(int i=0; i<inputValues.length; i++){
      net = net + (inputValues[i] * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
    return outputValue;
  }

   /**
   * Method that calculate the error term of the neuron and returns the
   * instantaneous calculated error.
   *
   * @param expectedOutput The expected ouptut value.
   * @return The instantaneous error.
   */
  public float calculateErrorTerm(float expectedOutput){
    float instantaneousError = expectedOutput - outputValue;
    errorTerm = (float)activationFunction.outputErrorTerm(expectedOutput, outputValue);  //calculo de sigma
    return instantaneousError;
  }
}
