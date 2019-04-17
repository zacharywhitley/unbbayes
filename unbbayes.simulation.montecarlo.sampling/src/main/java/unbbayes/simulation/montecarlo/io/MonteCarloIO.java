/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.simulation.montecarlo.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.controller.FileHistoryController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.prs.Node;

/**
 * @author Administrador
 * 
 *         To change this generated comment go to Window>Preferences>Java>Code
 *         Generation>Code and Comments
 * @author Shou Matsumoto
 * 			Minor changes to allow access as API from non-swing environment
 */
public class MonteCarloIO {

	private byte[][] matrix;

	private File file = null;
	
	private List<Node> sampledNodeOrder;

//	private PrintStream ps;

	/**
	 * @param matrix :  data
	 * @throws IOException
	 * @deprecated use {@link #MonteCarloIO(byte[][], List)}
	 */
	@Deprecated
	public MonteCarloIO(byte[][] matrix) throws IOException {
		this.matrix = matrix;
	}
	
	/**
	 * @param matrix : data
	 * @@param sampledNodeOrder
	 *            The order the nodes were sampled.
	 * @throws IOException
	 */
	public MonteCarloIO(byte[][] matrix, List<Node> sampledNodeOrder) throws IOException {
		this(matrix);
		this.sampledNodeOrder = sampledNodeOrder;
	}

	/**
	 * Creates a txt file with the sampled states from the simulation.
	 * 
	 * @param desiredNodeOrder : the order to print nodes (this will be the header).
	 * If {@link #getSampledNodeOrder()} is null, then it will be assumed
	 * that the samples were generated with this ordering of nodes.
	 *            
	 * @param pn
	 *            The probabilistic network that was sampled.
	 */
	public void makeFile(List<Node> desiredNodeOrder) {
		
		// get the order of nodes that were used for sampling
		List<Node> sampledNodeOrder = getSampledNodeOrder();
		if (sampledNodeOrder == null) {
			// if none, then consider this as the ordering of nodes used at sampling
			sampledNodeOrder = desiredNodeOrder;
		}
		
		PrintStream ps;
		try {
			ps = new PrintStream(new FileOutputStream(getFile()));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		makeFirstLine(ps, desiredNodeOrder);
		Node node;
		for (int row = 0; row < matrix.length; row++) {
			for (int desiredNodeIndex = 0; desiredNodeIndex < desiredNodeOrder.size(); desiredNodeIndex++) {
				node = desiredNodeOrder.get(desiredNodeIndex);
				int sampledNodeIndex = sampledNodeOrder.indexOf(node);
				if (sampledNodeIndex < 0) {
					throw new IllegalArgumentException("Node " + node + " not found in list of nodes originally used in sampling.");
				}
				ps.print(node.getStateAt(matrix[row][sampledNodeIndex]));
				if (desiredNodeIndex != desiredNodeOrder.size() - 1) {
					ps.print('\t');
				} else {
					ps.println();
				}
			}
		}
	}

	protected void makeFirstLine(PrintStream ps, List<Node> desiredNodeOrder) {
		Node node;
		for (int i = 0; i < desiredNodeOrder.size(); i++) {
			node = desiredNodeOrder.get(i);
			ps.print(node.getName());
			if (i != desiredNodeOrder.size() - 1) {
				ps.print('\t');
			} else {
				ps.println();
			}
		}
	}

	public File getFile() {
		if (file != null) {
			return file;
		}
		
		return requestFile();
	}

	/**
	 * request file from user
	 * @return : file
	 * @deprecated use {@link #setFile(File)} instead
	 */
	protected File requestFile() {
		// TODO remove GUI code from IO classes
		String[] nets = new String[] { "txt" };
		FileHistoryController fileHistoryController = FileHistoryController.getInstance();
		;
		JFileChooser chooser = new JFileChooser(fileHistoryController
				.getCurrentDirectory());
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.addChoosableFileFilter(new SimpleFileFilter(nets, "txt"));
		int option = chooser.showSaveDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the sampledNodeOrder
	 */
	public List<Node> getSampledNodeOrder() {
		return sampledNodeOrder;
	}

	/**
	 * @param sampledNodeOrder the sampledNodeOrder to set
	 */
	public void setSampledNodeOrder(List<Node> sampledNodeOrder) {
		this.sampledNodeOrder = sampledNodeOrder;
	}

}
