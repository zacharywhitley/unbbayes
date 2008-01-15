/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package unbbayes.aprendizagem;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

/** 
 * Class which controlls a screen where the user should define which variable
 * must contain the mumber of times a particular case repeats within the archive
 * @author Danilo Custodio da Silva
 * @version 1.0
 */

public class CompactChooseWindow extends JDialog{
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private NodeList variablesVector;
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
    CompactChooseWindow(NodeList variablesVector){
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

