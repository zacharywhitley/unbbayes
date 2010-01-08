// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g 2009-12-29 10:11:52

package unbbayes.cps; 


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class DistributionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Identifier", "FloatingPointLiteral", "STRING_LITERAL", "DecimalLiteral", "EXP", "VAR", "SLIST", "EscapeSequence", "Letter", "JavaIDDigit", "OctalEscape", "WS", "COMMENT", "LINE_COMMENT", "'{'", "'}'", "'if'", "'else'", "'print'", "';'", "'return'", "'throw'", "'break'", "'continue'", "':'", "'('", "')'", "','", "'='", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'['", "']'", "'boolean'", "'char'", "'byte'", "'short'", "'int'", "'long'", "'float'", "'double'", "'number'", "'string'", "'?'", "'||'", "'&&'", "'|'", "'^'", "'&'", "'=='", "'!='", "'<='", "'>='", "'<'", "'>'", "'+'", "'-'", "'*'", "'/'", "'%'", "'++'", "'--'", "'~'", "'!'", "'null'", "'this'", "'.'", "'class'", "'void'", "'true'", "'false'"
    };
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__64=64;
    public static final int T__29=29;
    public static final int T__65=65;
    public static final int T__28=28;
    public static final int T__62=62;
    public static final int T__27=27;
    public static final int T__63=63;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int T__60=60;
    public static final int EOF=-1;
    public static final int Identifier=4;
    public static final int T__55=55;
    public static final int T__19=19;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int STRING_LITERAL=6;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__18=18;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int EXP=8;
    public static final int T__59=59;
    public static final int SLIST=10;
    public static final int VAR=9;
    public static final int COMMENT=16;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__80=80;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int LINE_COMMENT=17;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int DecimalLiteral=7;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=15;
    public static final int T__71=71;
    public static final int T__33=33;
    public static final int T__72=72;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__70=70;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int FloatingPointLiteral=5;
    public static final int JavaIDDigit=13;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int OctalEscape=14;
    public static final int Letter=12;
    public static final int EscapeSequence=11;
    public static final int T__73=73;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;

    // delegates
    // delegators


        public DistributionParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DistributionParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return DistributionParser.tokenNames; }
    public String getGrammarFileName() { return "Distribution.g"; }


    public static class program_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "program"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:1: program : ( statement )* ;
    public final DistributionParser.program_return program() throws RecognitionException {
        DistributionParser.program_return retval = new DistributionParser.program_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.statement_return statement1 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:9: ( ( statement )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:12: ( statement )*
            {
            root_0 = (Object)adaptor.nil();

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:12: ( statement )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=Identifier && LA1_0<=DecimalLiteral)||LA1_0==18||LA1_0==20||(LA1_0>=22 && LA1_0<=27)||LA1_0==29||LA1_0==41||(LA1_0>=43 && LA1_0<=52)||(LA1_0>=65 && LA1_0<=66)||(LA1_0>=70 && LA1_0<=76)||(LA1_0>=78 && LA1_0<=80)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:12: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_program47);
            	    statement1=statement();

            	    state._fsp--;

            	    adaptor.addChild(root_0, statement1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "program"

    public static class block_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "block"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:44:1: block : '{' ( blockStatement )* '}' -> ^( SLIST ( blockStatement )* ) ;
    public final DistributionParser.block_return block() throws RecognitionException {
        DistributionParser.block_return retval = new DistributionParser.block_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal2=null;
        Token char_literal4=null;
        DistributionParser.blockStatement_return blockStatement3 = null;


        Object char_literal2_tree=null;
        Object char_literal4_tree=null;
        RewriteRuleTokenStream stream_19=new RewriteRuleTokenStream(adaptor,"token 19");
        RewriteRuleTokenStream stream_18=new RewriteRuleTokenStream(adaptor,"token 18");
        RewriteRuleSubtreeStream stream_blockStatement=new RewriteRuleSubtreeStream(adaptor,"rule blockStatement");
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:5: ( '{' ( blockStatement )* '}' -> ^( SLIST ( blockStatement )* ) )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:9: '{' ( blockStatement )* '}'
            {
            char_literal2=(Token)match(input,18,FOLLOW_18_in_block73);  
            stream_18.add(char_literal2);

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:13: ( blockStatement )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=Identifier && LA2_0<=DecimalLiteral)||LA2_0==18||LA2_0==20||(LA2_0>=22 && LA2_0<=27)||LA2_0==29||LA2_0==41||(LA2_0>=43 && LA2_0<=52)||(LA2_0>=65 && LA2_0<=66)||(LA2_0>=70 && LA2_0<=76)||(LA2_0>=78 && LA2_0<=80)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:13: blockStatement
            	    {
            	    pushFollow(FOLLOW_blockStatement_in_block75);
            	    blockStatement3=blockStatement();

            	    state._fsp--;

            	    stream_blockStatement.add(blockStatement3.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            char_literal4=(Token)match(input,19,FOLLOW_19_in_block78);  
            stream_19.add(char_literal4);



            // AST REWRITE
            // elements: blockStatement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 45:33: -> ^( SLIST ( blockStatement )* )
            {
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:36: ^( SLIST ( blockStatement )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SLIST, "SLIST"), root_1);

                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:44: ( blockStatement )*
                while ( stream_blockStatement.hasNext() ) {
                    adaptor.addChild(root_1, stream_blockStatement.nextTree());

                }
                stream_blockStatement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "block"

    public static class blockStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blockStatement"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:48:1: blockStatement : statement ;
    public final DistributionParser.blockStatement_return blockStatement() throws RecognitionException {
        DistributionParser.blockStatement_return retval = new DistributionParser.blockStatement_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.statement_return statement5 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:49:5: ( statement )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:52:5: statement
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_statement_in_blockStatement121);
            statement5=statement();

            state._fsp--;

            adaptor.addChild(root_0, statement5.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "blockStatement"

    public static class statement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:56:1: statement : ( block | 'if' ifExpression statement ( options {k=1; } : 'else' statement )? | 'print' ( arguments )? ';' -> ^( 'print' arguments ) | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );
    public final DistributionParser.statement_return statement() throws RecognitionException {
        DistributionParser.statement_return retval = new DistributionParser.statement_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal7=null;
        Token string_literal10=null;
        Token string_literal12=null;
        Token char_literal14=null;
        Token string_literal15=null;
        Token char_literal17=null;
        Token string_literal18=null;
        Token char_literal20=null;
        Token string_literal21=null;
        Token Identifier22=null;
        Token char_literal23=null;
        Token string_literal24=null;
        Token Identifier25=null;
        Token char_literal26=null;
        Token char_literal27=null;
        Token char_literal29=null;
        Token Identifier30=null;
        Token char_literal31=null;
        DistributionParser.block_return block6 = null;

        DistributionParser.ifExpression_return ifExpression8 = null;

        DistributionParser.statement_return statement9 = null;

        DistributionParser.statement_return statement11 = null;

        DistributionParser.arguments_return arguments13 = null;

        DistributionParser.expression_return expression16 = null;

        DistributionParser.expression_return expression19 = null;

        DistributionParser.statementExpression_return statementExpression28 = null;

        DistributionParser.statement_return statement32 = null;


        Object string_literal7_tree=null;
        Object string_literal10_tree=null;
        Object string_literal12_tree=null;
        Object char_literal14_tree=null;
        Object string_literal15_tree=null;
        Object char_literal17_tree=null;
        Object string_literal18_tree=null;
        Object char_literal20_tree=null;
        Object string_literal21_tree=null;
        Object Identifier22_tree=null;
        Object char_literal23_tree=null;
        Object string_literal24_tree=null;
        Object Identifier25_tree=null;
        Object char_literal26_tree=null;
        Object char_literal27_tree=null;
        Object char_literal29_tree=null;
        Object Identifier30_tree=null;
        Object char_literal31_tree=null;
        RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleSubtreeStream stream_arguments=new RewriteRuleSubtreeStream(adaptor,"rule arguments");
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:57:5: ( block | 'if' ifExpression statement ( options {k=1; } : 'else' statement )? | 'print' ( arguments )? ';' -> ^( 'print' arguments ) | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement )
            int alt8=10;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:57:7: block
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_block_in_statement148);
                    block6=block();

                    state._fsp--;

                    adaptor.addChild(root_0, block6.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:59:11: 'if' ifExpression statement ( options {k=1; } : 'else' statement )?
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal7=(Token)match(input,20,FOLLOW_20_in_statement161); 
                    string_literal7_tree = (Object)adaptor.create(string_literal7);
                    root_0 = (Object)adaptor.becomeRoot(string_literal7_tree, root_0);

                    pushFollow(FOLLOW_ifExpression_in_statement164);
                    ifExpression8=ifExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, ifExpression8.getTree());
                    pushFollow(FOLLOW_statement_in_statement166);
                    statement9=statement();

                    state._fsp--;

                    adaptor.addChild(root_0, statement9.getTree());
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:59:40: ( options {k=1; } : 'else' statement )?
                    int alt3=2;
                    alt3 = dfa3.predict(input);
                    switch (alt3) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:59:56: 'else' statement
                            {
                            string_literal10=(Token)match(input,21,FOLLOW_21_in_statement176); 
                            pushFollow(FOLLOW_statement_in_statement179);
                            statement11=statement();

                            state._fsp--;

                            adaptor.addChild(root_0, statement11.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:70:9: 'print' ( arguments )? ';'
                    {
                    string_literal12=(Token)match(input,22,FOLLOW_22_in_statement197);  
                    stream_22.add(string_literal12);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:70:17: ( arguments )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==29) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:70:17: arguments
                            {
                            pushFollow(FOLLOW_arguments_in_statement199);
                            arguments13=arguments();

                            state._fsp--;

                            stream_arguments.add(arguments13.getTree());

                            }
                            break;

                    }

                    char_literal14=(Token)match(input,23,FOLLOW_23_in_statement202);  
                    stream_23.add(char_literal14);



                    // AST REWRITE
                    // elements: arguments, 22
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 70:35: -> ^( 'print' arguments )
                    {
                        // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:70:38: ^( 'print' arguments )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(stream_22.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_arguments.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:71:9: 'return' ( expression )? ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal15=(Token)match(input,24,FOLLOW_24_in_statement227); 
                    string_literal15_tree = (Object)adaptor.create(string_literal15);
                    adaptor.addChild(root_0, string_literal15_tree);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:71:18: ( expression )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( ((LA5_0>=Identifier && LA5_0<=DecimalLiteral)||LA5_0==29||LA5_0==41||(LA5_0>=43 && LA5_0<=52)||(LA5_0>=65 && LA5_0<=66)||(LA5_0>=70 && LA5_0<=76)||(LA5_0>=78 && LA5_0<=80)) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:71:18: expression
                            {
                            pushFollow(FOLLOW_expression_in_statement229);
                            expression16=expression();

                            state._fsp--;

                            adaptor.addChild(root_0, expression16.getTree());

                            }
                            break;

                    }

                    char_literal17=(Token)match(input,23,FOLLOW_23_in_statement232); 
                    char_literal17_tree = (Object)adaptor.create(char_literal17);
                    adaptor.addChild(root_0, char_literal17_tree);


                    }
                    break;
                case 5 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:72:9: 'throw' expression ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal18=(Token)match(input,25,FOLLOW_25_in_statement242); 
                    string_literal18_tree = (Object)adaptor.create(string_literal18);
                    adaptor.addChild(root_0, string_literal18_tree);

                    pushFollow(FOLLOW_expression_in_statement244);
                    expression19=expression();

                    state._fsp--;

                    adaptor.addChild(root_0, expression19.getTree());
                    char_literal20=(Token)match(input,23,FOLLOW_23_in_statement246); 
                    char_literal20_tree = (Object)adaptor.create(char_literal20);
                    adaptor.addChild(root_0, char_literal20_tree);


                    }
                    break;
                case 6 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:73:9: 'break' ( Identifier )? ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal21=(Token)match(input,26,FOLLOW_26_in_statement256); 
                    string_literal21_tree = (Object)adaptor.create(string_literal21);
                    adaptor.addChild(root_0, string_literal21_tree);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:73:17: ( Identifier )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==Identifier) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:73:17: Identifier
                            {
                            Identifier22=(Token)match(input,Identifier,FOLLOW_Identifier_in_statement258); 
                            Identifier22_tree = (Object)adaptor.create(Identifier22);
                            adaptor.addChild(root_0, Identifier22_tree);


                            }
                            break;

                    }

                    char_literal23=(Token)match(input,23,FOLLOW_23_in_statement261); 
                    char_literal23_tree = (Object)adaptor.create(char_literal23);
                    adaptor.addChild(root_0, char_literal23_tree);


                    }
                    break;
                case 7 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:74:9: 'continue' ( Identifier )? ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal24=(Token)match(input,27,FOLLOW_27_in_statement271); 
                    string_literal24_tree = (Object)adaptor.create(string_literal24);
                    adaptor.addChild(root_0, string_literal24_tree);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:74:20: ( Identifier )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==Identifier) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:74:20: Identifier
                            {
                            Identifier25=(Token)match(input,Identifier,FOLLOW_Identifier_in_statement273); 
                            Identifier25_tree = (Object)adaptor.create(Identifier25);
                            adaptor.addChild(root_0, Identifier25_tree);


                            }
                            break;

                    }

                    char_literal26=(Token)match(input,23,FOLLOW_23_in_statement276); 
                    char_literal26_tree = (Object)adaptor.create(char_literal26);
                    adaptor.addChild(root_0, char_literal26_tree);


                    }
                    break;
                case 8 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:75:9: ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal27=(Token)match(input,23,FOLLOW_23_in_statement286); 

                    }
                    break;
                case 9 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:76:9: statementExpression ';'
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_statementExpression_in_statement297);
                    statementExpression28=statementExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, statementExpression28.getTree());
                    char_literal29=(Token)match(input,23,FOLLOW_23_in_statement299); 

                    }
                    break;
                case 10 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:77:9: Identifier ':' statement
                    {
                    root_0 = (Object)adaptor.nil();

                    Identifier30=(Token)match(input,Identifier,FOLLOW_Identifier_in_statement310); 
                    Identifier30_tree = (Object)adaptor.create(Identifier30);
                    adaptor.addChild(root_0, Identifier30_tree);

                    char_literal31=(Token)match(input,28,FOLLOW_28_in_statement312); 
                    char_literal31_tree = (Object)adaptor.create(char_literal31);
                    adaptor.addChild(root_0, char_literal31_tree);

                    pushFollow(FOLLOW_statement_in_statement314);
                    statement32=statement();

                    state._fsp--;

                    adaptor.addChild(root_0, statement32.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement"

    public static class statementExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statementExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:81:1: statementExpression : expression ;
    public final DistributionParser.statementExpression_return statementExpression() throws RecognitionException {
        DistributionParser.statementExpression_return retval = new DistributionParser.statementExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.expression_return expression33 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:82:5: ( expression )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:82:9: expression
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_expression_in_statementExpression334);
            expression33=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression33.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statementExpression"

    public static class constantExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constantExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:85:1: constantExpression : expression ;
    public final DistributionParser.constantExpression_return constantExpression() throws RecognitionException {
        DistributionParser.constantExpression_return retval = new DistributionParser.constantExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.expression_return expression34 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:86:5: ( expression )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:86:9: expression
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_expression_in_constantExpression357);
            expression34=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression34.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constantExpression"

    public static class ifExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ifExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:90:1: ifExpression : '(' expression ')' -> ^( EXP expression ) ;
    public final DistributionParser.ifExpression_return ifExpression() throws RecognitionException {
        DistributionParser.ifExpression_return retval = new DistributionParser.ifExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal35=null;
        Token char_literal37=null;
        DistributionParser.expression_return expression36 = null;


        Object char_literal35_tree=null;
        Object char_literal37_tree=null;
        RewriteRuleTokenStream stream_30=new RewriteRuleTokenStream(adaptor,"token 30");
        RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:91:5: ( '(' expression ')' -> ^( EXP expression ) )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:91:9: '(' expression ')'
            {
            char_literal35=(Token)match(input,29,FOLLOW_29_in_ifExpression382);  
            stream_29.add(char_literal35);

            pushFollow(FOLLOW_expression_in_ifExpression384);
            expression36=expression();

            state._fsp--;

            stream_expression.add(expression36.getTree());
            char_literal37=(Token)match(input,30,FOLLOW_30_in_ifExpression386);  
            stream_30.add(char_literal37);



            // AST REWRITE
            // elements: expression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 91:28: -> ^( EXP expression )
            {
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:91:31: ^( EXP expression )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXP, "EXP"), root_1);

                adaptor.addChild(root_1, stream_expression.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ifExpression"

    public static class parExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:95:1: parExpression : '(' expression ')' ;
    public final DistributionParser.parExpression_return parExpression() throws RecognitionException {
        DistributionParser.parExpression_return retval = new DistributionParser.parExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal38=null;
        Token char_literal40=null;
        DistributionParser.expression_return expression39 = null;


        Object char_literal38_tree=null;
        Object char_literal40_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:96:5: ( '(' expression ')' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:96:9: '(' expression ')'
            {
            root_0 = (Object)adaptor.nil();

            char_literal38=(Token)match(input,29,FOLLOW_29_in_parExpression419); 
            pushFollow(FOLLOW_expression_in_parExpression422);
            expression39=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression39.getTree());
            char_literal40=(Token)match(input,30,FOLLOW_30_in_parExpression424); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parExpression"

    public static class expressionList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expressionList"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:99:1: expressionList : expression ( ',' expression )* ;
    public final DistributionParser.expressionList_return expressionList() throws RecognitionException {
        DistributionParser.expressionList_return retval = new DistributionParser.expressionList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal42=null;
        DistributionParser.expression_return expression41 = null;

        DistributionParser.expression_return expression43 = null;


        Object char_literal42_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:100:5: ( expression ( ',' expression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:100:9: expression ( ',' expression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_expression_in_expressionList445);
            expression41=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression41.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:100:20: ( ',' expression )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==31) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:100:21: ',' expression
            	    {
            	    char_literal42=(Token)match(input,31,FOLLOW_31_in_expressionList448); 
            	    pushFollow(FOLLOW_expression_in_expressionList451);
            	    expression43=expression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, expression43.getTree());

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expressionList"

    public static class arguments_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arguments"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:103:1: arguments : '(' ( expressionList )? ')' ;
    public final DistributionParser.arguments_return arguments() throws RecognitionException {
        DistributionParser.arguments_return retval = new DistributionParser.arguments_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal44=null;
        Token char_literal46=null;
        DistributionParser.expressionList_return expressionList45 = null;


        Object char_literal44_tree=null;
        Object char_literal46_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:104:5: ( '(' ( expressionList )? ')' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:104:9: '(' ( expressionList )? ')'
            {
            root_0 = (Object)adaptor.nil();

            char_literal44=(Token)match(input,29,FOLLOW_29_in_arguments480); 
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:104:14: ( expressionList )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=Identifier && LA10_0<=DecimalLiteral)||LA10_0==29||LA10_0==41||(LA10_0>=43 && LA10_0<=52)||(LA10_0>=65 && LA10_0<=66)||(LA10_0>=70 && LA10_0<=76)||(LA10_0>=78 && LA10_0<=80)) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:104:14: expressionList
                    {
                    pushFollow(FOLLOW_expressionList_in_arguments483);
                    expressionList45=expressionList();

                    state._fsp--;

                    adaptor.addChild(root_0, expressionList45.getTree());

                    }
                    break;

            }

            char_literal46=(Token)match(input,30,FOLLOW_30_in_arguments486); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arguments"

    public static class expression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:107:1: expression : conditionalExpression ( assignmentOperator expression )? ;
    public final DistributionParser.expression_return expression() throws RecognitionException {
        DistributionParser.expression_return retval = new DistributionParser.expression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.conditionalExpression_return conditionalExpression47 = null;

        DistributionParser.assignmentOperator_return assignmentOperator48 = null;

        DistributionParser.expression_return expression49 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:108:5: ( conditionalExpression ( assignmentOperator expression )? )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:108:9: conditionalExpression ( assignmentOperator expression )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_conditionalExpression_in_expression514);
            conditionalExpression47=conditionalExpression();

            state._fsp--;

            adaptor.addChild(root_0, conditionalExpression47.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:108:31: ( assignmentOperator expression )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( ((LA11_0>=32 && LA11_0<=40)) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:108:32: assignmentOperator expression
                    {
                    pushFollow(FOLLOW_assignmentOperator_in_expression517);
                    assignmentOperator48=assignmentOperator();

                    state._fsp--;

                    root_0 = (Object)adaptor.becomeRoot(assignmentOperator48.getTree(), root_0);
                    pushFollow(FOLLOW_expression_in_expression520);
                    expression49=expression();

                    state._fsp--;

                    adaptor.addChild(root_0, expression49.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class expression2_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression2"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:111:1: expression2 : conditionalExpression ( assignmentOperator expression )? ;
    public final DistributionParser.expression2_return expression2() throws RecognitionException {
        DistributionParser.expression2_return retval = new DistributionParser.expression2_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.conditionalExpression_return conditionalExpression50 = null;

        DistributionParser.assignmentOperator_return assignmentOperator51 = null;

        DistributionParser.expression_return expression52 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:112:5: ( conditionalExpression ( assignmentOperator expression )? )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:112:9: conditionalExpression ( assignmentOperator expression )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_conditionalExpression_in_expression2548);
            conditionalExpression50=conditionalExpression();

            state._fsp--;

            adaptor.addChild(root_0, conditionalExpression50.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:112:31: ( assignmentOperator expression )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=32 && LA12_0<=40)) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:112:32: assignmentOperator expression
                    {
                    pushFollow(FOLLOW_assignmentOperator_in_expression2551);
                    assignmentOperator51=assignmentOperator();

                    state._fsp--;

                    pushFollow(FOLLOW_expression_in_expression2554);
                    expression52=expression();

                    state._fsp--;

                    adaptor.addChild(root_0, expression52.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression2"

    public static class assignmentOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignmentOperator"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:115:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' );
    public final DistributionParser.assignmentOperator_return assignmentOperator() throws RecognitionException {
        DistributionParser.assignmentOperator_return retval = new DistributionParser.assignmentOperator_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set53=null;

        Object set53_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:116:5: ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            {
            root_0 = (Object)adaptor.nil();

            set53=(Token)input.LT(1);
            if ( (input.LA(1)>=32 && input.LA(1)<=40) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set53));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignmentOperator"

    public static class type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:144:1: type : primitiveType ( '[' ']' )* ;
    public final DistributionParser.type_return type() throws RecognitionException {
        DistributionParser.type_return retval = new DistributionParser.type_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal55=null;
        Token char_literal56=null;
        DistributionParser.primitiveType_return primitiveType54 = null;


        Object char_literal55_tree=null;
        Object char_literal56_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:144:6: ( primitiveType ( '[' ']' )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:144:9: primitiveType ( '[' ']' )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_primitiveType_in_type685);
            primitiveType54=primitiveType();

            state._fsp--;

            adaptor.addChild(root_0, primitiveType54.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:144:23: ( '[' ']' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==41) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:144:24: '[' ']'
            	    {
            	    char_literal55=(Token)match(input,41,FOLLOW_41_in_type688); 
            	    char_literal55_tree = (Object)adaptor.create(char_literal55);
            	    adaptor.addChild(root_0, char_literal55_tree);

            	    char_literal56=(Token)match(input,42,FOLLOW_42_in_type690); 
            	    char_literal56_tree = (Object)adaptor.create(char_literal56);
            	    adaptor.addChild(root_0, char_literal56_tree);


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class primitiveType_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primitiveType"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:147:1: primitiveType : ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' | 'number' | 'string' );
    public final DistributionParser.primitiveType_return primitiveType() throws RecognitionException {
        DistributionParser.primitiveType_return retval = new DistributionParser.primitiveType_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set57=null;

        Object set57_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:148:5: ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' | 'number' | 'string' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            {
            root_0 = (Object)adaptor.nil();

            set57=(Token)input.LT(1);
            if ( (input.LA(1)>=43 && input.LA(1)<=52) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set57));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primitiveType"

    public static class conditionalExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "conditionalExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:161:1: conditionalExpression : conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )? ;
    public final DistributionParser.conditionalExpression_return conditionalExpression() throws RecognitionException {
        DistributionParser.conditionalExpression_return retval = new DistributionParser.conditionalExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal59=null;
        Token char_literal61=null;
        DistributionParser.conditionalOrExpression_return conditionalOrExpression58 = null;

        DistributionParser.conditionalExpression_return conditionalExpression60 = null;

        DistributionParser.conditionalExpression_return conditionalExpression62 = null;


        Object char_literal59_tree=null;
        Object char_literal61_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:162:5: ( conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )? )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:162:9: conditionalOrExpression ( '?' conditionalExpression ':' conditionalExpression )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_conditionalOrExpression_in_conditionalExpression820);
            conditionalOrExpression58=conditionalOrExpression();

            state._fsp--;

            adaptor.addChild(root_0, conditionalOrExpression58.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:162:33: ( '?' conditionalExpression ':' conditionalExpression )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==53) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:162:35: '?' conditionalExpression ':' conditionalExpression
                    {
                    char_literal59=(Token)match(input,53,FOLLOW_53_in_conditionalExpression824); 
                    char_literal59_tree = (Object)adaptor.create(char_literal59);
                    adaptor.addChild(root_0, char_literal59_tree);

                    pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression826);
                    conditionalExpression60=conditionalExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, conditionalExpression60.getTree());
                    char_literal61=(Token)match(input,28,FOLLOW_28_in_conditionalExpression828); 
                    char_literal61_tree = (Object)adaptor.create(char_literal61);
                    adaptor.addChild(root_0, char_literal61_tree);

                    pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression830);
                    conditionalExpression62=conditionalExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, conditionalExpression62.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "conditionalExpression"

    public static class conditionalOrExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "conditionalOrExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:165:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
    public final DistributionParser.conditionalOrExpression_return conditionalOrExpression() throws RecognitionException {
        DistributionParser.conditionalOrExpression_return retval = new DistributionParser.conditionalOrExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal64=null;
        DistributionParser.conditionalAndExpression_return conditionalAndExpression63 = null;

        DistributionParser.conditionalAndExpression_return conditionalAndExpression65 = null;


        Object string_literal64_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:166:5: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:166:9: conditionalAndExpression ( '||' conditionalAndExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression852);
            conditionalAndExpression63=conditionalAndExpression();

            state._fsp--;

            adaptor.addChild(root_0, conditionalAndExpression63.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:166:34: ( '||' conditionalAndExpression )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==54) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:166:36: '||' conditionalAndExpression
            	    {
            	    string_literal64=(Token)match(input,54,FOLLOW_54_in_conditionalOrExpression856); 
            	    string_literal64_tree = (Object)adaptor.create(string_literal64);
            	    root_0 = (Object)adaptor.becomeRoot(string_literal64_tree, root_0);

            	    pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression859);
            	    conditionalAndExpression65=conditionalAndExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, conditionalAndExpression65.getTree());

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "conditionalOrExpression"

    public static class conditionalAndExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "conditionalAndExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:169:1: conditionalAndExpression : inclusiveOrExpression ( '&&' inclusiveOrExpression )* ;
    public final DistributionParser.conditionalAndExpression_return conditionalAndExpression() throws RecognitionException {
        DistributionParser.conditionalAndExpression_return retval = new DistributionParser.conditionalAndExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal67=null;
        DistributionParser.inclusiveOrExpression_return inclusiveOrExpression66 = null;

        DistributionParser.inclusiveOrExpression_return inclusiveOrExpression68 = null;


        Object string_literal67_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:170:5: ( inclusiveOrExpression ( '&&' inclusiveOrExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:170:9: inclusiveOrExpression ( '&&' inclusiveOrExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression881);
            inclusiveOrExpression66=inclusiveOrExpression();

            state._fsp--;

            adaptor.addChild(root_0, inclusiveOrExpression66.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:170:31: ( '&&' inclusiveOrExpression )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==55) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:170:33: '&&' inclusiveOrExpression
            	    {
            	    string_literal67=(Token)match(input,55,FOLLOW_55_in_conditionalAndExpression885); 
            	    string_literal67_tree = (Object)adaptor.create(string_literal67);
            	    root_0 = (Object)adaptor.becomeRoot(string_literal67_tree, root_0);

            	    pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression888);
            	    inclusiveOrExpression68=inclusiveOrExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, inclusiveOrExpression68.getTree());

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "conditionalAndExpression"

    public static class inclusiveOrExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inclusiveOrExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:173:1: inclusiveOrExpression : exclusiveOrExpression ( '|' exclusiveOrExpression )* ;
    public final DistributionParser.inclusiveOrExpression_return inclusiveOrExpression() throws RecognitionException {
        DistributionParser.inclusiveOrExpression_return retval = new DistributionParser.inclusiveOrExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal70=null;
        DistributionParser.exclusiveOrExpression_return exclusiveOrExpression69 = null;

        DistributionParser.exclusiveOrExpression_return exclusiveOrExpression71 = null;


        Object char_literal70_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:174:5: ( exclusiveOrExpression ( '|' exclusiveOrExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:174:9: exclusiveOrExpression ( '|' exclusiveOrExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression910);
            exclusiveOrExpression69=exclusiveOrExpression();

            state._fsp--;

            adaptor.addChild(root_0, exclusiveOrExpression69.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:174:31: ( '|' exclusiveOrExpression )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==56) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:174:33: '|' exclusiveOrExpression
            	    {
            	    char_literal70=(Token)match(input,56,FOLLOW_56_in_inclusiveOrExpression914); 
            	    char_literal70_tree = (Object)adaptor.create(char_literal70);
            	    root_0 = (Object)adaptor.becomeRoot(char_literal70_tree, root_0);

            	    pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression917);
            	    exclusiveOrExpression71=exclusiveOrExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, exclusiveOrExpression71.getTree());

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "inclusiveOrExpression"

    public static class exclusiveOrExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exclusiveOrExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:177:1: exclusiveOrExpression : andExpression ( '^' andExpression )* ;
    public final DistributionParser.exclusiveOrExpression_return exclusiveOrExpression() throws RecognitionException {
        DistributionParser.exclusiveOrExpression_return retval = new DistributionParser.exclusiveOrExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal73=null;
        DistributionParser.andExpression_return andExpression72 = null;

        DistributionParser.andExpression_return andExpression74 = null;


        Object char_literal73_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:178:5: ( andExpression ( '^' andExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:178:9: andExpression ( '^' andExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression939);
            andExpression72=andExpression();

            state._fsp--;

            adaptor.addChild(root_0, andExpression72.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:178:23: ( '^' andExpression )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==57) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:178:25: '^' andExpression
            	    {
            	    char_literal73=(Token)match(input,57,FOLLOW_57_in_exclusiveOrExpression943); 
            	    char_literal73_tree = (Object)adaptor.create(char_literal73);
            	    root_0 = (Object)adaptor.becomeRoot(char_literal73_tree, root_0);

            	    pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression946);
            	    andExpression74=andExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, andExpression74.getTree());

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exclusiveOrExpression"

    public static class andExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "andExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:181:1: andExpression : equalityExpression ( '&' equalityExpression )* ;
    public final DistributionParser.andExpression_return andExpression() throws RecognitionException {
        DistributionParser.andExpression_return retval = new DistributionParser.andExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal76=null;
        DistributionParser.equalityExpression_return equalityExpression75 = null;

        DistributionParser.equalityExpression_return equalityExpression77 = null;


        Object char_literal76_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:182:5: ( equalityExpression ( '&' equalityExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:182:9: equalityExpression ( '&' equalityExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_equalityExpression_in_andExpression968);
            equalityExpression75=equalityExpression();

            state._fsp--;

            adaptor.addChild(root_0, equalityExpression75.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:182:28: ( '&' equalityExpression )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==58) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:182:30: '&' equalityExpression
            	    {
            	    char_literal76=(Token)match(input,58,FOLLOW_58_in_andExpression972); 
            	    char_literal76_tree = (Object)adaptor.create(char_literal76);
            	    root_0 = (Object)adaptor.becomeRoot(char_literal76_tree, root_0);

            	    pushFollow(FOLLOW_equalityExpression_in_andExpression975);
            	    equalityExpression77=equalityExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, equalityExpression77.getTree());

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "andExpression"

    public static class equalityExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "equalityExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:185:1: equalityExpression : relationalExpression ( ( '==' | '!=' ) relationalExpression )* ;
    public final DistributionParser.equalityExpression_return equalityExpression() throws RecognitionException {
        DistributionParser.equalityExpression_return retval = new DistributionParser.equalityExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set79=null;
        DistributionParser.relationalExpression_return relationalExpression78 = null;

        DistributionParser.relationalExpression_return relationalExpression80 = null;


        Object set79_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:186:5: ( relationalExpression ( ( '==' | '!=' ) relationalExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:186:9: relationalExpression ( ( '==' | '!=' ) relationalExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_relationalExpression_in_equalityExpression997);
            relationalExpression78=relationalExpression();

            state._fsp--;

            adaptor.addChild(root_0, relationalExpression78.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:186:30: ( ( '==' | '!=' ) relationalExpression )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0>=59 && LA20_0<=60)) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:186:32: ( '==' | '!=' ) relationalExpression
            	    {
            	    set79=(Token)input.LT(1);
            	    set79=(Token)input.LT(1);
            	    if ( (input.LA(1)>=59 && input.LA(1)<=60) ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot((Object)adaptor.create(set79), root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_relationalExpression_in_equalityExpression1010);
            	    relationalExpression80=relationalExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, relationalExpression80.getTree());

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "equalityExpression"

    public static class relationalExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "relationalExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:189:1: relationalExpression : additiveExpression ( relationalOp additiveExpression )* ;
    public final DistributionParser.relationalExpression_return relationalExpression() throws RecognitionException {
        DistributionParser.relationalExpression_return retval = new DistributionParser.relationalExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.additiveExpression_return additiveExpression81 = null;

        DistributionParser.relationalOp_return relationalOp82 = null;

        DistributionParser.additiveExpression_return additiveExpression83 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:190:5: ( additiveExpression ( relationalOp additiveExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:190:9: additiveExpression ( relationalOp additiveExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_additiveExpression_in_relationalExpression1033);
            additiveExpression81=additiveExpression();

            state._fsp--;

            adaptor.addChild(root_0, additiveExpression81.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:190:28: ( relationalOp additiveExpression )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=61 && LA21_0<=64)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:190:30: relationalOp additiveExpression
            	    {
            	    pushFollow(FOLLOW_relationalOp_in_relationalExpression1037);
            	    relationalOp82=relationalOp();

            	    state._fsp--;

            	    root_0 = (Object)adaptor.becomeRoot(relationalOp82.getTree(), root_0);
            	    pushFollow(FOLLOW_additiveExpression_in_relationalExpression1040);
            	    additiveExpression83=additiveExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, additiveExpression83.getTree());

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relationalExpression"

    public static class relationalOp_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "relationalOp"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:193:1: relationalOp : ( '<=' | '>=' | '<' | '>' );
    public final DistributionParser.relationalOp_return relationalOp() throws RecognitionException {
        DistributionParser.relationalOp_return retval = new DistributionParser.relationalOp_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set84=null;

        Object set84_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:194:5: ( '<=' | '>=' | '<' | '>' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            {
            root_0 = (Object)adaptor.nil();

            set84=(Token)input.LT(1);
            if ( (input.LA(1)>=61 && input.LA(1)<=64) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set84));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relationalOp"

    public static class elementValue_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elementValue"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:201:1: elementValue : ( conditionalExpression | elementValueArrayInitializer );
    public final DistributionParser.elementValue_return elementValue() throws RecognitionException {
        DistributionParser.elementValue_return retval = new DistributionParser.elementValue_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.conditionalExpression_return conditionalExpression85 = null;

        DistributionParser.elementValueArrayInitializer_return elementValueArrayInitializer86 = null;



        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:202:5: ( conditionalExpression | elementValueArrayInitializer )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=Identifier && LA22_0<=DecimalLiteral)||LA22_0==29||LA22_0==41||(LA22_0>=43 && LA22_0<=52)||(LA22_0>=65 && LA22_0<=66)||(LA22_0>=70 && LA22_0<=76)||(LA22_0>=78 && LA22_0<=80)) ) {
                alt22=1;
            }
            else if ( (LA22_0==18) ) {
                alt22=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:202:9: conditionalExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_conditionalExpression_in_elementValue1124);
                    conditionalExpression85=conditionalExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, conditionalExpression85.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:204:9: elementValueArrayInitializer
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_elementValueArrayInitializer_in_elementValue1135);
                    elementValueArrayInitializer86=elementValueArrayInitializer();

                    state._fsp--;

                    adaptor.addChild(root_0, elementValueArrayInitializer86.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elementValue"

    public static class additiveExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "additiveExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:207:1: additiveExpression : multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* ;
    public final DistributionParser.additiveExpression_return additiveExpression() throws RecognitionException {
        DistributionParser.additiveExpression_return retval = new DistributionParser.additiveExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set88=null;
        DistributionParser.multiplicativeExpression_return multiplicativeExpression87 = null;

        DistributionParser.multiplicativeExpression_return multiplicativeExpression89 = null;


        Object set88_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:208:5: ( multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:208:9: multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression1154);
            multiplicativeExpression87=multiplicativeExpression();

            state._fsp--;

            adaptor.addChild(root_0, multiplicativeExpression87.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:208:34: ( ( '+' | '-' ) multiplicativeExpression )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( ((LA23_0>=65 && LA23_0<=66)) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:208:36: ( '+' | '-' ) multiplicativeExpression
            	    {
            	    set88=(Token)input.LT(1);
            	    set88=(Token)input.LT(1);
            	    if ( (input.LA(1)>=65 && input.LA(1)<=66) ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot((Object)adaptor.create(set88), root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression1167);
            	    multiplicativeExpression89=multiplicativeExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, multiplicativeExpression89.getTree());

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "additiveExpression"

    public static class multiplicativeExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiplicativeExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:211:1: multiplicativeExpression : unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* ;
    public final DistributionParser.multiplicativeExpression_return multiplicativeExpression() throws RecognitionException {
        DistributionParser.multiplicativeExpression_return retval = new DistributionParser.multiplicativeExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set91=null;
        DistributionParser.unaryExpression_return unaryExpression90 = null;

        DistributionParser.unaryExpression_return unaryExpression92 = null;


        Object set91_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:212:5: ( unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:212:9: unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1189);
            unaryExpression90=unaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, unaryExpression90.getTree());
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:212:25: ( ( '*' | '/' | '%' ) unaryExpression )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( ((LA24_0>=67 && LA24_0<=69)) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:212:27: ( '*' | '/' | '%' ) unaryExpression
            	    {
            	    set91=(Token)input.LT(1);
            	    set91=(Token)input.LT(1);
            	    if ( (input.LA(1)>=67 && input.LA(1)<=69) ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot((Object)adaptor.create(set91), root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1208);
            	    unaryExpression92=unaryExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, unaryExpression92.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multiplicativeExpression"

    public static class unaryExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryExpression"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:215:1: unaryExpression : ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus );
    public final DistributionParser.unaryExpression_return unaryExpression() throws RecognitionException {
        DistributionParser.unaryExpression_return retval = new DistributionParser.unaryExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal93=null;
        Token char_literal95=null;
        Token string_literal97=null;
        Token string_literal99=null;
        DistributionParser.unaryExpression_return unaryExpression94 = null;

        DistributionParser.unaryExpression_return unaryExpression96 = null;

        DistributionParser.unaryExpression_return unaryExpression98 = null;

        DistributionParser.unaryExpression_return unaryExpression100 = null;

        DistributionParser.unaryExpressionNotPlusMinus_return unaryExpressionNotPlusMinus101 = null;


        Object char_literal93_tree=null;
        Object char_literal95_tree=null;
        Object string_literal97_tree=null;
        Object string_literal99_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:216:5: ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus )
            int alt25=5;
            switch ( input.LA(1) ) {
            case 65:
                {
                alt25=1;
                }
                break;
            case 66:
                {
                alt25=2;
                }
                break;
            case 70:
                {
                alt25=3;
                }
                break;
            case 71:
                {
                alt25=4;
                }
                break;
            case Identifier:
            case FloatingPointLiteral:
            case STRING_LITERAL:
            case DecimalLiteral:
            case 29:
            case 41:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 78:
            case 79:
            case 80:
                {
                alt25=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:216:9: '+' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal93=(Token)match(input,65,FOLLOW_65_in_unaryExpression1234); 
                    char_literal93_tree = (Object)adaptor.create(char_literal93);
                    root_0 = (Object)adaptor.becomeRoot(char_literal93_tree, root_0);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1237);
                    unaryExpression94=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression94.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:217:9: '-' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal95=(Token)match(input,66,FOLLOW_66_in_unaryExpression1247); 
                    char_literal95_tree = (Object)adaptor.create(char_literal95);
                    root_0 = (Object)adaptor.becomeRoot(char_literal95_tree, root_0);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1250);
                    unaryExpression96=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression96.getTree());

                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:218:9: '++' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal97=(Token)match(input,70,FOLLOW_70_in_unaryExpression1263); 
                    string_literal97_tree = (Object)adaptor.create(string_literal97);
                    adaptor.addChild(root_0, string_literal97_tree);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1265);
                    unaryExpression98=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression98.getTree());

                    }
                    break;
                case 4 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:219:9: '--' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal99=(Token)match(input,71,FOLLOW_71_in_unaryExpression1275); 
                    string_literal99_tree = (Object)adaptor.create(string_literal99);
                    adaptor.addChild(root_0, string_literal99_tree);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1277);
                    unaryExpression100=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression100.getTree());

                    }
                    break;
                case 5 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:220:9: unaryExpressionNotPlusMinus
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression1287);
                    unaryExpressionNotPlusMinus101=unaryExpressionNotPlusMinus();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpressionNotPlusMinus101.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unaryExpression"

    public static class unaryExpressionNotPlusMinus_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryExpressionNotPlusMinus"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:223:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | primary );
    public final DistributionParser.unaryExpressionNotPlusMinus_return unaryExpressionNotPlusMinus() throws RecognitionException {
        DistributionParser.unaryExpressionNotPlusMinus_return retval = new DistributionParser.unaryExpressionNotPlusMinus_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal102=null;
        Token char_literal104=null;
        DistributionParser.unaryExpression_return unaryExpression103 = null;

        DistributionParser.unaryExpression_return unaryExpression105 = null;

        DistributionParser.primary_return primary106 = null;


        Object char_literal102_tree=null;
        Object char_literal104_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:224:5: ( '~' unaryExpression | '!' unaryExpression | primary )
            int alt26=3;
            switch ( input.LA(1) ) {
            case 72:
                {
                alt26=1;
                }
                break;
            case 73:
                {
                alt26=2;
                }
                break;
            case Identifier:
            case FloatingPointLiteral:
            case STRING_LITERAL:
            case DecimalLiteral:
            case 29:
            case 41:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 74:
            case 75:
            case 76:
            case 78:
            case 79:
            case 80:
                {
                alt26=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:224:9: '~' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal102=(Token)match(input,72,FOLLOW_72_in_unaryExpressionNotPlusMinus1306); 
                    char_literal102_tree = (Object)adaptor.create(char_literal102);
                    adaptor.addChild(root_0, char_literal102_tree);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus1308);
                    unaryExpression103=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression103.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:225:9: '!' unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal104=(Token)match(input,73,FOLLOW_73_in_unaryExpressionNotPlusMinus1318); 
                    char_literal104_tree = (Object)adaptor.create(char_literal104);
                    adaptor.addChild(root_0, char_literal104_tree);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus1320);
                    unaryExpression105=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression105.getTree());

                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:228:9: primary
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_primary_in_unaryExpressionNotPlusMinus1332);
                    primary106=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary106.getTree());
                     System.out.println((primary106!=null?input.toString(primary106.start,primary106.stop):null)  );

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unaryExpressionNotPlusMinus"

    public static class elementValueArrayInitializer_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elementValueArrayInitializer"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:231:1: elementValueArrayInitializer : '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' ;
    public final DistributionParser.elementValueArrayInitializer_return elementValueArrayInitializer() throws RecognitionException {
        DistributionParser.elementValueArrayInitializer_return retval = new DistributionParser.elementValueArrayInitializer_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal107=null;
        Token char_literal109=null;
        Token char_literal111=null;
        Token char_literal112=null;
        DistributionParser.elementValue_return elementValue108 = null;

        DistributionParser.elementValue_return elementValue110 = null;


        Object char_literal107_tree=null;
        Object char_literal109_tree=null;
        Object char_literal111_tree=null;
        Object char_literal112_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:5: ( '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:9: '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}'
            {
            root_0 = (Object)adaptor.nil();

            char_literal107=(Token)match(input,18,FOLLOW_18_in_elementValueArrayInitializer1356); 
            char_literal107_tree = (Object)adaptor.create(char_literal107);
            adaptor.addChild(root_0, char_literal107_tree);

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:13: ( elementValue ( ',' elementValue )* )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0>=Identifier && LA28_0<=DecimalLiteral)||LA28_0==18||LA28_0==29||LA28_0==41||(LA28_0>=43 && LA28_0<=52)||(LA28_0>=65 && LA28_0<=66)||(LA28_0>=70 && LA28_0<=76)||(LA28_0>=78 && LA28_0<=80)) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:14: elementValue ( ',' elementValue )*
                    {
                    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer1359);
                    elementValue108=elementValue();

                    state._fsp--;

                    adaptor.addChild(root_0, elementValue108.getTree());
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:27: ( ',' elementValue )*
                    loop27:
                    do {
                        int alt27=2;
                        int LA27_0 = input.LA(1);

                        if ( (LA27_0==31) ) {
                            int LA27_1 = input.LA(2);

                            if ( ((LA27_1>=Identifier && LA27_1<=DecimalLiteral)||LA27_1==18||LA27_1==29||LA27_1==41||(LA27_1>=43 && LA27_1<=52)||(LA27_1>=65 && LA27_1<=66)||(LA27_1>=70 && LA27_1<=76)||(LA27_1>=78 && LA27_1<=80)) ) {
                                alt27=1;
                            }


                        }


                        switch (alt27) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:28: ',' elementValue
                    	    {
                    	    char_literal109=(Token)match(input,31,FOLLOW_31_in_elementValueArrayInitializer1362); 
                    	    char_literal109_tree = (Object)adaptor.create(char_literal109);
                    	    adaptor.addChild(root_0, char_literal109_tree);

                    	    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer1364);
                    	    elementValue110=elementValue();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, elementValue110.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);


                    }
                    break;

            }

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:49: ( ',' )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==31) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:232:50: ','
                    {
                    char_literal111=(Token)match(input,31,FOLLOW_31_in_elementValueArrayInitializer1371); 
                    char_literal111_tree = (Object)adaptor.create(char_literal111);
                    adaptor.addChild(root_0, char_literal111_tree);


                    }
                    break;

            }

            char_literal112=(Token)match(input,19,FOLLOW_19_in_elementValueArrayInitializer1375); 
            char_literal112_tree = (Object)adaptor.create(char_literal112);
            adaptor.addChild(root_0, char_literal112_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elementValueArrayInitializer"

    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literal"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:237:1: literal : ( integerLiteral | FloatingPointLiteral | booleanLiteral | 'null' );
    public final DistributionParser.literal_return literal() throws RecognitionException {
        DistributionParser.literal_return retval = new DistributionParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token FloatingPointLiteral114=null;
        Token string_literal116=null;
        DistributionParser.integerLiteral_return integerLiteral113 = null;

        DistributionParser.booleanLiteral_return booleanLiteral115 = null;


        Object FloatingPointLiteral114_tree=null;
        Object string_literal116_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:238:5: ( integerLiteral | FloatingPointLiteral | booleanLiteral | 'null' )
            int alt30=4;
            switch ( input.LA(1) ) {
            case DecimalLiteral:
                {
                alt30=1;
                }
                break;
            case FloatingPointLiteral:
                {
                alt30=2;
                }
                break;
            case 79:
            case 80:
                {
                alt30=3;
                }
                break;
            case 74:
                {
                alt30=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:238:9: integerLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_integerLiteral_in_literal1403);
                    integerLiteral113=integerLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, integerLiteral113.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:239:9: FloatingPointLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    FloatingPointLiteral114=(Token)match(input,FloatingPointLiteral,FOLLOW_FloatingPointLiteral_in_literal1413); 
                    FloatingPointLiteral114_tree = (Object)adaptor.create(FloatingPointLiteral114);
                    adaptor.addChild(root_0, FloatingPointLiteral114_tree);


                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:242:9: booleanLiteral
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanLiteral_in_literal1425);
                    booleanLiteral115=booleanLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, booleanLiteral115.getTree());

                    }
                    break;
                case 4 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:243:9: 'null'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal116=(Token)match(input,74,FOLLOW_74_in_literal1435); 
                    string_literal116_tree = (Object)adaptor.create(string_literal116);
                    adaptor.addChild(root_0, string_literal116_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "literal"

    public static class primary_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primary"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:247:1: primary : ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | literal | createdName | STRING_LITERAL | Identifier arguments | Identifier ( '.' Identifier )* ( identifierSuffix )? | ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );
    public final DistributionParser.primary_return primary() throws RecognitionException {
        DistributionParser.primary_return retval = new DistributionParser.primary_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal118=null;
        Token char_literal119=null;
        Token Identifier120=null;
        Token STRING_LITERAL124=null;
        Token Identifier125=null;
        Token Identifier127=null;
        Token char_literal128=null;
        Token Identifier129=null;
        Token char_literal131=null;
        Token char_literal132=null;
        Token char_literal133=null;
        Token string_literal134=null;
        Token string_literal135=null;
        Token char_literal136=null;
        Token string_literal137=null;
        DistributionParser.parExpression_return parExpression117 = null;

        DistributionParser.identifierSuffix_return identifierSuffix121 = null;

        DistributionParser.literal_return literal122 = null;

        DistributionParser.createdName_return createdName123 = null;

        DistributionParser.arguments_return arguments126 = null;

        DistributionParser.identifierSuffix_return identifierSuffix130 = null;


        Object string_literal118_tree=null;
        Object char_literal119_tree=null;
        Object Identifier120_tree=null;
        Object STRING_LITERAL124_tree=null;
        Object Identifier125_tree=null;
        Object Identifier127_tree=null;
        Object char_literal128_tree=null;
        Object Identifier129_tree=null;
        Object char_literal131_tree=null;
        Object char_literal132_tree=null;
        Object char_literal133_tree=null;
        Object string_literal134_tree=null;
        Object string_literal135_tree=null;
        Object char_literal136_tree=null;
        Object string_literal137_tree=null;

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:248:5: ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | literal | createdName | STRING_LITERAL | Identifier arguments | Identifier ( '.' Identifier )* ( identifierSuffix )? | ( '[' ']' )* '.' 'class' | 'void' '.' 'class' )
            int alt36=9;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:248:8: parExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_parExpression_in_primary1454);
                    parExpression117=parExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, parExpression117.getTree());

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:249:5: 'this' ( '.' Identifier )* ( identifierSuffix )?
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal118=(Token)match(input,75,FOLLOW_75_in_primary1463); 
                    string_literal118_tree = (Object)adaptor.create(string_literal118);
                    adaptor.addChild(root_0, string_literal118_tree);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:249:12: ( '.' Identifier )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==76) ) {
                            alt31=1;
                        }


                        switch (alt31) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:249:13: '.' Identifier
                    	    {
                    	    char_literal119=(Token)match(input,76,FOLLOW_76_in_primary1466); 
                    	    char_literal119_tree = (Object)adaptor.create(char_literal119);
                    	    adaptor.addChild(root_0, char_literal119_tree);

                    	    Identifier120=(Token)match(input,Identifier,FOLLOW_Identifier_in_primary1468); 
                    	    Identifier120_tree = (Object)adaptor.create(Identifier120);
                    	    adaptor.addChild(root_0, Identifier120_tree);


                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:249:30: ( identifierSuffix )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==41) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:249:30: identifierSuffix
                            {
                            pushFollow(FOLLOW_identifierSuffix_in_primary1472);
                            identifierSuffix121=identifierSuffix();

                            state._fsp--;

                            adaptor.addChild(root_0, identifierSuffix121.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:251:9: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_primary1484);
                    literal122=literal();

                    state._fsp--;

                    adaptor.addChild(root_0, literal122.getTree());

                    }
                    break;
                case 4 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:253:9: createdName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_createdName_in_primary1496);
                    createdName123=createdName();

                    state._fsp--;

                    adaptor.addChild(root_0, createdName123.getTree());

                    }
                    break;
                case 5 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:254:9: STRING_LITERAL
                    {
                    root_0 = (Object)adaptor.nil();

                    STRING_LITERAL124=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_primary1506); 
                    STRING_LITERAL124_tree = (Object)adaptor.create(STRING_LITERAL124);
                    adaptor.addChild(root_0, STRING_LITERAL124_tree);


                    }
                    break;
                case 6 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:255:9: Identifier arguments
                    {
                    root_0 = (Object)adaptor.nil();

                    Identifier125=(Token)match(input,Identifier,FOLLOW_Identifier_in_primary1516); 
                    Identifier125_tree = (Object)adaptor.create(Identifier125);
                    root_0 = (Object)adaptor.becomeRoot(Identifier125_tree, root_0);

                    pushFollow(FOLLOW_arguments_in_primary1519);
                    arguments126=arguments();

                    state._fsp--;

                    adaptor.addChild(root_0, arguments126.getTree());

                    }
                    break;
                case 7 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:256:9: Identifier ( '.' Identifier )* ( identifierSuffix )?
                    {
                    root_0 = (Object)adaptor.nil();

                    Identifier127=(Token)match(input,Identifier,FOLLOW_Identifier_in_primary1530); 
                    Identifier127_tree = (Object)adaptor.create(Identifier127);
                    adaptor.addChild(root_0, Identifier127_tree);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:256:20: ( '.' Identifier )*
                    loop33:
                    do {
                        int alt33=2;
                        int LA33_0 = input.LA(1);

                        if ( (LA33_0==76) ) {
                            alt33=1;
                        }


                        switch (alt33) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:256:21: '.' Identifier
                    	    {
                    	    char_literal128=(Token)match(input,76,FOLLOW_76_in_primary1533); 
                    	    Identifier129=(Token)match(input,Identifier,FOLLOW_Identifier_in_primary1536); 
                    	    Identifier129_tree = (Object)adaptor.create(Identifier129);
                    	    root_0 = (Object)adaptor.becomeRoot(Identifier129_tree, root_0);


                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:256:40: ( identifierSuffix )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==41) ) {
                        alt34=1;
                    }
                    switch (alt34) {
                        case 1 :
                            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:256:40: identifierSuffix
                            {
                            pushFollow(FOLLOW_identifierSuffix_in_primary1541);
                            identifierSuffix130=identifierSuffix();

                            state._fsp--;

                            adaptor.addChild(root_0, identifierSuffix130.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 8 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:257:10: ( '[' ']' )* '.' 'class'
                    {
                    root_0 = (Object)adaptor.nil();

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:257:10: ( '[' ']' )*
                    loop35:
                    do {
                        int alt35=2;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==41) ) {
                            alt35=1;
                        }


                        switch (alt35) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:257:11: '[' ']'
                    	    {
                    	    char_literal131=(Token)match(input,41,FOLLOW_41_in_primary1554); 
                    	    char_literal131_tree = (Object)adaptor.create(char_literal131);
                    	    adaptor.addChild(root_0, char_literal131_tree);

                    	    char_literal132=(Token)match(input,42,FOLLOW_42_in_primary1556); 
                    	    char_literal132_tree = (Object)adaptor.create(char_literal132);
                    	    adaptor.addChild(root_0, char_literal132_tree);


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);

                    char_literal133=(Token)match(input,76,FOLLOW_76_in_primary1560); 
                    char_literal133_tree = (Object)adaptor.create(char_literal133);
                    adaptor.addChild(root_0, char_literal133_tree);

                    string_literal134=(Token)match(input,77,FOLLOW_77_in_primary1562); 
                    string_literal134_tree = (Object)adaptor.create(string_literal134);
                    adaptor.addChild(root_0, string_literal134_tree);


                    }
                    break;
                case 9 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:258:9: 'void' '.' 'class'
                    {
                    root_0 = (Object)adaptor.nil();

                    string_literal135=(Token)match(input,78,FOLLOW_78_in_primary1572); 
                    string_literal135_tree = (Object)adaptor.create(string_literal135);
                    adaptor.addChild(root_0, string_literal135_tree);

                    char_literal136=(Token)match(input,76,FOLLOW_76_in_primary1574); 
                    char_literal136_tree = (Object)adaptor.create(char_literal136);
                    adaptor.addChild(root_0, char_literal136_tree);

                    string_literal137=(Token)match(input,77,FOLLOW_77_in_primary1576); 
                    string_literal137_tree = (Object)adaptor.create(string_literal137);
                    adaptor.addChild(root_0, string_literal137_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primary"

    public static class createdName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "createdName"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:261:1: createdName : primitiveType expression2 -> ^( VAR primitiveType expression2 ) ;
    public final DistributionParser.createdName_return createdName() throws RecognitionException {
        DistributionParser.createdName_return retval = new DistributionParser.createdName_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        DistributionParser.primitiveType_return primitiveType138 = null;

        DistributionParser.expression2_return expression2139 = null;


        RewriteRuleSubtreeStream stream_primitiveType=new RewriteRuleSubtreeStream(adaptor,"rule primitiveType");
        RewriteRuleSubtreeStream stream_expression2=new RewriteRuleSubtreeStream(adaptor,"rule expression2");
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:262:5: ( primitiveType expression2 -> ^( VAR primitiveType expression2 ) )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:263:7: primitiveType expression2
            {
            pushFollow(FOLLOW_primitiveType_in_createdName1609);
            primitiveType138=primitiveType();

            state._fsp--;

            stream_primitiveType.add(primitiveType138.getTree());
            pushFollow(FOLLOW_expression2_in_createdName1611);
            expression2139=expression2();

            state._fsp--;

            stream_expression2.add(expression2139.getTree());


            // AST REWRITE
            // elements: primitiveType, expression2
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 263:34: -> ^( VAR primitiveType expression2 )
            {
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:263:37: ^( VAR primitiveType expression2 )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(VAR, "VAR"), root_1);

                adaptor.addChild(root_1, stream_primitiveType.nextTree());
                adaptor.addChild(root_1, stream_expression2.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "createdName"

    public static class identifierSuffix_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifierSuffix"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:267:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' -> 'class' | ( '[' expression ']' )+ -> expression );
    public final DistributionParser.identifierSuffix_return identifierSuffix() throws RecognitionException {
        DistributionParser.identifierSuffix_return retval = new DistributionParser.identifierSuffix_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal140=null;
        Token char_literal141=null;
        Token char_literal142=null;
        Token string_literal143=null;
        Token char_literal144=null;
        Token char_literal146=null;
        DistributionParser.expression_return expression145 = null;


        Object char_literal140_tree=null;
        Object char_literal141_tree=null;
        Object char_literal142_tree=null;
        Object string_literal143_tree=null;
        Object char_literal144_tree=null;
        Object char_literal146_tree=null;
        RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
        RewriteRuleTokenStream stream_42=new RewriteRuleTokenStream(adaptor,"token 42");
        RewriteRuleTokenStream stream_41=new RewriteRuleTokenStream(adaptor,"token 41");
        RewriteRuleTokenStream stream_76=new RewriteRuleTokenStream(adaptor,"token 76");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:268:5: ( ( '[' ']' )+ '.' 'class' -> 'class' | ( '[' expression ']' )+ -> expression )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==41) ) {
                int LA39_1 = input.LA(2);

                if ( (LA39_1==42) ) {
                    alt39=1;
                }
                else if ( ((LA39_1>=Identifier && LA39_1<=DecimalLiteral)||LA39_1==29||LA39_1==41||(LA39_1>=43 && LA39_1<=52)||(LA39_1>=65 && LA39_1<=66)||(LA39_1>=70 && LA39_1<=76)||(LA39_1>=78 && LA39_1<=80)) ) {
                    alt39=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:268:9: ( '[' ']' )+ '.' 'class'
                    {
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:268:9: ( '[' ']' )+
                    int cnt37=0;
                    loop37:
                    do {
                        int alt37=2;
                        int LA37_0 = input.LA(1);

                        if ( (LA37_0==41) ) {
                            alt37=1;
                        }


                        switch (alt37) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:268:10: '[' ']'
                    	    {
                    	    char_literal140=(Token)match(input,41,FOLLOW_41_in_identifierSuffix1649);  
                    	    stream_41.add(char_literal140);

                    	    char_literal141=(Token)match(input,42,FOLLOW_42_in_identifierSuffix1651);  
                    	    stream_42.add(char_literal141);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt37 >= 1 ) break loop37;
                                EarlyExitException eee =
                                    new EarlyExitException(37, input);
                                throw eee;
                        }
                        cnt37++;
                    } while (true);

                    char_literal142=(Token)match(input,76,FOLLOW_76_in_identifierSuffix1655);  
                    stream_76.add(char_literal142);

                    string_literal143=(Token)match(input,77,FOLLOW_77_in_identifierSuffix1657);  
                    stream_77.add(string_literal143);



                    // AST REWRITE
                    // elements: 77
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 268:33: -> 'class'
                    {
                        adaptor.addChild(root_0, stream_77.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:269:9: ( '[' expression ']' )+
                    {
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:269:9: ( '[' expression ']' )+
                    int cnt38=0;
                    loop38:
                    do {
                        int alt38=2;
                        int LA38_0 = input.LA(1);

                        if ( (LA38_0==41) ) {
                            alt38=1;
                        }


                        switch (alt38) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:269:10: '[' expression ']'
                    	    {
                    	    char_literal144=(Token)match(input,41,FOLLOW_41_in_identifierSuffix1673);  
                    	    stream_41.add(char_literal144);

                    	    pushFollow(FOLLOW_expression_in_identifierSuffix1675);
                    	    expression145=expression();

                    	    state._fsp--;

                    	    stream_expression.add(expression145.getTree());
                    	    char_literal146=(Token)match(input,42,FOLLOW_42_in_identifierSuffix1677);  
                    	    stream_42.add(char_literal146);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt38 >= 1 ) break loop38;
                                EarlyExitException eee =
                                    new EarlyExitException(38, input);
                                throw eee;
                        }
                        cnt38++;
                    } while (true);



                    // AST REWRITE
                    // elements: expression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 269:32: -> expression
                    {
                        adaptor.addChild(root_0, stream_expression.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "identifierSuffix"

    public static class integerLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "integerLiteral"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:278:1: integerLiteral : DecimalLiteral -> DecimalLiteral ;
    public final DistributionParser.integerLiteral_return integerLiteral() throws RecognitionException {
        DistributionParser.integerLiteral_return retval = new DistributionParser.integerLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DecimalLiteral147=null;

        Object DecimalLiteral147_tree=null;
        RewriteRuleTokenStream stream_DecimalLiteral=new RewriteRuleTokenStream(adaptor,"token DecimalLiteral");

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:279:5: ( DecimalLiteral -> DecimalLiteral )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:279:10: DecimalLiteral
            {
            DecimalLiteral147=(Token)match(input,DecimalLiteral,FOLLOW_DecimalLiteral_in_integerLiteral1714);  
            stream_DecimalLiteral.add(DecimalLiteral147);



            // AST REWRITE
            // elements: DecimalLiteral
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 279:25: -> DecimalLiteral
            {
                adaptor.addChild(root_0, stream_DecimalLiteral.nextNode());

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "integerLiteral"

    public static class booleanLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanLiteral"
    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:282:1: booleanLiteral : ( 'true' -> 'true' | 'false' -> 'false' );
    public final DistributionParser.booleanLiteral_return booleanLiteral() throws RecognitionException {
        DistributionParser.booleanLiteral_return retval = new DistributionParser.booleanLiteral_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal148=null;
        Token string_literal149=null;

        Object string_literal148_tree=null;
        Object string_literal149_tree=null;
        RewriteRuleTokenStream stream_79=new RewriteRuleTokenStream(adaptor,"token 79");
        RewriteRuleTokenStream stream_80=new RewriteRuleTokenStream(adaptor,"token 80");

        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:283:5: ( 'true' -> 'true' | 'false' -> 'false' )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==79) ) {
                alt40=1;
            }
            else if ( (LA40_0==80) ) {
                alt40=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:283:9: 'true'
                    {
                    string_literal148=(Token)match(input,79,FOLLOW_79_in_booleanLiteral1737);  
                    stream_79.add(string_literal148);



                    // AST REWRITE
                    // elements: 79
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 283:17: -> 'true'
                    {
                        adaptor.addChild(root_0, stream_79.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:284:9: 'false'
                    {
                    string_literal149=(Token)match(input,80,FOLLOW_80_in_booleanLiteral1752);  
                    stream_80.add(string_literal149);



                    // AST REWRITE
                    // elements: 80
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 284:18: -> 'false'
                    {
                        adaptor.addChild(root_0, stream_80.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "booleanLiteral"

    // Delegated rules


    protected DFA8 dfa8 = new DFA8(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA36 dfa36 = new DFA36(this);
    static final String DFA8_eotS =
        "\14\uffff";
    static final String DFA8_eofS =
        "\14\uffff";
    static final String DFA8_minS =
        "\1\4\11\uffff\1\27\1\uffff";
    static final String DFA8_maxS =
        "\1\120\11\uffff\1\114\1\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\12";
    static final String DFA8_specialS =
        "\14\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\12\3\11\12\uffff\1\1\1\uffff\1\2\1\uffff\1\3\1\10\1\4\1"+
            "\5\1\6\1\7\1\uffff\1\11\13\uffff\1\11\1\uffff\12\11\14\uffff"+
            "\2\11\3\uffff\7\11\1\uffff\3\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\11\4\uffff\1\13\1\11\2\uffff\12\11\13\uffff\21\11\6\uffff"+
            "\1\11",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "56:1: statement : ( block | 'if' ifExpression statement ( options {k=1; } : 'else' statement )? | 'print' ( arguments )? ';' -> ^( 'print' arguments ) | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );";
        }
    }
    static final String DFA3_eotS =
        "\37\uffff";
    static final String DFA3_eofS =
        "\1\2\36\uffff";
    static final String DFA3_minS =
        "\1\4\36\uffff";
    static final String DFA3_maxS =
        "\1\120\36\uffff";
    static final String DFA3_acceptS =
        "\1\uffff\1\1\1\2\34\uffff";
    static final String DFA3_specialS =
        "\37\uffff}>";
    static final String[] DFA3_transitionS = {
            "\4\2\12\uffff\3\2\1\1\6\2\1\uffff\1\2\13\uffff\1\2\1\uffff"+
            "\12\2\14\uffff\2\2\3\uffff\7\2\1\uffff\3\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "59:40: ( options {k=1; } : 'else' statement )?";
        }
    }
    static final String DFA36_eotS =
        "\13\uffff";
    static final String DFA36_eofS =
        "\6\uffff\1\12\4\uffff";
    static final String DFA36_minS =
        "\1\4\5\uffff\1\23\4\uffff";
    static final String DFA36_maxS =
        "\1\120\5\uffff\1\114\4\uffff";
    static final String DFA36_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\uffff\1\10\1\11\1\6\1\7";
    static final String DFA36_specialS =
        "\13\uffff}>";
    static final String[] DFA36_transitionS = {
            "\1\6\1\3\1\5\1\3\25\uffff\1\1\13\uffff\1\7\1\uffff\12\4\25"+
            "\uffff\1\3\1\2\1\7\1\uffff\1\10\2\3",
            "",
            "",
            "",
            "",
            "",
            "\1\12\3\uffff\1\12\4\uffff\1\12\1\11\15\12\12\uffff\21\12"+
            "\6\uffff\1\12",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA36_eot = DFA.unpackEncodedString(DFA36_eotS);
    static final short[] DFA36_eof = DFA.unpackEncodedString(DFA36_eofS);
    static final char[] DFA36_min = DFA.unpackEncodedStringToUnsignedChars(DFA36_minS);
    static final char[] DFA36_max = DFA.unpackEncodedStringToUnsignedChars(DFA36_maxS);
    static final short[] DFA36_accept = DFA.unpackEncodedString(DFA36_acceptS);
    static final short[] DFA36_special = DFA.unpackEncodedString(DFA36_specialS);
    static final short[][] DFA36_transition;

    static {
        int numStates = DFA36_transitionS.length;
        DFA36_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA36_transition[i] = DFA.unpackEncodedString(DFA36_transitionS[i]);
        }
    }

    class DFA36 extends DFA {

        public DFA36(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 36;
            this.eot = DFA36_eot;
            this.eof = DFA36_eof;
            this.min = DFA36_min;
            this.max = DFA36_max;
            this.accept = DFA36_accept;
            this.special = DFA36_special;
            this.transition = DFA36_transition;
        }
        public String getDescription() {
            return "247:1: primary : ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | literal | createdName | STRING_LITERAL | Identifier arguments | Identifier ( '.' Identifier )* ( identifierSuffix )? | ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );";
        }
    }
 

    public static final BitSet FOLLOW_statement_in_program47 = new BitSet(new long[]{0x001FFA002FD400F2L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_18_in_block73 = new BitSet(new long[]{0x001FFA002FDC00F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_blockStatement_in_block75 = new BitSet(new long[]{0x001FFA002FDC00F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_19_in_block78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_blockStatement121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_statement148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_statement161 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_ifExpression_in_statement164 = new BitSet(new long[]{0x001FFA002FF400F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_statement_in_statement166 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_21_in_statement176 = new BitSet(new long[]{0x001FFA002FD400F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_statement_in_statement179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_statement197 = new BitSet(new long[]{0x0000000020800000L});
    public static final BitSet FOLLOW_arguments_in_statement199 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_statement227 = new BitSet(new long[]{0x001FFA00208000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_statement229 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_statement242 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_statement244 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_statement256 = new BitSet(new long[]{0x0000000000800010L});
    public static final BitSet FOLLOW_Identifier_in_statement258 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_statement271 = new BitSet(new long[]{0x0000000000800010L});
    public static final BitSet FOLLOW_Identifier_in_statement273 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_statement286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statementExpression_in_statement297 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_statement299 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_statement310 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_statement312 = new BitSet(new long[]{0x001FFA002FD400F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_statement_in_statement314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_statementExpression334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_constantExpression357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_ifExpression382 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_ifExpression384 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_ifExpression386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_parExpression419 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_parExpression422 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_parExpression424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionList445 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_31_in_expressionList448 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_expressionList451 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_29_in_arguments480 = new BitSet(new long[]{0x001FFA00600000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expressionList_in_arguments483 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_arguments486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_expression514 = new BitSet(new long[]{0x000001FF00000002L});
    public static final BitSet FOLLOW_assignmentOperator_in_expression517 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_expression520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_expression2548 = new BitSet(new long[]{0x000001FF00000002L});
    public static final BitSet FOLLOW_assignmentOperator_in_expression2551 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_expression2554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignmentOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_type685 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_type688 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_type690 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_set_in_primitiveType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalOrExpression_in_conditionalExpression820 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_53_in_conditionalExpression824 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression826 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_conditionalExpression828 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression852 = new BitSet(new long[]{0x0040000000000002L});
    public static final BitSet FOLLOW_54_in_conditionalOrExpression856 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression859 = new BitSet(new long[]{0x0040000000000002L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression881 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_55_in_conditionalAndExpression885 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression888 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression910 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_56_in_inclusiveOrExpression914 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression917 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression939 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_57_in_exclusiveOrExpression943 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression946 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression968 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_58_in_andExpression972 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression975 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_relationalExpression_in_equalityExpression997 = new BitSet(new long[]{0x1800000000000002L});
    public static final BitSet FOLLOW_set_in_equalityExpression1001 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_relationalExpression_in_equalityExpression1010 = new BitSet(new long[]{0x1800000000000002L});
    public static final BitSet FOLLOW_additiveExpression_in_relationalExpression1033 = new BitSet(new long[]{0xE000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_relationalOp_in_relationalExpression1037 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_additiveExpression_in_relationalExpression1040 = new BitSet(new long[]{0xE000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_set_in_relationalOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_elementValue1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elementValueArrayInitializer_in_elementValue1135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression1154 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000006L});
    public static final BitSet FOLLOW_set_in_additiveExpression1158 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression1167 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000006L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1189 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000038L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpression1193 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1208 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000038L});
    public static final BitSet FOLLOW_65_in_unaryExpression1234 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_unaryExpression1247 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_70_in_unaryExpression1263 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_71_in_unaryExpression1275 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression1287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_72_in_unaryExpressionNotPlusMinus1306 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus1308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_unaryExpressionNotPlusMinus1318 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus1320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_unaryExpressionNotPlusMinus1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_elementValueArrayInitializer1356 = new BitSet(new long[]{0x001FFA00A00C00F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer1359 = new BitSet(new long[]{0x0000000080080000L});
    public static final BitSet FOLLOW_31_in_elementValueArrayInitializer1362 = new BitSet(new long[]{0x001FFA00200400F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer1364 = new BitSet(new long[]{0x0000000080080000L});
    public static final BitSet FOLLOW_31_in_elementValueArrayInitializer1371 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_elementValueArrayInitializer1375 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_integerLiteral_in_literal1403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FloatingPointLiteral_in_literal1413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_literal1435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parExpression_in_primary1454 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_75_in_primary1463 = new BitSet(new long[]{0x0000020000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_primary1466 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary1468 = new BitSet(new long[]{0x0000020000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary1472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_primary1484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createdName_in_primary1496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_primary1506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_primary1516 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_arguments_in_primary1519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_primary1530 = new BitSet(new long[]{0x0000020000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_primary1533 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary1536 = new BitSet(new long[]{0x0000020000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary1541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_primary1554 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_primary1556 = new BitSet(new long[]{0x0000020000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_primary1560 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_primary1562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_primary1572 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_primary1574 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_primary1576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_createdName1609 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression2_in_createdName1611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_identifierSuffix1649 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_identifierSuffix1651 = new BitSet(new long[]{0x0000020000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_identifierSuffix1655 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_identifierSuffix1657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_identifierSuffix1673 = new BitSet(new long[]{0x001FFA00200000F0L,0x000000000001DFC6L});
    public static final BitSet FOLLOW_expression_in_identifierSuffix1675 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_identifierSuffix1677 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_DecimalLiteral_in_integerLiteral1714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_79_in_booleanLiteral1737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_booleanLiteral1752 = new BitSet(new long[]{0x0000000000000002L});

}