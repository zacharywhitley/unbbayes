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

/**
 *  Interface that defines the methods that an activationFunction must have.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public interface ActivationFunction{

  /**
   * Calculate the outputo of the activation function for a given value.
   *
   * @param v The value to be evaluated by the activation function
   * @return The evaluated value
   */
  public double functionValue(double v);

  /**
   * Calculate the error term of an output neuron, given the expected value and
   * the actual calculated value.
   *
   * @param desired The desired value.
   * @param actualValue The actual calculated value.
   * @return The error term.
   */
  public double outputErrorTerm(double desired, double actualValue);

  /**
   * Calculate the error term of a hidden neuron, given the actual calculated
   * value and the sum of the error terms of all neuron of the next layer.
   *
   * @param y The actual output of the hidden neuron
   * @param sum The sum of the error terms of all neuron of the next layer
   * @return The error term.
   */
  public double hiddenErrorTerm(double y, double sum);

  /**
   * Method that normalizes a number to the activation function range
   *
   * @param data The number to be normalized to the activation function interval.
   * @param dataHighestValue The maximum value that the original data may assume.
   * @param dataLowestValue The minimum value that the original data may assume.
   * @return The normalized data.
   */
  public float normalizeToFunctionInterval(float data, float dataHighestValue, float dataLowestValue);
}
