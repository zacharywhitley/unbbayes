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
  private boolean[][][] combinations;

  public CombinatorialNetwork(InstanceSet instanceSet) {
    this.instanceSet = instanceSet;
    generateNetwork();
  }

  public void generateNetwork(){
    Instance instance;
    Enumeration instanceEnum = instanceSet.enumerateInstances();
    int attributeNum = instanceSet.numAttributes();
    int classIndex = instanceSet.getClassIndex();


    int maxOrder = /*attributeNum-1*/3;  //retirar isso, maxOrder será passado por parametro
    /**/combinations = makeCombinations(attributeNum-1, maxOrder);

    while(instanceEnum.hasMoreElements()){
      instance = (Instance)instanceEnum.nextElement();

      createInputNeurons(instance, attributeNum, classIndex);
      createOutputNeuron(instance, classIndex);
      createCombinatorialNeurons(instance, attributeNum, classIndex, maxOrder);
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

  private void createCombinatorialNeurons(Instance instance, int attributeNum, int classIndex, int maxOrder){
    short value;
    InputNeuron inputNeuron;
    InputNeuron[] inputList = new InputNeuron[attributeNum - 1]; //lista de todos os neuronios de entrada da instancia
    InputNeuron[] combList;                                      //lista do neuronios da combinanção de ordem x
    CombinatorialNeuron combNeuron;
    OutputNeuron outputNeuron = (OutputNeuron)outputLayer.get(generateOutputKey(classIndex, instance.classValue()));
    String key;
//    int maxOrder = attributeNum;

    int position = 0;
    for(int att=0; att<attributeNum; att++){                    //cria uma lista com todas os neuronios de entrada associados a instancia do exemplo
      if(att != classIndex){
        value = instance.getValue(att);
        key = generateInputKey(att, value);
        inputNeuron = (InputNeuron)inputLayer.get(key);
        inputList[position] = inputNeuron;
        position++;
      }
    }

    for(int order=1; order<maxOrder+1; order++){                //faz as ligações de ordem 1
      if(order==1){
        for(int i=0; i<inputList.length; i++){
          inputList[i].addNeuron(outputNeuron);
          outputNeuron.addCombination(inputList[i]);
        }
      } else {                                                  //faz combinações de ordem > 1
        combList = new InputNeuron[order];
        int combNum = combinations[order].length;
        for(int comb=0; comb<combNum; comb++){                  //para cada combinação
          position = 0;
          for(int att=0; att<attributeNum-1; att++){            //para cada atributo
            if(combinations[order][comb][att] == true){         //se atributo é verdadeiro
              combList[position] = inputList[att];              //adiciona o atributo corresp da inputList na combList
              position++;
            }
          }

          key = generateCombKey(combList);                       //gera a chave da combinação
          if(!combinatorialLayer.containsKey(key)){              //se nao existe comb neuron cria um novo
            combNeuron = new CombinatorialNeuron(combList, outputNeuron, key);
            combinatorialLayer.put(key, combNeuron);
          } else {                                               //se já existir pega ele e adiciona uma saida
            combNeuron = (CombinatorialNeuron)combinatorialLayer.get(key);
            combNeuron.addOutputNeuron(outputNeuron);
          }

          outputNeuron.addCombination(combNeuron);               // adiciona o comb neuron no output da instancia

          for(int i=0; i<order; i++){                            // adiciona o comb neuron em todas as entradas da instancia
            combList[i].addNeuron(combNeuron);
          }
        }
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

  private boolean[][][] makeCombinations(int numAttributes, int maxOrder){
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
/*
    for(int i=1;i<maxOrder; i++){
      System.out.println("ordem:" + i);
      for(int j=0; j<combTable[i].length; j++){
        System.out.print("combinaçaõ:" + j + " ");
        for(int k=0; k<numAttributes; k++){
          System.out.print(combTable[i][j][k] + " ");
        }
        System.out.println();
      }
    }
*/

    return combTable;
  }

  private int possibleCombinations(int numAttributes, int order){
    long nFactorial = 1;
    for(int i=1; i<numAttributes+1; i++){
      nFactorial = nFactorial * i;
    }

    long pFactorial = 1;
    for(int i=1; i<order+1; i++){
      pFactorial = pFactorial * i;
    }

    long npFactorial = 1;
    for(int i=1; i<(numAttributes-order)+1; i++){
      npFactorial = npFactorial * i;
    }

    return (int)(nFactorial/(pFactorial * npFactorial));
  }

}
