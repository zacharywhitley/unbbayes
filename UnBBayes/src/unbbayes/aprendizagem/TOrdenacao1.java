package unbbayes.aprendizagem;

import javax.swing.*;
import unbbayes.fronteira.SimpleFileFilter;
import unbbayes.jprs.jbn.*;
import unbbayes.util.NodeList;
import unbbayes.controlador.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

/**
 * Classe que informa quala ordem das váriaveis.
 * A partir dessa classe é gerada uma tela com
 * quatro botoes, um para definir uma ordenaçao
 * superior para a variável,um para definir uma
 * ordenação inferior para a variável,um para abrir
 * uma caixa onde seja possivel definir pais fixos
 * para uma determinada variável, e um para o calculo
 * da funçao
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TOrdenacao1 extends JDialog
{
    private TAprendizagemTollKit Alg;
    private MainController controlador;
    private java.util.List vetorHorizontalBase;
    private NodeList vetorVariaveis;
    private NodeList variaveis;
    private JPanel painelSul;
    private JPanel painelCentro;
    private JPanel pBotao;
    private JPanel pOrdenacao;
    private JPanel pDecisao;
    public static JProgressBar progress;
    public static JLabel label;
    private JButton cima;
    private JButton baixo;
    private JButton continua;
    private JButton relacionamentos;
    private JList  JLOrdenacao;
    private DefaultListModel  modeloLista;
    private JComboBox listaAlgoritmos;
    private JTextField txtDelta;
    private byte matriz[][];
    private int vetor[];
    private int numeroCaso;
    private int linhas;
    private int delta;
    
    /*teste*/
    private double eps;

    /**
     * Método construtor que monta a tela, adiciona os
     * ouvidores, monta o vetor das variáveis e monta
     * a base de casos
     * @param arquivo - Arquivo que deve ser lido(<code>
     * File<code>)
     * @param controlador - Controlador que monta a rede bayseana
     * no JViewPort(<code>TControladorTelaPrincipal<code>)
     * @see TVariavel
     * @see StreamTokenizer
     * @see java.util.List
     */
    public  TOrdenacao1(File arquivo, MainController controlador){
    	
       super(new Frame(), "", true);
       Container painelPrincipal = getContentPane();
       this.controlador = controlador;
       TVariavel variavelAux = null;
       painelPrincipal.setLayout(new BorderLayout());
       painelPrincipal.add(getPainelCentro(),BorderLayout.CENTER);
       //Tratamento do arquivo
       try{
           InputStreamReader arquivoLeituraBytes = new InputStreamReader(new FileInputStream(arquivo));
           BufferedReader  arquivoLeituraChar    = new BufferedReader(arquivoLeituraBytes);
           String linha = arquivoLeituraChar.readLine();
           while( linha != null){
               linha = arquivoLeituraChar.readLine();
               linhas++;
           }
           arquivoLeituraBytes = new InputStreamReader(new FileInputStream(arquivo));
           arquivoLeituraChar  = new BufferedReader(arquivoLeituraBytes);
           StreamTokenizer colunas = new StreamTokenizer(arquivoLeituraChar);
           colunas.wordChars('A', 'Z');
           colunas.wordChars('a', '}');
           colunas.wordChars('_', '_');
           colunas.wordChars('-', '-');
           colunas.wordChars('0', '9');
           colunas.wordChars('.', '.');
           colunas.quoteChar('\t');
           colunas.commentChar('%');
           colunas.eolIsSignificant(true);
           vetorVariaveis = new NodeList();
           variaveis = new NodeList();
           int posicao = 0;

           /*Montagem do vetor de variáveis*/
           while (colunas.nextToken() != StreamTokenizer.TT_EOL){
               if(colunas.sval != null){
                  vetorVariaveis.add(new TVariavel(colunas.sval, posicao));
               } else{
                  vetorVariaveis.add(new TVariavel(String.valueOf(colunas.nval),posicao));
               }
               posicao++;
           }
           new ChooseVariablesWindow(vetorVariaveis);
           new CompactFileWindow(vetorVariaveis);
           int cont = 0;
           for (int i = 0; i < vetorVariaveis.size();i++){
             TVariavel aux = (TVariavel)vetorVariaveis.get(i);
             if(aux.getParticipa()){
                 if (!aux.getRep()){
                      insereLista(aux.getName());
                      aux.setPos(cont);
                      variaveis.add(aux);
                      cont++;
                 } else{
                      vetor = new int[linhas];
                      TAprendizagemTollKit.compactado = true;
                 }
             }
           }
           matriz = new byte[linhas][variaveis.size()];
           posicao = 0;
           String nomeEstado = "";
           System.out.println("Linhas = " + linhas);
           System.out.println("tamanho = "+variaveis.size());
           /*Criação da matriz da base de casos*/
           while (colunas.ttype != StreamTokenizer.TT_EOF && numeroCaso <= linhas){
               while(colunas.ttype != StreamTokenizer.TT_EOL && posicao < vetorVariaveis.size() && numeroCaso <= linhas){
                   variavelAux = (TVariavel)vetorVariaveis.get(posicao);
                   if (variavelAux.getRep()){
                     vetor[numeroCaso] = (int)colunas.nval;
                   } else if(variavelAux.getParticipa()){
                        if(colunas.sval != null){
                            nomeEstado = colunas.sval;
                            if(! variavelAux.existeEstado(nomeEstado)){
                                variavelAux.adicionaEstado(nomeEstado);
                            }
                        } else{
                            nomeEstado = String.valueOf(colunas.nval);
                            if (! variavelAux.existeEstado(nomeEstado)){
                                variavelAux.adicionaEstado(nomeEstado);
                            }
                        }
                        matriz[numeroCaso][variavelAux.getPos()] = (byte)variavelAux.getEstadoPosicao(nomeEstado);
                   }
                   colunas.nextToken();
                   posicao++;
               }
               numeroCaso++;
               while (colunas.ttype != StreamTokenizer.TT_EOL && numeroCaso < linhas ){
                   colunas.nextToken();
               }
               posicao = 0;
               colunas.nextToken();
           }
           System.out.println("NumeroCasos " + numeroCaso);
           arquivoLeituraChar.close();
       } catch (IOException e){
             System.out.println("Erro de leitura de arquivo :" + e);
       }
       painelSul = new JPanel(new GridLayout(2,1,5,5));
       label = new JLabel("Variavel : ");
       TOrdenacao1.progress = new JProgressBar(JProgressBar.HORIZONTAL,0,100);
       TOrdenacao1.progress.setStringPainted(true);
       painelSul.add(label);
       painelSul.add(TOrdenacao1.progress);
       painelPrincipal.add(painelSul,BorderLayout.SOUTH);
       setBounds(100,100,300,280);
       setResizable(false);
       setVisible(true);
    }

    private JPanel getPainelCentro(){
        painelCentro    = new JPanel(new GridLayout(1,2));
        painelCentro.add(getOrdenacao());
        painelCentro.add(getDecisao());
        return painelCentro;
    }

    private JPanel getOrdenacao(){
       pOrdenacao    = new JPanel(new BorderLayout());
       modeloLista   = new DefaultListModel();
       JLOrdenacao   = new JList(modeloLista);
       JScrollPane JLOrdenacaoScrollPane = new JScrollPane(JLOrdenacao);
       JLOrdenacao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       JLOrdenacao.setSelectedIndex(0);
       pOrdenacao.add(new JLabel("Ordenacao"), BorderLayout.NORTH);
       pOrdenacao.add(JLOrdenacaoScrollPane, BorderLayout.CENTER);
       JLOrdenacao.addMouseListener(eventoClick);
       return pOrdenacao;
    }

    private JPanel getDecisao(){
        pDecisao  = new JPanel(new BorderLayout());
        pDecisao.add(new JLabel("Algoritmos"), BorderLayout.NORTH);
        pDecisao.add(getPainelBotao(),BorderLayout.CENTER);
        return pDecisao;
    }

    private JPanel getPainelBotao(){
        pBotao          = new JPanel(new GridLayout(8,1,5,5));
        listaAlgoritmos = new JComboBox();
        txtDelta        = new JTextField();
        cima            = new JButton("Cima");
        baixo           = new JButton("Baixo");
        continua        = new JButton("Continuar");
        relacionamentos = new JButton("Relacionamentos");
        listaAlgoritmos.addItem("CBL");
        listaAlgoritmos.addItem("K2_MDL");
        listaAlgoritmos.addItem("B_MDL");
        listaAlgoritmos.addItem("K2_GHS");
        listaAlgoritmos.addItem("B_GHS");
        listaAlgoritmos.addItem("K2_GH");
        listaAlgoritmos.addItem("B_GH");
        pBotao.add(listaAlgoritmos);
        pBotao.add(cima);
        pBotao.add(baixo);
        pBotao.add(relacionamentos);
        pBotao.add(continua);
        pBotao.add(new  JLabel("Delta"));
        pBotao.add(txtDelta);
        continua.addActionListener(continuaEvento);
        relacionamentos.addActionListener(relacionamentoEvento);
        cima.addActionListener(cimaEvento);
        baixo.addActionListener(baixoEvento);
        return pBotao;
    }
    private void insereLista(String nome)
    {
        modeloLista.addElement(nome);
    }

    /*Listener mudar a ordenacao de uma variável para cima*/
    ActionListener cimaEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            NodeList vetorAux = new NodeList();
            String nomeAux = (String)JLOrdenacao.getSelectedValue();
            TVariavel variavelAux = null;
            int index = JLOrdenacao.getSelectedIndex();
            if (index != 0){
                modeloLista.remove(index);
                index--;
                modeloLista.add(index, nomeAux);
                for (int i = 0 ; i < vetorVariaveis.size() ; i++ ){
                    variavelAux = (TVariavel)variaveis.get(i);
                    if (variavelAux.getName().equals(nomeAux)){
                        variaveis.remove(i);
                        for(int j = 0; j < i - 1 ; j++){
                            vetorAux.add(variaveis.get(j));
                        }
                        vetorAux.add(variavelAux);
                        for (int k = i -1 ; k < variaveis.size() ; k++){
                            vetorAux.add(variaveis.get(k));
                        }
                        variaveis = vetorAux;
                        for (int g = 0 ;g < variaveis.size() ; g++ ){
                            variavelAux = (TVariavel)variaveis.get(g);
                        }
                        JLOrdenacao.setSelectedIndex(index);
                        break;
                    }
                }
            }
        }
    };

    /*Listener mudar a ordenacao de uma variável para baixo*/
    ActionListener baixoEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            String nomeAux = (String)JLOrdenacao.getSelectedValue();
            TVariavel variavelAux = null;
            int index = JLOrdenacao.getSelectedIndex();
            if (index < modeloLista.getSize()-1){
                modeloLista.remove(index);
                index++;
                modeloLista.add(index, nomeAux);
                NodeList vetorAux = new NodeList();
                for (int i = 0 ; i < variaveis.size() ; i++ ){
                    variavelAux = (TVariavel)variaveis.get(i);
                    if (variavelAux.getName().equals(nomeAux)){
                        variaveis.remove(i);
                        for(int j = 0; j < i +1 ; j++){
                            vetorAux.add(variaveis.get(j));
                        }
                        vetorAux.add(variavelAux);
                        for (int k = i +1 ; k < variaveis.size() ; k++){
                            vetorAux.add(variaveis.get(k));
                        }
                        variaveis = vetorAux;
                        for (int g = 0 ;g < variaveis.size() ; g++ ){
                            variavelAux = (TVariavel)variaveis.get(g);
                        }
                        JLOrdenacao.setSelectedIndex(index);
                        break;
                    }
                }
            }
        }
    };

    /*Esse listener abre a caixa de relacionamentos, onde é possivel
      definir com certeza quais os pais de uma determinada variável*/
    ActionListener relacionamentoEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            new TCaixaRelacionamentos(variaveis);
        }
    };

    /*Esse listener apenas continua o processo, chamando o procedimento K2*/
    ActionListener continuaEvento = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            try {
                //delta = Integer.parseInt(txtDelta.getText());
                eps = Double.parseDouble(txtDelta.getText());
                System.out.println("Delta = " +delta);
                System.out.println("Tamanho Variaveis = " + variaveis.size());
                Thread t = new Thread(new Runnable(){
                    public void run(){
						for (int i = 0; i < variaveis.size();i++ ){
                            TVariavel variavelAux = (TVariavel)variaveis.get(i);
                            normalize(variavelAux);
                        }
                        dispose();
                        ProbabilisticNetwork net = controlador.makeNetwork(variaveis);
                        if(listaAlgoritmos.getSelectedItem().equals("K2_GH")){
                            Alg = new TAlgoritmoK2_GH(variaveis,matriz,numeroCaso,vetor,delta,net);
							//Alg = new TAlgoritmoK2_GH(variaveis,matriz,numeroCaso,vetor,delta,controlador);
                        } else if(listaAlgoritmos.getSelectedItem().equals("K2_GHS")){
                            Alg = new TAlgoritmoK2_GHS(variaveis,matriz,numeroCaso,vetor,delta,net);
							//Alg = new TAlgoritmoK2_GHS(variaveis,matriz,numeroCaso,vetor,delta,controlador);
                        } else if(listaAlgoritmos.getSelectedItem().equals("K2_MDL")){
                            Alg = new TAlgoritmoK2_MDL(variaveis,matriz,numeroCaso, vetor,delta,net);
							//Alg = new TAlgoritmoK2_MDL(variaveis,matriz,numeroCaso, vetor,delta,controlador);
                        } else if(listaAlgoritmos.getSelectedItem().equals("B_MDL")){
                            Alg = new TAlgoritmoB_MDL(variaveis,matriz,numeroCaso,vetor,net);
							//Alg = new TAlgoritmoB_MDL(variaveis,matriz,numeroCaso,vetor,controlador);
                        } else if(listaAlgoritmos.getSelectedItem().equals("B_GHS")){
                            Alg = new TAlgoritmoB_GHS(variaveis,matriz,numeroCaso,vetor,net);
							//Alg = new TAlgoritmoB_GHS(variaveis,matriz,numeroCaso,vetor,controlador);
                        } else if(listaAlgoritmos.getSelectedItem().equals("B_GH")){
                            Alg  = new TAlgoritmoB_GHS(variaveis,matriz,numeroCaso,vetor,net);
							//Alg  = new TAlgoritmoB_GHS(variaveis,matriz,numeroCaso,vetor,controlador);
                        } else{
                            TCBL Alg1 = new TCBL(variaveis,matriz,numeroCaso,vetor,eps,net);                    	
                        }
                    }
                });
                t.start();

            } catch(Exception e){
                System.out.println("Erro " + e);
            }
        }
    };

    MouseListener eventoClick = new MouseListener(){
        public void mouseClicked(MouseEvent e){
            int nClick = e.getClickCount();
            if (nClick == 2){
                JList lista = (JList)e.getSource();
                if (!lista.isSelectionEmpty()){
                    TVariavel variavelLista = null;
                    Object objeto = lista.getSelectedValue();
                    String nome = objeto.toString();
                    for (int i = 0 ; i < variaveis.size(); i++ ){
                        variavelLista = (TVariavel)variaveis.get(i);
                        if (variavelLista.getName().equals(nome)){
                            break;
                        }
                    }
                    new TTelaOpcoesVariavel(variavelLista);
                }
            }
        }
        public void mousePressed(MouseEvent e){}
        public void mouseReleased(MouseEvent e){}
        public void mouseEntered(MouseEvent e){}
        public void mouseExited(MouseEvent e){}

    };

    /**
     * Método que normaliza uma variavel do grafo.
     *@param  variavel - Variavel a ser normalizada
     *(<code>TVariavel<code>)
     *@see TTabPot
     */
    private void normalize(TVariavel variavel) {
        for (int c = 0; c < variavel.getPotentialTable().tableSize()/*.getDados().size()*/; c+=variavel.getEstadoTamanho()/*.noEstados()*/){
            double soma = 0.0;
            for (int i = 0; i < variavel.getEstadoTamanho()/*.noEstados()*/; i++){
               soma += variavel.getPotentialTable().getValue(c+i);
            }
            if (soma == 0){
                for (int i = 0; i < variavel.getEstadoTamanho()/*.noEstados()*/; i++){
                    variavel.getPotentialTable().setValue(c+i, 1.0/variavel.getEstadoTamanho()/*.noEstados()*/);
                }
            } else{
                 for (int i = 0; i < variavel.getEstadoTamanho()/*.noEstados()*/; i++){
                     variavel.getPotentialTable().setValue(c+i, variavel.getPotentialTable().getValue(c+i)/soma);
                 }
            }
        }
    }
}