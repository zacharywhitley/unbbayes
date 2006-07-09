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

import javax.xml.bind.JAXBException;

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

   /**
    * 
    */

   private File file;

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

}
