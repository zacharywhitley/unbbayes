package unbbayes.aprendizagem;

import java.awt.*;

import javax.swing.*;
import unbbayes.util.NodeList;

import java.awt.event.*;

/**
 * Classe que controi uma tela onde o usuário deverá
 * definir qual a variável que deverá conter o numero
 * de vezes que um certo caso se repete no arquivo.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */

public class CompactChooseWindow extends JDialog{
	
    private NodeList variablesVector;
    private JPanel centerPanel;
    private JButton ok;
    private JButton cancel;
    private JComboBox variablesCombo;
    private CompactChooseInterationController chooseController;

    /**
     * Constrói a tela que contem uma lista onde
     * o usuário deverá escolhera variável que contem
     * o número de vezes que um determinado caso se
     * repete.
     * @param vetorVariaveis - O vetor com as variáveis
     * do arquivo(<code>java.util.List<code>)
     * @see TVariavel
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
        TVariavel aux;
        for (int i = 0 ; i < variablesVector.size() ;i++ ){
            aux = (TVariavel)variablesVector.get(i);
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

