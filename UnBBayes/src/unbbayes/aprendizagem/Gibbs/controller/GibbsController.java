
package unbbayes.aprendizagem.Gibbs.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class GibbsController extends LearningToolkit {

   private byte[][]data;
   private int[] vector;
   private NodeList variables;
   
   public GibbsController(byte[][] data, int[] vector, NodeList variables){
   		this.data = data;
   		this.vector = vector;
   		this.variables = variables;
   		double[] distribution = null;
   		for(int i = 0 ; i < data[0].length; i++){
   			for(int j = 0; j < data.length; j++){
   				//Dado faltante
   				if(data[j][i] == -1){
   					distribution = getDistribution((TVariavel)variables.get(i),j);
					data[j][i] = (byte)getEstado(distribution);   					   					  			   			
   				}   				
   			}
   		}      
   }
   
   private double[] getDistribution(TVariavel node, int line){
   		byte[] parents = new byte[variables.size()-1];
   		for(int i = 0 ; i < parents.length +1; i++){
   			if(i !=  node.getPos()){
				parents[i] = data[line][i];
   			}else{
   				parents[i] = -1;   			
   			}
   		}   		   		
   		return distributionCalc(node,parents, node.getPos());   		
   }
   
   private double[] distributionCalc(TVariavel node, byte[] parents, int col){   		   	 	
   		int[] absoluteDistribution = new int[node.getEstadoTamanho()];
		int cont = 0;		
   		boolean achou;
   		for(int i = 0 ; i < data.length; i++){
   			achou = true;
   			for(int j = 0; j < data[0].length && achou;j++){
   				if(j!= col){
   					if(data[i][j] != parents[j]){
   						achou = false;
   						continue;   					   					
   					}
   				}
   			}
   			if(achou && parents[col] != -1){
   				cont++;   				
				absoluteDistribution[data[i][col]]++;
   			}
   		}   	
   		return makeRelativeDistribution(absoluteDistribution,cont,node);
   }
   
   private double[] makeRelativeDistribution(int[] absolute, int cont, TVariavel node){
   		double[] relative = new double[absolute.length];
   		int ri = node.getEstadoTamanho();
   		for(int i = 0 ; i < relative.length; i++){
   			relative[i] = ((double)(1+absolute[i]))/(ri+cont);  			
   		}
   		return relative;
   }
   
	private int getEstado(double[] coluna){		
			double[][] faixa;
			double numero = Math.random();
			System.out.println("Numero Randomico = "+ numero);
			faixa = criarFaixasIntervalo(coluna);			
			for(int i = 0; i< coluna.length; i++){
				if(i ==0){
					if (numero <= faixa[i][1] && numero >= faixa[i][0]){
						return i;					
					}  				
				}else{
					if(numero <= faixa[i][1] && numero > faixa[i][0]){
						return i;	
					}
				}			
			}
			return -1;				
		}
	
		private double[][] criarFaixasIntervalo(double[] coluna){
			double[][] faixa = new double[coluna.length][2];		
			double[] colunaOrdenada = ordenar(coluna);
			double atual = 0.0d;
			for(int i = 0 ; i < coluna.length; i++){
				faixa[i][0] = atual;
				faixa[i][1] = coluna[i] + atual;
				atual = faixa[i][1]; 
			}
			return faixa;
		}
	
		private double[] ordenar(double[] coluna){		
			List lista = new ArrayList();
			double[] colunaOrdenada = new double[coluna.length];	
			for(int i = 0 ; i < coluna.length; i++){
				lista.add(new Double(coluna[i]));				
			}
			Collections.sort(lista);
			for(int i = 0 ; i < lista.size(); i++){
				colunaOrdenada[i] = ((Double)lista.get(i)).doubleValue();				
			}				
			return colunaOrdenada;		
		}
   	
   
   

}
