/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.io;

import unbbayes.prs.bn.Network;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;

import java.io.*;

/**
 * Interface de io de uma rede.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @version 1.0
 */
public interface BaseIO {
	/**
	 * Loads a new Probabilistic network from the input file.
	 * 
	 * @param input the input file for the network
	 * @return The loaded network
	 * @throws LoadException If the file doesn't describes a network.
	 * @throws IOException	If an IO error occurs
	 */
    public ProbabilisticNetwork load(File input) throws LoadException, IOException;
    
    /**
     * Loads a new MSBN from the input DIRECTORY
     * @param input	Input directory for the MSBN
     * @return The loaded MSBN
     * @throws LoadException If the directory doesn't describes a MSBN.
     * @throws IOException	 If an IO error occurs
     */
    public SingleAgentMSBN loadMSBN(File input) throws LoadException, IOException;
    
    /**
     * Saves a network to the output file.
     * @param output	The output file to save
     * @param net		The network to save.
     */
    public void save(File output, Network net) throws FileNotFoundException;
    
    /**
     * Saves a MSBN to the output directory.
     * @param output The output file to save
     * @param net		The MSBN to save.
     */
    public void saveMSBN(File output, SingleAgentMSBN net) throws FileNotFoundException;
}