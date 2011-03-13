/**
 * 
 */
package unbbayes.prs.mebn;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;

/**
 * This is a MEBN containing a map from MEBN elements (e.g. resident nodes, input nodes,
 * object entities) to the IRI of its correspondent OWL objects.
 * This is useful if MEBN elements should retain what was its original OWL Objects.
 * {@link unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO} will fill the values
 * of {@link #getIriMap()}.
 * The "definesUncertaintyOf" property (the one that associates
 * a resident node to an OWL property) of the PR-OWL2 definition will be implemented
 * by {@link #getDefineUncertaintyOfMap()}
 * @author Shou Matsumoto
 *
 */
public class IRIAwareMultiEntityBayesianNetwork extends
		MultiEntityBayesianNetwork {
	
	private Map<Object, IRI> iriMap = new HashMap<Object, IRI>();
	
	private Map<ResidentNode, IRI> defineProbabilityOfMap = new HashMap<ResidentNode, IRI>();

	/**
	 * The default constructor is not private in order to allow inheritance
	 * @param name
	 */
	protected IRIAwareMultiEntityBayesianNetwork(String name) {
		super(name);
	}
	
	/**
	 * Default constructor method initializing fields
	 * @param name
	 * @return
	 */
	public static MultiEntityBayesianNetwork getInstance(String name) {
		return new IRIAwareMultiEntityBayesianNetwork(name);
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.MultiEntityBayesianNetwork#getStorageImplementor()
//	 */
//	public IOWLAPIStorageImplementorDecorator getStorageImplementor() {
//		return (IOWLAPIStorageImplementorDecorator)super.getStorageImplementor();
//	}

	/**
	 * @return the iriMap
	 */
	public Map<Object, IRI> getIriMap() {
		return iriMap;
	}

	/**
	 * @param iriMap the iriMap to set
	 */
	public void setIriMap(Map<Object, IRI> iriMap) {
		this.iriMap = iriMap;
	}
	
	/**
	 * This is a facilitator to add a key-value to {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key
	 * @param value
	 * @deprecated static method calls should be avoided
	 */
	public static void addIRIToMEBN(MultiEntityBayesianNetwork mebn, Object key, IRI value) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIriMap() == null) {
				((IRIAwareMultiEntityBayesianNetwork)mebn).setIriMap(new HashMap<Object, IRI>());
			}
			((IRIAwareMultiEntityBayesianNetwork)mebn).getIriMap().put(key, value);
		}
	}
	
	/**
	 * This is a facilitator to obtain a value from {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key
	 * @return value
	 * @deprecated static method calls should be avoided
	 */
	public static IRI getIRIFromMEBN(MultiEntityBayesianNetwork mebn, Object key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIriMap() != null) {
				return ((IRIAwareMultiEntityBayesianNetwork)mebn).getIriMap().get(key);
			}
		}
		return null;
	}
	
	/**
	 * This is a facilitator to add a key-value to {@link #getDefineUncertaintyOfMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}.
	 * The "definesUncertaintyOf" property (the one that associates  
	 * a resident node to an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @param mebn
	 * @param key
	 * @param value
	 */
	public static void addDefineUncertaintyToMEBN(MultiEntityBayesianNetwork mebn, ResidentNode key, IRI value) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getDefineUncertaintyOfMap() == null) {
				((IRIAwareMultiEntityBayesianNetwork)mebn).setDefineUncertaintyOfMap(new HashMap<ResidentNode, IRI>());
			}
			((IRIAwareMultiEntityBayesianNetwork)mebn).getDefineUncertaintyOfMap().put(key, value);
		}
	}
	
	/**
	 * This is a facilitator to obtain a value from {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key
	 * @return value
	 */
	public static IRI getDefineUncertaintyFromMEBN(MultiEntityBayesianNetwork mebn, Object key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getDefineUncertaintyOfMap() != null) {
				return ((IRIAwareMultiEntityBayesianNetwork)mebn).getDefineUncertaintyOfMap().get(key);
			}
		}
		return null;
	}

	/**
	 * The "definesUncertaintyOf" property (the one that associates  
	 * a resident node to an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @return the defineProbabilityOfMap
	 */
	public Map<ResidentNode, IRI> getDefineUncertaintyOfMap() {
		return defineProbabilityOfMap;
	}

	/**
	 * The "definesUncertaintyOf" property (the one that associates  
	 * a resident node to an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @param defineProbabilityOfMap the defineProbabilityOfMap to set
	 */
	public void setDefineUncertaintyOfMap(
			Map<ResidentNode, IRI> defineProbabilityOfMap) {
		this.defineProbabilityOfMap = defineProbabilityOfMap;
	}
	
	

}
