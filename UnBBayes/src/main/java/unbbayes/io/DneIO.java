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

import unbbayes.io.exception.LoadException;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNodeBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.ArrayMap;
import unbbayes.util.Debug;

/**
 * Manipulates input/output of DNE files.
 * @author Rommel N. Carvalho (rommel.carvalho@gmail.com)
 * @version 2.0
 */
public class DneIO implements BaseIO {

	/** Load resource file from this package */
	private static ResourceBundle resource =
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.resources.IoResources.class.getName());

	private static final String ERROR_NET = resource.getString("errorDne");
	
	// since the StreamTokenizer is not counting linenumber depending on the configuration,
	// let's count it by ourselves
	protected long lineno = 1;
	
	// If the current node being loaded is discrete
	protected boolean isDiscreteNode;
	
	/** Array with a single element "dne" */
	public static final String[] SUPPORTED_EXTENSIONS = {"dne"};
	
	private String name = "DNE";

	private boolean isToUseAbsurdState = false;
	
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
		
		// create network using default builder
		ProbabilisticNetwork net = this.load(input, DefaultProbabilisticNetworkBuilder.newInstance());
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
		
		try {
			load(input, net, networkBuilder);
		} catch (HasImpossProbDeclarationException impossException) {
			// there is a cpt with @imposs. Retry using isToUseAbsurdState() == true
			boolean backup = isToUseAbsurdState();	// backup original value
			setToUseAbsurdState(true);				// force this class to append an "absurd" state to all nodes
			try {
				net = networkBuilder.buildNetwork(id);	// guarantee that old network is discarded
				this.load(input, net, networkBuilder);	// reload network using new config
			} catch (Exception e) {
				setToUseAbsurdState(backup);			// restore backup
				throw new IOException(e);
			} 
			// I'm not using the "finally" in order to restore backup, because for some reason JRE (not created by Sun) was not executing it
			setToUseAbsurdState(backup);			// restore backup
		}
		return net;		
	}

	/**
	 * Saves a network in basic NET file format.
	 *
	 * @param  output file where the net should be saved.
	 * @param graph network to be saved.
	 */
	public void save(File output, Graph graph) throws FileNotFoundException {
		PrintStream stream = new PrintStream(new FileOutputStream(output));
		
		SingleEntityNetwork net = (SingleEntityNetwork) graph;
		
		this.saveNetHeader(stream, net);

		
		// start filling node/decision/utility declaration
		
		int nodeSize = net.getNodeCount();
		Node auxNode1;
		for (int c1 = 0; c1 < nodeSize; c1++) {
			auxNode1 =  net.getNodeAt(c1);
			
			this.saveNodeDeclaration(stream, auxNode1, net);
			
		}
		/* 
		 * end of variable writing
		 * let's start writing potenciais!
		 */
		for (int c1 = 0; c1 < net.getNodeCount(); c1++) {
			auxNode1 = (Node) net.getNodeAt(c1);

			this.savePotentialDeclaration(stream, auxNode1, net);
		}
		stream.close();
	}
	
	
	protected void load(File input, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) 
				throws IOException, LoadException {
		
		BufferedReader r = new BufferedReader(new FileReader(input));
		StreamTokenizer st = new StreamTokenizer(r);
		this.setUpStreamTokenizer(st);

		// treat header 
		
		// not for now
		
//		getNext(st);
//		if (st.sval.equals("net")) {
//			this.loadNetHeader(st, net);
//		} else {
//			throw new LoadException(
//				ERROR_NET + resource.getString("LoadException"));
//		}

		// start treating body
		while (getNext(st) != StreamTokenizer.TT_EOF) {
			
			// if declaration is "visual" type, treat it
			this.loadVisualDeclaration(st, net, networkBuilder);
			
			// if declaration is "node" type, treat it
			this.loadNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "continuous node" type, treat it
//			this.loadContinuousNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "potential" type, treat it
//			this.loadPotentialDeclaration(st, net);
			
			// ignore other declarations
		}
		
		r.close();
		
	}	

	

	protected int getNext(StreamTokenizer st) throws IOException {
		do {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_EOL) {
				this.lineno++;
			}
			// force the following chars to be treated as ordinal chars, no matter what is the config of the st
			if (st.ttype == ';') {
				st.sval = ";";
			}
			if (st.ttype == '}') {
				st.sval = "}";
			}
			if (st.ttype == '{') {
				st.sval = "{";
			}
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != ';')
				&& (st.ttype != '{')
				&& (st.ttype != '}')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}


	/**
	* Reads and skips all tokens before next end of line token.
	*
	* @param tokenizer Stream tokenizer
	* @throws IOException EOF not found
	*/
	protected void readTillEOL(StreamTokenizer tokenizer) throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
		};
		tokenizer.pushBack();
	}

	protected String formatString(String string) {
		return string.replace('\n', '#');
	}

	protected String unformatString(String string) {
		return string.replace('#', '\n');
	}
	
	/**
	 * Configures valid/invalid character ranges of stream tokenizer.
	 * By setting up using protected method, it becomes easier to extend this class
	 * @param st: StreamTokenizer to set up
	 */
	protected void setUpStreamTokenizer(StreamTokenizer st) throws IOException {
		
		st.resetSyntax();
		
		st.wordChars('A', 'Z');
		st.wordChars('a', '}');
		st.wordChars('\u00A0', '\u00FF'); // characters with accents
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('0', '9');
		st.wordChars('.', '.');
		st.wordChars('%', '%');
		st.ordinaryChars('(', ')');
		st.ordinaryChar(';');
		st.ordinaryChar('{');
		st.ordinaryChar('}');
		st.eolIsSignificant(false);
		st.quoteChar('"');
		st.slashSlashComments(true);
		//st.commentChar('%');
		
	}

	/**
	 * Start loading net{} header from .net specification file; iterating under 
	 * {@link DneIO#loadNetHeaderBody(StreamTokenizer, SingleEntityNetwork)}
	 * in order to treat each declaration.
	 * {@link DneIO#loadNetHeaderBody(StreamTokenizer, SingleEntityNetwork)}
	 * must ignore incompatible declaration.
	 * @param st
	 * @param net
	 * @throws IOException
	 */
	protected void loadNetHeader(StreamTokenizer st, SingleEntityNetwork net)
								throws IOException {
		
		getNext(st);
		if (st.sval.equals("{")) {
			getNext(st);
			while (!st.sval.equals("}")) {
				this.loadNetHeaderBody(st, net);
				getNext(st);
			}			
		}
	}
	
	/**
	 * If a declaration inside net header is treatable, treat it.
	 * The currently compatible declarations are:
	 * 			name, node_size, tree, UnBBayes_Color_Utility, UnBBayes_Color_Decision,
	 * 			UnBBayes_Color_Probabilistic_Description, UnBBayes_Color_Probabilistic_Explanation.
	 * If declaration is not compatible, it will be ignored.
	 * @param st
	 * @param net
	 * @throws IOException
	 */
	protected void loadNetHeaderBody(StreamTokenizer st, SingleEntityNetwork net)
													throws IOException {
		
		if (st.sval.equals("name")) {
			getNext(st);
			net.setName(st.sval);
		} else if (st.sval.equals("node_size")) {
			getNext(st);
			getNext(st);
			net.setRadius(Double.parseDouble(st.sval) / 2);
		} else if (st.sval.equals("UnBBayes_Color_Utility")) {
			getNext(st);
			UtilityNode.setStaticColor(Integer.parseInt(st.sval));
		} else if (st.sval.equals("UnBBayes_Color_Decision")) {
			getNext(st);
			DecisionNode.setStaticColor(Integer.parseInt(st.sval));
		} else if (st.sval.equals("UnBBayes_Color_Probabilistic_Description")) {
            getNext(st);
            ProbabilisticNode.setDescriptionColor(Integer.parseInt(st.sval));
        } else if (st.sval.equals("UnBBayes_Color_Probabilistic_Explanation")) {
            getNext(st);
            ProbabilisticNode.setExplanationColor(Integer.parseInt(st.sval));
        }
		
	}
	
	/**
	 * This method handles the "VISUAL" declaration of a DNE file.
	 * @param st : tokenizer responsible for reading the DNE file
	 * @param net :  the network to be filled
	 * @param networkBuilder : builder for generating new nodes or networks. This may be used by subclasses extending this method in order
	 * to create new nodes/networks whenever necessary.
	 * @throws IOException 
	 */
	protected void loadVisualDeclaration(StreamTokenizer st, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) throws IOException {
		if (st.sval.equals("visual")) {
			// read name (identificator) of the "visual" declaration block, but ignore it
			if (this.getNext(st) != st.TT_WORD) {
				throw new LoadException(
						ERROR_NET
						+ " l."
						+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
						+ resource.getString("LoadException3"));
			}
			// read block initiator "{"
			this.getNext(st);
			if (!st.sval.equals("{")) {
				throw new LoadException(
						ERROR_NET
						+ " l."
						+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
						+ resource.getString("LoadException3"));
			}
			// start reading visual content
			while (this.getNext(st) != st.TT_EOF) {
				// TODO actually reflect it into UnBBayes config, like node colors, etc
				if (st.sval.equals("}")) {
					// end of visual content
					// expect end of declaration ";"
					if (this.getNext(st) == ';') {
						// end of "visual" block
						break;
					} else {
						throw new LoadException(
								ERROR_NET
								+ " l."
								+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
								+ resource.getString("LoadException3"));
					}
				} else if (st.sval.equals("{")) {
					// This is beginning of an inner block. Read content until we find end of inner block, but ignore contents
					// TODO do not ignore content of inner "visual" block
					while(this.getNext(st) != st.TT_EOF) {
						// caution: this if-clause assumes the second term of AND expression is only evaluated if the first is true (this is the default in java)
						if (st.sval.equals("}") && this.getNext(st) == ';') {	
							// end of inner block: "}" followed by ";"
							break;
						}
					}
				} else {
					// ignore reserved words and declarations in "<attribute> = <value>;" format
				}
			}
		}
	}
	
	/**
	 * If the current declaration is of type "node" (or "decision" or "utility"), loads
	 * that node (creating new instances using networkBuilder) and adds it to net.
	 * If declaration is not "node" (or "decision" or "utility"), it will not move the
	 * token index from st.
	 * @param st
	 * @param net
	 * @param networkBuilder
	 * @throws IOException
	 * @throws LoadException
	 */
	protected void loadNodeDeclaration (StreamTokenizer st, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder)
										throws IOException , LoadException{
		if (st.sval.equals("node")) {
				Node auxNode = null;
				if (st.sval.equals("node")) {
					auxNode = networkBuilder.getProbabilisticNodeBuilder().buildNode();
				}

				getNext(st);
				auxNode.setName(st.sval);
				getNext(st);
				if (st.sval.equals("{")) {
//					boolean discrete = true;
					getNext(st);
					isDiscreteNode = true;
					this.loadNodeDeclarationBody(st, auxNode, net);
					
				} else {
					throw new LoadException(
						ERROR_NET
							+ " l."
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
							+ resource.getString("LoadException3"));
				}
			}
	}
	
	/**
	 * If the current declaration is "potential", treat that declaration and adds it
	 * to a node contained within net.
	 * If current declaration is not "potential", this method ignores it and remains the
	 * st untouched
	 * @param st
	 * @param net
	 * @throws IOException
	 * @throws LoadException
	 */
	protected void loadParents(StreamTokenizer st, Node node, SingleEntityNetwork net) 
											throws IOException , LoadException {
		
			IRandomVariable auxTableVar = null;
			PotentialTable auxPotentialTable = null;
			
			if (node instanceof IRandomVariable) {
				auxTableVar = (IRandomVariable) node;
				auxPotentialTable = (PotentialTable)auxTableVar.getProbabilityFunction();
				auxPotentialTable.addVariable(node);
			}

			Node parent;
			Edge edge;
			while (getNext(st) != ';') {
				parent = net.getNode(st.sval);
				edge = new Edge(parent, node);
				try {
					net.addEdge(edge);
				} catch (InvalidParentException e) {
					throw new LoadException(e.getMessage());
				}
			}
			
			/*
			 * Invert the parents in the table, to
			 * mantain consistency in the program.
			 * Internal pre-requisite.
			 */
			if (node instanceof IRandomVariable) {
				int sizeVetor = auxPotentialTable.variableCount() / 2;
				for (int k = 1; k <= sizeVetor; k++) {
					Object temp = auxPotentialTable.getVariableAt(k);
					auxPotentialTable.setVariableAt(
						k,
						auxPotentialTable.getVariableAt(
							auxPotentialTable.variableCount() - k));
					auxPotentialTable.setVariableAt(
						auxPotentialTable.variableCount() - k,
						(Node) temp);
				}
			}
			
	}
	
	/**
	 * Reads inside the node declaration and add node to net when necessary.
	 * 			node {[READS THIS CONTENT]}
	 * @param st
	 * @param node: node to be filled
	 * @param net : network where node will be inserted.
	 * @throws IOException
	 */
	protected void loadNodeDeclarationBody(StreamTokenizer st , Node node, SingleEntityNetwork net) throws IOException, LoadException {
		boolean isConstantNode = false;	// if true, this node is a constant (i.e. do not represent random variable)
		while (!st.sval.equals("}")) {
			if (st.sval.equals("title")) {
				getNext(st);
				node.setDescription(st.sval);
				getNext(st);
			} else if (st.sval.equals("discrete")) {
				getNext(st);
				isDiscreteNode = st.sval.equals("TRUE");
				getNext(st);
				// If discrete
			} else if (st.sval.equals("states")) {
				int numStates = 0;
				while (getNext(st) != ';') {
					node.appendState(st.sval);
					numStates++;
				}
				if (numStates <= 0) {
					throw new IOException("Found node with 0 states: " + node);
				}
				if (numStates == 1) {
					// nodes with only 1 state is actually a constant
					isConstantNode = true;
				}
				if (numStates > 1 && isToUseAbsurdState()) {
					// all nodes should have the absurd state
					String absurdState = "absurd";
					// guarantee uniqueness of the name of the state
					if (this.indexOfState(node, absurdState) >= 0) {
						// append a numeric suffix to "absurd" in order to become unique
						for (int i = 0; i < Integer.MAX_VALUE; i++) {
							if (this.indexOfState(node, absurdState + i) < 0) {
								absurdState += i;
								break;
							}
						}
					}
					node.appendState(absurdState);
				}
				// If continuous, but in a discrete way
			} else if (st.sval.equals("levels")) {
				getNext(st);
				if (!isDiscreteNode) {
					String stateBeginValue = st.sval;
					while (getNext(st) != ';') {
						node.appendState(stateBeginValue + "To" + st.sval);
						stateBeginValue = st.sval;
					}
				} else {
					do {
						node.appendState(st.sval);
					} while (getNext(st) != ';');
				}
			} else if (st.sval.equals("parents")) {
				loadParents(st, node, net);
			} else if (st.sval.equals("probs")) {
				loadPotentialDataOrdinal(st, node);
			} else if (st.sval.equals("functable")) {
				loadPotentialDataFuncTable(st, node);
			} else if (st.sval.equals("visual")) {
				getNext(st); // The name of the visual
				getNext(st);
				if (st.sval.equals("{")) {
					getNext(st);
					while (!st.sval.equals("}")) {
						if (st.sval.equals("center")) {
							getNext(st);
							int x = Integer.parseInt(st.sval);
							if (x <= 0) {
								x = Node.getDefaultWidth();
							}
							getNext(st);
							int y = Integer.parseInt(st.sval);
							if (y <= 0) {
								y = Node.getDefaultHeight();
							}
							node.setPosition(x, y);
						}
						getNext(st);
					}
				} else {
					throw new LoadException(
							ERROR_NET
							+ " l."
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
							+ resource.getString("LoadException3"));
				}
			} else if (st.sval.equals("kind")) {
				this.getNext(st);// read "=" and then the kind of node (e.g. CONSTANT, NATURE, etc.)
				isConstantNode = isConstantNode || st.sval.equalsIgnoreCase("CONSTANT");
				this.getNext(st);
			} else {
				// Ignore for now...
				// TODO treat other types of attributes
				getNext(st);
			}
		}
		if (isConstantNode) {
			// set the CPT of the node to 1
			this.setConstantCPT(node);
		}
		net.addNode(node);
	}
	
	/**
	 * Forces the CPT of a node to constant. It assumes that the node has only 1 state
	 * @param node
	 */
	protected void setConstantCPT(Node node) {
		// remove all states if there are more than 1
		while (node.getStatesSize() > 1) {
			Debug.println(getClass(), "Found constant node " + node + " with more than 1 state.");
			node.removeLastState();
		}
		// force the CPT of the node to 1
		if (node instanceof ProbabilisticNode) {
			PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
			for (int i = 0; i < table.tableSize(); i++) {
				table.setValue(i, 1f);
			}
		} 
	}

	/**
	 * Loads potential declaration's content assuming it is declaring
	 * ordinal "stateful" probability declaration
	 * @param st
	 * @param node
	 * @throws LoadException
	 * @throws IOException
	 */
	protected void loadPotentialDataOrdinal(StreamTokenizer st, Node node)
								throws LoadException , IOException {
		
		PotentialTable auxPotentialTable = (PotentialTable)((IRandomVariable)node).getProbabilityFunction();
		
		if (node.getType() == Node.DECISION_NODE_TYPE) {
			throw new LoadException(
				ERROR_NET
					+ " l."
					+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
					+ resource.getString("LoadException4"));
		}

		int nDim = 0;
		
		float probSum = 0f;			// sum of a column in cpt (the sum is usually 1, except for cpts having tags instead of numbers, like "@imposs").
		while (getNext(st) != ';') {
			float value = Float.NaN;	// Float.NaN will represent any unknown value
			if (st.sval.equalsIgnoreCase("imposs")) {
				if (!isToUseAbsurdState()) {
					// cannot represent @Imposs without using the absurd state.
					throw new HasImpossProbDeclarationException(
							ERROR_NET
							+ " l."
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
							+ resource.getString("LoadException4"));
				}
				// @Imposs is represented in unbbayes as absurd with 100% probability and all other states with 0% probability
				value = 0; 
			} else {
				try {
					value = Float.parseFloat(st.sval);
				} catch (NumberFormatException e) {
					// ignore number format
					Debug.println(getClass(), e.getMessage() ,e);
				}
			}
			/*
			 * if isToUseAbsurdState() == true, the following routine should fill the cpt as the following example:
			 * 
			 * CPT in the dmp file (note: t = true, f = false, I = @Imposs):
			 * 
			 * 		t		t		f		f
			 * 		t		f		t		f
			 * 	t	.1		.2		I		.4
			 * 	f	.9		.8		I		.6
			 * 
			 * CPT in UnBBayes (note: "a" represents the "absurd" state):
			 * 
			 * 		t		t		t		f		f		f		a		a		a
			 * 		t		f		a		t		f		a		t		f		a
			 * t	.1		.2		0		0		.4		0		0		0		0
			 * f	.9		.8		0		0		.6		0		0		0		0
			 * a	0		0		1		1		0		1		1		1		1
			 * 
			 * The basic rules for filling the CPT in unbbayes were: 
			 * 	1 - if parent state is "absurd", then the probability of "absurd" is 100% for this node
			 * 	2 - if the dmp is using @imposs as the probability value, then the probability of absurd is 100% for this node
			 */
			// NOTE: we are assuming that all nodes contain at least 1 state other than the absurd
			auxPotentialTable.setValue(
					nDim++,
					value);
			probSum += value;	// update the sum of probability of that column
			
			// if the absurd state is present, then each node has 1 additional state, so we should increment nDim properly
			if (isAbsurdStateLinearCoord(auxPotentialTable, nDim, false)) {
				// only the main node (i.e. auxPotentialTable.getVariableAt(0)) is in absurd state
				// prob of being absurd is 1 - sum of other cells in the same column 
				// if we find @imposs, which is considered to be 0%, absurd becomes 100%
				auxPotentialTable.setValue(
						nDim++,
						1 - probSum);
				probSum = 0f;	// reset probSum
			}
			while (isAbsurdStateLinearCoord(auxPotentialTable, nDim, true)) {
				// this is a column in which a parent is in absurd state. Fill all values with 0 except for the last column (main node := absurd), which will be 1.
				auxPotentialTable.setValue(nDim++,0);	// again, we are assuming that all nodes contain at least 1 state other than the absurd
				if (auxPotentialTable.getMultidimensionalCoord(nDim)[0] == auxPotentialTable.getVariableAt(0).getStatesSize() - 1) {
					// in this column, the main node is in absurd state. This is supposedly the last cell in the column
					auxPotentialTable.setValue(nDim++,1);
				}
			}
		}
	}
	
	/**
	 * Loads potential declaration's content assuming it is declaring
	 * deterministic declaration (i.e. only 1 and 0)
	 * @param st
	 * @param node
	 * @throws LoadException
	 * @throws IOException
	 */
	protected void loadPotentialDataFuncTable(StreamTokenizer st, Node node)
								throws LoadException , IOException {
		
		PotentialTable auxPotentialTable = (PotentialTable)((IRandomVariable)node).getProbabilityFunction();
		
		if (node.getType() == Node.DECISION_NODE_TYPE) {
			throw new LoadException(
				ERROR_NET
					+ " l."
					+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
					+ resource.getString("LoadException4"));
		}

		int cptIndex = 0;
		while (getNext(st) != ';') {
			// extract the index of the state to set to 1
			int indexOfStateToSetTo1 = indexOfState(node, st.sval);
			if (indexOfStateToSetTo1 < 0) {
				throw new IOException(node + " does not have state " + st.sval);
			}
			
			// convert cptIndex to multi-dimensional coordinate (so that we can use indexOfStateToSetTo1 directly)
			int[] multidimensionalCoord = auxPotentialTable.getMultidimensionalCoord(cptIndex);
			
			// make the state of main node (i.e. auxPotentialTable.getVariableAt(0)) to point to indexOfStateToSetTo1
			multidimensionalCoord[0] = indexOfStateToSetTo1;
			
			// set the cell (of the CPT) of that state to 1. Note: by default, all other cells are supposedly 0.
			auxPotentialTable.setValue(auxPotentialTable.getLinearCoord(multidimensionalCoord), 1);
			
			// move multidimensionalCoord to the end of the column, convert to linear coordinate and increment 1, so that we reach first cell of next column.
			multidimensionalCoord[0] = auxPotentialTable.getVariableAt(0).getStatesSize()-1;
			cptIndex = auxPotentialTable.getLinearCoord(multidimensionalCoord) + 1;
			
			// the next iteration will update next column of the cpt
			
			// make sure that columns with the parents in "absurd" state are filled consistently
			while (isAbsurdStateLinearCoord(auxPotentialTable, cptIndex, true)) {
				// this is a column in which a parent is in absurd state. Fill all values with 0 except for the last column (main node := absurd), which will be 1.
				auxPotentialTable.setValue(cptIndex++,0);	// again, we are assuming that all nodes contain at least 1 state other than the absurd
				if (auxPotentialTable.getMultidimensionalCoord(cptIndex)[0] == auxPotentialTable.getVariableAt(0).getStatesSize() - 1) {
					// in this column, the main node is in absurd state. This is supposedly the last cell in the column
					auxPotentialTable.setValue(cptIndex++,1);
				}
			}
		}
	}
	
	/**
	 * This method is used in {@link #loadPotentialDataOrdinal(StreamTokenizer, Node)}
	 * in order to verify if the current cell of the CPT is representing the probability for the
	 * "absurd" state of some variable in the CPT.
	 * @param table : table being analyzed. If null, the method will return false
	 * (there is no way it is in absurd state, if there is no table to analyze).
	 * @param linearCoord : the linear coordinate of the values in table
	 * @param isToIgnoreMainNode : if true, the node {@link PotentialTable#getVariableAt(0)} of the argument "table" will be ignored.
	 * @return true if linearCoord is pointing to any absurd state. The absurd state is always
	 * the last state of a node if {@link #isToUseAbsurdState()} is true.
	 */
	protected boolean isAbsurdStateLinearCoord(PotentialTable table, int linearCoord, boolean isToIgnoreMainNode) {
		// initial assertions
		if (!isToUseAbsurdState()) {
			// automatically false, because the absurd state is supposedly absent if isToUseAbsurdState() == false
			return false;
		}
		if (table == null) {
			// consider that there is no way it is in absurd state, if there is no table to analyze.
			return false;
		}
		
		// turn the linear coordinate into a multi-dimensional coordinate
		int[] multidimensionalCoord = table.getMultidimensionalCoord(linearCoord);

		// if isToIgnoreMainNode == true, start from variable 1 instead of variable 0 (i.e. do not consider the index 0, which is the main node).
		for (int i = (isToIgnoreMainNode?1:0); i < table.getVariablesSize(); i++) {
			// check that multidimensionalCoord of the current variable is pointing to the last state (i.e. absurd state)
			if (multidimensionalCoord[i] == table.getVariableAt(i).getStatesSize()-1) {
				// if at least 1 node is in the absurd state (last state of that node), then return true
				return true;
			}
		}
		// no node in the coordinate is pointing to the last state
		return false;
	}
	
	/**
	 * @return : the index of the state. -1 if not present
	 * @param node : node to look for state
	 * @param state : name of the state to look for
	 */
	private int indexOfState(Node node, String state) {
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase(state)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Fills the PrintStream with net{} header, starting with "net {" declaration and closing with "}"
	 * @param stream: stream to write to
	 * @param net: network to be saved to stream
	 */
	protected void saveNetHeader(PrintStream stream, SingleEntityNetwork net) {
		
		stream.println("net");
		stream.println("{");
		
		this.saveNetHeaderBody(stream, net);
		
		
		stream.println("}");
		stream.println();
		
	}
	
	/**
	 * Stores the content of net{[CONTENT]} header to a stream.
	 * the informations are: 
	 * 			node_size, name, tree, UnBBayes_Color_Probabilistic_Description, 
	 * 			UnBBayes_Color_Probabilistic_Explanation, UnBBayes_Color_Utility,
	 * 			UnBBayes_Color_Decision.
	 * @param stream
	 * @param net
	 */
	protected void saveNetHeaderBody(PrintStream stream, SingleEntityNetwork net) {
		stream.println(
				"     node_size = ("
					+ (int) (net.getRadius() * 2)
					+ " "
					+ (int) (net.getRadius() * 2)
					+ ");");
		stream.println("     name = \"" + net.getName() + "\";");
        stream.println("     UnBBayes_Color_Probabilistic_Description = \"" + ProbabilisticNode.getDescriptionColor().getRGB() + "\";");
        stream.println("     UnBBayes_Color_Probabilistic_Explanation = \"" + ProbabilisticNode.getExplanationColor().getRGB() + "\";");
		stream.println("     UnBBayes_Color_Utility = \"" + UtilityNode.getStaticColor().getRGB() + "\";");
		stream.println("     UnBBayes_Color_Decision = \"" + DecisionNode.getStaticColor().getRGB() + "\";");
	}
	
	
	/**
	 * Writes to a PrintStream a node/decision/utility{} declaration.
	 * 						node|decision|utility [NAME] {[CONTENT]}
	 * @param stream
	 * @param node
	 * @param net
	 */
	protected void saveNodeDeclaration(PrintStream stream, Node node, SingleEntityNetwork net) {
		if (node instanceof ContinuousNode) {
			stream.print("continuous node");
		} else if (node.getType() == Node.PROBABILISTIC_NODE_TYPE) {
			stream.print("node");
		} else if (node.getType() == Node.DECISION_NODE_TYPE) {
			stream.print("decision");
		} else { // TVU
			stream.print("utility");
		}

		stream.println(" " + node.getName());
		stream.println("{");
		
		this.saveNodeDeclarationBody(stream, node, net);
		
		stream.println("}");
		stream.println();
	}
	
	/**
	 * Writes to a PrintStream the body of node/decision/utility{} declaration.
	 * 						node|decision|utility [NAME] {[THIS_CONTENT_IS_WRITTEN]}
	 * @param stream
	 * @param node
	 * @param net
	 */
	protected void saveNodeDeclarationBody(PrintStream stream, Node node, SingleEntityNetwork net) {
			
			this.saveNodeLabelAndPosition(stream, node);
		

			if (node instanceof ContinuousNode) {
				// a continuous node only needs to save label and position				
			} else {
				if (!(node.getType() == Node.UTILITY_NODE_TYPE)) {
					/* Check if the node represents a numeric attribute */
					if (node.getStatesSize() == 0) {
						/* The node represents a numeric attribute */
						double[] mean = node.getMean();
						double[] stdDev = node.getStandardDeviation();
						StringBuffer auxString = new StringBuffer();
						
						/* Mean per class */
						auxString.append("\"" + mean[0] + "\"");
						for (int i = 1; i < mean.length; i++) {
							auxString.append(" \"" + mean[i] + "\"");
						}
						stream.println(
								"     meanPerClass = (" + auxString.toString() + ");");
						
						/* Standard deviation per class */
						auxString = new StringBuffer();
						auxString.append("\"" + stdDev[0] + "\"");
						for (int i = 1; i < mean.length; i++) {
							auxString.append(" \"" + stdDev[i] + "\"");
						}
						stream.println(
								"     stdDevPerClass = (" + auxString.toString() + ");");
					} else {
						/* The node represents a nominal attribute */
						StringBuffer auxString =
							new StringBuffer("\"" + node.getStateAt(0) + "\"");

						int sizeEstados = node.getStatesSize();
						for (int c2 = 1; c2 < sizeEstados; c2++) {
							auxString.append(" \"" + node.getStateAt(c2) + "\"");
						}
						stream.println(
							"     states = (" + auxString.toString() + ");");
					}
				}
				
				if (node.getInformationType() == Node.EXPLANATION_TYPE)
		                        {
		                          String explanationDescription = formatString(node.getExplanationDescription());
		                          stream.println("     %descricao \"" + explanationDescription + "\"");
		                          ArrayMap arrayMap = node.getPhrasesMap();
		                          int size = arrayMap.size();
		                          ArrayList keys = arrayMap.getKeys();
		                          for (int i = 0; i < size; i++)
		                          {
		                            Object key = keys.get(i);
		                            ExplanationPhrase explanationPhrase = (ExplanationPhrase) arrayMap.get(key);
		                            stream.println("     %frase \""+ explanationPhrase.getNode()+ "\" "+ "\""
		                                + explanationPhrase.getEvidenceType()+ "\" "+ "\""
		                                + formatString(explanationPhrase.getPhrase())+ "\"");
		                          }
				}
			}
	}
	
	
	/**
	 * Stores to PrintStream the potential{} declaration
	 * @param stream
	 * @param node
	 * @param net
	 */
	protected void savePotentialDeclaration(PrintStream stream, Node node, SingleEntityNetwork net) {
		ArrayList<Node> auxParentList = node.getParents();

		stream.print("potential (" + node.getName());

		int sizeVa = auxParentList.size();
		if (sizeVa > 0) {
			stream.print(" |");
			for (int c2 = 0; c2 < sizeVa; c2++) {
				Node auxNo2 = (Node) auxParentList.get(c2);
				stream.print(" " + auxNo2.getName());
			}
		}
		
		stream.println(")");
		stream.println("{");
		
		this.savePotentialDeclarationBody(stream, node, net);

		stream.println("}");
		stream.println();
	}
	
	
	/**
	 * Stores to PrintStream the [BODY] of potential {[BODY]} declaration
	 * @param stream
	 * @param node
	 * @param net
	 */
	protected void savePotentialDeclarationBody(PrintStream stream, Node node, SingleEntityNetwork net) {
		if (node instanceof ContinuousNode) {
			// TODO stub!!
			// TODO implement continuous node's potential treatment
			Debug.println(this.getClass(), "TODO implement continuous node's potential treatment: " + node.getName());
			
			ContinuousNode continuous = (ContinuousNode)node;
			stream.print(" data = normal ( ");
			stream.print(continuous.getCnNormalDistribution().getMean(0));
			for (int i = 0; i < continuous.getParents().size(); i++) {
				// TODO implement continuous node's parent treatment. This is stub
				Debug.println(this.getClass(), "TODO implement continuous node's parent treatment: " + continuous.getParents().get(i).getName());
				stream.print(" + " + continuous.getCnNormalDistribution().getConstantAt(i, 0));
				stream.print(" * " + continuous.getParents().get(i).getName());
			}

			stream.print(", " + continuous.getCnNormalDistribution().getVariance(0));
			stream.println(" );");
		
		} else if (node instanceof IRandomVariable) {
			PotentialTable auxTabPot =
				(PotentialTable)((IRandomVariable) node).getProbabilityFunction();
			int sizeVa1 = auxTabPot.variableCount();

			stream.print(" data = ");
			int[] coord;
			boolean[] paren = new boolean[sizeVa1];

			int sizeDados = auxTabPot.tableSize();
			for (int c2 = 0; c2 < sizeDados; c2++) {
				coord = auxTabPot.getMultidimensionalCoord(c2);

				for (int c3 = 0; c3 < sizeVa1; c3++) {
					if ((coord[c3] == 0) && (!paren[c3])) {
						stream.print("(");
						paren[c3] = true;
					}
				}
				stream.print(" " + auxTabPot.getValue(c2));
				if ((c2 % node.getStatesSize())
					== node.getStatesSize() - 1) {
					stream.print(" ");
				}

				int celulas = 1;

				Node auxNo2;
				for (int c3 = 0; c3 < sizeVa1; c3++) {
					auxNo2 = (Node)auxTabPot.getVariableAt(c3);
					celulas *= auxNo2.getStatesSize();
					if (((c2 + 1) % celulas) == 0) {
						stream.print(")");
						if (c3 == sizeVa1 - 1) {
							stream.print(";");
						}
						paren[c3] = false;
					}
				}

				if (((c2 + 1) % node.getStatesSize()) == 0) {
					stream.println();
				}
			}
		}
	}
	
	/**
	 * Stores node's label = "[LABEL]"; and position = ([X], [Y]); declarations 
	 * inside "node" declaration's body
	 * @param stream
	 * @param node
	 */
	protected void saveNodeLabelAndPosition(PrintStream stream, Node node) {
		stream.println(
				"     label = \"" + node.getDescription() + "\";");
		stream.println(
			"     position = ("
				+ (int) node.getPosition().getX()
				+ " "
				+ (int) node.getPosition().getY()
				+ ");");
	}

	/**
	 * @return SUPPORTED_EXTENSIONS_LOAD[0].equalsIgnoreCase(extension) && isLoadOnly;
	 * FIXME fix saving functionality and make this method return true to "DNE" extension for both save and load.
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS[0].equalsIgnoreCase(extension) && isLoadOnly;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return isLoadOnly?SUPPORTED_EXTENSIONS:null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return isLoadOnly?"Netica (.dne)":null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		String fileExtension = null;
		try {
			int index = file.getName().lastIndexOf(".");
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return this.supports(fileExtension, isLoadOnly);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * If true, {@link #loadNodeDeclarationBody(StreamTokenizer, Node, SingleEntityNetwork)}
	 * will automatically add the "absurd" state to all nodes, to consistently
	 * represent impossible probability distribution (e.g. the @Absurd
	 * tag in {@link #loadPotentialDataOrdinal(StreamTokenizer, Node)}).
	 * @param isToUseAbsurdState the isToUseAbsurdState to set
	 */
	public void setToUseAbsurdState(boolean isToUseAbsurdState) {
		this.isToUseAbsurdState = isToUseAbsurdState;
	}

	/**
	 * If true, {@link #loadNodeDeclarationBody(StreamTokenizer, Node, SingleEntityNetwork)}
	 * will automatically add the "absurd" state to all nodes, to consistently
	 * represent impossible probability distribution (e.g. the @Absurd
	 * tag in {@link #loadPotentialDataOrdinal(StreamTokenizer, Node)}).
	 * @return the isToUseAbsurdState
	 */
	public boolean isToUseAbsurdState() {
		return isToUseAbsurdState;
	}

	/**
	 * This is just a subclass of IOException
	 * thrown when {@link DneIO#loadPotentialDataOrdinal(StreamTokenizer, Node)}
	 * detects a @Imposs tag
	 * @author Shou Matsumoto
	 */
	public class HasImpossProbDeclarationException extends IOException {
		private static final long serialVersionUID = 5193370925842884205L;
		public HasImpossProbDeclarationException() {
			super();
		}
		public HasImpossProbDeclarationException(String message, Throwable cause) {
			super(message, cause);
		}
		public HasImpossProbDeclarationException(String message) {
			super(message);
		}
		public HasImpossProbDeclarationException(Throwable cause) {
			super(cause);
		}
	}
}