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
package unbbayes.datamining.classifiers.neuralnetwork;

import java.io.Serializable;

/**
 *  Abstract class that defines the methods that a neuron must have.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public abstract class Neuron implements Serializable{

  /**
   * Array that contains the wights associated to each input of the neuron
   * and the weight associated to the bias value
   */
  float[] weights;

  /**
   * Array that contains the delta W of each input and the bias value
   * calculated for the previous instance.
   */
  transient float[] deltaW;

  /**
   * The activaiton function been used.
   */
  ActivationFunction activationFunction;

  /**
   * Method that returns the output value of the neuron.
   *
   * @return The actual output value.
   */
  public abstract float outputValue();

  /**
   * Method that return the erro term of the neuron.
   *
   * @return The calculated error term.
   */
  public abstract float getErrorTerm();

  /**
   * Method use to set the initial weights of the network connections.
   */
  void startWeights(){
    double weight;
    for(int i=0; i<weights.length; i++){
      weight = Math.random();
      weights[i] = (float)(weight - 0.5);
    }
  }
}