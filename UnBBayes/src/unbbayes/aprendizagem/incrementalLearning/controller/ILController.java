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

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import unbbayes.aprendizagem.LearningToolkit;
import unbbayes.aprendizagem.PonctuationToolkit;
import unbbayes.aprendizagem.TVariavel;
import unbbayes.aprendizagem.incrementalLearning.util.ILToolkit;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.io.NetIO;
import unbbayes.monteCarlo.gui.TelaParametros;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/**
 * 
 * 
 * @author Administrador 
 */
public class ILController extends ILToolkit{
	
	private BaseIO io;
	private ProbabilisticNetwork pn;	
	private ArrayList nijks;
   
   	public ILController(ArrayList nijks){ 
      super();
      this.nijks = nijks;
      io = new NetIO();
	  getNet();
	  getFrontier();
	  paramRecalc();      
   	}
   	
   	private void getFrontier(){  		  
		float betterValue = Float.MIN_VALUE;
		Object[] o;
		Object[]o1;		 		
		Object[] betterTransformation = null;
		TVariavel node;
   		for(int i = 0 ; i < pn.getNodeCount(); i++){
   			node = (TVariavel)pn.getNodeAt(i);
   			node.setPos(i);   			
   			int[][] nijk = (int[][])nijks.get(i);
   			o = getBetterFamilyPonctuation(node,nijk);
   			o1 = (Object[])o[0];
   			if(((Float)o1[1]).floatValue() > betterValue){
   				betterValue = ((Float)o1[1]).floatValue();
   				betterTransformation = new Object[]{node,o1[0],o[1]};   			
   			}   			
   		}
   		makeBetterNetwork(betterTransformation);   	
   	}
   	
   	private void makeBetterNetwork(Object[] bt){
   		byte number =((Byte)bt[2]).byteValue();
		TVariavel node = (TVariavel)bt[0]; 
   		if( number == 2){
   			return;  					   		   		   		
   		}else if(number == 1){   			
   			TVariavel parentNode = (TVariavel)bt[1];
   			node.getParents().add(parentNode);   			
   		}else if(number == 0){
   			TVariavel removeNode = (TVariavel)bt[1];
   			node.getParents().remove(removeNode);
   		}   		
   	}
   	
   	private Object[] getBetterFamilyPonctuation(TVariavel node,int[][]nijk){   		
		double g = g((TVariavel)node,node.getParents(),nijk);   			   			
		Object[]  o = getChangedNode(node,nijk);
		Object[]  o1= (Object[])o[0]; 
		if(g > ((Float)o1[1]).floatValue()){
			Object[] o2 = new Object[]{null,new Float(g)};
			return new Object[]{o2,new Byte((byte)2)};   				    				
		}else{
			return o;   			   				   			  		
		}   		
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
   	
   	private boolean isParent(TVariavel parent, Node sun){   		
   		NodeList parents = sun.getParents();
		int numberParents = parents.size();
		TVariavel parentNode = null;   		 
   		for(int i = 0; i < numberParents; i++){
   			parentNode = (TVariavel)parents.get(i);   			 
   			if(parentNode.getName().equals(parent.getName())){
   				return true;   			   			 
   			}
   		}
   		return false;
   	}
   	
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
					pn = io.load(chooser.getSelectedFile());					
				}
			}
		}catch(LoadException le){
			le.printStackTrace();
		}catch(IOException ie){
			ie.printStackTrace();
		}   		
   	}
   	
   	private void paramRecalc(){
   		for(int i = 0; i < pn.getNodeCount(); i++){
   			int[][]old = (int[][])nijks.get(i);
   			TVariavel node = (TVariavel)pn.getNodeAt(i);
   			int[][] news =  getFrequencies((TVariavel)node,node.getParents());
   			updateFrequencies(news, old);
   			getProbability(news,(TVariavel)node);   			
   		}  	
   	}
}
