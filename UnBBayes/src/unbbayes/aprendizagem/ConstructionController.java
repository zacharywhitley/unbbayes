/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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
package unbbayes.aprendizagem;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Date;

import javax.swing.JOptionPane;

import unbbayes.controller.MainController;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/*
 * UnbBayes Copyright (C) 2002 Universidade de Bras�lia
 * 
 * This file is part of UnbBayes.
 * 
 * UnbBayes is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * UnbBayes is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * UnbBayes; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * @author Danilo
 * 
 * This class reads the file and constructs the matrix of indexes, the array of
 * repeated instances and initializes the learning process
 */

public class ConstructionController {

	private NodeList variablesVector;
	private NodeList variables;
	private int[] vector;
	private byte[][] matrix;
	private long caseNumber;
	private boolean compacted;

	/**
	 * Starts the process of read the file, construct and fill the structres
	 * 
	 * @param file -
	 *            The file that contains the data base of cases.
	 * 
	 * @param controller -
	 *            The controller that will be called to continue the process of
	 *            propagate evidences
	 * 
	 * @see MainController
	 * 
	 * @see ChooseVariablesWindow
	 * 
	 * @see CompactFileWindow
	 * 
	 * @see OrdenationWindow
	 * 
	 * @see OrdenationInterarionController
	 * 
	 * @see AlgorithmController
	 * 
	 * @see ProbabilisticController
	 */

	public ConstructionController(File file, ProbabilisticNetwork pn) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(isr);
			int rows = getRowCount(br);
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);
			StreamTokenizer cols = new StreamTokenizer(br);
			setColsConstraints(cols);
			variablesVector = new NodeList();
			variables = new NodeList();
			makeVariablesVector(cols);
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			verificarConsistencia(pn);
			matrix = new byte[rows][variables.size()];
			makeMatrix(cols, rows);
			br.close();
		} catch (Exception e) {
			String msg = "N�o foi poss�vel abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public ConstructionController(File file) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(isr);
			int rows = getRowCount(br);
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);
			StreamTokenizer cols = new StreamTokenizer(br);
			setColsConstraints(cols);
			variablesVector = new NodeList();
			variables = new NodeList();
			makeVariablesVector(cols);
			filterVariablesVector(rows);
			matrix = new byte[rows][variables.size()];

			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.WAIT_CURSOR));
			// ordenatevector();
			makeMatrix(cols, rows);
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.DEFAULT_CURSOR));
			br.close();
		} catch (Exception e) {
			String msg = "N�o foi poss�vel abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
	}

	private void verificarConsistencia(ProbabilisticNetwork pn) {
		for (int i = 0; i < variables.size(); i++) {
			TVariavel variavel = (TVariavel) variables.get(i);
			Node no = pn.getNode(variavel.getName());
			for (int j = 0; j < no.getStatesSize(); j++) {
				String estado = no.getStateAt(j);
				variavel.adicionaEstado(estado);
			}
		}
	}

	public ConstructionController(File file, MainController controller) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(isr);
			int rows = getRowCount(br);
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);
			StreamTokenizer cols = new StreamTokenizer(br);
			setColsConstraints(cols);
			variablesVector = new NodeList();
			variables = new NodeList();
			makeVariablesVector(cols);
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			matrix = new byte[rows][variables.size()];
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.WAIT_CURSOR));
			makeMatrix(cols, rows);
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.DEFAULT_CURSOR));
			br.close();
		} catch (Exception e) {
			String msg = "N�o foi poss�vel abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
		OrdenationWindow ordenationWindow = new OrdenationWindow(variables);
		OrdenationInterationController ordenationController = ordenationWindow
				.getController();
		String[] pamp = ordenationController.getPamp();
		variables = ordenationController.getVariables();
		/* Constructs the topology of the net */
		Date d = new Date();
		long time = d.getTime();
		UnBBayesFrame.getIUnBBayes().setCursor(new Cursor(Cursor.WAIT_CURSOR));
		AlgorithmController algorithmController = new AlgorithmController(
				variables, matrix, vector, caseNumber, pamp, compacted);
		UnBBayesFrame.getIUnBBayes().setCursor(
				new Cursor(Cursor.DEFAULT_CURSOR));
		Date d2 = new Date();
		long time1 = d2.getTime();
		long resul = time1 - time;
		/* Efeito de debug */
		System.out.println("Resultado = " + resul);
		/* Gives the probability of each node */
		ProbabilisticController probabilisticController = new ProbabilisticController(
				variables, matrix, vector, caseNumber, controller, compacted);
	}

	/**
	 * Sets the constraints of the StreamTokenizer. These constraint separates
	 * the tokens
	 * 
	 * @param cols -
	 *            A streamTokenizer object
	 */
	public void setColsConstraints(StreamTokenizer cols) {
		cols.wordChars('A', 'Z');
		cols.wordChars('a', '}');
		cols.wordChars('_', '_');
		cols.wordChars('-', '-');
		cols.wordChars('0', '9');
		cols.wordChars('.', '.');
		cols.quoteChar('\t');
		cols.commentChar('%');
		cols.eolIsSignificant(true);
	}

	/**
	 * Makes the variables vector. The vector is composed by many TVariavel
	 * objects.
	 * 
	 * @param cols -
	 *            A streamTokenizer object.
	 */
	private void makeVariablesVector(StreamTokenizer cols) {
		int position = 0;
		try {
			while (cols.nextToken() != StreamTokenizer.TT_EOL) {
				if (cols.sval != null) {
					variablesVector.add(new TVariavel(cols.sval, position));
					((TVariavel) variablesVector
							.get(variablesVector.size() - 1))
							.setDescription(cols.sval);
					((TVariavel) variablesVector
							.get(variablesVector.size() - 1))
							.setParticipa(true);
				} else {
					variablesVector.add(new TVariavel(
							String.valueOf(cols.nval), position));
					((TVariavel) variablesVector
							.get(variablesVector.size() - 1))
							.setDescription(String.valueOf(cols.nval));
					((TVariavel) variablesVector
							.get(variablesVector.size() - 1))
							.setParticipa(true);
				}
				position++;
			}
		} catch (Exception e) {
			String msg = "The tokenizer process could not be completed";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Gets the number of rows in the file. This information is relevant because
	 * of the size of the matrix of indexes and the vector of repeated
	 * instances.
	 * 
	 * @param br -
	 *            A bufferedReader object
	 * 
	 * @return int - The numbers of rows in the file
	 */
	private int getRowCount(BufferedReader br) {
		int rows = 0;
		try {
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
				rows++;
				;
			}
		} catch (Exception e) {
		}
		return rows;
	}

	/**
	 * Filtes the variables that will participate of the learning process. This
	 * variables are choose by the user of the program. Remember that the
	 * compacted variable will not participate of the leaning process.
	 * 
	 * @param rows -
	 *            The number of rows of the file.
	 */
	private void filterVariablesVector(int rows) {
		int nCols = 0;
		for (int i = 0; i < variablesVector.size(); i++) {
			TVariavel aux = (TVariavel) variablesVector.get(i);
			if (aux.getParticipa()) {
				if (!aux.getRep()) {
					aux.setPos(nCols);
					variables.add(aux);
					nCols++;
				} else {
					vector = new int[rows];
					compacted = true;
				}
			}
		}
	}

	/**
	 * Constructs the matrix of indexes. This matrix is composed by bytes
	 * primitive types occupy fewer memory.
	 * 
	 * @param cols -
	 *            A StreamTokenizer object
	 * 
	 * @param rows -
	 *            The number of rows in the file that constains the database.
	 */
	private void makeMatrix(StreamTokenizer cols, int rows) {
		boolean missing = false;
		int position = 0;
		String stateName = "";
		TVariavel aux;
		try {
			while (cols.ttype != StreamTokenizer.TT_EOF && caseNumber <= rows) {
				while (cols.ttype != StreamTokenizer.TT_EOL
						&& position < variablesVector.size()
						&& caseNumber <= rows) {
					aux = (TVariavel) variablesVector.get(position);
					if (aux.getRep()) {
						/*
						 * if(cols.nval > 0.0){ vector[(int)caseNumber] =
						 * (int)cols.nval; }else{
						 */
						vector[(int) caseNumber] = Integer.parseInt(cols.sval);
						// }
					} else if (aux.getParticipa()) {
						if (cols.sval != null) {
							stateName = cols.sval;
							if (!aux.existeEstado(stateName)) {
								if (!stateName.equals("?")) {
									aux.adicionaEstado(stateName);
								} else {
									missing = true;
								}
							}
						} else {
							stateName = String.valueOf(cols.nval);
							if (!aux.existeEstado(stateName)) {
								aux.adicionaEstado(stateName);
							}
						}
						if (!missing) {
							matrix[(int) caseNumber][aux.getPos()] = (byte) aux
									.getEstadoPosicao(stateName);

						} else {
							matrix[(int) caseNumber][aux.getPos()] = -1;
							missing = true;
						}
					}
					cols.nextToken();
					position++;
				}
				caseNumber++;
				while (cols.ttype != StreamTokenizer.TT_EOL
						&& caseNumber < rows) {
					cols.nextToken();
				}
				position = 0;
				cols.nextToken();
			}
		} catch (Exception e) {
			String msg = "There are errors on the matrix construction";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
		/* Tirar isso. S� pra debug */
		System.out.println("NumeroCasos " + caseNumber);
	}

	/**
	 * Normalizes the probabilities of a variable.
	 * 
	 * @param variable -
	 *            A TVariavel object.
	 */
	private void normalize(TVariavel variable) {
		for (int c = 0; c < variable.getPotentialTable().tableSize()/* .getDados().size() */; c += variable
				.getEstadoTamanho()/* .noEstados() */) {
			float sum = 0;
			for (int i = 0; i < variable.getEstadoTamanho(); i++) {
				sum += variable.getPotentialTable().getValue(c + i);
			}
			if (sum == 0) {
				for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
					variable.getPotentialTable().setValue(c + i,
							1 / variable.getEstadoTamanho());
				}
			} else {
				for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
					variable.getPotentialTable().setValue(c + i,
							variable.getPotentialTable().getValue(c + i) / sum);
				}
			}
		}
	}

	public byte[][] getMatrix() {
		return this.matrix;
	}

	public NodeList getVariables() {
		return this.variables;
	}

	public int[] getVector() {
		return this.vector;
	}

	public long getCaseNumber() {
		return caseNumber;
	}

	public boolean isCompacted() {
		return compacted;
	}

	public NodeList getVariablesVector() {
		return variablesVector;
	}
}
