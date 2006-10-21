package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Network;


/**
 * This class represents a MultiEntityBayesianNetwork
 * and here it is going to have the same semantics as a
 * MTheory.
 */
public class MultiEntityBayesianNetwork extends Network {
 
	private List<DomainMFrag> domainMFragList;
	
	private DomainMFrag currentDomainMFrag;
	
	/**
	 * Contructs a new MEBN with empty domainMFrag's list.
	 * @param name The name of the MEBN.
	 */
	public MultiEntityBayesianNetwork(String name) {
		super(name);
		domainMFragList = new ArrayList<DomainMFrag>();
	}
	
	/**
	 * Method responsible for adding a new DomainMFrag.
	 * @param domainMFrag The new DomainMFrag to be added.
	 */
	public void addDomainMFrag(DomainMFrag domainMFrag) {
		domainMFragList.add(domainMFrag);
	}
	
	/**
	 * Method responsible for removing the given DomainMFrag.
	 * @param domainMFrag The DomainMFrag to be removed.
	 */
	public void removeDomainMFrag(DomainMFrag domainMFrag) {
		domainMFrag.delete();
		domainMFragList.remove(domainMFrag);
	}
	
	/**
	 * Get the DomainMFrag list of this MEBN.
	 * @return The DomainMFrag list of this MEBN.
	 */
	public List<DomainMFrag> getDomainMFragList() {
		return domainMFragList;
	}
	
	/**
	 * Get total number of DomainMFrags.
	 * @return The total number of DomainMFrags.
	 */
	public int getDomainMFragCount() {
		return domainMFragList.size();
	}
	
	/**
	 * Gets the current DomainMFrag. In other words, 
	 * the DomainMFrag being edited at the present moment.
	 * @return The current DomainMFrag.
	 */
	public DomainMFrag getCurrentDomainMFrag() {
		return currentDomainMFrag;
	}
	
	/**
	 * Sets the current DomainMFrag. In other words, 
	 * the DomainMFrag being edited at the present moment.
	 * @param currentDomainMFrag The current DomainMFrag.
	 */
	public void setCurrentDomainMFrag(DomainMFrag currentDomainMFrag) {
		this.currentDomainMFrag = currentDomainMFrag; 
	}
	 
}
 
