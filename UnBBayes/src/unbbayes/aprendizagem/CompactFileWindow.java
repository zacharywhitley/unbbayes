package unbbayes.aprendizagem;

import java.awt.*;

import javax.swing.*;
import unbbayes.util.NodeList;

import java.awt.event.*;

/**
 * Classe que constroi uma tela onde usu�rio devera
 * informar ao programa se o arquivo � o do tipo
 * compactado ou n�o.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class CompactFileWindow extends JDialog
{
    private NodeList variablesVector;
    private JPanel centerPanel;
    private JButton yes;
    private JButton no;
    private CompactInterationController compactController;
    /**
     * Constr�i a tela essa tela possui um label
     * que pergunta ao usu�rio se o arquivo que ele
     * est� tratando � compactado ou n�o, e dois bot�es.
     * @param vetorVariaveis - As vari�veis presentes na
     * (<code>java.util.List<code>)
     * @see TVariavel
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