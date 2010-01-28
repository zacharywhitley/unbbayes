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
package unbbayes.datamining.gui.neuralmodel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import unbbayes.controller.FileController;
import unbbayes.controller.JavaHelperController;
import unbbayes.controller.PrintMonitor;
import unbbayes.controller.PrintPreviewer;
import unbbayes.controller.PrintTable;
import unbbayes.datamining.classifiers.CombinatorialNeuralModel;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;

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
    resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource.class.getName());
    mainScreen = new NeuralModelMain(this);
    mainScreen.setController(this);
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
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] arff = {"ARFF"};
    String[] txt = {"TXT"};
    boolean fileOpenSuccess = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openFile2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar �cones de arquivos
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
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return fileOpenSuccess;
  }

  private void openFile(File selectedFile) throws Exception{
    instanceSet = FileController.getInstance().getInstanceSet(selectedFile, mainScreen);
    boolean numericAttributes = instanceSet.checkNumericAttributes();
    if (numericAttributes == true){
      throw new Exception(resource.getString("numericAttributesException"));
    }
    mainScreen.setTitle("CNM - " + selectedFile.getName());
    mainScreen.attributePanel.setInstances(instanceSet);
    mainScreen.attributePanel.enableComboBox(true);
  }

  /**
   * Used to sava a model.
   *
   * @return <code>true</code> if the file is saved successfully;
   *         <code>false</code> otherwise.
   * @throws Exception if the file is not saved successfully.
   */
  public boolean saveModel() throws Exception{
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] cnmString = {"cnm"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("saveModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar �cones de arquivos
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnmString, "Modelo Neural Combinat�rio (*.cnm)"));
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
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }

  private void modelPrunnig(){
    int minSupport = mainScreen.rulesPanel.getSupport();
    int minConfidence = mainScreen.rulesPanel.getConfidence();
    cnm.prunning(minSupport, minConfidence);
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
    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    String[] cnmString = {"cnm"};
    boolean success = false;
    fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
    fileChooser.setDialogTitle(resource.getString("openModel2"));
    fileChooser.setMultiSelectionEnabled(false);
    //adicionar FileView no FileChooser para desenhar �cones de arquivos
    fileChooser.setFileView(new FileIcon(mainScreen));
    fileChooser.addChoosableFileFilter(new SimpleFileFilter(cnmString, "Modelo Neural Combinat�ri (*.cnm)"));
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
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    return success;
  }

  /**
   * Used to call the help files.
   */
  public void help() throws Exception{
	  JavaHelperController.getInstance().openHelp(mainScreen);
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

    mainScreen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    if(instanceSet != null){
      maxOrder = mainScreen.optionsPanel.getMaxOrder();
      confidence = mainScreen.optionsPanel.getConfidence();
      support = mainScreen.optionsPanel.getSupport();
      cnm = new CombinatorialNeuralModel(maxOrder);
      cnm.buildClassifier(instanceSet);
      mainScreen.rulesPanel.setRulesPanel(cnm, confidence, support);
      mainScreen.inferencePanel.setNetwork(cnm);
    }
    mainScreen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * Used to show the rules generated by the model.
   *
   * @param table the table where the rule will be shown.
   */
  public void printTable(final JTable table) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        ArrayList<JTable> tables = new ArrayList<JTable>();
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
        ArrayList<JTable> tables = new ArrayList<JTable>();
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