
package unbbayes.aprendizagem;
import unbbayes.util.NodeList;

public class AlgorithmController{
	
	public AlgorithmController(NodeList variables,byte[][] matrix, int[] vector,
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

}
