/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import unbbayes.util.Debug;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

/**
 * @author Shou Matsumoto
 *
 */
public abstract class PROWLModelUser implements IProtegeOWLModelUser {

	
	
	//names of the classes in PR_OWL definition FIle, which are used inside UnBBayes IO:
	
	public static final String ARGUMENT_RELATIONSHIP = "ArgRelationship";
	public static final String BUILTIN_RV = "BuiltInRV";
	public static final String BOOLEAN_STATE = "BooleanRVState";
	public static final String CATEGORICAL_STATE = "CategoricalRVState"; 
	public static final String CONTEXT_NODE = "Context";
	public static final String DECLARATIVE_PROBABILITY_DISTRIBUTION = "DeclarativeDist";		
	public static final String DOMAIN_MFRAG = "Domain_MFrag";
	public static final String DOMAIN_RESIDENT = "Domain_Res";
	public static final String GENERATIVE_INPUT = "Generative_input";
	public static final String META_ENTITY = "MetaEntity"; 
	public static final String MTHEORY = "MTheory";	
	public static final String OBJECT_ENTITY = "ObjectEntity"; 	
	public static final String ORDINARY_VARIABLE = "OVariable";
	public static final String SIMPLE_ARGUMENT_RELATIONSHIP = "SimpleArgRelationship";
	
	
	// Names of meta entities' individuals native to pr-owl definition
	public static final String BOOLEAN_LABEL = "Boolean";
	public static final String TYPE_LABEL = "TypeLabel";
	public static final String CATEGORY_LABEL = "CategoryLabel";
	
	// after plugin support, the PROWLMODELFILE has changed its position
	public static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 
	
	/**
	 * 
	 */
	public PROWLModelUser() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * Load the upper-ontology pr-owl.owl to the JenaOwlModel given by the parameter.
	 * Returns a reference to the modified owlModel.
	 * 
	 * @param owlModel model to be updated/overwritten
	 * @throws IOException in case the Jena model reports errors
	 * @return a reference to updated/overwritten model. It's not really necessary to use it, since
	 * it's the parameter itself.
	 */
	public JenaOWLModel loadPrOwlModel(JenaOWLModel owlModel)throws IOException{
		
		File filePrOwl = null;
		try {
			filePrOwl = new File(this.getClass().getClassLoader().getResource(PROWLMODELFILE).toURI());
		} catch (Exception e1) {
			Debug.println(this.getClass(), "Could not load pr-owl definitions from resource. Retry...", e1);
			try {
				// retrying using file on root instead
				filePrOwl = new File(PROWLMODELFILE);
				if (!filePrOwl.exists()) {
					filePrOwl = null;
				}
			} catch (Exception e) {
				e1.printStackTrace();
				e.printStackTrace();
				throw new IOException(e.toString()); 
			}
		}
		
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, true));
		
		InputStream inputStreamOwl; 
		try{
			inputStreamOwl = this.getClass().getClassLoader().getResourceAsStream(PROWLMODELFILE);
			if (inputStreamOwl == null) {
				inputStreamOwl = new FileInputStream(PROWLMODELFILE);
			}
			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
		} catch (Exception e){
			Debug.println(this.getClass(), "Could not load pr-owl definitions from resource. Retry...", e);
			try {
				// retrying using file on root instead
				inputStreamOwl = new FileInputStream(PROWLMODELFILE);
				owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new IOException(e2.toString()); 
			}
		}			
		return owlModel;
	}
	
	/**
	 * A subclass extending this class should implement this method returning all PR-OWL classes
	 * which its individuals were somehow altered.
	 * This might be useful to trace diffs.
	 * The default behavior is to return all PR-OWL classes' names declared as static constants at this
	 * class.
	 * @return collection of OWL classes' names
	 */
	public Collection<String> getNamesOfAllModifiedPROWLClasses() {
		Collection<String> ret = new ArrayList<String>();
		
		ret.add(ARGUMENT_RELATIONSHIP);
		ret.add(BUILTIN_RV);
		ret.add(BOOLEAN_STATE);
		ret.add(CATEGORICAL_STATE);
		ret.add(CONTEXT_NODE);
		ret.add(DECLARATIVE_PROBABILITY_DISTRIBUTION);
		ret.add(DOMAIN_MFRAG);
		ret.add(DOMAIN_RESIDENT);
		ret.add(GENERATIVE_INPUT);
		ret.add(META_ENTITY);
		ret.add(MTHEORY);
		ret.add(OBJECT_ENTITY);
		ret.add(ORDINARY_VARIABLE);
		ret.add(SIMPLE_ARGUMENT_RELATIONSHIP);
		
		return ret;
	}
	
	
	/**
	 * A subclass extending this class should implement this method returning all PR-OWL individuals
	 * which is a label (Meta entity) were somehow altered.
	 * This might be useful to trace diffs.
	 * The default behavior is to return "TypeLabel", "CategoryLabel" and "Boolean".
	 * @return collection of OWL individuals' names
	 */
	public Collection<String> getNamesOfAllModifiedLabels() {
		Collection<String> ret = new ArrayList<String>();
		
		ret.add(BOOLEAN_LABEL);
		ret.add(TYPE_LABEL);
		ret.add(CATEGORY_LABEL);
		
		return ret;
	}
}
