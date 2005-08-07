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


import java.util.ResourceBundle;
import unbbayes.controller.WindowController;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;

import java.awt.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  Classe responsável pelas configurações básicas da rede Bayesiana. Ela extende
 *  a classe <code>JDialog</code>.
 *
 *@author     Rommel N. Carvalho, Michael S. Onishi
 *@created    27 de Junho de 2001
 *@see JDialog
 */
public class GlobalOptions extends JDialog {
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

    final JButton probabilisticDescriptionNode;
    final JButton probabilisticExplanationNode;
	final JButton decisionNode;
	final JButton utilityNode;
    final JButton arc;
    final JButton selection;
    final JButton back;

    private WindowController controller;
	private JTabbedPane jtp;
	private JPanel controllerColorPanel = new JPanel(new BorderLayout());
	private JPanel northControllerColorPanel = new JPanel(new GridLayout(2,1));
	private JPanel controllerSizePanel = new JPanel(new BorderLayout());
	private JPanel northControllerSizePanel = new JPanel();
    private JPanel flowControllerColorPanel1;
    private JPanel flowControllerColorPanel2;
//    private JPanel decimalPatternPanel;
    private JPanel radiusPanel;
    private JPanel netPanel;
    private JPanel confirmationPanel;
    private JPanel logPanel;
    private GridBagLayout gbl;
    private GridBagConstraints gbc;
    private JButton confirm;
    private JButton restore;
    private JButton cancel;
    private Color probabilisticDescriptionNodeColor;
    private Color probabilisticExplanationNodeColor;
	private Color decisionNodeColor;
	private Color utilityNodeColor;
    private Color arcColor;
    private Color selectionColor;
    private Color backColor;
    private JLabel radius;
    private JLabel net;
    private JSlider radiusSlider;
    private JSlider netSlider;
    private JCheckBox createLog;
    private boolean createLogBoolean;
    private final IGraph graph;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");


    /**
     *  Constrói a estrutura da janela que mostra as opções globais
     *
     *@param  a rede a ser configurada (<code>TDesenhaRede</code>)
     */
    public GlobalOptions(IGraph gra, WindowController con) {
        super(new Frame(), resource.getString("globalOptionTitle"), true);
        Container contentPane = getContentPane();
        setSize(550, 470);
        setResizable(true);
        this.graph = gra;
        this.controller = con;

		/*
        decimalGroup = new ButtonGroup();
        usa          = new JRadioButtonMenuItem(resource.getString("usaName"));
        china        = new JRadioButtonMenuItem(resource.getString("chinaName"));
        japan        = new JRadioButtonMenuItem(resource.getString("japanName"));
        canada       = new JRadioButtonMenuItem(resource.getString("canadaName"));
        uk           = new JRadioButtonMenuItem(resource.getString("ukName"));
        italy        = new JRadioButtonMenuItem(resource.getString("italyName"));
        brazil       = new JRadioButtonMenuItem(resource.getString("brazilName"));
        korea        = new JRadioButtonMenuItem(resource.getString("koreaName"));
        */

        createLog = new JCheckBox(resource.getString("createLogLabel"));

        gbl     = new GridBagLayout();
        gbc     = new GridBagConstraints();
        new Preview(this);

        //setar cores padrões do nó, arco e de seleção e boolean de criar log
        probabilisticDescriptionNodeColor = ProbabilisticNode.getDescriptionColor();
        probabilisticExplanationNodeColor = ProbabilisticNode.getExplanationColor();
		decisionNodeColor      = DecisionNode.getColor();
		utilityNodeColor       = UtilityNode.getColor();
        arcColor               = graph.getArcColor();
        selectionColor         = graph.getSelectionColor();
        backColor              = graph.getBackColor();
        createLogBoolean       = controller.getNet().isCreateLog();

        radius = new JLabel(resource.getString("radiusLabel"));
        radius.setToolTipText(resource.getString("radiusToolTip"));
        radiusSlider = new JSlider(JSlider.HORIZONTAL, 10, 40, (int) graph.getRadius());
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
//        decimalPatternPanel       = new JPanel(new GridLayout(4, 2));
        confirmationPanel         = new JPanel(new FlowLayout(FlowLayout.CENTER));
        flowControllerColorPanel1 = new JPanel();
        flowControllerColorPanel2 = new JPanel();
        logPanel                  = new JPanel();

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
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("arcColor"), arcColor);
                  if (color != null)
                  {
                    arcColor = color;
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
                  Color color = JColorChooser.showDialog(((Component) e.getSource()).getParent(), resource.getString("backGroundColor"), backColor);
                  if (color != null)
                  {
                    backColor = color;
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
                    graph.setArcColor(arcColor);
                    graph.setSelectionColor(selectionColor);
                    graph.setBackColor(backColor);
                    graph.setRadius(radiusSlider.getValue());
                    graph.setGraphDimension(new Dimension((int) netSlider.getValue(), (int) netSlider.getValue()));
                    controller.getNet().setCreateLog(createLog.isSelected());
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
                    arcColor = graph.getArcColor();
                    selectionColor = graph.getSelectionColor();
                    backColor = graph.getBackColor();
                    netSlider.setValue((int) graph.getGraphDimension().getWidth());
                    radiusSlider.setValue((int) graph.getRadius());
                    controller.getNet().setCreateLog(createLogBoolean);
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

            /** @todo listeners de numero! */

            /*
        usa.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.US);
                }
            });

        canada.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.CANADA);
                }
            });

        japan.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.JAPAN);
                }
            });

        china.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.CHINA);
                }
            });

        uk.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.UK);
                }
            });

        italy.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.ITALY);
                }
            });

        brazil.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.GERMANY);
                }
            });

        korea.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.setNumberFormat(Locale.KOREA);
                }
            });

            */

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
        /*
        decimalGroup.add(usa);
        decimalGroup.add(canada);
        decimalGroup.add(japan);
        decimalGroup.add(china);
        decimalGroup.add(uk);
        decimalGroup.add(italy);
        decimalGroup.add(brazil);
        decimalGroup.add(korea);
        decimalPatternPanel.add(brazil);
        decimalPatternPanel.add(usa);
        decimalPatternPanel.add(canada);
        decimalPatternPanel.add(china);
        decimalPatternPanel.add(japan);
        decimalPatternPanel.add(korea);
        decimalPatternPanel.add(uk);
        decimalPatternPanel.add(italy);
        */
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
		controllerColorPanel.add(new Preview(this), BorderLayout.CENTER);
        northControllerSizePanel.add(netPanel);
		northControllerSizePanel.add(radiusPanel);
		controllerSizePanel.add(northControllerSizePanel,  BorderLayout.NORTH);
		controllerSizePanel.add(new Preview(this),  BorderLayout.CENTER);
		logPanel.add(createLog);

//		jtp.addTab(resource.getString("decimalPatternTab"), decimalPatternPanel);
		jtp.addTab(resource.getString("colorControllerTab"), controllerColorPanel);
		jtp.addTab(resource.getString("sizeControllerTab"), controllerSizePanel);
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
     *  Retorna a cor do nó de probabilidade do botão <code>corNo</code>
     *
     *@return    a cor do nó de probabilidade (<code>Color<code>)
     *@see Color
     */
    public Color getPropabilisticDescriptionNodeColor() {
        return this.probabilisticDescriptionNodeColor;
    }

    /**
     *  Retorna a cor do nó de probabilidade do botão <code>corNo</code>
     *
     *@return    a cor do nó de probabilidade (<code>Color<code>)
     *@see Color
     */
    public Color getPropabilisticExplanationNodeColor() {
        return this.probabilisticExplanationNodeColor;
    }

	/**
     *  Retorna a cor do nó de decisão do botão <code>corNo</code>
     *
     *@return    a cor do nó de decisão (<code>Color<code>)
     *@see Color
     */
    public Color getDecisionNodeColor() {
        return this.decisionNodeColor;
    }

	/**
     *  Retorna a cor do nó de utilidade do botão <code>corNo</code>
     *
     *@return    a cor do nó de utilidade (<code>Color<code>)
     *@see Color
     */
    public Color getUtilityNodeColor() {
        return this.utilityNodeColor;
    }


    /**
     *  Retorna a cor do arco do botão <code>corArco</code>
     *
     *@return    a cor do arco (<code>Color<code>)
     *@see Color
     */
    public Color getArcColor() {
        return this.arcColor;
    }


    /**
     *  Retorna a cor de seleção do botão <code>corSelecao</code>
     *
     *@return    a cor de seleção (<code>Color<code>)
     *@see Color
     */
    public Color getSelectionColor() {
        return this.selectionColor;
    }


    /**
     *  Retorna a cor de fundo do botão <code>corFundo</code>
     *
     *@return    a cor de fundo (<code>Color<code>)
     *@see Color
     */
    public Color getBackColor() {
        return this.backColor;
    }


    //método para usar no GridBagLayout, para setar constraints
    /**
     *  Seta as constraints do <code>GridBagConstraints</code>
     *
     *@param  gbc  o <code>GridBagConstraints</code>
     *@param  gx   posição x
     *@param  gy   posição y
     *@param  gw   proporção da largura
     *@param  gh   proporção da altura
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

