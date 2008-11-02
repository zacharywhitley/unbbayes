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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.aprendizagem.incrementalLearning.ILBridge;
import unbbayes.controller.ConfigurationsController;
import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.JavaHelperController;
import unbbayes.controller.MainController;
import unbbayes.io.mebn.UbfIO;
import unbbayes.simulation.montecarlo.controller.MCMainController;

/**
 * This class extends <code>JFrame</code> and it is responsible for 
 * the UnBBayes main panel.
 * 
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 * @author Michael S. Onishi (mso@gmail.com)
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @created 27 de Junho de 2001
 * 
 * @see JFrame
 */
public class UnBBayesFrame extends JFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private MDIDesktopPane desktop;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JToolBar jtbFile;
	private JToolBar jtbView;
	private JToolBar jtbTools;
	private JToolBar jtbWindow;
	// TODO CHANGE HELP
	private JToolBar jtbHelp;
	private MainController controller;

	private JButton newNet;
	private JButton newMsbn;
	private JButton newMebn;
	private JButton newOobn;
	private JButton openNet;
	private JButton saveNet;

	private JButton learn;
	private JButton metal;
	private JButton motif;
	private JButton windows;
	private JButton tile;
	private JButton cascade;
	private JButton help;
	private JButton about; 
	
	 private URL helpSetURL;
	 private HelpSet set;
	private JHelp jHelp;
	private ActionListener alNewBN;
	
	private ActionListener alTAN;
	private ActionListener alBAN;
	private ActionListener alNewMSBN;
	private ActionListener alNewMEBN;
	private ActionListener alNewOOBN;
	private ActionListener alOpen;
	private ActionListener alSave;
	private ActionListener alExit;
	private ActionListener alTbFile;
	private ActionListener alTbView;
	private ActionListener alTbTools;
	private ActionListener alTbWindow;
	private ActionListener alTbHelp;
	private ActionListener alMetal;
	private ActionListener alMotif;
	private ActionListener alWindows;
	private ActionListener alLearn;
	private ActionListener alCascade;
	private ActionListener alTile;
	private ActionListener alHelp;
	private ActionListener alAbout;
	private ActionListener alMonteCarlo;
	private ActionListener alGibbs;
	private ActionListener alIL;

	private JFileChooser chooser;
	private FileController fileController;

	protected IconController iconController = IconController.getInstance();

	private static UnBBayesFrame singleton = null;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.gui.resources.GuiResources");

	/**
	 * Build the main program frame, adjusting the layouts and building the 
	 * buttons, labels, and others elements of the interface. Create too the 
	 * listeners. 
	 * 
	 * @see ActionListener
	 * @see MouseListener
	 * @see KeyListener
	 */
	public UnBBayesFrame(MainController _controller) {
		super(resource.getString("unbbayesTitle"));
		this.controller = _controller;
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new ListenerCloserFrame()); 
		
		fileController = FileController.getInstance();

		Container contentPane = getContentPane();

		// instantiate panels
		desktop = new MDIDesktopPane();
		topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));

		createActionListeners();
		createMenu();
		createToolBars();
		assignActionListeners();

		// add panels to the content pane
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(desktop), BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		this.setIconImage(iconController.getUnBBayesIcon().getImage());
		
		setLocationByPlatform(true); 
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setVisible(true);

		singleton = this;
	}

	private void setLnF(String lnfName) {
		try {
			UIManager.setLookAndFeel(lnfName);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (UnsupportedLookAndFeelException ex1) {
			System.err.println(resource
					.getString("LookAndFeelUnsupportedException")
					+ lnfName);
		} catch (ClassNotFoundException ex2) {
			System.err.println(resource
					.getString("LookAndFeelClassNotFoundException")
					+ lnfName);
		} catch (InstantiationException ex3) {
			System.err.println(resource
					.getString("LookAndFeelInstantiationException")
					+ lnfName);
		} catch (IllegalAccessException ex4) {
			System.err.println(resource
					.getString("LookAndFeelIllegalAccessException")
					+ lnfName);
		}
	}

	/**
	 * Returns the selected window.
	 * 
	 * @return the selected window.
	 */
	public JInternalFrame getSelectedWindow() {
		return desktop.getSelectedFrame();
	}

	/**
	 * Adds a new window.
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

		// create an ActionListener for opening new window for BN
		alNewBN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				controller.newPN();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for MSBN
		alNewMSBN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				controller.newMSBN();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for opening new window for MEBN
		alNewMEBN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				controller.newMEBN();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};
		
		// create an ActionListener for opening new window for OOBN
		alNewOOBN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				controller.newOOBN();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		alTAN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String[] nets = new String[] { "txt" };
				int classe = 0;
				chooser = new JFileChooser(fileController.getCurrentDirectory());
				chooser.setMultiSelectionEnabled(false);
				chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
						resource.getString("textFileFilter")));
				int option = chooser.showOpenDialog(UnBBayesFrame.this);
				File file;
				if (option == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					fileController.setCurrentDirectory(chooser
							.getCurrentDirectory());
					new ConstructionController(file, controller, classe);
				}

			}
		};		
		alBAN = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String[] nets = new String[] { "txt" };
				int classe = 0;
				chooser = new JFileChooser(fileController.getCurrentDirectory());
				chooser.setMultiSelectionEnabled(false);
				chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
						resource.getString("textFileFilter")));
				int option = chooser.showOpenDialog(UnBBayesFrame.this);
				File file;
				if (option == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					fileController.setCurrentDirectory(chooser
							.getCurrentDirectory());
					new ConstructionController(file, controller, classe, true);
				}

			}
		};

		

		// create an ActionListener for loading
		alOpen = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// String[] nets = new String[] { "net", "xml", "owl" };
				String[] nets = new String[] { "net", "xml", "owl",
						UbfIO.fileExtension };
				chooser = new JFileChooser(fileController.getCurrentDirectory());
				chooser.setDialogTitle(resource.getString("openTitle")); 
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				// adicionar FileView no FileChooser para desenhar �cones de
				// arquivos
				chooser.setFileView(new FileIcon(UnBBayesFrame.this));

				chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
						resource.getString("netFileFilter")));
				
				
				int option = chooser.showOpenDialog(UnBBayesFrame.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile() != null) {
						chooser.setVisible(false); 
						UnBBayesFrame.this.repaint(); 
						File file = chooser.getSelectedFile(); 
						fileController.setCurrentDirectory(chooser
								.getCurrentDirectory());
					    chooser.setVisible(false); 
					    chooser.setEnabled(false);
					    
						controller.loadNet(file);
					}
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for saving
		alSave = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// String[] nets = new String[] { "net", "xml", "owl" };
				String[] nets = new String[] { "net", "xml",
						UbfIO.fileExtension };

				chooser = new JFileChooser(fileController.getCurrentDirectory());
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setDialogTitle(resource.getString("saveTitle")); 
				
				// adicionar FileView no FileChooser para desenhar �cones de
				// arquivos
				chooser.setFileView(new FileIcon(UnBBayesFrame.this));
				chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
						resource.getString("netFileFilterSave")));
				int option = chooser.showSaveDialog(null);
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file != null) {
						if (file.isFile()) {
							// String name = file.getName();
							/*
							 * if (! name.endsWith(".net")) { file = new
							 * File(file.getAbsoluteFile() + ".net");
							 * fileController.setCurrentDirectory(chooser.getCurrentDirectory()); }
							 */
						}
						controller.saveNet(file);
					}
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for exiting
		alExit = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
				System.exit(0);
			}
		};

		// create an ActionListener for opening new window for learning
		alLearn = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String[] nets = new String[] { "txt" };
				chooser = new JFileChooser(fileController.getCurrentDirectory());
				chooser.setMultiSelectionEnabled(false);
				chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
						resource.getString("textFileFilter")));
				int option = chooser.showOpenDialog(UnBBayesFrame.this);
				File file;
				if (option == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					fileController.setCurrentDirectory(chooser
							.getCurrentDirectory());
					new ConstructionController(file, controller);
				}
			}
		};

		alIL = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				new ILBridge(controller);
			}
		};

		alMonteCarlo = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// ControladorPrincipal cp = new ControladorPrincipal();
				new MCMainController();
			}
		};

		alGibbs = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			}
		};

		// create an ActionListener for showing the File Tool Bar
		alTbFile = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbFile);
				} else {
					topPanel.remove(jtbFile);
				}
				// lay out its subcomponents again after an container has been
				// added, removed or modified
				validate();
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

		// create an ActionListener for showing the Tools Tool Bar
		alTbTools = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (((JCheckBoxMenuItem) e.getSource()).getState()) {
					topPanel.add(jtbTools);
				} else {
					topPanel.remove(jtbTools);
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
				try {
					JavaHelperController.getInstance().openHelp(singleton);
				} catch (Exception evt) {
					System.out.println("Error= " + evt.getMessage() + " "
							+ this.getClass().getName());
					evt.printStackTrace();
				}
			}
		};
		
		alAbout = new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
    			
		        javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	showAboutPane();
		            }
		        });

			}
		};
	}
	
	private class OpenFileListener implements ActionListener{

		private final File file; 
		
		OpenFileListener(File file){
			this.file = file; 
		}
		
		public void actionPerformed(ActionEvent e) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			controller.loadNet(file);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
	}

	private void showAboutPane(){
		AboutPane abourPane = new AboutPane();
		abourPane.pack();
		abourPane.setVisible(true);
	}
	
	/**
	 * Method responsible for creating the menu used in this class, JFrame.
	 */
	public void createMenu() {
		JMenuBar menu = new JMenuBar();

		// create menus and set their mnemonic
		JMenu fileMenu = new JMenu(resource.getString("fileMenu"));
		JMenu recentFilesMenu = new JMenu(resource.getString("recentFilesMenu"));
		JMenu lafMenu = new JMenu(resource.getString("lafMenu"));
		JMenu viewMenu = new JMenu(resource.getString("viewMenu"));
		JMenu tbMenu = new JMenu(resource.getString("tbMenu"));
		JMenu newMenu = new JMenu(resource.getString("newMenu"));
		JMenu toolsMenu = new JMenu(resource.getString("toolsMenu"));
		JMenu windowMenu = new JMenu(resource.getString("windowMenu"));
		JMenu helpMenu = new JMenu(resource.getString("helpMenu"));
		
		fileMenu.setMnemonic(resource.getString("fileMenuMn").charAt(0));
		recentFilesMenu.setMnemonic(resource.getString("recentFilesMn").charAt(0));
		newMenu.setMnemonic(resource.getString("newMenuMn").charAt(0)); 
		lafMenu.setMnemonic(resource.getString("lafMenuMn").charAt(0));
		viewMenu.setMnemonic(resource.getString("viewMenuMn").charAt(0));
		tbMenu.setMnemonic(resource.getString("tbMenuMn").charAt(0));
		toolsMenu.setMnemonic(resource.getString("toolsMenuMn").charAt(0));
		windowMenu.setMnemonic(resource.getString("windowMenuMn").charAt(0));
		helpMenu.setMnemonic(resource.getString("helpMenuMn").charAt(0));

		// create menu items, set their mnemonic and their key accelerator
		JMenuItem newBN = new JMenuItem(resource.getString("newBN"),
				iconController.getNewIcon());
		JMenuItem newMSBN = new JMenuItem(resource.getString("newMSBN"),
				iconController.getNewIcon());
		JMenuItem newMEBN = new JMenuItem(resource.getString("newMEBN"),
				iconController.getNewIcon());
		
		JMenuItem newOOBN = new JMenuItem(resource.getString("newOOBN"),
				iconController.getNewIcon());

		newBN.setMnemonic(resource.getString("newBNMn").charAt(0));
		newMSBN.setMnemonic(resource.getString("newMSBNMn").charAt(0));
		newMEBN.setMnemonic(resource.getString("newMEBNMn").charAt(0));
		newOOBN.setMnemonic(resource.getString("newOOBNMn").charAt(0));
		
		JMenuItem openItem = new JMenuItem(resource.getString("openItem"),
				iconController.getOpenIcon());
		JMenuItem saveItem = new JMenuItem(resource.getString("saveItem"),
				iconController.getSaveIcon());
		JMenuItem exitItem = new JMenuItem(resource.getString("exitItem"), 'X');
		JMenuItem tbFile = new JCheckBoxMenuItem(resource.getString("tbFile"),
				true);
		JMenuItem tbView = new JCheckBoxMenuItem(resource.getString("tbView"),
				true);
		JMenuItem tbTools = new JCheckBoxMenuItem(
				resource.getString("tbTools"), true);
		JMenuItem tbWindow = new JCheckBoxMenuItem(resource
				.getString("tbWindow"), true);
		JMenuItem tbHelp = new JCheckBoxMenuItem(resource.getString("tbHelp"),
				true);
		

		
		JMenuItem metalItem = new JMenuItem(resource.getString("metalItem"),
				iconController.getMetalIcon());
		JMenuItem motifItem = new JMenuItem(resource.getString("motifItem"),
				iconController.getMotifIcon());
		JMenuItem windowsItem = new JMenuItem(
				resource.getString("windowsItem"), iconController
						.getWindowsIcon());
		JMenuItem cascadeItem = new JMenuItem(
				resource.getString("cascadeItem"), iconController
						.getCascadeIcon());
		JMenuItem tileItem = new JMenuItem(resource.getString("tileItem"),
				iconController.getTileIcon());
		JMenuItem helpItem = new JMenuItem(resource.getString("helpItem"),
				iconController.getHelpIcon());
		JMenuItem aboutItem = new JMenuItem(resource.getString("aboutItem"));

		
		openItem.setMnemonic(resource.getString("openItemMn").charAt(0));
		saveItem.setMnemonic(resource.getString("saveItemMn").charAt(0));
		exitItem.setMnemonic(resource.getString("exitItemMn").charAt(0));
		metalItem.setMnemonic(resource.getString("metalItemMn").charAt(0));
		motifItem.setMnemonic(resource.getString("motifItemMn").charAt(0));
		windowsItem.setMnemonic(resource.getString("windowsItemMn").charAt(0));
		cascadeItem.setMnemonic(resource.getString("cascadeItemMn").charAt(0));
		tileItem.setMnemonic(resource.getString("tileItemMn").charAt(0));
		helpItem.setMnemonic(resource.getString("helpItemMn").charAt(0));
		aboutItem.setMnemonic(resource.getString("aboutItemMn").charAt(0));

		newBN.setAccelerator(KeyStroke.getKeyStroke(resource.getString(
		"newItemMn").charAt(0), Event.CTRL_MASK, false));
		openItem.setAccelerator(KeyStroke.getKeyStroke(resource.getString(
		"openItemMn").charAt(0), Event.CTRL_MASK, false));
		saveItem.setAccelerator(KeyStroke.getKeyStroke(resource.getString(
		"saveItemMn").charAt(0), Event.CTRL_MASK, false));
		helpItem.setAccelerator(KeyStroke.getKeyStroke(resource.getString(
		"helpItemMn").charAt(0), Event.CTRL_MASK, false));

		
		//Learning menu itens
		JMenuItem learningItem = new JMenuItem(resource.getString("learningItem"), iconController.getLearningIcon());
		JMenuItem tanItem = new JMenuItem(resource.getString("tanItem"));
		JMenuItem banItem = new JMenuItem(resource.getString("banItem"));
		JMenuItem monteCarloItem = new JMenuItem(resource.getString("monteCarloItem"));
		JMenuItem gibbsItem = new JMenuItem(resource.getString("GibbsItem"));
		JMenuItem iLearningItem = new JMenuItem(resource.getString("ILearningItem"));
		
		learningItem.setMnemonic(resource.getString("learningItemMn").charAt(0));
		tanItem.setMnemonic(resource.getString("tanItemMn").charAt(0));
		banItem.setMnemonic(resource.getString("banItemMn").charAt(0));
		monteCarloItem.setMnemonic(resource.getString("monteCarloItemMn").charAt(0));
		gibbsItem.setMnemonic(resource.getString("GibbsItemMn").charAt(0));
		iLearningItem.setMnemonic(resource.getString("ILearningItemMn").charAt(0));
		
		learningItem.setAccelerator(KeyStroke.getKeyStroke(resource.getString(
				"learningItemMn").charAt(0), Event.CTRL_MASK, false));
		
		
		
		
		

		// add ActionListener to all menu items
		newBN.addActionListener(alNewBN);
		newMSBN.addActionListener(alNewMSBN);
		newMEBN.addActionListener(alNewMEBN);
		newOOBN.addActionListener(alNewOOBN);
		openItem.addActionListener(alOpen);
		saveItem.addActionListener(alSave);
		exitItem.addActionListener(alExit);
		tbFile.addActionListener(alTbFile);
		tbView.addActionListener(alTbView);
		tbTools.addActionListener(alTbTools);
		tbWindow.addActionListener(alTbWindow);
		tbHelp.addActionListener(alTbHelp);
		metalItem.addActionListener(alMetal);
		motifItem.addActionListener(alMotif);
		windowsItem.addActionListener(alWindows);
		learningItem.addActionListener(alLearn);
		cascadeItem.addActionListener(alCascade);
		tileItem.addActionListener(alTile);
		helpItem.addActionListener(alHelp);
		monteCarloItem.addActionListener(alMonteCarlo);
		iLearningItem.addActionListener(alIL);
		gibbsItem.addActionListener(alGibbs);
		aboutItem.addActionListener(alAbout);

		// add menu items to their respective menu
		
		tanItem.addActionListener(alTAN);
		banItem.addActionListener(alBAN);
		
		newMenu.add(newBN);
		newMenu.add(newMSBN);
		newMenu.add(newMEBN);
		newMenu.add(newOOBN);
		fileMenu.add(newMenu);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		
		
		fileMenu.add(recentFilesMenu); 
		
		
		ConfigurationsController configController = ConfigurationsController.getInstance(); 
		for(String nameOfFile: configController.getConfigurations().getPreviewOpenFiles()){
			File file = new File(nameOfFile); 
			JMenuItem fileMenuItem = new JMenuItem(nameOfFile);  
			fileMenuItem.addActionListener(new OpenFileListener(file)); 
			
			recentFilesMenu.add(fileMenuItem); 
		}
		

		fileMenu.addSeparator(); 
		fileMenu.add(exitItem);
		
		lafMenu.add(metalItem);
		lafMenu.add(motifItem);
		lafMenu.add(windowsItem);
		
		tbMenu.add(tbFile);
		tbMenu.add(tbView);
		tbMenu.add(tbTools);
		tbMenu.add(tbWindow);
		tbMenu.add(tbHelp);
		
		viewMenu.add(tbMenu);
		viewMenu.addSeparator();
		viewMenu.add(lafMenu);
		
		toolsMenu.add(learningItem);
		toolsMenu.add(tanItem);
		toolsMenu.add(banItem);
		toolsMenu.add(monteCarloItem);
		toolsMenu.add(gibbsItem);
		toolsMenu.add(iLearningItem);
		
		windowMenu.add(cascadeItem);
		windowMenu.add(tileItem);
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);

		menu.add(fileMenu);
		menu.add(viewMenu);
		menu.add(toolsMenu);
		menu.add(windowMenu);
		menu.add(helpMenu);

		this.setJMenuBar(menu);
	}

	/**
	 * Call the method for creating the needed buttons and then create the tool
	 * bars and add the buttons to them and finally to the topPanel.
	 * TODO fix the help toolbar
	 */
	public void createToolBars() {

		createButtons();

		// create tool bars
		jtbFile = new JToolBar();
		jtbView = new JToolBar();
		jtbTools = new JToolBar();
		jtbWindow = new JToolBar();
		jtbHelp = new JToolBar();

		// add their buttons
		jtbFile.add(newNet);
		jtbFile.add(newMsbn);
		jtbFile.add(newMebn);
		jtbFile.add(newOobn);
		jtbFile.add(openNet);
		jtbFile.add(saveNet);
		jtbTools.add(learn);
		jtbView.add(metal);
		jtbView.add(motif);
		jtbView.add(windows);
		jtbWindow.add(cascade);
		jtbWindow.add(tile);
		jtbHelp.add(help);

		// add the tool bars to the topPanel
		topPanel.add(jtbFile);
		topPanel.add(jtbView);
		topPanel.add(jtbTools);
		topPanel.add(jtbWindow);
		topPanel.add(jtbHelp);
	}

	/**
	 * Create the needed buttons and add their respectinve tool tip.
	 */
	public void createButtons() {

		// create the buttons
		newNet = new JButton(iconController.getNewBNIcon());
		newMsbn = new JButton(iconController.getNewMSBNIcon());
		newMebn = new JButton(iconController.getNewMEBNIcon());
		newOobn = new JButton(iconController.getNewOOBNIcon());
		openNet = new JButton(iconController.getOpenIcon());
		saveNet = new JButton(iconController.getSaveIcon());
		learn = new JButton(iconController.getLearningIcon());
		metal = new JButton(iconController.getMetalIcon());
		motif = new JButton(iconController.getMotifIcon());
		windows = new JButton(iconController.getWindowsIcon());
		tile = new JButton(iconController.getTileIcon());
		cascade = new JButton(iconController.getCascadeIcon());
		help = new JButton(iconController.getHelpIcon());

		// add their tool tip
		help.setToolTipText(resource.getString("helpToolTip"));
		newNet.setToolTipText(resource.getString("newToolTip"));
		newMsbn.setToolTipText(resource.getString("newMsbnToolTip"));
		newMebn.setToolTipText(resource.getString("newMebnToolTip"));
		newOobn.setToolTipText(resource.getString("newOobnToolTip"));
		openNet.setToolTipText(resource.getString("openToolTip"));
		saveNet.setToolTipText(resource.getString("saveToolTip"));
		learn.setToolTipText(resource.getString("learningToolTip"));
		metal.setToolTipText(resource.getString("metalToolTip"));
		motif.setToolTipText(resource.getString("motifToolTip"));
		windows.setToolTipText(resource.getString("windowsToolTip"));
		tile.setToolTipText(resource.getString("tileToolTip"));
		cascade.setToolTipText(resource.getString("cascadeToolTip"));
	}

	/**
	 * Method responsible for assigning ActionListeners to all buttons
	 * available.
	 */
	public void assignActionListeners() {

		newNet.addActionListener(alNewBN);
		newMsbn.addActionListener(alNewMSBN);
		newMebn.addActionListener(alNewMEBN);
		newOobn.addActionListener(alNewOOBN);
		openNet.addActionListener(alOpen);
		saveNet.addActionListener(alSave);
		metal.addActionListener(alMetal);
		motif.addActionListener(alMotif);
		windows.addActionListener(alWindows);
		learn.addActionListener(alLearn);
		tile.addActionListener(alTile);
		cascade.addActionListener(alCascade);
		help.addActionListener(alHelp);

	}

	public static UnBBayesFrame getIUnBBayes() {
		return UnBBayesFrame.singleton;
	}
	
	public class ListenerCloserFrame implements WindowListener{
		  /**
	     * Invoked the first time a window is made visible.
	     */
	    public void windowOpened(WindowEvent e){
	    	
	    }

	    /**
	     * Invoked when the user attempts to close the window
	     * from the window's system menu.
	     */
	    public void windowClosing(WindowEvent e){
	    	System.out.println("foi...");
	    	controller.saveConfigurations(); 
	    }

	    /**
	     * Invoked when a window has been closed as the result
	     * of calling dispose on the window.
	     */
	    public void windowClosed(WindowEvent e){
	    	
	    }

	    /**
	     * Invoked when a window is changed from a normal to a
	     * minimized state. For many platforms, a minimized window 
	     * is displayed as the icon specified in the window's 
	     * iconImage property.
	     * @see java.awt.Frame#setIconImage
	     */
	    public void windowIconified(WindowEvent e){
	    	
	    }

	    /**
	     * Invoked when a window is changed from a minimized
	     * to a normal state.
	     */
	    public void windowDeiconified(WindowEvent e){
	    	
	    }

	    /**
	     * Invoked when the Window is set to be the active Window. Only a Frame or
	     * a Dialog can be the active Window. The native windowing system may
	     * denote the active Window or its children with special decorations, such
	     * as a highlighted title bar. The active Window is always either the
	     * focused Window, or the first Frame or Dialog that is an owner of the
	     * focused Window.
	     */
	    public void windowActivated(WindowEvent e){
	    	
	    }

	    /**
	     * Invoked when a Window is no longer the active Window. Only a Frame or a
	     * Dialog can be the active Window. The native windowing system may denote
	     * the active Window or its children with special decorations, such as a
	     * highlighted title bar. The active Window is always either the focused
	     * Window, or the first Frame or Dialog that is an owner of the focused
	     * Window.
	     */
	    public void windowDeactivated(WindowEvent e){
	    	
	    }
	}

}