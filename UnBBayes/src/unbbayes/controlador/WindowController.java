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

package unbbayes.controlador;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.shetline.io.*;
import unbbayes.fronteira.*;
import unbbayes.jprs.jbn.*;
import unbbayes.util.NodeList;

/**
 *  Essa classe implementa o <code>KeyListener</code> e o <code>
 *  AdjustmentListener</code> para tratar eventos de tecla do <code>TDesenhaRede
 *  </code>e de ajuste do scroll do <code>jspDesenho</code> . Essa classe é
 *  responsável principalmente por fazer a ligação entre interface e lógica.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    27 de Junho de 2001
 * @see        KeyListener
 * @see        AdjustmentListener
 * @version    1.0 24/06/2001
 */
public class WindowController implements KeyListener {

    private NetWindow tela;
    private ProbabilisticNetwork rede;

    private NumberFormat df;

    private boolean[] situacaoArvore;

    private List copia;
    private List copiados;

    private boolean bColou;

    /** Load resource file from this package */
    private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controlador.resources.ControllerResources");

    private WindowController() {}

    /**
     *  Constrói o controlador responsável pela criação da rede Bayesiana ( <code>
     *  TRP</code> ) e da tela principal ( <code>IUnBBayes</code> ). Além
     *  disso, este construtor também adiciona AdjustmentListener para os
     *  JScrollBars do <code>jspDesenho</code> .
     *
     * @since
     * @see      KeyListener
     */
    public WindowController(ProbabilisticNetwork _rede, NetWindow _tela) {
        this.rede = _rede;
        this.tela = _tela;
        df = NumberFormat.getInstance(Locale.US);
        df.setMaximumFractionDigits(4);
        copia = new ArrayList();
        copiados = new ArrayList();
    }


    /**
     *  Retorna a tela principal ( <code>IUnBBayes</code> ).
     *
     * @return    retorna a tela <code>IUnBBayes</code>
     * @since
     * @see       unbbayes.fronteira.IUnBBayes
     */
    public NetWindow getTela() {
        return this.tela;
    }


    /**
     * Converte um componente em uma imagem.
     */
    private BufferedImage graphicsToImage(Component graf, Rectangle r){
        BufferedImage buffImg = new BufferedImage(graf.getWidth(),

        graf.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        graf.setVisible(true);
        Graphics g = (Graphics)buffImg.createGraphics();

       /*
        if (r != null) {
           graf.setSize(r.getSize());
        }
        */

        graf.paint(g);
        g.dispose();
        return(buffImg);
    }


    /**
     *  Retorna a tela rede Bayesiana ( <code>TRP</code> ).
     *
     * @return    retorna a rede <code>TRP</code>
     */
    public ProbabilisticNetwork getRede() {
        return this.rede;
    }

    public boolean[] getSituacaoArvore() {
      return this.situacaoArvore;
    }


    /**
     * Salva a imagem da rede para um arquivo.
     */
    public void salvarImagemRede() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(".");
        chooser.setMultiSelectionEnabled(false);

        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        chooser.setFileView(new FileIcon(tela));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(tela);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                Rectangle r = calcularBordasRede();
                out.write(graphicsToImage(tela.getIGraph().getGraphViewport(), r));
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Salva a imagem da tabela para um arquivo.
     */
    public void salvarImagemTabela() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(".");
        chooser.setMultiSelectionEnabled(false);


        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        chooser.setFileView(new FileIcon(tela));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(tela);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                out.write(graphicsToImage(tela.getTable(), null));
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *  Insere novo estado no nó selecionado.
     *
     * @param  no  o <code>Object <code>selecionado.
     * @since
     * @see        Object
     */
    public void inserirEstado(Node no) {
    	if (no instanceof ProbabilisticNode) {
        	no.appendState(resource.getString("stateProbabilisticName") + no.getStatesSize());
    	} else if (no instanceof DecisionNode) {
    		no.appendState(resource.getString("stateDecisionName") + no.getStatesSize());
    	}
        tela.setTable(retornarTabela(no));
    }


    /**
     *  Remove último estado do nó selecionado.
     *
     * @param  no  o <code>Object <code>selecionado.
     * @since
     * @see        Object
     */
    public void removerEstado(Node no) {
        no.removeLastState();
        tela.setTable(retornarTabela(no));
    }


    /**
     *  Abre uma nova janela modal para inserir os dados para serem usados no
     *  likelihood.
     *
     * @param  caminho  um <code>TreePath <code>dizendo a posição do mouse.
     * @since
     * @see             TreePath
     */
    public void mostrarLikelihood(DefaultMutableTreeNode node) {
        ProbabilisticNode auxVP = (ProbabilisticNode) node.getUserObject();
        int i;
        JPanel panel = new JPanel();
        JTable tabela = new JTable(auxVP.getStatesSize(), 2);
        for (i = 0; i < auxVP.getStatesSize(); i++) {
            tabela.setValueAt(auxVP.getStateAt(i), i, 0);
            tabela.setValueAt("100", i, 1);
        }
        JLabel label = new JLabel(auxVP.toString());
        panel.add(label);
        panel.add(tabela);
        if (JOptionPane.showConfirmDialog(tela, panel, resource.getString("likelihoodName"), JOptionPane.OK_CANCEL_OPTION) ==
                JOptionPane.OK_OPTION) {

            DefaultMutableTreeNode auxNode;

            double[] valores = new double[auxVP.getStatesSize()];

            try {
                for (i = 0; i < auxVP.getStatesSize(); i++) {
                    valores[i] = df.parse((String) tabela.getValueAt(i, 1)).doubleValue();
                }
            } catch (ParseException e) {
                System.err.println(e.getMessage());
                return;
            }

            double valorMax = valores[0];
            for (i = 1; i < auxVP.getStatesSize(); i++) {
                if (valorMax < valores[i]) {
                    valorMax = valores[i];
                }
            }

            if (valorMax == 0.0) {
                System.err.println(resource.getString("likelihoodException"));
                return;
            }

            for (i = 0; i < auxVP.getStatesSize(); i++) {
                valores[i] /= valorMax;
            }

            for (i = 0; i < valores.length && valores[i] == 1; i++)
                ;
            if (i == valores.length) {
                return;
            }

            String str;
            auxVP.addLikeliHood(valores);
            for (i = 0; i < node.getChildCount(); i++) {
                auxNode = (DefaultMutableTreeNode) node.getChildAt(i);
                str = (String) auxNode.getUserObject();
                auxNode.setUserObject(str.substring(0, str.lastIndexOf(':') + 1) + df.format(valores[i] * 100));
            }
            ((DefaultTreeModel) tela.getEvidenceTree().getModel()).reload(node);
        }
    }


    /**
     * Inicia as crenças da árvore de junção.
     */
    public void initialize() {
        rede.initialize();
        this.updateTree();
    }


    /**
     *  Propaga as evidências da rede Bayesiana ( <code>TRP</code> ).
     *
     * @since
     */
    public void propagar() {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        boolean temLikeliHood = false;
        if (rede.updateEvidences()) {
            if (! temLikeliHood) {
                NetWindow.getInstance().setStatus(resource.getString("statusEvidenceProbabilistic") + rede.PET() * 100.0);
            }
        } else {
            JOptionPane.showMessageDialog(tela, resource.getString("statusEvidenceException"), resource.getString("statusError"), JOptionPane.ERROR_MESSAGE);
        }
        updateTree();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     *  Retrai todos os nós da árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a rede Bayesiana em
     *      forma de árvore.
     * @since
     * @see            JTree
     */
    public void collapseTree(JTree arvore) {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        for (int i = 0; i < arvore.getRowCount(); i++) {
            arvore.collapseRow(i);
        }

        for (int i = 0; i < situacaoArvore.length; i++) {
          situacaoArvore[i] = false;
        }

        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     *  Expande todos os nós da árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a rede Bayesiana em
     *      forma de árvore.
     * @since
     * @see            JTree
     */
    public void expandTree(JTree arvore) {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        for (int i = 0; i < arvore.getRowCount(); i++) {
            arvore.expandRow(i);
        }

        for (int i = 0; i < situacaoArvore.length; i++) {
          situacaoArvore[i] = true;
        }

        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     *  Mostra todas os passos realizados na compilação da rede Bayesiana em um
     *  <code>JDialog</code> .
     *
     * @since
     * @see      JDialog
     */
    public void showLog() {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final JTextArea texto = new JTextArea();

        texto.setEditable(false);
        texto.setText(rede.getLog());
        texto.moveCaretPosition(0);
        texto.setSelectionEnd(0);

//            texto.setRows(linhas);
        texto.setSize(texto.getPreferredSize());
        texto.append("\n");
//            arq.close();

        final JDialog dialog = new JDialog();
        JScrollPane jspTexto = new JScrollPane(texto);
        jspTexto.setPreferredSize(new Dimension(450, 400));

        JPanel panel = new JPanel(new BorderLayout());
        JButton botaoImprimir = new JButton(new ImageIcon("icones/IMPRIMIR.gif"));
        botaoImprimir.setToolTipText(resource.getString("printLogToolTip"));
        JButton botaoVisualizar = new JButton(new ImageIcon("icones/VISUALIZAR.gif"));
        botaoVisualizar.setToolTipText(resource.getString("previewLogToolTip"));
        botaoImprimir.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    printLog(texto);
                }
            });
        botaoVisualizar.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    previewPrintLog(texto, dialog);
                }
            });

        panel.add(jspTexto, BorderLayout.CENTER);

        JPanel topPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPane.add(botaoImprimir);
        topPane.add(botaoVisualizar);
        panel.add(topPane, BorderLayout.NORTH);

        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoOK = new JButton(resource.getString("okButtonLabel"));
        botaoOK.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    dialog.dispose();
                }
            });


        bottomPane.add(botaoOK);
        panel.add(bottomPane, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setVisible(true);
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     *  Adiciona uma evidencia no estado especificado.
     *
     * @param  caminho  caminho do estado a ser setado para 100%;
     * @see             TreePath
     */
    public void arvoreDuploClick(DefaultMutableTreeNode treeNode) {
        DefaultMutableTreeNode pai = ((DefaultMutableTreeNode) treeNode.getParent());
        TreeVariable node = (TreeVariable) pai.getUserObject();


        for (int i = 0; i < pai.getChildCount(); i++) {
            DefaultMutableTreeNode auxNode = (DefaultMutableTreeNode) pai.getChildAt(i);
            auxNode.setUserObject(node.getStateAt(i) + ": 0");
        }

        if (node instanceof ProbabilisticNode) {
            treeNode.setUserObject(node.getStateAt(pai.getIndex(treeNode)) + ": 100");
        } else {
            treeNode.setUserObject(node.getStateAt(pai.getIndex(treeNode)) + ": **");
        }
        node.addFinding(pai.getIndex(treeNode));
        ((DefaultTreeModel) tela.getEvidenceTree().getModel()).reload(pai);
    }


    /**
     *  Compila a rede Bayesiana. Caso haja algum problema na compilação, mostra-se
     *  o erro em um <code>JOptionPane</code> .
     *
     * @return    true se a rede compilar sem problemas e false se houver algum
     *      problema na compilação
     * @since
     * @see       JOptionPane
     */
    public boolean compileNetwork() {
        long ini = System.currentTimeMillis();
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            rede.compile();
        } catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), resource.getString("statusError"), JOptionPane.ERROR_MESSAGE);
            tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return false;
        }

        // Ordenar pela descricao do nó apenas para facilitar a visualização da árvore.
        NodeList nos = rede.getCopiaNos();
        boolean haTroca = true;
        while (haTroca) {
            haTroca = false;
            for (int i = 0; i < nos.size() - 1; i++) {
                Node node1 = nos.get(i);
                Node node2 = nos.get(i + 1);
                if (node1.getDescription().compareToIgnoreCase(node2.getDescription()) > 0) {
                    nos.set(i + 1, node1);
                    nos.set(i, node2);
                    haTroca = true;
                }
            }
        }

        situacaoArvore = new boolean[nos.size()];

        for (int i = 0; i < situacaoArvore.length; i++) {
            situacaoArvore[i] = false;
        }

        updateTree();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        NetWindow.getInstance().setStatus(resource.getString("statusTotalTime") + (((double)(System.currentTimeMillis() - ini))/1000) + resource.getString("statusSeconds"));
        return true;
    }


    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>Node</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.jprs.jbn.Node
     */
    public void insertProbabilisticNode(double x, double y) {
        ProbabilisticNode node = new ProbabilisticNode();
        node.setPosicao(x, y);
        node.appendState(resource.getString("firstStateProbabilisticName"));
        node.setName(resource.getString("probabilisticNodeName") + rede.noVariaveis());
        node.setDescription(node.getName());
        PotentialTable auxTabProb = ((ITabledVariable)node).getPotentialTable();
        auxTabProb.addVariable(node);
        auxTabProb.addValueAt(0, 1d);
        rede.addNode(node);
    }


    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>DecisionNode</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.jprs.jbn.DecisionNode
     */
    public void insertDecisionNode(double x, double y) {
        DecisionNode node = new DecisionNode();
        node.setPosicao(x, y);
        node.appendState(resource.getString("firstStateDecisionName"));
        node.setName(resource.getString("decisionNodeName") + rede.noVariaveis());
        node.setDescription(node.getName());
        rede.addNode(node);
    }

    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>UtilityNode</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.jprs.jbn.UtilityNode
     */
    public void insertUtilityNode(double x, double y) {
        UtilityNode node = new UtilityNode();
        node.setPosicao(x, y);
        node.setName(resource.getString("utilityNodeName") + rede.noVariaveis());
        node.setDescription(node.getName());
        PotentialTable auxTab = ((ITabledVariable)node).getPotentialTable();
        auxTab.addVariable(node);        
        rede.addNode(node);
    }


    /**
     *  Faz a ligacão do arco desejado entre pai e filho.
     *
     * @param  arco  um <code>TArco</code> que representa o arco a ser ligado
     * @since
     */
    public void inserirArco(Edge arco) {
        rede.addEdge(arco);
    }


    /**
     *  Mostra a tabela de potenciais do no desejado.
     *
     * @param  no  um <code>Node</code> que representa o nó o qual deve-se mostrar a
     *      tabela de potenciais
     * @since
     * @see        unbbayes.jprs.jbn.Node
     */
    public JTable retornarTabela(final Node no) {
        tela.getTxtDescription().setEnabled(true);
        tela.getTxtSigla().setEnabled(true);
        tela.getTxtDescription().setText(no.getDescription());
        tela.getTxtSigla().setText(no.getName());

        final JTable tabela;
        final PotentialTable auxTabPot;
        final int noVariaveis;

        if (no instanceof ITabledVariable) {
            auxTabPot = ((ITabledVariable) no).getPotentialTable();

            int nEstados = 1;
            noVariaveis = auxTabPot.variableCount();

            for (int count = 1; count < noVariaveis; count++) {
                nEstados *= auxTabPot.getVariableAt(count).getStatesSize();
            }

            tabela = new JTable(no.getStatesSize() + noVariaveis - 1, nEstados + 1);

            for (int k = noVariaveis - 1, l = 0; k < tabela.getRowCount(); k++, l++) {
                tabela.setValueAt(no.getStateAt(l), k, 0);
            }

            for (int k = noVariaveis - 1, l = 0; k >= 1; k--, l++) {
                Node auxNo = auxTabPot.getVariableAt(k);
                nEstados /= auxNo.getStatesSize();
                tabela.setValueAt(auxNo.getName(), l, 0);
                for (int i = 0; i < tabela.getColumnCount() - 1; i++) {
                    tabela.setValueAt(auxNo.getStateAt((i / nEstados) % auxNo.getStatesSize()), l, i + 1);
                }
            }

            nEstados = no.getStatesSize();
            for (int i = 1, k = 0; i < tabela.getColumnCount(); i++, k += nEstados) {
                for (int j = noVariaveis - 1, l = 0; j < tabela.getRowCount(); j++, l++) {
                    tabela.setValueAt("" + df.format(auxTabPot.getValue(k + l)), j, i);
                }
            }
        } else {
            // decision
            auxTabPot = null;
            noVariaveis = no.getParents().size();

            tabela = new JTable(no.getStatesSize(), 1);
            for (int i = 0; i < no.getStatesSize(); i++) {
                tabela.setValueAt(no.getStateAt(i), i, 0);
            }
        }
        tabela.setTableHeader(null);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    if (e.getLastRow() < noVariaveis - 1) {
                        return;
                    }
                    if (e.getColumn() == 0) {
                        if (!tabela.getValueAt(e.getLastRow(), e.getColumn()).equals("")) {
                            no.setStateAt(tabela.getValueAt(e.getLastRow(), e.getColumn()).toString(), e.getLastRow() - (tabela.getRowCount() - no.getStatesSize()));
                        }
                    } else {
                        try {
                            String temp = tabela.getValueAt(e.getLastRow(), e.getColumn()).toString();
                            double valor = df.parse(temp).doubleValue();
                            /*
                            if (valor > 1.0) {
                                valor = 1.0;
                                tabela.setValueAt("1", e.getLastRow(), e.getColumn());
                            } else if (valor < 0) {
                                valor = 0.0;
                                tabela.setValueAt("0", e.getLastRow(), e.getColumn());
                            }
                            */
                            auxTabPot.addValueAt((e.getColumn() - 1) * no.getStatesSize() + e.getLastRow() - noVariaveis + 1, valor);
                        } catch (Exception pe) {
                            System.err.println(resource.getString("potentialTableException"));
                        }
                    }
                }
            });


            return tabela;
    }


    /**
     *  Não faz nada quando uma tecla é pressionada e em seguida solta.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyTyped(KeyEvent e) { }


    /**
     *  Apaga o objeto selecionado da rede quando a tecla del(KeyEvent.VK_DELETE) é
     *  pressionada, copia um pedaço da rede quando a tecla c(KeyEvent.VK_C) é pressionada
     *  e cola um pedaço da rede quando a tecla p(KeyEvent.VK_P) é pressionada.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() ==  e.VK_C) {
            copia = tela.getIGraph().getSelectedGroup();
        }

        if ((e.getKeyCode() ==  e.VK_P) && (!bColou)) {
            for (int i = 0; i < copia.size(); i++) {
                if (copia.get(i) instanceof Node) {
                    ProbabilisticNode noAux = (ProbabilisticNode)copia.get(i);
                    NodeList nos = rede.getNos();
                    ProbabilisticNode noAux2 = new ProbabilisticNode();
                    noAux2 = (ProbabilisticNode)noAux.clone(tela.getIGraph().getRadius());
                    nos.add(noAux2);
                    copiados.add(noAux2);
                }
            }

            for (int i = 0; i < copia.size(); i++) {
                if (copia.get(i) instanceof Edge) {

                    Edge arcoAux = (Edge)copia.get(i);
                    Node no1 = null;
                    Node no2 = null;
                    Node noAux;
                    boolean achouNo1 = false;
                    boolean achouNo2 = false;
                    for (int j = 0; j < copiados.size(); j++) {
                        noAux = (ProbabilisticNode)copiados.get(j);
                        if (noAux.getName().equals(resource.getString("copiedNodeName") + arcoAux.getOriginNode().getName()) && noAux.getDescription().equals(resource.getString("copiedNodeName") + arcoAux.getOriginNode().getDescription())) {
                            no1 = noAux;
                            achouNo1 = true;
                        } else if (noAux.getName().equals(resource.getString("copiedNodeName") + arcoAux.getDestinationNode().getName()) && noAux.getDescription().equals(resource.getString("copiedNodeName") + arcoAux.getDestinationNode().getDescription())) {
                            no2 = noAux;
                            achouNo2 = true;
                        }
                    }
                    if (!achouNo1) {
                        no1 = arcoAux.getOriginNode();
                    }
                    if (!achouNo2) {
                        no2 = arcoAux.getDestinationNode();
                    }
                    no2.getParents().add(no1);
                    no1.getChildren().add(no2);
                    rede.getArcos().add(new Edge(no1, no2));
                }
            }
            bColou = true;
        }
        copiados.clear();

        if (e.getKeyCode() == e.VK_DELETE) {
            Object selecionado = tela.getIGraph().getSelected();
            deleteSelected(selecionado);
            for (int i = 0; i < tela.getIGraph().getSelectedGroup().size(); i++) {
                selecionado = tela.getIGraph().getSelectedGroup().get(i);
                deleteSelected(selecionado);
            }
        }
        tela.getIGraph().update();
    }

    private void deleteSelected(Object selecionado) {
        if (selecionado instanceof Edge) {
            rede.removeEdge((Edge) selecionado);
//            tela.getIGraph().apagaArco(selecionado);
        } else if (selecionado instanceof Node) {
            /*
            TArco arcoAux = tela.getIGraph().getArco((TVP) selecionado);
            while (arcoAux != null) {

                tela.getIGraph().apagaArco(arcoAux);
                arcoAux = tela.getIGraph().getArco((TVP) selecionado);
            }
            tela.getIGraph().apagaNo(selecionado);
            */
            rede.removeNode((Node) selecionado);
        }
    }



    /**
     *  Não faz nada quando uma tecla é solta.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyReleased(KeyEvent e) {
        bColou = false;
    }


    /**
     * Visualiza a impressão do Log.
     */
    public void previewPrintLog(final JTextArea texto, final JDialog dialog) {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            PrintText it = new PrintText(texto,
                new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                it, 0);

            JDialog dlg = new JDialog(dialog,
                resource.getString("previewLogToolTip"));
            dlg.getContentPane().add(pp);
            dlg.setSize(640, 480);
            dlg.setVisible(true);
          }
        });

        t.start();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Visualiza a impressão da tabela.
     */
    public void visualizarImpressaoTabela() {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List tabelas = new ArrayList();
            List donos = new ArrayList();
            List temp = tela.getIGraph().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(tela.getTable());
               donos.add(tela.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(retornarTabela((Node)temp.get(i)));
                    }
                }
            }

            PrintTable tp = new PrintTable(tabelas, donos, new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                tp, 0);
            JDialog dlg = new JDialog();
            dlg.getContentPane().add(pp);
            dlg.setSize(400, 300);
            dlg.setVisible(true);
          }
        });
        t.start();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Visualiza a impressão da rede.
     */
    public void visualizarImpressaoRede(final JComponent rede, final Rectangle retangulo) {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(tela, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
            if (rotulo == null) {
                rotulo = "";
            }
            PrintNet it = new PrintNet(rotulo, rede, retangulo, new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                it, 0);

            JDialog dlg = new JDialog();
            dlg.getContentPane().add(pp);
            dlg.setSize(640, 480);
            dlg.setVisible(true);
          }
        });

        t.start();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Imprime a rede contida no <code>JTextArea</code> do parametro.
     *
     * @param texto JTextArea que contém o log de compilação.
     */
    public void imprimirRede(final JComponent rede, final Rectangle retangulo) {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(tela, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
            if (rotulo == null) {
                rotulo = "";
            }
            PrintNet it = new PrintNet(rotulo, rede, retangulo, new PageFormat());
            PrintMonitor pm = new PrintMonitor(it);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  tela,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Imprime a tabela especificada.
     *
     * @param tabela tabela a ser impressa.
     */
    public void imprimirTabela() {
        tela.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List tabelas = new ArrayList();
            List donos = new ArrayList();
            List temp = tela.getIGraph().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(tela.getTable());
               donos.add(tela.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(retornarTabela((Node)temp.get(i)));
                    }
                }
            }
            PrintTable impressora = new PrintTable(tabelas, donos, new PageFormat());
            PrintMonitor pm = new PrintMonitor(impressora);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  tela,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        tela.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Imprime o log contido no <code>JTextArea</code> do parametro.
     *
     * @param texto JTextArea que contém o log de compilação.
     */
    private void printLog(final JTextArea texto) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            PrintText it = new PrintText(texto,
                new PageFormat());
            PrintMonitor pm = new PrintMonitor(it);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  tela,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
    }


    /**
     *  Atualiza as marginais na árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a árvore a ser
     *      atualizada
     * @since
     * @see            JTree
     */
    private void updateTree() {
        JTree arvore = tela.getEvidenceTree();
        NodeList nos = rede.getCopiaNos();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) arvore.getModel().getRoot();

        root.removeAllChildren();

        for (int c = 0; c < nos.size(); c++) {
            Node node = (Node) nos.get(c);
            TreeVariable treeVariable = (TreeVariable) node;
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);

            for (int i = 0; i < node.getStatesSize(); i++) {
                String label;
                if (treeVariable instanceof ProbabilisticNode) {
                    label = node.getStateAt(i) + ": " + df.format(treeVariable.getMarginalAt(i) * 100.0);
                } else {
                    label = node.getStateAt(i) + ": " + df.format(treeVariable.getMarginalAt(i));
                }
                treeNode.add(new DefaultMutableTreeNode(label));
            }
            root.add(treeNode);
        }

        ((DefaultTreeModel) arvore.getModel()).reload(root);

        int temp = 0;
        for (int i = 0; i < situacaoArvore.length; i++) {
          if (situacaoArvore[i]) {
            arvore.expandRow(temp);
            Node node = (Node) nos.get(i);
            temp += node.getStatesSize();
          }
          temp++;
        }
    }



    /**
     * Modifica o formato de números
     *
     * @param local localidade do formato de números.
     */
    public void setNumberFormat(Locale local) {
        df = NumberFormat.getInstance(local);
    }



    /**
     * Calcula as bordas da rede. Caso exista algum objeto selecionado,
     * o retangulo resultante é calculado apenas levando em conta os selecionados.
     * Caso contrario, a rede toda é levada em conta.
     */
    public Rectangle calcularBordasRede() {
        NodeList nos;
        List vetorAux = tela.getIGraph().getSelectedGroup();

        if (vetorAux.size() == 0) {
            nos = rede.getNos();
        } else {
            nos = new NodeList();
            for (int i = 0; i < vetorAux.size(); i++) {
                if (vetorAux.get(i) instanceof Node) {
                    nos.add((Node)vetorAux.get(i));
                }
            }
        }
        int maiorX = 0;
        int menorX = Integer.MAX_VALUE;
        int maiorY = 0;
        int menorY = Integer.MAX_VALUE;
        Node noAux;
        Point2D pontoAux;
        int xAux;
        int yAux;
        for (int i = 0; i < nos.size(); i++) {
            noAux = (Node)nos.get(i);
            pontoAux = noAux.getPosicao();
            xAux = (int)pontoAux.getX();
            yAux = (int)pontoAux.getY();
            if (xAux > maiorX) {
                maiorX = xAux;
            }
            if (xAux < menorX) {
                menorX = xAux;
            }
            if (yAux > maiorY) {
                maiorY = yAux;
            }
            if (yAux < menorY) {
                menorY = yAux;
            }
        }
        double raio = tela.getIGraph().getRadius();
        maiorX += raio;
        maiorY += raio;
        menorX -= raio;
        menorY -= raio;
        return new Rectangle(menorX, menorY, maiorX - menorX, maiorY - menorY);
    }
}