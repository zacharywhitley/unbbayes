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

package unbbayes.fronteira;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import unbbayes.aprendizagem.ConstructionController;
import unbbayes.controlador.MainController;

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
    private JToolBar jtbDraw;
    private MainController controller;
    private	JButton newNet;
    private	JButton loadNet;
    private	JButton saveNet;
    private JButton learn;
    private JButton metal;
    private	JButton motif;
    private JButton windows;
    private JButton tile;
    private JButton cascade;
    private JButton help;
    private URL helpSetURL;
    private HelpSet set;
    private JHelp jHelp;

    private static IUnBBayes singleton = null;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.fronteira.resources.FronteiraResources");

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
    public IUnBBayes(MainController contr) {
        super(resource.getString("unbbayesTitle"));
        this.controller = contr;
        setSize(650, 480);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();

        //instancia variáveis de instância
        desktop          = new MDIDesktopPane();
        jtbDraw          = new JToolBar();
        topPanel         = new JPanel(new GridLayout(0,1));
        bottomPanel      = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));

        //criar botões que serão usados nos toolbars
        newNet         = new JButton(new ImageIcon(getClass().getResource("/icons/new.gif")));
        loadNet        = new JButton(new ImageIcon(getClass().getResource("/icons/open.gif")));
        saveNet        = new JButton(new ImageIcon(getClass().getResource("/icons/save.gif")));
        learn          = new JButton(new ImageIcon(getClass().getResource("/icons/learn.gif")));
        metal          = new JButton(new ImageIcon(getClass().getResource("/icons/metal.gif")));
        motif          = new JButton(new ImageIcon(getClass().getResource("/icons/motif.gif")));
        windows        = new JButton(new ImageIcon(getClass().getResource("/icons/windows.gif")));
        tile           = new JButton(new ImageIcon(getClass().getResource("/icons/tile.gif")));
        cascade        = new JButton(new ImageIcon(getClass().getResource("/icons/cascade.gif")));
		help           = new JButton(new ImageIcon(getClass().getResource("/icons/help.gif")));

        //setar tooltip para esses botões
        help.setToolTipText(resource.getString("helpToolTip"));
        newNet.setToolTipText(resource.getString("newToolTip"));
        loadNet.setToolTipText(resource.getString("openToolTip"));
        saveNet.setToolTipText(resource.getString("saveToolTip"));
        learn.setToolTipText(resource.getString("learningToolTip"));
        metal.setToolTipText(resource.getString("metalToolTip"));
        motif.setToolTipText(resource.getString("motifToolTip"));
        windows.setToolTipText(resource.getString("windowsToolTip"));
        tile.setToolTipText(resource.getString("tileToolTip"));
        cascade.setToolTipText(resource.getString("cascadeToolTip"));

        //Criar um file chooser que abre um 'Open' dialog
        loadNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                String[] nets = new String[] {"net"};
                JFileChooser chooser = new JFileChooser(".");
                chooser.setMultiSelectionEnabled(false);

                //adicionar FileView no FileChooser para desenhar ícones de arquivos
                chooser.setFileView(new FileIcon(IUnBBayes.this));

                chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
                                                 resource.getString("netFileFilter")));
                int option = chooser.showOpenDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
                    if (chooser.getSelectedFile()!=null) {
                        controller.loadNet(chooser.getSelectedFile());
                    }
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        //ao clicar no butão newNet, limpa-se a tela para desenhar uma nova rede
        newNet.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                controller.newNet();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });


        //ao clicar no butão help, mostra-se um frame com os htmls de ajuda
        help.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        try {
                                set        = new HelpSet(null, getClass().getResource("/ajuda/JUnBBayes.hs"));
                                jHelp      = new JHelp(set);
                                JFrame f   = new JFrame();
                                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                f.setContentPane(jHelp);
                                f.setSize(500,400);
                                f.setVisible(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

        });


        //se apertar nesse botão o look and feel do metal é acionado
        metal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

        });

        //se apertar nesse botão o look and feel do motif é acionado
        motif.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

        });

        //se apertar nesse botão o look and feel do windows é acionado
        windows.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

        });

        tile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desktop.tileFrames();
            }
        });

        cascade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desktop.cascadeFrames();
            }
        });

        //Criar um file chooser que abre um 'Save' dialog
        saveNet.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                String[] nets = new String[] {"net"};
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);

                //adicionar FileView no FileChooser para desenhar ícones de arquivos
                chooser.setFileView(new FileIcon(IUnBBayes.this));
                chooser.setCurrentDirectory(new File("."));
                chooser.addChoosableFileFilter(new SimpleFileFilter(nets,
                                                 resource.getString("netFileFilter")));
                int option = chooser.showSaveDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
//                    File a;
                    controller.saveNet(((chooser.getSelectedFile()!=null)?
                                new File(chooser.getSelectedFile().getAbsolutePath() + ".net"):
                                new File(resource.getString("fileUntitled"))));
                }
//                graph.update();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        //ao clicar no botão learn, mostra-se o menu para escolha do arquivo para o aprendizado.
        learn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String[] nets = new String[] {"txt"};
                JFileChooser chooser = new JFileChooser(".");
                chooser.setMultiSelectionEnabled(false);
                chooser.addChoosableFileFilter(new SimpleFileFilter(nets, resource.getString("textFileFilter")));
                int option = chooser.showOpenDialog(IUnBBayes.this);
                File file;
                if (option == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    new ConstructionController(file, controller);
                }
            }
        });

        //colocar botões e controladores do look-and-feel no toolbar e esse no topPanel
        jtbDraw.add(newNet);
        jtbDraw.add(loadNet);
        jtbDraw.add(saveNet);

        jtbDraw.addSeparator();

        jtbDraw.add(learn);

        jtbDraw.addSeparator();

        jtbDraw.add(metal);
        jtbDraw.add(motif);
        jtbDraw.add(windows);

        jtbDraw.addSeparator();

        jtbDraw.add(tile);
        jtbDraw.add(cascade);

        jtbDraw.addSeparator();

        jtbDraw.add(help);
        topPanel.add(jtbDraw);

        //adiciona containers para o contentPane
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
        }
        catch (UnsupportedLookAndFeelException ex1) {
            System.err.println(resource.getString("LookAndFeelUnsupportedException") + lnfName);
        }
        catch (ClassNotFoundException ex2) {
            System.err.println(resource.getString("LookAndFeelClassNotFoundException") + lnfName);
        }
        catch (InstantiationException ex3) {
            System.err.println(resource.getString("LookAndFeelInstantiationException") + lnfName);
        }
        catch (IllegalAccessException ex4) {
            System.err.println(resource.getString("LookAndFeelIllegalAccessException") + lnfName);
        }
    }


    /**
     * Retorna a janela que está selecionada.
     *
     * @return janela que está selecionada.
     */
    public NetWindow getSelectedWindow() {
        return (NetWindow)desktop.getSelectedFrame().getContentPane().getComponent(0);
    }

    /**
     * Adiciona uma nova janela.
     *
     * @param newWindow nova janela.
     */
    public void addWindow(/*NetWindow*/JInternalFrame newWindow) {
        desktop.add(newWindow);
    }

}