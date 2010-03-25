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

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;

public abstract class PonctuationToolkit extends LearningToolkit{
  	
    private final int GH  = 0;
    private final int MDL = 1;
    private final int GHS = 2;    

    private int metric;    
    
    
    protected double gMDL(LearningNode variable, ArrayList<Node> parents){
		double riSum = 0;
		double qiSum = 0;
        float  nij  = 0;
        float  nijk = 0;
        int  ri   = variable.getEstadoTamanho();
        int  qi   = 1;
        float ArrayNijk[][] = getFrequencies(variable,parents);
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
	
   
   protected double gGH(LearningNode variable, ArrayList<Node> parents){
       double rSum;
       double sSum;
       double tSum;
       double qiSum = 0;              
       float nij;
       float nijk;
       int qi = 1;
       int  ri   = variable.getEstadoTamanho();
       float ArrayNijk[][] = getFrequencies(variable,parents);
        if (parents != null && parents.size() > 0){
           qi = getQ(parents);
        }
       for (int j = 0 ; j < qi ; j++ ){            
            rSum = fatorialLog(ri-1);
            nij = 0;            
            for(int k = 0 ; k < ri; k++){
                nij += ArrayNijk[k][j];   	
            }
            sSum = fatorialLog(nij+ri-1);            
            tSum = 0;                                  
            for (int k = 0 ; k < ri ; k++){ 
            	nijk = ArrayNijk[k][j];            	               
                tSum += fatorialLog(nijk);                 
            }
            qiSum += (rSum - sSum + tSum); 
       }       
       return qiSum;              
   }
   
   protected double gGHS(LearningNode variable, ArrayList<Node> parents){
       double rSum;
       double sSum;
       double tSum;
       double qiSum = 0;              
       float nij;
       float nijk;
       int qi = 1;
       int  ri   = variable.getEstadoTamanho();
       float ArrayNijk[][] = getFrequencies(variable,parents);
        if (parents != null && parents.size() > 0){
           qi = getQ(parents);
        }
       for (int j = 0 ; j < qi ; j++ ){            
            rSum = fatLog(ri-1);
            nij = 0;            
            for(int k = 0 ; k < ri; k++){
                nij += ArrayNijk[k][j];   	
            }
            sSum = fatLog(nij+ri-1);            
            tSum = 0;                                  
            for (int k = 0 ; k < ri ; k++){ 
            	nijk = ArrayNijk[k][j];            	               
                tSum += fatLog(nijk);                 
            }
            qiSum += (rSum - sSum + tSum); 
       }       
       return qiSum;
   }	
   
   protected double getG(LearningNode variable , ArrayList<Node> parents){
       switch(metric){       
           case 0 : return gGH(variable, parents);
           case 1 : return gMDL(variable, parents);
           case 2 : return gGHS(variable, parents);                  	
       }
       return 0;	   	
   }
   
   protected void setMetric(String metric){
       if(metric.equalsIgnoreCase("GH")){
           this.metric = 0;       	
           return;
       } else if(metric.equalsIgnoreCase("MDL")){
       	   this.metric = 1;
       	   return;      	
       } else if(metric.equalsIgnoreCase("GHS")){
       	   this.metric = 2;
       	   return; 
       }	 	
   }
   
   private double fatLog(float n){
       if( n <= 100){
           return fatorialLog(n);       	
       } else{
       	   return stirlingLog(n);       	       	
       }	   	
   }
   
   private double fatorialLog(float n){
       double f = 0;
       for(int i = 1 ; i <= n ; i++){
       	   f += log(i);       	
       }   	
       return f;
   }
   
   private double stirlingLog(float n ){
       return (0.5*log(2*Math.PI) + (n+0.5)*log(n) - n*log(Math.E));	
   }
   
   
   

}
