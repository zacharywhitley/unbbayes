package unbbayes.datamining.datamanipulation.neuralmodel;


import java.util.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class CombinatorialNetwork {

  private Hashtable inputLayer = new Hashtable();
  private Hashtable combinatorialLayer = new Hashtable();
  private Hashtable outputLayer = new Hashtable();
  private InstanceSet instanceSet;

  public CombinatorialNetwork(InstanceSet instanceSet) {
    this.instanceSet = instanceSet;
    generateNetwork();
  }

  public void generateNetwork(){
    Instance instance;
    Enumeration instanceEnum = instanceSet.enumerateInstances();
    int attributeNum = instanceSet.numAttributes();
    int classIndex = instanceSet.getClassIndex();

    while(instanceEnum.hasMoreElements()){
      instance = (Instance)instanceEnum.nextElement();

      createInputNeurons(instance, attributeNum, classIndex);
      createOutputNeuron(instance, classIndex);
      createCombinatorialNeurons(instance, attributeNum, classIndex);
    }

    punishment();

//////////////////////////////////// impressao para teste
    for(int i=0; i<instanceSet.numAttributes(); i++){
      System.out.println("indice:" + i + " " + instanceSet.getAttribute(i).toString());
    }
    System.out.println("Classe:" + instanceSet.getClassAttribute().toString());

    Enumeration enum = outputLayer.elements();
    while(enum.hasMoreElements()){
      ((OutputNeuron)enum.nextElement()).printClassValue();
    }
////////////////////////////////////////

  }

  private void createInputNeurons(Instance instance, int attributeNum, int classIndex){
    short value;
    String key;
    InputNeuron inputNeuron;
    for(int att=0; att<attributeNum; att++){
      if(att != classIndex){
        value = instance.getValue(att);
        key = generateInputKey(att, value);
        if(!inputLayer.containsKey(key)){
          inputLayer.put(key, new InputNeuron(att, value, key));
        }
      }
    }
  }

  private void createOutputNeuron(Instance instance, int classIndex){
    short value = instance.classValue();
    String key = generateOutputKey(classIndex, value);
    if(!outputLayer.containsKey(key)){
      outputLayer.put(key, new OutputNeuron(classIndex, value, key));
    }
  }

  private void createCombinatorialNeurons(Instance instance, int attributeNum, int classIndex){
    short value;
    InputNeuron inputNeuron;
    InputNeuron[] inputList = new InputNeuron[attributeNum - 1];
    CombinatorialNeuron combNeuron;
    OutputNeuron outputNeuron = (OutputNeuron)outputLayer.get(generateOutputKey(classIndex, instance.classValue()));
    String key;
    int maxOrder = attributeNum;

    for(int att=0; att<attributeNum; att++){        //cria uma lista com todas os neuronios de entrada associados a instancia do exemplo
      if(att != classIndex){
        value = instance.getValue(att);
        key = generateInputKey(att, value);
        inputNeuron = (InputNeuron)inputLayer.get(key);
        inputList[att] = inputNeuron;
      }
    }

    for(int order=1; order<maxOrder+1; order++){
      if(order==1){
        for(int i=0; i<inputList.length; i++){
          inputList[i].addNeuron(outputNeuron);
          outputNeuron.addCombination(inputList[i]);
        }
      } else {                 //implementar um algoritmo que faça as combinações das entradas
//////////////////////
        if(order == maxOrder){
          key = generateCombKey(inputList);
          if(!combinatorialLayer.containsKey(key)){              // se nao existe comb neuron cria um novo
            combNeuron = new CombinatorialNeuron(inputList, outputNeuron, key);
            combinatorialLayer.put(key, combNeuron);
          } else {                                               // se já existir pega ele e adiciona uma saida
            combNeuron = (CombinatorialNeuron)combinatorialLayer.get(key);
            combNeuron.addOutputNeuron(outputNeuron);
          }

          outputNeuron.addCombination(combNeuron);               // adiciona o comb neuron no output da instancia

          int inputSize = inputList.length;
          for(int i=0; i<inputSize; i++){                        // adiciona o comb neuron em todas as entradas da instancia
            inputList[i].addNeuron(combNeuron);
          }

        }
///////////////////////
      }
    }

  }


  private String generateCombKey(InputNeuron[] inputList){
    int inputListSize = inputList.length;
    String stringKey = new String("comb");
    for(int i=0; i<inputListSize; i++){
      stringKey = stringKey.concat(inputList[i].getKey().toString());
    }
    return stringKey;
  }

  private String generateInputKey(int attribute, int value){
    return new String("in" + attribute + value);
  }

  private String generateOutputKey(int attribute, int value){
    return new String("out" + attribute + value);
  }

  private void punishment(){
    int outputNum = outputLayer.size();
    Hashtable[] outputs = new Hashtable[outputNum];
    Enumeration outputEnum = outputLayer.elements();

    int position=0;
    while(outputEnum.hasMoreElements()){
      outputs[position] = ((OutputNeuron)outputEnum.nextElement()).getCombinations();
      position++;
    }

    Arc arc;
    int sum = 0;
    String tempKey;

    for(int i=0; i<outputNum; i++){
      outputEnum = outputs[i].elements();
      while(outputEnum.hasMoreElements()){           //para cada combinação do neuronio i de saida
        arc = (Arc)outputEnum.nextElement();
        tempKey = arc.combinatorialNeuron.getKey();
        for(int j=0; j<outputNum; j++){
          if(j!=i && outputs[j].containsKey(tempKey)){
            sum += ((Arc)outputs[j].get(tempKey)).accumulator;
          }
        }
        arc.weigth = arc.accumulator - sum;
        sum = 0;
      }
    }
  }
}
