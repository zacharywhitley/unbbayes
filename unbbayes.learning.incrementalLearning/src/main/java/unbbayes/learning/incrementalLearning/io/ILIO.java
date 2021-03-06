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
package unbbayes.learning.incrementalLearning.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.controller.FileHistoryController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Administrador
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 
 * @author Shou Matsumoto : refactories for plugin support
 */
public class ILIO {

   public ILIO() {
      super();

   }

   public ProbabilisticNetwork getNet(File file, BaseIO io) {
      try {
         return (ProbabilisticNetwork)io.load(file);
      } catch (LoadException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e){
      	e.printStackTrace(); 
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
      } catch (Exception e){
      	e.printStackTrace(); 
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
       FileHistoryController fileHistoryController = FileHistoryController.getInstance();
       JFileChooser chooser = new JFileChooser(fileHistoryController
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
	
   public File chooseFile(String[] types, String title) {
       try {
           FileHistoryController fileHistoryController = FileHistoryController.getInstance();            
           JFileChooser chooser = new JFileChooser(fileHistoryController
                   .getCurrentDirectory());
           chooser.setMultiSelectionEnabled(false);
           chooser.addChoosableFileFilter(new SimpleFileFilter(types, this.generateDescriptionFromTypes(title, types)));
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

   /**
    * It just creates a string describing a file type.
    * @param types
    * @return
    */
	private String generateDescriptionFromTypes(String title, String[] types) {
		String ret = title + " - ";
		for (int i = 0; i < types.length; i++) {
			ret += types[i];
			if (i + 1 < types.length) {
				ret += ", ";
			}
		}
		return ret;
	}

}
