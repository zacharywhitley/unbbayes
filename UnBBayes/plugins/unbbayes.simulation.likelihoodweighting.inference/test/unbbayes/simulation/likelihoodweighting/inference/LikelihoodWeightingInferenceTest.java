/**
 * 
 */
package unbbayes.simulation.likelihoodweighting.inference;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import junit.framework.TestCase;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;

/**
 * @author Shou Matsumoto
 *
 */
public class LikelihoodWeightingInferenceTest extends TestCase {

	private LikelihoodWeightingInference likelihoodWeightingInference = null;
	private int sampleSize = 100000;
	private String netFileName = "src/test/resources/testCases/asia.net";
	ProbabilisticNetwork probabilisticNetwork = null;
	
	
	protected ProbabilisticNetwork loadNetwork(String netFileName) {
		File netFile = new File(netFileName);
		String fileExt = netFileName.substring(netFileName.length() - 3);
		

		try {
			BaseIO io = null;
			if (fileExt.equalsIgnoreCase("xml")) {
				io = new XMLBIFIO();
			} else if (fileExt.equalsIgnoreCase("net")) {
				io = new NetIO();
			} else {
				fail("The network must be in XMLBIF 0.4 or NET format!");
			}
			return (ProbabilisticNetwork)io.load(netFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.setProbabilisticNetwork(this.loadNetwork(this.getNetFileName()));
		this.likelihoodWeightingInference = new LikelihoodWeightingInference(
				this.getProbabilisticNetwork(), this.getSampleSize());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link unbbayes.simulation.likelihoodweighting.inference.LikelihoodWeightingInference#main(java.lang.String[])}.
	 * TODO actually test the values automatically.
	 */
	public final void testMain() {
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);
		
		
		
		LikelihoodWeightingInference lw = this.getLikelihoodWeightingInference();
		
		lw.run();
		
		ProbabilisticNetwork pn = this.getProbabilisticNetwork();
		
		for (Node node : pn.getNodes()) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println("	" + node.getStateAt(i) + ": " + nf.format(((TreeVariable)node).getMarginalAt(i) * 100) );
			}
			System.out.println();
		}
		
		((TreeVariable)pn.getNodeAt(0)).addFinding(0);
		
		lw.run();
		
		for (Node node : pn.getNodes()) {
			System.out.println(node.getDescription());
			for (int i = 0; i < node.getStatesSize(); i++) {
				System.out.println("	" + node.getStateAt(i) + ": " + nf.format(((TreeVariable)node).getMarginalAt(i) * 100) );
			}
			System.out.println();
		}
	}

	/**
	 * @return the likelihoodWeightingInference under test
	 */
	public LikelihoodWeightingInference getLikelihoodWeightingInference() {
		return likelihoodWeightingInference;
	}

	/**
	 * @param likelihoodWeightingInference the likelihoodWeightingInference to set
	 */
	public void setLikelihoodWeightingInference(
			LikelihoodWeightingInference likelihoodWeightingInference) {
		this.likelihoodWeightingInference = likelihoodWeightingInference;
	}

	/**
	 * @return the sampleSize
	 */
	public int getSampleSize() {
		return sampleSize;
	}

	/**
	 * @param sampleSize the sampleSize to set
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	/**
	 * @return the netFileName
	 */
	public String getNetFileName() {
		return netFileName;
	}

	/**
	 * @param netFileName the netFileName to set
	 */
	public void setNetFileName(String netFileName) {
		this.netFileName = netFileName;
	}

	/**
	 * @return the probabilisticNetwork
	 */
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return probabilisticNetwork;
	}

	/**
	 * @param probabilisticNetwork the probabilisticNetwork to set
	 */
	public void setProbabilisticNetwork(ProbabilisticNetwork probabilisticNetwork) {
		this.probabilisticNetwork = probabilisticNetwork;
	}
	
	

}
