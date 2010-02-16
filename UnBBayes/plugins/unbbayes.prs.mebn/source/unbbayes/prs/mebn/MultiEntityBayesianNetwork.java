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
package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.BooleanStatesEntityContainer;
import unbbayes.prs.mebn.entity.CategoricalStatesEntityContainer;
import unbbayes.prs.mebn.entity.ObjectEntityConteiner;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.IBridgeImplementor;

/**
 * This class represents a MultiEntityBayesianNetwork
 * and here it is going to have the same semantics as a
 * MTheory.
 */

public class MultiEntityBayesianNetwork extends Network {
 
	/* contains all Domain MFrags and Input MFrags: each MFrag in this list
	 * or is in the domainMFragList or in the findingMFragList */ 
	private List<MFrag> mFragList; 
	
	private List<MFrag> domainMFragList;
	
	//TODO analisar uma outra forma de armazenar os builtIn j� que eles s�o inerentes 
	//ao programa e n�o a cada MTheory. 
	private List<BuiltInRV> builtInRVList; 
	
	/* aponta para a MFrag atualmente sendo trabalhada/visualisada/criada */
	private MFrag currentMFrag;
	
	/* Entidades */
	TypeContainer typeContainer; 
	ObjectEntityConteiner objectEntityContainer; 
	
	BooleanStatesEntityContainer booleanStatesEntityContainer; 
	CategoricalStatesEntityContainer categoricalStatesEntityContainer; 
	
	private String description; 
	
	/* 
	 * Counters for indicate what is the automatic number for the next name
	 * generated.
	 *  
	 **/
	
	private int domainMFragNum = 1; 
	private int generativeInputNodeNum = 1; 
	private int domainResidentNodeNum = 1; 	
	private int contextNodeNum = 1; 
    private int entityNum = 1; 
	
    // Bridge pattern: separates this mebn representation and how mebn should be saved
    IBridgeImplementor storageImplementor = null;
    
    private boolean useStorageImplementor = true;
    
    //Controller of the names of the objects in this MTheory for avoid duplicated names. 
    private Set<String> namesUsed; 
    
    /**
	 * Contructs a new MEBN with empty mFrag's lists.
	 * @param name The name of the MEBN.
	 */
	public MultiEntityBayesianNetwork(String name) {
		super(name);
		mFragList = new ArrayList<MFrag>();
		domainMFragList = new ArrayList<MFrag>(); 
		builtInRVList = new ArrayList<BuiltInRV>(); 
		
		typeContainer = new TypeContainer(); 
		objectEntityContainer = new ObjectEntityConteiner(typeContainer); 
		booleanStatesEntityContainer = new BooleanStatesEntityContainer(); 
		categoricalStatesEntityContainer = new CategoricalStatesEntityContainer(); 
		namesUsed = new TreeSet<String>();
		try {
			this.setUseStorageImplementor(Boolean.valueOf(
					// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
					ApplicationPropertyHolder.getProperty().get(
							this.getClass().getCanonicalName()+".useStorageImplementor").toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*------------------------ Names of objects in this MTheory ------------*/
	public Set<String> getNamesUsed(){
		return namesUsed; 
	}
	
	public void initListNamesUsedWithReservatedNames(){
		
		if(namesUsed==null){
			namesUsed =new TreeSet<String>();
		}
		
		namesUsed.add("true"); 
		namesUsed.add("false"); 
		namesUsed.add("absurd"); 
		
		for(String typesNames : typeContainer.getTypesNames()){
			namesUsed.add(typesNames); 
		}
		
		//TODO complete this with the classes of the Pr-OWL...
		
	}
	
	/*--------------------------- MFrags ---------------------*/
	
	/**
	 * Method responsible for adding a new Domain MFrag .
	 * @param domainMFrag The new DomainMFrag to be added.
	 */
	public void addDomainMFrag(MFrag domainMFrag) {
		mFragList.add(domainMFrag);
		domainMFragList.add(domainMFrag); 
		currentMFrag = domainMFrag; 
		
		domainMFragNum++; 
	}
	
	/**
	 * Method responsible for removing the given Domain MFrag.
	 * Set the current MFrag to null. 
	 * @param mFrag The DomainMFrag to be removed.
	 */
	public void removeDomainMFrag(MFrag domainMFrag) {
		domainMFrag.delete();
		mFragList.remove(domainMFrag);
		domainMFragList.remove(domainMFrag); 
		currentMFrag = null; 
	}
	
	/**
	 * Return a MFrag with the name if it exists or null otherside. 
	 */
	public MFrag getMFragByName(String name){
		
		for(MFrag test: domainMFragList){
			if (test.getName().equals(name)){
				return test; 
			}
		}
		
		return null; 
	}
	
	/**
	 * Get the MFrag list of this MEBN.
	 * @return The MFrag list of this MEBN.
	 */
	public List<MFrag> getMFragList() {
		return mFragList;
	}
	
	/**
	 * Get the Domain MFrag list of this MEBN.
	 * @return The MFrag list of this MEBN.
	 */
	public List<MFrag> getDomainMFragList() {
		return domainMFragList;
	}
	
	/**
	 * Get total number of MFrags.
	 * @return The total number of MFrags.
	 */
	public int getMFragCount() {
		return mFragList.size();
	}
	
	/**
	 * Gets the current MFrag. In other words, 
	 * the MFrag being edited at the present moment.
	 * @return The current MFrag.
	 */
	public MFrag getCurrentMFrag() {
		return currentMFrag;
	}
	
	/**
	 * Sets the current MFrag. In other words, 
	 * the MFrag being edited at the present moment.
	 * @param currentMFrag The current MFrag.
	 */
	public void setCurrentMFrag(MFrag currentMFrag) {
		this.currentMFrag = currentMFrag; 
	}	
	
	
	
	/*--------------------------- BuiltInRV ---------------------*/
		
	/**
	 * Get the BuiltIn randon variables list of this MEBN.
	 * @return The built in list of this MEBN.
	 */
	public List<BuiltInRV> getBuiltInRVList() {
		return builtInRVList;
	}
	
	/**
	 * Add a built-in rv to mebn
	 */
	//TODO substituir por um m�todo que crie as built in rvs as quais h� suporte... 
	public void addBuiltInRVList(BuiltInRV builtInRV){
		builtInRVList.add(builtInRV); 
	}
	

	/*--------------------------- Nodes ---------------------*/
		
	/**
	 * Returns the NodeList of the current MFrag
	 */	
	public ArrayList<Node> getNodeList(){
		if (currentMFrag != null){
		    return this.currentMFrag.getNodeList();
		}
		else{
			return null; 
		}
	}
	
	/**
	 * Return the number for put in the name of the next MFrag build.
	 */
	
	public int getDomainMFragNum(){
		return domainMFragNum; 
	}

	public int getContextNodeNum() {
		return contextNodeNum;
	}

	public void setContextNodeNum(int contextNodeNum) {
		this.contextNodeNum = contextNodeNum;
	}
	
	public void plusContextNodeNul(){
		contextNodeNum++; 
	}

	public int getDomainResidentNodeNum() {
		return domainResidentNodeNum;
	}
	

	public void setDomainResidentNodeNum(int domainResidentNodeNum) {
		this.domainResidentNodeNum = domainResidentNodeNum;
	}
	
	public void plusDomainResidentNodeNum() {
		domainResidentNodeNum++;
	}
	

	public int getGenerativeInputNodeNum() {
		return generativeInputNodeNum;
	}

	public void setGenerativeInputNodeNum(int generativeInputNodeNum) {
		this.generativeInputNodeNum = generativeInputNodeNum;
	}	
	
	public void plusGenerativeInputNodeNum() {
		generativeInputNodeNum++;
	}		
	
	public int getEntityNum() {
		return entityNum;
	}

	public void setEntityNum(int entityNum) {
		this.entityNum = entityNum;
	}
	
	public void plusEntityNul(){
		entityNum++; 
	}

	public void setDomainMFragNum(int domainMFragNum) {
		this.domainMFragNum = domainMFragNum;
	}
	 
	public String toString(){
		return name;
	}

	public BooleanStatesEntityContainer getBooleanStatesEntityContainer() {
		return booleanStatesEntityContainer;
	}

	public CategoricalStatesEntityContainer getCategoricalStatesEntityContainer() {
		return categoricalStatesEntityContainer;
	}

	public ObjectEntityConteiner getObjectEntityContainer() {
		return objectEntityContainer;
	}

	public TypeContainer getTypeContainer() {
		return typeContainer;
	}
	
	
	
	
	/*--------------------- Searchs using the unique names -------------------*/
	
	
	/**
	 * Searches for a domain mfrag containing a resident node with a name passed by its argument.
	 * @param nodeName: a name for a resident node to look for
	 * @return if found, a DomainMFrag. If not found, null.
	 */
	public MFrag getDomainMFragByNodeName(String nodeName) {
		for (MFrag mfrag : this.domainMFragList) {
			if (mfrag.getDomainResidentNodeByName(nodeName) != null) {
				return mfrag;
			}
		}
		return null;
	}
	
	/**
	 * searches for a MFrag containing a node
	 * @param node: the node to search for
	 * @return a MFrag containing the required node
	 */
	public MFrag getMFragByNode(MultiEntityNode node) {
		for (MFrag element : this.mFragList) {
			if (element.containsNode(node)) {
				return element;
			}
		}
		return null;
	}
	
	/**
	 * Search the context node with the name. 
	 * @param name Name of context node
	 * @return the context node if it exists and null otherside. 
	 */
	public ContextNode getContextNode(String name){
		for(MFrag mfrag: domainMFragList){
			for(ContextNode node: mfrag.getContextNodeList()){
				if(node.getName().equals(name)){
					return node; 
				}
			}
		}
		return null; 
	}

	/**
	 * Search the DomainResidentNode node with the name. 
	 * @param name Name of DomainResidentNode 
	 * @return the DomainResidentNode  if it exists and null otherside. 
	 */
	public ResidentNode getDomainResidentNode(String name){
		for(MFrag mfrag: domainMFragList){
			for(ResidentNode node: mfrag.getResidentNodeList()){
				if(node.getName().equalsIgnoreCase(name)){
					return node; 
				}
			}
		}
		return null; 
	}
	
	/**
	 * Obtains every resident nodes declared in a MTheory
	 * @return list containing resident nodes.
	 */
	public List<ResidentNode> getDomainResidentNodes() {
		List<ResidentNode> nodes = new ArrayList<ResidentNode>();
		for (MFrag mfrag : this.domainMFragList) {
			 nodes.addAll(mfrag.getResidentNodeList());
		}
		return nodes;
	}
	
	/**
	 * Search the GenerativeInputNode  with the name. 
	 * @param name Name of GenerativeInputNode
	 * @return the GenerativeInputNode if it exists and null otherwise. 
	 */
	public InputNode getInputNode(String name){
		for(MFrag mfrag: domainMFragList){
			for(InputNode node: mfrag.getInputNodeList()){
				if(node.getName().equals(name)){
					return node; 
				}
			}
		}
		return null; 
	}	
	
	
	/*	This method should never be used by this project
	public MFrag getMFragByNodeName(String nodeName) {
		for (MFrag element : this.mFragList) {
			if (element.containsNode(nodeName) != null) {
				return element;
			}
		}
		return null;
	}
	*/
	
	/* This method should never be used by this project
	public DomainMFrag getDomainMFragByNode(MultiEntityNode node) {
		MFrag ret = this.getMFragByNode(node);
		if (ret instanceof DomainMFrag) {
			return (DomainMFrag) ret;
		}
		return null;
	}
	*/
	
	/* This method should never be used by this project
	public MFrag getDomainMFragByNodeName(String nodeName) {
		MFrag ret = this.getMFragByNodeName(nodeName);
		if (ret instanceof DomainMFrag) {
			return (DomainMFrag) ret;
		}
		return null;
	}
	*/
	
	
	/**
	 * If any MFrag has been set to use default CPT, this method would be
	 * usefull to clear it all (set isUsingDefaultCPT flag to false)
	 */
	public void clearMFragsIsUsingDefaultCPTFlag() {
		for (MFrag mfrag : this.getDomainMFragList()) {
			mfrag.setAsUsingDefaultCPT(false);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Obtains a class which should be responsible for saving the MTheory by calling execute().
	 * It might be useful if you are implementing a "save current" feature.
	 * @return the storageImplementor. If {@link #isUseStorageImplementor()} == false, return null.
	 * @see #isUseStorageImplementor()
	 */
	public IBridgeImplementor getStorageImplementor() {
		if (!this.isUseStorageImplementor()) {
			return null;
		}
		return storageImplementor;
	}

	/**
	 * Sets a class which should be responsible for saving the MTheory by calling execute().
	 * It might be useful if you are implementing a "save current" feature.
	 * If {@link #isUseStorageImplementor()} != true, this method does nothing.
	 * @param storageImplementor the storageImplementor to set
	 * @see #isUseStorageImplementor()
	 */
	public void setStorageImplementor(IBridgeImplementor storageImplementor) {
		if (!this.isUseStorageImplementor()) {
			return;
		}
		this.storageImplementor = storageImplementor;
	}

	/**
	 * @return the useStorageImplementor
	 */
	public boolean isUseStorageImplementor() {
		return useStorageImplementor;
	}

	/**
	 * @param useStorageImplementor the useStorageImplementor to set
	 */
	public void setUseStorageImplementor(boolean useStorageImplementor) {
		this.useStorageImplementor = useStorageImplementor;
	}
	
	
	

}