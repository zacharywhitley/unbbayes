package unbbayes.aprendizagem;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Classe que constroi uma tela onde usu�rio devera
 * informar ao programa se o arquivo � o do tipo
 * compactado ou n�o.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TArq extends JDialog
{
    private java.util.List vetorVariaveis;
    private JPanel painelCentro;
    private JButton sim;
    private JButton nao;

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
    TArq(java.util.List vetorVariaveis){
        super(new Frame(), "", true);
        Container container = getContentPane();
        this.vetorVariaveis = vetorVariaveis;
        container.add(new JLabel("Deseja utilizar arquivos compactados?"),BorderLayout.NORTH);
        container.add(getPainelCentro(),BorderLayout.CENTER);
        setBounds(100,100,230,65);
        setVisible(true);
    }

    private JPanel getPainelCentro(){
        painelCentro        = new JPanel(new GridLayout(1,2,5,5));
        sim                 = new JButton("Sim");
        nao                 = new JButton("N�o");
        painelCentro.add(sim);
        painelCentro.add(nao);
        sim.addActionListener(ActionSim);
        nao.addActionListener(ActionNao);
        return painelCentro;
    }

    ActionListener ActionSim = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            dispose();
            TCaixaArq caixa = new TCaixaArq(vetorVariaveis);
        }
    };

    ActionListener ActionNao = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            dispose();
        }
    };
}