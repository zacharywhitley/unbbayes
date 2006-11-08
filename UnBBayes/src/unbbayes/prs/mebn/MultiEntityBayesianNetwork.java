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
 
	private List<MFrag> mFragList; /* contains all Domain MFrags and Input MFrags */ 
	
	private List<DomainMFrag> domainMFragList;
	
	private List<FindingMFrag> findingMFragList; 
	
	private MFrag currentMFrag;
	
	/**
	 * Contructs a new MEBN with empty mFrag's lists.
	 * @param name The name of the MEBN.
	 */
	public MultiEntityBayesianNetwork(String name) {
		super(name);
		mFragList = new ArrayList<MFrag>();
		domainMFragList = new ArrayList<DomainMFrag>(); 
		findingMFragList = new ArrayList<FindingMFrag>(); 
	}
	
	/**
	 * Method responsible for adding a new Domain MFrag.
	 * @param domainMFrag The new DomainMFrag to be added.
	 */
	public void addDomainMFrag(DomainMFrag domainMFrag) {
		mFragList.add(domainMFrag);
		domainMFragList.add(domainMFrag); 
		currentMFrag = domainMFrag; 
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
	
	 
}