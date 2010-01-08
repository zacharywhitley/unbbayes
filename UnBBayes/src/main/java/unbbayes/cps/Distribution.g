grammar Distribution;

options { 
  output = AST;
   
} 
/**/
@header{
package UMath; 
}

@lexer::header{
package UMath; 
}
  
program	:  statement*
    	; 
/*
method
scope {
  String name; 
}
 //   :   'Prob' '(' thisnode '|' parentnodes ')' '='
     
 
    	{
    		$method::name=$ID.text; 
    		Float x = Float.parseFloat($parax.text);
    		Float a = Float.parseFloat($paraa.text);
    		Float b = Float.parseFloat($parab.text);
    		Float y = 0.0f;
    		
    	 	y = math.normalDist(x, a, b);

    		
    		System.out.println($ID.text + "(" + x+ "," +a+ ","+b+ ") = " + y );
    	}
    	
     body
    ; */

// STATEMENTS / BLOCKS

block
    :   '{' blockStatement* '}' -> ^(SLIST blockStatement* )
    ;
    
blockStatement
    : //  localVariableDeclarationStatement
  //  |   classOrInterfaceDeclaration
   // |  
    statement
    ;
     
    
statement
    : block
//    |   ASSERT expression (':' expression)? ';' 
      |   'if'^ ifExpression statement (options {k=1;}:'else'! statement)? 
//    |   'for' '(' forControl ')' statement
//    |   'while' parExpression statement
//    |   'do' statement 'while' parExpression ';'
/*    |   'try' block
        ( catches 'finally' block
        | catches
        |   'finally' block
        )
    |   'switch' parExpression '{' switchBlockStatementGroups '}'
    |   'synchronized' parExpression block */
    |   'print' arguments? ';'				-> ^( 'print' arguments )		
    |   'return' expression? ';'
    |   'throw' expression ';'
    |   'break' Identifier? ';'
    |   'continue' Identifier? ';'
    |   ';'!
    |   statementExpression ';'!
    |   Identifier ':' statement
    ;


statementExpression
    :   expression
    ;
    
constantExpression
    :   expression
    
    ;
 
ifExpression
    :   '(' expression ')' -> ^(EXP expression)
    ;
     

parExpression
    :   '('! expression ')'! 
    ;

expressionList
    :   expression (','! expression)*
    ;  
      
arguments
    :   '('! expressionList? ')'!
    ;
        
expression
    :   conditionalExpression (assignmentOperator^ expression)?
    ;
       
expression2
    :   conditionalExpression (assignmentOperator! expression)?
    ;
        
assignmentOperator
    :   '='
    |   '+='
    |   '-='
    |   '*='
    |   '/='
    |   '&='
    |   '|='
    |   '^='
    |   '%='
  /*  |   ('<' '<' '=')=> t1='<' t2='<' t3='=' 
        { $t1.getLine() == $t2.getLine() &&
          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && 
          $t2.getLine() == $t3.getLine() && 
          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() }?
    |   ('>' '>' '>' '=')=> t1='>' t2='>' t3='>' t4='='
        { $t1.getLine() == $t2.getLine() && 
          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&
          $t2.getLine() == $t3.getLine() && 
          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() &&
          $t3.getLine() == $t4.getLine() && 
          $t3.getCharPositionInLine() + 1 == $t4.getCharPositionInLine() }?
    |   ('>' '>' '=')=> t1='>' t2='>' t3='='
        { $t1.getLine() == $t2.getLine() && 
          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && 
          $t2.getLine() == $t3.getLine() && 
          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() }?
   */ ;    
    
type	:	 primitiveType ('[' ']')*
	;
	
primitiveType
    :   'boolean'
    |   'char'
    |   'byte'
    |   'short'
    |   'int'
    |   'long'
    |   'float'
    |   'double'
    |	'number'
    |	'string'
    ;		
 
  
conditionalExpression
    :   conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )?
    ;

conditionalOrExpression
    :   conditionalAndExpression ( '||'^ conditionalAndExpression )*
    ;

conditionalAndExpression
    :   inclusiveOrExpression ( '&&'^ inclusiveOrExpression )*
    ;

inclusiveOrExpression
    :   exclusiveOrExpression ( '|'^ exclusiveOrExpression )*
    ;

exclusiveOrExpression
    :   andExpression ( '^'^ andExpression )*
    ;

andExpression
    :   equalityExpression ( '&'^ equalityExpression )*
    ;

equalityExpression
    :   relationalExpression ( ('==' | '!=')^ relationalExpression )*
    ;
 
relationalExpression
    :   additiveExpression ( relationalOp^ additiveExpression )* 
    ;
    
relationalOp
    :   '<='
    |   '>='
    |   '<' 
    |   '>' 
    ;
     

elementValue
    :   conditionalExpression
//    |   annotation
    |   elementValueArrayInitializer
    ;

additiveExpression
    :   multiplicativeExpression ( ('+' | '-')^ multiplicativeExpression )*
    ;

multiplicativeExpression
    :   unaryExpression ( ( '*' | '/' | '%' )^ unaryExpression )*
    ;
    
unaryExpression
    :   '+'^ unaryExpression
    |   '-'^ unaryExpression   
    |   '++' unaryExpression
    |   '--' unaryExpression
    |   unaryExpressionNotPlusMinus
    ;

unaryExpressionNotPlusMinus
    :   '~' unaryExpression
    |   '!' unaryExpression
//    |   castExpression
//    |   primary selector* ('++'|'--')?
    |   primary 	{ System.out.println($primary.text  );}  
    ;

elementValueArrayInitializer
    :   '{' (elementValue (',' elementValue)*)? (',')? '}'
    ;
  
    

literal 
    :   integerLiteral
    |   FloatingPointLiteral
//    |   CharacterLiteral
//    |   StringLiteral
    |   booleanLiteral
    |   'null'
    ;


primary
    :  parExpression | 
    'this' ('.' Identifier)* identifierSuffix?
//    |   'super' superSuffix
    |   literal
 //  |   'new' (nonWildcardTypeArguments createdName classCreatorRest
    |   createdName// (arrayCreatorRest | 'classCreatorRest')
    |   STRING_LITERAL
    |   Identifier^ arguments 
    |   Identifier ('.'! Identifier^)* identifierSuffix?
    |    ('[' ']')* '.' 'class'
    |   'void' '.' 'class'				
    ; 
    
createdName
    : //  classOrInterfaceType
      primitiveType expression2  -> ^(VAR primitiveType expression2 )
    ;
 
    
identifierSuffix
    :   ('[' ']')+ '.' 'class' 	-> 'class'
    |   ('[' expression ']')+ 	-> expression// can also be matched by selector, but do here
//    |   arguments
//    |   '.' 'class'
//    |   '.' explicitGenericInvocation
//    |   '.' 'this'
//    |   '.' 'super' arguments
//   |   '.' 'new' innerCreator
    ;
    
integerLiteral
    :    DecimalLiteral	-> DecimalLiteral
    ;

booleanLiteral
    :   'true'		-> 'true'
    |   'false'		-> 'false'
    ; 



EXP	: 'EXP'	;
VAR	: 'VAR'	;
SLIST	: 'SLIST'	;
	
DecimalLiteral : ('0' | '1'..'9' '0'..'9'*);

FloatingPointLiteral
    :   ('0'..'9')+ '.' ('0'..'9')* //Exponent? FloatTypeSuffix?
//      |   '.' ('0'..'9')//+ Exponent? FloatTypeSuffix?
//    |   ('0'..'9')+ Exponent FloatTypeSuffix?
//    |   ('0'..'9')+ FloatTypeSuffix
    ;

STRING_LITERAL
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;
     
Identifier 
    :   Letter (Letter|JavaIDDigit)* 
    ;
       
fragment
Letter
    :  '\u0024' |
       '\u0041'..'\u005a' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

fragment
JavaIDDigit
    :  '\u0030'..'\u0039' |
       '\u0660'..'\u0669' |
       '\u06f0'..'\u06f9' |
       '\u0966'..'\u096f' |
       '\u09e6'..'\u09ef' |
       '\u0a66'..'\u0a6f' |
       '\u0ae6'..'\u0aef' |
       '\u0b66'..'\u0b6f' |
       '\u0be7'..'\u0bef' |
       '\u0c66'..'\u0c6f' |
       '\u0ce6'..'\u0cef' |
       '\u0d66'..'\u0d6f' |
       '\u0e50'..'\u0e59' |
       '\u0ed0'..'\u0ed9' |
       '\u1040'..'\u1049'
   ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   OctalEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;
    
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;


COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
