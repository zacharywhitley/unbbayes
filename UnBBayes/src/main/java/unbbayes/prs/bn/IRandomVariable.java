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
package unbbayes.prs.bn;


/**
 * A interface for variables containing probability functions
 */
public interface IRandomVariable {
	
	/**
	 * A class representing the probability distribution associated to this random variable
	 * @return a probability function (e.g. probability table for tabled variables)
	 */
    public IProbabilityFunction getProbabilityFunction();
    
    /**
     * @returns the identification key which can be
     * easily compared, so that this random
     * variable can be uniquely identified over several 
     * similar instances in the same network without performing
     * name comparison or complex data comparison.
     * This is particularly useful when a random variable
     * must be re-used across different plug-ins,
     * and performance is required.
     */
    public int getInternalIdentificator();
    
    /**
     * @param internalIdentificator : the identification key which can be
     * easily compared, so that this random
     * variable can be uniquely identified over several 
     * similar instances without performing
     * name comparison or complex data comparison.
     * This is particularly useful when a random variable
     * must be re-used across different plug-ins,
     * and performance is required.
     */
    public void setInternalIdentificator(int internalIdentificator);
    
}