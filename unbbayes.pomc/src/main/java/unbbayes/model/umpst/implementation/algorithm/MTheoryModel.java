package unbbayes.model.umpst.implementation.algorithm;

import java.util.ArrayList;
import java.util.List;

import unbbayes.model.umpst.implementation.node.NodeContextModel;
import unbbayes.model.umpst.implementation.node.NodeInputModel;
import unbbayes.model.umpst.implementation.node.NodeObjectModel;
import unbbayes.model.umpst.implementation.node.NodeResidentModel;

public class MTheoryModel {
	
	private String id;
	private String name;
	private String author;
	
	private List<MFragModel> mfragList;

	public MTheoryModel(String name, String author) {
		
		this.name = name;
		this.author = author;
		
		mfragList = new ArrayList<MFragModel>();
	}
	
	public void updateResidentNodeInMFrag(String idMFrag, NodeResidentModel node) {
		if (getMFragList().size() > 0) {
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).updateResidentNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	public void updateInputNodeInMFrag(String idMFrag, NodeInputModel node) {
		if (getMFragList().size() > 0) {
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).updateInputNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	/**
	 * Add node input in a specific MFrag.
	 * @param idMFrag
	 * @param node
	 */
	public void addInputNodeInMFrag(String idMFrag, NodeInputModel node) {
		if (getMFragList().size() > 0) {			
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).addInputNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	/**
	 * Add node context in a specific MFrag.
	 * @param idMFrag
	 * @param node
	 */
	public void addContextNodeInMFrag(String idMFrag, NodeContextModel node) {
		if (getMFragList().size() > 0) {			
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).addContextNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	/**
	 * Add node resident in a specific MFrag.
	 * @param idMFrag
	 * @param node
	 */
	public void addResidentNodeInMFrag(String idMFrag, NodeResidentModel node) {
		if (getMFragList().size() > 0) {			
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).addResidentNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	/**
	 * Add node not defined in a specific MFrag.
	 * @param idMFrag
	 * @param node
	 */
	public void addNotDefinedNodeInMFrag(String idMFrag, NodeObjectModel node) {
		if (getMFragList().size() > 0) {			
			for (int i = 0; i < getMFragList().size(); i++) {
				if (getMFragList().get(i).getId().equals(idMFrag)) {
					getMFragList().get(i).addNotDefinedNode(node);
					break;
				}
			}
		} else {
			System.err.println("Error MTheory. Include MFrag.");
		}
	}
	
	/**
	 * Add MFrag.
	 * @param mfrag
	 */
	public void addMFrag(MFragModel mfrag) {
		getMFragList().add(mfrag);
	}
	
	/**
	 * Remove MFrag.
	 * @param mfrag
	 */
	public void removeMFrag(MFragModel mfrag) {
		getMFragList().remove(mfrag);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the mfragList
	 */
	public List<MFragModel> getMFragList() {
		return mfragList;
	}

	/**
	 * @param mfragList the mfragList to set
	 */
	public void setMFragList(List<MFragModel> mfragList) {
		this.mfragList = mfragList;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}	
}
