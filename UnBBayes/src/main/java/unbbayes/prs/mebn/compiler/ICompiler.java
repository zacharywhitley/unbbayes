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
package unbbayes.prs.mebn.compiler;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.mebn.compiler.exception.UndeclaredTableException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode;

public interface ICompiler {

	/**
	 * Initializes compiler. Sets the text to parse by
	 * parse() method
	 * @param text: text to parse
	 * @throws UndeclaredTableException when parameter is empty
	 * @see unbbayes.gui.UnBBayesFrame.prs.mebn.compiler.ICompiler#parse()
	 */
	public abstract void init(String text) ;
	
	/**
	 * Parse the string passed by init method
	 * @see unbbayes.gui.UnBBayesFrame.prs.mebn.compiler.ICompiler#init(String)
	 */
	public abstract void parse() throws MEBNException;
	
	/**
	 * Use this method to determine where the error has occurred
	 * @return Returns the last read index.
	 */
	public abstract int getIndex();
	
	
	/**
	 * generates CPT using a pseudocode.
	 * @param ssbnnode: a SSBN-generation time node containing informations about a
	 * resident node having a pseudocode to parse, every parent-child structure previously
	 * built and a reference to a ProbabilisticNode which the generated CPT should be 
	 * whitten.
	 * @return a reference to the generated PotentialTable (which also can be accessed from
	 * the ProbabilisticNode contained inside the ssbnnode).
	 */
	public PotentialTable generateCPT(SSBNNode ssbnnode) throws MEBNException;
}