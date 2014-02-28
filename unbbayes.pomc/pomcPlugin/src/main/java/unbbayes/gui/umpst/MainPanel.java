package unbbayes.gui.umpst;

/*
 * TabbedPaneDemo.java requires one additional loadedFile:
 *   images/middle.gif.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.entity.EntitiesMainPanel;
import unbbayes.gui.umpst.goal.GoalsEditionPanel;
import unbbayes.gui.umpst.goal.GoalsMainPanel;
import unbbayes.gui.umpst.goal.GoalsSearchPanel;
import unbbayes.gui.umpst.group.GroupsMainPanel;
import unbbayes.gui.umpst.rules.RulesMainPanel;
import unbbayes.io.umpst.FileLoad;
import unbbayes.io.umpst.FileSave;
import unbbayes.model.umpst.project.UMPSTProject;

public class MainPanel extends IUMPSTPanel{
	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
		//private UMPSTProject umpstProject;
	
	    private static final String  FILE_EXTENSION = "ump";
	
		private GoalsMainPanel goalsPane;
		private EntitiesMainPanel entitiesPane;
		private RulesMainPanel rulesPane;
		private GroupsMainPanel groupsPane;
		
		private File loadedFile;
		private File newFile;

		private String fileExtension;
		
	  	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
	  			unbbayes.gui.umpst.resources.Resources.class.getName());
	  	

	    public MainPanel(UmpstModule janelaPai,UMPSTProject umpstProject) {	    	
	        super(new GridLayout(1, 1),janelaPai);
	        
	        IconController iconController = IconController.getInstance(); 
	        
	    	this.setUmpstProject(umpstProject);
	        
	        JTabbedPane tabbedPane = new JTabbedPane();
	        
	        goalsPane = new GoalsMainPanel(getFatherPanel(),umpstProject);
	        goalsPane.setPreferredSize(new Dimension(1000, 500));
	        
	        tabbedPane.addTab(resource.getString("ttGoals"), 
	        		iconController.getRequirementsIcon(), 
	        		new JScrollPane(goalsPane),
	                resource.getString("hpGoalsTab"));
	        
	        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	        
	        entitiesPane = new EntitiesMainPanel(getFatherPanel(),getUmpstProject());
	        entitiesPane.setPreferredSize(new Dimension(1000,500));
	        
	        tabbedPane.addTab(resource.getString("ttEntities"), 
	        		iconController.getAnalysisDesignIcon(), 
	        		entitiesPane,
	                resource.getString("hpEntitiesTab"));
	        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

	        rulesPane = new RulesMainPanel(getFatherPanel(),umpstProject);
	        rulesPane.setPreferredSize(new Dimension(1000,500));	        
	        tabbedPane.addTab(resource.getString("ttRules"), 
	        		iconController.getAnalysisDesignIcon(), 
	        		rulesPane,
	                resource.getString("hpRulesTab"));
	        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
	        
	        groupsPane = new GroupsMainPanel(getFatherPanel(),umpstProject);
	        groupsPane.setPreferredSize(new Dimension(1000,500));
	        tabbedPane.addTab(resource.getString("ttGroups"), 
	        		iconController.getAnalysisDesignIcon(), 
	        		groupsPane,
	                resource.getString("hpGroupsTab"));
	        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
	        
	        
	        //------------------------ File ------------------------------------
	        
	        JMenu fileMenu = new JMenu(resource.getString("mnFile"));
	        fileMenu.setMnemonic(resource.getString("mnFileMnemonic").charAt(0));
	        
	        JMenuItem loadItem = new JMenuItem(resource.getString("mnOpen"));
	        loadItem.setMnemonic(resource.getString("mnOpenMnemonic").charAt(0));
	        
	        fileMenu.add(loadItem);
	        
	        loadItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
									
					JFileChooser fc = new JFileChooser();
					 // restringe a amostra a diretorios apenas
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				    fc.setCurrentDirectory (new File ("."));

                    int res = fc.showOpenDialog(null);
                    
                    if(res == JFileChooser.APPROVE_OPTION){
                        loadedFile = fc.getSelectedFile();
                    }
                 
                    int index = loadedFile.getName().lastIndexOf(".");
                    
        			if (index >= 0) {
        				fileExtension = loadedFile.getName().substring(index + 1);
        			}
					
        			if (fileExtension.equals(FILE_EXTENSION)){
        				FileLoad io = new FileLoad();
						setUmpstProject(io.loadUbf(loadedFile,getUmpstProject())) ;
						GoalsSearchPanel goalPanel = new GoalsSearchPanel(getFatherPanel(),getUmpstProject());
						goalPanel.returnTableGoals();
                    }
        			else{
        				JOptionPane.showMessageDialog(null, "This file format is not suported. Try .ump");
        			}
				}
			});
	        
	        JMenuItem saveItem = new JMenuItem("Save");
	        saveItem.setMnemonic('s');
	        fileMenu.add(saveItem);
	        
	        saveItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					FileSave io = new FileSave();
					JFileChooser fc =  new JFileChooser();  
				    fc.setCurrentDirectory (new File ("."));
				   // fc.setSelectedFile (newFile);
				    
				    
				    
				    int res = fc.showSaveDialog(null);
				    

				    if(res == JFileChooser.APPROVE_OPTION){
                        newFile = fc.getSelectedFile();
                    }
				    
				    
				    
					if (newFile!=null)	{
						try {
							io.saveUbf(newFile,getUmpstProject());
							JOptionPane.showMessageDialog(null, "File saved");
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "Error while creating saving file");
					}
					
				}
			});
	        
	        //------------------------ Help ------------------------------------
	        JMenu helpMenu = new JMenu(resource.getString("mnHelp"));
	        helpMenu.setMnemonic(resource.getString("mnHelpMnemonic").charAt(0));
	        
	        JMenuItem helpContentsItem = new JMenuItem(resource.getString("mnHelpContents"));
	        helpContentsItem.setMnemonic(resource.getString("mnHelpContentsMnemonic").charAt(0));
	        
	        JMenuItem aboutItem = new JMenuItem(resource.getString("mnAbout"));
	        aboutItem.setMnemonic(resource.getString("mnAboutMnemonic").charAt(0));
	        
	        helpMenu.add(helpContentsItem);
//	        helpMenu.add(aboutItem);
	        
	        helpContentsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						HelpSet set =  new HelpSet(null, getClass().getResource("/help/UMPHelp/ump.hs"));
//						set.setHomeID("UMP_Example");

//						HelpBroker hb = set.createHelpBroker();
//						DisplayHelpFromSource display = new CSH.DisplayHelpFromSource( hb );

						JHelp help = new JHelp(set);
						JFrame f = new JFrame();
						f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						f.setContentPane(help);
						f.pack();
						f.setLocationRelativeTo(getFatherPanel()); 
//						f.setTitle(resource.getString("helperDialogTitle"));
						f.setVisible(true);
					} catch (Exception evt) {
						evt.printStackTrace();
					}
				}
			});
	        
	        aboutItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
									
				}
			});
	        
	        
	        
	        
	        JMenuBar bar = new JMenuBar();
	        bar.add(fileMenu);
	        bar.add(helpMenu); 
	        
	        JPanel panel = new JPanel();
	        panel.setLayout(new BorderLayout());
	        
	        panel.add(bar,BorderLayout.PAGE_START);
	        panel.add(tabbedPane,BorderLayout.CENTER);
	        
	        //Add the tabbed pane to this panel.
	        add(panel);
	        
	        //The following line enables to use scrolling tabs.
	        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	    }
	    
	    
		 
		 public String getFileExtension() {
				return this.FILE_EXTENSION;
			}
	    
	    /**
		 * @return the groupsPane
		 */
		public GroupsMainPanel getGroupsPane() {
			return groupsPane;
		}

		/**
		 * @param groupsPane the groupsPane to set
		 */
		public void setGroupsPane(GroupsMainPanel groupsPane) {
			this.groupsPane = groupsPane;
		}

		/**
		 * @return the rulesPane
		 */
		public RulesMainPanel getRulesPane() {
			return rulesPane;
		}


		/**
		 * @param rulesPane the rulesPane to set
		 */
		public void setRulesPane(RulesMainPanel rulesPane) {
			this.rulesPane = rulesPane;
		}

		/**
		 * @return the requirementsPane
		 */
		public GoalsMainPanel getRequirementsPane() {
			return goalsPane;
		}

		/**
		 * @param requirementsPane the requirementsPane to set
		 */
		public void setRequirementsPane(GoalsMainPanel requirementsPane) {
			this.goalsPane = requirementsPane;
		}

		/**
		 * @return the entitiesPane
		 */
		public EntitiesMainPanel getEntitiesPane() {
			return entitiesPane;
		}

		/**
		 * @param entitiesPane the entitiesPane to set
		 */
		public void setEntitiesPane(EntitiesMainPanel entitiesPane) {
			this.entitiesPane = entitiesPane;
		}

		protected JPanel  createInternalPane (JPanel pane){
	    	
	    	this.setLayout(new FlowLayout());
			this.add(new GoalsEditionPanel(getFatherPanel(),getUmpstProject(),null,null));
	    	
			return pane;
	    }
	    
	    protected JComponent makeTextPanel(String text) {
	        JPanel panel = new JPanel(false);
	        JLabel filler = new JLabel(text);
	        filler.setHorizontalAlignment(JLabel.LEFT);
	        panel.setLayout(new GridLayout(1, 1));
	        panel.add(filler);
	        return panel;
	    }
	    
	    /** Returns an ImageIcon, or null if the path was invalid. */
	    protected static ImageIcon createImageIcon(String path) {
	        java.net.URL imgURL = MainPanel.class.getResource(path);
	        if (imgURL != null) {
	            return new ImageIcon(imgURL);
	        } else {
	            System.err.println("Couldn't find loadedFile: " + path);
	            return null;
	        }
	    }
	    
	    /**
	     * Create the GUI and show it.  For thread safety,
	     * this method should be invoked from
	     * the event dispatch thread.
	     */
	    private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("TabbedPaneDemo");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        //Add content to the window.
	        //frame.add(new MainPanel(getJanelaPai()), BorderLayout.CENTER);
	        
	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }
	    
	    public static void main(String[] args) {
	        //Schedule a job for the event dispatch thread:
	        //creating and showing this application's GUI.
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                //Turn off metal's use of bold fonts
			UIManager.put("swing.boldMetal", Boolean.FALSE);
			createAndShowGUI();
	            }
	        });
	    }

}
