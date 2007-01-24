package unbbayes.prs.mebn.table;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.exception.TableFunctionMalformedException;

/**
 * Classe responsavel por realizar um parser na tabela definida
 * pelo usuario. 
 * 
 * @author Laecio Lima dos Santos
 *
 */

public class TableParser {
	
	// The MEBN where this table is defined
	private MultiEntityBayesianNetwork mebn;
	// The node that this table is defining
	private MultiEntityNode node;
	
	private TableFunction tableFunction = new TableFunction();
	
	public TableParser(MultiEntityBayesianNetwork mebn, MultiEntityNode node) {
		this.mebn = mebn;
		this.node = node;
		
	}
	
	public List parseTable(String tableFunction) throws TableFunctionMalformedException, NodeNotPresentInMTheoryException, EntityNotPossibleValueOfNodeException {
		List data = new ArrayList();
		
		StringTokenizer st = new StringTokenizer(tableFunction);
		
		while (st.hasMoreTokens()) {
			IfClause ifClause = parseIfClause(st);
			parseProbabilityFunction(st, ifClause);
		}
		
		return data;
	}
	
	private IfClause parseIfClause(StringTokenizer st) throws TableFunctionMalformedException, NodeNotPresentInMTheoryException, EntityNotPossibleValueOfNodeException {
		String token = st.nextToken();
		IfClause ifClause;
		if (token.equalsIgnoreCase("IF")) {
			token = st.nextToken();
			ifClause = new IfClause();
			if (token.equalsIgnoreCase("ANY")) {
				// A string representing the parameter subset
				token = st.nextToken();
				ifClause.setIfParameterSetName(token);
				ifClause.setIfOperator(IfOperator.ANY);
				token = st.nextToken();
				if (token.equalsIgnoreCase("HAVE(")) {
					parseIfClause(st, ifClause);
				} else {
					throw new TableFunctionMalformedException("\'HAVE(\' expected where " + token + "was found.");
				}
			} else if (token.equalsIgnoreCase("ALL")) {
				// A string representing the parameter set
				token = st.nextToken();
				ifClause.setIfParameterSetName(token);
				ifClause.setIfOperator(IfOperator.ALL);
				token = st.nextToken();
				if (token.equalsIgnoreCase("HAVE(")) {
					parseIfClause(st, ifClause);
				} else {
					throw new TableFunctionMalformedException("\'HAVE(\' expected where " + token + "was found.");
				}
			} else if (token.equalsIgnoreCase("(")) {
				ifClause.setIfOperator(IfOperator.NONE);
				parseIfClause(st, ifClause);
			} else {
				throw new TableFunctionMalformedException("\'ANY\\ALL\\(\' expected where " + token + "was found.");
			}
		} else {
			throw new TableFunctionMalformedException("\'IF\' expected where " + token + "was found.");
		}
		return ifClause;
	}
	
	private void parseIfClause(StringTokenizer st, IfClause ifClause) throws TableFunctionMalformedException, NodeNotPresentInMTheoryException, EntityNotPossibleValueOfNodeException {
		BooleanFunction booleanFunction;
		String nodeName = st.nextToken();
		String token = st.nextToken();
		String stateName;
		if (token.equalsIgnoreCase("==")) {
			stateName = st.nextToken();
			booleanFunction = new BooleanFunction(mebn, nodeName, stateName);
			ifClause.addBooleanFunction(booleanFunction);
			token = st.nextToken();
			if (token.equalsIgnoreCase(")THEN")) {
				tableFunction.addIfClause(ifClause);
				return;
			} else if (token.equalsIgnoreCase("AND")) {
				ifClause.addLogicOperator(LogicOperator.AND);
				parseIfClause(st, ifClause);
				return;
			} else if (token.equalsIgnoreCase("OR")) {
				ifClause.addLogicOperator(LogicOperator.OR);
				parseIfClause(st, ifClause);
				return;
			} else {
				throw new TableFunctionMalformedException("\')THEN\\AND\\OR\' expected where " + token + "was found.");
			}
		} else {
			throw new TableFunctionMalformedException("\'=\' expected where " + token + "was found.");
		}
	}
	
	private void parseProbabilityFunction(StringTokenizer st, IfClause ifClause) throws TableFunctionMalformedException, EntityNotPossibleValueOfNodeException {
		ProbabilityFunction probabilityFunction = new ProbabilityFunction(node);
		String token = st.nextToken();
		if (token.equalsIgnoreCase("[")) {
			// Node's state
			token = st.nextToken();
			StateFunction stateFunction = new StateFunction(node, token);
			token = st.nextToken();
			if (token.equalsIgnoreCase("=")) {
				token = st.nextToken();
				boolean isNumber = false;
				// Try to convert the token to a number
				try {
					Float.parseFloat(token);
					isNumber = true;
				} catch(NumberFormatException e) {
				}
				if (isNumber) {
					
				} else if (token.equalsIgnoreCase("CARDINALITY(")) {
					token = st.nextToken();
					if (token.equalsIgnoreCase(ifClause.getIfParameterSetName())) {
						token = st.nextToken();
						if (token.equalsIgnoreCase(")")) {
							stateFunction.addFunctionElement("CARDINALITY(" + ifClause.getIfParameterSetName() + ")");
							token = st.nextToken();
							if (token.equalsIgnoreCase("+")) {
								
							} else if (token.equalsIgnoreCase("-")) {
								
							} else if (token.equalsIgnoreCase("*")) {
								
							} else if (token.equalsIgnoreCase("/")) {
								
							} else if (token.equalsIgnoreCase("]")) {
								
							} else {
								throw new TableFunctionMalformedException("\'+\\-\\*\\/\\]\' expected where " + token + "was found.");
							}
						} else {
							throw new TableFunctionMalformedException("\'" + ifClause.getIfParameterSetName() + "\' expected where " + token + "was found.");
						}
					} else {
						throw new TableFunctionMalformedException("\'" + ifClause.getIfParameterSetName() + "\' expected where " + token + "was found.");
					}
				} else if (token.equalsIgnoreCase("MAX(")) {
					
				} else if (token.equalsIgnoreCase("MIN(")) {
					
				} else if (node.hasPossibleValue(token)) {
					
				}
			} else {
				throw new TableFunctionMalformedException("\'=\' expected where " + token + "was found.");
			}
		} else {
			throw new TableFunctionMalformedException("\'[\' expected where " + token + "was found.");
		}
	}
	
}
