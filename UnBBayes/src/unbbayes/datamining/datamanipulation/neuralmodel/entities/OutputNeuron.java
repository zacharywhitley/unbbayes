package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;
import unbbayes.datamining.datamanipulation.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class OutputNeuron extends Neuron{

  private Hashtable combinationsList = new Hashtable();
  private int attributeIndex;
  private short value;

  public OutputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
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

  public void prunning(String key){}  //metodo declarado para satisfazer a classe abstrata pai
  public void prunning(int threshold){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;

    while(outputEnum.hasMoreElements()){
      tempArc = (Arc)outputEnum.nextElement();
      if(tempArc.weigth < threshold){
        tempArc.combinationNeuron.prunning(this.getKey());
        combinationsList.remove(tempArc.combinationNeuron.key);
      }
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