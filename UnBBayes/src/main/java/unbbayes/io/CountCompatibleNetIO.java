package unbbayes.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.id.UtilityNode;

/**
 * This is an extension to {@link NetIO} so that
 * parameters of Dirichlet-Multinomial distribution
 * (counts used for parameter learning)
 * are also properly stored and loaded in files with
 * extension ".net".
 * @author Shou Matsumoto
 * TODO This is a quick and dirty version to be used as a prototype.
 */
public class CountCompatibleNetIO extends NetIO {

	/** Prefix to be used in {@link ProbabilisticNetwork#getProperty(String)} to retrieve/store table of counts. 
	 * The table can be retrieved with this prefix + name of the node */
	public static final String DEFAULT_COUNT_TABLE_PREFIX = "DirichletDistributionParamTable_";

	/**
	 * 
	 */
	public CountCompatibleNetIO () {
		setName("NET (Dirichlet)");
	}
	
	
	/**
	 * Method in superclass is overridden in order to store the count tables instead of
	 * the conditional probability tables.
	 * @see unbbayes.io.NetIO#savePotentialDeclarationBody(java.io.PrintStream, unbbayes.prs.Node, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	@Override
	protected void savePotentialDeclarationBody(PrintStream stream, Node node, SingleEntityNetwork net) {
		
		PotentialTable countTable = (PotentialTable) net.getProperty(DEFAULT_COUNT_TABLE_PREFIX + node.getName());
		if (countTable == null) {
			// if there is no count table to store, then reuse superclass' method
			super.savePotentialDeclarationBody(stream, node, net);
			return;
		}
		
		if (node instanceof ContinuousNode) {
			// just delegate to superclass
			super.savePotentialDeclarationBody(stream, node, net);
			return;
		} else if (node instanceof IRandomVariable) {
			// save the count table instead of probabilities 
			// (probabilities can be retrieved back by normalizing the count table)
			PotentialTable auxTable = countTable;
			
			// the rest of the code is the same of superclass' method
			
			int sizeVa1 = auxTable.variableCount();
			
			// stores whether this node is an utility.
			boolean isUtilityNode = (node instanceof UtilityNode);

			stream.print(" data = ");
			
//			TODO _if _this is an utility node, then we can simply write a space/tab separated list of utility;
//			(it doesnt have to be hierarchical with parenthesis anymore);
			
			int[] coord;
			boolean[] hasParenthesis = new boolean[sizeVa1];

			int dataSize = auxTable.tableSize();
			for (int tableCellIndex = 0; tableCellIndex < dataSize; tableCellIndex++) {	// iterating on each cell of the table
				coord = auxTable.getMultidimensionalCoord(tableCellIndex);

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
					if ((coord[tableVarIndex] == 0) && (!hasParenthesis[tableVarIndex])) {
						stream.print("(");
						hasParenthesis[tableVarIndex] = true;
					}
				}
				stream.print(" " + auxTable.getValue(tableCellIndex));
				if ((tableCellIndex % node.getStatesSize()) == node.getStatesSize() - 1) {
					stream.print(" ");
				}

				int cells = 1;

				Node auxNode2;
				
				tableVarIndex = 0;	// make sure index is reset
				// check if we should close parenthesis when this is an utility node
				if (isUtilityNode 			// this is an utility node
						&& sizeVa1 > 1 ) {	// node has 1 or more parents (sizeVa1 == 1 means no parent: i.e. the table has only 1 node -- the node itself)
					tableVarIndex = 1;		// start index from 1 (ignore the 0-th variable  -- the utility node itself -- when opening the parenthesis)
//					paren[tableVarIndex] = false;	// simply reset flag and don't add parenthesis
				} 
				for (; tableVarIndex < sizeVa1; tableVarIndex++) {
					auxNode2 = (Node)auxTable.getVariableAt(tableVarIndex);
					cells *= auxNode2.getStatesSize();
					// check for condition to close parenthesis
					if (((tableCellIndex + 1) % cells) == 0) {
						// close parenthesis
						stream.print(")");
						if (tableVarIndex == sizeVa1 - 1) {
							stream.print(";");
						}
						hasParenthesis[tableVarIndex] = false;
					}
				}

				if (((tableCellIndex + 1) % node.getStatesSize()) == 0) {
					stream.println();
				}
			}
		}
	}
	
	/**
	 * Method in superclass is overridden in order to load the potential data
	 * to the count tables and then set the conditional probability tables to
	 * the normalized values of count tables.
	 * @see unbbayes.io.NetIO#load(java.io.File, unbbayes.prs.bn.SingleEntityNetwork, unbbayes.prs.builder.IProbabilisticNetworkBuilder)
	 */
	@Override
	protected void load(File input, SingleEntityNetwork net, IProbabilisticNetworkBuilder networkBuilder) throws IOException, LoadException {
		
		// run the same code of superclass (this should fill the potential tables with counts)
		super.load(input, net, networkBuilder);
		
		// prepare component to normalize potential table.
		NormalizeTableFunction normalizer = new NormalizeTableFunction();
		
		// copy the counts to proper place (property of network) and normalize the original potential table (so that they become probabilities)
		for (Node node : net.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				// extract the potential table (it currently stores counts)
				ProbabilisticTable table = (ProbabilisticTable) ((ProbabilisticNode) node).getProbabilityFunction();
				
				// copy count table to proper place
				PotentialTable countTable = (PotentialTable)table.clone();
				net.addProperty(DEFAULT_COUNT_TABLE_PREFIX + node.getName(), countTable);
				
				// convert original table to table of conditional probabilities (by normalization)
				normalizer.applyFunction(table);
			}
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.NetIO#getSupportedFilesDescription(boolean)
	 */
	@Override
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "Net with Dirichlet (.net)";
	}

	

}
