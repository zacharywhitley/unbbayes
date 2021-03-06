package unbbayes.prs.mebn.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.ITableFunction;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.InstanceException;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.compiler.extension.IUserDefinedFunction;
import unbbayes.prs.mebn.compiler.extension.IUserDefinedFunctionBuilder;
import unbbayes.prs.mebn.compiler.extension.jpf.UserDefinedFunctionPluginManager;
import unbbayes.prs.mebn.entity.BooleanStatesEntityContainer;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;


/**
 * 
 <pre>
 BNF MEBN Table:
 ===============================================================
 distribution ::= statement | if_statement
 if_statement  ::= 
 "if" allop varsetname "have" "(" b_expression ")" statement 
 	"else" else_statement 
 allop ::= "any" | "all"
 varsetname ::= ident[["."|","]ident]*
 b_expression ::= b_term [ "|" b_term ]*
 b_term ::= not_factor [ "&" not_factor ]*
 not_factor ::= [ "~" ] b_factor
TODO b_factor ::= "(" b_expression ")" 
 		| ident ["(" arguments ")"]  "=" ident ["(" arguments ")"] 
TODO 		| external_boolean_function([arbitrary_arguments])
 arguments ::= ident[["."|","]ident]*
 else_statement ::= statement | if_statement
 statement ::= "[" assignment_or_if "]" 
TODO assignment_or_if ::= assignment_or_func [ "," if_statement] | if_statement
TODO assignment_or_func = assignment | func
TODO func ::= external_function([arbitrary_arguments]) [ "," assignment_or_func ]*
TODO assignment ::= ident "=" expression [ "," assignment_or_func ]*
 expression ::= term [ addop term ]*
 term ::= signed_factor [ mulop signed_factor ]*
 signed_factor ::= [ addop ] factor
 factor ::= number | string | function | "(" expression ")"
 function ::= possibleVal 
 	| "CARDINALITY" "(" [varsetname] ")"
 	| "MIN" "(" expression ";"|"," expression ")"
  	| "MAX" "(" expression ";"|"," expression ")"
 	| external_function([expression [";"|"," expression]])
 possibleVal ::= ident
 addop ::= "+" | "-"
 mulop ::= "*" | "/"
 ident ::= letter [ letter | digit ]*
 </pre> 
 
 string is a text between quotes
 
 <br/>
 <br/>
 TODO Built-in external functions:
<pre> 
void SET_PROBABILITY(String regex, float prob, boolean isNegativeRegex);
		Sets the probability of all states identified by regex to prob. If isNegativeRegex is true, then states not matching the regex will be considered.

float GET_SUM()
		Returns the sum of the probability of current column in CPT.

float NORMALIZE()
		Normalize the current column in CPT.

String GET_MATCHING_OV(String varSetName)
		Returns a String containing a list of OVs that matched with the if-clause condition. The list is separated by “|” (so that it can also be used as a regex).

String GET_MATCHING_STATE(String nodeName)
	Returns a String containing a list of states that matched with the if-clause condition. The list is separated by “|” (so that it can also be used as a regex). The argument is a name of parent node, with arguments.
</pre> 

<br/>
<br/>
 TODO Built-in external boolean functions:
<pre> 
CARDINALITY_COMPARISON(String b_expression, String comparisonSymbol, int arg2);
		Compares the number of parents that passed the condition in b_expression with the arg2. The comparisonSymbol can be <, >, =, <=, >=, !=.
</pre> 

<pre> 
 ================================================================
</pre> 
 
 Changes (Month/Date/Year): 
 @version 03/16/2017:
 			Description: Keywords, shorthand words, and support for external functions included.
 @version 02/16/2016:
 			Description: ordinary variables (OVs) can be used in if-clause (in order to be able to compare states of nodes with values of OVs, or values of 2 OVs), 
 			and names of nodes in if-clauses can now have arguments between parenthesis, so that a node with two input node parents pointing
 			to same resident node (but with different OVs in their arguments) can be distinguished.
 @version 08/02/2008:
 			Description: Added nested "if" feature.
 			Now an if-clause would look like this ("else" is still mandatory):
 			<pre>
 				if any x have (parentx = valuex1) [
 					myprob1 = 0.5 , 
 					myprob2 = 0.5
 				] else if all y have (parenty = valuey2) [
 					if any z have (parentz = valuez3) [
 						myprob1 = 0.2,
 						myprob2 = 0.8
 					] else [
 						myprob1 = 0.8,
 						myprob2 = 0.2
 					]
 				] else if any w have (parentw = veluew4) [
 					if any v have (parentv = valuev5 || parentv = valuev6) [
 						if any v have (parentv = valuev5 || parentv = valuev6) [
 							if any v have (parentv = valuev5 || parentv = valuev6) [
 								myprob1 = 0.8,
 								myprob2 = 0.2
 							] else [
 								myprob1 = 0.7,
 								myprob2 = 0.3
 							]
 						] else [
 							myprob1 = 0.6,
 							myprob2 = 0.4
 						]
 					] else [
 						myprob1 = 0.5,
 						myprob2 = 0.5
 					]
 				] else [
 					if all asdf have (parentasdf = valueasdf) [
 						myprob1 = 0.1 , 
 						myprob2 = 0.9
 					] else [
 						myprob1 = 0.9 , 
 						myprob2 = 0.1
 					]
 				]
 			</pre>
 
 	@version 07/09/2008:
 			Description: BNF fails to describe a complex boolean expression
 	<br/>
 	<br/>
 	
 	@version 06/15/2008:
 			Description: a boolean expression was returning a boolean neutral value (false in "Any" 
 			and true in "All") when no valid expression (involving parents) was evaluated. It
 			is now returning false when no expression was valid
 	<br/>
 	<br/>
 
 	@version 03/03/2008:
 			Description: compiler was trying to parse a table even when node was
 			known to be a finding (a finding doesn't need a cpt). This condition now 
 			is tested before parsing a ssbn node.
 	<br/>
 	<br/>
 
 	@version 12/28/2007:
 			Description: fixed the BNF definition of factor, and
 			added the " char before and after ;.
 	<br/>
 	<br/>
 
 	@version 11/27/2007:
 			Description: term ::= signed_factor [ mulop factor ]* changed to
 				term ::= signed_factor [ mulop signed_factor ]*,
 				and CPT generation is in alpha state now.
 	<br/>
 	<br/>
 
 	@version 11/25/2007:
 			Description: added non-terminal variable "possibleVal" to the grammar (and its implementation).
 	<br/>
 	<br/>
 	
 	@version 10/07/2007:
 			Description: "varsetname" has been added to the grammar (and implemented inside the class)
 				in order to allow us to declare parent set by strong OV.
 	<br/>
 	<br/>
 	
 
 	@version 06/24/2007:
 			Description: The top level BNF Grammar class was changed from 
 				if_statement to table, in order to make possible a probability
 				table without an if clause.
 	<br/>
 	<br/>
 
 	@version 06/10/2007: 
 			Description: Added cardinality(), min() and max() functions
 			syntax analyzer.
 	<br/>
 	<br/>

 	@version 05/29/2007: 
 			Description: the else clause is now required, in order to
 				force user to declare a default distribution and
 				grant declaration of every possible combination of states
 				(if we don't add this restriction, a semantic analysis
 				would be required in order to verify if every combination
 				of states were provided and the last else
 				must be related to the first if every time - since
 				we don't have a block sentence yet, it is not possible).
 	<br/>
 	<br/>
 				
 	@version 06/03/2014: 
 			Description: CARDINALITY() was included. That is, if CARDINALITY
 			is called with no argument, then it will return the number of all parents.
 	<br/>
 	<br/>
 			
 	@version 09/30/2015:
 			Description: restrictions in varsetname were loosen. 
 			Previously, if I declare x.y.z, then only the nodes containing all x,y,
 			and z ordinary variables in their arguments were considered.
 			Now, all nodes containing at least x, y, or z ordinary variables
 			in their arguments are being considered.
 			Additionally, we can separate varsetname with commas (',').
 	<br/>
 	<br/>
 			
 	@version 10/08/2015:
 			Description: Included support for unnormalized values (i.e. declaration of values that doesn't need to sum up to 1).
 			{@link #setToNormalize(boolean)} can be used to disable/enable this feature.
 	<br/>
 	<br/>
 	
 	@version 02/13/2016:
 			Description: Included arguments in b_factor
 	<br/>
 	<br/>
    
	@Author Shou Matsumoto (cardialfly@[gmail,yahoo].com)
	@Author Rommel Carvalho (rommel.carvalho@gmail.com)		
 */
public class Compiler implements ICompiler {
	// TODO migrate to simpler parsers, like stream/string tokenizer or Antlr
	// TODO report a problem and next compilation reports next problem (i.e. multiple compilation error report).
	
	/** This is a default instance of {@link #getTableNormalizer()} */
	public static final ITableFunction DEFAULT_NORMALIZE_TABLE_FUNCTION = new NormalizeTableFunction();

	private Comparator<List<INode>> cacheParentsComparator = new Comparator<List<INode>>() {
		public int compare(List<INode> o1, List<INode> o2) {
			if (o1.size() > o2.size()) {
				return 1;
			} else if (o1.size() < o2.size()) {
				return -1;
			} else {
				for (int i = 0; i < o1.size(); i++) {
					int compared = o1.get(i).getName().compareTo(o2.get(i).getName());
					if (compared != 0) {
						return compared;
					}
				}
			}
			return 0;
		}
	};

	private Map<String, Map<List<INode>, IProbabilityFunction>> nameToParentProbValuesCache = new HashMap<String, Map<List<INode>, IProbabilityFunction>>();

	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of compiler
     */
    private static class SingletonHolder { 
    	private static final Compiler INSTANCE = new Compiler();
    }
 
    /** If this class' constructor should return a singleton or not */
	private static Boolean singleton = false;
	
	// resource files
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.prs.mebn.compiler.resources.Resources.class.getName(),
			Locale.getDefault(),
			Compiler.class.getClassLoader()
		);

	
	/* A previously read character (lookahead) */
	private char look;

	/* Current text cursor position (where inside "text" we're scanning now) */
	private int index = 0;

	private char[] text = null;

	/* keywords. */
	private String kwlist[] = { "IF", "ELSE", "ALL", "ANY", "HAVE" };

	/*
	 * Special codes for keywords.
	 * Obviously, it should represent kwlist.
	 */
	private char kwcode[] = { 'i', 'l', 'a', 'y', 'h' };

	/* coded token */
	private char token;

	/*
	 * uncoded token's value
	 */
	private String value = "";
	private String noCaseChangeValue = "";
	
	
	// Informations used by this class to check pre-SSBN consistency
//	private MultiEntityBayesianNetwork mebn = null;
	private ResidentNode node = null;
	

	// Variables used for ProbabilisticTable generation
	private PotentialTable cpt = null;
	private SSBNNode ssbnnode = null;
	
	/*
	 * This temporally represents a parsed CPT, to make potential table generation easier, like:
	 * 
	 * (TempTableHeaderCell)      (TempTableHeaderCell)      (TempTableHeaderCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 * 
	 * Where TempTableHeaderCell contains the parent and its expected value in the table.
	 * TempTableProbabilityCell represents a possible value of THIS node and its occurrence
	 * probability.
	 * 
	 */
	private TempTable tempTable = null;
	private TempTableHeaderCell currentHeader = null;
	//private List<TempTableProbabilityCell> currentProbCellList = null;
	//private TempTableProbabilityCell currentCell = null;
	
	
	private int originalTextLength = 0;	// stores the length of the original text before deleting extra spaces
	
	// if true, varSetName must use exact match for strong OVs. If false, then all parents containing at least one of the OVs will be considered
	private boolean isExactMatchStrongOV = false;

	private boolean isToNormalize = true;

	private ITableFunction tableNormalizer = DEFAULT_NORMALIZE_TABLE_FUNCTION;

	private UserDefinedFunctionPluginManager userFunctionPluginManager = UserDefinedFunctionPluginManager.getInstance(false);
	
	/**
	 * Because at least one constructor must be visible to subclasses in order to allow
	 * inheritance, we use protected instead of private.
	 */
	protected Compiler() {
		tempTable = new TempTable();
		originalTextLength = 0;
		this.cpt = null;
	}
	
	
//	/**
//	 * Creates an instance of Compiler. The resident node is necessary
//	 * in order to perform semantic consisntency check.
//	 * @param node: a resident node containing the table to parse
//	 * @return a instance of the compiler.
//	 */
//	protected Compiler (ResidentNode node) {
//		this();
//		this.setNode(node);
//	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * Depending on the application.properties file read by {@link ApplicationPropertyHolder}, 
	 * this method may return a singleton instance.
	 * @param node: a resident node containing the table to parse
	 * @param ssbnnode : a node actually generating cpt table at ssbn generation time. It is optional
	 * @return a instance of the compiler.
	 * @see {@link ApplicationPropertyHolder}
	 */
	public static Compiler getInstance(ResidentNode node, SSBNNode ssbnnode) {
		Compiler comp = null;
		try {
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
    		if (Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
    				Compiler.class.getCanonicalName()+".singleton").toString())) {
        		comp = SingletonHolder.INSTANCE;
        	} else {
        		comp = new Compiler();
        	}
		} catch (Exception e) {
//			Debug.println(Compiler.class, "Using default LPD compiler", e);
			comp = new Compiler();
		}
		comp.setNode(node);
		comp.ssbnnode = ssbnnode;
		if (comp.ssbnnode != null) {
			if (comp.ssbnnode.getProbNode() != null) {
				comp.cpt = comp.ssbnnode.getProbNode().getProbabilityFunction();
			}			
		}
		return comp;
	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * @param node: a resident node containing the table to parse
	 * @return a instance of the compiler.
	 * @see {@link Compiler#getInstance(ResidentNode, SSBNNode)}
	 */
	public static Compiler getInstance(ResidentNode node) {
	// since we are not using other specific pseudocode Compilers, and we do not use Builders/Factories,
	// it is not necessary to have a constructor method...
		return Compiler.getInstance(node, null);
	}
	
	
//	/**
//	 * Note: if the pseudocode passed to this class is either empty or null, this class
//	 * should consider equal probability distribution for all possible states.
//	 * TODO break this class apart, because it's becoming too huge.
//	 * @param node: the node having the CPT's pseudocode being evaluated by this class.
//	 * @param ssbnnode: the node where we should set the output CPT to.
//	 */
//	protected Compiler (ResidentNode node, SSBNNode ssbnnode) {
//		this(node);
//		this.ssbnnode = ssbnnode;
//		if (this.ssbnnode != null) {
//			if (this.ssbnnode.getProbNode() != null) {
//				this.cpt = this.ssbnnode.getProbNode().getPotentialTable();
//			}			
//		}
//	}
	
	/**
	 * This method just calls {@link #init(text, true)}.
	 * @see #init(String, boolean)
	 */
	public void init(String text) {
		this.init(text, true);
	}
	
	/**
	 * Compiler's initialization.
	 * @param isToResetCache : if this parameter is true, 
	 * this method also resets cache used in {@link #generateLPD(SSBNNode)}.
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#init(java.lang.String)
	 * @see #getNameToParentProbValuesCache()
	 * @see #clearCache()
	 */
	public void init(String text, boolean isToResetCache) {
		if (isToResetCache) {
			this.clearCache();
		}
		this.originalTextLength = 0;
		if (text == null) {
			this.originalTextLength = 0;
			this.text = null;
			return;
		} else if (text.length() == 0) {
			/*
			 * testing if ((text == null) || text.isEmpty()) would be much better, but
			 * since some non-standard JVM could not to stop testing boolean OR condition
			 * after finding a true value, the text.isEmpty() might be executed without
			 * testing if it is null, which may cause NullPointerException.
			 */
			this.originalTextLength = 0;
			this.text = null;
			return;
		}
		index = 0;
		// Debug.println("************************************");
		// Debug.println("ORIGINAL: " + text);
		//if (text != null) {
			originalTextLength = text.length();
			text = text.replaceAll("\\s+", " ");
		//}
		
		// Debug.println("CHANGED: " + text);
		// Debug.println("************************************");
		//if (text != null) { 
			this.text = text.toCharArray();
			nextChar();
		//}
			
		getUserFunctionPluginManager().initialize();
		
		tempTable = new TempTable();
	}
	
	/**
	 * use this method to initialize this parser on SSBN generation step.
	 * All information will be extracted from the ssbnnode.
	 * @param ssbnnode
	 */
	public void init(SSBNNode ssbnnode) {
		this.setSSBNNode(ssbnnode);
		//this.node = ssbnnode.getResident();	//setSSBNNode already does it.
//		this.mebn = this.node.getMFrag().getMultiEntityBayesianNetwork();
		String pseudocode = this.node.getTableFunction();
		
		if (this.ssbnnode.getProbNode() != null) {
			this.setPotentialTable(this.ssbnnode.getProbNode().getProbabilityFunction());			
		}
		// do not clar cache
		this.init(pseudocode, false);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException {
		//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>(); //Initialize lists
		if (this.text == null) {
			// Debug.println("Pseudocode = null");
			return;
		}
		// Debug.println("PARSED: ");
		this.skipWhite();
		this.distribution();
	}
	
	/**
	 * this is identical to init(table)->parse()
	 * @param table
	 * @throws MEBNException
	 */
	public void parse(String table) throws MEBNException {
		this.init(table);
		this.parse();
	}
	
	/**
	 * This method actually regenerates a CPT.
	 * This method will not parse. Use parse() before this.
	 * Please, note that this method will generate linear (equal) distribution
	 * if no pseudocode is declared (all values will have same probability).
	 * @return GENERATED potential table
	 */
	protected PotentialTable getCPT() throws InconsistentTableSemanticsException,
											InvalidProbabilityRangeException,
											InstanceException{
		
		// try to clean garbage before starting an expensive method like this one
//		System.gc();
		
		// initial tests
		if (this.ssbnnode == null) {
			return null;
		}
		if (this.tempTable == null) {
			return null;
		}
		
//		if (this.ssbnnode.isFinding()) {
//			return null;
//		}
		
		if (this.ssbnnode.getProbNode() == null) {
			return null;
		}
		if (this.text == null || this.tempTable.isEmptyNestedClauses() ) {
			// Special condition: if pseudocode was not declared, use linear (equal) distribution instead
			this.generateLinearDistroCPT(this.ssbnnode.getProbNode());
			return this.ssbnnode.getProbNode().getProbabilityFunction();
		}

		// moved to generateLPD, before initializing and parsing LPD script again
//		// check cache
//		Map<Collection<INode>, float[]> cache = this.getNameToParentProbValuesCache().get(this.ssbnnode.getProbNode().getName());
//		try {
//			if (cache != null) {
//				for (Collection<INode> parents : cache.keySet()) {
//					if (parents.size() == this.ssbnnode.getParents().size() 
//							&& parents.containsAll(this.ssbnnode.getParents())){
//						// cache and current have the same parents
//						this.cpt = this.ssbnnode.getProbNode().getProbabilityFunction();
//						// populate column
//						float[] value = cache.get(parents);
//						for (int i = 0; i < this.cpt.tableSize(); i++) {
//							this.cpt.setValue(i, value[i]);
//						}
//						return this.cpt;
//					}
//				}
//			}
//		} catch (Exception e) {
//			// ignore
//			Debug.println(getClass(), e.getMessage(), e);
//		}
		
		// initialization
		
		// sets all parents current mfrag as the same of this ssbn node, in order to use same OV names
		// use a map to store previous states, in order to rollback at the end of CPT generation
		Map<SSBNNode, MFrag> parentToPreviousMFragMap = new HashMap<SSBNNode, MFrag>();
		for (SSBNNode parent : this.getSSBNNode().getParents()) {
			if (parent.getCurrentlySelectedMFragByTurnArguments() != null) {
				parentToPreviousMFragMap.put(parent, parent.getCurrentlySelectedMFragByTurnArguments());
			} else {
				//defaults to resident node's mfrag
				parentToPreviousMFragMap.put(parent, parent.getResident().getMFrag());
			}
			try {
				parent.turnArgumentsForMFrag(this.getSSBNNode().getResident().getMFrag());
			} catch (Exception e) {
//				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		// eliminates redundancies on table's boolean expression
		try {
			this.tempTable.cleanUpKnownValues(this.getSSBNNode());
		} catch (NullPointerException e) {
			// The SSBNNode was null...
			throw new InstanceException(getNode().toString(),e);
		}
		
		// extracting base values
		this.cpt = this.ssbnnode.getProbNode().getProbabilityFunction();
		
		ArrayList<SSBNNode> parents = null;
		try {
			parents = new ArrayList<SSBNNode>(this.ssbnnode.getParents());
		} catch (NullPointerException e) {
			parents = new ArrayList<SSBNNode>();
		} catch (Exception e) {
			throw new InconsistentTableSemanticsException(getNode().toString(),e);
		}
		
		
		ArrayList<Entity> possibleValues = new ArrayList<Entity>(this.ssbnnode.getActualValues());
		if (!this.ssbnnode.isFinding() && ( this.ssbnnode.getProbNode().getStatesSize() != possibleValues.size()  )) {
			// the ssbnnode and the table is not synchronized!!
			throw new InconsistentTableSemanticsException(this.ssbnnode.toString());
		}
		
		
		Map<String, List<EntityAndArguments>> residentNameToCurrentValueMap = null; // parameter of boolean expression evaluation method
		
		// this iterators helps us combine parents' possible values
		// e.g. (True,Alpha), (True,Beta), (False,Alpha), (False,Beta).
		List<Iterator<Entity>> valueCombinationIterators = new ArrayList<Iterator<Entity>>();
		// variable which stores nodes not belonging to chain generated when getResident().isToLimitQuantityOfParentsInstances() == true
		// TODO guarantee that nodes in the chain contains the same states of the other (non-chain) node.
//		SSBNNode nodeNotInChain = null;		
		for (SSBNNode ssbnnode : parents) {
//			if (nodeNotInChain == null && ssbnnode.getResident().isToLimitQuantityOfParentsInstances()) {
//				nodeNotInChain = ssbnnode;
//			}
			valueCombinationIterators.add(ssbnnode.getActualValues().iterator());
		}
		
		// saves the current values of the iterators
		List<Entity> currentIteratorValue = new ArrayList<Entity>();
		for (Iterator it : valueCombinationIterators) {
			 currentIteratorValue.add((Entity)it.next());
		}
		
		
		// start running at the probabilistic table and filling its cells
		
		
		// Keep track of how many cells in the table are yet to be handled. Repeat until this is zero. 
		// If cells are independent each other, this should become zero in just 1 do-while iteration 
		int numUnhandledCells = this.cpt.tableSize();	
		do {
			// keep track of how many cells we still need to handle at this iteration.
			// If cells are independent, numUnhandledCells will become 0 after single do-while iteration.
			// If the value in a cell depend on a value of another cell, we might need to swipe the table multiple times to guarantee that all dependent cells are filled.
			// Keeping track of how many cells we still need to handle will help in deciding whether to stop swiping the table or not.
			int numUnhandledCellsBeforeIteration = numUnhandledCells;	
			
			for( int i = 0; i < this.cpt.tableSize(); i += this.ssbnnode.getProbNode().getStatesSize()) {
				//	clears and initializes map
				residentNameToCurrentValueMap = new HashMap<String, List<EntityAndArguments>>();
				// fill map with SSBN nodes (i.e. parent nodes)
				for (SSBNNode ssbnnode : parents) {
					if (ssbnnode.getResident().isToLimitQuantityOfParentsInstances()) {
						// do not add nodes in the chain which limits the max quantity of parents.
						// this is because such parents must be considered as instances of the other parent
						continue;
					}
					if (!residentNameToCurrentValueMap.containsKey(ssbnnode.getResident().getName())) {
						residentNameToCurrentValueMap.put(ssbnnode.getResident().getName(), new ArrayList<EntityAndArguments>());
					}
				}
				
				// fill map at this loop. Note that parents.size, currentIteratorValue.size, and
				// valueCombinationiterators are the same
				for (int j = 0; j < parents.size(); j++) {
					SSBNNode parentSSBNNode = parents.get(j);
					// val will be added to map using following key
					String key = parentSSBNNode.getResident().getName();
					if (parentSSBNNode.getResident().isToLimitQuantityOfParentsInstances()) {
						// nodes in the chain which limits the max quantity of parents has special meaning:
						// such parents must be considered as instances of another parent, although it points to another resident node
						// in order to simulate such behavior, we adjust the content of map
						if (j != 0) {
							// if this is not the first parent, use the first parent as the "another" parent
							key = parents.get(0).getResident().getName();
						} else if ( (parents.size() - 1) != j ) {
							// use the last element instead, because the special parent is the first parent
							key = parents.get((parents.size() - 1)).getResident().getName();
						} else {
							// there is only 1 parent, and the parent is a node in the chain which limits the max quantity of parents.
							// this is an error, because such chain must contain at least 2 nodes: the dynamically generated node, and one of the original parent.
							throw new IllegalStateException("Node " + getSSBNNode() + " has only 1 parent (" + parents.get(j) + "), but the parent has isToLimitQuantityOfParentsInstances() == true.");
						}
					}
					EntityAndArguments val = new EntityAndArguments(currentIteratorValue.get(j),new ArrayList<OVInstance>(parentSSBNNode.getArguments()));
					residentNameToCurrentValueMap.get(key).add(val);
				}
				
				MultiEntityBayesianNetwork mebn = getMEBN();
				
				// fill map with ordinary variables in this mfrag (these are like identity nodes -- function that returns the value of its argument)
				for (OrdinaryVariable ov : this.getNode().getMFrag().getOrdinaryVariableList()) {
					if (ov == null || ov.getValueType() == null || ov.getName() == null) {
						Debug.println(getClass(), ov + " is a null ordinary variable, or it is an ordinary variable with no type. It will be ignored.");
						continue;	// ignore this ov
					}
					if (!residentNameToCurrentValueMap.containsKey(ov.getName())) {
						// extract the object entity associated with this ov
						ObjectEntity objectEntity = mebn.getObjectEntityContainer().getObjectEntityByType(ov.getValueType());
						if (objectEntity == null) {
							Debug.println(getClass(), ov.getValueType().getName() + " is not a known object entity. " + ov + " will be ignored.");
							continue;	// ignore this ov, because it is not associated with an object entity
						}
						// fill map with possible values of this ov (i.e. possible instances of this object entity)
						ArrayList<EntityAndArguments> valueToAssign = new ArrayList<EntityAndArguments>();
						for (ObjectEntityInstance objectEntityInstance : objectEntity.getInstanceList()) {
							// again, OVs in LPDs are like identity nodes (i.e. something like Id(OV1) = OV1)
							valueToAssign.add(new EntityAndArguments(objectEntityInstance, Collections.singletonList(OVInstance.getInstance(ov, objectEntityInstance))));
						}
						residentNameToCurrentValueMap.put(ov.getName(), valueToAssign);
					}
				}
				
				// updates iterators
				for (int j = 0; j < valueCombinationIterators.size(); j++) {
					if (valueCombinationIterators.get(j).hasNext()) {
						// if has next, then update current value and exits loop
						currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
						break;
					} else {
						// else, reset the iterator (and current value) until exit loop
						valueCombinationIterators.set(j, parents.get(j).getActualValues().iterator());
						if (valueCombinationIterators.get(j).hasNext()) {
							currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
						}
					}
				}
				
				
				// prepare to extract which column to verify
				TempTableHeaderCell header = null;
				
				// if default distro, then use the default header...
				// also, if no parents are declared, use default distro...
				boolean thereAreNoParents = false;
				if(this.getSSBNNode().getParents() == null) {
//				thereAreNoParents = true;
					// if table also does not use the embedded node feature (i.e. parents that are not explicit in MFrag, but used in LPD), then there are no parents.
					// E.g. we can use OVs in the if-clause. In this case, the LPD behaves like when we have identity nodes (node that returns its argument) as parents.
					thereAreNoParents = !this.tempTable.hasEmbeddedNodeDeclaration();
				} else if (this.getSSBNNode().getParents().size() == 0) {
					thereAreNoParents = !this.tempTable.hasEmbeddedNodeDeclaration();
				}
				if (thereAreNoParents || this.getSSBNNode().isUsingDefaultCPT()) {
					// we assume the default distro is the last block on pseudocode
					header = this.tempTable.getDefaultClause();
					// let's just check if this header is really declaring a default distro... Just in case...
					if (!header.isDefault()) {
						throw new InconsistentTableSemanticsException(getSSBNNode().toString());
					}
				} else {
					//	if not default, look for the column to verify
					// the first expression to return true is the one we want
					header = this.tempTable.getFirstTrueClause(residentNameToCurrentValueMap);
					// note that default (else) expression should allways return true!
					
				}
				
				
				// populate column
				for (int j = 0; j < possibleValues.size() ; j++) {
					float value = 0f;
					
					// extract the value to set
					for (TempTableProbabilityCell cell : header.getCellList()) {
						// we assume the Probabilistic table is in the same order of the list "possibleValues", 
						// so we look for the entity of possibleValues at a cell
						if (cell.getPossibleValue().getName().equalsIgnoreCase(possibleValues.get(j).getName())) {
							value = cell.getProbabilityValue();
							break;
						}
					}
					// consistency check, allow only positive values
					if (isToNormalize()
							&& ((value < 0) 
//								|| (value > 1)
									)) {
						throw new InvalidProbabilityRangeException(getNode().toString() + " = " + value);
					}
					this.cpt.setValue(i+j, value );
					
					// NaN indicates that this cell cannot be calculated now.
					if (!Float.isNaN(value)) {
						numUnhandledCells--;	// tell that we just handled this cell
					}
				}
				
			}	// while i < this.cpt.tableSize()
			
			/* 
			 * If this iteration did not handle any cell, this means we are stuck (no matter how many times we sweep the table, no updates will happen). 
			 * This may happen if there is a cyclic dependence between values of cell.
			 * For example, when cell X depends on value of cell Y which depends on value of cell X.
			 */
			if (numUnhandledCellsBeforeIteration == numUnhandledCells) {
				throw new RuntimeException("CyclicCellDependency");
			}
			// keep iterating
		} while (numUnhandledCells > 0);
		
		// the code below is commented because calling getCPT twice must be working nicely.
//		// dispose temporary table, because since it is useless anymore
//		try{
//			this.tempTable.clear();
//		} catch (UnsupportedOperationException uoe) {
//			uoe.printStackTrace();
//			
//		}
		
		try {
			if (isToNormalize()) {
				getTableNormalizer().applyFunction((ProbabilisticTable)this.cpt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// rollback MFrag settings
		for (SSBNNode parent : parentToPreviousMFragMap.keySet()) {
			try{
				parent.turnArgumentsForMFrag(parentToPreviousMFragMap.get(parent));
			} catch (Exception e) {
//				e.printStackTrace();
				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		// migrated to generateLPD
//		// prepare to fill cache
//		if (cache == null) {
//			cache = new HashMap<Collection<INode>, float[]>();
//			this.getNameToParentProbValuesCache().put(this.ssbnnode.getProbNode().getName(), cache);
//		}
//		// fill cache
//		cache.put((Collection)this.ssbnnode.getParents(), this.cpt.getValues());
		
		return this.cpt;
	}
	
	public void clear() {
		this.tempTable = null;
		this.cpt = null;
		this.currentHeader = null;
		this.kwcode = null;
		this.kwlist = null;
//		this.mebn = null;
		this.noCaseChangeValue = null;
		this.node = null;
		this.ssbnnode = null;
		this.text = null;
		this.value = null;
		this.clearCache();
//		System.gc();
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#generateCPT()
	 */
	protected IProbabilityFunction generateCPT() throws MEBNException {
		return this.generateLPD(this.getSSBNNode());
	}

	
	/**
	 * 
	 * @see unbbayes.prs.mebn.compiler.ICompiler#generateCPT(unbbayes.prs.mebn.ssbn.SSBNNode)
	 * @deprecated use {@link #generateLPD(SSBNNode)} instead.
	 */
	public PotentialTable generateCPT(SSBNNode ssbnnode) throws MEBNException {
		IProbabilityFunction pf = this.generateLPD(ssbnnode);
		if (pf instanceof PotentialTable) {
			return (PotentialTable)pf;
		}
		return null;
	}
	
	/**
	 * this is identical to init(ssbnnode) -> parse() -> getCPT.
	 * It will use cache if CPT was cached previously.
	 *  @return the generated LPD
	 *  @see #getCachedLPD(SSBNNode)
	 *  @see #putCPTToCache(SSBNNode, PotentialTable)
	 */
	public IProbabilityFunction generateLPD(SSBNNode ssbnnode) throws MEBNException {
//		System.gc();
		if (ssbnnode == null || ssbnnode.getProbNode() == null) {
			return null;
		}
//		if (ssbnnode.isFinding()) {
//			return null;
//		}
		
		// check content of cache
		IProbabilityFunction cachedLPD = this.getCachedLPD(ssbnnode);
		if (cachedLPD != null) {
			if (cachedLPD instanceof PotentialTable) {
				PotentialTable cachedCPT = (PotentialTable) cachedLPD;
				// copy only the content (values), because the variables (parent and current nodes) are not actually the same instances compared to the previous time this method was called
				PotentialTable cpt  = ssbnnode.getProbNode().getProbabilityFunction();
				cpt.setValues(cachedCPT.getValues());
				return cpt;
			} else {
				Debug.println(getClass(), "Compiler is not returning a PotentialTable. Cache may not be usable, thus we are not using cache.");
			}
		}
		// actually compile pseudo code and obtain cpt
		this.init(ssbnnode);
		this.parse();
		PotentialTable cpt = getCPT();
		
		this.putCPTToCache(ssbnnode, cpt);
		
		return cpt;
	}
	
	/**
	 * Inserts an entry into cache of cpts by node.
	 * @param ssbnnode : this is going to be the key of the cache. 
	 * More technically, the name of this node and all its parents {@link SSBNNode#getParents()} are going to be
	 * used as the key.
	 * @param cpt : content (probability distribution) of this CPT is going to be the cached value
	 * @see #getNameToParentProbValuesCache() 
	 * @see #generateLPD(SSBNNode) 
	 * @see #init(String)
	 */
	protected void putCPTToCache(SSBNNode ssbnnode, IProbabilityFunction cpt) {
		// initial assertion
		if (ssbnnode == null || ssbnnode.getProbNode() == null || cpt == null) {
			return;
		}
		
		// prepare to fill cache
		if (this.getNameToParentProbValuesCache() == null) {
			this.setNameToParentProbValuesCache(new HashMap<String, Map<List<INode>,IProbabilityFunction>>());
		}
		
		Map<List<INode>, IProbabilityFunction> cache = this.getNameToParentProbValuesCache().get(ssbnnode.getProbNode().getName());
		if (cache == null) {
			cache = new TreeMap<List<INode>, IProbabilityFunction>(this.getCacheParentsComparator());
			this.getNameToParentProbValuesCache().put(ssbnnode.getProbNode().getName(), cache);
		}
		// fill cache
		cache.put(ssbnnode.getParentNodes(), cpt);
	}

	/**
	 * Obtains a CPT from cache.
	 * @param ssbnnode : this is the key of the cache.
	 * More technically, the name of this node and all its parents {@link SSBNNode#getParents()} are going to be
	 * used as the key.
	 * @return : cached cpt or null if the key was not found.
	 * @see #getNameToParentProbValuesCache() 
	 * @see #generateLPD(SSBNNode)  
	 * @see #init(String)
	 */
	protected IProbabilityFunction getCachedLPD(SSBNNode ssbnnode) {
		try {
			Map<List<INode>, IProbabilityFunction> cache = this.getNameToParentProbValuesCache().get(ssbnnode.getProbNode().getName());
			if (cache != null) {
				// cached value
				IProbabilityFunction cachedLPD = cache.get(ssbnnode.getParents());
				if (cachedLPD != null) {
					return cachedLPD;
				}
//				for (Collection<INode> parents : cache.keySet()) {
//					if (parents.size() == ssbnnode.getParents().size() 
//							&& parents.containsAll(ssbnnode.getParents())){	// cache and current have the same parents
//						// cached value
//						IProbabilityFunction cachedLPD = cache.get(parents);
//						return cachedLPD;
//					}
//				}
			}
		} catch (Exception e) {
			// do nothing
			Debug.println(getClass(), e.getMessage(), e);
		}
		return null;
	}


	/**
	 * This method just fills the node's probabilistic tables w/ equal values.
	 * For instance, if a node has 4 possible values, then the table will contain
	 * only cells w/ 25% value (1/4).
	 * @param probNode
	 */
	public IProbabilityFunction generateLinearDistroCPT(ProbabilisticNode probNode) {
		float value = 1.0F / probNode.getStatesSize();
		if (!isToNormalize()) {
			// if we don't need to normalize, then fill everything with zeros
			value = 1f;
		}
		PotentialTable table = probNode.getProbabilityFunction();
		if (table != null) {
			for (int i = 0; i < table.tableSize(); i++) {
				// TODO in float operation, since 1/3 + 1/3 + 1/3 might not be 1, implement some precision control
				table.setValue(i, value);
			}
		}
		return table;
	}
	
	
	/**
	 * distribution := statement | if_statement
	 */
	protected void distribution() throws NoDefaultDistributionDeclaredException,
			InvalidConditionantException,
			SomeStateUndeclaredException,
			InvalidProbabilityRangeException,
			TableFunctionMalformedException{
		this.table();
	}
	
	/**
	 * @deprecated
	 * @see #distribution()
	 */
	protected void table() throws NoDefaultDistributionDeclaredException,
	  							InvalidConditionantException,
	  							SomeStateUndeclaredException,
	  							InvalidProbabilityRangeException,
	  							TableFunctionMalformedException{
		
		if (this.look == '[') {
			// Debug.println("STARTING DEFAULT STATEMENT");	
			
			// Prepare temporary table's header to declare a default (no-if-clause) statement
			this.currentHeader = new TempTableHeaderCell(null, true, true, this.ssbnnode);
			this.tempTable.addNestedClause(this.currentHeader);			
			
			// every exceptions would be thrown up...
			statement(this.currentHeader);
			
		} else {
			// We don't have to prepare temporary table's header to declare a if-clause statement
			// because the if statement parser would do so.
			
			// Please note table() repasses every exception reported by ifStatement()
			
			// evaluate if-statement setting the tempTable as the upper statement container
			this.ifStatement(this.tempTable);
		}
		
		// after the final else clause, no declaration should be present
		this.skipWhite();
		if (this.look != ' ') {
			expected("end of declaration");
		}
		
	}

	/**
	 * if_statement ::= "if" allop ident "have" "(" b_expression ")" statement [
	 * "else" else_statement ]
	 * @param upperIf: if this if clause is an inner if, the upper if-clause should be referenced
	 * by this parameter. If this is a temporary table, this if-clause is the upper-most if-clause within CPT pseudocode.
	 * If it is null, it will assume tempTable is the upper container
	 */
	protected void ifStatement(INestedIfElseClauseContainer upperIf) 
							   throws NoDefaultDistributionDeclaredException,
									  InvalidConditionantException,
									  SomeStateUndeclaredException,
									  InvalidProbabilityRangeException,
									  TableFunctionMalformedException{
		// Debug.println("PARSING IF STATEMENT");
		// SCAN FOR IF. Note that any blank spaces were already skipped
		scan();
		matchString("IF");
		
		
		// SCAN FOR ALL/ANY
		scan();
		switch (token) {
		case 'a':
			// Debug.println("ALL VERIFIED");
			// sets the table header w/ this parameters (empty list,false,false): empty list (no verified parents), is not ANY and is not default
			this.currentHeader = new TempTableHeaderCell(new ArrayList<TempTableHeader>(), false, false, this.ssbnnode);
			break;
		case 'y':
			// Debug.println("ANY VERIFIED");
			//	sets the table header w/ this parameters (empty list,true,false): empty list (no verified parents), is ANY and is not default
			this.currentHeader = new TempTableHeaderCell(new ArrayList<TempTableHeader>(), true, false, this.ssbnnode);
			break;
		default:
			expected("ALL or ANY");
		}

		// stores this.currentHeader in order to become an upper clause of any further nested if/else clause
		INestedIfElseClauseContainer currentIfContainer = this.currentHeader;

		
		// adds the header to the container (table or upper if/else-clause) before it is changed to another header.
		if (upperIf == null) {
			// No upper container identified. Let's assume to be the upper-most container (the temporary table)
			upperIf = this.tempTable;			
		} 		
		upperIf.addNestedClause(this.currentHeader);
		
		// SCAN FOR varsetname
		String varSetName = this.varsetname();
		this.currentHeader.setVarsetname(varSetName);
		// Debug.println("SCANNED VARSETNAME := " + varSetName);

		// SCAN FOR HAVE
		// Debug.println("SCAN FOR HAVE");
		scan();
		matchString("HAVE");

		// ( EXPECTED
		match('(');
		// if we catch sintax error here, it may be conditionant error
		
		// Now, parsing a boolean expression - tree format (we'll store it inside this variable)
		ICompilerBooleanValue expressionTree = null;
		
		try {
			expressionTree = bExpression();
		} catch (TableFunctionMalformedException e) {
			throw new InvalidConditionantException(getNode().toString() , e);
		}
		//this.nextChar();
		//this.skipWhite();
		// Debug.println("LOOKAHEAD = " + look);
		// ) EXPECTED
		match(')');
		
		// since we extracted the expression tree, store it inside the current header in temporary table
		if (expressionTree != null) {
			this.currentHeader.setBooleanExpressionTree(expressionTree);
		} else {
			throw new InvalidConditionantException(this.node.toString());
		}
		
		// Debug.println("STARTING STATEMENTS");
		
		// if we catch a sintax error here, it may be a value error
//		try {
			// if there is a nested if, this if should be the upper clause (set currentHeader as upper clause).
			statement(currentIfContainer);
//		} catch (TableFunctionMalformedException e) {
//			// Debug.println("->" + getNode());
//			throw new InvalidProbabilityRangeException("["+this.getNode().getName()+"]",e);
//		}
		
		
		
		// Debug.println("LOOKING FOR ELSE STATEMENT");
		// LOOK FOR ELSE
		// Consistency check C09: the grammar "may" state else as optional,
		// but semantically every table must have a default distribution, which is
		// declared within an else clause.
		
		// We dont have to create a new temp table header, because else_statement would do so.
		
		//	This test is necessary to verify if  there is an else clause
		if (this.index < this.text.length) {
			try {
				scan();
			} catch (TableFunctionMalformedException e) {
				// a sintax error here represents a statement other than an else statement
				throw new NoDefaultDistributionDeclaredException(e);
			}
		} else {
			// No statement was found at all (that means no else statement).
			// Debug.println("END OF TABLE");
			throw new NoDefaultDistributionDeclaredException(getNode().toString());
		}
		
		if (token == 'l') {
			// The else statement should be a child statement of the upper container,
			// that means, it is on the same level of currently evaluated IF clause
			else_statement(upperIf);
		} else {
			// The statement found was not an else statement
			throw new NoDefaultDistributionDeclaredException(getNode().toString());
		}
		
		// we may have another if/else clause after this...
		
	}
	
	/**
	 *   It skippes white spaces after evaluation.
	 *   varsetname ::= ident[["."|","]ident]*
	 *   @return a string containing "varsetname" (e.g. "st.sr.z")
	 */
	protected String varsetname() throws TableFunctionMalformedException {
		
		// we don't have to set header's varsetname here because ifStatement (upper caller) would do so.
		
		String ret = "";	// a string containing "varsetname" (e.g. "st.sr.z")
		
		// scan for the ident
		do {
			
//			scanNoSkip();	// no white spaces should stay between ident and "." and next ident
			scan();	
			
			if (token == 'x') {
				// Debug.println("SCANING IDENTIFIER " + value);
				ret += this.noCaseChangeValue;
			} else {
				expected("Identifier");
			}	
			
			// search for ["." ident]* loop
			if (this.look == '.' || this.look == ',') {
				this.nextChar();
				if (this.ssbnnode != null) {
					ret += this.ssbnnode.getStrongOVSeparator();	// adds a separator (a dot ".")
				} else {
					ret += ".";
				}
				continue;
			} else {
				break;
			}
		} while (index < text.length); 	// actually, this check is unreachable
		
		skipWhite();
		return ret;
	}

	/**
	 * b_expression ::= b_term [ "|" b_term ]*
	 * 
	 */
	protected ICompilerBooleanValue bExpression() throws InvalidConditionantException,
									  TableFunctionMalformedException{
		
		ICompilerBooleanValue val1 = bTerm();
		
		// LOOK FOR OR (OPTIONAL)
		// scan();
		if (look == '|') {
			match('|');
			ICompilerBooleanValue val2 = bTerm();
			// Debug.println("EXITING BEXPRESSION AS OR");
			if (look == '|') {
				match('|');
				return new CompilerOrValue(val1, new CompilerOrValue(val2,bExpression()));
			} else {
				return new CompilerOrValue(val1, val2);
			}
		} else {
			// Debug.println("EXITING BEXPRESSION AS SINGLE TERM");			
			return val1;
		}
		
	}

	/**
	 * b_term ::= not_factor [ "&" not_factor ]*
	 * 
	 */
	protected ICompilerBooleanValue bTerm() throws InvalidConditionantException,
							    TableFunctionMalformedException{
		
		ICompilerBooleanValue val1 = notFactor();

		// LOOK FOR AND (OPTIONAL)
		// scan();
		if (look == '&') {
			match('&');
			ICompilerBooleanValue val2 = notFactor();
			if (look == '&') {
				match('&');
				return new CompilerAndValue(val1, new CompilerAndValue(val2,bTerm()));
			} else {
				return new CompilerAndValue(val1, val2);
			}
		} else {
			return val1;
		}
	}

	/**
	 * not_factor ::= [ "~" ] b_factor
	 * 
	 */
	protected ICompilerBooleanValue notFactor() throws InvalidConditionantException,
									TableFunctionMalformedException{
		
		boolean isNot = false;	// tests if '~' was found previously.
		
		// SCAN FOR NOT (OPTIONAL)
		// scan();
		if (look == '~') {
			isNot = true;
			match('~');
		}

		ICompilerBooleanValue factor = bFactor();
		if (factor == null) {
			throw new TableFunctionMalformedException(getNode().toString());
		}
		// Debug.println("EXITING NOT FACTOR");
		
		if (isNot) {
			return new CompilerNotValue(factor);
		} else {
			return factor;
		}
	}

	/**
	 * b_factor ::= ident ["(" arguments ")"]  "=" ident ["(" arguments ")"] | "(" b_expression ")"
	 * <br/>
	 * <br/>
	 * For example: Node = state
	 * <br/>
	 * Another example: OV = entityInstance
	 * <br/>
	 * Another example: b_cardinality(Node;state;1)
	 * 
	 */
	protected ICompilerBooleanValue bFactor() throws InvalidConditionantException,
								  TableFunctionMalformedException{
		
		String conditionantName1 = null;
		
		
		
		// first, check if it is an expression with parenhtesis - "(" b_expression ")" case.
		if (look == '(') {
			match('(');
			ICompilerBooleanValue ret = bExpression();
			match(')');
			return ret;
		} 
		
		// this is not a "(" b_expression ")" case; so, check as ident "=" ident.
		
		// Debug.println("Parsing bFactor");
		// SCAN FOR CONDITIONANTS
		scan();
		
		MultiEntityBayesianNetwork mebn = getMEBN();
		if (token == 'x') {
			conditionantName1 = this.noCaseChangeValue;
			// consistency check C09: verify whether is conditionant of the node
			if (this.node != null) {
				if (!this.isValidConditionant(mebn , this.node, conditionantName1 )) {
					// Debug.println("->" + getNode());
					throw new InvalidConditionantException(this.node.toString());
				}
			}
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(getNode().toString() , e);
			}
		}
		
		// check if node has arguments specified
		List<OrdinaryVariable> arguments1 = null;	// arguments of 1st conditionant (i.e. left-side of "=")
		if (look == '(') {
			match('(');
			arguments1 = this.arguments(conditionantName1);
			match(')');
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR CONDITIONANTS' POSSIBLE STATES
		scan();
		
		// Debug.println("SCANED FOR CONDITIONANTS' POSSIBLE STATES");
		
		// now, handle the right side of "=". It may be a variable (node or ordinary variable), or a single value
		
		String conditionantName2 = this.noCaseChangeValue;
		boolean isConditionantName2SingleValue = true;	// if true, then conditionantName2 represents a single value. If false, then it represents another node
		List<OrdinaryVariable> arguments2 = null;	// this will be the arguments of 2nd conditionant (i.e. right-side of "="), if such thing exists
		if (token == 'x') {
			// TODO the name of the possible value may contain caracters like dots, but this method cannot handle such characters
			
			// consistency check C09: verify whether conditionant has valid values
			if (this.node != null) {
				if (!this.isValidConditionantValue(mebn,this.node,conditionantName1,conditionantName2)) {
					// if it is not a single value, then let's check if it is a variable (node or ordinary variable)
					if (this.isValidConditionant(mebn , this.node, conditionantName2 )) {
						// the 2nd conditionant (right side of "=") is a name of another node
						isConditionantName2SingleValue = false;
						
						// check if node has arguments specified
						if (look == '(') {
							match('(');
							arguments2 = this.arguments(conditionantName2);
							match(')');
						}
						
					} else {
						throw new InvalidConditionantException(this.node.toString());
					}
				}
			}
			
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(getNode().toString(),e);
			}
		}
		
		// if code reached here, the condicionant check is ok
		
		// this is an object representing either the left-side of "=", or a value attribution if the right side is a vale 
		TempTableHeader header1 = null;	 // (e.g. Node=state, or OV=value)

		//	prepare to add current temp table's header's parent node (condicionant list)
		ResidentNode resident1 = mebn.getDomainResidentNode(conditionantName1);
		if (resident1 != null) {
			
			Entity condvalue = null;
			if (isConditionantName2SingleValue) {
				// search for an entity with a name this.noCaseChangeValue
				for (Entity possibleValue : resident1.getPossibleValueListIncludingEntityInstances()) {
					if (possibleValue.getName().equalsIgnoreCase(conditionantName2)) {
						condvalue = possibleValue;
						break;
					}
				}
				// If not found, its an error!		
				if (condvalue == null) {
					try{
						expected("Identifier");
					} catch (TableFunctionMalformedException e) {
						throw new InvalidConditionantException(getNode().toString(),e);
					}
				}
				// TODO optimize above code, because its highly redundant (condvalue should be found anyway on that portion of code)
			}
			// Set temp table's header condicionant. condvalue will be null if this is not a declaration like Node=state
			header1 = new TempTableHeaderParent(resident1, condvalue, arguments1, null);
		} else {
			// we did not find a parent node with the specified name. It may be an ordinary variable
			OrdinaryVariable ov = this.node.getMFrag().getOrdinaryVariableByName(conditionantName1);
			// If we did not find either one (node or OV), its an error!		
			if (ov == null) {
				try{
					expected("Identifier");
				} catch (TableFunctionMalformedException e) {
					throw new InvalidConditionantException(getNode().toString(),e);
				}
			}
			if (ov.getValueType() == null) {
				throw new InvalidConditionantException(ov + " has no associated value.");
			}
			
			// extract the type of this OV
			ObjectEntity objectEntity = mebn.getObjectEntityContainer().getObjectEntityByType(ov.getValueType());
			
			// extract the actual instance of this type
			Entity value = null;
			if (isConditionantName2SingleValue) {
				value = objectEntity.getInstanceByName(conditionantName2);
			}
			
			// value will be null if this is not a declaration like OV=value
			header1 = new TempTableHeaderOV(ov, value);
		}
		
		// Similarly, create an instance of the right-side of "=" if its not a simple value
		TempTableHeader header2 = null;		// e.g. node=node, or ov=node, or node=ov, or ov=ov
		
		if (!isConditionantName2SingleValue) {
			//	prepare to add current temp table's header's parent node (condicionant list)
			ResidentNode resident2 = mebn.getDomainResidentNode(conditionantName2);
			if (resident2 != null) {
				// Set temp table's header condicionant. Its never a single value, so the second argument is null
				header2 = new TempTableHeaderParent(resident2, null, arguments2, null);
			} else {
				// we did not find a parent node with the specified name. It may be an ordinary variable
				OrdinaryVariable ov = this.node.getMFrag().getOrdinaryVariableByName(conditionantName2);
				// If we did not find either one (node or OV), its an error!		
				if (ov == null) {
					try{
						expected("Identifier");
					} catch (TableFunctionMalformedException e) {
						throw new InvalidConditionantException(getNode().toString(),e);
					}
				}
				if (ov.getValueType() == null) {
					throw new InvalidConditionantException(ov + " has no associated value.");
				}
				// again, the right-hand side of "=" is never in a format like OV=value, so use null as the value of this OV
				header2 = new TempTableHeaderOV(ov, null);
			}
		}
		
		
		// store headers as conditionants declared inside a boolean expression.
		// This record is used later in order to fill the headers with current parent's values (i.e. states of parents in the header of current column in CPT)
		this.currentHeader.addParent(header1);	
		if (header2 != null) {
			// only add the right-side of "=" if it was created.
			// the right side is created only if the right-side is a variable (not a single value)
			this.currentHeader.addParent(header2);	
		}
		
		if (isConditionantName2SingleValue) {
			return header1;
		} else {
			return new TempTableHeaderVariableEqualsVariable(header1,header2);
		}
	}
	
	/**
	 * @param nodesToConsider : only these nodes will be considered
	 * @param parentOV : nodes with OV as the argument at a given index will be retrieved. 
	 * If null, then this method will return an empty list.
	 * @param parentOVIndex : argument (OV) at this index will be checked. 
	 * If negative, then any index will be considered.
	 * @return non-null collection of all nodes having parentOV at the position parentOVIndex.
	 */
	protected Collection<MultiEntityNode> getNodesByOV(Collection<MultiEntityNode> nodesToConsider, OrdinaryVariable parentOV, int parentOVIndex) {
		
		// initial assertion
		if (nodesToConsider == null
				|| parentOV == null) {
			return Collections.EMPTY_LIST;
		}
		
		Collection<MultiEntityNode> ret = new ArrayList<MultiEntityNode>();
		
		for (MultiEntityNode multiEntityNode : nodesToConsider) {
			try {
				if (parentOVIndex < 0) {
					// if parentOVIndex is negative, then we shall consider all indexes
					if (multiEntityNode.getOrdinaryVariablesInArgument().contains(parentOV)) {
						ret.add(multiEntityNode);
					}
				} else if (multiEntityNode.getOrdinaryVariablesInArgument().get(parentOVIndex).equals(parentOV)) { // only consider this index
					ret.add(multiEntityNode);
				}
			} catch (RuntimeException e) {
				// ignore errors at this method, because they are irrelevant for the purpose of this method
//				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 * arguments ::= ident[["."|","]ident]*
	 * @param parentName : name of the node (usually a parent of {@link #getNode()}) specified in {@link #bFactor()}.
	 * Consistency of this name will not be checked here, because such check is a responsibility 
	 * of {@link #isValidConditionant(MultiEntityBayesianNetwork, ResidentNode, String)}
	 * @return : ordinary variables representing the arguments
	 * @throws TableFunctionMalformedException  this is thrown by {@link #expected(String)}
	 * @throws InvalidConditionantException   if ident is invalid (because it's not an OV or the OV is never used by any parent).
	 */
	protected List<OrdinaryVariable> arguments(String parentName) throws TableFunctionMalformedException, InvalidConditionantException {
		
		// this is the value to return
		List<OrdinaryVariable> ret = new ArrayList<OrdinaryVariable>();
		
		// extract all parents that will be checked for arguments
		Collection<MultiEntityNode> parentsToConsider = new ArrayList<MultiEntityNode>();
		// we need to look for resident nodes and input nodes separately, due to restriction of implementation
		// TODO create common methods in MultiEntityNode
		for (ResidentNode parent : this.getNode().getResidentNodeFatherList()) {
			if (parent == null || parent.getName() == null) {
				continue;
			}
			if (parent.getName().equalsIgnoreCase(parentName)) {
				parentsToConsider.add(parent);
			}
		}
		// similarly, look for input nodes
		for (InputNode parent : this.getNode().getParentInputNodesList()) {
			if (parent == null || parent.getResidentNodePointer() == null) {
				continue;
			}
			// extract the resident node being pointed by the input node
			ResidentNodePointer residentNodePointer = parent.getResidentNodePointer();
			if (residentNodePointer.getResidentNode() == null || residentNodePointer.getResidentNode().getName() == null) {
				continue;
			}
			if (residentNodePointer.getResidentNode().getName().equalsIgnoreCase(parentName)) {
				parentsToConsider.add(parent);
			}
		}
		
		// scan for the ident
		// in the next for-clause, the test for condition "index < text.length" (to end loop) is never executed, because of breaks and continues
		// argIndex represents the index of parent node's argument.
		// If parent node is Parent(x,y,z), then argIndex == 0 points to x, argIndex == 2 points to y, and argIndex == 2 points to z  
		int argIndex = 0;	// this will be pointing to last argument after exitting the for-loop
		for (; index < text.length; argIndex++) { 
			
			scan();	
			
			if (token == 'x') {
				// found an identifier. Check if it is a valid OV
				OrdinaryVariable ov = this.getNode().getMFrag().getOrdinaryVariableByName(this.noCaseChangeValue);
				if (ov == null) {
					// build message to display. It's something like "Parent( _ , _ , "x" , ... ) is an invalid conditionant"
					String message = parentName + "( ";
					for (int i = 0; i < argIndex; i++) {
						message += "_ , ";
					}
					message +=  "\"" + this.noCaseChangeValue + "\" , ... )";
					throw new InvalidConditionantException(message);
				}
				
				// check if there are parents with such OV at current argument index.
				// also update parentsToConsider, because in next iteration we don't need to consider parents with no such OV at current index
				parentsToConsider = this.getNodesByOV(parentsToConsider, ov, argIndex);
				if (parentsToConsider.isEmpty()) {
					// build message to display. It's something like "Parent( _ , _ , "x" , ... ) is an invalid conditionant"
					String message = parentName + "( ";
					for (int i = 0; i < argIndex; i++) {
						message += "_ , ";
					}
					message +=  "\"" + this.noCaseChangeValue + "\" , ... )";
					throw new InvalidConditionantException(message);
				}
				
				ret.add(ov);
			} else {
				expected("Identifier");
			}	
			
			// search for ["." ident]* loop
			if (this.look == '.' || this.look == ',') {
				this.nextChar();
				continue;
			} else {
				break;
			}
		}
		
		// make sure all the arguments were considered
		for (MultiEntityNode multiEntityNode : parentsToConsider) {
			// argIndex will be pointing to last argument
			if (argIndex < multiEntityNode.getOrdinaryVariablesInArgument().size()-1) {
				// the declaration in LPD had less arguments than the actual number of arguments in the node.
				// build message to display. It's something like "Parent( _ , _ , ...  is an invalid conditionant"
				String message = parentName + "( ";
				for (int i = 0; i < argIndex; i++) {
					message += "_ , ";
				}
				message +=  " ... ";
				throw new InvalidConditionantException(message);
			}
		}
		
		skipWhite();
		return ret;
	}


	/**
	 *  else_statement ::= statement | if_statement
	 *  @param upperIf: if this if clause is an inner if, the upper if-clause should be referenced
	 * by this parameter. If this is a temporary table, this if-clause is the upper-most if-clause within CPT pseudocode.
	 * If it is null, it will assume tempTable is the upper container
	 */
	protected void else_statement(INestedIfElseClauseContainer upperIf) throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException {
		
		// Debug.println("ELSE STATEMENT");
		if ( look == '[' ) {
			// header ::= there are no known parents yet, is ANY and is default.
			this.currentHeader = new TempTableHeaderCell(null,true,true, this.ssbnnode); 
			
			// register it to the upper container (might be another clause or the temporary table)
			if (upperIf == null) {
				// No upper container identified. Let's assume to be the upper-most container (the temporary table)
				this.tempTable.addNestedClause(this.currentHeader);
			} else {
				upperIf.addNestedClause(this.currentHeader);
			}
			
			// if there are nested if/else clauses, their upper container should be the currently evaluated else clause (currentHeader)
			this.statement(this.currentHeader);
		} else {
			// Debug.println("COULD NOT FIND '['");
			// we dont have to create new header here because ifStatement would do so.
			// the if statement without "[" is on the same level of currently evaluated else clause, so, pass upperIf as upper container
			ifStatement(upperIf);
		}
	
	}
	

	/**
	 * statement ::= "[" assignment_or_if "]" 
	 * 
	 * @param upperIf: the upper if-clause or the tempTable (upper-most container).
	 * Since assignment_or_if might start a nested if/else-clause, this parameter
	 * helps us keep track where the new if/else-clause is contained.
	 */
	protected void statement(INestedIfElseClauseContainer upperIf) 
							 throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException{
		// Debug.println("PARSING STATEMENT, VALUE = " + value + ", LOOKAHEAD = " + look);
		if (look == '[') {
							
			// Debug.println("");
			// Debug.print("  ");
			match('[');
			
			// initialize currently evaluated temporary table's collumn
			//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>();
			
			assignmentOrIf(upperIf);
			
			
			match(']');
			// Debug.println("");
			
			
		} else {
			// Debug.println("COULD NOT FIND '['");
			this.expected("[");
		}
	}

	
	/**
	 * assignment_or_if ::= assignment [ "," if_statement] | if_statement
	 * 
	 * @param upperIf: the upper if-clause or the tempTable (upper-most container).
	 * Since assignment_or_if might start a nested if/else-clause, this parameter
	 * helps us keep track where the new if/else-clause is contained.
	 * @throws InvalidProbabilityRangeException
	 * @throws TableFunctionMalformedException
	 * @throws SomeStateUndeclaredException
	 * @thwows NoDefaultDistributionDeclaredException
	 * @throws InvalidConditionantException
	 */
	protected void assignmentOrIf(INestedIfElseClauseContainer upperIf) 
			throws InvalidProbabilityRangeException, 
				   TableFunctionMalformedException,
				   SomeStateUndeclaredException,
				   NoDefaultDistributionDeclaredException,	// if-clause would eventually throw this
				   InvalidConditionantException{			// if-clause would eventually throw this
		
		try {
			if (this.tokenLookAhead() == this.kwcode[this.lookup("IF")]) {
				// this is an if-clause
				this.ifStatement(upperIf);
				// an if-clause doesnt have do return something...
			} else {
				// since it is an assignment, we should check probability consistency as well
				
				// Consistency check C09
				// Structures that allow us to Verify if all states has probability declared
				List<Entity> declaredStates = new ArrayList<Entity>();
				List<Entity> possibleStates = null;			
				if (this.node != null) {
					possibleStates = this.node.getPossibleValueListIncludingEntityInstances();
				}
				
				this.assignment(declaredStates, possibleStates);
				

				if (this.look == this.kwcode[this.lookup("IF")]) {
					
					// this is an if-clause after user-defined variable declaration
					this.ifStatement(upperIf);
					
				} else {
					// After the assignment, if there are undeclared states, distribute the remaining probability uniformly.
					// obtain undeclared states = possibleStates - declaredStates
					Collection<Entity> undeclaredStates = new HashSet<Entity>(possibleStates);
					undeclaredStates.removeAll(declaredStates);
					if (undeclaredStates.size() > 0) {
						// get the current (without the undeclared states) sum of probabilities
						float sumOfDeclaredProb = currentHeader.getProbCellSum();
						
						// distribute the remaining probability (1-sumOfDeclaredProb) uniformly across the non-declared states
						float probOfUndeclaredState = (1f-sumOfDeclaredProb)/undeclaredStates.size();
						if (!isToNormalize() || (sumOfDeclaredProb >= 1)) {
							// if we don't need to normalize, then simply set all undeclared states to zero
							probOfUndeclaredState = 0f;
						}
						for (Entity entity : undeclaredStates) {
							if (entity != null) {
								// distribute the remaining probability (1-retValue) uniformly across the non-declared states, but substitute NaN with 0
								if (this.getSSBNNode() != null) {
									// use a special type of cell which will recalculate the probability each time getProbabiity is called, by uniformly distributing 1-(probability of declared states)
									this.currentHeader.addCell(new TempTableProbabilityCell(entity, new UniformComplementProbabilityValue(this.currentHeader, entity)));
								} else {
									this.currentHeader.addCell(new TempTableProbabilityCell(entity, new SimpleProbabilityValue(Float.isNaN(probOfUndeclaredState)?0f:probOfUndeclaredState )));
								}
								// the following may be irrelevant now, since we fill all undeclared states automatically anyway
//								declaredStates.add(entity);
							}
						}
					}
					
					// the following check may be irrelevant now, since we fill all undeclared states automatically anyway
//					if (this.node != null) {
//						// Consistency check C09
//						// Verify if all states has probability declared
//						if (!declaredStates.containsAll(possibleStates)) {
//							throw new SomeStateUndeclaredException();
//						}
//					}
					
					
					// Consistency check C09
					// Verify if sum of all declared states' probability is 1
					
					// runtime probability bound check (on SSBN generation time)
					if (isToNormalize()
							&& this.currentHeader.getProbCellSum() < 0.99995	// check if normalization of probabilities smaller than 1 worked
							) {
						// Debug.println("Testing cell's probability value's sum: " + currentHeader.getProbCellSum());
						if (!Float.isNaN(this.currentHeader.getProbCellSum())) {
							throw new InvalidProbabilityRangeException(getNode().toString());
						} else {
							// Debug.println("=>NaN found!!!");
						}
					}
				}
				
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			/* 
			 * catching ArrayIndexOutOfBoundsException means that the keyword "IF"
			 * was not found the list of keywords. It is an horrible implementation error!!
			 */
			throw new RuntimeException(this.getResource().getString("FatalError"),e);
		}
		
		// any other exception should not be treated by this scope (equivalent to "catch(Exception e){throw e}")
		
	}
	
	/**
	 * assignment ::= ident "=" expression [ "," assignment ]*
	 * 
	 * declaredStates, possibleStates are used to verify if every single possible state for
	 * RV has its probability declared.
	 * @param declaredStates: states actually declared within this clause. It is used afterwards in order to check
	 * if all states have probability declared.
	 * @param possibleStates: list of all possible states a node (hosting the pseudocode) may have.
	 * It is compared to declaredStates in order to ensure declardStates is a sublist of possibleStates.
	 * returns the sum of all declared states' probability after this assignment recursion phase
	 * 
	 */
	protected IExpressionValue assignment(List<Entity> declaredStates, List<Entity> possibleStates) 
					throws InvalidProbabilityRangeException, 
						   TableFunctionMalformedException,
						   SomeStateUndeclaredException{
		
		// prepare a representation of a cell inside the temporary table
		TempTableProbabilityCell currentCell = new TempTableProbabilityCell(null, null);
		
		// SCAN FOR IDENTIFIER
		scan();
		String userDefinedVariableName = null;	// this will become non-null if there is an unknown state declared (unknown states will become user-defined variables)
		if (token == 'x') {
			if (this.node != null) {
				// Consistency check C09
				// Remember declared states, so we can check later if all states was declared
				Entity possibleValue = null;
				int index = this.node.getPossibleValueIndex(this.noCaseChangeValue);
				if (index < 0) {
					// unknown states will be considered as a new variable that user has declared
					userDefinedVariableName = this.noCaseChangeValue;
				} else {
					try {
						possibleValue = possibleStates.get(index);
					} catch (Exception e) {
						//throw new TableFunctionMalformedException(e.getMessage());
						throw new TableFunctionMalformedException(this.node.toString(), e);
					}
					if (possibleValue == null) {
						throw new TableFunctionMalformedException(getNode().toString());
					}
					declaredStates.add(possibleValue);
					currentCell.setPossibleValue(possibleValue);
				}
			}
			
		} else {
			expected("Identifier");
		}

		// LOOK FOR = OPERATOR
		match('=');

		// consistency check C09
		// ret verifies the sum of all declared states' probability (must be 1)
		// boolean hasUnknownValue shows if some ret was negative or NaN.
		IExpressionValue ret = expression();	
		if (ret == null) {
			throw new TableFunctionMalformedException(getResource().getString("NonDeclaredVarStateAssignment"));
		}
		float retValue = 0;	// initialize with a value which will not impact consistency check (the one that checks if sum is 1)
		if (userDefinedVariableName == null) {
			// this is a state of current node, so store the probability in order to calculate consistency later
			if (!ret.isNumeric()) {
				// state of nodes must be probabilities (numbers)
				throw new TableFunctionMalformedException(getResource().getString("NonNumericProbAssignment"));
			}
			try {
				retValue = Float.parseFloat(ret.getValue());
			} catch (NumberFormatException e) {
				retValue = Float.NaN;
			}
		} else {
			// this is a user-defined value
			// store it in the scope of current if-clause
			this.currentHeader.addUserDefinedVariable(userDefinedVariableName, ret);
		}
		boolean hasUnknownValue = Float.isNaN(retValue);
		
		// add cell to header
		currentCell.setProbability(ret);
		if (currentCell.getPossibleValue() != null && currentCell.getPossibleValue() != null) {
			this.currentHeader.addCell(currentCell);
		}
		// Debug.println("Adding cell: " + currentCell.getPossibleValue().getName() + " = " + ret.toString());

		// consistency check C09
		// a single state shall never have negative prob 
		if ( isToNormalize()
				&& ((retValue < 0.0) 
//						|| (1.0 < retValue)
						)) {
			throw new InvalidProbabilityRangeException(getNode().toString());
		}
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			if (look != this.kwcode[this.lookup("IF")]) {
				IExpressionValue temp = assignment(declaredStates, possibleStates);
				float tempValue = Float.NaN;
				try {
					tempValue = Float.parseFloat(temp.getValue());
				} catch (NumberFormatException e) {
					tempValue = Float.NaN;
				}
				hasUnknownValue = hasUnknownValue || (Float.isNaN(tempValue));
				if (hasUnknownValue) {
					retValue = Float.NaN;
				} else {
					retValue += tempValue;
				}
			} else {
				// this is an if-clause after assignment.
				// we should finish assignment (assignments in same block of if-clauses must be always before the if clause)
				// if assignment and if-clause happens in same block, assingments must be only for user-defined variables
				if (!currentHeader.getCellList().isEmpty()) {
					// user-defined variables are not added to cellList. Assignments to states of variables are added to cell list.
					// if this is not empty, then there were assignments to states of variables, so this is invalid.
					throw new SomeStateUndeclaredException(getResource().getString("NonUserDefinedVariablesFoundBeforeIfClause"));
				}
				// let the upper clause actually handle the if=clause
			}
		}
		
		// Debug.println("Returned expression value = " + retValue);
		if (isToNormalize() && (retValue < 0)) {
			throw new InvalidProbabilityRangeException(getNode().toString());
		}
		return new SimpleProbabilityValue(retValue);
	}

	/**
	 * expression ::= term [ addop term ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue expression() throws TableFunctionMalformedException,
												  InvalidProbabilityRangeException,
												  SomeStateUndeclaredException{
		
		// temp table already created by upper caller
		
		IExpressionValue temp1 = term();
		IExpressionValue temp2 = null;
		
		Float temp1Value = null;
		Float temp2Value = null;
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			temp2 = term();
			try {
				temp1Value = Float.parseFloat(temp1.getValue());
			} catch (NumberFormatException e) {
				temp1Value = Float.NaN;
			}
			try {
				temp2Value = Float.parseFloat(temp2.getValue());
			} catch (NumberFormatException e) {
				temp2Value = Float.NaN;
			}
			if (!Float.isNaN(temp1Value) && !Float.isNaN(temp2Value)) {
				// TODO cut the subtree if it is a known value...
				temp1 = new AddOperationProbabilityValue(
						temp1.isFixedValue()?(new SimpleProbabilityValue(temp1Value)):temp1 ,
								temp2.isFixedValue()?(new SimpleProbabilityValue(temp2Value)):temp2);
			}		
			break;
		case '-':
			match('-');
			temp2 = term();
			try {
				temp1Value = Float.parseFloat(temp1.getValue());
			} catch (NumberFormatException e) {
				temp1Value = Float.NaN;
			}
			try {
				temp2Value = Float.parseFloat(temp2.getValue());
			} catch (NumberFormatException e) {
				temp2Value = Float.NaN;
			}
			if (!Float.isNaN(temp1Value) && !Float.isNaN(temp2Value)){
				// TODO cut the subtree if it is known value...
				temp1 = new SubtractOperationProbabilityValue(
						temp1.isFixedValue()?(new SimpleProbabilityValue(temp1Value)):temp1 ,
								temp2.isFixedValue()?(new SimpleProbabilityValue(temp2Value)):temp2);
			}			
			break;
		}
		
		// Debug.println("Expression returned " + temp1.getProbability());
		return temp1;
	}

	/**
	 * term ::= signed_factor [ mulop signed_factor ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue term() throws TableFunctionMalformedException,
											InvalidProbabilityRangeException,
											SomeStateUndeclaredException{
		IExpressionValue temp1 = signedFactor();
		IExpressionValue temp2 = null;
		
		Float temp1Value = null;
		Float temp2Value = null;
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			temp2 = this.signedFactor();
			try {
				temp1Value = Float.parseFloat(temp1.getValue());
			} catch (NumberFormatException e) {
				temp1Value = Float.NaN;
			}
			try {
				temp2Value = Float.parseFloat(temp2.getValue());
			} catch (NumberFormatException e) {
				temp2Value = Float.NaN;
			}
			if (!Float.isNaN(temp1Value) && !Float.isNaN(temp2Value)) {
				// TODO cut the subtree if it is known value
				return new MultiplyOperationProbabilityValue(
						temp1.isFixedValue()?(new SimpleProbabilityValue(temp1Value)):temp1 ,
								temp2.isFixedValue()?(new SimpleProbabilityValue(temp2Value)):temp2);
			}
			break;
		case '/':
			match('/');
			temp2 = this.signedFactor();
			try {
				temp1Value = Float.parseFloat(temp1.getValue());
			} catch (NumberFormatException e) {
				temp1Value = Float.NaN;
			}
			try {
				temp2Value = Float.parseFloat(temp2.getValue());
			} catch (NumberFormatException e) {
				temp2Value = Float.NaN;
			}
			if (!Float.isNaN(temp1Value) && !Float.isNaN(temp2Value)) {
				// TODO cut the subtree if it is known value
				return new DivideOperationProbabilityValue(
						temp1.isFixedValue()?(new SimpleProbabilityValue(temp1Value)):temp1 ,
								temp2.isFixedValue()?(new SimpleProbabilityValue(temp2Value)):temp2);
			}
			break;
		//default:
			//return temp1;
		}
		// Debug.println("Term is not matching to an * or / nor signed factor !!");
		//return new SimpleProbabilityValue(Float.NaN);
		return temp1;
	}

	/**
	 * signed_factor ::= [ addop ] factor
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue signedFactor() throws TableFunctionMalformedException,
													InvalidProbabilityRangeException,
													SomeStateUndeclaredException{

		boolean isMinus = false;
		
		// CHECK TO SEE IF THERE IS A -/+ UNARY SIGN
		// boolean negative;
		// negative = (look == '-');
		if (isAddOp(look)) {
			// Debug.print("" + look);
			
			if (this.isMinus(look)) {
				isMinus = true;
			}
			
			nextChar();
			skipWhite();
		}
		// Debug.println("Signed factor returning minus = " + isMinus);
		if (isMinus) {
			return new NegativeOperationProbabilityValue(factor());
		} else {
			return factor();
		}
	}

	/**
	 * factor ::= number | string | function | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue factor() throws TableFunctionMalformedException,
											  InvalidProbabilityRangeException,
											  SomeStateUndeclaredException{
		IExpressionValue ret = null;
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (look == '"') {
			ret = this.getStr();
		} else if (isAlpha(look)) {
			ret = function();
		} else {
			ret =  getNum();
		}
		// Debug.println("Factor returning " + ret.toString());
		return ret;
	}

	/**
	 * String is anything between quotes
	 * @return instance of a {@link SimpleStringValue}
	 */
	protected IExpressionValue getStr()throws TableFunctionMalformedException {
		value = "";
		if (look != '"') {
			expected("String");
		}
		nextChar();
		while ((look != '"')) {
			if (index >= text.length) {
				break;
			}
			value += look;
			nextChar();
		}
		if (look != '"') {
			throw new TableFunctionMalformedException(getResource().getString("PrematureEndScript"));
		}
		
		noCaseChangeValue = value;	// this is "value" without case change
		value = value.toUpperCase();
		
		token = 'x';
		
		nextChar();
		skipWhite();

		// Debug.println("GetNum returned " + Float.parseFloat(value));
		return new SimpleStringValue(noCaseChangeValue);
	}
	
	/**
	 * ident ::= letter [ letter | digit ]*
	 * 
	 */
	protected void getName()throws TableFunctionMalformedException {
		//// Debug.println("RESETING VALUE FROM " + value);
		value = "";
		//// Debug.println("LOOKAHEAD IS " + look);
		if (!isAlpha(look))
			expected("Name");
		while (isAlphaNumeric(look)) {
			value += look;
			nextChar();
		}
		
		noCaseChangeValue = value;	// this is "value" without case change
		value = value.toUpperCase();

		token = 'x';
		//skipWhite();

		//// Debug.print(value + " ");
		
		
		
	}
	
	/**
	 * possibleVal ::= ident
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue possibleVal()throws TableFunctionMalformedException,
												  SomeStateUndeclaredException {

		this.getName();
		
		// Use a list to store already known states or identifiers to evaluate already known values... 
//		IExpressionValue ret = new SimpleProbabilityValue(Float.NaN);
		if (this.currentHeader != null) {
			// check if this is not a user-defined variable first;
			IExpressionValue userDefinedVariableValue = this.currentHeader.getUserDefinedVariable(noCaseChangeValue);
			if (userDefinedVariableValue != null) {
				return userDefinedVariableValue;
			}
			// check if this is not another state of current node
			for (TempTableProbabilityCell cell : this.currentHeader.getCellList()) {
				 if (cell.getPossibleValue().getName().equalsIgnoreCase(value) ) {
					 // Debug.println("\n => Variable value found: " + cell.getPossibleValue().getName());
					 return cell.getProbability();
					 
				 }
			}
		} else {
			// if null, it means it was called before an assignment
			throw new SomeStateUndeclaredException(getNode().toString());
		}
		


		// Debug.println("An undeclared possible value or a \"varsetname\" was used : " + value);
//		return ret;
		return null;
	}

	/**
	 * number ::= [digit]+
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected IExpressionValue getNum() throws TableFunctionMalformedException {
		value = "";

		if (!((isNumeric(look)) || ((look == '.') && (value.indexOf('.') == -1))))
			expected("Number");

		while ((isNumeric(look))
				|| ((look == '.') && (value.indexOf('.') == -1))) {
			value += look;
			nextChar();
		}

		token = '#';
		skipWhite();

		// Debug.println("GetNum returned " + Float.parseFloat(value));
		return new SimpleProbabilityValue(Float.parseFloat(value));
	}
	
	/**
	 * Scan the text and skippes white space
	 * @throws TableFunctionMalformedException
	 */
	protected void scan() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		skipWhite();
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		// Debug.println("\n!!!Value = "  + value);
	}
	
	/**
	 * Scan the text but doesnt skip white spaces
	 * @throws TableFunctionMalformedException
	 */
	protected void scanNoSkip() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		// Debug.println("\n!!!Value = "  + value);
	}
	
	/**
	 * Performs a scan and revert attributes (value, token, look, index, noCaseChangeValue)
	 * @return: token of the scanned element
	 */
	protected char tokenLookAhead () throws TableFunctionMalformedException {
		int originalIndex = this.index;
		char originalLook = this.look;
		String originalValue = new String(this.value);
		String originalNoCaseChangeValue = new String(this.noCaseChangeValue);
		char originalToken = this.token;
		char returnedToken = ' ';
		
		this.scan();
		
		returnedToken = this.token;	// this value will be the return value
		
		// revert global attributes
		this.index = originalIndex;
		this.look = originalLook;
		this.value = originalValue;
		this.noCaseChangeValue = originalNoCaseChangeValue;
		this.token = originalToken;
		
		return returnedToken;
		
	}

	/**
	 * Searches the kwlist for a String. Returns its index when found.
	 * @param s: a keyword to look for
	 * @return kwlist's index where the keyword resides. -1 if not found.
	 */
	protected int lookup(String s) {
		int i;

		for (i = 0; i < kwlist.length; i++) {
			if (kwlist[i].equalsIgnoreCase(s))
				return i;
		}

		return -1;
	}

	/**
	 *  reads the next input character (updates lookup character)
	 */
	protected void nextChar() {
		
//		if (index < text.length) {
//			look = text[index++];
//		} else {
//			look = ' ';
//		}
		look = (index < text.length)?(text[index++]):(' ');
	}

	protected void skipWhite() {
		while ((index < text.length) && (look == ' '))
			nextChar();
	}

	/* Sends an alert telling that we expected some particular input */
	protected void expected(String error) throws TableFunctionMalformedException {
		System.err.println("Error: " + error + " expected!");
		throw new TableFunctionMalformedException(getNode().toString() + " : " + error);
	}

	/* Verifies if an input is an expected one */
	protected void match(char c) throws TableFunctionMalformedException {
		
		//Debug.println("Matching " + c + " ");
		if (look != c)
			expected("" + c);
		nextChar();
		skipWhite();
	}

	protected void matchString(String s) throws TableFunctionMalformedException {
		if (!value.equalsIgnoreCase(s))
			expected(s);
	}

	protected boolean isAlpha(final char c) {
//		if ((c >= 'a') && (c <= 'z'))
//			return true; // lowercase
//		if ((c >= 'A') && (c <= 'Z'))
//			return true; // uppercase
//		if (c == '_') {
//			return true; // underscore
//		}
		
		return ((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z'))
					|| (c == '_');
	}

	protected boolean isAlphaNumeric(final char c) {
		if (isAlpha(c)) {
			return true; // uppercase
		} else	if (isNumeric(c)) {
			return true; // numeric
		}
		return false;
	}

	protected boolean isNumeric(final char c) {
		return (((c >= '0') && (c <= '9')));
	}

	/** identifies the + or - symbols */
	protected boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}
	
	/** identifies the "negative" simbol*/
	protected boolean isMinus(char c) {
		return c == '-';
	}

	
	/**
	 * @return Returns the node.
	 */
	public ResidentNode getNode() {
		if (this.ssbnnode != null) {
			this.node = this.ssbnnode.getResident();
		}
		return node;
	}

	/**
	 * Setting this to null or not setting at all makes
	 * Compiler not to check structural consistency (like node states or conditionants
	 * are real)
	 * @param node The node to set.
	 */
	public void setNode(ResidentNode node) {
		this.node = node;
//		if (this.node != null && this.node.getMFrag() != null) {
//			this.mebn = this.node.getMFrag().getMultiEntityBayesianNetwork();
//		}
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node,
	 * or ordinary variables in the same MFrag
	 * @param node : this will be used in order to extract parents or MFrags from (in order to check if OV or node is within same MFrag).
	 * @param mebn : this will be used to extract nodes by name.
	 * @param conditionantName : name of node or ordinary variable to check consistency.
	 * @return if node or ordinary variable with name == conditionantName is a valid conditionant.
	 */
	protected boolean isValidConditionant(MultiEntityBayesianNetwork mebn, ResidentNode node, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName); // fortunately, mebn.getNode(conditionantName) is case-sensitive already (and LPD needs to be case-sensitive)
		
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParents().contains(conditionant)) {
				return true;
			} else {	// parent may be an input node
				ArrayList<Node> parents = node.getParents();
				for (int i = 0; i < parents.size(); i++) {
					if (parents.get(i) instanceof InputNode) {
						if ( ((InputNode)(parents.get(i))).getInputInstanceOf().equals(conditionant) ) {
							return true;
						}
					}
				}
			}
			
			
			
			//	Check if it's a context node. Not necessary, because contexts are parents of all residents
			// TODO verify if it isn't a redundant check, since context node might not be
			// parents of all resident nodes
			
			// OVs can be nodes
//			return false;	// this is a node, but not a parent
		}
		
		// conditionant may be an ordinary variable
		// fortunately, node.getMFrag().getOrdinaryVariableByName(conditionantName) is case-sensitive already (and LPD needs to be case-sensitive)
		return node.getMFrag().getOrdinaryVariableByName(conditionantName) != null;	// return true if we found an OV. Return false if we did not.
			
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must have a consistent possible value
	 * @return whether conditionantValue is a valid state for a conditionant with name conditionantName
	 */
	protected boolean isValidConditionantValue(MultiEntityBayesianNetwork mebn, ResidentNode node, String conditionantName, String conditionantValue) {
		Node conditionant = mebn.getNode(conditionantName);
		if (conditionant != null) {
			//// Debug.println("Conditionant node found: " + conditionant.getName());
			if ( conditionant instanceof IResidentNode) {
				// Debug.println("IS MULTIENTITYNODE");
				StateLink possibleValue = ((IResidentNode)conditionant).getPossibleValueByName(conditionantValue);
				if (possibleValue == null) {
					return false;
				}
				// we need to check name again, because ResidentNode#getPossibleValueByName is case-insensitive, and Compiler needs to be case sensitive.
				return  possibleValue.getState().getName().equals(conditionantValue);
			}
			// it was a node, but not a resident node...
			// OVs can be nodes
//			return false;
		}
		// Debug.println("Conditionant is not a resident node");
		
		// the name of the conditionant may be an OV
		OrdinaryVariable ov = node.getMFrag().getOrdinaryVariableByName(conditionantName);
		// fortunately, getOrdinaryVariableByName is case sensitive already
		if (ov == null || ov.getValueType() == null) {
			return false;
		}
		
		// extract the object entity related to this OV's type
		ObjectEntity objectEntity = mebn.getObjectEntityContainer().getObjectEntityByType(ov.getValueType());
		if (objectEntity == null) {
			return false;
		}
		
		// return true if there is an instance (for that OV) with the specified value. False otherwise
		ObjectEntityInstance ovInstance = objectEntity.getInstanceByName(conditionantValue);
		if (ovInstance == null) {
			return false;
		}
		// we need to check again, because objectEntity.getInstanceByName(conditionantValue) is case insensitive, but we need case sensitive
		return  ovInstance.getInstanceName().equals(conditionantValue);
		
	}
	
	
	/**
	 *  function ::= ident 
	 *   	| "CARDINALITY" "(" ident ")"
	 *   	| "CARDINALITY" "(" ")"
	 *    	| "MIN" "(" expression ";" expression ")"
	 *     	| "MAX" "(" expression ";" expression ")"
	 *     	| external_function([expression [";"|"," expression]])
	 * @return numeric value expected for the function
	 * @throws TableFunctionMalformedException
	 */
	protected IExpressionValue function()throws TableFunctionMalformedException,
											   InvalidProbabilityRangeException,
											   SomeStateUndeclaredException{
		IExpressionValue ret = this.possibleVal();
		skipWhite();
		if (this.look == '(') {
			if (this.value.equalsIgnoreCase("CARDINALITY")) {
				return cardinality();
			} else if (this.value.equalsIgnoreCase("MIN") ) {
				return min();
			} else if (this.value.equalsIgnoreCase("MAX") ) {
				return max();
			} else {
				for (IUserDefinedFunctionBuilder functionBuilder : getUserFunctionPluginManager().getFunctionBuilders()) {
					if (this.value.equalsIgnoreCase(functionBuilder.getFunctionName())) {
						return external_function(functionBuilder);
					}
				}
				// if reached this point, we did not find function with specified name
				// Debug.println("UNKNOWN FUNCTION FOUND: " + this.value);
				throw new TableFunctionMalformedException(((getSSBNNode()!=null)?getSSBNNode():getNode()) + " : " + this.getResource().getString("UnexpectedTokenFound")
						+ ": " + value);
			}
		}
		
		// Debug.println("Function returning " + ret);
		return ret;
	}
	
	
	/**
	 * external_function([expression [";"|"," expression]])
	 * @param functionBuilder : builder responsible for building an object representing this external function.
	 * @return a wrapper for functionBuilder which virtually convertes {@link IUserDefinedFunction} (built by {@link IUserDefinedFunctionBuilder})
	 * to a {@link IExpressionValue}.
	 * @throws TableFunctionMalformedException
	 * @throws SomeStateUndeclaredException 
	 * @throws InvalidProbabilityRangeException 
	 */
	protected IExpressionValue external_function(IUserDefinedFunctionBuilder functionBuilder) throws TableFunctionMalformedException, InvalidProbabilityRangeException, SomeStateUndeclaredException {
		
		// prepare arguments to pass to external function
		List<IExpressionValue> args = new ArrayList<Compiler.IExpressionValue>();
		
		match('(');
		if (look != ')') {	// check if argument was provided
			// add 1st argument
			args.add(this.expression());
			while (look == ';' || look == ',') {
				// add next arguments if they were provided (";" or "," separates arguments)
				nextChar();
				skipWhite();
				args.add(this.expression());
			}
		}
		match(')');
		
		if (!args.isEmpty()) {
			functionBuilder.setArguments(args);
		}
		
		return new ExternalFunctionProbabilityValue(functionBuilder);
		
	}
	
	/**
	 * Computes cardinality funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	protected IExpressionValue cardinality()throws TableFunctionMalformedException {
		IExpressionValue ret = null;
		match('(');
		
		skipWhite();
		String var = null;
		if (look != ')') {	// check if argument was provided
			var = this.varsetname();
			skipWhite();
		}
		
		// Debug.println("CARDINALITY'S ARGUMENT IS " + var);
		// TODO test if ret has returned NaN (guarantees "value" is a varsetname)?
		ret = new CardinalityProbabilityValue(this.currentHeader, var);
		match(')');
		return ret;
		
	}
	
	/**
	 * Computes min funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	protected IExpressionValue min()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MIN FUNCTION");
		
		IExpressionValue ret1 = null;
		IExpressionValue ret2 = null;
		match('(');
		ret1 = this.expression();
//		match(';');
		if (look != ';' && look != ',') {
			expected(";");
		}
		nextChar();
		skipWhite();
		ret2 = this.expression();
		match(')');
		/*
		// old code: tests which ret1/ret2 to return and test consistency. ComparisionProbabilityValue replaces it.
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2<ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		*/
		return new ComparisionProbabilityValue(ret1,ret2,false);
		
	}
	
	/**
	 * Computes MAX funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	protected IExpressionValue max()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MAX FUNCTION");
		
		IExpressionValue ret1 = null;
		IExpressionValue ret2 = null;
		match('(');
		ret1 = this.expression();
//		match(';');
		if (look != ';' && look != ',') {
			expected(";");
		}
		nextChar();
		skipWhite();
		ret2 = this.expression();
		match(')');
		/*
		// old code: tests which ret1/ret2 to return and test consistency. ComparisionProbabilityValue replaces it.
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2>ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		*/
		return new ComparisionProbabilityValue(ret1,ret2,true);
		
	}

	/**
	 * Use this method to determine where the error has occurred
	 * @return Returns the last read index.
	 */
	public int getIndex() {
		try {
			return (int)(index * ((float)this.originalTextLength / (float)this.text.length));
		} catch (Exception e) {
			// maybe there were a division by zero
			return index;
		}
	}

	/**
	 * @return the PotentialTable
	 */
	protected IProbabilityFunction getPotentialTable() {
		return cpt;
	}

	/**
	 * @param cpt the PotentialTable to set
	 */
	public void setPotentialTable(PotentialTable cpt) {
		this.cpt = cpt;
	}
	



	/**
	 * @return the ssbnnode
	 */
	public SSBNNode getSSBNNode() {
		return ssbnnode;
	}

	/**
	 * @param ssbnnode the ssbnnode to set
	 */
	public void setSSBNNode(SSBNNode ssbnnode) {
		this.ssbnnode = ssbnnode;
		if (this.ssbnnode != null) {
			this.setNode(this.ssbnnode.getResident());
		}
	}
	
	
	 
	
	// Some inner classes that might be useful for temporaly table creation (organize the table parsed from pseudocode)
	
	public interface IEmbeddedNodeUser {
		/**
		 * @return true if at least one (nested) if-clause is using an embedded node feature.
		 */
		public boolean hasEmbeddedNodeDeclaration();
	}
	
	/**
	 * Container of a if-else-clause
	 */
	public interface INestedIfElseClauseContainer extends IEmbeddedNodeUser {
		/**
		 * registers an if-clause (or else-clause) as an inner clause of this clause
		 * @param nestedClause
		 * @throws NullPointerException: if nestedClause is null
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause);
		
		/**
		 * Looks for the first if/else clause (performing deep scan - searches every nested if/else clause
		 * as well) which the bExpression returns true.
		 * 
		 * @param valuesOnCPTColumn: a map which the key is a name of a parent node and
		 * the value is its current possible values to be evaluated.
		 * For example, if we want to evalueate an expression when for a node "Node(!ST0)" we
		 * have parents Parent1(!ST0,!Z0), Parent1(!ST0,!Z1), Parent2(!ST0,!T0), and Parent2(!ST0,!T0)
		 * with values True, False, Alpha, Beta  respectively, the map should be:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * 
		 * @return: the first if/else clause which returned true.
		 */
		public TempTableHeaderCell getFirstTrueClause(Map<String, List<EntityAndArguments>> valuesOnCPTColumn) ;
		
		
		/**
		 * Tests if this container has no nested clauses.
		 * @return true if this if/else clause container has 0 nested elements
		 */
		public boolean isEmptyNestedClauses ();
		
		/**
		 * @return the clauses
		 */
		public List<TempTableHeaderCell> getNestedClauses();
		
		/**
		 * 
		 * @return the clause this object is contained within
		 */
		public INestedIfElseClauseContainer getUpperClause();
		
		
		/**
		 * sets the clause this object is contained within
		 * @param upper
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper);
		
		
		/**
		 * Initializes the "isKnownValue" attributes of TempTableHeader objects
		 * by recursively calling this method for all nested causes.
		 * @param ssbnnode
		 * @see TempTableHeader
		 * @throws NullPointerException if ssbnnode is null
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode);
		

		/**
		 * Hierarchically searches for user-defined variables in scope.
		 * @param key : name of variable to look for
		 * @return : value of the variable
		 * @see #addUserDefinedVariable(String, IExpressionValue)
		 * @see #clearUserDefinedVariables()
		 */
		public IExpressionValue getUserDefinedVariable(String key);
		
		/**
		 *  Adds a new user-defined variable retrievable from {@link #getUserDefinedVariable(String)}
		 * @see #clearUserDefinedVariables()
		 */
		public void addUserDefinedVariable(String key, IExpressionValue value);
		
		/**
		 * Deletes all user variables that were included by {@link #addUserDefinedVariable(String, IExpressionValue)}.
		 * @see #getUserDefinedVariable(String)
		 */
		public void clearUserDefinedVariables();
		
		/**
		 * @return {@link #getUserDefinedVariable(String)} for this clause and all {@link #getNestedClauses()} recursively
		 * @param keepFirst : if true, then variables found first will be kept if
		 * variables with same name are found. If false, then variables found later will be used
		 * in case of duplicate names.
		 */
		public Map<String, IExpressionValue> getUserDefinedVariablesRecursively(boolean keepFirst);
		
		/**
		 * @return instance of resident node whose LPD script is being applied.
		 * (this can be used if we are compiling a script before generating SSBN)
		 * @see #getSSBNNode()
		 */
		public IResidentNode getResidentNode();
		
		/**
		 * @return : instance of SSBN node whose LPD script is being applied during SSBN generation.
		 * May return null if compiler is called before SSBN generation. If so, {@link #getResidentNode()}
		 * must be used.
		 */
		public SSBNNode getSSBNNode();
	}
	
	/**
	 * This class represents the root of the if-else clause (i.e. this is outside all if clauses in the script).
	 * In other words, this represents the entire script itself.
	 * @author Shou Matsumoto
	 */
	public class TempTable implements INestedIfElseClauseContainer{
		
		private List<TempTableHeaderCell> clauses = null;
		private Map<String, IExpressionValue> userDefinedVariables;
		
		/**
		 * Represents the temporary CPT table (a list of if-else clauses)
		 */
		public TempTable() {
			super();
			this.clauses = new ArrayList<TempTableHeaderCell>();
		}

		/**
		 * @return true if at least one if-clause is using an embedded node feature.
		 * @see TempTableHeaderCell#hasEmbeddedNodeDeclaration()
		 */
		public boolean hasEmbeddedNodeDeclaration() {
			for (TempTableHeaderCell clause : getNestedClauses()) {
				if (clause.hasEmbeddedNodeDeclaration()) {
					// if we found at least one if-clause using an embedded node feature, then return true immediately
					return true;
				}
			}
			
			// at this point, no if-clause had embedded node.
			return false;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#addNestedClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause) {
			this.clauses.add(nestedClause);
			nestedClause.setUpperClause(this);
		}
		
		
		
		/**
		 * @return the clauses
		 */
		public List<TempTableHeaderCell> getNestedClauses() {
			return clauses;
		}

		/**
		 * Tests if this table is empty
		 * @return true if this table has 0 elements
		 */
		public boolean isEmptyNestedClauses () {
			// clauses are asserted not to be null (because we do not have a setter for clauses)
			return (this.clauses.size() == 0);
		}
		
		/**
		 * Runs recursively until we get the default distribution of the nested if/else clause's tree
		 * @return: the default clause
		 */
		public TempTableHeaderCell getDefaultClause() {
			// clauses are asserted not to be null (because we do not have a setter for clauses)
			
			// assert clauses are not empty
			if (this.isEmptyNestedClauses()){
				return null;
			}				
			
			return this.getNestedClauses().get(this.getNestedClauses().size() - 1).getDefaultClause();
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getFirstTrueClause(java.util.Map)
		 */
		public TempTableHeaderCell getFirstTrueClause(
				Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			// initial assertion
			if (this.isEmptyNestedClauses()) {
				// actually, this is an error, since at least a default distribution should be present
				return null;
			}
			
			for (TempTableHeaderCell clause : this.getNestedClauses()) {
				if (clause.evaluateBooleanExpressionTree(valuesOnCPTColumn)) {
					return clause.getFirstTrueClause(valuesOnCPTColumn);
				}
			}
			
			// no clause was found... This is an error, since at least the default distro should return true...
			return null;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUpperClause()
		 */
		public INestedIfElseClauseContainer getUpperClause() {
			// No  upper clause should exist to temporary table (the temporary table should be the upper-most)
			return null;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#setUpperClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper) {
			// No  upper clause should exist to temporary table (the temporary table should be the upper-most)
			// do nothing
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#cleanUpKnownValues(unbbayes.prs.mebn.ssbn.SSBNNode)
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode) {
			// no try-catch for NullPointerException should be necessary, since allways this.getNestedClauses != null
			for (TempTableHeaderCell clause : this.getNestedClauses()) {
				clause.cleanUpKnownValues(ssbnnode);
			}
		}
		
		/**
		 * @return the userDefinedVariables : mapping from user-defined variable names to its values.
		 */
		protected Map<String, IExpressionValue> getUserDefinedVariables() {
			if (userDefinedVariables == null) {
				userDefinedVariables = new HashMap<String, IExpressionValue>();
			}
			return userDefinedVariables;
		}
		/**
		 * @param userDefinedVariables : mapping from user-defined variable names to its values.
		 */
		protected void setUserDefinedVariables(Map<String, IExpressionValue> userDefinedVariables) {
			this.userDefinedVariables = userDefinedVariables;
		}
		

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUserDefinedVariable(java.lang.String)
		 */
		public IExpressionValue getUserDefinedVariable(String key) {
			// this is the top level, so return immediately
			return getUserDefinedVariables().get(key);
		}
		
		/**
		 *  Delegates to {@link #getUserDefinedVariables()} and {@link Map#put(Object, Object)}
		 */
		public void addUserDefinedVariable(String key, IExpressionValue value){
			getUserDefinedVariables().put(key, value);
		}
		
		/**
		 * Simply delegates to {@link #getUserDefinedVariables()} and {@link Map#clear()}
		 */
		public void clearUserDefinedVariables() {
			getUserDefinedVariables().clear();
		}

		public IResidentNode getResidentNode() { return Compiler.this.getNode(); }
		public SSBNNode getSSBNNode() { return Compiler.this.getSSBNNode(); }

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUserDefinedVariablesRecursively(boolean)
		 */
		public Map<String, IExpressionValue> getUserDefinedVariablesRecursively(boolean keepFirst) {
			Map<String, IExpressionValue> ret = new HashMap<String, Compiler.IExpressionValue>(getUserDefinedVariables());
			for (TempTableHeaderCell nested : getNestedClauses()) {
				Map<String, IExpressionValue> recursive = nested.getUserDefinedVariablesRecursively(keepFirst);
				if (keepFirst) {
					// do not overwrite existing entries
					for (Entry<String, IExpressionValue> entry : recursive.entrySet()) {
						if (!ret.containsKey(entry.getKey())) {
							ret.put(entry.getKey(), entry.getValue());
						}
					}
				} else {
					// overwrite all existing ones
					ret.putAll(recursive);
				}
			}
			return ret;
		}
		
		
	}
	
	/**
	 * This class represents either an if-clause or an else-clause.
	 * If this is an else-clause, then {@link TempTableHeaderCell#isDefault()}
	 * is true (because it is the default block under current scope of if-then-else clause).
	 * @author Shou Matsumoto
	 */
	public class TempTableHeaderCell implements INestedIfElseClauseContainer {
		private ICompilerBooleanValue booleanExpressionTree = null; // core of the if statement
		private List<TempTableHeader> parents = null;	// this is also the leaf of boolean expression tree

		private String varsetname = "";
		
		private boolean isAny = true;
		private boolean isDefault = false;
		
		private List<TempTableProbabilityCell> cellList = null;
		
		private int validParentSetCount = 0;
		
		float leastCellValue = Float.NaN;
		
		SSBNNode currentSSBNNode = null;
		
		private List<TempTableHeaderCell> nestedIfs = null;
		
		private INestedIfElseClauseContainer upperContainer = null;
		
		private Map<String, IExpressionValue> userDefinedVariables;
		
		/**
		 * Represents an entry for temporary table header (parents and their expected single values
		 * at that table entry/collumn).
		 * It can directly represent an if-clause (varsetname, any|all, bExpression, values, and nested ifs)
		 * Since an if-clause may be nested, it has a list of nested if-clauses
		 * @param parents: entries of an if-clause (list of (parent = value) pairs)
		 * @param isAny
		 * @param isDefault
		 */
		public TempTableHeaderCell (List<TempTableHeader> parents , boolean isAny, boolean isDefault, SSBNNode currentSSBNNode) {
			this.parents = parents;
			this.isAny = isAny;
			this.isDefault = isDefault;
			this.cellList = new ArrayList<TempTableProbabilityCell>();
			this.validParentSetCount = 0;
			this.currentSSBNNode = currentSSBNNode;
			this.nestedIfs = new ArrayList<TempTableHeaderCell>();
		}
		/**
		 * It Gets entries of boolean expression (a pair of Node and its expected value declared
		 * within a boolean expression inside a if clause)
		 * @return List of expected parents within if-clause
		 */		
		public List<TempTableHeader> getParents() {
			return parents;
		}


		/**
		 * counts how many parents set were returning true at that sentence (column) of boolean
		 * expression.
		 * @return the validParentSetCount
		 */
		public int getValidParentSetCount() {
			return validParentSetCount;
		}
		/**
		 * @param validParentSetCount the validParentSetCount to set
		 */
		public void setValidParentSetCount(int validParentSetCount) {
			this.validParentSetCount = validParentSetCount;
		}
		
		public void increaseValidParentSetCount() {
			this.validParentSetCount++;
		}
		
		public void setParents(List<TempTableHeader> parents) {
			this.parents = parents;
		}
		
		public void addParent(TempTableHeader parent) {
			this.parents.add(parent);
		}
		
		/**
		 * @return true if this header is declared as "any", false if "all".
		 */
		public boolean isAny() {
			return isAny;
		}
		/**
		 * @param isAny : true if this header is declared as "any", false if "all".
		 */
		public void setAny(boolean isAny) {
			this.isAny = isAny;
		}
		/**
		 * @return true if this header is declaring a default distro, false otherwise
		 */
		public boolean isDefault() {
			return isDefault;
		}
		/**
		 * @param isDefault : if this header is declaring a default distro, false otherwise
		 */
		public void setDefault(boolean isDefault) {
			this.isDefault = isDefault;
		}
		
		/**
		 * Checks if the argument is a correct parent set names (if parents of this node are
		 * declared within varsetname). E.g: if varsetname == "st.z", tests if parents has those
		 * OVs as their arguments.
		 * @param varsetname
		 * @return
		 */
		public boolean isParentSetName(String varsetname) {
			// remove undesired characters
			varsetname = varsetname.replaceAll("\\s","");
			// make sure ',' can also be used as separators
			varsetname = varsetname.replace(',', this.currentSSBNNode.getStrongOVSeparator().charAt(0));
			
			Collection<SSBNNode> parentSet = null;
			if (isExactMatchStrongOV()) {
				parentSet = this.currentSSBNNode.getParentSetByStrongOVWithWeakOVCheck(varsetname.split("\\" + this.currentSSBNNode.getStrongOVSeparator()));
			} else {
				// we just need to find at least one parent matching this ov
				parentSet = new ArrayList<SSBNNode>();
				for (String ovName : varsetname.split("\\" + this.currentSSBNNode.getStrongOVSeparator())) {
					parentSet = this.currentSSBNNode.getParentSetByStrongOV(false, ovName);
					if (!parentSet.isEmpty()) {
						return true;
					}
				}
			}
			return parentSet.size() > 0;
		}
		/**
		 * @return the varsetname
		 */
		public String getVarsetname() {
			return varsetname;
		}
		/**
		 * @param varsetname the varsetname to set
		 */
		public void setVarsetname(String varsetname) {
			this.varsetname = varsetname;
		}
		/**
		 * @return the cellList: possible values and its probability
		 */
		public List<TempTableProbabilityCell> getCellList() {
			return cellList;
		}
		/**
		 * @param cellList the cellList to set
		 */
		protected void setCellList(List<TempTableProbabilityCell> cellList) {
			this.cellList = cellList;
		}
		
		
		/**
		 * @return the nestedIfs
		 */
		public List<TempTableHeaderCell> getNestedClauses() {
			return nestedIfs;
		}
		/**
		 * @param nestedIfs the nestedIfs to set
		 */
		public void setNestedIfs(List<TempTableHeaderCell> nestedIfs) {
			this.nestedIfs = nestedIfs;
		}
		public void addCell(Entity possibleValue , IExpressionValue probability) {
			this.addCell(new TempTableProbabilityCell(possibleValue, probability));
		}
		
		public void addCell(TempTableProbabilityCell cell) {
			if (cell == null) {
				return;
			}
			float value = Float.NaN;
			try {
				value = cell.getProbabilityValue();
			} catch (InvalidProbabilityRangeException e) {
				// do nothing at now - it will be detected soon
			}
			// update the smallest declared probability value
			if (value > 0.0f) {
				if (Float.isNaN(this.leastCellValue) || (value < this.leastCellValue)) {
					this.leastCellValue = value;
				}
			}
			
			this.cellList.add(cell);
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return
		 */
		public boolean isSumEquals1() throws InvalidProbabilityRangeException {
//			if (!isToNormalize()) {
//				return true;	// just return a default value if we don't need normalization
//			}
			float value = this.getProbCellSum();
			// (this.leastCellValue/2) is the error margin
			if ( (value >= 1f - (this.leastCellValue/2f)) && (value <= 1f + (this.leastCellValue/2f)) ) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return the sum of all cell's probability
		 */
		public float getProbCellSum() throws InvalidProbabilityRangeException {
			float sum = Float.NaN;
			if (this.cellList == null) {
				return sum;
			}
			if (this.cellList.size() <= 0) {
				return sum;
			}
			sum = 0.0f;
			for (TempTableProbabilityCell cell : this.cellList) {
				sum = (float)sum + (float)cell.getProbabilityValue();
			}
			return sum;
		}
		/**
		 * @return the booleanExpressionTree
		 */
		public ICompilerBooleanValue getBooleanExpressionTree() {
			return booleanExpressionTree;
		}
		/**
		 * @param booleanExpressionTree the booleanExpressionTree to set
		 */
		public void setBooleanExpressionTree(ICompilerBooleanValue booleanExpressionTree) {
			this.booleanExpressionTree = booleanExpressionTree;
		}


		
		/**
		 * This method evaluates a boolean expression (ICompilerBooleanValue - in tree format).
		 * The leaf-values are evaluated using the param. 
		 * Note that this method can used to evaluate a collum of ProbabilisticTable
		 * (consider evaluating each given collumn of CPT is like evaluating every parents as findings
		 * at a given moment).
		 * A "default" header will allways return true
		 * @param valuesOnCPTColumn: a map which the key is a name of a parent node and
		 * the value is its current possible values to be evaluated.
		 * For example, if we want to evalueate an expression when for a node "Node(!ST0)" we
		 * have parents Parent1(!ST0,!Z0), Parent1(!ST0,!Z1), Parent2(!ST0,!T0), and Parent2(!ST0,!T0)
		 * with values True, False, Alpha, Beta  respectively, the map should be:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * @return: if the map is as follows:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * Then, the result would be:
		 * - if this object was once declared as "ANY" (isAny() == true):
		 * 			returns: evaluation(True,Alpha) || evaluation(True,Beta) || evaluation(False,Alpha) || evaluation(False,Beta)
		 * - if declared as "ALL" (isAny() == false):
		 * 			returns: evaluation(True,Alpha) && evaluation(True,Beta) && evaluation(False,Alpha) && evaluation(False,Beta)
		 */
		public boolean evaluateBooleanExpressionTree(Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			// initial test
			if (this.isDefault()) {
				return true;
			}
			

			// reset cardinality counter
			this.setValidParentSetCount(0);
			
			List<TempTableHeader> parentsList = this.getParents();
			
			//	return is initialized with a boolean neutral value (on OR/ANY, its "false"; on AND/ALL, its "true")
			boolean ret = !this.isAny();	// this method will return this value
			
			// start evaluation. We should run through leafs again... TODO: optimize?
			
			// "pointer" 
			TempTableHeader pointer = null;
			
			// prepare leafs
			
			// run inside parent list (they are the declared condicionants within boolean expression),
			// which also are the leafs of that expression!
			for (TempTableHeader leaf : parentsList) {
				if (!leaf.isKnownValue()) {
					// if leaf is not set to be a constant value, then we should set it to 
					// evaluate a combination of entities
					leaf.setEvaluationList(valuesOnCPTColumn.get(leaf.getParent().getName()));
				}
			}
			
			//	evaluate (True,Alpha), (False,Alpha), (True,Beta), (False,Beta)...
			boolean hasMoreCombination = true;
			// expressionWasEvaluated checks if boolean header was once evaluated. If not, all condicionants were invalid (in that case, return false)
			boolean expressionWasEvaluated = false;
			while (hasMoreCombination) {
				// TODO the test below might be dangerous in multithread application...?
				if (isSameOVsameEntity()) { // only evaluates same entities for same OVs
					if (this.isAny()) {	// if ANY, then OR 
						boolean evaluation = this.getBooleanExpressionTree().evaluate();
						ret = ret || evaluation;
						if (evaluation) {
							this.increaseValidParentSetCount();
							// we cant return immediately because we should count "cardinality"
						}
					} else {	// if ALL, then AND
						ret = ret && this.getBooleanExpressionTree().evaluate();
						if (ret == false) {
							return false;
						}
						this.increaseValidParentSetCount();
					}
					expressionWasEvaluated = true; // there was a valid header, so, the return value is valid
//				} else {
//					Debug.println(getClass(), "isSameOVsameEntity returned false for node " + getNode());
				}
				
				// update leaf's evaluation variables
				pointer = parentsList.get(0);
				if (pointer.hasNextEvaluation()) {
					pointer.getNextEvaluation();	// now the leaf will evalueate the next element of EvaluationList
				} else {
					int lastPos = 0;
					// if reached the end of this list, reset it and step foward the next list, and so on
					while ( !pointer.hasNextEvaluation() && (lastPos < parentsList.size())) {
						pointer.resetEvaluationList();
						if (lastPos == parentsList.size() - 1) {
							// if we just changed all the values through first to last, then no more changes are available
							hasMoreCombination = false;
							break;
						}
						lastPos++;	
						pointer = parentsList.get(lastPos);
						
					}
					if (hasMoreCombination) {
						if (parentsList.get(lastPos).hasNextEvaluation()) {
							parentsList.get(lastPos).getNextEvaluation(); //step to next evaluation
						}
					}
				}
			}	
			
			if (expressionWasEvaluated) {
				// since the expression was actually evaluated, ret would be the result of evaluation.
				return ret;
			}
			
			// if the boolean expression was never evaluated, ret is storing a default boolean neutral value.
			// We do not want that (since a neutral in "Any" is false and in "All" is true). If no expression was
			// actually evaluated, it means that the whole expression was invalid (it happens in meantimes like
			// when the "strong OV set" is not matching any parents) and a invalid expression should return false!
//			Debug.println("!!!No valid expression was evaluated by the compiler!!!");
//			Debug.println("   It happens when the parents expected by the pseudocode was never linked by the SSBN Algorithm");
//			Debug.println("   or all connected parents was not expected by the pseudocode (invalidated by the boolean expression)");
			return false;
		}
		
		/**
		 * tests if when an argument of a leaf is the same OV, then it should have
		 * the same value at a given moment.
		 * Tests also between this ssbnnode and the leaves
		 * @return
		 */
		public boolean isSameOVsameEntity() {
			List<TempTableHeader> leaves = this.getParents(); // leaves of boolean expression evaluation tree
			
			// prepare a set with names of OVs declared in current if-clause, 
			// We'll use it later in order to check whether the OVs in leaf.getCurrentEntityAndArguments() matches with the ones declared in current if-clause
			// TODO migrate this set to somewhere else so that we don't have to instantiate the same set several times
			Collection<String> varSetNamesInCurrentIfClause = new ArrayList<String>();	// Note: we may use HashSet if we expect many OVs declared in a single if-clause	
			// Note: at this point, getVarsetname() is a string representing the ovs declared in this if-clause, and the ovs are separated by getSSBNNode().getStrongOVSeparator()
			for (String ovName : getVarsetname().split("\\" + getSSBNNode().getStrongOVSeparator())) {
				varSetNamesInCurrentIfClause.add(ovName);
			}
			
			for (TempTableHeader leaf : leaves) {
				if (leaf.isKnownValue()) {
					continue;
				}
				List<OVInstance> args = leaf.getCurrentEntityAndArguments().arguments;
				if (args.isEmpty()) {
					// ignore nodes with no arguments
					continue;
				}
				
				// at least 1 OV in arguments shall be declared in the varset field of this if-clause, or else current combination of values of parents must be ignored
				boolean isAtLeast1OVDeclaredInVarsetname = false;
				
				// first, test if leaf has same arguments as its ssbnnode (if ssbnnode has same arguments as parents)
				for (OVInstance argParent : args) {
					// check condition to activate the flag (i.e. to change content of isAllOVsDeclaredInVarsetname)
					// check if the ov of this argument was declared in the varsetname field of current if-clause
					if (varSetNamesInCurrentIfClause.contains(argParent.getOv().getName())) {
						isAtLeast1OVDeclaredInVarsetname = true;	// we found at least 1 OV, so turn the flag on
					} else if (!argParent.getOv().getValueType().hasOrder() // we don't need to consider weak ovs
								&& isExactMatchStrongOV()) {
						// we can immediately return if compiler requires exact match of strong ovs
						return false;
					}
					
					// if it has same OV as ssbnnode, then should be the same entity
					for (OVInstance argChild : this.currentSSBNNode.getArguments()) {
						if (argChild.getOv().getName().equalsIgnoreCase(argParent.getOv().getName())) {
							if (!argChild.getEntity().getInstanceName().equalsIgnoreCase(argParent.getEntity().getInstanceName())) {
								return false;
							}
						}
					}
				}
				
				if (!isAtLeast1OVDeclaredInVarsetname) {
					// current value of parents was not declared in the varsetname field of current if-clause, so we should not consider it.
					return false;
				}
				
				for (int i = leaves.indexOf(leaf) + 1; i < leaves.size(); i++) {
					// try all other leaves
					for (OVInstance argleaf : args) {
						if (leaves.get(i).isKnownValue()) {
							// if current leaf has a known value (i.e. it is allways evaluating false), then
							// it is not necessary to test OVInstance's name-value consistency
							// (we don't have to check if OVs with same name has same value, since
							// at evaluation time their values are not going to be used at all)
							continue;
						}
						for (OVInstance argothers : leaves.get(i).getCurrentEntityAndArguments().arguments) {
							if(argleaf.getOv().getName().equalsIgnoreCase(argothers.getOv().getName())) {
								if (!argleaf.getEntity().getInstanceName().equalsIgnoreCase(argothers.getEntity().getInstanceName()) ) {
									// if they are the same OV but different instances of Entities... then false
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}
		
		/**
		 * When a boolean expression refeers a node which doesn't have an argument declared inside
		 * varsetname (is not part of a "similar" set of parent called inside the pseudocode),
		 * it is useless to evaluate it (because it would be allways false at our implementation). 
		 * This method detects them and sets them to false before starting definitive CPT generation.
		 * @param baseSSBNNode: a SSBNNode which contains this CPT. This method doesn't check
		 * if this SSBNNode is really the expected one. This argument is used by this method
		 * in order to obtain the "similar" parent set at a given moment.
		 */
		public void cleanUpByVarSetName(SSBNNode baseSSBNNode) {
			
			// no cleanup is necessary when this is a default distro - no boolean evaluation is present
			if (this.isDefault()) {
				return;
			}
			
			// extracts parents' similar sets by strong OV names
			Collection<SSBNNode> parents = null;
			if (isExactMatchStrongOV()) {
				parents = baseSSBNNode.getParentSetByStrongOVWithWeakOVCheck(
						this.getVarsetname().split("\\" + baseSSBNNode.getStrongOVSeparator()));
			} else { // consider all parents that has at least one of the specified OVs in its argument
				parents = new HashSet<SSBNNode>();
				// search parents for each specified OV
				for (String ovName : this.getVarsetname().split("\\" + baseSSBNNode.getStrongOVSeparator())) {
					parents.addAll(baseSSBNNode.getParentSetByStrongOV(
							false,						// not an exact match
							ovName
						)
					);
				}
			}
			
			boolean found = false;
			
			// extract condicionants declared within the expression
			for (TempTableHeader headParent : this.getParents()) {
				 found = false;
				 // look for headParent inside parents (set of similar parents)
				 for (SSBNNode node : parents) {
					if (node.getResident().equals(headParent.getParent())) {
						// if node names are the same, then we found it
						found = true;
						break;
					}
				 }
			     if (!found) {
			    	 // if we did not find a condicionant (inside expression) inside the parent set
			    	 headParent.setKnownValue(true);
			     }
			}
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#addNestedClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause) {
			this.getNestedClauses().add(nestedClause);
			nestedClause.setUpperClause(this);
		}
		
		/**
		 * Searches recursively for the most default clause of nested ifs.
		 * If this object has no nested children, it tests if
		 * the object itself is a default object (checks isDefault attribute).
		 * It assumes the default child is always the last element of each
		 * nested if-clause (i.e. the last else-clause)
		 * @return: the else clause of all else clauses. Null if no default
		 * clause is found...
		 */
		public TempTableHeaderCell getDefaultClause() {
			if (this.isEmptyNestedClauses()) {
				if (this.isDefault()) {
					return this;
				} else {
					// if this code is reached, this object should have been returned, but its not a default clause...
					return null;
				}
			} else {
				// search for a default clause recursively by looking only the last nested if-clause
				// OBS. the last nested if-clause should be the default else-clause 
				return this.getNestedClauses().get(this.getNestedClauses().size() - 1).getDefaultClause();
			}
			
		}
		
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getFirstTrueClause(java.util.Map)
		 */
		public TempTableHeaderCell getFirstTrueClause(
				Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			if (this.isEmptyNestedClauses()) {
				// if there is no nested if/else clauses, then this object itself is the first true clause.
				return this;
			}
			
			for (TempTableHeaderCell nested : this.getNestedClauses()) {
				if (nested.evaluateBooleanExpressionTree(valuesOnCPTColumn)) {
					// found something returning true
					// look deep inside recursively.
					return nested.getFirstTrueClause(valuesOnCPTColumn);
				}
			}
			
			// since else-clause is mandatory, the code below should never be reached
			
			return null;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#isEmptyNestedClauses()
		 */
		public boolean isEmptyNestedClauses() {
			if (this.getNestedClauses() == null) {
				return true;
			}
			return this.getNestedClauses().size() == 0;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUpperClause()
		 */
		public INestedIfElseClauseContainer getUpperClause() {
			return this.upperContainer;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#setUpperClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper) {
			this.upperContainer = upper;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#cleanUpKnownValues(unbbayes.prs.mebn.ssbn.SSBNNode)
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode) {
			// clean up myself
			this.cleanUpByVarSetName(ssbnnode);
			
			// clean up my nested children recursively
			try {
				for (TempTableHeaderCell clause : this.getNestedClauses()) {
					clause.cleanUpKnownValues(ssbnnode);
				}
			} catch (NullPointerException e) {
				// there were no nested clauses. It is not an error. Do nothing
			}
		}
		
		/**
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#hasEmbeddedNodeDeclaration()
		 * @see TempTableHeader#hasEmbeddedNodeDeclaration()
		 */
		public boolean hasEmbeddedNodeDeclaration() {
			if (parents != null) {
				for (TempTableHeader header : parents) {
					if (header.hasEmbeddedNodeDeclaration()) {
						// if at least one is an embedded node, then return true immediately
						return true;
					}
				}
			}
			// if did not find embedded node yet, check for nested ifs
			if (nestedIfs != null) {
				for (TempTableHeaderCell nestedClause : nestedIfs) {
					if (nestedClause.hasEmbeddedNodeDeclaration()) {
						// again, immediately return true if found at least one
						return true;
					}
				}
			}
			
			// at this point, we did not find any embedded node declaration in the if-clause
			return false;
		}
		/**
		 * @return the userDefinedVariables : mapping from user-defined variable names to its values.
		 */
		protected Map<String, IExpressionValue> getUserDefinedVariables() {
			if (userDefinedVariables == null) {
				userDefinedVariables = new HashMap<String, IExpressionValue>();
			}
			return userDefinedVariables;
		}
		/**
		 * @param userDefinedVariables : mapping from user-defined variable names to its values.
		 */
		protected void setUserDefinedVariables(Map<String, IExpressionValue> userDefinedVariables) {
			this.userDefinedVariables = userDefinedVariables;
		}
		
		/**
		 * Delegates to {@link #getUserDefinedVariables()}.
		 * If not present, delegates to upper if-clause (if there are nested if-clauses).
		 * @param key : name of variable to look for
		 * @return : value of the variable
		 */
		public IExpressionValue getUserDefinedVariable(String key) {
			IExpressionValue value = getUserDefinedVariables().get(key);
			if (value != null) {
				// found
				return value;
			}
			// look recursively
			INestedIfElseClauseContainer upper = getUpperClause();
			if (upper != null) {
				return upper.getUserDefinedVariable(key);
			}
			// nothing found
			return null;
		}
		
		/**
		 *  Delegates to {@link #getUserDefinedVariables()} and {@link Map#put(Object, Object)}
		 */
		public void addUserDefinedVariable(String key, IExpressionValue value){
			getUserDefinedVariables().put(key, value);
		}
		
		/**
		 * Simply delegates to {@link #getUserDefinedVariables()} and {@link Map#clear()}
		 */
		public void clearUserDefinedVariables() {
			getUserDefinedVariables().clear();
		}
		
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		/*
		//@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TempTableHeaderCell) {
				TempTableHeaderCell arg = (TempTableHeaderCell)arg0;
				if (arg.getParents().containsAll(this.getParents()) 
						|| this.getParents().containsAll(arg.getParents()) ) {
					return true;
				}
			}
			return false;
		}
		*/
		
		public IResidentNode getResidentNode() { return Compiler.this.getNode(); }
		public SSBNNode getSSBNNode() { return Compiler.this.getSSBNNode(); }
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUserDefinedVariablesRecursively(boolean)
		 */
		public Map<String, IExpressionValue> getUserDefinedVariablesRecursively(boolean keepFirst) {
			Map<String, IExpressionValue> ret = new HashMap<String, Compiler.IExpressionValue>(getUserDefinedVariables());
			for (TempTableHeaderCell nested : getNestedClauses()) {
				Map<String, IExpressionValue> recursive = nested.getUserDefinedVariablesRecursively(keepFirst);
				if (keepFirst) {
					// do not overwrite existing entries
					for (Entry<String, IExpressionValue> entry : recursive.entrySet()) {
						if (!ret.containsKey(entry.getKey())) {
							ret.put(entry.getKey(), entry.getValue());
						}
					}
				} else {
					// overwrite all existing ones
					ret.putAll(recursive);
				}
			}
			return ret;
		}
	}
	
	public interface ICompilerBooleanValue {
		/**
		 * Obtains recursively a boolean value
		 * @return true or false
		 */
		public boolean evaluate();
	}
	
	public class CompilerNotValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value = null;
		/**
		 * implements "not" operation on ISSBNBooleanValue
		 * @param value: value to be operated
		 */
		public CompilerNotValue(ICompilerBooleanValue value) {
			this.value = value;
		}
		
		/**
		 * @return the value
		 */
		public ICompilerBooleanValue getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(ICompilerBooleanValue value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ISSBNBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return !this.value.evaluate();
		}
		
	}
	
	public class CompilerOrValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value1 = null;
		private ICompilerBooleanValue value2 = null;
		/**
		 * implements an "OR" boolean operation on ICompilerBooleanValue
		 * @param value1: value to be operated
		 * @param value2: value to be operated
		 */
		public CompilerOrValue(ICompilerBooleanValue value1, ICompilerBooleanValue value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		/**
		 * @return the value1
		 */
		public ICompilerBooleanValue getValue1() {
			return value1;
		}
		/**
		 * @param value1 the value1 to set
		 */
		public void setValue1(ICompilerBooleanValue value1) {
			this.value1 = value1;
		}
		/**
		 * @return the value2
		 */
		public ICompilerBooleanValue getValue2() {
			return value2;
		}
		/**
		 * @param value2 the value2 to set
		 */
		public void setValue2(ICompilerBooleanValue value2) {
			this.value2 = value2;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ICompilerBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return this.value1.evaluate() || this.value2.evaluate();
		}
		
	}
	
	public class CompilerAndValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value1 = null;
		private ICompilerBooleanValue value2 = null;
		/**
		 * implements an "OR" boolean operation on ICompilerBooleanValue
		 * @param value1: value to be operated
		 * @param value2: value to be operated
		 */
		public CompilerAndValue(ICompilerBooleanValue value1, ICompilerBooleanValue value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		/**
		 * @return the value1
		 */
		public ICompilerBooleanValue getValue1() {
			return value1;
		}
		/**
		 * @param value1 the value1 to set
		 */
		public void setValue1(ICompilerBooleanValue value1) {
			this.value1 = value1;
		}
		/**
		 * @return the value2
		 */
		public ICompilerBooleanValue getValue2() {
			return value2;
		}
		/**
		 * @param value2 the value2 to set
		 */
		public void setValue2(ICompilerBooleanValue value2) {
			this.value2 = value2;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ICompilerBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return this.value1.evaluate() && this.value2.evaluate();
		}
		
	}
	
	/**
	 * This class represents an entry in the LPD script in the format of "ident = ident" inside an if-clause.
	 * @author Shou Matsumoto
	 * @see TempTableHeader
	 * @see TempTableHeaderOV
	 */
	public abstract class TempTableHeader implements ICompilerBooleanValue, IEmbeddedNodeUser{
		private Node parent = null;
		private Entity value = null;
		
		private List<EntityAndArguments> evaluationList = null;
		
		//private Entity currentEvaluation = null;
		
		private int currentEvaluationIndex = -1;
		
		private boolean isKnownValue = false;	// if this leaf is "absurd", then its value is known = false.
		
		/**
		 * 
		 * @return which parent this leaf represents
		 */
		public Node getParent() {
			return parent;
		}
		
		public void setParent(Node parent) {
			this.parent = parent;
		}
		/** This can be null if this header represents a declaration which doesn't associate a variable to a single value */
		public Entity getValue() {
			return value;
		}
		/** This can be null if this header represents a declaration which doesn't associate a variable to a single value */
		public void setValue(Entity value) {
			this.value = value;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0) {
			if (this == arg0) {
				return true;
			}
			if (arg0 instanceof TempTableHeader) {
				TempTableHeader arg = (TempTableHeader)arg0;
				if (this.parent.getName().equalsIgnoreCase(arg.getParent().getName())) {
					if (this.value == null) {
						return arg.getValue() == null;
					} else if (this.value.getName().equalsIgnoreCase(arg.getValue().getName())) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			//return false;
		}
		
		
		/**
		 * evaluates this leaf boolean value and returns it.
		 * @return : evaluated value. If isKnownValue is set to true, 
		 * this will return false everytime
		 * 
		 */
		public boolean evaluate() {
			if (this.isKnownValue()) {
				return false;
			}
			// handle the case when this.getValue() is null
			if (this.getValue() == null) {
				// if the value is null, then we just need to see if expected evaluation is also null
				return this.getCurrentEvaluation() == null;
			}
			// if entities have the same name, they are equals.
			if (this.getCurrentEvaluation().getName().equalsIgnoreCase(this.getValue().getName())) {
				return true;
			}
			return false;
		}
		
		
		
		/**
		 * Obtains a list of values to be tested on boolean value evaluation
		 * @return the evaluationList
		 */
		public List<EntityAndArguments> getEvaluationList() {
			return evaluationList;
		}
		/**
		 * note: if param is null, it will set the value of this object as known value = "false"
		 * (assume this boolean evaluation as allways false) immediately.
		 * @param evaluationList the evaluationList to set
		 */
		public void setEvaluationList(List<EntityAndArguments> evaluationList) {
			this.evaluationList = evaluationList;
			if (this.evaluationList != null) {
				if (this.evaluationList.size() > 0) {
					this.currentEvaluationIndex = 0;
				}
			} else {
				// isKnownValue = true indicates that expression as value "false" no matter when, where or how it is evaluated
				this.isKnownValue  = true; // we assume if set to null, then false immediately
			}
			
		}
		
		
		/**
		 * @return the currentEvaluation (currently evaluated entity value on boolean
		 * expression - comparing a parent with its value)
		 * @see #getCurrentEntityAndArguments()
		 */
		public Entity getCurrentEvaluation() {
//			EntityAndArguments entityAndArguments = this.getCurrentEntityAndArguments();
//			if (entityAndArguments == null) {
//				return null;
//			}
//			return entityAndArguments.entity;
			if (this.evaluationList == null) {
				return null;
			}
			return this.evaluationList.get(this.currentEvaluationIndex).entity;
		}
		
		/**
		 * Obtains a compact representation of the current value of
		 * a node and its arguments.
		 * @return
		 */
		public EntityAndArguments getCurrentEntityAndArguments() {
			if (this.evaluationList == null) {
				return null;
			}
			return this.evaluationList.get(this.currentEvaluationIndex);
		}
		
		/**
		 * @param currentEvaluation the currentEvaluation 
		 * (currently evaluated entity value on boolean
		 * expression - comparing a parent with its value) to set
		 */
		public void setCurrentEvaluation(Entity currentEvaluation) {
			this.currentEvaluationIndex = this.evaluationList.indexOf(currentEvaluation);
		}
		
		/**
		 * 
		 * @return true if currently evaluated entity list has next element
		 */
		public boolean hasNextEvaluation() {
			// the below chech is truly dangerous, since if this.evaluationList == null at this moment,
			// it means that something was wrong long way before... It might hide important bugs...
			// but in order to mantain execution security, we are making those checks.
			if (this.evaluationList == null) {
//				Debug.println("!!!==========================================!!!");
//				Debug.println("There was an attempt to evaluate a expression with no possible states.");
//				Debug.println("It may happen sometimes at SSBN generation when pseudocode expects parents");
//				Debug.println("   but no valid parents were linked to its node.");
//				Debug.println("!!!==========================================!!!");
				return false; 
			}
			
			return (this.currentEvaluationIndex + 1 ) < this.evaluationList.size();
		}
		
		/**
		 * add 1 to the index of evaluationList and returns its value
		 * @return
		 */
		public Entity getNextEvaluation() {
			this.currentEvaluationIndex++;
			return this.getCurrentEvaluation();
		}
		
		/**
		 * resets evaluationlist's index to its initial value = 0
		 * or -1 if list is empty
		 *
		 */
		public void resetEvaluationList() {
			if (this.evaluationList == null) {
				this.currentEvaluationIndex = -1;
				return;
			}
			if (this.evaluationList.size() <= 0) {
				this.currentEvaluationIndex = -1;
				return;
			}
			this.currentEvaluationIndex = 0;
		}

		/**
		 * If this is set to true, this object's evaluate() method will always
		 * return a same value.
		 * @return the isKnownValue
		 */
		public boolean isKnownValue() {
			return isKnownValue;
		}

		/**
		 * If this is set to true, this object's evaluate() method will always
		 * return a same value.
		 * @param isKnownValue the isKnownValue to set
		 */
		public void setKnownValue(boolean isKnownValue) {
			this.isKnownValue = isKnownValue;
			if (this.isKnownValue()) {
				// if this leaf should return a constant, then evaluationList is useless...
				this.setEvaluationList(null);
				//this.setEvaluationList(new ArrayList<EntityAndArguments>());
			}
		}
	}
	
	/**
	 * This class represents a content of an if-clause in the format like 
	 * "Variable=Variable". For example: Node=Node or Node=OV or OV=Node or OV=OV.
	 * @author Shou Matsumoto
	 */
	public class TempTableHeaderVariableEqualsVariable implements ICompilerBooleanValue {
		
		private TempTableHeader header1;
		private TempTableHeader header2;

		/** 
		 * This object represents a content of an if-clause in the format like 
		 * "Variable=Variable". For example: Node=Node or Node=OV or OV=Node or OV=OV. 
		 * @param header1 : this is the left-side of "="
		 * @param header2 : this is the right-side of "="
		 */
		public TempTableHeaderVariableEqualsVariable(TempTableHeader header1, TempTableHeader header2) {
			this.header1 = header1;
			this.header2 = header2;
		}

		/** This is the right-side of "=" */
		public TempTableHeader getHeader2() { return header2; }
		/** This is the right-side of "=" */
		public void setHeader2(TempTableHeader header2) { this.header2 = header2; }
		/** This is the left-side of "=" */
		public TempTableHeader getHeader1() { return header1; }
		/** This is the left-side of "=" */
		public void setHeader1(TempTableHeader header1) { this.header1 = header1; }

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeader#evaluate()
		 */
		public boolean evaluate() {
			return this.header1.getCurrentEvaluation().getName().equalsIgnoreCase(this.header2.getCurrentEvaluation().getName());
		}
		
		public boolean equals(Object arg0) {
			if (this == arg0) {
				return true;
			}
			if (arg0 instanceof TempTableHeaderVariableEqualsVariable) {
				TempTableHeaderVariableEqualsVariable arg = (TempTableHeaderVariableEqualsVariable)arg0;
				if (this.header1.getParent().getName().equalsIgnoreCase(arg.getHeader1().getParent().getName())) {
					if (this.header2.getParent().getName().equalsIgnoreCase(arg.getHeader2().getParent().getName())) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			//return false;
		}
		
//		public boolean hasEmbeddedNodeDeclaration() { return getHeader1().hasEmbeddedNodeDeclaration() || getHeader2().hasEmbeddedNodeDeclaration(); }
//		public Node getParent() { return this.header1.getParent();}
//		public void setParent(Node parent) { throw new UnsupportedOperationException(); }
//		public Entity getValue() { return this.header1.getValue(); }
//		public void setValue(Entity value) { throw new UnsupportedOperationException();  }
//		public List<EntityAndArguments> getEvaluationList() { return null; }
//		public void setEvaluationList(List<EntityAndArguments> evaluationList) { throw new UnsupportedOperationException();  }
//		public Entity getCurrentEvaluation() {return null;}
//		public EntityAndArguments getCurrentEntityAndArguments() {return null;}
//		public void setCurrentEvaluation(Entity currentEvaluation) { throw new UnsupportedOperationException();  }
//		public boolean hasNextEvaluation() { return this.header1.hasNextEvaluation() || this.header2.hasNextEvaluation(); }
//		public Entity getNextEvaluation() {return null;}
//		
//		public void resetEvaluationList() {
//			this.header1.resetEvaluationList();
//			this.header2.resetEvaluationList();
//		}
//
//		public boolean isKnownValue() { return this.header1.isKnownValue && this.header2.isKnownValue; }
//		public void setKnownValue(boolean isKnownValue) {
//			this.header1.setKnownValue(isKnownValue);
//			this.header2.setKnownValue(isKnownValue);
//		}
	}
	
	/**
	 * This class represents a content of an if-clause in the format of "OV=value"
	 * @author Shou Matsumoto
	 *
	 */
	public class TempTableHeaderOV extends TempTableHeader {

		public TempTableHeaderOV(OrdinaryVariable ov , Entity value) {
			this.setParent(ov);
			this.setValue(value);
		}
		public OrdinaryVariable getOV() {
			return (OrdinaryVariable)this.getParent();
		}
		public void setOV(OrdinaryVariable ov) {
			this.setParent(ov);
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeader#isKnownValue()
		 */
		public boolean isKnownValue() {
			return false;	// by default, indicate that this is never a known value until we resolve the values correctly.
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeader#setKnownValue(boolean)
		 */
		public void setKnownValue(boolean isKnownValue) {
			// do nothing
		}
		
		/**
		 * This will return true, because OVs are like embedded identity node.
		 * @see unbbayes.prs.mebn.compiler.Compiler.IEmbeddedNodeUser#hasEmbeddedNodeDeclaration()
		 */
		public boolean hasEmbeddedNodeDeclaration() {
			return true;
		}
	}
	
	/**
	 * This class represents a content of an if-clause in the format of "Node=state"
	 * @author Shou Matsumoto
	 *
	 */
	public class TempTableHeaderParent extends TempTableHeader {
		
		private List<OrdinaryVariable> expectedArgumentOVs = null;

		/**
		 * Constructor initializing fields.
		 * <br/> <br/>
		 * This object represents a parent and its expected single value
		 * at that table entry/column
		 * @param parent : see {@link #setParent(Node)}
		 * @param value : see {@link #setValue(Entity)}
		 * @param expectedArgumentOVs : see {@link #setExpectedArgumentOVs(List)}
		 * @param evaluationList : see {@link #setEvaluationList(List)}
		 * 
		 */
		public TempTableHeaderParent (ResidentNode parent , Entity value, 
				List<OrdinaryVariable> expectedArgumentOVs,
				List<EntityAndArguments>evaluationList) {
			this.setParent(parent);
			this.setValue(value);
			this.setExpectedArgumentOVs(expectedArgumentOVs);
			this.setEvaluationList(evaluationList);
		}
		
		/**
		 * Constructor with fewer fields are kept here for backward compatibility.
		 */
		public TempTableHeaderParent (ResidentNode parent , Entity value, List<EntityAndArguments>evaluationList) {
			this(parent, value, null, null);
		}
		
		/**
		 * Constructor with fewer fields are kept here for backward compatibility.
		 */
		public TempTableHeaderParent (ResidentNode parent , Entity value) {
			this(parent, value, null);
		}
		
		/**
		 * 
		 * @return which parent this leaf represents
		 */
		public MultiEntityNode getParent() {
			return (MultiEntityNode)super.getParent();
		}

		/**
		 * Returns false by default, because all nodes declared here should be explicit (not embedded) in the MFrag.
		 * @see unbbayes.prs.mebn.compiler.Compiler.IEmbeddedNodeUser#hasEmbeddedNodeDeclaration()
		 */
		public boolean hasEmbeddedNodeDeclaration() {
			return false;
		}
		
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeader#setEvaluationList(java.util.List)
		 */
		public void setEvaluationList(List<EntityAndArguments> evaluationList) {
			// make sure the evaluation list is compatible with the arguments of this node
			super.setEvaluationList(this.removeIncompatibleOVs(evaluationList));
		}
		
		/**
		 * This method will filter out values whose ordinary variables don't match
		 * {@link #getExpectedArgumentOVs()}. If null, then this will be ignored. This is particularly important when the resident node
		 * has 2 input nodes as its parents, but these input nodes are related to the same resident node 
		 * (so the LPD compiler can distinguish them only from its arguments -- namely the OVs).
		 * @param evaluationList : the list to have elements filtered out
		 * @return non-null list generated after filtering out incompatible entries.
		 * @see #setEvaluationList(List)
		 * @see #setExpectedArgumentOVs(List)
		 */
		public List<EntityAndArguments> removeIncompatibleOVs(List<EntityAndArguments> evaluationList) {
			
			// initial assertions
			if (evaluationList == null) {
				return Collections.EMPTY_LIST;
			}
			
			// this is the filter to be applied
			// TODO check if we need to use hash tables for optimization (but we need to be careful about the difference between hash comparison and Object#equals(Object)) in lists
			List<OrdinaryVariable> expectedArgumentOVs = getExpectedArgumentOVs();
			
			if (evaluationList.isEmpty() 	// if there is nothing to filter from
					|| expectedArgumentOVs == null || expectedArgumentOVs.isEmpty()) {	// or if the filter is absent
				// then return the same list instance
				return evaluationList;
			}
			
			// this is the value to be returned
			List<EntityAndArguments> ret = new ArrayList<Compiler.EntityAndArguments>();
			
			// only include the entries in evaluationList which are compatible with the OVs in filter
			for (EntityAndArguments evaluation : evaluationList) {
				if (evaluation == null) {
					continue;	// ignore null values
				}
				
				// this will keep track if evaluation contains any argument incompatible with filter.
				boolean hasInvalidArgument = false;
				
				// iterate on arguments of current value in evaluation list, and check if there is any invalid argument/OV
				for (OVInstance argument : evaluation.arguments) {
					if (!expectedArgumentOVs.contains(argument.getOv())) {
						// if there is at least one invalid argument, then we can immediately stop searching
						hasInvalidArgument = true;
						break;
					}
				}
				
				if (!hasInvalidArgument) {
					ret.add(evaluation);
				}
			}
			
			return ret;
		}

		/**
		 * @return the expectedArgumentOVs : this list will be used by {@link #removeIncompatibleOVs()} 
		 * in order to filter out from {@link #getEvaluationList()} those values whose ordinary variables don't match
		 * this list. If null, then this will be ignored. This is particularly important when the resident node
		 * has 2 input nodes as its parents, but these input nodes are related to the same resident node 
		 * (so the LPD compiler can distinguish them only from its arguments -- namely the OVs).
		 */
		public List<OrdinaryVariable> getExpectedArgumentOVs() {
			return expectedArgumentOVs;
		}

		/**
		 * @param expectedArgumentOVs the expectedArgumentOVs to set : this list will be used by {@link #removeIncompatibleOVs()} 
		 * in order to filter out from {@link #getEvaluationList()} those values whose ordinary variables don't match
		 * this list. If null, then this will be ignored. This is particularly important when the resident node
		 * has 2 input nodes as its parents, but these input nodes are related to the same resident node 
		 * (so the LPD compiler can distinguish them only from its arguments -- namely the OVs).
		 */
		public void setExpectedArgumentOVs(List<OrdinaryVariable> expectedArgumentOVs) {
			this.expectedArgumentOVs = expectedArgumentOVs;
		}

		
		
	}
	
	/**
	 * This class represents some assignment of 
	 * a possible state ({@link TempTableProbabilityCell#getPossibleValue()})
	 * to a probability ({@link TempTableProbabilityCell#getProbability()})
	 * @author Shou Matsumoto
	 */
	public class TempTableProbabilityCell {
		private Entity possibleValue = null;
		private IExpressionValue probability = null;
		
		/**
		 * Represents a simple entry at a temporaly table representation (the
		 * value and its probability pair)
		 * @param possibleValue
		 * @param probability
		 */
		public TempTableProbabilityCell (Entity possibleValue , IExpressionValue probability) {
			this.possibleValue = possibleValue;
			this.probability = probability;
		}
		public Entity getPossibleValue() {
			return possibleValue;
		}
		public void setPossibleValue(Entity possibleValue) {
			this.possibleValue = possibleValue;
		}
		/**
		 * @deprecated use {@link #getProbability()} and then {@link IExpressionValue#getValue()} instead.
		 * */
		public float getProbabilityValue() throws InvalidProbabilityRangeException {
			try {
				return Float.parseFloat(probability.getValue());
			} catch (NumberFormatException e) {}
			return Float.NaN;
		}
		public IExpressionValue getProbability() {
			return probability;
		}
		public void setProbability(IExpressionValue probability) {
			this.probability = probability;
		}		
	}
	
	/**
	 * Interface for classes representing terms in a expression in LPD.
	 * @author Shou Matsumoto
	 */
	public abstract class IExpressionValue {
		/**
		 * @return: a string representation of a term in a expression (e.g. value between [0,1] for probability).
		 * It's a string so that non-numeric values can also be used.
		 */
		public abstract String getValue() throws InvalidProbabilityRangeException;
		
		/**
		 * true if the value is fixed. False if value is
		 * dynamic (values changes depending on SSBN configuration)
		 */
		private boolean isFixedValue = true;

		/**
		 * @return true if the value is fixed. False if value is
		 * dynamic (values changes depending on SSBN configuration)
		 */
		public boolean isFixedValue() {
			return this.isFixedValue;
		}

		/**
		 * @param isFixedValue : true if the value is fixed. False if value is
		 * dynamic (values changes depending on SSBN configuration)
		 */
		public void setFixedValue(boolean isFixedValue) {
			this.isFixedValue = isFixedValue;
		}
		
		/**
		 * Subclasses must overwrite this method for representing non-numeric values in expressions.
		 * @return : true if this value represents a numeric value. False otherwise.
		 */
		public abstract boolean isNumeric();
	}
	
	/**
	 * Class representing a string label.
	 * @author Shou Matsumoto
	 */
	public class SimpleStringValue extends IExpressionValue {
		
		private String value = "";
		
		/**
		 * @param value : to be returned by {@link #getValue()}
		 */
		public SimpleStringValue(String value) {
			super();
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#getValue()
		 */
		public String getValue() throws InvalidProbabilityRangeException {
			return value;
		}
		
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() {
			return false;
		}
		
	}
	
	/**
	 * Class representing a number (probability)
	 * @author Shou Matsumoto
	 */
	public class SimpleProbabilityValue extends IExpressionValue {
		private float value = Float.NaN;
		/**
		 * Represents a simple float value for a probability
		 * @param value
		 */
		SimpleProbabilityValue (float value) {
			this.value = value;
			this.setFixedValue(true);
		}
		public String getValue() throws InvalidProbabilityRangeException {
			return ""+this.value;
		}
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() { return true;}
	}
	
	public class UniformComplementProbabilityValue extends IExpressionValue {

		private TempTableHeaderCell currentHeader;
		private Entity entity;

		public UniformComplementProbabilityValue(TempTableHeaderCell currentHeader, Entity entity) {
			this.currentHeader = currentHeader;
			this.entity = entity;
		}

		public String getValue() throws InvalidProbabilityRangeException {
			if (currentHeader == null || getSSBNNode() == null || getSSBNNode().getProbNode() == null) {
				return ".0";
			}
			float sumOtherStates = 0;
			int declaredStates = 0;
			for (TempTableProbabilityCell cell : currentHeader.getCellList()) {
				if (this.getClass().isAssignableFrom(cell.getProbability().getClass())) {
					continue;	// ignore cells that are also dynamic like this class
				}
				declaredStates++;
				sumOtherStates += cell.getProbabilityValue();
			}
			if (Float.isNaN(sumOtherStates) || declaredStates <= 0 || sumOtherStates >= 1) {
				return "0";
			}
			return ""+((1-sumOtherStates)/(getSSBNNode().getProbNode().getStatesSize() - declaredStates));
		}
		

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() { return true;}
	}
	
	/**
	 * Class of binary (2-argument) operation, like *,/,+,-
	 * @author Shou Matsumoto
	 */
	public abstract class MathOperationProbabilityValue extends IExpressionValue {
		protected IExpressionValue op1 = null;
		protected IExpressionValue op2 = null;
		
		public abstract String getValue() throws InvalidProbabilityRangeException;

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() {
			if (op1 != null && op2 != null) {
				return op1.isNumeric() && op2.isNumeric();
			}
			return true;
		}
	}
	
	/**
	 * Class of "+" operation
	 * @author Shou Matsumoto
	 */
	public class AddOperationProbabilityValue extends MathOperationProbabilityValue {
		AddOperationProbabilityValue(IExpressionValue op1 , IExpressionValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.setFixedValue(op1.isFixedValue()?op2.isFixedValue():false);
		}
		@Override
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				return "" + (Float.parseFloat(this.op1.getValue()) + Float.parseFloat(this.op2.getValue()));
			} catch (NumberFormatException e) {}
			return "";
		}		
	}
	
	/**
	 * Class of "-" (binary) operation
	 * @author Shou Matsumoto
	 */
	public class SubtractOperationProbabilityValue extends MathOperationProbabilityValue {
		SubtractOperationProbabilityValue(IExpressionValue op1 , IExpressionValue op2) {
			this.op1 = op1;
			this.op2 = op2;

			this.setFixedValue(op1.isFixedValue()?op2.isFixedValue():false);
		}
		@Override
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				return "" + (Float.parseFloat(this.op1.getValue()) - Float.parseFloat(this.op2.getValue()));
			} catch (NumberFormatException e) {}
			return "";
		}		
	}
	
	/**
	 * Class of "*" operation
	 * @author Shou Matsumoto
	 */
	public class MultiplyOperationProbabilityValue extends MathOperationProbabilityValue {
		MultiplyOperationProbabilityValue(IExpressionValue op1 , IExpressionValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.setFixedValue(op1.isFixedValue()?op2.isFixedValue():false);
		}
		@Override
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				return "" + (Float.parseFloat(this.op1.getValue()) * Float.parseFloat(this.op2.getValue()));
			} catch (NumberFormatException e) {}
			return "";
		}		
	}
	
	/**
	 * Class of "/" operation
	 * @author Shou Matsumoto
	 */
	public class DivideOperationProbabilityValue extends MathOperationProbabilityValue {
		DivideOperationProbabilityValue(IExpressionValue op1 , IExpressionValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.setFixedValue(op1.isFixedValue()?op2.isFixedValue():false);
		}
		@Override
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				return "" + (Float.parseFloat(this.op1.getValue()) / Float.parseFloat(this.op2.getValue()));
			} catch (NumberFormatException e) {}
			return "";
		}		
	}
	
	/**
	 * Class of "-" (unary) operation
	 * @author Shou Matsumoto
	 */
	public class NegativeOperationProbabilityValue extends MathOperationProbabilityValue {
		NegativeOperationProbabilityValue(IExpressionValue op1) {
			this.op1 = op1;
			this.op2 = op1;
			this.setFixedValue(op1.isFixedValue());
		}
		@Override
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				return "" + ( -Float.parseFloat(this.op1.getValue()) );
			} catch (NumberFormatException e) {}
			return "";
		}		
	}
	
	/**
	 * This class wrapps/adapts a {@link IUserDefinedFunctionBuilder} to {@link IExpressionValue}
	 * @author Shou Matsumoto
	 */
	public class ExternalFunctionProbabilityValue extends IExpressionValue {
		
		private IUserDefinedFunctionBuilder functionBuilder;

		public ExternalFunctionProbabilityValue(IUserDefinedFunctionBuilder functionBuilder) {
			this.functionBuilder = functionBuilder;
		}

		/** Delegates to {@link IUserDefinedFunction#getResult()} */
		public String getValue() throws InvalidProbabilityRangeException {
			IUserDefinedFunctionBuilder builder = getFunctionBuilder();
			IUserDefinedFunction func = builder.buildUserDefinedFunction();
			String result = func.getResult();
			if (result != null) {
				return result;
			}
			return "";
		}

		public IUserDefinedFunctionBuilder getFunctionBuilder() {return functionBuilder;}
		public void setFunctionBuilder(IUserDefinedFunctionBuilder functionBuilder) {this.functionBuilder = functionBuilder;}
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() {
			try {
				float ret = Float.parseFloat(getValue());
				return !Float.isNaN(ret);
			} catch (InvalidProbabilityRangeException e) {
			} catch (NumberFormatException e) {}
			return false;
		}
	}
	
	public class CardinalityProbabilityValue extends IExpressionValue {
		//private float value = Float.NaN;
		private String varSetName = null;		
		//private SSBNNode thisNode = null;
		
		private TempTableHeaderCell currentHeader = null;
		
		/**
		 * Represents a probability value from cardinality function
		 * It calculates the value using thisNode's parents set
		 * @param currentHeader: its currentParentSetMap should contain all considered parents at
		 * that point, mapped by resident's name and each elements should be lists of nodes
		 * containing SAME strong OV instances (e.g. ST0)
		 */
		CardinalityProbabilityValue (TempTableHeaderCell currentHeader, String varsetname) {
			this.currentHeader = currentHeader;
			this.varSetName= varsetname;
			this.setFixedValue(false);
		}

		public String getValue() throws InvalidProbabilityRangeException {
			
			if (this.currentHeader == null) {
				return "";
			}
			if (getSSBNNode() == null) {
				return "";
			}
			
			// if argument was not provided (i.e. it was "CARDINALITY()"), simply return total number of parents
			if (this.varSetName == null || this.varSetName.trim().isEmpty()) {
				return ""+getSSBNNode().getParents().size();
			}
			
			// look for the upper if clauses which has matching varsetname
			TempTableHeaderCell matchingHeader = this.currentHeader;
			while (!matchingHeader.getVarsetname().equalsIgnoreCase(this.varSetName)) {
				try{
					matchingHeader = (TempTableHeaderCell)matchingHeader.getUpperClause();
				} catch (ClassCastException e) {
					// we found a container other than TempTableHeaderCell
					// probably, it is a TempTable
					// so, there was no perfect match for varsetname...
					return "0";
				}
			}
			
			// if we reach this code, we found a perfect match for varsetname
			return ""+matchingHeader.getValidParentSetCount();
		}

		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() { return true;}
	}
	
	
	public class ComparisionProbabilityValue extends IExpressionValue {
		private IExpressionValue arg0 = null;
		private IExpressionValue arg1 = null;
		private boolean isMax = false;
		/**
		 * Represents a comparision function between two values (MAX or MIN)
		 * @param arg0
		 * @param arg1
		 * @param isMax true if it represents a MAX function. If false, it represents a MIN
		 * function.
		 */
		ComparisionProbabilityValue (IExpressionValue arg0, IExpressionValue arg1, boolean isMax) {
			this.arg0 = arg0;
			this.arg1 = arg1;
			this.isMax = isMax;
			this.setFixedValue(arg0.isFixedValue()?arg1.isFixedValue():false);
		}
		public String getValue() throws InvalidProbabilityRangeException {
			Float prob0 = Float.NaN;
			try {
				prob0 = Float.parseFloat(this.arg0.getValue());
			} catch (NumberFormatException e) {
				prob0 = Float.NaN;
			}
			if (Float.isNaN(prob0)) {
				return "";
			}
			
			Float prob1 = Float.NaN;
			try {
				prob1 = Float.parseFloat(this.arg1.getValue());
			} catch (NumberFormatException e) {
				prob1 = Float.NaN;
			}
			if (Float.isNaN(prob1)) {
				return "";
			}
			
			/*
			 * the code below has the same meaning of:
			 * if (isMax) {
			 * 		if (prob0 > prob1) {
			 * 			return prob0;
			 * 		} else {
			 * 			return prob1;
			 * 		}
			 * } else {
			 * 		if (prob0 < prob1) {
			 * 			return prob0;
			 * 		} else {
			 * 			return prob1;
			 * 		}
			 * }
			 * 
			 */
			if (this.isMax == (prob0 > prob1)) {				
				return ""+prob0;
			} else {
				return ""+prob1;
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.IExpressionValue#isNumeric()
		 */
		public boolean isNumeric() {
			if (arg0 != null && arg1 != null) {
				return arg0.isNumeric() && arg1.isNumeric();
			}
			return true;
		}

	}

	/**
	 * An alternative (compact) way to represent a particular state of
	 * a SSBNNode, by storing its current value (entity) and its current 
	 * arguments (arguments).
	 * For instance, if SSBNNode = DangerToSelf((st,ST0),(t,T0)) = [Phaser2Range |
	 * PulseCannonRange | TorpedoRange], then a possible value of EntityAndArguments 
	 * would be (Phaser2Range;[(st,ST0),(t,T0)]), which means that DangerToSelf
	 * is at value Phaser2Range when its arguments st=T0 and t=T0.
	 */
	public class EntityAndArguments {
		private Entity entity = null;
		List<OVInstance> arguments = null;
		/**
		 * Creates an alternative (compact) way to represent a particular state of
		 * a SSBNNode, by storing its current value (entity) and its current 
		 * arguments (arguments).
		 * For instance, if SSBNNode = DangerToSelf((st,ST0),(t,T0)) = [Phaser2Range |
		 * PulseCannonRange | TorpedoRange], then a possible value of EntityAndArguments 
		 * would be (Phaser2Range;[(st,ST0),(t,T0)]), which means that DangerToSelf
		 * is at value Phaser2Range when its arguments st=T0 and t=T0.
		 * @param entity
		 * @param arguments
		 */
		public EntityAndArguments (Entity entity, List<OVInstance> arguments) {
			this.entity = entity;
			this.arguments = arguments;
		}
		/**
		 * @return the entity
		 */
		public Entity getEntity() {
			return entity;
		}
		/**
		 * @param entity the entity to set
		 */
		public void setEntity(Entity entity) {
			this.entity = entity;
		}
		/**
		 * @return the arguments
		 */
		public List<OVInstance> getArguments() {
			return arguments;
		}
		/**
		 * @param arguments the arguments to set
		 */
		public void setArguments(List<OVInstance> arguments) {
			this.arguments = arguments;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			
			return this.getEntity() + " = " +  this.arguments;
		}
	}



	/**
	 * If this class' constructor should return a singleton or not
	 * @return the singleton
	 */
	public static Boolean getSingleton() {
		return singleton;
	}


	/**
	 * If this class' constructor should return a singleton or not
	 * @param singleton the singleton to set
	 */
	public static void setSingleton(Boolean singleton) {
		Compiler.singleton = singleton;
	}


	/**
	 * This is a datastructure to store cache of CPTs used in {@link #generateLPD(SSBNNode)}
	 * @return the nameToParentProbValuesCache
	 */
	public Map<String, Map<List<INode>, IProbabilityFunction>> getNameToParentProbValuesCache() {
		return nameToParentProbValuesCache;
	}


	/**
	 * This is a datastructure to store cache of CPTs used in {@link #generateLPD(SSBNNode)}
	 * @param nameToParentProbValuesCache the nameToParentProbValuesCache to set
	 */
	public void setNameToParentProbValuesCache(
			Map<String, Map<List<INode>, IProbabilityFunction>> nameToParentProbValuesCache) {
		this.nameToParentProbValuesCache = nameToParentProbValuesCache;
	}


	/**
	 * This method clears all pre-cached compiled values of this compiler. 
	 * Use this method to guarantee that {@link #generateLPD(SSBNNode)} re-compiles
	 * LPD script (e.g. when the LPD script has been changed).
	 * It just removes the content of {@link #getNameToParentProbValuesCache()}.
	 * @see unbbayes.prs.mebn.compiler.ICompiler#clearCache()
	 */
	public void clearCache() {
		if (this.getNameToParentProbValuesCache() != null) {
			this.getNameToParentProbValuesCache().clear();
//			System.gc();
		}
	}


	/**
	 * Comparator used for {@link TreeMap}, which are the values in {@link #getNameToParentProbValuesCache()}
	 * @return the cacheParentsComparator
	 */
	public Comparator<List<INode>> getCacheParentsComparator() {
		return cacheParentsComparator;
	}


	/**
	 * *
	 * Comparator used for {@link TreeMap}, which are the values in {@link #getNameToParentProbValuesCache()}
	 * @param cacheParentsComparator the cacheParentsComparator to set
	 */
	public void setCacheParentsComparator(
			Comparator<List<INode>> cacheParentsComparator) {
		this.cacheParentsComparator = cacheParentsComparator;
	}


	/**
	 * @return : true if varSetName must use exact match for strong OVs. 
	 * False if all parents containing at least one of the OVs will be considered.
	 * @see TempTableHeaderCell#cleanUpByVarSetName(SSBNNode)
	 * @see TempTableHeaderCell#isParentSetName(String)
	 * @see #varsetname()
	 */
	public boolean isExactMatchStrongOV() {
		return isExactMatchStrongOV;
	}


	/**
	 * @param isExactMatchStrongOV : true if varSetName must use exact match for strong OVs. 
	 * False if all parents containing at least one of the OVs will be considered.
	 * @see TempTableHeaderCell#cleanUpByVarSetName(SSBNNode)
	 * @see TempTableHeaderCell#isParentSetName(String)
	 * @see #varsetname()
	 */
	public void setExactMatchStrongOV(boolean isExactMatchStrongOV) {
		this.isExactMatchStrongOV = isExactMatchStrongOV;
	}
	

	/**
	 * This is just a wrapper method.
	 * @return this.getNode().getMFrag().getMultiEntityBayesianNetwork()
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		try {
			return this.getNode().getMFrag().getMultiEntityBayesianNetwork();
		} catch (NullPointerException npe) {
			try {
				Debug.println(this.getClass(), this.getNode() + " has no link to MEBN.", npe);
			} catch (Throwable t) {
				npe.printStackTrace();
				t.printStackTrace();
			}
		}
		return null;
	}
	

	/**
	 * This method is can be used with {@link #restoreMemento()}
	 * in order to undo changes.
	 * If you implement this method, you should also implement {@link #restoreMemento(Object)}
	 * in order to treat the returned object consistently.
	 * E.g. <br/>
	 *   memento = getMemento();<br/>
	 *   // do changes<br/>
	 *   restoreMemento(memento);// undo changes<br/>
	 * @return the current state of the compiler.
	 */
	public Object getMemento() {
		Map<String, Object> memento = new HashMap<String, Object>();
		memento.put("index", this.index);
		memento.put("look", this.look);
		memento.put("value", new String(this.value));
		memento.put("noCaseChangeValue", new String(this.noCaseChangeValue));
		memento.put("token", this.token);
		return memento;
	}
	

	/**
	 * This method can be used with {@link #getMemento()} in order to undo changes.
	 * If you implement this method, you should also implement {@link #getMemento()}
	 * in order to treat the parameter object consistently.
	 * E.g. <br/>
	 *   memento = getMemento();<br/>
	 *   // do changes<br/>
	 *   restoreMemento(memento);// undo changes<br/>
	 * @param memento
	 */
	public void restoreMemento(Object memento) {
		Map<String, Object> temp = (Map<String, Object>)memento;
		this.index = (Integer)(temp.get("index"));
		this.look = (Character) temp.get("look");
		this.value = (String) temp.get("value");
		this.noCaseChangeValue = (String) temp.get("noCaseChangeValue");
		this.token = (Character) temp.get("token");
	}


	/**
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		Compiler.resource = resource;
	}


	/**
	 * @return the look
	 */
	protected char getLook() {
		return this.look;
	}


	/**
	 * @param look the look to set
	 */
	protected void setLook(char look) {
		this.look = look;
	}


	/**
	 * @return the text
	 */
	protected char[] getText() {
		return this.text;
	}


	/**
	 * @param text the text to set
	 */
	protected void setText(char[] text) {
		this.text = text;
	}


	/**
	 * @return the kwlist
	 */
	protected String[] getKwlist() {
		return this.kwlist;
	}


	/**
	 * @param kwlist the kwlist to set
	 */
	protected void setKwlist(String[] kwlist) {
		this.kwlist = kwlist;
	}


	/**
	 * @return the kwcode
	 */
	protected char[] getKwcode() {
		return this.kwcode;
	}


	/**
	 * @param kwcode the kwcode to set
	 */
	protected void setKwcode(char[] kwcode) {
		this.kwcode = kwcode;
	}


	/**
	 * @return the token
	 */
	protected char getToken() {
		return this.token;
	}


	/**
	 * @param token the token to set
	 */
	protected void setToken(char token) {
		this.token = token;
	}


	/**
	 * @return the value
	 */
	protected String getValue() {
		return this.value;
	}


	/**
	 * @param value the value to set
	 */
	protected void setValue(String value) {
		this.value = value;
	}


	/**
	 * @return the noCaseChangeValue
	 */
	protected String getNoCaseChangeValue() {
		return this.noCaseChangeValue;
	}


	/**
	 * @param noCaseChangeValue the noCaseChangeValue to set
	 */
	protected void setNoCaseChangeValue(String noCaseChangeValue) {
		this.noCaseChangeValue = noCaseChangeValue;
	}


	/**
	 * @return the ssbnnode
	 */
	protected SSBNNode getSsbnnode() {
		return this.ssbnnode;
	}


	/**
	 * @param ssbnnode the ssbnnode to set
	 */
	protected void setSsbnnode(SSBNNode ssbnnode) {
		this.ssbnnode = ssbnnode;
	}


	/**
	 * @return the tempTable
	 */
	protected TempTable getTempTable() {
		return this.tempTable;
	}


	/**
	 * @param tempTable the tempTable to set
	 */
	protected void setTempTable(TempTable tempTable) {
		this.tempTable = tempTable;
	}


	/**
	 * @return the originalTextLength
	 */
	protected int getOriginalTextLength() {
		return this.originalTextLength;
	}


	/**
	 * @param originalTextLength the originalTextLength to set
	 */
	protected void setOriginalTextLength(int originalTextLength) {
		this.originalTextLength = originalTextLength;
	}


	/**
	 * @param index the index to set
	 */
	protected void setIndex(int index) {
		this.index = index;
	}


	/**
	 * @return if true, then this compiler is assuming declaration of normalized values (i.e. probabilities).
	 * If false, then this compiler is assuming declaration of non-normalized values (e.g. utility)
	 */
	public boolean isToNormalize() {
		return isToNormalize;
	}


	/**
	 * @param isToNormalize : if true, then this compiler is assuming declaration of normalized values (i.e. probabilities).
	 * If false, then this compiler is assuming declaration of non-normalized values (e.g. utility)
	 */
	public void setToNormalize(boolean isToNormalize) {
		this.isToNormalize = isToNormalize;
	}


	/**
	 * @return the tableNormalizer : this is a {@link ITableFunction} responsible for normalizing a CPT at the end of {@link #getCPT()}.
	 * By default this is {@link #DEFAULT_NORMALIZE_TABLE_FUNCTION}
	 */
	public ITableFunction getTableNormalizer() {
		if (tableNormalizer == null) {
			tableNormalizer = DEFAULT_NORMALIZE_TABLE_FUNCTION;
		}
		return tableNormalizer;
	}


	/**
	 * @param tableNormalizer : this is a {@link ITableFunction} responsible for normalizing a CPT at the end of {@link #getCPT()}.
	 * By default this is {@link #DEFAULT_NORMALIZE_TABLE_FUNCTION}
	 */
	public void setTableNormalizer(ITableFunction tableNormalizer) {
		this.tableNormalizer = tableNormalizer;
	}


	/**
	 * @return the userFunctionPluginManager : this is used for loading user-defined functions.
	 * @see #function()
	 * @see #init(String, boolean)
	 */
	public UserDefinedFunctionPluginManager getUserFunctionPluginManager() {
		return userFunctionPluginManager;
	}


	/**
	 * @param userFunctionPluginManager : this is used for loading user-defined functions.
	 * @see #function()
	 * @see #init(String, boolean)
	 */
	public void setUserFunctionPluginManager(UserDefinedFunctionPluginManager userFunctionPluginManager) {
		this.userFunctionPluginManager = userFunctionPluginManager;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#getKeyWords()
	 */
	public Collection<String> getKeyWords() {
		Collection<String> ret = new HashSet<String>();
		
		// basic keywords
		ret.add("any");
		ret.add("all");
		ret.add("have");
		ret.add("if");
		ret.add("else");
		ret.add("MIN");
		ret.add("MAX");
		ret.add("CARDINALITY");
		ret.add("=");
		ret.add("&");
		ret.add("|");
		ret.add("~");
		ret.add(",");
		ret.add(";");
		
		// possible states of this node
		for (Entity possibleValue : getNode().getPossibleValueList()) {
			ret.add(possibleValue.getName());
		}
		// arguments (limited to ordinary variables) of this node
		for (OrdinaryVariable ov : getNode().getOrdinaryVariablesInArgument()) {
			ret.add(ov.getName());
		}
		

		
		// handle  parents
		for (ResidentNode parent : getNode().getResidentNodeFatherList()) {
			ret.add(parent.getName());
			// add possible states
			for (Entity possibleValue : parent.getPossibleValueList()) {
				ret.add(possibleValue.getName());
			}
			// add argument OVs
			for (OrdinaryVariable ov : parent.getOrdinaryVariablesInArgument()) {
				ret.add(ov.getName());
			}
		}
		// handle input parents
		for (InputNode parent : getNode().getParentInputNodesList()) {
			ret.add(parent.getResidentNodePointer().getResidentNode().getName());
			// add possible states
			for (Entity possibleValue : parent.getResidentNodePointer().getResidentNode().getPossibleValueList()) {
				ret.add(possibleValue.getName());
			}
			// add argument OVs
			for (OrdinaryVariable ov : parent.getOrdinaryVariablesInArgument()) {
				ret.add(ov.getName());
			}
		}
		

		// external functions
		for (IUserDefinedFunctionBuilder functionBuilder : getUserFunctionPluginManager().getFunctionBuilders()) {
			ret.add(functionBuilder.getFunctionName());
		}

		// user-defined variables (if any)
		try {
			this.parse(getNode().getTableFunction());
			TempTable tempTable = getTempTable();
			if (tempTable != null) {
				for (String varName : tempTable.getUserDefinedVariablesRecursively(false).keySet()) {
					ret.add(varName);
				}
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#getShorthandKeywords()
	 */
	public Collection<Entry<String, String>> getShorthandKeywords() {
		
		Collection<Entry<String, String>> ret = new ArrayList<Map.Entry<String,String>>();
		
		// keep track of sample nodes (1st and 2nd parents to be used as placeholders)
		String nodeName = "Node";
		String stateName = "state";
		String nodeName2 = "Node2";
		String stateName2 = "state2";
		String varSetName = "ov1,ov2";
		
		// extract nodeName, stateName, and ovName from 1st and 2nd parents
		try {
			int found = 0;
			// look for resident nodes first
			for (ResidentNode parent : getNode().getResidentNodeFatherList()) {
				if (found <= 0) {
					nodeName = parent.getName();
					stateName = parent.getPossibleValueList().get(0).getName();
					varSetName = null;
					for (OrdinaryVariable ov : parent.getOrdinaryVariablesInArgument()) {
						if (varSetName == null) {
							varSetName = ov.getName();
						} else {
							varSetName += "," + ov.getName();
						}
					}
				} else {
					nodeName2 = parent.getName();
					stateName2 = parent.getPossibleValueList().get(0).getName();
				}
				found++;
				if (found >= 2) {
					break;
				}
			}
			if (found < 2) {
				// look for input nodes
				for (InputNode parent : getNode().getParentInputNodesList()) {
					if (found <= 0) {
						nodeName = parent.getResidentNodePointer().getResidentNode().getName();
						stateName = parent.getResidentNodePointer().getResidentNode().getPossibleValueList().get(0).getName();
						varSetName = null;
						for (OrdinaryVariable ov : parent.getOrdinaryVariablesInArgument()) {
							if (varSetName == null) {
								varSetName = ov.getName();
							} else {
								varSetName += "," + ov.getName();
							}
						}
					} else {
						nodeName2 = parent.getResidentNodePointer().getResidentNode().getName();
						stateName2 = parent.getResidentNodePointer().getResidentNode().getPossibleValueList().get(0).getName();
					}
					found++;
					if (found >= 2) {
						break;
					}
				}
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		// if-then-else
		
		ret.add(Collections.singletonMap(
				"any",
				"if any "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"all",
				"if all "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"if",
				"if any "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"if",
	    		"if all "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
	    		"else",
	    		"else []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
	    		"else",
	    		"else if any "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
	    		"else",
	    		"else if all "+varSetName+" have ("+nodeName+"="+stateName+") []").entrySet().iterator().next());
//		ret.add(Collections.singletonMap(
//				"default",
//				"[]").entrySet().iterator().next());
		
		// built in functions
		ret.add(Collections.singletonMap(
	    		"min",
	    		"MIN(exp1 ; exp2)").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
	    		"max",
	    		"MAX(exp1 ; exp2)").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
	    		"cardinality",
	    		"CARDINALITY("+varSetName+")").entrySet().iterator().next());
		ret.add(Collections.singletonMap( 
	    		"cardinality",
	    		"CARDINALITY()").entrySet().iterator().next());
		
		// external functions
		for (IUserDefinedFunctionBuilder functionBuilder : getUserFunctionPluginManager().getFunctionBuilders()) {
			ret.add(Collections.singletonMap( 
					functionBuilder.getFunctionName(),
					functionBuilder.getFunctionName() + "(arg1,arg2,...)").entrySet().iterator().next());
		}
		
		// boolean operators
		ret.add(Collections.singletonMap(
				"=",
				"name = value").entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"~",
				" ~ " + nodeName).entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"&",
				nodeName + " = " + stateName + " & " + nodeName2 + " = " + stateName2).entrySet().iterator().next());
		ret.add(Collections.singletonMap(
				"|",
				nodeName + " = " + stateName + " | " + nodeName2 + " = " + stateName2).entrySet().iterator().next());
		
		
		
		
		
		// ParentName = parentState
		// Parent = ov
		//Parent1 = Parent2
		for (ResidentNode parent : getNode().getResidentNodeFatherList()) {
			// get the 1st state
			String state = "state";	// if no state is found, simply use this default value
			List<Entity> possibleValues = parent.getPossibleValueList();
			if (possibleValues!= null && !possibleValues.isEmpty()) {
				state = possibleValues.get(0).getName();
			}
			ret.add(Collections.singletonMap(
					parent.getName(),
					parent.getName() + " = " + state).entrySet().iterator().next());
			ret.add(Collections.singletonMap(
					parent.getName(),
					parent.getName() + " = OV" ).entrySet().iterator().next());
			
			try {
				if (!parent.getName().equals(nodeName)) {
					ret.add(Collections.singletonMap(
							parent.getName(),
							parent.getName() + " = " + nodeName).entrySet().iterator().next());
				} else {
					ret.add(Collections.singletonMap(
							parent.getName(),
							parent.getName() + " = " + nodeName2).entrySet().iterator().next());
				}
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
			}
		}
		for (InputNode parent : getNode().getParentInputNodesList()) {
			// get the 1st state
			String state = "state";	// if no state is found, simply use this default value
			List<Entity> possibleValues = parent.getResidentNodePointer().getResidentNode().getPossibleValueList();
			if (possibleValues!= null && !possibleValues.isEmpty()) {
				state = possibleValues.get(0).getName();
			}
			ret.add(Collections.singletonMap(
					parent.getResidentNodePointer().getResidentNode().getName(),
					parent.getResidentNodePointer().getResidentNode().getName() + " = " + state).entrySet().iterator().next());
			ret.add(Collections.singletonMap(
					parent.getResidentNodePointer().getResidentNode().getName(),
					parent.getResidentNodePointer().getResidentNode().getName() + " = OV" ).entrySet().iterator().next());
			
			try {
				if (!parent.getResidentNodePointer().getResidentNode().getName().equals(nodeName)) {
					ret.add(Collections.singletonMap(
							parent.getResidentNodePointer().getResidentNode().getName(),
							parent.getResidentNodePointer().getResidentNode().getName() + " = " + nodeName).entrySet().iterator().next());
				} else {
					ret.add(Collections.singletonMap(
							parent.getResidentNodePointer().getResidentNode().getName(),
							parent.getResidentNodePointer().getResidentNode().getName() + " = " + nodeName2).entrySet().iterator().next());
				}
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
			}
		}
		
		
		// state = 100%
		try {
			for (Entity possibleValue : getNode().getPossibleValueList()) {
				ret.add(Collections.singletonMap(
						possibleValue.getName(),
						possibleValue.getName() + " = 1").entrySet().iterator().next());
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		
		// create a template with placeholders to be substituted later
		String template = null;
		int numStatesWithoutAbsurd = getNode().getPossibleValueList().size();
		try {
			String absurd = "absurd";
			try {
				// extract the absurd name from mebn
				absurd = getMEBN().getBooleanStatesEntityContainer().getAbsurdStateEntity().getName();
			} catch (Exception e) {
				Debug.println(getClass(), "Failed to extract absurd state from boolean states entity container of MEBN. Extracting from a new instance...", e);
				try {
					// extract the absurd name from new boolean states entity container
					absurd = new BooleanStatesEntityContainer().getAbsurdStateEntity().getName();
				} catch (Exception e2) {
					Debug.println(getClass(), "Failed to extract absurd state from new boolean states entity container. Using default = absurd.", e2);
					absurd = "absurd";
				}
			}
			
			// build distribution
			for (Entity possibleValue : getNode().getPossibleValueList()) {
				if (possibleValue.getName().equalsIgnoreCase(absurd)) {
					numStatesWithoutAbsurd--;
					continue;	// ignore absurd
				}
				if (template == null) {
					template = possibleValue + " = $VALUE_STATE0";
				} else {
					template += " , " + possibleValue + " = $VALUE_DEFAULT";
				}
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		// uniform (excluding absurd)
		try {
			ret.add(Collections.singletonMap( 
					"uniform",
					// just call replaceAll 2 times (to substitute $VALUE_STATE0 and then $VALUE_DEFAULT)
					template.replaceAll(
							"\\$VALUE_STATE0", "" + 1f/numStatesWithoutAbsurd
						).replaceAll(
							"\\$VALUE_DEFAULT", "" + 1f/numStatesWithoutAbsurd)
					).entrySet().iterator().next());	// extract entry
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		// auto-fill by number of 1st state
		try {
			for (int i = 0; i < 100; i += 10) {
				ret.add(Collections.singletonMap( 
						"" + i,
						// just call replaceAll 2 times (to substitute $VALUE_STATE0 and then $VALUE_DEFAULT)
						template.replaceAll(
								"\\$VALUE_STATE0", "" + i/100f
								).replaceAll(
										"\\$VALUE_DEFAULT", "" + (1f-(i/100f))/(numStatesWithoutAbsurd-1f))
						).entrySet().iterator().next());	// extract entry
				
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		try {
			// suggestions to show by default (autocomplete for empty string)
			ret.add(Collections.singletonMap(
					"",
					"if any "+varSetName+" have ("+nodeName+"="+stateName+") ["
							+template.replaceAll(
									"\\$VALUE_STATE0", "" + 1f/numStatesWithoutAbsurd
									).replaceAll(
											"\\$VALUE_DEFAULT", "" + 1f/numStatesWithoutAbsurd)
											+"]").entrySet().iterator().next());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		try {
			ret.add(Collections.singletonMap(
					"",
					"[" + template.replaceAll(
							"\\$VALUE_STATE0", "" + 1f/numStatesWithoutAbsurd
							).replaceAll(
									"\\$VALUE_DEFAULT", "" + 1f/numStatesWithoutAbsurd)
									+"]").entrySet().iterator().next());
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		
		return ret;
	}



}
