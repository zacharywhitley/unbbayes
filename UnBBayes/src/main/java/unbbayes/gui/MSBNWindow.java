/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import unbbayes.controller.IconController;
import unbbayes.controller.MSBNController;
import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.UBIOException;
import unbbayes.prs.Graph;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.extension.UnBBayesModule;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSBNWindow extends UnBBayesModule {
	
	// since this implements IPersistenceAwareWindow, let's store them 
	// The supported file is a folder...
	private static final String[] SUPPORTED_FILE_EXTENSIONS = {};
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	public static String EDITION_PANE = "editionPane";
	public static String COMPILED_PANE = "compiledPane";

	/** Load resource file from this package */
	private static ResourceBundle resource =
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.resources.GuiResources.class.getName());

	private class MSBNListModel extends AbstractListModel {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;	
		
		public int getSize() {
			return msbn.getNetCount();
		}

		public Object getElementAt(int index) {
			return msbn.getNetAt(index);
		}
	}

	private SingleAgentMSBN msbn;

	private JScrollPane netScroll;
	private JList netList;

	private JButton compileBtn;
	private JButton editionBtn;
	private JButton removeBtn;
	private JButton newBtn;

	private CardLayout btnCard;
	private JToolBar jtbBtns;

        protected IconController iconController = IconController.getInstance();
        
    private MSBNController controller;

	public MSBNWindow(SingleAgentMSBN msbn, MSBNController controller) {
		super(msbn.getId());
		this.msbn = msbn;
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		Container pane = getContentPane();
		initComponents();
		pane.setLayout(new BorderLayout());
		pane.add(makeListPanel(), BorderLayout.WEST);
		init();
		this.controller = controller;
	}

	public SingleAgentMSBN getMSNet() {
		return msbn;
	}

	private void initComponents() {
		netList = new JList(new MSBNListModel());
		compileBtn = new JButton(iconController.getCompileIcon());
		compileBtn.setToolTipText(resource.getString("compileToolTip"));
		editionBtn = new JButton(iconController.getEditIcon());
		removeBtn = new JButton(iconController.getLessIcon());
		newBtn = new JButton(iconController.getMoreIcon());
	}

	private void init() {
		netList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	private JPanel makeListPanel() {
		JPanel netPanel = new JPanel(new BorderLayout());
		netScroll = new JScrollPane(netList);
		netPanel.add(netScroll, BorderLayout.CENTER);
		setupButtonsPanel();
		netPanel.add(jtbBtns, BorderLayout.NORTH);
		return netPanel;
	}

	private void setupButtonsPanel() {
		btnCard = new CardLayout();
		jtbBtns = new JToolBar();
		jtbBtns.setLayout(btnCard);
		JPanel editionPane = new JPanel();
		jtbBtns.add(editionPane, EDITION_PANE);
		editionPane.add(newBtn);
		editionPane.add(removeBtn);
		editionPane.add(compileBtn);

		JPanel compiledPane = new JPanel();
		compiledPane.add(editionBtn);
		jtbBtns.add(compiledPane, COMPILED_PANE);
		showBtnPanel(EDITION_PANE);
	}

	public void addCompileBtnActionListener(ActionListener a) {
		compileBtn.addActionListener(a);
	}

	public void addRemoveBtnActionListener(ActionListener a) {
		removeBtn.addActionListener(a);
	}

	public void addNewBtnActionListener(ActionListener a) {
		newBtn.addActionListener(a);
	}

	public void addEditionActionListener(ActionListener a) {
		editionBtn.addActionListener(a);
	}

	public void addListMouseListener(MouseListener l) {
		netList.addMouseListener(l);
	}

	public void showBtnPanel(String paneName) {
		btnCard.show(jtbBtns, paneName);
	}

	/**
	 * Returns the netList.
	 * @return JList
	 */
	public JList getNetList() {
		return netList;
	}

	public void changeToTreeView(JTree tree) {
		netScroll.setViewportView(tree);
	}

	public void changeToListView() {
		netScroll.setViewportView(netList);
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.gui.IPersistenceAwareWindow#getSupportedFileExtensions()
//	 */
//	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
//		// The supported file is a folder...
//		return SUPPORTED_FILE_EXTENSIONS;
//	}
//
//	/* (non-Javadoc)
//	 * @see unbbayes.gui.IPersistenceAwareWindow#getSupportedFilesDescription()
//	 */
//	public String getSupportedFilesDescription(boolean isLoadOnly) {
//		return resource.getString("netFileFilterSaveMSBN");
//	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSavingMessage()
	 */
	public String getSavingMessage() {
		return resource.getString("saveTitle");
	}

	

	/**
	 * @return the controller
	 */
	public MSBNController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(MSBNController controller) {
		this.controller = controller;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getInternalFrame()
	 */
	public JInternalFrame getInternalFrame() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getIO()
	 */
	public BaseIO getIO() {
		return this.getController().getMsbnIO();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getPersistingGraph()
	 */
	public Graph getPersistingGraph() {
		return this.getMSNet();
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return this.resource.getString("MSBNModuleName");
	}

	/**
	 * Opens a new desktop window into currently used java desktop
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		Graph g = null;
		try {
			g = this.getIO().load(file);
		} catch (LoadException e) {
			new UBIOException(e);
		}
		
		
		MSBNController controller = null;
		try {
			controller = new MSBNController((SingleAgentMSBN)g);
		} catch (Exception e) {
			throw new RuntimeException(this.resource.getString("unsupportedGraphFormat"),e);
		}
		
		this.dispose();
		
		return (MSBNWindow)controller.getPanel();
	}
	
	
	
	
}