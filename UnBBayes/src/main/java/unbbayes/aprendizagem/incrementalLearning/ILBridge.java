package unbbayes.aprendizagem.incrementalLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.aprendizagem.ProbabilisticController;
import unbbayes.aprendizagem.incrementalLearning.controller.ILController;
import unbbayes.aprendizagem.incrementalLearning.io.ILIO;
import unbbayes.controller.MainController;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNetwork;


/**
 * Classe que recupera todos os paramêtros necessários para a utilização 
 * da classe ILController. Desacoplando a API da interface do UnBBayes essa
 * classe é especifica do framework gráfico do UnBBayes.
 *  
 * @author Danilo Custódio da Silva
 *
 */
public class ILBridge {
	
	public ILBridge(MainController controller){		
        BaseIO io = new NetIO();
        ILIO ilio = new ILIO();
        List<Object>ssList = null;
        /* Escolha do arquivo .net para a atualizaçãoo da rede*/ 
        File file = ilio.chooseFile(new String[] { "net" }, "Choose the priori net.");
        /* Recuperação da rede a partir de um arquivo .net*/
        ProbabilisticNetwork pn = ilio.getNet(file, io);        
        /*Escolher um outro arquivo, agora que contenha informções das
         statísticas suficientes*/                 
        file = ilio.chooseFile(new String[] { "obj" }, "Choose the frontier set.");
        if (file != null) {
            ssList = (ArrayList<Object>)ilio.getSuficStatistics(file);
        }
        file = ilio.chooseFile(new String[] { "txt" }, "Choose the training set.");
        ConstructionController constructionController= new ConstructionController(file,pn);        
        esperar(2000);        
        ILController ilc = new ILController(pn,ssList,constructionController.getVariables());
        esperar(2000);
        /*Gives the probability of each node*/        
        new ProbabilisticController(ilc.getListaVariaveis(),constructionController.getMatrix(),
        							constructionController.getVector(),constructionController.getCaseNumber(),
        							controller, constructionController.isCompacted());        
        //paramRecalc();
        file = ilio.getFile();
        ilio.makeNetFile(file, io, pn);
        file = ilio.getFile();
        ilio.makeContFile(ssList, file);
	}

	/**
	 * Esse método faz com que a Thread que controla essa tela espere a quantidade
	 * de milisegundos desejada.
	 * 
	 * @paraam tempo - Quantidade de milisegundos que a Thread irá esperar  
	 */
	private void esperar(long tempo) {
		try {
            Thread.sleep(tempo);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

}
