/**
 * 
 */
package edu.gmu.scicast.mebn;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

/**
 * Classes implementing this interface uses {@link OntologyClient}
 * in order to access the Tuuyi ontology servlet
 * (by default, available at the URL {@link OntologyClient#getServerURL()}).
 * @author Shou Matsumoto
 */
public interface TuuyiOntologyUser {
	
//	/** Default static/singleton instance of {@link OntologyClient} that can be used by implementations if they are not personalizing execution. */
//	public static final OntologyClient DEFAULT_ONTOLOGY_CLIENT = new OntologyClient();
	
	/** Name of the OWL data property that indicates that the instance shall not be considered */
	public static final String IS_TO_EXCLUDE_DATA_PROPERTY_NAME = "isToExclude";
	
	/** 
	 * Name of the OWL class that will make the reasoner think that individuals of this class are classes/categories in tuuyi server, 
	 * and skol property "broader" will be used to search for new individuals 
	 */
	public static final String REMOTE_CLASS_NAME = "TuuyiClass";

	/** Name of the OWL data property that stores the numeric ID of the individual in Tuuyi server */
	public static final String HAS_UID_PROPERTY_NAME = "hasUID";
	
	/** Name of the OWL data property that stores the numeric ID of a property at Tuuyi server */
	public static final String HIERARCHY_PROPERTY_ID = "hierarchyPropertyID";
	
	/** Name of the OWL data property that stores whether the inverse of {@link #HIERARCHY_PROPERTY_ID} shal be used */
	public static final String IS_INVERSE_HIERARCHY_PROPERTY_ID = "isInverseHierarchyPropertyID";
	
	/** 
	 * Name of the OWL data property that indicates how deep in skol:broader property at Tuuyi server shall be considered. 
	 * Zero means that the hierarchy shall not be considered, and negative numbers have implementation-specific meanings. 
	 */
	public static final String HAS_MAX_DEPTH_PROPERTY_NAME = "hasMaxDepth";
	
	/** This is the default URI of scicast external ontology definition ontology */
	public static final String EXTERNAL_ONTOLOGY_NAMESPACE_URI =  "http://www.scicast.org/questionGen/scicast.owl";
	
	/** This is a prefix manager for {@link #EXTERNAL_ONTOLOGY_NAMESPACE_URI} */
	public static final PrefixManager EXTERNAL_ONTOLOGY_DEFAULT_PREFIX_MANAGER = new DefaultPrefixManager(EXTERNAL_ONTOLOGY_NAMESPACE_URI + '#');
	
	/**
	 * @return the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 * By default, the servlet can be accessed at {@link OntologyClient#getServerURL()}
	 */
	public OntologyClient getOntologyClient() ;
}
