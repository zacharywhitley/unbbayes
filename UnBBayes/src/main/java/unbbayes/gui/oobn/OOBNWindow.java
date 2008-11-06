/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.MSBNController;
import unbbayes.controller.OOBNController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNWindow extends JInternalFrame  {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	public static String EDITION_PANE = "editionPane";

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	// lets stop using model object directly and start quering controller each time we need to access oobn
	//private IObjectOrientedBayesianNetwork oobn;

	private JScrollPane netScroll;
	private JList netList;

	private JButton compileBtn;
	//private JButton editionBtn;
	private JButton removeBtn;
	private JButton newBtn;
	private JButton newFromFileBtn;

	private CardLayout btnCard;
	private JToolBar jtbBtns;
	
	// status bar
	private JPanel statusPanel;
	private JLabel statusBar;
	
	private JPanel netPanel;
	private JPanel editionPane;

    protected IconController iconController = IconController.getInstance();
    
    private OOBNController controller = null;
    
    private FileController fileController = null;
	
	/**
	 * Builds a window to visualize and edit OOBN
	 * @param oobn: the model representation of OOBN
	 * @param controller: who is the controller of this OOBN
	 */
	public OOBNWindow(IObjectOrientedBayesianNetwork oobn, OOBNController controller) {
		
		super(oobn.getTitle(), true, true, true, true);
		
//		this.setOobn(oobn);
		
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
		Container pane = this.getContentPane();
		
		this.initComponents();
		
		pane.setLayout(new BorderLayout());
		
		pane.add(buildClassNavigationPanel(), BorderLayout.WEST);

	    pane.add(this.buildStatusBar(), BorderLayout.SOUTH);
		
	    this.fillListeners();
		
	}
	
	

	

	

	/**
	 * Build up the basic component hierarchy of this window
	 * and initializes some attributes
	 */
	private void initComponents() {
		this.netList = new JList(new OOBNListModel());
		this.netList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.netList.setEnabled(true);
		this.netList.setDragEnabled(true);
		
		// icons
		
		this.compileBtn = new JButton(iconController.getCompileIcon());		
		//this.editionBtn = new JButton(iconController.getEditIcon());
		this.removeBtn = new JButton(iconController.getDeleteClassIcon());
		this.newBtn = new JButton(iconController.getNewIcon());
		this.newFromFileBtn = new JButton(iconController.getNewClassFromFileIcon());
		
		// tool tips
		
		this.compileBtn.setToolTipText(resource.getString("compileToolTip"));
		//this.editionBtn.setToolTipText(resource.getString("editionToolTip"));
		this.removeBtn.setToolTipText(resource.getString("removeToolTip"));
		this.newBtn.setToolTipText(resource.getString("newToolTip"));
		this.newFromFileBtn.setToolTipText(resource.getString("newFromFileToolTip"));
		
		// file controller for class loading
		this.fileController = FileController.getInstance();
	}
	
	/**
	 * Builds up the status bar
	 * @return the status bar
	 */
	private Container buildStatusBar() {
		
		statusPanel = new JPanel();
		statusBar = new JLabel();
		
		statusPanel.setLayout(new BorderLayout());
	    statusPanel.setBorder(new TitledBorder(resource.getString("status")));
	    statusBar.setText(resource.getString("statusReadyLabel"));
	    
	    statusPanel.add(statusBar);
	    
	    return this.statusPanel;
	}

	
	
	/**
	 * build up the class navigation panel
	 * @return a JPanel with class navigation
	 */
	private JPanel buildClassNavigationPanel() {		
		
		this.netPanel = new JPanel(new BorderLayout());
		this.netScroll = new JScrollPane(netList);
		
		netPanel.add(this.netScroll, BorderLayout.CENTER);
		
		this.jtbBtns = new JToolBar();
		this.jtbBtns.add(buildButtonsPanel(), EDITION_PANE);
		
		netPanel.add(jtbBtns, BorderLayout.NORTH);
		
		return netPanel;
	}

	/**
	 * Sets up the edition pane (which contains basic buttons to control classes).
	 * Also fills up some attributes of this class
	 */
	private JPanel buildButtonsPanel() {
		this.btnCard = new CardLayout();
		
		this.jtbBtns.setLayout(this.btnCard);
		this.editionPane = new JPanel();
//		JToolBar toolbar = new JToolBar();
//		this.jtbBtns.add(toolbar, EDITION_PANE);
		editionPane.add(this.newBtn);
		editionPane.add(this.newFromFileBtn);
		editionPane.add(this.removeBtn);
		editionPane.add(this.compileBtn);
//		toolbar.add(this.newBtn);
//		toolbar.add(this.removeBtn);
//		toolbar.add(this.compileBtn);

		//JPanel compiledPane = new JPanel();
		//compiledPane.add(this.editionBtn);
		//this.jtbBtns.add(compiledPane, COMPILED_PANE);
		showBtnPanel(EDITION_PANE);
		
		return this.editionPane;
	}

	public void addCompileBtnActionListener(ActionListener a) {
		this.compileBtn.addActionListener(a);
	}

	public void addRemoveBtnActionListener(ActionListener a) {
		this.removeBtn.addActionListener(a);
	}

	public void addNewBtnActionListener(ActionListener a) {
		this.newBtn.addActionListener(a);
	}
	
	public void addNewFromFileBtnActionListener(ActionListener a) {
		this.newFromFileBtn.addActionListener(a);
	}

//	public void addEditionActionListener(ActionListener a) {
//		this.editionBtn.addActionListener(a);
//	}

	public void addListMouseListener(MouseListener l) {
		this.netList.addMouseListener(l);
	}

	public void showBtnPanel(String paneName) {
		this.btnCard.show(this.jtbBtns, paneName);
	}
	
	
	/**
	 * starts filling the action listeners
	 */
	private void fillListeners() {
		
		// action performed by clicking a class within oobn class list
		addListMouseListener(new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		       
		       int index = getNetList().locationToIndex(e.getPoint());
		       
		       // change the selected oobn class only if the selected one is different than the previous
		       if ( ( index >= 0 ) 
	              && ( getNetList().getModel().getElementAt(index) != getController().getActive().getSingleEntityNetwork())) {
		    	   // set the active oobn class as the selected one
		           OOBNClassWindow classWindow = OOBNClassWindow.newInstance((getController().getOobn().getOOBNClassList().get(index)));
		           getController().changeActiveOOBNClass(classWindow);		             	
	           } 	
		       
		       // if left click, change the name
               if(e.getModifiers() == MouseEvent.BUTTON3_MASK){

                   ListSelectionModel selmodel = getNetList().getSelectionModel();
                   selmodel.setLeadSelectionIndex(index);
            	   
            	   // if double click, extract the element and open an input dialog
                   Object item = getNetList().getModel().getElementAt(index);
                   String text = JOptionPane.showInputDialog(resource.getString("renameClass"), item);
                   
                   //tests if the input is reasonable (not null and not empty)                   
                   String newName = null;
                   if (text != null) {
                	   newName = text.trim();
                   } else {
                	   return;
                   }
                   if (getController().containsOOBNClassByName(newName)) {
					   Debug.println(this.getClass(), "The name already exists");
					   JOptionPane.showMessageDialog(getController().getPanel(), resource.getString("DuplicatedClassName"), resource.getString("renameClass"), JOptionPane.ERROR_MESSAGE);
						
					   return;
                   }
                   if (!newName.isEmpty()) {
                	    // renames the class
                	    try {
                		   getController().getOobn().getOOBNClassList().get(index).setClassName(newName);
						} catch (Exception e1) {
						   Debug.println(this.getClass(), "Invalid name", e1);
						   System.err.print(e1.getMessage());
						}
                   }
                   
                   
                   // TODO change the references if needed
                   Debug.println(this.getClass(), "Changing references at renaming event is not implemented yet.");
                }
		     	
		     }
		});
		
			
		// listener for pressing the "add new class" button
		addNewBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				getController().addNewOOBNClass(resource.getString("newOOBNClass") + getController().getOobn().getOOBNClassList().size());
				// renew the list view
				getNetList().updateUI();
			}
		});
		
		// create an ActionListener for loading a class
		addNewFromFileBtnActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						// String[] nets = new String[] { "net", "xml", "owl" };
						String[] nets = new String[] { "net", "oobn"};
						JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
						chooser.setDialogTitle(resource.getString("openTitle")); 
						chooser.setMultiSelectionEnabled(false);
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

						// adicionar FileView no FileChooser para desenhar icones de
						// arquivos
						chooser.setFileView(new FileIcon(getController().getUpperUnBBayesFrame()));

						chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
								resource.getString("oobnFileFilter")));
						
						
						int option = chooser.showOpenDialog(getController().getUpperUnBBayesFrame());
						if (option == JFileChooser.APPROVE_OPTION) {
							if (chooser.getSelectedFile() != null) {
								chooser.setVisible(false); 
								getController().getUpperUnBBayesFrame().repaint(); 
								File file = chooser.getSelectedFile(); 
								fileController.setCurrentDirectory(chooser
										.getCurrentDirectory());
							    chooser.setVisible(false); 
							    chooser.setEnabled(false);
							    
							    
								try{
									Collection<IOOBNClass> newClasses = getController().loadOOBNClassesFromFile(file);
									for (IOOBNClass loadedClass : newClasses) {
										getController().addOOBNClass(loadedClass);
									}
								} catch (IllegalArgumentException iae) {
									JOptionPane.showMessageDialog(getController().getPanel(), resource.getString("DuplicatedClassName"), iae.getMessage(), JOptionPane.ERROR_MESSAGE);
									Debug.println(this.getClass(), resource.getString("NoClassSelected"), iae);
								}
								
								// renew the list view
								getNetList().updateUI();
							}
						}
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});
		
		// listener for pressing the "remove class" button
		addRemoveBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// tests if ther is any active class. If not, no class is selected...
				if (getController().getActive() == null) {
					// since no class is selected, we'll do nothing
					return;
				}
				
				// extracts the selected class
				int index = getNetList().getSelectedIndex();
				if (index < 0) {
					// no element was selected at all...
					return;
				}
				
				getController().removeClassAt(index);
				
				// I'm not sure if the test below is really necessary
//				if (getOobn().getOOBNClassCount() <= 0) {					
//					newBtnAction.actionPerformed(null);
//				} 
				
				// set the next selected network as the default (root - non editable)
				getNetList().setSelectedIndex(0);
				
				// updates view
				getNetList().updateUI();
				getNetList().repaint();
				
			}
		});
		
		addCompileBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					getController().compileActiveOOBNClass();
					// TODO stub!!
					SingleAgentMSBN msbn = new SingleAgentMSBN(getController().getActive().getName());
					MSBNController controller = new MSBNController(msbn);
					getController().getUpperUnBBayesFrame().addWindow(controller.getPanel());
				} catch (NullPointerException npe) {
					JOptionPane.showMessageDialog(getController().getPanel(), resource.getString("NoClassSelected"), resource.getString("compilationError"), JOptionPane.ERROR_MESSAGE);
					Debug.println(this.getClass(), resource.getString("NoClassSelected"), npe);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(getController().getPanel(), e.getMessage(), resource.getString("compilationError"), JOptionPane.ERROR_MESSAGE);
					Debug.println(this.getClass(), "Unknown", e);
				}
			}
		});
	}
	
	
	
	

	/**
	 * Returns the netList.
	 * @return JList
	 */
	public JList getNetList() {
		return this.netList;
	}

	public void changeToTreeView(JTree tree) {
		this.netScroll.setViewportView(tree);
	}

	public void changeToListView() {
		this.netScroll.setViewportView(this.netList);
	}


	public OOBNController getController() {
		return controller;
	}

	public void setController(OOBNController controller) {
		this.controller = controller;
	}
	
	/**
	 * Inner class to make it easier to  manage JList and its
	 * contents
	 * @author Shou Matsumoto
	 *
	 */
	private class OOBNListModel extends AbstractListModel {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;	
		
		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getSize()
		 */
		public int getSize() {
			return getController().getOobn().getOOBNClassCount();
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int index) {
			try {
				return getController().getOobn().getOOBNClassList().get(index);
			} catch (RuntimeException e) {
				try {
					Debug.println(this.getClass(), "Cannot retrieve oobn class at " 
							+ index + 
							" from " + getController().getOobn().getTitle());
				} catch (Exception e2) {
					Debug.println(this.getClass(), "Unknown error - may be no OOBN is set.");
				}
				throw e;
			}
		}
		
		
		

	}
	

}
