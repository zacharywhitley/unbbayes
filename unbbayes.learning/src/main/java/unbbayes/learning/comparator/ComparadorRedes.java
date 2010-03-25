 /*
 * Created on 05/10/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package unbbayes.learning.comparator;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.controller.FileHistoryController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author custodio
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ComparadorRedes {
    
    private BaseIO io;
    
    ProbabilisticNetwork redeBase;
    
    ProbabilisticNetwork redeComparacao;
    
    
     

    /**
     * 
     */
    public ComparadorRedes() {     
        File file = chooseFile(new String[] { "net" });        
        io = new NetIO();        
        /* Recupera��o da rede a partir de um arquivo .net */
        try{
            redeBase = (ProbabilisticNetwork)io.load(file);
            file = new File("C:\\eclipse-SDK-3.0-win32\\eclipse\\workspace\\UnBBayes\\examples\\Asia\\CBL\\B\\ASIA45");
            File[] files = file.listFiles();
            for(int i = 0 ; i < files.length; i++){                
                file = files[i];
                System.out.println(file.getName());
                redeComparacao = (ProbabilisticNetwork)io.load(file);
                comparar(redeBase, redeComparacao);
                System.out.println();
            }
        }catch(Throwable t){
            t.printStackTrace();            
        }
        System.exit(0);
    }
    
    private void comparar(ProbabilisticNetwork redeBase, ProbabilisticNetwork redeComparacao){
        List listaNosBase = redeBase.getEdges();
        List listaNosComparacao = redeComparacao.getEdges();
        boolean existe = false;
        int contIgual = 0;
        int contExtra = 0;
        int contFaltante = 0;
        int contInvertido = 0;
        for(int i = 0 ; i < listaNosBase.size(); i++){
            Edge arcoBase = (Edge)listaNosBase.get(i);
            String nomeOrigemBase = arcoBase.getOriginNode().getName();
            String nomeDestinoBase = arcoBase.getDestinationNode().getName();
            for(int j = 0 ; j < listaNosComparacao.size(); j++){
                Edge arcoComparacao = (Edge)listaNosComparacao.get(j);
                String nomeOrigemCom = arcoComparacao.getOriginNode().getName();
                String nomeDestinoCom = arcoComparacao.getDestinationNode().getName();
                if(nomeOrigemBase.equals(nomeOrigemCom) && nomeDestinoBase.equals(nomeDestinoCom)){
                    contIgual++;
                    existe = true;
                    break;
                }else if(nomeOrigemBase.equals(nomeDestinoCom)  && nomeDestinoBase.equals(nomeOrigemCom)){
                    contInvertido++;
                    existe = true;
                    break;
                }                   
            }
            if(!existe){
                contFaltante++;
            }else{
                existe = false;
            }
        }
        contExtra = listaNosComparacao.size() - contIgual - contInvertido;
        System.out.println("Arcos Iguais = "+ contIgual);
        System.out.println("Arcos Extras = "+ contExtra);
        System.out.println("Arcos Faltantes = "+ contFaltante);
        System.out.println("Arcos Invertidos = "+ contInvertido);        
    }
    
    private File chooseFile(String[] tipos) {
        try {
        	FileHistoryController fileController = FileHistoryController.getInstance();
            JFileChooser chooser = new JFileChooser(fileController
                    .getCurrentDirectory());
            chooser.setMultiSelectionEnabled(false);
            chooser.addChoosableFileFilter(new SimpleFileFilter(tipos, "txt"));
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
    
    public static void main(String[] args){
        new ComparadorRedes();
    }
}
