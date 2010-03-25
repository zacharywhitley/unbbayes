/*
 * Created on 15/04/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package unbbayes.learning.generator;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import unbbayes.learning.ConstructionController;
import unbbayes.controller.FileHistoryController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;

/**
 * @author custodio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MissingFilesGenerator {

	private int[][] data;
	private List<Node> variables;
	private PrintStream ps;
	/**
	 * 
	 */
	public MissingFilesGenerator(double percentagem) {
	    Map linhas = new HashMap();	    
		File file = getFile();
		ConstructionController cc = new ConstructionController(file);
		this.data = cc.getMatrix();
		int[][] copiaData = new int[data.length][data[0].length];
		for(int i = 0 ; i < data.length; i++){
		    for(int j = 0 ; j < data[i].length; j++){
		        copiaData[i][j] = data[i][j];
		    }
		}
		this.variables = cc.getVariables();		
		int quantidade = (int)(data.length*variables.size()*percentagem/100);		
		int cont = 0;
		int contadores0[] = new int[data[0].length];
		int contadores1[] = new int[data[0].length];
		int conts[] = new int[data[0].length];
		double valor = 0.0;
		for(int i = 0;  i < data.length; i++){
		    for(int j =0 ; j < data[i].length; j++){
		        if(data[i][j] == 0){
		            contadores0[j]++;		            
		            conts[j]++;		        
		        }else if(data[i][j] == 1){
		            conts[j]++;
		            contadores1[j]++;		            
		        }else if(data[i][j] == -1){
		            cont++;
		        }
		    }
		    if((i+1)% 25 == 0 ){
		        System.out.println("Contador = " + cont);		       
		        valor += Math.pow(cont -90,2)/90;
		        cont = 0;
		    }
		}
		System.out.println("Valor = " + valor);
		System.out.println(file.getName());
		System.out.println("Total = " + cont);
		for(int i = 0 ; i < contadores0.length; i++){		    
		    System.out.println("Estado 0 de " + i + " = " + contadores0[i]+ ", Estado 1 de " + i + " = " + contadores1[i]+ " , N� valores = " + conts[i]);
		    System.out.println("\tDistribui��o de " + i + ": Estado 0 = " + contadores0[i]/(float)conts[i] + ", Estado 1 = " + contadores1[i]/(float)conts[i]);
		}
		System.exit(0);
		/*for(int i = 0; i < 10; i++){
		    while(cont < quantidade){
			    int linha = (int)Math.floor(Math.random()*data.length);
				int coluna = (int)Math.floor(Math.random()*variables.size());
			    if(linhas.containsKey(new Integer(linha))){
			        boolean[] col = (boolean[])linhas.get(new Integer(linha));
			        if(col[coluna]){
			            continue;
			        }
			        col[coluna] = true;
			        data[linha][coluna] = -1;
			        cont++;
			    }else{
			        boolean[] col = new boolean[variables.size()];
			        col[coluna] = true;
			        linhas.put(new Integer(linha),col);
			        data[linha][coluna] = -1;
			        cont++;
			        
			    }
			}
			try {
				file = getFile();
				ps = new PrintStream(new FileOutputStream(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
			cont = 0;
			makeFile();
			linhas = new HashMap();	
			for(int k = 0 ; k < data.length; k++){
			    for(int j = 0 ; j < data[k].length; j++){
			        data[k][j] = copiaData[k][j];
			    }
			}			
		}		
		System.exit(0);*/
	}

	public void makeFile() {
		makeFirstLine();
		Node node;
		byte index;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < variables.size(); j++) {
				node = variables.get(j);
				if(data[i][j] == -1){
					ps.print("?");
				}else{
					ps.print(node.getStateAt(data[i][j]));
				}				
				if (j != variables.size() - 1) {
					ps.print('\t');
				} else {
					ps.println();
				}
			}
		}
		ps.close();
	}

	private void makeFirstLine() {
		Node node;
		for (int i = 0; i < variables.size(); i++) {
			node = variables.get(i);
			ps.print(node.getName());
			if (i != variables.size() - 1) {
				ps.print('\t');
			} else {
				ps.println();
			}
		}
	}

	public static void main(String[] args) {	    	    
	    try{
	        double percentagem = Double.parseDouble(args[0]);
	        new MissingFilesGenerator(percentagem);
	    }catch(NumberFormatException nfe){
	        System.out.println("Formato de entrada invalido");	        
	    }
		
	}

	private File getFile() {
		String[] nets = new String[] {"txt"};
		FileHistoryController fileController = FileHistoryController.getInstance();
		JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// adicionar FileView no FileChooser para desenhar �cones de
		// arquivos		
		chooser.addChoosableFileFilter(new SimpleFileFilter("txt"));
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
