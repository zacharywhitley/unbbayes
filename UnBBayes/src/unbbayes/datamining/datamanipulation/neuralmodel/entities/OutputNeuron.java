package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;
import java.io.*;

/**
 *  Class that defines how an output neuron must be and behave.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class OutputNeuron extends Neuron implements Serializable{
  /**Index of the class attribute this neuron describes*/
  private int attributeIndex;

  /**Value of the class attribute this neuron describes*/
  private short value;

  /**List of the combinatorial neurons that are associated to this neuron*/
  private HashMap combinationsList = new HashMap();

  /**
   * Constructs a new output neuron.
   *
   * @param attributeIndex the index of the class attribute this neuron will describe.
   * @param value the value of the class attribute this neuron will describe.
   * @param key the key that identifies this neuron solely.
   */
  public OutputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
  }

  /**
   * Returns the index of the class attribute this neuron describes.
   *
   * @return the index of the class attribute this neuron describes.
   */
  public int getAttributeIndex(){
    return attributeIndex;
  }

  /**
   * Returns the value of the class attribute this neuron describes.
   *
   * @return the value of the class attribute this neuron describes.
   */
  public short getValue(){
    return value;
  }

  /**
   * This method adds a new combinatorial neuron to the list of combinations
   * associated to this output neuron.
   *
   * @param combination the new combination, a combinatorial neuron or a single input neuron.
   * @param weight the weight of new combinations arc.
   */
  public void addCombination(Neuron combination, int weight){
    String combinationKey = combination.getKey();
    if(combinationsList.containsKey(combinationKey)){
      Arc tempArc = ((Arc)combinationsList.get(combinationKey));
      tempArc.accumulator = tempArc.accumulator + weight;
    } else {
      combinationsList.put(combinationKey, new Arc(combination, weight));
    }
  }

  /**
   * Returns the combinations associated to this output neuron.
   *
   * @return the list of combinations associated to this output neuron.
   */
  public Map getCombinations(){
    return combinationsList;
  }

  /**
   * Returns an enumeration of the combinations associated to this output neuron.
   *
   * @return the enumeration of the combinations associated to this output neuron.
   */
  public Iterator getCombinationsEnum(){
    return combinationsList.values().iterator();
  }

  public void prunning(String key){}  //metodo declarado para satisfazer a classe abstrata pai

  /**
   * Method used to prunne the network based on a threshold, prunning
   * the arcs with accumulator below this limit.
   *
   * @param threshold the limit value of the arc's accumulators.
   */
  public void prunning(int threshold){
    Iterator outputEnum = combinationsList.values().iterator();
    Arc tempArc;

    while(outputEnum.hasNext()){
      tempArc = (Arc)outputEnum.next();
      if(tempArc.netWeight < threshold){
        tempArc.combinationNeuron.prunning(this.key);
        combinationsList.remove(tempArc.combinationNeuron.key);
      }
    }
  }

  /**
   * Method used to prunne the network based on the minimum confidence
   * and the minimum support entered by the user, prunning
   * the arcs with support and confidence below these limits.
   *
   * @param minConfidence the minimum confidence allowed.
   * @param minSupport the minimum support allowed.
   */
  public void prunning(int minConfidence, int minSupport){
    Iterator outputEnum = combinationsList.values().iterator();
    Arc tempArc;

    while(outputEnum.hasNext()){
      tempArc = (Arc)outputEnum.next();
      if(tempArc.support < minSupport || tempArc.confidence < minConfidence){
        tempArc.combinationNeuron.prunning(this.key);
        combinationsList.remove(tempArc.combinationNeuron.key);
      }
    }
  }

  /**
   * Method used to calculate the support of all the arcs arriving to this
   * neuron.
   *
   * @param numOfInstances the total number of instaces of the training set.
   */
  public void calculateSupport(int numOfInstances){
    Iterator outputEnum = combinationsList.values().iterator();
    Arc tempArc;

    while(outputEnum.hasNext()){
      tempArc = (Arc)outputEnum.next();
      tempArc.support = ((float)tempArc.accumulator / (float)numOfInstances) * 100;
    }
  }

/*  public void calculateConfidence(){
    Enumeration outputEnum = combinationsList.elements();
    Arc tempArc;
    long netWeigthSum = 0;

  }*/

  public boolean getSignal(){return false;}  //??????????????????????

/*
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
*/

  /**
   * Used to make inferences on the model, after the activation of the input
   * neurons of the instace to classify.
   *
   * @return the arc with greater weight and signal equals to true.
   */
  public Arc classify(){
    Arc tempArc, returnArc = null;
    Iterator combEnum = combinationsList.values().iterator();
    boolean signal;
    int result = -1;

    while(combEnum.hasNext()){
      tempArc = (Arc)combEnum.next();
      signal = tempArc.getCombinationNeuron().getSignal();
      if(signal && tempArc.getNetWeight() >= result){                           //implementacao do OR, max(weight*sinal)
        result = tempArc.getNetWeight();
        returnArc = tempArc;
      } else if(!signal && returnArc == null && result < 0 && tempArc.getNetWeight() > -1){   //conferir como o hércules
        result = 0;                //RETIRAR ISSO E RETORNA NULL
        returnArc = tempArc;
      }
    }
    return returnArc;
  }

}