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
    	int[][] arrayNijk;
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
        controller.showProbabilisticNetwork(net);
    }
}
