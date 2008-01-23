/*
 * Created on 18/08/2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.aprendizagem.incrementalLearning.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.xml.bind.JAXBException;

import unbbayes.controller.FileController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ILIO {

   public ILIO() {
      super();

   }

   public ProbabilisticNetwork getNet(File file, BaseIO io) {
      try {
         return io.load(file);
      } catch (LoadException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (JAXBException je){
      	je.printStackTrace(); 
      }
      return null;
   }

   public List getSuficStatistics(File file) {
      FileInputStream fis;
      try {
         fis = new FileInputStream(file);
         ObjectInputStream ois = new ObjectInputStream(fis);
         return (List) ois.readObject();
      } catch (IOException e1) {
         e1.printStackTrace();
      } catch (ClassNotFoundException e1) {
         e1.printStackTrace();
      } 
      return null;

   }

   public void makeNetFile(File file, BaseIO io, ProbabilisticNetwork pn) {
      try {
         io.save(file, pn);
      } catch (IOException e) {
         e.printStackTrace();
      } catch (JAXBException je){
      	je.printStackTrace(); 
      }
   }

   public void makeContFile(List ssList, File file) {
      try {
         FileOutputStream fos = new FileOutputStream(file);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject((ArrayList) ssList);
         oos.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
   }
   
   public File getFile() {
       FileController fileController = FileController.getInstance();
       JFileChooser chooser = new JFileChooser(fileController
               .getCurrentDirectory());
       chooser.setMultiSelectionEnabled(false);
       chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
       // adicionar FileView no FileChooser para desenhar ícones de
       // arquivos
       int option = chooser.showSaveDialog(null);
       if (option == JFileChooser.APPROVE_OPTION) {
           File file = chooser.getSelectedFile();
           if (file != null) {
               return file;
           } 
       }
       return null;
   }
	
   public File chooseFile(String[] tipos, String title) {
       try {
           FileController fileController = FileController.getInstance();            
           JFileChooser chooser = new JFileChooser(fileController
                   .getCurrentDirectory());
           chooser.setMultiSelectionEnabled(false);
           chooser.addChoosableFileFilter(new SimpleFileFilter(tipos, tipos[0]));
           chooser.setDialogTitle(title);
           int option = chooser.showOpenDialog(null);
           if (option == JFileChooser.APPROVE_OPTION) {
               /* Seta o arquivo escolhido */
               return chooser.getSelectedFile();
           }
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
       return null;
   }

}