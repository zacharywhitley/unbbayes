/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.prs.mebn;

import unbbayes.prs.Node;

/**
 * Node used in order to draw an MFrag in MTheory view.
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031)
 */
public class MFragNode extends Node {
	
	private static final long serialVersionUID = 4602471673250304773L;
	
	private MFrag mfrag;
	
	public MFragNode(MFrag mfrag) {
		this.mfrag = mfrag;
		this.setLabel(mfrag.getName());
	}

	// There is no specific type for this node 
	// Using 0, but it does not really matter
	public int getType() {
		return 0;
	}
	
	public MFrag getMfrag() {
		return mfrag;
	}

	public void setMfrag(MFrag mfrag) {
		this.mfrag = mfrag;
	}
	
	public String getName() {
		return mfrag.getName();
	}

}
