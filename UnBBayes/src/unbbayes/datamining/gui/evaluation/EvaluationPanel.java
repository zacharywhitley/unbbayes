package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import unbbayes.controlador.*;
import unbbayes.datamining.classifiers.*;
//import unbbayes.datamining.controller.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.fronteira.*;

public class EvaluationPanel extends JPanel
{
  private JPanel jPanel63 = new JPanel();
  private BorderLayout borderLayout45 = new BorderLayout();
  private JPanel jPanel62 = new JPanel();
  private BorderLayout borderLayout44 = new BorderLayout();
  private BorderLayout borderLayout43 = new BorderLayout();
  private JButton jButton9 = new JButton();
  private JPanel jPanel50 = new JPanel();
  private BorderLayout borderLayout33 = new BorderLayout();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel49 = new JPanel();
  private JPanel jPanel48 = new JPanel();
  private JPanel jPanel47 = new JPanel();
  private JPanel jPanel46 = new JPanel();
  private JPanel jPanel45 = new JPanel();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private TitledBorder titledBorder10;
  private TitledBorder titledBorder9;
  private TitledBorder titledBorder8;
  private TitledBorder titledBorder7;
  private TitledBorder titledBorder6;
  private ImageIcon salvarIcon;
  private Border border10;
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet instances;
  private Classifier classifier;
//  private EvaluationThread thread;
  private JTextArea jTextArea2 = new JTextArea();
  private JTextArea jTextArea1 = new JTextArea();
  private JTextField jTextField1 = new JTextField();
  private InstanceSet userTest;
  private JLabel jLabel1 = new JLabel();
  private EvaluationMain reference;
  private JFileChooser fileChooser;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JScrollPane jScrollPane4 = new JScrollPane();
  private JComboBox jComboBox2 = new JComboBox();
  private BorderLayout borderLayout42 = new BorderLayout();
  private GridLayout gridLayout8 = new GridLayout();
  private GridLayout gridLayout3 = new GridLayout();
  private JButton jButton8 = new JButton();
  private JButton jButton7 = new JButton();
  private JPanel jPanel61 = new JPanel();
  private JPanel jPanel59 = new JPanel();
  private JPanel jPanel60 = new JPanel();
  private JPanel jPanel58 = new JPanel();
  private JPanel jPanel57 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private JLabel jLabel2 = new JLabel();
  private BorderLayout borderLayout4 = new BorderLayout();

  public EvaluationPanel(EvaluationMain reference)
  { this.reference = reference;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  { salvarIcon = new ImageIcon(getClass().getResource("/icones/salvar.gif"));
    titledBorder10 = new TitledBorder(border10,"Classifier output");
    titledBorder9 = new TitledBorder(border10,"Select Class");
    titledBorder8 = new TitledBorder(border10,"Log");
    titledBorder7 = new TitledBorder(border10,"Test Options");
    titledBorder6 = new TitledBorder(border10,"Model");
    border10 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    jPanel62.setLayout(borderLayout45);
    jButton9.setEnabled(false);
    jButton9.setIcon(salvarIcon);
    jButton9.setMnemonic('S');
    jButton9.setText("Save information...");
    jButton9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton9_actionPerformed(e);
      }
    });
    jPanel50.setLayout(borderLayout43);
    jPanel2.setLayout(borderLayout33);
    jPanel49.setLayout(borderLayout3);
    jPanel48.setLayout(borderLayout4);
    jPanel47.setLayout(borderLayout44);
    jPanel45.setLayout(gridBagLayout1);
    this.setLayout(borderLayout1);
    jPanel46.setBorder(titledBorder6);
    jPanel46.setLayout(borderLayout2);
    jPanel47.setBorder(titledBorder10);
    jPanel50.setBorder(titledBorder8);
    jPanel49.setBorder(titledBorder9);
    jPanel49.setMinimumSize(new Dimension(148, 104));
    jTextArea2.setEditable(false);
    jTextArea1.setEditable(false);
    jScrollPane4.setBorder(null);
    jComboBox2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jComboBox2_actionPerformed(e);
      }
    });
    jComboBox2.setEnabled(false);
    gridLayout8.setColumns(2);
    gridLayout3.setRows(2);
    jButton8.setEnabled(false);
    jButton8.setText("Stop");
    jButton8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton8_actionPerformed(e);
      }
    });
    jButton7.setEnabled(false);
    jButton7.setText("Start");
    jButton7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton7_actionPerformed(e);
      }
    });
    jPanel59.setLayout(gridLayout8);
    jPanel58.setLayout(borderLayout42);
    jPanel4.setLayout(gridLayout3);
    jPanel4.setMinimumSize(new Dimension(135, 62));
    jPanel4.setPreferredSize(new Dimension(135, 62));
    jLabel2.setText("  ");
    jPanel2.add(jPanel46, BorderLayout.NORTH);
    jPanel46.add(jLabel2,  BorderLayout.CENTER);
    jPanel2.add(jPanel45, BorderLayout.CENTER);
    jPanel47.add(jPanel62, BorderLayout.CENTER);
    jPanel62.add(jScrollPane2, BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTextArea2, null);
    jPanel47.add(jPanel63, BorderLayout.SOUTH);
    jPanel63.add(jButton9, null);
    /*jPanel45.add(jPanel48,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, -47));
    */
    jPanel45.add(jPanel48,   new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

    jPanel45.add(jPanel47,  new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 93, 169));

    jPanel48.add(jPanel49, BorderLayout.NORTH);
    jPanel49.add(jScrollPane4,  BorderLayout.CENTER);
    jScrollPane4.getViewport().add(jPanel4, null);
    jPanel4.add(jPanel57, null);
    jPanel57.add(jComboBox2, null);
    jPanel4.add(jPanel58, null);
    jPanel58.add(jPanel59, BorderLayout.CENTER);
    jPanel59.add(jPanel60, null);
    jPanel60.add(jButton7, null);
    jPanel59.add(jPanel61, null);
    jPanel61.add(jButton8, null);
    jPanel48.add(jPanel50, BorderLayout.CENTER);
    jPanel50.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea1, null);
    this.add(jPanel2, BorderLayout.CENTER);
  }

  /** Salva as informações da text area
   *  @param e One ActionEvent
   *  */
  void jButton9_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationPanel.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      int returnVal = fileChooser.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   String fileName = selectedFile.getName();
              if (!fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
              {   selectedFile = new File(selectedFile.getAbsolutePath()+".txt");
              }
              OutputStream w = new BufferedOutputStream(new FileOutputStream(selectedFile));
              PrintWriter pw = new PrintWriter(w, true);
              int lineCount = jTextArea2.getLineCount();
              for(int i=0; i<lineCount; i++)
              {   int startLine = jTextArea2.getLineStartOffset(i);
                  int endLine = jTextArea2.getLineEndOffset(i);
                  pw.println(jTextArea2.getText(startLine,(endLine-startLine-1)));
              }
              pw.flush();
              w.close();
              reference.setStatusBar("File saved. "+fileName);
          }
          catch (BadLocationException ble)
          {   reference.setStatusBar("Bad location "+ble.getMessage());
          }
          catch (IOException ioe)
          {   reference.setStatusBar("Error writing file "+selectedFile.getName()+" "+ioe.getMessage());
          }
          FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /** Faz a avaliação
   *  @param e An ActionEvent
   *  */
  void jButton7_actionPerformed(ActionEvent e)
  {   /*if (thread == null)
      {   try
          {   jButton7.setEnabled(false);
              jButton8.setEnabled(true);
              setCursor(new Cursor(Cursor.WAIT_CURSOR));
              String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
              String classifierName = classifier.getClass().getName().substring("unbbayes.datamining.classifiers.".length());
              jTextArea1.append("Started  "+currentHour+classifierName+"\n");

              thread = new EvaluationThread(classifier,instances,this);
              thread.start();

              //jTextArea2.setText(outBuff.toString());
              currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
              jTextArea1.append("Finished "+currentHour+classifierName+"\n");
              jButton9.setEnabled(true);
              setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          }
          catch (Exception ex)
          {   setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
              JOptionPane.showConfirmDialog(EvaluationPanel.this,"Exception "+ex.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
          }
          thread = null;  // Termina a thread.
          jButton7.setEnabled(true);
          jButton8.setEnabled(false);
      }*/
  }

  /** Interrompe a thread
   *  @param e One ActionEvent
   *  */
  void jButton8_actionPerformed(ActionEvent e)
  {   /*if (thread != null)
      {   thread.interrupt();
          String classifierName = classifier.getClass().getName().substring("unbbayes.datamining.classifiers.".length());
          String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
          jTextArea1.append("Finished "+currentHour+classifierName+" "+jComboBox2.getSelectedItem()+"\n");
      }
      jButton7.setEnabled(true);
      jButton8.setEnabled(false);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));*/
  }

  /** Altera um modelo
   *  @param classifier A classifier
   *  @param inst A InstanceSet
   *  */
  public void setModel(Classifier classifier,InstanceSet inst)
  {   jTextArea2.setText("");
      if (classifier instanceof BayesianLearning)
      {   jLabel2.setText("Model - Bayesian Network");
      }
      else if (classifier instanceof Id3)
      {   jLabel2.setText("Model - Decision Tree");
      }
      this.classifier = classifier;
      this.instances = inst;
      int numAtt = instances.numAttributes();
      jComboBox2.setEnabled(true);
      jButton7.setEnabled(true);
      jComboBox2.removeAllItems();
      for(int i=0; i<numAtt; i++)
      {   jComboBox2.addItem(instances.getAttribute(i).getAttributeName());
          if(i==(numAtt-1))
              jComboBox2.setSelectedItem(instances.getAttribute(i).getAttributeName());
      }
      instances.setClassIndex(jComboBox2.getSelectedIndex());
      if (classifier instanceof BayesianLearning)
      {   try
          {   ((BayesianNetwork)classifier).setClassAttribute(instances.getAttribute(jComboBox2.getSelectedIndex()));
          }
          catch (Exception ex)
          {
          }
      }
  }

  /** Altera a classe
   *  @param e An ActionEvent
   *  */
  void jComboBox2_actionPerformed(ActionEvent e)
  {   if(jComboBox2.getSelectedIndex()>=0)
      {   instances.setClassIndex(jComboBox2.getSelectedIndex());
          if (classifier instanceof BayesianNetwork)
          {   try
              {   ((BayesianNetwork)classifier).setClassAttribute(instances.getClassAttribute());
              }
              catch (Exception ex)
              {   reference.setStatusBar("Error "+ex.getMessage());
              }
          }
      }
  }

  public void setTextArea(String text)
  {   jTextArea2.setText(text);
  }
}