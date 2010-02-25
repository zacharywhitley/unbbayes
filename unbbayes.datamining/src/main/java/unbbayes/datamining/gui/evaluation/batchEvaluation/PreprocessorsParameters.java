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
package unbbayes.datamining.gui.evaluation.batchEvaluation;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;
import unbbayes.datamining.gui.UtilsGUI;
import unbbayes.datamining.gui.evaluation.batchEvaluation.controllers.PreprocessorsTabController;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 22/11/2006
 */
public class PreprocessorsParameters extends JFrame {
	private static final long serialVersionUID = 1L;

	/** Load resource file for this package */
	private static ResourceBundle resource;

	private Component parent;
	private String windowTitle;
	private JTextField ratioStart;
	private JTextField ratioEnd;
	private JTextField ratioStep;
	private JTextField clustersStart;
	private JTextField clustersEnd;
	private JTextField clustersStep;
	private JTextField oversamplingThresholdStart;
	private JTextField oversamplingThresholdEnd;
	private JTextField oversamplingThresholdStep;
	private JTextField positiveThresholdStart;
	private JTextField positiveThresholdEnd;
	private JTextField positiveThresholdStep;
	private JTextField negativeThresholdStart;
	private JTextField negativeThresholdEnd;
	private JTextField negativeThresholdStep;
	private JComboBox cleanType;
	private PreprocessorParameters parameters;

	public PreprocessorsParameters(PreprocessorsTabController controller, Component parent) {
		this.parent = parent;
		resource = controller.getResourceBundle();
		windowTitle = resource.getString("preprocessorsOptionsTitle");
	}

	public void run(PreprocessorParameters parameters) {
		this.parameters = parameters;
		
		/* Build the principal panel */
		JPanel principalPanel = buildWindow();

		/* Show window */
		if ((JOptionPane.showInternalConfirmDialog(parent, principalPanel,
				windowTitle, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)) {
			/* Pressing Ok */
			setParameters();
		} else {
			/* Canceled by the user. Do nothing! */
		}
	}

	private JPanel buildWindow() {
		/* Build the principal panel */
		JPanel principalPanel = new JPanel(new MigLayout());

		/* Labels */
		String ratioLabel = resource.getString("ratio");
		String clustersLabel = resource.getString("cluster");
		String oversamplingThresholdLabel = resource.getString("oversamplingThreshold");
		String positiveThresholdLabel = resource.getString("positiveThreshold");
		String negativeThresholdLabel = resource.getString("negativeThreshold");
		String cleanLabel = resource.getString("cleanType");
		String cleanActivated = resource.getString("cleanActivated");
		String cleanDeactivated = resource.getString("cleanDeactivated");
		String cleanBoth = resource.getString("cleanBoth");
		String startLabel = resource.getString("start");
		String endLabel = resource.getString("end");
		String stepLabel = resource.getString("step");

		/* Ratio */
		UtilsGUI.addSeparator(principalPanel, ratioLabel);
		ratioStart = UtilsGUI.createTextField(2);
		ratioEnd = UtilsGUI.createTextField(2);
		ratioStep = UtilsGUI.createTextField(2);
		principalPanel.add(UtilsGUI.createLabel(startLabel), "gap para");
		principalPanel.add(ratioStart);
		principalPanel.add(UtilsGUI.createLabel(endLabel),   "gap para");
		principalPanel.add(ratioEnd);
		principalPanel.add(UtilsGUI.createLabel(stepLabel),  "gap para");
		principalPanel.add(ratioStep,                        "wrap para");

		/* Clusters */
		UtilsGUI.addSeparator(principalPanel, clustersLabel);
		clustersStart = UtilsGUI.createTextField(2);
		clustersEnd = UtilsGUI.createTextField(2);
		clustersStep = UtilsGUI.createTextField(2);
		principalPanel.add(UtilsGUI.createLabel(startLabel), "gap para");
		principalPanel.add(clustersStart);
		principalPanel.add(UtilsGUI.createLabel(endLabel),   "gap para");
		principalPanel.add(clustersEnd);
		principalPanel.add(UtilsGUI.createLabel(stepLabel),  "gap para");
		principalPanel.add(clustersStep,                     "wrap para");

		/* Oversampling Threshold */
		UtilsGUI.addSeparator(principalPanel, oversamplingThresholdLabel);
		oversamplingThresholdStart = UtilsGUI.createTextField(2);
		oversamplingThresholdEnd = UtilsGUI.createTextField(2);
		oversamplingThresholdStep = UtilsGUI.createTextField(2);
		principalPanel.add(UtilsGUI.createLabel(startLabel), "gap para");
		principalPanel.add(oversamplingThresholdStart);
		principalPanel.add(UtilsGUI.createLabel(endLabel),   "gap para");
		principalPanel.add(oversamplingThresholdEnd);
		principalPanel.add(UtilsGUI.createLabel(stepLabel),  "gap para");
		principalPanel.add(oversamplingThresholdStep,        "wrap para");

		/* Positive Threshold */
		UtilsGUI.addSeparator(principalPanel, positiveThresholdLabel);
		positiveThresholdStart = UtilsGUI.createTextField(2);
		positiveThresholdEnd = UtilsGUI.createTextField(2);
		positiveThresholdStep = UtilsGUI.createTextField(2);
		principalPanel.add(UtilsGUI.createLabel(startLabel), "gap para");
		principalPanel.add(positiveThresholdStart);
		principalPanel.add(UtilsGUI.createLabel(endLabel),   "gap para");
		principalPanel.add(positiveThresholdEnd);
		principalPanel.add(UtilsGUI.createLabel(stepLabel),  "gap para");
		principalPanel.add(positiveThresholdStep,            "wrap para");

		/* Negative Threshold */
		UtilsGUI.addSeparator(principalPanel, negativeThresholdLabel);
		negativeThresholdStart = UtilsGUI.createTextField(2);
		negativeThresholdEnd = UtilsGUI.createTextField(2);
		negativeThresholdStep = UtilsGUI.createTextField(2);
		principalPanel.add(UtilsGUI.createLabel(startLabel), "gap para");
		principalPanel.add(negativeThresholdStart);
		principalPanel.add(UtilsGUI.createLabel(endLabel),   "gap para");
		principalPanel.add(negativeThresholdEnd);
		principalPanel.add(UtilsGUI.createLabel(stepLabel),  "gap para");
		principalPanel.add(negativeThresholdStep,            "wrap para");

		/* Clean */
		UtilsGUI.addSeparator(principalPanel, cleanLabel);
		String[] cleanOptions = {cleanActivated, cleanDeactivated, cleanBoth};
		cleanType = UtilsGUI.createCombo(cleanOptions);
		principalPanel.add(cleanType, "gapleft 15, span");

		/* Set initial values */
		setInitialValues();
		
		return principalPanel;
	}
	
	private void setParameters() {
		parameters.setRatioStart(Integer.parseInt(ratioStart.getText()));
		parameters.setRatioEnd(Integer.parseInt(ratioEnd.getText()));
		parameters.setRatioStep(Integer.parseInt(ratioStep.getText()));
		parameters.setClusterStart(Integer.parseInt(clustersStart.getText()));
		parameters.setClusterEnd(Integer.parseInt(clustersEnd.getText()));
		parameters.setClusterStep(Integer.parseInt(clustersStep.getText()));
		parameters.setOverThresholdStart(Integer.parseInt(oversamplingThresholdStart.getText()));
		parameters.setOverThresholdEnd(Integer.parseInt(oversamplingThresholdEnd.getText()));
		parameters.setOverThresholdStep(Integer.parseInt(oversamplingThresholdStep.getText()));
		parameters.setPosThresholdStart(Integer.parseInt(positiveThresholdStart.getText()));
		parameters.setPosThresholdEnd(Integer.parseInt(positiveThresholdEnd.getText()));
		parameters.setPosThresholdStep(Integer.parseInt(positiveThresholdStep.getText()));
		parameters.setNegThresholdStart(Integer.parseInt(negativeThresholdStart.getText()));
		parameters.setNegThresholdEnd(Integer.parseInt(negativeThresholdEnd.getText()));
		parameters.setNegThresholdStep(Integer.parseInt(negativeThresholdStep.getText()));
		parameters.setCleanType(cleanType.getSelectedIndex());
	}

	public void setInitialValues() {
		ratioStart.setText(Integer.toString(parameters.getRatioStart()));
		ratioEnd.setText(Integer.toString(parameters.getRatioEnd()));
		ratioStep.setText(Integer.toString(parameters.getRatioStep()));
		clustersStart.setText(Integer.toString(parameters.getClusterStart()));
		clustersEnd.setText(Integer.toString(parameters.getClusterEnd()));
		clustersStep.setText(Integer.toString(parameters.getClusterStep()));
		oversamplingThresholdStart.setText(Integer.toString(parameters.getOverThresholdStart()));
		oversamplingThresholdEnd.setText(Integer.toString(parameters.getOverThresholdEnd()));
		oversamplingThresholdStep.setText(Integer.toString(parameters.getOverThresholdStep()));
		positiveThresholdStart.setText(Integer.toString(parameters.getPosThresholdStart()));
		positiveThresholdEnd.setText(Integer.toString(parameters.getPosThresholdEnd()));
		positiveThresholdStep.setText(Integer.toString(parameters.getPosThresholdStep()));
		negativeThresholdStart.setText(Integer.toString(parameters.getNegThresholdStart()));
		negativeThresholdEnd.setText(Integer.toString(parameters.getNegThresholdEnd()));
		negativeThresholdStep.setText(Integer.toString(parameters.getNegThresholdStep()));
		cleanType.setSelectedIndex(parameters.getCleanType());
	}

}