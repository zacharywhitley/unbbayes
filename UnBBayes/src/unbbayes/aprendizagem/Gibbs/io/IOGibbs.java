/*
 * Created on 18/08/2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package unbbayes.aprendizagem.Gibbs.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.JFileChooser;

import unbbayes.controller.FileController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IOGibbs {
	
	private byte data[][];
	private NodeList variables;
	private PrintStream ps;
	private File file;
	
	public IOGibbs(byte data[][], NodeList variables){
		this.data = data;
		this.variables = variables;
		try{
			file = getFile();
			ps = new PrintStream(new FileOutputStream(file));
		}catch(Exception e){
			e.printStackTrace();
		}
			
	}	
	
	
	public void makeFile(){
			makeFirstLine();
			Node node;
			byte index;
			for(int i = 0 ; i < data.length; i++){
				for(int j = 0 ; j < variables.size(); j++){
					node = variables.get(j);
					ps.print(node.getStateAt(data[i][j]));
				if(j != variables.size()-1){
					ps.print('\t');   	    	 	
				}else{
					ps.println();
				}   				   				   				
				}
			}  	   	   	
	}
   
	private void makeFirstLine(){
		 Node node;
		 for(int i = 0 ; i < variables.size();i++){
			node = variables.get(i);
			ps.print(node.getName());
			if(i != variables.size()-1){
				ps.print('\t');   	    	 	
			}else{
				ps.println();
			}
		 }  	 
	}
	
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
