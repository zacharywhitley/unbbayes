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
  private Hashtable combinationsTable = new Hashtable();
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
    int maxOrder = /*maxOrder*/3;  //retirar isso, maxOrder será passado por parametro

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
    InputNeuron[] inputList;                                    //lista de todos os neuronios de entrada da instancia
    InputNeuron[] combInputList;                                //lista do neuronios de entrada da combinanção de ordem x
    CombinatorialNeuron combNeuron;
    OutputNeuron outputNeuron = (OutputNeuron)outputLayer.get(generateOutputKey(classIndex, instance.classValue()));
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

    boolean[][][] combinations = makeCombinations(inputList.length, maxOrder);

    for(int order=1; order<maxOrder+1; order++){                //faz as ligações de ordem 1
      if(order==1){
        for(int i=0; i<inputList.length; i++){
          inputList[i].addCombNeuron(outputNeuron);
          outputNeuron.addCombination(inputList[i]);
          System.out.println("peso " + instance.getWeight());
        }
      } else {                                                  //faz combinações de ordem > 1
        int combNum = combinations[order].length;
        for(int comb=0; comb<combNum; comb++){                  //para cada combinação
          combInputList = new InputNeuron[order];
          position = 0;
          for(int att=0; att<inputList.length; att++){          //para cada atributo
            if(combinations[order][comb][att] == true){         //se atributo é verdadeiro
              combInputList[position] = inputList[att];         //adiciona o atributo corresp da inputList na combList
              position++;
            }
          }

          key = generateCombKey(combInputList);                 //gera a chave da combinação

          if(!combinatorialLayer.containsKey(key)){             //se nao existe comb neuron cria um novo
            combNeuron = new CombinatorialNeuron(combInputList, outputNeuron, key);
            combinatorialLayer.put(key, combNeuron);
          } else {                                              //se já existir pega ele e adiciona uma saida
            combNeuron = (CombinatorialNeuron)combinatorialLayer.get(key);
            combNeuron.addOutputNeuron(outputNeuron);
          }

          outputNeuron.addCombination(combNeuron);              // adiciona o comb neuron no output da instancia

          for(int i=0; i<order; i++){                           // adiciona o comb neuron em todas as entradas da instancia
            combInputList[i].addCombNeuron(combNeuron);
          }
        }
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

  private boolean[][][] makeCombinations(int numAttributes, int maxOrder){
    if(combinationsTable.containsKey(new Integer(numAttributes))){      //verificaçao pra ver se já foi calculado para esse numero de atributos.
      return (boolean[][][])combinationsTable.get(new Integer(numAttributes));
    }

    boolean[][][] combTable = new boolean[maxOrder+1][][];              //primeira dimensão: ordem; segunda dimensão: quantidade de combinações; terceira dimensao: atributos
    boolean[][] combinations;

    for(int order=1; order<maxOrder+1; order++){
      if(order == 1){                                                   //gera as combinações de ordem 1
        combinations = new boolean[numAttributes][numAttributes];
        for(int j=0; j<numAttributes; j++){
          for(int i=0; i<numAttributes; i++){
            if(i==j){
              combinations[j][i] = true;
            }else{
              combinations[j][i] = false;
            }
          }
        }
        combTable[order] = combinations;
      } else {                                                          //gera as combinações de ordem maior q 1
        int prevCombNum = combTable[order-1].length;                    //numero de combinações da ordem anterior
        int possibleComb = possibleCombinations(numAttributes, order);  //numero de possiveis combinações para esta ordem
        combinations = new boolean[possibleComb][numAttributes];        //array de combinações para esta ordem
        boolean finished = false;                                       //variavel de controle
        int combCounter = 0;
        int attCounter;
        boolean repetition;

        for(int att=0; att<numAttributes; att++){                  //para cada atributo
          for(int i=0; i<prevCombNum; i++){                        //para cada combinação de ordem anterior
            if(!combTable[order-1][i][att]){                       //se falso,
              for(int k=0; k<numAttributes; k++){                  //cria uma nova combinação de ordem atual baseada na de ordem anterior
                combinations[combCounter][k] = combTable[order-1][i][k];
                combinations[combCounter][att] = true;
              }

              repetition = false;                                  //identificando repetição
              for(int comb2=0; comb2<combCounter; comb2++){        //para cada combinação de ordem atual já existente
                attCounter = 0;                                    //contador de atributos
                for(int att2=0; att2<numAttributes; att2++){       //para cada atributo da nova combinacao compara com as combinações já existentes
                  if(combinations[comb2][att2] == combinations[combCounter][att2]){
                    attCounter++;                                  //soma o numero de atributos iguais entre duas combinações
                  }
                }
                if(attCounter == numAttributes){                   //se o numero de atributos iguais for igual ao numero de atributos existentes
                  repetition = true;                               //sai do loop e seta repetição
                  break;
                }
              }
              if(!repetition){                                     //se não for repetição vai pra proxima combinacao
                combCounter++;
                if(combCounter == possibleComb){                   // quando atingir o numero de combinaçóes possíveis para
                  finished = true;
                  break;
                }
              }
            }
          }
          if(finished){                                            //se atingir o nr de comb possiveis pra esta ordem então para.
            break;
          }
        }
        combTable[order] = combinations;
      }
    }

    combinationsTable.put(new Integer(numAttributes), combTable);
    return combTable;
  }

  private int possibleCombinations(int numAttributes, int order){
    int numerator = 1;
    for (int i=numAttributes; i>order; i--){
      numerator = numerator * i;
    }

    int denominator = 1;
    for (int i=1; i<(numAttributes-order)+1; i++){
      denominator = denominator * i;
    }

    return numerator/denominator;
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