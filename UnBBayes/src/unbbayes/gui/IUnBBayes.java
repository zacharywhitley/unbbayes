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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.ImageIcon;
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
import unbbayes.controller.MainController;

/**
 *  Essa classe extende o <code>JFrame</code> e é responsável pela interface
 *  da tela principal do programa.
 *
 *@author     Rommel Novaes Carvalho, Michael S. Onishi
 *@created    27 de Junho de 2001
 *@see        JFrame
 *@version    1.0 06/07/2001
 */
public class IUnBBayes extends JFrame {
		
	private MDIDesktopPane desktop;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JToolBar jtbFile;
	private JToolBar jtbView;
	private JToolBar jtbTools;
	private JToolBar jtbWindow;
	private JToolBar jtbHelp;
	private MainController controller;
	private JButton newNet;
	private JButton openNet;
	private JButton saveNet;
	private JButton learn;
	private JButton metal;
	private JButton motif;
	private JButton windows;
	private JButton tile;
	private JButton cascade;
	private JButton help;
	private URL helpSetURL;
	private HelpSet set;
	private JHelp jHelp;
	private ActionListener alNew;
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

	private static IUnBBayes singleton = null;

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	/**
	 *  Constrói a tela principal do programa, ajustando os Layouts e criando
	 *  botões, labels, e outros responsáveis pela interface. Além disso nesse
	 *  contrutor também criamos os respectivos <code>ActionListener</code>,
	 *  <code>KeyListener</code> e <code>MouseListener</code> para os
	 *  respectivos componentes.
	 *
	 *@see    ActionListener
	 *@see    MouseListener
	 *@see    KeyListener
	 */
	public IUnBBayes(MainController _controller) {
		super(resource.getString("unbbayesTitle"));
		this.controller = _controller;
		setSize(650, 480);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

		setVisible(true);

		singleton = this;
	}

	private void setLnF(String lnfName) {
		try {
			UIManager.setLookAndFeel(lnfName);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (UnsupportedLookAndFeelException ex1) {
			System.err.println(
				resource.getString("LookAndFeelUnsupportedException")
					+ lnfName);
		} catch (ClassNotFoundException ex2) {
			System.err.println(
				resource.getString("LookAndFeelClassNotFoundException")
					+ lnfName);
		} catch (InstantiationException ex3) {
			System.err.println(
				resource.getString("LookAndFeelInstantiationException")
					+ lnfName);
		} catch (IllegalAccessException ex4) {
			System.err.println(
				resource.getString("LookAndFeelIllegalAccessException")
					+ lnfName);
		}
	}

	/**
	 * Retorna a janela que está selecionada.
	 *
	 * @return janela que está selecionada.
	 */
	public NetWindow getSelectedWindow() {
		return (NetWindow) desktop
			.getSelectedFrame()
			.getContentPane()
			.getComponent(
			0);
	}

	/**
	 * Adiciona uma nova janela.
	 *
	 * @param newWindow nova janela.
	 */
	public void addWindow(/*NetWindow*/
	JInternalFrame newWindow) {
		desktop.add(newWindow);
	}

	/**
	 * Method responsible for creating all ActionListeners
	 * needed.
	 */
	public void createActionListeners() {

		// create an ActionListener for opening new window		
		alNew = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				controller.newNet();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for loading
		alOpen = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				String[] nets = new String[] { "net" };
				JFileChooser chooser = new JFileChooser(".");
				chooser.setMultiSelectionEnabled(false);

				// adicionar FileView no FileChooser para desenhar ícones de 
				// arquivos
				chooser.setFileView(new FileIcon(IUnBBayes.this));

				chooser.addChoosableFileFilter(
					new SimpleFileFilter(
						nets,
						resource.getString("netFileFilter")));
				int option = chooser.showOpenDialog(null);
				if (option == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile() != null) {
						controller.loadNet(chooser.getSelectedFile());
					}
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		};

		// create an ActionListener for saving
		alSave = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				String[] nets = new String[] { "net" };
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);

				// adicionar FileView no FileChooser para desenhar ícones de 
				// arquivos
				chooser.setFileView(new FileIcon(IUnBBayes.this));
				chooser.setCurrentDirectory(new File("."));
				chooser.addChoosableFileFilter(
					new SimpleFileFilter(
						nets,
						resource.getString("netFileFilter")));
				int option = chooser.showSaveDialog(null);
				if (option == JFileChooser.APPROVE_OPTION) {
					//                    File a;
					controller.saveNet(
						((chooser.getSelectedFile() != null)
							? new File(
								chooser.getSelectedFile().getAbsolutePath()
									+ ".net")
							: new File(resource.getString("fileUntitled"))));
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
				JFileChooser chooser = new JFileChooser(".");
				chooser.setMultiSelectionEnabled(false);
				chooser.addChoosableFileFilter(
					new SimpleFileFilter(
						nets,
						resource.getString("textFileFilter")));
				int option = chooser.showOpenDialog(IUnBBayes.this);
				File file;
				if (option == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					new ConstructionController(file, controller);
				}
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
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				try {
					set =
						new HelpSet(
							null,
							getClass().getResource("/help/JUnBBayes.hs"));
					jHelp = new JHelp(set);
					JFrame f = new JFrame();
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.setContentPane(jHelp);
					f.setSize(500, 400);
					f.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};

	}

	/**
	 * Method responsible for creating the menu used in this
	 * class, JFrame.
	 */
	public void createMenu() {
		JMenuBar menu = new JMenuBar();

		// create menus and set their mnemonic
		JMenu fileMenu = new JMenu(resource.getString("fileMenu"));
		JMenu lafMenu = new JMenu(resource.getString("lafMenu"));
		JMenu viewMenu = new JMenu(resource.getString("viewMenu"));
		JMenu tbMenu = new JMenu(resource.getString("tbMenu"));
		JMenu toolsMenu = new JMenu(resource.getString("toolsMenu"));
		JMenu windowMenu = new JMenu(resource.getString("windowMenu"));
		JMenu helpMenu = new JMenu(resource.getString("helpMenu"));
		fileMenu.setMnemonic(resource.getString("fileMenuMn").charAt(0));
        lafMenu.setMnemonic(resource.getString("lafMenuMn").charAt(0));
		viewMenu.setMnemonic(resource.getString("viewMenuMn").charAt(0));
		tbMenu.setMnemonic(resource.getString("tbMenuMn").charAt(0));
		toolsMenu.setMnemonic(resource.getString("toolsMenuMn").charAt(0));
		windowMenu.setMnemonic(resource.getString("windowMenuMn").charAt(0));
		helpMenu.setMnemonic(resource.getString("helpMenuMn").charAt(0));

		// create menu items, set their mnemonic and their key accelerator		
		JMenuItem newItem =
			new JMenuItem(
				resource.getString("newItem"),
				new ImageIcon(getClass().getResource("/icons/new.gif")));
		JMenuItem openItem =
			new JMenuItem(
				resource.getString("openItem"),
				new ImageIcon(getClass().getResource("/icons/open.gif")));
		JMenuItem saveItem =
			new JMenuItem(
				resource.getString("saveItem"),
				new ImageIcon(getClass().getResource("/icons/save.gif")));
		JMenuItem exitItem = new JMenuItem(resource.getString("exitItem"), 'X');
		JMenuItem tbFile =
			new JCheckBoxMenuItem(resource.getString("tbFile"), true);
		JMenuItem tbView =
			new JCheckBoxMenuItem(resource.getString("tbView"), true);
		JMenuItem tbTools =
			new JCheckBoxMenuItem(resource.getString("tbTools"), true);
		JMenuItem tbWindow =
			new JCheckBoxMenuItem(resource.getString("tbWindow"), true);
		JMenuItem tbHelp =
			new JCheckBoxMenuItem(resource.getString("tbHelp"), true);
		JMenuItem metalItem =
			new JMenuItem(
				resource.getString("metalItem"),
				new ImageIcon(getClass().getResource("/icons/metal.gif")));
		JMenuItem motifItem =
			new JMenuItem(
				resource.getString("motifItem"),
				new ImageIcon(getClass().getResource("/icons/motif.gif")));
		JMenuItem windowsItem =
			new JMenuItem(
				resource.getString("windowsItem"),
				new ImageIcon(getClass().getResource("/icons/windows.gif")));
		JMenuItem learningItem =
			new JMenuItem(
				resource.getString("learningItem"),
				new ImageIcon(getClass().getResource("/icons/learn.gif")));
		JMenuItem cascadeItem =
			new JMenuItem(
				resource.getString("cascadeItem"),
				new ImageIcon(getClass().getResource("/icons/cascade.gif")));
		JMenuItem tileItem =
			new JMenuItem(
				resource.getString("tileItem"),
				new ImageIcon(getClass().getResource("/icons/tile.gif")));
		JMenuItem helpItem =
			new JMenuItem(
				resource.getString("helpItem"),
				new ImageIcon(getClass().getResource("/icons/help.gif")));
		JMenuItem aboutItem =
			new JMenuItem(resource.getString("aboutItem"));

		newItem.setMnemonic(resource.getString("newItemMn").charAt(0));
		openItem.setMnemonic(resource.getString("openItemMn").charAt(0));
		saveItem.setMnemonic(resource.getString("saveItemMn").charAt(0));
        exitItem.setMnemonic(resource.getString("exitItemMn").charAt(0));
		metalItem.setMnemonic(resource.getString("metalItemMn").charAt(0));
		motifItem.setMnemonic(resource.getString("motifItemMn").charAt(0));
		windowsItem.setMnemonic(resource.getString("windowsItemMn").charAt(0));
		learningItem.setMnemonic(resource.getString("learningItemMn").charAt(0));
		cascadeItem.setMnemonic(resource.getString("cascadeItemMn").charAt(0));
		tileItem.setMnemonic(resource.getString("tileItemMn").charAt(0));
		helpItem.setMnemonic(resource.getString("helpItemMn").charAt(0));
        aboutItem.setMnemonic(resource.getString("aboutItemMn").charAt(0));

		newItem.setAccelerator(
			KeyStroke.getKeyStroke(resource.getString("newItemMn").charAt(0), Event.CTRL_MASK, false));
		openItem.setAccelerator(
			KeyStroke.getKeyStroke(resource.getString("openItemMn").charAt(0), Event.CTRL_MASK, false));
		saveItem.setAccelerator(
			KeyStroke.getKeyStroke(resource.getString("saveItemMn").charAt(0), Event.CTRL_MASK, false));
		learningItem.setAccelerator(
			KeyStroke.getKeyStroke(resource.getString("learningItemMn").charAt(0), Event.CTRL_MASK, false));
		helpItem.setAccelerator(
			KeyStroke.getKeyStroke(resource.getString("helpItemMn").charAt(0), Event.CTRL_MASK, false));

		// add ActionListener to all menu items
		newItem.addActionListener(alNew);
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
		// aboutItem.addActionListener(alAbout);

		// add menu items to their respective menu
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
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
	 * Call the method for creating the needed buttons and then create the 
	 * tool bars and add the buttons to them and finally to the topPanel.
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
		newNet =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/new.gif")));
		openNet =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/open.gif")));
		saveNet =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/save.gif")));
		learn =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/learn.gif")));
		metal =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/metal.gif")));
		motif =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/motif.gif")));
		windows =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/windows.gif")));
		tile =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/tile.gif")));
		cascade =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/cascade.gif")));
		help =
			new JButton(
				new ImageIcon(getClass().getResource("/icons/help.gif")));

		// add their tool tip
		help.setToolTipText(resource.getString("helpToolTip"));
		newNet.setToolTipText(resource.getString("newToolTip"));
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

		newNet.addActionListener(alNew);
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
	
	public static IUnBBayes getIUnBBayes(){
		return IUnBBayes.singleton;		
	}
	
}