package unbbayes.datamining.datamanipulation.mtree;

import java.util.Hashtable;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
public class PageFileMemory extends PageFile {
 
	private Hashtable file = new Hashtable();
	 
	public void PageFileMemory(int nodeMinSize, int nodeMaxSize) {
	}
	 
	Node readNode(int pageNumber) {
		return null;
	}
	 
	void writeNode(Node node) {
	}
	 
	void deleteNode(int pageNumber) {
	}
	 
	void close() {
	}
	 
}
 
