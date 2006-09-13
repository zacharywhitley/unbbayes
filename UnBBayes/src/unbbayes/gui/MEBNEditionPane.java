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
import unbbayes.controller.WindowController;
import unbbayes.prs.Node;

public class MEBNEditionPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6194855055129252835L;
	
	
	
	private final NetWindow netWindow;

    private GlobalOptionsDialog go;
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

    private final JButton btnCompile;
    private final JButton btnAddState;
    private final JButton btnRemoveState;
    private final JButton btnAddEdge;
    private final JButton btnAddContextNode;
    private final JButton btnAddInputNode;
    private final JButton btnAddResidentNode;
    private final JButton btnSelectObject;
    private final JButton btnGlobalOption;
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;

    private final IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

  	public MEBNEditionPane(NetWindow _netWindow,
            WindowController _controller) {
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

        //criar botões que serão usados nodeList toolbars
        btnCompile           = new JButton(iconController.getCompileIcon());
        btnAddState              = new JButton(iconController.getMoreIcon());
        btnRemoveState              = new JButton(iconController.getLessIcon());
        btnAddEdge               = new JButton(iconController.getEdgeIcon());
        btnAddContextNode = new JButton(iconController.getContextNodeIcon());
        btnAddInputNode      = new JButton(iconController.getInputNodeIcon());
        btnAddResidentNode       = new JButton(iconController.getResidentNodeIcon());
        btnSelectObject            = new JButton(iconController.getSelectionIcon());
        btnGlobalOption      = new JButton(iconController.getGlobalOptionIcon());

        //setar tooltip para esses botões
        btnCompile.setToolTipText(resource.getString("compileToolTip"));
        btnAddState.setToolTipText(resource.getString("moreToolTip"));
        btnRemoveState.setToolTipText(resource.getString("lessToolTip"));
        btnAddEdge.setToolTipText(resource.getString("arcToolTip"));
        btnAddContextNode.setToolTipText(resource.getString("probabilisticNodeInsertToolTip"));
        btnAddInputNode.setToolTipText(resource.getString("decisionNodeInsertToolTip"));
        btnAddResidentNode.setToolTipText(resource.getString("utilityNodeInsertToolTip"));;
        btnSelectObject.setToolTipText(resource.getString("selectToolTip"));
        btnGlobalOption.setToolTipText(resource.getString("globalOptionTitle"));

        //ao clicar no botão btnGlobalOption, mostra-se o menu para escolha das opções
        btnGlobalOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                go = new GlobalOptionsDialog(netWindow.getGraphPane(), controller);
                go.setVisible(true);
                netWindow.getGraphPane().update();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        //ao clicar no botão btnCompile, chama-se o método de compilação da rede e
        //atualiza os toolbars
        btnCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (! controller.compileNetwork()) {
                    return;
                }
                netWindow.changeToNetCompilation();
            }
        });

        //ao clicar no botão btnAddEdge setamos as variáveis booleanas e os estados dos butões
        btnAddEdge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_EDGE);
            }
        });

        //ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
        btnAddContextNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE);
            }
        });


        //ao clicar no botão btnAddInputNode setamos as variáveis booleanas e os estados dos butões
        btnAddInputNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE);
            }
        });

        //ao clicar no botão btnAddResidentNode setamos as variáveis booleanas e os estados dos butões
        btnAddResidentNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	netWindow.getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE);
            }
        });


        //ao clicar no botão node setamos as variáveis booleanas e os estados dos butões
        btnSelectObject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	netWindow.getGraphPane().setAction(GraphAction.SELECT_MANY_OBJECTS);
            }
        });

        // listener responsável pela atualização do texo da sigla do nó
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


        // listener responsável pela atualização do texo da descrição do nó
        txtDescription.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            Object selected = netWindow.getGraphPane().getSelected();
            if (selected instanceof Node)
            {
              Node nodeAux = (Node)selected;
              if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtDescription.getText().length()>0)) {
                try {
                    String name = txtDescription.getText(0,txtDescription.getText().length());
                    matcher = wordPattern.matcher(name);
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

        //ao clicar no botão btnRemoveState, chama-se o metodo removerEstado do controller
        //para que esse remova um estado do nó
        btnRemoveState.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getGraphPane().getSelected() instanceof Node) {
               controller.removeState((Node)netWindow.getGraphPane().getSelected());
            }
          }
        });

        //ao clicar no botão btnRemoveState, chama-se o metodo inserirEstado do controller
        //para que esse insira um novo estado no nó
        btnAddState.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            if (netWindow.getGraphPane().getSelected() instanceof Node) {
               controller.insertState((Node)netWindow.getGraphPane().getSelected());
            }
          }
        });

        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel

        jtbEdition.add(btnAddContextNode);
        jtbEdition.add(btnAddInputNode);
        jtbEdition.add(btnAddResidentNode);
        jtbEdition.add(btnAddEdge);
        jtbEdition.add(btnSelectObject);
        jtbEdition.add(btnCompile);

        jtbEdition.addSeparator();

        jtbEdition.add(btnGlobalOption);

        topPanel.add(jtbEdition);

        //colocar botões, labels e textfields no toolbar e colocá-lo no topPanel
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

    public JButton getBtnAddEdge() {
        return this.btnAddEdge;
    }

    public JButton getBtnCompile() {
        return this.btnCompile;
    }

    public JButton getBtnAddInputNode() {
        return this.btnAddInputNode;
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

    public JButton getBtnAddContextNode() {
        return this.btnAddContextNode;
    }

    public JButton getBtnSelectObject() {
        return this.btnSelectObject;
    }

    public JLabel getSigla() {
        return this.sigla;
    }

    public JButton getBtnAddResidentNode() {
        return this.btnAddResidentNode;
    }

}
