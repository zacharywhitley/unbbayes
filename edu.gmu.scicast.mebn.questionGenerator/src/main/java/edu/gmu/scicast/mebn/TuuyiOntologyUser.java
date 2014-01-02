/**
 * 
 */
package edu.gmu.scicast.mebn;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

/**
 * Classes implementing this interface uses {@link OntologyClient}
 * in order to access the Tuuyi ontology servlet
 * (by default, available at the URL {@link OntologyClient#getServerURL()}).
 * @author Shou Matsumoto
 */
public interface TuuyiOntologyUser {
	
	/** Name of the OWL data property that indicates that the instance shall not be considered */
	public static final String IS_TO_EXCLUDE_DATA_PROPERTY_NAME = "isToExclude";
	
	/** 
	 * Name of the OWL class that will make the reasoner think that individuals of this class are classes/categories in tuuyi server, 
	 * and skol property "broader" will be used to search for new individuals 
	 */
	public static final String REMOTE_CLASS_NAME = "TuuyiClass";

	/** Name of the OWL data property that stores the numeric ID of the individual in Tuuyi server */
	public static final String HAS_UID_PROPERTY_NAME = "hasUID";
	
	/** 
	 * Name of the OWL data property that indicates how deep in skol:broader property at Tuuyi server shall be considered. 
	 * Zero means that the hierarchy shall not be considered, and negative numbers have implementation-specific meanings. 
	 */
	public static final String HAS_MAX_DEPTH_PROPERTY_NAME = "hasMaxDepth";
	
	/** This is the default URI of scicast external ontology definition ontology */
	public static final String EXTERNAL_ONTOLOGY_NAMESPACE_URI =  "http://www.scicast.org/questionGen/scicast.owl";
	
	/** This is a prefix manager for {@link #EXTERNAL_ONTOLOGY_NAMESPACE_URI} */
	public static final PrefixManager EXTERNAL_ONTOLOGY_DEFAULT_PREFIX_MANAGER = new DefaultPrefixManager(IPROWL2ModelUser.PROWL2_NAMESPACEURI + '#');
	
	/**
	 * @return the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 * By default, the servlet can be accessed at {@link OntologyClient#getServerURL()}
	 */
	public OntologyClient getOntologyClient() ;
}
