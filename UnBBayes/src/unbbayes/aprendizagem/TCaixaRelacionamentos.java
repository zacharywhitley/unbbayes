package unbbayes.aprendizagem;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import unbbayes.util.NodeList;

/**
 * Classe para a construção de uma tela que possui
 * como finalidade definir relacionamentos entre as
 * variáveis, lembrando que nunca um ancestral pode
 * ser filho da propria variável.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TCaixaRelacionamentos extends JDialog{
    private JPanel PCaixaRolagem;
    private JPanel PCaixaRelacionamentos;
    private JPanel JRelacionamentos;
    private JPanel JBotao;
    private JPanel PPais;
    private JPanel PFilhos;
    private JList  JPCaixaPais;
    private JList JPCaixaFilhos;
    private JList JLRelacionamentos;
    private DefaultListModel modeloLista;
    private DefaultListModel modeloListaPais;
    private DefaultListModel modeloListaFilhos;
    private JButton adiciona;
    private JButton remove;
    private JButton continua;
    private NodeList vetorGrafo;

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
    public  TCaixaRelacionamentos(NodeList vetor){
       super(new Frame(), "", true);
       vetorGrafo = vetor;
       Container painelPrincipal = getContentPane();
       painelPrincipal.add(getCaixaRelacionamentos());
       for (int i = 0 ; i < vetor.size(); i++ ){
           TVariavel variavelAux = (TVariavel)vetorGrafo.get(i);
           insereLista(variavelAux.getName());
       }
       setBounds(100,100,500,300);
       setResizable(false);
       setVisible(true);
    }

    private JPanel getPais(){
        PPais                             = new JPanel(new BorderLayout());
        modeloListaPais                   = new DefaultListModel();
        JPCaixaPais                       = new JList(modeloListaPais);
        JScrollPane JPCaixaPaisScrollPane = new JScrollPane(JPCaixaPais);
        JPCaixaPais.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPCaixaPais.setSelectedIndex(0);
        PPais.add(new JLabel("Pais"),BorderLayout.NORTH);
        PPais.add(JPCaixaPaisScrollPane, BorderLayout.CENTER);
        return PPais;
    }

    private JPanel getFilhos(){
        PFilhos                            = new JPanel(new BorderLayout());
        modeloListaFilhos                  = new DefaultListModel();
        JPCaixaFilhos                      = new JList(modeloListaFilhos);
        JScrollPane JPCaixaFilhosScrollPane = new JScrollPane(JPCaixaFilhos);
        JPCaixaFilhos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPCaixaFilhos.setSelectedIndex(0);
        PFilhos.add(new JLabel("Filhos"),BorderLayout.NORTH);
        PFilhos.add(JPCaixaFilhosScrollPane, BorderLayout.CENTER);
        return PFilhos;
    }

    private JPanel getBotoes(){
       JBotao                = new JPanel(new GridLayout(8,1,10,10));
       adiciona              = new JButton("Adicionar");
       remove                = new JButton("Remover");
       continua              = new JButton("Continuar");
       JBotao.add(new JLabel(""));
       JBotao.add(new JLabel(""));
       JBotao.add(adiciona);
       JBotao.add(new JLabel(""));
       JBotao.add(new JLabel(""));
       JBotao.add(remove);
       JBotao.add(continua);
       continua.addActionListener(continuaEvento);
       adiciona.addActionListener(adicionaEvento);
       remove.addActionListener(retiraEvento);
       return JBotao;
    }

    private JPanel getRelacionamentos(){
        JRelacionamentos      = new JPanel(new BorderLayout());
        modeloLista           = new DefaultListModel();
        JLRelacionamentos     = new JList(modeloLista);
        JLRelacionamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane JLRelacionamentosScrollPane = new JScrollPane(JLRelacionamentos);
        JRelacionamentos.add(new JLabel("Relacionamentos"), BorderLayout.NORTH);
        JRelacionamentos.add(JLRelacionamentosScrollPane, BorderLayout.CENTER);
        return JRelacionamentos;
    }

    private JPanel getCaixaRelacionamentos(){
        PCaixaRelacionamentos = new JPanel(new GridLayout(1,5,10,10));
        PCaixaRelacionamentos.add(getPais());
        PCaixaRelacionamentos.add(getFilhos());
        PCaixaRelacionamentos.add(getBotoes());
        PCaixaRelacionamentos.add(getRelacionamentos());
        return PCaixaRelacionamentos;
    }
    private void insereLista(String nome){
        modeloListaPais.addElement(nome);
        modeloListaFilhos.addElement(nome);
    }

    ActionListener continuaEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            dispose();
        }
    };


    ActionListener retiraEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            Object relacionamento     = JLRelacionamentos.getSelectedValue();
            String nomeRelacionamento = (String)relacionamento;
            java.util.List vetorAux = new ArrayList();
            if (!JLRelacionamentos.isSelectionEmpty() && relacionamento != null){
                int index   = nomeRelacionamento.indexOf('-');
                int tamanho = nomeRelacionamento.length();
                boolean achou = false;
                String nomePai   = nomeRelacionamento.substring(0,index);
                String nomeFilho = nomeRelacionamento.substring(index +3,tamanho);
                for (int i = 0; i < vetorGrafo.size() ; i++ ){
                    TVariavel variavelAux = (TVariavel)vetorGrafo.get(i);
                    if (variavelAux.getName().equals(nomeFilho)){
                        vetorAux =  (java.util.List)variavelAux.getPais();
                        for (int j = 0 ; j < vetorAux.size() ;j++ ){
                            TVariavel variavelAux1 = (TVariavel)vetorAux.get(j);
                            if (variavelAux1.getName().equals(nomePai)){
                                vetorAux.remove(j);
                                modeloLista.removeElement(relacionamento);
                                achou = true;
                                break;
                            }
                        }
                        if (achou){
                            break;
                        }
                    }
                }
            }
        }
    };


    ActionListener adicionaEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
          Object indice1 = JPCaixaPais.getSelectedValue();
          Object indice2 = JPCaixaFilhos.getSelectedValue();
          String indiceRelacionamento = indice1 +  "-->" + indice2;
          boolean existe = false;
          if (!(indiceRelacionamento.equals("null-->null"))){
              for(int i = 0; i < vetorGrafo.size(); i++){
                  TVariavel variavelAux = (TVariavel)vetorGrafo.get(i);
                  if (variavelAux.getName().equals(""+indice2)){
                      for(int j = 0; j < vetorGrafo.size(); j++){
                          if (i != j){
                            variavelAux = (TVariavel)vetorGrafo.get(j);
                            if (variavelAux.getName().equals(""+indice1)){
                                variavelAux = (TVariavel)vetorGrafo.get(j);
                                if (!(variavelAux.getPai(""+indice2).equals(""+indice2))){
                                    variavelAux = (TVariavel)vetorGrafo.get(i);
                                    java.util.List vetorAux = (java.util.List)variavelAux.getPais();
                                    for (int k = 0; k < vetorAux.size() ; k++ ){
                                        TVariavel variavelAux1 = (TVariavel)vetorAux.get(k);
                                        if (variavelAux1.getName().equals(""+indice1)){
                                            existe = true;
                                            break;
                                        } else{
                                            existe = false;
                                        }
                                    }
                                    if (!existe){                                       variavelAux.adicionaPai((TVariavel)vetorGrafo.get(j));
                                          modeloLista.addElement(indiceRelacionamento);
                                    }
                                }
                            }
                          }
                        }
                    }
              }
          }
        }
    };

}