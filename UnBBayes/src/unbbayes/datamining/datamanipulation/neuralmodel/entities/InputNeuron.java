package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;
import java.io.*;

/**
 *  Class that defines how an input neuron must be and behave.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class InputNeuron extends Neuron implements Serializable{
  /**Index of the attribute this neuron describes*/
  private int attributeIndex;

  /**Value of the attribute this neuron describes*/
  private short value;

  /**List of the combinatorial neurons that are associated to this neuron*/
  private HashMap combinationsList = new HashMap();

  /**Activation state of the neuron*/
  private boolean enabled;

  /**
   * Constructs a new input neuron.
   *
   * @param attributeIndex the index of the attribute this neuron will describe.
   * @param value the value of the attribute this neuron will describe.
   * @param key the key that identifies this neuron solely.
   */
  public InputNeuron(int attributeIndex, short value, String key) {
    this.attributeIndex = attributeIndex;
    this.value = value;
    this.key = key;
  }

  /**
   * This method adds a new combinatorial neuron to the list of combinations
   * associated to this neuron.
   *
   * @param neuron the new combination, a combinatorial neuron or a single input neuron.
   */
  public void addCombNeuron(Neuron neuron){   //adiciona um neuronio combinatorial
    String neuronKey = neuron.getKey();
    if(!combinationsList.containsKey(neuronKey)){
      combinationsList.put(neuronKey, neuron);
    }
  }

  /**
   * Method used to prunne the network, removing the combinatorial neuron
   * with the received key.
   *
   * @param combinationKey the key of the combinatorial neuron to be removed.
   */
  public void prunning(String combinationKey){
    removeCombNeuron(combinationKey);
  }

  /**
   * Method used to remove a combinatorial neuron from this neuron combinatorial
   * list.
   *
   * @param combKey the key of the combinatorial neuron to be removd from this
   *                  neuron combinatorial list.
   */
  public void removeCombNeuron(String combKey){
    combinationsList.remove(combKey);
  }

  /**
   * Returns the index of the attribute this neuron describes.
   *
   * @return the index of the attribute this neuron describes.
   */
  public int getAttributeIndex(){
    return attributeIndex;
  }

  /**
   * Returns the value of the attribute this neuron describes.
   *
   * @return the value of the attribute this neuron describes.
   */
  public short getValue(){
    return value;
  }

  /**
   * Retuns the number of combinatorial neurons connected to this neuron.
   *
   * @return the number of combinatorial neurons connected to this neuron.
   */
  public int getCombinationsNum(){
    return combinationsList.size();
  }

  /**
   * Used to enable or disable the input neuron.
   *
   * @param enable <code>true</code> to activate the neuron;
   *               <code>false</code> otherwise.
   */
  public void setEnabled(boolean enable){
    this.enabled = enable;
  }

  /**
   * Used to verify if the neuron has been activated or not.
   *
   * @return <code>true</code> if the neuron has been activated;
   *         <code>false</code> otherwise.
   */
  public boolean getSignal(){
    return enabled;
  }
}