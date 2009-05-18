/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.evaluation.controller;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import unbbayes.evaluation.EvidenceEvaluation;
import unbbayes.evaluation.FastLWApproximateEvaluation;
import unbbayes.evaluation.IEvaluation;
import unbbayes.evaluation.exception.EvaluationException;
import unbbayes.evaluation.gui.EvaluationPane;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

public class EvaluationController {
	
	private EvaluationPane evaluationPane;
	private ProbabilisticNetwork network;
	private IEvaluation evaluation;
	
	public EvaluationController(ProbabilisticNetwork network) {
		this.network = network;
		this.evaluationPane = new EvaluationPane();
		
		setUpEvaluation();
	}
	
	public void setUpEvaluation() {
		List<Node> nodeList = network.getNodes();
		
		Map<String, List<String>> nodeFindingMap = new HashMap<String, List<String>>();
		List<String> findingList;
		for (Node node : nodeList) {
			findingList = new ArrayList<String>(node.getStatesSize());
			for (int i = 0; i < node.getStatesSize(); i++) {
				findingList.add(node.getStateAt(i));
			}
			nodeFindingMap.put(node.getName(), findingList);
		}
		
		evaluationPane.addInputValues(nodeFindingMap);
		
		evaluationPane.setRunButtonActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				runEvaluation();
			}

		});
	}
	
	private void validateData() throws EvaluationException {
		StringBuffer errorMsg = new StringBuffer();
		
		try {
			Integer sampleSize = evaluationPane.getSampleSizeValue();
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
		evaluationPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		try {
			validateData();
		} catch (EvaluationException e) {
			JOptionPane.showMessageDialog(evaluationPane, e.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<String> targetNodeNameList = evaluationPane.getTargetNodeNameList();
		List<String> evidenceNodeNameList = evaluationPane.getEvidenceNodeNameList();
		int sampleSize = evaluationPane.getSampleSizeValue();
		
		Map<String, String> nodeFindingMap = evaluationPane.getNodeFindingMap();
		ProbabilisticNode node;
		for (String nodeName : nodeFindingMap.keySet()) {
			node = (ProbabilisticNode)network.getNode(nodeName);
			for (int i = 0; i < node.getStatesSize(); i++) {
				if (nodeFindingMap.get(nodeName).equals(node.getStateAt(i))) {
					node.addFinding(i);
				}
			}
		}
		
		try {
			evaluation = new FastLWApproximateEvaluation(sampleSize);
			evaluation.evaluate(network, targetNodeNameList, evidenceNodeNameList, false);
			
			List<EvidenceEvaluation> evidenceEvaluationList = evaluation.getBestMarginalImprovement();
			
			for (EvidenceEvaluation evidenceEvaluation : evidenceEvaluationList) {
				evidenceEvaluation.setCost(evaluationPane.getCost(evidenceEvaluation.getName()));
			}
			
			evaluationPane.setPccValue(evaluation.getEvidenceSetPCC());
			
			evaluationPane.setErrorValue(evaluation.getError());
			
			evaluationPane.addOutputValues(evidenceEvaluationList);
			
			evaluationPane.revalidate();
			evaluationPane.repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(evaluationPane, e.getMessage(), "Evaluation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Reset evidences to all finding/conditional nodes, which means they do not have findings any more.  
		network.resetEvidences();
		
		evaluationPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public JPanel getView() {
		return evaluationPane;
	}

}
