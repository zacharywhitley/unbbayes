package unbbayes.aprendizagem;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Classe que controi uma tela onde o usuário deverá
 * definir qual a variável que deverá conter o numero
 * de vezes que um certo caso se repete no arquivo.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */

public class TCaixaArq extends JDialog
{
    private java.util.List vetorVariaveis;
    private JPanel painelCentro;
    private JButton ok;
    private JButton cancelar;
    private JComboBox variavel;

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
    TCaixaArq(java.util.List vetorVariaveis){
        super(new Frame(), "", true);
        Container container = getContentPane();
        this.vetorVariaveis = vetorVariaveis;
        container.add(getPainelCentro());
        container.add(new JLabel("Informe a variavel que contem o numero de repeticoes"),BorderLayout.NORTH);
        setBounds(100,100,320,65);
        setVisible(true);
    }

    private JPanel getPainelCentro(){
        painelCentro = new JPanel(new GridLayout(1,3,5,5));
        ok           = new JButton("OK");
        cancelar     = new JButton("Cancelar");
        variavel     = new JComboBox();
        variavel.setMaximumRowCount(5);
        for (int i = 0 ; i < vetorVariaveis.size() ;i++ ){
            TVariavel aux = (TVariavel)vetorVariaveis.get(i);
            if(aux.getParticipa()){
               variavel.addItem(aux.getName());
            }
        }
        ok.addActionListener(ActionOk);
        cancelar.addActionListener(ActionCancelar);
        painelCentro.add(variavel);
        painelCentro.add(ok);
        painelCentro.add(cancelar);
        return painelCentro;
    }

    ActionListener ActionOk = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            String nome = (String)variavel.getSelectedItem();
            TVariavel aux;
            for (int i = 0 ; i < vetorVariaveis.size() ;i++ ){
                aux = (TVariavel)vetorVariaveis.get(i);
                if (aux.getName().equals(nome)){
                    System.out.println("Sigla = " + aux.getName());
                    aux.isRep(true);
                    break;
                }
            }
            dispose();
        }
    };

    ActionListener ActionCancelar = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            dispose();
        }
    };

}

