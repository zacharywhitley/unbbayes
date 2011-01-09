/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.find.OWLEntityFinderPreferences;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import unbbayes.util.Debug;


/**
 * This class rewrites protege's OWLEntityFinderImpl because the original class
 * was too coupled to protege's OWLModelManagerImpl, so that code reuse was
 * almost impossible...
 * @author Shou Matsumoto
 *
 */
public class UnBBayesOWLEntityFinder implements OWLEntityFinder {


    private OWLEntityRenderingCache renderingCache;

    private OWLModelManager mngr;

    /** This is the wildcard character in search expressions */
    public static final String WILDCARD = "*";
    /** This is the escape character in search expressions */
    public static final String ESCAPE_CHAR = "'";

    /**
     * The default constructor is not private in order to allow inheritance.
     * @deprecated use {@link #newInstance(OWLModelManager, OWLEntityRenderingCache)} instead
     */
    protected UnBBayesOWLEntityFinder() {
    	super();
    }
    
    /**
     * Creates a new instance of the protege's entity finder. An entity finder
     * helps ontology users (i.e. users of OWLModelManager) to find entities
     * (classes, individuals, properties) by name (whole or part) or expressions.
     * @param mngr
     * @param renderingCache
     * @return a new instance
     */
    public static OWLEntityFinder newInstance(OWLModelManager mngr, OWLEntityRenderingCache renderingCache) {
    	UnBBayesOWLEntityFinder ret = new UnBBayesOWLEntityFinder();
    	ret.setMngr(mngr);
    	ret.setRenderingCache(renderingCache);
    	return ret;
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLClass(java.lang.String)
     */
    public OWLClass getOWLClass(String rendering) {
        OWLClass cls = renderingCache.getOWLClass(rendering);
        if (cls == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            cls = renderingCache.getOWLClass(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return cls;
    }


    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLObjectProperty(java.lang.String)
     */
    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        OWLObjectProperty prop = renderingCache.getOWLObjectProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            prop = renderingCache.getOWLObjectProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }


    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLDataProperty(java.lang.String)
     */
    public OWLDataProperty getOWLDataProperty(String rendering) {
        OWLDataProperty prop = renderingCache.getOWLDataProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            prop = renderingCache.getOWLDataProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }


    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLAnnotationProperty(java.lang.String)
     */
    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        OWLAnnotationProperty prop = renderingCache.getOWLAnnotationProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            prop = renderingCache.getOWLAnnotationProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }

	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLIndividual(java.lang.String)
	 */
    public OWLNamedIndividual getOWLIndividual(String rendering) {
        OWLNamedIndividual individual = renderingCache.getOWLIndividual(rendering);
        if (individual == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            individual = renderingCache.getOWLIndividual(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return individual;
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLDatatype(java.lang.String)
     */
    public OWLDatatype getOWLDatatype(String rendering) {
        OWLDatatype dataType = renderingCache.getOWLDatatype(rendering);
        if (dataType == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            dataType = renderingCache.getOWLDatatype(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return dataType;
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLEntity(java.lang.String)
     */
    public OWLEntity getOWLEntity(String rendering) {
        OWLEntity entity = renderingCache.getOWLEntity(rendering);
        if (entity == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)){
            entity = renderingCache.getOWLEntity(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getOWLEntityRenderings()
     */
    public Set<String> getOWLEntityRenderings() {
        return renderingCache.getOWLEntityRenderings();
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLClasses(java.lang.String)
     */
    public Set<OWLClass> getMatchingOWLClasses(String match) {
        return getEntities(match, OWLClass.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLClasses(java.lang.String, boolean)
     */
    public Set<OWLClass> getMatchingOWLClasses(String match, boolean fullRegExp) {
        return getEntities(match, OWLClass.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLClasses(java.lang.String, boolean, int)
     */
    public Set<OWLClass> getMatchingOWLClasses(String match, boolean fullRegExp, int flags) {
    	return getEntities(match, OWLClass.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLObjectProperties(java.lang.String)
     */
    public Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match) {
        return getEntities(match, OWLObjectProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLObjectProperties(java.lang.String, boolean)
     */
    public Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLObjectProperty.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLObjectProperties(java.lang.String, boolean, int)
     */
    public Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLObjectProperty.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDataProperties(java.lang.String)
     */
    public Set<OWLDataProperty> getMatchingOWLDataProperties(String match) {
        return getEntities(match, OWLDataProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }


    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDataProperties(java.lang.String, boolean)
     */
    public Set<OWLDataProperty> getMatchingOWLDataProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLDataProperty.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDataProperties(java.lang.String, boolean, int)
     */
    public Set<OWLDataProperty> getMatchingOWLDataProperties(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLDataProperty.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLIndividuals(java.lang.String)
     */
    public Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match) {
        return getEntities(match, OWLNamedIndividual.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLIndividuals(java.lang.String, boolean)
     */
    public Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match, boolean fullRegExp) {
        return getEntities(match, OWLNamedIndividual.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLIndividuals(java.lang.String, boolean, int)
     */
    public Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLNamedIndividual.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDatatypes(java.lang.String)
     */
    public Set<OWLDatatype> getMatchingOWLDatatypes(String match) {
        return getEntities(match, OWLDatatype.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDatatypes(java.lang.String, boolean)
     */
    public Set<OWLDatatype> getMatchingOWLDatatypes(String match, boolean fullRegExp) {
        return getEntities(match, OWLDatatype.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLDatatypes(java.lang.String, boolean, int)
     */
    public Set<OWLDatatype> getMatchingOWLDatatypes(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLDatatype.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLAnnotationProperties(java.lang.String)
     */
    public Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match) {
        return getEntities(match, OWLAnnotationProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLAnnotationProperties(java.lang.String, boolean)
     */
    public Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLAnnotationProperty.class, fullRegExp);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLAnnotationProperties(java.lang.String, boolean, int)
     */
    public Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLAnnotationProperty.class, fullRegExp, flags);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLEntities(java.lang.String)
     */
    public Set<OWLEntity> getMatchingOWLEntities(String match) {
        return getEntities(match, OWLEntity.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.find.OWLEntityFinder#getMatchingOWLEntities(java.lang.String, boolean)
     */
    public Set<OWLEntity> getMatchingOWLEntities(String match, boolean fullRegExp) {
        return getEntities(match, OWLEntity.class, fullRegExp);
    }
    
    public Set<OWLEntity> getMatchingOWLEntities(String match, boolean fullRegExp, int flags) {
        return getEntities(match, OWLEntity.class, fullRegExp, flags);
    }


    public Set<OWLEntity> getEntities(IRI iri) {

        Set<OWLEntity> entities = new HashSet<OWLEntity>();

        for (OWLOntology ont : mngr.getActiveOntologies()){
            if (ont.containsClassInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLClass(iri));
            }
            if (ont.containsObjectPropertyInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLObjectProperty(iri));
            }
            if (ont.containsDataPropertyInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLDataProperty(iri));
            }
            if (ont.containsIndividualInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLNamedIndividual(iri));
            }
            if (ont.containsAnnotationPropertyInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLAnnotationProperty(iri));
            }
            if (ont.containsDatatypeInSignature(iri)){
                entities.add(mngr.getOWLDataFactory().getOWLDatatype(iri));
            }
        }
        return entities;
    }


    private <T extends OWLEntity> Set<T> getEntities(String match, Class<T> type, boolean fullRegExp) {
        return getEntities(match, type, fullRegExp, Pattern.CASE_INSENSITIVE);
    }
    
    private <T extends OWLEntity> Set<T> getEntities(String match, Class<T> type, boolean fullRegExp, int flags) {
        if (match.length() == 0) {
            return Collections.emptySet();
        }

        if (fullRegExp) {
            return doRegExpSearch(match, type, flags);
        }
        else {
            return doWildcardSearch(match, type);
        }
    }


    private <T extends OWLEntity> Set<T> doRegExpSearch(String match, Class<T> type, int flags) {
        Set<T> results = new HashSet<T>();
        try {
            Pattern pattern = Pattern.compile(match, flags);
            for (String rendering : getRenderings(type)) {
                Matcher m = pattern.matcher(rendering);
                if (m.find()) {
                    T ent = getEntity(rendering, type);
                    if (ent != null) {
                        results.add(ent);
                    }
                }
            }
        }
        catch (Exception e) {
            Debug.println(this.getClass(), "Invalid regular expression", e);
        }
        return results;
    }

    /* @@TODO fix wildcard searching - it does not handle the usecases correctly
     * eg A*B will not work, and endsWith is implemented the same as contains
     * (probably right but this should not be implemented separately)
     */
    private <T extends OWLEntity> Set<T> doWildcardSearch(String match, Class<T> type) {
        Set<T> results = new HashSet<T>();

        if (match.equals(WILDCARD)){
            results = getAllEntities(type);
        }
        else{
            SimpleWildCardMatcher matcher;
            if (match.startsWith(WILDCARD)) {
                if (match.length() > 1 && match.endsWith(WILDCARD)) {
                    // Contains
                    matcher = new SimpleWildCardMatcher() {
                        public boolean matches(String rendering, String s) {
                            return rendering.indexOf(s) != -1;
                        }
                    };
                    match = match.substring(1, match.length() - 1);
                }
                else {
                    // Ends with
                    matcher = new SimpleWildCardMatcher() {
                        public boolean matches(String rendering, String s) {
                            return rendering.indexOf(s) != -1;
                        }
                    };
                    match = match.substring(1, match.length());
                }
            }
            else {
                // Starts with
                if (match.endsWith(WILDCARD) && match.length() > 1) {
                    match = match.substring(0, match.length() - 1);
                }
                // @@TODO handle matches exactly?
                matcher = new SimpleWildCardMatcher() {
                    public boolean matches(String rendering, String s) {
                        return rendering.startsWith(s) || rendering.startsWith("'" + s);
                    }
                };
            }

            if (match.trim().length() == 0) {
            	Debug.println(this.getClass(), "Attempt to match the empty string (no results)");
            }
            else{
                match = match.toLowerCase();
                try {
                	Debug.println(this.getClass(), "Match: " + match);
                } catch (Throwable e) {
					e.printStackTrace();
				}
                for (String rendering : getRenderings(type)) {
                    if (rendering.length() > 0){
                        if (matcher.matches(rendering.toLowerCase(), match)) {
                            results.add(getEntity(rendering, type));
                        }
                    }
                }
            }

        }

        return results;
    }


    @SuppressWarnings("unchecked")
    private <T extends OWLEntity> Set<T> getAllEntities(Class<T> type) {
        if (type.equals(OWLDatatype.class)){
            return (Set<T>)new OWLDataTypeUtils(mngr.getOWLOntologyManager()).getKnownDatatypes(mngr.getActiveOntologies());
        }
        else{
            Set<T> entities = new HashSet<T>();
            for (OWLOntology ont: mngr.getActiveOntologies()){
                if (type.equals(OWLClass.class)){
                    entities.addAll((Set<T>)ont.getClassesInSignature());
                }
                else if (type.equals(OWLObjectProperty.class)){
                    entities.addAll((Set<T>)ont.getObjectPropertiesInSignature());
                }
                else if (type.equals((OWLDataProperty.class))){
                    entities.addAll((Set<T>)ont.getDataPropertiesInSignature());
                }
                else if (type.equals(OWLIndividual.class)){
                    entities.addAll((Set<T>)ont.getIndividualsInSignature());
                }
                else if (type.equals(OWLAnnotationProperty.class)){
                    entities.addAll((Set<T>)ont.getAnnotationPropertiesInSignature());
                }
                else if (type.equals(OWLDatatype.class)){
                    entities.addAll((Set<T>)ont.getDatatypesInSignature());
                }
            }
            return entities;
        }
    }


    private <T extends OWLEntity> T getEntity(String rendering, Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLClass(rendering));
        }
        else if (OWLObjectProperty.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLObjectProperty(rendering));
        }
        else if (OWLDataProperty.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLDataProperty(rendering));
        }
        else if (OWLNamedIndividual.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLIndividual(rendering));
        }
        else if (OWLAnnotationProperty.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLAnnotationProperty(rendering));
        }
        else if (OWLDatatype.class.isAssignableFrom(type)){
            return type.cast(renderingCache.getOWLDatatype(rendering));
        }
        else{
            return type.cast(renderingCache.getOWLEntity(rendering));
        }
    }


    private <T extends OWLEntity> Set<String> getRenderings(Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)){
            return renderingCache.getOWLClassRenderings();
        }
        else if (OWLObjectProperty.class.isAssignableFrom(type)){
            return renderingCache.getOWLObjectPropertyRenderings();
        }
        else if (OWLDataProperty.class.isAssignableFrom(type)){
            return renderingCache.getOWLDataPropertyRenderings();
        }
        else if (OWLNamedIndividual.class.isAssignableFrom(type)){
            return renderingCache.getOWLIndividualRenderings();
        }
        else if (OWLAnnotationProperty.class.isAssignableFrom(type)){
            return renderingCache.getOWLAnnotationPropertyRenderings();
        }
        else if (OWLDatatype.class.isAssignableFrom(type)){
            return renderingCache.getOWLDatatypeRenderings();
        }
        else{
            return renderingCache.getOWLEntityRenderings();
        }
    }


    private interface SimpleWildCardMatcher {

        boolean matches(String rendering, String s);
    }


	/**
	 * @return the renderingCache
	 */
	public OWLEntityRenderingCache getRenderingCache() {
		return renderingCache;
	}

	/**
	 * @param renderingCache the renderingCache to set
	 */
	public void setRenderingCache(OWLEntityRenderingCache renderingCache) {
		this.renderingCache = renderingCache;
	}

	/**
	 * @return the mngr
	 */
	public OWLModelManager getMngr() {
		return mngr;
	}

	/**
	 * @param mngr the mngr to set
	 */
	public void setMngr(OWLModelManager mngr) {
		this.mngr = mngr;
	}
}

