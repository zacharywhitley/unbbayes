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
package unbbayes.datamining.classifiers.cnmentities;

import java.io.Serializable;

/**
 *  Class that implements the combinations tha compound the
 *  Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class Combination implements Serializable{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  private String key;
  private OutputNeuron[] outputArray;

  /**
   * Constructs a new empty combination.
   *
   * @param key the unique key for the new combination.
   */
  public Combination(String key) {
    this.key = key;
  }

  /**
   * Constructs a new combination.
   *
   * @param key the unique key for the new combination.
   * @param outputArray an array of the output neurons connected to this combination.
   */
  public Combination(String key, OutputNeuron[] outputArray) {
    this.key = key;
    this.outputArray = outputArray;
  }

  /**
   * Returns the unique key of this combination.
   *
   * @return this combination key.
   */
  public String getKey(){
    return this.key;
  }

  /**
   * Returns the output neurons connected to this combination.
   *
   * @return an array with the output neurons connected to this combination.
   */
  public OutputNeuron[] getOutputArray(){
    return this.outputArray;
  }

  /**
   * Returns an specified output neuron connected to this combination.
   *
   * @param index the index of the desired output neuron.
   * @return the specified output neuron.
   */
  public OutputNeuron getOutputNeuron(int index){
    return outputArray[index];
  }

  /**
   * Executes the punishment of the model, calculating the confidence,
   * the support and the final weight of the accumulators (newWeight).
   *
   * @param numOfInstances the number of instances of the training set for the
   *                       calculation of the support.
   */
  public void punish(int numOfInstances){
    int outputNum = outputArray.length;
    int sum = 0;

    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null){
        for(int j=0; j<outputNum; j++){
          if((outputArray[j] != null) && (i != j)){
            sum = sum + outputArray[j].accumulator;
          }
        }
        outputArray[i].setNetWeight(outputArray[i].accumulator - sum);
        outputArray[i].setConfidence((float)outputArray[i].accumulator * 100 / (sum + outputArray[i].accumulator));
        outputArray[i].setSupport((float)outputArray[i].accumulator * 100 / numOfInstances);
      }
      sum = 0;
    }
  }

  /**
   * Prunnes the model based on a threshold.
   *
   * @param threshold the threshold to prunne the model.
   */
  public void prunning(int threshold){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && outputArray[i].netWeight < threshold){
        outputArray[i] = null;
      }
    }
  }

  /**
   * Prunnes the model based on a minimum support an confidence.
   *
   * @param minSupport the minimum support to prunne the model.
   * @param minConfidence the minimum confidence to prunne the model.
   */
  public void prunning(int minSupport, int minConfidence){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && (outputArray[i].support < minSupport || outputArray[i].confidence < minConfidence)){
        outputArray[i] = null;
      }
    }
  }

  /**
   * Tests if this combination is connected to any output neuron.
   *
   * @return <code>true</code> if the combination is disconnected;
   *         <code>false</code> otherwise.
   */
  public boolean isNull(){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null){
        return false;
      }
    }
    return true;
  }

  /**
   * Increases the accumulatos of the specified output neuron.
   *
   * @param classValue the class value that specify the output neuron.
   * @param weight the weight to increase the accumulator.
   */
  public void increaseAccumulator(int classValue, int weight){  // classValue, para selecionar qual classe deve ser incrementado o acumulador
    if(outputArray[classValue] != null){                          // e o peso para incrementar
      outputArray[classValue].increaseAccumulator(weight);
    } else {
      outputArray[classValue] = new OutputNeuron(weight);
    }
  }
}