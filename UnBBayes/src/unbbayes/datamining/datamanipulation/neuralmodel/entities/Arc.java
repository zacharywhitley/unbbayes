package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Arc implements Serializable{
  protected int accumulator;
  protected int netWeigth;
  protected float confidence;
  protected float support;
  protected Neuron combinationNeuron;

  public Arc(Neuron combinationNeuron){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = 1;
  }

  public Arc(Neuron combinationNeuron, int accumulator){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = accumulator;
  }

  public Neuron getCombinationNeuron(){
    return combinationNeuron;
  }

  public float getConfidence(){
    return confidence;
  }

  public float getSupport(){
    return support;
  }

  public int getAccumulator(){
    return accumulator;
  }

  public int getNetWeigth(){
    return netWeigth;
  }

  public void setNetWeight(int netWeigth){
    this.netWeigth = netWeigth;
  }

  public void setSupport(float support){
    this.support = support;
  }

  public void setConfidence(float confidence){
    this.confidence = confidence;
  }
}