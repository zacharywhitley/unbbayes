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

public class InputNeuron extends Neuron implements Serializable{
  private int attributeIndex;
  private short value;
  private Hashtable combinationsList = new Hashtable();
  private boolean enabled;

  public InputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
  }

  public void addCombNeuron(Neuron neuron){   //adiciona um neuronio combinatorial
    String neuronKey = neuron.getKey();
    if(!combinationsList.containsKey(neuronKey)){
      combinationsList.put(neuronKey, neuron);
    }
  }

  public void prunning(String combinationKey){
    removeCombNeuron(combinationKey);
  }

  public void removeCombNeuron(String combKey){
    combinationsList.remove(combKey);
  }

  public int getAttributeIndex(){
    return attributeIndex;
  }

  public short getValue(){
    return value;
  }

  public int getCombinationsNum(){
    return combinationsList.size();
  }

  public void setEnabled(boolean enable){
    this.enabled = enable;
  }

  public boolean getSignal(){
    return enabled;
  }
}