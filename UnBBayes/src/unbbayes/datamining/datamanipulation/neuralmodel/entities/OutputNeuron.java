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

  public void addCombination(Neuron combination){
    String combinationKey = combination.getKey();
    if(combinationsList.containsKey(combinationKey)){
      ((Arc)combinationsList.get(combinationKey)).accumulator ++;
    } else {
      combinationsList.put(combinationKey, new Arc(combination));
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
      if(tempArc.weigth < threshold){
        tempArc.combinationNeuron.prunning(this.key);
        combinationsList.remove(tempArc.combinationNeuron.key);
      }
    }
  }

  public int maxAccumulator(){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;
    int maxAccumulator = 0;

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      maxAccumulator = Math.max(maxAccumulator, tempArc.accumulator);
    }
    return maxAccumulator;
  }

  public int minAccumulator(){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;
    int minAccumulator = 0;

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      minAccumulator = Math.min(minAccumulator, tempArc.accumulator);
    }
    return minAccumulator;
  }

  public void calculateReliability(){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;
    long accumulatorsSum = 0;

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      if(tempArc.accumulator > 0){
        accumulatorsSum = accumulatorsSum + tempArc.accumulator;
      }
    }

    outputEnum = combinationsList.elements();
    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      tempArc.reliability = tempArc.accumulator / accumulatorsSum;
    }
  }


//////////////////////////////
  public void printClassValue(){
    System.out.println("Classe: " + attributeIndex + "\nValor: " + value );
    Enumeration enum = combinationsList.elements();
    Arc tempNeuron;
    while(enum.hasMoreElements()){
      tempNeuron = (Arc)enum.nextElement();
      if(tempNeuron.combinationNeuron instanceof InputNeuron){
        System.out.println("input neuron: " + tempNeuron.combinationNeuron.getKey() + " acc:" + tempNeuron.accumulator + " weight:" + tempNeuron.weigth);
      } else {
        System.out.println("comb neuron: " + tempNeuron.combinationNeuron.getKey() + " acc:" + tempNeuron.accumulator + " weight:" + tempNeuron.weigth);
      }
    }
  }

//////////////////////////////
/*  public void setSignal(int signal, Integer key){
    ((Arc)combinationsList.get(key)).
  }
*/

}