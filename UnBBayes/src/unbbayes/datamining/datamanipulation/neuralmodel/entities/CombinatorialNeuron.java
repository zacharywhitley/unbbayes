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

  public boolean getSignal(){                 //implementação do AND
    int inputSize = inputList.length;
    boolean signal = true;
    for(int i=0; i<inputSize; i++){
      signal = signal && inputList[i].getSignal();
    }
    return signal;
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
}