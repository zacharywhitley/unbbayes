package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class OutputNeuron extends Neuron implements Serializable{
  private int attributeIndex;
  private short value;
  private Hashtable combinationsList = new Hashtable();

  public OutputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
  }

  public int getAttributeIndex(){
    return attributeIndex;
  }

  public short getValue(){
    return value;
  }

  public void addCombination(Neuron combination, int weight){
    String combinationKey = combination.getKey();
    if(combinationsList.containsKey(combinationKey)){
      Arc tempArc = ((Arc)combinationsList.get(combinationKey));
      tempArc.accumulator = tempArc.accumulator + weight;
    } else {
      combinationsList.put(combinationKey, new Arc(combination, weight));
    }
  }

  public Hashtable getCombinations(){
    return combinationsList;
  }

  public Enumeration getCombinationsEnum(){
    return combinationsList.elements();
  }

  public void prunning(String key){}  //metodo declarado para satisfazer a classe abstrata pai

  public void prunning(int threshold){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      if(tempArc.netWeigth < threshold){
        tempArc.combinationNeuron.prunning(this.key);
        combinationsList.remove(tempArc.combinationNeuron.key);
      }
    }
  }

  public void calculateSupport(int numOfInstances){
    Enumeration outputEnum;
    Arc tempArc;

    outputEnum = combinationsList.elements();
    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      tempArc.support = ((float)tempArc.accumulator / (float)numOfInstances) * 100;
    }
  }

/*  public void calculateConfidence(){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;
    long netWeigthSum = 0;

  }*/

  public boolean getSignal(){return false;}  //??????????????????????

  public int classify(){
    Arc tempArc;
    Enumeration combEnum = combinationsList.elements();
    boolean signal;
    int result = 0;

    while(combEnum.hasMoreElements()){
      tempArc = (Arc)combEnum.nextElement();
      signal = tempArc.getCombinationNeuron().getSignal();
      if(signal){                           //implementacao do OR, max(weight*sinal)
        result = Math.max(result, tempArc.getNetWeigth());
      }
    }
    return result;
  }
}