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

public class CombinatorialNeuron extends Neuron{
  private InputNeuron[] inputList;
  private Hashtable outputList = new Hashtable();
//  private int signalValue = 1;
//  private int inputCounter = 0;

  public CombinatorialNeuron(InputNeuron[] inputList, OutputNeuron outputNeuron, String key) {
    this.inputList = inputList;
    outputList.put(outputNeuron.getKey(), outputNeuron);
    this.key = key;
  }

  public void addOutputNeuron(OutputNeuron outputNeuron){
    String outputKey = outputNeuron.getKey();

    if(!outputList.containsKey(outputKey)){
      outputList.put(outputKey, outputNeuron);
    }
  }

  public void prunning(String outputKey){
    removeOutputNeuron(outputKey);
  }

  public void removeOutputNeuron(String outputKey){
    outputList.remove(outputKey);                     //nao estou fazendo verificaçao pra ver se retirou mesmo.
    if(outputList.size() == 0){
      int inputListSize = inputList.length;
      for(int i=0; i<inputListSize; i++){
        inputList[i].prunning(this.key);
      }
    }
  }

  /** Retorna uma enumeracao com os neuronios de saida associados a este neuronio combinatorial */
  public Enumeration getOutputList(){
    return outputList.elements();
  }

  /** Retorn a lista de neuronios de entrada */
  public InputNeuron[] getInputList(){
    return inputList;
  }

  /** Retorna a quantidade de neuronios de entrada que são combinados por este neuronio */
  public int getInputCombinationsNum(){
    return inputList.length;
  }

  /** Retorna a quantidade de neuronios de saida apontados por este neuronio */
  public int getOutputCombinationsNum(){
    return outputList.size();
  }

/*  public void setSignal(int signalValue, Integer originKey){
    inputCounter ++;
    if(this.signalValue > signalValue){
      this.signalValue = signalValue;
    }
    if(signalValue == 0 && inputCounter == inputList.length){
      propagate(signalValue);
      inputCounter = 0;
      signalValue = 1;   // analizar as implicações desta linha
    }
  }

  public void propagate(int signal){
    int size = outputList.size();
    for(int i=0; i<size; i++){
      ((OutputNeuron)outputList.get(i)).setSignal(signal, key);
    }
  }
*/

}