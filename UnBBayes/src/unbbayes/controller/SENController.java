package unbbayes.controller;

import java.awt.Cursor;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import unbbayes.gui.ExplanationProperties;
import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.NodeList;

public class SENController {

	private NetworkWindow screen;

	private SingleEntityNetwork singleEntityNetwork;

	private NumberFormat df;

	private final Pattern decimalPattern = Pattern
			.compile("[0-9]*([.|,][0-9]+)?");

	private Matcher matcher;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.controller.resources.ControllerResources");

	/**
	 * Constructs a controller for SingleEntityNetwork.
	 * 
	 */
	public SENController(SingleEntityNetwork singleEntityNetwork,
			NetworkWindow screen) {
		this.singleEntityNetwork = singleEntityNetwork;
		this.screen = screen;
		df = NumberFormat.getInstance(Locale.getDefault());
		df.setMaximumFractionDigits(4);
	}

	/**
	 * Insere novo estado no nó selecionado.
	 * 
	 * @param no
	 *            o <code>Object <code>selecionado.
	 * @since
	 * @see		Object
	 */
	public void insertState(Node no) {
		if (no instanceof ProbabilisticNode) {
			no.appendState(resource.getString("stateProbabilisticName")
					+ no.getStatesSize());
		} else if (no instanceof DecisionNode) {
			no.appendState(resource.getString("stateDecisionName")
					+ no.getStatesSize());
		}
		screen.setTable(makeTable(no));
	}

	/**
	 * Remove último estado do nó selecionado.
	 * 
	 * @param no
	 *            o <code>Object <code>selecionado.
	 * @since
	 * @see		Object
	 */
	public void removeState(Node no) {
		no.removeLastState();
		screen.setTable(makeTable(no));
	}

	/**
	 * Inicia as crenças da árvore de junção.
	 */
	public void initialize() {
		try {
			singleEntityNetwork.initialize();
			screen.getEvidenceTree().updateTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Propaga as evidências da rede Bayesiana ( <code>TRP</code> ).
	 * 
	 * @since
	 */
	public void propagate() {
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		boolean temLikeliHood = false;
		try {
			singleEntityNetwork.updateEvidences();
			if (!temLikeliHood) {
				screen.setStatus(resource
						.getString("statusEvidenceProbabilistic")
						+ df.format(singleEntityNetwork.PET() * 100.0));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(screen, resource
					.getString("statusEvidenceException"), resource
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
		}
		screen.getEvidenceTree().updateTree();
		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Compila a rede Bayesiana. Caso haja algum problema na compilação,
	 * mostra-se o erro em um <code>JOptionPane</code> .
	 * 
	 * @return true se a rede compilar sem problemas e false se houver algum
	 *         problema na compilação
	 * @since
	 * @see JOptionPane
	 */
	public boolean compileNetwork() {
		long ini = System.currentTimeMillis();
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			((ProbabilisticNetwork) singleEntityNetwork).compile();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), resource
					.getString("statusError"), JOptionPane.ERROR_MESSAGE);
			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return false;
		}

		// Ordenar pela descricao do nó apenas para facilitar a visualização da
		// árvore.
		NodeList nos = singleEntityNetwork.getNodesCopy();
		boolean haTroca = true;
		while (haTroca) {
			haTroca = false;
			for (int i = 0; i < nos.size() - 1; i++) {
				Node node1 = nos.get(i);
				Node node2 = nos.get(i + 1);
				if (node1.getDescription().compareToIgnoreCase(
						node2.getDescription()) > 0) {
					nos.set(i + 1, node1);
					nos.set(i, node2);
					haTroca = true;
				}
			}
		}

		screen.getEvidenceTree().updateTree();

		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		screen.setStatus(resource.getString("statusTotalTime")
				+ df.format(((System.currentTimeMillis() - ini)) / 1000.0)
				+ resource.getString("statusSeconds"));
		return true;
	}

	/**
	 * Insere o nó desejado na rede criando estado, sigla e descrição padrões.
	 * 
	 * @param no
	 *            uma <code>Node</code> que representa o nó a ser inserido
	 * @since
	 * @see unbbayes.prs.Node
	 */
	public void insertProbabilisticNode(double x, double y) {
		ProbabilisticNode node = new ProbabilisticNode();
		node.setPosition(x, y);
		node.appendState(resource.getString("firstStateProbabilisticName"));
		node.setName(resource.getString("probabilisticNodeName")
				+ singleEntityNetwork.getNodeCount());
		node.setDescription(node.getName());
		PotentialTable auxTabProb = ((ITabledVariable) node)
				.getPotentialTable();
		auxTabProb.addVariable(node);
		auxTabProb.setValue(0, 1);
		singleEntityNetwork.addNode(node);
	}

	/**
	 * Insere o nó desejado na rede criando estado, sigla e descrição padrões.
	 * 
	 * @param no
	 *            uma <code>DecisionNode</code> que representa o nó a ser
	 *            inserido
	 * @since
	 * @see unbbayes.prs.DecisionNode
	 */
	public void insertDecisionNode(double x, double y) {
		DecisionNode node = new DecisionNode();
		node.setPosition(x, y);
		node.appendState(resource.getString("firstStateDecisionName"));
		node.setName(resource.getString("decisionNodeName")
				+ singleEntityNetwork.getNodeCount());
		node.setDescription(node.getName());
		singleEntityNetwork.addNode(node);
	}

	/**
	 * Insere o nó desejado na rede criando estado, sigla e descrição padrões.
	 * 
	 * @param no
	 *            uma <code>UtilityNode</code> que representa o nó a ser
	 *            inserido
	 * @since
	 * @see unbbayes.prs.UtilityNode
	 */
	public void insertUtilityNode(double x, double y) {
		UtilityNode node = new UtilityNode();
		node.setPosition(x, y);
		node.setName(resource.getString("utilityNodeName")
				+ singleEntityNetwork.getNodeCount());
		node.setDescription(node.getName());
		PotentialTable auxTab = ((ITabledVariable) node).getPotentialTable();
		auxTab.addVariable(node);
		singleEntityNetwork.addNode(node);
	}

	/**
	 * Faz a ligacão do arco desejado entre pai e filho.
	 * 
	 * @param arco
	 *            um <code>TArco</code> que representa o arco a ser ligado
	 * @since
	 */
	public void insertEdge(Edge arco) {
		singleEntityNetwork.addEdge(arco);
	}

	/**
	 * This method is responsible to represent the potential table as a JTable, 
	 * including its table model.
	 * 
	 * @param no The node to get its probabilistic table as JTable.
	 * @return Returns the JTable representing the node's probabilistic table.
	 */
	public JTable makeTable(final Node node) {
		screen.getTxtDescription().setEnabled(true);
		screen.getTxtSigla().setEnabled(true);
		screen.getTxtDescription().setText(node.getDescription());
		screen.getTxtSigla().setText(node.getName());

		final JTable table;
		final PotentialTable potTab;
		final int nVariables;

		/* Check if the node represents a numeric attribute */
		if (node.getStatesSize() == 0) {
			Node parent = node.getParents().get(0);
			int numClasses = parent.getStatesSize();
			double[] mean = node.getMean();
			double[] stdDev = node.getStandardDeviation();

			table = new JTable(3, numClasses + 1);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setTableHeader(null);

			/* First column */
			table.setValueAt(parent.getName(), 0, 0);
			table.setValueAt(resource.getString("mean"), 1, 0);
			table.setValueAt(resource.getString("stdDev"), 2, 0);

			/* Other columns */
			for (int i = 0; i < numClasses; i++) {
				table.setValueAt(parent.getStateAt(i), 0, i + 1);
				table.setValueAt(mean[i], 1, i + 1);
				table.setValueAt(stdDev[i], 2, i + 1);
			}

			return table;
		}

		if (node instanceof ITabledVariable) {
			potTab = ((ITabledVariable) node).getPotentialTable();

			// Number of variables
			nVariables = potTab.variableCount();
			
			table = potTab.makeTable();

		} else {
			// decision

			// the number of rows in this case is the number of states of the
			// node and the number of columns is always 1.
			// int rows = node.getStatesSize();
			// int columns = 1;

			// there is no potential table and the number of variables is the
			// number of parents this node has.
			potTab = null;
			nVariables = node.getParents().size();

			table = new JTable(node.getStatesSize(), 1);
			// put the name of each state in the first and only column.
			for (int i = 0; i < node.getStatesSize(); i++) {
				table.setValueAt(node.getStateAt(i), i, 0);
			}
			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setTableHeader(null);

		}
		// TODO MIGRATE TO A DIFFERENT CLASS - GUI.TABLE.PROBABILISTICTABLEMODEL
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getLastRow() < nVariables - 1) {
					return;
				}
				if (e.getColumn() == 0) {
					if (!table.getValueAt(e.getLastRow(), e.getColumn())
							.equals("")) {
						node.setStateAt(table.getValueAt(e.getLastRow(),
								e.getColumn()).toString(), e.getLastRow()
								- (table.getRowCount() - node.getStatesSize()));
					}
				} else {
					try {
						String temp = table.getValueAt(e.getLastRow(),
								e.getColumn()).toString().replace(',', '.');
						matcher = decimalPattern.matcher(temp);
						if (!matcher.matches()) {
							JOptionPane.showMessageDialog(null, /* resource.getString("decimalError") */
									"Decimal Error", /* resource.getString("decimalException") */
									"Decimal Exception",
									JOptionPane.ERROR_MESSAGE);
							table.revalidate();
							table.setValueAt(""
									+ potTab.getValue((e.getColumn() - 1)
											* node.getStatesSize()
											+ e.getLastRow() - nVariables + 1),
									e.getLastRow(), e.getColumn());
							return;
						}
						float valor = Float.parseFloat(temp);
						potTab.setValue((e.getColumn() - 1)
								* node.getStatesSize() + e.getLastRow()
								- nVariables + 1, valor);
					} catch (Exception pe) {
						System.err.println(resource
								.getString("potentialTableException"));
					}
				}
			}
		});

		return table;
	}
	
	/**
	 * Mostra a tabela de potenciais do no desejado.
	 * 
	 * @param no
	 *            um <code>Node</code> que representa o nó o qual deve-se
	 *            mostrar a tabela de potenciais
	 * @since
	 * @see unbbayes.prs.Node
	 * @deprecated
	 */
	public JTable makeTableOld(final Node node) {
		screen.getTxtDescription().setEnabled(true);
		screen.getTxtSigla().setEnabled(true);
		screen.getTxtDescription().setText(node.getDescription());
		screen.getTxtSigla().setText(node.getName());

		final JTable table;
		final PotentialTable potTab;
		final int variables;

		/* Check if the node represents a numeric attribute */
		if (node.getStatesSize() == 0) {
			Node parent = node.getParents().get(0);
			int numClasses = parent.getStatesSize();
			double[] mean = node.getMean();
			double[] stdDev = node.getStandardDeviation();

			table = new JTable(3, numClasses + 1);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setTableHeader(null);

			/* First column */
			table.setValueAt(parent.getName(), 0, 0);
			table.setValueAt(resource.getString("mean"), 1, 0);
			table.setValueAt(resource.getString("stdDev"), 2, 0);

			/* Other columns */
			for (int i = 0; i < numClasses; i++) {
				table.setValueAt(parent.getStateAt(i), 0, i + 1);
				table.setValueAt(mean[i], 1, i + 1);
				table.setValueAt(stdDev[i], 2, i + 1);
			}

			return table;
		}

		if (node instanceof ITabledVariable) {
			potTab = ((ITabledVariable) node).getPotentialTable();

			int states = 1;
			variables = potTab.variableCount();

			// calculate the number of states by multiplying the number of
			// states that each father (variables) has. Where variable 0 is the
			// node itself. That is why it starts at 1.
			/*
			 * Ex: states = 2 * 2;
			 * 
			 * |------------------------------------------------------| | Father
			 * 2 | State 1 | State 2 |
			 * |--------------|-------------------|-------------------| | Father
			 * 1 | State 1 | State 2 | State 1 | State 2 |
			 * |------------------------------------------------------| | Node
			 * State 1 | 1 | 1 | 1 | 1 | | Node State 2 | 0 | 0 | 0 | 0 |
			 * 
			 */
			states = potTab.tableSize() / node.getStatesSize();
			/*
			 * for (int count = 1; count < variables; count++) { states *=
			 * potTab.getVariableAt(count).getStatesSize(); }
			 */

			// the number of rows is the number of states the node has plus the
			// number of fathers (variables - 1, because one of the variables
			// is the node itself).
			int rows = node.getStatesSize() + variables - 1;

			// the number of columns is the number of states that we calculate
			// before plus one that is the column where the fathers names and
			// the states of the node itself will be placed.
			int columns = states + 1;

			table = new JTable(rows, columns);

			// put the name of the states of the node in the first column
			// starting in the (variables - 1)th row (number of fathers). That
			// is because on the rows before that there will be placed the
			// name of the fathers.
			for (int k = variables - 1, l = 0; k < table.getRowCount(); k++, l++) {
				table.setValueAt(node.getStateAt(l), k, 0);
			}

			// put the name of the father and its states' name in the right
			// place.
			for (int k = variables - 1, l = 0; k >= 1; k--, l++) {
				Node variable = potTab.getVariableAt(k);

				// the number of states is the multiplication of the number of
				// states of the other fathers above this one.
				states /= variable.getStatesSize();

				// put the name of the father in the first column.
				table.setValueAt(variable.getName(), l, 0);

				// put the name of the states of this father in the lth row
				// and ith column, repeating the name if necessary (for each
				// state of the father above).
				for (int i = 0; i < table.getColumnCount() - 1; i++) {
					table.setValueAt(variable.getStateAt((i / states)
							% variable.getStatesSize()), l, i + 1);
				}
			}

			// now states is the number of states that the node has.
			states = node.getStatesSize();

			// put the values of the probabilistic table in the jth row and ith
			// column, picking up the values in a double collection in potTab.
			for (int i = 1, k = 0; i < table.getColumnCount(); i++, k += states) {
				for (int j = variables - 1, l = 0; j < table.getRowCount(); j++, l++) {
					table.setValueAt("" + df.format(potTab.getValue(k + l)), j,
							i);
				}
			}

		} else {
			// decision

			// the number of rows in this case is the number of states of the
			// node and the number of columns is always 1.
			// int rows = node.getStatesSize();
			// int columns = 1;

			// there is no potential table and the number of variables is the
			// number of parents this node has.
			potTab = null;
			variables = node.getParents().size();

			table = new JTable(node.getStatesSize(), 1);
			// put the name of each state in the first and only column.
			for (int i = 0; i < node.getStatesSize(); i++) {
				table.setValueAt(node.getStateAt(i), i, 0);
			}

		}

		table.setTableHeader(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getLastRow() < variables - 1) {
					return;
				}
				if (e.getColumn() == 0) {
					if (!table.getValueAt(e.getLastRow(), e.getColumn())
							.equals("")) {
						node.setStateAt(table.getValueAt(e.getLastRow(),
								e.getColumn()).toString(), e.getLastRow()
								- (table.getRowCount() - node.getStatesSize()));
					}
				} else {
					try {
						String temp = table.getValueAt(e.getLastRow(),
								e.getColumn()).toString().replace(',', '.');
						matcher = decimalPattern.matcher(temp);
						if (!matcher.matches()) {
							JOptionPane.showMessageDialog(null, /* resource.getString("decimalError") */
									"Decimal Error", /* resource.getString("decimalException") */
									"Decimal Exception",
									JOptionPane.ERROR_MESSAGE);
							table.revalidate();
							table.setValueAt(""
									+ potTab.getValue((e.getColumn() - 1)
											* node.getStatesSize()
											+ e.getLastRow() - variables + 1),
									e.getLastRow(), e.getColumn());
							return;
						}
						float valor = Float.parseFloat(temp);
						potTab.setValue((e.getColumn() - 1)
								* node.getStatesSize() + e.getLastRow()
								- variables + 1, valor);
					} catch (Exception pe) {
						System.err.println(resource
								.getString("potentialTableException"));
					}
				}
			}
		});

		return table;
	}

	public void deleteSelected(Object selecionado) {
		if (selecionado instanceof Edge) {
			singleEntityNetwork.removeEdge((Edge) selecionado);
		} else if (selecionado instanceof Node) {
			singleEntityNetwork.removeNode((Node) selecionado);
		}
	}

	public void showExplanationProperties(ProbabilisticNode node) {
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		ExplanationProperties explanation = new ExplanationProperties(screen,
				singleEntityNetwork);
		explanation.setProbabilisticNode(node);
		explanation.setVisible(true);
		screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}
