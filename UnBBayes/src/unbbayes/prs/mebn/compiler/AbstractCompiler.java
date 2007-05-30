package unbbayes.prs.mebn.compiler;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.exception.MEBNException;

public interface AbstractCompiler {

	/**
	 * Initializes compiler. Sets the text to parse by
	 * parse() method
	 * @param text: text to parse
	 * @see unbbayes.gui.UnBBayesFrame.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public abstract void init(String text);
	
	/**
	 * Parse the string passed by init method
	 * @see unbbayes.gui.UnBBayesFrame.prs.mebn.compiler.AbstractCompiler#init(String)
	 */
	public abstract void parse() throws MEBNException;
	
	
}