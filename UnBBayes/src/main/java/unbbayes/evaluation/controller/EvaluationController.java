package unbbayes.evaluation.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import unbbayes.evaluation.Evaluation;
import unbbayes.evaluation.exception.EvaluationException;
import unbbayes.evaluation.gui.EvaluationPane;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;

public class EvaluationController {
	
	private EvaluationPane evaluationPane;
	private ProbabilisticNetwork network;
	private Evaluation evaluation;
	
	public EvaluationController(ProbabilisticNetwork network) {
		this.network = network;
		this.evaluationPane = new EvaluationPane();
		this.evaluation = new Evaluation();
		
		setUpEvaluation();
	}
	
	public void setUpEvaluation() {
		List<String> nodeNameList = new ArrayList<String>();
		List<Node> nodeList = network.getNodes();
		for (Node node : nodeList) {
			nodeNameList.add(node.getName());
		}
		evaluationPane.fillNodeList(nodeNameList);
		
		evaluationPane.setRunBtnActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				runEvaluation();
			}

		});
	}
	
	private void validateData() throws EvaluationException {
		StringBuffer errorMsg = new StringBuffer();
		
		try {
			Integer sampleSize = Integer.valueOf(evaluationPane.getSampleSizeText());
			if (sampleSize <= 0) {
				errorMsg.append("Sample size has to be greater than 0. \n");
			}
		} catch (NumberFormatException e) {
			errorMsg.append("Sample size has to be greater than 0. \n");
		}
		
		if (evaluationPane.getTargetNodeNameList().size() == 0) {
			errorMsg.append("There must be at least one target node selected. \n");
		}
		
		if (evaluationPane.getEvidenceNodeNameList().size() == 0) {
			errorMsg.append("There must be at least one evidence node selected. \n");
		}
		
		if (errorMsg.toString().length() > 0) {
			throw new EvaluationException(errorMsg.toString());
		}
			
	}
	
	private void runEvaluation() {
		try {
			validateData();
		} catch (EvaluationException e) {
			JOptionPane.showMessageDialog(evaluationPane, e.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<String> targetNodeNameList = evaluationPane.getTargetNodeNameList();
		List<String> evidenceNodeNameList = evaluationPane.getEvidenceNodeNameList();
		Integer sampleSize = Integer.valueOf(evaluationPane.getSampleSizeText());
		
		try {
			String output = evaluation.evaluate(network, targetNodeNameList, evidenceNodeNameList, sampleSize);
			evaluationPane.setOutputText(output);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(evaluationPane, e.getMessage(), "Evaluation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public JPanel getView() {
		return evaluationPane;
	}

}
