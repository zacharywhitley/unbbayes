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
 package unbbayes.learning;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;

/** 
 * Class which controlls a screen where the user should define which variable
 * must contain the mumber of times a particular case repeats within the archive
 * @author Danilo Custodio da Silva
 * @version 1.0
 */

public class CompactChooseWindow extends JDialog{
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private ArrayList<Node> variablesVector;
    private JPanel centerPanel;
    private JButton ok;
    private JButton cancel;
    private JComboBox variablesCombo;
    private CompactChooseInterationController chooseController;

    /**
     * Builds a screen containing a list where the user should choose the variable containing
     * the number of times a particular case is repeating.
     * @param vetorVariaveis - variable vector
     * do arquivo(<code>java.util.List<code>)
     * @see LearningNode
     * @see Container
     */
    CompactChooseWindow(ArrayList<Node> variablesVector){
        super(new Frame(), "UnbBayes - Learning Module", true);
        Container container = getContentPane();
        this.variablesVector = variablesVector;
        container.add(getCenterPanel(),BorderLayout.CENTER);
        container.add(new JLabel("Select the field that contain the frequencies"),BorderLayout.NORTH);
        chooseController = new CompactChooseInterationController(this);
        setResizable(false);
        pack();
        setVisible(true);        
    }

    private JPanel getCenterPanel(){
        centerPanel     = new JPanel(new GridLayout(1,3,5,5));
        ok              = new JButton("OK");
        cancel          = new JButton("Cancelar");
        variablesCombo  = new JComboBox();
        variablesCombo.setMaximumRowCount(5);
        LearningNode aux;
        for (int i = 0 ; i < variablesVector.size() ;i++ ){
            aux = (LearningNode)variablesVector.get(i);
            if(aux.getParticipa()){
               variablesCombo.addItem(aux.getName());
            }
        }
        ok.addActionListener(ActionOk);
        cancel.addActionListener(ActionCancel);
        centerPanel.add(variablesCombo);
        centerPanel.add(ok);
        centerPanel.add(cancel);
        return centerPanel;
    }

    ActionListener ActionOk = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            chooseController.actionOk(variablesCombo, variablesVector);            
        }
    };

    ActionListener ActionCancel = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            chooseController.actionCancel();
        }
    };

}

