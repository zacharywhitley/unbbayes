package unbbayes.datamining.classifiers;

import java.io.*;
import java.util.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;
import unbbayes.datamining.datamanipulation.*;

/**
 * Class that implements a multilayer neural network with backpropagation
 * trainning algorithm.
 *
 * @author Rafael Moraes Noivo
 * @version $1.0 $ (06/26/2003)
 */
public class NeuralNetwork extends DistributionClassifier implements Serializable{

  /**
   * Constant that defines that the size of the hidden layer is
   * automatically defined. The number of hidden neurons will be the number of
   * input attributes plus the number of output attributes divided by 2
   */
  public static final int AUTO_HIDDEN_LAYER_SIZE = -1;

  /**
   * Constant that defines a value to indicate that the error variation
   * stop criterion will not be used
   */
  public static final int NO_ERROR_VARIATION_STOP_CRITERION = -2;

  /**Constant that informs that the sigmoid (logistic) activation function is been used*/
  public static final int SIGMOID = 0;

  /**Constant that informs that the hyperbolic tangent (tanh) activation function is been used*/
  public static final int TANH = 1;

  /**Constant that indicates that no normalization will be used*/
  public static final int NO_NORMALIZATION = 0;

  /**Constant that indicates that the linear normalization will be used*/
  public static final int LINEAR_NORMALIZATION = 1;

  /**Constant that indicates that the mean 0 and standard deviation 1 normalization will be used*/
  public static final int MEAN_0_STANDARD_DEVIATION_1_NORMALIZATION = 2;


  /**The actual learning rate*/
  private transient float learningRate;

  /**The original leaning rate, used when the leaning rate decay is activated*/
  private float originalLearningRate;

  /**The momenum value*/
  private float momentum;

  /**The hidden layer size*/
  private transient int hiddenLayerSize;

  /**
   * The activation function been used
   * @see {@link ActivationFunction}
   */
  private transient ActivationFunction activationFunction = null;

  /**The training time, expressed in the number of epochs*/
  private int trainingTime;

  /**The minimum error variatio between epochs, this stop criterion may be disabled*/
  private transient float minimumErrorVariation;

  /**
   * Option that enable the learning rate to decay between epochs.
   * The original learning rate is divide by the number of the epoch.
   * */
  private boolean learningRateDecay = false;

  /**
   * The mean squared error
   * @see {@link MeanSquaredError}
   */
  private transient MeanSquaredError meanSquaredError;

  /**
   * The isntance set used for training
   * @see {@link InstanceSet}
   */
  private transient InstanceSet instanceSet;

  /**Array that defines the input layer of the network*/
  private float[] inputLayer;

  /**
   * Array of hidden neurons defining the hidden layer of the network
   * @see {@link HiddenNeuron}
   */
  private HiddenNeuron[] hiddenLayer;

  /**
   * Array of output neurons defining the output layer of the network
   * @see {@link OutputNeuron}
   */
  private OutputNeuron[] outputLayer;

  /**The activation function type been used (sigmoid or tanh)*/
  private int activationFunctionType;

  /**Variable that has the number of attibutes of the instance set*/
  private int numOfAttributes;

  /**Variable that informs if the input must normalized and the normalization method*/
  private int numericalInputNormalization = NO_NORMALIZATION;

  /**Variable that informs if the class attribute is numeric*/
  private boolean numericOutput;

  /**Array that contains the highest values of each attribute of the instance set*/
  private float[] highestValue;

  /**Array that contains the lowest values of each attribute of the instance set*/
  private float[] lowestValue;

  /**Array tha contains the number of possible values of each nominal attributes*/
  private int[] inputLayerIndexes;

  /**Array of all the attributes in the instance set*/
  private Attribute[] attributeVector;

  /**The attribute index of the class attribute*/
  private int classIndex;

  /**Array that contains the mean of the values of all instances for each attribute*/
  private float[] attributeMean;

  /**Array that contains the standard deviation of the values of all instances for each attribute*/
  private float[] attributeStandardDeviation;

  /**The normalization function choosed by the user*/
  private INormalization normalizationFunction;

  /**The activation function steep*/
  private float activationFunctionSteep;

  /**The number of possible values of the class attribute*/
  private transient int classAttNumOfValues;

  /**The size of the input layer*/
  private int inputLayerSize;

  /**Pre-processed expected outputs*/
  private transient float[][] expectedOutput;

  /**Calculate outputs of the hidden layer*/
  private float[] hiddenLayerOutput;

  /**
   * Constructor of the neural network
   *
   * @param learningRate The learning rate
   * @param learningRateDecay Sets if the learning rate should decay
   * @param momentum The momentum
   * @param hiddenLayerSize The hidden layer size
   * @param activationFunction The activation function
   * @param trainingTime The training time (in epochs)
   * @param numericalInputNormalization Sets if the numerical input should be normalized
   * @param activationFunctionSteep Sets the activation function steep
   * @param minimumErrorVariation Sets the minimum error variation
   */
  public NeuralNetwork(float learningRate,
                       boolean learningRateDecay,
                       float momentum,
                       int hiddenLayerSize,
                       int activationFunction,
                       int trainingTime,
                       int numericalInputNormalization,
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
    this.activationFunctionSteep = activationFunctionSteep;

    if(activationFunction == NeuralNetwork.SIGMOID){
      this.activationFunction = new Sigmoid(activationFunctionSteep);
    } else if(activationFunction == NeuralNetwork.TANH){
      this.activationFunction = new Tanh(activationFunctionSteep);
    }
  }

  /**
   * Constructor of the neural network
   *
   * @param learningRate The learning rate
   * @param momentum The momentum
   * @param hiddenLayerSize The hidden layer size
   * @param activationFunction The activation function
   * @param trainingTime The training time (in epochs)
   */
  public NeuralNetwork(float learningRate,
                       float momentum,
                       int hiddenLayerSize,
                       int activationFunction,
                       int trainingTime) {

    this(learningRate, false, momentum, hiddenLayerSize,
         activationFunction, trainingTime, NO_NORMALIZATION,
         1, NO_ERROR_VARIATION_STOP_CRITERION);
  }

  /**
   * Method used to build and train the neural network classifier
   *
   * @param instanceSet The training data
   * @throws Exception if classifier can't be built successfully
   */
  public void buildClassifier(InstanceSet instanceSet) throws Exception{

    System.out.println("Inicio = "+(new java.text.SimpleDateFormat("HH:mm:ss:SSS - ")).format(new Date()));

    this.instanceSet = instanceSet;
    Instance instance;
    numOfAttributes = instanceSet.numAttributes();
    numericOutput = instanceSet.getClassAttribute().isNumeric();
    float quadraticError = 0;
    float oldQuadraticError;
    long numWeightedInstances = instanceSet.numWeightedInstances();
    long numInstances = instanceSet.numInstances();
    attributeVector = instanceSet.getAttributes();      //cria um array com os atributos para serialização
    classIndex = instanceSet.getClassIndex();      //guarda o indice da classe para serialização
    classAttNumOfValues = attributeVector[classIndex].numValues();
    highestValue = new float[numOfAttributes];
    lowestValue = new float[numOfAttributes];

    //seta os maiores e menores valores da classe
    if(numericOutput){
      highestValue[classIndex] = Float.MIN_VALUE;
      lowestValue[classIndex] = Float.MAX_VALUE;
      String[] values = instanceSet.getClassAttribute().getAttributeValues();
      for(int j = 0; j<values.length; j++){
        highestValue[classIndex] = Math.max(highestValue[classIndex], Float.parseFloat(values[j]));
        lowestValue[classIndex] = Math.min(lowestValue[classIndex], Float.parseFloat(values[j]));
      }
    }

    //Cria um array com as saídas esperadas pré-processadas
    expectedOutput = new float[classAttNumOfValues][0];
    for(int i = 0; i<classAttNumOfValues; i++){
      if(numericOutput){
        expectedOutput[i] = new float[1];
        float output = Float.parseFloat(attributeVector[classIndex].getAttributeValues()[i]);
        expectedOutput[i][0] = activationFunction.normalizeToFunctionInterval(output, highestValue[classIndex], lowestValue[classIndex]);
      } else{
        expectedOutput[i] = new float[classAttNumOfValues];
        expectedOutput[i][i] = 1;
      }
    }

    //inicializa o tipo de normalização escolhida
    if(numericalInputNormalization == NO_NORMALIZATION){
      normalizationFunction = new NoNormalization();
    } else if(numericalInputNormalization == LINEAR_NORMALIZATION){
      normalizationFunction = new LinearNormalization();
    } else if(numericalInputNormalization == MEAN_0_STANDARD_DEVIATION_1_NORMALIZATION){
      normalizationFunction = new Mean0StdDeviation1Normalization();
    }


    //iniciliza numero de valores dos atributos
    inputLayerIndexes = new int[numOfAttributes-1];
    int counter = 1;
    Attribute att;
    inputLayerIndexes[0] = 0;
    for(int i = 0; i<(numOfAttributes-1); i++){
      att = attributeVector[i];
      if(i!=classIndex){
        if(att.isNumeric()){
          inputLayerIndexes[counter] = inputLayerIndexes[counter-1] + 1;
        } else{
          inputLayerIndexes[counter] = inputLayerIndexes[counter-1] + att.numValues();
        }
        counter++;
        if(inputLayerIndexes.length==counter){
          break;
        }
      }
    }

    //calcula o tamanho da input layer
    for(int i = 0; i<numOfAttributes; i++){
      if(i!=classIndex){
        if(instanceSet.getAttribute(i).isNumeric()){
          inputLayerSize++;
        } else{
          inputLayerSize = inputLayerSize+instanceSet.getAttribute(i).numValues();
        }
      }
    }

    inputLayer = new float[inputLayerSize];

    //verifica se o numero de hidden neurons deve ser automatico
    if(hiddenLayerSize == AUTO_HIDDEN_LAYER_SIZE){
      hiddenLayerSize = (instanceSet.numAttributes() + 1/*class attribute*/) / 2;
      if(hiddenLayerSize < 3){
        hiddenLayerSize = 3;
      }
    }

    hiddenLayer = new HiddenNeuron[hiddenLayerSize];
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i] = new HiddenNeuron(activationFunction, inputLayerSize, momentum);
    }

    hiddenLayerOutput = new float[hiddenLayerSize];  //used in the learn method.

    //output layer inicialization
    if(numericOutput){
      outputLayer = new OutputNeuron[1];
      outputLayer[0] = new OutputNeuron(activationFunction, hiddenLayer.length, momentum);
    } else{
      outputLayer = new OutputNeuron[instanceSet.getClassAttribute().numValues()];
      for(int i = 0; i<outputLayer.length; i++){
        outputLayer[i] = new OutputNeuron(activationFunction, hiddenLayer.length, momentum);
      }
    }

    int inst;
    float instanceWeight;

    //Learning
    for (int epoch=0; epoch<trainingTime; epoch++) {
      oldQuadraticError = quadraticError;
      quadraticError = 0;

      if(learningRateDecay){
        learningRate = originalLearningRate / epoch;
      }

      for (inst=0;inst<numInstances;inst++){
        instance = instanceSet.getInstance(inst);
        instanceWeight = instance.getWeight();
        for(int i=0; i<instanceWeight; i++){
          quadraticError = quadraticError + learn(instance);
        }
      }
      quadraticError = quadraticError / numWeightedInstances;

      if(meanSquaredError != null){
        meanSquaredError.setMeanSquaredError(epoch, quadraticError);
      }

      if(minimumErrorVariation != NO_ERROR_VARIATION_STOP_CRITERION){
        if(Math.abs((oldQuadraticError - quadraticError) * 100 / oldQuadraticError) < minimumErrorVariation){
          trainingTime = epoch;
          break;
        }
      }
    }

    System.out.println("Fim = "+(new java.text.SimpleDateFormat("HH:mm:ss:SSS - ")).format(new Date()));
  }

  /**
   * Learns an specific instance
   *
   * @param instance The instance to be learned
   * @return The total error energy
   * @see {@link Instance}
   */
  private float learn(Instance instance){
    float totalErrorEnergy = 0;

    inputLayerSetUp(instance);

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
      hiddenLayerOutput[i] = hiddenLayer[i].outputValue;
    }

    //////////calcula as saidas da camada de saída
    for(int i=0; i<outputLayer.length; i++){
      float instantaneousError;
      outputLayer[i].calculateOutputValue(hiddenLayerOutput);
      instantaneousError = outputLayer[i].calculateErrorTerm(expectedOutput[instance.classValue()][i]);
      totalErrorEnergy = totalErrorEnergy + (instantaneousError * instantaneousError);
    }

    ///////// UPDATE  dos pesos dos neuronios de saida
    for(int i=0; i<outputLayer.length; i++){
      outputLayer[i].updateWeights(learningRate, hiddenLayerOutput);
    }

    //////////calcula error terms (SIGMA) da camada oculta, da saída já está calculado
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateErrorTerm(outputLayer, i);
    }

    /////////UPDATE dos pesos dos neuronios ocultos
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].updateWeights(learningRate, inputLayer);
    }

    return (totalErrorEnergy / 2);
  }

  /**
   * Prepare the input layer for propagation acording to the received instance.
   *
   * @param instance The instance to prepare the input layer
   */
  private void inputLayerSetUp(Instance instance){
    int counter = 0; //inicializa o contador de entradas
    int index;
    Arrays.fill(inputLayer, -1);  //inicializa com -1 o vetor de entradas

    for(int i = 0; i<numOfAttributes; i++){
      if(i!=classIndex){
        if(!instance.isMissing(i)){
          index = inputLayerIndexes[counter];
          Attribute att = attributeVector[i];
          if(att.isNumeric()){
            float data = Float.parseFloat(att.getAttributeValues()[instance.getValue(att)]);
            inputLayer[index] = normalizationFunction.normalize(data, i);
          } else{
            index = index + instance.getValue(i);
            inputLayer[index] = 1;
          }
        }
        counter++;
      }
    }
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
    Arrays.fill(inputLayer, -1);  //zera o vetor de entradas

    ////////prepara a camada de entrada
    inputLayerSetUp(instance);

    ///////////calcula as saidas da hiddem
    for(int i=0; i<hiddenLayer.length; i++){
      hiddenLayer[i].calculateOutputValue(inputLayer);
      hiddenLayerOutput[i] = hiddenLayer[i].outputValue;
    }

    //////////calcula as saidas da camada oculta
    for(int i=0; i<outputLayer.length; i++){
      distribution[i] = outputLayer[i].calculateOutputValue(hiddenLayerOutput);
    }

    ///////// un-normalization
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

    return distribution;
  }

  /**
   * Method used to set a class that implements the MeanSquaredError class so
   * this class may outptu the mean squared error and the epoch of this error.
   *
   * @param meanSquaredError A class that implements MeanSquaredError
   * @see {@link MeanSquaredError}
   */
  public void setMeanSquaredErrorOutput(MeanSquaredError meanSquaredError){
    this.meanSquaredError = meanSquaredError;
  }

  /**
   * Outputs an array of attributes with the attributes of the training set.
   *
   * @return an attribute array.
   * @see {@link Attribute}
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
   * Method that outputs the model parameters as a string.
   *
   * @return A string with the model parameters.
   */
  public String toString(){
    String learningRateStr = "Learning Rate: " + originalLearningRate;
    String momentumStr = "Momentum: " + momentum;
    String hiddenSizeStr = "Hidden Layer Size: " + hiddenLayer.length;
    String actFunctionSteepStr = "Activation Function Steep: " + activationFunctionSteep;
    String learningRateDecayStr = "Learning Rate Decay: " + learningRateDecay;
    String trainingTimeStr =  "Training Time: " + trainingTime;
    String activationFunctionStr = "Activation Function: ";
    if(activationFunctionType == SIGMOID){
      activationFunctionStr = activationFunctionStr + "Sigmoid";
    } else if(activationFunctionType == TANH){
      activationFunctionStr = activationFunctionStr + "Tanh";
    }
    String inputNormalization = "Numerical Input Normalization: ";
    if(numericalInputNormalization == NO_NORMALIZATION){
      inputNormalization = inputNormalization + "No normalization";
    } else if(numericalInputNormalization == LINEAR_NORMALIZATION){
      inputNormalization = inputNormalization + "Linear normalization";
    } else if(numericalInputNormalization == MEAN_0_STANDARD_DEVIATION_1_NORMALIZATION){
      inputNormalization = inputNormalization + "Mean 0 and standard deviation 1 normalization";
    }
    String classAttribute = "Class Attribute: " + attributeVector[classIndex].getAttributeName();

    return learningRateStr + "\n" +
           momentumStr + "\n" +
           hiddenSizeStr + "\n" +
           trainingTimeStr + "\n" +
           activationFunctionStr + "\n" +
           learningRateDecayStr + "\n" +
           inputNormalization + "\n" +
           actFunctionSteepStr + "\n" +
           classAttribute;
  }

  /**
   * <p>Title: </p> INormalization
   * <p>Description: </p> Interface that defines how a normalization method shoul be called.
   */
  public interface INormalization{
    public float normalize(float data, int attributeIndex);
  }

  /**
   * <p>Title: </p> LinearNormalization
   * <p>Description: </p> Class that implements a linear normalization (to an interval).
   */
  public class LinearNormalization implements INormalization, Serializable{
    public LinearNormalization(){
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

    /**
     * Method that implements the normalization.
     *
     * @param data The data to be normalized.
     * @param attributeIndex The index of the attribute been normalized.
     * @return The normalized data.
     */
    public float normalize(float data, int attributeIndex){
      return Utils.normalize(data, highestValue[attributeIndex], lowestValue[attributeIndex], 1, -1);
    }
  }

  /**
   * <p>Title: </p> Mean0StdDeviation1Normalization (Mean 0 and stadard deviation 1)
   * <p>Description: </p> Class that implements the mean 0 and standard deviatio 1 normalization
   */
  public class Mean0StdDeviation1Normalization implements INormalization, Serializable{
    public Mean0StdDeviation1Normalization() throws Exception{
      attributeMean = new float[numOfAttributes];
      attributeStandardDeviation = new float[numOfAttributes];

      for(int i=0; i<numOfAttributes; i++){
        Attribute att = instanceSet.getAttribute(i);
        if(att.isNumeric() && i!=classIndex){
          attributeMean[i] = (float)Utils.mean(instanceSet, i);
          attributeStandardDeviation[i] = (float)Utils.standardDeviation(instanceSet, i, attributeMean[i]);
        }
      }
    }

    /**
     * Method that implements the normalization.
     *
     * @param data The data to be normalized.
     * @param attributeIndex The index of the attribute been normalized.
     * @return The normalized data.
     */
    public float normalize(float data, int attributeIndex){
      return (data - attributeMean[attributeIndex]) / attributeStandardDeviation[attributeIndex];
    }
  }

  /**
   * <p>Title: </p> NoNormalization (No normalization)
   * <p>Description: </p> Class that implements no normalization, returning the original data.
   */
  public class NoNormalization implements INormalization, Serializable{
    /**
     * Method that implements the normalization.
     *
     * @param data The data to be normalized.
     * @param attributeIndex The index of the attribute been normalized.
     * @return The original data not normalized.
     */
    public float normalize(float data, int attributeIndex){
      return data;
    }
  }
}