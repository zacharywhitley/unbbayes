package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.print.*;
import javax.swing.*;
import unbbayes.controller.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.gui.*;

/**
 *  Class that implements a controller of the program behavior.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class NeuralModelController {
  private CombinatorialNeuralModel cnm = null;
  private NeuralModelMain mainScreen;
  private JFileChooser fileChooser;
  private InstanceSet instanceSet;
  private ResourceBundle resource;
  private File file;

  /**
   * Builds a new controller.
   */
  public NeuralModelController(){
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
    mainScreen = new NeuralModelMain(this);
    mainScreen.setController(this);
//    mainScreen.rulesPanel.setController(this);
  }

  /**
   * Used to get the internal frame of the CNM.
   *
   * @return the internal frame.
   * @see {@link JInternalFrame}
   */
  public JInternalFrame getCnmFrame(){
    return mainScreen;
  }

  /**
   * Used to open a new training set.
   *
   * @return <code>true</code> if the file is loaded successfully;
   *         <code>false</code> otherwise.
   * @throws Exception if the file is not openned successfully.
   */
  protected boolean openFile() throws Exception{
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    boolean fileOpenSuccess = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openFile2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(txt, "TxtFiles (*.txt)"));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(arff, "ArffFiles (*.arff)"));
    int returnValue = fileChooser.showOpenDialog(mainScreen);
    if (returnValue == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      openFile(selectedFile);
      file = selectedFile;
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      fileOpenSuccess = true;
    }
    return fileOpenSuccess;
  }

  private void openFile(File selectedFile) throws Exception{
    instanceSet = FileController.getInstance().getInstanceSet(selectedFile, mainScreen);
    if (instanceSet!=null)
    {
      boolean numericAttributes = instanceSet.checkNumericAttributes();
      if (numericAttributes == true){
        throw new Exception(resource.getString("numericAttributesException"));
      }
      mainScreen.setTitle("CNM - " + selectedFile.getName());
      mainScreen.attributePanel.setInstances(instanceSet);
      mainScreen.attributePanel.enableComboBox(true);
    }
    else
    {
      // Setar status bar com operação de abertura de arquivo cancelada
      throw new Exception("Operação cancelada");
    }
  }

  /**
   * Used to sava a model.
   *
   * @return <code>true</code> if the file is saved successfully;
   *         <code>false</code> otherwise.
   * @throws Exception if the file is not saved successfully.
   */
  public boolean saveModel() throws Exception{
    String[] cnmString = {"cnm"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("saveModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnmString, "Modelo Neural Combinatório (*.cnm)"));
    int returnVal = fileChooser.showSaveDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      String fileName = selectedFile.getName();
      if (!fileName.regionMatches(true,fileName.length() - 4,".cnm",0,4)){
        selectedFile = new File(selectedFile.getAbsolutePath()+".cnm");
      }
      modelPrunnig();
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile));
      out.writeObject(cnm);
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      success = true;
    }
    return success;
  }

  private void modelPrunnig(){
    int minSupport = mainScreen.rulesPanel.getSupport();
    int minConfidence = mainScreen.rulesPanel.getConfidence();
//    cnm.prunning(minSupport, minConfidence);
  }

  /**
   * Used to open a saved CNM model.
   *
   * @return <code>true</code> if the file is openned successfully;
   *         <code>false</code> otherwise.
   * @throws IOException if the file is not openned successfully.
   * @throws ClassNotFoundException if the file is not openned successfully.
   */
  public boolean openModel() throws IOException, ClassNotFoundException{
    String[] cnmString = {"cnm"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar ícones de arquivos
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnmString, "Modelo Neural Combinatóri (*.cnm)"));
    int returnVal = fileChooser.showOpenDialog(mainScreen);
    if (returnVal == JFileChooser.APPROVE_OPTION){
      File selectedFile = fileChooser.getSelectedFile();
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
      cnm = null;
      cnm = (CombinatorialNeuralModel)in.readObject();
      mainScreen.tabbedPaneRules.removeAll();
      mainScreen.rulesPanel = new RulesPanel(this);
      mainScreen.rulesPanel.setRulesPanel(cnm, cnm.getConfidence(), cnm.getSupport());
      mainScreen.tabbedPaneRules.add(mainScreen.rulesPanel, BorderLayout.CENTER);
      mainScreen.inferencePanel.setNetwork(cnm);
      mainScreen.setTitle("CNM - " + resource.getString("model") + " " + selectedFile.getName());
      FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
      file = selectedFile;
      success = true;
    }
    return success;
  }

  /**
   * Used to call the help files.
   */
  public void help(){
    //FileController.getInstance().openHelp(this);
  }

  /**
   * Used to construct the model based on the training set previously loaded.
   *
   * @throws Exception if the the model is not constructed successfully.
   */
  public void learn() throws Exception{
    int maxOrder;
    int confidence;
    int support;

    if(instanceSet != null){
      maxOrder = mainScreen.optionsPanel.getMaxOrder();
      confidence = mainScreen.optionsPanel.getConfidence();
      support = mainScreen.optionsPanel.getSupport();
      cnm = new CombinatorialNeuralModel(maxOrder);
      cnm.buildClassifier(instanceSet);
      mainScreen.rulesPanel.setRulesPanel(cnm, confidence, support);
      mainScreen.inferencePanel.setNetwork(cnm);
    }
  }

  /**
   * Used to show the rules generated by the model.
   *
   * @param table the table where the rule will be shown.
   */
  public void printTable(final JTable table) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        ArrayList tables = new ArrayList();
        tables.add(table);
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        PrintTable printTable = new PrintTable(tables, file.getName().toUpperCase(), pageFormat);
        PrintMonitor printMonitor = new PrintMonitor(printTable);
        try {
          printMonitor.performPrint(true);
        } catch (PrinterException pe) {
          JOptionPane.showMessageDialog(mainScreen, resource.getString("printException") + pe.getMessage());
        }
      }
    });
    t.start();
  }

  /**
   * Used to preview the printing of the rules.
   *
   * @param table the table where the rules are being shown.
   */
  public void printPreviewer(final JTable table) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        ArrayList tables = new ArrayList();
        tables.add(table);
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        PrintTable printTable = new PrintTable(tables, file.getName().toUpperCase(), pageFormat);
        PrintPreviewer printPreviewer = new PrintPreviewer(printTable, 0);
        JDialog dlg = new JDialog();
        dlg.getContentPane().add(printPreviewer);
        dlg.setSize(400, 300);
        dlg.setVisible(true);
      }
    });
    t.start();
  }

}