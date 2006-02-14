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
package unbbayes.aprendizagem;

import unbbayes.util.NodeList;
import unbbayes.controller.MainController;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.gui.TJanelaEdicao;


public class ProbabilisticController extends LearningToolkit{

    public ProbabilisticController(NodeList variables,int[][] matrix,
                       int[] vector,long caseNumber, MainController controller, boolean compacted){
        this.compacted = compacted;
        this.dataBase = matrix;
        this.vector = vector;
        this.caseNumber = caseNumber;
    	TVariavel variable;
    	int parentsLength;
    	int[][] arrayNijk;
    	PotentialTable table;
    	ProbabilisticNetwork net    = controller.makeNetwork(variables);
    	int length  = variables.size();    	
        for(int i = 0; i < length; i++) {
            variable  = (TVariavel)variables.get(i);
            table     = variable.getProbabilidades();
            table.addVariable(variable);
        }
    	new TJanelaEdicao(net);
        for(int i = 0; i < length; i++) {
            variable  = (TVariavel)variables.get(i);
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
        controller.showNetwork(net);
    }
}
