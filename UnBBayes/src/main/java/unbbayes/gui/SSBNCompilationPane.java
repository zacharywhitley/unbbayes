package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.xml.bind.JAXBException;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.prs.bn.SingleEntityNetwork;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 */

public class SSBNCompilationPane extends JPanel {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private final NetworkWindow netWindow;

    private EvidenceTree evidenceTree;
    private final NetworkController controller;
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
    private final JButton saveNet;
    
    private final SingleEntityNetwork network;

    private final IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

  	public SSBNCompilationPane(){
  		editMode = null;
  		log = null;
  		netWindow = null;
  		controller = null;
  		jspTree = null;
  		centerPanel = null;
  		bottomPanel = null;
  		topPanel = null;
  		jtbCompilation = null;
  		status = null;
  		propagate = null;
  	    expand = null;
  	    collapse = null;
  	    reset = null;
  	    printNet = null;
  	    previewNet = null;
  	    saveNetImage = null;
  	    saveNet = null; 
  	    network = null; 
  	}
  	
    public SSBNCompilationPane(SingleEntityNetwork sen, NetworkWindow _netWindow,
                          NetworkController _controller) {
        super();
        this.netWindow     = _netWindow;
        this.controller    = _controller;
        this.setLayout(new BorderLayout());

        this.network = sen;
        
        topPanel       = new JPanel(new GridLayout(0,1));
        jtbCompilation = new JToolBar();
        centerPanel    = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        evidenceTree   = new EvidenceTree(sen, netWindow);
        jspTree        = new JScrollPane(evidenceTree);
        bottomPanel    = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        status         = new JLabel(resource.getString("statusReadyLabel"));

        //criar bot_es que ser_o usados nodeList toolbars
        propagate         = new JButton(iconController.getPropagateIcon());
        expand            = new JButton(iconController.getExpandIcon());
        collapse          = new JButton(iconController.getColapseIcon());
        editMode          = new JButton(iconController.getEditIcon());
        log               = new JButton(iconController.getInformationIcon());
        reset             = new JButton(iconController.getInitializeIcon());
        printNet          = new JButton(iconController.getPrintNetIcon());
        previewNet        = new JButton(iconController.getPrintPreviewNetIcon());
        saveNetImage      = new JButton(iconController.getSaveNetIcon());
        saveNet           = new JButton(iconController.getSaveIcon());

        //setar tooltip para esses bot_es
        propagate.setToolTipText(resource.getString("propagateToolTip"));
        expand.setToolTipText(resource.getString("expandToolTip"));
        collapse.setToolTipText(resource.getString("collapseToolTip"));
        editMode.setToolTipText(resource.getString("editToolTip"));
        log.setToolTipText(resource.getString("logToolTip"));
        reset.setToolTipText(resource.getString("resetCrencesToolTip"));
//        printNet.setToolTipText(resource.getString("printNetToolTip"));
//        previewNet.setToolTipText(resource.getString("previewNetToolTip"));
//        saveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));

        //mostra o log da rede compilada
        log.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.showLog();
                netWindow.getGraphPane().update();
            }
        });

        //ao clicar no bot_o reset, chama-se o m_todo de inicia__o de cren_as da rede
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.initialize();
            }
        });

//        volta para o modo de edi__o e constru__o da rede
        editMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.changeToMEBNEditionPane();
            }
        });

        //ao clicar nesse bot_o, chama-se o m_todo do controller respons_vel por
        //propagar as evidencias
        propagate.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    controller.propagate();
                }
        });

        //ao clicar nesse bot_o, chama-se o m_todo do controller respons_vel por
        //contrair _rvore de evid_ncias
        collapse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    evidenceTree.collapseTree();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
        });

        //ao clicar nesse bot_o, chama-se o m_todo do controller respons_vel por
        //expandir _rvore de evid_ncias
        expand.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    evidenceTree.expandTree();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
        });


//        // action para imprimir a rede
        printNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.printNet(netWindow.getGraphPane(), controller.calculateNetRectangle());
            }
        });

        // action para visualizar a rede.
        previewNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.previewPrintNet(netWindow.getGraphPane(), controller.calculateNetRectangle());
            }
        });

        saveNetImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.saveNetImage();
            }
        });

        saveNet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
    			setCursor(new Cursor(Cursor.WAIT_CURSOR));
    			
    			JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    			chooser.setMultiSelectionEnabled(false);
    			chooser
    			.setFileSelectionMode(JFileChooser.FILES_ONLY);
    			
    			int option = chooser.showSaveDialog(null);
    			if (option == JFileChooser.APPROVE_OPTION) {
    				File file = chooser.getSelectedFile();
    				if (file != null) {
    					if (!file.isDirectory()){
    						BaseIO io = null; 
    						String name = file.getName().toLowerCase();							
    						if (name.endsWith("net")) {
    							io = new NetIO();	
    							try {
									io.save(file, network);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (JAXBException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
    						} 
    						else if (name.endsWith("xml")){
    							io = new XMLIO();
    							try {
									io.save(file, network);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (JAXBException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
    						}
    						try {
    							if(io != null){
    								io.save(file, network);
    							}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (JAXBException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
    						JOptionPane.showMessageDialog(controller.getScreen(), "Arquivo salvo com sucesso");
    					}
    				}
    			}
    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
        	
        }); 
        //colocar bot_es e controladores do look-and-feel no toolbar e esse no topPanel
        //TODO fazer os botões funcionarem e colocá-los de volta...
//        jtbCompilation.add(printNet);
//        jtbCompilation.add(previewNet);
//        jtbCompilation.add(saveNetImage);

        jtbCompilation.addSeparator();

        jtbCompilation.add(collapse);
        jtbCompilation.add(expand);
        jtbCompilation.add(propagate);

        jtbCompilation.addSeparator();

        jtbCompilation.add(reset);
        
        jtbCompilation.addSeparator();

        jtbCompilation.add(editMode);
        jtbCompilation.add(log);

        jtbCompilation.addSeparator();
        jtbCompilation.add(saveNet);
        
        topPanel.add(jtbCompilation);
        

        //setar a estrutura da _rvore para falso, j_ que ainda n_o foi compilada
        //jspTree.setVisible(true);

        //adicionar tela da _rvore(JScrollPane) na esquerda do centerPanel
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
     *  Retorna o painel da _rvore.
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
     *  Retorna a _rvore de evidencias.
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

//	public JButton getEditMode() {
//		return this.editMode;
//	}

	public JButton getExpand() {
		return this.expand;
	}
//
//	public JButton getLog() {
//		return this.log;
//	}
//
//	public JButton getPreviewNet() {
//		return this.previewNet;
//	}
//
//	public JButton getPrintNet() {
//		return this.printNet;
//	}

	public JButton getPropagate() {
		return this.propagate;
	}

	public JButton getReset() {
		return this.reset;
	}
//
//	public JButton getSaveNetImage() {
//		return this.saveNetImage;
//	}
}
