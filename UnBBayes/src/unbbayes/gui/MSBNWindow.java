package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import unbbayes.prs.msbn.MSNetwork;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSBNWindow extends JInternalFrame {
	public static String EDITION_PANE = "editionPane";
	public static String COMPILED_PANE = "compiledPane";
	
	private class MSBNListModel extends AbstractListModel {
		public int getSize() {
			return msbn.getNetCount();
		}

		public Object getElementAt(int index) {
			return msbn.getNetAt(index);
		}
	}
	
	private MSNetwork msbn;
	
	private JScrollPane netScroll;
	private JList netList;
	
	private JButton compileBtn;
	private JButton editionBtn;
	private JButton removeBtn;
	private JButton newBtn;
	
	private CardLayout btnCard;
	private JPanel btnPanel;
	
	public MSBNWindow(MSNetwork msbn) {
		super(msbn.getId(), true, true, true, true);
		this.msbn = msbn;		
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		Container pane = getContentPane();		
		initComponents();
		pane.setLayout(new BorderLayout());
		pane.add(makeListPanel(), BorderLayout.WEST);
		init();
	}
	
	public MSNetwork getMSNet() {
		return msbn;		
	}
	
	private void initComponents() {
		netList = new JList(new MSBNListModel());		
		compileBtn = new JButton("Compile");
		editionBtn = new JButton("Edit MSBN");
		removeBtn = new JButton("Remove");
		newBtn = new JButton("New");
	}
	
	private void init() {
		netList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private JPanel makeListPanel() {
		JPanel netPanel = new JPanel(new BorderLayout());
		netPanel.add(new JLabel("Networks"), BorderLayout.NORTH);
		netScroll = new JScrollPane(netList);
		netPanel.add(netScroll, BorderLayout.CENTER);
		setupButtonsPanel();
		netPanel.add(btnPanel, BorderLayout.SOUTH);		
		return netPanel;
	}
	
	private void setupButtonsPanel() {
		btnCard = new CardLayout();
		btnPanel = new JPanel(btnCard);
		JPanel editionPane = new JPanel(new GridLayout(0,1));
		btnPanel.add(editionPane, EDITION_PANE);				
		editionPane.add(newBtn);
		editionPane.add(removeBtn);
		editionPane.add(compileBtn);
		
		JPanel compiledPane = new JPanel();		
		compiledPane.add(editionBtn);
		btnPanel.add(compiledPane, COMPILED_PANE);
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
		btnCard.show(btnPanel, paneName);		
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