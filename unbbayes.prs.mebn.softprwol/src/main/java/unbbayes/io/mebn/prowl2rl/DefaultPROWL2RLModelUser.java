package unbbayes.io.mebn.prowl2rl;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;

public class DefaultPROWL2RLModelUser extends DefaultPROWL2ModelUser implements IPROWL2RLModelUser{

//	private String prowlOntologyNamespaceURI = PROWL2RL_NAMESPACEURI;

	private Collection<OWLClassExpression> nonPROWLClassesCache = new HashSet<OWLClassExpression>();
	
	/**
	 * The default constructor is visible to subclasses so that inheritance is allowed
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected DefaultPROWL2RLModelUser() {
		// TODO Auto-generated constructor stub
		setProwlOntologyNamespaceURI(PROWL2RL_NAMESPACEURI);
	}
	
	/**
	 * This is the default method for construction
	 * @return
	 */
	public static IPROWL2ModelUser getInstance() {
		return new DefaultPROWL2RLModelUser();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#getOntologyPrefixManager(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		// use PR-OWL prefix if no ontology was specified
		if (ontology == null) {
			try {
				// extract the PR-OWL ontology namespaces with '#'
				String defaultPrefix = getProwlOntologyNamespaceURI();
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
			return PROWL2RL_DEFAULTPREFIXMANAGER;
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

}
