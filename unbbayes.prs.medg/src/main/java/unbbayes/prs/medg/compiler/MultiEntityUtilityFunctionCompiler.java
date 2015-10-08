/**
 * 
 */
package unbbayes.prs.medg.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.InstanceException;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.medg.MultiEntityDecisionNode;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;

/**
 * This is the compiler for continuous node LPD.
 * The new LPD compiler translates the following grammar:<br/>
 * ===============================================================<br/>
 *table := statement | if_statement 		<br/>
 *if_statement  ::= 						<br/>
 * 	"if" constraintSet "have" "(" b_expression ")" statement "else" else_statement <br/>
 *allop ::= "some" | "all"<br/>
 *varsetname ::= ident ["," ident]* <br/>
 *	| ident ["." ident]*<br/>
 *entityname ::= ident  					// added 11/9/2011<br/>
 *nodename ::= ident  						// added 11/9/2011<br/>
 *constraint  :: = allop varsetname<br/>		
 *constraintSet ::= constraint (constraint)*<br/>     
 *b_expression ::= b_term [ "|" b_term ]*<br/>
 *b_term ::= not_factor [ "&" not_factor ]*<br/>
 *not_factor ::= [ "~" ] b_factor<br/>
 *b_factor ::= ident "=" ident | "(" b_expression ")"<br/>
 *else_statement ::= statement | if_statement<br/>
 *statement ::= "[" assignment_or_if "]"<br/> 
 *assignment_or_if ::= assignment | if_statement<br/>
 *assignment ::= ident "=" expression [ "," assignment ]* | expression <br/>
 *expression ::= term [ addop term ]*<br/> 
 *term ::= signed_factor [ mulop signed_factor ]*<br/>
 *signed_factor ::= [ addop ] factor<br/>
 *factor ::= number | function | "(" expression ")"<br/>
 *function ::= possibleVal<br/> 
 * 	| "CARDINALITY" "(" varsetname ")"<br/>
 *	| "NodeCARD" "(" nodename ")"				// added 11/9/2011<br/>
 *  | "EntityCARD" "(" entityname ")"			// added 11/9/2011<br/>
 *	| "MIN" "(" expression "," expression ")"<br/>
 *	| "MAX" "(" expression "," expression ")"<br/>
 *	| "Mean" "(" nodeName ")"<br/>
 *  | "Sum" "(" nodeName ")"<br/>
 *  | "Multiply" "(" nodeName ")" <br/>
 *  | "NormalDist" "(" expression "," expression ")" 	// added 11/9/2011<br/>
 *  | "Exp" "(" expression ";" expression ")"	// modified to support 2 arguments 12/05/05
 *  | "Root" "(" expression ";" expression ")"	// modified to support 2 arguments 12/05/05
 *  | "Log" "(" expression "," expression ")" 		// added 11/9/2011<br/>
 *  | "Power" "(" expression "," expression ")"  	// added 11/9/2011<br/>
 *<br/>
 *possibleVal ::= ident<br/>
 *addop ::= "+" | "-"<br/>
 *mulop ::= "*" | "/"<br/>
 *ident ::= letter [ letter | digit ]*<br/>
 *number ::= [digit]+<br/>
 *================================================================<br/>
 * Note: the euler number "e" can be used in ident.
 *
 * @author Shou Matsumoto
 *
 */
public class MultiEntityUtilityFunctionCompiler extends Compiler {




	private static ResourceBundle resource;
	
	/* A previously read character (lookahead) */
	private char look;

	/* Current text cursor position (where inside "text" we're scanning now) */
	private int index = 0;

	private char[] text = null;

	/* keywords. */
	private String kwlist[] = { "IF", "ELSE", "ALL", "SOME", "HAVE", "ANY" };

	/*
	 * Special codes for keywords.
	 * Obviously, it should represent kwlist.
	 */
	private char kwcode[] = { 'i', 'l', 'a', 'y', 'h', 'y' };

	/* coded token */
	private char token;

	/*
	 * uncoded token's value
	 */
	private String value = "";
	private String noCaseChangeValue = "";
	
	
	// Informations used by this class to check pre-SSBN consistency
//	private MultiEntityBayesianNetwork mebn = null;
//	private ResidentNode node = null;
	

	// Variables used for ProbabilisticTable generation
//	private PotentialTable cpt = null;
//	private SSBNNode ssbnnode = null;
	
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

	private boolean isToFillCPSNow = true;

	private boolean isToAddStatesToContinuousResidentNodes = false;
	
	/**
	 * The default constructor is protected.
	 * Use {@link #getInstance(ResidentNode)} instead
	 */
	protected MultiEntityUtilityFunctionCompiler() {
		try {
			this.setResource(unbbayes.util.ResourceController.newInstance().getBundle(
						unbbayes.prs.mebn.compiler.resources.Resources.class.getName(),
						Locale.getDefault(),
						MultiEntityUtilityFunctionCompiler.class.getClassLoader()));
		} catch (Throwable t) {
			Debug.println(getClass(), "Could not initialize resource " + unbbayes.prs.mebn.compiler.resources.Resources.class.getName(), t);
		}
	}
	
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
	public static ICompiler newInstance(ResidentNode node, SSBNNode ssbnnode) {
		MultiEntityUtilityFunctionCompiler comp = new MultiEntityUtilityFunctionCompiler();
		comp.setNode(node);
		comp.setSSBNNode(ssbnnode);
		if (comp.getSSBNNode() != null) {
			if (comp.getSSBNNode().getProbNode() != null) {
				comp.setPotentialTable(comp.getSSBNNode().getProbNode().getProbabilityFunction());
			}			
		}
		try {
			node.setCompiler(comp);
		} catch (Exception e) {}
		// the following is redundant
//		try {
//			ssbnnode.setCompiler(comp);
//		} catch (Exception e) {}
		return comp;
	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * @param node: a resident node containing the table to parse
	 * @return a instance of the compiler.
	 * @see {@link Compiler#getInstance(ResidentNode, SSBNNode)}
	 */
	public static ICompiler newInstance(ResidentNode node) {
	// since we are not using other specific pseudocode Compilers, and we do not use Builders/Factories,
	// it is not necessary to have a constructor method...
		return MultiEntityUtilityFunctionCompiler.newInstance(node, null);
	}
	
	/**
	 * @deprecated
	 * @see #newInstance(ResidentNode, SSBNNode)
	 */
	public static Compiler getInstance(ResidentNode node, SSBNNode ssbnnode){
		return (MultiEntityUtilityFunctionCompiler)MultiEntityUtilityFunctionCompiler.newInstance(node, ssbnnode);
	}
	/**
	 * @deprecated
	 * @see #newInstance(ResidentNode)
	 */
	public static Compiler getInstance(ResidentNode node) {
		return (MultiEntityUtilityFunctionCompiler)MultiEntityUtilityFunctionCompiler.newInstance(node);
	}
	
	/**
	 * Compiler's initialization.
	 * It overwrites {@link Compiler#init(String)}
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
		
		tempTable = new TempTable();
	}
	
	/**
	 * use this method to initialize this parser on SSBN generation step.
	 * All information will be extracted from the ssbnnode.
	 * @param ssbnnode
	 */
	public void init(SSBNNode ssbnnode) {
		this.setSSBNNode(ssbnnode);
		String pseudocode = this.getNode().getTableFunction();
		
		if (this.getSSBNNode().getProbNode() != null) {
			this.setPotentialTable(this.getSSBNNode().getProbNode().getProbabilityFunction());			
		}
		// do not clear cache
		this.init(pseudocode, false);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException {
		//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>(); //Initialize lists
		if (this.text == null) {
			// Debug.println("Pseudocode = null");
			throw new MEBNException("Script == null");
		}
		// Debug.println("PARSED: ");
		this.skipWhite();
		this.table();
	}

	/**
	 *  table := statement | if_statement
	 */
	protected void table() throws NoDefaultDistributionDeclaredException,
	  							InvalidConditionantException,
	  							SomeStateUndeclaredException,
	  							InvalidProbabilityRangeException,
	  							TableFunctionMalformedException{
		
		if (this.look == '[') {
			// Debug.println("STARTING DEFAULT STATEMENT");	
			
			// Prepare temporary table's header to declare a default (no-if-clause) statement
			this.currentHeader = new TempTableHeaderCell(null, null, true, this.getSSBNNode());
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
		
		
		
		// SCAN FOR ALL/SOME
		this.constraintSet(upperIf);
		

		// SCAN FOR HAVE
		// Debug.println("SCAN FOR HAVE");
//		scan();
		matchString("HAVE");

		// ( EXPECTED
		match('(');
		// if we catch sintax error here, it may be conditionant error
		
		// Now, parsing a boolean expression - tree format (we'll store it inside this variable)
		ICompilerBooleanValue expressionTree = null;
		
		try {
			expressionTree = bExpression();
		} catch (TableFunctionMalformedException e) {
			throw new InvalidConditionantException(e);
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
			throw new InvalidConditionantException();
		}
		
		// Debug.println("STARTING STATEMENTS");
		
		// if we catch a sintax error here, it may be a value error
		try {
			// stores this.currentHeader in order to become an upper clause of any further nested if/else clause
			INestedIfElseClauseContainer currentIfContainer = this.currentHeader;
			// if there is a nested if, this if should be the upper clause (set currentHeader as upper clause).
			statement(currentIfContainer);
		} catch (TableFunctionMalformedException e) {
			// Debug.println("->" + getNode());e.printStackTrace();
			throw new InvalidProbabilityRangeException("["+this.getNode().getName()+"]",e);
		}
		
		
		
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
			throw new NoDefaultDistributionDeclaredException();
		}
		
		if (token == 'l') {
			// The else statement should be a child statement of the upper container,
			// that means, it is on the same level of currently evaluated IF clause
			else_statement(upperIf);
		} else {
			// The statement found was not an else statement
			throw new NoDefaultDistributionDeclaredException();
		}
		
		// we may have another if/else clause after this...
		
	}
	
	/**
	 * constraintSet ::= constraint (constraint)*     
	 * @param upperIf
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected Map<String, Boolean> constraintSet( INestedIfElseClauseContainer upperIf) throws TableFunctionMalformedException {
		
		scan();
		
		// ret contains the constraint set (mapping from ovName to a boolean indicating if it is a "SOME" clause)
		Map<String, Boolean>  ret = new HashMap<String, Boolean>();
		while (!(token=='h') && (index < text.length)) {// loop until we find a "have"
			ret.putAll(this.constraint());
		}

		this.currentHeader = new TempTableHeaderCell(new ArrayList<TempTableHeaderParent>(), ret, false, this.getSSBNNode());
		
		// adds the header to the container (table or upper if/else-clause) before it is changed to another header.
		if (upperIf == null) {
			// No upper container identified. Let's assume to be the upper-most container (the temporary table)
			upperIf = this.tempTable;			
		} 		
		upperIf.addNestedClause(this.currentHeader);
		
		return ret;
	}

	/**
	 * constraint  :: = allop varsetname		
	 * @param upperIf
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected Map<String, Boolean> constraint() throws TableFunctionMalformedException {
		
		// init header
		if (!(token == 'a') && !(token == 'y')) {
			expected("ALL or SOME");
		}
		
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		// load some/all varsetname
		boolean isSome = true;
		if (token == 'a') {
			// Debug.println("ALL VERIFIED");
			isSome = false;
		} else if (token == 'y') {
			isSome = true;
		} else {
			expected("ALL or SOME");
		}
		// SCAN FOR varsetname
		List<String> varSetName = this.varsetname();
		// Debug.println("SCANNED VARSETNAME := " + varName);
		for (String varName : varSetName) {
			if (ret.containsKey(varName) && (isSome != ret.get(varName))) {
				// this is for tracing.
				try {
					Debug.println(getClass(), "Overwriting varsetname: " + (ret.get(varName)?"SOME ":"ALL ") + varName
							+ " -> " + (isSome?"SOME ":"ALL ") + varName);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			ret.put(varName, isSome);
		}
		scan();
		
		return ret;
	}

	/**
	 *   It skippes white spaces after evaluation.<br/>
	 *   varsetname ::= ident[","ident]*<br/>
	 *   Or, for backward compatibility,<br/>
	 *   varsetname ::= ident[","ident]*<br/>
	 *   @return list of loaded variable names
	 */
	protected List<String> varsetname() throws TableFunctionMalformedException {
		
		// we don't have to set header's varsetname here because ifStatement (upper caller) would do so.
		
		List<String> ret = new ArrayList<String>();	// a list containing variable names (e.g. {st, sr, z})
		
		// scan for the ident
		do {
			scan();	
			if (token == 'x') {
				// Debug.println("SCANING IDENTIFIER " + value);
				if (this.noCaseChangeValue != null && (this.noCaseChangeValue.trim().length() > 0)) {
					ret.add(this.noCaseChangeValue);
				}
			} else {
				expected("Identifier");
			}	
			
			// search for ["," ident]* loop or, for backward compatibility, ["." ident]
			if (this.look == ',' || this.look == '.') {
				this.nextChar();
				this.skipWhite();
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
			throw new TableFunctionMalformedException();
		}
		// Debug.println("EXITING NOT FACTOR");
		
		if (isNot) {
			return new CompilerNotValue(factor);
		} else {
			return factor;
		}
	}

	/**
	 * b_factor ::= ident "=" ident | "(" b_expression ")"
	 * 
	 */
	protected ICompilerBooleanValue bFactor() throws InvalidConditionantException,
								  TableFunctionMalformedException{
		
		String conditionantName = null;
		
		
		
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
		
		if (token == 'x') {
			conditionantName = this.noCaseChangeValue;
			// consistency check C09: verify whether is conditionant of the node
			if (this.getNode() != null) {
				if (!this.isValidConditionant(this.getMEBN(), this.getNode(), conditionantName )) {
					// Debug.println("->" + getNode());
					throw new InvalidConditionantException();
				}
			}
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR CONDITIONANTS' POSSIBLE STATES
		scan();
		
		// Debug.println("SCANED FOR CONDITIONANTS' POSSIBLE STATES");
		
		if (token == 'x') {
			// TODO the name of the possible value may contain caracters like dots, but this method cannot handle such characters
			
			// consistency check C09: verify whether conditionant has valid values
			if (this.getNode() != null) {
				if (!this.isValidConditionantValue(this.getMEBN(),conditionantName,this.noCaseChangeValue)) {
					throw new InvalidConditionantException();
				}
			}
			
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		
		// if code reached here, the condicionant check is ok

		//	prepare to add current temp table's header's parent (condicionant list)
		MultiEntityNode mebnNode = null;
		for (INode parent : getNode().getParentNodes()) {
			if (parent == null) {
				try {
					Debug.println(getClass(), getNode() + " contains a null parent...");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			String parentName = null;
			if (parent instanceof InputNode) {
				InputNode inputNode = (InputNode) parent;
				if (inputNode.getResidentNodePointer() != null && inputNode.getResidentNodePointer().getResidentNode() != null) {
					parentName = inputNode.getResidentNodePointer().getResidentNode().getName();
				} else {
					try {
						Debug.println(getClass(), inputNode + " is not pointing to a resident node...");
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			} else {
				parentName = parent.getName();
			}
			if (conditionantName.equalsIgnoreCase(parentName) 
					&& (parent instanceof MultiEntityNode)) {
				mebnNode = (MultiEntityNode)parent;
			}
		}
		// If not found, its an error!		
		if (mebnNode == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		Entity condvalue = null;
		
		// search for an entity with a name this.value
		ResidentNode possibleValueHolder = null;
		if (mebnNode instanceof InputNode) {
			possibleValueHolder = ((InputNode) mebnNode).getResidentNodePointer().getResidentNode();
		} else if (mebnNode instanceof ResidentNode) {
			possibleValueHolder = (ResidentNode)mebnNode;
		} else {
			throw new InvalidConditionantException(this.value + " must be either a resident node or an input node.");
		}
		for (Entity possibleValue : possibleValueHolder.getPossibleValueListIncludingEntityInstances()) {
			if (possibleValue.getName().equalsIgnoreCase(this.value)) {
				condvalue = possibleValue;
				break;
			}
		}
		
		// If not found, its an error!		
		if (condvalue == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		// Set temp table's header condicionant
		TempTableHeaderParent headerParent = new TempTableHeaderParent(mebnNode, condvalue);
		// TODO optimize above code, because its highly redundant (condvalue should be found anyway on that portion of code)
		
		this.currentHeader.addParent(headerParent);	// store it as a conditionant declared inside a boolean expression
		
		return headerParent;
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
			this.currentHeader = new TempTableHeaderCell(null,null,true, this.getSSBNNode()); 
			
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
	 * assignment_or_if ::= assignment | if_statement
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
			// prepare to revert 
			if ((this.look == 'i') && (this.tokenLookAhead() == this.kwcode[this.lookup("IF")])) {
				// this is an if-clause
				this.ifStatement(upperIf);
				// an if-clause doesnt have do return something...
			} else {
				// since it is an assignment, we should check probability consistency as well
				
				// Consistency check C09
				// Structures that allow us to Verify if all states has probability declared
				List<Entity> declaredStates = new ArrayList<Entity>();
				List<Entity> possibleStates = null;			
				if (this.getNode() != null) {
					possibleStates = this.getNode().getPossibleValueListIncludingEntityInstances();
				}
				
				this.assignment(declaredStates, possibleStates);
				
				if (this.getNode() != null) {
					// Consistency check C09
					// Verify if all states has probability declared
					if (!declaredStates.containsAll(possibleStates)) {
						throw new SomeStateUndeclaredException();
					}
				}
				
				// Consistency check C09
				// Verify if sum of all declared states' probability is 1
				
				// runtime probability bound check (on SSBN generation time)
				if (!(getNode() instanceof MultiEntityDecisionNode)) {
					// ignore continuous nodes, because sum may not be 1
					if (!this.currentHeader.isSumEquals1()) {
						// Debug.println("Testing cell's probability value's sum: " + currentHeader.getProbCellSum());
						if (!Float.isNaN(this.currentHeader.getProbCellSum())) {
							throw new InvalidProbabilityRangeException();
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
	 * assignment ::= ident "=" expression [ "," assignment ]* | expression 
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
	protected AProbabilityValue assignment(List<Entity> declaredStates, List<Entity> possibleStates) 
					throws InvalidProbabilityRangeException, 
						   TableFunctionMalformedException,
						   SomeStateUndeclaredException{
		
		// prepare a representation of a cell inside the temporary table
		TempTableProbabilityCell currentCell = new TempTableProbabilityCell(null, null);
		
		// this boolean is true is this "assignment" is actually not an assignment (this is not ident "=" expression [ "," assignment ]*)
		boolean isAssigningToNoState = false;
		
		// check if this is an assignment by checking if it is in format "<token> = <something>"
		char nextLookAhead = 0;
		if (this.isAlpha(this.look)){	// the scan() works only for non-numeric values
			Object memento = this.getMemento();	// store current state, so that we can revert changes later
			this.scan();
			nextLookAhead = this.look;
			this.restoreMemento(memento);	// revert change made by scan()
		}
		if ((nextLookAhead == '=')) {
			// this is an assignment
			// SCAN FOR IDENTIFIER
			scan();
			if (token == 'x') {
				if (this.getNode() != null) {
					// Consistency check C09
					// Remember declared states, so we can check later if all states was declared
					Entity possibleValue = null;
					try {
						possibleValue = possibleStates.get(this.getNode().getPossibleValueIndex(this.noCaseChangeValue));
					} catch (Exception e) {
						//throw new TableFunctionMalformedException(e.getMessage());
						throw new TableFunctionMalformedException(e);
					}
					if (possibleValue == null) {
						throw new TableFunctionMalformedException();
					}
					declaredStates.add(possibleValue);
					currentCell.setPossibleValue(possibleValue);
				}
				
			} else {
				expected("Identifier");
			}
			
			// LOOK FOR = OPERATOR
			match('=');
		} else {
			isAssigningToNoState = true;
		}
		

		// consistency check C09
		// ret verifies the sum of all declared states' probability (must be 1)
		// boolean hasUnknownValue shows if some ret was unsolvable (e.g. parents currently unavailable).
		AProbabilityValue ret = expression();		
		String retValue = ret.getValue();
		boolean hasUnknownValue = (retValue == null);
		if (!hasUnknownValue) {
			try {
				// if it is not parsable, then it is unknown
				float retValueFloat = Float.parseFloat(retValue);
				// consistency check C09
				// a single state shall never have prob range out from [0,1]
				if ( (retValueFloat < 0.0) || (1.0 < retValueFloat)) {
					throw new InvalidProbabilityRangeException();
				}
			} catch (InvalidProbabilityRangeException e) {
				// InvalidProbabilityRangeException may become a subclass of NumberFormatException in a future.
				// this is to guarantee that InvalidProbabilityRangeException is thrown even in such case.
				throw e;
			} catch (NumberFormatException e) {
				hasUnknownValue = true;
			}
		}
		
		// add cell to header
		currentCell.setProbability(ret);
		if (currentCell.getPossibleValue() != null 	// this is an assignment ::= state = probability
//				|| (isAssigningToNoState && (possibleStates == null || possibleStates.isEmpty()))) {	// this is an expression, and node has no possible state
				|| isAssigningToNoState ) {	// this is an expression (not an assignment)
			this.currentHeader.addCell(currentCell);
			// if node has no possible value, add current cell, although it has no related entity (state)
		}
		// Debug.println("Adding cell: " + currentCell.getPossibleValue().name + " = " + ret.toString());
		
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			AProbabilityValue temp = assignment(declaredStates, possibleStates);
			String tempValue = temp.getValue();
			hasUnknownValue = hasUnknownValue || (tempValue== null);
			if (hasUnknownValue) {
				// TODO we are returning null, but if a combining rule must be applied for assignments, then it should return an string expression...
				retValue = null;
			} else {
				try {
					retValue = String.valueOf(Float.parseFloat(retValue) + Float.parseFloat(temp.getValue()));
				} catch (NumberFormatException e) {
					retValue = null;
				}
			}
		} else {
			// this is the last assignment. If there are undeclared states, force their values to 0%.
			// obtain undeclared states = possibleStates - declaredStates
			Collection<Entity> undeclaredStates = new HashSet<Entity>(possibleStates);
			undeclaredStates.removeAll(declaredStates);
			for (Entity entity : undeclaredStates) {
				if (entity != null) {
					if (isAssigningToNoState) {
						// if node has state and this is an expression (not an assignment), then all states will have the current expression as its value
						// do not add again, because previous "this.currentHeader.addCell(currentCell);" (line 1038) is adding a cell with no entity 
						// so that a cell with no entity represents a non-assignment
//						this.currentHeader.addCell(new TempTableProbabilityCell(entity, ret));
					} else {
						// add assignment: <undeclared state> = 0.0,
						this.currentHeader.addCell(new TempTableProbabilityCell(entity, new SimpleProbabilityValue(0.0f)));
					}
					declaredStates.add(entity);
				}
				// we do not need to update retValue (the total probability),
				// because it would be something like retValue += 0.0 (that is, it will not be altered at all);
			}
		}
		
		Float retValueFloat = Float.NaN;
		try {
			if (retValue != null) {
				retValueFloat = Float.parseFloat(retValue);
			}
		} catch (NumberFormatException e) {
			// in this case, retValueFloat = Float.NaN;
		} 

		// Debug.println("Returned expression value = " + retValue);
		if (!Float.isNaN(retValueFloat) && (retValueFloat < 0)) {
			throw new InvalidProbabilityRangeException();
		}
		
		return new SimpleProbabilityValue(retValueFloat);
	}

	/**
	 * expression ::= term [ addop term ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected AProbabilityValue expression() throws TableFunctionMalformedException,
												  InvalidProbabilityRangeException,
												  SomeStateUndeclaredException{
		
		// temp table already created by upper caller
		
		AProbabilityValue temp1 = term();
		AProbabilityValue temp2 = null;
		
		String temp1StrValue = null;
		String temp2StrValue = null;
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			temp2 = term();
			temp1StrValue = temp1.getValue();
			temp2StrValue = temp2.getValue();
			try {
				// prune the expression subtree if they are known values and solvable sub-expressions...
				temp1 = new AddOperationProbabilityValue(
							temp1.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp1StrValue))):temp1 ,
							temp2.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp2StrValue))):temp2);
			} catch (NumberFormatException e) {
				temp1 = new AddOperationProbabilityValue(temp1, temp2);
			}
			break;
		case '-':
			match('-');
			temp2 = term();
			temp1StrValue = temp1.getValue();
			temp2StrValue = temp2.getValue();
			try {
				// prune the expression subtree if they are known values and solvable sub-expressions...
				temp1 = new SubtractOperationProbabilityValue(
						temp1.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp1StrValue))):temp1 ,
						temp2.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp2StrValue))):temp2);
			} catch (NumberFormatException e) {
				temp1 = new SubtractOperationProbabilityValue(temp1, temp2);
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
	protected AProbabilityValue term() throws TableFunctionMalformedException,
											InvalidProbabilityRangeException,
											SomeStateUndeclaredException{
		AProbabilityValue temp1 = signedFactor();
		AProbabilityValue temp2 = null;
		
		String temp1Value = null;
		String temp2Value = null;
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			temp2 = this.signedFactor();
			temp1Value = temp1.getValue();
			temp2Value = temp2.getValue();
			try {
				// prune subexpression if it is known value
				return new MultiplyOperationProbabilityValue(
							temp1.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp1Value))):temp1 ,
							temp2.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp2Value))):temp2);
			} catch (NumberFormatException e) {}
			return new MultiplyOperationProbabilityValue(temp1 , temp2);
		case '/':
			match('/');
			temp2 = this.signedFactor();
			temp1Value = temp1.getValue();
			temp2Value = temp2.getValue();
			try {
				// prune subexpression if it is known value
				return new DivideOperationProbabilityValue(
							temp1.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp1Value))):temp1 ,
							temp2.isSolvableNow()?(new SimpleProbabilityValue(Float.parseFloat(temp2Value))):temp2);
			} catch (NumberFormatException e) {
				return new DivideOperationProbabilityValue(temp1 , temp2);
			}
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
	protected AProbabilityValue signedFactor() throws TableFunctionMalformedException,
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
	 * factor ::= number | function | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected AProbabilityValue factor() throws TableFunctionMalformedException,
											  InvalidProbabilityRangeException,
											  SomeStateUndeclaredException{
		AProbabilityValue ret = null;
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (isAlpha(look)) {
			ret = function();
		} else {
			ret =  getNum();
		}
		// Debug.println("Factor returning " + ret.toString());
		return ret;
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
	 * If ident is a euler number (i.e. "e"), then it will return ident (i.e. "e").
	 * 	It will return ident if undefined or unknown.
	 */
	protected AProbabilityValue possibleVal()throws TableFunctionMalformedException,
												  SomeStateUndeclaredException {

		this.getName();
		
//		AProbabilityValue ret = new SimpleProbabilityValue(Float.NaN);
		
		// look for the probability value of the state
		if (this.currentHeader != null) {
			for (TempTableProbabilityCell cell : this.currentHeader.getCellList()) {
				 if (cell.getPossibleValue().getName().equalsIgnoreCase(value) ) {
					 // Debug.println("\n => Variable value found: " + cell.getPossibleValue().name);
					 return cell.getProbability();	// it was a state with probability previously declared
				 }
			}
		} else {
			// if null, it means it was called before an assignment
			throw new SomeStateUndeclaredException();
		}
		
		// Debug.println("An undeclared possible value or a \"varsetname\" was used : " + value);
//		return ret;
		// return UntranslatedConstantProbabilityValue, so that by default "ident" is considered a constant number, instead of a state
		return new UntranslatedConstantProbabilityValue(noCaseChangeValue);
	}

	/**
	 * number ::= [digit]+
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	protected AProbabilityValue getNum() throws TableFunctionMalformedException {
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
	 * If true, it will generate the states for continuous nodes in the CPS. 
	 * I.e. it will generate ",1, 2, 3, 4, 5, 6" in
	 * "defineState( Continuous ,1, 2, 3, 4, 5, 6);".
	 * If false, it will generate only "defineState( Continuous );"
	 * This is set to false by default.
	 * @return the isToAddStatesToContinuousResidentNodes
	 */
	public boolean isToAddStatesToContinuousResidentNodes() {
		return isToAddStatesToContinuousResidentNodes;
	}

	/**
	 * If true, it will generate the states for continuous nodes in the CPS. 
	 * I.e. it will generate ",1, 2, 3, 4, 5, 6" in
	 * "defineState( Continuous ,1, 2, 3, 4, 5, 6);".
	 * If false, it will generate only "defineState( Continuous );"
	 * This is set to false by default.
	 * @param isToAddStatesToContinuousResidentNodes the isToAddStatesToContinuousResidentNodes to set
	 */
	public void setToAddStatesToContinuousResidentNodes(
			boolean isToAddStatesToContinuousResidentNodes) {
		this.isToAddStatesToContinuousResidentNodes = isToAddStatesToContinuousResidentNodes;
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
	 * Performs a scan and revert attributes (value, token, look, index, noCaseChangeValue)
	 * called twice, and method will return second token.
	 * @return: token of the scanned element
	 * 
	 */
	protected char tokenLookAhead () throws TableFunctionMalformedException {
		Object memento = this.getMemento();	// store current state
		char returnedToken = ' ';
		
		this.scan();
		returnedToken = this.token;	// this value will be the return value
		
		// revert global attributes
		this.restoreMemento(memento);
		
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
		throw new TableFunctionMalformedException("Error: " + error + " expected");
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
		if ((c >= 'a') && (c <= 'z'))
			return true; // lowercase
		if ((c >= 'A') && (c <= 'Z'))
			return true; // uppercase
		if (c == '_') {
			return true; // underscore
		}
		
		return false;
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

	/* reconhece operador aditivo */
	protected boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}
	
	// identifies the "negative" simbol
	protected boolean isMinus(char c) {
		return c == '-';
	}

	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node	
	 * @return if node with name == nodeName is a valid conditionant.
	 */
	protected boolean isValidConditionant(MultiEntityBayesianNetwork mebn, ResidentNode node, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName);
		
		
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParentNodes().contains(conditionant)) {
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
			
		}
		
		return false;
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must have a consistent possible value
	 * @return whether conditionantValue is a valid state for a conditionant with name conditionantName
	 */
	protected boolean isValidConditionantValue(MultiEntityBayesianNetwork mebn, String conditionantName, String conditionantValue) {
		Node conditionant = mebn.getNode(conditionantName);
		if (conditionant == null) {
			// Debug.println("No conditionant of name " + conditionantName);
			return false;
		}
		//// Debug.println("Conditionant node found: " + conditionant.getName());
		if ( conditionant instanceof IResidentNode) {
			// Debug.println("IS MULTIENTITYNODE");
			return ((IResidentNode)conditionant).getPossibleValueByName(conditionantValue) != null;
		} else {
			// Debug.println("Conditionant is not a resident node");
		}
			
		return false;
	}
	
	
	/**
	 *  function ::= possibleVal 
	 *  | "CARDINALITY" "(" varsetname ")" 
	 *  | "NodeCARD" "(" nodename ")"				 
	 *  | "EntityCARD" "(" entityname ")"			 
	 *  | "MIN" "(" expression ";" expression ")" 
	 *  | "MAX" "(" expression ";" expression ")" 
	 *  | "Mean" "(" nodename  ")" 
	 *  | "Sum" "(" nodename  ")" 
	 *  | "Multiply" "(" nodename  ")"  
	 *  | "NormalDist" "(" expression ";" expression ")"
	 *  | "Exp" "(" expression ";" expression ")"
	 *  | "Root" "(" expression ";" expression ")"
	 *  | "Log" "(" expression ";" expression ")"
	 *  | "Power" "(" expression ";" expression ")"
	 * @return numeric value expected for the function
	 * @throws TableFunctionMalformedException
	 */
	protected AProbabilityValue function()throws TableFunctionMalformedException,
											   InvalidProbabilityRangeException,
											   SomeStateUndeclaredException{
		AProbabilityValue ret = this.possibleVal();
		skipWhite();
		if (this.look == '(') {
			if (this.value.equalsIgnoreCase("CARDINALITY")) {
				return cardinality();
			} else if (this.value.equalsIgnoreCase("MIN") ) {
				return min();
			} else if (this.value.equalsIgnoreCase("MAX") ) {
				return max();
			} else if (this.value.equalsIgnoreCase("NodeCARD") ) {
				return this.nodeCard(); 
			} else if (this.value.equalsIgnoreCase("EntityCARD") ) {
					return this.entityCARD();
			} else if (this.value.equalsIgnoreCase("Mean") ) {
				return this.mean();
			} else if (this.value.equalsIgnoreCase("Sum") ) {
				return this.sum();
			} else if (this.value.equalsIgnoreCase("Multiply") ) {
				return this.multiply();
			} else if (this.value.equalsIgnoreCase("NormalDist")  
							|| this.value.equalsIgnoreCase("Log") 
							|| this.value.equalsIgnoreCase("Power") 
							|| this.value.equalsIgnoreCase("Exp")
							|| this.value.equalsIgnoreCase("Root")
						) {
				return this.untranslatedFunction(this.noCaseChangeValue, 2);	// must be case-sensitive
			// EXP and ROOT were changed to support 2 arguments
//			} else if (this.value.equalsIgnoreCase("Exp")
//					|| this.value.equalsIgnoreCase("Root")) {
//				return this.untranslatedFunction(this.noCaseChangeValue, 1);	// must be case-sensitive
			} else {
				// Debug.println("UNKNOWN FUNCTION FOUND: " + this.value);
				throw new TableFunctionMalformedException(this.getResource().getString("UnexpectedTokenFound")
						+ ": " + value);
			}
		}
		
		// Debug.println("Function returning " + ret);
		return ret;
	}
	
	/**
	 * This method represents functions which do not have to be "translated"
	 * (i.e. no semantics should be considered, and the name of the function should literaly be used in the output).
	 * These functions include:
	 * "NormalDist", "Log", "Power", "Exp", "Root"
	 * 
	 * @param numberOfArgs : how many arguments the functions have. The arguments are {@link #expression()}
	 * @param nameOfFunction : name of the function 
	 * @return
	 * @throws TableFunctionMalformedException 
	 * @throws SomeStateUndeclaredException 
	 * @throws InvalidProbabilityRangeException 
	 */
	protected AProbabilityValue untranslatedFunction(String nameOfFunction , int numberOfArgs) throws InvalidProbabilityRangeException, SomeStateUndeclaredException, TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		// read arguments
		List<AProbabilityValue> arguments = new ArrayList<MultiEntityUtilityFunctionCompiler.AProbabilityValue>();
		while(numberOfArgs > 0) {
			arguments.add(this.expression());
			if (--numberOfArgs <= 0) {
				break;	// no more arguments
			} else if (look == ',') {
				match(',');	// there are more arguments after comma
			} else {
				match(';');	// there are more arguments after comma
			}
		}
		ret = new FunctionExpressionProbabilityValue(this.currentHeader, nameOfFunction, arguments);
		match(')');
		return ret;
	}

	/**
	 * "Mean" "(" nodename  ")" 
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected AProbabilityValue mean() throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		scan();
		String nodeName = this.nodeName();
		ret = new MeanCombiningRuleProbabilityValue(this.currentHeader, nodeName);
		match(')');
		return ret;
	}
	
	/**
	 * "Sum" "(" nodename  ")" 
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected AProbabilityValue sum() throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		scan();
		String nodeName = this.nodeName();
		ret = new ParentAwareCombiningRuleProbabilityValue(this.currentHeader, nodeName , " + ");
		match(')');
		return ret;
	}
	
	/**
	 * "Multiply" "(" nodename  ")" 
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected AProbabilityValue multiply() throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		scan();
		String nodeName = this.nodeName();
		ret = new ParentAwareCombiningRuleProbabilityValue(this.currentHeader, nodeName, " * ");
		match(')');
		return ret;
	}

	/**
	 * "EntityCARD" "(" entityname ")"		
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected AProbabilityValue entityCARD() throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		scan();
		String entityName = null;
		if (token == 'x') {
			// Debug.println("SCANING IDENTIFIER " + value);
			if (this.noCaseChangeValue != null && (this.noCaseChangeValue.trim().length() > 0)) {
				entityName = this.noCaseChangeValue;
			}
		} else {
			expected("Identifier");
		}	
		// check entityName consistency: entityName must be an object entity
		// extract entity from mebn
		ObjectEntity entity = null;
		try {
			entity = this.getNode().getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getObjectEntityByName(entityName);
		} catch (Exception e) {
			throw new TableFunctionMalformedException(e);
		}
		if (entity == null) {
			throw new TableFunctionMalformedException(entityName + " is not an Object Entity");
		}
		
		skipWhite();
		ret = new EntityCardinalityProbabilityValue(this.currentHeader, entity);
		match(')');
		return ret;
	}

	/**
	 * "NodeCARD" "(" nodename ")"
	 * @return
	 * @throws TableFunctionMalformedException 
	 */
	protected AProbabilityValue nodeCard() throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		scan();
		String nodeName = this.nodeName();
		ret = new NodeCardinalityProbabilityValue(this.currentHeader, nodeName);
		match(')');
		return ret;
	}

	/**
	 * nodename ::= ident
	 * This method also checks if nodename is a name of a valid parent
	 * @return the ident read.
	 * @throws TableFunctionMalformedException 
	 */
	protected String nodeName() throws TableFunctionMalformedException {
		String nodeName = null;
		if (token == 'x') {
			// Debug.println("SCANING IDENTIFIER " + value);
			if (this.noCaseChangeValue != null && (this.noCaseChangeValue.trim().length() > 0)) {
				nodeName = this.noCaseChangeValue;
			}
		} else {
			expected("Identifier");
		}	
		// assert this compiler is associated with an resident node
		if (getNode() == null) {
			throw new TableFunctionMalformedException("Node == null");
		}
		// check nodeName consistency: nodeName must be a parent
		boolean found = false;
		for (INode node : getNode().getParentNodes()) {
			try {
				if (node instanceof InputNode) {
					InputNode inputNode = (InputNode) node;
					if (inputNode.getResidentNodePointer().getResidentNode().getName().equals(nodeName)) {
						found = true;
						break;
					}
				}
				if (node.getName().equals(nodeName)) {
					found = true;
					break;
				}
			} catch (Exception e) {
				try {
					Debug.println(getClass(), e.getMessage(), e);
				} catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
		}
		if (!found) {
			throw new TableFunctionMalformedException(nodeName + " is not a parent of " + getNode());
		}
		
		skipWhite();
		return nodeName;
	}

	/**
	 * Computes cardinality funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	protected AProbabilityValue cardinality()throws TableFunctionMalformedException {
		AProbabilityValue ret = null;
		match('(');
		
		scan();	
		if (token == 'x' && (this.noCaseChangeValue != null) && (this.noCaseChangeValue.trim().length() > 0)) {
			// Debug.println("SCANING IDENTIFIER " + value);
			ret = new CardinalityProbabilityValue(this.currentHeader, this.noCaseChangeValue);
		} else {
			expected("Identifier");
		}	
		skipWhite();
		// Debug.println("CARDINALITY'S ARGUMENT IS " + var);
		// TODO test if ret has returned NaN (guarantees "value" is a varsetname)?
		match(')');
		return ret;
		
	}
	
	/**
	 * Computes min funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	protected AProbabilityValue min()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MIN FUNCTION");
		
		AProbabilityValue ret1 = null;
		AProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		// separator of arguments can be either ';' or ','
		if ((look != ';') && (look != ',')){
			expected(",");
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
	protected AProbabilityValue max()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MAX FUNCTION");
		
		AProbabilityValue ret1 = null;
		AProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		// separator of arguments can be either ';' or ','
		if ((look != ';') && (look != ',')){
			expected(",");
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

	
	
	 
	
	// Some inner classes that might be useful for temporaly table creation (organize the table parsed from pseudocode)
	
	/**
	 * Container of a if-else-clause
	 */
	protected interface INestedIfElseClauseContainer {
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
		 * Initializes the "isKnownValue" attributes of TempTableHeaderParent objects
		 * by recursively calling this method for all nested causes.
		 * @param ssbnnode
		 * @see TempTableHeaderParent
		 * @throws NullPointerException if ssbnnode is null
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode);
	}
	
	
	protected class TempTable  implements INestedIfElseClauseContainer{

		protected List<TempTableHeaderCell> clauses = null;
		
		/**
		 * Represents the temporary CPT table (a list of if-else clauses)
		 */
		public TempTable() {
			super();
			this.clauses = new ArrayList<TempTableHeaderCell>();
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

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "" + this.clauses;
		}
		
		
		
		
	}
	
	protected class TempTableHeaderCell implements INestedIfElseClauseContainer {
		private ICompilerBooleanValue booleanExpressionTree = null; // core of the if statement
		List<TempTableHeaderParent> parents = null;	// this is also the leaf of boolean expression tree
		
		Map<String, Boolean> constraintMap = new HashMap<String,Boolean>();
		
		boolean isDefault = false;
		
		private List<TempTableProbabilityCell> cellList = null;
		
		private float leastCellValue = Float.NaN;
		
		private SSBNNode currentSSBNNode = null;
		
		private List<TempTableHeaderCell> nestedIfs = null;
		
		private INestedIfElseClauseContainer upperContainer = null;
//		private Map<String, Integer> validParentSetCounterMap = new HashMap<String, Integer>();
		
		/**
		 * Represents an entry for temporary table header (parents and their expected single values
		 * at that table entry/collumn).
		 * It can directly represent an if-clause (varsetname, any|all, bExpression, values, and nested ifs)
		 * Since an if-clause may be nested, it has a list of nested if-clauses
		 * @param parents: entries of an if-clause (list of (parent = value) pairs)
		 * @param constraintMap
		 * @param isDefault
		 */
		TempTableHeaderCell (List<TempTableHeaderParent> parents , Map<String, Boolean> constraintMap, boolean isDefault, SSBNNode currentSSBNNode) {
			this.parents = parents;
			this.constraintMap = constraintMap;
			this.isDefault = isDefault;
			this.cellList = new ArrayList<TempTableProbabilityCell>();
			this.currentSSBNNode = currentSSBNNode;
			this.nestedIfs = new ArrayList<TempTableHeaderCell>();
		}
		/**
		 * It Gets entries of boolean expression (a pair of Node and its expected value declared
		 * within a boolean expression inside a if clause)
		 * @return List of expected parents within if-clause
		 */		
		public List<TempTableHeaderParent> getParents() {
			return parents;
		}


//		/**
//		 * counts how many parents set were returning true at that sentence (column) of boolean
//		 * expression.
//		 * @return the validParentSetCount
//		 */
//		public Map<String, Integer> getValidParentSetCounterMap() {
//			return validParentSetCounterMap;
//		}
		
		public void setParents(List<TempTableHeaderParent> parents) {
			this.parents = parents;
		}
		
		public void addParent(TempTableHeaderParent parent) {
			this.parents.add(parent);
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
			if (this.currentSSBNNode.getParentSetByStrongOVWithWeakOVCheck(varsetname.split("\\.")).size() > 0) {
				return true;
			} else {
				return false;
			}
		}
		/**
		 * @return the cellList: possible values and its probability
		 */
		protected List<TempTableProbabilityCell> getCellList() {
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
		public void addCell(Entity possibleValue , AProbabilityValue probability) {
			this.addCell(new TempTableProbabilityCell(possibleValue, probability));
		}
		
		public void addCell(TempTableProbabilityCell cell) {
			if (cell == null) {
				return;
			}
			float value = Float.NaN;
			try {
				if (cell.getProbabilityValue() != null) {
					value = Float.parseFloat(cell.getProbabilityValue());
					// update the smallest declared probability value
					if (value > 0.0f) {
						if (Float.isNaN(this.leastCellValue) || (value < this.leastCellValue)) {
							this.leastCellValue = value;
						}
					}
				}
			} catch (InvalidProbabilityRangeException e) {
				// do nothing at now - it will be detected soon
			} catch (NumberFormatException e) {
				// do nothing - just don't updaate least cell value.
			}
			
			this.cellList.add(cell);
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return
		 */
		public boolean isSumEquals1() throws InvalidProbabilityRangeException {
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
			try {
				for (TempTableProbabilityCell cell : this.cellList) {
					if (cell.getProbabilityValue() == null) {
						// unknown value
						sum = Float.NaN;
						break;
					}
					sum = (float)sum + Float.parseFloat(cell.getProbabilityValue());
				}
			}catch (NumberFormatException e) {
				sum = Float.NaN;
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
		 * This method just calls {@link #evaluateBooleanExpressionTreeRecursive(Map, Map)}
		 * @param valuesOnCPTColumn
		 * @return
		 */
		public boolean evaluateBooleanExpressionTree(Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			if (this.isDefault) {
				// force it to return true if this is a default (i.e. there is no if-clause)
				return true;
			}
			
			if (constraintMap == null) {
				throw new IllegalArgumentException("This is an if-clause with no variable in SOME/ALL clause.");
			}
			
//			// prepare to call recursive with a new map instead of original, so that original map remains unchanged
//			Map<String, Boolean> constraintMapCpy = new HashMap<String, Boolean>(constraintMap);
			
			// the following code is already performed by cleanUpByVarsetName
//			// remove from constraint map (argument) all entries (SOME/ALL) that is not used by header parents
//			Set<String> keyToRemove = new HashSet<String>();
//			for (String key : simplifiedConstraintMap.keySet()) {
//				boolean isToRemove = true;
//				if (key != null) {
//					for (TempTableHeaderParent tableParent : parents) {
//						for (Argument arg : tableParent.parent.getArgumentList()) {
//							if (key.equalsIgnoreCase(arg.getOVariable().name)) {
//								isToRemove = false;
//								break;
//							}
//						}
//						if (!isToRemove) {
//							break;
//						}
//					}
//				}
//				if (isToRemove) {
//					keyToRemove.add(key);
//				}
//			}
//			for (String key : keyToRemove) {
//				simplifiedConstraintMap.remove(key);
//			}
			
			
//			return this.evaluateBooleanExpressionTreeRecursive(valuesOnCPTColumn, constraintMapCpy );
			return this.evaluateBooleanExpressionTreeRecursive(valuesOnCPTColumn, new HashMap<String, Boolean>(constraintMap) );
		}
		
		/**
		 * This is a wrapper method for calling {@link #evaluateBooleanExpressionTree(Map, Boolean)} recursively
		 * after setting some values in valuesOnCPTColumn as a immutable value (this was done in order to implement
		 * combinations of SOME and ALL in same expression).
		 * This method is called by {@link #evaluateBooleanExpressionTree(Map)} to perform recursive SOME/ALL evaluation
		 * of {@link #evaluateBooleanExpressionTree(Map, Boolean). The basic algorithm is:<br/>
		 * 1 - if the quantities of OVs in constraintMap is less than or equals to 1, call #evaluateBooleanExpressionTree(Map, Boolean) (this is similar to the old LPD implementation); <br/>
		 * 2 - pick an OV from constraintMap and remove it from constraintMap;<br/>
		 * 3 - Set the picked OV to a fixed value;<br/>
		 * 4 - Call recursive;<br/>
		 * 5 - If constraint of OV is SOME, aggregate using boolean OR, if not, aggregate with boolean AND;<br/>
		 * 6 - Iterate steps 3 to 5 until there are no more (fixed) possible values<br/>
		 * @param valuesOnCPTColumn
		 * @param constraintMap : stores which ordinary variable was not treated yet.
		 * @return
		 */
		protected boolean evaluateBooleanExpressionTreeRecursive(Map<String, List<EntityAndArguments>> valuesOnCPTColumn, Map<String, Boolean> constraintMap) {
			// clean null mapping
			try {
				constraintMap.remove(null);
			} catch (Exception e) {}
			// clean empty mapping
			try {
				constraintMap.remove("");
			} catch (Exception e) {}
			
			// 1 - if the quantities of OVs in constraintMap is less than or equals to 1, call #evaluateBooleanExpressionTree(Map, Boolean) (this is similar to the old LPD implementation); 
			if (constraintMap == null || constraintMap.keySet().size() <= 0) {
				return this.evaluateBooleanExpressionTree(valuesOnCPTColumn, true);	// by default, call as if any
			}
			if (constraintMap.keySet().size() == 1) {
				return this.evaluateBooleanExpressionTree(valuesOnCPTColumn, constraintMap.values().iterator().next());
			}
			
			
			// at this point, constraintMap is not empty and has no null key
			
			// 2 - pick an OV from constraintMap and remove it from constraintMap;
			String ovName = constraintMap.keySet().iterator().next();
			Boolean isSome = constraintMap.get(ovName);
			if (isSome == null) {
				throw new IllegalArgumentException(ovName + " is neither SOME nor ALL");
			}
			constraintMap.remove(ovName);
			
			// obtain what fixed values we can use for step 3-6
			Set<String> possibleValuesForRemovedOV = new HashSet<String>();
			for (List<EntityAndArguments> entityAndArgumentList : valuesOnCPTColumn.values()) {
				for (EntityAndArguments entityAndArguments : entityAndArgumentList) {
					for (OVInstance ovi : entityAndArguments.arguments) {
						if (ovName.equalsIgnoreCase(ovi.getOv().getName())) {
							possibleValuesForRemovedOV.add(ovi.getEntity().getInstanceName());
						}
					}
				}
			}
			
			if (possibleValuesForRemovedOV.isEmpty()) {
				// there is a constraint for a ov (removed ov) which is not used inside the boolean expression.
				// just return the evaluation without such constraint
				return this.evaluateBooleanExpressionTreeRecursive(valuesOnCPTColumn, constraintMap);
			}
			
			// prepare return, which is initialized with a boolean neutral value (on OR/SOME, it's "false"; on AND/ALL, it's "true")
			// at this point, isSome is never null
			Boolean ret = !isSome;
			
			// 6 - Iterate steps 3 to 5 until there are no more (fixed) possible values
			// at this point, possibleValuesForRemovedOV is not empty
			for (String fixedOVValue : possibleValuesForRemovedOV) {
				// 3 - Set the picked OV to a fixed value;
				
				// generate a copy of valuesOnCPTColumn (changes in the copy shall not change the original)
				HashMap<String, List<EntityAndArguments>> valuesOnCPTColumnCpy = new HashMap<String, List<EntityAndArguments>>();
				for (String key : valuesOnCPTColumn.keySet()) {
					valuesOnCPTColumnCpy.put(key, new ArrayList<EntityAndArguments>(valuesOnCPTColumn.get(key)));
				}
				
				// remove entries of EntityAndArguments which does not match fixedOVValue 
				// (this should be equivalent to setting OV to a fixed value at recursive call)
				for (List<EntityAndArguments> entityAndArgumentList : valuesOnCPTColumn.values()) {
					List<EntityAndArguments> entityAndArgumentsToRemove = new ArrayList<MultiEntityUtilityFunctionCompiler.EntityAndArguments>();
					for (EntityAndArguments entityAndArguments : entityAndArgumentList) {
						for (OVInstance ovi : entityAndArguments.arguments) {
							if (ovName.equalsIgnoreCase(ovi.getOv().getName())
									&& !fixedOVValue.equalsIgnoreCase(ovi.getEntity().getInstanceName())) {
								// this OV is the same of the picked OV (ovName), but its value is not the fixedOVValue. Hence, we should remove it from cpy
								entityAndArgumentsToRemove.add(entityAndArguments);
								break;	// break "for (OVInstance ovi : entityAndArguments.getArguments())"
							}
						}
					}
					entityAndArgumentList.removeAll(entityAndArgumentsToRemove);
				}
				
				
				// 4 - Call recursive;
				// 5 - If constraint of OV is SOME, aggregate using boolean OR, if not, aggregate with boolean AND;
				if (isSome) {
					ret = ret || this.evaluateBooleanExpressionTreeRecursive(valuesOnCPTColumnCpy, constraintMap);
					if (ret) {
						break;
					}
				} else {
					ret = ret && this.evaluateBooleanExpressionTreeRecursive(valuesOnCPTColumnCpy, constraintMap);
					if (!ret) {
						break;
					}
				}
			}
			
			return ret;	
		}
		
		
		/**
		 * NOTE: this method does not support combination of SOME and ALL. If you want to use a combination of SOME and ALL, call
		 * {@link #evaluateBooleanExpressionTree(Map)} instead.
		 * This method evaluates a boolean expression (ICompilerBooleanValue - in tree format).
		 * The leaf-values are evaluated using the param. 
		 * Note that this method can used to evaluate a collum of ProbabilisticTable
		 * (consider evaluating each given collumn of CPT is like evaluating every parents as findings
		 * at a given moment).
		 * A "default" header will allways return true
		 * @param valuesOnCPTColumn: a map which the key is a name of a parent node and
		 * the value is its current possible values to be evaluated.
		 * For example, if we want to evalueate an expression when for a node "Node(!ST0)" we
		 * have parents Parent1(!ST0,!Z0), Parent1(!ST0,!Z1), Parent2(!ST0,!T0), and Parent2(!ST0,!T1)
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
		public boolean evaluateBooleanExpressionTree(Map<String, List<EntityAndArguments>> valuesOnCPTColumn, Boolean isAny) {
			// TODO use recursive call to treat some and all
			
			// initial test
			if (this.isDefault) {
				return true;
			}
			// TODO

			// reset cardinality counter
//			this.getValidParentSetCounterMap().clear();
			
			List<TempTableHeaderParent> parentsList = parents;
			
			//	return is initialized with a boolean neutral value (on OR/ANY, its "false"; on AND/ALL, its "true")
			boolean ret = !isAny;	// this method will return this value
			
			// start evaluation. We should run through leafs again... TODO: optimize?
			
			// "pointer" 
			TempTableHeaderParent pointer = null;
			
			// prepare leafs
			
			// run inside parent list (they are the declared condicionants within boolean expression),
			// which also are the leafs of that expression!
			for (TempTableHeaderParent leaf : parentsList) {
				if (!leaf.isKnownValue()) {
					// if leaf is not set to be a constant value, then we should set it to 
					// evaluate a combination of entities
					if (leaf.parent instanceof InputNode) {
						InputNode inputNode = (InputNode) leaf.parent;
						leaf.setEvaluationList(valuesOnCPTColumn.get(inputNode.getResidentNodePointer().getResidentNode().getName()));
					} else {
						leaf.setEvaluationList(valuesOnCPTColumn.get(leaf.parent.getName()));
					}
				}
			}
			
			//	evaluate (True,Alpha), (False,Alpha), (True,Beta), (False,Beta)...
			boolean hasMoreCombination = true;
			// expressionWasEvaluated checks if boolean header was once evaluated. If not, all condicionants were invalid (in that case, return false)
			boolean expressionWasEvaluated = false;
			while (hasMoreCombination) {
				// TODO the test below might be dangerous in multithread application...?
				if (isSameOVsameEntity(parents)) { // only evaluates same entities for same OVs
					if (isAny) {	
						// if ANY, then OR 
						ret = ret || this.getBooleanExpressionTree().evaluate();
						if (ret == true) {
//							this.increaseValidParentSetCount();
							// we cant return immediately because we should count "cardinality"
							// if we are not counting cardinality here, we can return immediately
							return true;
						}
					} else {	// if ALL, then AND
						ret = ret && this.getBooleanExpressionTree().evaluate();
						if (ret == false) {
							return false;
						}
//						this.increaseValidParentSetCount();
					}
					expressionWasEvaluated = true; // there was a valid header, so, the return value is valid
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
		protected boolean isSameOVsameEntity(List<TempTableHeaderParent> leaves) {
			// the following code was commented out because it seems that we do not have to be worried about matches (i.e. if same ov, same entity value)
			// when generating the prob distro, because we are not counting "combinations" of nodes anymore (the atomicity went to node level from now on)
//			for (TempTableHeaderParent leaf : leaves) {
//				if (leaf.isKnownValue()) {
//					continue;
//				}
//				List<OVInstance> args = leaf.getCurrentEntityAndArguments().arguments;
////				asdf
//				
//				// TODO stop obtaining args from actual SSBNNodes and start analyzing input nodes
//				
//				// first, test if leaf has same arguments as its ssbnnode (if ssbnnode has same arguments as parents)
//				for (OVInstance argParent : args) {
//					// if it has same OV as ssbnnode, then should be the same entity
//					
//					for (OVInstance argChild : this.currentSSBNNode.getArguments()) {
//						if (argChild.getOv().name.equalsIgnoreCase(argParent.getOv().name)) {
//							if (!argChild.getEntity().getInstanceName().equalsIgnoreCase(argParent.getEntity().getInstanceName())) {
//								return false;
//							}
//						}
//					}
//				}
//				for (int i = leaves.indexOf(leaf) + 1; i < leaves.size(); i++) {
//					// try all other leaves
//					for (OVInstance argleaf : args) {
//						if (leaves.get(i).isKnownValue()) {
//							// if current leaf has a known value (i.e. it is allways evaluating false), then
//							// it is not necessary to test OVInstance's name-value consistency
//							// (we don't have to check if OVs with same name has same value, since
//							// at evaluation time their values are not going to be used at all)
//							continue;
//						}
//						for (OVInstance argothers : leaves.get(i).getCurrentEntityAndArguments().arguments) {
//							if(argleaf.getOv().name.equalsIgnoreCase(argothers.getOv().name)) {
//								if (!argleaf.getEntity().getInstanceName().equalsIgnoreCase(argothers.getEntity().getInstanceName()) ) {
//									// if they are the same OV but different instances of Entities... then false
//									return false;
//								}
//							}
//						}
//					}
//				}
//			}
			return true;
		}
		
		/**
		 * If a boolean expression uses a node whose argument was not declared in constraintSet,
		 * then the boolean expression should return false. In this case, it is useless to evaluate
		 * such expressions (because it is allways false). So, this method identifies such boolean expressions.
		 * 
		 * @param baseSSBNNode: a SSBNNode which contains this CPT. This method doesn't check
		 * if this SSBNNode is really the expected one. This argument is used by this method
		 * in order to obtain the "similar" parent set at a given moment.
		 */
		protected void cleanUpByVarSetName(SSBNNode baseSSBNNode) {
			
			// clean constraintMap, so that it contains only constraints that are actually used.
			if (this.getConstraintMap() != null) {
				Set<String> keysToRemove = new HashSet<String>();
				for (String key : this.getConstraintMap().keySet()) {
					boolean isToRemove = true;
					if (key != null) {
						for (TempTableHeaderParent tableParent : this.parents) {
							for (Argument arg : tableParent.parent.getArgumentList()) {
								if (key.equalsIgnoreCase(arg.getOVariable().getName())) {
									isToRemove = false;
									break;
								}
							}
							if (!isToRemove) {
								break;
							}
						}
					}
					if (isToRemove) {
						keysToRemove.add(key);
					}
				}
				for (String key : keysToRemove) {
					this.getConstraintMap().remove(key);
				}
			}
			
			// no cleanup is necessary when this is a default distro - no boolean evaluation is present
			if (this.isDefault()) {
				return;
			}
			
			
			// extract condicionants declared within the expression
			for (TempTableHeaderParent headParent : this.parents) {
				// check if the ovs (arguments) of the headParent is a subset of ovs in constraintSet
				// i.e. check if all the arguments in headParentOVNames were previously declared in constraintSet
				for (Argument arg : headParent.parent.getArgumentList()) {
					if (!this.getConstraintMap().containsKey(arg.getOVariable().getName())) {
						// headParent has an ov which was not in constraintSet. Hence, its evaluation must allways return false.
						headParent.setKnownValue(true);	// by indicating that it is a known value, it will return false
						break;
					}
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
				if (this.isDefault) {
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
		 * This is the smallest probability value.
		 * This is used for estimating a reasonable error margin (the error margin in float comparison should be 
		 * proportional to the smallest probability value)
		 * @return the leastCellValue
		 */
		public float getLeastCellValue() {
			return leastCellValue;
		}
		/**
		 * This is the smallest probability value.
		 * This is used for estimating a reasonable error margin (the error margin in float comparison should be 
		 * proportional to the smallest probability value)
		 * @param leastCellValue the leastCellValue to set
		 */
		public void setLeastCellValue(float leastCellValue) {
			this.leastCellValue = leastCellValue;
		}
		/**
		 * @return the currentSSBNNode
		 */
		public SSBNNode getCurrentSSBNNode() {
			return currentSSBNNode;
		}
		/**
		 * @param currentSSBNNode the currentSSBNNode to set
		 */
		public void setCurrentSSBNNode(SSBNNode currentSSBNNode) {
			this.currentSSBNNode = currentSSBNNode;
		}
		/**
		 * @return the upperContainer
		 */
		public INestedIfElseClauseContainer getUpperContainer() {
			return upperContainer;
		}
		/**
		 * @param upperContainer the upperContainer to set
		 */
		public void setUpperContainer(INestedIfElseClauseContainer upperContainer) {
			this.upperContainer = upperContainer;
		}
		/**
		 * @return the nestedIfs
		 */
		public List<TempTableHeaderCell> getNestedIfs() {
			return nestedIfs;
		}
		/**
		 * Represents the constraintsSet (the pair SOME/ALL + ordVariableName). The map represents the following schema:
		 * key := name of ordinary variables
		 * value := whether it is SOME.
		 * E.g. "SOME X,Y ALL Z SOME V ALL W,R" is represented as:
		 * 		{X->TRUE, Y->TRUE, V->TRUE, Z->FALSE, W->FALSE, R->FALSE}
		 * @return the constraintMap
		 * @return the constraintMap
		 */
		public Map<String, Boolean> getConstraintMap() {
			return constraintMap;
		}
		/**
		 * Represents the constraintsSet (the pair SOME/ALL + ordVariableName). The map represents the following schema:
		 * key := name of ordinary variables
		 * value := whether it is SOME.
		 * E.g. "SOME X,Y ALL Z SOME V ALL W,R" is represented as:
		 * 		{X->TRUE, Y->TRUE, V->TRUE, Z->FALSE, W->FALSE, R->FALSE}
		 * @return the constraintMap
		 * @param constraintMap the constraintMap to set
		 */
		public void setConstraintMap(Map<String, Boolean> constraintMap) {
			this.constraintMap = constraintMap;
		}
//		/**
//		 * @param validParentSetCounterMap the validParentSetCounterMap to set
//		 */
//		public void setValidParentSetCounterMap(
//				Map<String, Integer> validParentSetCounterMap) {
//			this.validParentSetCounterMap = validParentSetCounterMap;
//		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "" + constraintMap + "; " + this.getBooleanExpressionTree();
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
		
		
		
		
	}
	
	protected interface ICompilerBooleanValue {
		/**
		 * Obtains recursively a boolean value
		 * @return true or false
		 */
		public boolean evaluate();
	}
	
	protected class CompilerNotValue implements ICompilerBooleanValue{
		protected ICompilerBooleanValue value = null;
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
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "!" + value;
		}
	}
	
	protected class CompilerOrValue implements ICompilerBooleanValue{
		protected ICompilerBooleanValue value1 = null;
		protected ICompilerBooleanValue value2 = null;
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
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value1 + " | " + value2;
		}
	}
	
	protected class CompilerAndValue implements ICompilerBooleanValue{
		protected ICompilerBooleanValue value1 = null;
		protected ICompilerBooleanValue value2 = null;
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
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value1 + " & " + value2;
		}
		
	}
	
	protected class TempTableHeaderParent implements ICompilerBooleanValue {
		MultiEntityNode parent = null;
		protected Entity value = null;
		
		protected List<EntityAndArguments> evaluationList = null;
		
		//protected Entity currentEvaluation = null;
		
		protected int currentEvaluationIndex = -1;
		
		protected boolean isKnownValue = false;	// if this leaf is "absurd", then its value is known = false.
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return parent + " = " + value;
		}
		
		/**
		 * Represents a parent and its expected single value
		 * at that table entry/collumn
		 * @param parent
		 * @param value
		 */
		TempTableHeaderParent (MultiEntityNode parent , Entity value) {
			this.parent = parent;
			this.value = value;
			this.evaluationList = null;
			//this.evaluationList = new ArrayList<EntityAndArguments>();
			this.currentEvaluationIndex = -1;
		}
		
		TempTableHeaderParent (MultiEntityNode parent , Entity value, List<EntityAndArguments>evaluationList) {
			this.parent = parent;
			this.value = value;
			this.setEvaluationList(evaluationList);
		}
		
		/**
		 * 
		 * @return which parent this leaf represents
		 */
		public MultiEntityNode getParent() {
			return parent;
		}
		public void setParent(MultiEntityNode parent) {
			this.parent = parent;
		}
		public Entity getValue() {
			return value;
		}
		public void setValue(Entity value) {
			this.value = value;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TempTableHeaderParent) {
				TempTableHeaderParent arg = (TempTableHeaderParent)arg0;
				if (this.parent.getName().equalsIgnoreCase(arg.parent.getName())) {
					if (this.value.getName().equalsIgnoreCase(arg.getValue().getName())) {
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
		 */
		public Entity getCurrentEvaluation() {
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
	
	protected class TempTableProbabilityCell {
		protected Entity possibleValue = null;
		protected AProbabilityValue probability = null;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return possibleValue + " = " + probability;
		}
		/**
		 * Represents a simple entry at a temporaly table representation (the
		 * value and its probability pair)
		 * @param possibleValue
		 * @param probability
		 */
		TempTableProbabilityCell (Entity possibleValue , AProbabilityValue probability) {
			this.possibleValue = possibleValue;
			this.probability = probability;
		}
		public Entity getPossibleValue() {
			return possibleValue;
		}
		public void setPossibleValue(Entity possibleValue) {
			this.possibleValue = possibleValue;
		}
		public String getProbabilityValue() throws InvalidProbabilityRangeException {
			return probability.getValue();
		}
		public AProbabilityValue getProbability() {
			return probability;
		}
		public void setProbability(AProbabilityValue probability) {
			this.probability = probability;
		}		
	}
	/** This is a node in a tree representing expressions for probability values */
	protected abstract class AProbabilityValue {
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			try {
				return getValue();
			} catch (InvalidProbabilityRangeException e) {
				return super.toString();
			}
		}
		/**
		 * 
		 * @return: a compiled expression representing the probability (e.g. a number between [0,1] or an expression in CPS)
		 */
		public abstract String getValue() throws InvalidProbabilityRangeException;
		
		/**
		 * 
		 * true if the probability is fixed or can be solved now. False if probability is
		 * dynamic (values changes depending on SSBN configuration) and cannot be solved now.
		 */
		public abstract boolean isSolvableNow();
	}
	/**Represents a simple float value for a probability*/
	protected class SimpleProbabilityValue extends AProbabilityValue {
		protected float value = Float.NaN;
		/**
		 * Represents a simple float value for a probability
		 * @param value
		 */
		SimpleProbabilityValue (float value) {
			this.value = value;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			return String.valueOf(this.value);
		}
		public boolean isSolvableNow() {return true;}
	}
	/**Represents a constant number which shall not be translated (e.g., "e" shall be passed to CPS as "e", not 2.71....)*/
	protected class UntranslatedConstantProbabilityValue extends AProbabilityValue {
		protected String value = "NaN";
		/**
		 * Represents a constant number which shall not be
		 * translated (e.g., "e" shall be passed to CPS as "e", not 2.71....)
		 * @param value
		 */
		UntranslatedConstantProbabilityValue (String value) {
			this.value = value;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			return this.value;
		}
		public boolean isSolvableNow() {return true;}
	}
	/** Represents binary operation expression */
	protected abstract class MathOperationProbabilityValue extends AProbabilityValue {
		protected AProbabilityValue op1 = null;
		protected AProbabilityValue op2 = null;
		
		
		
		public abstract String getValue() throws InvalidProbabilityRangeException;
		

	}
	
	protected class AddOperationProbabilityValue extends MathOperationProbabilityValue {
		AddOperationProbabilityValue(AProbabilityValue op1 , AProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				if (this.isSolvableNow()) {
					// solve expression if possible
					return String.valueOf(Float.parseFloat(this.op1.getValue())  +  Float.parseFloat(this.op2.getValue()));
				}
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {}
			
			return "(" + this.op1.getValue() + " + " + this.op2.getValue() + ")";
		}
		public boolean isSolvableNow() {
			return this.op1.isSolvableNow() && this.op2.isSolvableNow();
		}		
	}
	
	protected class SubtractOperationProbabilityValue extends MathOperationProbabilityValue {
		SubtractOperationProbabilityValue(AProbabilityValue op1 , AProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				if (this.isSolvableNow()) {
					// solve expression if possible
					return String.valueOf(Float.parseFloat(this.op1.getValue())  -  Float.parseFloat(this.op2.getValue()));
				}
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {}
			return "(" + this.op1.getValue() + " - " + this.op2.getValue() + ")";
		}
		public boolean isSolvableNow() {
			return this.op1.isSolvableNow() && this.op2.isSolvableNow();
		}
	}
	
	protected class MultiplyOperationProbabilityValue extends MathOperationProbabilityValue {
		MultiplyOperationProbabilityValue(AProbabilityValue op1 , AProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				if (this.isSolvableNow()) {
					// solve expression if possible
					return String.valueOf(Float.parseFloat(this.op1.getValue())  *  Float.parseFloat(this.op2.getValue()));
				}
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {}
			return "(" + this.op1.getValue() + " * " + this.op2.getValue() + ")";
		}	
		public boolean isSolvableNow() {
			return this.op1.isSolvableNow() && this.op2.isSolvableNow();
		}
	}
	
	protected class DivideOperationProbabilityValue extends MathOperationProbabilityValue {
		DivideOperationProbabilityValue(AProbabilityValue op1 , AProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				if (this.isSolvableNow()) {
					// solve expression if possible
					return String.valueOf(Float.parseFloat(this.op1.getValue())  /  Float.parseFloat(this.op2.getValue()));
				}
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {}
			return "(" + this.op1.getValue() + " / " + this.op2.getValue() + ")";
		}		
		public boolean isSolvableNow() {
			return this.op1.isSolvableNow() && this.op2.isSolvableNow();
		}
	}
	
	protected class NegativeOperationProbabilityValue extends MathOperationProbabilityValue {
		NegativeOperationProbabilityValue(AProbabilityValue op1) {
			this.op1 = op1;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			try {
				if (this.isSolvableNow()) {
					// solve expression if possible
					return String.valueOf(- Float.parseFloat(this.op1.getValue()));
				}
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {}
			return "(-" + this.op1.getValue() + ")";
		}
		public boolean isSolvableNow() {
			return this.op1.isSolvableNow();
		}
	}
	
	protected class CardinalityProbabilityValue extends AProbabilityValue {
		//protected float value = Float.NaN;
		protected String varName = null;		
		//protected SSBNNode thisNode = null;
		
		protected TempTableHeaderCell currentHeader = null;
		
		/**
		 * Represents a probability value from cardinality function
		 * It calculates the value using thisNode's parents set
		 * @param currentHeader: its currentParentSetMap should contain all considered parents at
		 * that point, mapped by resident's name and each elements should be lists of nodes
		 * containing SAME strong OV instances (e.g. ST0)
		 */
		CardinalityProbabilityValue (TempTableHeaderCell currentHeader, String varName) {
			this.currentHeader = currentHeader;
			this.varName= varName;
		}

		public String getValue() throws InvalidProbabilityRangeException {
			
			if (!this.isSolvableNow()) {
				return null;
			}
			
			// look for the upper if clauses which has matching varsetname
			TempTableHeaderCell matchingHeader = this.currentHeader;
			while (matchingHeader != null) {
				if ((matchingHeader.constraintMap!= null)
						&& matchingHeader.constraintMap.keySet().contains(this.varName)) {
					break;
				}
				try{
					matchingHeader = (TempTableHeaderCell)matchingHeader.getUpperClause();
				} catch (ClassCastException e) {
					// we found a container other than TempTableHeaderCell
					// probably, it is a TempTable
					// so, there was no perfect match for varsetname...
					return "0";
				}
			}
			if (matchingHeader == null) {
				return "0";
			}
			// if we reach this code, we found a perfect match for varsetname
			// count the number of instances of OVs in parents
			Set<String> ovInstanceNames = new HashSet<String>();
			for (SSBNNode parent : getSSBNNode().getParents()) {
				if (parent == null) {
					continue;
				}
				for (OVInstance ovi : parent.getArguments()) {
					if (ovi == null) {
						continue;
					}
					if (ovi.getOv().getName().equalsIgnoreCase(this.varName)) {
						ovInstanceNames.add(ovi.getEntity().getInstanceName());
					}
				}
			}
			return ""+ovInstanceNames.size();
		}

		/**
		 * @return the varName
		 */
		public String getVarName() {
			return varName;
		}

		/**
		 * @param varName the varName to set
		 */
		public void setVarName(String varName) {
			this.varName = varName;
		}

		/**
		 * @return the currentHeader
		 */
		public TempTableHeaderCell getCurrentHeader() {
			return currentHeader;
		}

		/**
		 * @param currentHeader the currentHeader to set
		 */
		public void setCurrentHeader(TempTableHeaderCell currentHeader) {
			this.currentHeader = currentHeader;
		}

		public boolean isSolvableNow() {
			return getSSBNNode() != null && getSSBNNode().getParents() != null && this.currentHeader != null;
		}
	}
	
	protected class EntityCardinalityProbabilityValue extends CardinalityProbabilityValue {
		//protected float value = Float.NaN;
		protected ObjectEntity entity = null;		
		//protected SSBNNode thisNode = null;
		
		protected TempTableHeaderCell currentHeader = null;
		
		/**
		 * Represents a probability value from entityCARD function
		 * It must calculate the number of instances of the given entity.
		 * @param currentHeader: the header (if-clause in LPD) where this cardinality function belong
		 */
		EntityCardinalityProbabilityValue (TempTableHeaderCell currentHeader, ObjectEntity entity) {
			super(currentHeader, entity.getName());
			this.currentHeader = currentHeader;
			this.entity = entity;
		}

		public String getValue() throws InvalidProbabilityRangeException {
			try {
				// extract number of instances.
				// TODO extract instance using reasoner instead of the object entity container, or make sure container and reasoner are syncrhonized
				return String.valueOf(this.entity.getInstanceList().size());
			} catch (Exception e) {
				try {
					Debug.println(getClass(), e.getMessage(), e);
				}catch (Throwable t) {
					e.printStackTrace();
					t.printStackTrace();
				}
			}
			return "0";
		}
		public boolean isSolvableNow() {
			return this.entity != null && this.entity.getInstanceList() != null;
		}
	}
	
	/**
	 * This class represents the combining rule Mean
	 * @author Shou Matsumoto
	 *
	 */
	protected class MeanCombiningRuleProbabilityValue extends ParentAwareCombiningRuleProbabilityValue {
		public MeanCombiningRuleProbabilityValue(TempTableHeaderCell currentHeader, String nodeName) {
			super(currentHeader, nodeName, " + ");
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.medg.compiler.MultiEntityUtilityFunctionCompiler.SumCombiningRuleProbabilityValue#getPrefixValueForEachParent(java.util.Set)
		 */
		protected String getPrefixValueForEachParent(Set<SSBNNode> parents) {
			return (1.0/parents.size()) + "*" ;
		}
		
		
		
	}
	
	/**
	 * This class represents the combining rule Sum
	 * @author Shou Matsumoto
	 *
	 */
	protected class ParentAwareCombiningRuleProbabilityValue extends AProbabilityValue {

		private TempTableHeaderCell currentHeader;
		private String nodeName;
		private String operationPerParent = " + ";

		/**
		 * This class represents the combining rule Sum
		 * @param currentHeader
		 * @param nodeName
		 * @param operationPerParent : see {@link #setOperationPerParent(String)}
		 */
		public ParentAwareCombiningRuleProbabilityValue(TempTableHeaderCell currentHeader, String nodeName, String operationPerParent) {
			this.currentHeader = currentHeader;
			this.nodeName = nodeName;
			this.operationPerParent = operationPerParent;
		}
		
//		/**
//		 * This class represents the combining rule Sum
//		 * @param currentHeader
//		 * @param nodeName
//		 */
//		public ParentAwareCombiningRuleProbabilityValue(TempTableHeaderCell currentHeader, String nodeName) {
//			this(currentHeader, nodeName, " + ");
//		}


		public String getValue() throws InvalidProbabilityRangeException {
			if ((getSSBNNode() == null) 
					|| (getSSBNNode().getParents() == null)
					|| (getSSBNNode().getParents().isEmpty())) {
				return "";
			}
			String ret = "(";
			// extract parents from name
			Set<SSBNNode> parents = new HashSet<SSBNNode>();
			for (SSBNNode node : getSSBNNode().getParents()) {
				if (node.getResident().getName().equalsIgnoreCase(getNodeName())) {
					parents.add(node);
				}
			}
			if (parents.isEmpty()) {
				try {
					Debug.println(getNodeName() + " was not found as a parent");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return "0";		// return 0 as default
			}
			for (SSBNNode parent : parents) {
				ret += (this.getPrefixValueForEachParent(parents) + parent.getProbNode().getName() + this.getOperationPerParent());
			}
			ret = ret.substring(0, ret.lastIndexOf(this.getOperationPerParent())); // remove last +
			return ret + ")";
		}

		/**
		 * This string is added before each ocurrence of parent in {@link #getValue()}
		 * @param parents
		 * @return
		 */
		protected String getPrefixValueForEachParent(Set<SSBNNode> parents) {
			return "";
		}

		/**
		 * @return the currentHeader
		 */
		public TempTableHeaderCell getCurrentHeader() {
			return currentHeader;
		}

		/**
		 * @return the nodeName
		 */
		public String getNodeName() {
			return nodeName;
		}

		/**
		 * This is the operation to be performed between parents (e.g. "+", "*") 
		 * @return the operationPerParent
		 * @see #getValue()
		 */
		public String getOperationPerParent() {
			return operationPerParent;
		}

		/**
		 * This is the operation to be performed between parents (e.g. " + ", " * ") 
		 * @param operationPerParent the operationPerParent to set
		 * @see #getValue()
		 */
		public void setOperationPerParent(String operationPerParent) {
			this.operationPerParent = operationPerParent;
		}

		/**
		 * @param currentHeader the currentHeader to set
		 */
		public void setCurrentHeader(TempTableHeaderCell currentHeader) {
			this.currentHeader = currentHeader;
		}

		/**
		 * @param nodeName the nodeName to set
		 */
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.medg.compiler.MultiEntityUtilityFunctionCompiler.AProbabilityValue#toString()
		 */
		public String toString() {
			try {
				String ret = "(";
				// extract parents from name
				Set<SSBNNode> parents = new HashSet<SSBNNode>();
				ret += (this.getPrefixValueForEachParent(parents) +getNodeName() + this.getOperationPerParent());
				return ret + ")";
			} catch (Exception e) {
				// TODO: handle exception
			}
			return super.toString();
		}

		public boolean isSolvableNow() {
			return false;
		}
		
	}
	
	/**
	 * This class represents functions which do not have to be "translated"
	 * (i.e. no semantics should be considered, and the name of the function should literaly be used in the output).
	 * These functions include:
	 * "NormalDist", "Log", "Power", "Exp", "Root"
	 * @author Shou Matsumoto
	 *
	 */
	protected class FunctionExpressionProbabilityValue extends AProbabilityValue {
		private  TempTableHeaderCell currentHeader;
		private String functionName;
		private List<AProbabilityValue> arguments;

		/**
		 * This class represents functions which do not have to be "translated"
		 * (i.e. no semantics should be considered, and the name of the function should literaly be used in the output).
		 * These functions include:
		 * "NormalDist", "Log", "Power", "Exp", "Root"
		 * @param currentHeader : header (if-clause) where this function belongs
		 * @param arguments : arguments of the function
		 * @param functionName : name of the function
		 */
		public FunctionExpressionProbabilityValue (TempTableHeaderCell currentHeader, String functionName, List<AProbabilityValue> arguments) {
			this.currentHeader = currentHeader;
			this.functionName = functionName;
			this.arguments = arguments;
		}

		public String getValue() throws InvalidProbabilityRangeException {
			String ret = this.getFunctionName() + "(";
			for (AProbabilityValue arg : this.getArguments()) {
				ret += (arg.getValue() + ", ");
			}
			ret = ret.substring(0, ret.lastIndexOf(", "));	// remove last comma
			return ret + ")";
		}

		/**
		 * @return the currentHeader
		 */
		public TempTableHeaderCell getCurrentHeader() {
			return currentHeader;
		}

		/**
		 * @return the functionName
		 */
		public String getFunctionName() {
			return functionName;
		}

		/**
		 * @return the arguments
		 */
		public List<AProbabilityValue> getArguments() {
			return arguments;
		}

		public boolean isSolvableNow() {
			return false;
		}
	}
	
	protected class NodeCardinalityProbabilityValue extends CardinalityProbabilityValue {
		
		
		/**
		 * Represents a probability value from entityCARD function
		 * It must calculate the number of instances of the given entity.
		 * @param currentHeader: the header (if-clause in LPD) where this cardinality function belong
		 */
		NodeCardinalityProbabilityValue (TempTableHeaderCell currentHeader, String nodeName) {
			super(currentHeader, nodeName);
		}

		public String getValue() throws InvalidProbabilityRangeException {
			if (!this.isSolvableNow()) {
				return null;
			}
			int counter = 0;
			for (SSBNNode parent : getSSBNNode().getParents()) {
				if (parent.getResident().getName().equalsIgnoreCase(this.getVarName())) {
					counter++;
				}
			}
			return ""+counter;
		}
	}
	
	
	protected class ComparisionProbabilityValue extends AProbabilityValue {
		protected AProbabilityValue arg0 = null;
		protected AProbabilityValue arg1 = null;
		protected boolean isMax = false;
		/**
		 * Represents a comparision function between two values (MAX or MIN)
		 * @param arg0
		 * @param arg1
		 * @param isMax true if it represents a MAX function. If false, it represents a MIN
		 * function.
		 */
		ComparisionProbabilityValue (AProbabilityValue arg0, AProbabilityValue arg1, boolean isMax) {
			this.arg0 = arg0;
			this.arg1 = arg1;
			this.isMax = isMax;
		}
		public String getValue() throws InvalidProbabilityRangeException {
			String arg0Value = this.arg0.getValue();
			String arg1Value = this.arg1.getValue();
			try {
				if (this.isSolvableNow()) {
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
					if (this.isMax == (Float.parseFloat(arg0Value) > Float.parseFloat(arg1Value))) {				
						return arg0Value;
					} else {
						return arg1Value;
					}
				}
			} catch (NumberFormatException e) {}
			
			// if any of the arguments are unsolvable, then they are uncomparable now
			try {
				Debug.println(getClass(), "[WARNING] " + this + " is not a solvable expression.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return this.isMax?"MAX(":"MIN(" + arg0Value + ", " + arg1Value + ")";
		}
		public boolean isSolvableNow() {
			return this.arg0.isSolvableNow() && this.arg1.isSolvableNow();
		}
		


	}

	protected class EntityAndArguments {
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
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		MultiEntityUtilityFunctionCompiler.resource = resource;
	}

	
	/**
	 * @return the easiest way to to extract the script from the returned value is to call {@link Object#toString()}. An
	 * alternative way is to call {@link IProbabilityFunction#getVariableAt(int)} and then {@link INode#name}.
	 * @see unbbayes.prs.mebn.compiler.Compiler#generateLPD(unbbayes.prs.mebn.ssbn.SSBNNode)
	 */
	public IProbabilityFunction generateLPD(SSBNNode ssbnnode) throws MEBNException {
//		System.gc();
		if (ssbnnode == null) {
			return null;
		}
		
		// check content of cache
		IProbabilityFunction cachedLPD = this.getCachedLPD(ssbnnode);
		if (cachedLPD != null) {
			return cachedLPD;
		}
		
		this.init(ssbnnode);	// this will also set up the value returned by getSSBNNode()
		this.parse();

		
		// try to clean garbage before starting an expensive method like this one
//		System.gc();
		
		if (this.getTempTable() == null) {
			// the parse has failed
			try {
				Debug.println(getClass(), "Parse has failed");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return null;
		}
		
		
		if (this.getSSBNNode().getProbNode() == null) {
			return null;
		}
		if (this.text == null || this.tempTable.isEmptyNestedClauses() ) {
			// Special condition: if pseudocode was not declared, use linear (equal) distribution instead
			this.generateLinearDistroCPT(this.getSSBNNode().getProbNode());
			this.putCPTToCache(ssbnnode, this.getSSBNNode().getProbNode().getProbabilityFunction());
			return this.getSSBNNode().getProbNode().getProbabilityFunction();
		}
		
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
				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		// eliminates redundancies on table's boolean expression
		try {
			this.tempTable.cleanUpKnownValues(this.getSSBNNode());
		} catch (NullPointerException e) {
			// The SSBNNode was null...
			throw new InstanceException(e);
		}
		
		// extracting base values
		
		ArrayList<SSBNNode> parents = null;
		try {
			parents = new ArrayList<SSBNNode>(this.getSSBNNode().getParents());
		} catch (NullPointerException e) {
			parents = new ArrayList<SSBNNode>();
		} catch (Exception e) {
			throw new InconsistentTableSemanticsException(e);
		}
		
		
		Map<String, List<EntityAndArguments>> map = null; // parameter of boolean expression evaluation method
		
		// this iterators helps us combine parents' possible values
		// e.g. (True,Alpha), (True,Beta), (False,Alpha), (False,Beta).
		List<Iterator<Entity>> valueCombinationIterators = new ArrayList<Iterator<Entity>>();
		for (SSBNNode parentNode : parents) {
			if (parentNode.getActualValues() == null || parentNode.getActualValues().isEmpty() 
					|| (parentNode.getResident() instanceof MultiEntityDecisionNode)) {	// don't consider "states" of parent continuous nodes
				// add a dummy value just to ensure index are synchronized
				List<Entity> temp = new ArrayList<Entity>();
				temp.add(new NullEntity());
				valueCombinationIterators.add(temp.iterator());
			} else {
				valueCombinationIterators.add(parentNode.getActualValues().iterator());
			}
		}
		
		// saves the current values of the iterators
		List<Entity> currentIteratorValue = new ArrayList<Entity>();
		for (Iterator it : valueCombinationIterators) {
			currentIteratorValue.add((Entity)it.next());
		}
		
		// initialize cps to something like "defineState( Continuous ,1, 2, 3, 4, 5, 6);p( C0 | C1 ) = "
		String cps = " defineState( ";
		if (this.getNode() instanceof MultiEntityDecisionNode) {
			cps += "Continuous";
			if (isToAddStatesToContinuousResidentNodes()) {
				for (int i = 0 ; i < getSSBNNode().getProbNode().getStatesSize(); i++ ) {
					cps += ", ";
					cps += getSSBNNode().getProbNode().getStateAt(i);
				}	
			}
		} else {
			cps += "Discrete";
			for (int i = 0 ; i < getSSBNNode().getProbNode().getStatesSize(); i++ ) {
				cps += ", ";
				cps += getSSBNNode().getProbNode().getStateAt(i);
			}
		}
		
	 	cps += ");p( " + getSSBNNode().getProbNode().getName();
	 	if (getSSBNNode().getProbNode().getParentNodes() != null 
	 			&& !getSSBNNode().getProbNode().getParentNodes().isEmpty()) {
	 		// add condition nodes if exist
	 		cps += " | ";
	 		for (INode parent : getSSBNNode().getProbNode().getParentNodes()) {
	 			cps += parent.getName() + " , ";
	 		}
	 		cps = cps.substring(0, cps.lastIndexOf(" , "));	// remove last comma
	 	}
	 	
	 	cps += " ) = ";
	 	
		// start running at the probabilistic table and filling its cells
		//List<Entity> entityList = null;
//		for( int i = 0; i < this.cpt.tableSize(); i += this.getSSBNNode().getProbNode().getStatesSize()) {
	 	boolean isLastCombination = true;	// indicates that the valueCombinationIterators has hasNext() == false for all contents. if set to false, the loop ends
//	 	try {
//			System.gc();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
		boolean isItTheFisrtIf = true;
	 	do {
			//	clears and initializes map
			map = new HashMap<String, List<EntityAndArguments>>();
			for (SSBNNode node : parents) {
			  if (!map.containsKey(node.getResident().getName())) {
				  map.put(node.getResident().getName(), new ArrayList<EntityAndArguments>());
			  }
			}
			
			
			// fill cps with something like "if( P1 == true && P2 == true )"
			String cpsIfClause = "";	// content of if-clause. If there is no discrete parent, this content is empty
			// fill map at this loop. Note that parents.size, currentIteratorValue.size, and valueCombinationiterators are the same
			for (int j = 0; j < parents.size(); j++) {
				SSBNNode parent = parents.get(j);
				ResidentNode parentResident = parent.getResident();
				// ignore continuous parent
				if (!(parentResident instanceof MultiEntityDecisionNode)) {
					Entity val = currentIteratorValue.get(j);
					if (cpsIfClause.length() > 0) {
						// there are other boolean expressions. Combine using &&
						cpsIfClause += " && ";
					}
					cpsIfClause += parent.getProbNode().getName() + " == " + val.getName();
					map.get(parentResident.getName()).add(new EntityAndArguments(val,new ArrayList<OVInstance>(parent.getArguments())));
				}
			}
			if (cpsIfClause.length() > 0) {
				// only start if-clause if there are discrete parents
				if (!isItTheFisrtIf) {
					cps += "else ";
				}
				cps += "if( " + cpsIfClause + ")";
				isItTheFisrtIf = false;
			}
			
			isLastCombination = true;	// reset flag before testing conditions
			
			// updates iterators and check if this is the last combination
			for (int j = 0; j < valueCombinationIterators.size(); j++) {
				if (valueCombinationIterators.get(j).hasNext()) {
					// if has next, then update current value and exits loop
					currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
					isLastCombination = false;
					break;
				} else {
					// else, reset the iterator (and current value) until exit loop
					if (!(parents.get(j).getResident() instanceof MultiEntityDecisionNode)) { // ignore continuous nodes
						valueCombinationIterators.set(j, parents.get(j).getActualValues().iterator());
						if (valueCombinationIterators.get(j).hasNext()) {
							currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
						}
					}
				}
			}
			
				
			// prepare to extract which column to verify
			TempTableHeaderCell header = null;
			
			// if default distro, then use the default header...
			// also, if no parents are declared, use default distro...
			boolean thereAreNoParents = false;
			if(this.getSSBNNode().getParents() == null) {
				thereAreNoParents = true;
			} else if (this.getSSBNNode().getParents().size() == 0) {
				thereAreNoParents = true;
			}
			if (thereAreNoParents || this.getSSBNNode().isUsingDefaultCPT()) {
				// we assume the default distro is the last block on pseudocode
				header = this.tempTable.getDefaultClause();
				// let's just check if this header is really declaring a default distro... Just in case...
				if (!header.isDefault) {
					throw new InconsistentTableSemanticsException();
				}
			} else {
				//	if not default, look for the column to verify
				// the first expression to return true is the one we want
//				long time = new Date().getTime();
				header = this.tempTable.getFirstTrueClause(map);
				// note that default (else) expression should allways return true!
				
//				System.out.println(header + ": took " + (new Date().getTime() - time) + " in loop " + loop++ + " for values " + map);
			}
			
			
			
			// the "{" is only necessary if there is any if clause
			if (cpsIfClause.length() > 0) {
				cps += "{ ";
			}
			// fill cps with something like "{NormalDist( 5,1 ) } " or "{ true:1.0; false:0.0; }"
			ArrayList<Entity> possibleValues = new ArrayList<Entity>(this.getSSBNNode().getActualValues());
			if ((this.getNode() instanceof MultiEntityDecisionNode) 
					|| possibleValues == null 
					|| possibleValues.size() <= 0) {
				// this should be a continuous expression, like "{NormalDist( 5,1 ) } "
				// trace how many cells we have (we are expecting only 1)
				if (header.getCellList() == null || header.getCellList().size() <= 0) {
					throw new MEBNException("Temporary table " + header + " has no cell. This is either a wrong script or compiler bug.");
				} else if (header.getCellList().size() > 1) {
					try {
						Debug.println(getClass(), getSSBNNode() + " is supposed to be continuous, but we found states: " + header.getCellList());
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				// use the first possible value and concatenate as-is
				cps += header.getCellList().get(0).getProbabilityValue() + ";";
			} else {
				// this is a discrete expression, like "{ true:1.0; false:0.0; }"
				// extract the value to set
				for (TempTableProbabilityCell cell : header.getCellList()) {
					if (cell.getPossibleValue() != null) {
						cps += cell.getPossibleValue().getName() + ":" + cell.getProbabilityValue() + ";";
					} else {
						cps += cell.getProbabilityValue() + ";";
						try {
							Debug.println(getClass(), "The header " + header + " contains a cell with no possible value: " + cell);
						} catch (Throwable t) {
							t.printStackTrace();
						}
						break;
					}
				}
			}
			// the "}" is only necessary if there is any if clause
			if (cpsIfClause.length() > 0) {
				cps += " } ";
			}
			
		} while (!isLastCombination);
		
		
		// rollback MFrag settings
		for (SSBNNode parent : parentToPreviousMFragMap.keySet()) {
			try{
				parent.turnArgumentsForMFrag(parentToPreviousMFragMap.get(parent));
			} catch (Exception e) {
				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		ScriptProbabilityFunction ret = new ScriptProbabilityFunction(cps);
		this.putCPTToCache(ssbnnode, ret);	// cache this value, so that next call will not re-calculate value
		return ret;
	}
	
	/**
	 * This is just a holder class for a CPS (string script).
	 * The cps can be accessed by calling {@link #getName()}, {@link #getDescription()} or
	 * {@link #getStateAt(int)}
	 * @author Shou Matsumoto
	 *
	 */
	public class CPSHolder implements INode {
		private String cps;
		public CPSHolder(String cps) {this.cps = cps;}
		public void setStates(List<String> arg0) {}
		public void setStateAt(String arg0, int arg1) {}
		public void setParentNodes(List<INode> arg0) {}
		public void setName(String arg0) {cps = arg0;}
		public void setDescription(String arg0) {cps = arg0;}
		public void setChildNodes(List<INode> arg0) {}
		public void removeStateAt(int arg0) {}
		public void removeParentNode(INode arg0) {}
		public void removeLastState() {}
		public void removeChildNode(INode arg0) {}
		public int getType() {return -1;}
		public int getStatesSize() {return 0;}
		public String getStateAt(int arg0) {return cps;}
		public List<INode> getParentNodes() {return null;}
		public String getName() {return cps;}
		public String getDescription() {return cps;}
		public List<INode> getChildNodes() {return null;}
		public List<INode> getAdjacentNodes() {return null;}
		public void appendState(String arg0) {cps += arg0;}
		public void addParentNode(INode arg0) throws InvalidParentException {throw new InvalidParentException(cps);}
		public void addChildNode(INode arg0) throws InvalidParentException {throw new InvalidParentException(cps);}
		public String getCps() {return cps;}
		public void setCps(String cps) {this.cps = cps;}
	}
	
	/**
	 * This is a probability function represented by a script (string) instead of a table.
	 * It uses {@link CPSHolder} (which is a node) to represent a script (the script can be
	 * accessed by accessing its name, description or state).
	 * The {@link CPSHolder} can be accessed by calling #getVariableAt(int) for any argument, and
	 * changed by calling #setVariableAt(int, INode) (for any int) or #addVariable(INode)
	 * @author Shou Matsumoto
	 *
	 */
	public class ScriptProbabilityFunction extends PotentialTable implements IProbabilityFunction {
		private CPSHolder cpsHolder;
		public ScriptProbabilityFunction(String script) {
			this.setCpsHolder(new CPSHolder(script));
		}
		public void addVariable(INode arg0) {
			this.setCpsHolder((CPSHolder)arg0);
		}
		public INode getVariableAt(int arg0) {return this.getCpsHolder();}
		public void notifyModification() {}
		public void removeVariable(INode arg0) {}
		public void removeVariable(INode arg0, boolean arg1) {}
		public void setVariableAt(int arg0, INode arg1) {this.addVariable(arg1);};
		public int variableCount() {return 0;}
		public String getScript() {return cpsHolder.getCps();}
		public void setScript(String script) {this.cpsHolder.setCps(script);}
		public CPSHolder getCpsHolder() {return cpsHolder;}
		public void setCpsHolder(CPSHolder cpsHolder) {this.cpsHolder = cpsHolder;}
		public PotentialTable newInstance() {return new ScriptProbabilityFunction(this.getScript());}
		public String toString() {try {return this.getScript();}catch (Exception e) {e.printStackTrace();} return super.toString();}
		public PotentialTable getTemporaryClone() {
			return (PotentialTable) clone();
		}
		public void purgeVariable(INode variable, boolean normalize) {
			removeVariable(variable, normalize);
		}
		
	}

	/**
	 * @return the look
	 */
	protected char getLook() {
		return look;
	}

	/**
	 * @param look the look to set
	 */
	protected void setLook(char look) {
		this.look = look;
	}


	/**
	 * @param index the index to set
	 */
	protected void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the text
	 */
	protected char[] getText() {
		return text;
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
		return kwlist;
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
		return kwcode;
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
		return token;
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
		return value;
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
		return noCaseChangeValue;
	}

	/**
	 * @param noCaseChangeValue the noCaseChangeValue to set
	 */
	protected void setNoCaseChangeValue(String noCaseChangeValue) {
		this.noCaseChangeValue = noCaseChangeValue;
	}

	/**
	 * @return the tempTable
	 */
	protected TempTable getTempTable() {
		return tempTable;
	}

	/**
	 * @param tempTable the tempTable to set
	 */
	protected void setTempTable(TempTable tempTable) {
		this.tempTable = tempTable;
	}

	/**
	 * @return the currentHeader
	 */
	protected TempTableHeaderCell getCurrentHeader() {
		return currentHeader;
	}

	/**
	 * @param currentHeader the currentHeader to set
	 */
	protected void setCurrentHeader(TempTableHeaderCell currentHeader) {
		this.currentHeader = currentHeader;
	}

	/**
	 * @return the originalTextLength
	 */
	protected int getOriginalTextLength() {
		return originalTextLength;
	}

	/**
	 * @param originalTextLength the originalTextLength to set
	 */
	protected void setOriginalTextLength(int originalTextLength) {
		this.originalTextLength = originalTextLength;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 
	 * @deprecated use {@link #generateLPD(SSBNNode)} instead
	 */
	public PotentialTable generateCPT(SSBNNode ssbnnode) throws MEBNException {
		try {
			return (PotentialTable)this.generateLPD(ssbnnode);
		} catch (ClassCastException e) {
			throw new RuntimeException(this.getClass() + "#generateCPT(SSBNNode) cannot be used anymore. Use #generateLPD(SSBNNode) instead", e);
		}
	}

	/**
	 * If true, the {@link #generateLPD(SSBNNode)} method will call
	 * {@link CPS#setScript(Node, String)}.
	 * Set this to false if you want {@link #generateLPD(SSBNNode)
	 * to automatically change the content of CPS.
	 * @return the isToFillCPSNow
	 */
	public boolean isToFillCPSNow() {
		return isToFillCPSNow;
	}

	/**
	 * If true, the {@link #generateLPD(SSBNNode)} method will call
	 * {@link CPS#setScript(Node, String)}.
	 * Set this to false if you want {@link #generateLPD(SSBNNode)
	 * to automatically change the content of CPS.
	 * @param isToFillCPSNow the isToFillCPSNow to set
	 */
	public void setToFillCPSNow(boolean isToFillCPSNow) {
		this.isToFillCPSNow = isToFillCPSNow;
	}
	

	/**
	 * This is a null object (object representing null) for an entity.
	 * @author Shou Matsumoto
	 *
	 */
	public class NullEntity extends Entity {
		
		/**
		 * Not private in order to allow inheritance
		 */
		protected NullEntity() {
			super("null", TypeContainer.typeCategoryLabel);
		}
		
	}
	
}
