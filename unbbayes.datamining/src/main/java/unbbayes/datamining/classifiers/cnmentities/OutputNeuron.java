/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.classifiers.cnmentities;

import java.io.Serializable;

/**
 *  Class that implements the output neurons that compound the
 *  Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class OutputNeuron implements Serializable{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  /**Value of the accumulator*/
  protected int accumulator;

  /**Value of the final weight*/
  protected int netWeight;

  /**Value of the confidence*/
  protected float confidence;

  /**Value of the support*/
  protected float support;

  /**
   * Constructs a new output neuron.
   */
  public OutputNeuron(){
    this.accumulator = 1;
  }

  /**
   * Constructs a new output neuron setting the accumulator value.
   *
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

  /**
   * Increases the accumulator value by 1.
   */
  public void increaseAccumulator(){
    accumulator++;
  }

  /**
   * Increases the accumulator value by the weight parameter.
   *
   * @param weight the value to increase the accumulator.
   */
  public void increaseAccumulator(int weight){
    accumulator = accumulator + weight;
  }
}