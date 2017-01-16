/**
 * 
 */
package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.io.NetIO;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.util.Debug;

/**
 * Reads a csv file and fills a hugin net file.
 * @author Shou Matsumoto
 *
 */
public class NetFileFiller extends CSVJointDistributionReader implements INetFileFiller {

	private NetIO netIO;
	private boolean isToTreatRootNodes = false;

	/**
	 * Default constructor
	 */
	public NetFileFiller() {
		this.setToAdd1ToCounts(true);
		this.setIdColumn(Integer.MIN_VALUE);
	}

	/*
	 * (non-Javadoc)
	 * @see io.INetFileFiller#fillNetFile(java.io.File, java.io.File)
	 */
	public void fillNetFile(File input, File netFile) throws IOException {
		
		// extract the object to be used to read/write network file
		NetIO netIO = this.getNetIO();
		
		// load the network structure from net file
		Graph graph = netIO.load(netFile);
		
		// this will be used to normalize conditional probabilities
		NormalizeTableFunction conditionalNormalizer = new NormalizeTableFunction();
		
		// read nodes and get conditional probability tables to fill;
		for (Node node : graph.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
				
				// if we should consider root nodes, then treat all nodes. If not, then only treat nodes with parents
				if (isToTreatRootNodes() || table.getVariablesSize() > 1) {	// tables with more than 1 variable mean they have parents
					// fill table with data;
					if (this.fillJointDist(table, new FileInputStream(input), false)) {	// we don't need to normalize at this point
						// Data is joint distribution/counts. Convert joint counts to conditional probabilities;
						conditionalNormalizer.applyFunction((ProbabilisticTable) table);
					} else {
						throw new IOException("Unable to fill table " + table);
					}
				}
			}
		}
		
		// overwrite network file
		netIO.save(netFile, graph);
	}
	
	/**
	 * @return the netIO
	 */
	public NetIO getNetIO() {
		if (netIO == null) {
			netIO = new NetIO();
		}
		return netIO;
	}

	/**
	 * @param netIO the netIO to set
	 */
	public void setNetIO(NetIO netIO) {
		this.netIO = netIO;
	}

	/**
	 * @return the isToTreatRootNodes
	 */
	public boolean isToTreatRootNodes() {
		return isToTreatRootNodes;
	}

	/**
	 * @param isToTreatRootNodes the isToTreatRootNodes to set
	 */
	public void setToTreatRootNodes(boolean isToTreatRootNodes) {
		this.isToTreatRootNodes = isToTreatRootNodes;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("i","input", true, "CSV file to get joint distribution from.");
		options.addOption("o","output", true, "Network file (.net extension) to overwrite with conditional probabilities.");
		options.addOption("d","debug", false, "Enables debug mode.");
		
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		if (cmd == null) {
			System.err.println("Invalid command line");
			return;
		}
		
		if (cmd.hasOption("h")) {
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		Debug.setDebug(cmd.hasOption("d"));
		
		
		File dataInput = null;
		if (cmd.hasOption("i")) {
			dataInput = (new File(cmd.getOptionValue("i")));
		} else {
			dataInput = (new File("selectedUserDetectorIndicatorThreat.csv"));
		}
		
		File netFile = null;
		if (cmd.hasOption("o")) {
			netFile = new File(cmd.getOptionValue("o"));
		} else {
			netFile = new File("conditionals.net");
		}
		
		
		new NetFileFiller().fillNetFile(dataInput, netFile);
		
	}

}
