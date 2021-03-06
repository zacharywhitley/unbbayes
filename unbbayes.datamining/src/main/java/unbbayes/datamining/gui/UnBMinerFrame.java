﻿/*
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
package unbbayes.datamining.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import unbbayes.controller.IconController;
import unbbayes.controller.JavaHelperController;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.gui.bayesianlearning.BayesianLearningMain;
import unbbayes.datamining.gui.evaluation.EvaluationMain;
import unbbayes.datamining.gui.evaluation.batchEvaluation.BatchEvaluationMain;
import unbbayes.datamining.gui.naivebayes.NaiveBayesMain;
import unbbayes.datamining.gui.neuralmodel.NeuralModelController;
import unbbayes.datamining.gui.neuralnetwork.NeuralNetworkController;
import unbbayes.datamining.gui.preprocessor.PreprocessorMain;
import unbbayes.datamining.gui.preprocessor.janeladiscret;
import unbbayes.gui.MDIDesktopPane;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.learning.gui.extension.LearningModule;

public class UnBMinerFrame extends JFrame {
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private JPanel contentPane;

	private MDIDesktopPane desktop = new MDIDesktopPane();

	private ImageIcon metalIcon;

	private ImageIcon motifIcon;

	private ImageIcon windowsIcon;

	private ImageIcon cascadeIcon;

	private ImageIcon tileIcon;

	private ImageIcon helpIcon;

	private ImageIcon opcaoglobalIcon;

	private int defaultStates = 40;

	private int confidenceLimit = 100;

	private String defaultLanguage = "English";

	private String defaultLaf = "Windows";

	/**
	 * Carrega o arquivo de recursos para internacionaliza��o da localidade
	 * padr�o
	 */
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.gui.resources.GuiResource.class.getName());

//	private UnBMinerFrame reference = this;

	private IconController iconController = IconController.getInstance();

	private JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

	private JToolBar jtbView;

	private JToolBar jtbPreferences;

	private JToolBar jtbWindow;

	private JToolBar jtbHelp;

	private JButton metal = new JButton();

	private JButton motif = new JButton();

	private JButton windows = new JButton();

	private JButton tile = new JButton();

	private JButton cascade = new JButton();

	private JButton help = new JButton();

	private JButton preferences = new JButton();

	private ActionListener alNaiveBayes;

	private ActionListener alPreProcessor;

	private ActionListener alId3;

	private ActionListener alEvaluation;

	private ActionListener alCnm;

	private ActionListener alC45;

	private ActionListener alBayesianLearning;

	private ActionListener alPreferences;

	private ActionListener alTbPreferences;

	private ActionListener alTbView;

	private ActionListener alTbWindow;

	private ActionListener alTbHelp;

	private ActionListener alMetal;

	private ActionListener alMotif;

	private ActionListener alWindows;

	private ActionListener alCascade;

	private ActionListener alTile;

	private ActionListener alHelp;

	private ActionListener alNeuralNetwork;

	private ActionListener alBatchEvaluation;

	private ActionListener alDiscretize;

	private JMenu lafMenu;

	private JMenu tbMenu;

	private JMenu viewMenu;
	
	private boolean loadLookAndFeelFromINI = true;

	private JCheckBoxMenuItem tbView;

	private JCheckBoxMenuItem tbPreferences;

	private JCheckBoxMenuItem tbWindow;

	private JCheckBoxMenuItem tbHelp;

	private JMenu programMenu;

	private JMenu preferencesMenu;

	private JMenu windowMenu;

	private JMenu helpMenu;

	private JMenuItem optionsItem;
	
	private GlobalOptions currentGlobalOption;
	
	/** If this frame's content pane is included to another frame, this attribute will be a reference to it */
	private JFrame upperFrame = null;

	/**
	 * constructor for the frame
	 */
	public UnBMinerFrame() {
		this(true);
	}
	
	/**
	 * Constructs the frame telling it not to load look and feel options from INI file
	 * or to dynamically change look and feel settings.
	 * @param loadLookAndFeel
	 */
	public UnBMinerFrame(boolean loadLookAndFeel) {
		this.setLoadLookAndFeelChangeable(loadLookAndFeel);
		
		openDefaultOptions();

		metalIcon = iconController.getMetalIcon();
		motifIcon = iconController.getMotifIcon();
		windowsIcon = iconController.getWindowsIcon();
		cascadeIcon = iconController.getCascadeIcon();
		tileIcon = iconController.getTileIcon();
		helpIcon = iconController.getHelpIcon();
		opcaoglobalIcon = iconController.getGlobalOptionIcon();

		createActionListeners();
		createToolBars();
		createMenu();

		contentPane = (JPanel) this.getContentPane();
		contentPane.add(topPanel, BorderLayout.NORTH);
		topPanel.setBorder(null);
		contentPane.add(new JScrollPane(desktop), BorderLayout.CENTER);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setTitle("UnBMiner");
		Options.getInstance().setNumberStatesAllowed(defaultStates);
		Options.getInstance().setConfidenceLimit(confidenceLimit);
		if (defaultLaf.equals("Metal")) {
			setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
		} else if (defaultLaf.equals("Motif")) {
			setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		} else if (defaultLaf.equals("Windows")) {
			setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void openDefaultOptions() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File(
					"DataMining.ini")));
			String header = r.readLine();
			if (header.equals("[data mining]")) { // N�mero de estados
													// permitidos
				String states = r.readLine();
				if ((states.substring(0, 17)).equals("Maximum states = ")) {
					defaultStates = Integer.parseInt(states.substring(17));
				}
				// Intervalo de confian�a
				String confidence = r.readLine();
				if ((confidence.substring(0, 19)).equals("Confidence limit = ")) {
					confidenceLimit = Integer
							.parseInt(confidence.substring(19));
				}
				// Op��o de l�ngua
				String language = r.readLine();
				if ((language.substring(0, 11)).equals("Language = ")) {
					language = language.substring(11);
					if (language.equals("English")) {
						Locale.setDefault(new Locale("en", ""));
						defaultLanguage = language;
					} else if (language.equals("Potuguese")) {
						Locale.setDefault(new Locale("pt", ""));
						defaultLanguage = language;
					}
				}
				// Op��o de look and feel
				String laf = r.readLine();
				if ((laf.substring(0, 16)).equals("Look and Feel = ")) {
					laf = laf.substring(16);
					if (laf.equals("Metal")) {
						defaultLaf = laf;
					} else if (laf.equals("Motif")) {
						defaultLaf = laf;
					} else if (laf.equals("Windows")) {
						defaultLaf = laf;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public void setLnF(String lnfName) {
		if (!isLoadLookAndFeelChangeable()) {
			// do nothing if we should not change look and feel settings.
			return;
		}
		try {
			UIManager.setLookAndFeel(lnfName);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (UnsupportedLookAndFeelException ex1) {
			System.err.println(resource
					.getString("unsupportedLookAndFeelException")
					+ lnfName);
		} catch (ClassNotFoundException ex2) {
			System.err.println(resource.getString("classNotFoundException")
					+ lnfName);
		} catch (InstantiationException ex3) {
			System.err.println(resource.getString("instanciationException")
					+ lnfName);
		} catch (IllegalAccessException ex4) {
			System.err.println(resource.getString("illegalAccessException")
					+ lnfName);
		}
	}

	/**
	 * Retorna a janela que est� selecionada.
	 * 
	 * @return janela que est� selecionada.
	 */
	public JInternalFrame getSelectedWindow() {
		return desktop.getSelectedFrame();
	}

	/**
	 * Adiciona uma nova janela.
	 * 
	 * @param newWindow
	 *            nova janela.
	 */
	public void addWindow(JInternalFrame newWindow) {
		desktop.add(newWindow);
	}

	/**
	 * Method responsible for creating all ActionListeners needed.
	 */
	public void createActionListeners() {

		// create an ActionListener for opening new window for Naive Bayes
		alNaiveBayes = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				NaiveBayesMain naive = new NaiveBayesMain();
				addWindow(naive);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Naive Bayes
		alPreferences = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (currentGlobalOption == null) {
					currentGlobalOption = new GlobalOptions();
					currentGlobalOption.setDefaultOptions(Options.getInstance()
							.getNumberStatesAllowed(), Options.getInstance()
							.getConfidenceLimit(), defaultLanguage, defaultLaf);
				}
				addWindow(currentGlobalOption);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for
		// InitializePreprocessors
		alPreProcessor = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				PreprocessorMain preprocessor = new PreprocessorMain();
				addWindow(preprocessor);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for ID3
		alId3 = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				unbbayes.datamining.gui.id3.DecisionTreeMain id3 = new unbbayes.datamining.gui.id3.DecisionTreeMain();
				addWindow(id3);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for C4.5
		alC45 = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				unbbayes.datamining.gui.c45.DecisionTreeMain c45 = new unbbayes.datamining.gui.c45.DecisionTreeMain(
						desktop);
				addWindow(c45);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Evaluation
		alEvaluation = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				EvaluationMain evaluation = new EvaluationMain();
				addWindow(evaluation);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Combinatorial
		// Neural Model
		alCnm = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				NeuralModelController cnm = new NeuralModelController();
				addWindow(cnm.getCnmFrame());
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Combinatorial
		// Neural Model
		alNeuralNetwork = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				NeuralNetworkController neuralNetworkContoller = new NeuralNetworkController();
				addWindow(neuralNetworkContoller.getMainFrame());
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Bayesian Learning
		alBayesianLearning = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					if ( ( getUpperFrame() != null ) 
					  && ( getUpperFrame() instanceof UnBBayesFrame ) ) {
						// delegating to Learning plugin, if UnBMiner was incorporated as plugin
						LearningModule bayesianLearning = new LearningModule();
						bayesianLearning.setUnbbayesFrame((UnBBayesFrame)getUpperFrame());
					} else {
						// delegating to original BayesianLearningMain
						// FIXME BayesianLearningMain is not working at all
						BayesianLearningMain bayesianLearning = new BayesianLearningMain();
					}
				} catch (Throwable e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(UnBMinerFrame.this, 
							e.getMessage(), 
							"", 
							JOptionPane.ERROR_MESSAGE); 
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for Batch Evaluation
		alBatchEvaluation = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				BatchEvaluationMain batchEvaluation = new BatchEvaluationMain();
				addWindow(batchEvaluation);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for showing the View Tool Bar
		alTbView = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbView);
				} else {
					topPanel.remove(jtbView);
				}
				// lay out its subcomponents again after an container has been
				// added, removed or modified
				validate();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for showing the View Tool Bar
		alTbPreferences = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbPreferences);
				} else {
					topPanel.remove(jtbPreferences);
				}
				// lay out its subcomponents again after an container has been
				// added, removed or modified
				validate();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for showing the Window Tool Bar
		alTbWindow = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbWindow);
				} else {
					topPanel.remove(jtbWindow);
				}
				// lay out its subcomponents again after an container has been
				// added, removed or modified
				validate();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for showing the Help Tool Bar
		alTbHelp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbHelp);
				} else {
					topPanel.remove(jtbHelp);
				}
				// lay out its subcomponents again after an container has been
				// added, removed or modified
				validate();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for choosing Metal Look and Feel
		alMetal = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for choosing Motif Look and Feel
		alMotif = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for choosing Windows Look and Feel
		alWindows = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		// create an ActionListener for ordering windows as cascade
		alCascade = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				desktop.cascadeFrames();
			}
		};

		// create an ActionListener for ordering windows as tile
		alTile = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				desktop.tileFrames();
			}
		};

		// create an ActionListener for opening the Help window
		alHelp = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				try {
					JavaHelperController.getInstance().openHelp(UnBMinerFrame.this);
				} catch (Exception evt) {
					System.out.println("Error= " + evt.getMessage() + " "
							+ this.getClass().getName());
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

		alDiscretize = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// setCursor(new Cursor(Cursor.WAIT_CURSOR));
				janeladiscret janela = new janeladiscret();
				janela.setVisible(true);
				/*
				 * String[] nets = new String[] { "txt", "arff" }; chooser = new
				 * JFileChooser(fileController.getCurrentDirectory());
				 * chooser.setMultiSelectionEnabled(false);
				 * chooser.setFileSelectionMode
				 * (JFileChooser.FILES_AND_DIRECTORIES); // adicionar FileView
				 * no FileChooser para desenhar �cones de // arquivos
				 * chooser.setFileView(new FileIcon(IUnBBayes.this));
				 * 
				 * chooser.addChoosableFileFilter( new SimpleFileFilter( nets,
				 * "")); int option = chooser.showOpenDialog(null); if (option
				 * == JFileChooser.APPROVE_OPTION) { if
				 * (chooser.getSelectedFile() != null) { //at� agora o c�digo
				 * era semelhante ao do comando abrir
				 * 
				 * 
				 * 
				 * //agora o final do codigo � igual ao do comando abrir } }
				 * setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				 */
			}
		};

	}

	/**
	 * Method responsible for creating the menu used in this class, JFrame.
	 */
	public void createMenu() {
		JMenuBar menu = new JMenuBar();

		// create menus and set their mnemonic
		programMenu = new JMenu(resource.getString("selectProgram"));
		lafMenu = new JMenu(resource.getString("lookAndFeel"));
		viewMenu = new JMenu(resource.getString("view"));
		tbMenu = new JMenu(resource.getString("toolsbar"));
		preferencesMenu = new JMenu(resource.getString("globalOptions"));
		windowMenu = new JMenu(resource.getString("window"));
		helpMenu = new JMenu(resource.getString("help"));

		programMenu.setMnemonic(((Character) resource
				.getObject("selectMnemonic")).charValue());
		lafMenu.setMnemonic(((Character) resource.getObject("lafMnemonic"))
				.charValue());
		preferencesMenu.setMnemonic(((Character) resource
				.getObject("globalOptionsMnemonic")).charValue());
		viewMenu.setMnemonic(((Character) resource.getObject("viewMnemonic"))
				.charValue());
		windowMenu.setMnemonic(((Character) resource
				.getObject("windowMnemonic")).charValue());
		helpMenu.setMnemonic(((Character) resource.getObject("helpMnemonic"))
				.charValue());
		tbMenu.setMnemonic(((Character) resource.getObject("tbMenuMnemonic"))
				.charValue());

		// create menu items, set their mnemonic and their key accelerator
		JMenuItem preprocessorItem = new JMenuItem(resource
				.getString("instancesPreprocessor")/* , icon */);
		JMenuItem id3Item = new JMenuItem(resource.getString("id3Classifier")/*
																			 * ,
																			 * icon
																			 */);
		JMenuItem naiveBayesItem = new JMenuItem(resource
				.getString("naiveBayesClassifier")/* , icon */);
		JMenuItem evaluationItem = new JMenuItem(resource
				.getString("evaluation")/* , icon */);
		optionsItem = new JMenuItem(
				resource.getString("preferences"), opcaoglobalIcon);
		JMenuItem discretizeItem = new JMenuItem("Discretizar");
		// ///////////
		JMenuItem cnmItem = new JMenuItem("Combinatorial Neural Model"/* , icon */);
		JMenuItem c45Item = new JMenuItem("C4.5 Classifier"/* , icon */);
		JMenuItem bayesianItem = new JMenuItem("Bayesian Learning"/* , icon */);
		JMenuItem neuralNetworkItem = new JMenuItem("Neural Network"/* , icon */);

		JMenuItem batchEvaluationItem;
		batchEvaluationItem = new JMenuItem(resource
				.getString("batchEvaluation"));

		// /////////
		JMenuItem metalItem = new JMenuItem("Metal", metalIcon);
		JMenuItem motifItem = new JMenuItem("Motif", motifIcon);
		JMenuItem windowsItem = new JMenuItem("Windows", windowsIcon);
		JMenuItem cascadeItem = new JMenuItem(resource.getString("cascade"),
				cascadeIcon);
		JMenuItem tileItem = new JMenuItem(resource.getString("tile"), tileIcon);
		JMenuItem helpItem = new JMenuItem(resource.getString("helpTopics"),
				helpIcon);

		tbPreferences = new JCheckBoxMenuItem(resource
				.getString("tbPreferences"), true);
		tbView = new JCheckBoxMenuItem(resource.getString("tbView"),
				true);
		tbWindow = new JCheckBoxMenuItem(resource
				.getString("tbWindow"), true);
		tbHelp = new JCheckBoxMenuItem(resource.getString("tbHelp"),
				true);

		preprocessorItem.setMnemonic(((Character) resource
				.getObject("preprocessorMnemonic")).charValue());
		id3Item.setMnemonic(((Character) resource.getObject("id3Mnemonic"))
				.charValue());
		naiveBayesItem.setMnemonic(((Character) resource
				.getObject("naiveBayesMnemonic")).charValue());
		evaluationItem.setMnemonic(((Character) resource
				.getObject("evaluationMnemonic")).charValue());
		optionsItem.setMnemonic(((Character) resource
				.getObject("preferencesMnemonic")).charValue());
		// /////////////
		cnmItem.setMnemonic('N');
		c45Item.setMnemonic('C');
		bayesianItem.setMnemonic('B');

		batchEvaluationItem.setMnemonic(((Character) resource
				.getObject("batchEvaluationMnemonic")).charValue());

		// ////////////
		metalItem.setMnemonic('M');
		motifItem.setMnemonic('O');
		windowsItem.setMnemonic('W');
		cascadeItem.setMnemonic(((Character) resource
				.getObject("cascadeMnemonic")).charValue());
		tileItem.setMnemonic(((Character) resource.getObject("tileMnemonic"))
				.charValue());
		helpItem.setMnemonic(((Character) resource
				.getObject("helpTopicsMnemonic")).charValue());

		tbPreferences.setMnemonic(((Character) resource
				.getObject("tbPreferencesMnemonic")).charValue());
		tbView.setMnemonic(((Character) resource.getObject("tbViewMnemonic"))
				.charValue());
		tbWindow.setMnemonic(((Character) resource
				.getObject("tbWindowMnemonic")).charValue());
		tbHelp.setMnemonic(((Character) resource.getObject("tbHelpMnemonic"))
				.charValue());
		/*
		 * newBN.setAccelerator(
		 * KeyStroke.getKeyStroke(resource.getString("newItemMn").charAt(0),
		 * Event.CTRL_MASK, false)); openItem.setAccelerator(
		 * KeyStroke.getKeyStroke(resource.getString("openItemMn").charAt(0),
		 * Event.CTRL_MASK, false)); saveItem.setAccelerator(
		 * KeyStroke.getKeyStroke(resource.getString("saveItemMn").charAt(0),
		 * Event.CTRL_MASK, false)); learningItem.setAccelerator(
		 * KeyStroke.getKeyStroke
		 * (resource.getString("learningItemMn").charAt(0), Event.CTRL_MASK,
		 * false)); helpItem.setAccelerator(
		 * KeyStroke.getKeyStroke(resource.getString("helpItemMn").charAt(0),
		 * Event.CTRL_MASK, false));
		 */

		// add ActionListener to all menu items
		tbPreferences.addActionListener(alTbPreferences);
		tbView.addActionListener(alTbView);
		tbWindow.addActionListener(alTbWindow);
		tbHelp.addActionListener(alTbHelp);
		preprocessorItem.addActionListener(alPreProcessor);
		id3Item.addActionListener(alId3);
		c45Item.addActionListener(alC45);
		naiveBayesItem.addActionListener(alNaiveBayes);
		evaluationItem.addActionListener(alEvaluation);
		cnmItem.addActionListener(alCnm);
		neuralNetworkItem.addActionListener(alNeuralNetwork);
		batchEvaluationItem.addActionListener(alBatchEvaluation);
		bayesianItem.addActionListener(alBayesianLearning);
		metalItem.addActionListener(alMetal);
		motifItem.addActionListener(alMotif);
		windowsItem.addActionListener(alWindows);
		cascadeItem.addActionListener(alCascade);
		tileItem.addActionListener(alTile);
		helpItem.addActionListener(alHelp);
		optionsItem.addActionListener(alPreferences);
		discretizeItem.addActionListener(alDiscretize);

		// add menu items to their respective menu
		programMenu.add(preprocessorItem);
		programMenu.add(id3Item);
		programMenu.add(c45Item);//
		programMenu.add(naiveBayesItem);
		programMenu.add(bayesianItem);//
		programMenu.add(cnmItem);
		programMenu.add(neuralNetworkItem);//
		programMenu.add(evaluationItem);
		programMenu.add(discretizeItem);
		programMenu.add(batchEvaluationItem);
		lafMenu.add(metalItem);
		lafMenu.add(motifItem);
		lafMenu.add(windowsItem);
		tbMenu.add(tbView);
		tbMenu.add(tbPreferences);
		tbMenu.add(tbWindow);
		tbMenu.add(tbHelp);
		viewMenu.add(tbMenu);
		viewMenu.addSeparator();
		viewMenu.add(lafMenu);
		windowMenu.add(cascadeItem);
		windowMenu.add(tileItem);
		helpMenu.add(helpItem);
		preferencesMenu.add(optionsItem);

		menu.add(programMenu);
		menu.add(viewMenu);
		menu.add(preferencesMenu);
		menu.add(windowMenu);
		menu.add(helpMenu);

		this.setJMenuBar(menu);
	}

	/**
	 * Call the method for creating the needed buttons and then create the tool
	 * bars and add the buttons to them and finally to the topPanel.
	 */
	public void createToolBars() {

		createButtons();

		// create tool bars
		jtbView = new JToolBar();
		jtbView.setFloatable(false);
		jtbPreferences = new JToolBar();
		jtbPreferences.setFloatable(false);
		jtbWindow = new JToolBar();
		jtbWindow.setFloatable(false);
		jtbHelp = new JToolBar();
		jtbHelp.setFloatable(false);

		// add their buttons
		jtbView.add(metal);
		jtbView.add(motif);
		jtbView.add(windows);
		jtbPreferences.add(preferences);
		jtbWindow.add(cascade);
		jtbWindow.add(tile);
		jtbHelp.add(help);

		// add the tool bars to the topPanel
		topPanel.add(jtbView);
		topPanel.add(jtbPreferences);
		topPanel.add(jtbWindow);
		topPanel.add(jtbHelp);
	}

	/**
	 * Create the needed buttons and add their respectinve tool tip.
	 */
	public void createButtons() {
		createButton(metal, metalIcon, "", alMetal);
		createButton(motif, motifIcon, "", alMotif);
		createButton(windows, windowsIcon, "", alWindows);
		createButton(tile, tileIcon, "", alTile);
		createButton(cascade, cascadeIcon, "", alCascade);
		createButton(help, helpIcon, "", alHelp);
		createButton(preferences, opcaoglobalIcon, "", alPreferences);
	}

	private void createButton(JButton button, ImageIcon icon, String toolTip,
			ActionListener al) {
		button.setIcon(icon);
		button.setToolTipText(toolTip);
		button.addActionListener(al);
	}

	/**
	 * @return the jtbView
	 */
	public JToolBar getJtbView() {
		return jtbView;
	}

	/**
	 * @param jtbView the jtbView to set
	 */
	public void setJtbView(JToolBar jtbView) {
		this.jtbView = jtbView;
	}

	/**
	 * @return the jtbPreferences
	 */
	public JToolBar getJtbPreferences() {
		return jtbPreferences;
	}

	/**
	 * @param jtbPreferences the jtbPreferences to set
	 */
	public void setJtbPreferences(JToolBar jtbPreferences) {
		this.jtbPreferences = jtbPreferences;
	}

	/**
	 * @return the jtbWindow
	 */
	public JToolBar getJtbWindow() {
		return jtbWindow;
	}

	/**
	 * @param jtbWindow the jtbWindow to set
	 */
	public void setJtbWindow(JToolBar jtbWindow) {
		this.jtbWindow = jtbWindow;
	}

	/**
	 * @return the jtbHelp
	 */
	public JToolBar getJtbHelp() {
		return jtbHelp;
	}

	/**
	 * @param jtbHelp the jtbHelp to set
	 */
	public void setJtbHelp(JToolBar jtbHelp) {
		this.jtbHelp = jtbHelp;
	}

	/**
	 * @return the lafMenu
	 */
	public JMenu getLafMenu() {
		return lafMenu;
	}

	/**
	 * @param lafMenu the lafMenu to set
	 */
	public void setLafMenu(JMenu lafMenu) {
		this.lafMenu = lafMenu;
	}

	/**
	 * @return the viewMenu
	 */
	public JMenu getViewMenu() {
		return viewMenu;
	}

	/**
	 * @param viewMenu the viewMenu to set
	 */
	public void setViewMenu(JMenu viewMenu) {
		this.viewMenu = viewMenu;
	}

	/**
	 * @return the tbMenu
	 */
	public JMenu getTbMenu() {
		return tbMenu;
	}

	/**
	 * @param tbMenu the tbMenu to set
	 */
	public void setTbMenu(JMenu tbMenu) {
		this.tbMenu = tbMenu;
	}

	/**
	 * @return the loadLookAndFeelFromINI
	 */
	public boolean isLoadLookAndFeelChangeable() {
		return loadLookAndFeelFromINI;
	}

	/**
	 * @param loadLookAndFeelFromINI the loadLookAndFeelFromINI to set
	 */
	public void setLoadLookAndFeelChangeable(boolean loadLookAndFeelFromINI) {
		this.loadLookAndFeelFromINI = loadLookAndFeelFromINI;
	}

	/**
	 * @return the tbView
	 */
	public JCheckBoxMenuItem getTbView() {
		return tbView;
	}

	/**
	 * @param tbView the tbView to set
	 */
	public void setTbView(JCheckBoxMenuItem tbView) {
		this.tbView = tbView;
	}

	/**
	 * @return the tbPreferences
	 */
	public JCheckBoxMenuItem getTbPreferences() {
		return tbPreferences;
	}

	/**
	 * @param tbPreferences the tbPreferences to set
	 */
	public void setTbPreferences(JCheckBoxMenuItem tbPreferences) {
		this.tbPreferences = tbPreferences;
	}

	/**
	 * @return the tbWindow
	 */
	public JCheckBoxMenuItem getTbWindow() {
		return tbWindow;
	}

	/**
	 * @param tbWindow the tbWindow to set
	 */
	public void setTbWindow(JCheckBoxMenuItem tbWindow) {
		this.tbWindow = tbWindow;
	}

	/**
	 * @return the tbHelp
	 */
	public JCheckBoxMenuItem getTbHelp() {
		return tbHelp;
	}

	/**
	 * @param tbHelp the tbHelp to set
	 */
	public void setTbHelp(JCheckBoxMenuItem tbHelp) {
		this.tbHelp = tbHelp;
	}

	/**
	 * @return the programMenu
	 */
	public JMenu getProgramMenu() {
		return programMenu;
	}

	/**
	 * @param programMenu the programMenu to set
	 */
	public void setProgramMenu(JMenu programMenu) {
		this.programMenu = programMenu;
	}

	/**
	 * @return the preferencesMenu
	 */
	public JMenu getPreferencesMenu() {
		return preferencesMenu;
	}

	/**
	 * @param preferencesMenu the preferencesMenu to set
	 */
	public void setPreferencesMenu(JMenu preferencesMenu) {
		this.preferencesMenu = preferencesMenu;
	}

	/**
	 * @return the windowMenu
	 */
	public JMenu getWindowMenu() {
		return windowMenu;
	}

	/**
	 * @param windowMenu the windowMenu to set
	 */
	public void setWindowMenu(JMenu windowMenu) {
		this.windowMenu = windowMenu;
	}

	/**
	 * @return the helpMenu
	 */
	public JMenu getHelpMenu() {
		return helpMenu;
	}

	/**
	 * @param helpMenu the helpMenu to set
	 */
	public void setHelpMenu(JMenu helpMenu) {
		this.helpMenu = helpMenu;
	}

	/**
	 * @return the preferences
	 */
	public JButton getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences the preferences to set
	 */
	public void setPreferences(JButton preferences) {
		this.preferences = preferences;
	}

	/**
	 * @return the optionsItem
	 */
	public JMenuItem getOptionsItem() {
		return optionsItem;
	}

	/**
	 * @param optionsItem the optionsItem to set
	 */
	public void setOptionsItem(JMenuItem optionsItem) {
		this.optionsItem = optionsItem;
	}

	/**
	 * @return the alPreferences
	 */
	public ActionListener getAlPreferences() {
		return alPreferences;
	}

	/**
	 * @param alPreferences the alPreferences to set
	 */
	public void setAlPreferences(ActionListener alPreferences) {
		this.alPreferences = alPreferences;
	}

	/**
	 * @return the defaultLanguage
	 */
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * @param defaultLanguage the defaultLanguage to set
	 */
	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	/**
	 * @return the currentGlobalOption
	 */
	public GlobalOptions getCurrentGlobalOption() {
		return currentGlobalOption;
	}

	/**
	 * @param currentGlobalOption the currentGlobalOption to set
	 */
	public void setCurrentGlobalOption(GlobalOptions currentGlobalOption) {
		this.currentGlobalOption = currentGlobalOption;
	}

	/**
	 * If this frame's content pane is included to another frame, this attribute will be a reference to it.
	 * If set to null, then this frame's content pane is not included to another frame.
	 * OBS. inserting a frame's content pane into another frame is a way to simulate a frame containing another frame.
	 * @return
	 */
	public JFrame getUpperFrame() {
		return upperFrame;
	}

	/**
	 * If this frame's content pane is included to another frame, this attribute will be a reference to it.
	 * If set to null, then this frame's content pane is not included to another frame.
	 * OBS. inserting a frame's content pane into another frame is a way to simulate a frame containing another frame.
	 * @return
	 */
	public void setUpperFrame(JFrame upperFrame) {
		this.upperFrame = upperFrame;
	}
}