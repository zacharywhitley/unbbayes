package unbbayes.datamining.datamanipulation.mtree;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
public abstract class PageFile {
 
	private int nodeMinSize;
	 
	private int nodeMaxSize;
	 
	void PageFile(int nodeMinSize, int nodeMaxSize) {
	}
	 
	int getNodeMinSize() {
		return 0;
	}
	 
	int getNodeMaxSize() {
		return 0;
	}
	 
	abstract Node readNode(int pageNumber);
	abstract void writeNode(Node node);
	abstract void deleteNode(int pageNumber);
	abstract void close();
}
 
