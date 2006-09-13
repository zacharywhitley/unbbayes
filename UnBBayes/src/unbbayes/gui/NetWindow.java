/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.gui;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;

import unbbayes.controller.WindowController;
import unbbayes.prs.Node;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * Janela de uma rede.
 * 
 * @author Michael
 * @author Rommel
 */
public class NetWindow extends JInternalFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private JViewport graphViewport;

	private final GraphPane graph;

	private final WindowController controller;

	private JScrollPane jspGraph;

	private JLabel status;

	private boolean bCompiled;

	private CardLayout carta;

	private PNEditionPane netEdition;

	private PNCompilationPane netCompilation;

	private HierarchicDefinitionPane hierarchyPanel;

	private EditNet editNet;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.gui.resources.GuiResources");

	public NetWindow(SingleEntityNetwork net) {
		super(net.getName(), true, true, true, true);
		Container contentPane = getContentPane();
		carta = new CardLayout();
		contentPane.setLayout(carta);
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		// instancia variáveis de instância
		graphViewport = new JViewport();
		controller = new WindowController(net, this);
		graph = new GraphPane(controller, graphViewport);
		/*
		 * graph.setNode(net.getNos()); graph.setArc(net.getArcos());
		 */
		jspGraph = new JScrollPane(graphViewport);
		status = new JLabel(resource.getString("statusReadyLabel"));
		bCompiled = false;
		long width = Node.getWidth() / 2;
		long height = Node.getHeight() / 2;
		graph.getGraphViewport().reshape(0, 0,
				(int) (graph.getBiggestPoint().getX() + 2 * width),
				(int) (graph.getBiggestPoint().getY() + 2 * height));
		graph.getGraphViewport().setViewSize(
				new Dimension(
						(int) (graph.getBiggestPoint().getX() + 2 * width),
						(int) (graph.getBiggestPoint().getY() + 2 * height)));

		// setar o conteúdo e o tamanho do graphViewport
		graphViewport.setView(graph);
		graphViewport.setSize(800, 600);

		jspGraph.getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						graph.update();
					}
				});

		jspGraph.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						graph.update();
					}
				});

		// setar defaults para jspGraph
		jspGraph.setHorizontalScrollBar(jspGraph.createHorizontalScrollBar());
		jspGraph.setVerticalScrollBar(jspGraph.createVerticalScrollBar());
		jspGraph
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jspGraph
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		netEdition = new PNEditionPane(this, controller);
		editNet = new EditNet(this, controller);
		netCompilation = new PNCompilationPane(this, controller);
		hierarchyPanel = new HierarchicDefinitionPane(net, this);

		contentPane.add(netEdition, "netEdition");
		contentPane.add(editNet, "editNet");
		contentPane.add(netCompilation, "netCompilation");
		contentPane.add(hierarchyPanel, "hierarchy");

		// inicia com a tela de edicao de rede(NetEdition)
		netEdition.getCenterPanel().setBottomComponent(jspGraph);
		carta.show(getContentPane(), "netEdition");

		// pack();
		setVisible(true);
		graph.update();
	}

	/**
	 * Retorna o grafo responsável pela representação gráfica da rede.
	 * 
	 * @return retorna o (<code>GraphPane</code>)
	 * @see GraphPane
	 */
	public GraphPane getGraphPane() {
		return this.graph;
	}

	/**
	 * Retorna a árvore de evidencias.
	 * 
	 * @return retorna o evidenceTree (<code>JTree</code>)
	 * @see JTree
	 */
	public EvidenceTree getEvidenceTree() {
		return netCompilation.getEvidenceTree();
	}

	/**
	 * Retorna o container, graphViewport (<code>JViewport</code>), que
	 * contem o grafo responsável pela representação gráfica da rede.
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
		return netEdition.getTable();
	}

	/**
	 * Retorna o text field da descrição do nó.
	 * 
	 * @return retorna a txtDescrição (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtDescription() {
		return netEdition.getTxtDescription();
	}

	/**
	 * Retorna o text field da sigla do nó.
	 * 
	 * @return retorna a txtSigla (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtSigla() {
		return netEdition.getTxtSigla();
	}

	/**
	 * Substitui a tabela de probabilidades existente pela desejada.
	 * 
	 * @param table
	 *            a nova tabela (<code>JTable</code>) desejada.
	 * @see JTable
	 */
	public void setTable(JTable table) {
		netEdition.setTable(table);
	}

	/**
	 * Retorna o painel do draw.
	 * 
	 * @return retorna o jspDraw (<code>JScrollPane</code>)
	 * @see JScrollPane
	 */
	public JScrollPane getJspGraph() {
		return this.jspGraph;
	}

	/**
	 * Retorna o painel da árvore.
	 * 
	 * @return retorna o jspTree (<code>JScrollPane</code>)
	 * @see JScrollPane
	 */
	public JScrollPane getJspTree() {
		return netCompilation.getJspTree();
	}

	public Node getTableOwner() {
		return netEdition.getTableOwner();
	}

	public void setTableOwner(Node node) {
		netEdition.setTableOwner(node);
	}

	/**
	 * Retorna a rede probabilística <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabilística
	 * @see ProbabilisticNetwork
	 */
	public SingleEntityNetwork getRede() {
		return controller.getNet();
	}

	/**
	 * Seta o status exibido na barra de status.
	 * 
	 * @param status
	 *            mensagem de status.
	 */
	public void setStatus(String status) {
		netCompilation.setStatus(status);
		netEdition.setStatus(status);
		this.status.setText(status);
	}

	/**
	 * Método responsável por fazer as alterações necessárias para a mudar da
	 * tela de edição para a de compilação.
	 */
	public void changeToNetCompilation() {

		graph.setAction(GraphAction.NONE);
		graph.removeKeyListener(controller);

		netCompilation.getCenterPanel().setRightComponent(jspGraph);
		netCompilation.setStatus(status.getText());
		netCompilation.getEvidenceTree().setRootVisible(true);
		netCompilation.getEvidenceTree().expandRow(0);
		netCompilation.getEvidenceTree().setRootVisible(false);

		bCompiled = true;

		controller.getNet().setFirstInitialization(true);

		carta.show(getContentPane(), "netCompilation");
		netCompilation.getEvidenceTree().updateTree();
	}

	/**
	 * Método responsável por fazer as alterações necessárias para a mudar da
	 * tela de compilação para a de edição.
	 */
	public void changeToNetEdition() {

		graph.addKeyListener(controller);

		netEdition.getCenterPanel().setBottomComponent(jspGraph);
		netEdition.setStatus(status.getText());

		bCompiled = false;

		controller.getNet().setFirstInitialization(true);

		carta.show(getContentPane(), "netEdition");
	}

	/**
	 * Método responsável por fazer as alterações necessárias para a mudar da
	 * tela de edição para a tela de definição da hierarquia.
	 */
	public void changeToHierarchy() {

		hierarchyPanel.updateExplanationTree();
		carta.show(getContentPane(), "hierarchy");

	}

	public void changeToEditNet() {
		carta.show(getContentPane(), "editNet");
	}

	/**
	 * Retorna se a janela que esta aparecendo esta em modo de edicao(false) ou
	 * se ela esta em modo de compilação(true).
	 * 
	 * @return true se estiver em modo de compilação, e false caso contrário.
	 */
	public boolean isCompiled() {
		return this.bCompiled;
	}

	/**
	 * Retorna a tela de edição (<code>PNEditionPane</code>).
	 * 
	 * @return a tela de edição
	 * @see PNEditionPane
	 */
	public PNEditionPane getNetWindowEdition() {
		return this.netEdition;
	}

	/**
	 * Retorna a tela de compilação (<code>PNCompilationPane</code>).
	 * 
	 * @return a tela de compilação
	 * @see PNCompilationPane
	 */
	public PNCompilationPane getNetWindowCompilation() {
		return this.netCompilation;
	}

	public HierarchicDefinitionPane getHierarchicDefinitionPanel() {
		return this.hierarchyPanel;
	}

	public WindowController getWindowController() {
		return controller;
	}

	public EditNet getEditNet() {
		return editNet;
	}

}
