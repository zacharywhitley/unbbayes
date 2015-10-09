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

//by young 
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import unbbayes.gui.HierarchicTree;
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
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.ArrayMap;
import unbbayes.util.Debug;

/**
 * Manipulates input/output of NET files.
 * This implements specification version 2 of .net file specification of Hugin.
 * Therefore, do not make changes in the format and attributes written to the file,
 * or else you may break compatibility with Hugin.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @author Mario Henrique Paes Vieira (mariohpv@bol.com.br)
 * @author Shou Matsumoto
 * @version 2.1
 */
public class NetIO implements BaseIO, IPrintStreamBuilder, IReaderBuilder {

	/** Load resource file from this package */
	private static ResourceBundle resource =
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.resources.IoResources.class.getName());

	private static final String ERROR_NET = resource.getString("errorNet");
	
	// since the StreamTokenizer is not counting linenumber depending on the configuration,
	// let's count it by ourselves
	protected long lineno = 1;
	
	/** Single array containing "net" */
	public static final String[] SUPPORTED_EXTENSIONS = {"net"};
	
	private String name = "NET";
	
	private IPrintStreamBuilder printStreamBuilder = this;
	private IReaderBuilder readerBuilder = this;
	
	private String defaultNodeNamePrefix = "";

	private boolean isToDisableDirection = false;
	
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
	public Graph load(File input)
		throws LoadException, IOException {
		
		String id = input.getName();
		int index = input.getName().lastIndexOf('.');
		if (index > 0) {
			id = id.substring(0, index);
		}
		
		// create network using default builder
		IProbabilisticNetworkBuilder networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
		
		ProbabilisticNetwork net = networkBuilder.buildNetwork(id);
		
		load(input, net, networkBuilder);
		
		if (isToDisableDirection()) {
			for (Edge edge : net.getEdges()) {
				edge.setDirection(false);
			}
		}
		return net;		
	}
	
	/**
	 *  Loads a NET format file using network builder.
	 *  It calls {@link #load(File, SingleEntityNetwork, IProbabilisticNetworkBuilder)} internally.
	 * @see IProbabilisticNetworkBuilder
	 * @param  input  file to be read.
	 * @param  networkBuilder: builder to be used in order to generate expected instances
	 * of probabilistic network, probabilistic nodes, decision nodes and utility nodes. This
	 * is useful if you want to reuse NetIO for networks/nodes which extends ProbabilisticNetwork,
	 * ProbabilisticNode, DecisionNode and UtilityNode (or else NetIO will be bound to those 
	 * superclasses only).
	 * 
	 * @return loaded net.
	 * @throws LoadException when there were errors loading the network
	 * @throws IOException in case there were errors when manipulating files.
	 * @see #getReaderBuilder()
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
	 * If you need to save it to a stream other than
	 * a file, then change {@link #setPrintStreamBuilder(IPrintStreamBuilder)}
	 * @param  output file where the net should be saved.
	 * @param graph network to be saved.
	 * @see #getPrintStreamBuilder()
	 */
	public void save(File output, Graph graph) throws FileNotFoundException {
		if (output.getName().lastIndexOf(".") < 0) {
			String name = output.getPath() + "."+SUPPORTED_EXTENSIONS[0];
			output = new File(name);
		}
		
		PrintStream stream = getPrintStreamBuilder().getPrintStreamFromFile(output);
		
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
	
	/**
	 * Loads a network from file, specifying what instance of {@link SingleEntityNetwork} to fill,
	 * and what instance of {@link IProbabilisticNetworkBuilder} to use in order
	 * to instantiate network components (e.g. nodes).
	 * If you need to load network from tokenizable streams that are not files, then 
	 * change {@link #setReaderBuilder(IReaderBuilder)}. 
	 * @param input
	 * @param net
	 * @param networkBuilder
	 * @throws IOException
	 * @throws LoadException
	 * @see #getReaderBuilder()
	 */
	protected void load(File input, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) 
				throws IOException, LoadException {
		
		Reader reader = this.getReaderBuilder().getReaderFromFile(input);
		StreamTokenizer st = new StreamTokenizer(reader);
		
		this.setUpStreamTokenizer(st);

		// treat header
		
		getNext(st);
		if (st.sval.equals("net")) {
			this.loadNetHeader(st, net);
		} else {
			throw new LoadException(
				ERROR_NET + resource.getString("LoadException"));
		}

		// start treating body
		while (getNext(st) != StreamTokenizer.TT_EOF) {
			
			// if declaration is "node" type, treat it
			this.loadNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "continuous node" type, treat it
			this.loadContinuousNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "potential" type, treat it
			this.loadPotentialDeclaration(st, net);
			
			// ignore other declarations
		}
		
		reader.close();
		
		this.setUpHierarchicTree(net);
	}	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.IReaderBuilder#getReaderFromFile(java.io.File)
	 */
	public Reader getReaderFromFile(File file)  throws FileNotFoundException {
		return (new BufferedReader(new FileReader(file)));
	}

	protected int getNext(StreamTokenizer st) throws IOException {
		do {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_EOL) {
				this.lineno++;
			}
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}

	protected void loadHierarchicTree(
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
				while ((c != '(') && (c != ')') && (c != ',') && (i < sb.length())) {
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

	protected String saveHierarchicTree(HierarchicTree hierarchicTree) {
		if (hierarchicTree == null) {
			return null;
		}
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

	protected void processTreeNode(
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
	 * By setting up using protected method, it becomes easier to extend this class.
	 * The {@link StreamTokenizer} is instantiated in {@link #load(File, SingleEntityNetwork, IProbabilisticNetworkBuilder)},
	 * and such method is likely to be using {@link #getReaderBuilder()} in order to instantiate
	 * the {@link StreamTokenizer}.
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
		st.eolIsSignificant(false);
		st.quoteChar('"');
		//st.commentChar('%');
		
	}

	/**
	 * Start loading net{} header from .net specification file; iterating under 
	 * {@link NetIO#loadNetHeaderBody(StreamTokenizer, SingleEntityNetwork)}
	 * in order to treat each declaration.
	 * {@link NetIO#loadNetHeaderBody(StreamTokenizer, SingleEntityNetwork)}
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
			//by young
			//UtilityNode.setColor(Integer.parseInt(st.sval));
			Integer.parseInt(st.sval);
			//by young end
		} else if (st.sval.equals("UnBBayes_Color_Decision")) {
			getNext(st);
			//by young
			//DecisionNode.setColor(Integer.parseInt(st.sval));
			Integer.parseInt(st.sval);
			//by young end
		} else if (st.sval.equals("UnBBayes_Color_Probabilistic_Description")) {
            getNext(st);
            //by young
            Integer.parseInt(st.sval);
            //ProbabilisticNode.setDescriptionColor(Integer.parseInt(st.sval));
            //by young end
        } else if (st.sval.equals("UnBBayes_Color_Probabilistic_Explanation")) {
            getNext(st);
            //by young
            Integer.parseInt(st.sval);
            //ProbabilisticNode.setExplanationColor(Integer.parseInt(st.sval));
            //by young end
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
		if (st.sval.equals("node")
				|| st.sval.equals("decision")
				|| st.sval.equals("utility")) {
				Node auxNode = null;
				if (st.sval.equals("node")) {
//					auxNo = new ProbabilisticNode();
					auxNode = networkBuilder.getProbabilisticNodeBuilder().buildNode();
				} else if (st.sval.equals("decision")) {
//					auxNo = new DecisionNode();
					auxNode = networkBuilder.getDecisionNodeBuilder().buildNode();
				} else { // utility
//					auxNo = new UtilityNode();
					auxNode = networkBuilder.getUtilityNodeBuilder().buildNode();
				}

				getNext(st);
				auxNode.setName(st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval);
				getNext(st);
				if (st.sval.equals("{")) {
					getNext(st);
					while (!st.sval.equals("}")) {
						this.loadNodeDeclarationBody(st, auxNode);
					}
					net.addNode(auxNode);
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
	 * If the current declaration is of type "continuous node", loads
	 * that node (creating new instances using networkBuilder) and adds it to net.
	 * If declaration is not "continuous node", it will not move the
	 * token index from st.
	 * @param st
	 * @param net
	 * @param networkBuilder
	 * @throws IOException
	 * @throws LoadException
	 * @deprecated Continuous node is no longer supported in UnBBayes core. It has 
	 * now been replaced by the CPS plugin available at http://sourceforge.net/projects/prognos/.
	 */
	protected void loadContinuousNodeDeclaration (StreamTokenizer st, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder)
										throws IOException , LoadException{
		if (st.sval.equals("continuous")) {
			this.getNext(st);
			if (st.sval.equals("node")) {
				Node continuousNode = networkBuilder.getContinuousNodeBuilder().buildNode();;
				
				getNext(st);
				continuousNode.setName(st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval);
				getNext(st);
				if (st.sval.equals("{")) {
					getNext(st);
					while (!st.sval.equals("}")) {
						this.loadNodeDeclarationBody(st, continuousNode);
					}
					net.addNode(continuousNode);
				} else {
					throw new LoadException(
						ERROR_NET
							+ " l."
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
							+ resource.getString("LoadException3"));
				}
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
	protected void loadPotentialDeclaration(StreamTokenizer st, SingleEntityNetwork net) 
											throws IOException , LoadException {
		
		if (st.sval.equals("potential")) {
			
			IRandomVariable auxTableVar = null;
			PotentialTable auxPotentialTable = null;
			
			getNext(st);	// node name
			Node auxNode1 = net.getNode(st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval);

			if (auxNode1 instanceof IRandomVariable) {
				auxTableVar = (IRandomVariable) auxNode1;
				// TODO find a way to implement without assumption of potential table
				auxPotentialTable = (PotentialTable)auxTableVar.getProbabilityFunction();
				auxPotentialTable.addVariable(auxNode1);
			}

			getNext(st);
			if (st.sval.equals("|")) {
				getNext(st);	// parent names
			}

			Node auxNo2;
			Edge auxArco;
			while (!st.sval.startsWith("{")) {
				auxNo2 = net.getNode(st.sval.startsWith(getDefaultNodeNamePrefix())?st.sval.substring(getDefaultNodeNamePrefix().length()):st.sval);
				auxArco = new Edge(auxNo2, auxNode1);
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
			if (auxNode1 instanceof IRandomVariable) {
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
			
			if (st.sval.length() == 1) {
				getNext(st);
			}
			
			if (st.sval.startsWith("%")) {
				// ignore other types of comments which do not have special meaning
				readTillEOL(st);
				getNext(st);
			}
			
			if (st.sval.endsWith("}")) {
				// there were nothing declared
				Debug.println(this.getClass(), "Empty potential declaration found for " + auxNode1.getName());
			}

			while (!st.sval.endsWith("}")) {
				if (st.sval.startsWith("%")) {
					// ignore other types of comments which do not have special meaning
					readTillEOL(st);
					getNext(st);
				}
				if (st.sval.equals("data")) {
					getNext(st);	// extract "normal"
					if (st.sval.equals("normal")) {
						// this is a continuous node
						this.loadPotentialDataContinuous(st, auxNode1);
					} else {
						// this is a ordinal node
						this.loadPotentialDataOrdinal(st, auxNode1);
					}
					
				} else {
					throw new LoadException(
						ERROR_NET
							+ " l."
							+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
							+ resource.getString("LoadException5"));
				}
			}
		}
	}
	
	
	/**
	 * Sets up the hierarchic tree after the network is completely loaded
	 * @param net
	 */
	protected void setUpHierarchicTree(SingleEntityNetwork net) {
		HierarchicTree tree = net.getHierarchicTree();
		if (tree != null) {
			tree.setProbabilisticNetwork(net,HierarchicTree.DESCRIPTION_TYPE);
		}
	}
	
	
	/**
	 * Reads inside the node declaration.
	 * 			node {[READS THIS CONTENT]}
	 * @param st
	 * @param node: node to be filled
	 * @throws IOException
	 */
	protected void loadNodeDeclarationBody(StreamTokenizer st , Node node) throws IOException, LoadException {
		if (st.sval.equals("label")) {
			getNext(st);
			node.setDescription(st.sval);
			getNext(st);
		} else if (st.sval.equals("position")) {
			getNext(st);
			int x = Integer.parseInt(st.sval);
			getNext(st);
			if (x <= 0) {
				//by young
				x = node.getWidth();
				//by young end
			}
			int y = Integer.parseInt(st.sval);
			if (y <= 0) {
				//by young
				y = node.getHeight();
				//by young end
			}
			
//			node.getPosition().setLocation(x, y);
			node.setPosition(x, y);
			
			getNext(st);
		}  //by young
		else if (st.sval.equals("color")) {
			getNext(st);
			int c = Integer.parseInt(st.sval);
		 
			node.setColor(new Color(c));
			
			getNext(st);
		} //by young end
		else if (st.sval.equals("states")) {
			while (getNext(st) == '"') {
				node.appendState(st.sval);
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
			node.setMean(mean);
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
			node.setStandardDeviation(stdDev);
		} else if (st.sval.equals("%descricao")) {
			getNext(st);
			node.setExplanationDescription(
				unformatString(st.sval));
			node.setInformationType(Node.EXPLANATION_TYPE);
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
						+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
						+ resource.getString("LoadException2")
						+ st.sval);
			}
			getNext(st);
			explanationPhrase.setPhrase(
				unformatString(st.sval));
			node.addExplanationPhrase(explanationPhrase);
			readTillEOL(st);
			getNext(st);
		} else if (st.sval.contains("HR_")) {
			// this is a HUGIN specific declaration.
			// let's ignore it
			Debug.println(this.getClass(), "Ignoring HR declaration: " + st.sval);
			while (getNext(st) == '"');
		} else if (st.sval.startsWith("%")) {
			// ignore other types of comments which do not have special meaning
			readTillEOL(st);
			getNext(st);
		} else {
//			throw new LoadException(
//				ERROR_NET
//					+ " l."
//					+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
//					+ resource.getString("LoadException2")
//					+ st.sval);
			// instead of throwing exception, try to ignore it
			System.err.println(
					ERROR_NET
					+ " l."
					+ ((st.lineno() < this.lineno)?this.lineno:st.lineno())
					+ resource.getString("LoadException2")
					+ st.sval);
			while (getNext(st) == '"');
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
//		this.getNext(st);
		int nDim = 0;
		while (!st.sval.equals("}")) {
			if (st.sval.equals("%")) {
				readTillEOL(st);
			} else {
				auxPotentialTable.setValue(
					nDim++,
					Float.parseFloat(st.sval));
			}
			getNext(st);
		}
	}
	
	/**
	 * Loads potential declaration assuming it is declaring continuous distribution
	 * @param st
	 * @param node
	 */
	protected void loadPotentialDataContinuous (StreamTokenizer st, Node node)
												throws LoadException , IOException {
		// TODO finish implementing this
		Debug.println(this.getClass(), "Continuous potential loading is not implemented yet: " + node.getName());
		while (!st.sval.equals("}")) {
			// just skip it...
			getNext(st);
		}
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
		String tree = saveHierarchicTree(net.getHierarchicTree());
		if (tree != null)
			stream.println("     tree = \"" + tree + "\";");
        stream.println("     UnBBayes_Color_Probabilistic_Description = \"" + ProbabilisticNode.getDescriptionColor().getRGB() + "\";");
        stream.println("     UnBBayes_Color_Probabilistic_Explanation = \"" + ProbabilisticNode.getExplanationColor().getRGB() + "\";");
		
        //by young
//      stream.println("     UnBBayes_Color_Utility = \"" + "1" + "\";");
//		stream.println("     UnBBayes_Color_Decision = \"" + "1" + "\";");
        //stream.println("     UnBBayes_Color_Utility = \"" + UtilityNode.getColor().getRGB() + "\";");
		//stream.println("     UnBBayes_Color_Decision = \"" + DecisionNode.getColor().getRGB() + "\";");
		//by young end
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

		stream.println(" " + getDefaultNodeNamePrefix() + node.getName());
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

		stream.print("potential (" + getDefaultNodeNamePrefix() + node.getName());

		int sizeVa = auxParentList.size();
		if (sizeVa > 0) {
			stream.print(" |");
			for (int c2 = 0; c2 < sizeVa; c2++) {
				Node auxNo2 = null;
				// If this is a random variable, the order of the parents 
				// has to follow (be consistent) the order in the CPT
				if (node instanceof IRandomVariable) {
					PotentialTable auxTabPot =
						(PotentialTable)((IRandomVariable) node).getProbabilityFunction();
					// The order of the parents in the CPT is the inverse of the order it is added
					auxNo2 = (Node) auxTabPot.getVariableAt(sizeVa - c2);
				} else {
					auxNo2 = (Node) auxParentList.get(c2);
				}
				stream.print(" " + getDefaultNodeNamePrefix() + auxNo2.getName());
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
			Debug.println(this.getClass(), "TODO implement continuous node's potential treatment: " + node);
			
			ContinuousNode continuous = (ContinuousNode)node;
			stream.print(" data = normal ( ");
			stream.print(continuous.getCnNormalDistribution().getMean(0));
			for (int i = 0; i < continuous.getParents().size(); i++) {
				// TODO implement continuous node's parent treatment. This is stub
				try {
					Debug.println(this.getClass(), "TODO implement continuous node's parent treatment: " + continuous.getParents().get(i).getName());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				stream.print(" + " + continuous.getCnNormalDistribution().getConstantAt(i, 0));
				stream.print(" * " + getDefaultNodeNamePrefix() + continuous.getParents().get(i).getName());
			}

			stream.print(", " + continuous.getCnNormalDistribution().getVariance(0));
			stream.println(" );");
		
		} else if (node instanceof IRandomVariable) {
			PotentialTable auxTabPot =
				(PotentialTable)((IRandomVariable) node).getProbabilityFunction();
			int sizeVa1 = auxTabPot.variableCount();
			
			// stores whether this node is an utility.
			boolean isUtilityNode = (node instanceof UtilityNode);

			stream.print(" data = ");
			
//			TODO _if _this is an utility node, then we can simply write a space/tab separated list of utility;
//			(it doesnt have to be hierarchical with parenthesis anymore);
			
			int[] coord;
			boolean[] paren = new boolean[sizeVa1];

			int sizeDados = auxTabPot.tableSize();
			for (int tableCellIndex = 0; tableCellIndex < sizeDados; tableCellIndex++) {	// iterating on each cell of the table
				coord = auxTabPot.getMultidimensionalCoord(tableCellIndex);

				int tableVarIndex = 0;		// index to be used to iterate on variables in the table
				if (isUtilityNode 			// this is an utility node
						&& sizeVa1 > 1 ) {	// node has 1 or more parents (sizeVa1 == 1 means no parent: i.e. the table has only 1 node -- the node itself)
					// if this is an utility node with 1 or more parents, then utility node has only 1 state, but the table has more than 1 cell.
					// if we add parenthesis in this scenario, each number will come between parenthesis, like "(((100) (50)) ((10) (-10))))"
					// Since Hugin cannot open such specification, this should be converted to "((100 50) (10 -10)))"
					tableVarIndex = 1;		// start index from 1 (ignore the 0-th variable  -- the utility node itself -- when opening the parenthesis)
//					paren[0] = true;		// but mark utility node as visited, for compatibility
				}
				// include open parenthesis
				for (; tableVarIndex < sizeVa1; tableVarIndex++) {
					if ((coord[tableVarIndex] == 0) && (!paren[tableVarIndex])) {
						stream.print("(");
						paren[tableVarIndex] = true;
					}
				}
				stream.print(" " + auxTabPot.getValue(tableCellIndex));
				if ((tableCellIndex % node.getStatesSize())
					== node.getStatesSize() - 1) {
					stream.print(" ");
				}

				int celulas = 1;

				Node auxNo2;
				
				tableVarIndex = 0;	// make sure index is reset
				// check if we should close parenthesis when this is an utility node
				if (isUtilityNode 			// this is an utility node
						&& sizeVa1 > 1 ) {	// node has 1 or more parents (sizeVa1 == 1 means no parent: i.e. the table has only 1 node -- the node itself)
					tableVarIndex = 1;		// start index from 1 (ignore the 0-th variable  -- the utility node itself -- when opening the parenthesis)
//					paren[tableVarIndex] = false;	// simply reset flag and don't add parenthesis
				} 
				for (; tableVarIndex < sizeVa1; tableVarIndex++) {
					auxNo2 = (Node)auxTabPot.getVariableAt(tableVarIndex);
					celulas *= auxNo2.getStatesSize();
					// check for condition to close parenthesis
					if (((tableCellIndex + 1) % celulas) == 0) {
						// close parenthesis
						stream.print(")");
						if (tableVarIndex == sizeVa1 - 1) {
							stream.print(";");
						}
						paren[tableVarIndex] = false;
					}
				}

				if (((tableCellIndex + 1) % node.getStatesSize()) == 0) {
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
		
		// the following code was generating an invalid file (hugin cannot open it)
		//by young
//		stream.println(
//				"     color = ("
//					+ (int)node.getColor().getRGB()
//					+ ");");
		//by young end
	}

	/**
	 * Checks if file extension is compatible to what this i/o expects.
	 * @see #supports(File, boolean)
	 * @param extension
	 * @param isLoadOnly
	 * @return
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS[0].equalsIgnoreCase(extension);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "Net (.net)";
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		String fileExtension = null;
		try {
			if (file.isDirectory()) {
				// do not support directory
				return false;
			}
			int index = file.getName().lastIndexOf(".");
			if (!isLoadOnly && index < 0) {
				// force this to support saving files with no extension
				return true;
			}
			if (index > 0) {
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File)
	 */
	public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(file));
	}

	/**
	 * This is used in {@link #save(File, Graph)}
	 * in order to generate an instance of {@link PrintStream}
	 * to be used in order to write to a {@link File}.
	 * You may use {@link StringPrintStreamBuilder}
	 * in order to write to a {@link String} instead
	 * of a {@link File}.
	 * @return the printStreamBuilder
	 * @see #getPrintStreamFromFile(File)
	 * @see StringPrintStreamBuilder
	 */
	public IPrintStreamBuilder getPrintStreamBuilder() {
		return printStreamBuilder;
	}

	/**
	 * This is used in {@link #save(File, Graph)}
	 * in order to generate an instance of {@link PrintStream}
	 * to be used in order to write to a {@link File}
	 * You may use {@link StringPrintStreamBuilder}
	 * in order to write to a {@link String} instead
	 * of a {@link File}.
	 * @param printStreamBuilder the printStreamBuilder to set
	 * @see #getPrintStreamFromFile(File)
	 * @see StringPrintStreamBuilder
	 */
	public void setPrintStreamBuilder(IPrintStreamBuilder printStreamBuilder) {
		this.printStreamBuilder = printStreamBuilder;
	}

	/**
	 * This is used in {@link #load(File, SingleEntityNetwork, IProbabilisticNetworkBuilder)} in order
	 * to generate a {@link Reader} to be used to read a stream
	 * and create new instance of network.
	 * You may want to use {@link StringReaderBuilder}
	 * if you want to read from {@link String} instead of 
	 * from a {@link File}.
	 * @return the readerBuilder
	 * @see #getReaderFromFile(File)
	 * @see StringReaderBuilder
	 */
	public IReaderBuilder getReaderBuilder() {
		return readerBuilder;
	}

	/**
	 * This is used in {@link #load(File, SingleEntityNetwork, IProbabilisticNetworkBuilder)} in order
	 * to generate a {@link Reader} to be used to read a stream
	 * and create new instance of network;
	 * You may want to use {@link StringReaderBuilder}
	 * if you want to read from {@link String} instead of 
	 * from a {@link File}.
	 * @param readerBuilder the readerBuilder to set
	 * @see #getReaderFromFile(File)
	 * @see StringReaderBuilder
	 */
	public void setReaderBuilder(IReaderBuilder readerBuilder) {
		this.readerBuilder = readerBuilder;
	}

	/**
	 * This prefix will be automatically inserted to node names
	 * if the node names do not start from a letter.
	 * @return the defaultNodeNamePrefix
	 */
	public String getDefaultNodeNamePrefix() {
		return defaultNodeNamePrefix;
	}

	/**
	 * This prefix will be automatically inserted to node names
	 * if the node names do not start from a letter.
	 * @param defaultNodeNamePrefix the defaultNodeNamePrefix to set
	 */
	public void setDefaultNodeNamePrefix(String defaultNodeNamePrefix) {
		this.defaultNodeNamePrefix = defaultNodeNamePrefix;
	}

	/**
	 * @return the isToDisableDirection : if set to true, then {@link #load(File)} will set {@link Edge#setDirection(boolean)} to false for all
	 * arcs in the network being returned. This is false by default.
	 */
	public boolean isToDisableDirection() {
		return isToDisableDirection;
	}

	/**
	 * @param isToDisableDirection the isToDisableDirection to set: if set to true, then {@link #load(File)} will set {@link Edge#setDirection(boolean)} to false for all
	 * arcs in the network being returned. This is false by default.
	 */
	public void setToDisableDirection(boolean isToDisableDirection) {
		this.isToDisableDirection = isToDisableDirection;
	}
	
}