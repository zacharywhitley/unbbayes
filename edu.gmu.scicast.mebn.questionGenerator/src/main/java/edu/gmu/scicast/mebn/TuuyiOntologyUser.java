/**
 * 
 */
package edu.gmu.scicast.mebn;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

/**
 * Classes implementing this interface uses {@link OntologyClient}
 * @author Shou Matsumoto
 */
public interface TuuyiOntologyUser {
	
	/** Name of the OWL data property that indicates that the instance shall not be considered */
	public static final String IS_TO_EXCLUDE_DATA_PROPERTY_NAME = "isToExclude";

	
	/**
	 * @return the {@link OntologyClient} to be used to access Tuuyi ontology servlet.
	 * By default, the servlet can be accessed at http://q.tuuyi.net:8080/TuuyiOntologyServlet/api/
	 */
	public OntologyClient getOntologyClient() ;
}
