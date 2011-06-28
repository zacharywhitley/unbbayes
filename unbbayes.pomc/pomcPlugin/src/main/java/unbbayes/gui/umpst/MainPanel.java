package unbbayes.gui.umpst;

/*
 * TabbedPaneDemo.java requires one additional loadedFile:
 *   images/middle.gif.
 */

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import unbbayes.io.umpst.FileLoad;
import unbbayes.io.umpst.FileSave;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

public class MainPanel extends IUMPSTPanel{
	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		JPanel pane;
		private Goals requirementsPane;
		private Entities entitiesPane;
		private Rules rulesPane;
		private Groups groupsPane;
		private File loadedFile;
		private File newFile;
		private static final String  FILE_EXTENSION = "ump";
		private String fileExtension;
		private FileFilter filter;

	    public MainPanel(UmpstModule janelaPai) {
	        super(new GridLayout(1, 1),janelaPai);
	        
	        JTabbedPane tabbedPane = new JTabbedPane();
	        ImageIcon icon = createImageIcon("images/middle.gif");
	        
	        requirementsPane = new Goals(getFatherPanel());
	        requirementsPane.setPreferredSize(new Dimension(1000, 500));
	        
	        tabbedPane.addTab("Requirement", icon, new JScrollPane(requirementsPane),
	                "goals,queries and envidences");
	        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	        
	        entitiesPane = new Entities(getFatherPanel());
	        entitiesPane.setPreferredSize(new Dimension(1000,500));
	        tabbedPane.addTab("Entity", icon, entitiesPane,
	                "entities, atributtes and relationships");
	        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

	        rulesPane = new Rules(getFatherPanel());
	        rulesPane.setPreferredSize(new Dimension(1000,500));	        
	        tabbedPane.addTab("Rules", icon, rulesPane,
	                "Deterministic or Stochastic");
	        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
	        
	        groupsPane = new Groups(getFatherPanel());
	        groupsPane.setPreferredSize(new Dimension(1000,500));
	        tabbedPane.addTab("Group", icon, groupsPane,
	                "Grouping");
	        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
	        
	        
	        JMenu fileMenu = new JMenu("File");
	        fileMenu.setMnemonic('f');
	        
	        
	        JMenuItem loadItem = new JMenuItem("Open existent");
	        loadItem.setMnemonic('o');
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
						io.loadUbf(loadedFile);
						GoalsSearchPanel goalPanel = new GoalsSearchPanel(getFatherPanel());
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
	        
	  
	        
	        JMenuBar bar = new JMenuBar();
	        bar.add(fileMenu);
	        
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
							io.saveUbf(newFile);
							JOptionPane.showMessageDialog(null, "file saved");
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
		public Groups getGroupsPane() {
			return groupsPane;
		}




		/**
		 * @param groupsPane the groupsPane to set
		 */
		public void setGroupsPane(Groups groupsPane) {
			this.groupsPane = groupsPane;
		}




		/**
		 * @return the rulesPane
		 */
		public Rules getRulesPane() {
			return rulesPane;
		}




		/**
		 * @param rulesPane the rulesPane to set
		 */
		public void setRulesPane(Rules rulesPane) {
			this.rulesPane = rulesPane;
		}




		/**
		 * @return the requirementsPane
		 */
		public Goals getRequirementsPane() {
			return requirementsPane;
		}
		

		/**
		 * @param requirementsPane the requirementsPane to set
		 */
		public void setRequirementsPane(Goals requirementsPane) {
			this.requirementsPane = requirementsPane;
		}
		
		

		/**
		 * @return the entitiesPane
		 */
		public Entities getEntitiesPane() {
			return entitiesPane;
		}

		/**
		 * @param entitiesPane the entitiesPane to set
		 */
		public void setEntitiesPane(Entities entitiesPane) {
			this.entitiesPane = entitiesPane;
		}

		protected JPanel  createInternalPane (JPanel pane){
	    	
	    	this.setLayout(new FlowLayout());
			this.add(new GoalsAdd(getFatherPanel(),null,null));
	    	
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
