package unbbayes.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.controller.*;
import unbbayes.prs.*;
import unbbayes.prs.bn.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 */

public class NetWindowCompilation extends JPanel {

	private final NetWindow netWindow;

    private EvidenceTree evidenceTree;
    private final WindowController controller;
    private final JScrollPane jspTree;
    private final JSplitPane centerPanel;
    private final JLabel status;
    private final JPanel bottomPanel;

	private final JPanel topPanel;
    private final JToolBar jtbCompilation;
	private final JButton propagate;
    private final JButton expand;
    private final JButton collapse;
    private final JButton editMode;
    private final JButton log;
    private final JButton reset;
    private final JButton printNet;
    private final JButton previewNet;
    private final JButton saveNetImage;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

    public NetWindowCompilation(NetWindow _netWindow,
                          WindowController _controller) {
        super();
        this.netWindow     = _netWindow;
        this.controller    = _controller;
        this.setLayout(new BorderLayout());


        topPanel       = new JPanel(new GridLayout(0,1));
        jtbCompilation = new JToolBar();
        centerPanel    = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        evidenceTree   = new EvidenceTree(netWindow);
        jspTree        = new JScrollPane(evidenceTree);
        bottomPanel    = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status         = new JLabel(resource.getString("statusReadyLabel"));

        //criar botões que serão usados nos toolbars
        propagate         = new JButton(new ImageIcon(getClass().getResource("/icons/propagate.gif")));
        expand            = new JButton(new ImageIcon(getClass().getResource("/icons/expand-nodes.gif")));
        collapse          = new JButton(new ImageIcon(getClass().getResource("/icons/contract-nodes.gif")));
        editMode          = new JButton(new ImageIcon(getClass().getResource("/icons/edit.gif")));
        log               = new JButton(new ImageIcon(getClass().getResource("/icons/information.gif")));
        reset             = new JButton(new ImageIcon(getClass().getResource("/icons/initialize.gif")));
        printNet          = new JButton(new ImageIcon(getClass().getResource("/icons/print-net.gif")));
        previewNet        = new JButton(new ImageIcon(getClass().getResource("/icons/preview-print.gif")));
        saveNetImage      = new JButton(new ImageIcon(getClass().getResource("/icons/save-net.gif")));


        //setar tooltip para esses botões
        propagate.setToolTipText(resource.getString("propagateToolTip"));
        expand.setToolTipText(resource.getString("expandToolTip"));
        collapse.setToolTipText(resource.getString("collapseToolTip"));
        editMode.setToolTipText(resource.getString("editToolTip"));
        log.setToolTipText(resource.getString("logToolTip"));
        reset.setToolTipText(resource.getString("resetCrencesToolTip"));
        printNet.setToolTipText(resource.getString("printNetToolTip"));
        previewNet.setToolTipText(resource.getString("previewNetToolTip"));
        saveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));

        //mostra o log da rede compilada
        log.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.showLog();
                netWindow.getIGraph().update();
            }
        });

        //ao clicar no botão reset, chama-se o método de iniciação de crenças da rede
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.initialize();
            }
        });

        //volta para o modo de edição e construção da rede
        editMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.changeToNetEdition();
            }
        });

        //ao clicar nesse botão, chama-se o método do controller responsável por
        //propagar as evidencias
        propagate.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    controller.propagate();
                }
        });

        //ao clicar nesse botão, chama-se o método do controller responsável por
        //contrair árvore de evidências
        collapse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    evidenceTree.collapseTree();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
        });

        //ao clicar nesse botão, chama-se o método do controller responsável por
        //expandir árvore de evidências
        expand.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    evidenceTree.expandTree();                    
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
        });


        // action para imprimir a rede
        printNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.printNet(netWindow.getIGraph(), controller.calculateNetRectangle());
            }
        });

        // action para visualizar a rede.
        previewNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.previewPrintNet(netWindow.getIGraph(), controller.calculateNetRectangle());
            }
        });

        saveNetImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.saveNetImage();
            }
        });

        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel
        jtbCompilation.add(printNet);
        jtbCompilation.add(previewNet);
        jtbCompilation.add(saveNetImage);

        jtbCompilation.addSeparator();

        jtbCompilation.add(collapse);
        jtbCompilation.add(expand);
        jtbCompilation.add(propagate);

        jtbCompilation.addSeparator();

        jtbCompilation.add(editMode);
        jtbCompilation.add(log);
        jtbCompilation.add(reset);

        topPanel.add(jtbCompilation);

        //setar a estrutura da árvore para falso, já que ainda não foi compilada
        //jspTree.setVisible(true);

        //adicionar tela da árvore(JScrollPane) na esquerda do centerPanel
        centerPanel.setLeftComponent(jspTree);

        //setar o tamanho do divisor entre o jspGraph(vem do NetWindow) e jspTree
        centerPanel.setDividerSize(3);

        //setar os tamanho de cada jsp(arvore e draw) para os seus PreferredSizes
        centerPanel.resetToPreferredSizes();

        bottomPanel.add(status);

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     *  Retorna o painel da árvore.
     *
     *@return    retorna o jspTree (<code>JScrollPane</code>)
     *@see       JScrollPane
     */
    public JScrollPane getJspTree() {
        return this.jspTree;
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
     *  Retorna a árvore de evidencias.
     *
     *@return    retorna o evidenceTree (<code>JTree</code>)
     *@see       JTree
     */
    public EvidenceTree getEvidenceTree()
    {
        return evidenceTree;
    }

    /**
     *  Retorna o painel do centro onde fica o graph e a tree.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getCenterPanel() {
        return this.centerPanel;
    }

	public JButton getCollapse() {
		return this.collapse;
	}

	public JButton getEditMode() {
		return this.editMode;
	}

	public JButton getExpand() {
		return this.expand;
	}

	public JButton getLog() {
		return this.log;
	}

	public JButton getPreviewNet() {
		return this.previewNet;
	}

	public JButton getPrintNet() {
		return this.printNet;
	}

	public JButton getPropagate() {
		return this.propagate;
	}

	public JButton getReset() {
		return this.reset;
	}

	public JButton getSaveNetImage() {
		return this.saveNetImage;
	}
}
