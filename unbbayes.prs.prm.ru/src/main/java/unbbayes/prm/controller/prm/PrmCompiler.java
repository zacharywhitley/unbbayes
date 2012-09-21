package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.List;

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
	private List<String> nodeNames;

	public PrmCompiler(IPrmController prmController, IDBController dbController) {
		this.prmController = prmController;
		this.dbController = dbController;
		networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
	}

	public Graph compile(Table t, Column uniqueIndexColumn, Object indexValue,
			Column column, Object value) {
		nodeNames = new ArrayList<String>();
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

		// Agregate function.
		// Create nodes for the children.

		// / CPT
		// CPT for query node.
		// PotentialTable[] cpd = prmController.getCPDs(queryAtt);
		// if (cpd == null) {
		// // TODO throw error.
		// }
		// assignCPDToNode(queryNode, cpd);
		// TODO CPD table

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

			// PotentialTable[] cpDs =
			// prmController.getCPDs(parentRel.getChild());
			// int cpdIndex = 0;

			// A node for each Instance.
			for (int i = 0; i < instanceValues.length; i++) {
				String[] possibleValues = dbController
						.getPossibleValues(parentRel.getParent());
				ProbabilisticNode parentNode = createProbNode(parentRel
						.getParent().getAttribute().getName()
						+ "_" + i, possibleValues);
				resultNet.addNode(parentNode);

				// Edge to the child
				try {
					resultNet.addEdge(new Edge(parentNode, queryNode));
				} catch (InvalidParentException e) {
					log.error(e);
				}

				// // CPT ////

				if (parentRel.getParent().equals(parentRel.getChild())) {

				}

				// TODO parents of parents

			}
		}
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

		// Node name
		while (nodeNames.contains(nodeName)) {
			String substring = nodeName.substring(nodeName.length() - 2);
			try {
				int cont = Integer.getInteger(substring) + 1;
				nodeName = nodeName.substring(0, nodeName.length() - 2) + cont;
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
