/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import unbbayes.controller.NetworkController;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;

/**
 *  Classe responsável pelas configuracoes basicas da rede Bayesiana. Ela extende
 *  a classe <code>JDialog</code>.
 *
 *@author Rommel N. Carvalho
 *@author Michael S. Onishi
 *@created 27 de Junho de 2001
 *@see JDialog
 */
public class GlobalOptionsDialog extends JDialog {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	final JButton probabilisticDescriptionNode;
    final JButton probabilisticExplanationNode;
	final JButton decisionNode;
	final JButton utilityNode;
    final JButton arc;
    final JButton selection;
    final JButton back;

    private NetworkController controller;
	private JTabbedPane jtp;
	private JPanel controllerColorPanel = new JPanel(new BorderLayout());
	private JPanel northControllerColorPanel = new JPanel(new GridLayout(2,1));
	private JPanel controllerSizePanel = new JPanel(new BorderLayout());
	private JPanel northControllerSizePanel = new JPanel();
    private JPanel flowControllerColorPanel1;
    private JPanel flowControllerColorPanel2;
    private JPanel radiusPanel;
    private JPanel netPanel;
    private JPanel confirmationPanel;
    private JPanel logPanel;
    private JPanel algorithmPanel;
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private JButton confirm;
    private JButton restore;
    private JButton cancel;
    private Color probabilisticDescriptionNodeColor;
    private Color probabilisticExplanationNodeColor;
	private Color decisionNodeColor;
	private Color utilityNodeColor;
    private Color edgeColor;
    private Color selectionColor;
    private Color backgroundColor;
    private JLabel radius;
    private JLabel net;
    private JSlider radiusSlider;
    private JSlider netSlider;
    private JCheckBox createLog;
    private boolean createLogBoolean;
    private ButtonGroup algorithmGroup;
    private JRadioButtonMenuItem junctionTreeAlgorithm;
    private JRadioButtonMenuItem likelihoodWeightingAlgorithm;
    //private PreviewPane preview;
    private final GraphPane graph;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");


    /**
     *  Constroi a estrutura da janela que mostra as opcoes globais
     *
     *@param  a rede a ser configurada (<code>TDesenhaRede</code>)
     */
    public GlobalOptionsDialog(GraphPane gra, NetworkController con) {
        super(new Frame(), resource.getString("globalOptionTitle"), true);
        Container contentPane = getContentPane();
        setSize(550, 470);
        setResizable(true);
        this.graph = gra;
        this.controller = con;

        createLog = new JCheckBox(resource.getString("createLogLabel"));

        algorithmGroup = new ButtonGroup();
        junctionTreeAlgorithm         = new JRadioButtonMenuItem(resource.getString("junctionTreeAlgorithmName"), controller.isUseJunctionTree());
        likelihoodWeightingAlgorithm  = new JRadioButtonMenuItem(resource.getString("likelihoodWeightingAlgorithmName"), !controller.isUseJunctionTree());
        
        gbl     = new GridBagLayout();
        gbc     = new GridBagConstraints();
        //preview = new PreviewPane(this);

        //setar cores padroes do no, arco e de selecao e boolean de criar log
        probabilisticDescriptionNodeColor = ProbabilisticNode.getDescriptionColor();
        probabilisticExplanationNodeColor = ProbabilisticNode.getExplanationColor();
		decisionNodeColor      = DecisionNode.getColor();
		utilityNodeColor       = UtilityNode.getColor();
        edgeColor              = Edge.getColor();
        selectionColor         = GraphPane.getSelectionColor();
        backgroundColor              = graph.getBackgroundColor();
        createLogBoolean       = controller.getSingleEntityNetwork().isCreateLog();

        radius = new JLabel(resource.getString("radiusLabel"));
        radius.setToolTipText(resource.getString("radiusToolTip"));
        // TODO Acrescentar possibilidade de alterar largura e altura.
        radiusSlider = new JSlider(JSlider.HORIZONTAL, 10, 40, (int)(Node.getWidth()/2));
        radiusSlider.setToolTipText(resource.getString("radiusToolTip"));
        radiusSlider.setMinorTickSpacing(1);
        radiusSlider.setMajorTickSpacing(10);
        radiusSlider.setPaintTicks(true);
        radiusSlider.setPaintLabels(true);

        net = new JLabel(resource.getString("netLabel"));
        net.setToolTipText(resource.getString("netToolTip"));
        netSlider = new JSlider(JSlider.HORIZONTAL, 1500, 11500, (int) graph.getGraphDimension().getWidth());
        netSlider.setToolTipText(resource.getString("netToolTip"));
        netSlider.setMinorTickSpacing(1000);
        netSlider.setMajorTickSpacing(5000);
        netSlider.setPaintTicks(true);
        netSlider.setPaintLabels(true);

		jtp                       = new JTabbedPane();
        radiusPanel               = new JPanel(gbl);
        netPanel                  = new JPanel(gbl);
        confirmationPanel         = new JPanel(new FlowLayout(FlowLayout.CENTER));
        flowControllerColorPanel1 = new JPanel();
        flowControllerColorPanel2 = new JPanel();
        logPanel                  = new JPanel();
        algorithmPanel            = new JPanel();

		probabilisticDescriptionNode = new JButton(resource.getString("probabilisticDescriptionNodeColorLabel"));
		probabilisticDescriptionNode.setToolTipText(resource.getString("probabilisticDescriptionNodeColorToolTip"));
        probabilisticDescriptionNode.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("nodeColorLabel"), probabilisticDescriptionNodeColor);
                  if (color != null)
                  {
                    probabilisticDescriptionNodeColor = color;
                    repaint();
                  }
                }
            });

        probabilisticExplanationNode = new JButton(resource.getString("probabilisticExplanationNodeColorLabel"));
		probabilisticExplanationNode.setToolTipText(resource.getString("probabilisticExplanationNodeColorToolTip"));
        probabilisticExplanationNode.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("nodeColorLabel"), probabilisticExplanationNodeColor);
                  if (color != null)
                  {
                    probabilisticExplanationNodeColor = color;
                    repaint();
                  }
                }
            });

		decisionNode = new JButton(resource.getString("decisionNodeColorLabel"));
		decisionNode.setToolTipText(resource.getString("decisionNodeColorToolTip"));
        decisionNode.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("nodeColorLabel"), decisionNodeColor);
                  if (color != null)
                  {
                    decisionNodeColor = color;
                    repaint();
                  }
                }
            });

		utilityNode = new JButton(resource.getString("utilityNodeColorLabel"));
		utilityNode.setToolTipText(resource.getString("utilityNodeColorToolTip"));
        utilityNode.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("nodeColorLabel"), utilityNodeColor);
                  if (color != null)
                  {
                    utilityNodeColor = color;
                    repaint();
                  }
                }
            });

		arc = new JButton(resource.getString("arcColorLabel"));
		arc.setToolTipText(resource.getString("arcColorToolTip"));
        arc.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("edgeColor"), edgeColor);
                  if (color != null)
                  {
                    edgeColor = color;
                    repaint();
                  }
                }
            });

		selection = new JButton(resource.getString("selectionColorLabel"));
		selection.setToolTipText(resource.getString("selectionColorToolTip"));
        selection.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("selectionColor"), selectionColor);
                  if (color != null)
                  {
                    selectionColor = color;
                    repaint();
                  }
                }
            });

		back = new JButton(resource.getString("backgroundColorLabel"));
		back.setToolTipText(resource.getString("backgroundColorToolTip"));
        back.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("backGroundColor"), backgroundColor);
                  if (color != null)
                  {
                    backgroundColor = color;
                    repaint();
                  }
                }
            });

		confirm = new JButton(resource.getString("confirmLabel"));
		confirm.setToolTipText(resource.getString("confirmToolTip"));
        confirmationPanel.add(confirm);
        confirm.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ProbabilisticNode.setDescriptionColor(probabilisticDescriptionNodeColor.getRGB());
                    ProbabilisticNode.setExplanationColor(probabilisticExplanationNodeColor.getRGB());
					DecisionNode.setColor(decisionNodeColor.getRGB());
					UtilityNode.setColor(utilityNodeColor.getRGB());
                    Edge.setColor(edgeColor.getRGB());
                    GraphPane.setSelectionColor(selectionColor);
                    graph.setBackgroundColor(backgroundColor);
                    Node.setSize(radiusSlider.getValue()*2, radiusSlider.getValue()*2);
                    graph.setGraphDimension(new Dimension((int) netSlider.getValue(), (int) netSlider.getValue()));
                    controller.getSingleEntityNetwork().setCreateLog(createLog.isSelected());
                    controller.setUseJunctionTree(junctionTreeAlgorithm.isSelected());
                    setVisible(false);
                    dispose();
                    graph.update();
                }
            });

		restore = new JButton(resource.getString("resetLabel"));
		restore.setToolTipText(resource.getString("resetToolTip"));
        confirmationPanel.add(restore);
        restore.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    probabilisticDescriptionNodeColor = ProbabilisticNode.getDescriptionColor();
                    probabilisticExplanationNodeColor = ProbabilisticNode.getExplanationColor();
					decisionNodeColor = DecisionNode.getColor();
					utilityNodeColor = UtilityNode.getColor();
                    edgeColor = Edge.getColor();
                    selectionColor = GraphPane.getSelectionColor();
                    backgroundColor = graph.getBackgroundColor();
                    netSlider.setValue((int) graph.getGraphDimension().getWidth());
                    radiusSlider.setValue((int)Node.getWidth()/2);
                    controller.getSingleEntityNetwork().setCreateLog(createLogBoolean);
                    repaint();
                }
            });

		cancel = new JButton(resource.getString("cancelLabel"));
		cancel.setToolTipText(resource.getString("cancelToolTip"));
        confirmationPanel.add(cancel);
        cancel.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                    graph.update();
                }
            });

        radiusSlider.addChangeListener(
            new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    repaint();
                }
            });

        //adicionar constrainsts para layout do label raio
        setConstraints(gbc, 0, 0, 1, 1, 1, 1);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(radius, gbc);

        //adicionar constraints para layout do slider raio
        setConstraints(gbc, 1, 0, 0, 0, 9, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(radiusSlider, gbc);

        //adicionar constrainsts para layout do label rede
        setConstraints(gbc, 0, 0, 1, 1, 1, 1);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(net, gbc);

        //adicionar constraints para layout do slider rede
        setConstraints(gbc, 1, 0, 0, 0, 9, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(netSlider, gbc);

        radiusPanel.add(radius);
        radiusPanel.add(radiusSlider);
        netPanel.add(net);
        netPanel.add(netSlider);

        flowControllerColorPanel1.add(probabilisticDescriptionNode);
        flowControllerColorPanel1.add(probabilisticExplanationNode);
		flowControllerColorPanel1.add(decisionNode);
		flowControllerColorPanel2.add(utilityNode);
        flowControllerColorPanel2.add(arc);
        flowControllerColorPanel2.add(selection);
        flowControllerColorPanel2.add(back);
		northControllerColorPanel.add(flowControllerColorPanel1);
		northControllerColorPanel.add(flowControllerColorPanel2);
		controllerColorPanel.add(northControllerColorPanel, BorderLayout.NORTH);
		controllerColorPanel.add(new PreviewPane(this), BorderLayout.CENTER);
        northControllerSizePanel.add(netPanel);
		northControllerSizePanel.add(radiusPanel);
		controllerSizePanel.add(northControllerSizePanel,  BorderLayout.NORTH);
		controllerSizePanel.add(new PreviewPane(this),  BorderLayout.CENTER);
		logPanel.add(createLog);
		algorithmGroup.add(junctionTreeAlgorithm);
        algorithmGroup.add(likelihoodWeightingAlgorithm);
        algorithmPanel.add(junctionTreeAlgorithm);
        algorithmPanel.add(likelihoodWeightingAlgorithm);

		jtp.addTab(resource.getString("colorControllerTab"), controllerColorPanel);
		jtp.addTab(resource.getString("sizeControllerTab"), controllerSizePanel);
		jtp.addTab(resource.getString("algorithmTab"), algorithmPanel);
		jtp.addTab(resource.getString("logTab"), logPanel);
        contentPane.add(jtp, BorderLayout.CENTER);
        contentPane.add(confirmationPanel, BorderLayout.SOUTH);
    }


    /**
     *  Retorna o valor do raio no <code>sliderRaio</code>.
     *
     *@return    o valor do raio em inteiro
     */
    public int getRadius() {
        return this.radiusSlider.getValue();
    }


    /**
     *  Retorna a cor do no de probabilidade do botao <code>corNo</code>
     *
     *@return    a cor do no de probabilidade (<code>Color<code>)
     *@see Color
     */
    public Color getPropabilisticDescriptionNodeColor() {
        return this.probabilisticDescriptionNodeColor;
    }

    /**
     *  Retorna a cor do no de probabilidade do botao <code>corNo</code>
     *
     *@return    a cor do no de probabilidade (<code>Color<code>)
     *@see Color
     */
    public Color getPropabilisticExplanationNodeColor() {
        return this.probabilisticExplanationNodeColor;
    }

	/**
     *  Retorna a cor do no de decisao do botao <code>corNo</code>
     *
     *@return    a cor do no de decisao (<code>Color<code>)
     *@see Color
     */
    public Color getDecisionNodeColor() {
        return this.decisionNodeColor;
    }

	/**
     *  Retorna a cor do no de utilidade do botao <code>corNo</code>
     *
     *@return    a cor do no de utilidade (<code>Color<code>)
     *@see Color
     */
    public Color getUtilityNodeColor() {
        return this.utilityNodeColor;
    }


    /**
     *  Retorna a cor do arco do botao <code>corArco</code>
     *
     *@return    a cor do arco (<code>Color<code>)
     *@see Color
     */
    public Color getArcColor() {
        return this.edgeColor;
    }


    /**
     *  Retorna a cor de selecao do botao <code>corSelecao</code>
     *
     *@return    a cor de selecao (<code>Color<code>)
     *@see Color
     */
    public Color getSelectionColor() {
        return this.selectionColor;
    }


    /**
     *  Retorna a cor de fundo do botao <code>corFundo</code>
     *
     *@return    a cor de fundo (<code>Color<code>)
     *@see Color
     */
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }


    //m�todo para usar no GridBagLayout, para setar constraints
    /**
     *  Seta as constraints do <code>GridBagConstraints</code>
     *
     *@param  gbc  o <code>GridBagConstraints</code>
     *@param  gx   posicao x
     *@param  gy   posicao y
     *@param  gw   proporcao da largura
     *@param  gh   proporcao da altura
     *@param  wx   peso x
     *@param  wy   peso y
     *@see GridBagConstraints
     */
    private void setConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

}

