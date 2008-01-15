package unbbayes.datamining.datamanipulation.mtree;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
public class Node {
 
	private int parentNode;
	 
	private int currentSize;
	 
	private PageFile file;
	 
	private int pageNumber;
	 
	private Entry[] entries;
	 
	public void Node(int pageNumber, PageFile file) {
	}
	 
	int getParentNode() {
		return 0;
	}
	 
	void setParentNode(int parentNode) {
	}
	 
	short getCurrentNodeSize() {
		return 0;
	}
	 
	void setPromotePartitionFunction() {
	}
	 
	void addEntry(Entry entry) {
	}
	 
	public void deleteEntry(Entry entry) {
	}
	 
	void split() {
	}
	 
	private void promote() {
	}
	 
	private void partition() {
	}
	 
}
 
