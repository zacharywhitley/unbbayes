
package unbbayes.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import unbbayes.learning.ConstructionController;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * 
 * This reimplements {@link ConstructionController}, but imports to GUI classes
 * were removed
 * 
 * @author Shou Matsumoto Edited by Young
 * @author Bo
 */
public class TextModeConstructionController {

	private ArrayList<LearningNode> variablesVector; // TODO use interface java.util.List instead
	private ArrayList<LearningNode> variables; // TODO use interface java.util.List instead
	private int[] vector;
	private int[][] matrix;
	private long caseNumber;
	private boolean compacted = false;
	

	private String missingValueToken = ConstructionController.DEFAULT_MISSING_VALUE_TOKEN;

	public TextModeConstructionController(File file, ProbabilisticNetwork net) throws IOException {
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
		BufferedReader br = new BufferedReader(isr);
		int rows = getRowCount(br);
		isr = new InputStreamReader(new FileInputStream(file));
		br = new BufferedReader(isr);
		StreamTokenizer cols = new StreamTokenizer(br);
		setColsConstraints(cols);
		variablesVector = new ArrayList<LearningNode>();
		variables = new ArrayList<LearningNode>();
		makeVariablesVector(cols);
		filterVariablesVector(rows);
		matrix = new int[rows][variables.size()];

		// if there is an empty net, states are updated using the empty net.
		if (net != null) {
			for (Node n : variables) {
				if (n.getName().equalsIgnoreCase("Tuberculose")) {
					System.out.println();
				}
				Node original = net.getNode(n.getName());
				n.removeStates();
				for (int i = 0; i < original.getStatesSize(); i++) {
					String state = original.getStateAt(i);
					n.appendState(state);
				}
			}
		}

		// create a matrix data set
		makeMatrix(cols, rows);
		br.close();
	}

	// private void verificarConsistencia(ProbabilisticNetwork pn) {
	// for (int i = 0; i < variables.size(); i++) {
	// LearningNode variavel = (LearningNode) variables.get(i);
	// Node no = pn.getNode(variavel.getName());
	// for (int j = 0; j < no.getStatesSize(); j++) {
	// String estado = no.getStateAt(j);
	// variavel.adicionaEstado(estado);
	// }
	// }
	// }

	/**
	 * Sets the constraints of the StreamTokenizer. These constraint separates the
	 * tokens
	 * 
	 * @param cols
	 *            - A streamTokenizer object
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
	 * @param cols
	 *            - A streamTokenizer object.
	 * @throws IOException
	 */
	private void makeVariablesVector(StreamTokenizer cols) throws IOException {
		int position = 0;

		int token = cols.nextToken();
		while (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
			String val;
			if (cols.sval != null) {
				val = cols.sval;
			} else {
				val = String.valueOf(cols.nval);
			}
			LearningNode ln = new LearningNode(val, position);
			ln.setDescription(val);
			ln.setParticipa(true);
			variablesVector.add(ln);
			token = cols.nextToken();
			position++;
		}
	}

	/**
	 * Gets the number of rows in the file. This information is relevant because of
	 * the size of the matrix of indexes and the vector of repeated instances.
	 * 
	 * @param br
	 *            - A bufferedReader object
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
	 * variables are choose by the user of the program. Remember that the compacted
	 * variable will not participate of the leaning process.
	 * 
	 * @param rows
	 *            - The number of rows of the file.
	 */
	private void filterVariablesVector(int rows) {
		int nCols = 0;
		for (int i = 0; i < variablesVector.size(); i++) {
			LearningNode node = (LearningNode) variablesVector.get(i);
			if (node.getParticipa()) {
				if (!node.getRep()) {
					node.setPos(nCols);
					variables.add(node);
					nCols++;
				} else {
					vector = new int[rows];
					compacted = true;
				}
			}
		}
	}

	public boolean isAState(LearningNode node, String statName) {
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase(statName)) {
				return true;
			}
		}
		return false;
	}

	public int getStatePosition(LearningNode node, String statName) {
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase(statName)) {
				if (i == 2) {
					System.out.println();
				}
				return i;
			}
		}

		return 0;
	}

	/**
	 * Constructs the matrix of indexes. This matrix is composed by bytes primitive
	 * types occupy fewer memory.
	 * 
	 * @param cols
	 *            - A StreamTokenizer object
	 * 
	 * @param rows
	 *            - The number of rows in the file that constains the database.
	 * @throws IOException
	 */
	private void makeMatrix(StreamTokenizer cols, int rows) throws IOException {
		boolean missing = false;
		int position = 0;
		String stateName = "";
		LearningNode node;
		if (cols.ttype == StreamTokenizer.TT_EOL) {
			cols.nextToken();
		}
		while (cols.ttype != StreamTokenizer.TT_EOF && caseNumber <= rows) {
			while (cols.ttype != StreamTokenizer.TT_EOL && position < variablesVector.size() && caseNumber <= rows) {
				node = (LearningNode) variablesVector.get(position);
				if (node.getRep()) { // <-- if getRep() == true, compressed data
					try {
						vector[(int) caseNumber] = (int) cols.nval;
					} catch (Throwable e) {
						vector[(int) caseNumber] = Integer.parseInt(cols.sval);
					}
				} else if (node.getParticipa()) {
					if (cols.sval != null) {
						stateName = cols.sval;
						if (!isAState(node, stateName)) {
							if (!stateName.equals(missingValueToken)) {
								node.adicionaEstado(stateName);
								missing = false;
							} else {
								missing = true;
							}
						} else {
							missing = false;
						}
					} else {
						stateName = String.valueOf(cols.nval);
						if (!isAState(node, stateName)) {
							node.adicionaEstado(stateName);
							missing = false;
						}
					}
					if (!missing) {
						matrix[(int) caseNumber][node.getPos()] = (int) getStatePosition(node, stateName); // <--
																											// getEstadoPosicao()
																											// returns
																											// the
																											// number of
																											// a state
																											// value

					} else {
						matrix[(int) caseNumber][node.getPos()] = -1;
					}
				}
				cols.nextToken();
				position++;
			}
			caseNumber++;
			while (cols.ttype != StreamTokenizer.TT_EOL && caseNumber < rows) {
				cols.nextToken();
			}
			position = 0;
			cols.nextToken();
		}
	}

	/**
	 * Normalizes the probabilities of a variable.
	 * 
	 * @param variable
	 *            - A LearningNode object.
	 */
	// private void normalize(LearningNode variable) {
	// for (int c = 0; c < variable.getProbabilityFunction().tableSize()/*
	// .getDados().size() */; c += variable
	// .getEstadoTamanho()/* .noEstados() */) {
	// float sum = 0;
	// for (int i = 0; i < variable.getEstadoTamanho(); i++) {
	// sum += variable.getProbabilityFunction().getValue(c + i);
	// }
	// if (sum == 0) {
	// for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
	// variable.getProbabilityFunction().setValue(c + i,
	// 1 / variable.getEstadoTamanho());
	// }
	// } else {
	// for (int i = 0; i < variable.getEstadoTamanho()/* .noEstados() */; i++) {
	// variable.getProbabilityFunction().setValue(c + i,
	// variable.getProbabilityFunction().getValue(c + i) / sum);
	// }
	// }
	// }
	// }

	public int[][] getMatrix() {
		return this.matrix;
	}

	public ArrayList<LearningNode> getVariables() {
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

	public ArrayList<LearningNode> getVariablesVector() {
		// TODO use interface java.util.List instead
		return variablesVector;
	}
	
	/**
	 * @return token to be used to represent a missing value.
	 * @see #DEFAULT_MISSING_VALUE_TOKEN
	 */
	public String getMissingValueToken() {
		return missingValueToken;
	}

	/**
	 * @param missingValueToken :
	 * token to be used to represent a missing value.
	 * @see #DEFAULT_MISSING_VALUE_TOKEN
	 */
	public void setMissingValueToken(String missingValueToken) {
		this.missingValueToken = missingValueToken;
	}
}
