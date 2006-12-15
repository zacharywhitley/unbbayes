package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Network;
import unbbayes.util.NodeList;

/**
 * This class represents a MultiEntityBayesianNetwork
 * and here it is going to have the same semantics as a
 * MTheory.
 */

public class MultiEntityBayesianNetwork extends Network {
 
	/* contains all Domain MFrags and Input MFrags: each MFrag in this list
	 * or is in the domainMFragList or in the findingMFragList */ 
	private List<MFrag> mFragList; 
	
	private List<DomainMFrag> domainMFragList;
	
	private List<FindingMFrag> findingMFragList; 
	
	//TODO analisar uma outra forma de armazenar os builtIn já que eles são inerentes 
	//ao programa e não a cada MTheory. 
	private List<BuiltInRV> builtInRVList; 
	
	/* aponta para a MFrag atualmente sendo trabalhada/visualisada/criada */
	private MFrag currentMFrag;
	
	
	/* Este contador serve apenas para indicar qual deve ser o número
	 * da próxima MFrag criada (ao se gerar o nome automatico. Este número
	 * não está em sincronia com o número atual de MFrags porque ele 
	 * inclui também MFrags criadas e escluidas posteriormente */
	
	private int domainMFragNum = 1; 
	private int generativeInputNodeNum = 1; 
	private int domainResidentNodeNum = 1; 	
	private int contextNodeNum = 1; 
    
	
	
	
	/**
	 * Contructs a new MEBN with empty mFrag's lists.
	 * @param name The name of the MEBN.
	 */
	public MultiEntityBayesianNetwork(String name) {
		super(name);
		mFragList = new ArrayList<MFrag>();
		domainMFragList = new ArrayList<DomainMFrag>(); 
		findingMFragList = new ArrayList<FindingMFrag>();
		builtInRVList = new ArrayList<BuiltInRV>(); 
	}
	
	/*--------------------------- MFrags ---------------------*/
	
	/**
	 * Method responsible for adding a new Domain MFrag .
	 * @param domainMFrag The new DomainMFrag to be added.
	 */
	public void addDomainMFrag(DomainMFrag domainMFrag) {
		mFragList.add(domainMFrag);
		domainMFragList.add(domainMFrag); 
		currentMFrag = domainMFrag; 
		
		domainMFragNum++; 
	}
	
	/**
	 * Method responsible for removing the given Domain MFrag.
	 * @param mFrag The DomainMFrag to be removed.
	 */
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		domainMFrag.delete();
		mFragList.remove(domainMFrag);
		domainMFragList.remove(domainMFrag); 
		currentMFrag = null; 
	}
	
	/**
	 * Method responsible for adding a new Finding MFrag.
	 * @param findingMFrag The new FindingMFrag to be added.
	 */
	public void addFindingMFrag(FindingMFrag findingMFrag) {
		mFragList.add(findingMFrag);
		findingMFragList.add(findingMFrag); 
		currentMFrag = findingMFrag; 
	}
	
	/**
	 * Method responsible for removing the given Finding MFrag.
	 * @param mFrag The FindingMFrag to be removed.
	 */
	public void removeFindingMFrag(FindingMFrag findingMFrag) {
		findingMFrag.delete();
		mFragList.remove(findingMFrag);
		domainMFragList.remove(findingMFrag); 
		currentMFrag = null; 
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
	public List<DomainMFrag> getDomainMFragList() {
		return domainMFragList;
	}
	
	/**
	 * Get the Finding MFrag list of this MEBN.
	 * @return The MFrag list of this MEBN.
	 */
	public List<FindingMFrag> getFindingMFragList() {
		return findingMFragList;
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
	//TODO substituir por um método que crie as built in rvs as quais há suporte... 
	public void addBuiltInRVList(BuiltInRV builtInRV){
		builtInRVList.add(builtInRV); 
	}
	

	/*--------------------------- Nodes ---------------------*/
		
	/**
	 * Returns the NodeList of the current MFrag
	 */	
	public NodeList getNodeList(){
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
	 
}