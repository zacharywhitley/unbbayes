package unbbayes.prm.controller.prm;

import java.util.HashMap;
import java.util.HashSet;
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

		// Resultant network
		ProbabilisticNetwork resultNet = networkBuilder
				.buildNetwork("RESULTANT BN");

		// //// Create a node for the instance /////
		Attribute queryAtt = new Attribute(t, column);
		String[] possibleValues = dbController.getPossibleValues(queryAtt);

		// Probabilistic node
		ProbabilisticNode queryNode = createProbNode(indexValue + "-"
				+ queryAtt.getAttribute().getName(), possibleValues);
		resultNet.addNode(queryNode);

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

			// ////////// For intrinsic attributes ////////////////
			// If the parent is intrinsic, it only has two attributes in the
			// path, because it does not have foreign keys.
			if (parentRel.getPath().length == 2) {
				// Possible values
				String[] possibleValues = dbController
						.getPossibleValues(parentRel.getParent());

				// Create a new node.
				String nodeName = indexValue + "-"
						+ parentRel.getParent().getAttribute().getName();
				ProbabilisticNode parentNode = createProbNode(nodeName,
						possibleValues);
				resultNet.addNode(parentNode);

				// EVIDENCE
				String specificValue = dbController.getSpecificValue(parentRel
						.getParent().getAttribute(), new Attribute(parentRel
						.getParent().getTable(), indexCol), "" + indexValue);

				// Get instance value
				evidence.put(parentNode, specificValue);

				// Edge to the child.
				try {
					resultNet.addEdge(new Edge(parentNode, queryNode));
				} catch (InvalidParentException e) {
					log.error(e);
				}

				// / CPT Table
				PotentialTable pt = prmController.getCPD(parentRel.getParent());

				if (pt == null) {
					throw new Exception("Attribute "
							+ parentRel.getParent().getTable().getName() + "."
							+ parentRel.getParent().getAttribute().getName()
							+ " does not have an associated CPT");
				}

				assignCPDToNode(parentNode, pt);

				//
				continue;
			}

			// /////////// For external attributes./////////////7
			// Get the foreign key with the second element of the path.
			Attribute fkAttribute = parentRel.getPath()[1];
			// Get the local table with the fist element of the path.
			Table localTable = parentRel.getPath()[0].getTable();

			// Get FK value.
			String fkInstanceValue = dbController.getSpecificValue(fkAttribute
					.getAttribute(), new Attribute(localTable, indexCol),
					String.valueOf(indexValue));

			// If it has parents
			if (fkInstanceValue != null
					&& !fkInstanceValue.equalsIgnoreCase("null")) {

				// Get instances.
				String[][] instanceValues = dbController.getRelatedInstances(
						parentRel, fkInstanceValue);

				// Result of applying the aggregate function;
				String[] afResult = applyAggregateFunction(instanceValues,
						parentRel.getAggregateFunction());

				String afIndex = afResult[0];
				String afValue = afResult[1];

				// A node for each Instance.
				// for (int i = 0; i < instanceValues.length; i++) {
				String[] possibleValues = dbController
						.getPossibleValues(parentRel.getParent());

				// Node name based on the index value.
				String indexNameValue = afIndex != null ? afIndex + "-" : "";
				ProbabilisticNode parentNode = createProbNode(indexNameValue
						+ parentRel.getParent().getAttribute().getName(),
						possibleValues);
				resultNet.addNode(parentNode);

				// Store evidence.
				// FIXME Aggregate function is required.
				evidence.put(parentNode, afValue);

				// Edge to the child.
				try {
					resultNet.addEdge(new Edge(parentNode, queryNode));
				} catch (InvalidParentException e) {
					log.error(e);
				}

				// // CPT ////
				// If the node parent is the same child.
				if (parentRel.getParent().equals(parentRel.getChild())) {
					assignCPDToNode(parentNode, cpDs[cpdIndex++]);
				} else {
					PotentialTable pt = prmController.getCPD(parentRel
							.getParent());
					if (pt == null) {
						throw new Exception("Attribute "
								+ parentRel.getParent().getTable().getName()
								+ "."
								+ parentRel.getParent().getAttribute()
										.getName()
								+ " does not have an associated CPT");
					}

					assignCPDToNode(parentNode, pt);
				}

				Column parentIndex = parentRel.getPath()[parentRel.getPath().length - 2]
						.getAttribute();

				// Fill with parents recursively.
				fillNetworkWithParents(resultNet, parentRel.getParent(),
						parentNode, parentIndex, afIndex, afValue);
			}
		}
		// CPD for child
		assignCPDToNode(queryNode, cpDs[cpdIndex++]);
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
		return instanceValues[0];
	}

	/**
	 * This method is required because
	 * 
	 * @param queryNode
	 * @param cpd
	 */
	private void assignCPDToNode(ProbabilisticNode queryNode, PotentialTable cpd) {
		// int variablesSize = cpd.getValues().length;
		// PotentialTable probabilityFunction =
		// queryNode.getProbabilityFunction();
		//
		// // Variable
		// for (int i = 0; i < variablesSize; i++) {
		// probabilityFunction.addValueAt(i, cpd.getValue(i));
		// }
		queryNode.setProbabilityFunction((ProbabilisticTable) cpd);
	}

	private ProbabilisticNode createProbNode(String nodeName,
			String[] possibleValues) {
		ProbabilisticNode node = new ProbabilisticNode();

		// Node position
		int x = (nodePosition % NUM_COLUMNS) * 320 + 20;// Only 5 Columns
		int y = (nodePosition / NUM_COLUMNS) * 150 + 20;
		node.setPosition(x, y);
		nodePosition++;

		
		// Node name must be unique.
		while (!nodeNames.add(nodeName)) {
			String substring = nodeName.substring(nodeName.length() - 1);
			try {
				int cont = Integer.parseInt(substring) + 1;
				nodeName = nodeName.substring(0, nodeName.length() - 2) + "_"
						+ cont;
			} catch (Exception e) {
				nodeName = nodeName + "_1";
			}

		}

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

		return node;
	}

	public IInferenceAlgorithm getInferenceAlgorithm() {
		return inferenceAlgorithm;
	}
}
