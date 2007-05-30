/**
 * 
 */
package unbbayes.prs.mebn.compiler;


import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.Debug;

/**
 * Use this instead of Compiler to parse MEBN nodes' tables
 * It encapsulates some details of consistency checking and
 * 	provides a way to avoid sequence-coupling programming anti-pattern.
 * @author Shou Matsumoto
 * @see unbbayes.prs.mebn.compiler.Compiler
 *
 */
public class MEBNTableParser implements AbstractCompiler {
	
	private AbstractCompiler compiler = null;
	private MultiEntityBayesianNetwork mebn = null;
	private DomainResidentNode node = null;
	
	private String text = null;
	
	private MEBNTableParser(MultiEntityBayesianNetwork mebn, DomainResidentNode node) {
		super();
		this.setCompiler(new Compiler());
		((Compiler)this.getCompiler()).setNode(node);
	}
	
	/**
	 * Instantiates MEBNTableParser
	 * @param node: the node where the table resides
	 * @return instance of MEBNTableParser
	 */
	public static MEBNTableParser getInstance(DomainResidentNode node) {
		MEBNTableParser parser = new MEBNTableParser( node.getMFrag().getMultiEntityBayesianNetwork(),  node);
		return parser;
	}
	
	/**
	 * Instantiates MEBNTableParser
	 * @param mebn: the MEBN currently evaluated
	 * @param node: the node where the table resides
	 * @return instance of MEBNTableParser
	 */
	public static MEBNTableParser getInstance(MultiEntityBayesianNetwork mebn, DomainResidentNode node) {
		MEBNTableParser parser = new MEBNTableParser( mebn,  node);
		return parser;
	}
	
	/**
	 * Instantiates MEBNTableParser
	 * @param mebn: the MEBN currently evaluated
	 * @param node: the node where the table resides
	 * @param compiler: sets the implementation of AbstractCompiler the instance of MEBNTableParser shall behave like.
	 * @return instance of MEBNTableParser
	 */
	public static MEBNTableParser getInstance(MultiEntityBayesianNetwork mebn, DomainResidentNode node, AbstractCompiler compiler) {
		MEBNTableParser parser = new MEBNTableParser( mebn,  node);
		parser.setCompiler(compiler);
		if (parser.getCompiler() instanceof Compiler) {
			((Compiler)parser.getCompiler()).setNode(node);
		}
		return parser;
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#init(java.lang.String)
	 */
	public void init(String text) {
		compiler.init(text);
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException{
		try {
			this.compiler.init(this.text);
		} catch (Exception e) {
			// default. Get table from node
			Debug.println(e.getMessage());
			this.compiler.init(this.node.getTableFunction());
		}
		
		this.compiler.parse();
		
	}
	
	/**
	 * Parses initializing the parser as txt
	 * It changes the current evaluated table string to txt
	 * @param txt: the text to parse
	 */
	public void parse(String txt) throws MEBNException {
		this.init(txt);
		this.parse();
	}

	/**
	 * @return Returns the currently used implementation of AbstractCompiler to parse tables.
	 */
	public AbstractCompiler getCompiler() {
		return compiler;
	}

	/**
	 * @param compiler Sets the currently used implementation of AbstractCompiler to parse tables.
	 */
	public void setCompiler(AbstractCompiler compiler) {
		this.compiler = compiler;
		// If its not our compiler, no need for the nodes
		if (!(compiler instanceof Compiler)) {
			this.node = null;
			this.mebn = null;
		}
	}

	/**
	 * @return Returns the mebn.
	 */
	public MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}

	/**
	 * @param mebn The mebn to set.
	 */
	/*public void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}*/

	/**
	 * @return Returns the node where table resides.
	 */
	public DomainResidentNode getNode() {
		return node;
	}

	/**
	 * @param node The node to set.
	 */
	public void setNode(DomainResidentNode node) {
		this.node = node;
		this.mebn = node.getMFrag().getMultiEntityBayesianNetwork();
	}

}
