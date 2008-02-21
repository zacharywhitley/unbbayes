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
 
