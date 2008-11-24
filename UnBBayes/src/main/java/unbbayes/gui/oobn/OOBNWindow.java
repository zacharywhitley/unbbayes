/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.MSBNController;
import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.IFileExtensionAwareWindow;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNWindow extends JInternalFrame implements IFileExtensionAwareWindow  {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	public static String EDITION_PANE = "editionPane";
	
	// since this implements IFileExtensionAwareWindow, let's store them 
	private static final String[] SUPPORTED_FILE_EXTENSIONS = {IObjectOrientedBayesianNetworkIO.fileExtension};

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.oobn.resources.OOBNGuiResource");
//	private static ResourceBundle rootGUIResource =
//		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	// lets stop using model object directly and start quering controller each time we need to access oobn
	//private IObjectOrientedBayesianNetwork oobn;

	private JScrollPane oobnClassScroll;
	private JList oobnClassList;

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
    protected OOBNWindow(IObjectOrientedBayesianNetwork oobn, OOBNController controller) {
		
		super(oobn.getTitle(), true, true, true, true);
		
		// we do not need to trace the oobn since we can do it by calling it from the controller
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
	 * Builds a window to visualize and edit OOBN
	 * @param oobn: the model representation of OOBN
	 * @param controller: who is the controller of this OOBN
	 */
    public static OOBNWindow newInstance(IObjectOrientedBayesianNetwork oobn, OOBNController controller) {
    	return new OOBNWindow(oobn, controller);
    }
	

    
	/**
	 * Build up the basic component hierarchy of this window
	 * and initializes some attributes
	 */
	private void initComponents() {
		this.oobnClassList = new JList(new OOBNListModel());
		this.oobnClassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.oobnClassList.setEnabled(true);
		this.oobnClassList.setDragEnabled(true);
		this.oobnClassList.setToolTipText(resource.getString("dragNDropToAddInstance"));
		
		// change the data transfer behavior on drag n drop action (or copy n paste)
		this.oobnClassList.setTransferHandler(new TransferHandler () {

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
			 */
			@Override
			protected Transferable createTransferable(JComponent c) {
				try{
					// obtains the currently selected oobn class (which may be different than currently active one)
					return getController().getSelectedClass();
				} catch (Exception e) {
					Debug.println(this.getClass(), "It was not possible to create transferable data", e);
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
			 */
			@Override
			public int getSourceActions(JComponent c) {
				// declares that only copy mode is enabled
				// copy mode means that the source is not deleted
				return TransferHandler.COPY;
			}

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
			 */
			@Override
			protected void exportDone(JComponent source, Transferable data,
					int action) {
				Debug.println(this.getClass(), "Export of data was done");
				renewClassListIndex();
				super.exportDone(source, data, action);
			}
			
			
			
			
			
		});
		
		
		// icons
		
		this.compileBtn = new JButton(iconController.getCompileIcon());		
		//this.editionBtn = new JButton(iconController.getEditIcon());
		this.removeBtn = new JButton(iconController.getDeleteClassIcon());
		this.newBtn = new JButton(iconController.getNewClassIcon());
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
		this.oobnClassScroll = new JScrollPane(oobnClassList);
		this.oobnClassScroll.setToolTipText(resource.getString("dragNDropToAddInstance"));
		
		
		
		netPanel.setBorder(new TitledBorder(resource.getString("classNavigationPanelLabel")));
		
		netPanel.add(this.oobnClassScroll, BorderLayout.CENTER);
		
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
		this.oobnClassList.addMouseListener(l);
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
		     
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == e.BUTTON1) {
					try {
						int index = getNetList().locationToIndex(e.getPoint());
						getController().setSelectedClass((getController().getOobn().getOOBNClassList().get(index)));
					} catch (Exception exc) {
						Debug.println(this.getClass(), "It was not possible to perform mouse pressed event", exc);
					}
				}
				
//				super.mousePressed(e);
			}
			
			

			



			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				// just to make it sure that the list is pointing to the correct element
				renewClassListIndex();
				super.mouseEntered(e);
			}
			
			
			







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
						String[] nets = new String[] { "net", "oobn"};
						JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
						chooser.setDialogTitle(resource.getString("openClassFromFile")); 
						chooser.setMultiSelectionEnabled(false);
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

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
										try{
											getController().addOOBNClass(loadedClass);
										} catch (IllegalArgumentException iae) {
											Debug.println(this.getClass(), "Loaded a class already loaded.");
										}
									}
								} catch (Exception e) {
									JOptionPane.showMessageDialog(getController().getPanel(), resource.getString("ErrorLoadingClass"), e.getMessage(), JOptionPane.ERROR_MESSAGE);
									Debug.println(this.getClass(), "Error opening file", e);
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
				
				// tests if there is any active class. If not, no class is selected...
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
					
					AbstractMSBN msbn = getController().compileActiveOOBNClassToMSBN();
					MSBNController controller = new MSBNController((SingleAgentMSBN)msbn);
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
	 * Returns the oobnClassList.
	 * @return JList
	 */
	public JList getNetList() {
		return this.oobnClassList;
	}

	public void changeToTreeView(JTree tree) {
		this.oobnClassScroll.setViewportView(tree);
	}

	public void changeToListView() {
		this.oobnClassScroll.setViewportView(this.oobnClassList);
	}

	
	/**
	 * This method resets the currently selected class list element
	 * to the currently active class window.
	 * It is useful to make sure that the selected class at class list is
	 * allways the currently active class.
	 * @see OOBNController#getActive()
	 */
	protected void renewClassListIndex() {
		try {
			SingleEntityNetwork activeNetwork = getController().getActive().getController().getSingleEntityNetwork();
			int indexOfActiveNetwork = getController().getOobn().getOOBNClassList().indexOf(activeNetwork);
			
			getOobnClassList().setSelectedIndex(indexOfActiveNetwork);
			getOobnClassList().updateUI();
			Debug.println(this.getClass(), "Setted active class list index to " + indexOfActiveNetwork);
		} catch (Exception exc) {
			Debug.println(this.getClass(), "Could not treat event in order to change selected OOBN class", exc);
		}
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




	/**
	 * @return the oobnClassScroll
	 */
	public JScrollPane getOobnClassScroll() {
		return oobnClassScroll;
	}


	/**
	 * @param oobnClassScroll the oobnClassScroll to set
	 */
	public void setOobnClassScroll(JScrollPane oobnClassScroll) {
		this.oobnClassScroll = oobnClassScroll;
	}


	/**
	 * @return the oobnClassList
	 */
	public JList getOobnClassList() {
		return oobnClassList;
	}


	/**
	 * @param oobnClassList the oobnClassList to set
	 */
	public void setOobnClassList(JList oobnClassList) {
		this.oobnClassList = oobnClassList;
	}


	/**
	 * @return the compileBtn
	 */
	public JButton getCompileBtn() {
		return compileBtn;
	}


	/**
	 * @param compileBtn the compileBtn to set
	 */
	public void setCompileBtn(JButton compileBtn) {
		this.compileBtn = compileBtn;
	}


	/**
	 * @return the removeBtn
	 */
	public JButton getRemoveBtn() {
		return removeBtn;
	}


	/**
	 * @param removeBtn the removeBtn to set
	 */
	public void setRemoveBtn(JButton removeBtn) {
		this.removeBtn = removeBtn;
	}


	/**
	 * @return the newBtn
	 */
	public JButton getNewBtn() {
		return newBtn;
	}


	/**
	 * @param newBtn the newBtn to set
	 */
	public void setNewBtn(JButton newBtn) {
		this.newBtn = newBtn;
	}


	/**
	 * @return the newFromFileBtn
	 */
	public JButton getNewFromFileBtn() {
		return newFromFileBtn;
	}


	/**
	 * @param newFromFileBtn the newFromFileBtn to set
	 */
	public void setNewFromFileBtn(JButton newFromFileBtn) {
		this.newFromFileBtn = newFromFileBtn;
	}


	/**
	 * @return the btnCard
	 */
	public CardLayout getBtnCard() {
		return btnCard;
	}


	/**
	 * @param btnCard the btnCard to set
	 */
	public void setBtnCard(CardLayout btnCard) {
		this.btnCard = btnCard;
	}


	/**
	 * @return the jtbBtns
	 */
	public JToolBar getJtbBtns() {
		return jtbBtns;
	}


	/**
	 * @param jtbBtns the jtbBtns to set
	 */
	public void setJtbBtns(JToolBar jtbBtns) {
		this.jtbBtns = jtbBtns;
	}


	/**
	 * @return the statusPanel
	 */
	public JPanel getStatusPanel() {
		return statusPanel;
	}


	/**
	 * @param statusPanel the statusPanel to set
	 */
	public void setStatusPanel(JPanel statusPanel) {
		this.statusPanel = statusPanel;
	}


	/**
	 * @return the statusBar
	 */
	public JLabel getStatusBar() {
		return statusBar;
	}


	/**
	 * @param statusBar the statusBar to set
	 */
	public void setStatusBar(JLabel statusBar) {
		this.statusBar = statusBar;
	}


	/**
	 * @return the netPanel
	 */
	public JPanel getNetPanel() {
		return netPanel;
	}


	/**
	 * @param netPanel the netPanel to set
	 */
	public void setNetPanel(JPanel netPanel) {
		this.netPanel = netPanel;
	}


	/**
	 * @return the editionPane
	 */
	public JPanel getEditionPane() {
		return editionPane;
	}


	/**
	 * @param editionPane the editionPane to set
	 */
	public void setEditionPane(JPanel editionPane) {
		this.editionPane = editionPane;
	}


	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}


	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}


	/**
	 * @return the fileController
	 */
	public FileController getFileController() {
		return fileController;
	}


	/**
	 * @param fileController the fileController to set
	 */
	public void setFileController(FileController fileController) {
		this.fileController = fileController;
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.IFileExtensionAwareWindow#getSupportedFileExtensions()
	 */
	public String[] getSupportedFileExtensions() {
		return SUPPORTED_FILE_EXTENSIONS;
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.IFileExtensionAwareWindow#getSupportedFilesDescription()
	 */
	public String getSupportedFilesDescription() {
		return resource.getString("netFileFilterSaveOOBN");
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.gui.IFileExtensionAwareWindow#getSavingMessage()
	 */
	public String getSavingMessage() {
		return resource.getString("saveTitle");
	}

}
