package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.gui.*;
import unbbayes.prs.bn.*;
import unbbayes.util.GraphPaperLayout;

public class EvaluationPanel extends JPanel
{
  private JPanel jPanel63 = new JPanel();
  private BorderLayout borderLayout45 = new BorderLayout();
  private JPanel jPanel62 = new JPanel();
  private BorderLayout borderLayout44 = new BorderLayout();
  private JButton jButton9 = new JButton();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel47 = new JPanel();
  private JPanel jPanel46 = new JPanel();
  private JScrollPane jScrollPane2 = new JScrollPane();
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
  private Thread thread; //fasffsafsasfafsafasfa vai ser removido depois
  private JTextArea jTextArea2 = new JTextArea();
  private JTextField jTextField1 = new JTextField();
  private InstanceSet userTest;
  private JLabel jLabel1 = new JLabel();
  private EvaluationMain reference;
  private JFileChooser fileChooser;
  private JLabel jLabel2 = new JLabel();
  private JButton jButton7 = new JButton();
  private GridLayout gridLayout2 = new GridLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel1 = new JPanel();
  private JComboBox jComboBox2 = new JComboBox();
  private JPanel jPanel49 = new JPanel();
  //private JButton jButton8 = new JButton();
  private JPanel jPanel50 = new JPanel();
  private BorderLayout borderLayout43 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel5 = new JPanel();
  private JPanel jPanel6 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JRadioButton jRadioButton1 = new JRadioButton();
  private JRadioButton jRadioButton2 = new JRadioButton();
  private JRadioButton jRadioButton3 = new JRadioButton();
  private GraphPaperLayout paperLayout = new GraphPaperLayout(new Dimension(4,9));
  private JButton jButton1 = new JButton();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private TitledBorder titledBorder1;
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea jTextArea1 = new JTextArea();
  private int[] priorityClassValues;
  private float[] priorityProbabilities;

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
  {
    salvarIcon = IconController.getInstance().getSaveIcon();
    titledBorder10 = new TitledBorder(border10,"Classifier output");
    titledBorder9 = new TitledBorder(border10,"Select Class");
    titledBorder8 = new TitledBorder(border10,"Log");
    titledBorder7 = new TitledBorder(border10,"Test Options");
    titledBorder6 = new TitledBorder(border10,"Model");
    titledBorder1 = new TitledBorder(border10,"Evaluation Type");
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
    jPanel2.setLayout(paperLayout);
    jPanel47.setLayout(borderLayout44);
    this.setLayout(borderLayout1);
    jPanel46.setBorder(titledBorder6);
    jPanel46.setLayout(borderLayout2);
    jPanel47.setBorder(titledBorder10);
    jTextArea2.setEditable(false);
    jLabel2.setText("  ");
    jButton7.setEnabled(false);
    jButton7.setText("Start");
    jButton7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton7_actionPerformed(e);
      }
    });
    gridLayout2.setColumns(2);
    gridLayout1.setColumns(1);
    gridLayout1.setRows(2);
    jPanel1.setLayout(gridLayout2);
    jComboBox2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jComboBox2_actionPerformed(e);
      }
    });
    jComboBox2.setEnabled(false);
    jPanel49.setLayout(gridLayout1);
    jPanel49.setBorder(titledBorder9);
    jPanel49.setMinimumSize(new Dimension(148, 104));
    //jButton8.setEnabled(false);
    //jButton8.setText("Stop");
    /*jButton8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton8_actionPerformed(e);
      }
    });*/
    jPanel50.setLayout(borderLayout43);
    jPanel50.setBorder(titledBorder8);
    jPanel3.setLayout(gridLayout3);
    gridLayout3.setRows(3);
    jPanel3.setBorder(titledBorder1);
    jPanel6.setLayout(borderLayout3);
    jPanel5.setLayout(borderLayout4);
    jPanel4.setLayout(borderLayout5);
    jRadioButton1.setEnabled(false);
    jRadioButton1.setSelected(true);
    jRadioButton1.setText("Normal");
    jRadioButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jRadioButton_actionPerformed(e);
      }
    });
    jRadioButton2.setEnabled(false);
    jRadioButton2.setText("Relative probabilities");
    jRadioButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jRadioButton_actionPerformed(e);
      }
    });
    jRadioButton3.setEnabled(false);
    jRadioButton3.setText("Absolute probabilities");
    jRadioButton3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jRadioButton_actionPerformed(e);
      }
    });
    jButton1.setEnabled(false);
    jButton1.setText("Set ...");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jTextArea1.setEditable(false);
    jPanel46.add(jLabel2,  BorderLayout.CENTER);
    jPanel47.add(jPanel62, BorderLayout.CENTER);
    jPanel62.add(jScrollPane2, BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTextArea2, null);
    jPanel47.add(jPanel63, BorderLayout.SOUTH);
    jPanel63.add(jButton9, null);
    jPanel3.add(jPanel6, null);
    jPanel6.add(jRadioButton1, BorderLayout.CENTER);
    jPanel3.add(jPanel5, null);
    jPanel5.add(jRadioButton2, BorderLayout.NORTH);
    jPanel3.add(jPanel4, null);
    jPanel4.add(jRadioButton3, BorderLayout.CENTER);
    jPanel4.add(jButton1,  BorderLayout.EAST);
    jPanel49.add(jComboBox2, null);
    jPanel49.add(jPanel1, null);
    jPanel1.add(jButton7, null);
    //jPanel1.add(jButton8, null);
    jPanel50.add(jScrollPane1,  BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea1, null);
    jPanel2.add(jPanel46, new Rectangle(0,0,4,1));
    jPanel2.add(jPanel3, new Rectangle(0,1,1,2));
    jPanel2.add(jPanel49, new Rectangle(0,3,1,2));
    jPanel2.add(jPanel50, new Rectangle(0,5,1,4));
    jPanel2.add(jPanel47, new Rectangle(1,1,3,8));
    this.add(jPanel2, BorderLayout.CENTER);
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton2);
    buttonGroup1.add(jRadioButton3);
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
  {   if (thread == null)
      {   jButton7.setEnabled(false);
          //jButton8.setEnabled(true);
          thread = new Thread()
          {   public void run()
              {   //Classifier classifier;
                  int numAttributes = instances.numAttributes();
                  if (classifier instanceof BayesianLearning)
                  {   if (jRadioButton1.isSelected())
                      {   ((BayesianLearning)classifier).setNormalClassification();
                      }
                      else if (jRadioButton2.isSelected())
                      {   ((BayesianLearning)classifier).setRelativeClassification();
                      }
                      else if (jRadioButton3.isSelected())
                      {   ((BayesianLearning)classifier).setAbsoluteClassification(priorityClassValues,priorityProbabilities);
                          for (int i=0;i<priorityClassValues.length;i++)
                          {   System.out.println(priorityClassValues[i]+" "+priorityProbabilities[i]);
                          }
                      }
                  }
                  setCursor(new Cursor(Cursor.WAIT_CURSOR));
                  try
                  {   StringBuffer outBuff = new StringBuffer();
	              String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
	              String classifierName = classifier.getClass().getName().substring("unbbayes.datamining.classifiers.".length());
                      jTextArea1.append("Started  "+currentHour+classifierName+/*" "+jComboBox3.getSelectedItem()+*/"\n");
                      outBuff.append("=== Run information ===\n\n");
	              outBuff.append("Scheme:           " + classifierName);
	              outBuff.append("\n");
                      outBuff.append("Relation:         " + instances.getRelationName() + '\n');
                      outBuff.append("Instances:        " + instances.numWeightedInstances() + '\n');
                      outBuff.append("Attributes:       " + instances.numAttributes() + '\n');
                      if (numAttributes < 100)
                      {   for (int i = 0; i < numAttributes; i++)
                          {   outBuff.append("                  "+instances.getAttribute(i).getAttributeName()+'\n');
                          }
                      }
                      else
                      {   outBuff.append("                  [list of attributes omitted]\n");
                      }
                      outBuff.append("ClassAttribute:   " + instances.getClassAttribute().getAttributeName()+'\n');
                      outBuff.append("Test mode:    ");
                         outBuff.append("=== Classifier model ===\n\n");
                      outBuff.append(classifier.toString() + "\n\n");
                      Evaluation eval;

                                eval = new Evaluation(instances,classifier);
                                ProgressDialog progressDialog = new ProgressDialog (null, eval);
								boolean successStatus = progressDialog.load();

                                //eval.evaluateModel(classifier);
                                    outBuff.append(eval.toString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toClassDetailsString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toMatrixString());

                      jTextArea2.setText(outBuff.toString());
                      currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
	              jTextArea1.append("Finished "+currentHour+classifierName+/*" "+jComboBox3.getSelectedItem()+*/"\n");
                      jButton9.setEnabled(true);
                      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                  }
                  catch (Exception e)
                  {   setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                      JOptionPane.showConfirmDialog(EvaluationPanel.this,"Exception "+e.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
                  }

                  thread = null;  // Termina a thread.
                  jButton7.setEnabled(true);
                  //jButton8.setEnabled(false);
              }
          };
          thread.start();

      }

  }

  /** Interrompe a thread
   *  @param e One ActionEvent
   *  */
  /*void jButton8_actionPerformed(ActionEvent e)
  {   if (thread != null)
      {   thread.interrupt();
          String classifierName = classifier.getClass().getName().substring("unbbayes.datamining.classifiers.".length());
          String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
          jTextArea1.append("Finished "+currentHour+classifierName+" "+jComboBox2.getSelectedItem()+"\n");
      }
      jButton7.setEnabled(true);
      jButton8.setEnabled(false);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }*/

  /** Altera um modelo
   *  @param classifier A classifier
   *  @param inst A InstanceSet
   *  */
  public void setModel(Classifier classifier,InstanceSet inst)
  {   jTextArea2.setText("");
      if (classifier instanceof BayesianLearning && !(classifier instanceof CombinatorialNeuralModel))
      {   jLabel2.setText("Model - Bayesian Network");
      }
      else if (classifier instanceof DecisionTreeLearning)
      {   jLabel2.setText("Model - Decision Tree");
      }
      else if (classifier instanceof CombinatorialNeuralModel)
      {   jLabel2.setText("Model - Combinatorial Neural Model");
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
      if (classifier instanceof BayesianLearning && !(classifier instanceof CombinatorialNeuralModel))
      {   jRadioButton1.setEnabled(true);
          jRadioButton2.setEnabled(true);
          jRadioButton3.setEnabled(true);
          try
          {   ((BayesianNetwork)classifier).setClassAttribute(instances.getAttribute(jComboBox2.getSelectedIndex()));
              ProbabilisticNode classNode = ((BayesianNetwork)classifier).getClassNode();
              int statesSize = classNode.getStatesSize();
              priorityClassValues = new int[statesSize];
              priorityProbabilities = new float[statesSize];
              for (int i=0;i<statesSize;i++)
              {   priorityClassValues[i] = i;
                  priorityProbabilities[i] = (float)classNode.getMarginalAt(i);
              }
          }
          catch (Exception ex)
          {   reference.setStatusBar(ex.getMessage());
          }
      }
      else if (classifier instanceof DecisionTreeLearning || classifier instanceof CombinatorialNeuralModel)
      {   jRadioButton1.setEnabled(true);
          jRadioButton2.setEnabled(false);
          jRadioButton3.setEnabled(false);
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
              {   Attribute classAttribute = instances.getClassAttribute();
                  ((BayesianNetwork)classifier).setClassAttribute(classAttribute);
                  reference.setStatusBar("Class Attribute "+classAttribute.getAttributeName()+" Set");
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

  void jRadioButton_actionPerformed(ActionEvent e)
  {   if (e.getSource() == jRadioButton3)
      {   jButton1.setEnabled(true);
      }
      else
      {   jButton1.setEnabled(false);
      }
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   if (classifier instanceof BayesianNetwork)
      {   ((BayesianNetwork)classifier).resetNet();
          new EvaluationOptions(((BayesianNetwork)classifier).getClassNode(),this);
      }
  }

  public void setAbsoluteValues(int[] priorityClassValues,float[] priorityProbabilities)
  {   this.priorityClassValues = priorityClassValues;
      this.priorityProbabilities = priorityProbabilities;
  }

}