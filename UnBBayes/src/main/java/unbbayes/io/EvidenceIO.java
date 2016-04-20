/**
 * 
 */
package unbbayes.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;

/**
 * Default implementation of {@link IEvidenceIO}
 * @author Shou Matsumoto
 *
 */
public class EvidenceIO implements IEvidenceIO {

	private int stateOffset = 1;

	/**
	 * Default constructor is kept public in order to allow plug-ins
	 */
	public EvidenceIO() {}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#loadEvidences(java.io.File, unbbayes.prs.Graph)
	 */
	public void loadEvidences(File file, Graph graph) throws IOException {
		
		// initial assertions
		if (file == null || !file.isFile()) {
			throw new IllegalArgumentException(file.getName() + " is not a file.");
		}
		
		if ((graph == null)
				|| !(graph instanceof ProbabilisticNetwork)) {
			throw new UnsupportedOperationException("Current version can only handle evidences for probabilistic networks. Name of network = " + graph);
		}
		ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
		
		// set up file tokenizer
		StreamTokenizer st = new StreamTokenizer(new BufferedReader(new FileReader(file)));
		st.wordChars('A', 'z');
		st.wordChars('0', '9');
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.whitespaceChars(':',':');	// jumps separators
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.quoteChar('"');
		st.quoteChar('\'');		
		st.eolIsSignificant(true);	// declaration must be in same line
		st.commentChar('#');

		// read the file
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// read the left value (name of the node)
			String nodeName = st.sval;
			Node node = net.getNode(nodeName);
			if (node == null) {
				// go to next line
				while (st.nextToken() != st.TT_EOL){};
				continue;
			}
			
			// read the right value (state of the node)
			st.nextToken();
			int stateIndex = (int)st.nval;
			if (st.ttype == st.TT_WORD) {
				String state = st.sval;
				// check if we can parse the state to number.
				try {
					stateIndex = Integer.parseInt(state);
				} catch (NumberFormatException e) {
					// we cannot parse. Search for state
					stateIndex = -1;
					for (int i = 0; i < node.getStatesSize(); i++) {
						if (node.getStateAt(i).equals(state)) {
							stateIndex = i+getStateOffset();
							break;
						}
					}
				}
			}
			if (node instanceof TreeVariable) {
				TreeVariable probNode = (TreeVariable) node;
				probNode.addFinding(stateIndex-getStateOffset());
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		if (file == null) {
			return false;
		}
		if (file.exists() && file.isFile()) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		String[] ret = {"in", "txt"};
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "Evidences in format \"NAME: value\"";
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.IEvidenceIO#getName()
	 */
	public String getName() {
		return "Evidences";
	}

	/**
	 * @return the stateOffset : states will start from this index
	 */
	public int getStateOffset() {
		return stateOffset;
	}

	/**
	 * @param stateOffset the stateOffset to set : states will start from this index
	 */
	public void setInitialIndex(int stateOffset) {
		this.stateOffset = stateOffset;
	}

}
