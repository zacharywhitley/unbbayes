
package unbbayes.aprendizagem;

import unbbayes.controlador.*;
import unbbayes.util.NodeList;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.StreamTokenizer;

public class ConstructionController {
	 
	private NodeList variablesVector; 
	private NodeList variables;
	private int[] vector;
	private byte[][] matrix;
	private long caseNumber; 
	private boolean compacted;
	
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
           makeMatrix(cols, rows);           
           br.close();          
	    }
	    catch(Exception e){};
        OrdenationWindow ordenationWindow = new OrdenationWindow(variables);	    	    	    	    
        OrdenationInterationController ordenationController = ordenationWindow.getController();                    
        String[] pamp = ordenationController.getPamp();
        /*Constructs the topology of the net*/
        AlgorithmController algorithmController = new AlgorithmController
                                       (variables,matrix,vector,caseNumber,pamp,compacted);                
        /*Gives the probability of each node*/
        ProbabilisticController probabilisticController = new ProbabilisticController
                                (variables,matrix, vector,caseNumber,controller, compacted);
    }
	
	private void setColsConstraints(StreamTokenizer cols){
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
	
	private void makeVariablesVector(StreamTokenizer cols){
		int position = 0 ;
		try{
            while (cols.nextToken() != StreamTokenizer.TT_EOL){
                if(cols.sval != null){
                     variablesVector.add(new TVariavel(cols.sval, position));
                } else{
                     variablesVector.add(new TVariavel(String.valueOf(cols.nval),position));
                }
                position++;
            }            
		} catch (Exception e){};				
	}
	
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
	
	private void makeMatrix(StreamTokenizer cols, int rows){
	    int position = 0;
        String stateName = "";     
        TVariavel aux;
        try{
            while (cols.ttype != StreamTokenizer.TT_EOF && caseNumber <= rows){
                while(cols.ttype != StreamTokenizer.TT_EOL && position < variablesVector.size() && caseNumber <= rows){
                    aux = (TVariavel)variablesVector.get(position);
                	if (aux.getRep()){
                    	vector[(int)caseNumber] = (int)cols.nval;
                	} else if(aux.getParticipa()){
                    	if(cols.sval != null){
                         	stateName = cols.sval;
                         	if(! aux.existeEstado(stateName)){
                              	aux.adicionaEstado(stateName);
                         	}
                    	} else{
                         	stateName = String.valueOf(cols.nval);
                         	if (! aux.existeEstado(stateName)){
                               	aux.adicionaEstado(stateName);
                         	}
                    	}
                    	matrix[(int)caseNumber][aux.getPos()] = (byte)aux.getEstadoPosicao(stateName);
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
        } catch(Exception e ){};        	
        System.out.println("NumeroCasos " + caseNumber);	
	}
}
