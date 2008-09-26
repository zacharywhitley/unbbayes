/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.Node;


/**
 * <p>Title: UnBBayes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 */

public class PNEditionPane extends JPanel {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private final NetworkWindow netWindow;

    private GlobalOptionsDialog go;
    private JTable table;
    private final JTextField txtSigla;
    private final JTextField txtDescription;
    private final NetworkController controller;
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

    private final JButton btnEvaluate;
    
    private final JButton btnCompile;
    private final JButton btnAddState;
    private final JButton btnRemoveState;
    private final JButton btnAddEdge;
    private final JButton btnAddProbabilisticNode;
    private final JButton btnAddDecisionNode;
    private final JButton btnAddUtilityNode;
    private final JButton btnSelectObject;
    private final JButton btnPrintNet;
    private final JButton btnPrintTable;
    private final JButton btnPreviewNet;
    private final JButton btnPreviewTable;
    private final JButton btnSaveNetImage;
    private final JButton btnSaveTableImage;
    private final JButton btnGlobalOption;
    private final JButton btnHierarchy;
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private final Pattern descriptionPattern = Pattern.compile("[ a-zA-Z_0-9áéíóúãõçâêîôûüà]*");
    private Matcher matcher;

    private final IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

    public PNEditionPane(NetworkWindow _netWindow,
                            NetworkController _controller) {
        super();
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

        //criar labels e textfields que ser�o usados no jtbState
        sigla       = new JLabel(resource.getString("siglaLabel") + " ");
        description = new JLabel(resource.getString("descriptionLabel") + " ");
        txtSigla           = new JTextField(10);
        txtDescription     = new JTextField(15);

        // Create buttons that are going to be used in the nodeList tool bar
        btnEvaluate           = new JButton(iconController.getEvaluateIcon());
        btnCompile           = new JButton(iconController.getCompileIcon());
        btnAddState              = new JButton(iconController.getMoreIcon());
        btnRemoveState              = new JButton(iconController.getLessIcon());
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddProbabilisticNode = new JButton(iconController.getEllipsisIcon());
        btnAddDecisionNode      = new JButton(iconController.getDecisionNodeIcon());
        btnAddUtilityNode       = new JButton(iconController.getUtilityNodeIcon());
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnPrintNet          = new JButton(iconController.getPrintNetIcon());
        btnPrintTable        = new JButton(iconController.getPrintTableIcon());
        btnPreviewNet        = new JButton(iconController.getPrintPreviewNetIcon());
        btnPreviewTable      = new JButton(iconController.getPrintPreviewTableIcon());
        btnSaveNetImage      = new JButton(iconController.getSaveNetIcon());
        btnSaveTableImage    = new JButton(iconController.getSaveTableIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());
        btnHierarchy         = new JButton(iconController.getHierarchyIcon());

        // Set tool tip for the following buttons
        btnEvaluate.setToolTipText(resource.getString("evaluateToolTip"));
        btnCompile.setToolTipText(resource.getString("compileToolTip"));
        btnAddState.setToolTipText(resource.getString("moreToolTip"));
        btnRemoveState.setToolTipText(resource.getString("lessToolTip"));
        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
        btnAddProbabilisticNode.setToolTipText(resource.getString("probabilisticNodeInsertToolTip"));
        btnAddDecisionNode.setToolTipText(resource.getString("decisionNodeInsertToolTip"));
        btnAddUtilityNode.setToolTipText(resource.getString("utilityNodeInsertToolTip"));;
        btnSelectObject.setToolTipText(resource.getString("selectToolTip"));
        btnPrintNet.setToolTipText(resource.getString("printNetToolTip"));
        btnPrintTable.setToolTipText(resource.getString("printTableToolTip"));
        btnPreviewNet.setToolTipText(resource.getString("previewNetToolTip"));
        btnPreviewTable.setToolTipText(resource.getString("previewTableToolTip"));
        btnSaveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));
        btnSaveTableImage.setToolTipText(resource.getString("saveTableImageToolTip"));
        btnGlobalOption.setToolTipText(resource.getString("globalOptionTitle"));
        btnHierarchy.setToolTipText(resource.getString("hierarchyToolTip"));

        //ao clicar no bot�o btnGlobalOption, mostra-se o menu para escolha das op��es
        btnGlobalOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                go = new GlobalOptionsDialog(netWindow.getGraphPane(), controller);
                go.setVisible(true);
                netWindow.getGraphPane().update();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        //ao clicar no bot�o btnHierarchy, chama-se a tela para defini��o de hierarquia
        btnHierarchy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.changeToHierarchy();
            }
        });

        //ao clicar no bot�o btnCompile, chama-se o m�todo de compila��o da rede e
        //atualiza os toolbars
        btnCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (! controller.compileNetwork()) {
                    return;
                }
                netWindow.changeToPNCompilationPane();
            }
        });
        
        // Call the evaluate method in the controller
        btnEvaluate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.evaluateNetwork();
            }
        });

        //ao clicar no bot�o btnAddEdge setamos as vari�veis booleanas e os estados dos but�es
        btnAddEdge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbArc(true);*/
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_EDGE);
            }
        });

        //ao clicar no bot�o node setamos as vari�veis booleanas e os estados dos but�es
        btnAddProbabilisticNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbProbabilisticNode(true);*/
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_PROBABILISTIC_NODE);
            }
        });


        //ao clicar no bot�o btnAddDecisionNode setamos as vari�veis booleanas e os estados dos but�es
        btnAddDecisionNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(true);*/
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_DECISION_NODE);
            }
        });

        //ao clicar no bot�o btnAddUtilityNode setamos as vari�veis booleanas e os estados dos but�es
        btnAddUtilityNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(true);*/
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_UTILITY_NODE);
            }
        });


        //ao clicar no bot�o node setamos as vari�veis booleanas e os estados dos but�es
        btnSelectObject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*netWindow.getIGraph().setbArc(false);
                netWindow.getIGraph().setbProbabilisticNode(false);
                netWindow.getIGraph().setbDecisionNode(false);
                netWindow.getIGraph().setbUtilityNode(false);
                netWindow.getIGraph().setbSelect(true);*/
            	netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
            }
        });
        /*
        // listener respons�vel pela entrada de
        table.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {

              if ((e.getKeyCode() == e.VK_ENTER) && (txtSigla.getText().length()>1)) {
                try {
                    String name = txtSigla.getText(0,txtSigla.getText().length());
                    matcher = wordPattern.matcher(name);
                    if (matcher.matches()) {
                      nodeAux.setName(name);
                      repaint();
                    }  else {
                        JOptionPane.showMessageDialog(netWindow, resource.getString("siglaError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
                        txtSigla.selectAll();
                    }
                }
                catch (javax.swing.text.BadLocationException ble) {
                    System.out.println(ble.getMessage());
                }
              }

          }
        });
*/

        // listener respons�vel pela atualiza��o do texo da sigla do n�
        txtSigla.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            Object selected = netWindow.getGraphPane().getSelected();
            if (selected instanceof Node) {
              Node nodeAux = (Node)selected;
              if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtSigla.getText().length()>0)) {
                try {
                    String name = txtSigla.getText(0,txtSigla.getText().length());
                    matcher = wordPattern.matcher(name);
                    if (matcher.matches()) {
                      nodeAux.setName(name);
                      repaint();
                    }  else {
                        JOptionPane.showMessageDialog(netWindow, resource.getString("siglaError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
                        txtSigla.selectAll();
                    }
                }
                catch (javax.swing.text.BadLocationException ble) {
                    System.out.println(ble.getMessage());
                }
              }
            }
          }
        });


        // listener respons�vel pela atualiza��o do texo da descri��o do n�
        txtDescription.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            Object selected = netWindow.getGraphPane().getSelected();
            if (selected instanceof Node)
            {
              Node nodeAux = (Node)selected;
              if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtDescription.getText().length()>0)) {
                try {
                    String name = txtDescription.getText(0,txtDescription.getText().length());
                    matcher = descriptionPattern.matcher(name);
                    if (matcher.matches()) {
                      nodeAux.setDescription(name);
                      repaint();
                    } else {
                        JOptionPane.showMessageDialog(netWindow, resource.getString("descriptionError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
                        txtDescription.selectAll();
                    }
                }
                catch (javax.swing.text.BadLocationException ble) {
                    System.out.println(ble.getMessage());
                }
              }
            }
          }
        });

        //ao clicar no bot�o btnRemoveState, chama-se o metodo removerEstado do controller
        //para que esse remova um estado do n�
        btnRemoveState.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getGraphPane().getSelected() instanceof Node) {
               controller.removeState((Node)netWindow.getGraphPane().getSelected());
            }
          }
        });

        //ao clicar no bot�o btnRemoveState, chama-se o metodo inserirEstado do controller
        //para que esse insira um novo estado no n�
        btnAddState.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getGraphPane().getSelected() instanceof Node) {
               controller.insertState((Node)netWindow.getGraphPane().getSelected());
            }
          }
        });

        // action para imprimir a rede
        btnPrintNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.printNet(netWindow.getGraphPane(), controller.calculateNetRectangle());
            }
        });

        // action para imprimir a tabela
        btnPrintTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.printTable();
            }
        });

        // action para visualizar a tabela
        btnPreviewTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.previewPrintTable();
            }
        });

        // action para visualizar a rede.
        btnPreviewNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.previewPrintNet(netWindow.getGraphPane(), controller.calculateNetRectangle());
            }
        });


        btnSaveNetImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.saveNetImage();
            }
        });


        btnSaveTableImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.saveTableImage();
            }
        });

        //colocar bot�es e controladores do look-and-feel no toolbar e esse no topPanel
        jtbEdition.add(btnPrintNet);
        jtbEdition.add(btnPreviewNet);
        jtbEdition.add(btnSaveNetImage);
        jtbEdition.add(btnPrintTable);
        jtbEdition.add(btnPreviewTable);
        jtbEdition.add(btnSaveTableImage);

        jtbEdition.addSeparator();

        jtbEdition.add(btnAddProbabilisticNode);
        jtbEdition.add(btnAddDecisionNode);
        jtbEdition.add(btnAddUtilityNode);
        jtbEdition.add(btnAddEdge);
        jtbEdition.add(btnSelectObject);
        jtbEdition.add(btnCompile);
        jtbEdition.add(btnEvaluate);

        jtbEdition.addSeparator();

        jtbEdition.add(btnGlobalOption);
        jtbEdition.add(btnHierarchy);

        topPanel.add(jtbEdition);

        //setar controladores de estados para false
        /*
        btnAddState.setEnabled(false);
        btnRemoveState.setEnabled(false);
        txtDescription.setEnabled(false);
        txtSigla.setEnabled(false);
        */

        //colocar bot�es, labels e textfields no toolbar e coloc�-lo no topPanel
        jtbState.add(sigla);
        jtbState.add(txtSigla);

        jtbState.addSeparator();
        jtbState.addSeparator();

        jtbState.add(btnAddState);
        jtbState.add(btnRemoveState);

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
     *  Retorna o text field da descri��o do n�.
     *
     *@return    retorna a txtDescri��o (<code>JTextField</code>)
     *@see       JTextField
     */
    public JTextField getTxtDescription() {
      return this.txtDescription;
    }

    /**
     *  Retorna o text field da sigla do n�.
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

    public JButton getBtnAddEdge() {
        return this.btnAddEdge;
    }

    public JButton getBtnCompile() {
        return this.btnCompile;
    }

    public JButton getBtnAddDecisionNode() {
        return this.btnAddDecisionNode;
    }

    public JLabel getDescription() {
        return this.description;
    }

    public JButton getBtnGlobalOption() {
        return this.btnGlobalOption;
    }

    public JButton getBtnRemoveState() {
        return this.btnRemoveState;
    }

    public JButton getBtnAddState() {
        return this.btnAddState;
    }

    public JButton getBtnPreviewNet() {
        return this.btnPreviewNet;
    }

    public JButton getBtnPreviewTable() {
        return this.btnPreviewTable;
    }

    public JButton getBtnPrintNet() {
        return this.btnPrintNet;
    }

    public JButton getBtnPrintTable() {
        return this.btnPrintTable;
    }

    public JButton getBtnAddProbabilisticNode() {
        return this.btnAddProbabilisticNode;
    }

    public JButton getBtnSaveNetImage() {
        return this.btnSaveNetImage;
    }

    public JButton getBtnSaveTableImage() {
        return this.btnSaveTableImage;
    }

    public JButton getBtnSelectObject() {
        return this.btnSelectObject;
    }

    public JLabel getSigla() {
        return this.sigla;
    }

    public JButton getBtnAddUtilityNode() {
        return this.btnAddUtilityNode;
    }

    public JButton getBtnHierarchy() {
        return this.btnHierarchy;
    }

}
