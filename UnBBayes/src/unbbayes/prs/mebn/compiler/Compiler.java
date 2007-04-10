package unbbayes.prs.mebn.compiler;

import java.io.File;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.Debug;

/*
BNF MEBN Table:
----------------
===============================================================
if_statement 
      ::= 
      "if" allop ident "have" "(" b_expression ")" statement 
      [ "else" statement ]
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

public class Compiler {
	
	private static final String TABLE_TO_PARSE = 
		"if any STi have (OpSpec = Cardassian & HarmPot = true)  " +
		" [Un = .90, Hi = (1 - Un) * .8, Me = (1 - Un) * .2, Lo = 0] " +
		"else if any STl have (OpSpec = Friend & HarmPot = true | OpSpec = Friend & HarmPot = false)  " +
		" [Un = 0, Hi = 0, Me = .01, Lo = .99] " +
		"else [Un = 0, Hi = 0, Me = 0, Lo = 1] ";

	/* O caracter lido "antecipadamente" (lookahead) */
	private char look; 

	/* Posi��o de leitura do text */
	private int index = 0; 

	private char[] text;

	/* palavras-chave */
	private String kwlist[] = { "IF", "ELSE", "ALL", "ANY", "HAVE"}; 

	/*
	 * c�digo para as
	 * palavras-chave
	 */
	private char kwcode[] = { 'i', 'l', 'a', 'y', 'h'}; 

	/* token codificado */
	private char token; 

	/*
	 * valor do token n�o codificado
	 */
	private String value = "";

	/* PROGRAMA PRINCIPAL */
	public static void main(String[] args) {
		ProbabilisticNetwork rede = new ProbabilisticNetwork("MEBN Table Test");

		ProbabilisticNode dangerToSelf = new ProbabilisticNode();
		dangerToSelf.setName("DangerToSelf");
		dangerToSelf.setDescription("Danger to self");
		dangerToSelf.appendState("Un");
		dangerToSelf.appendState("Hi");
		dangerToSelf.appendState("Me");
		dangerToSelf.appendState("Lo");
		PotentialTable auxTabPot = dangerToSelf.getPotentialTable();
		auxTabPot.addVariable(dangerToSelf);
		rede.addNode(dangerToSelf);
		
		ProbabilisticNode opSpec = new ProbabilisticNode();
		opSpec.setName("OpSpec");
		opSpec.setDescription("Operator Specie");
		opSpec.appendState("Cardassian");
		opSpec.appendState("Unknown");
		opSpec.appendState("Friend");
		opSpec.appendState("Klingon");
		opSpec.appendState("Romulan");
		auxTabPot = opSpec.getPotentialTable();
		auxTabPot.addVariable(opSpec);
		rede.addNode(opSpec);

		Edge auxArco = new Edge(dangerToSelf, opSpec);
		rede.addEdge(auxArco);
		
		ProbabilisticNode harmPotential = new ProbabilisticNode();
		harmPotential.setName("HarmPotential");
		harmPotential.setDescription("Harm Potential");
		harmPotential.appendState("True");
		harmPotential.appendState("False");
		auxTabPot = harmPotential.getPotentialTable();
		auxTabPot.addVariable(harmPotential);
		rede.addNode(harmPotential);
		
		auxArco = new Edge(dangerToSelf, harmPotential);
		rede.addEdge(auxArco);
		
		opSpec = new ProbabilisticNode();
		opSpec.setName("OpSpec2");
		opSpec.setDescription("Operator Specie 2");
		opSpec.appendState("Cardassian");
		opSpec.appendState("Unknown");
		opSpec.appendState("Friend");
		opSpec.appendState("Klingon");
		opSpec.appendState("Romulan");
		auxTabPot = opSpec.getPotentialTable();
		auxTabPot.addVariable(opSpec);
		rede.addNode(opSpec);

		auxArco = new Edge(dangerToSelf, opSpec);
		rede.addEdge(auxArco);
		
		harmPotential = new ProbabilisticNode();
		harmPotential.setName("HarmPotential2");
		harmPotential.setDescription("Harm Potential 2");
		harmPotential.appendState("True");
		harmPotential.appendState("False");
		auxTabPot = harmPotential.getPotentialTable();
		auxTabPot.addVariable(harmPotential);
		rede.addNode(harmPotential);
		
		auxArco = new Edge(dangerToSelf, harmPotential);
		rede.addEdge(auxArco);
		
		/*
		Debug.setDebug(true);
		Compiler c = new Compiler();
		c.init(TABLE_TO_PARSE);
		c.parse();
		*/
	}

	/* inicializa��o do compilador */
	public void init(String text) {
		Debug.println("************************************");
		Debug.println("ORIGINAL: " + text);
		text = text.replaceAll("\\s+", " ");
		Debug.println("CHANGED: " + text);
		Debug.println("************************************");
		this.text = text.toCharArray();
		nextChar();
	}

	public void parse() {
		Debug.println("PARSED: ");
		ifStatement();
	}
	
	/**
	 * if_statement 
     * 	::= 
     * 	"if" allop ident "have" "(" b_expression ")" statement 
     * 	[ "else" statement ]
     * 
	 */
	private void ifStatement() {
		// SCAN FOR IF
		scan();
		matchString("IF");
		
		// SCAN FOR ALL/ANY
		scan();
		switch (token) {
		case 'a':

			break;
		case 'y':
	
			break;
		default:
			expected("ALL or ANY");
		}
		
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
	
		} else {
			expected("Identifier");
		}
		
		// SCAN FOR HAVE
		scan();
		matchString("HAVE");
		
		// ( EXPECTED
		match('(');
		bExpression();
		// ) EXPECTED
		match(')');
		
		statement();
		
		// LOOK FOR ELSE (OPTIONAL)
		scan();
		if (token == 'l') {
			statement();
		}
	}
	
	/**
	 * b_expression ::= b_term [ "|" b_term ]*
	 *
	 */
	private void bExpression() {
		bTerm();
		
		// LOOK FOR OR (OPTIONAL)
		//scan();
		if (look == '|') {
			match('|');
			bTerm();
		}
	}
	
	/**
	 * b_term ::= not_factor [ "&" not_factor ]*
	 *
	 */
	private void bTerm() {
		notFactor();
		
		// LOOK FOR AND (OPTIONAL)
		//scan();
		if (look == '&') {
			match('&');
			notFactor();
		}
	}
	
	/**
	 * not_factor ::= [ "~" ] b_factor
	 *
	 */
	private void notFactor() {
		// SCAN FOR NOT (OPTIONAL)
		//scan();
		if (look == '~') {
			match('~');
		}
		
		bFactor();
	}
	
	/**
	 * b_factor ::= ident "=" ident
	 *
	 */
	private void bFactor() {
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			
		} else {
			expected("Identifier");
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			
		} else {
			expected("Identifier");
		}
	}
	
	/**
	 * statement ::= "[" assignment "]" | if_statement
	 *
	 */
	private void statement() {
		if (look == '[') {
			Debug.println("");
			Debug.print("  ");
			match('[');
			assignment();
			match(']');
			Debug.println("");
		} else {
			ifStatement();
		}
	}
	
	/**
	 * assignment ::= ident "=" expression [ "," assignment ]*
	 *
	 */
	private void assignment() {
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			
		} else {
			expected("Identifier");
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		expression();
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			assignment();
		}
	}
	
	/**
	 * expression ::= term [ addop term ]*
	 *
	 */
	private void expression() {
		term();
		
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			term();
			break;
		case '-':
			match('-');
			term();
			break;
		}
	}
	
	/**
	 * term ::= signed_factor [ mulop factor ]*
	 *
	 */
	private void term() {
		signedFactor();
		
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			factor();
			break;
		case '/':
			match('/');
			factor();
			break;
		}
	}
	
	/**
	 * signed_factor ::= [ addop ] factor
	 *
	 */
	private void signedFactor() {
		
		// CHECK TO SEE IF THERE IS A -/+ UNARY SIGN
		//boolean negative;
		//negative = (look == '-');
		if (isAddOp(look)) {
			Debug.print("" + look);
			nextChar();
			skipWhite();
		}
		
		factor();

	}
	
	/**
	 * factor ::= number | ident | ( expression )
	 *
	 */
	private void factor() {
		if (look == '(') {
			match('(');
			expression();
			match(')');
		} else if (isAlpha(look))
			getName();
		else
			getNum();

	}
	
	/**
	 * ident ::= letter [ letter | digit ]*
	 *
	 */
	private void getName() {
		value = "";

		if (!isAlpha(look))
			expected("Name");
		while (isAlphaNumeric(look)) {
			value += look;
			nextChar();
		}
		value = value.toUpperCase();

		token = 'x';
		skipWhite();
		
		Debug.print(value + " ");
	}
	
	/**
	 * number ::= [digit]+
	 *
	 */
	private void getNum() {
		value = "";
			
		if (!((isNumeric(look)) || ((look == '.') && (value.indexOf('.') == -1))))
			expected("Number");

		while((isNumeric(look)) || ((look == '.') && (value.indexOf('.') == -1))) {
			value += look;
			nextChar();
		}

	     token = '#';
	     skipWhite();
	     
	     Debug.print(value + " ");
	}

	private void scan() {

		int kw;

		getName();
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
	}

	private int lookup(String s) {
		int i;

		for (i = 0; i < kwlist.length; i++) {
			if (kwlist[i].equalsIgnoreCase(s))
				return i;
		}

		return -1;
	}

	/* l� pr�ximo caracter da entrada */
	private void nextChar() {
		if (index < text.length) {
			look = text[index++];
		}
	}
	
	private void skipWhite()
	{
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
		return ( ((c >= '0') && (c <= '9')));
	}

	/* reconhece operador aditivo */
	private boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}

}
