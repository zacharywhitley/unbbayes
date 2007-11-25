﻿package unbbayes.prs.mebn.compiler;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.Debug;
import unbbayes.util.NodeList;

/*
 BNF MEBN Table:
 ----------------
 Changes (Date/Month/Year): 
 
 	25/11/2007:
 			Description: added non-terminal variable "possibleVal" to the grammar (and its implementation).
 			Author: Shou Matsumoto
 	
 	07/10/2007:
 			Description: "varsetname" has been added to the grammar (and implemented inside the class)
 				in order to allow us to declare parent set by strong OV.
 			Author: Shou Matsumoto
 	
 
 	24/06/2007:
 			Description: The top level BNF Grammar class was changed from 
 				if_statement to table, in order to make possible a probability
 				table without an if clause.
			Author: Shou Matsumoto
 
 	10/06/2007: 
 			Description: Added cardinality(), min() and max() functions
 			syntax analiser.
 			Author: Shou Matsumoto 

 	29/05/2007: 
 			Description: the else clause is now required, in order to
 				force user to declare a default distribution and
 				grant declaration of every possible conditionants
 				(if we don't add this restriction, a semantic analisis
 				would be required in order to verify if every states
 				of conditionants were provided and the last else
 				must be related to the first if everytime - since
 				we don't have a block sentence yet, it is not possible).
 			Author: Shou Matsumoto
 			

 	
 ===============================================================
 table := statement | if_statement
 if_statement 
 ::= 
 "if" allop varsetname "have" "(" b_expression ")" statement 
 	"else" else_statement 
 allop ::= "any" | "all"
 varsetname ::= ident ["." ident]*
 b_expression ::= b_term [ "|" b_term ]*
 b_term ::= not_factor [ "&" not_factor ]*
 not_factor ::= [ "~" ] b_factor
 b_factor ::= ident "=" ident
 else_statement ::= statement | if_statement
 statement ::= "[" assignment "]" 
 assignment ::= ident "=" expression [ "," assignment ]*
 expression ::= term [ addop term ]*
 term ::= signed_factor [ mulop factor ]*
 signed_factor ::= [ addop ] factor
 factor ::= number | function | "(" expression ")" 
 	| simplefunction "(" expression ")"
 	| biargfunction "(" expression ; expression ")"
 function ::= possibleVal 
 	| "CARDINALITY" "(" ident ")"
 	| "MIN" "(" expression ; expression ")"
 	| "MAX" "(" expression ; expression ")"
 possibleVal ::= ident
 addop ::= "+" | "-"
 mulop ::= "*" | "/"
 ident ::= letter [ letter | digit ]*
 number ::= [digit]+
 ================================================================
 */



public class Compiler implements ICompiler {

	// resource files
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.compiler.resources.Resources");

	
	/* A previously read character (lookahead) */
	private char look;

	/* Current text cursor position (where inside "text" we're scanning now) */
	private int index = 0;

	private char[] text;

	/* keywords */
	private String kwlist[] = { "IF", "ELSE", "ALL", "ANY", "HAVE" };

	/*
	 * Special codes for keywords
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
	private MultiEntityBayesianNetwork mebn = null;
	private DomainResidentNode node = null;
	

	// Variables used for ProbabilisticTable generation
	private PotentialTable cpt = null;
	private SSBNNode ssbnnode = null;
	
	/*
	 * This temporaly represents a parsed CPT, to make potential table generation easier, like:
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
	private List<TempTableHeaderCell> tempTable = null;
	private TempTableHeaderCell currentHeader = null;
	//private List<TempTableProbabilityCell> currentProbCellList = null;
	//private TempTableProbabilityCell currentCell = null;
	
	private Compiler() {
		tempTable = new ArrayList<TempTableHeaderCell>();
	}
	
	public Compiler (DomainResidentNode node) {
		super();
		this.setNode(node);
		this.cpt = null;
		tempTable = new ArrayList<TempTableHeaderCell>();
	}
	
	
	/**
	 * TODO break this class apart, because it's becoming too huge.
	 * @param node: the node having the CPT's pseudocode being evaluated by this class.
	 * @param ssbnnode: the node where we should set the output CPT to.
	 */
	public Compiler (DomainResidentNode node, SSBNNode ssbnnode) {
		super();
		this.setNode(node);
		this.ssbnnode = ssbnnode;
		if (this.ssbnnode != null) {
			if (this.ssbnnode.getProbNode() != null) {
				this.cpt = this.ssbnnode.getProbNode().getPotentialTable();
			}			
		}
		tempTable = new ArrayList<TempTableHeaderCell>();
	}
	
	/* Compiler's initialization */
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#init(java.lang.String)
	 */
	public void init(String text) {
		index = 0;
		Debug.println("************************************");
		Debug.println("ORIGINAL: " + text);
		text = text.replaceAll("\\s+", " ");
		Debug.println("CHANGED: " + text);
		Debug.println("************************************");
		this.text = text.toCharArray();
		nextChar();
		
		tempTable = new ArrayList<TempTableHeaderCell>();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException {
		//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>(); //Initialize lists
		Debug.println("PARSED: ");
		this.skipWhite();
		this.table();
	}
	
	public void parse(String table) throws MEBNException {
		this.init(table);
		this.parse();
	}
	
	/**
	 * 
	 * TODO implement this method
	 * @return
	 */
	public PotentialTable generateCPT() {
		// TODO implement this!!!
		return this.cpt;
	}
	
	/**
	 *  table := statement | if_statement
	 */
	private void table() throws NoDefaultDistributionDeclaredException,
	  							InvalidConditionantException,
	  							SomeStateUndeclaredException,
	  							InvalidProbabilityRangeException,
	  							TableFunctionMalformedException{
		
		if (this.look == '[') {
			Debug.println("STARTING DEFAULT STATEMENT");	
			
			// Prepare temporary table's header to declare a default (no-if-clause) statement
			this.currentHeader = new TempTableHeaderCell(null, true, true);
			this.tempTable.add(this.currentHeader);
			// if we catch a sintax error here, it may be a value error
			try {
				statement();
			} catch (TableFunctionMalformedException e) {
				// Exception translation (perharps an anti-pattern ?)
				throw new InvalidProbabilityRangeException(e.getMessage());
			}
			
		} else {
			// We don't have to prepare temporary table's header to declare a if-clause statement
			// because the if statement parser would do so.
			
			// Please note table() repasses every exception reported by ifStatement()
			this.ifStatement();
		}
	}

	/**
	 * if_statement ::= "if" allop ident "have" "(" b_expression ")" statement [
	 * "else" else_statement ]
	 * 
	 */
	private void ifStatement() throws NoDefaultDistributionDeclaredException,
									  InvalidConditionantException,
									  SomeStateUndeclaredException,
									  InvalidProbabilityRangeException,
									  TableFunctionMalformedException{
		Debug.println("PARSING IF STATEMENT");
		// SCAN FOR IF. Note that any blank spaces were already skipped
		scan();
		matchString("IF");
		
		

		// SCAN FOR ALL/ANY
		scan();
		switch (token) {
		case 'a':
			Debug.println("ALL VERIFIED");
			// sets the table header w/ this parameters (null,false,false): null verified parents, is not ANY and is not default
			this.currentHeader = new TempTableHeaderCell(null, false, false);
			break;
		case 'y':
			Debug.println("ANY VERIFIED");
			//	sets the table header w/ this parameters (null,false,false): null verified parents, is ANY and is not default
			this.currentHeader = new TempTableHeaderCell(null, true, false);
			break;
		default:
			expected("ALL or ANY");
		}

		// adds the header to table before it is changed to another header.
		this.tempTable.add(this.currentHeader);
		
		// SCAN FOR varsetname
		String varSetName = this.varsetname();
		this.currentHeader.setVarsetname(varSetName);
		Debug.println("SCANNED VARSETNAME := " + varSetName);

		// SCAN FOR HAVE
		Debug.println("SCAN FOR HAVE");
		scan();
		matchString("HAVE");

		// ( EXPECTED
		match('(');
		// if we catch sintax error here, it may be conditionant error
		try {
			bExpression();
		} catch (TableFunctionMalformedException e) {
			throw new InvalidConditionantException(e.getMessage());
		}
		//this.nextChar();
		//this.skipWhite();
		Debug.println("LOOKAHEAD = " + look);
		// ) EXPECTED
		match(')');
		
		Debug.println("STARTING STATEMENTS");
		
		// if we catch a sintax error here, it may be a value error
		try {
			statement();
		} catch (TableFunctionMalformedException e) {
			throw new InvalidProbabilityRangeException(e.getMessage());
		}
		
		
		
		Debug.println("LOOKING FOR ELSE STATEMENT");
		// LOOK FOR ELSE
		// Consistency check C09: the grammar may state else as optional,
		// but semantically every table must have a default distribution, which is
		// declared within an else clause.
		
		// We dont have to create a new temp table header, because else_statement would do so.
		
		//	This test is necessary to verify if  there is an else clause
		if (this.index < this.text.length) {
			try {
				scan();
			} catch (TableFunctionMalformedException e) {
				// a sintax error here represents a statement other than an else statement
				throw new NoDefaultDistributionDeclaredException();
			}
		} else {
			// No statement was found at all (that means no else statement).
			Debug.println("END OF TABLE");
			throw new NoDefaultDistributionDeclaredException();
		}
		
		if (token == 'l') {
			else_statement();
		} else {
			// The statement found was not an else statement
			throw new NoDefaultDistributionDeclaredException();
		}
	}
	
	/**
	 *   It skippes white spaces after evaluation.
	 *   varsetname ::= ident["."ident]*
	 */
	private String varsetname() throws TableFunctionMalformedException {
		
		// we don't have to set header's varsetname here because ifStatement (upper caller) would do so.
		
		String ret = "";	// a string containing "varsetname" (e.g. "st.sr.z")
		
		// scan for the ident
		do {
			scanNoSkip();	// no white spaces should stay between ident and "." and next ident
			if (token == 'x') {
				Debug.println("SCANING IDENTIFIER " + value);
				ret += value;
			} else {
				expected("Identifier");
			}	
			
			// search for ["." ident]* loop
			if (this.look == '.') {
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
	private void bExpression() throws InvalidConditionantException,
									  TableFunctionMalformedException{
		// TODO treat complex boolean evaluation.
		
		bTerm();
		
		// LOOK FOR OR (OPTIONAL)
		// scan();
		if (look == '|') {
			match('|');
			bTerm();
		}
		Debug.println("EXITING BEXPRESSION");
	}

	/**
	 * b_term ::= not_factor [ "&" not_factor ]*
	 * 
	 */
	private void bTerm() throws InvalidConditionantException,
							    TableFunctionMalformedException{
		// TODO treat complex boolean evaluation.
		notFactor();

		// LOOK FOR AND (OPTIONAL)
		// scan();
		if (look == '&') {
			match('&');
			notFactor();
		}
	}

	/**
	 * not_factor ::= [ "~" ] b_factor
	 * 
	 */
	private void notFactor() throws InvalidConditionantException,
									TableFunctionMalformedException{
		// SCAN FOR NOT (OPTIONAL)
		// scan();
		if (look == '~') {
			match('~');
		}

		bFactor();
		Debug.println("EXITING NOT FACTOR");
	}

	/**
	 * b_factor ::= ident "=" ident
	 * 
	 */
	private void bFactor() throws InvalidConditionantException,
								  TableFunctionMalformedException{
		
		String conditionantName = null;
		

		
		
		
		Debug.println("Parsing bFactor");
		// SCAN FOR CONDITIONANTS
		scan();
		if (token == 'x') {
			conditionantName = this.noCaseChangeValue;
			// consistency check C09: verify whether is conditionant of the node
			if (this.node != null) {
				if (!this.isValidConditionant(this.mebn, this.node, conditionantName )) {
					throw new InvalidConditionantException();
				}
			}
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e.getMessage());
			}
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR CONDITIONANTS' POSSIBLE STATES
		scan();
		
		Debug.println("SCANED FOR CONDITIONANTS' POSSIBLE STATES");
		
		if (token == 'x') {
			// consistency check C09: verify whether conditionant has valid values
			if (this.node != null) {
				if (!this.isValidConditionantValue(this.mebn,conditionantName,this.noCaseChangeValue)) {
					throw new InvalidConditionantException();
				}
			}
			
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e.getMessage());
			}
		}
		
		// if code reached here, the condicionant check is ok

		//	prepare to add current temp table's header's parent (condicionant list)
		DomainResidentNode resident = this.mebn.getDomainResidentNode(conditionantName);
		// If not found, its an error!		
		if (resident == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e.getMessage());
			}
		}
		Entity condvalue = null;
		// search for an entity with a name this.noCaseChangeValue
		for (Entity possibleValue : resident.getPossibleValueList()) {
			if (possibleValue.getName().compareTo(this.noCaseChangeValue) == 0) {
				condvalue = possibleValue;
				break;
			}
		}
		// If not found, its an error!		
		if (condvalue == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e.getMessage());
			}
		}
		// Set temp table's header condicionant
		TempTableHeaderParent headerParent = new TempTableHeaderParent(resident, condvalue);
		// TODO optimize above code, because its highly redundant (condvalue should be found anyway on that portion of code)

	}
	
	
	/**
	 *  else_statement ::= statement | if_statement
	 */
	private void else_statement() throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException {
		
		Debug.println("ELSE STATEMENT");
		if ( look == '[' ) {
			// header ::= no known parent yet, is ANY and is default.
			this.currentHeader = new TempTableHeaderCell(null,true,true); 
			this.tempTable.add(this.currentHeader);
			this.statement();
		} else {
			Debug.println("COULD NOT FIND '['");
			// we dont have to create new header here because ifStatement would do so.
			ifStatement();
		}
	
	}
	

	/**
	 * statement ::= "[" assignment "]" 
	 * 
	 */
	private void statement() throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException{
		Debug.println("PARSING STATEMENT, VALUE = " + value + ", LOOKAHEAD = " + look);
		if (look == '[') {
			
			// Consistency check C09
			// Structures that allow us to Verify if all states has probability declared
			List<Entity> declaredStates = new ArrayList<Entity>();
			List<Entity> possibleStates = null;			
			if (this.node != null) {
				possibleStates = this.node.getPossibleValueList();
			}
			
			Debug.println("");
			Debug.print("  ");
			match('[');
			
			// initialize currently evaluated temporary table's collumn
			//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>();
			
			IProbabilityValue totalProb = assignment(declaredStates, possibleStates);
			match(']');
			Debug.println("");
			
			if (this.node != null) {
				// Consistency check C09
				// Verify if all states has probability declared
				if (!declaredStates.containsAll(possibleStates)) {
					throw new SomeStateUndeclaredException();
				}
			}
			// Consistency check C09
			// Verify if sum of all declared states' probability is 1
			float totalProbValue = totalProb.getProbability();
			if (totalProbValue >=0) {
				if ( Float.compare(totalProbValue, 1.0F) != 0 ) {
					throw new InvalidProbabilityRangeException();
				}
			}
			// runtime probability bound check (on SSBN generation time)
			if (!this.currentHeader.isSumEquals1()) {
				Debug.println("Testing cell's probability value's sum: " + currentHeader.getProbCellSum());
				if (!Float.isNaN(this.currentHeader.getProbCellSum())) {
					throw new InvalidProbabilityRangeException();
				} else {
					Debug.println("=>NaN found!!!");
				}
			}
		} else {
			Debug.println("COULD NOT FIND '['");
			this.expected("[");
		}
	}

	/**
	 * assignment ::= ident "=" expression [ "," assignment ]*
	 * 
	 * declaredStates, possibleStates are used to verify if every single possible state for
	 * RV has its probability declared.
	 * 
	 * returns the sum of all declared states' probability after this assignment recursion phase
	 * 
	 */
	private IProbabilityValue assignment(List<Entity> declaredStates, List<Entity> possibleStates) 
					throws InvalidProbabilityRangeException, 
						   TableFunctionMalformedException,
						   SomeStateUndeclaredException{
		
		// prepare a representation of a cell inside the temporary table
		TempTableProbabilityCell currentCell = new TempTableProbabilityCell(null, null);
		
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			if (this.node != null) {
				// Consistency check C09
				// Remember declared states, so we can check later if all states was declared
				Entity possibleValue = null;
				try {
					possibleValue = possibleStates.get(this.node.getPossibleValueIndex(this.noCaseChangeValue));
				} catch (Exception e) {
					throw new TableFunctionMalformedException(e.getMessage());
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

		// consistency check C09
		// ret verifies the sum of all declared states' probability (must be 1)
		// boolean hasUnknownValue shows if some ret was negative.
		IProbabilityValue ret = expression();		
		float retValue = ret.getProbability();
		boolean hasUnknownValue = Float.compare(retValue,Float.NaN) == 0;
		
		// add cell to header
		currentCell.setProbability(ret);
		if (currentCell.getPossibleValue() != null) {
			this.currentHeader.addCell(currentCell);
		}
		Debug.println("Adding cell: " + currentCell.getPossibleValue().getName() + " = " + ret.toString());
		
		// consistency check C09
		// a single state shall never have prob range out from [0,1]
		if ( (retValue < 0.0) || (1.0 < retValue)) {
			throw new InvalidProbabilityRangeException();
		}
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			IProbabilityValue temp = assignment(declaredStates, possibleStates);
			float tempValue = temp.getProbability();
			hasUnknownValue = hasUnknownValue || (Float.compare(tempValue,Float.NaN) == 0);
			if (hasUnknownValue) {
				retValue = Float.NaN;
			} else {
				retValue += temp.getProbability();
			}
		}
		if (retValue > 1) {
			throw new InvalidProbabilityRangeException();
		}
		return new SimpleProbabilityValue(retValue);
	}

	/**
	 * expression ::= term [ addop term ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue expression() throws TableFunctionMalformedException,
												  InvalidProbabilityRangeException,
												  SomeStateUndeclaredException{
		
		// TODO create temp table
		IProbabilityValue temp1 = term();
		IProbabilityValue temp2 = new SimpleProbabilityValue(Float.NaN);
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			temp2 = term();
			if (!Float.isNaN(temp2.getProbability()) && !Float.isNaN(temp1.getProbability())) {
				temp1 = new MathOperationProbabilityValue(temp2 , MathOperationProbabilityValue.ADD , temp1);
			}		
			break;
		case '-':
			match('-');
			temp2 = term();
			if (!Float.isNaN(temp2.getProbability()) && !Float.isNaN(temp1.getProbability())){
				temp1 = new MathOperationProbabilityValue(temp1 , MathOperationProbabilityValue.SUBTRACT , temp2);
			}			
			break;
		}
		
		Debug.println("Expression returned " + temp1.getProbability());
		return temp1;
	}

	/**
	 * term ::= signed_factor [ mulop factor ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue term() throws TableFunctionMalformedException,
											InvalidProbabilityRangeException,
											SomeStateUndeclaredException{
		IProbabilityValue temp1 = signedFactor();
		IProbabilityValue temp2 = new SimpleProbabilityValue(Float.NaN);
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			temp2 = factor();
			if (!Float.isNaN(temp2.getProbability()) && !Float.isNaN(temp1.getProbability())) {
				return new MathOperationProbabilityValue(temp2, MathOperationProbabilityValue.MULTIPLY , temp1);
			}
			break;
		case '/':
			match('/');
			temp2 = factor();
			if (!Float.isNaN(temp2.getProbability()) && !Float.isNaN(temp1.getProbability())) {
				return new MathOperationProbabilityValue(temp2, MathOperationProbabilityValue.DIVIDE , temp1);
			}
			break;
		default:
			return temp1;
		}
		Debug.println("Term is not matching to an * or / nor signed factor !!");
		return new SimpleProbabilityValue(Float.NaN);
	}

	/**
	 * signed_factor ::= [ addop ] factor
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue signedFactor() throws TableFunctionMalformedException,
													InvalidProbabilityRangeException,
													SomeStateUndeclaredException{

		int sign = 1;
		
		// CHECK TO SEE IF THERE IS A -/+ UNARY SIGN
		// boolean negative;
		// negative = (look == '-');
		if (isAddOp(look)) {
			Debug.print("" + look);
			
			if (this.isMinus(look)) {
				sign = -1;
			}
			
			nextChar();
			skipWhite();
		}
		Debug.println("Signed factor returning " + sign);
		return new MathOperationProbabilityValue(new SimpleProbabilityValue(sign) , MathOperationProbabilityValue.MULTIPLY , factor());
	}

	/**
	 * factor ::= number | function | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue factor() throws TableFunctionMalformedException,
											  InvalidProbabilityRangeException,
											  SomeStateUndeclaredException{
		IProbabilityValue ret = new SimpleProbabilityValue(Float.NaN);
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (isAlpha(look)) {
			ret = function();
		} else {
			ret =  getNum();
		}
		Debug.println("Factor returning " + ret.toString());
		return ret;
	}

	/**
	 * ident ::= letter [ letter | digit ]*
	 * 
	 */
	private void getName()throws TableFunctionMalformedException {
		Debug.println("RESETING VALUE FROM " + value);
		value = "";
		Debug.println("LOOKAHEAD IS " + look);
		if (!isAlpha(look))
			expected("Name");
		while (isAlphaNumeric(look)) {
			value += look;
			nextChar();
		}
		
		noCaseChangeValue = value;
		value = value.toUpperCase();

		token = 'x';
		//skipWhite();

		Debug.print(value + " ");
		
		
		
	}
	
	/**
	 * possibleVal ::= ident
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue possibleVal()throws TableFunctionMalformedException,
												  SomeStateUndeclaredException {

		this.getName();
		
		// Use a list to store already known states or identifiers to evaluate already known values... 
		IProbabilityValue ret = new SimpleProbabilityValue(Float.NaN);
		if (this.currentHeader != null) {
			for (TempTableProbabilityCell cell : this.currentHeader.getCellList()) {
				 if (cell.getPossibleValue().getName().compareTo(noCaseChangeValue) == 0) {
					 Debug.println("\n => Variable value found: " + cell.getPossibleValue().getName());
					 return cell.getProbability();
					 
				 }
			}
		} else {
			// if null, it means it was called before an assignment
			throw new SomeStateUndeclaredException();
		}
		


		Debug.println("An undeclared possible value or a \"varsetname\" was used : " + value);
		return ret;
	}

	/**
	 * number ::= [digit]+
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue getNum() throws TableFunctionMalformedException {
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

		Debug.println("GetNum returned " + Float.parseFloat(value));
		return new SimpleProbabilityValue(Float.parseFloat(value));
	}
	
	/**
	 * Scan the text and skippes white space
	 * @throws TableFunctionMalformedException
	 */
	private void scan() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		skipWhite();
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		Debug.println("\n!!!Value = "  + value);
	}
	
	/**
	 * Scan the text but doesnt skip white spaces
	 * @throws TableFunctionMalformedException
	 */
	private void scanNoSkip() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		Debug.println("\n!!!Value = "  + value);
	}

	/**
	 * Searches the kwlist for a String. Returns its index when found.
	 * @param s: a keyword to look for
	 * @return kwlist's index where the keyword resides. -1 if not found.
	 */
	private int lookup(String s) {
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
	private void nextChar() {
		if (index < text.length) {
			look = text[index++];
		}
	}

	private void skipWhite() {
		while ((index < text.length) && (look == ' '))
			nextChar();
	}

	/* Sends an alert telling that we expected some particular input */
	private void expected(String error) throws TableFunctionMalformedException {
		System.err.println("Error: " + error + " expected!");
		throw new TableFunctionMalformedException();
	}

	/* Verifies if an input is an expected one */
	private void match(char c) throws TableFunctionMalformedException {
		
		Debug.print(c + " ");
		if (look != c)
			expected("" + c);
		nextChar();
		skipWhite();
	}

	private void matchString(String s) throws TableFunctionMalformedException {
		if (!value.equalsIgnoreCase(s))
			expected(s);
	}

	private boolean isAlpha(final char c) {
		if ((c >= 'a') && (c <= 'z'))
			return true; // lowercase
		if ((c >= 'A') && (c <= 'Z'))
			return true; // uppercase
		if (c == '_') {
			return true; // underscore
		}
		
		return false;
	}

	private boolean isAlphaNumeric(final char c) {
		if (isAlpha(c))
			return true; // uppercase
		if (isNumeric(c))
			return true; // numeric
		return false;
	}

	private boolean isNumeric(final char c) {
		return (((c >= '0') && (c <= '9')));
	}

	/* reconhece operador aditivo */
	private boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}
	
	// identifies the "negative" simbol
	private boolean isMinus(char c) {
		return c == '-';
	}

	
	/**
	 * @return Returns the node.
	 */
	public DomainResidentNode getNode() {
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
	public void setNode(DomainResidentNode node) {
		this.node = node;
		if (this.node != null) {
			this.mebn = node.getMFrag().getMultiEntityBayesianNetwork();
		}
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node	
	 * @return if node with name == nodeName is a valid conditionant.
	 */
	private boolean isValidConditionant(MultiEntityBayesianNetwork mebn, DomainResidentNode node, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName);
		
		
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParents().contains(conditionant)) {
				return true;
			} else {	// parent may be an input node
				NodeList parents = node.getParents();
				for (int i = 0; i < parents.size(); i++) {
					if (parents.get(i) instanceof GenerativeInputNode) {
						if ( ((GenerativeInputNode)(parents.get(i))).getInputInstanceOf().equals(conditionant) ) {
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
	private boolean isValidConditionantValue(MultiEntityBayesianNetwork mebn, String conditionantName, String conditionantValue) {
		Node conditionant = mebn.getNode(conditionantName);
		if (conditionant == null) {
			Debug.println("No conditionant of name " + conditionantName);
			return false;
		}
		//Debug.println("Conditionant node found: " + conditionant.getName());
		if ( conditionant instanceof DomainResidentNode) {
			Debug.println("IS MULTIENTITYNODE");
			return ((DomainResidentNode)conditionant).getPossibleValueByName(conditionantValue) != null;
		} else {
			Debug.println("Conditionant is not a resident node");
		}
			
		return false;
	}
	
	
	/**
	 *  function ::= ident 
	 *   	| "CARDINALITY" "(" ident ")"
	 *    	| "MIN" "(" expression ; expression ")"
	 *     	| "MAX" "(" expression ; expression ")"
	 * @return numeric value expected for the function
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue function()throws TableFunctionMalformedException,
											   InvalidProbabilityRangeException,
											   SomeStateUndeclaredException{
		IProbabilityValue ret = this.possibleVal();
		skipWhite();
		if (this.look == '(') {
			if (this.value.compareToIgnoreCase("CARDINALITY") == 0) {
				return cardinality();
			} else if (this.value.compareToIgnoreCase("MIN") == 0) {
				return min();
			} else if (this.value.compareToIgnoreCase("MAX") == 0) {
				return max();
			} else {
				Debug.println("UNKNOWN FUNCTION FOUND: " + this.value);
				throw new TableFunctionMalformedException(this.resource.getString("UnexpectedTokenFound")
						+ ": " + value);
			}
		}
		
		Debug.println("Function returning " + ret);
		return ret;
	}
	
	
	/**
	 * Computes cardinality funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue cardinality()throws TableFunctionMalformedException {
		IProbabilityValue ret = null;
		match('(');
		
		this.getName();
		skipWhite();
		Debug.println("CARDINALITY'S ARGUMENT IS " + this.value);
		// TODO test if ret has returned NaN (guarantees "value" is a varsetname)?
		ret = new CardinalityProbabilityValue(this.ssbnnode, this.value);
		match(')');
		return ret;
		
	}
	
	/**
	 * Computes min funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue min()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		Debug.println("ANALISING MIN FUNCTION");
		
		IProbabilityValue ret1 = null;
		IProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		match(';');
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
	private IProbabilityValue max()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		Debug.println("ANALISING MAX FUNCTION");
		
		IProbabilityValue ret1 = null;
		IProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		match(';');
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
		return index;
	}

	/**
	 * @return the cpt
	 */
	public PotentialTable getCpt() {
		return cpt;
	}

	/**
	 * @param cpt the cpt to set
	 */
	public void setCpt(PotentialTable cpt) {
		this.cpt = cpt;
	}
	
	
	 
	
	// Some inner classes that might be useful for temporaly table creation (organize the table parsed from pseudocode)
	
	private class TempTableHeaderCell {
		private List<TempTableHeaderParent> parents = null;

		private String varsetname = "";
		
		private boolean isAny = true;
		private boolean isDefault = false;
		
		private List<TempTableProbabilityCell> cellList = null;
		
		/**
		 * Represents an entry for temporary table header (parents and their expected single values
		 * at that table entry/collumn)
		 * @param parents
		 * @param isAny
		 * @param isDefault
		 */
		TempTableHeaderCell (List<TempTableHeaderParent> parents , boolean isAny, boolean isDefault) {
			this.parents = parents;
			this.isAny = isAny;
			this.isDefault = isDefault;
			this.cellList = new ArrayList<TempTableProbabilityCell>();
		}
		public List<TempTableHeaderParent> getParents() {
			return parents;
		}
		public void setParents(List<TempTableHeaderParent> parents) {
			this.parents = parents;
		}
		
		public void addParent(TempTableHeaderParent parent) {
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
			if (ssbnnode.getParentSetByStrongOV(false, varsetname.split("\\.")).size() > 0) {
				return true;
			} else {
				return false;
			}
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
		 * @return the cellList
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
		
		
		public void addCell(Entity possibleValue , IProbabilityValue probability) {
			this.addCell(new TempTableProbabilityCell(possibleValue, probability));
		}
		
		public void addCell(TempTableProbabilityCell cell) {
			this.cellList.add(cell);
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return
		 */
		public boolean isSumEquals1() throws InvalidProbabilityRangeException {
			if (Float.compare(this.getProbCellSum(), 1.0F) != 0) {
				return false;
			} else {
				return true;
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
			sum = 0;
			for (TempTableProbabilityCell cell : this.cellList) {
				sum += cell.getProbabilityValue();
			}
			return sum;
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
	
	private class TempTableHeaderParent {
		private DomainResidentNode parent = null;
		private Entity value = null;
		
		/**
		 * Represents a parent and its expected single value
		 * at that table entry/collumn
		 * @param parent
		 * @param value
		 */
		TempTableHeaderParent (DomainResidentNode parent , Entity value) {
			this.parent = parent;
			this.value = value;
		}
		public DomainResidentNode getParent() {
			return parent;
		}
		public void setParent(DomainResidentNode parent) {
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
				if (this.parent.getName().compareTo(arg.getParent().getName()) == 0) {
					if (this.value.getName().compareTo(arg.getValue().getName()) == 0) {
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
		
		
	}
	
	private class TempTableProbabilityCell {
		private Entity possibleValue = null;
		private IProbabilityValue probability = null;
		
		/**
		 * Represents a simple entry at a temporaly table representation (the
		 * value and its probability pair)
		 * @param possibleValue
		 * @param probability
		 */
		TempTableProbabilityCell (Entity possibleValue , IProbabilityValue probability) {
			this.possibleValue = possibleValue;
			this.probability = probability;
		}
		public Entity getPossibleValue() {
			return possibleValue;
		}
		public void setPossibleValue(Entity possibleValue) {
			this.possibleValue = possibleValue;
		}
		public float getProbabilityValue() throws InvalidProbabilityRangeException {
			return probability.getProbability();
		}
		public IProbabilityValue getProbability() {
			return probability;
		}
		public void setProbability(IProbabilityValue probability) {
			this.probability = probability;
		}		
	}
	
	private interface IProbabilityValue {
		/**
		 * 
		 * @return: a value between [0,1] which represents a probability
		 */
		public float getProbability() throws InvalidProbabilityRangeException;
	}
	
	private class SimpleProbabilityValue implements IProbabilityValue {
		private float value = Float.NaN;
		/**
		 * Represents a simple float value for a probability
		 * @param value
		 */
		SimpleProbabilityValue (float value) {
			this.value = value;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.value;
		}
	}
	
	private class MathOperationProbabilityValue implements IProbabilityValue {
		private float value = Float.NaN;
		private IProbabilityValue op1 = null;
		private IProbabilityValue op2 = null;
		private int opcode = -1;
		
		public static final int ADD = 0;
		public static final int SUBTRACT = 1;
		public static final int MULTIPLY = 2;
		public static final int DIVIDE = 3;
		
		
		
		MathOperationProbabilityValue(IProbabilityValue op1 , int opcode, IProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.opcode = opcode;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			float ret = -1;
			switch (this.opcode) {
			case MathOperationProbabilityValue.ADD:
				ret = this.op1.getProbability() + this.op2.getProbability();
				Debug.println("Operation = +, getProbability: " + this.op1.getProbability() 
						+ " , " + this.op2.getProbability() + " = " + ret);
				break;
			case MathOperationProbabilityValue.SUBTRACT:
				ret = this.op1.getProbability() - this.op2.getProbability();
				Debug.println("Operation = -, getProbability: " + this.op1.getProbability() 
						+ " , " + this.op2.getProbability() + " = " + ret);
				break;
			case MathOperationProbabilityValue.MULTIPLY:
				ret = this.op1.getProbability() * this.op2.getProbability();
				Debug.println("Operation = *, getProbability: " + this.op1.getProbability() 
						+ " , " + this.op2.getProbability() + " = " + ret);
				break;
			case MathOperationProbabilityValue.DIVIDE:
				ret = this.op1.getProbability() / this.op2.getProbability();
				Debug.println("Operation = /, getProbability: " + this.op1.getProbability() 
						+ " , " + this.op2.getProbability() + " = " + ret);
				break;
			default:
				ret = Float.NaN;
				break;
			}
			// consistency check (verify probability range [0,1])
			if (!Float.isNaN(ret)) {
				
				if ( (ret < 0) || (ret > 1) ) {
					throw new InvalidProbabilityRangeException();
				} 
			}	
			return ret;
		}
	}
	
	private class CardinalityProbabilityValue implements IProbabilityValue {
		private float value = Float.NaN;
		private String parentSetName = null;		
		private SSBNNode thisNode = null;
		/**
		 * Represents a probability value from cardinality function
		 * It calculates the value using thisNode's parents set
		 * @param thisNode: SSBNNode containing this compiler and this pseudocode
		 * @param strongOVNames: literals containing ov names which determines
		 * the set containing strong variables, separated by the separator (usually comma ".")
		 * at any order
		 * e.g. "st.z.s" may return a node
		 * Node(st,z,s,t,tprev), because it contains st,z and s, and t and tprev are
		 * considered "weak".
		 */
		CardinalityProbabilityValue (SSBNNode thisNode, String strongOVNames) {
			this.thisNode = thisNode;
			this.parentSetName = strongOVNames;
		}

		public float getProbability() throws InvalidProbabilityRangeException {
			if (this.thisNode == null) {
				return Float.NaN;
			}
			// TODO calculate the value using the right steps!!!
			String regExp = this.thisNode.getStrongOVSeparator();
			// TODO find a better way to treat wild cards on regular expressions (like "[" or "\")
			if (regExp.compareTo(".") == 0) {
				regExp = "\\" + regExp;
			}
			return this.thisNode.getParentSetByStrongOV(false, this.parentSetName.split(regExp)).size();
		}
	}
	
	
	private class ComparisionProbabilityValue implements IProbabilityValue {
		private IProbabilityValue arg0 = null;
		private IProbabilityValue arg1 = null;
		private boolean isMax = false;
		/**
		 * Represents a comparision function between two values (MAX or MIN)
		 * @param arg0
		 * @param arg1
		 * @param isMax true if it represents a MAX function. If false, it represents a MIN
		 * function.
		 */
		ComparisionProbabilityValue (IProbabilityValue arg0, IProbabilityValue arg1, boolean isMax) {
			this.arg0 = arg0;
			this.arg1 = arg1;
			this.isMax = isMax;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			float prob0 = this.arg0.getProbability();
			float prob1 = this.arg1.getProbability();
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
				return prob0;
			} else {
				return prob1;
			}
		}
	}


	/**
	 * @return the tempTable
	 */
	public List<TempTableHeaderCell> getTempTable() {
		return tempTable;
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
	
	

}
