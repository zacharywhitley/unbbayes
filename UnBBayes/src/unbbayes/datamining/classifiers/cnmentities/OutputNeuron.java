package unbbayes.datamining.classifiers.cnmentities;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

/**
 *  Class that implements the arcs tha compound the
 *  Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class OutputNeuron implements Serializable{

  /**Value of the arc accumulator*/
  protected int accumulator;

  /**Value of the final weight*/
  protected int netWeight;

  /**Value of the confidence of the arc*/
  protected float confidence;

  /**Value of the support of the arc*/
  protected float support;

  /**
   * Constructs a new arc.
   *
   * @param combinationNeuron the neuron that represents the combination
   *                          connected by this arc.
   */
  public OutputNeuron(){
    this.accumulator = 1;
  }

  /**
   * Constructs a new arc setting the accumulator value.
   *
   * @param combinationNeuron the neuron that represents the combination
   *                          connected by this arc.
   * @param accumulator the value of the arc accumulator
   */
  public OutputNeuron(int accumulator){
    this.accumulator = accumulator;
  }

  /**
   * Outputs the confidence associated to this arc combination.
   *
   * @return the value of the confidence
   */
  public float getConfidence(){
    return confidence;
  }

  /**
   * Outputs the support associated to this arc combination.
   *
   * @return the value of the support
   */
  public float getSupport(){
    return support;
  }

  /**
   * Outputs the accumulator associated to this arc.
   *
   * @return the accumulator value
   */
  public int getAccumulator(){
    return accumulator;
  }

  /**
   * Outputs the net weight associated to this arc combination.
   *
   * @return the net weight value
   */
  public int getNetWeight(){
    return netWeight;
  }

  /**
   * Sets the value of the net weight associated with this arc combination.
   *
   * @param netWeight the new net weight value
   */
  public void setNetWeight(int netWeight){
    this.netWeight = netWeight;
  }

  /**
   * Sets the value of the support associated with this arc combination.
   *
   * @param support the new support value
   */
  public void setSupport(float support){
    this.support = support;
  }

  /**
   * Sets the value of the confidence associated with this arc combination.
   *
   * @param confidence the new confidence value
   */
  public void setConfidence(float confidence){
    this.confidence = confidence;
  }

  public void increaseAccumulator(){
    accumulator++;
  }

  public void increaseAccumulator(int weight){
    accumulator = accumulator + weight;
  }
}