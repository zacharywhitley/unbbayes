package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.io.*;

/**
 *  Class that implements the arcs tha compound the
 *  Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class Arc implements Serializable{
  /**Value of the arc accumulator*/
  protected int accumulator;

  /**Value of the final weight*/
  protected int netWeight;

  /**Value of the confidence of the arc*/
  protected float confidence;

  /**Value of the support of the arc*/
  protected float support;

  /**Neuron that represents the combination connected by the arc*/
  protected InternalNeuron combinationNeuron;

  /**
   * Constructs a new arc.
   *
   * @param combinationNeuron the neuron that represents the combination
   *                          connected by this arc.
   */
  public Arc(InternalNeuron combinationNeuron){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = 1;
  }

  /**
   * Constructs a new arc setting the accumulator value.
   *
   * @param combinationNeuron the neuron that represents the combination
   *                          connected by this arc.
   * @param accumulator the value of the arc accumulator
   */
  public Arc(InternalNeuron combinationNeuron, int accumulator){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = accumulator;
  }

  /**
   * Outputs the neuron that represents the combination conected by this arc.
   *
   * @return the neuron that represents the combination conected by this arc.
   */
  public InternalNeuron getCombinationNeuron(){
    return combinationNeuron;
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
}