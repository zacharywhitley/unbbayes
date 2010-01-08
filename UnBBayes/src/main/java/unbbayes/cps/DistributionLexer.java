// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g 2009-12-29 10:11:52

package unbbayes.cps; 


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DistributionLexer extends Lexer {
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__29=29;
    public static final int T__64=64;
    public static final int T__28=28;
    public static final int T__65=65;
    public static final int T__27=27;
    public static final int T__62=62;
    public static final int T__26=26;
    public static final int T__63=63;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int EOF=-1;
    public static final int T__60=60;
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
    public static final int T__46=46;
    public static final int T__80=80;
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
    public static final int T__33=33;
    public static final int T__71=71;
    public static final int WS=15;
    public static final int T__34=34;
    public static final int T__72=72;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__70=70;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int FloatingPointLiteral=5;
    public static final int JavaIDDigit=13;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int T__73=73;
    public static final int EscapeSequence=11;
    public static final int Letter=12;
    public static final int OctalEscape=14;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;

    // delegates
    // delegators

    public DistributionLexer() {;} 
    public DistributionLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DistributionLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Distribution.g"; }

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:7:7: ( '{' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:7:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:8:7: ( '}' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:8:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:9:7: ( 'if' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:9:9: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:10:7: ( 'else' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:10:9: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:11:7: ( 'print' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:11:9: 'print'
            {
            match("print"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:12:7: ( ';' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:12:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:13:7: ( 'return' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:13:9: 'return'
            {
            match("return"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:14:7: ( 'throw' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:14:9: 'throw'
            {
            match("throw"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:15:7: ( 'break' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:15:9: 'break'
            {
            match("break"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:7: ( 'continue' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:16:9: 'continue'
            {
            match("continue"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:17:7: ( ':' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:17:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:18:7: ( '(' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:18:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:19:7: ( ')' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:19:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:20:7: ( ',' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:20:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:21:7: ( '=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:21:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:22:7: ( '+=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:22:9: '+='
            {
            match("+="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:23:7: ( '-=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:23:9: '-='
            {
            match("-="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:24:7: ( '*=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:24:9: '*='
            {
            match("*="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:25:7: ( '/=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:25:9: '/='
            {
            match("/="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:26:7: ( '&=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:26:9: '&='
            {
            match("&="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:27:7: ( '|=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:27:9: '|='
            {
            match("|="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:28:7: ( '^=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:28:9: '^='
            {
            match("^="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:29:7: ( '%=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:29:9: '%='
            {
            match("%="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:30:7: ( '[' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:30:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:31:7: ( ']' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:31:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:32:7: ( 'boolean' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:32:9: 'boolean'
            {
            match("boolean"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:33:7: ( 'char' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:33:9: 'char'
            {
            match("char"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:34:7: ( 'byte' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:34:9: 'byte'
            {
            match("byte"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:35:7: ( 'short' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:35:9: 'short'
            {
            match("short"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:36:7: ( 'int' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:36:9: 'int'
            {
            match("int"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:37:7: ( 'long' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:37:9: 'long'
            {
            match("long"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:38:7: ( 'float' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:38:9: 'float'
            {
            match("float"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:39:7: ( 'double' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:39:9: 'double'
            {
            match("double"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:40:7: ( 'number' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:40:9: 'number'
            {
            match("number"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:41:7: ( 'string' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:41:9: 'string'
            {
            match("string"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:42:7: ( '?' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:42:9: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:43:7: ( '||' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:43:9: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:44:7: ( '&&' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:44:9: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:7: ( '|' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:45:9: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:46:7: ( '^' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:46:9: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:47:7: ( '&' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:47:9: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:48:7: ( '==' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:48:9: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:49:7: ( '!=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:49:9: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:50:7: ( '<=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:50:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:51:7: ( '>=' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:51:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:52:7: ( '<' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:52:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:53:7: ( '>' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:53:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:54:7: ( '+' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:54:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:55:7: ( '-' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:55:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:56:7: ( '*' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:56:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__67"

    // $ANTLR start "T__68"
    public final void mT__68() throws RecognitionException {
        try {
            int _type = T__68;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:57:7: ( '/' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:57:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__68"

    // $ANTLR start "T__69"
    public final void mT__69() throws RecognitionException {
        try {
            int _type = T__69;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:58:7: ( '%' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:58:9: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__69"

    // $ANTLR start "T__70"
    public final void mT__70() throws RecognitionException {
        try {
            int _type = T__70;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:59:7: ( '++' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:59:9: '++'
            {
            match("++"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__70"

    // $ANTLR start "T__71"
    public final void mT__71() throws RecognitionException {
        try {
            int _type = T__71;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:60:7: ( '--' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:60:9: '--'
            {
            match("--"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__71"

    // $ANTLR start "T__72"
    public final void mT__72() throws RecognitionException {
        try {
            int _type = T__72;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:61:7: ( '~' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:61:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__72"

    // $ANTLR start "T__73"
    public final void mT__73() throws RecognitionException {
        try {
            int _type = T__73;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:62:7: ( '!' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:62:9: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__73"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:63:7: ( 'null' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:63:9: 'null'
            {
            match("null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:64:7: ( 'this' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:64:9: 'this'
            {
            match("this"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:65:7: ( '.' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:65:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "T__77"
    public final void mT__77() throws RecognitionException {
        try {
            int _type = T__77;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:66:7: ( 'class' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:66:9: 'class'
            {
            match("class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__77"

    // $ANTLR start "T__78"
    public final void mT__78() throws RecognitionException {
        try {
            int _type = T__78;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:67:7: ( 'void' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:67:9: 'void'
            {
            match("void"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__78"

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:68:7: ( 'true' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:68:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:69:7: ( 'false' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:69:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "EXP"
    public final void mEXP() throws RecognitionException {
        try {
            int _type = EXP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:289:5: ( 'EXP' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:289:7: 'EXP'
            {
            match("EXP"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXP"

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:290:5: ( 'VAR' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:290:7: 'VAR'
            {
            match("VAR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VAR"

    // $ANTLR start "SLIST"
    public final void mSLIST() throws RecognitionException {
        try {
            int _type = SLIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:291:7: ( 'SLIST' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:291:9: 'SLIST'
            {
            match("SLIST"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLIST"

    // $ANTLR start "DecimalLiteral"
    public final void mDecimalLiteral() throws RecognitionException {
        try {
            int _type = DecimalLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:16: ( ( '0' | '1' .. '9' ( '0' .. '9' )* ) )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:18: ( '0' | '1' .. '9' ( '0' .. '9' )* )
            {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:18: ( '0' | '1' .. '9' ( '0' .. '9' )* )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='0') ) {
                alt2=1;
            }
            else if ( ((LA2_0>='1' && LA2_0<='9')) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:19: '0'
                    {
                    match('0'); 

                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:25: '1' .. '9' ( '0' .. '9' )*
                    {
                    matchRange('1','9'); 
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:34: ( '0' .. '9' )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:293:34: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DecimalLiteral"

    // $ANTLR start "FloatingPointLiteral"
    public final void mFloatingPointLiteral() throws RecognitionException {
        try {
            int _type = FloatingPointLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:5: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )*
            {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:9: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:10: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);

            match('.'); 
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:25: ( '0' .. '9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:296:26: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FloatingPointLiteral"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:303:5: ( '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:303:8: '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:303:12: ( EscapeSequence | ~ ( '\\\\' | '\"' ) )*
            loop5:
            do {
                int alt5=3;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\\') ) {
                    alt5=1;
                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='[')||(LA5_0>=']' && LA5_0<='\uFFFF')) ) {
                    alt5=2;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:303:14: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:303:31: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "Identifier"
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:307:5: ( Letter ( Letter | JavaIDDigit )* )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:307:9: Letter ( Letter | JavaIDDigit )*
            {
            mLetter(); 
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:307:16: ( Letter | JavaIDDigit )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='$'||(LA6_0>='0' && LA6_0<='9')||(LA6_0>='A' && LA6_0<='Z')||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z')||(LA6_0>='\u00C0' && LA6_0<='\u00D6')||(LA6_0>='\u00D8' && LA6_0<='\u00F6')||(LA6_0>='\u00F8' && LA6_0<='\u1FFF')||(LA6_0>='\u3040' && LA6_0<='\u318F')||(LA6_0>='\u3300' && LA6_0<='\u337F')||(LA6_0>='\u3400' && LA6_0<='\u3D2D')||(LA6_0>='\u4E00' && LA6_0<='\u9FFF')||(LA6_0>='\uF900' && LA6_0<='\uFAFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            	    {
            	    if ( input.LA(1)=='$'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Identifier"

    // $ANTLR start "Letter"
    public final void mLetter() throws RecognitionException {
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:312:5: ( '\\u0024' | '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            {
            if ( input.LA(1)=='$'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Letter"

    // $ANTLR start "JavaIDDigit"
    public final void mJavaIDDigit() throws RecognitionException {
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:329:5: ( '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06f0' .. '\\u06f9' | '\\u0966' .. '\\u096f' | '\\u09e6' .. '\\u09ef' | '\\u0a66' .. '\\u0a6f' | '\\u0ae6' .. '\\u0aef' | '\\u0b66' .. '\\u0b6f' | '\\u0be7' .. '\\u0bef' | '\\u0c66' .. '\\u0c6f' | '\\u0ce6' .. '\\u0cef' | '\\u0d66' .. '\\u0d6f' | '\\u0e50' .. '\\u0e59' | '\\u0ed0' .. '\\u0ed9' | '\\u1040' .. '\\u1049' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='\u0660' && input.LA(1)<='\u0669')||(input.LA(1)>='\u06F0' && input.LA(1)<='\u06F9')||(input.LA(1)>='\u0966' && input.LA(1)<='\u096F')||(input.LA(1)>='\u09E6' && input.LA(1)<='\u09EF')||(input.LA(1)>='\u0A66' && input.LA(1)<='\u0A6F')||(input.LA(1)>='\u0AE6' && input.LA(1)<='\u0AEF')||(input.LA(1)>='\u0B66' && input.LA(1)<='\u0B6F')||(input.LA(1)>='\u0BE7' && input.LA(1)<='\u0BEF')||(input.LA(1)>='\u0C66' && input.LA(1)<='\u0C6F')||(input.LA(1)>='\u0CE6' && input.LA(1)<='\u0CEF')||(input.LA(1)>='\u0D66' && input.LA(1)<='\u0D6F')||(input.LA(1)>='\u0E50' && input.LA(1)<='\u0E59')||(input.LA(1)>='\u0ED0' && input.LA(1)<='\u0ED9')||(input.LA(1)>='\u1040' && input.LA(1)<='\u1049') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "JavaIDDigit"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:348:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | OctalEscape )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\\') ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1=='\"'||LA7_1=='\''||LA7_1=='\\'||LA7_1=='b'||LA7_1=='f'||LA7_1=='n'||LA7_1=='r'||LA7_1=='t') ) {
                    alt7=1;
                }
                else if ( ((LA7_1>='0' && LA7_1<='7')) ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:348:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:349:9: OctalEscape
                    {
                    mOctalEscape(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "OctalEscape"
    public final void mOctalEscape() throws RecognitionException {
        try {
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt8=3;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='\\') ) {
                int LA8_1 = input.LA(2);

                if ( ((LA8_1>='0' && LA8_1<='3')) ) {
                    int LA8_2 = input.LA(3);

                    if ( ((LA8_2>='0' && LA8_2<='7')) ) {
                        int LA8_4 = input.LA(4);

                        if ( ((LA8_4>='0' && LA8_4<='7')) ) {
                            alt8=1;
                        }
                        else {
                            alt8=2;}
                    }
                    else {
                        alt8=3;}
                }
                else if ( ((LA8_1>='4' && LA8_1<='7')) ) {
                    int LA8_3 = input.LA(3);

                    if ( ((LA8_3>='0' && LA8_3<='7')) ) {
                        alt8=2;
                    }
                    else {
                        alt8=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:14: ( '0' .. '3' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:15: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:25: ( '0' .. '7' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:36: ( '0' .. '7' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:354:37: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:355:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:355:14: ( '0' .. '7' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:355:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:355:25: ( '0' .. '7' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:355:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:356:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:356:14: ( '0' .. '7' )
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:356:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "OctalEscape"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:359:5: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:359:8: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:364:5: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:364:9: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:364:14: ( options {greedy=false; } : . )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='*') ) {
                    int LA9_1 = input.LA(2);

                    if ( (LA9_1=='/') ) {
                        alt9=2;
                    }
                    else if ( ((LA9_1>='\u0000' && LA9_1<='.')||(LA9_1>='0' && LA9_1<='\uFFFF')) ) {
                        alt9=1;
                    }


                }
                else if ( ((LA9_0>='\u0000' && LA9_0<=')')||(LA9_0>='+' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:364:42: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match("*/"); 

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:7: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
            match("//"); 

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:12: (~ ( '\\n' | '\\r' ) )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='\u0000' && LA10_0<='\t')||(LA10_0>='\u000B' && LA10_0<='\f')||(LA10_0>='\u000E' && LA10_0<='\uFFFF')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:12: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:26: ( '\\r' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\r') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:368:26: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LINE_COMMENT"

    public void mTokens() throws RecognitionException {
        // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:8: ( T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | EXP | VAR | SLIST | DecimalLiteral | FloatingPointLiteral | STRING_LITERAL | Identifier | WS | COMMENT | LINE_COMMENT )
        int alt12=73;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:10: T__18
                {
                mT__18(); 

                }
                break;
            case 2 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:16: T__19
                {
                mT__19(); 

                }
                break;
            case 3 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:22: T__20
                {
                mT__20(); 

                }
                break;
            case 4 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:28: T__21
                {
                mT__21(); 

                }
                break;
            case 5 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:34: T__22
                {
                mT__22(); 

                }
                break;
            case 6 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:40: T__23
                {
                mT__23(); 

                }
                break;
            case 7 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:46: T__24
                {
                mT__24(); 

                }
                break;
            case 8 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:52: T__25
                {
                mT__25(); 

                }
                break;
            case 9 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:58: T__26
                {
                mT__26(); 

                }
                break;
            case 10 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:64: T__27
                {
                mT__27(); 

                }
                break;
            case 11 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:70: T__28
                {
                mT__28(); 

                }
                break;
            case 12 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:76: T__29
                {
                mT__29(); 

                }
                break;
            case 13 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:82: T__30
                {
                mT__30(); 

                }
                break;
            case 14 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:88: T__31
                {
                mT__31(); 

                }
                break;
            case 15 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:94: T__32
                {
                mT__32(); 

                }
                break;
            case 16 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:100: T__33
                {
                mT__33(); 

                }
                break;
            case 17 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:106: T__34
                {
                mT__34(); 

                }
                break;
            case 18 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:112: T__35
                {
                mT__35(); 

                }
                break;
            case 19 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:118: T__36
                {
                mT__36(); 

                }
                break;
            case 20 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:124: T__37
                {
                mT__37(); 

                }
                break;
            case 21 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:130: T__38
                {
                mT__38(); 

                }
                break;
            case 22 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:136: T__39
                {
                mT__39(); 

                }
                break;
            case 23 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:142: T__40
                {
                mT__40(); 

                }
                break;
            case 24 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:148: T__41
                {
                mT__41(); 

                }
                break;
            case 25 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:154: T__42
                {
                mT__42(); 

                }
                break;
            case 26 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:160: T__43
                {
                mT__43(); 

                }
                break;
            case 27 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:166: T__44
                {
                mT__44(); 

                }
                break;
            case 28 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:172: T__45
                {
                mT__45(); 

                }
                break;
            case 29 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:178: T__46
                {
                mT__46(); 

                }
                break;
            case 30 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:184: T__47
                {
                mT__47(); 

                }
                break;
            case 31 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:190: T__48
                {
                mT__48(); 

                }
                break;
            case 32 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:196: T__49
                {
                mT__49(); 

                }
                break;
            case 33 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:202: T__50
                {
                mT__50(); 

                }
                break;
            case 34 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:208: T__51
                {
                mT__51(); 

                }
                break;
            case 35 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:214: T__52
                {
                mT__52(); 

                }
                break;
            case 36 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:220: T__53
                {
                mT__53(); 

                }
                break;
            case 37 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:226: T__54
                {
                mT__54(); 

                }
                break;
            case 38 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:232: T__55
                {
                mT__55(); 

                }
                break;
            case 39 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:238: T__56
                {
                mT__56(); 

                }
                break;
            case 40 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:244: T__57
                {
                mT__57(); 

                }
                break;
            case 41 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:250: T__58
                {
                mT__58(); 

                }
                break;
            case 42 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:256: T__59
                {
                mT__59(); 

                }
                break;
            case 43 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:262: T__60
                {
                mT__60(); 

                }
                break;
            case 44 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:268: T__61
                {
                mT__61(); 

                }
                break;
            case 45 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:274: T__62
                {
                mT__62(); 

                }
                break;
            case 46 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:280: T__63
                {
                mT__63(); 

                }
                break;
            case 47 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:286: T__64
                {
                mT__64(); 

                }
                break;
            case 48 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:292: T__65
                {
                mT__65(); 

                }
                break;
            case 49 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:298: T__66
                {
                mT__66(); 

                }
                break;
            case 50 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:304: T__67
                {
                mT__67(); 

                }
                break;
            case 51 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:310: T__68
                {
                mT__68(); 

                }
                break;
            case 52 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:316: T__69
                {
                mT__69(); 

                }
                break;
            case 53 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:322: T__70
                {
                mT__70(); 

                }
                break;
            case 54 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:328: T__71
                {
                mT__71(); 

                }
                break;
            case 55 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:334: T__72
                {
                mT__72(); 

                }
                break;
            case 56 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:340: T__73
                {
                mT__73(); 

                }
                break;
            case 57 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:346: T__74
                {
                mT__74(); 

                }
                break;
            case 58 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:352: T__75
                {
                mT__75(); 

                }
                break;
            case 59 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:358: T__76
                {
                mT__76(); 

                }
                break;
            case 60 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:364: T__77
                {
                mT__77(); 

                }
                break;
            case 61 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:370: T__78
                {
                mT__78(); 

                }
                break;
            case 62 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:376: T__79
                {
                mT__79(); 

                }
                break;
            case 63 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:382: T__80
                {
                mT__80(); 

                }
                break;
            case 64 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:388: EXP
                {
                mEXP(); 

                }
                break;
            case 65 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:392: VAR
                {
                mVAR(); 

                }
                break;
            case 66 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:396: SLIST
                {
                mSLIST(); 

                }
                break;
            case 67 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:402: DecimalLiteral
                {
                mDecimalLiteral(); 

                }
                break;
            case 68 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:417: FloatingPointLiteral
                {
                mFloatingPointLiteral(); 

                }
                break;
            case 69 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:438: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 70 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:453: Identifier
                {
                mIdentifier(); 

                }
                break;
            case 71 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:464: WS
                {
                mWS(); 

                }
                break;
            case 72 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:467: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 73 :
                // C:\\workspace\\AntlrExamples\\src\\UMath\\Distribution.g:1:475: LINE_COMMENT
                {
                mLINE_COMMENT(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\3\uffff\3\54\1\uffff\4\54\4\uffff\1\74\1\77\1\102\1\104\1\110"+
        "\1\113\1\116\1\120\1\122\2\uffff\5\54\1\uffff\1\133\1\135\1\137"+
        "\2\uffff\4\54\2\144\3\uffff\1\147\14\54\30\uffff\7\54\6\uffff\4"+
        "\54\2\uffff\1\144\1\uffff\1\u0081\25\54\1\u0097\1\u0098\1\54\1\uffff"+
        "\1\u009a\3\54\1\u009e\1\u009f\2\54\1\u00a2\1\54\1\u00a4\3\54\1\u00a8"+
        "\4\54\1\u00ad\1\u00ae\2\uffff\1\54\1\uffff\1\u00b0\1\54\1\u00b2"+
        "\2\uffff\1\u00b3\1\54\1\uffff\1\54\1\uffff\1\u00b6\1\u00b7\1\54"+
        "\1\uffff\1\u00b9\1\u00ba\2\54\2\uffff\1\u00bd\1\uffff\1\u00be\2"+
        "\uffff\2\54\2\uffff\1\u00c1\2\uffff\1\u00c2\1\u00c3\2\uffff\1\u00c4"+
        "\1\54\4\uffff\1\u00c6\1\uffff";
    static final String DFA12_eofS =
        "\u00c7\uffff";
    static final String DFA12_minS =
        "\1\11\2\uffff\1\146\1\154\1\162\1\uffff\1\145\1\150\1\157\1\150"+
        "\4\uffff\1\75\1\53\1\55\1\75\1\52\1\46\3\75\2\uffff\1\150\1\157"+
        "\1\141\1\157\1\165\1\uffff\3\75\2\uffff\1\157\1\130\1\101\1\114"+
        "\2\56\3\uffff\1\44\1\164\1\163\1\151\1\164\1\151\1\165\1\145\1\157"+
        "\1\164\1\156\2\141\30\uffff\1\157\1\162\1\156\1\157\1\154\1\165"+
        "\1\154\6\uffff\1\151\1\120\1\122\1\111\2\uffff\1\56\1\uffff\1\44"+
        "\1\145\1\156\1\165\1\157\1\163\1\145\1\141\1\154\1\145\1\164\1\162"+
        "\1\163\1\162\1\151\1\147\1\141\1\163\2\142\1\154\1\144\2\44\1\123"+
        "\1\uffff\1\44\1\164\1\162\1\167\2\44\1\153\1\145\1\44\1\151\1\44"+
        "\1\163\1\164\1\156\1\44\1\164\1\145\1\154\1\145\2\44\2\uffff\1\124"+
        "\1\uffff\1\44\1\156\1\44\2\uffff\1\44\1\141\1\uffff\1\156\1\uffff"+
        "\2\44\1\147\1\uffff\2\44\1\145\1\162\2\uffff\1\44\1\uffff\1\44\2"+
        "\uffff\1\156\1\165\2\uffff\1\44\2\uffff\2\44\2\uffff\1\44\1\145"+
        "\4\uffff\1\44\1\uffff";
    static final String DFA12_maxS =
        "\1\ufaff\2\uffff\1\156\1\154\1\162\1\uffff\1\145\1\162\1\171\1"+
        "\157\4\uffff\6\75\1\174\2\75\2\uffff\1\164\1\157\1\154\1\157\1\165"+
        "\1\uffff\3\75\2\uffff\1\157\1\130\1\101\1\114\2\71\3\uffff\1\ufaff"+
        "\1\164\1\163\1\151\1\164\1\162\1\165\1\145\1\157\1\164\1\156\2\141"+
        "\30\uffff\1\157\1\162\1\156\1\157\1\154\1\165\1\155\6\uffff\1\151"+
        "\1\120\1\122\1\111\2\uffff\1\71\1\uffff\1\ufaff\1\145\1\156\1\165"+
        "\1\157\1\163\1\145\1\141\1\154\1\145\1\164\1\162\1\163\1\162\1\151"+
        "\1\147\1\141\1\163\2\142\1\154\1\144\2\ufaff\1\123\1\uffff\1\ufaff"+
        "\1\164\1\162\1\167\2\ufaff\1\153\1\145\1\ufaff\1\151\1\ufaff\1\163"+
        "\1\164\1\156\1\ufaff\1\164\1\145\1\154\1\145\2\ufaff\2\uffff\1\124"+
        "\1\uffff\1\ufaff\1\156\1\ufaff\2\uffff\1\ufaff\1\141\1\uffff\1\156"+
        "\1\uffff\2\ufaff\1\147\1\uffff\2\ufaff\1\145\1\162\2\uffff\1\ufaff"+
        "\1\uffff\1\ufaff\2\uffff\1\156\1\165\2\uffff\1\ufaff\2\uffff\2\ufaff"+
        "\2\uffff\1\ufaff\1\145\4\uffff\1\ufaff\1\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\3\uffff\1\6\4\uffff\1\13\1\14\1\15\1\16\11\uffff"+
        "\1\30\1\31\5\uffff\1\44\3\uffff\1\67\1\73\6\uffff\1\105\1\106\1"+
        "\107\15\uffff\1\52\1\17\1\20\1\65\1\60\1\21\1\66\1\61\1\22\1\62"+
        "\1\23\1\110\1\111\1\63\1\24\1\46\1\51\1\25\1\45\1\47\1\26\1\50\1"+
        "\27\1\64\7\uffff\1\53\1\70\1\54\1\56\1\55\1\57\4\uffff\1\103\1\104"+
        "\1\uffff\1\3\31\uffff\1\36\25\uffff\1\100\1\101\1\uffff\1\4\3\uffff"+
        "\1\72\1\76\2\uffff\1\34\1\uffff\1\33\3\uffff\1\37\4\uffff\1\71\1"+
        "\75\1\uffff\1\5\1\uffff\1\10\1\11\2\uffff\1\74\1\35\1\uffff\1\40"+
        "\1\77\2\uffff\1\102\1\7\2\uffff\1\43\1\41\1\42\1\32\1\uffff\1\12";
    static final String DFA12_specialS =
        "\u00c7\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\55\1\uffff\2\55\22\uffff\1\55\1\40\1\53\1\uffff\1\54\1\27"+
            "\1\24\1\uffff\1\14\1\15\1\22\1\20\1\16\1\21\1\44\1\23\1\51\11"+
            "\52\1\13\1\6\1\41\1\17\1\42\1\37\1\uffff\4\54\1\46\15\54\1\50"+
            "\2\54\1\47\4\54\1\30\1\uffff\1\31\1\26\1\54\1\uffff\1\54\1\11"+
            "\1\12\1\35\1\4\1\34\2\54\1\3\2\54\1\33\1\54\1\36\1\54\1\5\1"+
            "\54\1\7\1\32\1\10\1\54\1\45\4\54\1\1\1\25\1\2\1\43\101\uffff"+
            "\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff\u0150\54\u0170"+
            "\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff\u5200\54\u5900"+
            "\uffff\u0200\54",
            "",
            "",
            "\1\56\7\uffff\1\57",
            "\1\60",
            "\1\61",
            "",
            "\1\62",
            "\1\63\11\uffff\1\64",
            "\1\66\2\uffff\1\65\6\uffff\1\67",
            "\1\71\3\uffff\1\72\2\uffff\1\70",
            "",
            "",
            "",
            "",
            "\1\73",
            "\1\76\21\uffff\1\75",
            "\1\101\17\uffff\1\100",
            "\1\103",
            "\1\106\4\uffff\1\107\15\uffff\1\105",
            "\1\112\26\uffff\1\111",
            "\1\114\76\uffff\1\115",
            "\1\117",
            "\1\121",
            "",
            "",
            "\1\123\13\uffff\1\124",
            "\1\125",
            "\1\127\12\uffff\1\126",
            "\1\130",
            "\1\131",
            "",
            "\1\132",
            "\1\134",
            "\1\136",
            "",
            "",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\145\1\uffff\12\145",
            "\1\145\1\uffff\12\146",
            "",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\155\10\uffff\1\154",
            "\1\156",
            "\1\157",
            "\1\160",
            "\1\161",
            "\1\162",
            "\1\163",
            "\1\164",
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
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "\1\174\1\173",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\175",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "",
            "",
            "\1\145\1\uffff\12\146",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\1\u008a",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u0099",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u009b",
            "\1\u009c",
            "\1\u009d",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00a0",
            "\1\u00a1",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00a3",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00a9",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "",
            "\1\u00af",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00b1",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00b4",
            "",
            "\1\u00b5",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00b8",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00bb",
            "\1\u00bc",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "",
            "\1\u00bf",
            "\1\u00c0",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            "\1\u00c5",
            "",
            "",
            "",
            "",
            "\1\54\13\uffff\12\54\7\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\105\uffff\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff"+
            "\u0150\54\u0170\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff"+
            "\u5200\54\u5900\uffff\u0200\54",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | EXP | VAR | SLIST | DecimalLiteral | FloatingPointLiteral | STRING_LITERAL | Identifier | WS | COMMENT | LINE_COMMENT );";
        }
    }
 

}