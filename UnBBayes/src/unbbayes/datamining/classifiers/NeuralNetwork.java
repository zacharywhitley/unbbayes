package unbbayes.datamining.classifiers;

import java.io.*;
import java.util.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;
import unbbayes.datamining.datamanipulation.*;

public class NeuralNetwork extends DistributionClassifier implements Serializable{

  public static final int SIGMOID = 0;
  public static final int TANH = 1;

  private int[] inputLayer;
  private HiddenNeuron[] hiddenLayer;
  private OutputNeuron[] outputLayer;
  private transient float learningRate;
  private transient float momentum;
  private transient int hiddenLayerSize;
  private transient ActivationFunction activationFunction;
  private int numOfAttributes;
  private transient int trainningTime;

  private int[] attNumOfValues;

  /**Vector that contains the attributes of the training set.*/
  private Attribute[] attributeVector;

  /**Index of the class attribute.*/
  private int classIndex;

  /**The set of instances of the training set*/
  private transient InstanceSet instanceSet;

  public NeuralNetwork(float learningRate, float momentum, int hiddenLayerSize, int activationFunction, int trainningTime) {
    this.learningRate = learningRate;
    this.momentum = momentum;
    this.hiddenLayerSize = hiddenLayerSize;
    this.trainningTime = trainningTime;

    if(activationFunction == NeuralNetwork.SIGMOID){
      this.activationFunction = new Sigmoid(1.0);   //valores default   pode modificar?????
    } else if(activationFunction == NeuralNetwork.TANH){
      this.activationFunction = new Tanh(/*1.7159, 2/3*/1 ,1);
    }
  }

  /**
   * Builds the Combinatorial Neural Model classifier (CNM).
   *
   * @param instanceSet The training data
   * @exception Exception if classifier can't be built successfully
   */
  public void buildClassifier(InstanceSet instanceSet) throws Exception{
    this.instanceSet = instanceSet;
    Instance instance;
    Enumeration instanceEnum;
    numOfAttributes = instanceSet.numAttributes();
    float quadraticAverageError = 0;

    attributeVector = instanceSet.getAttributes();      //cria um array com os atributos para serialização
    this.classIndex = instanceSet.getClassIndex();      //guarda o indice da classa para serialização

    //iniciliza numero de valores dos atributos
    attNumOfValues = new int[numOfAttributes - 2];
    Enumeration attEnum = instanceSet.enumerateAttributes();
    int index = 0;
    while (attEnum.hasMoreElements()){
      Attribute att = (Attribute)attEnum.nextElement();
      if(!attEnum.hasMoreElements()){
        break;
      }
      attNumOfValues[index] = att.numValues();
      index++;
    }

   /////////////////////////////////////
    int inputLayerSize = 0;
    for(int i=0; i<numOfAttributes; i++){
      if(i != classIndex){
        inputLayerSize = inputLayerSize + instanceSet.getAttribute(i).numValues();
      }
    }
    inputLayer = new int[inputLayerSize];

    hiddenLayer = new HiddenNeuron[hiddenLayerSize];
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i] = new HiddenNeuron(activationFunction, inputLayer.length);
    }

    outputLayer = new OutputNeuron[instanceSet.getClassAttribute().numValues()];
    for(int i=0; i<outputLayer.length; i++){
      outputLayer[i] = new OutputNeuron(activationFunction, hiddenLayer.length);
    }

    for (int i = 0; i < 5/*trainningTime*/; i++) {
      quadraticAverageError = 0;
      instanceEnum = instanceSet.enumerateInstances();

      while (instanceEnum.hasMoreElements()) {
        instance = (Instance) instanceEnum.nextElement();
        quadraticAverageError = quadraticAverageError + learn(instance);
      }
      quadraticAverageError = quadraticAverageError/instanceSet.numWeightedInstances();


     ////////////////////////////////////////// teste para ver os valores dos pesos
      System.out.println("Erro quadrado médio " + i + " :" + quadraticAverageError);
     /////////////////////////////////////////
    }
  }

  public float learn(Instance instance){
    float totalErrorEnergy = 0;

    Arrays.fill(inputLayer, 0);  //zera o vetor de entradas

    int counter = 0;           //inicializa o vetor de entradas
    for(int i=0; i<numOfAttributes; i++){
      if(i != classIndex){
        if(!instance.isMissing(i)){
          int index = 0;
          for(int j=0; j<counter; j++){
            index = index + attNumOfValues[j];
          }
          index = index + instance.getValue(i);
          inputLayer[index] = 1;
        }
        counter++;
      }
    }

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
    }

    //////////create expected output
    int[] expectedOutput = new int[instanceSet.getClassAttribute().numValues()];
    Arrays.fill(expectedOutput, 0);
    expectedOutput[instance.classValue()] = 1;

    //////////calcula as saidas da camada oculta
    for(int i=0; i<outputLayer.length; i++){
      float instantaneousError;
      outputLayer[i].calculateOutputValue(hiddenLayer);
      instantaneousError = outputLayer[i].calculateErrorTerm(expectedOutput[i]);
      totalErrorEnergy = totalErrorEnergy + (instantaneousError * instantaneousError);

/*teste*/  //    System.out.println("saida " + i + ": " + outputLayer[i].outputValue() );
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

    int counter = 0; //inicializa o vetor de entradas
    for (int i = 0; i < numOfAttributes; i++) {
      if (i != classIndex) {
        if (!instance.isMissing(i)) {
          int index = 0;
          for (int j = 0; j < counter; j++) {
            index = index + attNumOfValues[j];
          }
          index = index + instance.getValue(i);
          inputLayer[index] = 1;
        }
        counter++;
      }
    }

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
    }

    //////////calcula as saidas da camada oculta
    for(int i=0; i<outputLayer.length; i++){
      distribution[i] = outputLayer[i].calculateOutputValue(hiddenLayer);
    }

    return distribution;
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