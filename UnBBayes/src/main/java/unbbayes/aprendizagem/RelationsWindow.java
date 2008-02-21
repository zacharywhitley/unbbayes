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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

/**
 * Class for building a screen which defines relations between
 * variables, observing that an ancester should never be a son of
 * itself.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class RelationsWindow extends JDialog{

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private JPanel rolBox;
    private JPanel relationBox;
    private JPanel relationPanel;
    private JPanel buttonPanel;
    private JPanel parentsPanel;
    private JPanel sunPanel;
    private JList  parentsList;
    private JList  sunList;
    private JList  relationList;
    private DefaultListModel relationListModel;
    private DefaultListModel parentsListModel;
    private DefaultListModel sunListModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton continueButton;
    private NodeList variables;
    private RelationInterationController relationController;

    /**
     * Method for screen construction, which defines fixed parent-children relationships.
     * It contains a panel with parents' list, a panel with a list of its childrens,
     * a panel with buttons for adding-removing relationships or leaving the program, and
     * a panel with previously-decided relationships.
     * @param vetor - Variables' list(<code>java.util.List<code>)
     * @see LearningNode
     * @see JList
     * @see DefaultListModel
     * @see JScrollPane
     */
    public  RelationsWindow(NodeList variables){
       super(new Frame(), "UnBBayes - Learning Module", true);
       Container container = getContentPane();
       this.variables = variables;       
       container.add(getRelationBox());
       listInsert();      
       relationController = new RelationInterationController(this,variables);
       pack();
       setResizable(false);
       setVisible(true);
    }
    
    public DefaultListModel getRelationModel(){
    	return relationListModel;
    }
    
    public JList getRelationList(){
        return relationList;    
    }
    
    private JPanel getRelationBox(){
        relationBox = new JPanel(new GridLayout(1,5,10,10));
        relationBox.add(getParents());
        relationBox.add(getSun());
        relationBox.add(getButtons());
        relationBox.add(getRelations());
        return relationBox;
    }

    private JPanel getParents(){
        parentsPanel                       = new JPanel(new BorderLayout());
        parentsListModel                   = new DefaultListModel();
        parentsList                        = new JList(parentsListModel);
        JScrollPane parentsBoxJP           = new JScrollPane(parentsList);
        parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        parentsList.setSelectedIndex(0);
        parentsPanel.add(new JLabel("Parents"),BorderLayout.NORTH);
        parentsPanel.add(parentsBoxJP, BorderLayout.CENTER);
        return parentsPanel;
    }

    private JPanel getSun(){
        sunPanel                           = new JPanel(new BorderLayout());
        sunListModel                       = new DefaultListModel();
        sunList                            = new JList(sunListModel);
        JScrollPane sunBoxJP               = new JScrollPane(sunList);
        sunList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sunList.setSelectedIndex(0);
        sunPanel.add(new JLabel("Suns"),BorderLayout.NORTH);
        sunPanel.add(sunBoxJP, BorderLayout.CENTER);
        return sunPanel;
    }

    private JPanel getButtons(){
       buttonPanel           = new JPanel(new GridLayout(8,1,10,10));
       addButton             = new JButton("Add");
       removeButton          = new JButton("Remove");
       continueButton        = new JButton("Continue");
       buttonPanel.add(new JLabel(""));
       buttonPanel.add(new JLabel(""));
       buttonPanel.add(addButton);
       buttonPanel.add(new JLabel(""));
       buttonPanel.add(new JLabel(""));
       buttonPanel.add(removeButton);
       buttonPanel.add(continueButton);
       continueButton.addActionListener(continueEvent);
       addButton.addActionListener(addEvent);
       removeButton.addActionListener(removeEvent);
       return buttonPanel;
    }

    private JPanel getRelations(){
        relationPanel         = new JPanel(new BorderLayout());
        relationListModel     = new DefaultListModel();
        relationList          = new JList(relationListModel);
        relationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane relationBoxJP = new JScrollPane(relationList);
        relationPanel.add(new JLabel("Relations"), BorderLayout.NORTH);
        relationPanel.add(relationBoxJP, BorderLayout.CENTER);
        return relationPanel;
    }
    
    private void listInsert(){
    	LearningNode aux;
    	for (int i = 0 ; i < variables.size(); i++ ){
           aux = (LearningNode)variables.get(i);
           parentsListModel.addElement(aux.getName());
           sunListModel.addElement(aux.getName());
        }        
    }

    ActionListener continueEvent = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            relationController.continueEvent();
        }
    };


    ActionListener removeEvent = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
        	relationController.removeEvent(relationList.getSelectedValue());
        }
    };


    ActionListener addEvent = new ActionListener(){
        public void actionPerformed(ActionEvent ae){          
          relationController.addEvent(parentsList.getSelectedValue(),sunList.getSelectedValue());
        }
    };

}