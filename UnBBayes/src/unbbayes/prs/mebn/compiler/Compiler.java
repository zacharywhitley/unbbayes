package unbbayes.prs.mebn.compiler;


import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.Debug;
import unbbayes.util.NodeList;

/*
 BNF MEBN Table:
 ----------------
 Changes: 
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
 if_statement 
 ::= 
 "if" allop ident "have" "(" b_expression ")" statement 
 	"else" statement 
 allop ::= "any" | "all"
 b_expression ::= b_term [ "|" b_term ]*
 b_term ::= not_factor [ "&" not_factor ]*
 not_factor ::= [ "~" ] b_factor
 b_factor ::= ident "=" ident
 statement ::= "[" assignment "]" | if_statement
 assignment ::= ident "=" expression [ "," assignment ]*
 expression ::= term [ addop term ]*
 term ::= signed_factor [ mulop factor ]*
 signed_factor ::= [ addop ] factor
 factor ::= number | ident | ( expression )
 addop ::= "+" | "-"
 mulop ::= "*" | "/"
 ident ::= letter [ letter | digit ]*
 number ::= [digit]+
 ================================================================
 */



public class Compiler implements AbstractCompiler {

	
	/* O caracter lido "antecipadamente" (lookahead) */
	private char look;

	/* Posição de leitura do text */
	private int index = 0;

	private char[] text;

	/* palavras-chave */
	private String kwlist[] = { "IF", "ELSE", "ALL", "ANY", "HAVE" };

	/*
	 * código para as palavras-chave
	 */
	private char kwcode[] = { 'i', 'l', 'a', 'y', 'h' };

	/* token codificado */
	private char token;

	/*
	 * valor do token não codificado
	 */
	private String value = "";
	private String noCaseChangeValue = "";
	
	
	// Informations used by this class to check consistency
	private MultiEntityBayesianNetwork mebn = null;
	private DomainResidentNode node = null;
	

	/* inicialização do compilador */
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#init(java.lang.String)
	 */
	public void init(String text) {
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
		ifStatement();
	}

	/**
	 * if_statement ::= "if" allop ident "have" "(" b_expression ")" statement [
	 * "else" statement ]
	 * 
	 */
	private void ifStatement() throws NoDefaultDistributionDeclaredException,
									  InvalidConditionantException,
									  SomeStateUndeclaredException,
									  InvalidProbabilityRangeException{
		Debug.println("PARSING IF STATEMENT");
		// SCAN FOR IF
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

		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			Debug.println("SCANING IDENTIFIER " + value);
		} else {
			expected("Identifier");
		}

		// SCAN FOR HAVE
		Debug.println("SCAN FOR HAVE");
		scan();
		matchString("HAVE");

		// ( EXPECTED
		match('(');
		bExpression();
		//this.nextChar();
		//this.skipWhite();
		Debug.println("LOOKAHEAD = " + look);
		// ) EXPECTED
		match(')');
		
		Debug.println("STARTING STATEMENTS");
		
		statement();

		Debug.println("LOOKING FOR ELSE STATEMENT");
		// LOOK FOR ELSE
		// Consistency check C09: the grammar may state else as optional,
		// but semantically every table must have a default distribution, which is
		// declared within an else clause.
		
		//	This test is necessary to verify if  there is an else clause
		if (this.index < this.text.length) {
			scan();
		} else {
			// No else statement was found.
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
	 * b_expression ::= b_term [ "|" b_term ]*
	 * 
	 */
	private void bExpression() throws InvalidConditionantException {
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
	private void bTerm() throws InvalidConditionantException {
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
	private void notFactor() throws InvalidConditionantException{
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
	private void bFactor() throws InvalidConditionantException {
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
			expected("Identifier");
		}

		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR CONDITIONANTS' POSSIBLE STATES
		scan();
		
		Debug.println("SCANED FOR CONDITIONANTS' POSSIBLE STATES");
		
		if (token == 'x') {
			// consistency check C09: verify whether conditionant has valid values
			if (!this.isValidConditionantValue(this.mebn,conditionantName,this.noCaseChangeValue)) {
				throw new InvalidConditionantException();
			}
		} else {
			expected("Identifier");
		}
	}

	/**
	 * statement ::= "[" assignment "]" | if_statement
	 * 
	 */
	private void statement() throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException{
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
					throws InvalidProbabilityRangeException{
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
	private float expression() {
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
	private float term() {
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
	private float signedFactor() {

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
	 * factor ::= number | ident | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float factor() {
		float ret = Float.NaN;
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (isAlpha(look))
			return getName();
		else {
			return getNum();
		}
		return ret;
	}

	/**
	 * ident ::= letter [ letter | digit ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private float getName() {
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
		skipWhite();

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
	private float getNum() {
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

	private void scan() {
			
		int kw;
		
		getName();
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		Debug.println("\n!!!Value = "  + value);
	}

	private int lookup(String s) {
		int i;

		for (i = 0; i < kwlist.length; i++) {
			if (kwlist[i].equalsIgnoreCase(s))
				return i;
		}

		return -1;
	}

	/* l? próximo caracter da entrada */
	private void nextChar() {
		if (index < text.length) {
			look = text[index++];
		}
	}

	private void skipWhite() {
		while ((index < text.length) && (look == ' '))
			nextChar();
	}

	/* alerta sobre alguma entrada esperada */
	private void expected(String error) {
		System.err.println("Error: " + error + " expected!");
		System.exit(1);
	}

	/* verifica se entrada combina com o esperado */
	private void match(char c) {
		
		Debug.print(c + " ");
		if (look != c)
			expected("" + c);
		nextChar();
		skipWhite();
	}

	private void matchString(String s) {
		if (!value.equalsIgnoreCase(s))
			expected(s);
	}

	private boolean isAlpha(final char c) {
		if ((c >= 'a') && (c <= 'z'))
			return true; // lowercase
		if ((c >= 'A') && (c <= 'Z'))
			return true; // uppercase
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
	DomainResidentNode getNode() {
		return node;
	}

	/**
	 * Setting this to null or not setting at all makes
	 * Compiler not to check structural consistency (like node states or conditionants
	 * are real)
	 * @param node The node to set.
	 */
	void setNode(DomainResidentNode node) {
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
		
		
		System.out.println("!!!Mynode = " + node.getName());
		
		System.out.println("!!!Conditionants, mebn = " + mebn.getName());
		System.out.println("!!!Conditionants, conditionantName = " + conditionantName);
		System.out.println("!!!Conditionants, conditionantNode = " + conditionant.getName());
		
		
		NodeList nodelist = conditionant.getChildren();
		for (int i = 0; i < nodelist.size(); i++) {
			System.out.println("!!!!!!Children of conditionants= " + nodelist.get(i).getName());
		}
		nodelist = this.node.getParents();
		for (int i = 0; i < nodelist.size(); i++) {
			System.out.println("!!!!!!Parents of me = " + nodelist.get(i).getName());
			System.out.println("!!!!!!Input instance of: " + nodelist.get(i).getClass().getName());
		}
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParents().contains(conditionant)) {
				System.out.println("!!!!Is node");
				return true;
			} else {
				NodeList parents = node.getParents();
				for (int i = 0; i < parents.size(); i++) {
					if (parents.get(i) instanceof GenerativeInputNode) {
						if ( ((GenerativeInputNode)(parents.get(i))).getInputInstanceOf().equals(conditionant) ) {
							System.out.println("!!!!Is GenerativeInputNode");
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
		Debug.println("Conditionant node found: " + conditionant.getName());
		if ( conditionant instanceof MultiEntityNode) {
			Debug.println("IS MULTIENTITYNODE");
			return ((MultiEntityNode)conditionant).hasPossibleValue(conditionantValue);
		}
			
		return false;
	}

}
