/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeLPEComplementAlgorithm extends JunctionTreeMPEAlgorithm {


	/**
	 * 
	 */
	public JunctionTreeLPEComplementAlgorithm() {
		this(null);
	}

	/**
	 * @param net
	 */
	public JunctionTreeLPEComplementAlgorithm(ProbabilisticNetwork net) {
		super(net);
		// initialize default junction tree with a min-product operator
		try{
			this.setDefaultJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MinProductJunctionTreeComplement.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "Least Probable Explanation with Junction Tree using MPE with complementar value";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	public String getName() {
		return "Junction Tree Least PE, Compl";
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#reset()
	 */
	public void reset() {
		super.reset();
		this.propagate();
	}

	
}
