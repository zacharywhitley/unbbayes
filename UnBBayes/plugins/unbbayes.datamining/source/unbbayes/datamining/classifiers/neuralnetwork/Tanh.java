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

import unbbayes.datamining.datamanipulation.Utils;

/**
 *  Class that implements the hyperbolic tangent (tanh) activation function
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 *  @see ActivationFunction
 */
public class Tanh implements ActivationFunction, Serializable{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  /**Constant that defines the minimum steep of the activation function*/
  public static final double MIN_STEEP = 0.0;

  /**Constant that defines the default steep of the activation function*/
  public static final double DEF_STEEP = 1.0;

  /**Constant determining the steepness of the tanh function*/
  private double steep;

 /**The default constructor of the tanh activation function*/
  public Tanh(){
    steep = DEF_STEEP;
  }

  /**
   * Constructor of the tanh activation function with a defined steep.
   *
   * @param steep The steepness of the sigmoid function.
   */
  public Tanh(double steep) {
    this.steep = steep;
  }

  /**
   * Method used to set or change the steepness of the function.
   *
   * @param steep The new steepness of the tanh function.
   */
  public void setSteep(float steep) {
    if (steep > MIN_STEEP){
      this.steep = steep;
    }
  }

  /**
   * Method used to get the actual value of the tanh function steep.
   *
   * @return The actual tanh function steep.
   */
  public double getSteep(){
    return steep;
  }

  /**
   * Calculate the output of the tanh activation function for the
   * given value.
   *
   * @param v the summation of the inputs multiplied by it's weights
   * @return the ouput of the tanh funciton
   */
  public double functionValue(double v){
    return ((2/(1 + Math.exp(-2 * steep * v))) -1);
  }

  /**
   * Method that calculate the error term of an output neuron using the
   * tanh activation function.
   *
   * @param desiredOutput the expected (desired) output
   * @param actualOutput the actual output
   * @return the value of the error term.
   */
  public double outputErrorTerm(double desiredOutput, double actualOutput){  //sigma
    return (steep * (desiredOutput - actualOutput) * (1 - (actualOutput * actualOutput)));
  }

  /**
   * Method that calculate the error term of a hidden neuron using the
   * tanh activation function.
   *
   * @param y The actual output of the hidden neuron
   * @param sum The sum of the error terms of all neuron of the next layer
   * @return the value of the error term.
   */
  public double hiddenErrorTerm(double y, double sum){   //sigma
    return (steep * (1 - (y * y)) * sum);
  }

  /**
   * Method that normalizes a number to the activation function range
   *
   * @param data The number to be normalized to the activation function interval.
   * @param dataHighestValue The maximum value that the original data may assume.
   * @param dataLowestValue The minimum value that the original data may assume.
   * @return The normalized data.
   */
  public float normalizeToFunctionInterval(float data, float dataHighestValue, float dataLowestValue){
    return Utils.normalize(data, dataHighestValue, dataLowestValue, 1, -1);
  }

}