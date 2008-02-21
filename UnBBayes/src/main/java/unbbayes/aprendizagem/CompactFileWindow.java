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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

/**
 * Class which builds a screen where the user should inform the program if the archive
 * is compressed or not.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class CompactFileWindow extends JDialog
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private NodeList variablesVector;
    private JPanel centerPanel;
    private JButton yes;
    private JButton no;
    private CompactInterationController compactController;
    /**
     * Builds a screen. That screen has a label which asks the user if that archive
     * is compressed or not, and two buttons.
     * @param variablesVector - variables inside
     * (<code>java.util.List<code>)
     * @see LearningNode
     * @see Container
     * @see JDialog
     */
    CompactFileWindow(NodeList variablesVector){
        super(new Frame(), "UnBBayes - Learning Module", true);
        Container container = getContentPane();
        this.variablesVector = variablesVector;
        container.add(new JLabel("Do you want to use compacted file? "),BorderLayout.NORTH);
        container.add(getCenterPanel(),BorderLayout.CENTER);
        compactController = new CompactInterationController(this);
        setResizable(false);
        pack();
        setVisible(true);        
    }

    private JPanel getCenterPanel(){
        centerPanel         = new JPanel(new GridLayout(1,2,5,5));
        yes                 = new JButton("Yes");
        no                  = new JButton("No");
        centerPanel.add(yes);
        centerPanel.add(no);
        yes.addActionListener(ActionYes);
        no.addActionListener(ActionNo);
        return centerPanel;
    }
    
    ActionListener ActionYes = new ActionListener(){
        public void actionPerformed(ActionEvent ae){            
            compactController.actionYes(variablesVector);
        }
    };

    ActionListener ActionNo = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
        	compactController.actionNo();
        }
    };
}