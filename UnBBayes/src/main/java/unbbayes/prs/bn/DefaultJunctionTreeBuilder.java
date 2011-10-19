/**
 * 
 */
package unbbayes.prs.bn;

import unbbayes.prs.Graph;
import unbbayes.prs.id.JunctionTreeID;
import unbbayes.util.Debug;

/**
 * This class instantiates an object of {@link JunctionTree}.
 * This is a default implementation of {@link IJunctionTreeBuilder}.
 * It uses the class object in {@link #getJunctionTreeClass()} 
 * to create new instances of {@link JunctionTree}
 * @author Shou Matsumoto
 *
 */
public class DefaultJunctionTreeBuilder implements IJunctionTreeBuilder {

	private Class junctionTreeClass;
	
	/**
	 * Default constructor
	 */
	public DefaultJunctionTreeBuilder() {
		this(JunctionTree.class);
	}
	
	/**
	 * Constructor initializing fields
	 * @param junctionTreeClass
	 * @see #setJunctionTreeClass(Class)
	 */
	public DefaultJunctionTreeBuilder(Class junctionTreeClass){
		try {
			this.setJunctionTreeClass(junctionTreeClass);
		} catch (Exception e) {
			Debug.println(getClass(), "Could not initialize junctionTreeClass, but it will continue.", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IJunctionTreeBuilder#buildJunctionTree(unbbayes.prs.Graph)
	 */
	public IJunctionTree buildJunctionTree(Graph network)  throws InstantiationException, IllegalAccessException {
		if (network != null) {
			if (network instanceof SingleEntityNetwork) {
				SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) network;
				if (singleEntityNetwork.isID()) {
					this.setJunctionTreeClass(JunctionTreeID.class);
				}
			}
		}
		return (IJunctionTree)this.getJunctionTreeClass().newInstance();	// empty junction tree
	}

	/**
	 * @return the junctionTreeClass
	 */
	public Class getJunctionTreeClass() {
		return junctionTreeClass;
	}

	/**
	 * @param junctionTreeClass the junctionTreeClass to set. It must be compatible with {@link IJunctionTree}.
	 */
	public void setJunctionTreeClass(Class junctionTreeClass) throws ClassCastException {
		if (IJunctionTree.class.isAssignableFrom(junctionTreeClass)) {
			this.junctionTreeClass = junctionTreeClass;
		} else {
			throw new ClassCastException(junctionTreeClass + " incompatible with " + IJunctionTree.class);
		}
	}

}
