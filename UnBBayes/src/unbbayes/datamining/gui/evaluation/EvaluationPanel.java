package unbbayes.datamining.gui.evaluation;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.BadLocationException;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.fronteira.*;

public class EvaluationPanel extends JPanel
{
  private JPanel jPanel63 = new JPanel();
  private BorderLayout borderLayout45 = new BorderLayout();
  private JPanel jPanel62 = new JPanel();
  private BorderLayout borderLayout44 = new BorderLayout();
  private BorderLayout borderLayout43 = new BorderLayout();
  private JButton jButton9 = new JButton();
  private BorderLayout borderLayout36 = new BorderLayout();
  private JPanel jPanel52 = new JPanel();
  private JPanel jPanel51 = new JPanel();
  private BorderLayout borderLayout34 = new BorderLayout();
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
  private GridLayout gridLayout7 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private TitledBorder titledBorder10;
  private TitledBorder titledBorder9;
  private TitledBorder titledBorder8;
  private TitledBorder titledBorder7;
  private TitledBorder titledBorder6;
  private ImageIcon image1;
  private Border border10;
  private JComboBox jComboBox1 = new JComboBox();
  private BorderLayout borderLayout2 = new BorderLayout();
  private InstanceSet instances;
  private Thread thread;
  private JTextArea jTextArea2 = new JTextArea();
  private JTextArea jTextArea1 = new JTextArea();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel1 = new JPanel();
  private JComboBox jComboBox3 = new JComboBox();
  private GridLayout gridLayout2 = new GridLayout();
  private JButton jButton7 = new JButton();
  private GridLayout gridLayout8 = new GridLayout();
  private JPanel jPanel61 = new JPanel();
  private JPanel jPanel60 = new JPanel();
  private JPanel jPanel59 = new JPanel();
  private JButton jButton8 = new JButton();
  private BorderLayout borderLayout42 = new BorderLayout();
  private JPanel jPanel58 = new JPanel();
  private JPanel jPanel57 = new JPanel();
  private JComboBox jComboBox2 = new JComboBox();
  private JPanel jPanel3 = new JPanel();
  private JTextField jTextField1 = new JTextField();
  private InstanceSet userTest;
  private JLabel jLabel1 = new JLabel();
  private JButton jButton1 = new JButton();
  private EvaluationMain reference;
  private JFileChooser fileChooser;

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
  { image1 = new ImageIcon("icones/salvar.gif");
    titledBorder10 = new TitledBorder(border10,"Classifier output");
    titledBorder9 = new TitledBorder(border10,"Select Class");
    titledBorder8 = new TitledBorder(border10,"Log");
    titledBorder7 = new TitledBorder(border10,"Test Options");
    titledBorder6 = new TitledBorder(border10,"Classifier");
    border10 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    jPanel62.setLayout(borderLayout45);
    jButton9.setEnabled(false);
    jButton9.setIcon(image1);
    jButton9.setMnemonic('S');
    jButton9.setText("Save information...");
    jButton9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton9_actionPerformed(e);
      }
    });
    jPanel52.setLayout(gridLayout7);
    jPanel51.setLayout(borderLayout36);
    jPanel50.setLayout(borderLayout43);
    jPanel2.setLayout(borderLayout33);
    jPanel49.setLayout(gridLayout2);
    jPanel48.setLayout(gridLayout1);
    jPanel47.setLayout(borderLayout44);
    jPanel45.setLayout(borderLayout34);
    gridLayout7.setRows(2);
    this.setLayout(borderLayout1);
    jPanel51.setBorder(titledBorder7);
    jPanel46.setBorder(titledBorder6);
    jPanel46.setLayout(borderLayout2);
    jPanel47.setBorder(titledBorder10);
    jPanel50.setBorder(titledBorder8);
    jPanel49.setBorder(titledBorder9);
    jTextArea2.setEditable(false);
    jTextArea1.setEditable(false);
    gridLayout1.setRows(3);
    gridLayout2.setRows(2);
    jButton7.setEnabled(false);
    jButton7.setText("Start");
    jButton7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton7_actionPerformed(e);
      }
    });
    gridLayout8.setColumns(2);
    jPanel59.setLayout(gridLayout8);
    jButton8.setEnabled(false);
    jButton8.setText("Stop");
    jButton8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton8_actionPerformed(e);
      }
    });
    jPanel58.setLayout(borderLayout42);
    jComboBox2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jComboBox2_actionPerformed(e);
      }
    });
    jComboBox3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jComboBox3_actionPerformed(e);
      }
    });
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jComboBox1.setEnabled(false);
    jComboBox3.setEnabled(false);
    jButton1.setEnabled(false);
    jComboBox2.setEnabled(false);
    jPanel2.add(jPanel46, BorderLayout.NORTH);
    jPanel46.add(jComboBox1, BorderLayout.CENTER);
    jPanel2.add(jPanel45, BorderLayout.CENTER);
    jPanel47.add(jPanel62, BorderLayout.CENTER);
    jPanel62.add(jScrollPane2, BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTextArea2, null);
    jPanel47.add(jPanel63, BorderLayout.SOUTH);
    jPanel63.add(jButton9, null);
    jPanel45.add(jPanel48, BorderLayout.WEST);
    jPanel45.add(jPanel47, BorderLayout.CENTER);
    jPanel48.add(jPanel51, null);
    jPanel51.add(jPanel52, BorderLayout.CENTER);
    jPanel52.add(jPanel1, null);
    jPanel1.add(jComboBox3, null);
    jPanel52.add(jPanel3, null);
    jPanel3.add(jButton1, null);
    jPanel48.add(jPanel49, null);
    jPanel59.add(jPanel60, null);
    jPanel60.add(jButton7, null);
    jPanel59.add(jPanel61, null);
    jPanel61.add(jButton8, null);
    jPanel49.add(jPanel57, null);
    jPanel57.add(jComboBox2, null);
    jPanel49.add(jPanel58, null);
    jPanel58.add(jPanel59, BorderLayout.CENTER);
    jPanel48.add(jPanel50, null);
    jPanel50.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea1, null);
    this.add(jPanel2, BorderLayout.CENTER);
    jComboBox1.addItem("Id3 Classifier");
    jComboBox1.addItem("Naive Bayes Classifier");
    jComboBox3.addItem("Use training set");
    jComboBox3.addItem("Suplied test set");
    jComboBox3.addItem("Cross validation");
    jComboBox3.addItem("Percentage split");
  }

  void jButton9_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(reference.getCurrentDirectory());
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
          {   //JOptionPane.showConfirmDialog(this,"Bad location "+ble.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
              reference.setStatusBar("Bad location "+ble.getMessage());
          }
          catch (IOException ioe)
          {   //JOptionPane.showConfirmDialog(this,"Error writing file "+selectedFile.getName()+" "+ioe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
              reference.setStatusBar("Error writing file "+selectedFile.getName()+" "+ioe.getMessage());
          }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jButton7_actionPerformed(ActionEvent e)
  {   if (thread == null)
      {   jButton7.setEnabled(false);
          jButton8.setEnabled(true);
          thread = new Thread()
          {   public void run()
              {   Classifier classifier;
                  int numAttributes = instances.numAttributes();
                  int classifierType = jComboBox1.getSelectedIndex();
                  int testMode = jComboBox3.getSelectedIndex();
	          int numFolds = 10, percent = 66;

                  setCursor(new Cursor(Cursor.WAIT_CURSOR));
                  try
                  {   switch (testMode)
                      {   case 0 :    break;
                          case 1 :    // Check the test instance compatibility
	                              if (userTest == null)
                                      {   throw new Exception("No user test set has been opened");
	                              }
   	                              if (numAttributes != userTest.numAttributes())
                                      {   throw new Exception("Train and test set are not compatible");
                                      }
                                      for (int i = 0; i < numAttributes; i++)
                                      {   if (!(instances.getAttribute(i).equals(userTest.getAttribute(i))))
                                          {   throw new Exception("Train and test set are not compatible");
                                          }
                                      }
	                              userTest.setClassIndex(instances.getClassIndex());
                                      break;
                          case 2 :    numFolds = Integer.parseInt(jTextField1.getText());
	                              if (numFolds <= 1)
                                      {   throw new Exception("Number of folds must be greater than 1");
	                              }
                                      break;
                          case 3 :    percent = Integer.parseInt(jTextField1.getText());
	                              if ((percent <= 0) || (percent >= 100))
                                      {   throw new Exception("Percentage must be between 0 and 100");
	                              }
                                      break;
                          default :   throw new Exception("Unknown test mode");
                      }
                      switch (classifierType)
                      {   case 0: classifier = new Id3();
                                  break;
                          case 1: classifier = new NaiveBayes();
                                  break;
                          default: classifier = new NaiveBayes();
                      }
                      StringBuffer outBuff = new StringBuffer();
	              String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
	              String classifierName = classifier.getClass().getName().substring("unbbayes.datamining.classifiers.".length());
                      jTextArea1.append(currentHour+classifierName+/*" "+jComboBox3.getSelectedItem()+*/"\n");
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

                      switch (testMode)
                      {   case 0:   // Test on training
	                            outBuff.append("evaluate on training data\n\n");
                                    reference.setStatusBar("Evaluation on training data");
	                            break;
	                  case 1:   // Test on user split
	                            outBuff.append("user supplied test set: " + userTest.numInstances() + " instances\n\n");
                                    reference.setStatusBar("Supplied test set");
	                            break;
	                  case 2:   // CV mode
	                            outBuff.append("" + numFolds + "-fold cross-validation\n\n");
                                    reference.setStatusBar("Cross Validation");
	                            break;
	                  case 3:   // Percent split
	                            outBuff.append("split " + percent + "% train, remainder test\n\n");
                                    reference.setStatusBar("Percent split");
	                            break;
	              }
                      classifier.buildClassifier(instances);
                      outBuff.append("=== Classifier model ===\n\n");
	              outBuff.append(classifier.toString() + "\n\n");
                      Evaluation eval;
                      switch (testMode)
                      {   case 0:   // Test on training
	                            eval = new Evaluation(instances);
	                            eval.evaluateModel(classifier);
                                    outBuff.append(eval.toString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toClassDetailsString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toMatrixString());
                                    break;
	                  case 1:   // Test on user split
	                            outBuff.append("user supplied test set: " + userTest.numInstances() + " instances\n\n");
	                            eval = new Evaluation(instances);
                                    eval.evaluateModel(classifier,userTest);
                                    outBuff.append(eval.toString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toClassDetailsString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toMatrixString());
                                    break;
	                  case 2:   // CV mode
	                            if (instances.getClassAttribute().isNominal())
                                    {   outBuff.append("" + numFolds + "-fold stratified cross-validation\n\n");
                                    }
                                    else
                                    {   outBuff.append("" + numFolds + "-fold cross-validation\n\n");
                                    }
	                            eval = new Evaluation(instances);
                                    eval.crossValidateModel(classifier,numFolds);
                                    outBuff.append(eval.toString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toClassDetailsString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toMatrixString());
                                    break;
	                  case 3:   // Percent split
	                            outBuff.append("split " + percent + "% train\n\n");
	                            instances.randomize(new Random(42));
	                            int numInstances = instances.numInstances();
                                    int trainSize = numInstances * percent / 100;
                                    int testSize = numInstances - trainSize;
	                            InstanceSet train = new InstanceSet(instances, 0, trainSize);
	                            InstanceSet test = new InstanceSet(instances, trainSize, testSize);
	                            eval = new Evaluation(train);
                                    eval.evaluateModel(classifier,test);
                                    outBuff.append(eval.toString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toClassDetailsString());
                                    outBuff.append("\n");
                                    outBuff.append(eval.toMatrixString());
                                    break;
	              }
                      jTextArea2.setText(outBuff.toString());
                      jButton9.setEnabled(true);
                      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                  }
                  catch (Exception e)
                  {   setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                      JOptionPane.showConfirmDialog(EvaluationPanel.this,"Exception "+e.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
                  }

                  thread = null;  // Termina a thread.
                  jButton7.setEnabled(true);
                  jButton8.setEnabled(false);
              }
          };
          thread.start();

      }
  }

  void jButton8_actionPerformed(ActionEvent e)
  {   if (thread != null)
      {   thread.interrupt();
      }
      jButton7.setEnabled(true);
      jButton8.setEnabled(false);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  public void setInstances(InstanceSet inst)
  {   this.instances = inst;
      int numAtt = instances.numAttributes();
      jComboBox1.setEnabled(true);
      jComboBox2.setEnabled(true);
      jComboBox3.setEnabled(true);
      jButton1.setEnabled(true);
      jButton7.setEnabled(true);
      jComboBox2.removeAllItems();
      for(int i=0; i<numAtt; i++)
      {   jComboBox2.addItem(instances.getAttribute(i).getAttributeName());
          if(i==(numAtt-1))
              jComboBox2.setSelectedItem(instances.getAttribute(i).getAttributeName());
      }
      instances.setClassIndex(jComboBox2.getSelectedIndex());
  }

  void jComboBox2_actionPerformed(ActionEvent e)
  {   if(jComboBox2.getSelectedIndex()>=0)
          instances.setClassIndex(jComboBox2.getSelectedIndex());
  }

  void jComboBox3_actionPerformed(ActionEvent e)
  {   int selectedIndex = jComboBox3.getSelectedIndex();
      jPanel3.removeAll();
      if(selectedIndex >= 0)
      {   switch (selectedIndex)
          {   case 0 :    break;  // Use training set
              case 1 :    jButton1.setText("Set..."); // Supplied test set
                          jPanel3.add(jButton1, null);
                          jPanel3.updateUI();
                          break;
              case 2 :    jTextField1.setText("10");  // Cross Validation
                          jTextField1.setColumns(3);
                          jLabel1.setText("Folds");
                          jPanel3.add(jTextField1, null);
                          jPanel3.add(jLabel1, null);
                          jPanel3.updateUI();
                          break;
              case 3 :    jTextField1.setText("66");  // Percentage split
                          jTextField1.setColumns(3);
                          jLabel1.setText("%");
                          jPanel3.add(jTextField1, null);
                          jPanel3.add(jLabel1, null);
                          jPanel3.updateUI();
                          break;
          }
      }
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      String[] s1 = {"ARFF"};
      String[] s2 = {"TXT"};
      fileChooser = new JFileChooser(reference.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EvaluationPanel.this));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          setBaseInstancesFromFile(selectedFile);
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void setBaseInstancesFromFile(File f)
  {   try
      {   Reader r = new BufferedReader(new FileReader(f));
	  Loader loader;
          String fileName = f.getName();
          if (fileName.regionMatches(true,fileName.length() - 5,".arff",0,5))
          {   loader = new ArffLoader(r,reference);
          }
          else if (fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
          {   loader = new TxtLoader(r,reference);
          }
          else
          {   throw new IOException(" Extensão de arquivo não conhecida.");
          }
          userTest = loader.getInstances();
          reference.setStatusBar("Arquivo de teste aberto com sucesso");
          r.close();
      }
      catch (NullPointerException npe)
      {   //JOptionPane.showConfirmDialog(this,"NullPointer "+npe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
          reference.setStatusBar("NullPointer "+npe.getMessage());
      }
      catch (FileNotFoundException fnfe)
      {   //JOptionPane.showConfirmDialog(this,"FileNotFound"+fnfe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
          reference.setStatusBar("FileNotFound "+fnfe.getMessage());
      }
      catch (IOException ioe)
      {   //JOptionPane.showConfirmDialog(this,"ErrorOpen"+ioe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
          reference.setStatusBar("ErrorOpen "+ioe.getMessage());
      }
      catch(Exception e)
      {   //JOptionPane.showConfirmDialog(this,"Exception "+e.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
          reference.setStatusBar("Exception "+e.getMessage());
          e.printStackTrace();
      }
  }
}