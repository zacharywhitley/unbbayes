/**
 * 
 */
package unbbayes.prs.mebn.prowl2;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;

/**
 * This is a MEBN containing a map from MEBN elements (e.g. resident nodes, input nodes,
 * object entities) to the IRI of its correspondent OWL objects.
 * This is useful if MEBN elements should retain what was its original OWL Objects.
 * {@link unbbayes.io.mebn.prowl2.owlapi.OWLAPICompatiblePROWLIO} will fill the values
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

	private Map<Argument, Collection<IRI>> isSubjectOfMap = new HashMap<Argument, Collection<IRI>>();
	private Map<Argument, Collection<IRI>> isObjectInMap = new HashMap<Argument, Collection<IRI>>();
	
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
	 * This is an adaptor to add a key-value to {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
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
	 * This is an adaptor to obtain a value from {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
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
	 * This is an adaptor to add a key-value to {@link #getDefineUncertaintyOfMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}.
	 * The "definesUncertaintyOf" property (the one that associates  
	 * a resident node to an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @param mebn
	 * @param key
	 * @param value
	 * @deprecated static methods are deprecated
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
	 * This is an adaptor to add a key-value to {@link #getIsSubjectOfMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}.
	 * The "isSubjectOf" property (the one that maps an argument to the domain of an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @param mebn : the {@link MultiEntityBayesianNetwork} we are working with. If it is a {@link IRIAwareMultiEntityBayesianNetwork},
	 * then {@link IRIAwareMultiEntityBayesianNetwork#getIsSubjectOfMap()} will be referenced.
	 * @param key  : argument in {@link ResidentNode#getArgumentList()}
	 * @param value : IRI of an OWL property referenced by isSubjectOf (with a symbolic link)
	 * @deprecated static methods are deprecated
	 */
	public static void addSubjectToMEBN(MultiEntityBayesianNetwork mebn, Argument key, IRI value) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			Map<Argument, Collection<IRI>> map =((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap();
			if (map == null) {
				map = new HashMap<Argument, Collection<IRI>>();
			}
			Collection<IRI> iris = map.get(key);
			if (iris == null) {
				iris = new HashSet<IRI>();
			}
			iris.add(value);
			// we are explicitly putting values to map again because we want to make sure that map is up to date (there could be maps returning copies in get(key) instead of references)
			map.put(key, iris);
			((IRIAwareMultiEntityBayesianNetwork)mebn).setIsSubjectOfMap(map);
		}
	}
	
	/**
	 * This is an adaptor to add a key-value to {@link #getIsObjectInMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}.
	 * The "isObjectIn" property (the one that maps an argument to the range of an OWL property) of the PR-OWL2 definition will be implemented
	 * by this map.
	 * @param mebn
	 * @param key
	 * @param value
	 * @deprecated static methods are deprecated
	 */
	public static void addObjectToMEBN(MultiEntityBayesianNetwork mebn, Argument key, IRI value) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			Map<Argument, Collection<IRI>> map =((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap();
			if (map == null) {
				map = new HashMap<Argument, Collection<IRI>>();
			}
			Collection<IRI> iris = map.get(key);
			if (iris == null) {
				iris = new HashSet<IRI>();
			}
			iris.add(value);
			// we are explicitly putting values to map again because we want to make sure that map is up to date (there could be maps returning copies in get(key) instead of references)
			map.put(key, iris);
			((IRIAwareMultiEntityBayesianNetwork)mebn).setIsObjectInMap(map);
		}
	}
	
	
	/**
	 * This method clears the content of {@link #getIsObjectInMap()} if mebn is an instance of
	 * {@link IRIAwareMultiEntityBayesianNetwork}. If {@link #getIsObjectInMap()} == null, it will
	 * be initialized.
	 * @param mebn
	 * @param key : key to clear mapping. If set to null, all keys will be cleared.
	 * @deprecated static methods are deprecated
	 */
	public static void clearObjectMappingOfMEBN(MultiEntityBayesianNetwork mebn, Argument key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap() != null) {
				if (key == null) {
					((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap().clear();
				} else {
					((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap().remove(key);
				}
			} else {
				// initialize
				((IRIAwareMultiEntityBayesianNetwork)mebn).setIsObjectInMap(new HashMap<Argument, Collection<IRI>>());
			}
		}
	}
	
	/**
	 * This method clears the content of {@link #getIsSubjectOfMap()} if mebn is an instance of
	 * {@link IRIAwareMultiEntityBayesianNetwork}. If {@link #getIsSubjectOfMap()} == null, it will
	 * be initialized.
	 * @param mebn
	 * @param key : key to clear mapping. If set to null, all keys will be cleared.
	 * @deprecated static methods are deprecated
	 */
	public static void clearSubjectMappingOfMEBN(MultiEntityBayesianNetwork mebn, Argument key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap() != null) {
				if (key == null) {
					((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap().clear();
				} else {
					((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap().remove(key);
				}
			} else {
				// initialize
				((IRIAwareMultiEntityBayesianNetwork)mebn).setIsSubjectOfMap(new HashMap<Argument, Collection<IRI>>());
			}
		}
	}
	
	/**
	 * This is an adaptor to obtain a value from {@link #getIriMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key
	 * @return value
	 * @deprecated static methods are deprecated
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
	 * This is an adaptor to obtain a value from {@link #getIsSubjectOfMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key 
	 * @return value
	 * @deprecated static methods are deprecated
	 */
	public static Collection<IRI> getIsSubjectFromMEBN(MultiEntityBayesianNetwork mebn, Argument key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap() != null) {
				return ((IRIAwareMultiEntityBayesianNetwork)mebn).getIsSubjectOfMap().get(key);
			}
		}
		return null;
	}
	
	/**
	 * This is an adaptor to obtain a value from {@link #getIsObjectInMap()} if mebn is a {@link IRIAwareMultiEntityBayesianNetwork}
	 * @param mebn
	 * @param key 
	 * @return value
	 * @deprecated static methods are deprecated
	 */
	public static Collection<IRI> getIsObjectFromMEBN(MultiEntityBayesianNetwork mebn, Argument key) {
		if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
			if (((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap() != null) {
				return ((IRIAwareMultiEntityBayesianNetwork)mebn).getIsObjectInMap().get(key);
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

	/**
	 * @return the isObjectInMap
	 */
	public Map<Argument, Collection<IRI>> getIsObjectInMap() {
		return isObjectInMap;
	}

	/**
	 * @param isObjectInMap the isObjectInMap to set
	 */
	public void setIsObjectInMap(Map<Argument, Collection<IRI>> isObjectInMap) {
		this.isObjectInMap = isObjectInMap;
	}

	/**
	 * @return the isSubjectOfMap
	 */
	public Map<Argument, Collection<IRI>> getIsSubjectOfMap() {
		return isSubjectOfMap;
	}

	/**
	 * @param isSubjectOfMap the isSubjectOfMap to set
	 */
	public void setIsSubjectOfMap(Map<Argument, Collection<IRI>> isSubjectOfMap) {
		this.isSubjectOfMap = isSubjectOfMap;
	}
	
	

}
