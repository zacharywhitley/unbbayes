package unbbayes.prs.mebn.table;


import java.util.ArrayList;

import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;




import unbbayes.prs.Node;

import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;

import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.exception.InvalidConditionantException;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.table.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.table.exception.TableFunctionMalformedException;
import unbbayes.util.NodeList;


/**
 * Classe responsavel por realizar um parser na tabela definida pelo usuario.
 */

public class TableParser {

	// System messages
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.table.resources.Resources");

	
	// The MEBN where this table is defined
	private MultiEntityBayesianNetwork mebn;

	// The node that this table is defining
	private MultiEntityNode node;

	private TableFunction tableFunction = new TableFunction();
	
	// Helper atributes
	private IfClause currentIfClause;
	private String token;
	
	// Last read probability distribution's possible value. Negative if unknown
	private float lastProbability = -1.0F;
	
	public TableParser(MultiEntityBayesianNetwork mebn, MultiEntityNode node) {
		this.mebn = mebn;
		this.node = node;

	}

	public List parseTable(String tableFunction)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException,
			InvalidProbabilityFunctionOperandException,
			NoDefaultDistributionDeclaredException, 
			InvalidConditionantException,
			SomeStateUndeclaredException {
		List data = new ArrayList();

		StringTokenizer st = new StringTokenizer(tableFunction);

		
		token = st.nextToken();
		
		IfClause ifClause = parseIfClause(st);
		currentIfClause = ifClause;
		token = st.nextToken();

		parseProbabilityFunction(st, ifClause);
		
		// check consistency C09
		// Verify if Default distribution was declared at the end
		boolean isDefaultDist = false;
		while (!isDefaultDist) {
			if (!st.hasMoreTokens()) {
				throw new NoDefaultDistributionDeclaredException();
			}
			token = st.nextToken();
			if (!token.equalsIgnoreCase("ELSE")) {
				// check consistency C09
				// Every if should have else, at least the default
				throw new NoDefaultDistributionDeclaredException();
			}
			token = st.nextToken();
			if (token.equalsIgnoreCase("IF")) {
				ifClause = parseIfClause(st);
				currentIfClause = ifClause;
				token = st.nextToken();
			} else {
				isDefaultDist = true;
				ifClause = new IfClause();
				currentIfClause = ifClause;
				// This is the parameter set for the outer else, that means
				// the default table value.
				// TODO ACRESCENTAR NA DOCUMENTAÇÃO DA TABELA
				ifClause.setIfParameterSetName("DEFAULT");
			} 
			try {

				parseProbabilityFunction(st, ifClause);
				
			} catch (TableFunctionMalformedException e) {
				// Consistency check C09
				// If we find malformed table here, it's because there is no "default" clause
				// This might be an anti-pattern (exception hiding), but its necessary
				// to notify user that he has to declare a default clause (else)
				throw new NoDefaultDistributionDeclaredException();
			}
			
		}

		return data;
	}

	private IfClause parseIfClause(StringTokenizer st)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException,
			InvalidConditionantException {
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
					throw new TableFunctionMalformedException(
							"\'HAVE(\' expected where " + token + " was found.");
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
					throw new TableFunctionMalformedException(
							"\'HAVE(\' expected where " + token + " was found.");
				}
			} else if (token.equalsIgnoreCase("(")) {
				ifClause.setIfOperator(IfOperator.NONE);
				parseIfClause(st, ifClause);
			} else {
				throw new TableFunctionMalformedException(
						"\'ANY\\ALL\\(\' expected where " + token
								+ " was found.");
			}
		} else {
			throw new TableFunctionMalformedException("\'IF\' expected where "
					+ token + " was found.");
		}
		return ifClause;
	}

	private void parseIfClause(StringTokenizer st, IfClause ifClause)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException,
			InvalidConditionantException {
		BooleanFunction booleanFunction;
		String nodeName = st.nextToken();
		// Consistency check C09: 
		token = st.nextToken();
		String stateName;
		if (token.equalsIgnoreCase("==")) {
			stateName = st.nextToken();
			booleanFunction = new BooleanFunction(mebn, nodeName, stateName);
			if (!isValidConditionant(mebn, nodeName)) {
				// Consistency check C09
				// Conditionants must be parents
				throw new InvalidConditionantException();
			}
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
				throw new TableFunctionMalformedException(
						"\')THEN\\AND\\OR\' expected where " + token
								+ " was found.");
			}
		} else {
			throw new TableFunctionMalformedException("\'=\' expected where "
					+ token + " was found.");
		}
	}
	

	private void parseProbabilityFunction(StringTokenizer st, IfClause ifClause)
			throws TableFunctionMalformedException,
			EntityNotPossibleValueOfNodeException,
			InvalidProbabilityFunctionOperandException,
			SomeStateUndeclaredException {
		ProbabilityFunction probabilityFunction = new ProbabilityFunction(node);
		ifClause.setProbabilityFunciton(probabilityFunction);
		if (token.equalsIgnoreCase("[")) {
			
			boolean hasNextStateFunction = false;
			
			// Consistency check C09
			// Verify if all states has probability declared
			List<Entity> declaredStates = new ArrayList<Entity>();
			List<Entity> possibleStates = this.node.getPossibleValueList();
			
			do {
				// Node's state
				token = st.nextToken();
				
				StateFunction stateFunction = new StateFunction(node, token,
						ifClause);
				probabilityFunction.addStateFunction(stateFunction);
				
				// Consistency check C09
				// Verify if all states has probability declared
				declaredStates.add(possibleStates.get(this.node.getPossibleValueIndex(token)));
				
				
				token = st.nextToken();
				if (token.equalsIgnoreCase("=")) {
					hasNextStateFunction = parseStateFunction(st, stateFunction);
				} else {
					throw new TableFunctionMalformedException(
							"\'=\' expected where " + token + " was found.");
				}
			} while (hasNextStateFunction);
			// Consistency check C09
			// Verify if all states has probability declared
			if (!declaredStates.containsAll(possibleStates)) {
				throw new SomeStateUndeclaredException();
			}
		} else {

			System.out.println("...Tracing...");
			
			throw new TableFunctionMalformedException("\'[\' expected where "
					+ token + " was found.");
		}
	}

	/**
	 * 
	 * @param st
	 * @param stateFunction
	 * @return True if it has another state function to parse (finishes with 
	 * ','), false otherwise.
	 * @throws InvalidProbabilityFunctionOperandException
	 * @throws TableFunctionMalformedException
	 * 
	 */
	private boolean parseStateFunction(StringTokenizer st,
			StateFunction stateFunction)
			throws InvalidProbabilityFunctionOperandException,
			TableFunctionMalformedException {
		ProbabilityFunctionOperator firstFunction;

		token = st.nextToken();

		if (token.equalsIgnoreCase("MAX(")) {
			firstFunction = ProbabilityFunctionOperator.MAX;
			token = st.nextToken();
			parseSimpleStateFunction(st, null, firstFunction, true);
			return parseStateFunction(st, stateFunction);
		} else if (token.equalsIgnoreCase("MIN(")) {
			firstFunction = ProbabilityFunctionOperator.MIN;
			token = st.nextToken();
			parseSimpleStateFunction(st, null, firstFunction, true);
			return parseStateFunction(st, stateFunction);
		} else if (token.equalsIgnoreCase(",")) {
			return true;
		} else {
			return parseSimpleStateFunction(st, stateFunction, null, false);
		}
	}

	/**
	 * 
	 * @param st
	 * @param token
	 * @param stateFunction
	 * @param minMaxFunction
	 * @param minMaxFirstOperand
	 * @return True if it has another state function to parse (finishes with 
	 * ','), false otherwise.
	 * @throws InvalidProbabilityFunctionOperandException
	 * @throws TableFunctionMalformedException
	 */
	private boolean parseSimpleStateFunction(StringTokenizer st,
			StateFunction stateFunction,
			ProbabilityFunctionOperator minMaxFunction, 
			boolean minMaxFirstOperand)
			throws InvalidProbabilityFunctionOperandException,
			TableFunctionMalformedException {
		ProbabilityFunctionOperator firstFunction;
		ProbabilityFunctionOperator secondFunction;

		boolean isNumber = false;
		// Try to convert the token to a number
		try {
			Float.parseFloat(token);
			isNumber = true;
		} catch (NumberFormatException e) {
		}
		if (token.equalsIgnoreCase("CARDINALITY(")) {
			token = st.nextToken();
			if (token.equalsIgnoreCase(currentIfClause
					.getIfParameterSetName())) {
				token = st.nextToken();
				if (token.equalsIgnoreCase(")")) {
					firstFunction = ProbabilityFunctionOperator.CARDINALITY;
					firstFunction.setUniqueOperand(currentIfClause
							.getIfParameterSetName());

					token = st.nextToken();
					if (token.equalsIgnoreCase("+")
							|| token.equalsIgnoreCase("-")
							|| token.equalsIgnoreCase("*")
							|| token.equalsIgnoreCase("/")) {
						secondFunction = getBasicFunction(token);
						secondFunction.setFirstOperand(firstFunction);
						if (stateFunction != null) {
							stateFunction.setFunction(secondFunction);
							return parseStateFunction(st, stateFunction);
						} else if (minMaxFunction != null) {
							if (minMaxFirstOperand) {
								setLastFunctionInFirstOperand(minMaxFunction, secondFunction);
							} else {
								setLastFunctionInSecondOperand(minMaxFunction, secondFunction);
							}
							token = st.nextToken();
							return parseSimpleStateFunction(st, null,
									minMaxFunction, minMaxFirstOperand);
						}
					} else if (stateFunction != null
							&& token.equalsIgnoreCase("]")) {
						stateFunction.setFunction(firstFunction);
						return false;
					} else if (stateFunction != null
							&& token.equalsIgnoreCase(",")) {
						stateFunction.setFunction(firstFunction);
						return true;
					} else if (minMaxFunction != null && minMaxFirstOperand
							&& token.equalsIgnoreCase(";")) {
						setLastFunctionInFirstOperand(minMaxFunction, firstFunction);
						token = st.nextToken();
						return parseSimpleStateFunction(st, null,
								minMaxFunction, false);
					} else if (minMaxFunction != null && !minMaxFirstOperand
							&& token.equalsIgnoreCase(")")) {
						setLastFunctionInSecondOperand(minMaxFunction, firstFunction);
						return false;
					} else if (stateFunction != null) {
						throw new TableFunctionMalformedException(
								"\'+\\-\\*\\/\\]\' expected where " + token
										+ " was found.");
					} else if (minMaxFunction != null && minMaxFirstOperand) {
						throw new TableFunctionMalformedException(
								"\'+\\-\\*\\/\\;\' expected where " + token
										+ " was found.");
					} else if (minMaxFunction != null && !minMaxFirstOperand) {
						throw new TableFunctionMalformedException(
								"\'+\\-\\*\\/\\)\' expected where " + token
										+ " was found.");
					}
				} else {
					throw new TableFunctionMalformedException(
							"\')\' expected where " + token + " was found.");
				}
			} else {
				throw new TableFunctionMalformedException("\'"
						+ currentIfClause.getIfParameterSetName()
						+ "\' expected where " + token + " was found.");
			}
		} else if (node.hasPossibleValue(token)) {
			firstFunction = ProbabilityFunctionOperator.REFERENCE;
			firstFunction.setUniqueOperand(token);

			token = st.nextToken();
			if (token.equalsIgnoreCase("+") || token.equalsIgnoreCase("-")
					|| token.equalsIgnoreCase("*")
					|| token.equalsIgnoreCase("/")) {
				secondFunction = getBasicFunction(token);
				secondFunction.setFirstOperand(firstFunction);
				if (stateFunction != null) {
					stateFunction.setFunction(secondFunction);
					return parseStateFunction(st, stateFunction);
				} else if (minMaxFunction != null) {
					if (minMaxFirstOperand) {
						setLastFunctionInFirstOperand(minMaxFunction, secondFunction);
					} else {
						setLastFunctionInSecondOperand(minMaxFunction, secondFunction);
					}
					token = st.nextToken();
					return parseSimpleStateFunction(st, null,
							minMaxFunction, minMaxFirstOperand);
				}
			} else if (stateFunction != null
					&& token.equalsIgnoreCase("]")) {
				stateFunction.setFunction(firstFunction);
				return false;
			} else if (stateFunction != null
					&& token.equalsIgnoreCase(",")) {
				stateFunction.setFunction(firstFunction);
				return true;
			} else if (minMaxFunction != null && minMaxFirstOperand
					&& token.equalsIgnoreCase(";")) {
				setLastFunctionInFirstOperand(minMaxFunction, firstFunction);
				token = st.nextToken();
				return parseSimpleStateFunction(st, null,
						minMaxFunction, false);
			} else if (minMaxFunction != null && !minMaxFirstOperand
					&& token.equalsIgnoreCase(")")) {
				setLastFunctionInSecondOperand(minMaxFunction, firstFunction);
				return false;
			} else if (stateFunction != null) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\]\' expected where " + token
								+ " was found.");
			} else if (minMaxFunction != null && minMaxFirstOperand) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\;\' expected where " + token
								+ " was found.");
			} else if (minMaxFunction != null && !minMaxFirstOperand) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\)\' expected where " + token
								+ " was found.");
			}
		} else if (isNumber) {
			float number = Float.parseFloat(token);

			token = st.nextToken();
			if (token.equalsIgnoreCase("+") || token.equalsIgnoreCase("-")
					|| token.equalsIgnoreCase("*")
					|| token.equalsIgnoreCase("/")) {
				secondFunction = getBasicFunction(token);
				secondFunction.setFirstOperand(number);
				if (stateFunction != null) {
					stateFunction.setFunction(secondFunction);
					return parseStateFunction(st, stateFunction);
				} else if (minMaxFunction != null) {
					if (minMaxFirstOperand) {
						setLastFunctionInFirstOperand(minMaxFunction, secondFunction);
					} else {
						setLastFunctionInSecondOperand(minMaxFunction, secondFunction);
					}
					token = st.nextToken();
					return parseSimpleStateFunction(st, null,
							minMaxFunction, minMaxFirstOperand);
				}
			} else if (stateFunction != null
					&& token.equalsIgnoreCase("]")) {
				stateFunction.setFunction(number);
				// Consistency check C09
				// Verify static probability sum is 1
				this.setLastProbability(number);
				return false;
			} else if (stateFunction != null
					&& token.equalsIgnoreCase(",")) {
				stateFunction.setFunction(number);
				//	Consistency check C09
				// Verify static probability sum is 1
				this.setLastProbability(number);
				return true;
			} else if (minMaxFunction != null && minMaxFirstOperand
					&& token.equalsIgnoreCase(";")) {
				setLastFunctionInFirstOperand(minMaxFunction, number);
				token = st.nextToken();
				return parseSimpleStateFunction(st, null,
						minMaxFunction, false);
			} else if (minMaxFunction != null && !minMaxFirstOperand
					&& token.equalsIgnoreCase(")")) {
				setLastFunctionInSecondOperand(minMaxFunction, number);
				return false;
			} else if (stateFunction != null) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\]\' expected where " + token
								+ " was found.");
			} else if (minMaxFunction != null && minMaxFirstOperand) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\;\' expected where " + token
								+ " was found.");
			} else if (minMaxFunction != null && !minMaxFirstOperand) {
				throw new TableFunctionMalformedException(
						"\'+\\-\\*\\/\\)\' expected where " + token
								+ " was found.");
			}
		} else if (token.equalsIgnoreCase("(")) {
			token = st.nextToken();
			// TODO LASCOU... NAO TO TRATANDO ()'S NEM PRECEDENCIA DE OPERADORES
			
		}
		return false;
	}

	private void setLastFunctionInFirstOperand(ProbabilityFunctionOperator function1,
			ProbabilityFunctionOperator function2)
			throws InvalidProbabilityFunctionOperandException {
		if (function1.getFirstOperand() != null) {
			function1 = function1.getFirstOperand();
		} else {
			function1.setFirstOperand(function2);
			return;
		}
		while (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		}
		function1.setSecondOperand(function2);
	}
	
	private void setLastFunctionInSecondOperand(ProbabilityFunctionOperator function1,
			ProbabilityFunctionOperator function2)
			throws InvalidProbabilityFunctionOperandException {
		if (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		} else {
			function1.setSecondOperand(function2);
			return;
		}
		while (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		}
		function1.setSecondOperand(function2);
	}
	
	private void setLastFunctionInFirstOperand(ProbabilityFunctionOperator function1,
			float number)
			throws InvalidProbabilityFunctionOperandException {
		if (function1.getFirstOperand() != null) {
			function1 = function1.getFirstOperand();
		} else {
			function1.setFirstOperand(number);
			return;
		}
		while (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		}
		function1.setSecondOperand(number);
	}
	
	private void setLastFunctionInSecondOperand(ProbabilityFunctionOperator function1,
			float number)
			throws InvalidProbabilityFunctionOperandException {
		if (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		} else {
			function1.setSecondOperand(number);
			return;
		}
		while (function1.getSecondOperand() != null) {
			function1 = function1.getSecondOperand();
		}
		function1.setSecondOperand(number);
	}

	private ProbabilityFunctionOperator getBasicFunction(String token) {
		if (token.equalsIgnoreCase("+")) {
			return ProbabilityFunctionOperator.PLUS;
		} else if (token.equalsIgnoreCase("-")) {
			return ProbabilityFunctionOperator.MINUS;
		} else if (token.equalsIgnoreCase("*")) {
			return ProbabilityFunctionOperator.TIMES;
		} else if (token.equalsIgnoreCase("/")) {
			return ProbabilityFunctionOperator.DIVIDE;
		}
		return null;
	}
	
	/**
	 * Consistency check C09
	 * Conditionants must be parents referenced by this.node	
	 * @return whether node with name == nodeName is a valid conditionant.
	 */
	private boolean isValidConditionant(MultiEntityBayesianNetwork mebn, String conditionantName) {
		
		Node conditionant = mebn.getNode(conditionantName);
		/*
		System.out.println("!!!Conditionants, mebn = " + mebn.getName());
		System.out.println("!!!Conditionants, conditionantName = " + conditionantName);
		System.out.println("!!!Conditionants, conditionantNode = " + conditionant.getName());
		
		NodeList nodelist = conditionant.getChildren();
		for (int i = 0; i < nodelist.size(); i++) {
			System.out.println("!!!!!!Children of conditionants= " + nodelist.get(i).getName());
		}
		nodelist = this.node.getParents();
		for (int i = 0; i < nodelist.size(); i++) {
			System.out.println("!!!!!!Parents of me = " + nodelist.get(i).getName());
			System.out.println("!!!!!!Input instance of: " + nodelist.get(i).getClass().getName());
		}
		*/
		if (conditionant != null) {
			
			//	Check if it's parent of current node	
			if (this.node.getParents().contains(conditionant)) {
				//System.out.println("!!!!Is node");
				return true;
			} else {
				NodeList parents = this.node.getParents();
				for (int i = 0; i < parents.size(); i++) {
					if (parents.get(i) instanceof GenerativeInputNode) {
						if ( ((GenerativeInputNode)(parents.get(i))).getInputInstanceOf().equals(conditionant) ) {
							//System.out.println("!!!!Is GenerativeInputNode");
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
	 * @return Returns the possible value of last parsed Probability distribution.
	 * Negative if unknown.
	 */
	private float getLastProbability() {
		return lastProbability;
	}

	/**
	 * @param lastProbability Possible value of last parsed Probability distribution to set.
	 */
	private void setLastProbability(float lastProbability) {
		this.lastProbability = lastProbability;
	}
	
	
}
