/**
 * 
 */
package unbbayes.prs.bn;

import java.util.List;

import unbbayes.prs.INode;
import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link ILikelihoodExtractor}.
 * Use
 * @author Shou Matsumoto
 *
 */
public class LikelihoodExtractor implements ILikelihoodExtractor {

	/**
	 * default constructor is protected to allow inheritance.
	 * @deprecated use {@link #newInstance()} instead.
	 */
	protected LikelihoodExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * @return
	 */
	public static ILikelihoodExtractor newInstance() {
		return new LikelihoodExtractor();
	}

	/**
	 * Just extracts the likelihood of the first node, assuming that the node
	 * is a {@link TreeVariable}.
	 * @see unbbayes.prs.bn.ILikelihoodExtractor#extractLikelihoodRatio(java.util.List)
	 */
	public float[] extractLikelihoodRatio(List<INode> nodes) {
		try {
			return ((TreeVariable)nodes.get(0)).getLikelihood();
		}catch (Exception e) {
			Debug.println(getClass(), e.getMessage(),e);
		}
		return null;
	}

}
