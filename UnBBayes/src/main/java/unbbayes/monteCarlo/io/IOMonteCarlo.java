/*
 * Created on 21/07/2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.monteCarlo.io;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFileChooser;

import unbbayes.controller.FileController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IOMonteCarlo {

   private byte[][] matrix;
   private File file;
   private PrintStream ps;  
   
   public IOMonteCarlo(byte[][] matrix) throws IOException{
   	    file = getFile();
   	    ps = new PrintStream(new FileOutputStream(file));
   		this.matrix = matrix;      
   }
   
   public void makeFile(int[] positions, ProbabilisticNetwork pn){
   		makeFirstLine(pn);
   		Node node;
   		byte index;
   		for(int i = 0 ; i < matrix.length; i++){
   			for(int j = 0 ; j < pn.getNodeCount(); j++){
   				node = pn.getNodeAt(j);
   				ps.print(node.getStateAt(matrix[i][positions[j]]));
				if(j != pn.getNodeCount()-1){
					ps.print('\t');   	    	 	
				}else{
					ps.println();
				}   				   				   				
   			}
   		}  	   	   	
   }
   
   private void makeFirstLine(ProbabilisticNetwork pn){
   	 Node node;
   	 for(int i = 0 ; i < pn.getNodeCount();i++){
   	 	node = pn.getNodeAt(i);
   	 	ps.print(node.getName());
   	 	if(i != pn.getNodeCount()-1){
   	 		ps.print('\t');   	    	 	
   	 	}else{
   	 		ps.println();
   	 	}
   	 }  	 
   }
   
   //TODO Isto esta mesmo sendo utilizado? 
   private File getFile(){	
		String[] nets = new String[] { "net", "xml" };
		FileController fileController = FileController.getInstance();;
		JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// adicionar FileView no FileChooser para desenhar ícones de
		// arquivos		
		chooser.addChoosableFileFilter(
			new SimpleFileFilter(
				nets,
				"txt"));
		int option = chooser.showSaveDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				return file;				
			}
		}
		return null;		 
   }

}
