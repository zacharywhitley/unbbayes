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
package unbbayes.aprendizagem.incrementalLearning.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.aprendizagem.TVariavel;
import unbbayes.aprendizagem.incrementalLearning.io.ILIO;
import unbbayes.aprendizagem.incrementalLearning.util.ILToolkit;
import unbbayes.controller.FileController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/**
 * 
 * 
 * @author Administrador 
 */
public class ILController extends ILToolkit {

   private BaseIO io;
   private ProbabilisticNetwork pn;
   private ArrayList nijks;
   private File file;
   private ILIO ilio;
   private List ssList;

   public ILController() {
      super();
      chooseFile();
      io = new NetIO();
      ilio = new ILIO();
      pn = ilio.getNet(file, io);
	  chooseFile();
      ssList = ilio.getSuficStatistics(file);
      chooseBetterNet();
      getFrontier();
      paramRecalc();
      getFile();
      ilio.makeNetFile(file,io,pn);
      getFile();      
      ilio.makeContFile(ssList,file);
   }

   private void getFrontier() {
      ssList.removeAll(ssList);
      Node node;
      for (int i = 0; i < pn.getNodeCount(); i++) {
         node = pn.getNodeAt(i);
         makeNodeFrontier(node);
      }
   }

   private void makeNodeFrontier(Node node) {
      makeNullFrontier(node);
      makeAddFrontier(node);
      makeRemoveFrontier(node);
   }

   private void makeNullFrontier(Node node) {
      Object[] frontier = new Object[3];
      frontier[0] = node.getName();
      frontier[1] = makeAddParentsStructure(node, null);
      frontier[2] = makeAddNijksStructure(node, null);
      ssList.add(frontier);
   }

   private void makeAddFrontier(Node node) {
      for (int i = 0; i < pn.getNodeCount(); i++) {
         if (!node.getDescription().equals(pn.getNodeAt(i).getDescription())
            && !isParent(node, pn.getNodeAt(i))) {
            Object[] frontier = new Object[3];
            frontier[0] = node.getName();
            frontier[1] = makeAddParentsStructure(node, pn.getNodeAt(i));
            frontier[2] = makeAddNijksStructure(node, pn.getNodeAt(i));
            ssList.add(frontier);
         }
      }
   }

   private void makeRemoveFrontier(Node node) {
      for (int i = 0; i < pn.getNodeCount(); i++) {
         if (isParent(node, pn.getNodeAt(i))) {
            Object[] frontier = new Object[3];
            frontier[0] = node.getName();
            frontier[1] = makeRemoveParentsStructure(node, pn.getNodeAt(i));
            frontier[2] = makeRemoveNijksStructure(node, pn.getNodeAt(i));
            ssList.add(frontier);
         }
      }
   }

   private List makeRemoveParentsStructure(Node node, Node lastParent) {
      List parentsName = new ArrayList();
      for (int i = 0; i < node.getParents().size(); i++) {
         String parentName = ((Node) node.getParents().get(i)).getDescription();
         if (!parentName.equals(lastParent.getDescription())) {
            parentsName.add(parentName);
         }
      }
      return parentsName;
   }

   private int[][] makeRemoveNijksStructure(Node node, Node lastParent) {
      NodeList parents = node.getParents();
      for (int i = 0; i < parents.size(); i++) {
         Node parent = (Node) node.getParents().get(i);
         if (!parent.getDescription().equals(lastParent.getDescription())) {
            parents.add(parent);
         }
      }
      int[][] nijk = getFrequencies((TVariavel) node, parents);
      parents.add(lastParent);
      return nijk;
   }

   private List makeAddParentsStructure(Node node, Node lastParent) {
      List parentsName = new ArrayList();
      for (int i = 0; i < node.getParents().size(); i++) {
         String parentName = ((Node) node.getParents().get(i)).getDescription();
         parentsName.add(parentName);
      }
      if (lastParent != null) {
         parentsName.add(lastParent.getDescription());
      }
      return parentsName;
   }

   private int[][] makeAddNijksStructure(Node node, Node lastParent) {
      NodeList parents = node.getParents();
      if (lastParent != null) {
         parents.add(lastParent);
      }
      int[][] nijk = getFrequencies((TVariavel) node, parents);
      if (lastParent != null) {
         parents.remove(parents.size() - 1);
      }

      return nijk;
   }

   private void chooseBetterNet() {
      float betterValue = Float.MIN_VALUE;
      Object[] frontierObject = (Object[]) ssList.get(0);
      Object[] betterFrontier = null;
      double g = Double.MIN_VALUE;
      Node node;
      for (int i = 0; i < ssList.size(); i++) {
         node = getNode((String) frontierObject[0]);
         double gAux =
            g(
               (TVariavel) node,
               getParents(node, (ArrayList) frontierObject[1]),
               (int[][]) frontierObject[2]);
         if (gAux > g) {
            betterFrontier = frontierObject;
            g = gAux;
         }
         frontierObject = (Object[]) ssList.get(i);
      }
      makeBetterNetwork(betterFrontier);
   }

   private NodeList getParents(Node node, ArrayList parents) {
      NodeList parentsAux = new NodeList();
      for (int i = 0; i < parents.size(); i++) {
         String parent = (String) parents.get(i);
         for (int j = 0; j < pn.getNodeCount(); j++) {
            if (pn.getNodeAt(j).getDescription().equals(parent))
               parentsAux.add(pn.getNodeAt(j));
            break;
         }
      }
      return parentsAux;
   }

   private Node getNode(String nodeName) {
      for (int i = 0; i < pn.getNodeCount(); i++) {
         if (pn.getNodeAt(i).getDescription().equals(nodeName)) {
            return pn.getNodeAt(i);
         }
      }
      return null;

   }

   private void makeBetterNetwork(Object[] betterNet) {
      Node node = getNode((String) betterNet[0]);
      NodeList parents = getParents(node, (ArrayList) betterNet[1]);
      node.getParents().removeAll(node.getParents());
      node.getParents().addAll(parents);
   }

   private boolean isParent(Node node, Node nodeAux) {
      int numberParents = node.getParents().size();
      for (int i = 0; i < numberParents; i++) {
         Node parentNode = (Node) node.getParents().get(i);
         if (parentNode.getDescription().equals(nodeAux.getDescription())) {
            return true;
         }
      }
      return false;
   }

   private void paramRecalc() {
      for (int i = 0; i < pn.getNodeCount(); i++) {
         int[][] old = (int[][]) nijks.get(i);
         TVariavel node = (TVariavel) pn.getNodeAt(i);
         int[][] news = getFrequencies((TVariavel) node, node.getParents());
         getProbability(news, (TVariavel) node);
      }
   }

   private File getFile() {
      String[] nets = new String[] { "net", "xml" };
      FileController fileController = FileController.getInstance();
      ;
      JFileChooser chooser =
         new JFileChooser(fileController.getCurrentDirectory());
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      // adicionar FileView no FileChooser para desenhar ícones de
      // arquivos		
      chooser.addChoosableFileFilter(new SimpleFileFilter(nets, "txt"));
      int option = chooser.showSaveDialog(null);
      if (option == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         if (file != null) {
            return file;
         }
      }
      return null;
   }

   private void chooseFile() {
      try {
         String[] nets = new String[] { "txt" };
         FileController fileController = FileController.getInstance();
         JFileChooser chooser =
            new JFileChooser(fileController.getCurrentDirectory());
         chooser.setMultiSelectionEnabled(false);
         chooser.addChoosableFileFilter(new SimpleFileFilter(nets, "txt"));
         int option = chooser.showOpenDialog(null);
         File file;
         if (option == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

/*
 * Os objetos vindos do arquivo de fronteira sao:
 * 	Nome da variavel
 * 	Nome dos pais da variavel
 *  matriz de contadores
 *


private Object[] getBetterFamilyPonctuation(TVariavel node){
	Object[] frontierObject = (Object[])ssList.get(0);
	double g = Double.MIN_VALUE;
	Object[] betterFrontier;   				
	while(node.getDescription().equals((String)frontierObject[0])){
	double gAux = g((TVariavel)node,getParents(node,(ArrayList)frontierObject[1]),(int[][])frontierObject[2]);
	if(gAux > g){
		betterFrontier = frontierObject;
		g = gAux;								
	}					
	frontierObject = new Object[]{(Object[])ssList.get(0)};			
	}
	}
	return frontierObject; 		
}   	


private Object[] getChangedNode(TVariavel node, int[][]nijk){
Object f[] = new Object[2];
f[0] = getAddFrontier(node,nijk);   				
f[1] = getRemoveFrontier(node,nijk);
Object[] o0 = (Object[])f[0];
Object[] o1 = (Object[])f[1];
if(((Float)o0[1]).floatValue() > ((Float)o1[1]).floatValue()){
	return new Object[]{o0,new Byte((byte)1)};
}		
return new Object[]{o1, new Byte((byte)0)};
}

private Object[] getAddFrontier(TVariavel node, int[][] nijk){   		
	NodeList parents = node.getParents();
	float betterValue = Float.MIN_VALUE;   		
	float g = 0.0f;
	TVariavel betterNode = null;   		  		
	for(int i = 0 ; i < pn.getNodeCount(); i++){
		TVariavel iNode = (TVariavel)pn.getNodeAt(i);
		if(iNode.getName().equals(node.getName()) || isParent(iNode,node)){
			 parents.add(iNode);
			 g = (float)g((TVariavel)node,parents,nijk);   				    				  				    				
		}
		if(g > betterValue){
			betterValue = g;
			betterNode = iNode;   			
		}
		parents.remove(iNode);   			
	}
	return new Object[]{betterNode,new Float(betterValue)};  		
}
*/

/*
private Object[] getRemoveFrontier(TVariavel node,int[][]nijk){
	NodeList parents = node.getParents();
int numberParents = parents.size();		
float betterValue = Float.MIN_VALUE;   		
float g = 0.0f;
TVariavel betterNode = null;
TVariavel iNode;	
for(int i = 0; i < numberParents; i++){
	iNode = (TVariavel)parents.remove(i);
	g = (float)g((TVariavel)node,parents,nijk);
	if(g > betterValue){
		betterValue = g;
		betterNode  = iNode; 
	}
	parents.add(iNode);
}
return new Object[]{betterNode,new Float(betterValue)};   		
}

private Object getReverseFrontier(TVariavel node){
int numberParents = node.getParents().size();
for(int i = 0; i < numberParents; i++){

}
return null;   		
}

private void  getNet(){
try{			
	String[] nets = new String[] { "net" };
	JFileChooser chooser = new JFileChooser(".");
	chooser.setMultiSelectionEnabled(false);				
	chooser.addChoosableFileFilter(
		new SimpleFileFilter(nets,"Carregar .net"));
	int option = chooser.showOpenDialog(null);
	if (option == JFileChooser.APPROVE_OPTION) {
		if (chooser.getSelectedFile() != null) {
			file = chooser.getSelectedFile();
			pn = io.load(file);					
		}
	}
}catch(LoadException le){
	le.printStackTrace();
}catch(IOException ie){
	ie.printStackTrace();
}   		
}*/
