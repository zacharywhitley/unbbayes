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
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Implements the interface MebnIO for the Pr-OWL format.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 2006/10/25
 */

public class PrOwlIO implements MebnIO {
	
	public static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 
	
	/**
	 * Make de loader from a file pr owl for the mebn structure. 
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException{
 		LoaderPrOwlIO loader = new LoaderPrOwlIO(); 
 		return loader.loadMebn(file); 
	}
	
	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */
	
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException{
	   SaverPrOwlIO saver = new SaverPrOwlIO();
	   saver.saveMebn(file, mebn); 
	}
	
}