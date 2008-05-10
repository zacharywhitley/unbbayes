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
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * @author Shou Matsumoto
 *
 */
public class Query {
	
	// TODO complete this class
	
	private MultiEntityBayesianNetwork mebn = null;
	private KnowledgeBase kb = null;
	private SSBNNode queryNode = null;
	
	/**
	 * Default query. 
	 */
	public Query(KnowledgeBase kb, SSBNNode queryNode, MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn; 
		this.kb = kb; 
		this.queryNode = queryNode;  
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}

	public MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}

	public void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

	public SSBNNode getQueryNode() {
		return queryNode;
	}

	public void setQueryNode(SSBNNode queryNode) {
		this.queryNode = queryNode;
	}
	
	

	
}
