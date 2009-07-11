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
package unbbayes.prs.mebn.ssbn.util;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

public class PositionAdjustmentUtils {

	public static String fileTestLoad = "rede.xml";  
	public static String fileTestSave= "novarede.xml";  
	
	
//	private static Point sizeGraph = new Point(1500, 1500); 
	private static Point sizeSquare = new Point(100, 100); 
	private static Point margin = new Point(100, 100); 
	
	public static Dimension adjustPositionProbabilisticNetwork(ProbabilisticNetwork net){
		InfoNetwork infoNetwork = new InfoNetwork(); 
		createInfoNodesList(infoNetwork, net);
		return adjustPositions(infoNetwork); 
	}

	private static void createInfoNodesList(InfoNetwork infoNetwork, ProbabilisticNetwork net) {
		ArrayList<Node> nodes = net.getNodes();
		
		for(int i=0; i < nodes.size(); i++){
			if(nodes.get(i).getChildren().size() == 0){
				createInfoNode(infoNetwork, (ProbabilisticNode)nodes.get(i), 1); 
			}
		}
	}
	
	private static void createInfoNode(InfoNetwork infoNetwork, ProbabilisticNode pn, int initialLevel){
		
		InfoNode infoNode = new InfoNode(pn); 
		infoNode.setLevel(initialLevel); 
		infoNetwork.addInfoNode(infoNode); 
//		System.out.println("InfoNode: " + pn + " Level:" + initialLevel);
		
		initialLevel++;
		for(int i=0; i < pn.getParents().size(); i++){
			createInfoNode(infoNetwork, (ProbabilisticNode)pn.getParents().get(i), initialLevel); 
		}
	}
	
	private static Dimension adjustPositions(InfoNetwork infoNetwork){
		
//		int numColunas = (int)(sizeGraph.getX() / sizeSquare.getX()); 
//		int numLinhas = (int)(sizeGraph.getY() / sizeSquare.getY());
		
		int numColunas = infoNetwork.getMaxNodesInLevel(); 
		int numLinhas = infoNetwork.getNumLevels();

		//size of the graph

		double width = numColunas*sizeSquare.getX()+ margin.getX() + sizeSquare.getX(); 
		double heigth = numLinhas*sizeSquare.getY()+ margin.getY() + sizeSquare.getY();
		
		InfoNode[][] map = new InfoNode[numColunas][numLinhas];
		
//		int line = numLinhas - 1; 
//		
//		int level = infoNetwork.getNumLevels(); 
////		System.out.println("Num Levels: " + level);
//		
//		while(level >= 0){
//			List<InfoNode> nodesOfLevel = infoNetwork.getInfoNodesOfLevel(level);
//			int coluna = 0; 
//			for(InfoNode infoNode: nodesOfLevel){
//				map[coluna][line] = infoNode; 
//				coluna++; 
//			}
//			line--; level--;  
//		}

		int line = 0; 
		
		int level = infoNetwork.getNumLevels(); 
//		System.out.println("Num Levels: " + level);
		
		while(level >= 0){
			List<InfoNode> nodesOfLevel = infoNetwork.getInfoNodesOfLevel(level);
			int coluna = 0; 
			for(InfoNode infoNode: nodesOfLevel){
				map[coluna][line] = infoNode; 
				coluna++; 
			}
			line++; level--;  
		}
		
		
		double positionX; 
		double positionY;
		
		for(int coluna = 0; coluna < numColunas; coluna++){
			for(int linha = 0; linha < numLinhas; linha++){
				if(map[coluna][linha] != null){
					positionX = (coluna)*sizeSquare.getX() + margin.getX(); 
					positionY = (linha)*sizeSquare.getY() - (numLinhas - infoNetwork.getNumLevels())*sizeSquare.getY()  + margin.getY();
					
//	                System.out.println("Node:" + map[coluna][linha].getPn() + " Map:" + coluna + "," + linha + 
//	                		" Position:" + positionX + "," + positionY);
					map[coluna][linha].getPn().setPosition(positionX, positionY); 
				}
			}
		}
		
		return new Dimension((int)width, (int)heigth); 
		
	}
	
	public static void main(String[] arguments){
		
		System.out.println("Init");
		ProbabilisticNetwork net = null;
		XMLBIFIO io = new XMLBIFIO();	
		try {
          net = io.load(new File(PositionAdjustmentUtils.fileTestLoad));
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PositionAdjustmentUtils adjustment = new PositionAdjustmentUtils(); 
		if(net!=null){
		   adjustment.adjustPositionProbabilisticNetwork(net); 
		   try {
			io.save(new File(PositionAdjustmentUtils.fileTestSave), net);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		System.out.println("End");
	}
	
private static class InfoNetwork{
	
	private int numLevels; 
	private List<InfoNode> nodes; 
	
	private Map<Integer, Integer> nodesAtLevel; 
	
	public InfoNetwork(){
		numLevels = 0; 
		nodesAtLevel = new HashMap<Integer, Integer>(); 
		nodes = new ArrayList<InfoNode>(); 
	}
	
	public void addInfoNode(InfoNode n){
		nodes.add(n);
		
		if(n.getLevel() > numLevels){
			numLevels = n.getLevel();
		}
		
		if(nodesAtLevel.get(n.getLevel()) == null){
			nodesAtLevel.put(n.getLevel(), 1); 
		}else{
			nodesAtLevel.put(n.getLevel(), nodesAtLevel.get(n.getLevel()) + 1); 
		}
		
	}
	
	public List<InfoNode> getInfoNodesOfLevel(int level){
		List<InfoNode> ret = new ArrayList<InfoNode>(); 
		for(InfoNode node : nodes){
			if(node.getLevel() == level){
				ret.add(node); 
			}
		}
		return ret; 
	}

	public int getNumLevels() {
		return numLevels;
	}

	public int getMaxNodesInLevel(){
		int max = 0; 
		for(int i = 1; i <= numLevels; i++){
			Integer testValue = nodesAtLevel.get(i); 
			if(testValue!=null){
				if(testValue > max){
					max = testValue; 
				}
			}
		}
		return max; 
	}
	
	public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
	}
	
}
	
private static class InfoNode{
	
	private int level; 
	private ProbabilisticNode pn; 
	
	public InfoNode(ProbabilisticNode pn){
		this.pn = pn; 
	}

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public ProbabilisticNode getPn() {
		return pn;
	}

	public void setPn(ProbabilisticNode pn) {
		this.pn = pn;
	}
	
}
	
}
