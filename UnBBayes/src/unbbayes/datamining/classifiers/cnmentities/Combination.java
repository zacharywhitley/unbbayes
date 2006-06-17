package unbbayes.datamining.classifiers.cnmentities;

import java.io.Serializable;

/**
 *  Class that implements the combinations tha compound the
 *  Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class Combination implements Serializable{

  private String key;
  private OutputNeuron[] outputArray;

  /**
   * Constructs a new empty combination.
   *
   * @param key the unique key for the new combination.
   */
  public Combination(String key) {
    this.key = key;
  }

  /**
   * Constructs a new combination.
   *
   * @param key the unique key for the new combination.
   * @param outputArray an array of the output neurons connected to this combination.
   */
  public Combination(String key, OutputNeuron[] outputArray) {
    this.key = key;
    this.outputArray = outputArray;
  }

  /**
   * Returns the unique key of this combination.
   *
   * @return this combination key.
   */
  public String getKey(){
    return this.key;
  }

  /**
   * Returns the output neurons connected to this combination.
   *
   * @return an array with the output neurons connected to this combination.
   */
  public OutputNeuron[] getOutputArray(){
    return this.outputArray;
  }

  /**
   * Returns an specified output neuron connected to this combination.
   *
   * @param index the index of the desired output neuron.
   * @return the specified output neuron.
   */
  public OutputNeuron getOutputNeuron(int index){
    return outputArray[index];
  }

  /**
   * Executes the punishment of the model, calculating the confidence,
   * the support and the final weight of the accumulators (newWeight).
   *
   * @param numOfInstances the number of instances of the training set for the
   *                       calculation of the support.
   */
  public void punish(int numOfInstances){
    int outputNum = outputArray.length;
    int sum = 0;

    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null){
        for(int j=0; j<outputNum; j++){
          if((outputArray[j] != null) && (i != j)){
            sum = sum + outputArray[j].accumulator;
          }
        }
        outputArray[i].setNetWeight(outputArray[i].accumulator - sum);
        outputArray[i].setConfidence((float)outputArray[i].accumulator * 100 / (sum + outputArray[i].accumulator));
        outputArray[i].setSupport((float)outputArray[i].accumulator * 100 / numOfInstances);
      }
      sum = 0;
    }
  }

  /**
   * Prunnes the model based on a threshold.
   *
   * @param threshold the threshold to prunne the model.
   */
  public void prunning(int threshold){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && outputArray[i].netWeight < threshold){
        outputArray[i] = null;
      }
    }
  }

  /**
   * Prunnes the model based on a minimum support an confidence.
   *
   * @param minSupport the minimum support to prunne the model.
   * @param minConfidence the minimum confidence to prunne the model.
   */
  public void prunning(int minSupport, int minConfidence){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null && (outputArray[i].support < minSupport || outputArray[i].confidence < minConfidence)){
        outputArray[i] = null;
      }
    }
  }

  /**
   * Tests if this combination is connected to any output neuron.
   *
   * @return <code>true</code> if the combination is disconnected;
   *         <code>false</code> otherwise.
   */
  public boolean isNull(){
    int outputNum = outputArray.length;
    for(int i=0; i<outputNum; i++){
      if(outputArray[i] != null){
        return false;
      }
    }
    return true;
  }

  /**
   * Increases the accumulatos of the specified output neuron.
   *
   * @param classValue the class value that specify the output neuron.
   * @param weight the weight to increase the accumulator.
   */
  public void increaseAccumulator(short classValue, int weight){  // classValue, para selecionar qual classe deve ser incrementado o acumulador
    if(outputArray[classValue] != null){                          // e o peso para incrementar
      outputArray[classValue].increaseAccumulator(weight);
    } else {
      outputArray[classValue] = new OutputNeuron(weight);
    }
  }
}