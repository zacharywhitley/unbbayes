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
 
