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

    int position = -1;
    for(int att=0; att<attributeNum; att++){        //cria uma lista com todas os neuronios de entrada associados a instancia do exemplo
      if(att != classIndex){
        value = instance.getValue(att);
        key = generateInputKey(att, value);
        inputNeuron = (InputNeuron)inputLayer.get(key);
        position++;
        inputList[position] = inputNeuron;
      }
    }

    for(int order=1; order<maxOrder+1; order++){
      if(order==1){
        for(int i=0; i<inputList.length; i++){
          inputList[i].addNeuron(outputNeuron);
          outputNeuron.addCombination(inputList[i]);
        }
      } else {                 //implementar um algoritmo que fa�a as combina��es das entradas
//////////////////////
        if(order == maxOrder){
          key = generateCombKey(inputList);
          if(!combinatorialLayer.containsKey(key)){              // se nao existe comb neuron cria um novo
            combNeuron = new CombinatorialNeuron(inputList, outputNeuron, key);
            combinatorialLayer.put(key, combNeuron);
          } else {                                               // se j� existir pega ele e adiciona uma saida
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


/**/makeCombinations(attributeNum, maxOrder);

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
      while(outputEnum.hasMoreElements()){           //para cada combina��o do neuronio i de saida
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


  private void makeCombinations(int numAttributes, int maxOrder){
//    int possibleComb = ((int)Math.pow(2,numAttributes)) - 1;
    boolean[][][] combTable = new boolean[maxOrder+1][/*possibleComb*/][/*numAttributes*/];
    boolean[][] combinations;

    for(int order=1; order<maxOrder+1; order++){
      if(order == 1){
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
      } else {
        int comb = combTable[order-1].length; //possibleCombinations(numAttributes, order-1);
        int possibleComb = possibleCombinations(numAttributes, order);
        combinations = new boolean[possibleComb][numAttributes];
        boolean finished = false;

        int combCounter = 0;
        for(int att=0; att<numAttributes; att++){
          for(int i=0; i<comb; i++){
            if(!combTable[order-1][i][att]){
              for(int k=0; k<numAttributes; k++){
                combinations[combCounter][k] = combTable[order-1][i][k];
                combinations[combCounter][att] = true;
              }
              //identificando repeti��o
              boolean repetition = false;
              for(int comb2=0; comb2<combCounter; comb2++){
                int attCounter = 0;
                for(int att2=0; att2<numAttributes; att2++){
                  if(combinations[comb2][att2] == combinations[combCounter][att2]){
                    attCounter++;
                  }
                }
                if(attCounter == numAttributes){
                  repetition = true;
                  break;
                }
              }
              if(!repetition){  //se n�o for repeti��o vai pra proxima combinacao
                combCounter++;
                if(combCounter == possibleComb){
                  finished = true;
                  break;
                }
              }
            }
          }
          if(finished)
            break;
        }
        combTable[order] = combinations;
      }
    }

    for(int i=1;i<maxOrder; i++){
      System.out.println("ordem:" + i);
      for(int j=0; j<combTable[i].length; j++){
        System.out.print("combina�a�:" + j + " ");
        for(int k=0; k<numAttributes; k++){
          System.out.print(combTable[i][j][k] + " ");
        }
        System.out.println();
      }
    }

  }

  private int possibleCombinations(int numAttributes, int order){
    int nFactorial = 1;
    for(int i=1; i<numAttributes+1; i++){
      nFactorial = nFactorial * i;
    }

    int pFactorial = 1;
    for(int i=1; i<order+1; i++){
      pFactorial = pFactorial * i;
    }

    int npFactorial = 1;
    for(int i=1; i<(numAttributes-order)+1; i++){
      npFactorial = npFactorial * i;
    }

    return nFactorial/(pFactorial * npFactorial);
  }



}
