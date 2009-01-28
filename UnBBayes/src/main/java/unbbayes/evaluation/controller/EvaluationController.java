/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import unbbayes.evaluation.Evaluation;
import unbbayes.evaluation.Evaluation.EvidenceEvaluation;
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
		evaluationPane.addInputValues(nodeNameList);
		
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
		try {
			validateData();
		} catch (EvaluationException e) {
			JOptionPane.showMessageDialog(evaluationPane, e.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<String> targetNodeNameList = evaluationPane.getTargetNodeNameList();
		List<String> evidenceNodeNameList = evaluationPane.getEvidenceNodeNameList();
		Integer sampleSize = evaluationPane.getSampleSizeValue();
		
		try {
			evaluation.evaluate(network, targetNodeNameList, evidenceNodeNameList, sampleSize);
			
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
	}
	
	public JPanel getView() {
		return evaluationPane;
	}

}
