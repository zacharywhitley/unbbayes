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
package unbbayes.learning;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;


/**
 * @author Danilo 
 * 
 * This class is a factory that choose witch algorith the 
 * program will use to learn the net.
 */
public class AlgorithmController{
	
	/** Available values in pamp array at paradigm position (0) */
	public enum PARADIGMS {Ponctuation,IC};	
	/** Available values in pamp array at algorithm position (1) */
	public enum SCORING_ALGORITHMS {K2,B};
	/** Available values in pamp array at metric position (2) */
	public enum METRICS {MDL,GH, GHS};		
	
	/** IC algorithm available for pamp array at algorithm position (1) */
	public static final String INDEPENDENCE_ALGORITHM_CBLA = "CBL-A";  
	/** IC algorithm available for pamp array at algorithm position (1) */
	public static final String INDEPENDENCE_ALGORITHM_CBLB = "CBLB";  
	
	/**
	 * 
	 */
	public AlgorithmController(List<Node> variables,int[][] matrix, int[] vector,
	        long caseNumber, String[] pamp, boolean compacted){	        	
	        if(pamp[0].equalsIgnoreCase(PARADIGMS.Ponctuation.name())){
	        	if(pamp[1].equalsIgnoreCase(SCORING_ALGORITHMS.K2.name())){
	        		K2 k2 = new K2(variables, matrix, vector, caseNumber,pamp[2],pamp[3],compacted);	        		
	        	}
	        	else if(pamp[1].equalsIgnoreCase(SCORING_ALGORITHMS.B.name())){
	        		B b = new B(variables, matrix, vector,caseNumber,pamp[2], pamp[3],compacted);	        			
	        	}        		        		        	
	        } else if (pamp[0].equalsIgnoreCase(PARADIGMS.IC.name())){ 
	        	if(pamp[1].equalsIgnoreCase(INDEPENDENCE_ALGORITHM_CBLA)){
	        		CBLA cblA = new CBLA(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	} else if(pamp[1].equalsIgnoreCase(INDEPENDENCE_ALGORITHM_CBLB)){
	        		CBLB cblB = new CBLB(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	}	        	
	        }	        
	} 
	
	public AlgorithmController(List<Node> variables,int[][] matrix, int[] vector,
	        long caseNumber, String[] pamp, boolean compacted, int classex){	        	
	        if(pamp[0].equalsIgnoreCase(PARADIGMS.Ponctuation.name())){
	        	if(pamp[1].equalsIgnoreCase(SCORING_ALGORITHMS.K2.name())){
	        		new K2(variables, matrix, vector, caseNumber,pamp[2],pamp[3],compacted);	        		
	        	}
	        	else if(pamp[1].equalsIgnoreCase(SCORING_ALGORITHMS.B.name())){
	        		new B(variables, matrix, vector,caseNumber,pamp[2], pamp[3],compacted);	        			
	        	}        		        		        	
	        } else if (pamp[0].equalsIgnoreCase(PARADIGMS.IC.name())){ 
	        	if(pamp[1].equalsIgnoreCase(INDEPENDENCE_ALGORITHM_CBLA)){
	        		new CBLA(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	} else if(pamp[1].equalsIgnoreCase(INDEPENDENCE_ALGORITHM_CBLB)){
	        		new CBLB(variables,matrix,vector,caseNumber,pamp[3],compacted,classex);	        		
	        	}	        	
	        }	        
	} 

}
