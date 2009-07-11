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

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * @author Shou Matsumoto
 * @author Laecio Lima dos Santos 
 */
public class Query {
	
	private MultiEntityBayesianNetwork mebn = null;
	private KnowledgeBase kb = null;
	private SSBNNode queryNode = null;

	private ResidentNode residentNode = null; 
	private List<OVInstance> arguments = null;
	private SimpleSSBNNode simpleSSBNNode = null; 
	
	private ProbabilisticNode probabilisticNode = null; 
	
	/*
	 * Note: For while have two versions of the Query's constructor for mantain the 
	 * conpatibility for the GIA's Algorithm version and for the Laskey's 
	 * algorithm version. Solve this after do the refactory in the Gia version. 
	 */
	
	/**
	 * Default query.
	 * This version is used in Gia's SSBN Algorithm 
	 */
	@Deprecated
	public Query(KnowledgeBase kb, SSBNNode queryNode, MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn; 
		this.kb = kb; 
		this.residentNode = queryNode.getResident(); 
		this.queryNode = queryNode;  
		this.arguments = new ArrayList<OVInstance>(); 
	}
	
	/**
	 * This version is used in Laskey's SSBN Algorithm
	 * @param residentNode
	 * @param arguments
	 */
	public Query(ResidentNode residentNode, List<OVInstance> arguments){
		this.residentNode = residentNode;
		this.mebn = residentNode.getMFrag().getMultiEntityBayesianNetwork(); 
		this.arguments = arguments; 
		
		this.simpleSSBNNode =  SimpleSSBNNode.getInstance(residentNode); 
		for(OVInstance ovInstance: arguments){
			simpleSSBNNode.setEntityForOv(ovInstance.getOv(), ovInstance.getEntity()); 
		}
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

	@Deprecated
	public SSBNNode getQueryNode() {
		return queryNode;
	}
	
	public void setSSBNNode(SimpleSSBNNode _simpleSSBNNode){
		this.simpleSSBNNode = _simpleSSBNNode; 
	}
	
	public SimpleSSBNNode getSSBNNode(){
		return simpleSSBNNode; 
	}

	public ResidentNode getResidentNode() {
		return residentNode;
	}

	public List<OVInstance> getArguments() {
		return arguments;
	}
	
	public void addArgument(OVInstance ovInstance){
		arguments.add(ovInstance); 
	}

	public ProbabilisticNode getProbabilisticNode() {
		return probabilisticNode;
	}

	public void setProbabilisticNode(ProbabilisticNode probabilisticNode) {
		this.probabilisticNode = probabilisticNode;
	}
	
	@Override
	/**
	 * ex.: 
	 * RandomVariable(ov1=X1 ov2=X2)
	 */
	public String toString(){
		String string = "";
		
		string+= this.getResidentNode().getName(); 
		string+="(";
		for(OVInstance argument: this.getArguments()){
			string+= argument.getOv().getName() + "=" + argument.getEntity().getInstanceName() + " "; 
		}
		string+=")";
		
		return string;
	}
	
	

	
}
