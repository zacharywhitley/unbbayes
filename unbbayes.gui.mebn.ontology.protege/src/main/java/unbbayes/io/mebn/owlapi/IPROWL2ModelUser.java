/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * I/O classes using PR-OWL2 ontology models should implement this class
 * @author Shou Matsumoto
 *
 */
public interface IPROWL2ModelUser {
	/** This is the default URI of a PR-OWL2 ontology */
	public static final String PROWL2_NAMESPACEURI =  "http://www.pr-owl.org/pr-owl2.owl";
	/** This is the default URI of a PR-OWL 1 (old) ontology */
	public static final String OLD_PROWL_NAMESPACEURI = "http://www.pr-owl.org/pr-owl.owl";
	
	/** This is a prefix manager for {@value IPROWL2ModelUser#PROWL2_NAMESPACEURI} */
	public static final PrefixManager PROWL2_DEFAULTPREFIXMANAGER = new DefaultPrefixManager(IPROWL2ModelUser.PROWL2_NAMESPACEURI + '#');
	
	/** this is a suffix appended to all individuals of {@link #DECLARATIVEDISTRIBUTION} */
	public static final String DECLARATIVE_DISTRO_SUFIX = "_Table";
	/** A string to be used as prefixes of MExpressions' names */
	public static final String MEXPRESSION_PREFIX = "MEXPRESSION_";
	/** A string to be used as prefixes of RV' names */
	public static final String RANDOMVARIABLE_PREFIX = "RV_";
	/** String to be used in order to separate names of arguments from its ordering number.*/
	public static final String ARGUMENT_NUMBER_SEPARATOR = "_";
	/** A string to be used as suffixes of MExpressions which are also types of individuals of MExpressionArgument */
	public static final String INNERMEXPRESSION_SUFFIX = "_inner";
	
	/** 
	 * A string to be used as suffixes of object entity's types, when MEBN or ontology uses Types internally. 
	 * @deprecated Typer will not be used in future releases ]
	 */
	public static final String TYPE_LABEL_SUFFIX = "_label";
	
	/** name of the individual equivalent to {@link #ABSURD}*/
	public static final String ABSURD_INDIVIDUAL = "absurd";
	
	// Some names of the classes in PR_OWL2 definition File

	public static final String ABSURD = "Absurd";
	public static final String ARGUMENT = "Argument";

	public static final String MAPPINGARGUMENT = "MappingArgument";
	
	public static final String CONTEXTNODE = "ContextNode";
	public static final String DOMAINMFRAG = "DomainMFrag";
	public static final String DOMAINRESIDENT = "DomainResidentNode";	//
	public static final String GENERATIVEINPUT = "GenerativeInputNode";//
	public static final String MTHEORY = "MTheory";	
	public static final String ORDINARYVARIABLE = "OrdinaryVariable";
	public static final String CONDRELATIONSHIP = "CondRelationship";
	public static final String EXEMPLAR = "Exemplar";
	public static final String MFRAG = "MFrag";
	public static final String NODE = "Node";
	public static final String PROBABILITYASSIGNMENT = "ProbabilityAssignment";
	public static final String PROBABILITYDISTRIBUTION = "ProbabilityDistribution";//
	
	public static final String RANDOMVARIABLE = "RandomVariable";
	public static final String BOOLEANRANDOMVARIABLE = "BooleanRandomVariable";
	
	public static final String ORDINARYVARIABLEARGUMENT = "OrdinaryVariableArgument";
	public static final String CONSTANTARGUMENT = "ConstantArgument";

	public static final String MEXPRESSIONARGUMENT = "MExpressionArgument";
	public static final String MEXPRESSION = "MExpression";
	public static final String BOOLEANMEXPRESSION = "BooleanMExpression";
	public static final String SIMPLEMEXPRESSION = "SimpleMExpression";
	public static final String DECLARATIVEDISTRIBUTION = "DeclarativeDistribution";

	public static final String LOGICALOPERATOR = "LogicalOperator";
	public static final String QUANTIFIER = "Quantifier";
	
	/** Name of the object property linking MFrag to Resident node */
	public static final String HASRESIDENTNODE = "hasResidentNode";
	/** Inverse of {@link #HASRESIDENTNODE} */
	public static final String ISRESIDENTNODEIN = "isResidentNodeIn"; 
	/** Name of the object property linking MFrag to Input node */
	public static final String HASINPUTNODE = "hasInputNode";
	/** Inverse of {@link #HASINPUTNODE} */
	public static final String ISINPUTNODEIN = "isInputNodeIn"; 
	/** Name of the object property linking MFrag to Context node */
	public static final String HASCONTEXTNODE = "hasContextNode";
	/** Inverse of {@link #HASCONTEXTNODE} */
	public static final String ISCONTEXTNODEIN = "isContextNodeIn"; 
	
	/** Name of the object property linking MFrag to ordinary variables */
	public static final String HASOVARIABLE = "hasOrdinaryVariable";
	/** Name of the datatype property linking ordinary variables to the URI of its possible value*/
	public static final String ISSUBSTITUTEDBY = "isSubstitutedBy";
	/** Name of datatype property specifying individual's unique ID */
	public static final String HASUID = "hasUID";
	

	/** Name of the object property linking nodes to mexpression */
	public static final String HASMEXPRESSION = "hasMExpression";
	/** Name of the object property linking mexpression to its type*/
	public static final String TYPEOFMEXPRESSION = "typeOfMExpression";
	/** inverse of {@link #TYPEOFMEXPRESSION}*/
	public static final String ISTYPEOFMEXPRESSION = "isTypeOfMExpression";
	/** Name of the object property linking mexpressions to nodes */
	public static final String ISMEXPRESSIONOF = "isMExpressionOf";
	/** Name of the object property linking mexpressions to arguments */
	public static final String HASARGUMENT = "hasArgument";
	/** Inverse of {@link #HASARGUMENT} */
	public static final String ISARGUMENTOF = "isArgumentOf";
	/** Name of the object property linking arguments to its type (owl object)*/
	public static final String TYPEOFARGUMENT = "typeOfArgument";
	/** Name of the object property linking arguments to its type (literal type)*/
	public static final String TYPEOFDATAARGUMENT = "typeOfDataArgument";
	/** inverse of {@link #TYPEOFARGUMENT}*/
	public static final String ISTYPEOFARGUMENTIN = "isTypeOfArgumentIn";
	/** Name of the object property linking arguments to a counter (this counter is used for ordering the arguments)*/
	public static final String HASARGUMENTNUMBER = "hasArgumentNumber";
	/** Name of the object property linking MTheory to MFrag*/
	public static final String HASMFRAG = "hasMFrag";
	/** inverse of {@link #HASMFRAG}*/
	public static final String ISMFRAGOF = "isMFragOf";
	
	
	/** Name of the object property linking nodes to its parents*/
	public static final String HASPARENT = "hasParent";
	/** Inverse of {@link #HASPARENT}*/
	public static final String ISPARENTOF = "isParentOf";
	
	/**  Name of the object property linking resident nodes to input nodes pointing to them*/
	public static final String HASINPUTINSTANCE = "hasInputInstance";
	/**  Name of the object property linking resident nodes or random variables to probability distribution functions*/
	public static final String HASPROBABILITYDISTRIBUTION = "hasProbabilityDistribution";
	/**  Name of the object property linking probability distribution functions to its actual contents*/
	public static final String HASDECLARATION= "hasDeclaration";
	/**  Name of the data property specifying the possible values of a random variable*/
	public static final String HASPOSSIBLEVALUES = "hasPossibleValues";

	/**  Name of the data property specifying the owl property related to a random variable in a PR-OWL2 ontology */
	public static final String DEFINESUNCERTAINTYOF = "definesUncertaintyOf";
	/**  Name of the data property specifying the owl property related to an argument of a random variable in a PR-OWL2 ontology */
	public static final String ISSUBJECTIN = "isSubjectIn";
	/**  Name of the data property specifying the owl property related to an argument of a random variable in a PR-OWL2 ontology */
	public static final String ISOBJECTIN = "isObjectIn";
	
	/**
	 * Obtains the default prefix manager, which will be used in order to extract classes by name/ID.
	 * If ontology == null, it returns {@link #PROWL2_DEFAULTPREFIXMANAGER} 
	 * (thus, if ontology == null, it returns a PR-OWL ontology prefix).
	 * @param ontology : ontology being read.
	 * @return a prefix manager or null if it could not be created at all
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology);
	
	/**
	 * Extracts a user-friendly name from an OWL object
	 * @param owlObject
	 * @return
	 */
	public String extractName(OWLObject owlObject);
	
}
