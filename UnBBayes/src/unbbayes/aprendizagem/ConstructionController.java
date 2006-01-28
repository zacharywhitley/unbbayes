/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package unbbayes.aprendizagem;

import unbbayes.controller.*;
import unbbayes.util.*;
import unbbayes.gui.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
/**
 *@author Danilo
 * 
 * This class reads the file and constructs
 * the matrix of indexes, the array of repeated
 * instances and initializes the learning process
 */

public class ConstructionController {
     	 
	public NodeList variablesVector;
	public NodeList variablesVector2;
	private NodeList variables;
	private int[] vector;
	private byte[][] matrix;
	private long caseNumber; 
	private boolean compacted;	 
	public boolean[] VariavelNumerica;

    /**
     * Starts the process of read the file, construct
     * and fill the structres
     * 
     * @param file - The file that contains the data base of
     * cases.
     * 
     * @param controller - The controller that will be 
     * called to continue the process of propagate evidences
     * 
     * @see MainController
     * 
     * @see ChooseVariablesWindow
     * 
     * @see CompactFileWindow
     * 
     * @see OrdenationWindow
     * 
     * @see OrdenationInterarionController
     * 
     * @see AlgorithmController
     * 
     * @see ProbabilisticController
     */
    
	public ConstructionController(File file){
		try{
		  InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
		  BufferedReader  br    = new BufferedReader(isr);
		  int rows = getRowCount(br);           
		  isr = new InputStreamReader(new FileInputStream(file));
		  br  = new BufferedReader(isr);
		  StreamTokenizer cols = new StreamTokenizer(br);
		  setColsConstraints(cols);
		  variablesVector = new NodeList();           
		  variables = new NodeList();                      
		  makeVariablesVector(cols);		                 
		  filterVariablesVector(rows);
		  matrix = new byte[rows][variables.size()];
	  	  IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.WAIT_CURSOR));
	  	  //ordenatevector();
		  makeMatrix(cols, rows);
		 IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		  br.close();          
		 }
		 catch(Exception e){
			String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
			JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    	
		 };		 		 		
	}
    
	public ConstructionController(File file, MainController controller){				
	    try{
           InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
           BufferedReader  br    = new BufferedReader(isr);
           int rows = getRowCount(br);           
           isr = new InputStreamReader(new FileInputStream(file));
           br  = new BufferedReader(isr);
           StreamTokenizer cols = new StreamTokenizer(br);
           setColsConstraints(cols);
           variablesVector = new NodeList();           
           variables = new NodeList();                      
           makeVariablesVector(cols);
           new ChooseVariablesWindow(variablesVector);
           new CompactFileWindow(variablesVector);               
           filterVariablesVector(rows);
           matrix = new byte[rows][variables.size()];      
           IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.WAIT_CURSOR));                
           makeMatrix(cols, rows);          
           IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
           br.close();          
	    }
	    catch(Exception e){
	    	String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
	    	JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    	
	    };		
        OrdenationWindow ordenationWindow = new OrdenationWindow(variables);        	    	    	    	    
        OrdenationInterationController ordenationController = ordenationWindow.getController();                    
        String[] pamp = ordenationController.getPamp();		
        variables = ordenationController.getVariables();				
        /*Constructs the topology of the net*/        
        Date d = new Date();
        long time = d.getTime();
        IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new AlgorithmController(variables,matrix,vector,caseNumber,pamp,compacted);
        IUnBBayes.getIUnBBayes().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));                
        Date d2 = new Date();
        long time1 = d2.getTime();
        long resul = time1 - time;
        /*Efeito de debug*/
        System.out.println("Resultado = "+ resul);                               
        /*Gives the probability of each node*/
        new ProbabilisticController(variables,matrix, vector,caseNumber,controller, compacted);                     
    }
	/**
	 * Do BAN
	 * @param classei
	 * @param inutil para diferenciar do TAN
	 * @author Gabriel Guimarães
	 * @author Patricia
	 */
	public ConstructionController(File file, MainController controller, int classei, int inutil){				
		int classex=0;
		try{
           InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
           BufferedReader  br    = new BufferedReader(isr);
           int rows = getRowCount(br);
           isr = new InputStreamReader(new FileInputStream(file));
           br  = new BufferedReader(isr);
           StreamTokenizer cols = new StreamTokenizer(br);
           setColsConstraints(cols);
           variablesVector = new NodeList();           
           variables = new NodeList();                      
           makeVariablesVector(cols);
           ChooseVariablesWindow cvw= new ChooseVariablesWindow(variablesVector,0);
           classex=cvw.classei;
           new ChooseVariablesWindow(variablesVector);
           new CompactFileWindow(variablesVector);               
           filterVariablesVector(rows);
           matrix = new byte[rows][variables.size()]; 
           makeMatrix(cols, rows);
           		  
           br.close();          
	    }
	    catch(Exception e){
	    	String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
	    	JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    	
	    };
	  	OrdenationWindow ordenationWindow = new OrdenationWindow(variables);
        OrdenationInterationController ordenationController = ordenationWindow.getController();                    
        String[] pamp = ordenationController.getPamp();
        variables = ordenationController.getVariables();				
        new AlgorithmController(variables,matrix,vector,caseNumber,pamp,compacted);
        
	    int i,j;
        j=variables.size();
        NodeList variaveis=new NodeList();
        variaveis.ensureCapacity(j+1);
        
        for(i=0;i<classex;i++)variaveis.add(variables.get(i));
        for(i=classex;i<j;i++)variaveis.add(variables.get(i));
        
          for(i=0;i<j;i++){
        	//se alguma variavel não é filha da classe então passa a ser!
        	if((i!=classex)&&(!(variaveis.get(classex).isParentOf(variaveis.get(i)))))variaveis.AddChildTo(classex,variaveis.get(i));
        	//se alguma variável tem como filho a classe--> retirar!
        	if((variaveis.get(i).isParentOf(variaveis.get(classex))))variaveis.RemoveParentFrom(classex,i);
        	//se alguma variavel não tem a classe como pai entao passa a ter
        	if((!(variaveis.get(i).isChildOf(variaveis.get(classex)))))variaveis.AddParentTo(i,variaveis.get(classex));        	
        }       
                variaveis.ClearParentsFrom(classex);
                                
        new ProbabilisticController(variaveis,matrix, vector,caseNumber,controller, compacted);                     
    }
	
	
	/**
	 * Para o TAN
	 * @param file
	 * @param controller
	 * @param classe
	 * @author Gabriel Guimarães
	 * @author Patricia
	 */
	public ConstructionController(File file, MainController controller, int classei){				
	int classex=0;
		try{
           InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
           BufferedReader  br    = new BufferedReader(isr);
           int rows = getRowCount(br);
           isr = new InputStreamReader(new FileInputStream(file));
           br  = new BufferedReader(isr);
           StreamTokenizer cols = new StreamTokenizer(br);
           setColsConstraints(cols);
           variablesVector = new NodeList();           
           variables = new NodeList();                      
           makeVariablesVector(cols);
           ChooseVariablesWindow cvw= new ChooseVariablesWindow(variablesVector,0);
           classex=cvw.classei;
           new ChooseVariablesWindow(variablesVector);
           new CompactFileWindow(variablesVector);               
           filterVariablesVector(rows);
           matrix = new byte[rows][variables.size()]; 
           makeMatrix(cols, rows);
           		  
           br.close();          
	    }
	    catch(Exception e){
	    	String msg = "Não foi possível abrir o arquivo solicitado. Verifique o formato do arquivo.";
	    	JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    	
	    };
	    new B(variables, matrix, vector,caseNumber,"MDL", "",compacted);	
        CL chowliu=new CL();
        chowliu.preparar(variables,classex,(int)caseNumber,vector,compacted,matrix);
	    variables=chowliu.variaveis;
	    int i,j;
	    j=variables.size();
        
	    //Adicionar a variavel de classe como pai de todas
	    
        for(i=0;i<j;i++){
        	//se alguma variavel não é filha da classe então passa a ser!
        	if((i!=classex)&&(!(variables.get(classex).isParentOf(variables.get(i)))))variables.AddChildTo(classex,variables.get(i));
        	//se alguma variável tem como filho a classe--> retirar!
        	if((variables.get(i).isParentOf(variables.get(classex))))variables.RemoveParentFrom(classex,i);
        	//se alguma variavel não tem a classe como pai entao passa a ter
        	if((!(variables.get(i).isChildOf(variables.get(classex)))))variables.AddParentTo(i,variables.get(classex));        	
        }       
                variables.ClearParentsFrom(classex);
                
        new ProbabilisticController(variables,matrix, vector,caseNumber,controller, compacted);                     
    }
	
	
	
	
	/**
	 * Sets the constraints of the StreamTokenizer.
	 * These constraint separates the tokens
	 * 
	 * @param cols - A streamTokenizer object
	 */
	public void setColsConstraints(StreamTokenizer cols){
        cols.wordChars('A', 'Z');
        cols.wordChars('a', '}');
        cols.wordChars('_', '_');
        cols.wordChars('-', '-');
        cols.wordChars('0', '9');
        cols.wordChars('.', '.');
        cols.quoteChar('\t');
        cols.commentChar('%');
        cols.eolIsSignificant(true);           		
	}
	
	/**
	 * Makes the variables vector. The vector is 
	 * composed by many TVariavel objects.
	 * 
	 * @param cols - A streamTokenizer object.
	 */
	private void makeVariablesVector(StreamTokenizer cols){
		int position = 0 ;
		try{
            while (cols.nextToken() != StreamTokenizer.TT_EOL){
                if(cols.sval != null){
                     variablesVector.add(new TVariavel(cols.sval, position));
                     ((TVariavel)variablesVector.get(variablesVector.size()-1)).setDescription(cols.sval);
					 ((TVariavel)variablesVector.get(variablesVector.size()-1)).setParticipa(true);
					// ((TVariavel)variablesVector.get(variablesVector.size()-1)).setNumerico(false);
                } else{
                     variablesVector.add(new TVariavel(String.valueOf(cols.nval),position));
                     ((TVariavel)variablesVector.get(variablesVector.size()-1)).setDescription(String.valueOf(cols.nval));
					 ((TVariavel)variablesVector.get(variablesVector.size()-1)).setParticipa(true);
					 //((TVariavel)variablesVector.get(variablesVector.size()-1)).setNumerico(true);
                }
                position++;
            }            
		} catch (Exception e){
			String msg = "The tokenizer process could not be completed";
			JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    						
		}				
	}
	
	/**
	 * Gets the number of rows in the file. This information
	 * is relevant because of the size of the matrix of indexes
	 * and the vector of repeated instances.
	 * 
	 * @param br - A  bufferedReader object
	 * 
	 * @return int - The numbers of rows in the file
	 */ 
	private int getRowCount(BufferedReader br){
        int rows = 0;
		try{		
            String line = br.readLine();
		    while( line != null){
                line = br.readLine();
                rows++;;
		    }
        } catch(Exception e){}
        return rows;           		
	}
	
	/**
	 * Filtes the variables that will participate of the 
	 * learning process. This variables are choose by the
	 * user of the program. Remember that the compacted variable
	 * will not participate of the leaning process.
	 * 
	 *@param rows - The number of rows of the file.
	 */
	private void filterVariablesVector(int rows){
        int nCols = 0;                
		for (int i = 0; i < variablesVector.size();i++){
            TVariavel aux = (TVariavel)variablesVector.get(i);
            if(aux.getParticipa()){
                if (!aux.getRep()){                      
                     aux.setPos(nCols);
                     variables.add(aux);
                     nCols++;
                } else{
                     vector = new int[rows];
                     compacted = true;
                }
            }
        }		
	}
	
	/**
	 * Constructs the matrix of indexes. This matrix
	 * is composed by bytes primitive types occupy fewer
	 * memory.
	 * 
	 * @param cols - A StreamTokenizer object
	 * 
	 * @param rows - The number of rows in the file that 
	 * constains the database.
	 */
	private void makeMatrix(StreamTokenizer cols, int rows){
		boolean missing = false;
	    int position = 0;
        String stateName = "";     
        TVariavel aux;
        
        try{
            while (cols.ttype != StreamTokenizer.TT_EOF && caseNumber <= rows){
                while(cols.ttype != StreamTokenizer.TT_EOL && position < variablesVector.size() && caseNumber <= rows){
                    aux = (TVariavel)variablesVector.get(position);
                    aux.setNumerico(false);
                	if (aux.getRep()){
                    	vector[(int)caseNumber] = (int)cols.nval;
                	} else if(aux.getParticipa()){
                    	if(cols.sval != null){
                         	stateName = cols.sval;
                         	                         	
                         	if(! aux.existeEstado(stateName)){
                         		if(!stateName.equals("?")){                         		
                              		aux.adicionaEstado(stateName);                              	
                         		} else {
                         			missing = true;
                         		}
                         	}
                    	} else{
                    		 stateName = String.valueOf(cols.nval);
                    		if((stateName!=null)&&(stateName!="")){
                    			if(!aux.existeEstado(stateName)){
                    		aux.setNumerico(true);
                    		aux.adicionaEstado(stateName);
                    			}
                    		}
                         	
                    	}
                    	if(! missing){
                        	matrix[(int)caseNumber][aux.getPos()] = (byte)aux.getEstadoPosicao(stateName);
                        	
                    	} else{
                    		matrix[(int)caseNumber][aux.getPos()] = -1;
                    		missing = true;                    		
                    	}
                	}
                	cols.nextToken();
                	position++;
            	}
            	caseNumber++;
            	while (cols.ttype != StreamTokenizer.TT_EOL && caseNumber < rows){
                	cols.nextToken();
            	}
            	position = 0;
            	cols.nextToken();	
            }
        } catch(Exception e ){
        	String msg = "There are errors on the matrix construction";
        	JOptionPane.showMessageDialog(null,msg,"ERROR",JOptionPane.ERROR_MESSAGE);                    	        
        };        	
        /*Tirar isso. Só pra debug*/
        System.out.println("NumeroCasos " + caseNumber);	
	}
	
	public boolean[] checavariaveis(NodeList temp){
		boolean[] result= new boolean[temp.size()];
		double teste=0;
		this.caseNumber=this.caseNumber+0*(long)teste;
		for(int i=0;i<temp.size();i++){
			try{
				result[i]=true;
				for(int j=0;j<temp.get(i).getStatesSize();j++){
				teste= Double.parseDouble(temp.get(i).getStateAt(j));				
				}
			}
			catch (Exception e){
				result[i]=false;
			}
		}
		
		return result;
	}

    public byte[][] getMatrix(){
    	return matrix;    	
    }
    
    public NodeList getVariables(){
    	return this.variables;
    }
    
    public int[] getVector(){
    	return this.vector;    	    	
    }    
    
    public long getCaseNumber(){
    	return caseNumber;    	
    }
}
