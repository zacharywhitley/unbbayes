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


import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.Debug;

/**
 * Use this instead of Compiler to parse MEBN nodes' tables
 * It encapsulates some details of consistency checking and
 * 	provides a way to avoid sequence-coupling programming anti-pattern.
 * @author Shou Matsumoto
 * @see unbbayes.prs.mebn.compiler.Compiler
 *
 */
public class MEBNTableParser implements ICompiler {
	
	private ICompiler compiler = null;
	private MultiEntityBayesianNetwork mebn = null;
	private ResidentNode node = null;
	
	private String text = null;
	
	private MEBNTableParser(MultiEntityBayesianNetwork mebn, ResidentNode node) {
		super();
		//this.setCompiler(new Compiler());
		this.setCompiler(new Compiler(node));
		((Compiler)this.getCompiler()).setNode(node);
	}
	
	/**
	 * Instantiates MEBNTableParser
	 * @param node: the node where the table resides
	 * @return instance of MEBNTableParser
	 */
	/*public static MEBNTableParser getInstance(DomainResidentNode node) {
		MEBNTableParser parser = new MEBNTableParser( node.getMFrag().getMultiEntityBayesianNetwork(),  node);
		return parser;
	}*/
	
	/**
	 * Instantiates MEBNTableParser
	 * @param mebn: the MEBN currently evaluated
	 * @param node: the node where the table resides
	 * @return instance of MEBNTableParser
	 */
	public static MEBNTableParser getInstance(MultiEntityBayesianNetwork mebn, ResidentNode node) {
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
	public static MEBNTableParser getInstance(MultiEntityBayesianNetwork mebn, ResidentNode node, ICompiler compiler) {
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
	public ICompiler getCompiler() {
		return compiler;
	}

	/**
	 * @param compiler Sets the currently used implementation of AbstractCompiler to parse tables.
	 */
	public void setCompiler(ICompiler compiler) {
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
	public ResidentNode getNode() {
		return node;
	}

	/**
	 * @param node The node to set.
	 */
	public void setNode(ResidentNode node) {
		this.node = node;
		this.mebn = node.getMFrag().getMultiEntityBayesianNetwork();
	}
	
	/**
	 * Use this method to determine where the error has occurred
	 * @return Returns the last read index.
	 */
	public int getIndex() {
		return this.compiler.getIndex();
	}
	
	public ProbabilisticTable generateCPT(SSBNNode ssbnnode) {
		return null;
	}

}
