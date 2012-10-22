package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.AggregateFunctionName;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
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

	private static final int NUM_COLUMNS = 4;
	private int nodePosition;

	/**
	 * Node names to do not repeat.
	 */
	private Set<String> nodeNames;

	private HashMap<ProbabilisticNode, String> evidence;
	private IInferenceAlgorithm inferenceAlgorithm;
	private List<ParentRel> compiledRels;
	private Hashtable<String, ProbabilisticNode> createdNodes;

	public PrmCompiler(IPrmController prmController, IDBController dbController) {
		this.prmController = prmController;
		this.dbController = dbController;
		networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
	}

	public Graph compile(Table t, Column uniqueIndexColumn, Object indexValue,
			Column column, Object value) throws Exception {
		nodeNames = new HashSet<String>();
		// Clear evidence
		evidence = new HashMap<ProbabilisticNode, String>();
		nodePosition = 0;
		compiledRels = new ArrayList<ParentRel>();
		createdNodes = new Hashtable<String, ProbabilisticNode>();

		// Resultant network
		ProbabilisticNetwork resultNet = networkBuilder
				.buildNetwork("RESULTANT BN");

		// //// Create a node for the instance /////
		Attribute queryAtt = new Attribute(t, column);

		// Probabilistic node
		ProbabilisticNode queryNode = createProbNode(
				String.valueOf(indexValue), queryAtt, resultNet);

		// /// Evidence for the first node ///
		String specificValue = dbController.getSpecificValue(column,
				new Attribute(t, uniqueIndexColumn), "" + indexValue);
		// Get instance value
		evidence.put(queryNode, specificValue);

		// ///// Create nodes for the parents //////
		fillNetworkWithParents(resultNet, queryAtt, queryNode,
				uniqueIndexColumn, indexValue, value);

		inferenceAlgorithm = new JunctionTreeAlgorithm();
		inferenceAlgorithm.setNetwork(resultNet);
		inferenceAlgorithm.run();
		inferenceAlgorithm.reset();

		// Update evidences.
		addEvidences(resultNet);
		inferenceAlgorithm.propagate();

		return resultNet;
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

		// Get the parent relationships of the probabilistic model.
		ParentRel[] parents = prmController.parentsOf(queryAtt);

		// CPTs for the query attribute.
		PotentialTable[] cpDs = prmController.getCPDs(queryAtt);
		if (cpDs == null) {
			throw new Exception("Attribute " + queryAtt.getTable().getName()
					+ "." + queryAtt.getAttribute().getName()
					+ " does not have an associated CPT");
		}
		// This index is used when the parent is the same child.
		int cpdIndex = 0;

		// Find parent instances for each parent relationship.
		for (ParentRel parentRel : parents) {
			Attribute parentAtt = parentRel.getParent();
			compiledRels.add(parentRel);

			// ////////// For intrinsic attributes ////////////////
			// If the parent is intrinsic, it only has two attributes in the
			// path, because it does not have foreign keys.
			if (parentRel.getPath().length == 2) {

				// Create a new node.
				ProbabilisticNode parentNode = createProbNode(indexValue,
						parentAtt, resultNet);

				// Edge to the child.
				addEdge(resultNet, parentNode, queryNode);

				// CPT Table
				PotentialTable pt = getCPT(parentAtt);
				assignCPDToNode(parentNode, pt);

				// EVIDENCE
				addEvidenceToNode(parentAtt, indexCol, indexValue, parentNode);

				//
				continue;
			}

			// /////////// For external attributes./////////////
			// Get the foreign key with the second element of the path.
			Attribute fkAttribute = parentRel.getPath()[1];
			// Get the local table with the fist element of the path.
			Table localTable = parentRel.getPath()[0].getTable();

			// Get FK value.
			String fkInstanceValue = dbController.getSpecificValue(fkAttribute
					.getAttribute(), new Attribute(localTable, indexCol),
					String.valueOf(indexValue));

			// If it has parents
			if (fkInstanceValue == null
					|| fkInstanceValue.equalsIgnoreCase("null")) {
				continue;
			}

			// Get instances.
			String[][] instanceValues = dbController.getParentRelatedInstances(
					parentRel, fkInstanceValue);

			// Result of applying the aggregate function;
			String[] afResult = applyAggregateFunction(instanceValues,
					parentRel.getAggregateFunction());

			String afIndex = afResult[0];
			String afValue = afResult[1];

			// create Node
			ProbabilisticNode parentNode = createProbNode(afIndex, parentAtt,
					resultNet);

			// Store evidence.
			evidence.put(parentNode, afValue);

			// Edge to the child.
			addEdge(resultNet, parentNode, queryNode);

			// // CPT ////
			// If the node parent is the same child.
			if (parentAtt.equals(parentRel.getChild())) {
				assignCPDToNode(parentNode, cpDs[cpdIndex++]);
			} else {
				PotentialTable pt = getCPT(parentAtt);
				assignCPDToNode(parentNode, pt);
			}

			// Path to the next node
			Column parentIndex = parentRel.getPath()[parentRel.getPath().length - 2]
					.getAttribute();

			// Fill with parents recursively.
			fillNetworkWithParents(resultNet, parentAtt, parentNode,
					parentIndex, afIndex, afValue);

		}
		// CPD for child/query node.
		assignCPDToNode(queryNode, cpDs[cpdIndex++]);

		ParentRel[] children = prmController.childrenOf(queryAtt);

		// For each child a new node is created and the recursive algorithm.
		for (ParentRel childRel : children) {
			Attribute childAtt = childRel.getChild();
			// Validate if this relationship has been created before.
			if (compiledRels.contains(childRel)) {
				continue;
			} else {
				compiledRels.add(childRel);
			}

			// /////////// For external attributes./////////////
			// Get the foreign key with the second last element of the path.
			int pLength = childRel.getPath().length;
			Attribute fkAttribute = childRel.getPath()[pLength - 2];
			// Get the local table with the last element of the path.
			Table localTable = childRel.getPath()[pLength - 1].getTable();

			// Instance index.
			String afIndex;
			// Specific attribute value.
			String afValue;

			String fkInstanceValue = dbController.getSpecificValue(fkAttribute
					.getAttribute(), new Attribute(localTable, indexCol),
					String.valueOf(indexValue));

			// If pLength==2 is because the path is to the same table (intrinsic
			// relationship). Else is because the path requires navigation by
			// the foreign keys.
			if (pLength == 2) {
				afValue = fkInstanceValue;
				afIndex = String.valueOf(indexValue);
			} else {

				// If it has children
				// pLength > 2 because it could be a local path without
				// evidence.
				if ((fkInstanceValue == null || fkInstanceValue
						.equalsIgnoreCase("null"))) {
					continue;
				}
				// Child instances
				// FIXME it does not work for children
				String[][] instanceValues = dbController
						.getParentRelatedInstances(childRel, fkInstanceValue);
				// Result of applying the aggregate function;
				String[] afResult = applyAggregateFunction(instanceValues,
						childRel.getAggregateFunction());

				afIndex = afResult[0];
				afValue = afResult[1];
			}

			// Create the node
			ProbabilisticNode childNode = createProbNode(afIndex, childAtt,
					resultNet);

			// Edge to the child.
			addEdge(resultNet, queryNode, childNode);

			// Create the CPT
			// XXX be careful with this cpt
			PotentialTable pt = getCPT(childAtt);
			assignCPDToNode(childNode, pt);

			// Evidence
			evidence.put(childNode, afValue);

			// Apply recursively
			// Path to the next node
			Column parentIndex = childRel.getPath()[1].getAttribute();

			// Fill with parents recursively.
			fillNetworkWithParents(resultNet, childAtt, childNode, parentIndex,
					afIndex, afValue);
		}
	}

	private PotentialTable getCPT(Attribute parentAtt) throws Exception {
		PotentialTable pt = prmController.getCPD(parentAtt);
		if (pt == null) {
			throw new Exception("Attribute " + parentAtt
					+ " does not have an associated CPT");
		}
		return pt;
	}

	private void addEdge(ProbabilisticNetwork resultNet,
			ProbabilisticNode parentNode, ProbabilisticNode queryNode) {
		try {
			resultNet.addEdge(new Edge(parentNode, queryNode));
		} catch (InvalidParentException e) {
			log.error(e);
		}

	}

	private ProbabilisticNode createProbNode(Object indexValue,
			Attribute parentAtt, ProbabilisticNetwork resultNet) {
		String nodeName = parentAtt.getTable().getName() + " " + indexValue
				+ "-" + parentAtt.getAttribute().getName();
		// Possible values
		String[] possibleValues = dbController.getPossibleValues(parentAtt);

		// New node
		ProbabilisticNode node = createdNodes.get(nodeName);

		if (node != null) {
			return node;
		} else {
			node = new ProbabilisticNode();
			createdNodes.put(nodeName, node);
		}

		// Node position
		int x = (nodePosition % NUM_COLUMNS) * 320 + 20;// Only 5 Columns
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
		PotentialTable cpd = node.getProbabilityFunction();
		cpd.addVariable(node);

		// Add to the network
		resultNet.addNode(node);

		return node;
	}

	private void addEvidenceToNode(Attribute parentAtt, Column indexCol,
			Object indexValue, ProbabilisticNode parentNode) {
		String specificValue = dbController.getSpecificValue(parentAtt
				.getAttribute(), new Attribute(parentAtt.getTable(), indexCol),
				"" + indexValue);

		// Get instance value
		evidence.put(parentNode, specificValue);
	}

	/**
	 * Apply aggregate function
	 * 
	 * @param instanceValues
	 * @param aggregateFuction
	 * @return an array with length=2. The fist element is the id and the second
	 *         is the value.
	 */
	private String[] applyAggregateFunction(String[][] instanceValues,
			AggregateFunctionName aggregateFuction) {
		// TODO Aggregate function is required.
		return instanceValues[0];
	}

	/**
	 * This method is required because
	 * 
	 * @param queryNode
	 * @param cpd
	 */
	private void assignCPDToNode(ProbabilisticNode queryNode, PotentialTable cpd) {
		// If it exist
		float[] vals = queryNode.getProbabilityFunction().getValues();
		if (vals.length == 0) {
			log.debug("new cpt for node");
			return;
		} else {
			log.debug("old node");
		}

		int variablesSize = cpd.getValues().length;
		PotentialTable probabilityFunction = queryNode.getProbabilityFunction();

		// Variable
		for (int i = 0; i < variablesSize; i++) {
			probabilityFunction.setValue(i, cpd.getValue(i));
		}
		// queryNode.setProbabilityFunction((ProbabilisticTable) cpd);
	}

	public IInferenceAlgorithm getInferenceAlgorithm() {
		return inferenceAlgorithm;
	}
}
