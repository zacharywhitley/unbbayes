package unbbayes.aprendizagem;


import javax.swing.*;
import unbbayes.util.NodeList;

import java.awt.*;
import java.awt.event.*;

public class ChooseVariablesWindow extends JDialog{

    private JPanel choosePanel;
    private JScrollPane scrollPane;
    private JPanel centerPanel;
    private JPanel buttonPanel;
    private NodeList variablesVector;
    private JButton ok;
    private ChooseInterationController chooseController;
    
    /**
     * Constructs the frame where the user decides which variables
     * will participate on the netwotk learning
     * 
     * @param variables - A NodeList with the variable read on the 
     * file
     **/
    public ChooseVariablesWindow(NodeList variables){
        super(new Frame(),"UnBBayes - Learning Module",true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container container = getContentPane();
        variablesVector  = variables;
        choosePanel = new JPanel();
        int length =  variables.size();
        choosePanel.setLayout(new GridLayout(variables.size(),1,3,3));

        /*Construct the checkboxes*/
        TVariavel variable;        
        for(int i = 0;  i < length ; i++){
            variable = (TVariavel)variables.get(i);
            choosePanel.add(new JCheckBox(variable.getName(),true));
        }
        ok           = new JButton("Ok");
        centerPanel  = new JPanel(new GridLayout(1,2,10,10));
        scrollPane   = new JScrollPane(choosePanel);
        buttonPanel  = new JPanel();
        centerPanel.add(scrollPane);
        buttonPanel.add(ok);
        centerPanel.add(buttonPanel);
        container.add(new JLabel("Check the fields to include on the believe network"),BorderLayout.NORTH);
        ok.addActionListener(okListener);        
        container.add(centerPanel,BorderLayout.CENTER);
        chooseController = new ChooseInterationController(this);
        setResizable(false);
        pack();
        setVisible(true);       
    }
    
    public JPanel getChoosePanel(){
    	return choosePanel;    	
    }    	
    
    public TVariavel getVariable(int i){
    	return (TVariavel)variablesVector.get(i);    	
    }

    ActionListener okListener = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
        	chooseController.setVariablesState();         	
        }        
    };
}