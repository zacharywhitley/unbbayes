package unbbayes.aprendizagem.Gibbs.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.aprendizagem.LearningToolkit;
import unbbayes.aprendizagem.TVariavel;
import unbbayes.aprendizagem.Gibbs.gui.GibbsFrame;
import unbbayes.aprendizagem.Gibbs.io.IOGibbs;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GibbsController extends LearningToolkit {

   private byte[][] data;
   private int[] vector;
   private NodeList variables;
   private GibbsFrame gf;
   private BaseIO io;
   private ProbabilisticNetwork pn = null;

   public GibbsController(byte[][] data, NodeList variables) {
      this.data = data;
      this.variables = variables;
      double[] distribution = null;
      gf = new GibbsFrame();
      addListeners();
      for (int i = 0; i < data[0].length; i++) {
         for (int j = 0; j < data.length; j++) {
            //Dado faltante
            if (data[j][i] == -1) {
               distribution = getDistribution((TVariavel) variables.get(i), j);
               data[j][i] = (byte) getEstado(distribution);
            }
         }
      }
	 
   }

   private void addListeners() {
      gf.addCancelListener(cancelListener);
      gf.addContinueListener(continueListener);
      gf.addChooseNetListener(chooseNetListener);
   }

   private double[] getDistribution(TVariavel node, int line) {
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

   private double[] distributionCalc(TVariavel node, byte[] parents, int col) {
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
      TVariavel node) {
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
      List lista = new ArrayList();
      double[] colunaOrdenada = new double[coluna.length];
      for (int i = 0; i < coluna.length; i++) {
         lista.add(new Double(coluna[i]));
      }
      Collections.sort(lista);
      for (int i = 0; i < lista.size(); i++) {
         colunaOrdenada[i] = ((Double) lista.get(i)).doubleValue();
      }
      return colunaOrdenada;
   }

   ActionListener cancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
         gf.dispose();
      }
   };

   ActionListener continueListener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
         gf.dispose();
		 IOGibbs iog = new IOGibbs(data,variables);
		 iog.makeFile();
      }
   };

   ActionListener chooseNetListener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
         try {
            String[] nets = new String[] { "net" };
            JFileChooser chooser = new JFileChooser(".");
            chooser.setMultiSelectionEnabled(false);
            chooser.addChoosableFileFilter(
               new SimpleFileFilter(nets, "Carregar .net"));
            int option = chooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
               if (chooser.getSelectedFile() != null) {
                  pn = io.load(chooser.getSelectedFile());
               }
            }
         } catch (LoadException le) {
            le.printStackTrace();
         } catch (IOException ie) {
            ie.printStackTrace();
         }
      }
   };

}
