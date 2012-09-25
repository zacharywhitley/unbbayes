package unbbayes.prm.controller.prm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.log4j.Logger;

import unbbayes.prm.controller.dao.IDBController;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.model.ParentRel;
import unbbayes.prm.util.helper.DBSchemaHelper;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

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
			Column column, Object value) throws Exception {
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

		// Update evidences.
		// try {
		// resultNet.updateEvidences();
		// } catch (Exception e) {
		// log.error(e);
		// }

		// prepare the algorithm to compile network
		runAlgorithm(resultNet);

		return resultNet;
	}

	private void runAlgorithm(Graph resultNet) {
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
		algorithm.setNetwork(resultNet);
		algorithm.run();

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

		// Get the parents
		ParentRel[] parents = prmController.parentsOf(queryAtt);

		// when the
		PotentialTable[] cpDs = prmController.getCPDs(queryAtt);
		if (cpDs == null) {
			throw new Exception("Attribute " + queryAtt.getTable().getName()
					+ "." + queryAtt.getAttribute().getName()
					+ " does not have an associated CPT");
		}

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
			String[][] instanceValues = dbController.getRelatedInstances(
					parentRel, fkInstanceValue);

			// A node for each Instance.
			// for (int i = 0; i < instanceValues.length; i++) {
			String[] possibleValues = dbController.getPossibleValues(parentRel
					.getParent());

			// FIXME revisar que está con el primer valor de instanceValues.
			ProbabilisticNode parentNode = createProbNode(instanceValues[0][0]
					+ "-" + parentRel.getParent().getAttribute().getName(),
					possibleValues);
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
				cpdIndex++;
			} else {
				PotentialTable pt = prmController.getCPD(parentRel.getParent());
				if (pt == null) {
					throw new Exception("Attribute "
							+ parentRel.getParent().getTable().getName() + "."
							+ parentRel.getParent().getAttribute().getName()
							+ " does not have an associated CPT");
				}

				assignCPDToNode(parentNode, pt);
			}

			// ///////////////////////////// REVISAR COMO AGREGAR EVIDENCIA
			// PORQUE ESTA SALIENDO NULL.
			// TODO evidence and aggregate functions.
			 runAlgorithm(resultNet);
			//
			// parentNode.addFinding(0);
			//
			// // Insert likelihood
			// float likelihood[] = new float[parentNode.getStatesSize()];
			//
			// for (int i = 0; i < likelihood.length; i++) {
			// // FIXME Only the fist evidence. instanceValues[0][1]
			// if (instanceValues[0][1] != null) {
			// likelihood[i] = possibleValues[i]
			// .equals(instanceValues[0][1]) ? 1 : 0;
			// }
			// }
			// parentNode.addLikeliHood(likelihood);

			// Parents
			// Actually this is searching the fist instance value.
			// fillNetworkWithParents(resultNet, parentRel.getParent(),
			// parentNode, indexCol, indexValue, instanceValues[0]);
			// }

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
