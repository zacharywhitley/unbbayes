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
		// adicionar FileView no FileChooser para desenhar �cones de
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
