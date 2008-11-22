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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import unbbayes.gui.HierarchicTree;
import unbbayes.io.builder.IProbabilisticNetworkBuilder;
import unbbayes.io.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.util.ArrayMap;
import unbbayes.util.Debug;

/**
 * Manipulates input/output of NET files.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @author Mario Henrique Paes Vieira (mariohpv@bol.com.br)
 * @author Shou Matsumoto
 * @version 2.0
 */
public class NetIO implements BaseIO {

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.io.resources.IoResources");

	private static final String ERROR_NET = resource.getString("errorNet");
	
	/**
	 *  Loads a NET format file using default node/network builder.
	 *  In other words, this method returns exactly instances of ProbabilisticNetwork filled
	 *  by DecisionNode, ProbabilisticNode and UtilityNode.
	 * @see DefaultProbabilisticNodeBuilder
	 * @param  input  file to be read.
	 * @return loaded net.
	 * @throws LoadException when there were errors loading the network
	 * @throws IOException in case there were errors when manipulating files.
	 */
	public ProbabilisticNetwork load(File input)
		throws LoadException, IOException {
		
		int index = input.getName().lastIndexOf('.');
		String id = input.getName().substring(0, index);
		
		// create network using default builder
		IProbabilisticNetworkBuilder networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
		
		ProbabilisticNetwork net = networkBuilder.buildNetwork(id);
		
		load(input, net, networkBuilder);
		return net;		
	}
	
	/**
	 *  Loads a NET format file using network builder
	 * @see IProbabilisticNetworkBuilder
	 * @param  input  file to be read.
	 * @param  networkBuilder: builder to be used in order to generate expected instances
	 * of probabilistic network, probabilistic nodes, decision nodes and utility nodes. This
	 * is useful if you want to reuse NetIO for networks/nodes which extends ProbabilisticNetwork,
	 * ProbabilisticNode, DecisionNode and UtilityNode (or else NetIO will be bound to those 
	 * superclasses only).
	 * @return loaded net.
	 * @throws LoadException when there were errors loading the network
	 * @throws IOException in case there were errors when manipulating files.
	 */
	public ProbabilisticNetwork load(File input, IProbabilisticNetworkBuilder networkBuilder)
		throws LoadException, IOException {
		
		int index = input.getName().lastIndexOf('.');
		String id = input.getName().substring(0, index);
		ProbabilisticNetwork net = networkBuilder.buildNetwork(id);
		load(input, net, networkBuilder);
		return net;		
	}

	/**
	 * Saves a network in basic NET file format.
	 *
	 * @param  output file where the net should be saved.
	 * @param net network to be saved.
	 */
	public void save(File output, SingleEntityNetwork net) throws FileNotFoundException {
		PrintStream arq = new PrintStream(new FileOutputStream(output));
		arq.println("net");
		arq.println("{");
		arq.println(
			"     node_size = ("
				+ (int) (net.getRadius() * 2)
				+ " "
				+ (int) (net.getRadius() * 2)
				+ ");");
		arq.println("     name = \"" + net.getName() + "\";");
		String tree = saveHierarchicTree(net.getHierarchicTree());
		if (tree != null)
			arq.println("     tree = \"" + tree + "\";");
        arq.println("     UnBBayes_Color_Probabilistic_Description = \"" + ProbabilisticNode.getDescriptionColor().getRGB() + "\";");
        arq.println("     UnBBayes_Color_Probabilistic_Explanation = \"" + ProbabilisticNode.getExplanationColor().getRGB() + "\";");
		arq.println("     UnBBayes_Color_Utility = \"" + UtilityNode.getColor().getRGB() + "\";");
		arq.println("     UnBBayes_Color_Decision = \"" + DecisionNode.getColor().getRGB() + "\";");
		arq.println("}");
		arq.println();

		int sizeNos = net.getNodeCount();
		Node auxNo1;
		for (int c1 = 0; c1 < sizeNos; c1++) {
			auxNo1 =  net.getNodeAt(c1);
			if (auxNo1.getType() == Node.PROBABILISTIC_NODE_TYPE) {
				arq.print("node");
			} else if (auxNo1.getType() == Node.DECISION_NODE_TYPE) {
				arq.print("decision");
			} else { // TVU
				arq.print("utility");
			}

			arq.println(" " + auxNo1.getName());
			arq.println("{");
			arq.println(
				"     label = \"" + auxNo1.getDescription() + "\";");
			arq.println(
				"     position = ("
					+ (int) auxNo1.getPosition().getX()
					+ " "
					+ (int) auxNo1.getPosition().getY()
					+ ");");

			if (!(auxNo1.getType() == Node.UTILITY_NODE_TYPE)) {
				/* Check if the node represents a numeric attribute */
				if (auxNo1.getStatesSize() == 0) {
					/* The node represents a numeric attribute */
					double[] mean = auxNo1.getMean();
					double[] stdDev = auxNo1.getStandardDeviation();
					StringBuffer auxString = new StringBuffer();
					
					/* Mean per class */
					auxString.append("\"" + mean[0] + "\"");
					for (int i = 1; i < mean.length; i++) {
						auxString.append(" \"" + mean[i] + "\"");
					}
					arq.println(
							"     meanPerClass = (" + auxString.toString() + ");");
					
					/* Standard deviation per class */
					auxString = new StringBuffer();
					auxString.append("\"" + stdDev[0] + "\"");
					for (int i = 1; i < mean.length; i++) {
						auxString.append(" \"" + stdDev[i] + "\"");
					}
					arq.println(
							"     stdDevPerClass = (" + auxString.toString() + ");");
				} else {
					/* The node represents a nominal attribute */
					StringBuffer auxString =
						new StringBuffer("\"" + auxNo1.getStateAt(0) + "\"");
	
					int sizeEstados = auxNo1.getStatesSize();
					for (int c2 = 1; c2 < sizeEstados; c2++) {
						auxString.append(" \"" + auxNo1.getStateAt(c2) + "\"");
					}
					arq.println(
						"     states = (" + auxString.toString() + ");");
				}
			}
			if (auxNo1.getInformationType() == Node.EXPLANATION_TYPE)
                            {
                              String explanationDescription = formatString(auxNo1.getExplanationDescription());
                              arq.println("     %descricao \"" + explanationDescription + "\"");
                              ArrayMap arrayMap = auxNo1.getPhrasesMap();
                              int size = arrayMap.size();
                              ArrayList keys = arrayMap.getKeys();
                              for (int i = 0; i < size; i++)
                              {
                                Object key = keys.get(i);
                                ExplanationPhrase explanationPhrase = (ExplanationPhrase) arrayMap.get(key);
                                arq.println("     %frase \""+ explanationPhrase.getNode()+ "\" "+ "\""
                                    + explanationPhrase.getEvidenceType()+ "\" "+ "\""
                                    + formatString(explanationPhrase.getPhrase())+ "\"");
                              }
			}
			arq.println("}");
			arq.println();
		}
		/* 
		 * end of variable writing
		 * let's start writing potenciais!
		 */
		for (int c1 = 0; c1 < net.getNodeCount(); c1++) {
			auxNo1 = (Node) net.getNodeAt(c1);

			ArrayList<Node> auxListVa = auxNo1.getParents();

			arq.print("potential (" + auxNo1.getName());

			int sizeVa = auxListVa.size();
			if (sizeVa > 0) {
				arq.print(" |");
				for (int c2 = 0; c2 < sizeVa; c2++) {
					Node auxNo2 = (Node) auxListVa.get(c2);
					arq.print(" " + auxNo2.getName());
				}
			}
			
			arq.println(")");
			arq.println("{");
			if (auxNo1 instanceof ITabledVariable) {
				PotentialTable auxTabPot =
					((ITabledVariable) auxNo1).getPotentialTable();
				int sizeVa1 = auxTabPot.variableCount();

				arq.print(" data = ");
				int[] coord;
				boolean[] paren = new boolean[sizeVa1];

				int sizeDados = auxTabPot.tableSize();
				for (int c2 = 0; c2 < sizeDados; c2++) {
					coord = auxTabPot.voltaCoord(c2);

					for (int c3 = 0; c3 < sizeVa1; c3++) {
						if ((coord[c3] == 0) && (!paren[c3])) {
							arq.print("(");
							paren[c3] = true;
						}
					}
					arq.print(" " + auxTabPot.getValue(c2));
					if ((c2 % auxNo1.getStatesSize())
						== auxNo1.getStatesSize() - 1) {
						arq.print(" ");
					}

					int celulas = 1;

					Node auxNo2;
					for (int c3 = 0; c3 < sizeVa1; c3++) {
						auxNo2 = auxTabPot.getVariableAt(c3);
						celulas *= auxNo2.getStatesSize();
						if (((c2 + 1) % celulas) == 0) {
							arq.print(")");
							if (c3 == sizeVa1 - 1) {
								arq.print(";");
							}
							paren[c3] = false;
						}
					}

					if (((c2 + 1) % auxNo1.getStatesSize()) == 0) {
						arq.println();
					}
				}
			}

			arq.println("}");
			arq.println();
		}
		arq.close();
	}
	
	/**
	 * TODO create a class for loading/saving MSBN and use delegator pattern to
	 * use NetIO routines.
	 */
	public void saveMSBN(File output, SingleAgentMSBN msbn) throws FileNotFoundException {
		if (! output.isDirectory()) {
			System.err.println(resource.getString("IsNotDirectoryException"));
			return;			
		}
		
		for (int i = msbn.getNetCount()-1; i>=0; i--) {
			SingleEntityNetwork net = msbn.getNetAt(i);
			File out = new File(output, net.getId() + ".net");
			save(out, net);
		}
	}

	/**
	 * TODO create a class for loading/saving MSBN and use delegator pattern to
	 * use NetIO routines.
	 */
	public SingleAgentMSBN loadMSBN(File input) throws IOException,LoadException {
		if (! input.isDirectory()) {
			throw new LoadException(resource.getString("IsNotDirectoryException"));
		}
		
		IProbabilisticNetworkBuilder networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
		
		SingleAgentMSBN msbn = new SingleAgentMSBN(input.getName());
		
		File files[] = input.listFiles();
		for (int i = 0; i < files.length; i++) {			
			if (files[i].isFile()) {
				String fileName = files[i].getName();
				int index = fileName.lastIndexOf('.');
				if (index < 0) {
					throw new RuntimeException();
				}
				if (fileName.substring(index+1).equalsIgnoreCase("net")) {
					SubNetwork net = new SubNetwork(fileName.substring(0, index));
					load(files[i], net, networkBuilder);
					msbn.addNetwork(net);
				}
			}
		}
		return msbn;
	}
	
	private void load(File input, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) 
				throws IOException, LoadException {
		ITabledVariable auxIVTab = null;
		PotentialTable auxTabPot = null;

		BufferedReader r = new BufferedReader(new FileReader(input));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();

		st.wordChars('A', 'Z');
		st.wordChars('a', '}');
		st.wordChars('\u00A0', '\u00FF'); // letras com acentos
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('0', '9');
		st.wordChars('.', '.');
		st.wordChars('%', '%');
		st.ordinaryChars('(', ')');
		st.eolIsSignificant(false);
		st.quoteChar('"');
		//st.commentChar('%');

		getNext(st);
		if (st.sval.equals("net")) {
			getNext(st);

			if (st.sval.equals("{")) {
				getNext(st);
				while (!st.sval.equals("}")) {
					if (st.sval.equals("name")) {
						getNext(st);
						net.setName(st.sval);
					} else if (st.sval.equals("node_size")) {
						getNext(st);
						getNext(st);
						net.setRadius(Double.parseDouble(st.sval) / 2);
					} else if (st.sval.equals("tree")) {
						getNext(st);
						StringBuffer sb = new StringBuffer(st.sval);
						DefaultMutableTreeNode root =
							new DefaultMutableTreeNode("Information Variable");
						loadHierarchicTree(sb, root);

						// construct tree
						DefaultTreeModel model = new DefaultTreeModel(root);
						HierarchicTree hierarchicTree =
							new HierarchicTree(model);

						net.setHierarchicTree(hierarchicTree);
					} else if (st.sval.equals("UnBBayes_Color_Utility")) {
						getNext(st);
						UtilityNode.setColor(Integer.parseInt(st.sval));
					} else if (st.sval.equals("UnBBayes_Color_Decision")) {
						getNext(st);
						DecisionNode.setColor(Integer.parseInt(st.sval));
					} else if (st.sval.equals("UnBBayes_Color_Probabilistic_Description")) {
                        getNext(st);
                        ProbabilisticNode.setDescriptionColor(Integer.parseInt(st.sval));
                    } else if (st.sval.equals("UnBBayes_Color_Probabilistic_Explanation")) {
                        getNext(st);
                        ProbabilisticNode.setExplanationColor(Integer.parseInt(st.sval));
                    }
					getNext(st);
				}
			}
		} else {
			throw new LoadException(
				ERROR_NET + resource.getString("LoadException"));
		}

		while (getNext(st) != StreamTokenizer.TT_EOF) {
			if (st.sval.equals("node")
				|| st.sval.equals("decision")
				|| st.sval.equals("utility")) {
				Node auxNo = null;
				if (st.sval.equals("node")) {
//					auxNo = new ProbabilisticNode();
					auxNo = networkBuilder.getProbabilisticNodeBuilder().buildNode();
				} else if (st.sval.equals("decision")) {
//					auxNo = new DecisionNode();
					auxNo = networkBuilder.getDecisionNodeBuilder().buildNode();
				} else { // utility
//					auxNo = new UtilityNode();
					auxNo = networkBuilder.getUtilityNodeBuilder().buildNode();
				}

				getNext(st);
				auxNo.setName(st.sval);
				getNext(st);
				if (st.sval.equals("{")) {
					getNext(st);
					while (!st.sval.equals("}")) {
						if (st.sval.equals("label")) {
							getNext(st);
							auxNo.setDescription(st.sval);
							getNext(st);
						} else if (st.sval.equals("position")) {
							getNext(st);
							int x = Integer.parseInt(st.sval);
							getNext(st);
							if (x <= 0) {
								x = Node.getWidth();
							}
							int y = Integer.parseInt(st.sval);
							if (y <= 0) {
								y = Node.getHeight();
							}
							auxNo.getPosition().setLocation(x, y);
							getNext(st);
						} else if (st.sval.equals("states")) {
							while (getNext(st) == '"') {
								auxNo.appendState(st.sval);
							}
						} else if (st.sval.equals("meanPerClass")) {
							ArrayList<String> array = new ArrayList<String>();
							while (getNext(st) == '"') {
								array.add(st.sval);
							}
							int size = array.size();
							double[] mean = new double[size];
							for (int i = 0; i < size; i++) {
								mean[i] = Double.parseDouble(array.get(i));
							}
							auxNo.setMean(mean);
						} else if (st.sval.equals("stdDevPerClass")) {
							ArrayList<String> array = new ArrayList<String>();
							while (getNext(st) == '"') {
								array.add(st.sval);
							}
							int size = array.size();
							double[] stdDev = new double[size];
							for (int i = 0; i < size; i++) {
								stdDev[i] = Double.parseDouble(array.get(i));
							}
							auxNo.setStandardDeviation(stdDev);
						} else if (st.sval.equals("%descricao")) {
							getNext(st);
							auxNo.setExplanationDescription(
								unformatString(st.sval));
							auxNo.setInformationType(Node.EXPLANATION_TYPE);
							readTillEOL(st);
							getNext(st);
						} else if (st.sval.equals("%frase")) {
							getNext(st);
							ExplanationPhrase explanationPhrase =
								new ExplanationPhrase();
							explanationPhrase.setNode(st.sval);
							getNext(st);
							try {
								explanationPhrase.setEvidenceType(
									Integer.parseInt(st.sval));
							} catch (Exception ex) {
								throw new LoadException(
									ERROR_NET
										+ " l."
										+ st.lineno()
										+ resource.getString("LoadException2")
										+ st.sval);
							}
							getNext(st);
							explanationPhrase.setPhrase(
								unformatString(st.sval));
							auxNo.addExplanationPhrase(explanationPhrase);
							readTillEOL(st);
							getNext(st);
						} else if (st.sval.contains("HR_")) {
							// this is a HUGIN specific declaration.
							// let's ignore it
							Debug.println(this.getClass(), "Ignoring HR declaration: " + st.sval);
							while (getNext(st) == '"');
						} else {
							throw new LoadException(
								ERROR_NET
									+ " l."
									+ st.lineno()
									+ resource.getString("LoadException2")
									+ st.sval);
						}
					}
					net.addNode(auxNo);
				} else {
					throw new LoadException(
						ERROR_NET
							+ " l."
							+ st.lineno()
							+ resource.getString("LoadException3"));
				}
			} else if (st.sval.equals("potential")) {
				getNext(st);
				Node auxNo1 = net.getNode(st.sval);

				if (auxNo1 instanceof ITabledVariable) {
					auxIVTab = (ITabledVariable) auxNo1;
					auxTabPot = auxIVTab.getPotentialTable();
					auxTabPot.addVariable(auxNo1);
				}

				getNext(st);
				if (st.sval.equals("|")) {
					getNext(st);
				}

				Node auxNo2;
				Edge auxArco;
				while (!st.sval.startsWith("{")) {
					auxNo2 = net.getNode(st.sval);
					auxArco = new Edge(auxNo2, auxNo1);
					try {
						net.addEdge(auxArco);
					} catch (InvalidParentException e) {
						throw new LoadException(e.getMessage());
					}
					getNext(st);
				}
				
				/*
				 * Invert the parents in the table, to
				 * mantain consistency in the program.
				 * Internal pre-requisite.
				 */
				if (auxNo1 instanceof ITabledVariable) {
					int sizeVetor = auxTabPot.variableCount() / 2;
					for (int k = 1; k <= sizeVetor; k++) {
						Object temp = auxTabPot.getVariableAt(k);
						auxTabPot.setVariableAt(
							k,
							auxTabPot.getVariableAt(
								auxTabPot.variableCount() - k));
						auxTabPot.setVariableAt(
							auxTabPot.variableCount() - k,
							(Node) temp);
					}
				}
				
				if (st.sval.length() == 1) {
					getNext(st);
				}

				int nDim = 0;

				while (!st.sval.endsWith("}")) {
					if (st.sval.equals("data")) {
						if (auxNo1.getType() == Node.DECISION_NODE_TYPE) {
							throw new LoadException(
								ERROR_NET
									+ " l."
									+ st.lineno()
									+ resource.getString("LoadException4"));
						}
						getNext(st);
						while (!st.sval.equals("}")) {
							if (st.sval.equals("%")) {
								readTillEOL(st);
							} else {
								auxTabPot.setValue(
									nDim++,
									Float.parseFloat(st.sval));
							}
							getNext(st);
						}
					} else {
						throw new LoadException(
							ERROR_NET
								+ " l."
								+ st.lineno()
								+ resource.getString("LoadException5"));
					}
				}
			}
		}
		r.close();
		HierarchicTree tree = net.getHierarchicTree();
		if (tree != null) {
			tree.setProbabilisticNetwork(net,HierarchicTree.DESCRIPTION_TYPE);
		}
	}	

	private int getNext(StreamTokenizer st) throws IOException {
		do {
			st.nextToken();
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}

	private void loadHierarchicTree(
		StringBuffer sb,
		DefaultMutableTreeNode root) {
		int size = sb.length();
		DefaultMutableTreeNode nextRoot = null;
		Stack<DefaultMutableTreeNode> stack = new Stack<DefaultMutableTreeNode>();
		for (int i = 0; i < size; i++) {
			char c = sb.charAt(i);
			if (c == '(') {
				if (nextRoot != null) {
					stack.push(root);
					root = nextRoot;
				}
			} else if (c == ')') {
				if (stack.size() > 0) {
					root = (DefaultMutableTreeNode) stack.pop();
				}
			} else if (c == ',') {
			} else {
				StringBuffer newWord = new StringBuffer();
				while ((c != '(') && (c != ')') && (c != ',')) {
					newWord.append(c);
					i++;
					c = sb.charAt(i);
				}
				i--;
				DefaultMutableTreeNode newNode =
					new DefaultMutableTreeNode(newWord);
				nextRoot = newNode;
				root.add(newNode);
			}
		}
	}

	private String saveHierarchicTree(HierarchicTree hierarchicTree) {
		TreeModel model = hierarchicTree.getModel();
		StringBuffer sb = new StringBuffer();
		TreeNode root = (TreeNode) model.getRoot();
		int childCount = model.getChildCount(root);
		if (childCount == 0) {
			return null;
		} else {
			sb.append('(');
			for (int i = 0; i < childCount; i++) {
				processTreeNode((TreeNode) model.getChild(root, i), sb, model);
				if (i != (childCount - 1)) {
					sb.append(',');
				}
			}
			sb.append(')');
			return sb.toString();
		}
	}

	private void processTreeNode(
		TreeNode node,
		StringBuffer sb,
		TreeModel model) {
		sb.append(node.toString());
		if (!node.isLeaf()) {
			sb.append('(');
			int childCount = model.getChildCount(node);
			for (int i = 0; i < childCount; i++) {
				processTreeNode((TreeNode) model.getChild(node, i), sb, model);
				if (i != (childCount - 1)) {
					sb.append(',');
				}
			}
			sb.append(')');
		}
	}

	/**
	* Reads and skips all tokens before next end of line token.
	*
	* @param tokenizer Stream tokenizer
	* @throws IOException EOF not found
	*/
	private void readTillEOL(StreamTokenizer tokenizer) throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
		};
		tokenizer.pushBack();
	}

	private String formatString(String string) {
		return string.replace('\n', '#');
	}

	private String unformatString(String string) {
		return string.replace('#', '\n');
	}

}