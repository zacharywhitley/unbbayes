
package unbbayes.aprendizagem;
import unbbayes.util.NodeList;

/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * @author Danilo 
 * 
 * This class is a factory that choose witch algorith the 
 * program will use to learn the net.
 */
public class AlgorithmController{
	
	/**
	 * 
	 */
	public AlgorithmController(NodeList variables,int[][] matrix, int[] vector,
	        long caseNumber, String[] pamp, boolean compacted){	        	
	        if(pamp[0].equalsIgnoreCase("Ponctuation")){
	        	if(pamp[1].equalsIgnoreCase("k2")){
	        		K2 k2 = new K2(variables, matrix, vector, caseNumber,pamp[2],pamp[3],compacted);	        		
	        	}
	        	else if(pamp[1].equalsIgnoreCase("B")){
	        		B b = new B(variables, matrix, vector,caseNumber,pamp[2], pamp[3],compacted);	        			
	        	}        		        		        	
	        } else if (pamp[0].equalsIgnoreCase("IC")){ 
	        	if(pamp[1].equalsIgnoreCase("CBL-A")){
	        		CBLA cblA = new CBLA(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	} else if(pamp[1].equalsIgnoreCase("CBL-B")){
	        		CBLB cblB = new CBLB(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	}	        	
	        }	        
	} 
	
	public AlgorithmController(NodeList variables,int[][] matrix, int[] vector,
	        long caseNumber, String[] pamp, boolean compacted, int classex){	        	
	        if(pamp[0].equalsIgnoreCase("Ponctuation")){
	        	if(pamp[1].equalsIgnoreCase("k2")){
	        		new K2(variables, matrix, vector, caseNumber,pamp[2],pamp[3],compacted);	        		
	        	}
	        	else if(pamp[1].equalsIgnoreCase("B")){
	        		new B(variables, matrix, vector,caseNumber,pamp[2], pamp[3],compacted);	        			
	        	}        		        		        	
	        } else if (pamp[0].equalsIgnoreCase("IC")){ 
	        	if(pamp[1].equalsIgnoreCase("CBL-A")){
	        		new CBLA(variables,matrix,vector,caseNumber,pamp[3],compacted);	        		
	        	} else if(pamp[1].equalsIgnoreCase("CBL-B")){
	        		new CBLB(variables,matrix,vector,caseNumber,pamp[3],compacted,classex);	        		
	        	}	        	
	        }	        
	} 

}
