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
package unbbayes.datamining.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Dialog window where Training Mode options are set.
 *
 * 	@author M�rio Hernique Paes Vieira (mhpv@hotmail.com)
 */
public class TrainingModePanel extends JPanel{

    /** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	private JRadioButton trainingSetRadioButton = new JRadioButton();
	private JRadioButton crossValidationRadioButton = new JRadioButton();
	private JComboBox crossValidationComboBox = new JComboBox();
	
	  /** Default constructor. */
	  public TrainingModePanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.setLayout(new BorderLayout());

        //customized layouts and borders
        GridLayout twoRowsLayout = new GridLayout();
        twoRowsLayout.setColumns(1);
        twoRowsLayout.setRows(2);
        Border border = BorderFactory.createEtchedBorder(Color.white,new Color(165, 163, 151));

        //CENTER PANEL: PANEL WITH THE OTHER OPTIONS
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(twoRowsLayout);
        this.add(centerPanel,"Center");
        centerPanel.setBorder(new TitledBorder(border,"Available Modes"));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        trainingSetRadioButton.setText("Use Training Set");
        trainingSetRadioButton.setSelected(true);
        buttonGroup.add(trainingSetRadioButton);
        for (int i=2;i<31;i++) {
            crossValidationComboBox.addItem(i);        	
        }
        crossValidationComboBox.setSelectedIndex(8);
        centerPanel.add(trainingSetRadioButton);
        
        JPanel crossValidationPanel = new JPanel();
        crossValidationPanel.setLayout(new BorderLayout());
        crossValidationRadioButton.setText("Cross-Validation");
        buttonGroup.add(crossValidationRadioButton);
        crossValidationPanel.add(crossValidationRadioButton,BorderLayout.WEST);
        crossValidationPanel.add(crossValidationComboBox,BorderLayout.CENTER);
        centerPanel.add(crossValidationPanel);
    }
    
    public boolean isTrainingSetRadioButtonSelected() {
    	return trainingSetRadioButton.isSelected();
    }

    public boolean isCrossValidationRadioButton() {
    	return crossValidationRadioButton.isSelected();
    }
    
    public int getNumSelectedFolds() {
    	return (Integer)crossValidationComboBox.getSelectedItem();
    }
}
