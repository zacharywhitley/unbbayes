package unbbayes.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import unbbayes.controller.*;
import unbbayes.prs.bn.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 */

public class NetWindowEdition extends JPanel {

    private final ProbabilisticNetwork net;
    private final NetWindow netWindow;

    private GlobalOptions go;
    private JTable table;
    private final JTextField txtSigla;
    private final JTextField txtDescription;
    private final WindowController controller;
    private final JScrollPane jspTable;
    private final JSplitPane centerPanel;
    private Node tableOwner;
    private final JLabel status;
    private final JPanel bottomPanel;

    private final JPanel topPanel;
    private final JToolBar jtbState;
    private final JToolBar jtbEdition;

    private final JLabel sigla;
    private final JLabel description;

    private final JButton compile;
    private final JButton more;
    private final JButton less;
    private final JButton arc;
    private final JButton probabilisticNode;
    private final JButton decisionNode;
    private final JButton utilityNode;
    private final JButton select;
    private final JButton printNet;
    private final JButton printTable;
    private final JButton previewNet;
    private final JButton previewTable;
    private final JButton saveNetImage;
    private final JButton saveTableImage;
    private final JButton globalOption;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

    public NetWindowEdition(ProbabilisticNetwork _net, NetWindow _netWindow,
                            WindowController _controller) {
        super();
        this.net           = _net;
        this.netWindow     = _netWindow;
        this.controller    = _controller;
        this.setLayout(new BorderLayout());

        table       = new JTable();
        jspTable    = new JScrollPane(table);
        topPanel    = new JPanel(new GridLayout(0,1));
        jtbState    = new JToolBar();
        jtbEdition  = new JToolBar();
        centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status      = new JLabel(resource.getString("statusReadyLabel"));

        //criar labels e textfields que serão usados no jtbState
        sigla       = new JLabel(resource.getString("siglaLabel"));
        description = new JLabel(resource.getString("descriptionLabel"));
        txtSigla           = new JTextField(10);
        txtDescription     = new JTextField(15);

        //criar botões que serão usados nos toolbars
        compile           = new JButton(new ImageIcon(getClass().getResource("/icons/compile.gif")));
        more              = new JButton(new ImageIcon(getClass().getResource("/icons/more.gif")));
        less              = new JButton(new ImageIcon(getClass().getResource("/icons/less.gif")));
        arc               = new JButton(new ImageIcon(getClass().getResource("/icons/arc.gif")));
        probabilisticNode = new JButton(new ImageIcon(getClass().getResource("/icons/ellipsis.gif")));
        decisionNode      = new JButton(new ImageIcon(getClass().getResource("/icons/decision-node.gif")));
        utilityNode       = new JButton(new ImageIcon(getClass().getResource("/icons/utility-node.gif")));
        select            = new JButton(new ImageIcon(getClass().getResource("/icons/selection.gif")));
        printNet          = new JButton(new ImageIcon(getClass().getResource("/icons/print-net.gif")));
        printTable        = new JButton(new ImageIcon(getClass().getResource("/icons/print-table.gif")));
        previewNet        = new JButton(new ImageIcon(getClass().getResource("/icons/preview-print.gif")));
        previewTable      = new JButton(new ImageIcon(getClass().getResource("/icons/preview-table.gif")));
        saveNetImage      = new JButton(new ImageIcon(getClass().getResource("/icons/save-net.gif")));
        saveTableImage    = new JButton(new ImageIcon(getClass().getResource("/icons/save-table.gif")));
        globalOption      = new JButton(new ImageIcon(getClass().getResource("/icons/global-options.gif")));


        //setar tooltip para esses botões
		compile.setToolTipText(resource.getString("compileToolTip"));
        more.setToolTipText(resource.getString("moreToolTip"));
        less.setToolTipText(resource.getString("lessToolTip"));
        arc.setToolTipText(resource.getString("arcToolTip"));
        probabilisticNode.setToolTipText(resource.getString("probabilisticNodeInsertToolTip"));
        decisionNode.setToolTipText(resource.getString("decisionNodeInsertToolTip"));
        utilityNode.setToolTipText(resource.getString("utilityNodeInsertToolTip"));;
        select.setToolTipText(resource.getString("selectToolTip"));
        printNet.setToolTipText(resource.getString("printNetToolTip"));
        printTable.setToolTipText(resource.getString("printTableToolTip"));
        previewNet.setToolTipText(resource.getString("previewNetToolTip"));
        previewTable.setToolTipText(resource.getString("previewTableToolTip"));
        saveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));
        saveTableImage.setToolTipText(resource.getString("saveTableImageToolTip"));
        globalOption.setToolTipText(resource.getString("globalOptionTitle"));

        //ao clicar no botão globalOption, mostra-se o menu para escolha das opções
        globalOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                go = new GlobalOptions(netWindow.getIGraph(), controller);
                go.setVisible(true);
                netWindow.getIGraph().update();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        //ao clicar no botão compile, chama-se o método de compilação da rede e
        //atualiza os toolbars
        compile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (! controller.compileNetwork()) {
                    return;
                }
                netWindow.changeToNetCompilation();
            }
        });

        //ao clicar no botão arc setamos as variáveis booleanas e os estados dos butões
        arc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbArc(true);
            }
        });

        //ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
        probabilisticNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbProbabilisticNode(true);
            }
        });


        //ao clicar no botão decisionNode setamos as variáveis booleanas e os estados dos butões
        decisionNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(true);
            }
        });

        //ao clicar no botão utilityNode setamos as variáveis booleanas e os estados dos butões
        utilityNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(true);
            }
        });


        //ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
        select.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbSelect(true);
            }
        });


        // listener responsável pela atualização do texo da sigla do nó
        txtSigla.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            Object selected = netWindow.getIGraph().getSelected();
            if (selected instanceof Node)
            {
              Node nodeAux = (Node)selected;
              if ((e.getKeyCode() == e.VK_BACK_SPACE) && (txtSigla.getText().length()>1))
              {
                try {
                  nodeAux.setName(txtSigla.getText(0,txtSigla.getText().length()-1));
                }
                catch (javax.swing.text.BadLocationException ble) {
                  System.out.println(ble.getMessage());
                }
              }
              else
              {
                nodeAux.setName(txtSigla.getText() + e.getKeyChar());
              }
              repaint();
            }
          }
        });


        // listener responsável pela atualização do texo da descrição do nó
        txtDescription.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            Object selected = netWindow.getIGraph().getSelected();
            if (selected instanceof Node)
            {
              Node nodeAux = (Node)selected;
              if ((e.getKeyCode() == e.VK_BACK_SPACE) && (txtDescription.getText().length()>1))
              {
                try {
                  nodeAux.setDescription(txtDescription.getText(0,txtDescription.getText().length()-1));
                }
                catch (javax.swing.text.BadLocationException ble) {
                  System.out.println(ble.getMessage());
                }
              }
              else
              {
                nodeAux.setDescription(txtDescription.getText() + e.getKeyChar());
              }
              repaint();
            }
          }
        });

        //ao clicar no botão less, chama-se o metodo removerEstado do controller
        //para que esse remova um estado do nó
        less.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getIGraph().getSelected() instanceof Node) {
               controller.removerEstado((Node)netWindow.getIGraph().getSelected());
            }
          }
        });

        //ao clicar no botão less, chama-se o metodo inserirEstado do controller
        //para que esse insira um novo estado no nó
        more.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getIGraph().getSelected() instanceof Node) {
               controller.inserirEstado((Node)netWindow.getIGraph().getSelected());
            }
          }
        });

        // action para imprimir a rede
        printNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.imprimirRede(netWindow.getIGraph(), controller.calcularBordasRede());
            }
        });

        // action para imprimir a tabela
        printTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.imprimirTabela();
            }
        });

        // action para visualizar a tabela
        previewTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.visualizarImpressaoTabela();
            }
        });

        // action para visualizar a rede.
        previewNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.visualizarImpressaoRede(netWindow.getIGraph(), controller.calcularBordasRede());
            }
        });


        saveNetImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.salvarImagemRede();
            }
        });


        saveTableImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.salvarImagemTabela();
            }
        });

        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel
        jtbEdition.add(printNet);
        jtbEdition.add(previewNet);
        jtbEdition.add(saveNetImage);
        jtbEdition.add(printTable);
        jtbEdition.add(previewTable);
        jtbEdition.add(saveTableImage);

        jtbEdition.addSeparator();

        jtbEdition.add(probabilisticNode);
        jtbEdition.add(decisionNode);
        jtbEdition.add(utilityNode);
        jtbEdition.add(arc);
        jtbEdition.add(select);
        jtbEdition.add(compile);

        jtbEdition.addSeparator();

        jtbEdition.add(globalOption);

        topPanel.add(jtbEdition);

        //setar controladores de estados para false
        /*
        more.setEnabled(false);
        less.setEnabled(false);
        txtDescription.setEnabled(false);
        txtSigla.setEnabled(false);
        */

        //colocar botões, labels e textfields no toolbar e colocá-lo no topPanel
        jtbState.add(sigla);
        jtbState.add(txtSigla);

        jtbState.addSeparator();
        jtbState.addSeparator();

        jtbState.add(more);
        jtbState.add(less);

        jtbState.addSeparator();
        jtbState.addSeparator();

        jtbState.add(description);
        jtbState.add(txtDescription);

        topPanel.add(jtbState);

        //setar o preferred size do jspTable para ser usado pelo SplitPanel
        jspTable.setPreferredSize(new Dimension(150,50));

        //setar o auto resize off para que a tabela fique do tamanho ideal
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //adicionar tela da tabela(JScrollPane) da tabela de estados para o painel do centro
        centerPanel.setTopComponent(jspTable);

        //setar o tamanho do divisor entre o jspGraph(vem do NetWindow) e jspTable
        centerPanel.setDividerSize(3);

        //setar os tamanho de cada jsp(tabela e graph) para os seus PreferredSizes
        centerPanel.resetToPreferredSizes();

        bottomPanel.add(status);

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);

    }

    /**
     *  Retorna a tabela de probabilidades.
     *
     *@return    retorna a table (<code>JTable</code>)
     *@see       JTable
     */
    public JTable getTable() {
        return table;
    }

    /**
     *  Retorna o text field da descrição do nó.
     *
     *@return    retorna a txtDescrição (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtDescription() {
      return this.txtDescription;
    }

    /**
     *  Retorna o text field da sigla do nó.
     *
     *@return    retorna a txtSigla (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtSigla() {
      return this.txtSigla;
    }

    /**
     *  Substitui a tabela de probabilidades existente pela desejada.
     *
     *@parm      table a nova tabela (<code>JTable</code>) desejada.
     *@see       JTable
     */
    public void setTable(JTable table) {
        this.table = table;
        jspTable.setViewportView(table);
    }

    public Node getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(Node node) {
        this.tableOwner = node;
    }

    /**
     * Seta o status exibido na barra de status.
     *
     * @param status mensagem de status.
     */
    public void setStatus(String status) {
        this.status.setText(status);
    }

    /**
     *  Retorna o painel do centro onde fica o graph e a table.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getCenterPanel() {
      return this.centerPanel;
    }

    public JButton getArc() {
        return this.arc;
    }

    public JButton getCompile() {
        return this.compile;
    }

    public JButton getDecisionNode() {
        return this.decisionNode;
    }

    public JLabel getDescription() {
        return this.description;
    }

    public JButton getGlobalOption() {
        return this.globalOption;
    }

    public JButton getLess() {
        return this.less;
    }

    public JButton getMore() {
        return this.more;
    }

    public JButton getPreviewNet() {
        return this.previewNet;
    }

    public JButton getPreviewTable() {
        return this.previewTable;
    }

    public JButton getPrintNet() {
        return this.printNet;
    }

    public JButton getPrintTable() {
        return this.printTable;
    }

    public JButton getProbabilisticNode() {
        return this.probabilisticNode;
    }

    public JButton getSaveNetImage() {
        return this.saveNetImage;
    }

    public JButton getSaveTableImage() {
        return this.saveTableImage;
    }

    public JButton getSelect() {
        return this.select;
    }

    public JLabel getSigla() {
        return this.sigla;
    }

    public JButton getUtilityNode() {
        return this.utilityNode;
    }

}
