package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.finding.FindingArgumentPane;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.RandonVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * Class for insert a query
 *
 * Composto por dois paineis: um para selecionar o nó ao qual a query irá se referir
 * e o outro para selecionar os argumentos deste nó.
 *
 * @author Laécio Lima dos Santos
 */

public class QueryPanel extends JFrame{

	private JButton btnSelect;
	private MEBNController mebnController;
	private ResidentNode residentSelected;

	private IconController iconController = IconController.getInstance();
  	private static ResourceBundle resource =
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	public QueryPanel(MEBNController mebnController){

		super();

		this.setLocationRelativeTo(mebnController.getMebnEditionPane());
		this.setSize(200, 200);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.mebnController = mebnController;

		JPanel contentPane;
		contentPane = new JPanel(new BorderLayout());

		btnSelect = new JButton(iconController.getCompileIcon());
		btnSelect.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				showArgumentsSelection(residentSelected);
			}

		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new BorderLayout());
		toolBar.add(btnSelect, BorderLayout.CENTER);

		RandonVariableListPane randonVariableListPane = new RandonVariableListPane();

		contentPane.add(randonVariableListPane, BorderLayout.CENTER);
		contentPane.add(toolBar, BorderLayout.PAGE_END);

		super.setContentPane(contentPane);
	}

	public void showArgumentsSelection(ResidentNode _residentNode){
		JPanel contentPane = new RandonVariableInstanceEditionPane(_residentNode);
		setContentPane(contentPane);
	}

	private class RandonVariableListPane extends JPanel{

		private JList jlistResident;
		private JScrollPane scrollListObjectEntity;
		private DefaultListModel listModel;

		public RandonVariableListPane(){

			super(new BorderLayout());

			listModel = new DefaultListModel();
			for(MFrag mfrag: mebnController.getMultiEntityBayesianNetwork().getMFragList()){
				for(ResidentNode node: mfrag.getResidentNodeList()){
					listModel.addElement(node);
				}
			}

			jlistResident = new JList(listModel);
			scrollListObjectEntity = new JScrollPane(jlistResident);

			jlistResident.setCellRenderer(new ListCellRenderer(iconController.getYellowNodeIcon()));

			jlistResident.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	if(jlistResident.getSelectedValue() != null){
		                	   residentSelected = (ResidentNode)jlistResident.getSelectedValue();
		                	}
		                }
		            }
			 );

			this.add(scrollListObjectEntity, BorderLayout.CENTER);

		}
	}

	/**
	 * Pane contains:
	 * - Name of ResidentNode
	 * - List of arguments
	 * - Selection of state
	 * - buttons for actions
	 *
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 * @version 1.0 (09/09/07)
	 */
	private class RandonVariableInstanceEditionPane extends JPanel{

		private final ResidentNode residentNode;

		private JLabel nodeName;

		private JComboBox comboState;
		private JPanel paneArguments;

		private FindingArgumentPane findingArgumentPane;

		private JButton btnInsert;
		private JButton btnClear;
		private JButton btnBack;

		private JToolBar jtbOptions;

		private JToolBar jtbName;

		public RandonVariableInstanceEditionPane(ResidentNode _residentNode){

			super(new BorderLayout());
			this.residentNode = _residentNode;

			nodeName = new JLabel(residentNode.getName());
			nodeName.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			nodeName.setBackground(Color.YELLOW);

			findingArgumentPane = new FindingArgumentPane(residentNode, mebnController);

			btnBack = new JButton(iconController.getEditUndo());
			btnClear = new JButton(iconController.getEditClear());
			btnInsert = new JButton(iconController.getMoreIcon());

			jtbOptions = new JToolBar();
			jtbOptions.setLayout(new GridLayout(1,3));
			jtbOptions.add(btnBack);
			jtbOptions.add(btnClear);
			jtbOptions.add(btnInsert);
			jtbOptions.setFloatable(false);

			btnBack.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {

				}
			});

			btnClear.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					findingArgumentPane.clear();
				}
			});

			btnInsert.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					try {
						ObjectEntityInstance[] arguments = findingArgumentPane.getArguments();
						Entity state = findingArgumentPane.getState();
						RandonVariableFinding finding = new RandonVariableFinding(
								(DomainResidentNode)residentNode,
								arguments,
								state,
								mebnController.getMultiEntityBayesianNetwork());
						((DomainResidentNode)residentNode).addRandonVariableFinding(finding);

					} catch (ParcialStateException e1) {
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(),
									resource.getString("nameError"),
									resource.getString("nameException"),
									JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			//this.add(nodeName, BorderLayout.PAGE_START);
			this.add(new JScrollPane(findingArgumentPane), BorderLayout.CENTER);
			this.add(jtbOptions, BorderLayout.PAGE_END);

			TitledBorder titledBorder;

			titledBorder = BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.BLUE),
					resource.getString("AddFinding") + ": " +residentNode.getName());
			titledBorder.setTitleColor(Color.BLUE);
			titledBorder.setTitleJustification(TitledBorder.CENTER);

			this.setBorder(titledBorder);

		}

	}

}
