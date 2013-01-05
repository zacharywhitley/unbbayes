package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.helper.DynamicTableHelper;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This is the the main class related to compile a bayesian network based on
 * data base instances. It is the implementation of Probabilistic Relational
 * Models Algorithm [Geetor, 2001].
 * 
 * @author David Saldaña.
 * 
 */
public class PrmCompiler {
	Logger log = Logger.getLogger(PrmCompiler.class);
	private IProbabilisticNetworkBuilder networkBuilder;
	private IPrmController prmController;
	private IDBController dbController;

	private static final int NUM_COLUMNS = 3;
	private int nodePosition;

	private HashMap<ProbabilisticNode, String> evidence;
	private IInferenceAlgorithm inferenceAlgorithm;
	private List<String> createdNodeNames;

	/**
	 * key: node name. value: node object.
	 */
	private Hashtable<String, ProbabilisticNode> createdNodes;

	private Hashtable<ProbabilisticNode, Attribute> createdNodeAtts;

	/**
	 * key: node name value: relationship where the node is the parent.
	 */
	Hashtable<String, ParentRel> parentInstanceNodes;

	public PrmCompiler(IPrmController prmController, IDBController dbController) {
		this.prmController = prmController;
		this.dbController = dbController;
		networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
	}

	/**
	 * Compile with PRM Algorithm.
	 * 
	 * @param qTable
	 *            query table.
	 * @param uniqueIndexColumn
	 *            unique index column.
	 * @param indexValue
	 *            index value.
	 * @param qColumn
	 *            query column.
	 * @param qValue
	 *            query value.
	 * @return created graph.
	 * @throws Exception
	 *             compiling exception.
	 */
	public Graph compile(Table qTable, Column uniqueIndexColumn,
			Object indexValue, Column qColumn, Object qValue) throws Exception {

		// Clear evidence
		evidence = new HashMap<ProbabilisticNode, String>();
		nodePosition = 0;
		createdNodeNames = new ArrayList<String>();
		createdNodes = new Hashtable<String, ProbabilisticNode>();
		createdNodeAtts = new Hashtable<ProbabilisticNode, Attribute>();
		// This is to create a dynamic CPT because it depends on the parents.
		parentInstanceNodes = new Hashtable<String, ParentRel>();

		// Resultant network
		ProbabilisticNetwork resultNet = networkBuilder
				.buildNetwork("RESULTANT BN for " + qColumn.getName() + "="
						+ String.valueOf(qValue) + "with id" + indexValue);

		// // //// Create a node for the query instance /////
		Attribute queryAtt = new Attribute(qTable, qColumn);
		//
		// // Probabilistic node
		ProbabilisticNode queryNode = createProbNode(
				String.valueOf(indexValue), queryAtt, resultNet);
		//
		// // /// Evidence for the first node ///
		String specificValue = dbController.getSpecificValue(qColumn,
				new Attribute(qTable, uniqueIndexColumn), "" + indexValue);
		// Get instance value
		evidence.put(queryNode, specificValue);

		// ///// Create nodes for the parents //////
		fillNetworkWithParents(resultNet, queryAtt, queryNode,
				uniqueIndexColumn, indexValue, qValue);

		// Fill CPTs
		fillCPTs();

		inferenceAlgorithm = new JunctionTreeAlgorithm();
		inferenceAlgorithm.setNetwork(resultNet);
		inferenceAlgorithm.run();
		inferenceAlgorithm.reset();

		// Update evidences.
		addEvidences(resultNet);
		inferenceAlgorithm.propagate();

		return resultNet;
	}

	private void fillCPTs() throws Exception {
		Enumeration<ProbabilisticNode> probNodes = createdNodeAtts.keys();

		while (probNodes.hasMoreElements()) {
			ProbabilisticNode pNode = (ProbabilisticNode) probNodes
					.nextElement();

			assignDynamicCPT(pNode, createdNodeAtts.get(pNode));

		}

	}

	private void addEvidences(ProbabilisticNetwork resultNet) {
		// Nodes with evidence.
		Set<ProbabilisticNode> nodes = evidence.keySet();

		// Adding evidence for each node.
		for (ProbabilisticNode node : nodes) {
			int statesSize = node.getStatesSize();

			for (int i = 0; i < statesSize; i++) {
				if (node.getStateAt(i).equals(evidence.get(node))) {
					node.addFinding(i);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @param resultNet
	 *            Network to fill.
	 * @param queryAtt
	 *            Attribute to convert to node recursively.
	 * @param queryNode
	 *            Current node of the query attribute.
	 * @param indexValue
	 *            id value to the instance.
	 * @param value
	 *            value of the instance.
	 */
	private void fillNetworkWithParents(ProbabilisticNetwork resultNet,
			Attribute queryAtt, ProbabilisticNode queryNode, Column indexCol,
			Object indexValue, Object value) throws Exception {

		// //////// Navigate by related parents until null references. ////////
		// Get the parent relationships of the probabilistic model.
		ParentRel[] parents = prmController.parentsOf(queryAtt);

		// Find parent instances for each parent relationship.
		for (ParentRel parentRel : parents) {

			Attribute parentAtt = parentRel.getParent();

			// ////////// For intrinsic attributes ////////////////
			// If the parent is intrinsic, it only has two attributes in the
			// path, because it does not have foreign keys and the relationship
			// is in the same instance.
			if (parentRel.getPath().length == 2) {
				// Create a new node.
				ProbabilisticNode parentNode = createProbNode(indexValue,
						parentAtt, resultNet);
				parentNode.setDescription(parentRel.getIdRelationsShip());

				// Edge to the child.
				addEdge(resultNet, parentNode, queryNode);

				// EVIDENCE
				String specificValue = dbController.getSpecificValue(
						parentAtt.getAttribute(),
						new Attribute(parentAtt.getTable(), indexCol), ""
								+ indexValue);

				// Get instance value
				evidence.put(parentNode, specificValue);

				// Add parent nodes to the query node.
				parentInstanceNodes.put(parentNode.getName(), parentRel);

				// Fill with parents recursively.
				fillNetworkWithParents(resultNet, parentAtt, parentNode,
						indexCol, indexValue, specificValue);

				continue;
			} else if (parentRel.getPath().length < 2) {
				throw new Exception("Invalid rel" + parentRel);
			} else {

				// /////////// For external attributes./////////////
				// Get the foreign key with the second element of the path.
				Attribute originAtt = parentRel.getPath()[1];

				// DIRECTION local.FK to other.ID or local.ID to other.FK. Child
				// to parent.
				boolean directionFKToId = !originAtt.getAttribute().equals(
						indexCol);

				// Get the local table with the fist element of the path.
				Table localTable = parentRel.getPath()[0].getTable();

				String initInstanceValue;

				if (directionFKToId) {
					// Get FK value.
					initInstanceValue = dbController.getSpecificValue(originAtt
							.getAttribute(),
							new Attribute(localTable, indexCol), String
									.valueOf(indexValue));
				} else {
					// id value.
					initInstanceValue = String.valueOf(indexValue);
				}

				// If it has parents.
				if (initInstanceValue == null
						|| initInstanceValue.equalsIgnoreCase("null")) {
					log.debug("Foreign key is null for "
							+ indexValue
							+ ". The recursivity must STOP here for this parent.");
					continue;
				}

				// Get instances.
				String[][] instanceValues = dbController
						.getParentRelatedInstances(parentRel, initInstanceValue);
				// Create a node for each parent instance.
				for (int i = 0; i < instanceValues.length; i++) {
					// Index for the instance i.
					String afIndex = instanceValues[i][0];
					// Value for the instance i.
					String afValue = instanceValues[i][1];

					// Validate repeated value is not necessary for parents.

					// Create Node.
					ProbabilisticNode parentNode = createProbNode(afIndex,
							parentAtt, resultNet);
					parentNode.setDescription(parentRel.getIdRelationsShip());

					// Add parent nodes to the query node.
					parentInstanceNodes.put(parentNode.getName(), parentRel);

					// Store evidence.
					evidence.put(parentNode, afValue);

					// Edge to the child.
					addEdge(resultNet, parentNode, queryNode);

					Column parentIndex;
					// Path id to the next node.
					if (directionFKToId) {
						parentIndex = parentRel.getPath()[parentRel.getPath().length - 2]
								.getAttribute();
					} else {
						Attribute lastAtt = parentRel.getPath()[parentRel
								.getPath().length - 1];
						try {
							parentIndex = lastAtt.getTable()
									.getPrimaryKeyColumns()[0];
						} catch (Exception e) {
							throw new Exception(
									"Error trying to load the primary key of "
											+ lastAtt, e);
						}

					}

					// Fill with parents recursively.
					fillNetworkWithParents(resultNet, parentAtt, parentNode,
							parentIndex, afIndex, afValue);
				}
			}
		}

		// ////////////////////////CHILDREN////////////////////////////////
		// //// Navigate by related children until null references. ///////
		ParentRel[] children = prmController.childrenOf(queryAtt);

		// For each child a new node is created and the recursive algorithm.
		for (ParentRel childRel : children) {

			// Add parent nodes to the query node.
			parentInstanceNodes.put(queryNode.getName(), childRel);

			Attribute childAtt = childRel.getChild();

			if (childRel.getPath().length == 2) {
				// Validate repeated value.
				if (createdNodeNames.contains(createNodeName(indexValue,
						childAtt))) {
					continue;
				}

				// Create a new node.
				ProbabilisticNode childNode = createProbNode(indexValue,
						childAtt, resultNet);

				queryNode.setDescription(queryNode.getDescription() + " "
						+ childRel.getIdRelationsShip());

				// Edge to the child.
				addEdge(resultNet, queryNode, childNode);

				// EVIDENCE
				String specificValue = dbController.getSpecificValue(
						childAtt.getAttribute(),
						new Attribute(childAtt.getTable(), indexCol), ""
								+ indexValue);

				// Get instance value
				evidence.put(childNode, specificValue);

				// Fill with children recursively.
				fillNetworkWithParents(resultNet, childAtt, childNode,
						indexCol, indexValue, specificValue);

				continue;
			} else if (childRel.getPath().length < 2) {
				// A path must be more than 1
				throw new Exception("Invalid rel" + childRel);

			} else {
				Attribute[] path = childRel.getPath();

				// /////////// For external attributes./////////////
				// Get the foreign key with the second element of the path.
				Attribute chilAttInit = path[path.length - 2];

				// DIRECTION local.FK to other.ID or local.ID to other.FK
				boolean directionFKToId = !chilAttInit.getAttribute().equals(
						indexCol);

				// Get the local table with the fist element of the path.
				Table localTable = path[path.length - 1].getTable();

				String initInstanceValue;

				if (directionFKToId) {
					// Get FK value.
					initInstanceValue = dbController.getSpecificValue(
							chilAttInit.getAttribute(), new Attribute(
									localTable, indexCol), String
									.valueOf(indexValue));
				} else {
					initInstanceValue = String.valueOf(indexValue);
				}

				// If it has children.
				if (initInstanceValue == null
						|| initInstanceValue.equalsIgnoreCase("null")) {
					log.debug("Foreign key is null. The recursivity must STOP here for this parent.");
					continue;
				}

				// Get instances.
				String[][] instanceValues = dbController
						.getChildRelatedInstances(childRel, initInstanceValue);

				// relationship in description.
				if (instanceValues.length > 0) {
					queryNode.setDescription(queryNode.getDescription() + " "
							+ childRel.getIdRelationsShip());
				}

				// Create a node for each parent instance.
				for (int i = 0; i < instanceValues.length; i++) {
					// Index for the instance i.
					String afIndex = instanceValues[i][0];
					// Value for the instance i.
					String afValue = instanceValues[i][1];

					// Validate repeated value.
					if (createdNodeNames.contains(createNodeName(afIndex,
							childAtt))) {
						continue;
					}

					// create Node
					ProbabilisticNode childNode = createProbNode(afIndex,
							childAtt, resultNet);

					// Add parent nodes to the query node.
					// parentInstanceNodes.get(childRel).add(childNode);

					// Store evidence.
					evidence.put(childNode, afValue);

					// Edge to the child.
					addEdge(resultNet, queryNode, childNode);

					// Path to the next node
					Column childIndex;

					// Path id to the next node.
					if (directionFKToId) {
						childIndex = childRel.getPath()[1].getAttribute();

					} else {
						Attribute fistAtt = childRel.getPath()[0];

						try {
							childIndex = fistAtt.getTable()
									.getPrimaryKeyColumns()[0];
						} catch (Exception e) {
							throw new Exception(
									"Error trying to load the primary key of "
											+ fistAtt, e);
						}
					}

					// Fill with parents recursively.
					fillNetworkWithParents(resultNet, childAtt, childNode,
							childIndex, afIndex, afValue);
				}
			}
		}
	}

	private void assignDynamicCPT(ProbabilisticNode queryNode,
			Attribute queryAtt) throws Exception {

		ParentRel[] parentRels = prmController.parentsOf(queryAtt);

		List<INode> childNodes = queryNode.getChildNodes();

		// ///////////////// IDENTIFY the right CPT with values ////////////////
		// Potential tables are many when an attribute is a parent and a child.
		PotentialTable[] cptsWithValues = prmController.getCPDs(queryAtt);

		if (cptsWithValues == null) {
			throw new Exception("No CPTs for " + queryAtt);
		}

		// The right CPT depending on if the query node is parent or child.
		PotentialTable rightCptWithValues;

		// Identify if queryNode is a parent or a child.
		// If the node is a parent.
		// FIXME sometimes, parentRelationships!=0, but it is a parent.
		// This (childNodes.size() > 0) is a trick, I am not sure if it always
		// works.
		if (parentRels.length == 0
				|| (childNodes.size() > 0 && queryNode.getParentNodes().size() == 0)) {
			// FIXME it could be other parent.
			rightCptWithValues = (PotentialTable) cptsWithValues[0].clone();
			log.debug("var count=" + rightCptWithValues.variableCount());
			assignCPDToNode(queryNode, rightCptWithValues);
			return;
		} else {
			// if the node is a child
			rightCptWithValues = (PotentialTable) cptsWithValues[cptsWithValues.length - 1]
					.clone();
		}

		// ////////////// FILL THE QUERY NODE CPT /////////////////////
		// TODO Bug multiples parents. could duplicate the instances.
		// CPT parents
		int numCptParents = rightCptWithValues.getVariablesSize();

		PotentialTable tmpTable = rightCptWithValues;

		// Parent instances to apply the aggregate function.
		List<INode> parentNodeInstances = queryNode.getParentNodes();

		// Every CPT parent. the first one is discarded because it is the same
		// attribute.
		for (int level = 0; level < numCptParents - 1; level++) {
			// CPT parent node
			INode parentCptNode = rightCptWithValues.getVariableAt(level + 1);

			log.debug("Parent 1 is discarted because it exists in the default CPT");

			int instanceCont = 0;

			// Find every instance in this level.
			// The fist node is created by default.
			for (int i = 0; i < parentNodeInstances.size(); i++) {
				// Get the number of columns.
				int numColumns = DynamicTableHelper.getNumColumns(tmpTable);

				PotentialTable newTable = (PotentialTable) tmpTable.clone();

				log.debug("Parent " + (i + 1) + " of "
						+ parentNodeInstances.size());

				// FIXME no es i en vez de 0?
				INode parentNodeInstance = parentNodeInstances.get(0);

				String relId = parentNodeInstance.getDescription();
				String cptRelId = parentCptNode.getDescription();
				// if the node is part of this thing.the id reletionship is
				// stored in the node description.
				if (relId.contains(cptRelId)) {
					instanceCont++;
					if (instanceCont <= 1) {
						continue;
					}

					// Get number of states for this variable (parent).
					int numNodeStates = parentNodeInstance.getStatesSize();
					// FIXME it could notnull be necessary because is the same
					// attribute numNodeStates.
					int numLevelStates = rightCptWithValues.getVariableAt(
							level + 1).getStatesSize();

					// Parent relationship.
					ParentRel rel = null;
					for (ParentRel r : parentRels) {
						if (r.getIdRelationsShip().equals(cptRelId)) {
							rel = r;
						}
					}
					if (rel == null) {
						throw new Exception("Relationship not found");
					}

					// Identify the columns related with every state of this
					// variable.
					int[] addedLevel = DynamicTableHelper.addLevel(level,
							newTable, queryNode);

					// Insert the new variable.
					newTable.addVariable(parentNodeInstance);

					for (int st = 0; st < numLevelStates; st++) {
						for (int col = 0; col < numColumns; col++) {
							// For rows
							for (int row = 0; row < numNodeStates; row++) {
								int indxValue = addedLevel[numLevelStates * st
										+ col]
										* numNodeStates + row;
								float col1 = tmpTable.getValue(indxValue);

								indxValue = col * numNodeStates + row;
								float col2 = tmpTable.getValue(indxValue);

								int index = st * numColumns * numNodeStates
										+ col * numNodeStates + row;

								// right relationship

								float afResult = AggregateFunction.calculate(
										rel.getAggregateFunction(), col1, col2);

								// TODO aggregate function
								log.debug("Aggregate fuction " + index + ": ["
										+ col + "," + row + "] for " + col1
										+ " y " + col2 + " Result=" + afResult);
								newTable.setValue(index, afResult);
							}
						}
					}
				}
				tmpTable = newTable;
			}

			// If does not exist any node for this CPT parent.
			if (instanceCont == 0) {
				tmpTable.removeVariable(parentCptNode, true);
			}

			String name = parentCptNode.getName();
			log.debug("Parent cpt variable = " + name);

		}

		// Normalize every column (this could be an external method).
		int newNumColumns = DynamicTableHelper.getNumColumns(tmpTable);
		int numRows = queryNode.getStatesSize(); // FIXME it must be calculated.
		for (int i = 0; i < newNumColumns; i++) {
			// Column sum
			float colSum = 0;
			for (int j = 0; j < numRows; j++) {
				colSum += tmpTable.getValue(i * numRows + j);
			}
			for (int j = 0; j < numRows; j++) {
				tmpTable.setValue(i * numRows + j,
						tmpTable.getValue(i * numRows + j) / colSum);
			}
		}

		// Assign the new CPT.
		assignCPDToNode(queryNode, tmpTable);
	}

	private void addEdge(ProbabilisticNetwork resultNet,
			ProbabilisticNode parentNode, ProbabilisticNode queryNode) {
		try {
			resultNet.addEdge(new Edge(parentNode, queryNode));
		} catch (InvalidParentException e) {
			log.error(e);
		}

	}

	private String createNodeName(Object indexValue, Attribute parentAtt) {
		String nodeName = parentAtt.getTable().getName() + " " + indexValue
				+ " " + parentAtt.getAttribute().getName();
		return nodeName;
	}

	private ProbabilisticNode createProbNode(Object indexValue,
			Attribute attribute, ProbabilisticNetwork resultNet) {
		String nodeName = createNodeName(indexValue, attribute);
		// Possible values
		String[] possibleValues = dbController.getPossibleValues(attribute);

		// New node
		ProbabilisticNode node = createdNodes.get(nodeName);

		if (node != null) {
			return node;
		} else {
			node = new ProbabilisticNode();
			createdNodes.put(nodeName, node);
		}

		// Node position
		int x = (nodePosition % NUM_COLUMNS) * 310 + 20;// Only 5 Columns
		int y = (nodePosition / NUM_COLUMNS) * 150 + 20;
		node.setPosition(x, y);
		nodePosition++;

		node.setName(nodeName);

		// Add node states
		for (String state : possibleValues) {
			// Null value is not added as a new state.
			if (state.equalsIgnoreCase("null")) {
				continue;
			}
			// add state
			node.appendState(state);
		}

		// CPT
		// FIXME it depends on the parents
		PotentialTable cpd = node.getProbabilityFunction();
		cpd.addVariable(node);

		// Add to the network
		resultNet.addNode(node);

		createdNodeNames.add(nodeName);
		createdNodeAtts.put(node, attribute);
		return node;
	}

	/**
	 * This method is required because
	 * 
	 * @param queryNode
	 * @param cpd
	 * @throws Exception
	 */
	private void assignCPDToNode(ProbabilisticNode queryNode, PotentialTable cpd)
			throws Exception {
		int variablesSize = cpd.tableSize();
		PotentialTable probabilityFunction = queryNode.getProbabilityFunction();

		// Variable

		try {
			for (int i = 0; i < variablesSize; i++) {
				probabilityFunction.setValue(i, cpd.getValue(i));
			}

		} catch (IndexOutOfBoundsException e) {
			throw new Exception("Error assigning the CPT for "
					+ queryNode.getName() + ". VariableSize=" + variablesSize
					+ " and CPT size=" + probabilityFunction.tableSize(), e);
		}

	}

	public IInferenceAlgorithm getInferenceAlgorithm() {
		return inferenceAlgorithm;
	}
}
