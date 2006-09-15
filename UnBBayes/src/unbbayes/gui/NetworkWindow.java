/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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

import unbbayes.controller.NetworkController;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Class responsible for representing the network window.
 * 
 * @author Michael
 * @author Rommel
 */
public class NetworkWindow extends JInternalFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private JViewport graphViewport;

	private final GraphPane graphPane;

	private final NetworkController controller;

	private JScrollPane jspGraph;

	private JLabel status;

	private boolean bCompiled;

	private CardLayout card;

	private PNEditionPane pnEditionPane;

	private PNCompilationPane pnCompilationPane;

	private HierarchicDefinitionPane hierarchyPanel;

	private EditNet editNet;
	
	private MEBNEditionPane mebnEditionPane;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.gui.resources.GuiResources");

	public NetworkWindow(Network net) {
		super(net.getName(), true, true, true, true);
		Container contentPane = getContentPane();
		card = new CardLayout();
		contentPane.setLayout(card);
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		// instancia vari�veis de inst�ncia
		graphViewport = new JViewport();
		if (net instanceof SingleEntityNetwork)
			controller = new NetworkController((SingleEntityNetwork)net, this);
		else controller = new NetworkController((MultiEntityBayesianNetwork)net, this);
			
		graphPane = new GraphPane(controller, graphViewport);

		jspGraph = new JScrollPane(graphViewport);
		status = new JLabel(resource.getString("statusReadyLabel"));
		bCompiled = false;
		long width = Node.getWidth();
		long height = Node.getHeight();
		graphPane.getGraphViewport().reshape(0, 0,
				(int) (graphPane.getBiggestPoint().getX() + width),
				(int) (graphPane.getBiggestPoint().getY() + height));
		graphPane.getGraphViewport().setViewSize(
				new Dimension(
						(int) (graphPane.getBiggestPoint().getX() + width),
						(int) (graphPane.getBiggestPoint().getY() + height)));

		// setar o conte�do e o tamanho do graphViewport
		graphViewport.setView(graphPane);
		graphViewport.setSize(800, 600);

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

		// setar defaults para jspGraph
		jspGraph.setHorizontalScrollBar(jspGraph.createHorizontalScrollBar());
		jspGraph.setVerticalScrollBar(jspGraph.createVerticalScrollBar());
		jspGraph
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jspGraph
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		if (net instanceof SingleEntityNetwork) {
			pnEditionPane = new PNEditionPane(this, controller);
			editNet = new EditNet(this, controller);
			pnCompilationPane = new PNCompilationPane(this, controller);
			hierarchyPanel = new HierarchicDefinitionPane((SingleEntityNetwork)net, this);
			
			contentPane.add(pnEditionPane, "pnEditionPane");
			contentPane.add(editNet, "editNet");
			contentPane.add(pnCompilationPane, "pnCompilationPane");
			contentPane.add(hierarchyPanel, "hierarchy");

			// inicia com a tela de edicao de rede(PNEditionPane)
			pnEditionPane.getCenterPanel().setBottomComponent(jspGraph);
			card.show(getContentPane(), "pnEditionPane");
		} else {
			mebnEditionPane = new MEBNEditionPane(this, controller);
			
			contentPane.add(mebnEditionPane, "mebnEditionPane");

			// inicia com a tela de edicao de rede(PNEditionPane)
			mebnEditionPane.getCenterPanel().setBottomComponent(jspGraph);
			card.show(getContentPane(), "mebnEditionPane");
		}

		setVisible(true);
		graphPane.update();
	}

	/**
	 * Retorna o grafo respons�vel pela representa��o gr�fica da rede.
	 * 
	 * @return retorna o (<code>GraphPane</code>)
	 * @see GraphPane
	 */
	public GraphPane getGraphPane() {
		return this.graphPane;
	}

	/**
	 * Retorna a �rvore de evidencias.
	 * 
	 * @return retorna o evidenceTree (<code>JTree</code>)
	 * @see JTree
	 */
	public EvidenceTree getEvidenceTree() {
		return pnCompilationPane.getEvidenceTree();
	}

	/**
	 * Retorna o container, graphViewport (<code>JViewport</code>), que
	 * contem o grafo respons�vel pela representa��o gr�fica da rede.
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
	 * Retorna o text field da descri��o do n�.
	 * 
	 * @return retorna a txtDescri��o (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtDescription() {
		return pnEditionPane.getTxtDescription();
	}

	/**
	 * Retorna o text field da sigla do n�.
	 * 
	 * @return retorna a txtSigla (<code>JTextField</code>)
	 * @see JTextField
	 */
	public JTextField getTxtSigla() {
		return pnEditionPane.getTxtSigla();
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
		return this.jspGraph;
	}

	/**
	 * Retorna o painel da �rvore.
	 * 
	 * @return retorna o jspTree (<code>JScrollPane</code>)
	 * @see JScrollPane
	 */
	public JScrollPane getJspTree() {
		return pnCompilationPane.getJspTree();
	}

	public Node getTableOwner() {
		return pnEditionPane.getTableOwner();
	}

	public void setTableOwner(Node node) {
		pnEditionPane.setTableOwner(node);
	}

	/**
	 * Retorna a rede probabil�stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil�stica
	 * @see ProbabilisticNetwork
	 */
	public SingleEntityNetwork getSingleEntityNetwork() {
		return (SingleEntityNetwork)controller.getNetwork();
	}
	
	/**
	 * Retorna a rede probabil�stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil�stica
	 * @see ProbabilisticNetwork
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return (MultiEntityBayesianNetwork)controller.getNetwork();
	}

	/**
	 * Seta o status exibido na barra de status.
	 * 
	 * @param status
	 *            mensagem de status.
	 */
	public void setStatus(String status) {
		pnCompilationPane.setStatus(status);
		pnEditionPane.setStatus(status);
		this.status.setText(status);
	}

	/**
	 * M�todo respons�vel por fazer as altera��es necess�rias para a mudar da
	 * tela de edi��o para a de compila��o.
	 */
	public void changeToPNCompilationPane() {

		graphPane.setAction(GraphAction.NONE);
		graphPane.removeKeyListener(controller);

		pnCompilationPane.getCenterPanel().setRightComponent(jspGraph);
		pnCompilationPane.setStatus(status.getText());
		pnCompilationPane.getEvidenceTree().setRootVisible(true);
		pnCompilationPane.getEvidenceTree().expandRow(0);
		pnCompilationPane.getEvidenceTree().setRootVisible(false);

		bCompiled = true;

		controller.getSingleEntityNetwork().setFirstInitialization(true);

		card.show(getContentPane(), "pnCompilationPane");
		pnCompilationPane.getEvidenceTree().updateTree();
	}

	/**
	 * M�todo respons�vel por fazer as altera��es necess�rias para a mudar da
	 * tela de compila��o para a de edi��o.
	 */
	public void changeToPNEditionPane() {

		graphPane.addKeyListener(controller);

		pnEditionPane.getCenterPanel().setBottomComponent(jspGraph);
		pnEditionPane.setStatus(status.getText());

		bCompiled = false;

		controller.getSingleEntityNetwork().setFirstInitialization(true);

		card.show(getContentPane(), "pnEditionPane");
	}

	/**
	 * M�todo respons�vel por fazer as altera��es necess�rias para a mudar da
	 * tela de edi��o para a tela de defini��o da hierarquia.
	 */
	public void changeToHierarchy() {

		hierarchyPanel.updateExplanationTree();
		card.show(getContentPane(), "hierarchy");

	}

	public void changeToEditNet() {
		card.show(getContentPane(), "editNet");
	}

	/**
	 * Retorna se a janela que esta aparecendo esta em modo de edicao(false) ou
	 * se ela esta em modo de compila��o(true).
	 * 
	 * @return true se estiver em modo de compila��o, e false caso contr�rio.
	 */
	public boolean isCompiled() {
		return this.bCompiled;
	}

	/**
	 * Retorna a tela de edi��o (<code>PNEditionPane</code>).
	 * 
	 * @return a tela de edi��o
	 * @see PNEditionPane
	 */
	public PNEditionPane getNetWindowEdition() {
		return this.pnEditionPane;
	}

	/**
	 * Retorna a tela de compila��o (<code>PNCompilationPane</code>).
	 * 
	 * @return a tela de compila��o
	 * @see PNCompilationPane
	 */
	public PNCompilationPane getNetWindowCompilation() {
		return this.pnCompilationPane;
	}

	public HierarchicDefinitionPane getHierarchicDefinitionPanel() {
		return this.hierarchyPanel;
	}

	public NetworkController getNetworkController() {
		return controller;
	}

	public EditNet getEditNet() {
		return editNet;
	}

}
