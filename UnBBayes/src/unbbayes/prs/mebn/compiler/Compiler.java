package unbbayes.prs.mebn.compiler;


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
 Changes: 
 
 
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
 	"else" statement 
 allop ::= "any" | "all"
 varsetname ::= ident ["." ident]*
 b_expression ::= b_term [ "|" b_term ]*
 b_term ::= not_factor [ "&" not_factor ]*
 not_factor ::= [ "~" ] b_factor
 b_factor ::= ident "=" ident
 statement ::= "[" assignment "]" | if_statement
 assignment ::= ident "=" expression [ "," assignment ]*
 expression ::= term [ addop term ]*
 term ::= signed_factor [ mulop factor ]*
 signed_factor ::= [ addop ] factor
 factor ::= number | function | "(" expression ")" 
 	| simplefunction "(" expression ")"
 	| biargfunction "(" expression ; expression ")"
 function ::= ident 
 	| "CARDINALITY" "(" ident ")"
 	| "MIN" "(" expression ; expression ")"
 	| "MAX" "(" expression ; expression ")"
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
	Hashtable<TempTableHeaderCell, List<TempTableProbabilityCell>> tempTable = null;
	
	
	private Compiler() {
		
	}
	
	public Compiler (DomainResidentNode node) {
		super();
		this.setNode(node);
		this.cpt = null;
	}
	
	public Compiler (DomainResidentNode node, SSBNNode ssbnnode) {
		super();
		this.setNode(node);
		this.ssbnnode = ssbnnode;
		if (this.ssbnnode != null) {
			this.cpt = this.ssbnnode.getProbNode().getPotentialTable();
		}
		tempTable = new Hashtable<TempTableHeaderCell, List<TempTableProbabilityCell>>();
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
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException {
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
			// if we catch a sintax error here, it may be a value error
			try {
				statement();
			} catch (TableFunctionMalformedException e) {
				// Exception translation (perharps an anti-pattern ?)
				throw new InvalidProbabilityRangeException(e.getMessage());
			}
		} else {
			// Please note table() repasses every exception reported by ifStatement()
			this.ifStatement();
		}
	}

	/**
	 * if_statement ::= "if" allop ident "have" "(" b_expression ")" statement [
	 * "else" statement ]
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
			break;
		case 'y':
			Debug.println("ANY VERIFIED");
			break;
		default:
			expected("ALL or ANY");
		}

		// SCAN FOR varsetname
		this.varsetname();
		

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
		
		//	This test is necessary to verify if  there is an else clause
		if (this.index < this.text.length) {
			try {
				scan();
			} catch (TableFunctionMalformedException e) {
				// a sintax error here represents no else statement
				throw new NoDefaultDistributionDeclaredException();
			}
		} else {
			// No else statement was found literally, and its end of table.
			Debug.println("END OF TABLE");
			throw new NoDefaultDistributionDeclaredException();
		}
		
		if (token == 'l') {
			statement();
		} else {
			// The statement found was not an else statement
			throw new NoDefaultDistributionDeclaredException();
		}
	}
	
	/**
	 *   It skippes white spaces after evaluation.
	 *   varsetname ::= ident["."ident]*
	 */
	private void varsetname() throws TableFunctionMalformedException {
		// scan for the ident
		do {
			scanNoSkip();	// no white spaces should stay between ident and "." and next ident
			if (token == 'x') {
				Debug.println("SCANING IDENTIFIER " + value);
			} else {
				expected("Identifier");
			}	
			
			// search for ["." ident]* loop
			if (this.look == '.') {
				this.nextChar();
				continue;
			} else {
				break;
			}
		} while (index < text.length); 	// actually, this check is unreachable
		
		skipWhite();
	}

	/**
	 * b_expression ::= b_term [ "|" b_term ]*
	 * 
	 */
	private void bExpression() throws InvalidConditionantException,
									  TableFunctionMalformedException{
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
	}

	/**
	 * statement ::= "[" assignment "]" | if_statement
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
			float totalProb = assignment(declaredStates, possibleStates);
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
			if (totalProb >=0) {
				if ( Float.compare(totalProb, 1.0F) != 0 ) {
					throw new InvalidProbabilityRangeException();
				}
			}
		} else {
			Debug.println("COULD NOT FIND '['");
			ifStatement();
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
	private float assignment(List<Entity> declaredStates, List<Entity> possibleStates) 
					throws InvalidProbabilityRangeException, 
						   TableFunctionMalformedException{
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			if (this.node != null) {
				// Consistency check C09
				// Remember declared states, so we can check later if all states was declared
				declaredStates.add(possibleStates.get(this.node.getPossibleValueIndex(this.noCaseChangeValue)));
			}
			
		} else {
			expected("Identifier");
		}

		// LOOK FOR = OPERATOR
		match('=');

		// consistency check C09
		// ret verifies the sum of all declared states' probability (must be 1)
		// boolean hasUnknownValue shows if some ret was negative.
		float ret = expression();
		boolean hasUnknownValue = Float.compare(ret,Float.NaN) == 0;

		// consistency check C09
		// a single state shall never have prob range out from [0,1]
		if ( (ret < 0.0) || (1.0 > ret)) {
			throw new InvalidProbabilityRangeException();
		}
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			float temp = assignment(declaredStates, possibleStates);
			hasUnknownValue = hasUnknownValue || (Float.compare(temp,Float.NaN) == 0);
			if (hasUnknownValue) {
				ret = Float.NaN;
			} else {
				ret += temp;
			}
		}
		if (ret > 1) {
			throw new InvalidProbabilityRangeException();
		}
		return ret;
	}

	/**
	 * expression ::= term [ addop term ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float expression() throws TableFunctionMalformedException {
		float temp1 = term();
		float temp2 = Float.NaN;
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			temp2 = term();
			if ((Float.compare(temp2 , Float.NaN) == 0) 
					&& (Float.compare(temp1 , Float.NaN) == 0)) {
				temp1 = temp2 + temp1;
			}
			break;
		case '-':
			match('-');
			temp2 = term();
			if ((Float.compare(temp2 , Float.NaN) == 0) 
					&& (Float.compare(temp1 , Float.NaN) == 0)) {
				temp1 = temp2 - temp1;
			}
			break;
		}
		
		
		return temp1;
	}

	/**
	 * term ::= signed_factor [ mulop factor ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float term() throws TableFunctionMalformedException {
		float temp1 = signedFactor();
		float temp2 = Float.NaN;
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			temp2 = factor();
			if ((Float.compare(temp2 , Float.NaN) == 0) 
					&& (Float.compare(temp1 , Float.NaN) == 0)) {
				return temp2 * temp1;
			}
			break;
		case '/':
			match('/');
			temp2 = factor();
			if ((Float.compare(temp2 , Float.NaN) == 0) 
					&& (Float.compare(temp1 , Float.NaN) == 0)) {
				return temp2 / temp1;
			}
			break;
		}
		return Float.NaN;
	}

	/**
	 * signed_factor ::= [ addop ] factor
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float signedFactor() throws TableFunctionMalformedException {

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
		
		return sign  * factor();
	}

	/**
	 * factor ::= number | function | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float factor() throws TableFunctionMalformedException {
		float ret = Float.NaN;
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (isAlpha(look)) {
			return function();
		} else {
			return getNum();
		}
		return ret;
	}

	/**
	 * ident ::= letter [ letter | digit ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float getName()throws TableFunctionMalformedException {
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
		
		// TODO avaliate already known values... 
		//  Use a list to store already known states or identifiers
		return Float.NaN;	// supposes an identifier has unknown value yet.
	}

	/**
	 * number ::= [digit]+
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float getNum() throws TableFunctionMalformedException {
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

		Debug.print(value + " ");
		return Float.parseFloat(value);
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
		this.mebn = node.getMFrag().getMultiEntityBayesianNetwork();
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node	
	 * @return whether node with name == nodeName is a valid conditionant.
	 */
	private boolean isValidConditionant(MultiEntityBayesianNetwork mebn, DomainResidentNode node, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName);
		
		
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParents().contains(conditionant)) {
				return true;
			} else {
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
		if ( conditionant instanceof MultiEntityNode) {
			Debug.println("IS MULTIENTITYNODE");
			return ((MultiEntityNode)conditionant).hasPossibleValue(conditionantValue);
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
	private float function()throws TableFunctionMalformedException {
		float ret = this.getName();
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
		
		
		return ret;
	}
	
	
	/**
	 * Computes cardinality funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private float cardinality()throws TableFunctionMalformedException {
		float ret = 0;
		match('(');
		ret = this.getName();
		skipWhite();
		Debug.println("CARDINALITY'S ARGUMENT IS " + this.value);
		match(')');
		return ret;
		
	}
	
	/**
	 * Computes min funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private float min()throws TableFunctionMalformedException {
		Debug.println("ANALISING MIN FUNCTION");
		
		float ret1 = 0;
		float ret2 = 0;
		match('(');
		ret1 = this.expression();
		match(';');
		ret2 = this.expression();
		match(')');
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2<ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		return ret1;
		
	}
	
	/**
	 * Computes MAX funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private float max()throws TableFunctionMalformedException {
		Debug.println("ANALISING MAX FUNCTION");
		
		float ret1 = 0;
		float ret2 = 0;
		match('(');
		ret1 = this.expression();
		match(';');
		ret2 = this.expression();
		match(')');
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2>ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		return ret1;
		
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
		private DomainResidentNode parent = null;
		private Entity value = null;
		/**
		 * Represents an entry for temporary table header (a parent and its expected single value
		 * at that table entry/collumn)
		 * @param parent
		 * @param value
		 */
		TempTableHeaderCell (DomainResidentNode parent , Entity value) {
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
		public float getProbability() throws InvalidProbabilityRangeException {
			return probability.getProbability();
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
		private float value = 0;
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
	
	private class CardinalityProbabilityValue implements IProbabilityValue {
		private float value = 0;
		private String[] parentSetName = null;		
		private SSBNNode thisNode = null;
		/**
		 * Represents a probability value from cardinality function
		 * It calculates the value using thisNode's parents set
		 * @param thisNode: SSBNNode containing this compiler and this pseudocode
		 * @param strongOVNames: literals containing ov names which determines
		 * the set containing strong variables (e.g. {"st","z","s"} may return a node
		 * Node(st,z,s,t,tprev), because it contains st,z and s, and t and tprev are
		 * considered "weak")
		 */
		CardinalityProbabilityValue (SSBNNode thisNode, String...strongOVNames) {
			this.thisNode = thisNode;
			this.parentSetName = strongOVNames;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			// TODO calculate the value!!!
			return this.value;
		}
	}
	
	
	private class MaxProbabilityValue implements IProbabilityValue {
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
		MaxProbabilityValue (IProbabilityValue arg0, IProbabilityValue arg1, boolean isMax) {
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
	

}
