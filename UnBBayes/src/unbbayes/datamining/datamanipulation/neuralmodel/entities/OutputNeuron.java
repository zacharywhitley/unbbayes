package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class OutputNeuron extends Neuron{
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

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      if(tempArc.netWeigth > 0){
        netWeigthSum = netWeigthSum + tempArc.netWeigth;
      }
    }

    outputEnum = combinationsList.elements();
    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      if(tempArc.netWeigth > 0){
        tempArc.confidence = ((float)tempArc.netWeigth / (float)netWeigthSum) * 100;
      }
    }
  }*/
}