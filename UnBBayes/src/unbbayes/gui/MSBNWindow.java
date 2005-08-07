package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;

import javax.swing.ListSelectionModel;

import unbbayes.controller.IconController;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSBNWindow extends JInternalFrame {
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	public static String EDITION_PANE = "editionPane";
	public static String COMPILED_PANE = "compiledPane";

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

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

	public MSBNWindow(SingleAgentMSBN msbn) {
		super(msbn.getId(), true, true, true, true);
		this.msbn = msbn;
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		Container pane = getContentPane();
		initComponents();
		pane.setLayout(new BorderLayout());
		pane.add(makeListPanel(), BorderLayout.WEST);
		init();
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
}