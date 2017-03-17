/**
 * 
 */
package unbbayes.gui.mebn.cpt;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.SizeGripIcon;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.GUIUtils;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.ResourceController;

/**
 * This is a {@link CPTFrame} (the dialog for editing lpd scripts in UnBBayes-MEBN)
 * which uses {@link RSyntaxTextArea} for text-highlighting, auto-complete, undo/redo, etc.
 * @author Shou Matsumoto
 */
public class RSyntaxTextAreaCPTFrame extends CPTFrame implements SearchListener {


	private static final long serialVersionUID = -2353246954340399648L;
	private JButton btnCompile;
	private JTextField txtPosition;
	private RSyntaxTextArea textArea;
	

	private CollapsibleSectionPanel collapsibleSectionPanel;
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private FindToolBar findToolBar;
	private ReplaceToolBar replaceToolBar;
	private StatusBar statusBar;
	

	private static ResourceBundle resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.resources.Resources.class.getName());

	/**
	 * Default constructor from superclass
	 * @param mediator
	 * @param residentNode
	 */
	public RSyntaxTextAreaCPTFrame(IMEBNMediator mediator,
			ResidentNode residentNode) {
		super((MEBNController) mediator, residentNode);
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.cpt.CPTFrame#initComponents()
	 */
	protected void initComponents() {
		initSearchDialogs();

		textArea = new RSyntaxTextArea(25, 85);
	    textArea.setText(getResidentNode().getTableFunction());
	    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);	// TODO use custom syntax style
	    
	    // some customization
	    textArea.setAutoIndentEnabled(true);
	    textArea.setAnimateBracketMatching(true);
	    textArea.setAutoscrolls(true);
	    textArea.setAntiAliasingEnabled(true);
	    textArea.setCloseCurlyBraces(true);
	    textArea.setCodeFoldingEnabled(false);
	    textArea.setDragEnabled(true);
	    textArea.setFadeCurrentLineHighlight(true);
	    textArea.setHyperlinksEnabled(false);
	    textArea.setLineWrap(false);
	    textArea.setMarkOccurrences(true);
	    textArea.setPaintMatchedBracketPair(true);
	    textArea.setRoundedSelectionEdges(true);
	    textArea.setFont(textArea.getFont().deriveFont(15f));;
	    
	    // init autocompletion feature
	    CompletionProvider provider = createCompletionProvider();
	    AutoCompletion ac = new AutoCompletion(provider);
	    ac.install(textArea);	// enable autocompletion for this text area
	    
	    collapsibleSectionPanel = new CollapsibleSectionPanel();
	    
	    RTextScrollPane scrollPane = new RTextScrollPane(textArea);
	    collapsibleSectionPanel.add(scrollPane);
	    
	    JPanel contentPane = new JPanel(new BorderLayout());
//	    contentPane.add(sp, BorderLayout.CENTER);
	    contentPane.add(collapsibleSectionPanel, BorderLayout.CENTER);
//	    contentPane.add(buildMainButtonsPanel(), BorderLayout.SOUTH);
	    
	    setJMenuBar(createMenuBar());
	    
	    ErrorStrip errorStrip = new ErrorStrip(textArea);
	    contentPane.add(errorStrip, BorderLayout.LINE_END);
	    
	    statusBar = new StatusBar();
	    contentPane.add(statusBar, BorderLayout.SOUTH);
	    
		setContentPane(contentPane);
    	setLocation(GUIUtils.getCenterPositionForComponent(640,200));
    	pack(); 
    	setVisible(true); 
    	
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    	
	}
	
	
	protected void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}
	
	protected JMenuBar createMenuBar() {

		// TODO use resources
		
		JMenuBar mb = new JMenuBar();
		
		JMenu menu;
		
		menu = new JMenu("File");
		AbstractAction saveAction = new AbstractAction(resource.getString("saveCPT"), IconController.getInstance().getSaveIcon()) {
			public void actionPerformed(ActionEvent e) {
		    	getMebnController().saveCPT(getResidentNode(), getTextArea().getText()); 
		    	getStatusBar().setLabel("Last saved at " + new Date());
			}
		};
		saveAction.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, getToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(saveAction));
		
		menu.add(new JMenuItem(new AbstractAction("Exit without saving") {
			public void actionPerformed(ActionEvent e) {
				// dispose in controller
		        getMebnController().closeCPTDialog(getResidentNode()); 
			}
		}));
		mb.add(menu);
		
		
		menu = new JMenu("Search");
		menu.add(new JMenuItem(new ShowFindDialogAction()));
		menu.add(new JMenuItem(new ShowReplaceDialogAction()));
		menu.add(new JMenuItem(new GoToLineAction()));
		menu.addSeparator();

		int ctrl = getToolkit().getMenuShortcutKeyMask();
		int shift = InputEvent.SHIFT_MASK;
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl|shift);
		Action a = getCollapsibleSectionPanel().addBottomComponent(ks, findToolBar);
		a.putValue(Action.NAME, "Show Find Search Bar");
		menu.add(new JMenuItem(a));
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl|shift);
		a = getCollapsibleSectionPanel().addBottomComponent(ks, replaceToolBar);
		a.putValue(Action.NAME, "Show Replace Search Bar");
		menu.add(new JMenuItem(a));

		mb.add(menu);

		AbstractAction compileAction = new AbstractAction(resource.getString("compileCPT"), IconController.getInstance().getCompileIcon()) {
			public void actionPerformed(ActionEvent e) {
				compile();
			}
		};
		compileAction.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, getToolkit().getMenuShortcutKeyMask()));
		JMenuItem item = new JMenuItem(compileAction);
		item.setToolTipText(resource.getString("compileCPTTip"));
		menu = new JMenu(resource.getString("toolsMenu"));
		menu.add(item);
		mb.add(menu);
		

		return mb;

	}
	
	public String getSelectedText() {
		return textArea.getSelectedText();
	}

	public void searchEvent(SearchEvent e) {
		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result = null;

		switch (type) {
			default: // Prevent FindBugs warning later
			case MARK_ALL:
				result = SearchEngine.markAll(textArea, context);
				break;
			case FIND:
				result = SearchEngine.find(textArea, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE:
				result = SearchEngine.replace(textArea, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE_ALL:
				result = SearchEngine.replaceAll(textArea, context);
				JOptionPane.showMessageDialog(null, result.getCount() +
						" occurrences replaced.");
				break;
		}

		String text = null;
		if (result.wasFound()) {
			text = "Text found; occurrences marked: " + result.getMarkedCount();
		}
		else if (type==SearchEvent.Type.MARK_ALL) {
			if (result.getMarkedCount()>0) {
				text = "Occurrences marked: " + result.getMarkedCount();
			}
			else {
				text = "";
			}
		}
		else {
			text = "Text not found";
		}
		statusBar.setLabel(text);

	
	}
	

	
	/**
	 * Creates our Find and Replace dialogs.
	 */
	public void initSearchDialogs() {

		findDialog = new FindDialog(this, this);
		
		replaceDialog = new ReplaceDialog(this, this);

		// This ties the properties of the two dialogs together (match case,
		// regex, etc.).
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);

		// Create tool bars and tie their search contexts together also.
		findToolBar = new FindToolBar(this);
		findToolBar.setSearchContext(context);
		replaceToolBar = new ReplaceToolBar(this);
		replaceToolBar.setSearchContext(context);

	}



//	/**
//	 * Panel containing buttons like the "compile" button
//	 * @return
//	 */
//	protected Component buildMainButtonsPanel() {
//		
//		JPanel buttonPanel = new JPanel();
//
//		Font font = new Font("Serif", Font.PLAIN, 12); 
//
//		btnCompile = new JButton(resource.getString("compileCPT"));
//		btnCompile.setFont(font); 
//		btnCompile.setToolTipText(resource.getString("compileCPTTip")); 
//
//		
//		txtPosition = new JTextField(); 
//		txtPosition.setEditable(false); 
//		txtPosition.setForeground(Color.black); 
//		txtPosition.setBackground(Color.WHITE); 
//		txtPosition.setAlignmentX(JTextField.CENTER_ALIGNMENT); 
//		
//		setLayout(new GridLayout(1,2)); 
//		
//		//Fourth row
//		buttonPanel.add(txtPosition); 
//		buttonPanel.add(btnCompile); 
//
//		return buttonPanel;
//	}

	/**
	 * @return a CompletionProvider which knows all possible completions, and
	 * analyzes contents of text area at position to determine what completion choices should be presented.
	 */
	public CompletionProvider createCompletionProvider() {
		
		// default implementation of autocompletion (with no semantic analysis)
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		
		// set up auto-completion words from keywords that the compiler declares
		
		// extract keywords from compiler
		ICompiler compiler = getResidentNode().getCompiler();
		Collection<String> keyWords = compiler.getKeyWords();
		if (keyWords != null) {
			// add all keywords declared by compiler
			for (String keyword : keyWords) {
				provider.addCompletion(new BasicCompletion(provider, keyword));
			}
		}
		
		
	    // also add some shorthand completion (similar to sysout or foreach in java+eclipse)
	    // extract shorthands from compiler
		Collection<Entry<String, String>> shorthandKeywords = compiler.getShorthandKeywords();
		if (shorthandKeywords != null) {
			// add all shorthand completions declared by compiler
			for (Entry<String, String> entry : shorthandKeywords) {
				provider.addCompletion(new ShorthandCompletion(provider, 
						entry.getKey(),
						entry.getValue(), 
						entry.getValue()));
			}
		}
	    
	    return provider;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.cpt.CPTFrame#initListeners()
	 */
	protected void initListeners() {
		
		// listener regarding the operation of closing this frame
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {
		    	// save on close
		    	getMebnController().saveCPT(getResidentNode(), getTextArea().getText()); 
		    	// dispose in controller
		        getMebnController().closeCPTDialog(getResidentNode()); 
		    }
		});
		
		// listener for the compile button
		if (getBtnCompile() != null) {
			getBtnCompile().addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent e) {
					compile();
				}
				
			}); 
		}
	}
	
	public void compile() {
		ICompiler compiler = getResidentNode().getCompiler();
		// use getResidentNode().setToLimitQuantityOfParentsInstances(true); to enable chain
		try {
			compiler.init(textArea.getText());
			compiler.parse(); 
			getStatusBar().setLabel(resource.getString("CptCompileOK"));
//			JOptionPane.showMessageDialog(getMebnController().getCPTEditionFrame(getResidentNode()), 
//					resource.getString("CptCompileOK"), resource.getString("sucess"), 
//					JOptionPane.INFORMATION_MESSAGE);
		} catch (MEBNException e1) {
			getStatusBar().setLabel("[" + resource.getString("error") + "] " + e1.getMessage() + " > " + compiler.getIndex() + " <");
			getTextArea().select(compiler.getIndex()-1, compiler.getIndex());
			JOptionPane.showMessageDialog(getMebnController().getCPTDialog(getResidentNode()), 
					e1.getMessage() + " > " + compiler.getIndex() + " <", resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception exc) {
			// this is an unknown exception...
			exc.printStackTrace();
			JOptionPane.showMessageDialog(getMebnController().getCPTDialog(getResidentNode()), 
					exc.getMessage(), resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE);
		}
		
	
	}

	/**
	 * @return the btnCompile
	 */
	public JButton getBtnCompile() {
		return btnCompile;
	}

	/**
	 * @param btnCompile the btnCompile to set
	 */
	public void setBtnCompile(JButton btnCompile) {
		this.btnCompile = btnCompile;
	}

	/**
	 * @return the txtPosition
	 */
	public JTextField getTxtPosition() {
		return txtPosition;
	}

	/**
	 * @param txtPosition the txtPosition to set
	 */
	public void setTxtPosition(JTextField txtPosition) {
		this.txtPosition = txtPosition;
	}

	/**
	 * @return the textArea
	 */
	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	/**
	 * @param textArea the textArea to set
	 */
	public void setTextArea(RSyntaxTextArea textArea) {
		this.textArea = textArea;
	}

	/**
	 * @return the csp
	 */
	public CollapsibleSectionPanel getCollapsibleSectionPanel() {
		return this.collapsibleSectionPanel;
	}

	/**
	 * @param csp the csp to set
	 */
	public void setCollapsibleSectionPanel(CollapsibleSectionPanel csp) {
		this.collapsibleSectionPanel = csp;
	}

	/**
	 * @return the findDialog
	 */
	public FindDialog getFindDialog() {
		return this.findDialog;
	}

	/**
	 * @param findDialog the findDialog to set
	 */
	public void setFindDialog(FindDialog findDialog) {
		this.findDialog = findDialog;
	}

	/**
	 * @return the replaceDialog
	 */
	public ReplaceDialog getReplaceDialog() {
		return this.replaceDialog;
	}

	/**
	 * @param replaceDialog the replaceDialog to set
	 */
	public void setReplaceDialog(ReplaceDialog replaceDialog) {
		this.replaceDialog = replaceDialog;
	}

	/**
	 * @return the findToolBar
	 */
	public FindToolBar getFindToolBar() {
		return this.findToolBar;
	}

	/**
	 * @param findToolBar the findToolBar to set
	 */
	public void setFindToolBar(FindToolBar findToolBar) {
		this.findToolBar = findToolBar;
	}

	/**
	 * @return the replaceToolBar
	 */
	public ReplaceToolBar getReplaceToolBar() {
		return this.replaceToolBar;
	}

	/**
	 * @param replaceToolBar the replaceToolBar to set
	 */
	public void setReplaceToolBar(ReplaceToolBar replaceToolBar) {
		this.replaceToolBar = replaceToolBar;
	}

	/**
	 * @return the statusBar
	 */
	public StatusBar getStatusBar() {
		return this.statusBar;
	}

	/**
	 * @param statusBar the statusBar to set
	 */
	protected void setStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;
	}
	
	

	protected static class StatusBar extends JPanel {

		private JLabel label;

		public StatusBar() {
			label = new JLabel("Ready");
			setLayout(new BorderLayout());
			add(label, BorderLayout.LINE_START);
			add(new JLabel(new SizeGripIcon()), BorderLayout.LINE_END);
		}

		public void setLabel(String label) {
			this.label.setText(label);
		}

	}
	

	private class GoToLineAction extends AbstractAction {

		public GoToLineAction() {
			super("Go To Line...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			GoToDialog dialog = new GoToDialog(RSyntaxTextAreaCPTFrame.this);
			dialog.setMaxLineNumberAllowed(textArea.getLineCount());
			dialog.setVisible(true);
			int line = dialog.getLineNumber();
			if (line>0) {
				try {
					textArea.setCaretPosition(textArea.getLineStartOffset(line-1));
				} catch (BadLocationException ble) { // Never happens
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
					ble.printStackTrace();
				}
			}
		}
		

	}
	

	private class ShowFindDialogAction extends AbstractAction {
		
		public ShowFindDialogAction() {
			super("Find...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			findDialog.setVisible(true);
		}

	}


	private class ShowReplaceDialogAction extends AbstractAction {
		
		public ShowReplaceDialogAction() {
			super("Replace...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			replaceDialog.setVisible(true);
		}

	}

}
