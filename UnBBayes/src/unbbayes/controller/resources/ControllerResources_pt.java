package unbbayes.controller.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.controller. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 05/04/2002
 */

public class ControllerResources_pt extends ListResourceBundle {

    /**
	 *  Sobrescreve getContents e retorna um array, onde cada item no array é
	 *	um par de objetos. O primeiro elemento do par é uma String chave, e o
	 *	segundo é o valor associado a essa chave.
	 *
	 * @return O conteúdo dos recursos
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * Os recursos
	 */
	static final Object[][] contents =
	{	{"imageFileFilter","Imagem (*.gif)"},
		{"likelihoodName","Likelihood"},
		{"likelihoodException","Só tem zeros!"},
		{"statusEvidenceProbabilistic","Probabilidade da Evidência Total: "},
		{"statusEvidenceException","Evidências não consistentes ou underflow"},
		{"statusError","Erro!"},
		{"printLogToolTip","Imprimir o log de compilação"},
		{"previewLogToolTip","Visualizar a impressão"},
		{"okButtonLabel"," Ok "},
		{"statusTotalTime","Tempo Total: "},
		{"statusSeconds"," segundos"},
		{"stateProbabilisticName","Estado "},
		{"stateDecisionName","Ação "},
		{"stateUtilityName","Utilidade "},
		{"firstStateProbabilisticName","Estado 0"},
		{"firstStateDecisionName","Ação 0"},
		{"nodeName","Nó: "},
		{"probabilisticNodeName","C"},
		{"decisionNodeName","D"},
		{"utilityNodeName","U"},
		{"potentialTableException","Não é um número!"},
		{"copiedNodeName","Cópia do "},
		{"askTitle","Digite um rótulo para a rede"},
		{"informationText","Informação"},
		{"printException","Erro de Impressão: "},
		{"loadNetException","Erro ao Abrir a Rede"},
		{"cancelOption","Cancelar"},
		{"printerStatus","Status da Impressora"},
		{"initializingPrinter","Inicializando impressora..."},
		{"printingPage","Imprimindo página "},
		{"previewButtonLabel","Anterior"},
		{"nextButtonLabel","Próxima"},
		{"fitToPageButtonLabel","Ajustar para Página"}
	};
}