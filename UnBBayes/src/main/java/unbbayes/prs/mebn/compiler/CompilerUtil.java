/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.mebn.compiler;

/*
BNF MEBN Table:
----------------
===============================================================
r
================================================================
 * BNF atual:
 * <b-expression>   = <b-term> [<orop> <b-term>]*
 <b-term>        = <not-factor> [AND <not-factor>]*
 <not-factor>    = [NOT] <b-factor>
 <b-factor>      = <b-literal> | <b-variable> | <relation>
 <relation>      = <expression> [<relop> <expression]
 <expression>    = <term> [<addop> <term>]*
 <term>          = <signed factor> [<mulop> factor]*
 <signed factor> = [<addop>] <factor>
 <factor>        = <number> | <ident> |
 ( <b-expression> )
 <ident>         = <letter> [ <letter> | <digit> ]*
 <number>        = [<digit>]+
 */

public class CompilerUtil {

	private static char look; /* O caracter lido "antecipadamente" (lookahead) */

	private static int index = 0; /* Posi��o de leitura do text */

	private static char[] text;

	public static String newline = System.getProperty("line.separator");

	private static int SYMTBL_SZ = 1000;

	private static int KWLIST_SZ = 4;

	private static char symtbl[] = new char[SYMTBL_SZ]; /* tabela de s�mbolos */

	private static String kwlist[] = { "IF", "ELSE", "ENDIF", "END" }; /* palavras-chave */

	private static char kwcode[] = { 'i', 'l', 'e', 'e' }; /*
															 * c�digo para as
															 * palavras-chave
															 */

	private static int MAXTOKEN = 10;

	private static int MAXNAME = 8;

	private static int MAXNUM = 5;

	private static int MAXOP = 2;

	private static char token; /* token codificado */

	/*
	 * valor do token n�o codificado
	 */
	private static String value; // = new char[MAXTOKEN + 1];

	/* PROGRAMA PRINCIPAL */
	public static void main(String[] args) {
		init(" a = -1   +2 -  a * 3+4       ");
		program();
		
		if (look != '\n')
			expected("NewLine");

	}

	/* inicializa��o do compilador */
	public static void init(String text) {
		System.out.println(text);
		text = text.replaceAll("\\s+", "");
		System.out.println(text);
		CompilerUtil.text = text.toCharArray();
		nextChar();
	}

	public static void program() {
		block();
		matchstring("END");
		// printf("\tint 20h\n");
	}

	public static void block() {
		boolean follow = false;

		do {
			scan();
			switch (token) {
			case 'i':
				doIf();
				break;
			case 'e':
			case 'l':
				follow = true;
				break;
			case '[':
				assignment();
				match(']');
				break;
			}
		} while (!follow);
	}

	public static void scan() {
		int kw;

		getName();
		kw = lookup(value.toString());
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
	}

	private static void doIf() {
		// int l1, l2;

		condition();
		// l1 = newlabel();
		// l2 = l1;
		// printf("\tjz L%d\n", l1);
		block();
		if (token == 'l') {
			// l2 = newlabel();
			// printf("\tjmp L%d\n", l2);
			// printf("L%d:\n", l1);
			block();
		}
		// printf("L%d:\n", l2);
		matchstring("ENDIF");
	}

	public static void condition() {
		// printf("\t## condition ##\n");
	}

	private static int lookup(String s) {
		int i;

		for (i = 0; i < KWLIST_SZ; i++) {
			if (kwlist[i].equalsIgnoreCase(s))
				return i;
		}

		return -1;
	}

	/* l� pr�ximo caracter da entrada */
	public static void nextChar() {
		if (index < text.length)
			look = text[index++];
	}

	/* exibe uma mensagem de erro formatada */
	public static void error(String error) {
		System.err.println("Error: " + error + "\n");
	}

	/* exibe uma mensagem de erro formatada e sai */
	void fatal(String error) {
		System.err.println("Error: " + error + "\n");
		System.exit(1);
	}

	/* alerta sobre alguma entrada esperada */
	public static void expected(String error) {
		System.err.println("Error: " + error + " expected!");
		System.exit(1);
	}

	/* verifica se entrada combina com o esperado */
	public static void match(char c) {
		if (look != c)
			expected("" + c);
		nextChar();
	}

	public static void matchstring(String s) {
		if (!value.equals(s))
			expected(s);
	}

	/* recebe o nome de um identificador */
	public static void getName() {

		while (look == '\n')
			//newline();
		if (!isAlpha(look))
			expected("Name");
		while (isAlphaNumeric(look)) {
			value += look;
			nextChar();
		}
		value.toUpperCase();
		//value[i] = '\0';
		token = 'x';
		//skipwhite();
	}

	/* recebe um n�mero inteiro */
	public static void getNum() {
		
		if (!isNumeric(look))
			expected("Integer");
		// for (i = 0; isNumeric(look) && i < MAXNUM; i++) {
		while(isNumeric(look)) {
			value += look;
			nextChar();
		}
		// value[i] = '\0';
	     token = '#';
	     // skipwhite();
	}

	/* emite uma instru��o seguida por uma nova linha */
	public static void emit(String instruction) {
		System.out.println(instruction + newline);
	}

	public static boolean isAlpha(final char c) {
		if ((c >= 'a') && (c <= 'z'))
			return true; // lowercase
		if ((c >= 'A') && (c <= 'Z'))
			return true; // uppercase
		return false;
	}

	public static boolean isAlphaNumeric(final char c) {
		if (isAlpha(c))
			return true; // uppercase
		if (isNumeric(c))
			return true; // numeric
		return false;
	}

	public static boolean isNumeric(final char c) {
		return ((c >= '0') && (c <= '9'));
	}

	/* reconhece operador aditivo */
	public static boolean isAddOp(char c) {
		return (c == '+' || c == '-');
	}

	/* reconhece operador multiplicativo */
	public static boolean isMulOp(char c) {
		return (c == '*' || c == '/');
	}

	/* analisa e traduz um fator */
	public static void factor() {
		if (look == '(') {
			match('(');
			expression();
			match(')');
		} else if (isAlpha(look))
			ident();
		else
			getNum();
    		// emit("\tmov ax, " + value);

	}

	/* analisa e traduz um identificador */
	public static void ident() {
		getName();
		if (look == '(') {
			match('(');
			match(')');
			// emit("CALL " + value);
		} // else
			// emit("MOV AX, " + value);
	}

	/* reconhece e traduz uma multiplica��o */
	public static void doMul() {
		match('*');
		factor();
		// emit("POP BX");
		// emit("IMUL BX");
	}

	/* reconhece e traduz uma divis�o */
	public static void doDiv() {
		match('/');
		factor();
		// emit("POP BX");
		// emit("XCHG AX, BX");
		// emit("CWD");
		// emit("IDIV BX");
	}

	/* reconhece e traduz uma adi��o */
	public static void doAdd() {
		match('+');
		term();
		// emit("POP BX");
		// emit("ADD AX, BX");
	}

	/* reconhece e traduz uma subtra��o */
	public static void doSub() {
		match('-');
		term();
		// emit("POP BX");
		// emit("SUB AX, BX");
		// emit("NEG AX");
	}

	/* analisa e traduz um termo */
	public static void term() {
		factor();
		term1();
	}

	public static void term1() {
		while (isMulOp(look)) {
			// printf("\tpush ax\n");
			switch (look) {
			case '*':
				doMul();
				break;
			case '/':
				doDiv();
				break;
			}
		}
	}

	public static void signedFactor() {
		boolean s;

		s = (look == '-');
		if (isAddOp(look)) {
			nextChar();
			// skipWhite();
		}
		factor();
		// if (s) TODO: NEGAR
		// printf("\tneg ax\n");
	}

	public static void firstTerm() {
		signedFactor();
		term1();
	}

	/* analisa e traduz uma express�o */
	public static void expression() {
		firstTerm();
		while (isAddOp(look)) {
			// printf("\tpush ax\n");
			switch (look) {
			case '+':
				doAdd();
				break;
			case '-':
				doSub();
				break;
			}
		}
	}

	/* analisa e traduz um comando de atribui��o */
	public static void assignment() {
		String name = value;

		match('=');
		expression();
		switch (look) {
		case ',':
			assignment();
			break;
		
		}
		// emit("MOV " + name + ", AX");
	}

}
