package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSBNWindow extends JPanel {
	public static String EDITION_PANE = "editionPane";
	public static String COMPILED_PANE = "compiledPane";	
	
	private JList netList;	
	
	private JButton compileBtn;
	private JButton editionBtn;
	private JButton removeBtn;
	private JButton newBtn;
	
	private CardLayout btnCard;
	private JPanel btnPanel;
	
	public MSBNWindow(ListModel listModel) {		
		initComponents(listModel);		
		setLayout(new BorderLayout());
		add(makeListPanel(), BorderLayout.WEST);
		init();
	}
	
	private void initComponents(ListModel listModel) {
		netList = new JList(listModel);		
		compileBtn = new JButton("Compile");
		editionBtn = new JButton("Edit MSBN");
		removeBtn = new JButton("Remove");
		newBtn = new JButton("New");
	}
	
	private void init() {
		netList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private JPanel makeListPanel() {
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(new JLabel("Networks"), BorderLayout.NORTH);
		JScrollPane jsp = new JScrollPane(netList);
		pane.add(jsp, BorderLayout.CENTER);
		setupButtonsPanel();
		pane.add(btnPanel, BorderLayout.SOUTH);		
		return pane;
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
}
