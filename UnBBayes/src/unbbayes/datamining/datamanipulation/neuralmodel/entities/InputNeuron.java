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

public class InputNeuron extends Neuron {

  private int attributeIndex;
  private short value;
  private Hashtable combinationsList = new Hashtable();

  public InputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
  }

  public void addNeuron(Neuron neuron){
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

  public int getCombinationsNum(){
    return combinationsList.size();
  }

/*  public void propagate(int signal){
    int size = combinatorialList.size();
    for(int i=0; i<size; i++){
      ((CombinatorialNeuron)combinatorialList.get(i)).setSignal(signal, key);
    }
  }
*/
}