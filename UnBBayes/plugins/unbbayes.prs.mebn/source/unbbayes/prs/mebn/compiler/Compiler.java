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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.InstanceException;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;


/*
 BNF MEBN Table:
 ----------------
 Changes (Month/Date/Year): 
 
 	08/02/2008:
 			Description: Added nested "if" feature.
 			Now an if-clause would look like this ("else" is still mandatory):
 			
 				if any x have (parentx = valuex1) [
 					myprob1 = 0.5 , 
 					myprob2 = 0.5
 				] else if all y have (parenty = valuey2) [
 					if any z have (parentz = valuez3) [
 						myprob1 = 0.2,
 						myprob2 = 0.8
 					] else [
 						myprob1 = 0.8,
 						myprob2 = 0.2
 					]
 				] else if any w have (parentw = veluew4) [
 					if any v have (parentv = valuev5 || parentv = valuev6) [
 						if any v have (parentv = valuev5 || parentv = valuev6) [
 							if any v have (parentv = valuev5 || parentv = valuev6) [
 								myprob1 = 0.8,
 								myprob2 = 0.2
 							] else [
 								myprob1 = 0.7,
 								myprob2 = 0.3
 							]
 						] else [
 							myprob1 = 0.6,
 							myprob2 = 0.4
 						]
 					] else [
 						myprob1 = 0.5,
 						myprob2 = 0.5
 					]
 				] else [
 					if all asdf have (parentasdf = valueasdf) [
 						myprob1 = 0.1 , 
 						myprob2 = 0.9
 					] else [
 						myprob1 = 0.9 , 
 						myprob2 = 0.1
 					]
 				]
 			Author: Shou Matsumoto (cardialfly@[gmail,yahoo].com)
 
 	07/09/2008:
 			Description: BNF fails to describe a complex boolean expression
 			Author: Shou Matsumoto (cardialfly@[gmail,yahoo].com)
 	
 	06/15/2008:
 			Description: a boolean expression was returning a boolean neutral value (false in "Any" 
 			and true in "All") when no valid expression (involving parents) was evaluated. It
 			is now returning false when no expression was valid
 			Author: Shou Matsumoto (cardialfly@[gmail,yahoo].com)
 
 	03/03/2008:
 			Description: compiler was trying to parse a table even when node was
 			known to be a finding (a finding doesn't need a cpt). This condition now 
 			is tested before parsing a ssbn node.
 			Author: Shou Matsumoto (cardialfly@[gmail,yahoo].com)
 
 	12/28/2007:
 			Description: fixed the BNF definition of factor, and
 			added the " char before and after ;.
 			Author: Rommel Carvalho (rommel.carvalho@gmail.com)
 
 	11/27/2007:
 			Description: term ::= signed_factor [ mulop factor ]* changed to
 				term ::= signed_factor [ mulop signed_factor ]*,
 				and CPT generation is in alpha state now.
 			Author: Shou Matsumoto
 
 	11/25/2007:
 			Description: added non-terminal variable "possibleVal" to the grammar (and its implementation).
 			Author: Shou Matsumoto
 	
 	10/07/2007:
 			Description: "varsetname" has been added to the grammar (and implemented inside the class)
 				in order to allow us to declare parent set by strong OV.
 			Author: Shou Matsumoto
 	
 
 	06/24/2007:
 			Description: The top level BNF Grammar class was changed from 
 				if_statement to table, in order to make possible a probability
 				table without an if clause.
			Author: Shou Matsumoto
 
 	06/10/2007: 
 			Description: Added cardinality(), min() and max() functions
 			syntax analyzer.
 			Author: Shou Matsumoto 

 	05/29/2007: 
 			Description: the else clause is now required, in order to
 				force user to declare a default distribution and
 				grant declaration of every possible combination of states
 				(if we don't add this restriction, a semantic analysis
 				would be required in order to verify if every combination
 				of states were provided and the last else
 				must be related to the first if every time - since
 				we don't have a block sentence yet, it is not possible).
 			Author: Shou Matsumoto
 			

 	
 ===============================================================
 table := statement | if_statement
 if_statement  ::= 
 	"if" allop varsetname "have" "(" b_expression ")" statement 
 	"else" else_statement 
 allop ::= "any" | "all"
 varsetname ::= ident ["." ident]*
 b_expression ::= b_term [ "|" b_term ]*
 b_term ::= not_factor [ "&" not_factor ]*
 not_factor ::= [ "~" ] b_factor
 b_factor ::= ident "=" ident | "(" b_expression ")"
 else_statement ::= statement | if_statement
 statement ::= "[" assignment_or_if "]" 
 assignment_or_if ::= assignment | if_statement
 assignment ::= ident "=" expression [ "," assignment ]*
 expression ::= term [ addop term ]*
 term ::= signed_factor [ mulop signed_factor ]*
 signed_factor ::= [ addop ] factor
 factor ::= number | function | "(" expression ")"
 function ::= possibleVal 
 	| "CARDINALITY" "(" varsetname ")"
 	| "MIN" "(" expression ";" expression ")"
 	| "MAX" "(" expression ";" expression ")"
 possibleVal ::= ident
 addop ::= "+" | "-"
 mulop ::= "*" | "/"
 ident ::= letter [ letter | digit ]*
 number ::= [digit]+
 ================================================================
 */



public class Compiler implements ICompiler {

	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of compiler
     */
    private static class SingletonHolder { 
    	private static final Compiler INSTANCE = new Compiler();
    }
 
    /** If this class' constructor should return a singleton or not */
	private static Boolean singleton = false;
	
	// resource files
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.prs.mebn.compiler.resources.Resources.class.getName());

	
	/* A previously read character (lookahead) */
	private char look;

	/* Current text cursor position (where inside "text" we're scanning now) */
	private int index = 0;

	private char[] text = null;

	/* keywords. */
	private String kwlist[] = { "IF", "ELSE", "ALL", "ANY", "HAVE" };

	/*
	 * Special codes for keywords.
	 * Obviously, it should represent kwlist.
	 */
	private char kwcode[] = { 'i', 'l', 'a', 'y', 'h' };

	/* coded token */
	private char token;

	/*
	 * uncoded token's value
	 */
	private String value = "";
	private String noCaseChangeValue = "";
	
	
	// Informations used by this class to check pre-SSBN consistency
	private MultiEntityBayesianNetwork mebn = null;
	private ResidentNode node = null;
	

	// Variables used for ProbabilisticTable generation
	private PotentialTable cpt = null;
	private SSBNNode ssbnnode = null;
	
	/*
	 * This temporally represents a parsed CPT, to make potential table generation easier, like:
	 * 
	 * (TempTableHeaderCell)      (TempTableHeaderCell)      (TempTableHeaderCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 *          |                          |                            |
	 * (TempTableProbabilityCell) (TempTableProbabilityCell) (TempTableProbabilityCell)
	 * 
	 * Where TempTableHeaderCell contains the parent and its expected value in the table.
	 * TempTableProbabilityCell represents a possible value of THIS node and its occurrence
	 * probability.
	 * 
	 */
	private TempTable tempTable = null;
	private TempTableHeaderCell currentHeader = null;
	//private List<TempTableProbabilityCell> currentProbCellList = null;
	//private TempTableProbabilityCell currentCell = null;
	
	
	private int originalTextLength = 0;	// stores the length of the original text before deleting extra spaces
	
	private Compiler() {
		tempTable = new TempTable();
		originalTextLength = 0;
		this.cpt = null;
	}
	
	
//	/**
//	 * Creates an instance of Compiler. The resident node is necessary
//	 * in order to perform semantic consisntency check.
//	 * @param node: a resident node containing the table to parse
//	 * @return a instance of the compiler.
//	 */
//	protected Compiler (ResidentNode node) {
//		this();
//		this.setNode(node);
//	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * Depending on the application.properties file read by {@link ApplicationPropertyHolder}, 
	 * this method may return a singleton instance.
	 * @param node: a resident node containing the table to parse
	 * @param ssbnnode : a node actually generating cpt table at ssbn generation time. It is optional
	 * @return a instance of the compiler.
	 * @see {@link ApplicationPropertyHolder}
	 */
	public static Compiler getInstance(ResidentNode node, SSBNNode ssbnnode) {
		Compiler comp = null;
		try {
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
    		if (Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
    				Compiler.class.getCanonicalName()+".singleton").toString())) {
        		comp = SingletonHolder.INSTANCE;
        	} else {
        		comp = new Compiler();
        	}
		} catch (Exception e) {
			e.printStackTrace();
			comp = new Compiler();
		}
		comp.setNode(node);
		comp.ssbnnode = ssbnnode;
		if (comp.ssbnnode != null) {
			if (comp.ssbnnode.getProbNode() != null) {
				comp.cpt = comp.ssbnnode.getProbNode().getProbabilityFunction();
			}			
		}
		return comp;
	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * @param node: a resident node containing the table to parse
	 * @return a instance of the compiler.
	 * @see {@link Compiler#getInstance(ResidentNode, SSBNNode)}
	 */
	public static Compiler getInstance(ResidentNode node) {
	// since we are not using other specific pseudocode Compilers, and we do not use Builders/Factories,
	// it is not necessary to have a constructor method...
		return Compiler.getInstance(node, null);
	}
	
	
//	/**
//	 * Note: if the pseudocode passed to this class is either empty or null, this class
//	 * should consider equal probability distribution for all possible states.
//	 * TODO break this class apart, because it's becoming too huge.
//	 * @param node: the node having the CPT's pseudocode being evaluated by this class.
//	 * @param ssbnnode: the node where we should set the output CPT to.
//	 */
//	protected Compiler (ResidentNode node, SSBNNode ssbnnode) {
//		this(node);
//		this.ssbnnode = ssbnnode;
//		if (this.ssbnnode != null) {
//			if (this.ssbnnode.getProbNode() != null) {
//				this.cpt = this.ssbnnode.getProbNode().getPotentialTable();
//			}			
//		}
//	}
	
	/* Compiler's initialization */
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#init(java.lang.String)
	 */
	public void init(String text) {
		this.originalTextLength = 0;
		if (text == null) {
			this.originalTextLength = 0;
			this.text = null;
			return;
		} else if (text.length() == 0) {
			/*
			 * testing if ((text == null) || text.isEmpty()) would be much better, but
			 * since some non-standard JVM could not to stop testing boolean OR condition
			 * after finding a true value, the text.isEmpty() might be executed without
			 * testing if it is null, which may cause NullPointerException.
			 */
			this.originalTextLength = 0;
			this.text = null;
			return;
		}
		index = 0;
		// Debug.println("************************************");
		// Debug.println("ORIGINAL: " + text);
		//if (text != null) {
			originalTextLength = text.length();
			text = text.replaceAll("\\s+", " ");
		//}
		
		// Debug.println("CHANGED: " + text);
		// Debug.println("************************************");
		//if (text != null) { 
			this.text = text.toCharArray();
			nextChar();
		//}
		
		tempTable = new TempTable();
	}
	
	/**
	 * use this method to initialize this parser on SSBN generation step.
	 * All information will be extracted from the ssbnnode.
	 * @param ssbnnode
	 */
	public void init(SSBNNode ssbnnode) {
		this.setSSBNNode(ssbnnode);
		//this.node = ssbnnode.getResident();	//setSSBNNode already does it.
		this.mebn = this.node.getMFrag().getMultiEntityBayesianNetwork();
		String pseudocode = this.node.getTableFunction();
		
		if (this.ssbnnode.getProbNode() != null) {
			this.setPotentialTable(this.ssbnnode.getProbNode().getProbabilityFunction());			
		}
		
		this.init(pseudocode);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.AbstractCompiler#parse()
	 */
	public void parse() throws MEBNException {
		//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>(); //Initialize lists
		if (this.text == null) {
			// Debug.println("Pseudocode = null");
			return;
		}
		// Debug.println("PARSED: ");
		this.skipWhite();
		this.table();
	}
	
	/**
	 * this is identical to init(table)->parse()
	 * @param table
	 * @throws MEBNException
	 */
	public void parse(String table) throws MEBNException {
		this.init(table);
		this.parse();
	}
	
	/**
	 * This method actually regenerates a CPT.
	 * This method will not parse. Use parse() before this.
	 * Please, note that this method will generate linear (equal) distribution
	 * if no pseudocode is declared (all values will have same probability).
	 * @return GENERATED potential table
	 */
	protected PotentialTable getCPT() throws InconsistentTableSemanticsException,
											InvalidProbabilityRangeException,
											InstanceException{
		
		// try to clean garbage before starting an expensive method like this one
		System.gc();
		
		// initial tests
		if (this.ssbnnode == null) {
			return null;
		}
		if (this.tempTable == null) {
			return null;
		}
		
//		if (this.ssbnnode.isFinding()) {
//			return null;
//		}
		
		if (this.ssbnnode.getProbNode() == null) {
			return null;
		}
		if (this.text == null || this.tempTable.isEmptyNestedClauses() ) {
			// Special condition: if pseudocode was not declared, use linear (equal) distribution instead
			this.generateLinearDistroCPT(this.ssbnnode.getProbNode());
			return this.ssbnnode.getProbNode().getProbabilityFunction();
		}
		
		// initialization
		
		// sets all parents current mfrag as the same of this ssbn node, in order to use same OV names
		// use a map to store previous states, in order to rollback at the end of CPT generation
		Map<SSBNNode, MFrag> parentToPreviousMFragMap = new HashMap<SSBNNode, MFrag>();
		for (SSBNNode parent : this.getSSBNNode().getParents()) {
			if (parent.getCurrentlySelectedMFragByTurnArguments() != null) {
				parentToPreviousMFragMap.put(parent, parent.getCurrentlySelectedMFragByTurnArguments());
			} else {
				//defaults to resident node's mfrag
				parentToPreviousMFragMap.put(parent, parent.getResident().getMFrag());
			}
			try {
				parent.turnArgumentsForMFrag(this.getSSBNNode().getResident().getMFrag());
			} catch (Exception e) {
				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		// eliminates redundancies on table's boolean expression
		try {
			this.tempTable.cleanUpKnownValues(this.getSSBNNode());
		} catch (NullPointerException e) {
			// The SSBNNode was null...
			throw new InstanceException(e);
		}
		
		// extracting base values
		this.cpt = this.ssbnnode.getProbNode().getProbabilityFunction();
		
		ArrayList<SSBNNode> parents = null;
		try {
			parents = new ArrayList<SSBNNode>(this.ssbnnode.getParents());
		} catch (NullPointerException e) {
			parents = new ArrayList<SSBNNode>();
		} catch (Exception e) {
			throw new InconsistentTableSemanticsException(e);
		}
		
		
		ArrayList<Entity> possibleValues = new ArrayList<Entity>(this.ssbnnode.getActualValues());
		if (!this.ssbnnode.isFinding() && ( this.ssbnnode.getProbNode().getStatesSize() != possibleValues.size()  )) {
			// the ssbnnode and the table is not synchronized!!
			throw new InconsistentTableSemanticsException();
		}
		
		
		Map<String, List<EntityAndArguments>> map = null; // parameter of boolean expression evaluation method
		
		// this iterators helps us combine parents' possible values
		// e.g. (True,Alpha), (True,Beta), (False,Alpha), (False,Beta).
		List<Iterator<Entity>> valueCombinationIterators = new ArrayList<Iterator<Entity>>();
		for (SSBNNode ssbnnode : parents) {
			valueCombinationIterators.add(ssbnnode.getActualValues().iterator());
		}
		
		// saves the current values of the iterators
		List<Entity> currentIteratorValue = new ArrayList<Entity>();
		for (Iterator it : valueCombinationIterators) {
			 currentIteratorValue.add((Entity)it.next());
		}
		
		
		// start running at the probabilistic table and filling its cells
		//List<Entity> entityList = null;
		for( int i = 0; i < this.cpt.tableSize(); i += this.ssbnnode.getProbNode().getStatesSize()) {
			//	clears and initializes map
			map = new HashMap<String, List<EntityAndArguments>>();
			for (SSBNNode ssbnnode : parents) {
			  if (!map.containsKey(ssbnnode.getResident().getName())) {
				  map.put(ssbnnode.getResident().getName(), new ArrayList<EntityAndArguments>());
			  }
			}
			
			
			// fill map at this loop. Note that parents.size, currentIteratorValue.size, and
			// valueCombinationiterators are the same
			for (int j = 0; j < parents.size(); j++) {
				EntityAndArguments val = new EntityAndArguments(currentIteratorValue.get(j),new ArrayList<OVInstance>(parents.get(j).getArguments()));
				map.get(parents.get(j).getResident().getName()).add(val);
			}
			
			
			// updates iterators
			for (int j = 0; j < valueCombinationIterators.size(); j++) {
				if (valueCombinationIterators.get(j).hasNext()) {
					// if has next, then update current value and exits loop
					currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
					break;
				} else {
					// else, reset the iterator (and current value) until exit loop
					valueCombinationIterators.set(j, parents.get(j).getActualValues().iterator());
					if (valueCombinationIterators.get(j).hasNext()) {
						currentIteratorValue.set(j, valueCombinationIterators.get(j).next());
					}
				}
			}
			
				
			// prepare to extract which column to verify
			TempTableHeaderCell header = null;
			
			// if default distro, then use the default header...
			// also, if no parents are declared, use default distro...
			boolean thereAreNoParents = false;
			if(this.getSSBNNode().getParents() == null) {
				thereAreNoParents = true;
			} else if (this.getSSBNNode().getParents().size() == 0) {
				thereAreNoParents = true;
			}
			if (thereAreNoParents || this.getSSBNNode().isUsingDefaultCPT()) {
				// we assume the default distro is the last block on pseudocode
				header = this.tempTable.getDefaultClause();
				// let's just check if this header is really declaring a default distro... Just in case...
				if (!header.isDefault()) {
					throw new InconsistentTableSemanticsException();
				}
			} else {
				//	if not default, look for the column to verify
				// the first expression to return true is the one we want
				header = this.tempTable.getFirstTrueClause(map);
				// note that default (else) expression should allways return true!
				
			}
			
			
			
			
			// populate column
			for (int j = 0; j < possibleValues.size() ; j++) {
				float value = -1.0f;
				
				// extract the value to set
				for (TempTableProbabilityCell cell : header.getCellList()) {
					// we assume the Probabilistic table is in the same order of the list "possibleValues", 
					// so we look for the entity of possibleValues at a cell
					if (cell.getPossibleValue().getName().equalsIgnoreCase(possibleValues.get(j).getName())) {
						value = cell.getProbabilityValue();
						break;
					}
				}
				// consistency check, allow only values between 0 and 1
				if ((value < 0) || (value > 1)) {
					throw new InvalidProbabilityRangeException();
				}
				this.cpt.setValue(i+j, value);
			}
			
		}	// while i < this.cpt.tableSize()
		
		// the code below is commented because calling getCPT twice must be working nicely.
//		// dispose temporary table, because since it is useless anymore
//		try{
//			this.tempTable.clear();
//		} catch (UnsupportedOperationException uoe) {
//			uoe.printStackTrace();
//			
//		}
		
		
		// rollback MFrag settings
		for (SSBNNode parent : parentToPreviousMFragMap.keySet()) {
			try{
				parent.turnArgumentsForMFrag(parentToPreviousMFragMap.get(parent));
			} catch (Exception e) {
				Debug.println(this.getClass(), parent.toString(), e);
			}
		}
		
		return this.cpt;
	}
	
	public void clear() {
		this.tempTable = null;
		this.cpt = null;
		this.currentHeader = null;
		this.kwcode = null;
		this.kwlist = null;
		this.mebn = null;
		this.noCaseChangeValue = null;
		this.node = null;
		this.ssbnnode = null;
		this.text = null;
		this.value = null;
		System.gc();
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#generateCPT()
	 */
	protected IProbabilityFunction generateCPT() throws MEBNException {
		return this.generateCPT(this.getSSBNNode());
	}

	
	/**
	 * this is identical to init(ssbnnode) -> parse() -> getCPT.
	 *  @return 
	 */
	public PotentialTable generateCPT(SSBNNode ssbnnode) throws MEBNException {
		System.gc();
		if (ssbnnode == null) {
			return null;
		}
//		if (ssbnnode.isFinding()) {
//			return null;
//		}
		this.init(ssbnnode);
		this.parse();
		return getCPT();
	}
	
	
	/**
	 * This method just fills the node's probabilistic tables w/ equal values.
	 * For instance, if a node has 4 possible values, then the table will contain
	 * only cells w/ 25% value (1/4).
	 * @param probNode
	 */
	public IProbabilityFunction generateLinearDistroCPT(ProbabilisticNode probNode) {
		float value = 1.0F / probNode.getStatesSize();
		PotentialTable table = probNode.getProbabilityFunction();
		for (int i = 0; i < probNode.getProbabilityFunction().tableSize(); i++) {
			 // TODO in float operation, since 1/3 + 1/3 + 1/3 might not be 1, implement some precision control
			 table.setValue(i, value);
		}
		return table;
	}
	
	/**
	 *  table := statement | if_statement
	 */
	private void table() throws NoDefaultDistributionDeclaredException,
	  							InvalidConditionantException,
	  							SomeStateUndeclaredException,
	  							InvalidProbabilityRangeException,
	  							TableFunctionMalformedException{
		
		if (this.look == '[') {
			// Debug.println("STARTING DEFAULT STATEMENT");	
			
			// Prepare temporary table's header to declare a default (no-if-clause) statement
			this.currentHeader = new TempTableHeaderCell(null, true, true, this.ssbnnode);
			this.tempTable.addNestedClause(this.currentHeader);			
			
			// every exceptions would be thrown up...
			statement(this.currentHeader);
			
		} else {
			// We don't have to prepare temporary table's header to declare a if-clause statement
			// because the if statement parser would do so.
			
			// Please note table() repasses every exception reported by ifStatement()
			
			// evaluate if-statement setting the tempTable as the upper statement container
			this.ifStatement(this.tempTable);
		}
		
		// after the final else clause, no declaration should be present
		this.skipWhite();
		if (this.look != ' ') {
			expected("end of declaration");
		}
		
	}

	/**
	 * if_statement ::= "if" allop ident "have" "(" b_expression ")" statement [
	 * "else" else_statement ]
	 * @param upperIf: if this if clause is an inner if, the upper if-clause should be referenced
	 * by this parameter. If this is a temporary table, this if-clause is the upper-most if-clause within CPT pseudocode.
	 * If it is null, it will assume tempTable is the upper container
	 */
	private void ifStatement(INestedIfElseClauseContainer upperIf) 
							   throws NoDefaultDistributionDeclaredException,
									  InvalidConditionantException,
									  SomeStateUndeclaredException,
									  InvalidProbabilityRangeException,
									  TableFunctionMalformedException{
		// Debug.println("PARSING IF STATEMENT");
		// SCAN FOR IF. Note that any blank spaces were already skipped
		scan();
		matchString("IF");
		
		
		// SCAN FOR ALL/ANY
		scan();
		switch (token) {
		case 'a':
			// Debug.println("ALL VERIFIED");
			// sets the table header w/ this parameters (empty list,false,false): empty list (no verified parents), is not ANY and is not default
			this.currentHeader = new TempTableHeaderCell(new ArrayList<TempTableHeaderParent>(), false, false, this.ssbnnode);
			break;
		case 'y':
			// Debug.println("ANY VERIFIED");
			//	sets the table header w/ this parameters (empty list,true,false): empty list (no verified parents), is ANY and is not default
			this.currentHeader = new TempTableHeaderCell(new ArrayList<TempTableHeaderParent>(), true, false, this.ssbnnode);
			break;
		default:
			expected("ALL or ANY");
		}

		// stores this.currentHeader in order to become an upper clause of any further nested if/else clause
		INestedIfElseClauseContainer currentIfContainer = this.currentHeader;

		
		// adds the header to the container (table or upper if/else-clause) before it is changed to another header.
		if (upperIf == null) {
			// No upper container identified. Let's assume to be the upper-most container (the temporary table)
			upperIf = this.tempTable;			
		} 		
		upperIf.addNestedClause(this.currentHeader);
		
		// SCAN FOR varsetname
		String varSetName = this.varsetname();
		this.currentHeader.setVarsetname(varSetName);
		// Debug.println("SCANNED VARSETNAME := " + varSetName);

		// SCAN FOR HAVE
		// Debug.println("SCAN FOR HAVE");
		scan();
		matchString("HAVE");

		// ( EXPECTED
		match('(');
		// if we catch sintax error here, it may be conditionant error
		
		// Now, parsing a boolean expression - tree format (we'll store it inside this variable)
		ICompilerBooleanValue expressionTree = null;
		
		try {
			expressionTree = bExpression();
		} catch (TableFunctionMalformedException e) {
			throw new InvalidConditionantException(e);
		}
		//this.nextChar();
		//this.skipWhite();
		// Debug.println("LOOKAHEAD = " + look);
		// ) EXPECTED
		match(')');
		
		// since we extracted the expression tree, store it inside the current header in temporary table
		if (expressionTree != null) {
			this.currentHeader.setBooleanExpressionTree(expressionTree);
		} else {
			throw new InvalidConditionantException();
		}
		
		// Debug.println("STARTING STATEMENTS");
		
		// if we catch a sintax error here, it may be a value error
		try {
			// if there is a nested if, this if should be the upper clause (set currentHeader as upper clause).
			statement(currentIfContainer);
		} catch (TableFunctionMalformedException e) {
			// Debug.println("->" + getNode());
			throw new InvalidProbabilityRangeException("["+this.getNode().getName()+"]",e);
		}
		
		
		
		// Debug.println("LOOKING FOR ELSE STATEMENT");
		// LOOK FOR ELSE
		// Consistency check C09: the grammar "may" state else as optional,
		// but semantically every table must have a default distribution, which is
		// declared within an else clause.
		
		// We dont have to create a new temp table header, because else_statement would do so.
		
		//	This test is necessary to verify if  there is an else clause
		if (this.index < this.text.length) {
			try {
				scan();
			} catch (TableFunctionMalformedException e) {
				// a sintax error here represents a statement other than an else statement
				throw new NoDefaultDistributionDeclaredException(e);
			}
		} else {
			// No statement was found at all (that means no else statement).
			// Debug.println("END OF TABLE");
			throw new NoDefaultDistributionDeclaredException();
		}
		
		if (token == 'l') {
			// The else statement should be a child statement of the upper container,
			// that means, it is on the same level of currently evaluated IF clause
			else_statement(upperIf);
		} else {
			// The statement found was not an else statement
			throw new NoDefaultDistributionDeclaredException();
		}
		
		// we may have another if/else clause after this...
		
	}
	
	/**
	 *   It skippes white spaces after evaluation.
	 *   varsetname ::= ident["."ident]*
	 */
	private String varsetname() throws TableFunctionMalformedException {
		
		// we don't have to set header's varsetname here because ifStatement (upper caller) would do so.
		
		String ret = "";	// a string containing "varsetname" (e.g. "st.sr.z")
		
		// scan for the ident
		do {
			scanNoSkip();	// no white spaces should stay between ident and "." and next ident
			if (token == 'x') {
				// Debug.println("SCANING IDENTIFIER " + value);
				ret += this.noCaseChangeValue;
			} else {
				expected("Identifier");
			}	
			
			// search for ["." ident]* loop
			if (this.look == '.') {
				this.nextChar();
				if (this.ssbnnode != null) {
					ret += this.ssbnnode.getStrongOVSeparator();	// adds a separator (a dot ".")
				} else {
					ret += ".";
				}
				continue;
			} else {
				break;
			}
		} while (index < text.length); 	// actually, this check is unreachable
		
		skipWhite();
		return ret;
	}

	/**
	 * b_expression ::= b_term [ "|" b_term ]*
	 * 
	 */
	private ICompilerBooleanValue bExpression() throws InvalidConditionantException,
									  TableFunctionMalformedException{
		
		ICompilerBooleanValue val1 = bTerm();
		
		// LOOK FOR OR (OPTIONAL)
		// scan();
		if (look == '|') {
			match('|');
			ICompilerBooleanValue val2 = bTerm();
			// Debug.println("EXITING BEXPRESSION AS OR");
			if (look == '|') {
				match('|');
				return new CompilerOrValue(val1, new CompilerOrValue(val2,bExpression()));
			} else {
				return new CompilerOrValue(val1, val2);
			}
		} else {
			// Debug.println("EXITING BEXPRESSION AS SINGLE TERM");			
			return val1;
		}
		
	}

	/**
	 * b_term ::= not_factor [ "&" not_factor ]*
	 * 
	 */
	private ICompilerBooleanValue bTerm() throws InvalidConditionantException,
							    TableFunctionMalformedException{
		
		ICompilerBooleanValue val1 = notFactor();

		// LOOK FOR AND (OPTIONAL)
		// scan();
		if (look == '&') {
			match('&');
			ICompilerBooleanValue val2 = notFactor();
			if (look == '&') {
				match('&');
				return new CompilerAndValue(val1, new CompilerAndValue(val2,bTerm()));
			} else {
				return new CompilerAndValue(val1, val2);
			}
		} else {
			return val1;
		}
	}

	/**
	 * not_factor ::= [ "~" ] b_factor
	 * 
	 */
	private ICompilerBooleanValue notFactor() throws InvalidConditionantException,
									TableFunctionMalformedException{
		
		boolean isNot = false;	// tests if '~' was found previously.
		
		// SCAN FOR NOT (OPTIONAL)
		// scan();
		if (look == '~') {
			isNot = true;
			match('~');
		}

		ICompilerBooleanValue factor = bFactor();
		if (factor == null) {
			throw new TableFunctionMalformedException();
		}
		// Debug.println("EXITING NOT FACTOR");
		
		if (isNot) {
			return new CompilerNotValue(factor);
		} else {
			return factor;
		}
	}

	/**
	 * b_factor ::= ident "=" ident | "(" b_expression ")"
	 * 
	 */
	private ICompilerBooleanValue bFactor() throws InvalidConditionantException,
								  TableFunctionMalformedException{
		
		String conditionantName = null;
		
		
		
		// first, check if it is an expression with parenhtesis - "(" b_expression ")" case.
		if (look == '(') {
			match('(');
			ICompilerBooleanValue ret = bExpression();
			match(')');
			return ret;
		} 
		
		// this is not a "(" b_expression ")" case; so, check as ident "=" ident.
		
		// Debug.println("Parsing bFactor");
		// SCAN FOR CONDITIONANTS
		scan();
		
		if (token == 'x') {
			conditionantName = this.noCaseChangeValue;
			// consistency check C09: verify whether is conditionant of the node
			if (this.node != null) {
				if (!this.isValidConditionant(this.mebn, this.node, conditionantName )) {
					// Debug.println("->" + getNode());
					throw new InvalidConditionantException();
				}
			}
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		
		// LOOK FOR = OPERATOR
		match('=');
		
		// SCAN FOR CONDITIONANTS' POSSIBLE STATES
		scan();
		
		// Debug.println("SCANED FOR CONDITIONANTS' POSSIBLE STATES");
		
		if (token == 'x') {
			// consistency check C09: verify whether conditionant has valid values
			if (this.node != null) {
				if (!this.isValidConditionantValue(this.mebn,conditionantName,this.noCaseChangeValue)) {
					throw new InvalidConditionantException();
				}
			}
			
		} else {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		
		// if code reached here, the condicionant check is ok

		//	prepare to add current temp table's header's parent (condicionant list)
		ResidentNode resident = this.mebn.getDomainResidentNode(conditionantName);
		// If not found, its an error!		
		if (resident == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		Entity condvalue = null;
		// search for an entity with a name this.noCaseChangeValue
		for (Entity possibleValue : resident.getPossibleValueListIncludingEntityInstances()) {
			if (possibleValue.getName().equalsIgnoreCase(this.value)) {
				condvalue = possibleValue;
				break;
			}
		}
		// If not found, its an error!		
		if (condvalue == null) {
			try{
				expected("Identifier");
			} catch (TableFunctionMalformedException e) {
				throw new InvalidConditionantException(e);
			}
		}
		// Set temp table's header condicionant
		TempTableHeaderParent headerParent = new TempTableHeaderParent(resident, condvalue);
		// TODO optimize above code, because its highly redundant (condvalue should be found anyway on that portion of code)
		
		this.currentHeader.addParent(headerParent);	// store it as a conditionant declared inside a boolean expression
		
		return headerParent;
	}
	
	
	/**
	 *  else_statement ::= statement | if_statement
	 *  @param upperIf: if this if clause is an inner if, the upper if-clause should be referenced
	 * by this parameter. If this is a temporary table, this if-clause is the upper-most if-clause within CPT pseudocode.
	 * If it is null, it will assume tempTable is the upper container
	 */
	private void else_statement(INestedIfElseClauseContainer upperIf) throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException {
		
		// Debug.println("ELSE STATEMENT");
		if ( look == '[' ) {
			// header ::= there are no known parents yet, is ANY and is default.
			this.currentHeader = new TempTableHeaderCell(null,true,true, this.ssbnnode); 
			
			// register it to the upper container (might be another clause or the temporary table)
			if (upperIf == null) {
				// No upper container identified. Let's assume to be the upper-most container (the temporary table)
				this.tempTable.addNestedClause(this.currentHeader);
			} else {
				upperIf.addNestedClause(this.currentHeader);
			}
			
			// if there are nested if/else clauses, their upper container should be the currently evaluated else clause (currentHeader)
			this.statement(this.currentHeader);
		} else {
			// Debug.println("COULD NOT FIND '['");
			// we dont have to create new header here because ifStatement would do so.
			// the if statement without "[" is on the same level of currently evaluated else clause, so, pass upperIf as upper container
			ifStatement(upperIf);
		}
	
	}
	

	/**
	 * statement ::= "[" assignment_or_if "]" 
	 * 
	 * @param upperIf: the upper if-clause or the tempTable (upper-most container).
	 * Since assignment_or_if might start a nested if/else-clause, this parameter
	 * helps us keep track where the new if/else-clause is contained.
	 */
	private void statement(INestedIfElseClauseContainer upperIf) 
							 throws NoDefaultDistributionDeclaredException,
									InvalidConditionantException,
									SomeStateUndeclaredException,
									InvalidProbabilityRangeException,									
									TableFunctionMalformedException{
		// Debug.println("PARSING STATEMENT, VALUE = " + value + ", LOOKAHEAD = " + look);
		if (look == '[') {
							
			// Debug.println("");
			// Debug.print("  ");
			match('[');
			
			// initialize currently evaluated temporary table's collumn
			//this.currentProbCellList = new ArrayList<TempTableProbabilityCell>();
			
			assignmentOrIf(upperIf);
			
			
			match(']');
			// Debug.println("");
			
			
		} else {
			// Debug.println("COULD NOT FIND '['");
			this.expected("[");
		}
	}

	
	/**
	 * assignment_or_if ::= assignment | if_statement
	 * 
	 * @param upperIf: the upper if-clause or the tempTable (upper-most container).
	 * Since assignment_or_if might start a nested if/else-clause, this parameter
	 * helps us keep track where the new if/else-clause is contained.
	 * @throws InvalidProbabilityRangeException
	 * @throws TableFunctionMalformedException
	 * @throws SomeStateUndeclaredException
	 * @thwows NoDefaultDistributionDeclaredException
	 * @throws InvalidConditionantException
	 */
	private void assignmentOrIf(INestedIfElseClauseContainer upperIf) 
			throws InvalidProbabilityRangeException, 
				   TableFunctionMalformedException,
				   SomeStateUndeclaredException,
				   NoDefaultDistributionDeclaredException,	// if-clause would eventually throw this
				   InvalidConditionantException{			// if-clause would eventually throw this
		
		try {
			if (this.tokenLookAhead() == this.kwcode[this.lookup("IF")]) {
				// this is an if-clause
				this.ifStatement(upperIf);
				// an if-clause doesnt have do return something...
			} else {
				// since it is an assignment, we should check probability consistency as well
				
				// Consistency check C09
				// Structures that allow us to Verify if all states has probability declared
				List<Entity> declaredStates = new ArrayList<Entity>();
				List<Entity> possibleStates = null;			
				if (this.node != null) {
					possibleStates = this.node.getPossibleValueListIncludingEntityInstances();
				}
				
				this.assignment(declaredStates, possibleStates);
				
				if (this.node != null) {
					// Consistency check C09
					// Verify if all states has probability declared
					if (!declaredStates.containsAll(possibleStates)) {
						throw new SomeStateUndeclaredException();
					}
				}
				
				// Consistency check C09
				// Verify if sum of all declared states' probability is 1
				
				// runtime probability bound check (on SSBN generation time)
				if (!this.currentHeader.isSumEquals1()) {
					// Debug.println("Testing cell's probability value's sum: " + currentHeader.getProbCellSum());
					if (!Float.isNaN(this.currentHeader.getProbCellSum())) {
						throw new InvalidProbabilityRangeException();
					} else {
						// Debug.println("=>NaN found!!!");
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			/* 
			 * catching ArrayIndexOutOfBoundsException means that the keyword "IF"
			 * was not found the list of keywords. It is an horrible implementation error!!
			 */
			throw new RuntimeException(this.resource.getString("FatalError"),e);
		}
		
		// any other exception should not be treated by this scope (equivalent to "catch(Exception e){throw e}")
		
	}
	
	/**
	 * assignment ::= ident "=" expression [ "," assignment ]*
	 * 
	 * declaredStates, possibleStates are used to verify if every single possible state for
	 * RV has its probability declared.
	 * @param declaredStates: states actually declared within this clause. It is used afterwards in order to check
	 * if all states have probability declared.
	 * @param possibleStates: list of all possible states a node (hosting the pseudocode) may have.
	 * It is compared to declaredStates in order to ensure declardStates is a sublist of possibleStates.
	 * returns the sum of all declared states' probability after this assignment recursion phase
	 * 
	 */
	private IProbabilityValue assignment(List<Entity> declaredStates, List<Entity> possibleStates) 
					throws InvalidProbabilityRangeException, 
						   TableFunctionMalformedException,
						   SomeStateUndeclaredException{
		
		// prepare a representation of a cell inside the temporary table
		TempTableProbabilityCell currentCell = new TempTableProbabilityCell(null, null);
		
		// SCAN FOR IDENTIFIER
		scan();
		if (token == 'x') {
			if (this.node != null) {
				// Consistency check C09
				// Remember declared states, so we can check later if all states was declared
				Entity possibleValue = null;
				try {
					possibleValue = possibleStates.get(this.node.getPossibleValueIndex(this.noCaseChangeValue));
				} catch (Exception e) {
					//throw new TableFunctionMalformedException(e.getMessage());
					throw new TableFunctionMalformedException(e);
				}
				if (possibleValue == null) {
					throw new TableFunctionMalformedException();
				}
				declaredStates.add(possibleValue);
				currentCell.setPossibleValue(possibleValue);
			}
			
		} else {
			expected("Identifier");
		}

		// LOOK FOR = OPERATOR
		match('=');

		// consistency check C09
		// ret verifies the sum of all declared states' probability (must be 1)
		// boolean hasUnknownValue shows if some ret was negative.
		IProbabilityValue ret = expression();		
		float retValue = ret.getProbability();
		boolean hasUnknownValue = Float.isNaN(retValue);
		
		// add cell to header
		currentCell.setProbability(ret);
		if (currentCell.getPossibleValue() != null) {
			this.currentHeader.addCell(currentCell);
		}
		// Debug.println("Adding cell: " + currentCell.getPossibleValue().getName() + " = " + ret.toString());
		
		// consistency check C09
		// a single state shall never have prob range out from [0,1]
		if ( (retValue < 0.0) || (1.0 < retValue)) {
			throw new InvalidProbabilityRangeException();
		}
		
		// LOOK FOR , (OPTIONAL)
		if (look == ',') {
			match(',');
			IProbabilityValue temp = assignment(declaredStates, possibleStates);
			float tempValue = temp.getProbability();
			hasUnknownValue = hasUnknownValue || (Float.isNaN(tempValue));
			if (hasUnknownValue) {
				retValue = Float.NaN;
			} else {
				retValue += temp.getProbability();
			}
		}
		
		// Debug.println("Returned expression value = " + retValue);
		if (retValue < 0) {
			throw new InvalidProbabilityRangeException();
		}
		return new SimpleProbabilityValue(retValue);
	}

	/**
	 * expression ::= term [ addop term ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue expression() throws TableFunctionMalformedException,
												  InvalidProbabilityRangeException,
												  SomeStateUndeclaredException{
		
		// temp table already created by upper caller
		
		IProbabilityValue temp1 = term();
		IProbabilityValue temp2 = null;
		
		Float temp1Value = null;
		Float temp2Value = null;
		// LOOK FOR +/- (OPTIONAL)
		switch (look) {
		case '+':
			match('+');
			temp2 = term();
			temp1Value = temp1.getProbability();
			temp2Value = temp2.getProbability();
			if (!Float.isNaN(temp1Value)) {
				if (!Float.isNaN(temp2Value)) {
					
					// pode the subtree if it is known value...
					temp1 = new AddOperationProbabilityValue(
							temp1.isFixedValue?(new SimpleProbabilityValue(temp1Value)):temp1 ,
							temp2.isFixedValue?(new SimpleProbabilityValue(temp2Value)):temp2);
				}				
			}		
			break;
		case '-':
			match('-');
			temp2 = term();
			temp1Value = temp1.getProbability();
			temp2Value = temp2.getProbability();
			if (!Float.isNaN(temp1Value)){
				if (!Float.isNaN(temp2Value)) {
					// pode the subtree if it is known value...
					temp1 = new SubtractOperationProbabilityValue(
							temp1.isFixedValue?(new SimpleProbabilityValue(temp1Value)):temp1 ,
							temp2.isFixedValue?(new SimpleProbabilityValue(temp2Value)):temp2);
				}
			}			
			break;
		}
		
		// Debug.println("Expression returned " + temp1.getProbability());
		return temp1;
	}

	/**
	 * term ::= signed_factor [ mulop signed_factor ]*
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue term() throws TableFunctionMalformedException,
											InvalidProbabilityRangeException,
											SomeStateUndeclaredException{
		IProbabilityValue temp1 = signedFactor();
		IProbabilityValue temp2 = null;
		
		Float temp1Value = null;
		Float temp2Value = null;
		// LOOK FOR *// (OPTIONAL)
		switch (look) {
		case '*':
			match('*');
			temp2 = this.signedFactor();
			temp1Value = temp1.getProbability();
			temp2Value = temp2.getProbability();
			if (!Float.isNaN(temp1Value)) {
				if(!Float.isNaN(temp2Value)) {
					// pode subtree if it is known value
					return new MultiplyOperationProbabilityValue(
							temp1.isFixedValue?(new SimpleProbabilityValue(temp1Value)):temp1 ,
							temp2.isFixedValue?(new SimpleProbabilityValue(temp2Value)):temp2);
				}
			}
			break;
		case '/':
			match('/');
			temp2 = this.signedFactor();
			temp1Value = temp1.getProbability();
			temp2Value = temp2.getProbability();
			if (!Float.isNaN(temp1Value)) {
				if (!Float.isNaN(temp2Value)) {
					// pode subtree if it is known value
					return new DivideOperationProbabilityValue(
							temp1.isFixedValue?(new SimpleProbabilityValue(temp1Value)):temp1 ,
							temp2.isFixedValue?(new SimpleProbabilityValue(temp2Value)):temp2);
				}
			}
			break;
		//default:
			//return temp1;
		}
		// Debug.println("Term is not matching to an * or / nor signed factor !!");
		//return new SimpleProbabilityValue(Float.NaN);
		return temp1;
	}

	/**
	 * signed_factor ::= [ addop ] factor
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue signedFactor() throws TableFunctionMalformedException,
													InvalidProbabilityRangeException,
													SomeStateUndeclaredException{

		boolean isMinus = false;
		
		// CHECK TO SEE IF THERE IS A -/+ UNARY SIGN
		// boolean negative;
		// negative = (look == '-');
		if (isAddOp(look)) {
			// Debug.print("" + look);
			
			if (this.isMinus(look)) {
				isMinus = true;
			}
			
			nextChar();
			skipWhite();
		}
		// Debug.println("Signed factor returning minus = " + isMinus);
		if (isMinus) {
			return new NegativeOperationProbabilityValue(factor());
		} else {
			return factor();
		}
	}

	/**
	 * factor ::= number | function | ( expression )
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue factor() throws TableFunctionMalformedException,
											  InvalidProbabilityRangeException,
											  SomeStateUndeclaredException{
		IProbabilityValue ret = null;
		if (look == '(') {
			match('(');
			ret = expression();
			match(')');
		} else if (isAlpha(look)) {
			ret = function();
		} else {
			ret =  getNum();
		}
		// Debug.println("Factor returning " + ret.toString());
		return ret;
	}

	/**
	 * ident ::= letter [ letter | digit ]*
	 * 
	 */
	private void getName()throws TableFunctionMalformedException {
		//// Debug.println("RESETING VALUE FROM " + value);
		value = "";
		//// Debug.println("LOOKAHEAD IS " + look);
		if (!isAlpha(look))
			expected("Name");
		while (isAlphaNumeric(look)) {
			value += look;
			nextChar();
		}
		
		noCaseChangeValue = value;	// this is "value" without case change
		value = value.toUpperCase();

		token = 'x';
		//skipWhite();

		//// Debug.print(value + " ");
		
		
		
	}
	
	/**
	 * possibleVal ::= ident
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue possibleVal()throws TableFunctionMalformedException,
												  SomeStateUndeclaredException {

		this.getName();
		
		// Use a list to store already known states or identifiers to evaluate already known values... 
		IProbabilityValue ret = new SimpleProbabilityValue(Float.NaN);
		if (this.currentHeader != null) {
			for (TempTableProbabilityCell cell : this.currentHeader.getCellList()) {
				 if (cell.getPossibleValue().getName().equalsIgnoreCase(value) ) {
					 // Debug.println("\n => Variable value found: " + cell.getPossibleValue().getName());
					 return cell.getProbability();
					 
				 }
			}
		} else {
			// if null, it means it was called before an assignment
			throw new SomeStateUndeclaredException();
		}
		


		// Debug.println("An undeclared possible value or a \"varsetname\" was used : " + value);
		return ret;
	}

	/**
	 * number ::= [digit]+
	 * returns the probability declared with this grammar category.
	 * 	NAN if undefined or unknown.
	 */
	private IProbabilityValue getNum() throws TableFunctionMalformedException {
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

		// Debug.println("GetNum returned " + Float.parseFloat(value));
		return new SimpleProbabilityValue(Float.parseFloat(value));
	}
	
	/**
	 * Scan the text and skippes white space
	 * @throws TableFunctionMalformedException
	 */
	private void scan() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		skipWhite();
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		// Debug.println("\n!!!Value = "  + value);
	}
	
	/**
	 * Scan the text but doesnt skip white spaces
	 * @throws TableFunctionMalformedException
	 */
	private void scanNoSkip() throws TableFunctionMalformedException {
			
		int kw;
		
		getName();
		
		kw = lookup(value);
		if (kw == -1)
			token = 'x';
		else
			token = kwcode[kw];
		// Debug.println("\n!!!Value = "  + value);
	}
	
	/**
	 * Performs a scan and revert attributes (value, token, look, index, noCaseChangeValue)
	 * @return: token of the scanned element
	 */
	private char tokenLookAhead () throws TableFunctionMalformedException {
		int originalIndex = this.index;
		char originalLook = this.look;
		String originalValue = new String(this.value);
		String originalNoCaseChangeValue = new String(this.noCaseChangeValue);
		char originalToken = this.token;
		char returnedToken = ' ';
		
		this.scan();
		
		returnedToken = this.token;	// this value will be the return value
		
		// revert global attributes
		this.index = originalIndex;
		this.look = originalLook;
		this.value = originalValue;
		this.noCaseChangeValue = originalNoCaseChangeValue;
		this.token = originalToken;
		
		return returnedToken;
		
	}

	/**
	 * Searches the kwlist for a String. Returns its index when found.
	 * @param s: a keyword to look for
	 * @return kwlist's index where the keyword resides. -1 if not found.
	 */
	private int lookup(String s) {
		int i;

		for (i = 0; i < kwlist.length; i++) {
			if (kwlist[i].equalsIgnoreCase(s))
				return i;
		}

		return -1;
	}

	/**
	 *  reads the next input character (updates lookup character)
	 */
	private void nextChar() {
		
//		if (index < text.length) {
//			look = text[index++];
//		} else {
//			look = ' ';
//		}
		look = (index < text.length)?(text[index++]):(' ');
	}

	private void skipWhite() {
		while ((index < text.length) && (look == ' '))
			nextChar();
	}

	/* Sends an alert telling that we expected some particular input */
	private void expected(String error) throws TableFunctionMalformedException {
		System.err.println("Error: " + error + " expected!");
		throw new TableFunctionMalformedException();
	}

	/* Verifies if an input is an expected one */
	private void match(char c) throws TableFunctionMalformedException {
		
		//Debug.println("Matching " + c + " ");
		if (look != c)
			expected("" + c);
		nextChar();
		skipWhite();
	}

	private void matchString(String s) throws TableFunctionMalformedException {
		if (!value.equalsIgnoreCase(s))
			expected(s);
	}

	private boolean isAlpha(final char c) {
		if ((c >= 'a') && (c <= 'z'))
			return true; // lowercase
		if ((c >= 'A') && (c <= 'Z'))
			return true; // uppercase
		if (c == '_') {
			return true; // underscore
		}
		
		return false;
	}

	private boolean isAlphaNumeric(final char c) {
		if (isAlpha(c)) {
			return true; // uppercase
		} else	if (isNumeric(c)) {
			return true; // numeric
		}
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
	public ResidentNode getNode() {
		if (this.ssbnnode != null) {
			this.node = this.ssbnnode.getResident();
		}
		return node;
	}

	/**
	 * Setting this to null or not setting at all makes
	 * Compiler not to check structural consistency (like node states or conditionants
	 * are real)
	 * @param node The node to set.
	 */
	public void setNode(ResidentNode node) {
		this.node = node;
		if (this.node != null) {
			this.mebn = node.getMFrag().getMultiEntityBayesianNetwork();
		}
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node	
	 * @return if node with name == nodeName is a valid conditionant.
	 */
	private boolean isValidConditionant(MultiEntityBayesianNetwork mebn, ResidentNode node, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName);
		
		
		
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (node.getParents().contains(conditionant)) {
				return true;
			} else {	// parent may be an input node
				ArrayList<Node> parents = node.getParents();
				for (int i = 0; i < parents.size(); i++) {
					if (parents.get(i) instanceof InputNode) {
						if ( ((InputNode)(parents.get(i))).getInputInstanceOf().equals(conditionant) ) {
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
			// Debug.println("No conditionant of name " + conditionantName);
			return false;
		}
		//// Debug.println("Conditionant node found: " + conditionant.getName());
		if ( conditionant instanceof ResidentNode) {
			// Debug.println("IS MULTIENTITYNODE");
			return ((ResidentNode)conditionant).getPossibleValueByName(conditionantValue) != null;
		} else {
			// Debug.println("Conditionant is not a resident node");
		}
			
		return false;
	}
	
	
	/**
	 *  function ::= ident 
	 *   	| "CARDINALITY" "(" ident ")"
	 *    	| "MIN" "(" expression ";" expression ")"
	 *     	| "MAX" "(" expression ";" expression ")"
	 * @return numeric value expected for the function
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue function()throws TableFunctionMalformedException,
											   InvalidProbabilityRangeException,
											   SomeStateUndeclaredException{
		IProbabilityValue ret = this.possibleVal();
		skipWhite();
		if (this.look == '(') {
			if (this.value.equalsIgnoreCase("CARDINALITY")) {
				return cardinality();
			} else if (this.value.equalsIgnoreCase("MIN") ) {
				return min();
			} else if (this.value.equalsIgnoreCase("MAX") ) {
				return max();
			} else {
				// Debug.println("UNKNOWN FUNCTION FOUND: " + this.value);
				throw new TableFunctionMalformedException(this.resource.getString("UnexpectedTokenFound")
						+ ": " + value);
			}
		}
		
		// Debug.println("Function returning " + ret);
		return ret;
	}
	
	
	/**
	 * Computes cardinality funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue cardinality()throws TableFunctionMalformedException {
		IProbabilityValue ret = null;
		match('(');
		
		String var = this.varsetname();
		skipWhite();
		// Debug.println("CARDINALITY'S ARGUMENT IS " + var);
		// TODO test if ret has returned NaN (guarantees "value" is a varsetname)?
		ret = new CardinalityProbabilityValue(this.currentHeader, var);
		match(')');
		return ret;
		
	}
	
	/**
	 * Computes min funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue min()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MIN FUNCTION");
		
		IProbabilityValue ret1 = null;
		IProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		match(';');
		ret2 = this.expression();
		match(')');
		/*
		// old code: tests which ret1/ret2 to return and test consistency. ComparisionProbabilityValue replaces it.
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2<ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		*/
		return new ComparisionProbabilityValue(ret1,ret2,false);
		
	}
	
	/**
	 * Computes MAX funcion's arguments and values
	 * @return
	 * @throws TableFunctionMalformedException
	 */
	private IProbabilityValue max()throws TableFunctionMalformedException,
										  InvalidProbabilityRangeException,
										  SomeStateUndeclaredException{
		// Debug.println("ANALISING MAX FUNCTION");
		
		IProbabilityValue ret1 = null;
		IProbabilityValue ret2 = null;
		match('(');
		ret1 = this.expression();
		match(';');
		ret2 = this.expression();
		match(')');
		/*
		// old code: tests which ret1/ret2 to return and test consistency. ComparisionProbabilityValue replaces it.
		if (!Float.isNaN(ret1)) {
			if (!Float.isNaN(ret2)) {
				ret1 = ((ret2>ret1)?ret2:ret1);
			}
		} else if (!Float.isNaN(ret2)) {
			return ret2;
		}
		*/
		return new ComparisionProbabilityValue(ret1,ret2,true);
		
	}

	/**
	 * Use this method to determine where the error has occurred
	 * @return Returns the last read index.
	 */
	public int getIndex() {
		try {
			return (int)(index * ((float)this.originalTextLength / (float)this.text.length));
		} catch (Exception e) {
			// maybe there were a division by zero
			return index;
		}
	}

	/**
	 * @return the PotentialTable
	 */
	protected IProbabilityFunction getPotentialTable() {
		return cpt;
	}

	/**
	 * @param cpt the PotentialTable to set
	 */
	public void setPotentialTable(PotentialTable cpt) {
		this.cpt = cpt;
	}
	



	/**
	 * @return the ssbnnode
	 */
	public SSBNNode getSSBNNode() {
		return ssbnnode;
	}

	/**
	 * @param ssbnnode the ssbnnode to set
	 */
	public void setSSBNNode(SSBNNode ssbnnode) {
		this.ssbnnode = ssbnnode;
		if (this.ssbnnode != null) {
			this.setNode(this.ssbnnode.getResident());
		}
	}
	
	
	 
	
	// Some inner classes that might be useful for temporaly table creation (organize the table parsed from pseudocode)
	
	/**
	 * Container of a if-else-clause
	 */
	private interface INestedIfElseClauseContainer {
		/**
		 * registers an if-clause (or else-clause) as an inner clause of this clause
		 * @param nestedClause
		 * @throws NullPointerException: if nestedClause is null
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause);
		
		/**
		 * Looks for the first if/else clause (performing deep scan - searches every nested if/else clause
		 * as well) which the bExpression returns true.
		 * 
		 * @param valuesOnCPTColumn: a map which the key is a name of a parent node and
		 * the value is its current possible values to be evaluated.
		 * For example, if we want to evalueate an expression when for a node "Node(!ST0)" we
		 * have parents Parent1(!ST0,!Z0), Parent1(!ST0,!Z1), Parent2(!ST0,!T0), and Parent2(!ST0,!T0)
		 * with values True, False, Alpha, Beta  respectively, the map should be:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * 
		 * @return: the first if/else clause which returned true.
		 */
		public TempTableHeaderCell getFirstTrueClause(Map<String, List<EntityAndArguments>> valuesOnCPTColumn) ;
		
		
		/**
		 * Tests if this container has no nested clauses.
		 * @return true if this if/else clause container has 0 nested elements
		 */
		public boolean isEmptyNestedClauses ();
		
		/**
		 * @return the clauses
		 */
		public List<TempTableHeaderCell> getNestedClauses();
		
		/**
		 * 
		 * @return the clause this object is contained within
		 */
		public INestedIfElseClauseContainer getUpperClause();
		
		
		/**
		 * sets the clause this object is contained within
		 * @param upper
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper);
		
		
		/**
		 * Initializes the "isKnownValue" attributes of TempTableHeaderParent objects
		 * by recursively calling this method for all nested causes.
		 * @param ssbnnode
		 * @see TempTableHeaderParent
		 * @throws NullPointerException if ssbnnode is null
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode);
	}
	
	
	private class TempTable implements INestedIfElseClauseContainer{

		private List<TempTableHeaderCell> clauses = null;
		
		/**
		 * Represents the temporary CPT table (a list of if-else clauses)
		 */
		public TempTable() {
			super();
			this.clauses = new ArrayList<TempTableHeaderCell>();
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#addNestedClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause) {
			this.clauses.add(nestedClause);
			nestedClause.setUpperClause(this);
		}
		
		
		
		/**
		 * @return the clauses
		 */
		public List<TempTableHeaderCell> getNestedClauses() {
			return clauses;
		}

		/**
		 * Tests if this table is empty
		 * @return true if this table has 0 elements
		 */
		public boolean isEmptyNestedClauses () {
			// clauses are asserted not to be null (because we do not have a setter for clauses)
			return (this.clauses.size() == 0);
		}
		
		/**
		 * Runs recursively until we get the default distribution of the nested if/else clause's tree
		 * @return: the default clause
		 */
		public TempTableHeaderCell getDefaultClause() {
			// clauses are asserted not to be null (because we do not have a setter for clauses)
			
			// assert clauses are not empty
			if (this.isEmptyNestedClauses()){
				return null;
			}				
			
			return this.getNestedClauses().get(this.getNestedClauses().size() - 1).getDefaultClause();
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getFirstTrueClause(java.util.Map)
		 */
		public TempTableHeaderCell getFirstTrueClause(
				Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			// initial assertion
			if (this.isEmptyNestedClauses()) {
				// actually, this is an error, since at least a default distribution should be present
				return null;
			}
			
			for (TempTableHeaderCell clause : this.getNestedClauses()) {
				if (clause.evaluateBooleanExpressionTree(valuesOnCPTColumn)) {
					return clause.getFirstTrueClause(valuesOnCPTColumn);
				}
			}
			
			// no clause was found... This is an error, since at least the default distro should return true...
			return null;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUpperClause()
		 */
		public INestedIfElseClauseContainer getUpperClause() {
			// No  upper clause should exist to temporary table (the temporary table should be the upper-most)
			return null;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#setUpperClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper) {
			// No  upper clause should exist to temporary table (the temporary table should be the upper-most)
			// do nothing
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#cleanUpKnownValues(unbbayes.prs.mebn.ssbn.SSBNNode)
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode) {
			// no try-catch for NullPointerException should be necessary, since allways this.getNestedClauses != null
			for (TempTableHeaderCell clause : this.getNestedClauses()) {
				clause.cleanUpKnownValues(ssbnnode);
			}
		}
		
		
		
		
	}
	
	private class TempTableHeaderCell implements INestedIfElseClauseContainer {
		private ICompilerBooleanValue booleanExpressionTree = null; // core of the if statement
		private List<TempTableHeaderParent> parents = null;	// this is also the leaf of boolean expression tree

		private String varsetname = "";
		
		private boolean isAny = true;
		private boolean isDefault = false;
		
		private List<TempTableProbabilityCell> cellList = null;
		
		private int validParentSetCount = 0;
		
		float leastCellValue = Float.NaN;
		
		SSBNNode currentSSBNNode = null;
		
		private List<TempTableHeaderCell> nestedIfs = null;
		
		private INestedIfElseClauseContainer upperContainer = null;
		
		/**
		 * Represents an entry for temporary table header (parents and their expected single values
		 * at that table entry/collumn).
		 * It can directly represent an if-clause (varsetname, any|all, bExpression, values, and nested ifs)
		 * Since an if-clause may be nested, it has a list of nested if-clauses
		 * @param parents: entries of an if-clause (list of (parent = value) pairs)
		 * @param isAny
		 * @param isDefault
		 */
		TempTableHeaderCell (List<TempTableHeaderParent> parents , boolean isAny, boolean isDefault, SSBNNode currentSSBNNode) {
			this.parents = parents;
			this.isAny = isAny;
			this.isDefault = isDefault;
			this.cellList = new ArrayList<TempTableProbabilityCell>();
			this.validParentSetCount = 0;
			this.currentSSBNNode = currentSSBNNode;
			this.nestedIfs = new ArrayList<TempTableHeaderCell>();
		}
		/**
		 * It Gets entries of boolean expression (a pair of Node and its expected value declared
		 * within a boolean expression inside a if clause)
		 * @return List of expected parents within if-clause
		 */		
		public List<TempTableHeaderParent> getParents() {
			return parents;
		}


		/**
		 * counts how many parents set were returning true at that sentence (column) of boolean
		 * expression.
		 * @return the validParentSetCount
		 */
		public int getValidParentSetCount() {
			return validParentSetCount;
		}
		/**
		 * @param validParentSetCount the validParentSetCount to set
		 */
		public void setValidParentSetCount(int validParentSetCount) {
			this.validParentSetCount = validParentSetCount;
		}
		
		public void increaseValidParentSetCount() {
			this.validParentSetCount++;
		}
		
		public void setParents(List<TempTableHeaderParent> parents) {
			this.parents = parents;
		}
		
		public void addParent(TempTableHeaderParent parent) {
			this.parents.add(parent);
		}
		
		/**
		 * @return true if this header is declared as "any", false if "all".
		 */
		public boolean isAny() {
			return isAny;
		}
		/**
		 * @param isAny : true if this header is declared as "any", false if "all".
		 */
		public void setAny(boolean isAny) {
			this.isAny = isAny;
		}
		/**
		 * @return true if this header is declaring a default distro, false otherwise
		 */
		public boolean isDefault() {
			return isDefault;
		}
		/**
		 * @param isDefault : if this header is declaring a default distro, false otherwise
		 */
		public void setDefault(boolean isDefault) {
			this.isDefault = isDefault;
		}
		
		/**
		 * Checks if the argument is a correct parent set names (if parents of this node are
		 * declared within varsetname). E.g: if varsetname == "st.z", tests if parents has those
		 * OVs as their arguments.
		 * @param varsetname
		 * @return
		 */
		public boolean isParentSetName(String varsetname) {
			if (this.currentSSBNNode.getParentSetByStrongOVWithWeakOVCheck(varsetname.split("\\.")).size() > 0) {
				return true;
			} else {
				return false;
			}
		}
		/**
		 * @return the varsetname
		 */
		public String getVarsetname() {
			return varsetname;
		}
		/**
		 * @param varsetname the varsetname to set
		 */
		public void setVarsetname(String varsetname) {
			this.varsetname = varsetname;
		}
		/**
		 * @return the cellList: possible values and its probability
		 */
		protected List<TempTableProbabilityCell> getCellList() {
			return cellList;
		}
		/**
		 * @param cellList the cellList to set
		 */
		protected void setCellList(List<TempTableProbabilityCell> cellList) {
			this.cellList = cellList;
		}
		
		
		/**
		 * @return the nestedIfs
		 */
		public List<TempTableHeaderCell> getNestedClauses() {
			return nestedIfs;
		}
		/**
		 * @param nestedIfs the nestedIfs to set
		 */
		public void setNestedIfs(List<TempTableHeaderCell> nestedIfs) {
			this.nestedIfs = nestedIfs;
		}
		public void addCell(Entity possibleValue , IProbabilityValue probability) {
			this.addCell(new TempTableProbabilityCell(possibleValue, probability));
		}
		
		public void addCell(TempTableProbabilityCell cell) {
			if (cell == null) {
				return;
			}
			float value = Float.NaN;
			try {
				value = cell.getProbabilityValue();
			} catch (InvalidProbabilityRangeException e) {
				// do nothing at now - it will be detected soon
			}
			// update the smallest declared probability value
			if (value > 0.0f) {
				if (Float.isNaN(this.leastCellValue) || (value < this.leastCellValue)) {
					this.leastCellValue = value;
				}
			}
			
			this.cellList.add(cell);
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return
		 */
		public boolean isSumEquals1() throws InvalidProbabilityRangeException {
			float value = this.getProbCellSum();
			// (this.leastCellValue/2) is the error margin
			if ( (value >= 1f - (this.leastCellValue/2f)) && (value <= 1f + (this.leastCellValue/2f)) ) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * check if sum of all probability assignment is 1
		 * @return the sum of all cell's probability
		 */
		public float getProbCellSum() throws InvalidProbabilityRangeException {
			float sum = Float.NaN;
			if (this.cellList == null) {
				return sum;
			}
			if (this.cellList.size() <= 0) {
				return sum;
			}
			sum = 0.0f;
			for (TempTableProbabilityCell cell : this.cellList) {
				sum = (float)sum + (float)cell.getProbabilityValue();
			}
			return sum;
		}
		/**
		 * @return the booleanExpressionTree
		 */
		public ICompilerBooleanValue getBooleanExpressionTree() {
			return booleanExpressionTree;
		}
		/**
		 * @param booleanExpressionTree the booleanExpressionTree to set
		 */
		public void setBooleanExpressionTree(ICompilerBooleanValue booleanExpressionTree) {
			this.booleanExpressionTree = booleanExpressionTree;
		}


		
		/**
		 * This method evaluates a boolean expression (ICompilerBooleanValue - in tree format).
		 * The leaf-values are evaluated using the param. 
		 * Note that this method can used to evaluate a collum of ProbabilisticTable
		 * (consider evaluating each given collumn of CPT is like evaluating every parents as findings
		 * at a given moment).
		 * A "default" header will allways return true
		 * @param valuesOnCPTColumn: a map which the key is a name of a parent node and
		 * the value is its current possible values to be evaluated.
		 * For example, if we want to evalueate an expression when for a node "Node(!ST0)" we
		 * have parents Parent1(!ST0,!Z0), Parent1(!ST0,!Z1), Parent2(!ST0,!T0), and Parent2(!ST0,!T0)
		 * with values True, False, Alpha, Beta  respectively, the map should be:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * @return: if the map is as follows:
		 * 		entry0, (key:"Parent1", values: {True, False});
		 * 		entry1, (key:"Parent2", values: {Alpha, Beta});
		 * Then, the result would be:
		 * - if this object was once declared as "ANY" (isAny() == true):
		 * 			returns: evaluation(True,Alpha) || evaluation(True,Beta) || evaluation(False,Alpha) || evaluation(False,Beta)
		 * - if declared as "ALL" (isAny() == false):
		 * 			returns: evaluation(True,Alpha) && evaluation(True,Beta) && evaluation(False,Alpha) && evaluation(False,Beta)
		 */
		public boolean evaluateBooleanExpressionTree(Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			// initial test
			if (this.isDefault()) {
				return true;
			}
			

			// reset cardinality counter
			this.setValidParentSetCount(0);
			
			List<TempTableHeaderParent> parentsList = this.getParents();
			
			//	return is initialized with a boolean neutral value (on OR/ANY, its "false"; on AND/ALL, its "true")
			boolean ret = !this.isAny();	// this method will return this value
			
			// start evaluation. We should run through leafs again... TODO: optimize?
			
			// "pointer" 
			TempTableHeaderParent pointer = null;
			
			// prepare leafs
			
			// run inside parent list (they are the declared condicionants within boolean expression),
			// which also are the leafs of that expression!
			for (TempTableHeaderParent leaf : parentsList) {
				if (!leaf.isKnownValue()) {
					// if leaf is not set to be a constant value, then we should set it to 
					// evaluate a combination of entities
					leaf.setEvaluationList(valuesOnCPTColumn.get(leaf.getParent().getName()));
				}
			}
			
						
			
			//	evaluate (True,Alpha), (False,Alpha), (True,Beta), (False,Beta)...
			boolean hasMoreCombination = true;
			// expressionWasEvaluated checks if boolean header was once evaluated. If not, all condicionants were invalid (in that case, return false)
			boolean expressionWasEvaluated = false;
			while (hasMoreCombination) {
				// TODO the test below might be dangerous in multithread application...?
				if (isSameOVsameEntity()) { // only evaluates same entities for same OVs
					if (this.isAny()) {	// if ANY, then OR 
						boolean evaluation = this.getBooleanExpressionTree().evaluate();
						ret = ret || evaluation;
						if (evaluation) {
							this.increaseValidParentSetCount();
							// we cant return immediately because we should count "cardinality"
						}
					} else {	// if ALL, then AND
						ret = ret && this.getBooleanExpressionTree().evaluate();
						if (ret == false) {
							return false;
						}
						this.increaseValidParentSetCount();
					}
					expressionWasEvaluated = true; // there was a valid header, so, the return value is valid
				}
				
				// update leaf's evaluation variables
				pointer = parentsList.get(0);
				if (pointer.hasNextEvaluation()) {
					pointer.getNextEvaluation();	// now the leaf will evalueate the next element of EvaluationList
				} else {
					int lastPos = 0;
					// if reached the end of this list, reset it and step foward the next list, and so on
					while ( !pointer.hasNextEvaluation() && (lastPos < parentsList.size())) {
						pointer.resetEvaluationList();
						if (lastPos == parentsList.size() - 1) {
							// if we just changed all the values through first to last, then no more changes are available
							hasMoreCombination = false;
							break;
						}
						lastPos++;	
						pointer = parentsList.get(lastPos);
						
					}
					if (hasMoreCombination) {
						if (parentsList.get(lastPos).hasNextEvaluation()) {
							parentsList.get(lastPos).getNextEvaluation(); //step to next evaluation
						}
					}
				}
			}	
			
			if (expressionWasEvaluated) {
				// since the expression was actually evaluated, ret would be the result of evaluation.
				return ret;
			}
			
			// if the boolean expression was never evaluated, ret is storing a default boolean neutral value.
			// We do not want that (since a neutral in "Any" is false and in "All" is true). If no expression was
			// actually evaluated, it means that the whole expression was invalid (it happens in meantimes like
			// when the "strong OV set" is not matching any parents) and a invalid expression should return false!
//			Debug.println("!!!No valid expression was evaluated by the compiler!!!");
//			Debug.println("   It happens when the parents expected by the pseudocode was never linked by the SSBN Algorithm");
//			Debug.println("   or all connected parents was not expected by the pseudocode (invalidated by the boolean expression)");
			return false;
		}
		
		/**
		 * tests if when an argument of a leaf is the same OV, then it should have
		 * the same value at a given moment.
		 * Tests also between this ssbnnode and the leaves
		 * @return
		 */
		private boolean isSameOVsameEntity() {
			List<TempTableHeaderParent> leaves = this.getParents(); // leaves of boolean expression evaluation tree
			
			for (TempTableHeaderParent leaf : leaves) {
				if (leaf.isKnownValue()) {
					continue;
				}
				List<OVInstance> args = leaf.getCurrentEntityAndArguments().arguments;
//				asdf
				
				// TODO stop obtaining args from actual SSBNNodes and start analyzing input nodes
				
				// first, test if leaf has same arguments as its ssbnnode (if ssbnnode has same arguments as parents)
				for (OVInstance argParent : args) {
					// if it has same OV as ssbnnode, then should be the same entity
					
					for (OVInstance argChild : this.currentSSBNNode.getArguments()) {
						if (argChild.getOv().getName().equalsIgnoreCase(argParent.getOv().getName())) {
							if (!argChild.getEntity().getInstanceName().equalsIgnoreCase(argParent.getEntity().getInstanceName())) {
								return false;
							}
						}
					}
				}
				for (int i = leaves.indexOf(leaf) + 1; i < leaves.size(); i++) {
					// try all other leaves
					for (OVInstance argleaf : args) {
						if (leaves.get(i).isKnownValue()) {
							// if current leaf has a known value (i.e. it is allways evaluating false), then
							// it is not necessary to test OVInstance's name-value consistency
							// (we don't have to check if OVs with same name has same value, since
							// at evaluation time their values are not going to be used at all)
							continue;
						}
						for (OVInstance argothers : leaves.get(i).getCurrentEntityAndArguments().arguments) {
							if(argleaf.getOv().getName().equalsIgnoreCase(argothers.getOv().getName())) {
								if (!argleaf.getEntity().getInstanceName().equalsIgnoreCase(argothers.getEntity().getInstanceName()) ) {
									// if they are the same OV but different instances of Entities... then false
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}
		
		/**
		 * When a boolean expression refeers a node which doesn't have an argument declared inside
		 * varsetname (is not part of a "similar" set of parent called inside the pseudocode),
		 * it is useless to evaluate it (because it would be allways false at our implementation). 
		 * This method detects them and sets them to false before starting definitive CPT generation.
		 * @param baseSSBNNode: a SSBNNode which contains this CPT. This method doesn't check
		 * if this SSBNNode is really the expected one. This argument is used by this method
		 * in order to obtain the "similar" parent set at a given moment.
		 */
		private void cleanUpByVarSetName(SSBNNode baseSSBNNode) {
			
			// no cleanup is necessary when this is a default distro - no boolean evaluation is present
			if (this.isDefault()) {
				return;
			}
			
			// extracts parents' similar sets by strong OV names
			Collection<SSBNNode> parents =  baseSSBNNode.getParentSetByStrongOVWithWeakOVCheck(
					this.getVarsetname().split("\\" + baseSSBNNode.getStrongOVSeparator()));
			
			boolean found = false;
			
			// extract condicionants declared within the expression
			for (TempTableHeaderParent headParent : this.getParents()) {
				 found = false;
				 // look for headParent inside parents (set of similar parents)
				 for (SSBNNode node : parents) {
					if (node.getResident().equals(headParent.getParent())) {
						// if node names are the same, then we found it
						found = true;
						break;
					}
				 }
			     if (!found) {
			    	 // if we did not find a condicionant (inside expression) inside the parent set
			    	 headParent.setKnownValue(true);
			     }
			}
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#addNestedClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void addNestedClause(TempTableHeaderCell nestedClause) {
			this.getNestedClauses().add(nestedClause);
			nestedClause.setUpperClause(this);
		}
		
		/**
		 * Searches recursively for the most default clause of nested ifs.
		 * If this object has no nested children, it tests if
		 * the object itself is a default object (checks isDefault attribute).
		 * It assumes the default child is always the last element of each
		 * nested if-clause (i.e. the last else-clause)
		 * @return: the else clause of all else clauses. Null if no default
		 * clause is found...
		 */
		public TempTableHeaderCell getDefaultClause() {
			if (this.isEmptyNestedClauses()) {
				if (this.isDefault()) {
					return this;
				} else {
					// if this code is reached, this object should have been returned, but its not a default clause...
					return null;
				}
			} else {
				// search for a default clause recursively by looking only the last nested if-clause
				// OBS. the last nested if-clause should be the default else-clause 
				return this.getNestedClauses().get(this.getNestedClauses().size() - 1).getDefaultClause();
			}
			
		}
		
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getFirstTrueClause(java.util.Map)
		 */
		public TempTableHeaderCell getFirstTrueClause(
				Map<String, List<EntityAndArguments>> valuesOnCPTColumn) {
			
			if (this.isEmptyNestedClauses()) {
				// if there is no nested if/else clauses, then this object itself is the first true clause.
				return this;
			}
			
			for (TempTableHeaderCell nested : this.getNestedClauses()) {
				if (nested.evaluateBooleanExpressionTree(valuesOnCPTColumn)) {
					// found something returning true
					// look deep inside recursively.
					return nested.getFirstTrueClause(valuesOnCPTColumn);
				}
			}
			
			// since else-clause is mandatory, the code below should never be reached
			
			return null;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#isEmptyNestedClauses()
		 */
		public boolean isEmptyNestedClauses() {
			if (this.getNestedClauses() == null) {
				return true;
			}
			return this.getNestedClauses().size() == 0;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#getUpperClause()
		 */
		public INestedIfElseClauseContainer getUpperClause() {
			return this.upperContainer;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#setUpperClause(unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell)
		 */
		public void setUpperClause(INestedIfElseClauseContainer upper) {
			this.upperContainer = upper;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer#cleanUpKnownValues(unbbayes.prs.mebn.ssbn.SSBNNode)
		 */
		public void cleanUpKnownValues(SSBNNode ssbnnode) {
			// clean up myself
			this.cleanUpByVarSetName(ssbnnode);
			
			// clean up my nested children recursively
			try {
				for (TempTableHeaderCell clause : this.getNestedClauses()) {
					clause.cleanUpKnownValues(ssbnnode);
				}
			} catch (NullPointerException e) {
				// there were no nested clauses. It is not an error. Do nothing
			}
		}
		
		
		
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		/*
		//@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TempTableHeaderCell) {
				TempTableHeaderCell arg = (TempTableHeaderCell)arg0;
				if (arg.getParents().containsAll(this.getParents()) 
						|| this.getParents().containsAll(arg.getParents()) ) {
					return true;
				}
			}
			return false;
		}
		*/
		
		
		
		
	}
	
	private interface ICompilerBooleanValue {
		/**
		 * Obtains recursively a boolean value
		 * @return true or false
		 */
		public boolean evaluate();
	}
	
	private class CompilerNotValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value = null;
		/**
		 * implements "not" operation on ISSBNBooleanValue
		 * @param value: value to be operated
		 */
		public CompilerNotValue(ICompilerBooleanValue value) {
			this.value = value;
		}
		
		/**
		 * @return the value
		 */
		public ICompilerBooleanValue getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(ICompilerBooleanValue value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ISSBNBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return !this.value.evaluate();
		}
		
	}
	
	private class CompilerOrValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value1 = null;
		private ICompilerBooleanValue value2 = null;
		/**
		 * implements an "OR" boolean operation on ICompilerBooleanValue
		 * @param value1: value to be operated
		 * @param value2: value to be operated
		 */
		public CompilerOrValue(ICompilerBooleanValue value1, ICompilerBooleanValue value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		/**
		 * @return the value1
		 */
		public ICompilerBooleanValue getValue1() {
			return value1;
		}
		/**
		 * @param value1 the value1 to set
		 */
		public void setValue1(ICompilerBooleanValue value1) {
			this.value1 = value1;
		}
		/**
		 * @return the value2
		 */
		public ICompilerBooleanValue getValue2() {
			return value2;
		}
		/**
		 * @param value2 the value2 to set
		 */
		public void setValue2(ICompilerBooleanValue value2) {
			this.value2 = value2;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ICompilerBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return this.value1.evaluate() || this.value2.evaluate();
		}
		
	}
	
	private class CompilerAndValue implements ICompilerBooleanValue{
		private ICompilerBooleanValue value1 = null;
		private ICompilerBooleanValue value2 = null;
		/**
		 * implements an "OR" boolean operation on ICompilerBooleanValue
		 * @param value1: value to be operated
		 * @param value2: value to be operated
		 */
		public CompilerAndValue(ICompilerBooleanValue value1, ICompilerBooleanValue value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		/**
		 * @return the value1
		 */
		public ICompilerBooleanValue getValue1() {
			return value1;
		}
		/**
		 * @param value1 the value1 to set
		 */
		public void setValue1(ICompilerBooleanValue value1) {
			this.value1 = value1;
		}
		/**
		 * @return the value2
		 */
		public ICompilerBooleanValue getValue2() {
			return value2;
		}
		/**
		 * @param value2 the value2 to set
		 */
		public void setValue2(ICompilerBooleanValue value2) {
			this.value2 = value2;
		}
		/* (non-Javadoc)
		 * @see unbbayes.prs.mebn.compiler.Compiler.ICompilerBooleanValue#evaluate()
		 */
		public boolean evaluate() {
			return this.value1.evaluate() && this.value2.evaluate();
		}
		
	}
	
	private class TempTableHeaderParent implements ICompilerBooleanValue {
		private ResidentNode parent = null;
		private Entity value = null;
		
		private List<EntityAndArguments> evaluationList = null;
		
		//private Entity currentEvaluation = null;
		
		private int currentEvaluationIndex = -1;
		
		private boolean isKnownValue = false;	// if this leaf is "absurd", then its value is known = false.
		
		/**
		 * Represents a parent and its expected single value
		 * at that table entry/collumn
		 * @param parent
		 * @param value
		 */
		TempTableHeaderParent (ResidentNode parent , Entity value) {
			this.parent = parent;
			this.value = value;
			this.evaluationList = null;
			//this.evaluationList = new ArrayList<EntityAndArguments>();
			this.currentEvaluationIndex = -1;
		}
		
		TempTableHeaderParent (ResidentNode parent , Entity value, List<EntityAndArguments>evaluationList) {
			this.parent = parent;
			this.value = value;
			this.setEvaluationList(evaluationList);
		}
		
		/**
		 * 
		 * @return which parent this leaf represents
		 */
		public ResidentNode getParent() {
			return parent;
		}
		public void setParent(ResidentNode parent) {
			this.parent = parent;
		}
		public Entity getValue() {
			return value;
		}
		public void setValue(Entity value) {
			this.value = value;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TempTableHeaderParent) {
				TempTableHeaderParent arg = (TempTableHeaderParent)arg0;
				if (this.parent.getName().equalsIgnoreCase(arg.getParent().getName())) {
					if (this.value.getName().equalsIgnoreCase(arg.getValue().getName())) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			//return false;
		}
		/**
		 * evaluates this leaf boolean value and returns it.
		 * @return : evaluated value. If isKnownValue is set to true, 
		 * this will return false everytime
		 * 
		 */
		public boolean evaluate() {
			if (this.isKnownValue()) {
				return false;
			}
			// if entities have the same name, they are equals.
			if (this.getCurrentEvaluation().getName().equalsIgnoreCase(this.getValue().getName())) {
				return true;
			}
			return false;
		}
		
		
		
		/**
		 * Obtains a list of values to be tested on boolean value evaluation
		 * @return the evaluationList
		 */
		public List<EntityAndArguments> getEvaluationList() {
			return evaluationList;
		}
		/**
		 * note: if param is null, it will set the value of this object as known value = "false"
		 * (assume this boolean evaluation as allways false) immediately.
		 * @param evaluationList the evaluationList to set
		 */
		public void setEvaluationList(List<EntityAndArguments> evaluationList) {
			this.evaluationList = evaluationList;
			if (this.evaluationList != null) {
				if (this.evaluationList.size() > 0) {
					this.currentEvaluationIndex = 0;
				}
			} else {
				// isKnownValue = true indicates that expression as value "false" no matter when, where or how it is evaluated
				this.isKnownValue  = true; // we assume if set to null, then false immediately
			}
		}
		
		/**
		 * @return the currentEvaluation (currently evaluated entity value on boolean
		 * expression - comparing a parent with its value)
		 */
		public Entity getCurrentEvaluation() {
			if (this.evaluationList == null) {
				return null;
			}
			return this.evaluationList.get(this.currentEvaluationIndex).entity;
		}
		
		/**
		 * Obtains a compact representation of the current value of
		 * a node and its arguments.
		 * @return
		 */
		public EntityAndArguments getCurrentEntityAndArguments() {
			if (this.evaluationList == null) {
				return null;
			}
			return this.evaluationList.get(this.currentEvaluationIndex);
		}
		
		/**
		 * @param currentEvaluation the currentEvaluation 
		 * (currently evaluated entity value on boolean
		 * expression - comparing a parent with its value) to set
		 */
		public void setCurrentEvaluation(Entity currentEvaluation) {
			this.currentEvaluationIndex = this.evaluationList.indexOf(currentEvaluation);
		}
		
		/**
		 * 
		 * @return true if currently evaluated entity list has next element
		 */
		public boolean hasNextEvaluation() {
			// the below chech is truly dangerous, since if this.evaluationList == null at this moment,
			// it means that something was wrong long way before... It might hide important bugs...
			// but in order to mantain execution security, we are making those checks.
			if (this.evaluationList == null) {
//				Debug.println("!!!==========================================!!!");
//				Debug.println("There was an attempt to evaluate a expression with no possible states.");
//				Debug.println("It may happen sometimes at SSBN generation when pseudocode expects parents");
//				Debug.println("   but no valid parents were linked to its node.");
//				Debug.println("!!!==========================================!!!");
				return false; 
			}
			
			return (this.currentEvaluationIndex + 1 ) < this.evaluationList.size();
		}
		
		/**
		 * add 1 to the index of evaluationList and returns its value
		 * @return
		 */
		public Entity getNextEvaluation() {
			this.currentEvaluationIndex++;
			return this.getCurrentEvaluation();
		}
		
		/**
		 * resets evaluationlist's index to its initial value = 0
		 * or -1 if list is empty
		 *
		 */
		public void resetEvaluationList() {
			if (this.evaluationList == null) {
				this.currentEvaluationIndex = -1;
				return;
			}
			if (this.evaluationList.size() <= 0) {
				this.currentEvaluationIndex = -1;
				return;
			}
			this.currentEvaluationIndex = 0;
		}

		/**
		 * If this is set to true, this object's evaluate() method will always
		 * return a same value.
		 * @return the isKnownValue
		 */
		public boolean isKnownValue() {
			return isKnownValue;
		}

		/**
		 * If this is set to true, this object's evaluate() method will always
		 * return a same value.
		 * @param isKnownValue the isKnownValue to set
		 */
		public void setKnownValue(boolean isKnownValue) {
			this.isKnownValue = isKnownValue;
			if (this.isKnownValue()) {
				// if this leaf should return a constant, then evaluationList is useless...
				this.setEvaluationList(null);
				//this.setEvaluationList(new ArrayList<EntityAndArguments>());
			}
		}
		
	}
	
	private class TempTableProbabilityCell {
		private Entity possibleValue = null;
		private IProbabilityValue probability = null;
		
		/**
		 * Represents a simple entry at a temporaly table representation (the
		 * value and its probability pair)
		 * @param possibleValue
		 * @param probability
		 */
		TempTableProbabilityCell (Entity possibleValue , IProbabilityValue probability) {
			this.possibleValue = possibleValue;
			this.probability = probability;
		}
		public Entity getPossibleValue() {
			return possibleValue;
		}
		public void setPossibleValue(Entity possibleValue) {
			this.possibleValue = possibleValue;
		}
		public float getProbabilityValue() throws InvalidProbabilityRangeException {
			return probability.getProbability();
		}
		public IProbabilityValue getProbability() {
			return probability;
		}
		public void setProbability(IProbabilityValue probability) {
			this.probability = probability;
		}		
	}
	
	private abstract class IProbabilityValue {
		/**
		 * 
		 * @return: a value between [0,1] which represents a probability
		 */
		public abstract float getProbability() throws InvalidProbabilityRangeException;
		
		/**
		 * 
		 * true if the probability is fixed. False if probability is
		 * dynamic (values changes depending on SSBN configuration)
		 */
		public boolean isFixedValue = true;
	}
	
	private class SimpleProbabilityValue extends IProbabilityValue {
		private float value = Float.NaN;
		/**
		 * Represents a simple float value for a probability
		 * @param value
		 */
		SimpleProbabilityValue (float value) {
			this.value = value;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.value;
		}
		
		
		
		
	}
	
	private abstract class MathOperationProbabilityValue extends IProbabilityValue {
		protected IProbabilityValue op1 = null;
		protected IProbabilityValue op2 = null;
		
		
		
		public abstract float getProbability() throws InvalidProbabilityRangeException;
		

	}
	
	private class AddOperationProbabilityValue extends MathOperationProbabilityValue {
		AddOperationProbabilityValue(IProbabilityValue op1 , IProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.isFixedValue = op1.isFixedValue?op2.isFixedValue:false;
		}
		@Override
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.op1.getProbability() + this.op2.getProbability();
		}		
	}
	
	private class SubtractOperationProbabilityValue extends MathOperationProbabilityValue {
		SubtractOperationProbabilityValue(IProbabilityValue op1 , IProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;

			this.isFixedValue = op1.isFixedValue?op2.isFixedValue:false;
		}
		@Override
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.op1.getProbability() - this.op2.getProbability();
		}		
	}
	
	private class MultiplyOperationProbabilityValue extends MathOperationProbabilityValue {
		MultiplyOperationProbabilityValue(IProbabilityValue op1 , IProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.isFixedValue = op1.isFixedValue?op2.isFixedValue:false;
		}
		@Override
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.op1.getProbability() * this.op2.getProbability();
		}		
	}
	
	private class DivideOperationProbabilityValue extends MathOperationProbabilityValue {
		DivideOperationProbabilityValue(IProbabilityValue op1 , IProbabilityValue op2) {
			this.op1 = op1;
			this.op2 = op2;
			this.isFixedValue = op1.isFixedValue?op2.isFixedValue:false;
		}
		@Override
		public float getProbability() throws InvalidProbabilityRangeException {
			return this.op1.getProbability() / this.op2.getProbability();
		}		
	}
	
	private class NegativeOperationProbabilityValue extends MathOperationProbabilityValue {
		NegativeOperationProbabilityValue(IProbabilityValue op1) {
			this.op1 = op1;
			this.op2 = op1;
			this.isFixedValue = op1.isFixedValue;
		}
		@Override
		public float getProbability() throws InvalidProbabilityRangeException {
			return - this.op1.getProbability();
		}		
	}
	
	private class CardinalityProbabilityValue extends IProbabilityValue {
		//private float value = Float.NaN;
		private String varSetName = null;		
		//private SSBNNode thisNode = null;
		
		private TempTableHeaderCell currentHeader = null;
		
		/**
		 * Represents a probability value from cardinality function
		 * It calculates the value using thisNode's parents set
		 * @param currentHeader: its currentParentSetMap should contain all considered parents at
		 * that point, mapped by resident's name and each elements should be lists of nodes
		 * containing SAME strong OV instances (e.g. ST0)
		 */
		CardinalityProbabilityValue (TempTableHeaderCell currentHeader, String varsetname) {
			this.currentHeader = currentHeader;
			this.varSetName= varsetname;
			this.isFixedValue = false;
		}

		public float getProbability() throws InvalidProbabilityRangeException {
			
			if (this.currentHeader == null) {
				return Float.NaN;
			}
			if (getSSBNNode() == null) {
				return Float.NaN;
			}
			
			// look for the upper if clauses which has matching varsetname
			TempTableHeaderCell matchingHeader = this.currentHeader;
			while (!matchingHeader.getVarsetname().equalsIgnoreCase(this.varSetName)) {
				try{
					matchingHeader = (TempTableHeaderCell)matchingHeader.getUpperClause();
				} catch (ClassCastException e) {
					// we found a container other than TempTableHeaderCell
					// probably, it is a TempTable
					// so, there was no perfect match for varsetname...
					return 0;
				}
			}
			
			// if we reach this code, we found a perfect match for varsetname
			return matchingHeader.getValidParentSetCount();
		}

		
		
		
	}
	
	
	private class ComparisionProbabilityValue extends IProbabilityValue {
		private IProbabilityValue arg0 = null;
		private IProbabilityValue arg1 = null;
		private boolean isMax = false;
		/**
		 * Represents a comparision function between two values (MAX or MIN)
		 * @param arg0
		 * @param arg1
		 * @param isMax true if it represents a MAX function. If false, it represents a MIN
		 * function.
		 */
		ComparisionProbabilityValue (IProbabilityValue arg0, IProbabilityValue arg1, boolean isMax) {
			this.arg0 = arg0;
			this.arg1 = arg1;
			this.isMax = isMax;
			this.isFixedValue = arg0.isFixedValue?arg1.isFixedValue:false;
		}
		public float getProbability() throws InvalidProbabilityRangeException {
			Float prob0 = this.arg0.getProbability();
			if (Float.isNaN(prob0)) {
				return prob0;
			}
			
			Float prob1 = this.arg1.getProbability();
			if (Float.isNaN(prob1)) {
				return prob1;
			}
			/*
			 * the code below has the same meaning of:
			 * if (isMax) {
			 * 		if (prob0 > prob1) {
			 * 			return prob0;
			 * 		} else {
			 * 			return prob1;
			 * 		}
			 * } else {
			 * 		if (prob0 < prob1) {
			 * 			return prob0;
			 * 		} else {
			 * 			return prob1;
			 * 		}
			 * }
			 * 
			 */
			if (this.isMax == (prob0 > prob1)) {				
				return prob0;
			} else {
				return prob1;
			}
		}
		


	}

	private class EntityAndArguments {
		public Entity entity = null;
		public List<OVInstance> arguments = null;
		/**
		 * Creates an alternative (compact) way to represent a particular state of
		 * a SSBNNode, by storing its current value (entity) and its current 
		 * arguments (arguments).
		 * For instance, if SSBNNode = DangerToSelf((st,ST0),(t,T0)) = [Phaser2Range |
		 * PulseCannonRange | TorpedoRange], then a possible value of EntityAndArguments 
		 * would be (Phaser2Range;[(st,ST0),(t,T0)]), which means that DangerToSelf
		 * is at value Phaser2Range when its arguments st=T0 and t=T0.
		 * @param entity
		 * @param arguments
		 */
		public EntityAndArguments (Entity entity, List<OVInstance> arguments) {
			this.entity = entity;
			this.arguments = arguments;
		}
	}

	/**
	 * @return the tempTable
	 */
	protected TempTable getTempTable() {
		return tempTable;
	}


	/**
	 * If this class' constructor should return a singleton or not
	 * @return the singleton
	 */
	public static Boolean getSingleton() {
		return singleton;
	}


	/**
	 * If this class' constructor should return a singleton or not
	 * @param singleton the singleton to set
	 */
	public static void setSingleton(Boolean singleton) {
		Compiler.singleton = singleton;
	}



}