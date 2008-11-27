/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.controller.oobn.OOBNClassController;
import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.EditNet;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.gui.HierarchicDefinitionPane;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.PNEditionPane;
import unbbayes.gui.SSBNCompilationPane;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassWindow extends NetworkWindow {
	
	public static final long serialVersionUID = 0x00BFL;


	private JViewport graphViewport = null;

	private  OOBNGraphPane graphPane = null;

	private  OOBNClassController controller = null;

	private  Network net = null; 
	
	private String fileName = null; 
	
	private JScrollPane jspGraph = null;

	private JLabel status = null;

	private boolean bCompiled = false;

	private CardLayout card = null;

	private PNEditionPane pnEditionPane = null;

	//private PNCompilationPane pnCompilationPane = null;

	//private SSBNCompilationPane ssbnCompilationPane = null;

	//private HierarchicDefinitionPane hierarchyPanel = null;

	private EditNet editNet = null;

	private final String PN_PANE_PN_EDITION_PANE = "pnEditionPane";

	private final String PN_PANE_PN_EDIT_NET = "editNet";

	
	private final String PN_PANE_HIERARCHY_PANE = "hierarchy";
	
	private final String PN_PANE_EVALUATION_PANE = "pnEvaluation";

	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.gui.resources.GuiResources");

	/**
	 * Use this just in case you want to extend this class...
	 */
	protected OOBNClassWindow() {
		super();
	}
	
	/**
	 * Use this just in case you want to extend this class...
	 */
	protected OOBNClassWindow(IOOBNClass net) {
		super();
		
		this.net = net.getNetwork(); 
		fileName = null; 
		this.controller = OOBNClassController.newInstance(net, this);
		card = new CardLayout();
		
		// instancia variaveis de instancia
		graphViewport = new JViewport();
		graphPane = OOBNGraphPane.newInstance(controller, graphViewport);
		jspGraph = new JScrollPane(graphViewport);
		status = new JLabel(resource.getString("statusReadyLabel"));
		bCompiled = false;
		
		// adding key action events impemented by the networkcontroller, which is a key controller as well...
		this.graphPane.addKeyListener(this.controller);
	}
	
	public static OOBNClassWindow newInstance (IOOBNClass net) {
		OOBNClassWindow ret = new OOBNClassWindow(net);
		ret.initComponents();
		return ret;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		try{
			graphPane.update();
		} catch (Exception e) {
			//Debug.println(this.getClass(), "Could not update graphPane", e);
		}
		
	}

	
	
	
	protected void initComponents() {
		Container contentPane = this.getContentPane();	
		contentPane.setLayout(this.card);	
		
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		


		
		long width = Node.getWidth();
		long height = Node.getHeight();
		this.graphPane.getGraphViewport().reshape(0, 0,
				(int) (this.graphPane.getBiggestPoint().getX() + width),
				(int) (this.graphPane.getBiggestPoint().getY() + height));
		this.graphPane.getGraphViewport().setViewSize(
				new Dimension(
						(int) (this.graphPane.getBiggestPoint().getX() + width),
						(int) (this.graphPane.getBiggestPoint().getY() + height)));

		// set content and the size of graphViewport
		this.graphViewport.setView(this.graphPane);
		this.graphViewport.setSize(800, 600);

		// initialize listeners
		this.initListeners();

		// set defaults to jspGraph
		this.jspGraph.setHorizontalScrollBar(this.jspGraph.createHorizontalScrollBar());
		this.jspGraph.setVerticalScrollBar(this.jspGraph.createVerticalScrollBar());
		this.jspGraph
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.jspGraph
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		
		pnEditionPane = OOBNEditionPane.newInstance(this, controller);
		editNet = EditOOBN.newInstance(this, controller);
		
//		hierarchyPanel = new HierarchicDefinitionPane(
//				(SingleEntityNetwork) net, this);

		contentPane.add(pnEditionPane, PN_PANE_PN_EDITION_PANE);
		contentPane.add(editNet, PN_PANE_PN_EDIT_NET);
//		contentPane.add(pnCompilationPane, PN_PANE_PN_COMPILATION_PANE);
//		contentPane.add(hierarchyPanel, PN_PANE_HIERARCHY_PANE);

		// inicia com a tela de edicao de rede(PNEditionPane)
		pnEditionPane.getCenterPanel().setBottomComponent(jspGraph);
		card.show(getContentPane(), PN_PANE_PN_EDITION_PANE);
		

		
	}
	
	private void initListeners() {
		jspGraph.getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						graphPane.update();
					}
				});

		jspGraph.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						graphPane.update();
					}
				});
	}
	

	public void updateTitle(){
		super.setTitle(net.getName() + " " + "[" + fileName + "]"); 
	}
	
	/**
	 * Retorna o grafo responsavel pela representacao grafica da rede.
	 * 
	 * @return retorna o (<code>GraphPane</code>)
	 * @see GraphPane
	 */
	public OOBNGraphPane getGraphPane() {
		return this.graphPane;
	}

//	/**
//	 * Retorna a arvore de evidencias.
//	 * 
//	 * @return retorna o evidenceTree (<code>JTree</code>)
//	 * @see JTree
//	 */
//	public EvidenceTree getEvidenceTree() {
//		if (pnCompilationPane != null) {
//			return pnCompilationPane.getEvidenceTree();
//		} else {
//			if (ssbnCompilationPane != null) {
//				return ssbnCompilationPane.getEvidenceTree();
//			} else {
//				return null;
//			}
//		}
//	}

	/**
	 * Retorna o container, graphViewport (<code>JViewport</code>), que
	 * contem o grafo responsavel pela representacao grafica da rede.
	 * 
	 * @return retorna o graphViewport(<code>JViewport</code>)
	 * @see JViewport
	 */
	public JViewport getGraphViewport() {
		return this.graphViewport;
	}

	/**
	 * Retorna a tabela de probabilidades.
	 * 
	 * @return retorna a table (<code>JTable</code>)
	 * @see JTable
	 */
	public JTable getTable() {
		return pnEditionPane.getTable();
	}

	/**
	 * Retorna o text field da descricao do no.
	 * 
	 * @return retorna a txtDescricao (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtDescription() {
		return pnEditionPane.getTxtDescription();
	}

	/**
	 * Retorna o text field da sigla do no.
	 * 
	 * @return retorna a txtSigla (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtSigla() {
		return pnEditionPane.getTxtSigla();
	}
	
	public void setDistributionPane(JPanel distributionPane) {
		pnEditionPane.setDistributionPane(distributionPane);
	}

	/**
	 * Substitui a tabela de probabilidades existente pela desejada.
	 * 
	 * @param table
	 *            a nova tabela (<code>JTable</code>) desejada.
	 * @see JTable
	 */
	public void setTable(JTable table) {
		pnEditionPane.setTable(table);
	}

	/**
	 * Retorna o painel do draw.
	 * 
	 * @return retorna o jspDraw (<code>JScrollPane</code>)
	 * @see JScrollPane
	 */
	public JScrollPane getJspGraph() {
		if (this.jspGraph != null) {
			return this.jspGraph;
		} else {
			return super.getJspGraph();
		}
	}

//	/**
//	 * Retorna o painel da arvore.
//	 * 
//	 * @return retorna o jspTree (<code>JScrollPane</code>)
//	 * @see JScrollPane
//	 */
//	public JScrollPane getJspTree() {
//		return pnCompilationPane.getJspTree();
//	}

	public Node getTableOwner() {
		return pnEditionPane.getTableOwner();
	}

	public void setTableOwner(Node node) {
		pnEditionPane.setTableOwner(node);
	}

	/**
	 * Retorna a rede probabil_stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil_stica
	 * @see ProbabilisticNetwork
	 */
	public SingleEntityNetwork getSingleEntityNetwork() {
		try{
			return (SingleEntityNetwork) controller.getNetwork();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna a rede probabil_stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil_stica
	 * @see ProbabilisticNetwork
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return (MultiEntityBayesianNetwork) controller.getNetwork();
	}

	/**
	 * Seta o status exibido na barra de status.
	 * 
	 * @param status
	 *            mensagem de status.
	 */
	public void setStatus(String status) {
		
		pnEditionPane.setStatus(status);
		this.status.setText(status);
	}

//	/**
//	 * M_todo respons_vel por fazer as altera__es necess_rias para a mudar da
//	 * tela de edi__o para a de compila__o.
//	 */
//	public void changeToPNCompilationPane() {
//
//		graphPane.setAction(GraphAction.NONE);
//		graphPane.removeKeyListener(controller);
//
//		pnCompilationPane.getCenterPanel().setRightComponent(jspGraph);
//		pnCompilationPane.setStatus(status.getText());
//		pnCompilationPane.getEvidenceTree().setRootVisible(true);
//		pnCompilationPane.getEvidenceTree().expandRow(0);
//		pnCompilationPane.getEvidenceTree().setRootVisible(false);
//
//		bCompiled = true;
//		controller.getSingleEntityNetwork().setFirstInitialization(true);
//
//		card.show(getContentPane(), PN_PANE_PN_COMPILATION_PANE);
//		pnCompilationPane.getEvidenceTree().updateTree();
//		
//		pnCompilationPane.updateToPreferredSize();
//
//	}
	
//	public void changeToPNEvaluationPane(JPanel evaluationPane) {
//
//		graphPane.setAction(GraphAction.NONE);
//		graphPane.removeKeyListener(controller);
//		
//		
//		// FIXME THINK MORE IN WHERE TO PUT THIS... DOES NOT LOOK GOOD...
//		JPanel leftPane = new JPanel(new BorderLayout());
//		JToolBar toolBar = new JToolBar();
//		
//		JButton editMode = new JButton(IconController.getInstance().getEditIcon());
//		editMode.setToolTipText(resource.getString("editToolTip"));
//			
//		// Go back to editing mode 
//        editMode.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//                changeToPNEditionPane();
//            }
//        });
//		toolBar.add(editMode);
//		
//		leftPane.add(toolBar, BorderLayout.NORTH);
//		leftPane.add(evaluationPane, BorderLayout.CENTER);
//		
//		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, jspGraph);
//		
//		mainPane.setDividerLocation(500);
//		
//		getContentPane().add(mainPane, PN_PANE_EVALUATION_PANE);
//
//		card.show(getContentPane(), PN_PANE_EVALUATION_PANE);
//
//	}

	

//	/**
//	 * M_todo respons_vel por fazer as altera__es necess_rias para a mudar da
//	 * tela de compila__o para a de edi__o.
//	 */
//	public void changeToPNEditionPane() {
//		graphPane.addKeyListener(controller);
//
//		pnEditionPane.getCenterPanel().setBottomComponent(jspGraph);
//		pnEditionPane.setStatus(status.getText());
//
//		bCompiled = false;
//
//		controller.getSingleEntityNetwork().setFirstInitialization(true);
//
//		card.show(getContentPane(), "pnEditionPane");
//		
//	}

//	/**
//	 * M_todo respons_vel por fazer as altera__es necess_rias para a mudar da
//	 * tela de edi__o para a tela de defini__o da hierarquia.
//	 */
//	public void changeToHierarchy() {
//
//		hierarchyPanel.updateExplanationTree();
//		card.show(getContentPane(), "hierarchy");
//
//	}

//	public void changeToEditNet() {
//		card.show(getContentPane(), "editNet");
//	}

	/**
	 * Retorna se a janela que esta aparecendo esta em modo de edicao(false) ou
	 * se ela esta em modo de compila__o(true).
	 * 
	 * @return true se estiver em modo de compila__o, e false caso contr_rio.
	 */
	public boolean isCompiled() {
		return this.bCompiled;
	}

	/**
	 * Retorna a tela de edi__o (<code>PNEditionPane</code>).
	 * 
	 * @return a tela de edi__o
	 * @see PNEditionPane
	 */
	public PNEditionPane getNetWindowEdition() {
		return this.pnEditionPane;
	}

	

//	/**
//	 * Retorna a tela de compila__o (<code>PNCompilationPane</code>).
//	 * 
//	 * @return a tela de compila__o
//	 * @see PNCompilationPane
//	 */
//	public PNCompilationPane getNetWindowCompilation() {
//		return this.pnCompilationPane;
//	}

//	public HierarchicDefinitionPane getHierarchicDefinitionPanel() {
//		return this.hierarchyPanel;
//	}

	public NetworkController getNetworkController() {
		return controller;
	}

	public EditNet getEditNet() {
		return editNet;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.updateTitle(); 
	}
	
	public void setAddRemoveStateButtonVisible(boolean visible) {
		pnEditionPane.getBtnAddState().setVisible(visible);
		pnEditionPane.getBtnRemoveState().setVisible(visible);
	}

	/**
	 * @return the controller
	 */
	public OOBNClassController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(OOBNClassController controller) {
		this.controller = controller;
	}

	/**
	 * @return the status
	 */
	public JLabel getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(JLabel status) {
		this.status = status;
	}

	/**
	 * @return the card
	 */
	public CardLayout getCard() {
		return card;
	}

	/**
	 * @param card the card to set
	 */
	public void setCard(CardLayout card) {
		this.card = card;
	}

	/**
	 * @return the pnEditionPane
	 */
	public PNEditionPane getPnEditionPane() {
		return pnEditionPane;
	}

	/**
	 * @param pnEditionPane the pnEditionPane to set
	 */
	public void setPnEditionPane(PNEditionPane pnEditionPane) {
		this.pnEditionPane = pnEditionPane;
	}

	/**
	 * @param graphViewport the graphViewport to set
	 */
	public void setGraphViewport(JViewport graphViewport) {
		this.graphViewport = graphViewport;
	}

	/**
	 * @param graphPane the graphPane to set
	 */
	public void setGraphPane(OOBNGraphPane graphPane) {
		this.graphPane = graphPane;
	}

	/**
	 * @param jspGraph the jspGraph to set
	 */
	public void setJspGraph(JScrollPane jspGraph) {
		this.jspGraph = jspGraph;
	}

	/**
	 * @param editNet the editNet to set
	 */
	public void setEditNet(EditNet editNet) {
		this.editNet = editNet;
	}
	
	

}
