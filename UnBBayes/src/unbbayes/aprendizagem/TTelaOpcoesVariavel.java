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
public class TTelaOpcoesVariavel extends JDialog
{
    private TVariavel variavel;
    private JTextField txtNumeroMaximoPais;
    private JTextField txtNome;
    private JLabel lNumeroMaximoPais;
    private JLabel lNome;
    private JLabel lEstados;
    private JPanel pPainelCentro;
    private JPanel pPainelNorte;
    private JPanel pPainelNome;
    private JPanel pPainelMaximo;
    private JPanel pPainelRepeticoes;
    private JPanel  pPainelEstados;
    private JList  lsEstados;
    private DefaultListModel  modeloListaEstados;
    private JButton bAplicar;
    private JButton bCancelar;

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
    TTelaOpcoesVariavel(TVariavel no){
        super(new Frame(),"Op��es das Vari�veis",true);
        variavel = no;
        Container painelPrincipal  = getContentPane();
        painelPrincipal.add(getPainelNorte(), BorderLayout.NORTH);
        painelPrincipal.add(getPainelCentro(), BorderLayout.CENTER);
        setBounds(100,100,290,250);
        setResizable(false);
        setVisible(true);
    }

    private JPanel getPainelNome(){
        pPainelNome  = new JPanel(new GridLayout(1,2,10,10));
        lNome        = new JLabel("          Nome");
        txtNome      = new JTextField(variavel.getName());
        txtNome.disable();
        pPainelNome.add(lNome);
        pPainelNome.add(txtNome);
        return pPainelNome;
    }

    private JPanel getPainelMaximo(){
        pPainelMaximo        = new JPanel(new GridLayout(1,2));
        lNumeroMaximoPais    = new JLabel("M�ximo");
        txtNumeroMaximoPais  = new JTextField("" + variavel.getNumeroMaximoPais());
        pPainelMaximo.add(lNumeroMaximoPais);
        pPainelMaximo.add(txtNumeroMaximoPais);
        return pPainelMaximo;
    }

    private JPanel getPainelNorte(){
        pPainelNorte  = new JPanel(new GridLayout(1,2,10,10));
        pPainelNorte.add(getPainelNome());
        pPainelNorte.add(getPainelMaximo());
        return pPainelNorte;
    }

    private JPanel getPainelRepeticoes(){
        pPainelRepeticoes = new JPanel(new GridLayout(7,1,5,5));
        bAplicar          = new JButton("Aplicar");
        bCancelar         = new JButton("Cancelar");
        pPainelRepeticoes.add(new JLabel(""));
        pPainelRepeticoes.add(new JLabel(""));
        pPainelRepeticoes.add(new JLabel(""));
        pPainelRepeticoes.add(bAplicar);
        pPainelRepeticoes.add(bCancelar);
        pPainelRepeticoes.add(new JLabel(""));
        pPainelRepeticoes.add(new JLabel(""));
        bAplicar.addActionListener(aplica);
        bCancelar.addActionListener(cancela);
        return pPainelRepeticoes;
    }

    private JPanel getPainelEstados(){
        pPainelEstados        = new JPanel(new BorderLayout());
        lEstados              = new JLabel("Estados");
        modeloListaEstados    = new DefaultListModel();
        lsEstados             = new JList(modeloListaEstados);
        JScrollPane scEstados = new JScrollPane(lsEstados);
        lsEstados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        java.util.List vetorAux = (java.util.List)variavel.getEstados();
        for (int j = 0; j < vetorAux.size(); j++ ){
            String estadoAux = (String)vetorAux.get(j);
            modeloListaEstados.addElement(estadoAux);
        }
        pPainelEstados.add(scEstados, BorderLayout.CENTER);
        pPainelEstados.add(lEstados, BorderLayout.NORTH);
        return pPainelEstados;
    }

    private JPanel getPainelCentro(){
        pPainelCentro = new JPanel(new GridLayout(1,2,10,10));
        pPainelCentro.add(getPainelEstados());
        pPainelCentro.add(getPainelRepeticoes());
        return pPainelCentro;


    }


    ActionListener aplica = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            if (! txtNumeroMaximoPais.getText().equals("")){
                try{
                    int maximo = Integer.parseInt(txtNumeroMaximoPais.getText());
                    variavel.setNumeroMaximoPais(maximo);
                    dispose();
                }catch (NumberFormatException e){
                    System.out.println("Digite um inteiro valido");
                }
            }
        }
    };

    ActionListener cancela = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            dispose();
        }
    };
}
