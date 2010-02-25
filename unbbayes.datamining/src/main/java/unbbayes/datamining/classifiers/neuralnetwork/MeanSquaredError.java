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
 * Interface that defines a method that is used by the neural network main
 * class (NeuralNetwork) to output the values of the mean squared error an
 * it's epoch
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public interface MeanSquaredError {

  /**
   * Method that receives the mean squared error and it's epoch
   *
   * @param epoch An specific epoch
   * @param meanSquaredError The mean squared error associated to the epoch
   */
  public void setMeanSquaredError(int epoch, double meanSquaredError);
}