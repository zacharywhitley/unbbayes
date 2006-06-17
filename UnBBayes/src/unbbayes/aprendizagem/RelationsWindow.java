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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import unbbayes.util.NodeList;

/**
 * Classe para a construção de uma tela que possui
 * como finalidade definir relacionamentos entre as
 * variáveis, lembrando que nunca um ancestral pode
 * ser filho da propria variável.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class RelationsWindow extends JDialog{
	
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
     * Método para a construção da tela onde são definidos
     * relacionamentos de paternidade fixos. Contem um painel
     * com a lista dos pais, um painel com a lista dos filhos,
     * um painel com botoes para adicionar, remover relacionamentos
     * ou continuar o programa, e um painel com os relacionamentos
     * previamente decididos.
     * @param vetor - A lista de variáveis(<code>java.util.List<code>)
     * @see TVariavel
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
    	TVariavel aux;
    	for (int i = 0 ; i < variables.size(); i++ ){
           aux = (TVariavel)variables.get(i);
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