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

public class CombinatorialNetworkConstructor{
  private Hashtable inputLayer = new Hashtable();
  private Hashtable combinatorialLayer = new Hashtable();
  private Hashtable outputLayer = new Hashtable();
/**/  private Hashtable combinationsTable = new Hashtable();
  private InstanceSet instanceSet;

  public CombinatorialNetworkConstructor(InstanceSet instanceSet) {
    this.instanceSet = instanceSet;
  }

  public CombinatorialNetwork generateNetwork(/*int threshold, int reliability, int maxOrder*/){
    Instance instance;
    Enumeration instanceEnum = instanceSet.enumerateInstances();
    Enumeration outputEnum;
    int attributeNum = instanceSet.numAttributes();
    int classIndex = instanceSet.getClassIndex();
    int maxOrder = 3;  //retirar isso, maxOrder será passado por parametro

    while(instanceEnum.hasMoreElements()){
      instance = (Instance)instanceEnum.nextElement();

      createInputNeurons(instance, attributeNum, classIndex);
      createOutputNeuron(instance, classIndex);
      createCombinatorialNeurons(instance, attributeNum, classIndex, maxOrder);
    }

    punishment();
//    prunning(/*threshold*/1);   //modificar, passar o threshold definido pelo usuario.

    outputEnum = outputLayer.elements();
    OutputNeuron output;
    while(outputEnum.hasMoreElements()){
      output = (OutputNeuron)outputEnum.nextElement();
      output.calculateReliability();
    }

    return new CombinatorialNetwork(inputLayer, combinatorialLayer, outputLayer);
  }

  private void createInputNeurons(Instance instance, int attributeNum, int classIndex){
    short value;
    String key;
    InputNeuron inputNeuron;
    for(int att=0; att<attributeNum; att++){
      if(att != classIndex && !instance.isMissing(att)){
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

  private void createCombinatorialNeurons(Instance instance, int attributeNum, int classIndex, int maxOrder){
    short value;
    int combinationsSize;
    InputNeuron[] inputList;                                    //lista de todos os neuronios de entrada da instancia
    InputNeuron[] tempInputList;
    CombinatorialNeuron combNeuron;
    OutputNeuron outputNeuron = (OutputNeuron)outputLayer.get(generateOutputKey(classIndex, instance.classValue()));
    ArrayList combinations;
    String key;

    int missingAttNum = 0;
    for(int att=0; att<attributeNum; att++){
      if(instance.isMissing(att)){
        missingAttNum++;
      }
    }
    inputList = new InputNeuron[attributeNum - (missingAttNum + 1)]; //soma um do atributo de classe

    int position = 0;
    for(int att=0; att<attributeNum; att++){                    //cria uma lista com todas os neuronios de entrada associados a instancia do exemplo
      if(att != classIndex && !instance.isMissing(att)){
        value = instance.getValue(att);
        key = generateInputKey(att, value);
        inputList[position] = (InputNeuron)inputLayer.get(key);
        position++;
      }
    }

    if(inputList.length < maxOrder){
      maxOrder = inputList.length;
    }

    combinations = makeCombinations(inputList, maxOrder);     //cria todas as combinações dos neuronios de entrada
    combinationsSize = combinations.size();

    for(int i=0; i<combinationsSize; i++){                    //para cada combinaçao
      tempInputList = (InputNeuron[])combinations.get(i);
      if(tempInputList.length == 1){                          //se combinação de ordem 1 então
        tempInputList[0].addCombNeuron(outputNeuron);
        outputNeuron.addCombination(tempInputList[0], instance.getWeight());
      } else {                                                //se combinação de ordem maior q 1 então
        key = generateCombKey(tempInputList);                 //gera a chave da combinação
        if(!combinatorialLayer.containsKey(key)){             //se nao existe comb neuron com esta chave cria um novo
          combNeuron = new CombinatorialNeuron(tempInputList, outputNeuron, key);
          combinatorialLayer.put(key, combNeuron);
          for(int j=0; j<tempInputList.length; j++){          // adiciona o comb neuron em todas as entradas da instancia
            tempInputList[j].addCombNeuron(combNeuron);
          }
        } else {                                              //se já existir pega ele e adiciona a saida da instancia
          combNeuron = (CombinatorialNeuron)combinatorialLayer.get(key);
          combNeuron.addOutputNeuron(outputNeuron);
        }
        outputNeuron.addCombination(combNeuron, instance.getWeight());  // adiciona o comb neuron no output da instancia
      }
    }
  }

  private String generateCombKey(InputNeuron[] inputList){
    int inputListSize = inputList.length;
    String stringKey = new String("c");
    for(int i=0; i<inputListSize; i++){
      stringKey = stringKey + inputList[i].getKey();
    }
    return stringKey;
  }

  private String generateInputKey(int attribute, short value){
    return new String("i" + attribute + value);
  }

  private String generateOutputKey(int attribute, short value){
    return new String("o" + attribute + value);
  }

  private ArrayList makeCombinations(InputNeuron[] inputArray, int maxOrder){
    InputNeuron[] tempInputArray, temp;
    ArrayList combinations = new ArrayList();
    int inputSize = inputArray.length;
    int combArraySize, tempSize;

    for(int inputNum=0; inputNum<inputSize; inputNum++){      //para todos os neuronios de entrada
      combArraySize = combinations.size();                    //pega o tamanho do array de combinações

      for(int j=0; j<combArraySize; j++){                     //para todas as combinações já existentes
        temp = (InputNeuron[])combinations.get(j);
        tempSize = temp.length;                               //pega o tamanho da combinação

        if(tempSize < maxOrder){                              //se tamanho da combinação < ordem máxima
          tempInputArray = new InputNeuron[tempSize + 1];     //cria nova combinação
          tempInputArray[tempSize] = inputArray[inputNum];    //adiciona o neuronio de entrada atual
          for(int k=0; k<tempSize; k++){                      //copia o resto da combinação atual
            tempInputArray[k] = temp[k];
          }
          combinations.add(tempInputArray);                   //adiciona nova combinação no array de combinacoes
        }
      }
      tempInputArray = new InputNeuron[1];                    //cria nova combinação de um elemento
      tempInputArray[0] = inputArray[inputNum];               //coloca o neuronio de entrada atual nesta combinação
      combinations.add(tempInputArray);                       //adiciona nova combinação no array de combinacoes
    }
    return combinations;
  }

  private void punishment(){
    Arc arc;
    String tempKey;
    int sum = 0;
    int outputNum = outputLayer.size();
    Hashtable[] outputs = new Hashtable[outputNum];
    Enumeration outputEnum = outputLayer.elements();

    int position = 0;
    while(outputEnum.hasMoreElements()){
      outputs[position] = ((OutputNeuron)outputEnum.nextElement()).getCombinations();
      position++;
    }

    for(int i=0; i<outputNum; i++){
      outputEnum = outputs[i].elements();
      while(outputEnum.hasMoreElements()){           //para cada combinação do neuronio i de saida
        arc = (Arc)outputEnum.nextElement();
        tempKey = arc.getCombinationNeuron().getKey();
        for(int j=0; j<outputNum; j++){
          if(j!=i && outputs[j].containsKey(tempKey)){
            sum += ((Arc)outputs[j].get(tempKey)).getAccumulator();
          }
        }
        arc.setWeight(arc.getAccumulator() - sum);
        sum = 0;
      }
    }
  }

  private void prunning(int threshold){
    Enumeration outputEnum;
    Enumeration combEnum;
    Enumeration inputEnum;
    OutputNeuron tempOutput;
    CombinatorialNeuron tempComb;
    InputNeuron tempInput;

    outputEnum = outputLayer.elements();
    while(outputEnum.hasMoreElements()){
      tempOutput = (OutputNeuron)outputEnum.nextElement();
      tempOutput.prunning(threshold);
    }

    combEnum = combinatorialLayer.elements();
    while(combEnum.hasMoreElements()){
      tempComb = (CombinatorialNeuron)combEnum.nextElement();
      if(tempComb.getInputCombinationsNum() == 0){
        combinatorialLayer.remove(tempComb.getKey());
      }
    }

    inputEnum = inputLayer.elements();
    while(inputEnum.hasMoreElements()){
      tempInput = (InputNeuron)inputEnum.nextElement();
      if(tempInput.getCombinationsNum() == 0){
        inputLayer.remove(tempInput.getKey());
      }
    }
  }
}