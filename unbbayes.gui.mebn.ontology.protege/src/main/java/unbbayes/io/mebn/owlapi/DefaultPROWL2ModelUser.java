/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
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
	

	private Collection<OWLClassExpression> nonPROWLClassesCache = new HashSet<OWLClassExpression>();
	
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#extractName(org.semanticweb.owlapi.model.OWLObject)
	 */
	public String extractName(OWLObject owlObject) {
		// assertions
		if (owlObject == null) {
			return null;	// if none was specified, use the ontology ID
		}
		// if this entity has an ID, extract what is after '#'
		if (owlObject instanceof OWLEntity) {
			String name = ((OWLEntity)owlObject).toStringID();
			// the ID is probably in the following format: <URI>#<Name>
			if (name != null) {
				try {
					while (name.contains("#")) {
						name = name.substring(name.indexOf('#') + 1, name.length());
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return name;
		}
		
		// check if it is literal
		if (owlObject instanceof OWLLiteral) {
			
			// parse the value if its type is known
			if ( ((OWLLiteral)owlObject).isBoolean() ) {
				return String.valueOf(((OWLLiteral)owlObject).parseBoolean());
			} else if ( ((OWLLiteral)owlObject).isInteger() ) {
				return String.valueOf(((OWLLiteral)owlObject).parseInteger());
			} else if ( ((OWLLiteral)owlObject).isFloat() ) {
				return String.valueOf(((OWLLiteral)owlObject).parseFloat());
			} else if ( ((OWLLiteral)owlObject).isDouble() ) {
				return String.valueOf(((OWLLiteral)owlObject).parseDouble());
			} 
			
			// this is an unknown literal, then return the literal
			return String.valueOf(((OWLLiteral)owlObject).getLiteral());
		}
		return owlObject.toString();
	}
	
//	/**
//	 * @see unbbayes.io.mebn.IPROWL2ModelUser#getNonPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
//	 * @see #getNonPROWLClassesCache() : this is a cache of this method (it contains the last returned value). If
//	 * this value is set to non-null, this method will just return {@link #getNonPROWLClassesCache()}
//	 */
//	public Collection<OWLClassExpression> getNonPROWLClasses(OWLOntology ontology) {
//		// TODO this method is only removing PR-OWL1 classes. Start removing PR-OWL2 classes as well
//		
//		// use cache.
//		if (this.getNonPROWLClassesCache() != null && !this.getNonPROWLClassesCache().isEmpty()) {
//			return this.getNonPROWLClassesCache();
//		}
//		
//		// the cache is clear. Reload classes 
//		Collection<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
//		
//		try {
//			// TODO find out a class expression that queries owl:Thing - <PR-OWL classes> (Note: Thing and not(<PR-OWL classes>) returns nothing, because the "not" operation seems to have special meaning)
//			
//			// add all subclasses of thing
//			ret.addAll(this.getOWLSubclasses(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing(), ontology));
//			
//			// use PR-OWL namespace (prefix)
//			PrefixManager prefixManager = this.getOntologyPrefixManager(null);
//			
//			// remove PR-OWL 1 elements (we are removing here because we do not know how to query Thing and not(<PR-OWL classes>) in open-world assumption)
//			// TODO find out a way to query Thing and not(<PR-OWL classes>) in open-world assumption
//			
//			// remove ArgRelationship
//			OWLClass classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ARGUMENT_RELATIONSHIP, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove BuiltInRV
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.BUILTIN_RV, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove CondRelationship
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(COND_RELATIONSHIP, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove Entity
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("Entity", prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove Exemplar
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(EXEMPLAR, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove MFrag
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(MFRAG, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove MTheory
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MTHEORY, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove Node
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(NODE, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove OVariable
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ORDINARY_VARIABLE, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
////			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARY_VARIABLE, prefixManager);
////			ret.remove(classToRemove);
////			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove ProbAssign
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROB_ASSIGN, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//			// remove ProbDist
//			classToRemove = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROB_DIST, prefixManager);
//			ret.remove(classToRemove);
//			ret.removeAll(this.getOWLSubclasses(classToRemove, ontology));
//			
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//		
//		// update cache
//		this.setNonPROWLClassesCache(ret);
//		
//		return ret;
//	}
//	
//	/**
//	 * Obtains the asserted subclasses of a particular class expression
//	 * @param owlClassExpression
//	 * @param ontology
//	 * @return
//	 */
//	protected Set<OWLClassExpression> getOWLSubclasses(OWLClassExpression owlClassExpression, OWLOntology ontology) {
//		Set<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
//		
//		
//		if (owlClassExpression instanceof OWLClass) {
//			// if expression is exactly an OWL class, use this method
//			try{
//				ret.addAll(owlClassExpression.asOWLClass().getSubClasses(ontology));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			// expression is a complex expression. use another method
//			try{
//				ret.addAll( (Set)owlClassExpression.getClassesInSignature());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		try {
//			// remove the nothing from returned subclasses
//			ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing());
////			ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		return ret;
//	}

	/**
	 * @return the nonPROWLClassesCache
	 */
	public Collection<OWLClassExpression> getNonPROWLClassesCache() {
		return nonPROWLClassesCache;
	}

	/**
	 * @param nonPROWLClassesCache the nonPROWLClassesCache to set
	 */
	public void setNonPROWLClassesCache(
			Collection<OWLClassExpression> nonPROWLClassesCache) {
		this.nonPROWLClassesCache = nonPROWLClassesCache;
	}
}
