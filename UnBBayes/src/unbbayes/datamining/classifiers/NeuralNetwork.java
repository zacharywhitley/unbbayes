package unbbayes.datamining.classifiers;

import java.io.*;
import java.util.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;
import unbbayes.datamining.datamanipulation.*;

public class NeuralNetwork extends DistributionClassifier implements Serializable{

  public static final int AUTO_HIDDEN_LAYER_SIZE = -1;
  public static final int NO_ERROR_VARIATION_STOP_CRITERION = -2;
  public static final int SIGMOID = 0;
  public static final int TANH = 1;

  private float[] inputLayer;
  private HiddenNeuron[] hiddenLayer;
  private OutputNeuron[] outputLayer;
  private transient float learningRate;
  private transient float originalLearningRate;
  private transient float momentum;
  private transient int hiddenLayerSize;
  private transient ActivationFunction activationFunction = null;
  private int activationFunctionType;
  private int numOfAttributes;
  private transient int trainingTime;
  private transient float minimumErrorVariation;
  private transient boolean learningRateDecay = false;
  private boolean numericalInputNormalization = true;
  private boolean numericOutput;
  private float[] highestValue;
  private float[] lowestValue;

  private transient QuadraticAverageError quadraticAverageError;

  private int[] attNumOfValues;

  /**Vector that contains the attributes of the training set.*/
  private Attribute[] attributeVector;

  /**Index of the class attribute.*/
  private int classIndex;

  /**The set of instances of the training set*/
  private transient InstanceSet instanceSet;

  public NeuralNetwork(float learningRate,
                       boolean learningRateDecay,
                       float momentum,
                       int hiddenLayerSize,
                       int activationFunction,
                       int trainingTime,
                       boolean numericalInputNormalization,
                       float activationFunctionSteep,
                       float minimumErrorVariation) {
    this.learningRate = learningRate;
    this.originalLearningRate = learningRate;
    this.learningRateDecay = learningRateDecay;
    this.momentum = momentum;
    this.hiddenLayerSize = hiddenLayerSize;
    this.trainingTime = trainingTime;
    this.numericalInputNormalization = numericalInputNormalization;
    this.minimumErrorVariation = minimumErrorVariation;
    activationFunctionType = activationFunction;

    if(activationFunction == NeuralNetwork.SIGMOID){
      this.activationFunction = new Sigmoid(activationFunctionSteep);
    } else if(activationFunction == NeuralNetwork.TANH){
      this.activationFunction = new Tanh(activationFunctionSteep);
    }
  }

  public NeuralNetwork(float learningRate,
                       float momentum,
                       int hiddenLayerSize,
                       int activationFunction,
                       int trainingTime) {
    this.learningRate = learningRate;
    this.originalLearningRate = learningRate;
    this.momentum = momentum;
    this.hiddenLayerSize = hiddenLayerSize;
    this.trainingTime = trainingTime;
    activationFunctionType = activationFunction;

    if(activationFunction == NeuralNetwork.SIGMOID){
      this.activationFunction = new Sigmoid();
    } else if(activationFunction == NeuralNetwork.TANH){
      this.activationFunction = new Tanh();
    }
  }

  /**
   *
   *
   * @param instanceSet The training data
   * @exception Exception if classifier can't be built successfully
   */
  public void buildClassifier(InstanceSet instanceSet) throws Exception{
    this.instanceSet = instanceSet;
    Instance instance;
    Enumeration instanceEnum;
    numOfAttributes = instanceSet.numAttributes();
    numericOutput = instanceSet.getClassAttribute().isNumeric();
    float quadraticError = 0;
    float oldQuadraticError;

    attributeVector = instanceSet.getAttributes();      //cria um array com os atributos para serialização
    this.classIndex = instanceSet.getClassIndex();      //guarda o indice da classe para serialização

    //seta os maiores e menores valores dos atributos do instanceSet
    highestValue = new float[numOfAttributes];
    lowestValue = new float[numOfAttributes];
    if(numericalInputNormalization){
      for(int i=0; i<numOfAttributes; i++){
        if(i!=classIndex && instanceSet.getAttribute(i).isNumeric()){
          highestValue[i] = Float.MIN_VALUE;
          lowestValue[i] = Float.MAX_VALUE;
          String[] values = instanceSet.getAttribute(i).getAttributeValues();
          for(int j=0; j<values.length; j++){
            highestValue[i] = Math.max(highestValue[i], Float.parseFloat(values[j]));
            lowestValue[i] = Math.min(lowestValue[i], Float.parseFloat(values[j]));
          }
        }
      }
    }

    //seta os maiores e menores valores da classe
    if(numericOutput){
      highestValue[classIndex] = Float.MIN_VALUE;
      lowestValue[classIndex] = Float.MAX_VALUE;
      String[] values = instanceSet.getClassAttribute().getAttributeValues();
      for(int j=0; j<values.length; j++){
        highestValue[classIndex] = Math.max(highestValue[classIndex], Float.parseFloat(values[j]));
        lowestValue[classIndex] = Math.min(lowestValue[classIndex], Float.parseFloat(values[j]));
      }
    }

    //iniciliza numero de valores dos atributos
    attNumOfValues = new int[numOfAttributes - 2];
    Enumeration attEnum = instanceSet.enumerateAttributes();
    int index = 0;
    while (attEnum.hasMoreElements()){
      Attribute att = (Attribute)attEnum.nextElement();
      if(!attEnum.hasMoreElements()){
        break;
      }
      if(att.isNumeric()){
        attNumOfValues[index] = 1;
      } else {
        attNumOfValues[index] = att.numValues ();
      }
      index++;
    }

   //inicializa input layer
    int inputLayerSize = 0;
    for(int i=0; i<numOfAttributes; i++){
      if(i != classIndex){
        if(instanceSet.getAttribute(i).isNumeric()){
          inputLayerSize++;
        } else {
          inputLayerSize = inputLayerSize + instanceSet.getAttribute(i).numValues();
        }
      }
    }
    inputLayer = new float[inputLayerSize];

    //verifica se o numero de hidden neurons deve ser automatico
    if(hiddenLayerSize == AUTO_HIDDEN_LAYER_SIZE){
      hiddenLayerSize = (instanceSet.numAttributes() + instanceSet.numClasses()) / 2;
      if(hiddenLayerSize < 3){
        hiddenLayerSize = 3;
      }
    }

    hiddenLayer = new HiddenNeuron[hiddenLayerSize];
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i] = new HiddenNeuron(activationFunction, inputLayer.length);
    }

    //output inicialization
    if(numericOutput){
      outputLayer = new OutputNeuron[1];
      outputLayer[0] = new OutputNeuron(activationFunction, hiddenLayer.length);
    } else{
      outputLayer = new OutputNeuron[instanceSet.getClassAttribute().numValues()];
      for(int i = 0; i<outputLayer.length; i++){
        outputLayer[i] = new OutputNeuron(activationFunction, hiddenLayer.length);
      }
    }

    //Learning
    for (int epoch = 1; epoch < (trainingTime+1); epoch++) {
      instanceEnum = instanceSet.enumerateInstances();
      oldQuadraticError = quadraticError;
      quadraticError = 0;

      if(learningRateDecay){
        learningRate = originalLearningRate / epoch;
      }

      while (instanceEnum.hasMoreElements()) {
        instance = (Instance) instanceEnum.nextElement();
        quadraticError = quadraticError + learn(instance);
      }
      quadraticError = quadraticError/instanceSet.numWeightedInstances();

      if(quadraticAverageError != null){
        quadraticAverageError.setQuadraticAverageError(epoch, quadraticError);
//        System.out.println("Erro quadrado médio " + epoch + " :" + quadraticError);
      }

      if(minimumErrorVariation != NO_ERROR_VARIATION_STOP_CRITERION){
        if(Math.abs((oldQuadraticError - quadraticError) * 100 / oldQuadraticError) < minimumErrorVariation){
          break;
        }
      }
    }
  }

  public float learn(Instance instance){
    float totalErrorEnergy = 0;
    float[] expectedOutput;

    Arrays.fill(inputLayer, 0);  //zera o vetor de entradas

    inputLayerSetUp(instance);

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
    }

    /////////calcula as saídas esperadas
    expectedOutput = calculateExpectedOutput(instance);

    //////////calcula as saidas da camada de saída
    for(int i=0; i<outputLayer.length; i++){
      float instantaneousError;
      outputLayer[i].calculateOutputValue(hiddenLayer);
      instantaneousError = outputLayer[i].calculateErrorTerm(expectedOutput[i]);
      totalErrorEnergy = totalErrorEnergy + (instantaneousError * instantaneousError);
    }

    ///////// UPDATE  dos pesos dos neuronios de saida
    for(int i=0; i<outputLayer.length; i++){
      outputLayer[i].updateWeights(learningRate, momentum, hiddenLayer);
    }

    //////////calcula error terms  (SIGMA) da camada oculta, da saída já está calculado
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateErrorTerm(outputLayer, i);
    }

    /////////UPDATE dos pesos dos neuronios ocultos
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].updateWeights(learningRate, momentum, inputLayer);
    }

    return (totalErrorEnergy / 2);
  }

  private void inputLayerSetUp(Instance instance){
    int counter = 0; //inicializa o vetor de entradas
    for (int i = 0; i < numOfAttributes; i++) {
      if (i != classIndex) {
        if (!instance.isMissing(i)) {
          int index = 0;
          for (int j=0; j<counter; j++) {
            index = index + attNumOfValues[j];
          }
          Attribute att = instanceSet.getAttribute(i);
          if (att.isNumeric()) {
            int value = instance.getValue (att);
            if(numericalInputNormalization){
              inputLayer[index] = Utils.normalize(Float.parseFloat(att.getAttributeValues()[value]), highestValue[i], lowestValue[i], 1, -1);
            } else {
              inputLayer[index] = Float.parseFloat(att.getAttributeValues()[value]);
            }
          } else {
            index = index + instance.getValue (i);
            inputLayer[index] = 1;
          }
        }
        counter++;
      }
    }
  }

  private float[] calculateExpectedOutput(Instance instance){
    float[] expectedOutput;
    if(numericOutput){
      expectedOutput = new float[1];
                           //sugestão pra melhorar, instanceSet.getClassAttribute().getAttributeValues() pode ser atribuido a uma variavel global.
      float output = Float.parseFloat(instanceSet.getClassAttribute().getAttributeValues()[instance.classValue()]);
      if(activationFunctionType == SIGMOID){
                           //sugestão, pode usar fórmulas mais otimizadas para intervalo [0,1] e [-1,1] que tem no FAQ
        expectedOutput[0] = Utils.normalize(output, highestValue[classIndex], lowestValue[classIndex], 1, 0);
      } else if(activationFunctionType == TANH){
        expectedOutput[0] = Utils.normalize(output, highestValue[classIndex], lowestValue[classIndex], 1, -1);
      }
    } else {
      expectedOutput = new float[instanceSet.getClassAttribute().numValues()];
      Arrays.fill(expectedOutput, 0);
      expectedOutput[instance.classValue()] = 1;
    }
    return expectedOutput;
  }

  /**
   * Make inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array of floats with the distribution of values for the given instance.
   * @throws Exception if classifier can't carry through the inference successfully
   */
  public float[] distributionForInstance(Instance instance) throws Exception {
    float[] distribution = new float[outputLayer.length];

    Arrays.fill(inputLayer, 0);  //zera o vetor de entradas

    inputLayerSetUp(instance);

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
    }

    //////////calcula as saidas da camada oculta
    for(int i=0; i<outputLayer.length; i++){
      distribution[i] = outputLayer[i].calculateOutputValue(hiddenLayer);
    }

////////////////////////////////// un-normalization
    if(numericOutput){
      if(activationFunctionType == SIGMOID){
        for(int i=0; i<distribution.length; i++){
          distribution[i] = Utils.normalize(distribution[i], 1, 0, highestValue[classIndex], lowestValue[classIndex]);
        }
      } else {
        for(int i=0; i<distribution.length; i++){
          distribution[i] = Utils.normalize(distribution[i], 1, -1, highestValue[classIndex], lowestValue[classIndex]);
        }
      }
    }
///////////////////////////////////

    return distribution;
  }


  public void setQuadraticErrorOutput(QuadraticAverageError quadraticAverageError){
    this.quadraticAverageError = quadraticAverageError;
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
}