package unbbayes.prs.bn;



/**
 * This is just an extension of {@link JunctionTree} which does never fill or update content of clique/separator potentials
 * when {@link ProbabilisticNetwork#compile()} is run.
 * This is used in {@link IncrementalJunctionTreeAlgorithm} in order to quickly build a junction tree
 * structure after changes in Bayes net, without wasting computational time for initializing beliefs or to guarantee global consistency.
 * Basically, this forces {@link SingleEntityNetwork#compileJT(IJunctionTree)} not to fill probabilities nor to assure global consistency,
 * so changes in {@link SingleEntityNetwork#compileJT(IJunctionTree)} may affect the behavior of this class.
 * Additionally, this class only assures that fields read by {@link IncrementalJunctionTreeAlgorithm}
 * are consistent, so other fields/methods must not be accessed.
 * @author Shou Matsumoto
 */
public class StructureOnlyJunctionTree extends JunctionTree {
	private static final long serialVersionUID = 5496557084782672808L;
	private ProbabilisticNetwork net;
	/** Default constructor using fields */
	public StructureOnlyJunctionTree(ProbabilisticNetwork net) { this.net = net;}
	/** 
	 * Do not initialize belief (this will make sure {@link SingleEntityNetwork#compileJT(IJunctionTree)} won't touch probabilities). 
	 * This will also clear {@link ProbabilisticNetwork#getNodesCopy()},
	 * so that {@link SingleEntityNetwork#compileJT(IJunctionTree)} will do nothing after this method.
	 * {@link ProbabilisticNetwork#resetNodesCopy()} shall be called to fill {@link ProbabilisticNetwork#getNodesCopy()} again, if necessary. 
	 */
	public void initBeliefs() throws Exception { net.getNodesCopy().clear(); return; }
}