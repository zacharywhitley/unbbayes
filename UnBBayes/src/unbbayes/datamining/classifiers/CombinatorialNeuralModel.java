package unbbayes.datamining.classifiers;

import java.util.*;
import java.io.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;

/**
 *  Class that implements the Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class CombinatorialNeuralModel extends BayesianLearning implements Serializable{
  /**The model's input layer.*/
  private Hashtable inputLayer = new Hashtable();

  /**The model's combinatorial layer.*/
  private Hashtable combinatorialLayer = new Hashtable();

  /**The model's output layer.*/
  private Hashtable outputLayer = new Hashtable();

  /**Vector that contains the attributes of the training set.*/
  private Attribute[] attributeVector;

  /**Index of the class attribute.*/
  private int classIndex;

  /**Value of the minimum support after prunning.*/
  private int support;

  /**Value of the minimum confidence after prunning.*/
  private int confidence;

  /**Value of the maximum order of combinations allowed.*/
  private transient int maxOrder;

  /**The set of instances of the training set*/
  private transient InstanceSet instanceSet;

  public CombinatorialNeuralModel(int maxOrder) {
    this.maxOrder = maxOrder;
  }

  /**
   * Builds the Combinatorial Neural Model classifier (CNM).
   *
   * @param data The training data
   * @exception Exception if classifier can't be built successfully
   */
  public void buildClassifier(InstanceSet instanceSet) throws Exception{
    this.instanceSet = instanceSet;
    Instance instance;
    Enumeration instanceEnum = instanceSet.enumerateInstances();
    Enumeration outputEnum;
    OutputNeuron output;
    int attributeNum = instanceSet.numAttributes();
    int numOfInstances = instanceSet.numWeightedInstances();

    createAttributeVector();                                //cria um array com os atributos para serialização
    this.classIndex = instanceSet.getClassIndex();           //guarda o indice da classa para serialização

    while(instanceEnum.hasMoreElements()){
      instance = (Instance)instanceEnum.nextElement();

      createInputNeurons(instance, attributeNum, classIndex);
      createOutputNeuron(instance, classIndex);
      createCombinatorialNeurons(instance, attributeNum, classIndex, maxOrder);
    }

    punishment();

    outputEnum = outputLayer.elements();
    while(outputEnum.hasMoreElements()){
      output = (OutputNeuron)outputEnum.nextElement();
      output.calculateSupport(numOfInstances);   // da pra fazer no método punishment
//      output.calculateConfidence();
    }
  }

  /**
   * Creates the vector that contains the attributes of the training set.
   */
  private void createAttributeVector(){
    int numAttributes = instanceSet.numAttributes();
    attributeVector = new Attribute[numAttributes];

    for(int att=0; att<numAttributes; att++){
      attributeVector[att] = instanceSet.getAttribute(att);
    }
  }

  /**
   * Creates the input neurons that composes the model.
   *
   * @param instance the instance to be processed
   * @param attributeNum the number of attributes of the instance
   * @param classIndex the index of the class attribute of the instance
   */
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

  /**
   * Creates the output neurons that composes the model.
   *
   * @param instance the instance to be processed
   * @param classIndex the index of the class attribute of the instance
   */
  private void createOutputNeuron(Instance instance, int classIndex){
    short value = instance.classValue();
    String key = generateOutputKey(classIndex, value);
    if(!outputLayer.containsKey(key)){
      outputLayer.put(key, new OutputNeuron(classIndex, value, key));
    }
  }

  /**
   * Creates the combinatorial neurons that composes the model.
   *
   * @param instance the instance to be processed
   * @param attributeNum the number of attributes of the instance
   * @param classIndex the index of the class attribute of the instance
   * @param maxOrder the maximum order of combinations of attributes allowed
   */
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

  /**
   * Creates a unique key for a combinatorial neuron based on the combination
   * that the combinatorial neuron implements.
   *
   * @param inputList the list of input neurons that represents the combination
   *        implemented by combinatorial neuron
   * @return the generated key
   */
  private String generateCombKey(InputNeuron[] inputList){
    int inputListSize = inputList.length;
    String stringKey = new String("c");
    for(int i=0; i<inputListSize; i++){
      stringKey = stringKey + inputList[i].getKey();
    }
    return stringKey;
  }

  /**
   * Creates a unique key for an input neuron based on it's attribute
   * and value.
   *
   * @param attribute the index of the attribute in the training set
   * @param value the value of the attribute
   * @return the generated key
   */
  private String generateInputKey(int attribute, short value){
    return new String("i" + attribute + value);
  }

  /**
   * Creates a unique key for an output neuron based on it's attribute
   * and value.
   *
   * @param attribute the index of the attribute in the training set
   * @param value the value of the attribute
   * @return the generated key
   */
  private String generateOutputKey(int attribute, short value){
    return new String("o" + attribute + value);
  }

  /**
   * Creates all possible combinations of input neurons with order limited
   * by the maxOrder parameter.
   *
   * @param inputArray the array with the input neurons to be combined
   * @param maxOrder the maximum order of combinations allowed
   * @return the generated combinations of input neurons
   */
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
          System.arraycopy(temp, 0, tempInputArray, 0, tempSize); //copia o resto da combinação atual
          combinations.add(tempInputArray);                   //adiciona nova combinação no array de combinacoes
        }
      }
      tempInputArray = new InputNeuron[1];                    //cria nova combinação de um elemento
      tempInputArray[0] = inputArray[inputNum];               //coloca o neuronio de entrada atual nesta combinação
      combinations.add(tempInputArray);                       //adiciona nova combinação no array de combinacoes
    }
    return combinations;
  }

  /**
   * Punishes the model after the training phase, calculating the final weight
   * of the arcs.
   */
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
        arc.setNetWeight(arc.getAccumulator() - sum);       //netWeight
        arc.setConfidence((float)arc.getAccumulator() * 100/(float)(sum + arc.getAccumulator())); //confidence
        sum = 0;
      }
    }
  }

  /**
   * Makes the pruning of the model after the punishment phase based on a
   * determined threshold.
   *
   * @param threshold the minimum weight value that the arcs
   *                  must be prunned
   */
  public void prunning(int threshold){
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

  /**
   * Makes the pruning of the model after the punishment phase based on the
   * minimum support and the minimum confidence of the arcs.
   *
   * @param minSupport the minimum support that the arcs must be prunned
   * @param minConfidence the minimum confidence that the arcs
   *                      must be prunned
   */
  public void prunning(int minSupport, int minConfidence){
    Enumeration outputEnum;
    Enumeration combEnum;
    Enumeration inputEnum;
    OutputNeuron tempOutput;
    CombinatorialNeuron tempComb;
    InputNeuron tempInput;

    outputEnum = outputLayer.elements();
    while(outputEnum.hasMoreElements()){
      tempOutput = (OutputNeuron)outputEnum.nextElement();
      tempOutput.prunning(minConfidence, minSupport);
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

    this.support = minSupport;
    this.confidence = confidence;
  }

/*
  public float[] distributionForInstance(Instance instance) throws Exception{
    Enumeration inputEnum, outputEnum;
    InputNeuron tempInput;
    OutputNeuron tempOutput;
    int numAtt = attributeVector.length;
    short value;
    String key;
    float[] distribution = new float[outputLayer.size()];

    inputEnum = inputLayer.elements();
    while(inputEnum.hasMoreElements()){                  //deixa todos os neuronios de entrada igual a false
      tempInput = (InputNeuron)inputEnum.nextElement();
      tempInput.setEnabled(false);
    }

    for(int att=0; att<numAtt; att++){
      if(att != classIndex && !instance.isMissing(att)){
        value = instance.getValue(att);
        key = generateInputKey(att, value);             //cria a chave atributo-valor da entrada

        if(inputLayer.containsKey(key)){                //faz a propagação
          ((InputNeuron)inputLayer.get(key)).setEnabled(true);
        }
      }
    }

    outputEnum = outputLayer.elements();
    while(outputEnum.hasMoreElements()){
      tempOutput = (OutputNeuron)outputEnum.nextElement();
      distribution[tempOutput.getValue()] = tempOutput.classify();
    }

    return distribution;
  }
*/

  /**
   * Make an inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array that contains the arc with greater weight of each
   *         output neuron.
   */
  public Arc[] inference(Instance instance){
    Enumeration inputEnum, outputEnum;
    InputNeuron tempInput;
    OutputNeuron tempOutput;
    int numAtt = attributeVector.length;
    short value;
    String key;
    Arc[] arcVector = new Arc[outputLayer.size()];      //vetor que contem os arcos de maior peso de cada neuronio

    inputEnum = inputLayer.elements();
    while(inputEnum.hasMoreElements()){                  //deixa todos os neuronios de entrada igual a false
      tempInput = (InputNeuron)inputEnum.nextElement();
      tempInput.setEnabled(false);
    }

    for(int att=0; att<numAtt; att++){
      if(att != classIndex && !instance.isMissing(att)){
        value = instance.getValue(att);
        key = generateInputKey(att, value);             //cria a chave atributo-valor da entrada

        if(inputLayer.containsKey(key)){                //faz a propagação
          ((InputNeuron)inputLayer.get(key)).setEnabled(true);
        }
      }
    }

    outputEnum = outputLayer.elements();
    Arc tempArc;
    while(outputEnum.hasMoreElements()){
      tempOutput = (OutputNeuron)outputEnum.nextElement();
      tempArc = tempOutput.classify();
      arcVector[tempOutput.getValue()] = tempArc;
    }

    return arcVector;
  }

  /**
   * Make an inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array of floats with the distribution of values for the given instance.
   * @throws Exception if classifier can't carry through the inference successfully
   */
  public float[] distributionForInstance(Instance instance) throws Exception{
    float[] distribution;
    Arc[] arcVector = inference(instance);
    distribution = new float[arcVector.length];
    for(int i=0; i<distribution.length; i++){
      distribution[i] = arcVector[i].getNetWeight();
    }
    return distribution;
  }

  /**
   * Outputs the model's input layer.
   *
   * @return the model's input layer.
   */
  public Hashtable getInputLayer(){
    return inputLayer;
  }

  /**
   * Outputs the model's output layer.
   *
   * @return the model's output layer.
   */
  public Hashtable getOutputLayer(){
    return outputLayer;
  }

  /**
   * Outputs the model's combinatorial layer.
   *
   * @return the model's combinatorial layer.
   */
  public Hashtable getCombinatorialLayer(){
    return combinatorialLayer;
  }

  /**
   * Outputs an array of attributes with the attributes of the training set.
   *
   * @return an attribute array.
   */
  public Attribute[] getAttributeVector(){
    return attributeVector;
  }

  /**
   * Outputs the index of the class attribute of the training set.
   *
   * @return the index of the class attribute
   */
  public int getClassIndex(){
    return classIndex;
  }

  /**
   * Outputs the minimum confidence of the model after the prunning phase.
   *
   * @return the minimum confidence.
   */
  public int getConfidence(){
    return confidence;
  }

  /**
   * Outputs the minimum support of the model after the prunning phase.
   *
   * @return the minimum support.
   */
  public int getSupport(){
    return support;
  }
}