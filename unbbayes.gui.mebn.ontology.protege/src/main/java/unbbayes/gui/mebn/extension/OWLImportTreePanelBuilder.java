/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveImport;

import unbbayes.controller.FileHistoryController;
import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;

/**
 * 
 * This is a factory/builder for a panel which allows user to edit the OWL import hierarchy
 * @author Shou Matsumoto
 *
 */
public class OWLImportTreePanelBuilder extends JPanel implements IMEBNEditionPanelBuilder {
	
//	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
//						unbbayes.gui.resources.GuiResources.class.getName(),
//						Locale.getDefault(),
//						OWLImportTreePanelBuilder.class.getClassLoader()
//					);

	private static final long serialVersionUID = -6472649875208064488L;

	public static final String DEFAULT_IMPORT_FILE_TOOLTIPTEXT = "Add new OWL import by choosing a file. The new import will be included as a child of selected ontology.";

	public static final String DEFAULT_IMPORT_URI_TOOLTIPTEXT = "Add new OWL import from URI. The new import will be included as a child of selected ontology.";

	public static final String DEFAULT_REMOVE_IMPORT_TOOLTIPTEXT = "Remove the selected OWL import. The ontology will not be actually unloaded until the program is closed.";
	
	private MultiEntityBayesianNetwork mebn;
	private IMEBNMediator mediator;

	private OntologyNode rootOntologyNode;

	private JTree ontologyJTree;

	private JScrollPane mainScrollPane;

	private JPopupMenu popupMenu;

	private JToolBar toolBar;

	private JButton addImportFromFileButton;

	private JButton addImportFromURIButton;

	private JButton deleteImportButton;

	private JMenuItem addImportFromFileMenuItem;

	private JMenuItem addImportMenuItem;

	private JMenuItem removeImportMenuItem;

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		this.setMEBN(mebn);
		this.setMediator(mediator);
		this.initComponents();
		if (getRootOntologyNode() == null) {
			return null;
		}
		return this;
	}
	
	/**
	 * A node in the 
	 * @author Shou Matsumoto
	 *
	 */
	protected class OntologyNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -8215880906395303237L;
		
		private boolean isLoaded = false;
		
		/**
		 * @param ontology the (imported) ontology that this node represents.
		 */
		public OntologyNode(OWLOntology ontology) {
			this.setOntology(ontology);
		}
		

		/**
		 * Just delegates to {@link #getUserObject()}
		 * @return the (imported) ontology that this node represents. If root, then it is the ontology currently being edited.
		 */
		public OWLOntology getOntology() {
			Object userObject = getUserObject();
			if (userObject != null
					&& (userObject instanceof OWLOntology)) {
				return (OWLOntology)userObject;
			}
			return null;
		}

		/**
		 * Just delegates to {@link #setUserObject(Object)}
		 * @param ontology : the (imported) ontology that this node represents. If root, then it is the ontology currently being edited.
		 */
		public void setOntology(OWLOntology ontology) {
			this.setUserObject(ontology);
		}


		/* (non-Javadoc)
		 * @see javax.swing.tree.DefaultMutableTreeNode#toString()
		 */
		public String toString() {
			OWLOntology ontology = getOntology();
			if (ontology != null) {
				return ontology.getOntologyID().getOntologyIRI().toURI().toString();
			}
			return super.toString();
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof OntologyNode) {
				OntologyNode ontologyNodeToCompare = (OntologyNode) obj;
				if (this.getOntology() == null) {
					return ontologyNodeToCompare.getOntology() == null;
				}
				if (ontologyNodeToCompare.getOntology() == null) {
					return false;
				}
				return this.getOntology().equals(((OntologyNode)obj).getOntology());
			}
			return super.equals(obj);
		}
		

		/**
		 * This is used by a listener in {@link JTree#getTreeWillExpandListeners()} at {@link OWLImportTreePanelBuilder#getOntologyJTree()} 
		 * (filled by {@link OWLImportTreePanelBuilder#initComponents()})
		 * in order to lazily load child nodes.
		 * @param treeModel 
		 * @see #getOntology()
		 * @see OWLOntology#getDirectImports()
		 * @see #isLoaded()
		 * @see #setLoaded(boolean)
		 * @see OWLImportTreePanelBuilder#getOntologyJTree()
		 */
		public void lazyLoadChildren(DefaultTreeModel treeModel) {
			
			if (this.isLoaded()) {
				// this node was already lazy-loaded.
				return;
			}
			
			// lazy-load the children of current node
			Set<OWLOntology> directOWLImports = this.getOntology().getOWLOntologyManager().getDirectImports(this.getOntology());
//			if (directOWLImports == null || directOWLImports.isEmpty()) {
//				// indicate that this node does not have children (so that renderer will not put a node expander anymore)
//				this.setAllowsChildren(false);
//			} else {
				for (OWLOntology owlImport : directOWLImports) {
					OntologyNode newChild = new OntologyNode(owlImport);
					newChild.setAllowsChildren(true);	// for lazy-loading, mark this node as if it has children, even though there are no children yet.
					treeModel.insertNodeInto(newChild, this, this.getChildCount());
				}
//			}
		
			// mark this node as "loaded"
			this.setLoaded(true);
		}


		/**
		 * @return : true if this node was marked that lazy-load was already performed. 
		 */
		public boolean isLoaded() {
			return isLoaded;
		}


		/**
		 * @param isLoaded the isLoaded to set
		 */
		public void setLoaded(boolean isLoaded) {
			if (this.isLoaded && !isLoaded) {
				// if it was loaded once, but we are setting it to "not loaded", then propagate to children (set children to "not loaded" too)
				for (int i = 0; i < getChildCount(); i++) {
					((OntologyNode)getChildAt(i)).setLoaded(false);
				}
			}
			this.isLoaded = isLoaded;
		}

		/**
		 * Reloads the children of this node.
		 * @param treeModel : {@link DefaultTreeModel#removeNodeFromParent(MutableTreeNode)} will be used to update tree structure.
		 * @see OWLImportTreePanelBuilder#getOntologyJTree()
		 * @see OWLImportTreePanelBuilder#doAddImportFromFile()
		 * @see OWLImportTreePanelBuilder#doAddImportFromURI()
		 * @see OWLImportTreePanelBuilder#doRemoveSelectedImport()
		 * @see #setLoaded(boolean)
		 * @see #lazyLoadChildren(DefaultTreeModel)
		 */
		public void reloadChildren(DefaultTreeModel treeModel) {
			
			// force/indicate that this node is not loaded yet
			this.setLoaded(false);

			// clean children
			for (int i = 0; i < getChildCount(); i++) {
				OntologyNode childToRemove = (OntologyNode) getChildAt(i);
				treeModel.removeNodeFromParent(childToRemove);
			}
			this.removeAllChildren();
			treeModel.nodeStructureChanged(this);
			
			// load children using the default loading method
			this.lazyLoadChildren(treeModel);
		}


	}

	/**
	 * Resets and initializes components and listeners of this panel
	 */
	public void initComponents() {
		// clear all components
		for (Component component : this.getComponents()) {
			this.remove(component);
		}
		
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		
		
		// init root ontology node (a node representing the prowl 2 project currently being edited)
		setRootOntologyNode(this.buildRootOntologyNode(getMEBN(), getMediator()));
		if (getRootOntologyNode() == null) {
			// there is nothing to render
			return;
		}
		
		// set the new node as the root of JTree
		// instantiate tree. Force JTree to use default tree model (so that we guarantee that method insertNodeInto is implemented)
		DefaultTreeModel treeModel = new DefaultTreeModel(getRootOntologyNode());
		// force it to check the #getAllowsChildren of the node (this is necessary for lazy evaluation, to show that a node can have children even though there are no actual children yet)
		treeModel.setAsksAllowsChildren(true);	
		setOntologyJTree(new JTree(treeModel));
		
		// load direct imports of root node
		getRootOntologyNode().lazyLoadChildren(treeModel);
		
		// just changing the background color
		getOntologyJTree().setBackground(Color.WHITE);
		// Do not allow users to edit the label of the nodes directly
		// TODO allow users to edit the content of the tree directly
		getOntologyJTree().setEditable(false);
		// only allow 1 node to be selected at a given time
		getOntologyJTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// force jtree to show the expansion handle (in some look'n'feel, it's the "+" sign to expand a node)
		getOntologyJTree().setShowsRootHandles(true);
		
		// change the way the tree nodes are displayed (e.g. icons)
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		getOntologyJTree().setCellRenderer(renderer);
		
		// set the listener of this JTree for lazy loading of children 
		getOntologyJTree().addTreeWillExpandListener(new TreeWillExpandListener() {
			/** Do nothing */
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
			/** Lazy-loading of imported ontologies (lazy loading of child nodes) */
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				// extract the node being expanded
				OntologyNode parentNode = ((OntologyNode)event.getPath().getLastPathComponent());
				try {
					parentNode.lazyLoadChildren((DefaultTreeModel) getOntologyJTree().getModel());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage() + " \n\n" + getStackTraceString(e), "Error loading child imports", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		
		// add tool bar with add/remove buttons
		toolBar = new JToolBar();
		toolBar.setBackground(Color.WHITE);
		toolBar.setToolTipText("Edit imports.");
		this.add(toolBar, BorderLayout.NORTH);
		
		
		// add import(from file/URI) and delete buttons to toolbar
		addImportFromFileButton = new JButton(" Import file ",IconController.getInstance().getOpenIcon());
		addImportFromFileButton.setToolTipText(DEFAULT_IMPORT_FILE_TOOLTIPTEXT);
		addImportFromFileButton.setBackground(Color.WHITE);
		toolBar.add(addImportFromFileButton);
		addImportFromFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddImportFromFile();
			}
		});
		
		addImportFromURIButton = new JButton(" Import URI ",IconController.getInstance().getTxtFileIcon());
		addImportFromURIButton.setToolTipText(DEFAULT_IMPORT_URI_TOOLTIPTEXT);
		addImportFromURIButton.setBackground(Color.WHITE);
		toolBar.add(addImportFromURIButton);
		addImportFromURIButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddImportFromURI();
			}
		});
		
		deleteImportButton = new JButton(" Remove selected ",IconController.getInstance().getAddFolderIcon());
		deleteImportButton.setToolTipText(DEFAULT_REMOVE_IMPORT_TOOLTIPTEXT);
		deleteImportButton.setBackground(Color.WHITE);
		toolBar.add(deleteImportButton);
		deleteImportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRemoveSelectedImport();
			}
		});
		
		
		
		// add mouse listener which will show add/remove/edit menu when right-clicked
		// initialize popup menu
		this.initPopupMenu();
		// add a listener to show popup menu
		getOntologyJTree().addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					// this was a right-click
					getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
		
		JScrollPane jScrollPane = new JScrollPane(getOntologyJTree());
		jScrollPane.setBackground(Color.WHITE);
		setMainScrollPane(jScrollPane);
		getMainScrollPane().setBorder(new TitledBorder("Hierarchy of OWL import declarations"));
		
		this.add(getMainScrollPane(), BorderLayout.CENTER);
		
		
	}
	
	/**
	 * Simply converts a stack trace to string.
	 * @param e : exception to get {@link Exception#getStackTrace()} from.
	 * @return simply a string which would be printed by calling {@link Exception#printStackTrace()}.
	 */
	private String getStackTraceString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return pw.toString();
	}


	public void initPopupMenu() {
		
		// TODO use a resource file for localization
		
		popupMenu = new JPopupMenu();
		
		JMenu submenu = new JMenu("New import");
		submenu.setToolTipText("Add new OWL import as child of selected ontology");
		popupMenu.add(submenu);
		
		addImportFromFileMenuItem = new JMenuItem("From file");
		addImportFromFileMenuItem.setToolTipText(DEFAULT_IMPORT_FILE_TOOLTIPTEXT);
		
		addImportFromFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddImportFromFile();
			}
		});
		
		submenu.add(addImportFromFileMenuItem);
		
		addImportMenuItem = new JMenuItem("Specify the URI");
		addImportMenuItem.setToolTipText(DEFAULT_IMPORT_URI_TOOLTIPTEXT);
		
		addImportMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddImportFromURI();
			}
		});
		
		submenu.add(addImportMenuItem);
		
		
		
		removeImportMenuItem = new JMenuItem("Remove currently selected import");
		removeImportMenuItem.setToolTipText(DEFAULT_REMOVE_IMPORT_TOOLTIPTEXT);
		
		removeImportMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRemoveSelectedImport();
			}
		});
		
		popupMenu.add(removeImportMenuItem);
		
		// TODO replace currently selected import
		
	}
	
	/**
	 * Handles GUI request for removing an OWL import statement, currently selected at {@link #getOntologyJTree()}.
	 * @see #getOntologyJTree()
	 * @see #initPopupMenu()
	 * @see #getPopupMenu()
	 */
	protected void doRemoveSelectedImport(){
		// extract the selected ontology node
		TreePath selectionPath = getOntologyJTree().getSelectionPath();
		if (selectionPath == null) {
			JOptionPane.showMessageDialog(null, "Please, select the ontology where you want to add the import statement.", "No ontology was selected", JOptionPane.WARNING_MESSAGE);
			return;
		}
		OntologyNode selectedNode = (OntologyNode) selectionPath.getLastPathComponent();
		// there must be some selected node
		if (selectedNode == null) {
			JOptionPane.showMessageDialog(null, "Please, select the ontology where you want to add the import statement.", "No ontology was selected", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// the selected node must not be a root
		if (selectionPath.getPathCount() <= 1) {
			JOptionPane.showMessageDialog(null, "The root (main) ontology cannot be removed from the list of OWL imports.", "Selected node is not an OWL import", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// extract the parent of the selected ontology node. 
		// At this point, we know the selected node is not a root (because we did such check already).
		OntologyNode parentNode = (OntologyNode) selectionPath.getPathComponent(selectionPath.getPathCount()-2);
		
		// extract the ontology of the parent node
		OWLOntology parentOntology = parentNode.getOntology();
		
		// extract the import declaration (parent imports the child/selected)
		OWLImportsDeclaration owlImportsDeclaration = parentOntology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(selectedNode.getOntology().getOntologyID().getOntologyIRI());
		
		// send request to remove the import declaration.
		// Note: this will not actually save the ontology file
		parentOntology.getOWLOntologyManager().applyChange(new RemoveImport(parentOntology, owlImportsDeclaration));
		

		// reload children of parent node (because we just removed a child)
		parentNode.reloadChildren((DefaultTreeModel) getOntologyJTree().getModel());
		
	}
	
	/**
	 * Handles GUI request for making an OWL import statement from user-specified URI.
	 * This method basically requests user for a text input, and then calls {@link #addOWLImportDeclarationToOntology(OWLOntology, URI)}
	 * @see #getOntologyJTree()
	 * @see #initPopupMenu()
	 * @see #getPopupMenu()
	 */
	protected void doAddImportFromURI () {
		if (getOntologyJTree().getSelectionPath() == null) {
			JOptionPane.showMessageDialog(null, "Please, select the ontology where you want to add the import statement.", "No ontology was selected", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// the ontology to include import
		OntologyNode selectedNode = (OntologyNode) getOntologyJTree().getSelectionPath().getLastPathComponent();
		if (selectedNode == null) {
			JOptionPane.showMessageDialog(null, "Please, select the ontology where you want to add the import statement.", "No ontology was selected", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// extract the ontology associated with the selected node
		OWLOntology ontology = selectedNode.getOntology();
		if (ontology == null) {
			JOptionPane.showMessageDialog(null, "Selected node is not associated with any ontology.", "Invalid Node", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// get uri from user input
		String input = JOptionPane.showInputDialog("Provide the URI");
		if (input == null || input.isEmpty()) {
			return;
		}
		// create the uri (which will be used later to create a IRI -- required by OWL API)
		URI uri = null;
		try {
			// this should guarantee that the IRI is also a valid URI
			uri = URI.create(input);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\n\n" + getStackTraceString(e), "Error parsing the URI", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// actually import the ontology
		try {
			this.addOWLImportDeclarationToOntology(ontology, uri);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\n\n" + getStackTraceString(e), "Error loading ontology", JOptionPane.WARNING_MESSAGE);
		}
		

		// reload children of this node
		selectedNode.reloadChildren((DefaultTreeModel) getOntologyJTree().getModel());
		
	}
	
	/**
	 * Handles GUI request for making an OWL import statement from a file.
	 * This method basically shows a file chooser to the user, and then calls {@link #addOWLImportDeclarationToOntology(OWLOntology, URI)}
	 * @see #getOntologyJTree()
	 * @see #initPopupMenu()
	 * @see #getPopupMenu()
	 */
	protected void doAddImportFromFile(){
		if (getOntologyJTree().getSelectionPath() == null) {
			JOptionPane.showMessageDialog(null, "Please, select the ontology where you want to add the import statement.", "No ontology was selected", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// the ontology to include import
		OntologyNode selectedNode = (OntologyNode) getOntologyJTree().getSelectionPath().getLastPathComponent();
		// extract the ontology associated with the selected node
		OWLOntology ontology = selectedNode.getOntology();
		if (ontology == null) {
			JOptionPane.showMessageDialog(null, "Selected node is not associated with any ontology.", "Invalid Node", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// show file chooser and make user to choose a file
		File file = chooseFile();
		if (file == null) {
			return;
		}
		// the following seems to be redundant, because the file chooser is configured to select only files, not directories
//		if (file.isDirectory()) {
//			JOptionPane.showMessageDialog(null, "A directory cannot be used as an OWL input.", "Invalid ontology", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
		
		// create the uri (which will be used later to create a IRI -- required by OWL API)
		URI uri = file.toURI();
		
		// actually import the ontology
		try {
			this.addOWLImportDeclarationToOntology(ontology, uri);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\n\n" + getStackTraceString(e), "Error loading ontology", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// reload children of this node
		selectedNode.reloadChildren((DefaultTreeModel) getOntologyJTree().getModel());
		
	}

	/**
	 * Show a JFileChooser to select a file.
	 * The file will be filtered (only OWL file will be listed)
	 * @return the selected file, or null if none was selected
	 */
	public File chooseFile() {
		
		// instantiate a pop-up file chooser. Show last seen directory by default.
		JFileChooser chooser = new JFileChooser(FileHistoryController.getInstance().getCurrentDirectory());
		
		// don't allow multiple files to be imported. 
		// TODO allow multiple OWL import from file
		chooser.setMultiSelectionEnabled(false);	
		// make sure only files are selected
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// filter by OWL extension
		String filterMessage = "OWL files";
		String[] extensions = {"owl"};
		chooser.addChoosableFileFilter(new SimpleFileFilter(extensions, filterMessage));
		
		// actually show the chooser
		int option = chooser.showOpenDialog(this);
		
		// check if user selected "OK"
		if (option == JFileChooser.APPROVE_OPTION) {
			FileHistoryController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
			return chooser.getSelectedFile();
		}
		
		// return nothing if user cancelled
		return null;
	}
	
	/**
	 * Creates an OWL import assertion and inserts it to a given ontology.
	 * @param ontology : ontology to create an OWL import declaration
	 * @param uri : the URI of the ontology to be imported
	 * @throws OWLOntologyCreationException 
	 */
	public void addOWLImportDeclarationToOntology(OWLOntology ontology, URI uri) throws OWLOntologyCreationException {
		// TODO move this code to some facade or other controller class
		
		// convert the URI to IRI (type required by OWL API)
		IRI ontologyIRI = IRI.create(uri);
		
		// call the ontology manager for the ontology identified by the iri
		OWLOntology ontologyToImport = ontology.getOWLOntologyManager().getOntology(ontologyIRI);	
		
		// check if ontology manager has loaded the ontology already.
		if (ontologyToImport == null) {
			// manager did not load this ontology yet. Load it now.
			try {
				Debug.println(getClass(), "Loading ontology as document: " + ontologyIRI);
				ontologyToImport = ontology.getOWLOntologyManager().loadOntologyFromOntologyDocument(ontologyIRI);
			} catch (OWLOntologyAlreadyExistsException e){
				// The ontology was already loaded. 
				// It was not detected previously, because URI of ontology may not be equal to its actual web location (and perhaps the user has specified the web location instead)
				// retrieve its actual URI/IRI (different from web location)
				ontologyIRI = e.getOntologyID().getOntologyIRI();
				// retrieve the ontology from it's actual URI
				ontologyToImport = ontology.getOWLOntologyManager().getOntology(ontologyIRI);	
				if (ontologyToImport == null) {
					// try to load from document iri
					ontologyIRI = e.getOntologyID().getDefaultDocumentIRI();
					ontologyToImport = ontology.getOWLOntologyManager().getOntology(ontologyIRI);	
				}
				if (ontologyToImport == null) {
					// try to load from ontology id
					ontologyToImport = ontology.getOWLOntologyManager().getOntology(e.getOntologyID());	
				}
			} catch (Exception e) {
				// ignore exception for now, because we'll try second attempt
				e.printStackTrace();
			}
			if (ontologyToImport == null) {
				Debug.println(getClass(), "Second attempt to load ontology: " + ontologyIRI);
				// trying to load it from a different method...
				ontologyToImport = ontology.getOWLOntologyManager().loadOntology(ontologyIRI);	
			}
		}
		
		// if manager did not load the ontology yet, then there was some problem...
		if (ontologyToImport == null) {
			throw new OWLOntologyCreationException("Failed to load ontology: " + ontologyIRI);
		}
		
		// create the import declaration. I'm using the IRI from ontology ID, just to make sure the ontology we just loaded is the one we will be importing
		OWLImportsDeclaration importDeclaraton = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(ontologyToImport.getOntologyID().getOntologyIRI()); 
		// commit the import declaration (but this supposedly won't actually save the file)
		ontology.getOWLOntologyManager().applyChange(new AddImport(ontology, importDeclaraton));
		
		
		//TODO changes in ontology should be reflected to other panels.
	}

	/**
	 * @param mediator: the mebn controller. This is likely to be referenced by subclasses if this method needs more information than the mebn.
	 * @param mebn  : the MEBN project object whose ontology will be extracted from
	 * @return a instance of {@link OntologyNode} (shall be set as the root node in {@link #getOntologyJTree()}).
	 */
	protected OntologyNode buildRootOntologyNode(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		if (mebn != null && mebn.getStorageImplementor() != null && (mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
			IOWLAPIStorageImplementorDecorator storageImplementor = (IOWLAPIStorageImplementorDecorator) mebn.getStorageImplementor();
			if (storageImplementor.getAdaptee() != null) {
				OntologyNode root = new OntologyNode(storageImplementor.getAdaptee());
				root.setAllowsChildren(true);	// indicate that this node may have children, even though they may not be present yet (this is for lazy loading).
				return root;
			}
		}
		return null;
	}

	/**
	 * @return the mebn
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}

	/**
	 * @param mebn the mebn to set
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(IMEBNMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @return the rootOntologyNode
	 */
	public OntologyNode getRootOntologyNode() {
		return rootOntologyNode;
	}

	/**
	 * @param rootOntologyNode the rootOntologyNode to set
	 */
	public void setRootOntologyNode(OntologyNode rootOntologyNode) {
		this.rootOntologyNode = rootOntologyNode;
	}

	/**
	 * @return the ontologyJTree
	 */
	public JTree getOntologyJTree() {
		return ontologyJTree;
	}

	/**
	 * @param ontologyJTree the ontologyJTree to set
	 */
	public void setOntologyJTree(JTree ontologyJTree) {
		this.ontologyJTree = ontologyJTree;
	}

	/**
	 * @return the mainScrollPane
	 */
	public JScrollPane getMainScrollPane() {
		return mainScrollPane;
	}

	/**
	 * @param mainScrollPane the mainScrollPane to set
	 */
	public void setMainScrollPane(JScrollPane mainScrollPane) {
		this.mainScrollPane = mainScrollPane;
	}

	/**
	 * @return the popupMenu
	 */
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	/**
	 * @param popupMenu the popupMenu to set
	 */
	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	/**
	 * @return the toolBar
	 */
	public JToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * @param toolBar the toolBar to set
	 */
	public void setToolBar(JToolBar toolBar) {
		this.toolBar = toolBar;
	}

	/**
	 * @return the addImportFromFileButton
	 */
	public JButton getAddImportFromFileButton() {
		return addImportFromFileButton;
	}

	/**
	 * @param addImportFromFileButton the addImportFromFileButton to set
	 */
	public void setAddImportFromFileButton(JButton addImportFromFileButton) {
		this.addImportFromFileButton = addImportFromFileButton;
	}

	/**
	 * @return the addImportFromURIButton
	 */
	public JButton getAddImportFromURIButton() {
		return this.addImportFromURIButton;
	}

	/**
	 * @param addImportFromURIButton the addImportFromURIButton to set
	 */
	public void setAddImportFromURIButton(JButton addImportFromURIButton) {
		this.addImportFromURIButton = addImportFromURIButton;
	}

	/**
	 * @return the deleteImportButton
	 */
	public JButton getDeleteImportButton() {
		return this.deleteImportButton;
	}

	/**
	 * @param deleteImportButton the deleteImportButton to set
	 */
	public void setDeleteImportButton(JButton deleteImportButton) {
		this.deleteImportButton = deleteImportButton;
	}

	/**
	 * @return the addImportFromFileMenuItem
	 */
	public JMenuItem getAddImportFromFileMenuItem() {
		return this.addImportFromFileMenuItem;
	}

	/**
	 * @param addImportFromFileMenuItem the addImportFromFileMenuItem to set
	 */
	public void setAddImportFromFileMenuItem(JMenuItem addImportFromFileMenuItem) {
		this.addImportFromFileMenuItem = addImportFromFileMenuItem;
	}

	/**
	 * @return the addImportMenuItem
	 */
	public JMenuItem getAddImportMenuItem() {
		return this.addImportMenuItem;
	}

	/**
	 * @param addImportMenuItem the addImportMenuItem to set
	 */
	public void setAddImportMenuItem(JMenuItem addImportMenuItem) {
		this.addImportMenuItem = addImportMenuItem;
	}

	/**
	 * @return the removeImportMenuItem
	 */
	public JMenuItem getRemoveImportMenuItem() {
		return this.removeImportMenuItem;
	}

	/**
	 * @param removeImportMenuItem the removeImportMenuItem to set
	 */
	public void setRemoveImportMenuItem(JMenuItem removeImportMenuItem) {
		this.removeImportMenuItem = removeImportMenuItem;
	}

}
