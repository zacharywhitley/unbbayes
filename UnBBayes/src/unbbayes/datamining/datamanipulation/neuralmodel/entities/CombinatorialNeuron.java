package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.util.*;
import java.io.*;

/**
 *  Class that defines how a combinatorial neuron must be and behave.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class CombinatorialNeuron extends Neuron implements Serializable{
  /**Array of input neurons that represent this neuron combination*/
  private InputNeuron[] inputList;

  /**List of output neurons that are connected to this combination neuron*/
  private HashMap outputList = new HashMap();

  /**
   * Constructs a new combinatorial neuron.
   *
   * @param inputList the list of input neurons that is the representation
   *                  of this neuron's combination.
   * @param outputNeuron the output neuron this combinatorial neuron is
   *                     connected to.
   * @param key the key that identifies this neuron solely.
   */
  public CombinatorialNeuron(InputNeuron[] inputList, OutputNeuron outputNeuron, String key) {
    this.inputList = inputList;
    outputList.put(outputNeuron.getKey(), outputNeuron);
    this.key = key;
  }

  /**
   * Adds a new connection to a different output neuron.
   *
   * @param outputNeuron the new output neuron.
   */
  public void addOutputNeuron(OutputNeuron outputNeuron){
    String outputKey = outputNeuron.getKey();

    if(!outputList.containsKey(outputKey)){
      outputList.put(outputKey, outputNeuron);
    }
  }

  /**
   * Method used to prunne the network.
   *
   * @param outputKey the key of the output neuron to be removd from this
   *                  neuron output list.
   */
  public void prunning(String outputKey){
    removeOutputNeuron(outputKey);
  }

  /**
   * Method used to remove an output neuron from this neuron output list,
   * verifying the size of the list and taking the necessary attitudes.
   *
   * @param outputKey the key of the output neuron to be removd from this
   *                  neuron output list.
   */
  public void removeOutputNeuron(String outputKey){
    outputList.remove(outputKey);                     //nao estou fazendo verificaçao pra ver se retirou mesmo.
    if(outputList.size() == 0){
      int inputListSize = inputList.length;
      for(int i=0; i<inputListSize; i++){
        inputList[i].prunning(this.key);
      }
    }
  }

  /**
   * Used to verify if the neuron has been activated or not.
   *
   * @return <code>true</code> if the neuron has been activated;
   *         <code>false</code> otherwise.
   */
  public boolean getSignal(){                 //implementação do AND
    int inputSize = inputList.length;
    boolean signal = true;
    for(int i=0; i<inputSize; i++){
      signal = signal && inputList[i].getSignal();
    }
    return signal;
  }

  /**
   * Returns an enumeration with the output neurons associated to this
   * combinatorial neuron.
   *
   * @returns the output neurons enumeration.
   */
  public Iterator getOutputList(){
    return outputList.values().iterator();
  }

  /**
   * Returns a list of input neurons.
   *
   * @returns a list of input neurons
   */
  public InputNeuron[] getInputList(){
    return inputList;
  }

  /**
   * Returns the number of input neurons that are combined by this neuron.
   *
   * @returns the number of input neurons associated to this neuron
   */
  public int getInputCombinationsNum(){
    return inputList.length;
  }

  /**
   * Returns the number of output neurons pointed by this neuron.
   *
   * @returns the number of output neurons associated to this neuron
   */
  public int getOutputCombinationsNum(){
    return outputList.size();
  }
}