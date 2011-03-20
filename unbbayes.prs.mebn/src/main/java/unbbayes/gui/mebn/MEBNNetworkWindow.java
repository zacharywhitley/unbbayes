/**
 * 
 */
package unbbayes.gui.mebn;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;

import unbbayes.controller.ConfigurationsController;
import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.EvidenceTree;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.mebn.auxiliary.ButtonLabel;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelPluginManager.IMEBNEditionPanelPluginComponents;
import unbbayes.gui.mebn.extension.editor.MEBNEditionPanelPluginManager;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.GraphLayoutUtil;
import unbbayes.util.ResourceController;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * Codes from NetworkWindow which was dealing  MEBN was migrated into here.
 * This class represents a MEBN module, by also extending UnBBayesModule.
 * @author Shou Matsumoto
 *
 */
public class MEBNNetworkWindow extends NetworkWindow {

	public static final Integer MEBN_MODE = 1;
	
	private static final String MEBN_PANE_MEBN_EDITION_PANE = "mebnEditionPane";

	private static final String MEBN_PANE_SSBN_COMPILATION_PANE = "ssbnCompilationPane";
	
	private MEBNEditionPane mebnEditionPane = null;

	private SSBNCompilationPane ssbnCompilationPane = null;
	
	/** The resource is not static, so that hotplug would become easier */
	private ResourceBundle resource;

	private JPanel mainContentPane;

	private JTabbedPane topTabbedPane;
	
//	private static final String[] SUPPORTED_FILE_EXTENSIONS_MEBN = { unbbayes.io.mebn.UbfIO.FILE_EXTENSION };

	/** This is the context for UnBBayes' plugin framework */
	private IMEBNEditionPanelPluginManager pluginManager = MEBNEditionPanelPluginManager.newInstance(false);	// instantiate, do not initialize

	private JScrollPane pluginDistributionScrollPane;
	
	private JToolBar pluginDistributionBar;
	
	private JButton pluginNodeToolBarButton;

	private JTextField pluginNodeNameTextField;


	/**
	 * Default constructor.
	 * It is made public because of plugin support. It is not recommended
	 * to use this constructor unless you are extending this class.
	 * @deprecated
	 */
	public MEBNNetworkWindow() {
		super();
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.resources.Resources.class.getName());
	}

	/**
	 * Initializes a MEBN module using a network as a parameter.
	 * It also adds a listener to call {@link #reloadMEBNEditionTabs()} when
	 * a "reload plugins" action is triggered by UnBBayes (usually, it happens when
	 * a user press the "reload plugins" button/menu).
	 * @param net
	 */
	public MEBNNetworkWindow(Network net) {
		this();
		this.setModuleName(net.getName());
		this.setTitle(net.getName());
		this.setName(this.getName());
		
		// the below code is a copy from superclass
		
		this.setNet(net); 
		this.setFileName(null); 
		
		// this is the top level container where all MEBN edition panels (tabs) will be placed
		this.setTopTabbedPane(new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
		this.getTopTabbedPane().addMouseListener(new MouseListener() {
			// notify on tab change (which is tab press event)
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				getTopTabbedPane().getSelectedComponent().firePropertyChange(
						IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, 
						-1,	// we must set a invalid value here, because it seems that the values must actually change, in order to trigger a property change event...
						getTopTabbedPane().getSelectedIndex());
			}
			public void mouseReleased(MouseEvent e) {}
			
		});
		this.getContentPane().add(this.getTopTabbedPane());
		
		
//		Container mainContentPane = getMainContentPane();
		
		// this is the default MEBN edition panel
		this.setCardLayout(new CardLayout());		// the superclass seems to use something about the layout of the main content panel
		this.setMainContentPane(new JPanel(this.getCardLayout()));
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		// trigger action when we choose a tab to switch between different edition panes (e.g. from plugin to default edition pane)
		this.getMainContentPane().addPropertyChangeListener(
				IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, 
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						try {
							// delegate...
							getMebnEditionPane().firePropertyChange(
									IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, 
									-1, 
									getTopTabbedPane().getSelectedIndex());
							// update graph 
							getController().getScreen().getGraphPane().resetGraph();
							
							// update itself
							getMainContentPane().updateUI();
							getMainContentPane().repaint();
							// add code here to do something when user change tabs
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		// instancia variaveis de instancia
		this.setGraphViewport(new JViewport());
		this.setController(new MEBNController((MultiEntityBayesianNetwork) net, this));
		this.setMebnEditionPane(((MEBNController)this.getController()).getMebnEditionPane());
		
		this.setModuleName(this.resource.getString("MEBNModuleName"));

		this.setGraphPane(new MEBNGraphPane(this.getController(), this.getGraphViewport()));

		this.setJspGraph(new JScrollPane(this.getGraphViewport()));
		
		this.setBCompiled(false);
		

		this.getJspGraph().getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		this.getJspGraph().getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		// set default values for jspGraph
		this.getJspGraph().setHorizontalScrollBar(this.getJspGraph().createHorizontalScrollBar());
		this.getJspGraph().setVerticalScrollBar(this.getJspGraph().createVerticalScrollBar());
		this.getJspGraph().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.getJspGraph().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.setMode(MEBN_MODE);
		this.setSsbnCompilationPane(new SSBNCompilationPane());
		mainContentPane.add(this.getMebnEditionPane(), MEBN_PANE_MEBN_EDITION_PANE);
		mainContentPane.add(this.getSsbnCompilationPane(), MEBN_PANE_SSBN_COMPILATION_PANE);
		
		// inicia com a tela de edicao de rede(PNEditionPane)
		this.getMebnEditionPane().getGraphPanel().setBottomComponent(this.getJspGraph());
		this.getCardLayout().show(getMainContentPane(), MEBN_PANE_MEBN_EDITION_PANE);

		setVisible(true);
		this.getGraphPane().update();
		
		// add a listener to reload tabs when "reload plugins" button is pressed
		try {
			this.getPluginManager().getPluginContextHolder().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
				public void onReload(EventObject eventObject) {
					reloadMEBNEditionTabs();	// only reload tabs.
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// start loading plugins. This method also adds the main content panel to the tabbed panel
		this.reloadMEBNEditionTabs();
	}
	
	/**
	 * This method initializes and loads/reloads the tabs for MEBN edition, including plugins.
	 * It uses the UnBBayes' plugin framework using a context obtained from {@link #getPluginContextHolder()}.
	 * Usually, this method is called when a "reload plugin" action is triggered. 
	 * This is implemented as {@link UnBBayesPluginContextHolder.OnReloadActionListener}.
	 * @see UnBBayesPluginContextHolder
	 * @see #getPluginContextHolder()
	 * @see #setPluginContextHolder(UnBBayesPluginContextHolder)
	 */
	public void reloadMEBNEditionTabs() {

		// assertions
		if (this.getTopTabbedPane() == null) {
			// this is just in case someone has called this method before top tabbed pane is initialized
			this.setTopTabbedPane(new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT));
			this.getContentPane().add(this.getTopTabbedPane());
		}
		
		// resets the top pane
		this.getTopTabbedPane().removeAll();
		
		// add the main content pane in the top pane as the default MEBN editor
		this.getTopTabbedPane().addTab(	
				 						this.resource.getString("defaultMEBNEditor"), 
										IconController.getInstance().getMTheoryNodeIcon(), 
										this.getMainContentPane(), 
										this.resource.getString("defaultMEBNEditorTip"));
		
		try {
			// actually reload the plugins
			this.getPluginManager().reloadPlugins();
			
			// obtains the result of the above operation and iterate over it
			for (IMEBNEditionPanelPluginComponents editionPanelPluginComponent : this.getPluginManager().getLoadedComponents()) {
				try {
					Component pluginTab = editionPanelPluginComponent.getPanelBuilder().buildPanel(getMultiEntityBayesianNetwork(), (IMEBNMediator)getController());
					if (pluginTab == null) {
						// do not add tab if plugin did not return a panel
						continue;
					}
					// add the plugins to tab
					this.getTopTabbedPane().addTab( editionPanelPluginComponent.getName(),
													editionPanelPluginComponent.getIcon(), 
													pluginTab, 
													editionPanelPluginComponent.getDescription());
				} catch (Throwable e) {
					// ignore every errors caused by plugins
					e.printStackTrace();
				}
			}
		} catch (Throwable t) {
			// ignore every errors caused by plugins
			t.printStackTrace();
		}
	}

	/**
	 * This method changes the main screen from compilation pane to edition pane
	 */
	public void changeToMEBNEditionPane() {

		if (this.getMode() == MEBN_MODE) {
			//by young
			//Node.setSize(MultiEntityNode.getDefaultSize().getX(), MultiEntityNode.getDefaultSize().getY());
			this.getGraphPane().addKeyListener(this.getController());

			try{
				((MEBNController)this.getController()).setEditionMode();
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("The controller of this network window must be set to MEBNController", e);
			}
			
			this.getGraphPane().resetGraph();
			
			// starts the network edition screen (PNEditionPane)
			this.getMebnEditionPane().getGraphPanel().setBottomComponent(this.getJspGraph());
			this.getMebnEditionPane().updateToPreferredSize();
			this.getCardLayout().show(getMainContentPane(), MEBN_PANE_MEBN_EDITION_PANE);
		}
	}
	
	/**
	 * This method changes the main screen from edition pane to 
	 * compilation pane
	 */
	public void changeToSSBNCompilationPane(SingleEntityNetwork ssbn) {

		if (this.getMode()  == MEBN_MODE) {

			Container mainContentPane = getMainContentPane();
			mainContentPane.remove(this.getSsbnCompilationPane());

			this.setSsbnCompilationPane(new SSBNCompilationPane(ssbn, this,this.getController()));
			this.getGraphPane().resetGraph();
			ssbnCompilationPane.getCenterPanel().setRightComponent(this.getJspGraph());
			ssbnCompilationPane.setStatus(this.getStatus().getText());
			ssbnCompilationPane.getEvidenceTree().setRootVisible(true);
			ssbnCompilationPane.getEvidenceTree().expandRow(0);
			ssbnCompilationPane.getEvidenceTree().setRootVisible(false);
			ssbnCompilationPane.getEvidenceTree().updateTree(true);
			
			(new GraphLayoutUtil(ssbn)).doLayout();
			
			ssbnCompilationPane.getEvidenceTree().selectTreeItemByNode(ssbn.getNodeAt(0));
			this.getGraphPane().compiled(true, ssbn.getNodeAt(0));
			
			mainContentPane.add(ssbnCompilationPane,
					MEBN_PANE_SSBN_COMPILATION_PANE);
			
			ssbnCompilationPane.updateToPreferredSize(); 
			
			CardLayout layout = (CardLayout) mainContentPane.getLayout();
			layout.show(getMainContentPane(), MEBN_PANE_SSBN_COMPILATION_PANE);
		}
	}
	
	/**
	 * Opens a new desktop window into currently used java desktop
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		
		Graph g = null;
		
		// This IO is instantiated at MEBNController' constructor.
		// Note that NetworkWindow#getIO() actually calls MEBNController#getBaseIO()
		BaseIO io = this.getIO();
		
		try {
			g = io.load(file);
		} catch (FileExtensionIODelegator.MoreThanOneCompatibleIOException e) {
			// More than one I/O was found to be compatible. Ask user to select one.
			String[] possibleValues = FileExtensionIODelegator.getNamesFromIOs(e.getIOs());
	    	String selectedValue = (String)JOptionPane.showInputDialog(
	    			this, 
	    			resource.getString("IOConflictMessage"), 
	    			resource.getString("IOConflictTitle"),
	    			JOptionPane.INFORMATION_MESSAGE, 
	    			null,
	    			possibleValues, 
	    			possibleValues[0]);
	    	if (selectedValue != null) {
	    		g = FileExtensionIODelegator.findIOByName(e.getIOs(), selectedValue).load(file);
	    	} else {
	    		// user appears to have cancelled
	    		this.dispose();
		    	return null;
	    	}
		}
		
		MEBNNetworkWindow window = null;
		
		try {
			ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
			window = new MEBNNetworkWindow((Network)g);	
			window.setFileName(file.getName().toLowerCase()); 
		} catch (Exception e) {
			throw new RuntimeException(this.resource.getString("unsupportedGraphFormat"),e);
		}
		
		// we do not use this current instance. Instead, dispose it and return the new instance of window
		this.dispose();
		return window;
	}
	
	/**
	 * This method is overwritten so that it displays the node editor in MEBN GUI instead of the ordinal BN GUI
	 * @see unbbayes.gui.NetworkWindow#showProbabilityDistributionPanel(unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder)
	 */
	public void showProbabilityDistributionPanel(IProbabilityFunctionPanelBuilder builder) {
		
		// building the panel using associated node
		this.setDistributionPane(builder.buildProbabilityFunctionEditionPanel());
		
		// create tool bar to edit the name of the plugin node
		if (this.getPluginNodeNameToolBar() == null) {
			
			// create plugin tool bar (where we edit plugin node's name) if it is not already created
			this.setPluginNodeNameToolBar(new JToolBar());
			this.getPluginNodeNameToolBar().setFloatable(false);
			this.getPluginNodeNameToolBar().setLayout(new GridLayout(1,5));
			
			// create the components in the tool bar (this is a button with label)
			this.setPluginNodeNameToolBarButton(new ButtonLabel(resource.getString("nodeName"), IconController.getInstance().getNodeNodeIcon()));
	    	this.getPluginNodeNameToolBar().add(this.getPluginNodeNameToolBarButton());
	    	
	    	// add listener to the button with label
	    	final IProbabilityFunctionPanelBuilder paramToListener = builder;	// use a final variable as a parameter for the listener
	    	this.getPluginNodeNameToolBarButton().addActionListener(new ActionListener()  {
				public void actionPerformed(ActionEvent e) {
					showProbabilityDistributionPanel(paramToListener);
				}
			});
	    	
	    	
	    	// this is the text field to edit the name
			this.setPluginNodeNameTextField(new JTextField(builder.getProbabilityFunctionOwner().getName() ,5));
			this.getPluginNodeNameTextField().setForeground(Color.BLACK);
	    	// use another tool bar as a container for the text field (this is just to make sure that it looks like the resident nodes' tool bar)
	    	JToolBar textFieldContainer = new JToolBar();
      		textFieldContainer.setFloatable(false);
      		textFieldContainer.add(this.getPluginNodeNameTextField());
      		this.getPluginNodeNameToolBar().add(textFieldContainer);
      		
      		// do some adjustments just to look like the other tool bars
      		for (int i = 0; i < 3; i++) {
      			// add disabled buttons
      			this.getPluginNodeNameToolBar().add(new JButton() {
      				protected void init(String text, Icon icon) {
      					super.init(text, icon);
      					setEnabled(false);
      				}
      			});
			}
      		
      		// add listeners to the text field so that it behaves like the resident node's tool bar
      		this.getPluginNodeNameTextField().addFocusListener(new FocusListenerTextField());
	    	
	    	// add a key listener so that it behaves similarly to resident nodes
      		this.getPluginNodeNameTextField().addKeyListener(new KeyAdapter() {
      			public void keyPressed(KeyEvent e) {
      				Node nodeAux = getController().getSelectedNode();

      				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (getPluginNodeNameTextField().getText().length()>0)) {
      					try {
      						String name = getPluginNodeNameTextField().getText(0,getPluginNodeNameTextField().getText().length());
      						Matcher matcher = Pattern.compile("[a-zA-Z_0-9]*").matcher(name);
      						if (matcher.matches()) {
      							// update name
      							nodeAux.setName(name);
      							// update the GUI so that it displays the new name
      							getMebnEditionPane().repaint();
      							getMebnEditionPane().getNetworkWindow().getGraphPane().update(); 
      							if (getPluginDistributionScrollPane() != null) {
      								getPluginDistributionScrollPane().updateUI();
      								getPluginDistributionScrollPane().repaint();
      								if (getPluginDistributionScrollPane().getViewport() != null
      										&& getPluginDistributionScrollPane().getViewport().getView() != null) {
      									getPluginDistributionScrollPane().getViewport().getView().repaint();
      								}
      							} 
      							
      						}  else {
      							getPluginNodeNameTextField().setBackground(MebnToolkit.getColorTextFieldError());
//      							getPluginNodeNameTextField().setForeground(Color.WHITE);
      							getPluginNodeNameTextField().selectAll();
      							JOptionPane.showMessageDialog(MEBNNetworkWindow.this,
      									resource.getString("nameError"),
      									resource.getString("nameException"),
      									JOptionPane.ERROR_MESSAGE);
      						}
      					}
      					catch (javax.swing.text.BadLocationException ble) {
      						System.out.println(ble.getMessage());
						} catch (Exception e2) {
							e2.printStackTrace();
							JOptionPane.showMessageDialog(MEBNNetworkWindow.this,
  									e2.getMessage(),
  									resource.getString("nameError"),
  									JOptionPane.ERROR_MESSAGE);
						}
      				}
      			}

      			public void keyReleased(KeyEvent e){
      				try{
      						String name = getPluginNodeNameTextField().getText(0,getPluginNodeNameTextField().getText().length());
      						Matcher matcher = Pattern.compile("[a-zA-Z_0-9]*").matcher(name);
    						if (!matcher.matches()) {
    							getPluginNodeNameTextField().setBackground(MebnToolkit.getColorTextFieldError());
//    							getPluginNodeNameTextField().setForeground(Color.WHITE);
    						}
    						else{
    							getPluginNodeNameTextField().setBackground(MebnToolkit.getColorTextFieldSelected());
    							getPluginNodeNameTextField().setForeground(Color.BLACK);
    						}
      				} catch(Exception efd){
      					efd.printStackTrace();
      				}

      			}
      		});
      		// add the new tool bar as "PluginNodeToolBar" into the card 
			this.getMebnEditionPane().getNodeSelectedToolBar().add("PluginNodeToolBar", this.getPluginNodeNameToolBar());
		} else {
			// reuse the one if already created. Just change the displayed text.
			this.getPluginNodeNameTextField().setText(builder.getProbabilityFunctionOwner().getName());
			this.getPluginNodeNameTextField().updateUI();
			this.getPluginNodeNameTextField().repaint();
		}
		
		// show the plugin name name tool bar
		this.getMebnEditionPane().getCardLayout().show(this.getMebnEditionPane().getNodeSelectedToolBar(), "PluginNodeToolBar");
		
		// the below line is a requisite from inherited code, so, it is not very important for plugin context
//		this.setTableOwner(builder.getProbabilityFunctionOwner());
		
		// update the name/description text fields at tool box
//		this.getTxtDescription().setText(builder.getProbabilityFunctionOwner().getDescription());
//		this.getTxtName().setText(builder.getProbabilityFunctionOwner().getName());
	}
	
	
	
	/**
	 * Displays the distributionPane in the MEBN edition pane.
	 * This is used mostly by plug-in nodes.
	 * @param distributionPane
	 * @see #showProbabilityDistributionPanel(IProbabilityFunctionPanelBuilder)
	 * @see unbbayes.gui.NetworkWindow#setDistributionPane(javax.swing.JPanel)
	 */
	public void setDistributionPane(JPanel distributionPane) {
		
		// create plugin panel if it is not already created
		if (this.getPluginDistributionScrollPane() == null) {
			this.setPluginDistributionScrollPane(new JScrollPane(distributionPane));
			this.getMebnEditionPane().getJpTabSelected().add("PluginNodeTab", this.getPluginDistributionScrollPane());
		} else {
			this.getPluginDistributionScrollPane().setViewportView(distributionPane);
			this.getPluginDistributionScrollPane().updateUI();
			this.getPluginDistributionScrollPane().repaint();
		}
		
		// hide other components, because it seems that the card layout is not hiding the others
		for (Component comp : this.getMebnEditionPane().getJpTabSelected().getComponents()) {
			comp.setVisible(false);
		}
		
		this.getPluginDistributionScrollPane().setVisible(true);
		
		this.getMebnEditionPane().getCardLayout().show(this.getMebnEditionPane().getJpTabSelected(), "PluginNodeTab");

    	
	}
	
	/**
	 * Retorna a rede probabil_stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil_stica
	 * @see ProbabilisticNetwork
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return (MultiEntityBayesianNetwork) this.getController().getNetwork();
	}

	/**
	 * @return the mebnEditionPane
	 */
	public MEBNEditionPane getMebnEditionPane() {
		return mebnEditionPane;
	}

	/**
	 * @param mebnEditionPane the mebnEditionPane to set
	 */
	public void setMebnEditionPane(MEBNEditionPane mebnEditionPane) {
		this.mebnEditionPane = mebnEditionPane;
	}
	
	/**
	 * Obtains the evidence tree
	 * 
	 * @return  (<code>JTree</code>)
	 * @see JTree
	 */
	public EvidenceTree getEvidenceTree() {
		if (ssbnCompilationPane != null) {
			return ssbnCompilationPane.getEvidenceTree();
		} else {
			return null;
		}
	}
	
	/**
	 * Updates the status shown at compilation pane
	 * 
	 * @param status
	 *            status message.
	 */
	public void setStatus(String status) {
		try {
			super.setStatus(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ssbnCompilationPane != null) {
			ssbnCompilationPane.setStatus(status);
		}
	}

	/**
	 * @return the ssbnCompilationPane
	 */
	public SSBNCompilationPane getSsbnCompilationPane() {
		return ssbnCompilationPane;
	}

	/**
	 * @param ssbnCompilationPane the ssbnCompilationPane to set
	 */
	public void setSsbnCompilationPane(SSBNCompilationPane ssbnCompilationPane) {
		this.ssbnCompilationPane = ssbnCompilationPane;
	}

	/**
	 * This is the panel where the default MEBN edition is placed.
	 * @return the mainContentPane
	 */
	public JPanel getMainContentPane() {
		return mainContentPane;
	}

	/**
	 * This is the panel where the default MEBN edition is placed.
	 * @param mainContentPane the mainContentPane to set
	 */
	public void setMainContentPane(JPanel mainContentPane) {
		this.mainContentPane = mainContentPane;
	}

	/**
	 * this is the top level container where all MEBN edition panels (tabs) will be placed
	 * @return the topTabbedPane
	 */
	public JTabbedPane getTopTabbedPane() {
		return topTabbedPane;
	}

	/**
	 * this is the top level container where all MEBN edition panels (tabs) will be placed
	 * @param topTabbedPane the topTabbedPane to set
	 */
	public void setTopTabbedPane(JTabbedPane topTabbedPane) {
		this.topTabbedPane = topTabbedPane;
	}

	/**
	 * This is the manager for MEBN editor panel's plugin framework.
	 * This is used by {@link #reloadMEBNEditionTabs()} in order to load
	 * plugins.
	 * @return the pluginManager
	 */
	public IMEBNEditionPanelPluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * This is the manager for MEBN editor panel's plugin framework.
	 * This is used by {@link #reloadMEBNEditionTabs()} in order to load
	 * plugins.
	 * @param pluginManager the pluginManager to set
	 */
	public void setPluginManager(IMEBNEditionPanelPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	/**
	 * This is the last plugin node editor pane created in {@link #setDistributionPane(JPanel)}
	 * @return the pluginDistributionScrollPane
	 */
	public JScrollPane getPluginDistributionScrollPane() {
		return pluginDistributionScrollPane;
	}

	/**
	 * This is the last plugin node editor pane created in {@link #setDistributionPane(JPanel)}
	 * @param pluginDistributionScrollPane the pluginDistributionScrollPane to set
	 */
	public void setPluginDistributionScrollPane(
			JScrollPane pluginDistributionScrollPane) {
		this.pluginDistributionScrollPane = pluginDistributionScrollPane;
	}

	/**
	 * This is the toolbar used for changing plugin node name.
	 * @return the pluginDistributionBar
	 */
	public JToolBar getPluginNodeNameToolBar() {
		return pluginDistributionBar;
	}

	/**
	 * This is the toolbar used for changing plugin node name.
	 * @param pluginDistributionBar the pluginDistributionBar to set
	 */
	public void setPluginNodeNameToolBar(JToolBar pluginDistributioBar) {
		this.pluginDistributionBar = pluginDistributioBar;
	}

	/**
	 * This is a button inside {@link #getPluginNodeNameToolBar()}
	 * @return the pluginNodeToolBarButton
	 */
	public JButton getPluginNodeNameToolBarButton() {
		return pluginNodeToolBarButton;
	}

	/**
	 * This is a button inside {@link #getPluginNodeNameToolBar()}
	 * @param pluginNodeToolBarButton the pluginNodeToolBarButton to set
	 */
	public void setPluginNodeNameToolBarButton(
			JButton pluginNodeToolBarButton) {
		this.pluginNodeToolBarButton = pluginNodeToolBarButton;
	}

	/**
	 * This is a text field to change plugin node name.
	 * @return the pluginNodeNameTextField
	 * @see #getPluginNodeNameToolBar()
	 */
	public JTextField getPluginNodeNameTextField() {
		return pluginNodeNameTextField;
	}

	/**
	 * This is a text field to change plugin node name.
	 * @param pluginNodeNameTextField the pluginNodeNameTextField to set
	 * @see #getPluginNodeNameToolBar()
	 */
	public void setPluginNodeNameTextField(JTextField pluginNodeNameTextField) {
		this.pluginNodeNameTextField = pluginNodeNameTextField;
	}



}
