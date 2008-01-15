package unbbayes.datamining.datamanipulation.mtree;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
import java.io.RandomAccessFile;
import java.util.Stack;

public class PageFilePersistent extends PageFile {
 
	/**
	 *This value will be written in the page file as mark. Only files with
	 *this mark can be considered a M-Tree index file.
	 */
	private static final String PAGEFILE_MARK = "GiST data file";
	 
	/**
	 *Used to mark a blank page in the page file
	 */
	private static final int EMPTY_PAGE = -22;
	 
	/**
	 *References the file that stores the M-Tree
	 */
	private RandomAccessFile file;
	 
	/**
	 *Name of the file which stores the M-Tree
	 */
	private String fileName;
	 
	/**
	 *Stores the total size (in bytes) of one page
	 */
	private int pageSize;
	 
	/**
	 *Keeps track of the blank pages in the page file
	 */
	private Stack emptyPages;
	 
	/**
	 *Variable used to parse the bytes from a page of the page file
	 */
	private byte[] buffer;
	 
	/**
	 *True if the page file has been closed
	 */
	private boolean closed;
	 
	public void PageFilePersistent(int nodeMinSize, int nodeMaxSize, String fileName) {
	}
	 
	void PageFilePersistent(String fileName) {
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
	 
	protected void finalize() {
	}
	 
}
 
