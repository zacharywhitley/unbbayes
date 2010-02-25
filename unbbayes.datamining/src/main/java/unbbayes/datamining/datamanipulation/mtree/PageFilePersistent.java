/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
 
