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
package unbbayes.aprendizagem;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;

public class ChooseVariablesWindow extends JDialog {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private JPanel choosePanel;
	private JScrollPane scrollPane;
	private JPanel centerPanel;
	private JPanel buttonPanel;
	private ArrayList<Node> variablesVector;
	private JButton ok;
	private ChooseInterationController chooseController;
	public int classei = -1;

	/**
	 * Constructs the frame where the user decides which variables will
	 * participate on the netwotk learning
	 * 
	 * @param variables -
	 *            A NodeList with the variable read on the file
	 */
	public ChooseVariablesWindow(ArrayList<Node> variables) {
		super(new Frame(), "UnBBayes - Learning Module", true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container container = getContentPane();
		variablesVector = variables;
		choosePanel = new JPanel();
		int length = variables.size();
		choosePanel.setLayout(new GridLayout(variables.size(), 1, 3, 3));

		/* Construct the checkboxes */
		LearningNode variable;
		for (int i = 0; i < length; i++) {
			variable = (LearningNode) variables.get(i);
			choosePanel.add(new JCheckBox(variable.getName(), true));
		}
		ok = new JButton("Ok");
		centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		scrollPane = new JScrollPane(choosePanel);
		buttonPanel = new JPanel();
		centerPanel.add(scrollPane);
		buttonPanel.add(ok);
		centerPanel.add(buttonPanel);
		container.add(new JLabel(
				"Check the fields to include on the believe network"),
				BorderLayout.NORTH);
		ok.addActionListener(okListener);
		container.add(centerPanel, BorderLayout.CENTER);
		chooseController = new ChooseInterationController(this);
		setResizable(false);
		pack();
		setVisible(true);
	}

	public ChooseVariablesWindow(ArrayList<Node> variables, int classe) {
		super(new Frame(), "", true);
		classei = -1;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container container = getContentPane();
		variablesVector = variables;
		choosePanel = new JPanel();
		int length = variables.size();
		choosePanel.setLayout(new GridLayout(variables.size(), 1, 3, 3));

		LearningNode variable;
		for (int i = 0; i < length; i++) {
			variable = (LearningNode) variables.get(i);
			// choosePanel.add(new JCheckBox(variable.getName(),true));
			choosePanel.add(new JRadioButton(variable.getName(), false));
		}

		ok = new JButton("Ok");
		centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		scrollPane = new JScrollPane(choosePanel);
		buttonPanel = new JPanel();
		centerPanel.add(scrollPane);
		buttonPanel.add(ok);
		centerPanel.add(buttonPanel);
		container.add(new JLabel("Escolha a variável de Classe"),
				BorderLayout.NORTH);
		ok.addActionListener(okListener2);
		container.add(centerPanel, BorderLayout.CENTER);
		chooseController = new ChooseInterationController(this);
		setResizable(false);
		pack();
		setVisible(true);
	}

	public JPanel getChoosePanel() {
		return choosePanel;
	}

	public LearningNode getVariable(int i) {
		return (LearningNode) variablesVector.get(i);
	}

	ActionListener okListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			chooseController.setVariablesState();
		}
	};

	ActionListener okListener2 = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			classei = chooseController.setVariablesState(0);
			if (classei != -1) {
				dispose();
			} else {
				String msg = "Escolha a variável a ser predita!";
				JOptionPane.showMessageDialog(null, msg, "ERROR",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};
}