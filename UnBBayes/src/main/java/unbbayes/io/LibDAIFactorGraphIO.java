package unbbayes.io;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.util.Debug;


/**
 * This class saves a UnBBayes network as a libDAI factor graph
 * (http://cs.ru.nl/~jorism/libDAI/doc/fileformats.html).
 * @author Shou Matsumoto
 *
 */
public class LibDAIFactorGraphIO implements BaseIO {

	/** Single array containing "fg" */
	public static final String[] SUPPORTED_EXTENSIONS = {"fg"};
	private String name = "LibDAI_Factor_Graph";
	
	/**
	 * This class saves a graph as a libDAI factor graph
	 * (http://cs.ru.nl/~jorism/libDAI/doc/fileformats.html).
	 * Default constructor is kept public to allow inheritance and
	 * instantiation as plugin.
	 */
	public LibDAIFactorGraphIO() {}

	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		throw new RuntimeException("Loading of " + getName() +" is not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public synchronized void save(File output, Graph graph) throws IOException {
		// force line separator
		String lineSeparatorBkp = System.getProperty("line.separator");
		if (lineSeparatorBkp == null || !lineSeparatorBkp.equals("\n")) {
			System.setProperty("line.separator", "\n");
		}
		
		PrintStream stream = new PrintStream(new FileOutputStream(output));
		SingleEntityNetwork net = (SingleEntityNetwork) graph;
		
		// assume quantity of factors is the quantity of cpts (i.e. quantity of nodes)
		stream.println(net.getNodeCount());
		
		// start filling factors (each factor is supposedly a cpt)
		for (Node node : net.getNodes()) {
			if (node == null || !(node instanceof ProbabilisticNode)) {
				Debug.println(getClass(), node + " is not a probabilistic node. Ignoring...");
				continue;
			}
			// if this line is reached, n is a probabilistic node
			
			// extract factor (i.e. cpt in a BN)
			PotentialTable cpt = ((ProbabilisticNode) node).getProbabilityFunction();
			if (cpt == null || cpt.getVariablesSize() <= 0) {
				Debug.println(getClass(), node + " has an invalid CPT. Ignoring...");
				continue;
			}
			
			// a factor declaration starts with a line break
			stream.println();
			
			// number of variables in factor (i.e. cpt)
			stream.println(cpt.getVariablesSize());
			
			
			// variables in the factor
			for (int i = 0; i < cpt.getVariablesSize(); i++) {
				// fg seems to expect numbers as node id. Lets use index as id then.
				// TODO check whether we need to reorder nodes, so that nodes with lower indexes are always parents of higher indexes.
				int nodeIndex = net.getNodeIndex(cpt.getVariableAt(i).getName());
				if (i < 0) {
					System.setProperty("line.separator", lineSeparatorBkp);
					throw new IllegalArgumentException("Indexes of nodes in " + net + " are inconsistent.");
				}
				stream.print(nodeIndex);	
				if (i + 1 < cpt.getVariablesSize()) {
					stream.print(" ");
				}
			}
			stream.println();
			
			// number of states of each variables in factor
			for (int i = 0; i < cpt.getVariablesSize(); i++) {
				stream.print(cpt.getVariableAt(i).getStatesSize());	
				if (i + 1 < cpt.getVariablesSize()) {
					stream.print(" ");
				}
			}
			stream.println();
			
			// number of lines from here (to actually declare the factor values - i.e. probs)
			stream.println(cpt.tableSize());
			
			// the actual factors (it is supposedly in order already)
			for (int i = 0; i < cpt.tableSize(); i++) {
				stream.println(i + "\t" + cpt.getValue(i));
			}
		}
		System.setProperty("line.separator", lineSeparatorBkp);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		// loading is not supported
		return !isLoadOnly && file.getName().toUpperCase().endsWith("."+SUPPORTED_EXTENSIONS[0].toUpperCase());
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		if (isLoadOnly) {
			// loading is not supported
			return new String[0];
		}
		return SUPPORTED_EXTENSIONS;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "LibDAI's factor graph (.fg)";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getName()
	 */
	public String getName() {
		return name;
	}
	

}
