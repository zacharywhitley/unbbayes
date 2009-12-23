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
package unbbayes.example;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * Title: Sample code for using UnBBayes' API at text mode.
 * Description: This class, written in JAVA, opens a ".net" file ("asia.net"), and, after that,
 * 				modifies some parts and then compiles it. This class is provided only as
 * 				a sample, in order to show how to work out using this API, developed for bayesian
 * 				network manipulation.
 * 
 * Copyright:   Copyright (c) 2001
 * Company:     UnB - Universidade de Brasilia
 * @author      Rommel Novaes Carvalho
 * @author      Michael S. Onishi
 */
public class TextMode {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.example.resources.ExampleResources.class.getName());

	public static void main(String[] args) throws Exception {

		ProbabilisticNetwork rede = null;

		try {
			BaseIO io = new NetIO();
			rede = (ProbabilisticNetwork)io.load(new File("./examples/asia.net"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		ProbabilisticNode auxVP = new ProbabilisticNode();
		auxVP.setName(resource.getString("nodeName1"));
		auxVP.setDescription(resource.getString("nodeDescription"));
		auxVP.appendState(resource.getString("stateName0"));
		auxVP.appendState(resource.getString("stateName1"));
		PotentialTable auxTabPot = auxVP.getProbabilityFunction();
		auxTabPot.addVariable(auxVP);
		auxTabPot.addValueAt(0, 0.99f);
		auxTabPot.addValueAt(1, 0.01f);
		rede.addNode(auxVP);

		ProbabilisticNode auxVP2 = (ProbabilisticNode) rede.getNode(resource.getString("nodeName2"));
		Edge auxArco = new Edge(auxVP, auxVP2);
		rede.addEdge(auxArco);

		rede.compile();
		
		List<Node> nodeList = rede.getNodes();
		for (Node node : nodeList) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println(node.getStateAt(i) + " : " + ((ProbabilisticNode)node).getMarginalAt(i));
			}
		}
		
		int indexFirstNode = 0;
		ProbabilisticNode findingNode = (ProbabilisticNode)nodeList.get(indexFirstNode);
		int indexFirstState = 0;
		findingNode.addFinding(indexFirstState);
		
		System.out.println();
		
		try {
        	rede.updateEvidences();
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        }
        
		for (Node node : nodeList) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println(node.getStateAt(i) + " : " + ((ProbabilisticNode)node).getMarginalAt(i));
			}
		}
	}
}