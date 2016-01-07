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
package unbbayes.learning;

import java.util.ArrayList;

import unbbayes.controller.MainController;
import unbbayes.gui.LearningPNEditionDialog;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;


public class ProbabilisticController extends LearningToolkit{

    //private boolean ok; 
    
    public ProbabilisticController(ArrayList<Node> variables,int[][] matrix,
                       int[] vector,long caseNumber, MainController controller, boolean compacted){
        this.compacted = compacted;
        this.dataBase = matrix;
        this.vector = vector;
        this.caseNumber = caseNumber;
    	LearningNode variable;
    	int parentsLength;
    	float[][] arrayNijk;
    	PotentialTable table;
    	//ProbabilisticNetwork net    = controller.makeNetwork(variables);
    	//BaseIO base = new NetIO();
    	ProbabilisticNetwork net = controller.makeProbabilisticNetwork(variables);
        int length  = variables.size();    	
        for(int i = 0; i < length; i++) {
            variable  = (LearningNode)variables.get(i);
            table     = variable.getProbabilidades();
            table.addVariable(variable);
        }
    	//LearningPNEditionDialog window = new LearningPNEditionDialog(net);
        new LearningPNEditionDialog(net);
        for(int i = 0; i < length; i++) {
            variable  = (LearningNode)variables.get(i);
            arrayNijk = getFrequencies(variable,variable.getPais());                        
            table     = variable.getProbabilidades();
            parentsLength = variable.getTamanhoPais();
for2:       for (int j = 0; j < parentsLength; j++) {
            	Node pai = variable.getPais().get(j);
            	for (int k = 0; k < table.variableCount(); k++) {
            		if (pai == table.getVariableAt(k)) {
            			continue for2;
            		}            		
            	}
                table.addVariable(pai);
            }
            getProbability(arrayNijk, variable); 
        }                
        
        
        this.normalizeCPTs(net); // normalize the CPT of all nodes in the network
        
        controller.showProbabilisticNetwork(net);
    }

    /**
     * This method normalizes the Conditional Probability Table (CPT) of all nodes in a probabilistic network.
     * <br/>
     * <br/>
     * TODO create this method in {@link MainController}, and this method should simply delegate to it.
     * 
     * @param net : the network whose 
     * @see #ProbabilisticController(ArrayList, int[][], int[], long, MainController, boolean)
     * @see NormalizeTableFunction
     */
	protected void normalizeCPTs(ProbabilisticNetwork net) {
		
		if (net == null || net.getNodes() == null) {
			return;	// ignore null networks
		}
		
		// use this table normalizer in order to normalize table
		NormalizeTableFunction normalizer = new NormalizeTableFunction();
		
		// iterate on all nodes
		for (Node node : net.getNodes()) {
			if (node == null) {
				continue;	// ignore invalid nodes
			}
			if (node instanceof ProbabilisticNode) {
				// extract the CPT of this node
				PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
				if (table != null 
						&& (table instanceof ProbabilisticTable)) { // ignore nodes with unknown or invalid CPTs
					// this should normalize the CPT
					normalizer.applyFunction((ProbabilisticTable) table);
				}
				
			}
		}
		
	}
}
