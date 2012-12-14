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
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
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
 * data base instances. It is the implemantation of Probabilistic Relational
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
	private Hashtable<String, ProbabilisticNode> createdNodes;

	private Hashtable<ProbabilisticNode, Attribute> createdNodeAtts;

	Hashtable<ParentRel, List<ProbabilisticNode>> parentInstanceNodes;

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
		parentInstanceNodes = new Hashtable<ParentRel, List<ProbabilisticNode>>();

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
			
			if (parentInstanceNodes.get(parentRel) == null) {
				parentInstanceNodes.put(parentRel,
						new ArrayList<ProbabilisticNode>());
			}
			
			
			Attribute parentAtt = parentRel.getParent();

			// ////////// For intrinsic attributes ////////////////
			// If the parent is intrinsic, it only has two attributes in the
			// path, because it does not have foreign keys and the relationship
			// is in the same instance.
			if (parentRel.getPath().length == 2) {
				// Create a new node.
				ProbabilisticNode parentNode = createProbNode(indexValue,
						parentAtt, resultNet);

				// Edge to the child.
				addEdge(resultNet, parentNode, queryNode);

				// Verify consistency for the new node.
				// try {
				// ((ProbabilisticTable) parentNode.getProbabilityFunction())
				// .verifyConsistency();
				// } catch (Exception e) {
				// throw new Exception(
				// "Error verifying consistency for the node "
				// + parentNode.getName() + " ", e);
				// }

				// EVIDENCE
				String specificValue = dbController.getSpecificValue(
						parentAtt.getAttribute(),
						new Attribute(parentAtt.getTable(), indexCol), ""
								+ indexValue);

				// Get instance value
				evidence.put(parentNode, specificValue);

				// Add parent nodes to the query node.
				parentInstanceNodes.get(parentRel).add(parentNode);

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
					log.debug("Foreign key is null. The recursivity must STOP here for this parent.");
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

					// create Node
					ProbabilisticNode parentNode = createProbNode(afIndex,
							parentAtt, resultNet);

					
					// Add parent nodes to the query node.
					parentInstanceNodes.get(parentRel).add(parentNode);

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
			Attribute childAtt = childRel.getChild();
			// Validate if this relationship has been created before.
			// if (compiledRels.contains(childRel)) {
			// continue;
			// } else {
			// compiledRels.add(childRel);
			// }

			if (childRel.getPath().length == 2) {
				// Validate repeated value.
				if (createdNodeNames.contains(createNodeName(indexValue,
						childAtt))) {
					continue;
				}
				
				// Create a new node.
				ProbabilisticNode childNode = createProbNode(indexValue,
						childAtt, resultNet);

				// Edge to the child.
				addEdge(resultNet, queryNode, childNode);

				// Verify consistency for the new node.
				// try {
				// ((ProbabilisticTable) childNode.getProbabilityFunction())
				// .verifyConsistency();
				// } catch (Exception e) {
				// throw new Exception(
				// "Error verifying consistence for the node "
				// + childNode.getName() + " ", e);
				// }

				// EVIDENCE
				String specificValue = dbController.getSpecificValue(
						childAtt.getAttribute(),
						new Attribute(childAtt.getTable(), indexCol), ""
								+ indexValue);

				// Get instance value
				evidence.put(childNode, specificValue);

				// Add parent nodes to the query node.
				parentInstanceNodes.get(childRel).add(childNode);

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

				// If it has childs.
				if (initInstanceValue == null
						|| initInstanceValue.equalsIgnoreCase("null")) {
					log.debug("Foreign key is null. The recursivity must STOP here for this parent.");
					continue;
				}

				// Get instances.
				String[][] instanceValues = dbController
						.getChildRelatedInstances(childRel, initInstanceValue);

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
					Column parentIndex = childRel.getPath()[1].getAttribute();
					// createdInstances.add(new InstanceRelationship(
					// Fill with parents recursively.
					fillNetworkWithParents(resultNet, childAtt, childNode,
							parentIndex, afIndex, afValue);
				}
			}
		}
	}

	private void assignDynamicCPT(ProbabilisticNode queryNode,
			Attribute queryAtt)
			throws Exception {

		// CPTs for the query attribute.
		int cptRows = queryNode.getStatesSize();
		int cptCols = 1;

		// ////// CPD for child/query node.////////
		Enumeration<ParentRel> keys = parentInstanceNodes.keys();
		
		while (keys.hasMoreElements()) {
			ParentRel parentRel = (ParentRel) keys.nextElement();
			
			// Only for children.
			if(parentRel.getChild().equals(queryAtt)){
				List<ProbabilisticNode> list = parentInstanceNodes.get(parentRel);
				
				for (ProbabilisticNode pn : list) {
					cptCols *= pn.getStatesSize();
				}
			}
		}
		
		
		log.debug("CPT for " + queryNode.getName() + " cols: " + cptCols
				+ " rows: " + cptRows);

		// Get the saved CPTs.
		PotentialTable[] cpDs = prmController.getCPDs(queryAtt);
		if (cpDs == null) {
			throw new Exception("Attribute " + queryAtt.getTable().getName()
					+ "." + queryAtt.getAttribute().getName()
					+ " does not have an associated CPT");
		}


		// TODO CREATE A DYNAMIC CPT
		// assignCPDToNode(queryNode, cpDs[cpdIndex]);
		// int variablesSize = cpDs[cpdIndex].getValues().length;
		PotentialTable probabilityFunction = queryNode.getProbabilityFunction();

		int valueCptCounter = 0;
		// Variable
		for (int c = 0; c < cptCols; c++) {
			for (int r = 0; r < cptRows; r++) {
				probabilityFunction.setValue(valueCptCounter, 1f / cptRows);
				valueCptCounter++;
			}
		}

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
	 */
	private void assignCPDToNode(ProbabilisticNode queryNode, PotentialTable cpd) {
		// If it exist
		// float[] vals = queryNode.getProbabilityFunction().getValues();
		// for (float f : vals) {
		// if (f > 0) {
		// return;
		// }
		// }
		// if (vals.length == 0) {
		// log.debug("new cpt for node");
		// return;
		// } else {
		// log.debug("old node");
		// }
		// int variablesSize2 = cpd.getVariablesSize();

		int variablesSize = cpd.getValues().length;
		PotentialTable probabilityFunction = queryNode.getProbabilityFunction();

		// Variable
		for (int i = 0; i < variablesSize; i++) {
			probabilityFunction.setValue(i, cpd.getValue(i));
		}
	}

	public IInferenceAlgorithm getInferenceAlgorithm() {
		return inferenceAlgorithm;
	}
}
