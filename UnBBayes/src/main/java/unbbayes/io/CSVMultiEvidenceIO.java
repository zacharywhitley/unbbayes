package unbbayes.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.DefaultStateIntervalParser;
import unbbayes.util.IStateIntervalParser;

/**
 * Reads a csv file
 * with 1st row (header) being names of
 * nodes, and other rows being
 * states of the nodes.
 * Evidences will be stored in 
 * {@link Network#getProperty(String)}
 * with name {@link #MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME}
 * or from {@link #getLastEvidencesRead()}.
 * @author Shou Matsumoto
 */
public class CSVMultiEvidenceIO implements IEvidenceIO {
	
	/**
	 * Name of {@link Network#getProperty(String)} to be used to access a 
	 * List<Map<String, Integer>> representing a list of evidences.
	 * The map contain name of node to state of evidence.
	 * This is a list, because each map in list represent a different scenario
	 * (i.e. there can be nodes in common with incompatible evidences).
	 * @see #loadEvidences(File, Graph)
	 */
	public static final String MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME = CSVMultiEvidenceIO.class.getName() + ".MULTI_EVIDENCE_LIST_MAP";
	
	private IStateIntervalParser stateIntervalParser = new DefaultStateIntervalParser();
	
	public static final String[] SUPPORTED_EXTENSIONS = {"txt", "csv"};
	
	private CSVFormat csvFormat = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
            .withDelimiter('\t');

	private List<Map<String, Integer>> lastEvidencesRead;

	private Map<String, Map<String, Entry<Float, Float>>> stateIntervalCache = new HashMap<String, Map<String,Entry<Float,Float>>>();

	/**
	 * Default constructor kept public for easy extension
	 */
	public CSVMultiEvidenceIO() {}

	/** 
	 * Evidences will be stored in 
	 * {@link Network#getProperty(String)}
	 * with name {@link #MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME}
 	 * or from {@link #getLastEvidencesRead()}.
	 * @see unbbayes.io.IEvidenceIO#loadEvidences(java.io.File, unbbayes.prs.Graph)
	 */
	public void loadEvidences(File file, Graph g) throws IOException {
		
		ProbabilisticNetwork net = (ProbabilisticNetwork) g;
		
		// read evidence file;
		CSVParser csvParser = new CSVParser(new BufferedReader(new FileReader(file)), getCsvFormat());
        
		stateIntervalCache.clear();
		
        lastEvidencesRead = new ArrayList<Map<String, Integer>>();
        for (CSVRecord record : csvParser) {
        	// each row/record is a data entry
        	
        	Map<String, Integer> evidencesInCurrentRow = new HashMap<String, Integer>();
        	
        	// Accessing Values by column header (which is supposedly the name of the node)
        	for (String evidenceNodeName : csvParser.getHeaderMap().keySet()) {
        		
        		// get node
        		Node node = net.getNode(evidenceNodeName);
        		if (!(node instanceof TreeVariable)) {
        	        csvParser.close();
        			throw new RuntimeException(node + " is not a variable that can have evidences.");
        		}
        		
        		int evidenceState = getEvidenceState(record, node);
        		if (evidenceState < 0) {
//        	        csvParser.close();
//        			throw new RuntimeException("In line " + record.getRecordNumber() 
//        					+ ", could not find state " + record.get(evidenceNodeName) 
//        					+ " of node " + evidenceNodeName);
        			// do not sample current node
        			continue;
        		}
        		
        		// insert the evidence
//        		((TreeVariable)node).addFinding(evidenceState);
        		
        		evidencesInCurrentRow.put(evidenceNodeName, evidenceState);
        		
			}
        	
        	lastEvidencesRead.add(evidencesInCurrentRow);
        
        }	// end of loop for CSV}
        
        csvParser.close();
        
        // register evidence set as property
        net.addProperty(MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME, lastEvidencesRead);
        
        // if there is only 1 set of evidences, then simply apply it
        if (lastEvidencesRead.size() == 1) {
        	for (Entry<String, Integer> entry : lastEvidencesRead.get(0).entrySet()) {
        		Node node = net.getNode(entry.getKey());
        		if (node != null
        				&& (node instanceof TreeVariable)
        				&& entry.getValue() >= 0) {
        			((TreeVariable)node).addFinding(entry.getValue());
        		}
			}
        }
        
	}
	

	/**
	 * Will check for state indicated by record.
	 * If no exact mach is found, then it will 
	 * parse the state name as interval 
	 * @param record
	 * @param node
	 * @return -1 if nothing was found or "?" was found
	 */
	protected int getEvidenceState(CSVRecord record, Node node) {
		
		// header of record is supposedly equal to name of node
		String valueInRecord = record.get(node.getName());
		if (valueInRecord != null && valueInRecord.equals("?")) {
			return -1; // this must be sampled
		}
		
		
		float recordAsNumber = Float.NaN;
		try {
			recordAsNumber = Float.parseFloat(valueInRecord);
		} catch (NumberFormatException e) {
			// This is not a number. Simply ignore.
			recordAsNumber = Float.NaN;
		}
		
		if (!Float.isNaN(recordAsNumber) && recordAsNumber < 0) {
//			return -1;
			throw new RuntimeException("Negative values are not supported yet. Found " + valueInRecord + " in column " + record.getRecordNumber());
		}
		
		// search for state that matches with value in record
		int largestState = -1;
		float largestStateUpperValue = -1000000f;
		for (int stateIndex = 0; stateIndex < node.getStatesSize(); stateIndex++) {
			
			// this is the respective state of node
			String stateLabel = node.getStateAt(stateIndex);
			
			// check if exact match
			if (stateLabel.equalsIgnoreCase(valueInRecord)) {
				return stateIndex;
			}
			
			// if not found, try considering state as an interval
			if (!Float.isNaN(recordAsNumber)) {
				
				// check if we can parse the label as an interval
				Entry<Float, Float> interval = null;
				// check cache
				Map<String, Entry<Float, Float>> stateCache = stateIntervalCache.get(node.getName());
				if (stateCache == null) {
					stateCache = new HashMap<String, Map.Entry<Float,Float>>();
				}
				interval = stateCache.get(stateLabel);
				if (!stateCache.containsKey(stateLabel)) {
					interval = getStateIntervalParser().parseLowerUpperBin(stateLabel);
				}
				// update cache
				stateCache.put(stateLabel, interval);
				stateIntervalCache.put(node.getName(), stateCache);
				
				if (interval != null) {
//					if (interval.getKey() <= 0
//							&& interval.getValue() <= 0) {
//						// this is the specia state "zero"\
//						if (recordAsNumber == 0f) {
//							return stateIndex;
//						}
//					}
					if (interval.getKey().floatValue() >= interval.getValue().floatValue()) {
						if (recordAsNumber == interval.getValue().floatValue()) {
							return stateIndex;
						}
					}
					
					// label was an interval. Check if value is within interval
					if (interval.getKey() < recordAsNumber
							&& recordAsNumber <= interval.getValue()) {
						return stateIndex;
					}
					
					// keep track of which bin had the largest numeric value
					if (interval.getValue() > largestStateUpperValue) {
						largestStateUpperValue = interval.getValue();
						largestState = stateIndex;
					}
				}
			}
		}
		
		if (!Float.isNaN(recordAsNumber)) {
			// record was too big. Use largest bin
			return largestState;
		}
		
		throw new RuntimeException("State not found: " + valueInRecord + " in column " + record.getRecordNumber());
//		return -1;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		if (!isLoadOnly) {
			return false;
		}
		return file.getName().endsWith(".txt") || file.getName().endsWith(".csv");
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		if (!isLoadOnly) {
			return null;
		}
		return SUPPORTED_EXTENSIONS;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "Comma or tab separated files (.txt or .csv)";
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getName()
	 */
	public String getName() {
		return "CSV multi-evidence IO";
	}

	/**
	 * @return the csvFormat
	 */
	public CSVFormat getCsvFormat() {
		return csvFormat;
	}

	/**
	 * @param csvFormat the csvFormat to set
	 */
	public void setCsvFormat(CSVFormat csvFormat) {
		this.csvFormat = csvFormat;
	}

	/**
	 * @return 
	 * parser to be used in order to search for
	 * state which matches with a number read from file
	 */
	public IStateIntervalParser getStateIntervalParser() {
		return stateIntervalParser;
	}

	/**
	 * @param stateIntervalParser 
	 * parser to be used in order to search for
	 * state which matches with a number read from file
	 */
	public void setStateIntervalParser(IStateIntervalParser stateIntervalParser) {
		this.stateIntervalParser = stateIntervalParser;
	}

	/**
	 * @return
	 * Evidences read from last call of {@link #loadEvidences(File, Graph)}.
	 * Same evidences can also be accessed from {@link Network#getProperty(String)}
	 * with name {@link #MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME}
	 */
	public List<Map<String, Integer>> getLastEvidencesRead() {
		return lastEvidencesRead;
	}

	/**
	 * @param lastEvidencesRead 
	 * Evidences read from last call of {@link #loadEvidences(File, Graph)}.
	 * Same evidences can also be accessed from {@link Network#getProperty(String)}
	 * with name {@link #MULTI_EVIDENCE_LIST_MAP_PROPERTY_NAME}
	 */
	public void setLastEvidencesRead(List<Map<String, Integer>> lastEvidencesRead) {
		this.lastEvidencesRead = lastEvidencesRead;
	}

	/**
	 * @return the stateIntervalCache
	 */
	public Map<String, Map<String, Entry<Float, Float>>> getStateIntervalCache() {
		return stateIntervalCache;
	}

	/**
	 * @param stateIntervalCache the stateIntervalCache to set
	 */
	public void setStateIntervalCache(Map<String, Map<String, Entry<Float, Float>>> stateIntervalCache) {
		this.stateIntervalCache = stateIntervalCache;
	}

}
