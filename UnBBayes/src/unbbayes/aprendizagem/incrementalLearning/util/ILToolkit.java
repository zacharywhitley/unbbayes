/*
 * Created on 19/07/2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.aprendizagem.incrementalLearning.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import unbbayes.aprendizagem.LearningToolkit;
import unbbayes.aprendizagem.TVariavel;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ILToolkit extends LearningToolkit {
	
	protected double g(TVariavel variable, NodeList parents,int[][]old){
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
		  	//Discutir com o Ladeira
		}
		
		public void load(File input) throws IOException{
			BufferedReader r = new BufferedReader(new FileReader(input));
			StreamTokenizer st = new StreamTokenizer(r);
			st.resetSyntax();
	
			st.wordChars('A', 'Z');
			st.wordChars('a', '}');
			st.wordChars('\u00A0', '\u00FF'); // letras com acentos
			st.wordChars('_', '_');
			st.wordChars('-', '-');
			st.wordChars('0', '9');
			st.wordChars('.', '.');
			st.wordChars('%', '%');
			st.ordinaryChars('(', ')');
			st.eolIsSignificant(false);
			st.quoteChar('"');
			//st.commentChar('%');
		 if (st.sval.equals("potential")) {
			  proximo(st);
			  //Node auxNo1 = net.getNode(st.sval);	
			  proximo(st);
			  if (st.sval.equals("|")) {
				  proximo(st);
			  }		  
			  while (!st.sval.startsWith("{")) {			  
				  proximo(st);
			  }			  
			  if (st.sval.length() == 1) {
				  proximo(st);
			  }	
			  int nDim = 0;	
			  while (!st.sval.endsWith("}")) {
				  if (st.sval.equals("data")) {					  
					  proximo(st);
					  while (!st.sval.equals("}")) {
						  if (st.sval.equals("%")) {
							  readTillEOL(st);
						  } else {
							  //Construcao da tabelas nijk
						  }
						  proximo(st);
					  }
				  } else {
					  /*throw new LoadException(
						  ERROR_NET
							  + " l."
							  + st.lineno()
							  + resource.getString("LoadException5"));*/
				  }
			  }
		  }
		}
		
	private int proximo(StreamTokenizer st) throws IOException {
		do {
			st.nextToken();
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}
	
	private void readTillEOL(StreamTokenizer tokenizer) throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
		};
		tokenizer.pushBack();
	}

}
