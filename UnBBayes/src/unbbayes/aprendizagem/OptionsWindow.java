package unbbayes.aprendizagem;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;



/**
 * Classe para a constru��o de uma tela que mostra
 * qual a sigla de uma determinada vari�vel, uma lista
 * com os poss�veis estados dessa vari�vel, e permite ao
 * usu�rio do sistema alterar o n�mero m�ximo de pais de
 * uma vari�vel.
 * @author Danilo Cust�dio da Silva
 * @version 1.0
 */
public class OptionsWindow extends JDialog
{
    private TVariavel variable;
    private JTextField txtMaxParents;
    private JTextField txtName;
    private JLabel lMaxParents;
    private JLabel lName;
    private JLabel lStates;
    private JPanel centerPanel;
    private JPanel northPanel;
    private JPanel namePanel;
    private JPanel maxPanel;
    private JPanel repetitionPanel;
    private JPanel statesPanel;
    private JList  statesList;
    private DefaultListModel  statesListModel;
    private JButton applyButton;
    private JButton cancelButton;
    private OptionsInterationController optionsController;

    /**
     * M�todo construtor da tela.Essa tela possui dois paineis
     * principais,o pPainelCentro que contem os paineis com a li
     * sta dos estados e com os bot�es e o pPainelNorte que cont�m
     * a sigla da vari�vel e o n�mero m�ximo de pais.
     * @param no - A v�riavel a qual se deseja obter as informa��es
     * (<code>TVariavel<code>)
     * @see TVariavel
     * @see JPanel
     * @see Container
     */
    public OptionsWindow(TVariavel variable){
        super(new Frame(),"UnBBayes - Learning Module",true);
        this.variable = variable;
        Container container  = getContentPane();
        container.add(getNorthPanel(), BorderLayout.NORTH);
        container.add(getCenterPanel(), BorderLayout.CENTER);
        optionsController = new OptionsInterationController(this,variable);
        pack();
        setResizable(false);
        setVisible(true);
    }
    
    private JPanel getNorthPanel(){
        northPanel  = new JPanel(new GridLayout(1,2,10,10));
        northPanel.add(getNamePanel());
        northPanel.add(getMaxPanel());
        return northPanel;
    }
    
    private JPanel getCenterPanel(){
        centerPanel = new JPanel(new GridLayout(1,2,10,10));
        centerPanel.add(getStatesPanel());
        centerPanel.add(getRepetitionPanel());
        return centerPanel;
    }

    private JPanel getNamePanel(){
        namePanel    = new JPanel(new GridLayout(1,2,10,10));
        lName        = new JLabel("          Nome");
        txtName      = new JTextField(variable.getName());
        txtName.setEnabled(false);
        namePanel.add(lName);
        namePanel.add(txtName);
        return namePanel;
    }

    private JPanel getMaxPanel(){
        maxPanel             = new JPanel(new GridLayout(1,2));
        lMaxParents          = new JLabel("M�x :");
        txtMaxParents        = new JTextField("" + variable.getNumeroMaximoPais());
        maxPanel.add(lMaxParents);
        maxPanel.add(txtMaxParents);
        return maxPanel;
    }    
    
    private JPanel getStatesPanel(){
        statesPanel           = new JPanel(new BorderLayout());
        lStates               = new JLabel("States");
        statesListModel       = new DefaultListModel();
        statesList            = new JList(statesListModel);
        JScrollPane statesSP  = new JScrollPane(statesList);
        statesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        java.util.List auxVector = (java.util.List)variable.getEstados();
        for (int j = 0; j < auxVector.size(); j++ ){
            String auxState = (String)auxVector.get(j);
            statesListModel.addElement(auxState);
        }
        statesPanel.add(statesSP, BorderLayout.CENTER);
        statesPanel.add(lStates, BorderLayout.NORTH);
        return statesPanel;
    }

    private JPanel getRepetitionPanel(){
        repetitionPanel   = new JPanel(new GridLayout(7,1,5,5));
        applyButton       = new JButton("Apply");
        cancelButton    = new JButton("Cancel");
        repetitionPanel.add(new JLabel(""));
        repetitionPanel.add(new JLabel(""));
        repetitionPanel.add(new JLabel(""));
        repetitionPanel.add(applyButton);
        repetitionPanel.add(cancelButton);
        repetitionPanel.add(new JLabel(""));
        repetitionPanel.add(new JLabel(""));
        applyButton.addActionListener(applyEvent);
        cancelButton.addActionListener(cancelEvent);
        return repetitionPanel;
    }
    
    

    ActionListener applyEvent = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
        	optionsController.applyEvent(txtMaxParents.getText());
        }
    };

    ActionListener cancelEvent = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            optionsController.cancelEvent();
        }
    };
}
