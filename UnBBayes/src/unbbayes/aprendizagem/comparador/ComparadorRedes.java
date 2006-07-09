 /*
 * Created on 05/10/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package unbbayes.aprendizagem.comparador;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.xml.bind.JAXBException;

import unbbayes.controller.FileController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
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
        /* Recuperação da rede a partir de um arquivo .net */
        try{
            redeBase = io.load(file);
            file = chooseFile(new String[] { "net" });
            redeComparacao = io.load(file);
            comparar(redeBase, redeComparacao);
        }catch(LoadException le){
            le.printStackTrace();            
        }catch(IOException ioe){
            ioe.printStackTrace();
        } catch (JAXBException je){
        	je.printStackTrace(); 
        }
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
            String nomeDestinoBase = arcoBase.getOriginNode().getName();
            for(int j = 0 ; j < listaNosComparacao.size(); j++){
                Edge arcoComparacao = (Edge)listaNosComparacao.get(j);
                String nomeOrigemCom = arcoComparacao.getOriginNode().getName();
                String nomeDestinoCom = arcoComparacao.getOriginNode().getName();
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
        System.out.println("Numero de Arcos da rede 1= "+ listaNosBase.size());
        System.out.println("Numero de Arcos da rede 2= "+ listaNosComparacao.size());
        System.out.println("Arcos Iguais = "+ contIgual);
        System.out.println("Arcos Extras = "+ contExtra);
        System.out.println("Arcos Faltantes = "+ contFaltante);
        System.out.println("Arcos Invertidos = "+ contInvertido);
        System.exit(0);
    }
    
    private File chooseFile(String[] tipos) {
        try {
            FileController fileController = FileController.getInstance();
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
