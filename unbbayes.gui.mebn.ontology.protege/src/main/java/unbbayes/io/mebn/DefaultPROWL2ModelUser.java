/**
 * 
 */
package unbbayes.io.mebn;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * This is a default implementation of {@link IPROWL2ModelUser}
 * @author Shou Matsumoto
 *
 */
public class DefaultPROWL2ModelUser implements IPROWL2ModelUser {

	private String prowlOntologyNamespaceURI = PROWL2_NAMESPACEURI;
	
	/**
	 * The default constructor is visible to subclasses so that inheritance is allowed
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected DefaultPROWL2ModelUser() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is the default method for construction
	 * @return
	 */
	public static IPROWL2ModelUser getInstance() {
		return new DefaultPROWL2ModelUser();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#getOntologyPrefixManager(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		// use PR-OWL prefix if no ontology was specified
		if (ontology == null) {
			try {
				// extract the PR-OWL ontology namespaces with '#'
				String defaultPrefix = this.getProwlOntologyNamespaceURI();
				if (defaultPrefix != null) {
					if (!defaultPrefix.endsWith("#")) {
						defaultPrefix += "#";
					}
					PrefixManager prefixManager = new DefaultPrefixManager(defaultPrefix);
					return prefixManager;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// use the PROWL2 default prefix manager if we could not extract manager and ontology == null
			return PROWL2_DEFAULTPREFIXMANAGER;
		} else {
			// read prefix from ontology
			try {
				String defaultPrefix = ontology.getOntologyID().getDefaultDocumentIRI().toString();
				if (!defaultPrefix.endsWith("#")) {
					defaultPrefix += "#";
				}
				PrefixManager prefixManager = new DefaultPrefixManager(defaultPrefix);
				return prefixManager;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		
		// could not extract prefixes at all...
		return null;
	}

	/**
	 * This is the String representing a URI of the PR-OWL definition ontology.
	 * The default value is {@link IPROWL2ModelUser#PROWL2_NAMESPACEURI}.
	 * @return the prowlOntologyNamespaceURI
	 */
	protected String getProwlOntologyNamespaceURI() {
		return prowlOntologyNamespaceURI;
	}

	/**
	 * This is the String representing a URI of the PR-OWL definition ontology.
	 * The default value is {@link IPROWL2ModelUser#PROWL2_NAMESPACEURI}.
	 * @param prowlOntologyNamespaceURI the prowlOntologyNamespaceURI to set
	 */
	protected void setProwlOntologyNamespaceURI(String prowlOntologyNamespaceURI) {
		this.prowlOntologyNamespaceURI = prowlOntologyNamespaceURI;
	}

}
