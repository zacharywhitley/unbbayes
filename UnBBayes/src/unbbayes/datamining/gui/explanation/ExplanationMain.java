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

import unbbayes.controller.*;
import unbbayes.gui.*;
import unbbayes.io.*;
import unbbayes.prs.bn.*;
import unbbayes.util.*;

/**
 *
 * @author  Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (01/07/2002)
 */
public class ExplanationMain extends JPanel
{
    private ButtonGroup typeButtonGroup = new ButtonGroup();
    private JToolBar explanationToolBar = new JToolBar();
    private JButton openButton = new JButton();
    private JButton saveButton = new JButton();
    private JButton helpButton = new JButton();
    private JPanel statusPanel = new JPanel();
    private JLabel statusBar = new JLabel();
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
  private BorderLayout borderLayout1 = new BorderLayout();

    /** Creates new form ExplanationMain */
    public ExplanationMain()
    {   //resource = ResourceBundle.getBundle("unbbayes.datamining.gui.decisiontree.resources.DecisiontreeResource");
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {   jbInit();
        }
        catch(Exception e)
        {   e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {   abrirIcon = new ImageIcon(getClass().getResource("/icons/open.gif"));
        salvarIcon = new ImageIcon(getClass().getResource("/icons/save.gif"));
        helpIcon = new ImageIcon(getClass().getResource("/icons/help.gif"));
        expandirIcon = new ImageIcon(getClass().getResource("/icons/hierarchy.gif"));
        compilaIcon = new ImageIcon(getClass().getResource("/icons/compile.gif"));

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
    this.setLayout(borderLayout1);




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

        add(explanationToolBar, BorderLayout.NORTH);

        statusPanel.setLayout(new java.awt.BorderLayout());

        statusPanel.setBorder(new javax.swing.border.TitledBorder("Status"));
        statusBar.setText("Welcome");
        statusPanel.add(statusBar, java.awt.BorderLayout.CENTER);
    add(jTabbedPane1, BorderLayout.CENTER);

    add(statusPanel, BorderLayout.SOUTH);
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
        {   statusBar.setText("Error= "+e.getMessage()+" "+this.getClass().getName());
        }
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)
    {   saveMenuItemActionPerformed(evt);
    }

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt)
    {   openMenuItemActionPerformed(evt);
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
                statusBar.setText("Arquivo salvo com sucesso.");
            }
            catch (Exception ioe)
            {   statusBar.setText("Erro ao salvar arquivo "+selectedFile.getName()+" "+ioe.getMessage());
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
            networkPanel.updateUI();

            statusBar.setText("File opened successfully");

            jTabbedPane1.setSelectedIndex(0);
            jTabbedPane1.setEnabledAt(1,false);
        }
        catch (Exception e)
        {   net = null;
            System.err.print(e.getMessage());
            statusBar.setText(e.getMessage());
        }
    }

    private void saveNetFile(File selectedFile) {
        (new NetIO()).save(selectedFile, net);
        statusBar.setText("File saved successfully");
    }

  void treeButton_actionPerformed(ActionEvent e)
  {   jTabbedPane1.setSelectedIndex(1);
      jTabbedPane1.setEnabledAt(1,true);
      definitionPanel.setHierarchicTree(net);
  }
}
