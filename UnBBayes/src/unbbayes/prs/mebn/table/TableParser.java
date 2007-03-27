package unbbayes.prs.mebn.table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.TableFunctionMalformedException;

/**
 * Classe responsavel por realizar um parser na tabela definida pelo usuario.
 */

public class TableParser {

	// The MEBN where this table is defined
	private MultiEntityBayesianNetwork mebn;

	// The node that this table is defining
	private MultiEntityNode node;

	private TableFunction tableFunction = new TableFunction();
	
	// Helper atributes
	private IfClause currentIfClause;
	private String token;

	public TableParser(MultiEntityBayesianNetwork mebn, MultiEntityNode node) {
		this.mebn = mebn;
		this.node = node;

	}

	public List parseTable(String tableFunction)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException,
			InvalidProbabilityFunctionOperandException {
		List data = new ArrayList();

		StringTokenizer st = new StringTokenizer(tableFunction);

		while (st.hasMoreTokens()) {
			token = st.nextToken();
			IfClause ifClause = parseIfClause(st);
			currentIfClause = ifClause;
			token = st.nextToken();
			parseProbabilityFunction(st, ifClause);
			token = st.nextToken();
			if (token.equalsIgnoreCase("IF")) {
				ifClause = parseIfClause(st);
				currentIfClause = ifClause;
				token = st.nextToken();
			} else {
				ifClause = new IfClause();
				currentIfClause = ifClause;
				// This is the parameter set for the outer else, that means
				// the default table value.
				// TODO ACRESCENTAR NA DOCUMENTAÇÃO DA TABELA
				ifClause.setIfParameterSetName("DEFAULT");
			}
			parseProbabilityFunction(st, ifClause);
		}

		return data;
	}

	private IfClause parseIfClause(StringTokenizer st)
			throws TableFunctionMalformedException,
			NodeNotPresentInMTheoryException,
			EntityNotPossibleValueOfNodeException {
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
			EntityNotPossibleValueOfNodeException {
		BooleanFunction booleanFunction;
		String nodeName = st.nextToken();
		token = st.nextToken();
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
			InvalidProbabilityFunctionOperandException {
		ProbabilityFunction probabilityFunction = new ProbabilityFunction(node);
		ifClause.setProbabilityFunciton(probabilityFunction);
		if (token.equalsIgnoreCase("[")) {
			boolean hasNextStateFunction = false;
			do {
				// Node's state
				token = st.nextToken();
				StateFunction stateFunction = new StateFunction(node, token,
						ifClause);
				probabilityFunction.addStateFunction(stateFunction);
				token = st.nextToken();
				if (token.equalsIgnoreCase("=")) {
					hasNextStateFunction = parseStateFunction(st, stateFunction);
				} else {
					throw new TableFunctionMalformedException(
							"\'=\' expected where " + token + " was found.");
				}
			} while (hasNextStateFunction);
		} else {
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
				return false;
			} else if (stateFunction != null
					&& token.equalsIgnoreCase(",")) {
				stateFunction.setFunction(number);
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
	
	public static void main(String[] args) throws TableFunctionMalformedException, NodeNotPresentInMTheoryException, EntityNotPossibleValueOfNodeException, InvalidProbabilityFunctionOperandException {
		MultiEntityBayesianNetwork mebn = null; 
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			mebn = prOwlIO.loadMebn(new File("examples/mebn/StarshipTableParser.owl")); 
			System.out.println("Load concluido"); 
		}
		catch (IOMebnException e){
			System.out.println("ERROR IO PROWL!!!!!!!!!"); 
			e.printStackTrace();
		}
		catch (IOException e){
			System.out.println("ERROR IO!!!!!!!!!"); 
			e.printStackTrace();
		}
		
		String tableString =  
		" if any STi have( OpSpec == Cardassian and HarmPotential == true )then " + 
		"  [ Un = .90 + min( .10 ; .025 * cardinality( STi ) ) , Hi = ( 1 - Un ) * .8 , Me = ( 1 - Un ) * .2 , Lo = 0 ] " +
		" else if any STj have( OpSpec == Romulan and HarmPotential == true )then " +
		"  [ Un = .70 + min( .30; .03 * cardinality( STj ) ) , Hi = ( 1 - Un ) * .6 , Me = ( 1 - Hi ) * .3 , Lo = ( 1 - Hi ) * .1 ] " + 
		" else if any STj have( OpSpec == Unknown and HarmPotential == true )then " + 
		"  [ Un = ( 1 - Hi ) , Hi = .50 - min( .20 ; .02 * cardinality( STk ) ) , Me = .50 - min( .20 ; .02 * number( STk ) ) , Lo = ( 1 - Me ) ] " +
		" else if any STk have( OpSpec == Klingon and HarmPotential == true )then " +
		"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
		" else if any STl have( OpSpec == Friend and HarmPotential == true )then " +
		"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
		" else [ Un = 0 , Hi = 0 , Me = 0 , Lo = 1 ] ";
		
		TableParser tableParser = new TableParser(mebn, (MultiEntityNode)mebn.getNode("DangerToSelf"));
		
		tableParser.parseTable(tableString);
	}
}
