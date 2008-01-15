package unbbayes.aprendizagem.incrementalLearning.util;




import unbbayes.aprendizagem.LearningToolkit;
import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ILToolkit extends LearningToolkit {
	
	protected double g(LearningNode variable, NodeList parents,int[][]old){
			double riSum = 0;
			double qiSum = 0;
			  int  nij  = 0;
			  int  nijk = 0;
			  int  ri   = variable.getEstadoTamanho();
			  int  qi   = 1;
			  int ArrayNijk[][] = getFrequencies(variable,parents);
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
