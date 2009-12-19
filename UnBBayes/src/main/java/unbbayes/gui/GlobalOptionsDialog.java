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
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.standard.StandardPluginLocation;

import unbbayes.controller.NetworkController;
import unbbayes.gui.option.GaussianMixtureOptionPanel;
import unbbayes.gui.option.GibbsSamplingOptionPanel;
import unbbayes.gui.option.JunctionTreeOptionPanel;
import unbbayes.gui.option.LikelihoodWeightingOptionPanel;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.extension.PluginCore;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 *  Class responsible for general configurations, like node color, size, algorithm to 
 *  use for compilation, etc.
 *
 *@author Rommel N. Carvalho (rommel.carvalho@gmail.com)
 *@author Michael S. Onishi
 *@created 27 of June 2001
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
    private JComponent algorithmMainPanel;
    private JPanel algorithmRadioPanel;
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
//    private JRadioButtonMenuItem junctionTreeAlgorithm;
//    private JRadioButtonMenuItem likelihoodWeightingAlgorithm;
//    private JRadioButtonMenuItem gibbsAlgorithm;
//    private JRadioButtonMenuItem gaussianMixtureAlgorithm;
    //private PreviewPane preview;
    private final GraphPane graph;

    private JComponent algorithmOptionPane;
    private Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> algorithmToOptionMap = new HashMap<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel>();
    
    private String pluginDirectory = "plugins";
//    private PluginManager pluginManager;
    private String pluginCoreID = "unbbayes.util.extension.core";
    private String algorithmExtensionPoint = "InferenceAlgorithm";
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.gui.resources.GuiResources.class.getName());

  	

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
        
        // set up plugins (algorithms) and fill map (this map associates radio button, its additional options and algorithm)
        this.setAlgorithmToOptionMap(this.loadAlgorithmsAsPlugins());
        
        
        gbl     = new GridBagLayout();
        gbc     = new GridBagConstraints();
        //preview = new PreviewPane(this);

        //setar cores padroes do no, arco e de selecao e boolean de criar log
        probabilisticDescriptionNodeColor = ProbabilisticNode.getDescriptionColor();
        probabilisticExplanationNodeColor = ProbabilisticNode.getExplanationColor();
        
        //by young
        decisionNodeColor      = Color.YELLOW;
		utilityNodeColor       = Color.YELLOW;
        edgeColor              = Color.YELLOW;
        selectionColor         = Color.YELLOW;
        backgroundColor        = Color.YELLOW;
        createLogBoolean       = controller.getSingleEntityNetwork().isCreateLog();
      
        /*
		decisionNodeColor      = DecisionNode.getColor();
		utilityNodeColor       = UtilityNode.getColor();
        edgeColor              = Edge.getColor();
        selectionColor         = GraphPane.getSelectionColor();
        backgroundColor        = graph.getBackgroundColor();
        createLogBoolean       = controller.getSingleEntityNetwork().isCreateLog();
*/  //by young end
        radius = new JLabel(resource.getString("radiusLabel"));
        radius.setToolTipText(resource.getString("radiusToolTip"));
        // TODO Acrescentar possibilidade de alterar largura e altura.
        //by young 
        radiusSlider = new JSlider(JSlider.HORIZONTAL, 10, 40, (int)(Node.getDefaultSize().x/2));
        //by young end
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
//        algorithmMainPanel            = new JPanel(new BorderLayout());
        
        algorithmMainPanel            = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        
        algorithmRadioPanel       = new JPanel(new GridLayout(0,3));
        algorithmRadioPanel.setBorder(new TitledBorder(this.resource.getString("availableAlgorithms")));
        
        algorithmOptionPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        algorithmOptionPane.setBorder(new TitledBorder(this.resource.getString("algorithmParameters")));

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
                    //by young 
                    /*
					DecisionNode.setColor(decisionNodeColor.getRGB());
					UtilityNode.setColor(utilityNodeColor.getRGB());
                    Edge.setColor(edgeColor.getRGB());
                    GraphPane.setSelectionColor(selectionColor);
                    graph.setBackgroundColor(backgroundColor);
                    Node.setSize(radiusSlider.getValue()*2, radiusSlider.getValue()*2);
                    graph.setGraphDimension(new Dimension((int) netSlider.getValue(), (int) netSlider.getValue()));
                    */
                    //by young end
                    controller.getSingleEntityNetwork().setCreateLog(createLog.isSelected());
                    
                    // avoiding if-then-else and using method overwriting
//                    if (junctionTreeAlgorithm.isSelected()) {
//                    	controller.setInferenceAlgorithm(InferenceAlgorithmEnum.JUNCTION_TREE);
//                    } else if (likelihoodWeightingAlgorithm.isSelected()) {
//                    	controller.setInferenceAlgorithm(InferenceAlgorithmEnum.LIKELIHOOD_WEIGHTING);
//                    } else if (gibbsAlgorithm.isSelected()) {
//                    	controller.setInferenceAlgorithm(InferenceAlgorithmEnum.GIBBS);
//                    } else if (gaussianMixtureAlgorithm.isSelected()) {
//                    	controller.setInferenceAlgorithm(InferenceAlgorithmEnum.GAUSSIAN_MIXTURE);
//                    }
                    
                    // commit changes (made at each option panel) on inference algorithm
                    InferenceAlgorithmOptionPanel currentPanel = getSelectedAlgorithmOptionPanel();
                    if (currentPanel != null) {
                    	currentPanel.commitChanges();
                    	
                    	// updating the inference algorithm referenced by controller
                        controller.setInferenceAlgorithm(currentPanel.getInferenceAlgorithm());
                    }
                    
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
                    //by young
                    /*
					decisionNodeColor = DecisionNode.getColor();
					utilityNodeColor = UtilityNode.getColor();
                    edgeColor = Edge.getColor();
                    selectionColor = GraphPane.getSelectionColor();
                    backgroundColor = graph.getBackgroundColor();
                    netSlider.setValue((int) graph.getGraphDimension().getWidth());
                    radiusSlider.setValue((int)Node.getWidth()/2);
                    */
                    //by young end
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
		
//		algorithmGroup.add(junctionTreeAlgorithm);
//        algorithmGroup.add(likelihoodWeightingAlgorithm);
//        algorithmGroup.add(gibbsAlgorithm);
//        algorithmGroup.add(gaussianMixtureAlgorithm);
//        algorithmMainPanel.add(junctionTreeAlgorithm);
//        algorithmMainPanel.add(likelihoodWeightingAlgorithm);
//        algorithmMainPanel.add(gibbsAlgorithm);
//        algorithmMainPanel.add(gaussianMixtureAlgorithm);
        
        // adding radio buttons to the same radio button group (algorithmGroup) and same panel (algorithmRadioPanel)
		for (JRadioButtonMenuItem radioItem : this.getAlgorithmToOptionMap().keySet()) {
			algorithmGroup.add(radioItem);
			algorithmRadioPanel.add(radioItem);
		}
		
        // adding radio buttons panel to the top of algorithm tab
//        algorithmMainPanel.add(algorithmRadioPanel, BorderLayout.NORTH);
        algorithmMainPanel.add(algorithmRadioPanel);
        
        // adding the option pane for the selected algorithm.
        Component currentOptionPanel = this.getSelectedAlgorithmOptionPanel();
        if (currentOptionPanel != null) {
        	algorithmOptionPane.add(currentOptionPanel);
        }
//        algorithmMainPanel.add(algorithmOptionPane, BorderLayout.CENTER);
        algorithmMainPanel.add(algorithmOptionPane);
        
		jtp.addTab(resource.getString("colorControllerTab"), controllerColorPanel);
		jtp.addTab(resource.getString("sizeControllerTab"), controllerSizePanel);
		jtp.addTab(resource.getString("algorithmTab"), algorithmMainPanel);
		jtp.addTab(resource.getString("logTab"), logPanel);
        contentPane.add(jtp, BorderLayout.CENTER);
        contentPane.add(confirmationPanel, BorderLayout.SOUTH);
    }

    /**
     * Use the plugin framework to load algorithms and fill radio button and its option panel
     * @return
     */
    protected Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> loadAlgorithmsAsPlugins() {
    	
    	Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> ret = new HashMap<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel>();
		
    	// initialize default algorithms (those which are not actually "plugins")
    	ret.putAll(this.getDefaultAlgorithms());
    	
    	// we assume the plugins are already published at UnBBayesFrame#loadPlugins(), so, we do not have to republish them.

	    // loads the "core" plugin, which declares general extension points for core (including algorithms)
	    PluginDescriptor core = this.getPluginManager().getRegistry().getPluginDescriptor(this.getPluginCoreID());
        
	    // load the extension point for new algorithms (functionalities).
	    ExtensionPoint point = this.getPluginManager().getRegistry().getExtensionPoint(core.getId(), this.getAlgorithmExtensionPoint());
    	
	    // iterate over the connected extension points
	    for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
			Extension ext = it.next();
            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
            
            try {
            	this.getPluginManager().activatePlugin(descr.getId());
			} catch (PluginLifecycleException e) {
				e.printStackTrace();
				// we could not load this plugin, but we shall continue searching for others
				continue;
			}
			
			// extracting parameters
			Parameter classParam = ext.getParameter(PluginCore.PARAMETER_CLASS);
			
			// extracting plugin class or builder clas
			ClassLoader classLoader = this.getPluginManager().getPluginClassLoader(descr);
            Class pluginClass = null;	// class for the plugin (InferenceAlgorithmOptionPanel)
            try {
            	 pluginClass = classLoader.loadClass(classParam.valueAsString());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				// we could not load this plugin, but we shall continue searching for others
				continue;
			}
			
            // intantiating plugin object
	    	InferenceAlgorithmOptionPanel algorithmOptionPanel = null;
	    	try {
				algorithmOptionPanel = (InferenceAlgorithmOptionPanel)pluginClass.newInstance();
			} catch (Exception e) {
				// OK. we could not load this one, but lets try others.
				e.printStackTrace();
				continue;
			} 
	    	
			// creating the radio buttons
	    	// we assume algorithm equality as class equality (if 2 algorithms uses the same class, we assume they are the same algorithm)
			JRadioButtonMenuItem radio =  new JRadioButtonMenuItem(algorithmOptionPanel.getInferenceAlgorithm().getName(), 
					(controller.getInferenceAlgorithm()!= null) && (controller.getInferenceAlgorithm().getClass().equals(algorithmOptionPanel.getInferenceAlgorithm().getClass())));
			radio.setToolTipText(algorithmOptionPanel.getInferenceAlgorithm().getDescription());
			
			
			// filling the return
			ret.put(radio, algorithmOptionPanel);
		}
	    
	    // creating action listener for each radio buttons in order to open the option panel when a radio button is choosen
	    for (JRadioButtonMenuItem radio : ret.keySet()) {
			radio.addActionListener(new PluginRadioButtonListener(ret.get(radio)));
		}
	    
    	return ret;
	}

    /**
     * Returns a map (radio button -> respective InferenceAlgorithmOptionPanel) for those "default" algorithms,
     * Those algorithms are treated like plugins, but they are not actually inside plugins directory.
     * 		Contents: {Junction Tree, Likelihood, gibbs, gaussian mixture}.
     * @return map of default plugins (usually, the default plugins are core classes).
     */
	protected Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> getDefaultAlgorithms() {
		
		Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> ret = new HashMap<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel>();
		JRadioButtonMenuItem radio = null;	// key of the map
		InferenceAlgorithmOptionPanel algorithmOptionPanel = null;	// the mapped object
		
		// we assume algorithm equality as class equality (if 2 algorithms uses the same class, we assume they are the same algorithm)
    	
    	// junction tree
    	algorithmOptionPanel = new JunctionTreeOptionPanel();
    	radio = new JRadioButtonMenuItem(algorithmOptionPanel.getInferenceAlgorithm().getName(), 
				(controller.getInferenceAlgorithm()!= null) && (controller.getInferenceAlgorithm().getClass().equals(algorithmOptionPanel.getInferenceAlgorithm().getClass())));
		radio.setToolTipText(algorithmOptionPanel.getInferenceAlgorithm().getDescription());
		ret.put(radio, algorithmOptionPanel);

    	// likelihood weighting
    	algorithmOptionPanel = new LikelihoodWeightingOptionPanel();
    	radio = new JRadioButtonMenuItem(algorithmOptionPanel.getInferenceAlgorithm().getName(), 
				(controller.getInferenceAlgorithm()!= null) && (controller.getInferenceAlgorithm().getClass().equals(algorithmOptionPanel.getInferenceAlgorithm().getClass())));
		radio.setToolTipText(algorithmOptionPanel.getInferenceAlgorithm().getDescription());
		ret.put(radio, algorithmOptionPanel);
    	
    	// gibbs
    	algorithmOptionPanel = new GibbsSamplingOptionPanel();
    	radio = new JRadioButtonMenuItem(algorithmOptionPanel.getInferenceAlgorithm().getName(), 
				(controller.getInferenceAlgorithm()!= null) && (controller.getInferenceAlgorithm().getClass().equals(algorithmOptionPanel.getInferenceAlgorithm().getClass())));
		radio.setToolTipText(algorithmOptionPanel.getInferenceAlgorithm().getDescription());
		ret.put(radio, algorithmOptionPanel);
    	
        // gaussian mixture algorithm
    	algorithmOptionPanel = new GaussianMixtureOptionPanel();
    	radio = new JRadioButtonMenuItem(algorithmOptionPanel.getInferenceAlgorithm().getName(), 
				(controller.getInferenceAlgorithm()!= null) && (controller.getInferenceAlgorithm().getClass().equals(algorithmOptionPanel.getInferenceAlgorithm().getClass())));
		radio.setToolTipText(algorithmOptionPanel.getInferenceAlgorithm().getDescription());
		ret.put(radio, algorithmOptionPanel);
        
		return ret;
	}

	/**
     * Obtains the currently selected (by j option radio button) panel for algorithm options
     * @return
     */
    private InferenceAlgorithmOptionPanel getSelectedAlgorithmOptionPanel() {
    	if (this.getAlgorithmToOptionMap() == null) {
    		return null;
    	}
		for (JRadioButtonMenuItem option : this.getAlgorithmToOptionMap().keySet()) {
			if (option.isSelected()) {
				return this.getAlgorithmToOptionMap().get(option);
			}
		}
		return null;
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


	/**
	 * @return the algorithmRadioPanel
	 */
	public JPanel getAlgorithmRadioPanel() {
		return algorithmRadioPanel;
	}


	/**
	 * @param algorithmRadioPanel the algorithmRadioPanel to set
	 */
	public void setAlgorithmRadioPanel(JPanel algorithmRadioPanel) {
		this.algorithmRadioPanel = algorithmRadioPanel;
	}


	/**
	 * @return the algorithmToOptionMap
	 */
	public Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> getAlgorithmToOptionMap() {
		return algorithmToOptionMap;
	}


	/**
	 * @param algorithmToOptionMap the algorithmToOptionMap to set
	 */
	public void setAlgorithmToOptionMap(
			Map<JRadioButtonMenuItem, InferenceAlgorithmOptionPanel> algorithmToOptionMap) {
		this.algorithmToOptionMap = algorithmToOptionMap;
	}


	/**
	 * @return the algorithmOptionPane
	 */
	public JComponent getAlgorithmOptionPane() {
		return algorithmOptionPane;
	}


	/**
	 * @param algorithmOptionPane the algorithmOptionPane to set
	 */
	public void setAlgorithmOptionPane(JComponent algorithmOptionPane) {
		this.algorithmOptionPane = algorithmOptionPane;
	}

	/**
	 * The directory where this class will search for plugins.
	 * 
	 * @return the pluginDirectory
	 */
	public String getPluginDirectory() {
		return pluginDirectory;
	}

	/**
	 * 
	 * The directory where this class will search for plugins.
	 * @param pluginDirectory the pluginDirectory to set
	 */
	public void setPluginDirectory(String pluginDirectory) {
		this.pluginDirectory = pluginDirectory;
	}

	
	/**
	 * @return the pluginManager
	 */
	public PluginManager getPluginManager() {
		return UnBBayesPluginContextHolder.getPluginManager();
	}


	
	/**
	 * The ID of the core plugin.
	 * @return the pluginCoreID
	 */
	public String getPluginCoreID() {
		return pluginCoreID;
	}

	/**
	 * The ID of the core plugin.
	 * @param pluginCoreID the pluginCoreID to set
	 */
	public void setPluginCoreID(String pluginCoreID) {
		this.pluginCoreID = pluginCoreID;
	}

	/**
	 * This is the extension point id for InferenceAlgorithm.
	 * @return the algorithmExtensionPoint
	 */
	public String getAlgorithmExtensionPoint() {
		return algorithmExtensionPoint;
	}

	/**
	 * This is the extension point id for InferenceAlgorithm.
	 * @param algorithmExtensionPoint the algorithmExtensionPoint to set
	 */
	public void setAlgorithmExtensionPoint(String algorithmExtensionPoint) {
		this.algorithmExtensionPoint = algorithmExtensionPoint;
	}

	
	/**
	 * A component aware listener for Plugin's radio buttons.
	 * It simply updates GlobalOptionsDialog depending on what
	 * "algorithms" option is called.
	 * @author Shou Matsumoto
	 *
	 */
	protected class PluginRadioButtonListener implements ActionListener {
		Component component;
		
		public PluginRadioButtonListener(Component component) {
			super();
			this.component = component;
		}

		public void actionPerformed(ActionEvent e) {
			// clear algorithm scroll pane and refills it with the current option panel
			GlobalOptionsDialog.this.getAlgorithmOptionPane().removeAll();
			GlobalOptionsDialog.this.getAlgorithmOptionPane().add(component);
			component.setVisible(true);
			GlobalOptionsDialog.this.repaint();
		}
	}
}

