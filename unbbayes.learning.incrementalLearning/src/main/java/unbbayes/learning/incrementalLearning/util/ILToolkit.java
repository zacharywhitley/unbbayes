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
package unbbayes.learning.incrementalLearning.util;

import java.util.ArrayList;

import unbbayes.learning.LearningToolkit;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ILToolkit extends LearningToolkit {
	
	/**
	 * @param floatArray
	 * @return floatArray converted to array of int
	 * @see Math#round(float)
	 */
	public int[][] toIntArray(float[][] floatArray) {
		if (floatArray == null) {
			return null;
		}
		if (floatArray.length <= 0) {
			return new int[0][0];
		}
		int[][] ret = new int[floatArray.length][floatArray[0].length];
		
		for (int i = 0; i < floatArray.length; i++) {
			for (int j = 0; j < floatArray[0].length; j++) {
				ret[i][j] = Math.round(floatArray[i][j]);
			}
		}
		
		return ret;
	}
	
	protected double g(LearningNode variable, ArrayList<Node> parents,int[][]old){
			double riSum = 0;
			double qiSum = 0;
			  int  nij  = 0;
			  int  nijk = 0;
			  int  ri   = variable.getEstadoTamanho();
			  int  qi   = 1;
			  int ArrayNijk[][] = toIntArray(getFrequencies(variable,parents));
			  updateFrequencies(ArrayNijk, old);			  
			  if (parents != null && parents.size() > 0){
				  qi = getQ(parents);
			  }
			  for (int j = 0 ; j < qi ; j++ ){
					for(int k = 0 ; k < ri ; k++){
						nij+= ArrayNijk[k][j];
					}
					for (int k = 0; k < ri  ; k++ ){
						 nijk = ArrayNijk[k][j];
						 if(nij != 0 && nijk != 0){
							  riSum += (nijk*(log(nijk)-log(nij)));
						 }
					}
					qiSum += riSum;
					nij = 0;
					riSum = 0;
			  }
			  qiSum -= 0.5*qi*(ri -1)*log(caseNumber);
			  return qiSum;
		}
		
		public void updateFrequencies(int[][] news, int[][]old){			
		  	for(int i = 0 ; i < news.length; i++){
		  		for(int j = 0 ; j < news[0].length;j++){
		  			news[i][j] += old[i][j];
		  		}
		  	}
		}
}
