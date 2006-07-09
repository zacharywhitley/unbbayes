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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import unbbayes.util.NodeList;

public class OrdenationWindow extends JDialog {	
    
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private String[] metrics = {"MDL","GH", "GHS"};		
	private String[] paradigms = {"Ponctuation","IC"};	
	private String[] ponctuationAlgorithms = {"K2","B"};
	private String[] icAlgorithms = {"CBL-A","CBL-B"};     
    private JPanel northPanel;			
    private JPanel southPanel;
    private JPanel centerPanel;
    private JPanel buttonPanel;
    private JPanel ordenationPanel;
    private JPanel decisionPanel;
    public  JProgressBar progress;
    public  JLabel label;
    private JButton upButton;
    private JButton downButton;
    private JButton continueButton;
    private JButton relationsButton;
    private JList  ordenationJL;
    private DefaultListModel  listModel;
    private JComboBox algorithmsList;
    private JTextField txtParam;    
    private JComboBox paradigmList;
    private JComboBox metricList;
	private NodeList variables;
	private OrdenationInterationController ordenationController;
	
	public OrdenationWindow(NodeList variables){
	    super(new Frame(),"UnBBayes - Learning Module",true);	    
	    this.variables = variables;
	    Container container = getContentPane();	    
        container.add(getCenterPanel(), BorderLayout.CENTER);                
        container.add(getSouthPanel(),BorderLayout.SOUTH);
        ordenationController = new OrdenationInterationController(variables,this);   
        setResizable(false);
        pack();
        setVisible(true); 
        
	}
	
	public OrdenationInterationController getController(){
		return ordenationController;
	}
	
	public JList getOrdenationJL(){
		return ordenationJL;		
	}	
	
	public JComboBox getMetricList(){
		return metricList;
	}

	public JComboBox getAlgorithmList(){
		return algorithmsList;
	}
	
	public int getIcSize(){
		return icAlgorithms.length;		
	}
	
	public int getPonctuationSize(){
		return ponctuationAlgorithms.length;		
	}
	
	public int getMetricsSize(){
		return metrics.length;		
	}
	
	public String getIcAlgorithms(int index){
		return icAlgorithms[index];				
	}
	
	public String getPonctuatioAlgorithms(int index){
		return ponctuationAlgorithms[index];				
	}
	
	public String getMetrics(int index){
		return metrics[index];				
	}
	
	private JPanel getSouthPanel(){
		southPanel = new JPanel(new GridLayout(2,1,5,5));
		label      = new JLabel("Variable : ");
        progress   = new JProgressBar(JProgressBar.HORIZONTAL,0,100);
        progress.setStringPainted(true);
        southPanel.add(label);
        southPanel.add(progress);
        return southPanel;		
	}
	
	private JPanel getCenterPanel(){
		centerPanel = new JPanel(new GridLayout(1,2));
        centerPanel.add(getOrdenationPanel());
        centerPanel.add(getDecisionPanel());
        return centerPanel;		
	}
	
	private JPanel getOrdenationPanel(){
	   ordenationPanel  = new JPanel(new BorderLayout());
       listModel        = new DefaultListModel();
       insertList();
       ordenationJL     = new JList(listModel);
       JScrollPane ordenationJLScrollPane = new JScrollPane(ordenationJL);
       ordenationJL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       ordenationJL.setSelectedIndex(0);
       ordenationPanel.add(new JLabel("Ordenation"), BorderLayout.NORTH);
       ordenationPanel.add(ordenationJLScrollPane, BorderLayout.CENTER);
       ordenationJL.addMouseListener(doubleClickEvent);
       return ordenationPanel;
	}
	
	private JPanel getDecisionPanel(){
        decisionPanel  = new JPanel(new BorderLayout());
        decisionPanel.add(getNorthPanel(), BorderLayout.NORTH);        
        decisionPanel.add(getButtonPanel(),BorderLayout.CENTER);
        return decisionPanel;		
	}
	
	private JPanel getNorthPanel(){
		northPanel = new JPanel(new GridLayout(3,2,3,3));
		paradigmList = new JComboBox(paradigms);		
		northPanel.add(new JLabel("Choose a Paradigm : "));		
		northPanel.add(paradigmList);
		northPanel.add(new JLabel("Choose an Algorithm : "));		
		algorithmsList = new JComboBox(ponctuationAlgorithms);
        northPanel.add(algorithmsList);  
        northPanel.add(new JLabel("Choose a Metric : "));
	    metricList = new JComboBox(metrics);
	    northPanel.add(metricList);			        			
		paradigmList.addItemListener(paradigmEvent);		
		return northPanel;		
	}
	
	private JPanel getButtonPanel(){
		buttonPanel     = new JPanel(new GridLayout(8,1,5,5));        
        txtParam        = new JTextField();
        upButton        = new JButton("Up");
        downButton      = new JButton("Down");
        continueButton  = new JButton("Continue");
        relationsButton = new JButton("Relations");       
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);
        buttonPanel.add(relationsButton);        
        buttonPanel.add(new  JLabel("Parameter :"));
        buttonPanel.add(txtParam);
        buttonPanel.add(continueButton);
        continueButton.addActionListener(continueEvent);
        relationsButton.addActionListener(relationsEvent);
        upButton.addActionListener(upEvent);
        downButton.addActionListener(downEvent);						
		return buttonPanel;		
	}
	
	private void insertList(){
		TVariavel aux;
		for(int i = 0 ; i< variables.size(); i++){			
			aux = (TVariavel)variables.get(i);
			listModel.addElement(aux.getName());
		}				
	}
	
	ActionListener continueEvent = new ActionListener(){		
		public void actionPerformed(ActionEvent ae){	
			ordenationController.continueEvent((String)paradigmList.getSelectedItem(),
			                                   (String)algorithmsList.getSelectedItem(),
			                                   (String)metricList.getSelectedItem(),
			                                   txtParam.getText());					
		}		
	};
	
	ActionListener upEvent = new ActionListener(){		
		public void actionPerformed(ActionEvent ae){	
			ordenationController.upEvent();					
		}		
	};
	
	ActionListener downEvent = new ActionListener(){		
		public void actionPerformed(ActionEvent ae){	
			ordenationController.downEvent();					
		}		
	};
	
	ActionListener relationsEvent = new ActionListener(){		
		public void actionPerformed(ActionEvent ae){	
			ordenationController.relationsEvent();					
		}		
	};
	
	MouseListener doubleClickEvent = new MouseListener(){
        public void mouseClicked(MouseEvent e){
        	ordenationController.doubleClickEvent(e);            
        }
        public void mousePressed(MouseEvent e){}
        public void mouseReleased(MouseEvent e){}
        public void mouseEntered(MouseEvent e){}
        public void mouseExited(MouseEvent e){}
    };	
    
    ItemListener paradigmEvent = new ItemListener(){
    	public void itemStateChanged(ItemEvent ie){ 
    		ordenationController.paradigmEvent(paradigmList.getSelectedIndex());
    	}    		    	  	
    };

}
