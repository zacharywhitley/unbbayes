
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;

public abstract class PonctuationToolkit extends LearningToolkit{
  	
    private final int GH  = 0;
    private final int MDL = 1;
    private final int GHS = 2;    

    private int metric;    
    
    
    protected double gMDL(TVariavel variable, NodeList parents){
		double riSum = 0;
		double qiSum = 0;
        int  nij  = 0;
        int  nijk = 0;
        int  ri   = variable.getEstadoTamanho();
        int  qi   = 1;
        int ArrayNijk[][] = getFrequencies(variable,parents);
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
	
   
   protected double gGH(TVariavel variable, NodeList parents){
       double rSum;
       double sSum;
       double tSum;
       double qiSum = 0;              
       int nij;
       int nijk;
       int qi = 1;
       int  ri   = variable.getEstadoTamanho();
       int ArrayNijk[][] = getFrequencies(variable,parents);
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
   
   protected double gGHS(TVariavel variable, NodeList parents){
       double rSum;
       double sSum;
       double tSum;
       double qiSum = 0;              
       int nij;
       int nijk;
       int qi = 1;
       int  ri   = variable.getEstadoTamanho();
       int ArrayNijk[][] = getFrequencies(variable,parents);
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
   
   protected double getG(TVariavel variable , NodeList parents){
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
   
   private double fatLog(int n){
       if( n <= 100){
           return fatorialLog(n);       	
       } else{
       	   return stirlingLog(n);       	       	
       }	   	
   }
   
   private double fatorialLog(int n){
       double f = 0;
       for(int i = 1 ; i <= n ; i++){
       	   f += log(i);       	
       }   	
       return f;
   }
   
   private double stirlingLog(int n ){
       return (0.5*log(2*Math.PI) + (n+0.5)*log(n) - n*log(Math.E));	
   }
   
   
   

}
