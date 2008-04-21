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
package unbbayes.aprendizagem.Gibbs.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unbbayes.aprendizagem.LearningToolkit;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GibbsController extends LearningToolkit {

   private byte[][] data;   
   private ArrayList<Node> variables;     
   private ProbabilisticNetwork pn = null;

   public GibbsController(ProbabilisticNetwork pn,byte[][] data,  ArrayList<Node> variables) {
      this.data = data;
      this.variables = variables;
      this.pn = pn;
      double[] distribution = null;      
      for (int i = 0; i < data[0].length; i++) {
         for (int j = 0; j < data.length; j++) {
            //Dado faltante
            if (data[j][i] == -1) {
               distribution = getDistribution((LearningNode) variables.get(i), j);
               data[j][i] = (byte) getEstado(distribution);
            }
         }
      }
	 
   }   

   private double[] getDistribution(LearningNode node, int line) {
      byte[] parents = new byte[variables.size() - 1];
      boolean find;
      
      if(pn == null){      
      	for (int i = 0; i < parents.length + 1; i++) {
         	if (i != node.getPos()) {
            	parents[i] = data[line][i];
 	        } else {
    	        parents[i] = -1;
         	}
      	}
      }else{
      	for(int i = 0 ; i < pn.getNodeCount(); i++){
      		Node nodeAux = pn.getNodeAt(i);
      		if (nodeAux.getName().equals(node.getName())){
      			for(int j = 0 ; j < parents.length;j++){
      				find = false;
      				Node variable = variables.get(j);
      				for(int k = 0 ; k < nodeAux.getParents().size();k++){
      					Node pnNode = nodeAux.getParents().get(k);
      					if(variable.getName().equals(pnNode.getName())){
      						parents[j] = data[line][j];
      						find = true;
      						break;
      					}      					      					
      				}
      				if(!find){
      					parents[j] = -1;
      				}
      			}
      			break;
      		}
      	}      	
      }
      return distributionCalc(node, parents, node.getPos());
   }

   private double[] distributionCalc(LearningNode node, byte[] parents, int col) {
      int[] absoluteDistribution = new int[node.getEstadoTamanho()];
      int cont = 0;
      boolean achou;
      for (int i = 0; i < data.length; i++) {
         achou = true;
         for (int j = 0; j < data[0].length && achou; j++) {
            if (j != col && parents[j] != -1) {
               if (data[i][j] != parents[j]) {
                  achou = false;
                  continue;
               }
            }
         }
         if (achou && parents[col] != -1) {
            cont++;
            absoluteDistribution[data[i][col]]++;
         }
      }
      return makeRelativeDistribution(absoluteDistribution, cont, node);
   }

   private double[] makeRelativeDistribution(
      int[] absolute,
      int cont,
      LearningNode node) {
      double[] relative = new double[absolute.length];
      int ri = node.getEstadoTamanho();
      for (int i = 0; i < relative.length; i++) {
         relative[i] = ((double) (1 + absolute[i])) / (ri + cont);
      }
      return relative;
   }

   private int getEstado(double[] coluna) {
      double[][] faixa;
      double numero = Math.random();
      System.out.println("Numero Randomico = " + numero);
      faixa = criarFaixasIntervalo(coluna);
      for (int i = 0; i < coluna.length; i++) {
         if (i == 0) {
            if (numero <= faixa[i][1] && numero >= faixa[i][0]) {
               return i;
            }
         } else {
            if (numero <= faixa[i][1] && numero > faixa[i][0]) {
               return i;
            }
         }
      }
      return -1;
   }

   private double[][] criarFaixasIntervalo(double[] coluna) {
      double[][] faixa = new double[coluna.length][2];
      double[] colunaOrdenada = ordenar(coluna);
      double atual = 0.0d;
      for (int i = 0; i < coluna.length; i++) {
         faixa[i][0] = atual;
         faixa[i][1] = coluna[i] + atual;
         atual = faixa[i][1];
      }
      return faixa;
   }

   private double[] ordenar(double[] coluna) {
      List<Double> lista = new ArrayList<Double>();
      double[] colunaOrdenada = new double[coluna.length];
      for (int i = 0; i < coluna.length; i++) {
         lista.add(new Double(coluna[i]));
      }
      Collections.sort(lista);
      for (int i = 0; i < lista.size(); i++) {
         colunaOrdenada[i] = (lista.get(i)).doubleValue();
      }
      return colunaOrdenada;
   }

}
