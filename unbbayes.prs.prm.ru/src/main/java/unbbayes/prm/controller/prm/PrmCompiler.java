package unbbayes.prm.controller.prm;

import java.util.HashSet;
import java.util.Set;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.exception.InvalidParentException;

public class PrmCompiler {
	Logger log = Logger.getLogger(PrmCompiler.class);
	private IProbabilisticNetworkBuilder networkBuilder;
	private IPrmController prmController;
	private IDBController dbController;

	/**
	 * Node names to do not repeat.
	 */
	private Set<String> nodeNames;

	public PrmCompiler(IPrmController prmController, IDBController dbController) {
		this.prmController = prmController;
		this.dbController = dbController;
		networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
	}

	public Graph compile(Table t, Column uniqueIndexColumn, Object indexValue,
			Column column, Object value) {
		nodeNames = new HashSet<String>();
		// Resultant network
		ProbabilisticNetwork resultNet = networkBuilder
				.buildNetwork("RESULTANT BN");

		// //// Create a node for the instance /////
		Attribute queryAtt = new Attribute(t, column);
		String[] possibleValues = dbController.getPossibleValues(queryAtt);

		// Probabilistic node
		ProbabilisticNode queryNode = createProbNode(queryAtt.getAttribute()
				.getName(), possibleValues);
		resultNet.addNode(queryNode);

		// ///// Create nodes for the parents //////
		fillNetworkWithParents(resultNet, queryAtt, queryNode,
				uniqueIndexColumn, indexValue, value);

		// TODO Aggregate function.

		return resultNet;
	}

	/**
	 * 
	 * @param resultNet
	 * @param queryAtt
	 * @param queryNode
	 * @param indexValue
	 * @param value
	 */
	private void fillNetworkWithParents(ProbabilisticNetwork resultNet,
			Attribute queryAtt, ProbabilisticNode queryNode, Column indexCol,
			Object indexValue, Object value) {

		// Get the parents
		ParentRel[] parents = prmController.parentsOf(queryAtt);

		// when the
		PotentialTable[] cpDs = prmController.getCPDs(queryAtt);
		int cpdIndex = 0;

		// Find parent instances
		for (ParentRel parentRel : parents) {
			Attribute fkAttribute = parentRel.getPath()[1];
			Table localTable = parentRel.getPath()[0].getTable();

			// Get FK value.
			String fkInstanceValue = dbController.getSpecificValue(fkAttribute
					.getAttribute(), new Attribute(localTable, indexCol),
					String.valueOf(indexValue));

			// Get instances.
			String[] instanceValues = dbController.getRelatedInstances(
					parentRel, fkInstanceValue);

			// A node for each Instance.
			for (int i = 0; i < instanceValues.length; i++) {
				String[] possibleValues = dbController
						.getPossibleValues(parentRel.getParent());
				ProbabilisticNode parentNode = createProbNode(parentRel
						.getParent().getAttribute().getName(), possibleValues);
				resultNet.addNode(parentNode);

				// Edge to the child
				try {
					resultNet.addEdge(new Edge(parentNode, queryNode));
				} catch (InvalidParentException e) {
					log.error(e);
				}

				// // CPT ////
				// If the node parent is the same child.
				if (parentRel.getParent().equals(parentRel.getChild())) {
					assignCPDToNode(parentNode, cpDs[cpdIndex]);
				} else {
					PotentialTable pt = prmController.getCPD(parentRel
							.getParent());
					assignCPDToNode(parentNode, pt);
				}

				// TODO Evidence.
			}

			cpdIndex++;

			// TODO parents of parents
		}
		// CPD for child
		assignCPDToNode(queryNode, cpDs[cpdIndex++]);
	}

	/**
	 * This method is required because
	 * 
	 * @param queryNode
	 * @param cpd
	 */
	private void assignCPDToNode(ProbabilisticNode queryNode, PotentialTable cpd) {
		int variablesSize = cpd.getVariablesSize();
		PotentialTable probabilityFunction = queryNode.getProbabilityFunction();
		for (int i = 0; i < variablesSize; i++) {
			probabilityFunction.addValueAt(i, cpd.getValue(i));
		}
	}

	private ProbabilisticNode createProbNode(String nodeName,
			String[] possibleValues) {
		ProbabilisticNode node = new ProbabilisticNode();

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
			node.appendState(state);
		}

		// CPT
		PotentialTable cpd = node.getProbabilityFunction();
		cpd.addVariable(node);

		return node;
	}

}
