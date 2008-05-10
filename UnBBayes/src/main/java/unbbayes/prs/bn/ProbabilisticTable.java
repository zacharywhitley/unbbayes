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
package unbbayes.prs.bn;

import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;

/**
 * Probabilistic Potential Table
 * @author Michael
 */
public class ProbabilisticTable extends PotentialTable implements java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

	public ProbabilisticTable() {
	}

	/**
	 *  Retira a vari�vel da tabela. Utilizado tamb�m para marginaliza��o generalizada.
	 *
	 *@param  variavel  Variavel a ser retirada da tabela.
	 */
	public void removeVariable(Node variavel) {
		calcularFatores();
		int index = variaveis.indexOf(variavel);
		if (variavel.getType() == Node.DECISION_NODE_TYPE) {
			DecisionNode decision = (DecisionNode) variavel;
			int statesSize = variavel.getStatesSize();
			if (decision.hasEvidence()) {
				finding(variaveis.size()-1, index, new int[variaveis.size()], decision.getEvidence());
			} else {
//				sum(variaveis.size()-1, index, 0, 0);
				sum(index);
				for (int i = dados.size-1; i >= 0; i--) {
					dados.data[i] = dados.data[i] / statesSize;
				}
			}
		} else {
//		  sum(variaveis.size()-1, index, 0, 0);
		  sum(index);
		}
		variableModified();
		variaveis.remove(index);
	}


	/**
	 *  Verifica a consist�ncia das probabilidades da tabela.
	 *
	 * @throws Exception se a tabela n�o soma 100 para todos os estados fixada
	 *				   qualquer configura��o de estados dos pais.
	 */
	public void verifyConsistency() throws Exception {
		Node auxNo = variaveis.get(0);
		int noLin = auxNo.getStatesSize();

		/* Check if the node represents a numeric attribute */
		if (noLin == 0) {
			/* 
			 * The node represents a numeric attribute which has no potential
			 * table. Just Return.
			 */
			return;
		}
		
		int noCol = 1;
		int sizeVariaveis = variaveis.size();
		for (int k = 1; k < sizeVariaveis; k++) {
			auxNo = variaveis.get(k);
			noCol *= auxNo.getStatesSize();
		}

		float soma;
		for (int j = 0; j < noCol; j++) {
			soma = 0;
			for (int i = 0; i < noLin; i++) {
				soma += dados.data[j * noLin + i] * 100;
			}

			if (Math.abs(soma - 100.0) > 0.01) {
				throw new Exception(resource.getString("variableTableName") + variaveis.get(0) + resource.getString("inconsistencyName") + soma + "%\n");
			}
		}
	}

	/**
	 * Returns a new instance of a ProbabilisticTable. Implements the abstract method from PotentialTable.
	 * @return a new instance of a ProbabilisticTable.
	 */
	public PotentialTable newInstance() {
		return new ProbabilisticTable();
	}
}