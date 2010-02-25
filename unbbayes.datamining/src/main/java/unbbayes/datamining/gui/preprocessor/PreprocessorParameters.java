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
package unbbayes.datamining.gui.preprocessor;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 14/02/2007
 */
public class PreprocessorParameters {
	private float sampleSize;
	private boolean compact;
	private boolean canceled;
	private int classIndex = -1;

	public PreprocessorParameters(Component parent, InstanceSet inst,
			boolean size) {
		canceled = false;

		/* TextBox for proportion */
		JTextField sizeField = new JTextField();
		JLabel sizeLabel = null;
		if (size) {
			sizeLabel = new JLabel("Size");
		} else {
			sizeLabel = new JLabel("Proportion");
		}
		
		/* ComboBox with attributes for selection */ 
		JComboBox attributesComboBox = new JComboBox();
		attributesComboBox.setMaximumRowCount(5);
		Attribute aux;
		attributesComboBox.addItem("None");
		int numAttributes = inst.numAttributes();
		for (int i = 0 ; i < numAttributes ;i++ ) {
			aux = (Attribute) inst.getAttribute(i);
		    attributesComboBox.addItem(aux.getAttributeName());
		}
		JLabel classLabel = new JLabel("Class");
		
		/* Compact checkBox */
		JCheckBox compactCheckBox = new JCheckBox();
		JLabel compactLabel = new JLabel("Compact");
		
		/* Create panel */
		JPanel panel = new JPanel(new GridLayout(3,3));
		panel.add(sizeLabel, null);
		panel.add(sizeField, null);
		panel.add(compactLabel, null);
		panel.add(compactCheckBox, null);
		panel.add(classLabel, null);
		panel.add(attributesComboBox, null);
        if ((JOptionPane.showInternalConfirmDialog(parent, panel, "", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {
			int selectedAttribute = attributesComboBox.getSelectedIndex();
			if (selectedAttribute != 0) {
				classIndex = selectedAttribute - 1;
			}
			sampleSize = Float.parseFloat(sizeField.getText());
			compact = compactCheckBox.isSelected();
		} else {
			canceled = true;
		}
	}

	/**
	 * @return the proportion
	 */
	public float getSampleSize() {
		return sampleSize;
	}

	/**
	 * @return the compact
	 */
	public boolean isCompact() {
		return compact;
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * @return the classIndex
	 */
	public int getClassIndex() {
		return classIndex;
	}

}

