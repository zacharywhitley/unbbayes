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
 
	private List<MFrag> mFragList;
	
	private MFrag currentMFrag;
	
	/**
	 * Contructs a new MEBN with empty mFrag's list.
	 * @param name The name of the MEBN.
	 */
	public MultiEntityBayesianNetwork(String name) {
		super(name);
		mFragList = new ArrayList<MFrag>();
	}
	
	/**
	 * Method responsible for adding a new MFrag.
	 * @param mFrag The new MFrag to be added.
	 */
	public void addMFrag(MFrag mFrag) {
		mFragList.add(mFrag);
	}
	
	/**
	 * Method responsible for removing the given MFrag.
	 * @param mFrag The MFrag to be removed.
	 */
	public void removeMFrag(MFrag mFrag) {
		mFrag.delete();
		mFragList.remove(mFrag);
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
	 
}
 
