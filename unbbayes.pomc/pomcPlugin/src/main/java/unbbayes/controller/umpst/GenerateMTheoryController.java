package unbbayes.controller.umpst;

import unbbayes.model.umpst.implementation.algorithm.FirstCriterionOfSelection;
import unbbayes.model.umpst.implementation.algorithm.MFragModel;
import unbbayes.model.umpst.implementation.algorithm.MTheoryModel;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;
import unbbayes.model.umpst.implementation.node.NodeResidentModel;
import unbbayes.model.umpst.project.UMPSTProject;

/**
 * @author Diego Marques
 *
 */
public class GenerateMTheoryController {
	
	private UMPSTProject umsptProject;
	private MTheoryModel mtheory;
	
	// Run Mode
	private FirstCriterionOfSelection firstCriterion;
	
	public GenerateMTheoryController (UMPSTProject umpstProject) {
		
		this.umsptProject = umpstProject;
		
		mtheory = new MTheoryModel(umpstProject.getModelName(), umpstProject.getAuthorModel());
		firstCriterion = new FirstCriterionOfSelection(umpstProject, this);
		
	}
	
	/**
	 * MTHEORY DEBBUG METHOD
	 */
	public void testMTheory() {
		for (int i = 0; i < mtheory.getMFragList().size(); i++) {
			
			MFragModel mfragTest = mtheory.getMFragList().get(i);
			System.out.println("NAME: " + mfragTest.getName());			
			
			System.out.println("---- RESIDENT NODE ------");
			for (int j = 0; j < mfragTest.getNodeResidentList().size(); j++) {
				System.out.println(mfragTest.getNodeResidentList().get(j).getName());
			}
			System.out.println("---- NOT DEFINED NODE ------");
			for (int j = 0; j < mfragTest.getNodeNotDefinedList().size(); j++) {
				System.out.println(mfragTest.getNodeNotDefinedList().get(j).getName());
			}
		}
	}
	
	/**
	 * Add node resident in a specific MFrag.
	 * @param idMFrag
	 * @param node
	 */
	public void addNodeResidentInMFrag(String idMFrag, NodeResidentModel node) {
		mtheory.addResidentNodeInMFrag(idMFrag, node);
	}
	
	/**
	 * Add node not defined in a specific MFrag.
	 * @param idMfrag
	 * @param node
	 */
	public void addNotDefinedNodeInMFrag(String idMFrag, NodeObjectModel node) {
		mtheory.addNotDefinedNodeInMFrag(idMFrag, node);
	}
	
	/**
	 * Add MFrag.
	 * @param mfrag
	 */
	public void addMFrag(MFragModel mfrag) {
		mtheory.addMFrag(mfrag);
	}

	/**
	 * @return the mtheory
	 */
	public MTheoryModel getMTheory() {
		return mtheory;
	}

	/**
	 * @param mtheory the mtheory to set
	 */
	public void setMTheory(MTheoryModel mtheory) {
		this.mtheory = mtheory;
	}	

}
