/**
 * 
 */
package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;
import unbbayes.prs.mebn.kb.extension.jpf.KnowledgeBasePluginManager;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This dialog manages options for MEBN (e.g. Knowledge base's attributes)
 * @author Shou Matsumoto
 *
 */
public class OptionsDialog extends JDialog {
	
	/** This reference may be used to change some MEBN options */
	private MEBNController controller;
	
	// Graphical contents
	
	/** this is the upper tabs */
	private JTabbedPane tabPane;
	
	/** This is where confirm and cancel button resides */
	private JPanel confirmationPanel;
	private JButton cancel;
	private JButton confirm;

	
	// content of KB options
	
	/** This is where all KB-related panels resides */
	private JComponent kbMainPanel;
	
	/** This is where kb's radio button resides */
    private JPanel kbRadioPanel;
    
    /** This group manages all loaded KB */
    private ButtonGroup kbGroup;
    
    /** This is where the currently selected KB's options resides */
	private JPanel kbOptionPane;
	
	/** This map relates a radio button to what option panel it represents */
    private Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> kbToOptionMap = new HashMap<JRadioButtonMenuItem, IKBOptionPanelBuilder>();
    
    /** This is the plugin manager to be used by this dialog to load KB plugins */
    private KnowledgeBasePluginManager kbPluginManager = KnowledgeBasePluginManager.getInstance(true);
    
    /** Resource file from this package */
  	private ResourceBundle resource;
  	
  	/** Stores the last option that the user has chosen */
  	private JRadioButtonMenuItem lastSelectedOption;
  	
  	/** stores the last option that the user has chosen AND confirmed */
  	private JRadioButtonMenuItem lastConfirmedOption;

  	/** This map stores the default KB information (those not loaded from plugins) */
	private Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> defaultKbToOptionMap = null;
  	
	/**
	 * Constructor initializing fields
	 * @param owner
	 * @param controller
	 * @throws HeadlessException
	 */
	public OptionsDialog(Frame owner, MEBNController controller) throws HeadlessException {
		super(owner, true);
		
		// resource is not static, to enable hotplug
		this.resource = unbbayes.util.ResourceController.newInstance().getBundle(
	  			unbbayes.gui.mebn.resources.Resources.class.getName());
		
		setSize(550, 470);
		
		this.setTitle(resource.getString("mebnOptionTitle"));
		
		this.controller = controller;
		this.buildPanels();
	}
	
	/**
	 * Initialize the content of this dialog.
	 * This method is called inside the constructor.
	 * Several sub-methods are called by this method, such as {@link #buildKBOptions()}
	 */
	protected void buildPanels() {
		
		// instantiate main components
		
		this.tabPane = new JTabbedPane();	
		confirmationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // this is where "confirm" and "cancel" buttons resides
		
		// initialize confirmation panel
		
		confirm = new JButton(resource.getString("confirmLabel"));
		confirm.setToolTipText(resource.getString("confirmToolTip"));
        confirm.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // hide this dialog
                    setVisible(false);
                    
                    // update the upper window
                    if (getOwner() != null) {
                    	getOwner().repaint();
                    }
                }
            });
        
        confirmationPanel.add(confirm);
        
        cancel = new JButton(resource.getString("cancelLabel"));
		cancel.setToolTipText(resource.getString("cancelToolTip"));
        cancel.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // hide this dialog
                    setVisible(false);
                    
                    // update the upper window
                    if (getOwner() != null) {
                    	getOwner().repaint();
                    }
                }
            });
        confirmationPanel.add(cancel);
        
		this.getContentPane().setLayout(new BorderLayout(10,10));
		this.getContentPane().add(tabPane, BorderLayout.CENTER);
        this.getContentPane().add(confirmationPanel, BorderLayout.SOUTH);
        
        // build options for KB
		this.buildKBOptions();
	}

	/**
	 * Builds the option panels for KB.
	 * This method is called within {@link #buildPanels()}
	 */
	protected void buildKBOptions() {
		
        kbMainPanel            = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        kbRadioPanel       = new JPanel(new GridLayout(0,3));
        kbRadioPanel.setBorder(new TitledBorder(this.resource.getString("availableKB")));
        
        kbOptionPane = new JPanel(new BorderLayout());
        kbOptionPane.setBorder(new TitledBorder(this.resource.getString("kbParameters")));
        
        // set up plugins (kb) and fill map (this map associates radio button, its additional options and kb)
        this.reloadKBPlugins();
        
//        // adding radio buttons to the same radio button group (kbGroup) and same panel (kbRadioPanel)
//		for (JRadioButtonMenuItem radioItem : this.getKbOptionItems()) {
//			kbGroup.add(radioItem);
//			kbRadioPanel.add(radioItem);
//		}
		
		// adding radio buttons panel to the top of kb tab
		kbMainPanel.add(new JScrollPane(kbRadioPanel));
		

        // adding the option pane for the selected kb.
        IKBOptionPanelBuilder currentOptionPanelBuilder = this.getSelectedKBOptionPanel();
        if (currentOptionPanelBuilder != null) {
        	Component componentToAdd = currentOptionPanelBuilder.getPanel();
        	if (componentToAdd != null) {
        		JScrollPane scrollPane  = new JScrollPane(componentToAdd);
        		kbOptionPane.add(scrollPane, BorderLayout.CENTER);
        	}
        }
        
        kbMainPanel.add(kbOptionPane);
        
        // registers the "reload action" to the UnBBayesPluginContextHolder, so that when we press the
	    // "reload plugin" button, kb's plugins are reloaded as well
	    UnBBayesPluginContextHolder.newInstance().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
			public void onReload(EventObject eventObject) {
				reloadKBPlugins();
			}
	    });
	    
	    tabPane.addTab(resource.getString("kbTab"), kbMainPanel);
	    
	    // fill KB-specific action listeners
	    
	    confirm.addActionListener(
	            new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                    
	                    // commit changes (made at each option panel) on KB
	                	for (IKBOptionPanelBuilder builder : getKbToOptionMap().values()) {
	                		if (builder != null) {
	                			builder.commitChanges();
		                    }
						}
	                	
	                	// trace what is the last confirmed selection
	                    lastConfirmedOption = lastSelectedOption;
	                	
	                	// updating the inference kb referenced by controller
	                    IKBOptionPanelBuilder currentPanel = getSelectedKBOptionPanel();
	                    controller.setKnowledgeBase(currentPanel.getKB());
	                    
	                }
	            });
	    
	    cancel.addActionListener(
	            new ActionListener() {
	                public void actionPerformed(ActionEvent e) {

	                	// reverting changes on kb plugins
	                	for (IKBOptionPanelBuilder builder : getKbToOptionMap().values()) {
	                		if (builder != null) {
	                			builder.discardChanges();
		                    }
	                	}
	                	
	                	// select the last option
	                	if (lastConfirmedOption != null) {
	                		lastConfirmedOption.doClick();
	                	}
	                }
	            });
	}
	
	/**
     * Obtains the currently selected (by j option radio button) panel for algorithm options
     * @return
     */
    private IKBOptionPanelBuilder getSelectedKBOptionPanel() {
    	if (this.getKbToOptionMap() == null) {
    		return null;
    	}
		for (JRadioButtonMenuItem option : this.getKbToOptionMap().keySet()) {
			if (option.isSelected()) {
				return this.getKbToOptionMap().get(option);
			}
		}
		return null;
	}

	/**
	 * Obtains a map of button item and its panel builder, using {@link #getKbPluginManager()}.
	 * It fills each IKBOptionPanelBuilder with the obtained instance of {@link KnowledgeBase}.
	 * The generated button item is not added to any container yet.
	 */
	protected Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> loadKBAsPlugins() {

		Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> ret = new HashMap<JRadioButtonMenuItem, IKBOptionPanelBuilder>();
		
		// refresh plugin
		this.getKbPluginManager().reloadPlugins();
		
		// obtains the KB and its panel builder
		Map<IKnowledgeBaseBuilder, IKBOptionPanelBuilder> kbMap = this.getKbPluginManager().getKbToOptionPanelMap();
		for (IKnowledgeBaseBuilder kbBuilder : kbMap.keySet()) {
			try {
				
				// retrieves the panel builder
				IKBOptionPanelBuilder panelBuilder = kbMap.get(kbBuilder);
				if (panelBuilder == null) {
					// if null, use a default implementaion of panel builder
					panelBuilder = new EmptyOptionPanelBuilder(null);
				}
				
				// tells the panel the correct kb to edit
				panelBuilder.setKB(kbBuilder.buildKB());
				
				// create new button item using KB's name as its label
				JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(kbBuilder.getName());
				radioButton.addActionListener(new PluginRadioButtonListener(panelBuilder));
				
				// fill return
				ret.put(radioButton, panelBuilder);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 * Obtains a collection of all default KB (knowledge bases that are not
	 * loaded from plugins), using the same format expected from {@link #loadKBAsPlugins()}.
	 * This method is used in {@link #reloadKBPlugins()} to initialize components.
	 * @return : default KB in {@link #loadKBAsPlugins()} format.
	 * 
	 */
	protected Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> buildDefaultKB() {
		if (defaultKbToOptionMap == null) {
			defaultKbToOptionMap = new HashMap<JRadioButtonMenuItem, IKBOptionPanelBuilder>();
			// fill default kb as PowerLoomKB
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(this.resource.getString("defaultKB"));
			EmptyOptionPanelBuilder panelBuilder = new EmptyOptionPanelBuilder(this.controller.getKnowledgeBase());
			radioButton.addActionListener(new PluginRadioButtonListener(panelBuilder));
			this.lastConfirmedOption = radioButton;
			defaultKbToOptionMap.put(radioButton, panelBuilder);
		}
		
		return defaultKbToOptionMap;
	}
	
	/**
	 * Reloads plugin KBs.
	 * Uses {@link #getKbPluginManager()} in order to fill 
	 * the content of {@link #getKbToOptionMap()} and content of
	 * the radio button group.
	 * {@link #kbRadioPanel} must be a non-null value
     */
    protected void reloadKBPlugins() {
    	
    	// reset map
    	this.getKbToOptionMap().clear();
    	
    	// fill default kb
    	this.getKbToOptionMap().putAll(this.buildDefaultKB());
    	
    	// load new algorithms as new map
		this.getKbToOptionMap().putAll(loadKBAsPlugins());

    	// reset components
    	this.setKbGroup(new ButtonGroup());
    	this.getKbRadioPanel().removeAll();
    	
    	// adding the plugins if they were not already added
		for (JRadioButtonMenuItem radioItem : this.getKbToOptionMap().keySet()) {
			// if this plugin was not loaded before, add it
			this.getKbGroup().add(radioItem);
			this.getKbRadioPanel().add(radioItem);
			
			// use class equivalency to update the currently selected option
			if (this.getController().getKnowledgeBase().getClass().equals(this.getKbToOptionMap().get(radioItem).getKB().getClass())) {
				radioItem.doClick();
				this.lastConfirmedOption = radioItem;
				this.lastSelectedOption = radioItem;
			}
		}
		
    }
    
    /**
     * Changes the {@link #getAlgorithmOptionPane()}'s content to
     * currentOptionPanel.
     * @param currentOptionPanel
     */
    protected void setCurrentAlgorithmOptionPanel(Component currentOptionPanel) {
    	// clear algorithm scroll pane and refills it with the current option panel
    	for (Component comp : this.getKbOptionPane().getComponents()) {
			comp.setVisible(false);
		}
    	this.getKbOptionPane().removeAll();
    	if (currentOptionPanel != null) {
    		JScrollPane scrollPane  = new JScrollPane(currentOptionPanel, 
    				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
    				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    		this.getKbOptionPane().add(scrollPane, BorderLayout.CENTER);
//    		currentOptionPanel.setVisible(true);
    		scrollPane.setVisible(true);
    	}
    	this.repaint();
    }

	/**
	 * @return the kbPluginManager
	 */
	public KnowledgeBasePluginManager getKbPluginManager() {
		return kbPluginManager;
	}

	/**
	 * @param kbPluginManager the kbPluginManager to set
	 */
	public void setKbPluginManager(KnowledgeBasePluginManager kbPluginManager) {
		this.kbPluginManager = kbPluginManager;
	}

	/**
	 * @return the controller
	 */
	public MEBNController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(MEBNController controller) {
		this.controller = controller;
	}

	/**
	 * @return the tabPane
	 */
	public JTabbedPane getTabPane() {
		return tabPane;
	}

	/**
	 * @param tabPane the tabPane to set
	 */
	public void setTabPane(JTabbedPane tabPane) {
		this.tabPane = tabPane;
	}

	/**
	 * @return the confirmationPanel
	 */
	public JPanel getConfirmationPanel() {
		return confirmationPanel;
	}

	/**
	 * @param confirmationPanel the confirmationPanel to set
	 */
	public void setConfirmationPanel(JPanel confirmationPanel) {
		this.confirmationPanel = confirmationPanel;
	}

	/**
	 * @return the cancel
	 */
	public JButton getCancel() {
		return cancel;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(JButton cancel) {
		this.cancel = cancel;
	}

	/**
	 * @return the confirm
	 */
	public JButton getConfirm() {
		return confirm;
	}

	/**
	 * @param confirm the confirm to set
	 */
	public void setConfirm(JButton confirm) {
		this.confirm = confirm;
	}

	/**
	 * @return the kbMainPanel
	 */
	public JComponent getKbMainPanel() {
		return kbMainPanel;
	}

	/**
	 * @param kbMainPanel the kbMainPanel to set
	 */
	public void setKbMainPanel(JComponent kbMainPanel) {
		this.kbMainPanel = kbMainPanel;
	}

	/**
	 * @return the kbRadioPanel
	 */
	public JPanel getKbRadioPanel() {
		return kbRadioPanel;
	}

	/**
	 * @param kbRadioPanel the kbRadioPanel to set
	 */
	public void setKbRadioPanel(JPanel kbRadioPanel) {
		this.kbRadioPanel = kbRadioPanel;
	}

	/**
	 * @return the kbGroup
	 */
	public ButtonGroup getKbGroup() {
		return kbGroup;
	}

	/**
	 * @param kbGroup the kbGroup to set
	 */
	public void setKbGroup(ButtonGroup kbGroup) {
		this.kbGroup = kbGroup;
	}

	/**
	 * @return the kbOptionPane
	 */
	public JComponent getKbOptionPane() {
		return kbOptionPane;
	}

	/**
	 * @param kbOptionPane the kbOptionPane to set
	 */
	public void setKbOptionPane(JPanel kbOptionPane) {
		this.kbOptionPane = kbOptionPane;
	}

	/**
	 * @return the kbToOptionMap
	 */
	public Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> getKbToOptionMap() {
		if (this.kbToOptionMap == null) {
			this.kbToOptionMap = new HashMap<JRadioButtonMenuItem, IKBOptionPanelBuilder>();
		}
		return kbToOptionMap;
	}

	/**
	 * @param kbToOptionMap the kbToOptionMap to set
	 */
	public void setKbToOptionMap(
			Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> kbToOptionMap) {
		this.kbToOptionMap = kbToOptionMap;
	}

	/**
	 * This is just a simplest implementation of IKBOptionPanelBuilder
	 * for plugins that does not have a option panel builder
	 * @author Shou Matsumoto
	 *
	 */
	protected class EmptyOptionPanelBuilder extends JPanel implements IKBOptionPanelBuilder {
		private KnowledgeBase kb;
		public EmptyOptionPanelBuilder(KnowledgeBase kb) {this.setKB(kb);}
		public void commitChanges() {}
		public void discardChanges() {}
		public KnowledgeBase getKB() {return this.kb;}
		public JPanel getPanel() {return null;}
		public void setKB(KnowledgeBase kb) {this.kb = kb;}
	}
	
	/**
	 * A component aware listener for Plugin's radio buttons.
	 * It simply updates OptionsDialog depending on what
	 * "kb" option is called.
	 * @author Shou Matsumoto
	 *
	 */
	protected class PluginRadioButtonListener implements ActionListener {
		IKBOptionPanelBuilder builder;
		public PluginRadioButtonListener(IKBOptionPanelBuilder builder) {
			super();
			this.builder = builder;
		}
		public void actionPerformed(ActionEvent e) {
			setCurrentAlgorithmOptionPanel(this.builder.getPanel());
			lastSelectedOption = (JRadioButtonMenuItem)e.getSource();
		}
	}
}
