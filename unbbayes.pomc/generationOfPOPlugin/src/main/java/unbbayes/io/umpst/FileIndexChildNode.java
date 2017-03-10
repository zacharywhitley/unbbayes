package unbbayes.io.umpst;

import java.util.ArrayList;

/**
 * Array of index to map all child nodes that are repeated like subgoals.  
 * 
 * @author Diego Marques
 *
 */

public class FileIndexChildNode {
	private String index;
	private ArrayList<String> listOfNodes;
	
	public FileIndexChildNode(
			String index,
			ArrayList<String> listOfNodes) {
		
		this.setIndex(index);
		this.setListOfNodes(listOfNodes);
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getIndex() {
		return index;
	}

	public void setListOfNodes(ArrayList<String> listOfNodes) {
		this.listOfNodes = listOfNodes;
	}

	public ArrayList<String> getListOfNodes() {
		return listOfNodes;
	}
	
}