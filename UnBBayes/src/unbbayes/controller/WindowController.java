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

package unbbayes.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.shetline.io.GIFOutputStream;
import unbbayes.gui.ExplanationProperties;
import unbbayes.gui.FileIcon;
import unbbayes.gui.NetWindow;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.Network;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
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

    private NetWindow screen;
    private Network net;

    private NumberFormat df;

    private List copia;
    private List copiados;

    private boolean bColou;

    private final Pattern decimalPattern = Pattern.compile("[0-9]*([.|,][0-9]+)?");
    private Matcher matcher;

    /** Load resource file from this package */
    private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");

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
    public WindowController(Network _rede, NetWindow _tela) {
        this.net = _rede;
        this.screen = _tela;
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
     * @see       unbbayes.gui.IUnBBayes
     */
    public NetWindow getScreen() {
        return this.screen;
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
    public Network getNet() {
        return this.net;
    }

    /**
     * Salva a imagem da rede para um arquivo.
     */
    public void saveNetImage() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);

        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                Rectangle r = calculateNetRectangle();
                out.write(graphicsToImage(screen.getIGraph().getGraphViewport(), r));
                out.flush();
                out.close();
                FileController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Salva a imagem da tabela para um arquivo.
     */
    public void saveTableImage() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);


        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                out.write(graphicsToImage(screen.getTable(), null));
                out.flush();
                out.close();
                FileController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
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
    public void insertState(Node no) {
    	if (no instanceof ProbabilisticNode) {
        	no.appendState(resource.getString("stateProbabilisticName") + no.getStatesSize());
    	} else if (no instanceof DecisionNode) {
    		no.appendState(resource.getString("stateDecisionName") + no.getStatesSize());
    	}
        screen.setTable(makeTable(no));
    }


    /**
     *  Remove último estado do nó selecionado.
     *
     * @param  no  o <code>Object <code>selecionado.
     * @since
     * @see        Object
     */
    public void removeState(Node no) {
        no.removeLastState();
        screen.setTable(makeTable(no));
    }


    /**
     * Inicia as crenças da árvore de junção.
     */
    public void initialize() {
    	try {
	        net.initialize();
       		screen.getEvidenceTree().updateTree();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }


    /**
     *  Propaga as evidências da rede Bayesiana ( <code>TRP</code> ).
     *
     * @since
     */
    public void propagate() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        boolean temLikeliHood = false;
        try {
        	net.updateEvidences();
            if (! temLikeliHood) {
                screen.setStatus(resource.getString("statusEvidenceProbabilistic") + df.format(net.PET() * 100.0));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(screen, resource.getString("statusEvidenceException"), resource.getString("statusError"), JOptionPane.ERROR_MESSAGE);
        }
        screen.getEvidenceTree().updateTree();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     *  Mostra todas os passos realizados na compilação da rede Bayesiana em um
     *  <code>JDialog</code> .
     *
     * @since
     * @see      JDialog
     */
    public void showLog() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final JTextArea texto = new JTextArea();

        texto.setEditable(false);
        texto.setText(net.getLog());
        texto.moveCaretPosition(0);
        texto.setSelectionEnd(0);

//            texto.setRows(linhas);
        texto.setSize(texto.getPreferredSize());
        texto.append("\n");
//            arq.close();

        final JDialog dialog = new JDialog();
        JScrollPane jspTexto = new JScrollPane(texto);
        jspTexto.setPreferredSize(new Dimension(450, 400));

        IconController iconController = IconController.getInstance();
        JPanel panel = new JPanel(new BorderLayout());
        JButton botaoImprimir = new JButton(iconController.getPrintIcon());
        botaoImprimir.setToolTipText(resource.getString("printLogToolTip"));
        JButton botaoVisualizar = new JButton(iconController.getVisualizeIcon());
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
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            ((ProbabilisticNetwork) net).compile();
        } catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), resource.getString("statusError"), JOptionPane.ERROR_MESSAGE);
            screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return false;
        }

        // Ordenar pela descricao do nó apenas para facilitar a visualização da árvore.
        NodeList nos = net.getNodesCopy();
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

        screen.getEvidenceTree().updateTree();

        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        screen.setStatus(resource.getString("statusTotalTime") + df.format(((System.currentTimeMillis() - ini))/1000.0) + resource.getString("statusSeconds"));
        return true;
    }


    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>Node</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.prs.Node
     */
    public void insertProbabilisticNode(double x, double y) {
        ProbabilisticNode node = new ProbabilisticNode();
        node.setPosition(x, y);
        node.appendState(resource.getString("firstStateProbabilisticName"));
        node.setName(resource.getString("probabilisticNodeName") + net.getNodeCount());
        node.setDescription(node.getName());
        PotentialTable auxTabProb = ((ITabledVariable)node).getPotentialTable();
        auxTabProb.addVariable(node);
        auxTabProb.setValue(0, 1);
        net.addNode(node);
    }


    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>DecisionNode</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.prs.DecisionNode
     */
    public void insertDecisionNode(double x, double y) {
        DecisionNode node = new DecisionNode();
        node.setPosition(x, y);
        node.appendState(resource.getString("firstStateDecisionName"));
        node.setName(resource.getString("decisionNodeName") + net.getNodeCount());
        node.setDescription(node.getName());
        net.addNode(node);
    }

    /**
     *  Insere o nó desejado na rede criando estado, sigla e descrição padrões.
     *
     * @param  no  uma <code>UtilityNode</code> que representa o nó a ser inserido
     * @since
     * @see        unbbayes.prs.UtilityNode
     */
    public void insertUtilityNode(double x, double y) {
        UtilityNode node = new UtilityNode();
        node.setPosition(x, y);
        node.setName(resource.getString("utilityNodeName") + net.getNodeCount());
        node.setDescription(node.getName());
        PotentialTable auxTab = ((ITabledVariable)node).getPotentialTable();
        auxTab.addVariable(node);
        net.addNode(node);
    }


    /**
     *  Faz a ligacão do arco desejado entre pai e filho.
     *
     * @param  arco  um <code>TArco</code> que representa o arco a ser ligado
     * @since
     */
    public void insertEdge(Edge arco) {
        net.addEdge(arco);
    }


    /**
     *  Mostra a tabela de potenciais do no desejado.
     *
     * @param  no  um <code>Node</code> que representa o nó o qual deve-se mostrar a
     *      tabela de potenciais
     * @since
     * @see        unbbayes.prs.Node
     */
    public JTable makeTable(final Node node) {
        screen.getTxtDescription().setEnabled(true);
        screen.getTxtSigla().setEnabled(true);
        screen.getTxtDescription().setText(node.getDescription());
        screen.getTxtSigla().setText(node.getName());

        final JTable table;
        final PotentialTable potTab;
        final int variables;
        if (node instanceof ITabledVariable) {
            potTab = ((ITabledVariable) node).getPotentialTable();

            int states = 1;
            variables = potTab.variableCount();

            // calculate the number of states by multiplying the number of
            // states that each father (variables) has. Where variable 0 is the
            // node itself. That is why it starts at 1.
            /* Ex: states = 2 * 2;
             *
             * |------------------------------------------------------|
             * |   Father 2   |      State 1      |      State 2      |
             * |--------------|-------------------|-------------------|
             * |   Father 1   | State 1 | State 2 | State 1 | State 2 |
             * |------------------------------------------------------|
             * | Node State 1 |    1    |    1    |    1    |    1    |
             * | Node State 2 |    0    |    0    |    0    |    0    |
             *
             */
            states = potTab.tableSize() / node.getStatesSize();
            /* 
            for (int count = 1; count < variables; count++) {
                states *= potTab.getVariableAt(count).getStatesSize();
            }
            */

            // the number of rows is the number of states the node has plus the
            // number of fathers (variables - 1, because one of the variables
            // is the node itself).
            int rows = node.getStatesSize() + variables - 1;

            // the number of columns is the number of states that we calculate
            // before plus one that is the column where the fathers names and
            // the states of the node itself will be placed.
            int columns = states + 1;


            table = new JTable(rows, columns);

            // put the name of the states of the node in the first column
            // starting in the (variables - 1)th row (number of fathers). That
            // is because on the rows before that there will be placed the
            // name of the fathers.
            for (int k = variables - 1, l = 0; k < table.getRowCount(); k++, l++) {
                table.setValueAt(node.getStateAt(l), k, 0);
            }

            // put the name of the father and its states' name in the right
            // place.
            for (int k = variables-1, l=0; k>=1; k--, l++) {
                Node variable = potTab.getVariableAt(k);

                // the number of states is the multiplication of the number of
                // states of the other fathers above this one.
                states /= variable.getStatesSize();

                // put the name of the father in the first column.
                table.setValueAt(variable.getName(), l, 0);

                // put the name of the states of this father in the lth row
                // and ith column, repeating the name if necessary (for each
                // state of the father above).
                for (int i = 0; i < table.getColumnCount() - 1; i++) {
                    table.setValueAt(variable.getStateAt((i / states) % variable.getStatesSize()), l, i + 1);
                }
            }

            // now states is the number of states that the node has.
            states = node.getStatesSize();

            // put the values of the probabilistic table in the jth row and ith
            // column, picking up the values in a double collection in potTab.
            for (int i = 1, k = 0; i < table.getColumnCount(); i++, k += states) {
                for (int j = variables - 1, l = 0; j < table.getRowCount(); j++, l++) {
                    table.setValueAt("" + df.format(potTab.getValue(k + l)), j, i);
                }
            }

        } else {
            // decision

            // the number of rows in this case is the number of states of the
            // node and the number of columns is always 1.
            int rows = node.getStatesSize();
            int columns = 1;

            // there is no potential table and the number of variables is the
            // number of parents this node has.
            potTab = null;
            variables = node.getParents().size();

            table = new JTable(node.getStatesSize(), 1);
            // put the name of each state in the first and only column.
            for (int i = 0; i < node.getStatesSize(); i++) {
                table.setValueAt(node.getStateAt(i), i, 0);
            }

        }

        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    if (e.getLastRow() < variables - 1) {
                        return;
                    }
                    if (e.getColumn() == 0) {
                        if (!table.getValueAt(e.getLastRow(), e.getColumn()).equals("")) {
                            node.setStateAt(table.getValueAt(e.getLastRow(), e.getColumn()).toString(), e.getLastRow() - (table.getRowCount() - node.getStatesSize()));
                        }
                    } else {
                        try {
                            String temp = table.getValueAt(e.getLastRow(), e.getColumn()).toString().replace(',', '.');
                            matcher = decimalPattern.matcher(temp);
                            if (!matcher.matches()) {
                                JOptionPane.showMessageDialog(null, /*resource.getString("decimalError")*/"Decimal Error", /*resource.getString("decimalException")*/"Decimal Exception", JOptionPane.ERROR_MESSAGE);
                                table.revalidate();
                                table.setValueAt("" + potTab.getValue((e.getColumn() - 1) * node.getStatesSize() + e.getLastRow() - variables + 1), e.getLastRow(), e.getColumn());
                                return;
                            }
							float valor = Float.parseFloat(temp);
                            potTab.setValue((e.getColumn() - 1) * node.getStatesSize() + e.getLastRow() - variables + 1, valor);
                        } catch (Exception pe) {
                            System.err.println(resource.getString("potentialTableException"));
                        }
                    }
                }
            });

            //table = new unbbayes.gui.table.ProbabilisticTable(node, new ProbabilisticTableModel(node));
            //table = new JTable(new ProbabilisticTableModel(node));
            //System.out.println(table.toString());


            return table;
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
        if (e.getKeyCode() ==  KeyEvent.VK_C) {
            copia = screen.getIGraph().getSelectedGroup();
        }

        if ((e.getKeyCode() ==  KeyEvent.VK_P) && (!bColou)) {
            for (int i = 0; i < copia.size(); i++) {
                if (copia.get(i) instanceof Node) {
                    ProbabilisticNode noAux = (ProbabilisticNode)copia.get(i);
                    ProbabilisticNode noAux2 = new ProbabilisticNode();
                    noAux2 = (ProbabilisticNode)noAux.clone(screen.getIGraph().getRadius());
                    net.addNode(noAux2);
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
                    net.getEdges().add(new Edge(no1, no2));
                }
            }
            bColou = true;
        }
        copiados.clear();

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            Object selecionado = screen.getIGraph().getSelected();
            deleteSelected(selecionado);
            for (int i = 0; i < screen.getIGraph().getSelectedGroup().size(); i++) {
                selecionado = screen.getIGraph().getSelectedGroup().get(i);
                deleteSelected(selecionado);
            }
        }
        screen.getIGraph().update();
    }

    private void deleteSelected(Object selecionado) {
        if (selecionado instanceof Edge) {
            net.removeEdge((Edge) selecionado);
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
            net.removeNode((Node) selecionado);
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
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
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
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Visualiza a impressão da tabela.
     */
    public void previewPrintTable() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List tabelas = new ArrayList();
            List donos = new ArrayList();
            List temp = screen.getIGraph().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(screen.getTable());
               donos.add(screen.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(makeTable((Node)temp.get(i)));
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
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Visualiza a impressão da rede.
     */
    public void previewPrintNet(final JComponent rede, final Rectangle retangulo) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(screen, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
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
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Imprime a rede contida no <code>JTextArea</code> do parametro.
     *
     * @param texto JTextArea que contém o log de compilação.
     */
    public void printNet(final JComponent rede, final Rectangle retangulo) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(screen, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
            if (rotulo == null) {
                rotulo = "";
            }
            PrintNet it = new PrintNet(rotulo, rede, retangulo, new PageFormat());
            PrintMonitor pm = new PrintMonitor(it);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Imprime a tabela especificada.
     *
     * @param tabela tabela a ser impressa.
     */
    public void printTable() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List tabelas = new ArrayList();
            List donos = new ArrayList();
            List temp = screen.getIGraph().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(screen.getTable());
               donos.add(screen.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(makeTable((Node)temp.get(i)));
                    }
                }
            }
            PrintTable impressora = new PrintTable(tabelas, donos, new PageFormat());
            PrintMonitor pm = new PrintMonitor(impressora);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
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
    public Rectangle calculateNetRectangle() {
        NodeList nos;
        List vetorAux = screen.getIGraph().getSelectedGroup();

        if (vetorAux.size() == 0) {
            nos = new NodeList();
            for (int i = 0; i < net.getNodeCount(); i++) {
            	nos.add(i, net.getNodeAt(i));
            }
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
            pontoAux = noAux.getPosition();
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
        double raio = screen.getIGraph().getRadius();
        maiorX += raio;
        maiorY += raio;
        menorX -= raio;
        menorY -= raio;
        return new Rectangle(menorX, menorY, maiorX - menorX, maiorY - menorY);
    }

    public void showExplanationProperties(ProbabilisticNode node)
    {   screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ExplanationProperties explanation = new ExplanationProperties(screen,net);
        explanation.setProbabilisticNode(node);
        explanation.setVisible(true);
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}