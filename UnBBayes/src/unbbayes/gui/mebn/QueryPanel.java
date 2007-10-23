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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.gui.ParcialStateException;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * Class for insert a query
 *
 * Composto por dois paineis: um para selecionar o n� ao qual a query ir� se referir
 * e o outro para selecionar os argumentos deste n�.
 *
 * @author La�cio Lima dos Santos
 */

public class QueryPanel extends JFrame{

	private JButton btnSelect;
	private MEBNController mebnController;
	private ResidentNode residentSelected;

	private IconController iconController = IconController.getInstance();
  	private static ResourceBundle resource =
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	public QueryPanel(MEBNController mebnController){

		super("Query");

		setLocationRelativeTo(mebnController.getMebnEditionPane());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.mebnController = mebnController;
		
		showRandonVariableListPane(); 
	}

	/**
	 * Mostra o painel para selecionar os argumntos da query. 
	 * @param _residentNode
	 */
	public void showArgumentsSelection(ResidentNode _residentNode){
		JPanel contentPane = new QueryArgumentsEditionPane(_residentNode);
		setContentPane(contentPane);
		validate(); 
		pack(); 
	}
	
	/**
	 * Mostra o painel para sele��o da randon variable da qual a query se
	 * refere.
	 */
	public void showRandonVariableListPane(){
		
		JPanel contentPane;
		contentPane = new JPanel(new BorderLayout());

		btnSelect = new JButton(iconController.getGoNextInstance());
		btnSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				showArgumentsSelection(residentSelected);
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		toolBar.add(new JLabel());
		toolBar.add(btnSelect);
		toolBar.add(new JLabel());

		RandonVariableListPane randonVariableListPane = new RandonVariableListPane();
		randonVariableListPane.setBorder(BorderFactory.createLineBorder(Color.YELLOW)); 

		JLabel label = new JLabel("Selecione a vari�vel aleat�ria:    ");
		
		contentPane.add(label, BorderLayout.PAGE_START); 
		contentPane.add(randonVariableListPane, BorderLayout.CENTER);
		contentPane.add(toolBar, BorderLayout.PAGE_END);

		setContentPane(contentPane);
		validate(); 
		pack(); 
	}
	
	public void exit(){
		this.dispose(); 
	}

	public void makeInvisible(){
		this.setVisible(false); 
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
	private class QueryArgumentsEditionPane extends JPanel{

		private final ResidentNode residentNode;

		private JLabel nodeName;

		private QueryArgumentsPane queryArgumentsPane;

		private JButton btnBack; 
		private JButton btnExecute;
		private JButton btnExit; 

		private JToolBar jtbOptions;

		public QueryArgumentsEditionPane(ResidentNode _residentNode){

			super(new BorderLayout());
			this.residentNode = _residentNode;

			nodeName = new JLabel(residentNode.getName());
			nodeName.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			nodeName.setBackground(Color.YELLOW);

			queryArgumentsPane = new QueryArgumentsPane(residentNode, mebnController);

			btnBack = new JButton(iconController.getGoPreviousInstance());
			btnExecute = new JButton(iconController.getCompileIcon());
			btnExit = new JButton(iconController.getProcessStopInstance());

			jtbOptions = new JToolBar();
			jtbOptions.setLayout(new GridLayout(1,3));
			jtbOptions.add(btnBack);
			jtbOptions.add(btnExecute);
			jtbOptions.add(btnExit);
			jtbOptions.setFloatable(false);

			btnBack.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					showRandonVariableListPane(); 
				}
			});

			btnExecute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					makeInvisible(); 
					try {
						ObjectEntityInstance[] arguments = queryArgumentsPane.getArguments();
				        ProbabilisticNetwork network = mebnController.executeQuery((DomainResidentNode)residentNode, arguments); 
				        // TODO treat returned network
					} catch (ParcialStateException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InconsistentArgumentException iae) {
						iae.printStackTrace();
					}
			        exit(); 
				}
			});

			btnExit.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					exit(); 
				}
			});
			
			this.add(new JLabel("Selecione os valores dos argumentos:   ")); 
			this.add(new JScrollPane(queryArgumentsPane), BorderLayout.CENTER);
			this.add(jtbOptions, BorderLayout.PAGE_END);

		}

	}

}
