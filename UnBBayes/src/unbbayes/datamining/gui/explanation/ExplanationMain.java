/*
 * ExplanationMain.java
 *
 * Created on 12 de Abril de 2002, 05:37
 */

package unbbayes.datamining.gui.explanation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import unbbayes.controlador.*;
import unbbayes.fronteira.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 *
 * @author  Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (01/07/2002)
 */
public class ExplanationMain extends JInternalFrame
{   private JMenuBar explanationMenuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu();
    private JMenuItem openMenuItem = new JMenuItem();
    private JMenuItem saveMenuItem = new JMenuItem();
    private JMenuItem exitMenuItem = new JMenuItem();
    private JMenu helpMenu = new JMenu();
    private JMenuItem topicsMenuItem = new JMenuItem();
    private ButtonGroup typeButtonGroup = new ButtonGroup();
    private JToolBar explanationToolBar = new JToolBar();
    private JButton openButton = new JButton();
    private JButton saveButton = new JButton();
    private JButton helpButton = new JButton();
    private JPanel statusPanel = new JPanel();
    private JLabel statusLabel = new JLabel();
    private ProbabilisticNetwork net = new ProbabilisticNetwork();
    private ImageIcon abrirIcon;
    private ImageIcon salvarIcon;
    private ImageIcon helpIcon;
    private ImageIcon expandirIcon;
    private ImageIcon compilaIcon;
    private JButton treeButton = new JButton();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private NetWindowEdition edition;
  private JScrollPane jScrollPane1 = new JScrollPane();
  private NetWindow netWindow = new NetWindow(net);
  private JPanel networkPanel = new JPanel();
  private HierarchicDefinitionPanel definitionPanel = new HierarchicDefinitionPanel();
  private JFileChooser fileChooser;

    /** Creates new form ExplanationMain */
    public ExplanationMain()
    {   super("Explanation",true,true,true,true);
        //resource = ResourceBundle.getBundle("unbbayes.datamining.gui.decisiontree.resources.DecisiontreeResource");
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {   jbInit();
        }
        catch(Exception e)
        {   e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {   abrirIcon = new ImageIcon(getClass().getResource("/icones/abrir.gif"));
        salvarIcon = new ImageIcon(getClass().getResource("/icones/salvar.gif"));
        helpIcon = new ImageIcon(getClass().getResource("/icones/help.gif"));
        expandirIcon = new ImageIcon(getClass().getResource("/icones/hierarquia.gif"));
        compilaIcon = new ImageIcon(getClass().getResource("/icones/compila.gif"));
        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open...");
        openMenuItem.setIcon(abrirIcon);
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

    treeButton.setToolTipText("");
    treeButton.setIcon(expandirIcon);
    treeButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        treeButton_actionPerformed(e);
      }
    });
    jTabbedPane1.setOpaque(true);
    openButton.setIcon(abrirIcon);
    networkPanel.setLayout(new BorderLayout());
    fileMenu.add(openMenuItem);
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save Network...");
        saveMenuItem.setIcon(salvarIcon);
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);
        exitMenuItem.setMnemonic('E');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);
        explanationMenuBar.add(fileMenu);
        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");
        topicsMenuItem.setMnemonic('E');
        topicsMenuItem.setText("Help topics...");
        topicsMenuItem.setIcon(helpIcon);
        topicsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        helpMenu.add(topicsMenuItem);
        explanationMenuBar.add(helpMenu);

        setMaximizable(true);
        explanationToolBar.setFloatable(false);
        openButton.setToolTipText("Open a file");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

    explanationToolBar.add(openButton);

        saveButton.setIcon(salvarIcon);
        saveButton.setToolTipText("Save a file");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        explanationToolBar.add(saveButton);

        helpButton.setIcon(helpIcon);
        helpButton.setToolTipText("Call help file");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        explanationToolBar.add(helpButton);
        explanationToolBar.add(treeButton, null);

        getContentPane().add(explanationToolBar, BorderLayout.NORTH);

        statusPanel.setLayout(new java.awt.BorderLayout());

        statusPanel.setBorder(new javax.swing.border.TitledBorder("Status"));
        statusLabel.setText("Welcome");
        statusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
    this.getContentPane().add(jTabbedPane1, BorderLayout.CENTER);

        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);
    jTabbedPane1.add(jScrollPane1, "networkPanel");
    jTabbedPane1.add(definitionPanel,  "definitionPanel");
    jScrollPane1.getViewport().add(networkPanel, null);
    networkPanel.add(netWindow, BorderLayout.CENTER);
        jTabbedPane1.setSelectedIndex(0);
        jTabbedPane1.setEnabledAt(1,false);

        edition = netWindow.getNetWindowEdition();
        edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());

    }

    /**Help | About action performed*/
    private void helpButtonActionPerformed(ActionEvent evt)
    {   try
        {   FileController.getInstance().openHelp(this);
        }
        catch (Exception e)
        {   statusLabel.setText("Error= "+e.getMessage()+" "+this.getClass().getName());
        }
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)
    {   saveMenuItemActionPerformed(evt);
    }

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt)
    {   openMenuItemActionPerformed(evt);
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)
    {   dispose();
    }

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)
    {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String[] s2 = {"net"};
        fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        fileChooser.setMultiSelectionEnabled(false);
        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        fileChooser.setFileView(new FileIcon(this));
        fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "Networks (*.net)"));
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {   File selectedFile = fileChooser.getSelectedFile();
            try
            {   String fileName = selectedFile.getName();
                if (!fileName.regionMatches(true,fileName.length() - 4,".net",0,4))
                {   selectedFile = new File(selectedFile.getAbsolutePath()+".net");
                }
                BaseIO io = new NetIO();
                io.save(selectedFile,net);
                statusLabel.setText("Arquivo salvo com sucesso.");
            }
            catch (Exception ioe)
            {   statusLabel.setText("Erro ao salvar arquivo "+selectedFile.getName()+" "+ioe.getMessage());
            }
            FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt)
    {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
        String[] s1 = {"net"};
        fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        fileChooser.setMultiSelectionEnabled(false);
        //adicionar FileView no FileChooser para desenhar ícones de arquivos
        fileChooser.setFileView(new FileIcon(ExplanationMain.this));
        fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "Networks (*.net)"));
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            openNetFile(selectedFile);
            saveButton.setEnabled(true);
            saveMenuItem.setEnabled(true);
            FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void openNetFile(File selectedFile)
    {   try
        {   net = new NetIO().load(selectedFile);
            netWindow = new NetWindow(net);
            edition = netWindow.getNetWindowEdition();
            edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());

            networkPanel.removeAll();
            networkPanel.setLayout(new BorderLayout());
            networkPanel.add(netWindow,BorderLayout.CENTER);

            statusLabel.setText("File opened successfully");

            jTabbedPane1.setSelectedIndex(0);
            jTabbedPane1.setEnabledAt(1,false);
        }
        catch (Exception e)
        {   net = null;
            System.err.print(e.getMessage());
            statusLabel.setText(e.getMessage());
        }
    }

    private void saveNetFile(File selectedFile) {
        (new NetIO()).save(selectedFile, net);
        statusLabel.setText("File saved successfully");
    }

  void treeButton_actionPerformed(ActionEvent e)
  {   jTabbedPane1.setSelectedIndex(1);
      jTabbedPane1.setEnabledAt(1,true);
      definitionPanel.setHierarchicTree(net);
  }
}
