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
package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.SaveException;
import unbbayes.prs.Graph;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 *  This is the most basic I/O interface for UnBBayes, which basically loads or stores a Graph from/into a file.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @author Shou Matsumoto
 * @version 2.0
 */
public interface BaseIO {
	
	/**
	 * Loads a new network from the input file.
	 * 
	 * @param input the input file for the network
	 * @return The loaded network
	 * @throws LoadException If the file doesn't describe a network.
	 * @throws IOException	If an IO error occurs
	 */
    public Graph load(File input) throws LoadException, IOException;
    
    
    /**
     * Saves a network to the output file.
     * @param output	The output file to save
     * @param net		The network to save.
     */
    public void save(File output, Graph net) throws IOException;
    
    
    /**
     * Returns true if the file extension is supported by this IO class.
     * False otherwise.
     * @param extension
     * @return
     */
    public boolean supportsExtension(String extension);
   
}