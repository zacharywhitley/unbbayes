/**
 * This is a dead code!!!! Do not use your effort to 
 * edit it because it is not called by any
 * class!!!!!!
 */
package unbbayes.prs.mebn.table;


import java.util.ArrayList;

import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;




import unbbayes.prs.Node;

import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;

import unbbayes.prs.mebn.compiler.MEBNTableParser;
import unbbayes.prs.mebn.compiler.exception.InvalidConditionantException;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.util.NodeList;


/**
 * 
 * This is a poltergeist programming anti-pattern.
 * It's previous version was a dead-code programming anti-pattern.
 * It exists only to mantain possible compatibility.
 * It only repasses functionality to MEBNTableParser class
 * Please, use MEBNTableParser instead
 * @author Shou Matsumoto
 * @see unbbayes.prs.mebn.compiler.MEBNTableParser
 */
public class TableParser {

	MEBNTableParser parser = null;
	
	public TableParser(MultiEntityBayesianNetwork mebn, MultiEntityNode node) {
		this.parser = MEBNTableParser.getInstance(mebn,(DomainResidentNode)node);

	}

	public List parseTable(String tableFunction)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException,
			InvalidProbabilityFunctionOperandException,
			NoDefaultDistributionDeclaredException, 
			InvalidConditionantException,
			SomeStateUndeclaredException, 
			InvalidProbabilityRangeException,
			MEBNException{
		List data = new ArrayList();
		//try {
			this.parser.parse(tableFunction);
		//} catch (Exception e) {
			//throw new TableFunctionMalformedException(e.getMessage());
		//}
		return data;
	}

}
