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
 */
public class MonteCarloIO {

	private byte[][] matrix;

	private File file;

	private PrintStream ps;

	public MonteCarloIO(byte[][] matrix) throws IOException {
		file = getFile();
		ps = new PrintStream(new FileOutputStream(file));
		this.matrix = matrix;
	}

	/**
	 * Creates a txt file with the sampled states from the simulation.
	 * 
	 * @param sampledNodeOrder
	 *            The order the nodes were sampled.
	 * @param pn
	 *            The probabilistic network that was sampled.
	 */
	public void makeFile(List<Node> sampledNodeOrder) {
		makeFirstLine(sampledNodeOrder);
		Node node;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < sampledNodeOrder.size(); j++) {
				node = sampledNodeOrder.get(j);
				ps.print(node.getStateAt(matrix[i][j]));
				if (j != sampledNodeOrder.size() - 1) {
					ps.print('\t');
				} else {
					ps.println();
				}
			}
		}
	}

	private void makeFirstLine(List<Node> sampledNodeOrder) {
		Node node;
		for (int i = 0; i < sampledNodeOrder.size(); i++) {
			node = sampledNodeOrder.get(i);
			ps.print(node.getName());
			if (i != sampledNodeOrder.size() - 1) {
				ps.print('\t');
			} else {
				ps.println();
			}
		}
	}

	private File getFile() {
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

}