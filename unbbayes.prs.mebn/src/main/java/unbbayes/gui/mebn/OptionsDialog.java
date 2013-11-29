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
import unbbayes.gui.mebn.extension.IPanelBuilder;
import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder;
import unbbayes.gui.mebn.extension.ssbn.LaskeyAlgorithmOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;
import unbbayes.prs.mebn.kb.extension.jpf.KnowledgeBasePluginManager;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.prs.mebn.ssbn.extension.jpf.SSBNGenerationAlgorithmPluginManager;
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
  	
  	/** Stores the last option that the user has chosen, for kb */
  	private JRadioButtonMenuItem lastSelectedKBOption;
  	
  	/** stores the last option that the user has chosen AND confirmed, for kb */
  	private JRadioButtonMenuItem lastConfirmedKBOption;

  	/** This map stores the default KB information (those not loaded from plugins) */
	private Map<JRadioButtonMenuItem, IKBOptionPanelBuilder> defaultKbToOptionMap = null;
	
	
	// components for SSBN algorithm configuration
	
	
	/** This is where all KB-related panels resides */
	private JComponent ssbnMainPanel;
	
	/** This is where kb's radio button resides */
    private JPanel ssbnRadioPanel;
    
    /** This group manages all loaded KB */
    private ButtonGroup ssbnGroup;
    
    /** This is where the currently selected KB's options resides */
	private JPanel ssbnOptionPane;
	
	/** This map relates a radio button to what option panel it represents */
    private Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> ssbnToOptionMap = new HashMap<JRadioButtonMenuItem, ISSBNOptionPanelBuilder>();
    
    /** This is the plugin manager to be used by this dialog to load KB plugins */
    private SSBNGenerationAlgorithmPluginManager ssbnPluginManager = SSBNGenerationAlgorithmPluginManager.getInstance(true);


  	/** Stores the last option that the user has chosen, for ssbn algorithm */
  	private JRadioButtonMenuItem lastSelectedSSBNOption;
  	
  	/** stores the last option that the user has chosen AND confirmed, for ssbn algorithm */
	private JRadioButtonMenuItem lastConfirmedSSBNOption;
	
	/** This map stores the default KB information (those not loaded from plugins) */
	private Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> defaultSSBNToOptionMap = null;
	
	/** This is the default position of split pane's divider (between the panel w/ radio buttons and the configuration panels) */
	public static final int DEFAULT_DIVIDER_LOCATION = 60;
	

    /** Resource file from this package */
  	private ResourceBundle resource;
  	
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
		this.buildSSBNGenerationOptions();
	}

	/**
	 * Builds the option panels for SSBN generation algorithm.
	 * This method is called within {@link #buildPanels()}
	 */
	protected void buildSSBNGenerationOptions() {
		ssbnMainPanel            = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		((JSplitPane)ssbnMainPanel).setDividerLocation(DEFAULT_DIVIDER_LOCATION);
        
        ssbnRadioPanel       = new JPanel(new GridLayout(0,3));
        ssbnRadioPanel.setBorder(new TitledBorder(this.resource.getString("availableSSBN")));
        
        ssbnOptionPane = new JPanel(new BorderLayout());
        ssbnOptionPane.setBorder(new TitledBorder(this.resource.getString("ssbnParameters")));
        
        // set up plugins (ssbn algorithm) and fill map (this map associates radio button, its additional options and ssbn)
        this.reloadSSBNGeneratorPlugins();
        
		// adding radio buttons panel to the top of kb tab
		ssbnMainPanel.add(new JScrollPane(ssbnRadioPanel));
		

        // adding the option pane for the selected ssbn.
        ISSBNOptionPanelBuilder currentOptionPanelBuilder = this.getSelectedSSBNOptionPanel();
        if (currentOptionPanelBuilder != null) {
        	Component componentToAdd = currentOptionPanelBuilder.getPanel();
        	if (componentToAdd != null) {
        		JScrollPane scrollPane  = new JScrollPane(componentToAdd);
        		ssbnOptionPane.add(scrollPane, BorderLayout.CENTER);
        	}
        }
        
        ssbnMainPanel.add(ssbnOptionPane);
        
        // registers the "reload action" to the UnBBayesPluginContextHolder, so that when we press the
	    // "reload plugin" button, ssbn algorithms' plugins are reloaded as well
	    UnBBayesPluginContextHolder.newInstance().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
			public void onReload(EventObject eventObject) {
				reloadSSBNGeneratorPlugins();
			}
	    });
	    
	    tabPane.addTab(resource.getString("ssbnTab"), ssbnMainPanel);
	    
	    // fill algorithm-specific action listeners
	    
	    confirm.addActionListener(
	            new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                    
	                    // commit changes (made at each option panel) on KB
	                	for (ISSBNOptionPanelBuilder builder : getSSBNToOptionMap().values()) {
	                		if (builder != null) {
	                			builder.commitChanges();
		                    }
						}
	                	
	                	// trace what is the last confirmed selection
	                    lastConfirmedSSBNOption = lastSelectedSSBNOption;
	                	
	                	// updating the inference kb referenced by controller
	                    ISSBNOptionPanelBuilder currentPanel = getSelectedSSBNOptionPanel();
	                    controller.setSSBNGenerator(currentPanel.getSSBNGenerator());
	                    
	                }
	            });
	    
	    cancel.addActionListener(
	            new ActionListener() {
	                public void actionPerformed(ActionEvent e) {

	                	// reverting changes on kb plugins
	                	for (ISSBNOptionPanelBuilder builder : getSSBNToOptionMap().values()) {
	                		if (builder != null) {
	                			builder.discardChanges();
		                    }
	                	}
	                	
	                	// select the last option
	                	if (lastConfirmedSSBNOption != null) {
	                		lastConfirmedSSBNOption.doClick();
	                	}
	                }
	            });		
	}

	/**
	 * Builds the option panels for KB.
	 * This method is called within {@link #buildPanels()}
	 */
	protected void buildKBOptions() {
		
        kbMainPanel            = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        ((JSplitPane)kbMainPanel).setDividerLocation(DEFAULT_DIVIDER_LOCATION);
        
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
	                    
	                	IKBOptionPanelBuilder selectedKBOptionPanel = getSelectedKBOptionPanel();
	                	// commit changes, only on currently selected KB
//	                	selectedKBOptionPanel.commitChanges();
	                	// the following code commits all panels instead.
	                	for (IKBOptionPanelBuilder builder : getKbToOptionMap().values()) {
	                		if (builder != null) {
	                			builder.commitChanges();
		                    }
						}
	                	
	                	// trace what is the last confirmed selection
	                    lastConfirmedKBOption = lastSelectedKBOption;
	                	
	                	// updating the inference kb referenced by controller
	                    controller.setKnowledgeBase(selectedKBOptionPanel.getKB());
	                    
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
	                	if (lastConfirmedKBOption != null) {
	                		lastConfirmedKBOption.doClick();
	                	}
	                }
	            });
	}
	
	/**
     * Obtains the currently selected (by j option radio button) panel for KB options
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
     * Obtains the currently selected (by j option radio button) panel for SSBN generation algorithm's options
     * @return
     */
    private ISSBNOptionPanelBuilder getSelectedSSBNOptionPanel() {
    	if (this.getSSBNToOptionMap() == null) {
    		return null;
    	}
		for (JRadioButtonMenuItem option : this.getSSBNToOptionMap().keySet()) {
			if (option.isSelected()) {
				return this.getSSBNToOptionMap().get(option);
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
				
				boolean isOK = false;
				try {
					// tells the panel the correct kb to edit
					panelBuilder.setKB(kbBuilder.buildKB(this.getController().getMultiEntityBayesianNetwork(), this.getController()));
					isOK = true;
				} catch (Throwable e) {
					e.printStackTrace();
					System.err.println(kbBuilder.getClass() + ": this plug-in uses old knowledge base class definition. Please, contact the plug-in developer in order to update class definition.");
//					JOptionPane.showMessageDialog(
//							OptionsDialog.this, 
//							resource.getString("moduleLoadingError"), 
//							e.getMessage(), 
//							JOptionPane.WARNING_MESSAGE); 
					panelBuilder.setKB(kbBuilder.buildKB()); // build using old deprecated method (it is OK, although it is not the best way)
					isOK = true;	
				}
				
				if (isOK) {
					// create new button item using KB's name as its label
					JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(kbBuilder.getName());
					radioButton.addActionListener(new KBPluginRadioButtonListener(panelBuilder));
					
					// fill return
					ret.put(radioButton, panelBuilder);
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Obtains a map of button item and its panel builder, using {@link #getSSBNPluginManager()}.
	 * It fills each ISSBNOptionPanelBuilder with the obtained instance of {@link ISSBNGenerator}
	 * The generated button item is not added to any container yet.
	 */
	protected Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> loadSSBNGeneratorAsPlugins() {

		Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> ret = new HashMap<JRadioButtonMenuItem, ISSBNOptionPanelBuilder>();
		
		// refresh plugin
		this.getSSBNPluginManager().reloadPlugins();
		
		// obtains the SSBN generators and its panel builder
		Map<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder> ssbnMap = this.getSSBNPluginManager().getSSBNToOptionPanelMap();
		for (ISSBNGeneratorBuilder ssbnBuilder : ssbnMap.keySet()) {
			try {
				// retrieves the panel builder
				ISSBNOptionPanelBuilder panelBuilder = ssbnMap.get(ssbnBuilder);
				if (panelBuilder == null) {
					// if null, use a default implementaion of panel builder
					panelBuilder = new EmptyOptionPanelBuilder(null);
				}
				
				// tells the panel the correct SSBN to edit
				panelBuilder.setSSBNGenerator(ssbnBuilder.buildSSBNGenerator());
				
				// create new button item using SSBN's name as its label
				JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(ssbnBuilder.getName());
				radioButton.addActionListener(new SSBNPluginRadioButtonListener(panelBuilder));
				
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
			EmptyOptionPanelBuilder panelBuilder;
			if (getController().getKnowledgeBase() != null 
					&& getController().getKnowledgeBase().getClass().equals(PowerLoomKB.class)) {
				// reuse the same object if the class being used by controller is the same
				panelBuilder = new EmptyOptionPanelBuilder(getController().getKnowledgeBase());
			} else {
				// or else, generate a new power loom kb
				panelBuilder = new EmptyOptionPanelBuilder(PowerLoomKB.getNewInstanceKB());
			}
			radioButton.addActionListener(new KBPluginRadioButtonListener(panelBuilder));
			this.lastConfirmedKBOption = radioButton;
			defaultKbToOptionMap.put(radioButton, panelBuilder);
		}
		
		return defaultKbToOptionMap;
	}
	
	/**
	 * Obtains a collection of all default SSBN generation algorithms (ISSBNGenerator that are not
	 * loaded from plugins), using the same format expected from {@link #loadSSBNGeneratorAsPlugins()}.
	 * This method is used in {@link #reloadSSBNGeneratorPlugins()} to initialize components.
	 * @return : default KB in {@link #loadSSBNGeneratorAsPlugins()} format.
	 * 
	 */
	protected Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> buildDefaultSSBNGenerator() {
		if (defaultSSBNToOptionMap == null) {
			// initialize map
			defaultSSBNToOptionMap = new HashMap<JRadioButtonMenuItem, ISSBNOptionPanelBuilder>();
			
			// fill default SSBN algorithm as the laskey algorithm
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(this.resource.getString("defaultSSBN"));
			
			// add laskey algorithm's option panel
			ISSBNOptionPanelBuilder panelBuilder = new LaskeyAlgorithmOptionPanelBuilder();
			
			// fill action listener for radio button and mark it as the selected one
			radioButton.addActionListener(new SSBNPluginRadioButtonListener(panelBuilder));
			this.lastConfirmedSSBNOption = radioButton;
			
			// fill map
			defaultSSBNToOptionMap.put(radioButton, panelBuilder);
		}
		
		return defaultSSBNToOptionMap;
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
    	
    	// load new kb as new map
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
				this.lastConfirmedKBOption = radioItem;
				this.lastSelectedKBOption = radioItem;
			}
		}
		
    }
    
    /**
	 * Reloads plugin SSBN generation algorithms.
	 * Uses {@link #getSSBNPluginManager()} in order to fill 
	 * the content of {@link #getSSBNToOptionMap()} and content of
	 * the radio button group.
	 * {@link #ssbnRadioPanel} must be a non-null value
     */
    protected void reloadSSBNGeneratorPlugins() {
    	
    	// reset map
    	this.getSSBNToOptionMap().clear();
    	
    	// fill default ssbn generator
    	this.getSSBNToOptionMap().putAll(this.buildDefaultSSBNGenerator());
    	
    	// load new algorithms as new map
		this.getSSBNToOptionMap().putAll(loadSSBNGeneratorAsPlugins());

    	// reset components
    	this.setSSBNGroup(new ButtonGroup());
    	this.getSSBNRadioPanel().removeAll();
    	
    	// adding the plugins if they were not already added
		for (JRadioButtonMenuItem radioItem : this.getSSBNToOptionMap().keySet()) {
			// if this plugin was not loaded before, add it
			this.getSSBNGroup().add(radioItem);
			this.getSSBNRadioPanel().add(radioItem);
			
			// use class equivalency to update the currently selected option
			if (this.getController().getSSBNGenerator().getClass().equals(this.getSSBNToOptionMap().get(radioItem).getSSBNGenerator().getClass())) {
				radioItem.doClick();
				this.lastConfirmedSSBNOption = radioItem;
				this.lastSelectedSSBNOption = radioItem;
			}
		}
		
    }
    
    /**
     * Changes the {@link #getKbOptionPane()}'s content to
     * currentOptionPanel.
     * @param currentOptionPanel
     */
    protected void setCurrentKBOptionPanel(Component currentOptionPanel) {
    	// clear kb scroll pane and refills it with the current option panel
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
     * Changes the {@link #getSSBNOptionPane()}'s content to
     * currentOptionPanel.
     * @param currentOptionPanel
     */
    protected void setCurrentSSBNOptionPanel(Component currentOptionPanel) {
    	// clear ssbn algorithm scroll pane and refills it with the current option panel
    	for (Component comp : this.getSSBNOptionPane().getComponents()) {
			comp.setVisible(false);
		}
    	this.getSSBNOptionPane().removeAll();
    	if (currentOptionPanel != null) {
    		JScrollPane scrollPane  = new JScrollPane(currentOptionPanel, 
    				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
    				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    		this.getSSBNOptionPane().add(scrollPane, BorderLayout.CENTER);
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
	 * This is just a simplest implementation of IPanelBuilder
	 * for plugins that does not have a option panel builder.
	 * It carries an object and a panel.
	 * The carried object is a placeholder for {@link #getKB()} (from {@link IKBOptionPanelBuilder})
	 * or for {@link #getSSBNGenerator()} (from {@link ISSBNOptionPanelBuilder}).
	 * Be very careful no to use this class as {@link IKBOptionPanelBuilder} and {@link ISSBNOptionPanelBuilder}
	 * simultaneously, because it can only carry 1 object at a time.
	 * @author Shou Matsumoto
	 *
	 */
	protected class EmptyOptionPanelBuilder extends JPanel implements IKBOptionPanelBuilder, ISSBNOptionPanelBuilder {
		private Object carriedObject;
		public EmptyOptionPanelBuilder(Object carriedObject) {this.carriedObject = carriedObject;}
		public void commitChanges() {}
		public void discardChanges() {}
		public JPanel getPanel() {return null;}
		public KnowledgeBase getKB() {return (KnowledgeBase) carriedObject;}
		public void setKB(KnowledgeBase kb) {this.carriedObject = kb;}
		public ISSBNGenerator getSSBNGenerator() {return (ISSBNGenerator)this.carriedObject;}
		public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {this.carriedObject = ssbnGenerator;}
	}
	
	/**
	 * A component aware listener for Plugin's radio buttons.
	 * It simply updates OptionsDialog depending on what
	 * "kb" option is called.
	 * @author Shou Matsumoto
	 *
	 */
	protected class KBPluginRadioButtonListener implements ActionListener {
		IPanelBuilder builder;
		public KBPluginRadioButtonListener(IPanelBuilder builder) {
			super();
			this.builder = builder;
		}
		public void actionPerformed(ActionEvent e) {
			setCurrentKBOptionPanel(this.builder.getPanel());
			lastSelectedKBOption = (JRadioButtonMenuItem)e.getSource();
		}
	}
	
	/**
	 * A component aware listener for Plugin's radio buttons.
	 * It simply updates OptionsDialog depending on what
	 * "ssbn generation algorithm" option is called.
	 * @author Shou Matsumoto
	 *
	 */
	protected class SSBNPluginRadioButtonListener implements ActionListener {
		IPanelBuilder builder;
		public SSBNPluginRadioButtonListener(IPanelBuilder builder) {
			super();
			this.builder = builder;
		}
		public void actionPerformed(ActionEvent e) {
			setCurrentSSBNOptionPanel(this.builder.getPanel());
			lastSelectedSSBNOption = (JRadioButtonMenuItem)e.getSource();
		}
	}

	/**
	 * @return the ssbnMainPanel
	 */
	public JComponent getSSBNMainPanel() {
		return ssbnMainPanel;
	}

	/**
	 * @param ssbnMainPanel the ssbnMainPanel to set
	 */
	public void setSSBNMainPanel(JComponent ssbnMainPanel) {
		this.ssbnMainPanel = ssbnMainPanel;
	}

	/**
	 * @return the ssbnRadioPanel
	 */
	public JPanel getSSBNRadioPanel() {
		return ssbnRadioPanel;
	}

	/**
	 * @param ssbnRadioPanel the ssbnRadioPanel to set
	 */
	public void setSSBNRadioPanel(JPanel ssbnRadioPanel) {
		this.ssbnRadioPanel = ssbnRadioPanel;
	}

	/**
	 * @return the ssbnGroup
	 */
	public ButtonGroup getSSBNGroup() {
		return ssbnGroup;
	}

	/**
	 * @param ssbnGroup the ssbnGroup to set
	 */
	public void setSSBNGroup(ButtonGroup ssbnGroup) {
		this.ssbnGroup = ssbnGroup;
	}

	/**
	 * @return the ssbnOptionPane
	 */
	public JPanel getSSBNOptionPane() {
		return ssbnOptionPane;
	}

	/**
	 * @param ssbnOptionPane the ssbnOptionPane to set
	 */
	public void setSSBNOptionPane(JPanel ssbnOptionPane) {
		this.ssbnOptionPane = ssbnOptionPane;
	}

	/**
	 * @return the ssbnToOptionMap
	 */
	public Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> getSSBNToOptionMap() {
		return ssbnToOptionMap;
	}

	/**
	 * @param ssbnToOptionMap the ssbnToOptionMap to set
	 */
	public void setSSBNToOptionMap(
			Map<JRadioButtonMenuItem, ISSBNOptionPanelBuilder> ssbnToOptionMap) {
		this.ssbnToOptionMap = ssbnToOptionMap;
	}

	/**
	 * @return the ssbnPluginManager
	 */
	public SSBNGenerationAlgorithmPluginManager getSSBNPluginManager() {
		return ssbnPluginManager;
	}

	/**
	 * @param ssbnPluginManager the ssbnPluginManager to set
	 */
	public void setSSBNPluginManager(
			SSBNGenerationAlgorithmPluginManager ssbnPluginManager) {
		this.ssbnPluginManager = ssbnPluginManager;
	}
}
