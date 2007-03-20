package unbbayes.prs.mebn.compiler;

/*
 * BNF:
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor> [ <mulop> <factor> ]*
 * <factor> ::= <number> | '(' <expression> ')' | <variable>
 * <ident> = <expression>
 */

public class CompilerUtil {

	private static char look; /* O caracter lido "antecipadamente" (lookahead) */

	private static int index = 0; /* Posição de leitura do text */

	private static char[] text;

	public static String newline = System.getProperty("line.separator");

	/* PROGRAMA PRINCIPAL */
	public static void main(String[] args) {
		init(" a = -1   +2 -  a * 3+4       ");
		assignment();
		if (look != '\n')
            expected("NewLine");

	}

	/* inicialização do compilador */
	public static void init(String text) {
		System.out.println(text);
		text = text.replaceAll("\\s+", "");
		System.out.println(text);
		CompilerUtil.text = text.toCharArray();
		nextChar();
	}

	/* lê próximo caracter da entrada */
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

	/* recebe o nome de um identificador */
	public static char getName() {
		char name;

		if (!isAlpha(look))
			expected("Name");
		name = ("" + look).toUpperCase().charAt(0);
		nextChar();

		return name;
	}

	/* recebe um número inteiro */
	public static char getNum() {
		char num;

		if (!isNumeric(look))
			expected("Integer");
		num = look;
		nextChar();

		return num;
	}

	/* emite uma instrução seguida por uma nova linha */
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

	/* analisa e traduz um fator */
	public static void factor() {
		if (look == '(') {
			match('(');
			expression();
			match(')');
		} else if (isAlpha(look))
			ident();
		else
			emit("MOV AX, " + getNum());

	}
	
	/* analisa e traduz um identificador */
	public static void ident()
	{
	        char name;

	        name = getName();
	        if (look == '(') {
	                match('(');
	                match(')');
	                emit("CALL " + name);
	        } else
	                emit("MOV AX, " + name);
	}



	/* reconhece e traduz uma multiplicação */
	public static void multiply() {
		match('*');
		factor();
		emit("POP BX");
		emit("IMUL BX");
	}

	/* reconhece e traduz uma divisão */
	public static void divide() {
		match('/');
		factor();
		emit("POP BX");
		emit("XCHG AX, BX");
		emit("CWD");
		emit("IDIV BX");
	}

	/* analisa e traduz um termo */
	public static void term() {
		factor();
		while (look == '*' || look == '/') {
			emit("PUSH AX");
			switch (look) {
			case '*':
				multiply();
				break;
			case '/':
				divide();
				break;
			}
		}
	}

	/* reconhece e traduz uma adição */
	public static void add() {
		match('+');
		term();
		emit("POP BX");
		emit("ADD AX, BX");
	}

	/* reconhece e traduz uma subtração */
	public static void subtract() {
		match('-');
		term();
		emit("POP BX");
		emit("SUB AX, BX");
		emit("NEG AX");
	}

	/* analisa e traduz uma expressão */
	public static void expression() {
		if (isAddOp(look))
			emit("XOR AX, AX");
		else
			term();
		while (isAddOp(look)) {
			emit("PUSH AX");
			switch (look) {
			case '+':
				add();
				break;
			case '-':
				subtract();
				break;
			}
		}
	}
	
	/* analisa e traduz um comando de atribuição */
	public static void assignment()
	{
		char name;

		name = getName();
		match('=');
		expression();
		emit("MOV " + name + ", AX");
	}

}
