package unbbayes.datamining.classifiers.cnmentities;

import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Combination implements Serializable{

  private String key;
  private OutputNeuron[] outputArray;

  public Combination(String key) {
    this.key = key;
  }

  public Combination(String key, OutputNeuron[] outputArray) {
    this.key = key;
    this.outputArray = outputArray;
  }

  public void setKey(String key){
    this.key = key;
  }

  public String getKey(){
    return this.key;
  }

  public OutputNeuron[] getOutputArray(){
    return this.outputArray;
  }

  public OutputNeuron getOutputNeuron(int index){
    return outputArray[index];
  }

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

  public void prunning(int threshold){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && outputArray[i].netWeight < threshold){
        outputArray[i] = null;
      }
    }
  }

  public void prunning(int minSupport, int minConfidence){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && (outputArray[i].support < minSupport || outputArray[i].confidence < minConfidence)){
        outputArray[i] = null;
      }
    }
  }

  public boolean isNull(){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null){
        return false;
      }
    }
    return true;
  }

  public void increaseAccumulator(short classValue, int weight){  // classValue, para selecionar qual classe deve ser incrementado o acumulador
    if(outputArray[classValue] != null){                          // e o peso para incrementar
      outputArray[classValue].increaseAccumulator(weight);
    } else {
      outputArray[classValue] = new OutputNeuron(weight);
    }
  }
}