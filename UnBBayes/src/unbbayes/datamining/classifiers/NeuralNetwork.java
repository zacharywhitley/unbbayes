package unbbayes.datamining.classifiers;

import java.io.*;
import java.util.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;
import unbbayes.datamining.datamanipulation.*;

public class NeuralNetwork extends BayesianLearning implements Serializable{

  public static final int SIGMOID = 0;
  public static final int TANH = 1;

  private HiddenNeuron[] hiddenLayer;
  private OutputNeuron[] outputLayer;
  private transient float learningRate;
  private transient float momentum;
  private transient int hiddenLayerSize;
  private transient ActivationFunction activationFunction;

  /**Vector that contains the attributes of the training set.*/
  private Attribute[] attributeVector;

  /**Index of the class attribute.*/
  private int classIndex;

  /**The set of instances of the training set*/
  private transient InstanceSet instanceSet;



  public NeuralNetwork(float learningRate, float momentum, int hiddenLayerSize, int activationFunction) {
    this.learningRate = learningRate;
    this.momentum = momentum;
    this.hiddenLayerSize = hiddenLayerSize;

    if(activationFunction == this.SIGMOID){
      this.activationFunction = new Sigmoid(0.5);   //valores default   pode modificar?????
    } else if(activationFunction == this.TANH){
      this.activationFunction = new Tanh(1.7159, 2/3);
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
    Enumeration instanceEnum = instanceSet.enumerateInstances();
    int attributeNum = instanceSet.numAttributes();
    int numOfInstances = instanceSet.numWeightedInstances();

    attributeVector = instanceSet.getAttributes();      //cria um array com os atributos para serialização
    this.classIndex = instanceSet.getClassIndex();      //guarda o indice da classa para serialização

    while(instanceEnum.hasMoreElements()){
      instance = (Instance)instanceEnum.nextElement();
///////////////////////propagar na rede
    }
  }

  /**
   * Make inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array that contains the arc with greater weight of each
   *         output neuron.
   */
//  public Combination[] inference(Instance instance){
//  }

  /**
   * Make inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array of floats with the distribution of values for the given instance.
   * @throws Exception if classifier can't carry through the inference successfully
   */
  public float[] distributionForInstance(Instance instance) throws Exception{
        float[] distribution = {0,1};// null;
/*        Combination[] outputArray = inference(instance);
        distribution = new float[outputArray.length];
        for(int i=0; i<distribution.length; i++){
          if(outputArray[i] != null){
                distribution[i] = outputArray[i].getOutputNeuron(i).getNetWeight();
          } else {
                distribution[i] = 0;
          }
        }
  */      return distribution;
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
   * Retuns an iterator conataining the objects that represents
   * the combinatorial neural model, that is, the combinations.
   *
   * @return an iterator of combinations.
   */
//  public Iterator getModel(){
  //      return model.values().iterator();
  //}
}