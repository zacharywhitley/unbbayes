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

	private String defaultLanguage = "Portuguese";

	private String defaultLaf = "Windows";

	/**
	 * Carrega o arquivo de recursos para internacionaliza��o da localidade
	 * padr�o
	 */
	private ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.datamining.gui.resources.GuiResource");

	private UnBMinerFrame reference = this;

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

	// Construct the frame
	public UnBMinerFrame() {
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

	private void setLnF(String lnfName) {
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
				GlobalOptions options = new GlobalOptions();
				options.setDefaultOptions(Options.getInstance()
						.getNumberStatesAllowed(), Options.getInstance()
						.getConfidenceLimit(), defaultLanguage, defaultLaf);
				addWindow(options);
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
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				BayesianLearningMain bayesianLearning = new BayesianLearningMain();
				addWindow(bayesianLearning);
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
					JavaHelperController.getInstance().openHelp(reference);
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
		JMenu programMenu = new JMenu(resource.getString("selectProgram"));
		JMenu lafMenu = new JMenu(resource.getString("lookAndFeel"));
		JMenu viewMenu = new JMenu(resource.getString("view"));
		JMenu tbMenu = new JMenu(resource.getString("toolsbar"));
		JMenu preferencesMenu = new JMenu(resource.getString("globalOptions"));
		JMenu windowMenu = new JMenu(resource.getString("window"));
		JMenu helpMenu = new JMenu(resource.getString("help"));

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
		JMenuItem optionsItem = new JMenuItem(
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

		JMenuItem tbPreferences = new JCheckBoxMenuItem(resource
				.getString("tbPreferences"), true);
		JMenuItem tbView = new JCheckBoxMenuItem(resource.getString("tbView"),
				true);
		JMenuItem tbWindow = new JCheckBoxMenuItem(resource
				.getString("tbWindow"), true);
		JMenuItem tbHelp = new JCheckBoxMenuItem(resource.getString("tbHelp"),
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
}