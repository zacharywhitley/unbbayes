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
package unbbayes.learning;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import unbbayes.controller.MainController;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;

/*
 * UnbBayes Copyright (C) 2002 Universidade de Brasília
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

	
	private ArrayList<Node> variablesVector;	// TODO use interface java.util.List instead
	private ArrayList<Node> variables;	// TODO use interface java.util.List instead
	private int[] vector;
	private int[][] matrix;
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
			variablesVector = new ArrayList<Node>(); 
			variables = new ArrayList<Node>();
			makeVariablesVector(cols);
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			verificarConsistencia(pn);
			matrix = new int[rows][variables.size()];
			makeMatrix(cols, rows);
			br.close();
		} catch (Exception e) {
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
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
			variablesVector = new ArrayList<Node>();
			variables = new ArrayList<Node>();
			makeVariablesVector(cols);
			filterVariablesVector(rows);
			matrix = new int[rows][variables.size()];

			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.WAIT_CURSOR));
			// ordenatevector();
			makeMatrix(cols, rows);
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.DEFAULT_CURSOR));
			br.close();
		} catch (Exception e) {
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
	}

	private void verificarConsistencia(ProbabilisticNetwork pn) {
		for (int i = 0; i < variables.size(); i++) {
			LearningNode variavel = (LearningNode) variables.get(i);
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
			variablesVector = new ArrayList<Node>();
			variables = new ArrayList<Node>();
			makeVariablesVector(cols);
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			matrix = new int[rows][variables.size()];
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.WAIT_CURSOR));
			makeMatrix(cols, rows);
			UnBBayesFrame.getIUnBBayes().setCursor(
					new Cursor(Cursor.DEFAULT_CURSOR));
			br.close();
		} catch (Exception e) {
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
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
	 * @version 1.0
	 * @author Gabriel Guimarães - aluno de IC 2005-2006
	 * @author Marcelo Ladeira - Orientador
	 * @author Patricia Marinho
	 * @throws InvalidParentException 
	 */
	public ConstructionController(File file, MainController controller,
			int classei, boolean cbg) throws InvalidParentException {
		int classex = 0;
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(isr);
			int rows = getRowCount(br);
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);
			StreamTokenizer cols = new StreamTokenizer(br);
			setColsConstraints(cols);
			variablesVector = new ArrayList<Node>();
			variables = new ArrayList<Node>();
			makeVariablesVector(cols);
			ChooseVariablesWindow cvw = new ChooseVariablesWindow(variablesVector, 0);
			classex = cvw.classei;
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			matrix = new int[rows][variables.size()];
			makeMatrix(cols, rows);

			br.close();
		} catch (Exception e) {
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
		OrdenationWindow ordenationWindow = new OrdenationWindow(variables);
		OrdenationInterationController ordenationController = ordenationWindow
				.getController();
		String[] pamp = ordenationController.getPamp();
		variables = ordenationController.getVariables();
		new AlgorithmController(variables, matrix, vector, caseNumber, pamp, compacted, classex);

		int i, j;
		j = variables.size();
		ArrayList<Node> variaveis = new ArrayList<Node>();
		variaveis.ensureCapacity(j + 1);

		for (i = 0; i < classex; i++)
			variaveis.add(variables.get(i));
		for (i = classex; i < j; i++)
			variaveis.add(variables.get(i));

		for (i = 0; i < j; i++) {
			// se alguma variavel não é filha da classe então passa a ser!
			if ((i != classex)
					&& (!(variaveis.get(classex).isParentOf(variaveis.get(i)))))
				//variaveis.AddChildTo(classex, variaveis.get(i));
				variaveis.get(classex).addChild(variaveis.get(i));
			// se alguma variavel tem como filho a classe--> retirar!
			if ((variaveis.get(i).isParentOf(variaveis.get(classex))))
				//variaveis.RemoveParentFrom(classex, i);
				variaveis.get(i).removeParent(variaveis.get(classex));
			// se alguma variavel nao tem a classe como pai entao passa a ter
			if ((!(variaveis.get(i).isChildOf(variaveis.get(classex)))))
				//variaveis.AddParentTo(i, variaveis.get(classex));
				variaveis.get(i).addParent(variaveis.get(classex));
		}
//		variaveis.ClearParentsFrom(classex);
		variaveis.get(classex).getParents().clear();

		new ProbabilisticController(variaveis, matrix, vector, caseNumber,
				controller, compacted);
	}

	/**
	 * For TAN
	 * 
	 * @param file
	 * @param controller
	 * @param classe
	 * @author Gabriel Guimarães
	 * @author Patricia
	 * @throws InvalidParentException 
	 */
	public ConstructionController(File file, MainController controller,
			int classei) throws InvalidParentException {
		int classex = 0;
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(isr);
			int rows = getRowCount(br);
			isr = new InputStreamReader(new FileInputStream(file));
			br = new BufferedReader(isr);
			StreamTokenizer cols = new StreamTokenizer(br);
			setColsConstraints(cols);
			variablesVector = new ArrayList<Node>();
			variables = new ArrayList<Node>();
			makeVariablesVector(cols);
			ChooseVariablesWindow cvw = new ChooseVariablesWindow(
					variablesVector, 0);
			classex = cvw.classei;
			new ChooseVariablesWindow(variablesVector);
			new CompactFileWindow(variablesVector);
			filterVariablesVector(rows);
			matrix = new int[rows][variables.size()];
			makeMatrix(cols, rows);

			br.close();
		} catch (Exception e) {
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null, msg, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
		;
		new B(variables, matrix, vector, caseNumber, "MDL", "", compacted);
		CL chowliu = new CL();
		chowliu.preparar(variables, classex, (int) caseNumber, vector,
				compacted, matrix);
		variables = new ArrayList(chowliu.variaveis); // TODO use interface java.util.List instead
		int i, j;
		j = variables.size();

		// Adicionar a variavel de classe como pai de todas

		for (i = 0; i < j; i++) {
			// se alguma variavel não é filha da classe então passa a ser!
			if ((i != classex)
					&& (!(variables.get(classex).isParentOf(variables.get(i)))))
				//variables.AddChildTo(classex, variables.get(i));
				variables.get(classex).addChild(variables.get(i));
			// se alguma variavel tem como filho a classe--> retirar!
			if ((variables.get(i).isParentOf(variables.get(classex))))
				//variables.RemoveParentFrom(classex, i);
				variables.get(i).removeParent(variables.get(classex));
			// se alguma variavel nao tem a classe como pai entao passa a ter
			if ((!(variables.get(i).isChildOf(variables.get(classex)))))
				//variables.AddParentTo(i, variables.get(classex));
				variables.get(i).addParent(variables.get(classex));
		}
		//variables.ClearParentsFrom(classex);
		variables.get(classex).getParents().clear();

		new ProbabilisticController(variables, matrix, vector, caseNumber,
				controller, compacted);
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
		cols.wordChars('?', '?');
		cols.quoteChar('\t');
		cols.commentChar('%');
		cols.eolIsSignificant(true);
	}

	/**
	 * Makes the variables vector. The vector is composed by many LearningNode
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
					variablesVector.add(new LearningNode(cols.sval, position));
					((LearningNode) variablesVector
							.get(variablesVector.size() - 1))
							.setDescription(cols.sval);
					((LearningNode) variablesVector
							.get(variablesVector.size() - 1))
							.setParticipa(true);
				} else {
					variablesVector.add(new LearningNode(
							String.valueOf(cols.nval), position));
					((LearningNode) variablesVector
							.get(variablesVector.size() - 1))
							.setDescription(String.valueOf(cols.nval));
					((LearningNode) variablesVector
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
			LearningNode aux = (LearningNode) variablesVector.get(i);
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
		LearningNode aux;
		try {
			if (cols.ttype == StreamTokenizer.TT_EOL) {
				cols.nextToken();
			}
			while (cols.ttype != StreamTokenizer.TT_EOF && caseNumber <= rows) {
				while (cols.ttype != StreamTokenizer.TT_EOL
						&& position < variablesVector.size()
						&& caseNumber <= rows) {
					aux = (LearningNode) variablesVector.get(position);
					if (aux.getRep()) {
						try {
							vector[(int)caseNumber] = (int)cols.nval; 
						} catch (Throwable e) {
							vector[(int) caseNumber] = Integer.parseInt(cols.sval);
						}
					} else if (aux.getParticipa()) {
						if (cols.sval != null) {
							stateName = cols.sval;
							if (!aux.existeEstado(stateName)) {
								if (!stateName.equals("?")) {
									aux.adicionaEstado(stateName);
									missing = false;
								} else {
									missing = true;
								}
							} else {
								missing = false;
							}
						} else {
							stateName = String.valueOf(cols.nval);
							if (!aux.existeEstado(stateName)) {
								aux.adicionaEstado(stateName);
								missing = false;
							}
						}
						if (!missing) {
							matrix[(int) caseNumber][aux.getPos()] = (int) aux
									.getEstadoPosicao(stateName);

						} else {
							matrix[(int) caseNumber][aux.getPos()] = -1;
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
		/* Tirar isso. So pra debug */
//		System.out.println("NumeroCasos " + caseNumber);
	}

	/**
	 * Normalizes the probabilities of a variable.
	 * 
	 * @param variable -
	 *            A LearningNode object.
	 */
	private void normalize(LearningNode variable) {
		for (int c = 0; c < variable.getProbabilityFunction().tableSize()/* .getDados().size() */; c += variable
				.getEstadoTamanho()/* .noEstados() */) {
			float sum = 0;
			for (int i = 0; i < variable.getEstadoTamanho(); i++) {
				sum += variable.getProbabilityFunction().getValue(c + i);
			}
			if (sum == 0) {
				for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
					variable.getProbabilityFunction().setValue(c + i,
							1 / variable.getEstadoTamanho());
				}
			} else {
				for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
					variable.getProbabilityFunction().setValue(c + i,
							variable.getProbabilityFunction().getValue(c + i) / sum);
				}
			}
		}
	}

	public int[][] getMatrix() {
		return this.matrix;
	}

	public ArrayList<Node> getVariables() {
		// TODO use interface java.util.List instead
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

	public ArrayList<Node> getVariablesVector() {
		// TODO use interface java.util.List instead
		return variablesVector;
	}
}
