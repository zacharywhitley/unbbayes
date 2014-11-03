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
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ResourceBundle;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import unbbayes.controller.umpst.Controller;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.entity.EntitiesMainPanel;
import unbbayes.gui.umpst.goal.GoalsEditionPanel;
import unbbayes.gui.umpst.goal.GoalsMainPanel;
import unbbayes.gui.umpst.group.GroupsMainPanel;
import unbbayes.gui.umpst.rule.RulesMainPanel;
import unbbayes.io.umpst.FileLoadObject;
import unbbayes.io.umpst.FileSaveObject;
import unbbayes.model.umpst.project.UMPSTProject;

public class MainPanel extends IUMPSTPanel{

	private static final long serialVersionUID = 1L;

	private static final String  FILE_EXTENSION = "ump";

	private GoalsMainPanel goalsPane;
	private EntitiesMainPanel entitiesPane;
	private RulesMainPanel rulesPane;
	private GroupsMainPanel groupsPane;

	private String fileExtension;

	private JMenuItem newMenuItem;
	private JMenuItem loadMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem helpContentsItem;
	private JMenuItem aboutItem;

	private Controller controller; 

	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());


	public MainPanel(UmpstModule janelaPai) {	    	
		super(new GridLayout(1, 1),janelaPai);

		final IconController iconController = IconController.getInstance(); 

		//------------------------ File ------------------------------------

		JMenu fileMenu = new JMenu(resource.getString("mnFile"));
		fileMenu.setMnemonic(resource.getString("mnFileMnemonic").charAt(0));

		newMenuItem = new JMenuItem(resource.getString("mnNewFile"));
		newMenuItem.setMnemonic(resource.getString("mnNewFileMnemonic").charAt(0));
		newMenuItem.setIcon(iconController.getNewIcon()); 

		fileMenu.add(newMenuItem);
		
		loadMenuItem = new JMenuItem(resource.getString("mnOpen"));
		loadMenuItem.setMnemonic(resource.getString("mnOpenMnemonic").charAt(0));
		loadMenuItem.setIcon(iconController.getOpenIcon()); 

		fileMenu.add(loadMenuItem);

		saveMenuItem = new JMenuItem(resource.getString("mnSave"));
		saveMenuItem.setMnemonic('v');
		saveMenuItem.setIcon(iconController.getSaveIcon());
		
		fileMenu.add(saveMenuItem);
		
		saveAsMenuItem = new JMenuItem(resource.getString("mnSaveAs"));
		saveAsMenuItem.setMnemonic('s');
		saveAsMenuItem.setIcon(iconController.getSaveIcon());

		fileMenu.add(saveAsMenuItem);

		//------------------------ Help ------------------------------------
		JMenu helpMenu = new JMenu(resource.getString("mnHelp"));
		helpMenu.setMnemonic(resource.getString("mnHelpMnemonic").charAt(0));

		helpContentsItem = new JMenuItem(resource.getString("mnHelpContents"));
		helpContentsItem.setMnemonic(resource.getString("mnHelpContentsMnemonic").charAt(0));

		aboutItem = new JMenuItem(resource.getString("mnAbout"));
		aboutItem.setMnemonic(resource.getString("mnAboutMnemonic").charAt(0));

		helpMenu.add(helpContentsItem);

		JMenuBar bar = new JMenuBar();
		bar.add(fileMenu);
		bar.add(helpMenu); 

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(bar,BorderLayout.PAGE_START);
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		panel.add(tabbedPane,BorderLayout.CENTER);

		//Add the tabbed pane to this panel.
		add(panel);

		createListeners(iconController, tabbedPane);
	}



	private void createListeners(final IconController iconController,
			final JTabbedPane tabbedPane) {

		newMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				UMPSTProject umpstProject = new UMPSTProject();
				setUmpstProject(umpstProject); 
				controller = Controller.getInstance(umpstProject); 
				
				createTabPanels(umpstProject, iconController, tabbedPane);
				
			}
		});

		
		loadMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser();
				File loadFile = null;
				
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setCurrentDirectory (new File ("."));

				int res = fc.showOpenDialog(null);

				if(res == JFileChooser.APPROVE_OPTION){
					loadFile = fc.getSelectedFile();
				}

				int index = loadFile.getName().lastIndexOf(".");

				if (index >= 0) {
					fileExtension = loadFile.getName().substring(index + 1);
				}

				if (fileExtension.equals(FILE_EXTENSION)){

					FileLoadObject io = new FileLoadObject();

					try {
						controller = Controller.getInstance(null); 
						setUmpstProject(io.loadUbf(loadFile,getUmpstProject())) ;
						controller.setUMPSTProject(getUmpstProject()); 
						
						createTabPanels(getUmpstProject(), iconController, tabbedPane);
						
						getUmpstProject().setFileName(loadFile.getAbsolutePath()); 
						
						System.out.println(getUmpstProject().getFileName());
						controller.showSucessMessageDialog(resource.getString("msLoadSuccessfull")); 
						
					} catch (ClassNotFoundException e1) {
						controller.showErrorMessageDialog(
								resource.getString("erLoadFatal")); 
						e1.printStackTrace();
					}  catch (InvalidClassException e1) {
						controller.showErrorMessageDialog(
								resource.getString("erIncompatibleVersion")); 
						e1.printStackTrace();
					}
					catch (IOException e1) {
						controller.showErrorMessageDialog(
								resource.getString("erLoadFatal")); 
						e1.printStackTrace();
					}

				}
				else{
					controller.showErrorMessageDialog(
							resource.getString("erNotUmpFormat")); 
				}
			}
		});

		saveAsMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				File newFile = null;
				
				FileSaveObject file = new FileSaveObject();

				JFileChooser fc =  new JFileChooser();  
				fc.setCurrentDirectory (new File ("."));

				int res = fc.showSaveDialog(null);

				if(res == JFileChooser.APPROVE_OPTION){
					newFile = fc.getSelectedFile();
				}

				if (newFile!=null)	{
					try {
						file.saveUbf(newFile,getUmpstProject());
						controller.showSucessMessageDialog(resource.getString("msSaveSuccessfull")); 
					} catch (FileNotFoundException e1) {
						controller.showErrorMessageDialog(resource.getString("erFileNotFound")); 
						e1.printStackTrace();
					} catch (IOException e2) {
						controller.showErrorMessageDialog(resource.getString("erSaveFatal")); 
						e2.printStackTrace();
					}
				}
				else {
					controller.showErrorMessageDialog(resource.getString("erSaveFatal")); 
				}
			}
			
		});
		
		saveMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				File newFile = null;
				
				FileSaveObject file = new FileSaveObject();

				if(getUmpstProject().getFileName() != null){
					newFile = new File(getUmpstProject().getFileName()); 
					if (!newFile.exists()){
						controller.showErrorMessageDialog(resource.getString("erFileNotFound")); 
					}
				}else{
					
					JFileChooser fc =  new JFileChooser();  
					fc.setCurrentDirectory (new File ("."));

					int res = fc.showSaveDialog(null);
					if(res == JFileChooser.APPROVE_OPTION){
						newFile = fc.getSelectedFile();
					}
				}

				if (newFile!=null)	{
					try {
						file.saveUbf(newFile,getUmpstProject());
						controller.showSucessMessageDialog(resource.getString("msSaveSuccessfull")); 
					} catch (FileNotFoundException e1) {
						controller.showErrorMessageDialog(resource.getString("erFileNotFound")); 
						e1.printStackTrace();
					} catch (IOException e2) {
						controller.showErrorMessageDialog(resource.getString("erSaveFatal")); 
						e2.printStackTrace();
					}
				}
				else {
					controller.showErrorMessageDialog(resource.getString("erSaveFatal")); 
				}
			}
		});
		
		helpContentsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					HelpSet set =  new HelpSet(null, getClass().getResource("/help/UMPHelp/ump.hs"));
					JHelp help = new JHelp(set);
					JFrame f = new JFrame();
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.setContentPane(help);
					f.pack();
					f.setLocationRelativeTo(getFatherPanel()); 
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

	}

	private void createTabPanels(UMPSTProject umpstProject,
			IconController iconController, JTabbedPane tabbedPane) {

		tabbedPane.removeAll(); 
		
		//GOALS
		goalsPane = new GoalsMainPanel(getFatherPanel(),umpstProject);
		goalsPane.setPreferredSize(new Dimension(1000, 500));

		tabbedPane.addTab(resource.getString("ttGoals"), 
				iconController.getRequirementsIcon(), 
				new JScrollPane(goalsPane),
				resource.getString("hpGoalsTab"));

		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		//ENTITIES
		entitiesPane = new EntitiesMainPanel(getFatherPanel(),getUmpstProject());
		entitiesPane.setPreferredSize(new Dimension(1000,500));

		tabbedPane.addTab(resource.getString("ttEntities"), 
				iconController.getAnalysisDesignIcon(), 
				entitiesPane,
				resource.getString("hpEntitiesTab"));
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		//RULES
		rulesPane = new RulesMainPanel(getFatherPanel(),umpstProject);
		rulesPane.setPreferredSize(new Dimension(1000,500));	        
		tabbedPane.addTab(resource.getString("ttRules"), 
				iconController.getAnalysisDesignIcon(), 
				rulesPane,
				resource.getString("hpRulesTab"));
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		
		//GROUPS
		groupsPane = new GroupsMainPanel(getFatherPanel(),umpstProject);
		groupsPane.setPreferredSize(new Dimension(1000,500));
		tabbedPane.addTab(resource.getString("ttGroups"), 
				iconController.getAnalysisDesignIcon(), 
				groupsPane,
				resource.getString("hpGroupsTab"));
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		
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
	public GoalsMainPanel getGoalsPane() {
		return goalsPane;
	}

	/**
	 * @param requirementsPane the requirementsPane to set
	 */
	public void setGoalsPane(GoalsMainPanel requirementsPane) {
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
	
}
